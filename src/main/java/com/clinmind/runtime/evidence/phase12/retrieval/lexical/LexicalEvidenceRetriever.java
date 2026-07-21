package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import java.util.List;

public interface LexicalEvidenceRetriever {
    List<LexicalRetrievalCandidate> retrieve(LexicalRetrievalRequest request);
}
