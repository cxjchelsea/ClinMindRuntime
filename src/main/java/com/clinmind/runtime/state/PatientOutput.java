package com.clinmind.runtime.state;

import java.util.List;

public record PatientOutput(
        boolean allowed,
        String content,
        OutputLevel outputLevel,
        List<String> constraintsApplied
) {
    public PatientOutput {
        constraintsApplied = constraintsApplied == null ? List.of() : List.copyOf(constraintsApplied);
    }

    public PatientOutput() {
        this(false, "", OutputLevel.O1_CONTINUE_QUESTIONING, List.of());
    }
}
