package com.clinmind.runtime.asset;

import com.clinmind.runtime.state.RiskLevel;
import java.util.List;

public record RedFlagRuleAsset(
        AssetMetadata metadata,
        String ruleId,
        String symptomGroup,
        List<String> features,
        RiskLevel riskLevel,
        String action,
        String patientConstraint
) {
    public RedFlagRuleAsset {
        features = features == null ? List.of() : List.copyOf(features);
    }
}
