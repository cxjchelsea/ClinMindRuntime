package com.clinmind.runtime.asset;

import java.util.List;

public record CapabilityProfileAsset(
        AssetMetadata metadata,
        String symptomGroup,
        String level,
        List<String> patientAllowedOutputs,
        List<String> clinicianAllowedOutputs,
        List<String> constraints
) {
    public CapabilityProfileAsset {
        patientAllowedOutputs = patientAllowedOutputs == null ? List.of() : List.copyOf(patientAllowedOutputs);
        clinicianAllowedOutputs = clinicianAllowedOutputs == null ? List.of() : List.copyOf(clinicianAllowedOutputs);
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
    }
}
