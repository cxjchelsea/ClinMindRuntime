package com.clinmind.runtime.api.dto;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ClinicianReport;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.NextAction;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.PatientProfile;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RedFlagRule;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.SymptomItem;
import com.clinmind.runtime.state.DifferentialDiagnosisBoard;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.EvidenceGraphItem;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ApiResponseMapper {

    private ApiResponseMapper() {
    }

    public static Map<String, Object> toOperationResponse(RuntimeState state) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("runtime_id", state.getRuntimeId());
        data.put("runtime_status", state.getRuntimeStatus().getValue());
        data.put("work_mode", state.getWorkMode() == null ? null : state.getWorkMode().getValue());
        data.put("risk_level", resolveRiskLevel(state.getSafetyGate()));
        data.put("entry_assessment", toEntryAssessmentMap(state.getEntryAssessment()));
        data.put("case_frame", toCaseFrameMap(state.getCaseFrame()));
        data.put("knowledge_context", toKnowledgeContextMap(state.getKnowledgeContext()));
        data.put("safety_gate", toSafetyGateMap(state.getSafetyGate()));
        data.put("differential_board", toDifferentialBoardMap(state.getDifferentialBoard()));
        data.put("evidence_graph", toEvidenceGraphMap(state.getEvidenceGraph()));
        data.put("next_action", toNextActionMap(state.getQuestionTestPolicy()));
        data.put("patient_output", toPatientOutputMap(state.getPatientOutput()));
        data.put("clinician_report", toClinicianReportMap(state.getClinicianReport()));
        applyRoleBasedVisibility(data, state);
        return data;
    }

    private static void applyRoleBasedVisibility(Map<String, Object> data, RuntimeState state) {
        RuntimeMode mode = state.getMode() == null ? RuntimeMode.PATIENT_FACING : state.getMode();
        if (mode == RuntimeMode.PATIENT_FACING) {
            data.put("clinician_report", null);
            data.put("differential_board", null);
            data.put("evidence_graph", null);
            data.put("knowledge_context", toPatientKnowledgeContextMap(state.getKnowledgeContext()));
            data.put("next_action", toPatientNextActionMap(state.getQuestionTestPolicy()));
        } else if (mode == RuntimeMode.CLINICIAN_COPILOT) {
            data.put("patient_output", null);
        }
    }

    private static Map<String, Object> toPatientKnowledgeContextMap(KnowledgeContext context) {
        if (context == null || context.symptomGroup() == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("symptom_group", context.symptomGroup());
        map.put("source_assets_count", context.sourceAssets().size());
        return map;
    }

    private static Map<String, Object> toPatientNextActionMap(QuestionTestPolicyResult policy) {
        if (policy == null || policy.nextAction() == null) {
            return null;
        }
        NextAction action = policy.nextAction();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", action.type().getValue());
        map.put("content", action.content());
        map.put("purpose", action.purpose());
        map.put("priority", action.priority());
        map.put("reason", policy.reason());
        return map;
    }

    public static Map<String, Object> toStatusResponse(RuntimeState state) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("runtime_id", state.getRuntimeId());
        data.put("runtime_status", state.getRuntimeStatus().getValue());
        data.put("work_mode", state.getWorkMode() == null ? null : state.getWorkMode().getValue());
        data.put("risk_level", resolveRiskLevelOrUnknown(state.getSafetyGate()));
        data.put("updated_at", formatInstant(state.getUpdatedAt()));
        return data;
    }

    public static Map<String, Object> toResultResponse(RuntimeState state) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("runtime_id", state.getRuntimeId());
        data.put("runtime_status", state.getRuntimeStatus().getValue());
        data.put("patient_output", toPatientOutputMap(state.getPatientOutput()));
        data.put("clinician_report", toClinicianReportMap(state.getClinicianReport()));
        data.put("decision_boundary", toDecisionBoundaryMap(state.getDecisionBoundary()));
        return data;
    }

    public static Map<String, Object> toTraceResponse(String runtimeId, List<RuntimeTrace> traces) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("runtime_id", runtimeId);
        data.put("traces", traces.stream().map(ApiResponseMapper::toTraceMap).toList());
        return data;
    }

    private static String resolveRiskLevel(SafetyGateResult safetyGate) {
        if (safetyGate == null) {
            return null;
        }
        return safetyGate.riskLevel().getValue();
    }

    private static String resolveRiskLevelOrUnknown(SafetyGateResult safetyGate) {
        if (safetyGate == null) {
            return "unknown";
        }
        return safetyGate.riskLevel().getValue();
    }

    private static Map<String, Object> toEntryAssessmentMap(EntryAssessmentResult entry) {
        if (entry == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("work_mode", entry.workMode().getValue());
        map.put("symptom_group", entry.symptomGroup());
        map.put("reason", entry.reason());
        map.put("confidence", entry.confidence());
        return map;
    }

    private static Map<String, Object> toCaseFrameMap(CaseFrame caseFrame) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("chief_complaint", caseFrame.chiefComplaint());
        map.put("missing_slots", caseFrame.missingSlots());
        map.put("symptoms", caseFrame.symptoms().stream().map(ApiResponseMapper::toSymptomMap).toList());
        map.put("patient_profile", toPatientProfileMap(caseFrame.patientProfile()));
        return map;
    }

    private static Map<String, Object> toPatientProfileMap(PatientProfile profile) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("age", profile.age());
        map.put("sex", profile.sex());
        map.put("risk_factors", profile.riskFactors());
        return map;
    }

    private static Map<String, Object> toSymptomMap(SymptomItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", item.name());
        map.put("duration", item.duration());
        map.put("severity", item.severity());
        map.put("location", item.location());
        map.put("trigger", item.trigger());
        map.put("frequency", item.frequency());
        map.put("relief", item.relief());
        return map;
    }

    private static Map<String, Object> toKnowledgeContextMap(KnowledgeContext context) {
        if (context == null || context.symptomGroup() == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("symptom_group", context.symptomGroup());
        map.put("common_diagnoses", context.commonDiagnoses().stream().map(ApiResponseMapper::toDiagnosisRefMap).toList());
        map.put("must_not_miss", context.mustNotMiss().stream().map(ApiResponseMapper::toDiagnosisRefMap).toList());
        map.put("red_flags", context.redFlags().stream().map(ApiResponseMapper::toRedFlagRuleMap).toList());
        map.put("required_questions", context.requiredQuestions());
        map.put("recommended_tests", context.recommendedTests());
        map.put("source_assets", context.sourceAssets());
        return map;
    }

    private static Map<String, Object> toDiagnosisRefMap(DiagnosisRef diagnosis) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", diagnosis.name());
        map.put("risk_level", diagnosis.riskLevel().getValue());
        return map;
    }

    private static Map<String, Object> toRedFlagRuleMap(RedFlagRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rule_id", rule.ruleId());
        map.put("symptom_group", rule.symptomGroup());
        map.put("features", rule.features());
        map.put("risk_level", rule.riskLevel().getValue());
        map.put("action", rule.action());
        map.put("patient_constraint", rule.patientConstraint());
        return map;
    }

    private static Map<String, Object> toSafetyGateMap(SafetyGateResult safetyGate) {
        if (safetyGate == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("triggered", safetyGate.triggered());
        map.put("risk_level", safetyGate.riskLevel().getValue());
        map.put("matched_rules", safetyGate.matchedRules());
        map.put("reason", safetyGate.reason());
        map.put("required_action", safetyGate.requiredAction());
        map.put("patient_output_constraint", safetyGate.patientOutputConstraint());
        map.put("fail_safe_required", safetyGate.failSafeRequired());
        return map;
    }

    private static Map<String, Object> toDifferentialBoardMap(DifferentialDiagnosisBoard board) {
        if (board == null || board.candidates().isEmpty()) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("candidates", board.candidates().stream().map(ApiResponseMapper::toDdxCandidateMap).toList());
        map.put("updated_reason", board.updatedReason());
        return map;
    }

    private static Map<String, Object> toDdxCandidateMap(DDxCandidate candidate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", candidate.name());
        map.put("status", candidate.status().getValue());
        map.put("risk_level", candidate.riskLevel().getValue());
        map.put("reason", candidate.reason());
        map.put("patient_visible", candidate.patientVisible());
        return map;
    }

    private static Map<String, Object> toEvidenceGraphMap(EvidenceGraph graph) {
        if (graph == null || graph.items().isEmpty()) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("items", graph.items().stream().map(ApiResponseMapper::toEvidenceGraphItemMap).toList());
        return map;
    }

    private static Map<String, Object> toEvidenceGraphItemMap(EvidenceGraphItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("diagnosis", item.diagnosis());
        map.put("supporting_evidence", item.supportingEvidence());
        map.put("opposing_evidence", item.opposingEvidence());
        map.put("missing_evidence", item.missingEvidence());
        map.put("conflicting_evidence", item.conflictingEvidence());
        map.put("status", item.status().getValue());
        map.put("next_questions", item.nextQuestions());
        map.put("recommended_tests", item.recommendedTests());
        return map;
    }

    private static Map<String, Object> toNextActionMap(QuestionTestPolicyResult policy) {
        if (policy == null) {
            return null;
        }
        NextAction action = policy.nextAction();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", action.type().getValue());
        map.put("content", action.content());
        map.put("purpose", action.purpose());
        map.put("target_diagnosis", action.targetDiagnosis());
        map.put("priority", action.priority());
        map.put("reason", policy.reason());
        return map;
    }

    private static Map<String, Object> toPatientOutputMap(PatientOutput output) {
        if (output == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("allowed", output.allowed());
        map.put("content", output.content());
        map.put("output_level", output.outputLevel().getValue());
        map.put("constraints_applied", output.constraintsApplied());
        return map;
    }

    private static Map<String, Object> toClinicianReportMap(ClinicianReport report) {
        if (report == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("allowed", report.allowed());
        map.put("case_summary", report.caseSummary());
        map.put("safety_summary", report.safetySummary());
        map.put("ddx_summary", report.ddxSummary().stream().map(ApiResponseMapper::toDdxCandidateMap).toList());
        map.put("evidence_summary", toEvidenceGraphMap(report.evidenceSummary()));
        map.put("recommended_questions", report.recommendedQuestions());
        map.put("recommended_tests", report.recommendedTests());
        return map;
    }

    private static Map<String, Object> toDecisionBoundaryMap(DecisionBoundaryResult boundary) {
        if (boundary == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("allowed_output_level", boundary.allowedOutputLevel().getValue());
        map.put("patient_diagnosis_label_allowed", boundary.patientDiagnosisLabelAllowed());
        map.put("clinician_ddx_allowed", boundary.clinicianDdxAllowed());
        map.put("reason", boundary.reason());
        map.put("constraints", boundary.constraints());
        return map;
    }

    private static Map<String, Object> toTraceMap(RuntimeTrace trace) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("trace_id", trace.getTraceId());
        map.put("runtime_id", trace.getRuntimeId());
        map.put("step", trace.getStep());
        map.put("input", trace.getInput());
        map.put("modules_executed", trace.getModulesExecuted());
        map.put("knowledge_used", trace.getKnowledgeUsed());
        map.put("experience_used", trace.getExperienceUsed());
        map.put("output_summary", trace.getOutputSummary());
        map.put("created_at", formatInstant(trace.getCreatedAt()));
        return map;
    }

    private static String formatInstant(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}
