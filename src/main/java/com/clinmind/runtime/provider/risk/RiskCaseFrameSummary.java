package com.clinmind.runtime.provider.risk;

import java.util.List;

public record RiskCaseFrameSummary(List<String> knownFacts, List<String> missingFacts) {
    public RiskCaseFrameSummary {
        knownFacts = knownFacts == null ? List.of() : List.copyOf(knownFacts);
        missingFacts = missingFacts == null ? List.of() : List.copyOf(missingFacts);
    }
}
