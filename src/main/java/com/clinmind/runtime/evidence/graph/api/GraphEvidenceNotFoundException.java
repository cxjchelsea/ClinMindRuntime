package com.clinmind.runtime.evidence.graph.api;

import com.clinmind.runtime.api.ApiException;
import org.springframework.http.HttpStatus;

public class GraphEvidenceNotFoundException extends ApiException {

    public GraphEvidenceNotFoundException(String graphRetrievalId) {
        super(HttpStatus.NOT_FOUND, "GRAPH_EVIDENCE_NOT_FOUND", "graph evidence run not found: " + graphRetrievalId);
    }
}
