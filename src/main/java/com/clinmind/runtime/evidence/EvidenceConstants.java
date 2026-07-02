package com.clinmind.runtime.evidence;

import java.util.Set;

public final class EvidenceConstants {

    public static final String RAG_EVIDENCE_PROVIDER_ID = "rag_evidence_provider";
    public static final String RAG_EVIDENCE_PROVIDER_VERSION = "0.7.0-p0";
    public static final String DEFAULT_CORPUS_PACKAGE_ID = "phase7-default";
    public static final String DEFAULT_CORPUS_VERSION = "phase7-default-0.1.0";
    public static final int DEFAULT_RETRIEVAL_LIMIT = 5;
    public static final int MAX_RETRIEVAL_LIMIT = 10;
    public static final int MAX_ACCEPTED_CANDIDATES = 5;
    public static final double MIN_RETRIEVAL_SCORE = 0.35;

    public static final Set<String> SUPPORTED_SYMPTOM_GROUPS = Set.of(
            "chest_pain", "fever", "abdominal_pain");

    private EvidenceConstants() {
    }
}
