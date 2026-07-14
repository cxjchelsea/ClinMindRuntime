package com.clinmind.runtime.view.source;

import com.clinmind.runtime.state.*;
import com.clinmind.runtime.storage.RuntimeStore;
import com.clinmind.runtime.view.clinician.dto.*;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.clinmind.runtime.view.patient.dto.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RuntimeStoreViewSource implements PatientViewSource, ClinicianViewSource {
    private static final int LIST_LIMIT = 100;
    private static final String DISCLAIMER = "This view summarizes stored runtime data and does not replace clinician judgment.";
    private final RuntimeStore store;

    public RuntimeStoreViewSource(RuntimeStore store) {
        this.store = store;
    }

    public List<PatientSessionSummaryDto> patientSessions() {
        return store.list(null, null, LIST_LIMIT).stream().map(this::patientSummary).toList();
    }

    public Optional<PatientRuntimeViewDto> patientRuntimeView(String id) {
        return find(id).map(this::patientView);
    }

    public Optional<PatientSafeSummaryDto> patientSafeSummary(String id) {
        return patientRuntimeView(id).map(v -> new PatientSafeSummaryDto(v.sessionId(), v.runtimeId(),
                v.safeSummary(), v.safetyNotices(), v.careNavigation(), v.disclaimer(), v.projectionStatus()));
    }

    public List<ClinicianCaseSummaryDto> clinicianCases() {
        return store.list(null, null, LIST_LIMIT).stream().map(this::clinicianSummary).toList();
    }

    public Optional<ClinicianCaseViewDto> clinicianCaseView(String id) {
        return find(id).map(this::clinicianView);
    }

    public Optional<ClinicianReportDraftViewDto> clinicianReportDraft(String id) {
        return find(id).map(this::reportDraft);
    }

    private Optional<RuntimeState> find(String id) {
        if (store.exists(id)) return Optional.of(store.get(id));
        return store.list(id, null, 1).stream().findFirst();
    }

    private PatientSessionSummaryDto patientSummary(RuntimeState s) {
        return new PatientSessionSummaryDto(s.getSessionId(), s.getRuntimeId(), status(s),
                complaint(s), risk(s), nextStep(s), text(s.getUpdatedAt()), patientProjection(s));
    }

    private ClinicianCaseSummaryDto clinicianSummary(RuntimeState s) {
        return new ClinicianCaseSummaryDto(s.getRuntimeId(), s.getRuntimeId(), status(s), risk(s),
                complaint(s), text(s.getUpdatedAt()), null, clinicianProjection(s));
    }

    private PatientRuntimeViewDto patientView(RuntimeState s) {
        List<String> missing = patientMissing(s);
        List<PatientFactSummaryDto> facts = s.getCaseFrame() == null ? List.of()
                : s.getCaseFrame().symptoms().stream().map(v -> new PatientFactSummaryDto(
                        v.name(), symptomValue(v), "Captured in RuntimeState CaseFrame")).toList();
        List<PatientQuestionDto> questions = nextQuestion(s);
        List<SafetyNoticeDto> notices = safetyNotices(s);
        return new PatientRuntimeViewDto(s.getSessionId(), s.getRuntimeId(), status(s),
                s.getPatientOutput() == null ? "Runtime output is not available yet." : s.getPatientOutput().content(),
                facts, questions, notices, careNavigation(s), List.of("view_safe_summary"), DISCLAIMER,
                missing.isEmpty() ? ProjectionStatus.COMPLETE : ProjectionStatus.PARTIAL, missing);
    }

    private ClinicianCaseViewDto clinicianView(RuntimeState s) {
        CaseFrame frame = s.getCaseFrame();
        PatientProfile profile = frame == null ? null : frame.patientProfile();
        List<DdxCandidateViewDto> ddx = s.getDifferentialBoard() == null ? List.of()
                : s.getDifferentialBoard().candidates().stream().map(v -> new DdxCandidateViewDto(
                        v.name(), text(v.status()), v.reason(), "Runtime candidate; clinician verification required")).toList();
        List<RiskSignalViewDto> risks = s.getSafetyGate() == null ? List.of()
                : List.of(new RiskSignalViewDto("safety_gate", risk(s), s.getSafetyGate().reason()));
        List<String> missing = clinicianMissing(s);
        return new ClinicianCaseViewDto(s.getRuntimeId(), s.getRuntimeId(), status(s),
                new PatientSummaryDto(ageBand(profile), profile == null ? null : profile.sex(), complaint(s), List.of()),
                new CaseFrameViewDto(complaint(s), knownContext(frame), frame == null ? List.of() : frame.missingSlots()),
                inquiryTimeline(s), ddx, evidencePanel(s), risks, clinicianSuggestions(s), reportDraft(s),
                new RuntimeBoundarySummaryDto(s.getSafetyGate() == null ? "not_available" : risk(s),
                        s.getDecisionBoundary() == null ? "not_available" : text(s.getDecisionBoundary().allowedOutputLevel()),
                        s.getDecisionBoundary() == null ? List.of() : s.getDecisionBoundary().constraints()),
                missing.isEmpty() ? ProjectionStatus.COMPLETE : ProjectionStatus.PARTIAL, missing);
    }

    private ClinicianReportDraftViewDto reportDraft(RuntimeState s) {
        ClinicianReport r = s.getClinicianReport();
        return new ClinicianReportDraftViewDto(s.getRuntimeId(), s.getRuntimeId(),
                r == null ? null : r.caseSummary(), r == null ? List.of() : r.recommendedQuestions(),
                r == null ? "Clinician report is not available yet." : r.safetySummary(),
                r != null, false, r == null ? ProjectionStatus.PARTIAL : ProjectionStatus.COMPLETE);
    }

    private List<String> patientMissing(RuntimeState s) {
        List<String> result = new ArrayList<>();
        if (s.getCaseFrame() == null) result.add("case_frame");
        if (s.getPatientOutput() == null) result.add("patient_output");
        if (s.getDecisionBoundary() == null) result.add("decision_boundary");
        if (s.getSafetyGate() == null) result.add("safety_gate");
        return List.copyOf(result);
    }

    private List<String> clinicianMissing(RuntimeState s) {
        List<String> result = new ArrayList<>();
        if (s.getCaseFrame() == null) result.add("case_frame");
        if (s.getClinicianReport() == null) result.add("clinician_report");
        if (s.getDecisionBoundary() == null) result.add("decision_boundary");
        if (s.getDifferentialBoard() == null || s.getDifferentialBoard().candidates().isEmpty()) result.add("ddx_board");
        if (evidencePanel(s).isEmpty()) result.add("evidence_panel");
        if (inquiryTimeline(s).isEmpty()) result.add("inquiry_timeline");
        if (clinicianSuggestions(s).isEmpty()) result.add("ai_suggestions");
        return List.copyOf(result);
    }

    private ProjectionStatus patientProjection(RuntimeState s) {
        return patientMissing(s).isEmpty() ? ProjectionStatus.COMPLETE : ProjectionStatus.PARTIAL;
    }

    private ProjectionStatus clinicianProjection(RuntimeState s) {
        return clinicianMissing(s).isEmpty() ? ProjectionStatus.COMPLETE : ProjectionStatus.PARTIAL;
    }
    private List<PatientQuestionDto> nextQuestion(RuntimeState s) {
        if (s.getQuestionTestPolicy() == null || s.getQuestionTestPolicy().nextAction() == null
                || s.getQuestionTestPolicy().nextAction().type() != NextActionType.ASK_QUESTION) return List.of();
        NextAction n = s.getQuestionTestPolicy().nextAction();
        return List.of(new PatientQuestionDto("runtime-next-action", n.content(), n.purpose()));
    }

    private List<SafetyNoticeDto> safetyNotices(RuntimeState s) {
        if (s.getSafetyGate() == null || !s.getSafetyGate().triggered()) return List.of();
        return List.of(new SafetyNoticeDto(risk(s).toLowerCase(), s.getSafetyGate().requiredAction()));
    }

    private List<CareNavigationDto> careNavigation(RuntimeState s) {
        List<CareNavigationDto> result = new ArrayList<>();
        if (s.getSafetyGate() != null && s.getSafetyGate().requiredAction() != null
                && !s.getSafetyGate().requiredAction().isBlank()) {
            result.add(new CareNavigationDto("Safety action", s.getSafetyGate().requiredAction()));
        }
        if (s.getDecisionBoundary() != null) {
            if (s.getDecisionBoundary().reason() != null && !s.getDecisionBoundary().reason().isBlank()) {
                result.add(new CareNavigationDto("Runtime guidance", s.getDecisionBoundary().reason()));
            }
            s.getDecisionBoundary().constraints().forEach(value ->
                    result.add(new CareNavigationDto("Safety constraint", value)));
        }
        return List.copyOf(result);
    }

    private List<InquiryTurnViewDto> inquiryTimeline(RuntimeState s) {
        List<InquiryTurnViewDto> result = new ArrayList<>();
        if (s.getInputHistory() != null) {
            s.getInputHistory().forEach(input -> result.add(new InquiryTurnViewDto(
                    "patient", input.text(), text(input.receivedAt()))));
        }
        if (s.getAgentOrchestration() != null) {
            s.getAgentOrchestration().acceptedQuestions().forEach(question -> result.add(new InquiryTurnViewDto(
                    "assistant", question.questionText(), "")));
        }
        return List.copyOf(result);
    }

    private List<EvidenceItemViewDto> evidencePanel(RuntimeState s) {
        if (s.getEvidenceRetrieval() != null && !s.getEvidenceRetrieval().acceptedCandidates().isEmpty()) {
            return s.getEvidenceRetrieval().acceptedCandidates().stream().map(candidate -> {
                var ref = candidate.evidenceRef();
                return new EvidenceItemViewDto(
                        ref == null ? candidate.candidateId() : ref.title(),
                        ref == null ? candidate.relatedDdxItem() : ref.sourceId(),
                        candidate.reasonSummary(),
                        "confidence=" + candidate.confidence());
            }).toList();
        }
        if (s.getEvidenceGraph() == null) return List.of();
        return s.getEvidenceGraph().items().stream().flatMap(item -> item.evidenceRefs().stream()
                .map(ref -> new EvidenceItemViewDto(ref.evidenceId(), ref.sourceId(),
                        ref.reasonSummary(), ref.confidence()))).toList();
    }

    private List<ClinicianSuggestionDto> clinicianSuggestions(RuntimeState s) {
        List<ClinicianSuggestionDto> result = new ArrayList<>();
        if (s.getAgentOrchestration() != null) {
            s.getAgentOrchestration().acceptedQuestions().forEach(question -> result.add(
                    new ClinicianSuggestionDto(question.questionText(), question.clinicalPurpose())));
        }
        if (result.isEmpty() && s.getClinicianReport() != null) {
            s.getClinicianReport().recommendedQuestions().forEach(question ->
                    result.add(new ClinicianSuggestionDto(question, "Runtime clinician report suggestion")));
        }
        return List.copyOf(result);
    }
    private List<String> knownContext(CaseFrame f) {
        if (f == null) return List.of();
        List<String> result = new ArrayList<>(f.pastHistory());
        result.addAll(f.medicationHistory());
        result.addAll(f.examinationResults());
        return List.copyOf(result);
    }

    private String complaint(RuntimeState s) {
        return s.getCaseFrame() == null ? null : s.getCaseFrame().chiefComplaint();
    }

    private String risk(RuntimeState s) {
        return s.getSafetyGate() == null || s.getSafetyGate().riskLevel() == null
                ? "UNKNOWN" : s.getSafetyGate().riskLevel().name();
    }

    private String nextStep(RuntimeState s) {
        return s.getQuestionTestPolicy() == null || s.getQuestionTestPolicy().nextAction() == null
                ? null : s.getQuestionTestPolicy().nextAction().content();
    }

    private String status(RuntimeState s) { return text(s.getRuntimeStatus()); }
    private String symptomValue(SymptomItem v) {
        return String.join("; ", List.of(text(v.duration()), text(v.severity()), text(v.location()),
                text(v.trigger()), text(v.frequency()), text(v.relief())).stream().filter(x -> !x.isBlank()).toList());
    }
    private String ageBand(PatientProfile p) {
        if (p == null || p.age() == null) return "unknown";
        return p.age() < 18 ? "child" : p.age() < 65 ? "adult" : "older_adult";
    }
    private String text(Object value) { return value == null ? "" : value.toString(); }
}