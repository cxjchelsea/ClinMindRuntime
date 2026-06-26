package com.clinmind.runtime.asset;

import java.util.List;

public record EvidenceAssetRef(
        AssetMetadata metadata,
        String refId,
        String symptomGroup,
        String title,
        String sourceType,
        String sourceUri,
        String summary,
        List<String> linkedDiagnoses
) {
    public EvidenceAssetRef {
        linkedDiagnoses = linkedDiagnoses == null ? List.of() : List.copyOf(linkedDiagnoses);
    }
}
