package com.clinmind.runtime.view.common;

import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RoleSpecificViewSafetyPolicy {

    private static final Set<String> PATIENT_FORBIDDEN_FIELDS = Set.of(
            "ddx_candidates",
            "confidence_score",
            "risk_score_internal",
            "raw_evidence",
            "raw_tool_result",
            "trace_nodes",
            "audit_events",
            "evaluation_result",
            "candidate_governance",
            "model_prompt",
            "internal_reasoning",
            "provider_metadata",
            "tool_metadata",
            "raw_prompt",
            "raw_external_response",
            "secret",
            "api_key",
            "private_key");

    private static final Set<String> CLINICIAN_FORBIDDEN_FIELDS = Set.of(
            "raw_prompt",
            "prompt_text",
            "secret",
            "api_key",
            "private_key",
            "raw_external_response",
            "raw_tool_result",
            "internal_chain_of_thought",
            "full_rationale",
            "unredacted_patient_dialogue",
            "provider_secret_metadata",
            "tool_raw_response");

    public Set<String> patientForbiddenFields() {
        return PATIENT_FORBIDDEN_FIELDS;
    }

    public Set<String> clinicianForbiddenFields() {
        return CLINICIAN_FORBIDDEN_FIELDS;
    }

    public boolean isPatientForbidden(String key) {
        return PATIENT_FORBIDDEN_FIELDS.contains(normalize(key));
    }

    public boolean isClinicianForbidden(String key) {
        return CLINICIAN_FORBIDDEN_FIELDS.contains(normalize(key));
    }

    private static String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }
}
