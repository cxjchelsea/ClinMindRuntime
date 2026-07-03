package com.clinmind.runtime.provider.validation;

import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderValidationResult;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.embedding.EmbeddingItemResult;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProviderValidationService {

    public ProviderValidationResult validateEmbedding(EmbeddingRequest request, EmbeddingResult result) {
        List<String> reasons = new ArrayList<>();
        if (result == null) {
            return rejected(List.of(), List.of(), List.of("embedding result missing"));
        }
        if (!ProviderConstants.PYTHON_AI_PROVIDER_ID.equals(result.providerId())) {
            reasons.add("provider_id mismatch");
        }
        if (result.providerVersion() == null || result.providerVersion().isBlank()) {
            reasons.add("provider_version missing");
        }
        if (result.modelId() == null || result.modelId().isBlank()) {
            reasons.add("model_id missing");
        }
        if (result.modelVersion() == null || result.modelVersion().isBlank()) {
            reasons.add("model_version missing");
        }
        if (!ProviderConstants.SCHEMA_VERSION.equals(result.schemaVersion())) {
            reasons.add("schema_version mismatch");
        }
        if (!request.requestId().equals(result.requestId())) {
            reasons.add("request_id mismatch");
        }
        if (result.items().size() != request.items().size()) {
            reasons.add("embedding item count mismatch");
        }

        List<String> accepted = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        Set<String> expectedIds = new HashSet<>();
        request.items().forEach(item -> expectedIds.add(item.itemId()));

        for (EmbeddingItemResult item : result.items()) {
            if (!expectedIds.contains(item.itemId())) {
                reasons.add("unexpected item_id: " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            if (item.vector() == null || item.vector().isEmpty()) {
                reasons.add("vector empty for " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            if (item.dimension() != ProviderConstants.EMBEDDING_DIMENSION) {
                reasons.add("embedding dimension mismatch for " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            if (item.vector().size() != item.dimension()) {
                reasons.add("vector length mismatch for " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            boolean invalid = false;
            for (Double value : item.vector()) {
                if (value == null || value.isNaN() || value.isInfinite()) {
                    reasons.add("vector contains invalid value for " + item.itemId());
                    invalid = true;
                    break;
                }
            }
            if (invalid) {
                rejected.add(item.itemId());
                continue;
            }
            if (item.textHash() == null || item.textHash().isBlank()) {
                reasons.add("text_hash missing for " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            accepted.add(item.itemId());
        }

        if (!reasons.isEmpty()) {
            return rejected(accepted, rejected, reasons);
        }
        return new ProviderValidationResult(ProviderValidationStatus.ACCEPTED, accepted, rejected, List.of());
    }

    public ProviderValidationResult validateRerank(RerankRequest request, RerankResult result) {
        List<String> reasons = new ArrayList<>();
        if (result == null) {
            return rejected(List.of(), List.of(), List.of("rerank result missing"));
        }
        if (!ProviderConstants.PYTHON_AI_PROVIDER_ID.equals(result.providerId())) {
            reasons.add("provider_id mismatch");
        }
        if (result.providerVersion() == null || result.providerVersion().isBlank()) {
            reasons.add("provider_version missing");
        }
        if (result.modelId() == null || result.modelId().isBlank()) {
            reasons.add("model_id missing");
        }
        if (result.modelVersion() == null || result.modelVersion().isBlank()) {
            reasons.add("model_version missing");
        }
        if (!ProviderConstants.SCHEMA_VERSION.equals(result.schemaVersion())) {
            reasons.add("schema_version mismatch");
        }
        if (!request.requestId().equals(result.requestId())) {
            reasons.add("request_id mismatch");
        }
        if (!request.query().queryId().equals(result.queryId())) {
            reasons.add("query_id mismatch");
        }

        Set<String> expectedIds = new HashSet<>();
        request.items().forEach(item -> expectedIds.add(item.itemId()));
        List<String> accepted = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        Set<Integer> ranks = new HashSet<>();

        for (RankedItem item : result.rankedItems()) {
            if (!expectedIds.contains(item.itemId())) {
                reasons.add("unexpected rerank item_id: " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            if (item.score() < 0.0 || item.score() > 1.0) {
                reasons.add("rerank score out of range for " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            if (item.rank() < 1) {
                reasons.add("invalid rank for " + item.itemId());
                rejected.add(item.itemId());
                continue;
            }
            if (!ranks.add(item.rank())) {
                reasons.add("duplicate rank " + item.rank());
            }
            accepted.add(item.itemId());
        }

        if (result.rankedItems().size() != request.items().size()) {
            reasons.add("rerank item count mismatch");
        }

        if (!reasons.isEmpty()) {
            return rejected(accepted, rejected, reasons);
        }
        return new ProviderValidationResult(ProviderValidationStatus.ACCEPTED, accepted, rejected, List.of());
    }

    private ProviderValidationResult rejected(
            List<String> accepted, List<String> rejected, List<String> reasons) {
        return new ProviderValidationResult(ProviderValidationStatus.REJECTED, accepted, rejected, reasons);
    }
}
