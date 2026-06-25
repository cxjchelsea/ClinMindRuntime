package com.clinmind.runtime.knowledge;

import java.util.List;

public record CapabilityProfile(
        String symptomGroup,
        String level,
        List<String> patientAllowedOutputs,
        List<String> clinicianAllowedOutputs
) {
}
