package com.clinmind.runtime.evidence.graph.api;

import com.clinmind.runtime.api.ApiException;
import org.springframework.http.HttpStatus;

public class GraphEvidenceAccessDeniedException extends ApiException {

    public GraphEvidenceAccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, "GRAPH_EVIDENCE_ACCESS_DENIED", message);
    }
}
