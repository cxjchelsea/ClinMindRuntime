package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record ConsoleOverviewDto(
        @JsonProperty("phase") String phase,
        @JsonProperty("runtime_count") int runtimeCount,
        @JsonProperty("provider_call_count") int providerCallCount,
        @JsonProperty("tool_invocation_count") int toolInvocationCount,
        @JsonProperty("model_governance_record_count") int modelGovernanceRecordCount,
        @JsonProperty("candidate_count") int candidateCount,
        @JsonProperty("audit_event_count") int auditEventCount,
        @JsonProperty("domain_cards") List<GovernanceDomainCardDto> domainCards,
        @JsonProperty("generated_at") Instant generatedAt
) {
    public ConsoleOverviewDto {
        domainCards = domainCards == null ? List.of() : List.copyOf(domainCards);
    }
}
