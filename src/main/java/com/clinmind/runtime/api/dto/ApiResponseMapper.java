package com.clinmind.runtime.api.dto;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ClinicianReport;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.NextAction;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.PatientProfile;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.SymptomItem;
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
        data.put("next_action", toNextActionMap(state.getQuestionTestPolicy()));
        data.put("patient_output", toPatientOutputMap(state.getPatientOutput()));
        data.put("clinician_report", toClinicianReportMap(state.getClinicianReport()));
        return data;
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
