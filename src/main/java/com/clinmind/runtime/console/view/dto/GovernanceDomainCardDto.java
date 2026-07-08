package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GovernanceDomainCardDto(
        @JsonProperty("domain_id") String domainId,
        String name,
        String status,
        @JsonProperty("record_count") int recordCount,
        @JsonProperty("alert_count") int alertCount,
        @JsonProperty("latest_event_at") Instant latestEventAt
) {
}
