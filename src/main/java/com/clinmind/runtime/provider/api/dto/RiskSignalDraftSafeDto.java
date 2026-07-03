package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RiskSignalDraftSafeDto(
        @JsonProperty("risk_labels") List<String> riskLabels,
        @JsonProperty("risk_score") double riskScore,
        @JsonProperty("matched_reasons") List<String> matchedReasons,
        @JsonProperty("uncertainty") double uncertainty) {
}
