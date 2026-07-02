package com.clinmind.runtime.evidence.runtime;

import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class EvidenceRetrievalStore {

    private final Map<String, EvidenceRetrievalResult> byId = new ConcurrentHashMap<>();

    public void save(EvidenceRetrievalResult result) {
        if (result != null && result.retrievalId() != null) {
            byId.put(result.retrievalId(), result);
        }
    }

    public Optional<EvidenceRetrievalResult> findById(String retrievalId) {
        return Optional.ofNullable(byId.get(retrievalId));
    }
}
