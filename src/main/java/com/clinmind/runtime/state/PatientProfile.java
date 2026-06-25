package com.clinmind.runtime.state;

import java.util.ArrayList;
import java.util.List;

public record PatientProfile(
        Integer age,
        String sex,
        List<String> riskFactors
) {
    public PatientProfile {
        riskFactors = riskFactors == null ? List.of() : List.copyOf(riskFactors);
    }

    public PatientProfile() {
        this(null, null, List.of());
    }
}
