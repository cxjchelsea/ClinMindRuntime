package com.clinmind.runtime.evidence.phase12;

import java.util.Map;

public record EvidenceApplicabilityContext(
        String ageBand,
        String sex,
        String careSetting,
        String jurisdiction,
        String intendedAudience,
        Map<String, String> attributes
) {
    public EvidenceApplicabilityContext {
        ageBand = blankToUnknown(ageBand);
        sex = blankToUnknown(sex);
        careSetting = blankToUnknown(careSetting);
        jurisdiction = blankToUnknown(jurisdiction);
        intendedAudience = blankToUnknown(intendedAudience);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    private static String blankToUnknown(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.trim();
    }
}
