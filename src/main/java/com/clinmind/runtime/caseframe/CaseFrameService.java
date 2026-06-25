package com.clinmind.runtime.caseframe;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.PatientProfile;
import com.clinmind.runtime.state.SymptomItem;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CaseFrameService {

    private static final List<SymptomPattern> SYMPTOM_PATTERNS = List.of(
            new SymptomPattern("chest_discomfort", "胸", "chest"),
            new SymptomPattern("chest_pain", "胸痛", "chest"),
            new SymptomPattern("chest_tightness", "胸闷", "chest"),
            new SymptomPattern("fever", "发热", null),
            new SymptomPattern("fever", "发烧", null),
            new SymptomPattern("cough", "咳嗽", null),
            new SymptomPattern("headache", "头痛", null),
            new SymptomPattern("dizziness", "头晕", null),
            new SymptomPattern("sweating", "出汗", null),
            new SymptomPattern("nausea", "恶心", null));

    private static final List<String> DURATION_PATTERNS = List.of("天", "小时", "周", "月", "久", "刚开始");
    private static final List<String> SEVERITY_PATTERNS = List.of("轻微", "明显", "严重", "剧烈", "加重");
    private static final List<String> TRIGGER_PATTERNS = List.of("活动后", "走路", "休息时", "夜间");
    private static final List<String> RELIEF_PATTERNS = List.of("休息后缓解", "休息会缓解", "缓解", "好转");

    @TraceStep("CaseFrameBuilder")
    public CaseFrame buildOrUpdateCaseFrame(
            UserInput userInput,
            CaseFrame existingCaseFrame,
            Map<String, Object> basicInfo) {
        CaseFrame frame = existingCaseFrame == null ? new CaseFrame() : existingCaseFrame;
        String text = userInput.text().strip();

        PatientProfile profile = frame.patientProfile();
        if (basicInfo != null) {
            profile = applyBasicInfo(profile, basicInfo);
        }

        String chiefComplaint = frame.chiefComplaint();
        List<SymptomItem> symptoms = new ArrayList<>(frame.symptoms());
        if (!text.isEmpty()) {
            if (chiefComplaint == null) {
                chiefComplaint = extractChiefComplaint(text);
            }
            symptoms = mergeSymptoms(symptoms, extractSymptoms(text));
        }

        List<String> missingSlots = computeMissingSlots(
                new CaseFrame(
                        chiefComplaint,
                        profile,
                        symptoms,
                        frame.pastHistory(),
                        frame.medicationHistory(),
                        frame.examinationResults(),
                        frame.missingSlots(),
                        frame.conflictingSlots()));

        return new CaseFrame(
                chiefComplaint,
                profile,
                symptoms,
                frame.pastHistory(),
                frame.medicationHistory(),
                frame.examinationResults(),
                missingSlots,
                frame.conflictingSlots());
    }

    private PatientProfile applyBasicInfo(PatientProfile profile, Map<String, Object> basicInfo) {
        Integer age = profile.age();
        String sex = profile.sex();
        List<String> riskFactors = new ArrayList<>(profile.riskFactors());

        Object ageValue = basicInfo.get("age");
        if (ageValue instanceof Number number) {
            age = number.intValue();
        }
        Object sexValue = basicInfo.get("sex");
        if (sexValue != null) {
            sex = String.valueOf(sexValue);
        }
        Object riskFactorValue = basicInfo.get("risk_factors");
        if (riskFactorValue instanceof List<?> list) {
            riskFactors = list.stream().map(String::valueOf).toList();
        }
        return new PatientProfile(age, sex, riskFactors);
    }

    private String extractChiefComplaint(String text) {
        for (String separator : List.of("。", "，", ",", "；", ";", "\n")) {
            int index = text.indexOf(separator);
            if (index >= 0) {
                String first = text.substring(0, index).strip();
                if (!first.isEmpty()) {
                    return first.substring(0, Math.min(first.length(), 120));
                }
            }
        }
        return text.substring(0, Math.min(text.length(), 120));
    }

    private List<SymptomItem> extractSymptoms(String text) {
        List<SymptomItem> symptoms = new ArrayList<>();
        Set<String> seenNames = new LinkedHashSet<>();

        for (SymptomPattern pattern : SYMPTOM_PATTERNS) {
            if (text.contains(pattern.keyword()) && seenNames.add(pattern.name())) {
                symptoms.add(new SymptomItem(
                        pattern.name(),
                        extractFirstMatch(text, DURATION_PATTERNS),
                        extractFirstMatch(text, SEVERITY_PATTERNS),
                        pattern.location(),
                        extractFirstMatch(text, TRIGGER_PATTERNS),
                        null,
                        extractFirstMatch(text, RELIEF_PATTERNS)));
            }
        }

        if (symptoms.isEmpty() && looksLikeGeneralDiscomfort(text)) {
            symptoms.add(new SymptomItem(
                    "general_discomfort",
                    extractFirstMatch(text, DURATION_PATTERNS),
                    extractFirstMatch(text, SEVERITY_PATTERNS),
                    null,
                    null,
                    null,
                    null));
        }
        return symptoms;
    }

    private List<SymptomItem> mergeSymptoms(List<SymptomItem> existing, List<SymptomItem> incoming) {
        List<SymptomItem> merged = new ArrayList<>(existing);
        for (SymptomItem item : incoming) {
            int index = findSymptomIndex(merged, item.name());
            if (index >= 0) {
                merged.set(index, mergeSymptomItem(merged.get(index), item));
            } else {
                merged.add(item);
            }
        }
        return merged;
    }

    private int findSymptomIndex(List<SymptomItem> symptoms, String name) {
        for (int i = 0; i < symptoms.size(); i++) {
            if (name != null && name.equals(symptoms.get(i).name())) {
                return i;
            }
        }
        return -1;
    }

    private SymptomItem mergeSymptomItem(SymptomItem current, SymptomItem incoming) {
        return new SymptomItem(
                current.name() != null ? current.name() : incoming.name(),
                current.duration() != null ? current.duration() : incoming.duration(),
                current.severity() != null ? current.severity() : incoming.severity(),
                current.location() != null ? current.location() : incoming.location(),
                current.trigger() != null ? current.trigger() : incoming.trigger(),
                current.frequency() != null ? current.frequency() : incoming.frequency(),
                current.relief() != null ? current.relief() : incoming.relief());
    }

    private List<String> computeMissingSlots(CaseFrame frame) {
        Set<String> missing = new LinkedHashSet<>();
        if (frame.patientProfile().age() == null) {
            missing.add("age");
        }
        if (frame.patientProfile().sex() == null) {
            missing.add("sex");
        }
        if (frame.symptoms().isEmpty()) {
            missing.add("symptoms");
        } else if (frame.symptoms().stream().allMatch(item -> item.duration() == null)) {
            missing.add("symptom_duration");
        }
        if (!frame.symptoms().isEmpty() && frame.symptoms().stream().allMatch(item -> item.severity() == null)) {
            missing.add("symptom_severity");
        }
        if (frame.chiefComplaint() != null
                && !frame.chiefComplaint().contains("伴随")
                && frame.symptoms().size() <= 1) {
            missing.add("associated_symptoms");
        }
        return missing.stream().sorted().toList();
    }

    private String extractFirstMatch(String text, List<String> patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                return pattern;
            }
        }
        return null;
    }

    private boolean looksLikeGeneralDiscomfort(String text) {
        return text.contains("不舒服") || text.contains("难受") || text.contains("不适");
    }

    private record SymptomPattern(String name, String keyword, String location) {
    }
}
