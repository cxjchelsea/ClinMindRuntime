package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record AuditCenterSummaryDto(
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("count_by_action_type") Map<String, Integer> countByActionType,
        @JsonProperty("count_by_resource_type") Map<String, Integer> countByResourceType,
        @JsonProperty("count_by_result_status") Map<String, Integer> countByResultStatus,
        @JsonProperty("recent_failures") List<AuditConsoleSummaryDto> recentFailures,
        @JsonProperty("recent_review_actions") List<AuditConsoleSummaryDto> recentReviewActions
) {}
