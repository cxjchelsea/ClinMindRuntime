package com.clinmind.runtime.modelgov;

import java.time.Instant;
import java.util.List;

public record ModelRollbackPlan(
        String rollbackPlanId,
        String releaseCandidateId,
        String previousModelRegistryId,
        String previousPromptRegistryId,
        List<String> rollbackTriggerConditions,
        List<String> rollbackSteps,
        String owner,
        ModelRollbackPlanStatus status,
        Instant createdAt
) {
    public ModelRollbackPlan {
        rollbackTriggerConditions = rollbackTriggerConditions == null ? List.of() : List.copyOf(rollbackTriggerConditions);
        rollbackSteps = rollbackSteps == null ? List.of() : List.copyOf(rollbackSteps);
        status = status == null ? ModelRollbackPlanStatus.REVIEW_REQUIRED : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
