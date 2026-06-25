package com.clinmind.runtime.state;

import java.util.List;

public record DifferentialDiagnosisBoard(
        List<DDxCandidate> candidates,
        String updatedReason
) {
    public DifferentialDiagnosisBoard {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    public DifferentialDiagnosisBoard() {
        this(List.of(), null);
    }
}
