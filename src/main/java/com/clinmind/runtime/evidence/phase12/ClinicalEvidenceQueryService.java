package com.clinmind.runtime.evidence.phase12;

public interface ClinicalEvidenceQueryService {
    ClinicalEvidenceRetrievalResult retrieve(EvidenceQueryRequest request);
}
