package com.clinmind.runtime.evidence.phase12;

public interface ClinicalEvidenceProviderAdapter {
    String providerId();

    String providerVersion();

    ClinicalEvidenceRetrievalResult retrieve(EvidenceQueryRequest request);
}
