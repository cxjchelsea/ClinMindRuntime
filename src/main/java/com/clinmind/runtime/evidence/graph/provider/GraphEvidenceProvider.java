package com.clinmind.runtime.evidence.graph.provider;

import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;

public interface GraphEvidenceProvider {

    String providerId();

    String providerVersion();

    GraphEvidenceResult expand(GraphEvidenceRequest request);
}
