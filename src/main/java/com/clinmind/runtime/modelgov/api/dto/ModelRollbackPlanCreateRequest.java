package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.ModelRollbackPlan;
import com.clinmind.runtime.modelgov.ModelRollbackPlanStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ModelRollbackPlanCreateRequest(
        @JsonProperty("release_candidate_id") String releaseCandidateId,
        @JsonProperty("previous_model_registry_id") String previousModelRegistryId,
        @JsonProperty("previous_prompt_registry_id") String previousPromptRegistryId,
        @JsonProperty("rollback_trigger_conditions") List<String> rollbackTriggerConditions,
        @JsonProperty("rollback_steps") List<String> rollbackSteps,
        String owner
) {
    public ModelRollbackPlan toPlan() {
        return new ModelRollbackPlan(null, releaseCandidateId, previousModelRegistryId, previousPromptRegistryId,
                rollbackTriggerConditions, rollbackSteps, owner, ModelRollbackPlanStatus.REVIEW_REQUIRED, null);
    }
}
