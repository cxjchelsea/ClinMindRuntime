package com.clinmind.runtime.evaluation.capability;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record CapabilityProfileUpdateProposal(
        @JsonProperty("proposal_id") String proposalId,
        @JsonProperty("run_id") String runId,
        @JsonProperty("case_set_id") String caseSetId,
        @JsonProperty("case_set_version") String caseSetVersion,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("current_profile_ref") String currentProfileRef,
        @JsonProperty("current_level") String currentLevel,
        @JsonProperty("recommended_level") String recommendedLevel,
        @JsonProperty("recommended_patient_allowed_outputs") List<String> recommendedPatientAllowedOutputs,
        @JsonProperty("recommended_clinician_allowed_outputs") List<String> recommendedClinicianAllowedOutputs,
        @JsonProperty("recommended_constraints") List<String> recommendedConstraints,
        List<String> reasons,
        @JsonProperty("blocking_findings") List<String> blockingFindings,
        ProposalStatus status,
        @JsonProperty("created_at") Instant createdAt
) {
    public CapabilityProfileUpdateProposal {
        if (proposalId == null || proposalId.isBlank()) {
            throw new IllegalArgumentException("proposalId must not be blank");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId must not be blank");
        }
        if (symptomGroup == null || symptomGroup.isBlank()) {
            throw new IllegalArgumentException("symptomGroup must not be blank");
        }
        if (recommendedLevel == null || recommendedLevel.isBlank()) {
            throw new IllegalArgumentException("recommendedLevel must not be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        recommendedPatientAllowedOutputs = recommendedPatientAllowedOutputs == null
                ? List.of()
                : List.copyOf(recommendedPatientAllowedOutputs);
        recommendedClinicianAllowedOutputs = recommendedClinicianAllowedOutputs == null
                ? List.of()
                : List.copyOf(recommendedClinicianAllowedOutputs);
        recommendedConstraints = recommendedConstraints == null ? List.of() : List.copyOf(recommendedConstraints);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
        blockingFindings = blockingFindings == null ? List.of() : List.copyOf(blockingFindings);
    }
}
