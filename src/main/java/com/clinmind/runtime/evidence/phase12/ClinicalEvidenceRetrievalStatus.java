package com.clinmind.runtime.evidence.phase12;

public enum ClinicalEvidenceRetrievalStatus {
    COMPLETE,
    REVIEW_REQUIRED,
    EMPTY,
    DEGRADED_LEXICAL_ONLY,
    DEGRADED_NO_RERANK,
    UNAVAILABLE,
    POLICY_REJECTED,
    FAILED
}
