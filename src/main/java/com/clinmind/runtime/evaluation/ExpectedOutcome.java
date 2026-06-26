package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.WorkMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ExpectedOutcome(
        @JsonProperty("work_mode") WorkMode workMode,
        @JsonProperty("runtime_status_any_of") List<RuntimeStatus> runtimeStatusAnyOf,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("safety_gate_triggered") Boolean safetyGateTriggered,
        @JsonProperty("expected_matched_rules") List<String> expectedMatchedRules,
        @JsonProperty("expected_ddx_contains") List<String> expectedDdxContains,
        @JsonProperty("expected_ddx_not_contains") List<String> expectedDdxNotContains,
        @JsonProperty("expected_next_action_types") List<NextActionType> expectedNextActionTypes,
        @JsonProperty("required_patient_phrases") List<String> requiredPatientPhrases,
        @JsonProperty("forbidden_patient_phrases") List<String> forbiddenPatientPhrases,
        @JsonProperty("forbidden_patient_fields") List<String> forbiddenPatientFields,
        @JsonProperty("required_clinician_fields") List<String> requiredClinicianFields,
        @JsonProperty("required_trace_modules") List<String> requiredTraceModules,
        @JsonProperty("forbidden_trace_modules_after_continue") List<String> forbiddenTraceModulesAfterContinue,
        @JsonProperty("required_asset_trace") Boolean requiredAssetTrace
) {
    public ExpectedOutcome {
        runtimeStatusAnyOf = runtimeStatusAnyOf == null ? List.of() : List.copyOf(runtimeStatusAnyOf);
        expectedMatchedRules = expectedMatchedRules == null ? List.of() : List.copyOf(expectedMatchedRules);
        expectedDdxContains = expectedDdxContains == null ? List.of() : List.copyOf(expectedDdxContains);
        expectedDdxNotContains = expectedDdxNotContains == null ? List.of() : List.copyOf(expectedDdxNotContains);
        expectedNextActionTypes = expectedNextActionTypes == null ? List.of() : List.copyOf(expectedNextActionTypes);
        requiredPatientPhrases = requiredPatientPhrases == null ? List.of() : List.copyOf(requiredPatientPhrases);
        forbiddenPatientPhrases = forbiddenPatientPhrases == null ? List.of() : List.copyOf(forbiddenPatientPhrases);
        forbiddenPatientFields = forbiddenPatientFields == null ? List.of() : List.copyOf(forbiddenPatientFields);
        requiredClinicianFields = requiredClinicianFields == null ? List.of() : List.copyOf(requiredClinicianFields);
        requiredTraceModules = requiredTraceModules == null ? List.of() : List.copyOf(requiredTraceModules);
        forbiddenTraceModulesAfterContinue =
                forbiddenTraceModulesAfterContinue == null ? List.of() : List.copyOf(forbiddenTraceModulesAfterContinue);
    }
}
