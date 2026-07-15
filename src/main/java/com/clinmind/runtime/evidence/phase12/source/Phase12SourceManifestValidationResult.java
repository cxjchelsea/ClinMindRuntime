package com.clinmind.runtime.evidence.phase12.source;

import java.util.List;

public record Phase12SourceManifestValidationResult(
        boolean valid,
        List<String> errors
) {
    public Phase12SourceManifestValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
