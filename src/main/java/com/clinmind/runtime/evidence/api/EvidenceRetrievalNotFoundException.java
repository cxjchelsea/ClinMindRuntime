package com.clinmind.runtime.evidence.api;

import com.clinmind.runtime.api.ApiException;
import org.springframework.http.HttpStatus;

public class EvidenceRetrievalNotFoundException extends ApiException {

    public EvidenceRetrievalNotFoundException(String retrievalId) {
        super(HttpStatus.NOT_FOUND, "EVIDENCE_RETRIEVAL_NOT_FOUND", "retrieval not found: " + retrievalId);
    }
}
