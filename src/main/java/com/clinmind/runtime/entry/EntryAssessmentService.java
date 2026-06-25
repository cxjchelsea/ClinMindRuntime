package com.clinmind.runtime.entry;

import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.TraceStep;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class EntryAssessmentService {

    private static final List<String> WELLNESS_KEYWORDS = List.of(
            "养生", "保健", "减肥", "健康饮食", "怎么锻炼", "运动建议", "睡眠建议");
    private static final List<String> UNSUPPORTED_KEYWORDS = List.of(
            "写代码", "编程", "股票", "天气怎么样", "帮我翻译", "python", "Python");
    private static final List<String> EMERGENCY_KEYWORDS = List.of(
            "呼吸困难", "意识不清", "大量出血", "快不行了", "喘不过气", "昏迷");
    private static final List<String> CLINICAL_KEYWORDS = List.of(
            "痛", "疼", "闷", "热", "发烧", "发热", "咳嗽", "头晕", "恶心", "呕吐", "腹泻", "不舒服", "症状", "难受");
    private static final List<String> CHEST_SYMPTOM_KEYWORDS = List.of("胸", "心口", "胸口", "胸闷", "胸痛");
    private static final List<String> FEVER_SYMPTOM_KEYWORDS = List.of("发热", "发烧", "体温", "高烧");
    private static final List<String> EMERGENCY_FEATURE_KEYWORDS = List.of(
            "出汗", "出冷汗", "加重", "放射", "濒死", "活动后加重");
    private static final Pattern UNRECOGNIZABLE_INPUT = Pattern.compile("[\\W\\d_]+");

    @TraceStep("EntryAssessment")
    public EntryAssessmentResult assessEntry(UserInput input, Map<String, Object> basicInfo) {
        String text = input.text().strip();
        if (text.isEmpty()) {
            return new EntryAssessmentResult(WorkMode.UNSUPPORTED, null, "empty input", 1.0);
        }
        if (isUnsupportedTopic(text)) {
            return new EntryAssessmentResult(WorkMode.UNSUPPORTED, null, "unsupported topic", 0.9);
        }
        if (containsAny(text, EMERGENCY_KEYWORDS) || looksLikeEmergencyHint(text)) {
            return new EntryAssessmentResult(
                    WorkMode.EMERGENCY_HINT,
                    detectSymptomGroup(text),
                    "possible emergency features detected",
                    0.85);
        }
        if (containsAny(text, WELLNESS_KEYWORDS) && !hasClinicalSignal(text)) {
            return new EntryAssessmentResult(WorkMode.WELLNESS_MODE, null, "wellness consultation", 0.8);
        }
        if (hasClinicalSignal(text)) {
            return new EntryAssessmentResult(
                    WorkMode.CLINICAL_MODE,
                    detectSymptomGroup(text),
                    "clinical symptoms detected",
                    0.8);
        }
        if (UNRECOGNIZABLE_INPUT.matcher(text).matches()) {
            return new EntryAssessmentResult(WorkMode.UNSUPPORTED, null, "unrecognizable input", 0.7);
        }
        return new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE,
                detectSymptomGroup(text),
                "default clinical clarification",
                0.5);
    }

    private boolean isUnsupportedTopic(String text) {
        String lowered = text.toLowerCase();
        for (String keyword : UNSUPPORTED_KEYWORDS) {
            if (lowered.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return text.contains("代码") && (text.contains("写") || text.contains("编程"));
    }

    private boolean hasClinicalSignal(String text) {
        return containsAny(text, CLINICAL_KEYWORDS);
    }

    private boolean looksLikeEmergencyHint(String text) {
        boolean hasChest = containsAny(text, CHEST_SYMPTOM_KEYWORDS);
        boolean hasFeature = containsAny(text, EMERGENCY_FEATURE_KEYWORDS);
        return hasChest && hasFeature;
    }

    private String detectSymptomGroup(String text) {
        if (containsAny(text, CHEST_SYMPTOM_KEYWORDS)) {
            return "chest_pain";
        }
        if (containsAny(text, FEVER_SYMPTOM_KEYWORDS)) {
            return "fever";
        }
        return null;
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
