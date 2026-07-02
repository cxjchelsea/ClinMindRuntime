package com.clinmind.runtime.evidence.rag;

import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;

public interface EvidenceProvider {

    String providerId();

    String providerVersion();

    EvidenceRetrievalResult retrieve(EvidenceRetrievalRequest request);
}
