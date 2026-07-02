package com.clinmind.runtime.evidence.api;

import com.clinmind.runtime.api.ApiException;
import org.springframework.http.HttpStatus;

public class EvidenceAccessDeniedException extends ApiException {

    public EvidenceAccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, "EVIDENCE_ACCESS_DENIED", message);
    }
}
