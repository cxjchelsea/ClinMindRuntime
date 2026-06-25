package com.clinmind.runtime.state;

import java.util.List;

public record DecisionBoundaryResult(
        OutputLevel allowedOutputLevel,
        boolean patientDiagnosisLabelAllowed,
        boolean clinicianDdxAllowed,
        String reason,
        List<String> constraints
) {
    public DecisionBoundaryResult {
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
    }
}
