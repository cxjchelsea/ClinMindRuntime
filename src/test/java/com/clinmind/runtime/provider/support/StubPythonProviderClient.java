package com.clinmind.runtime.provider.support;

import com.clinmind.runtime.provider.ProviderCapabilitiesResult;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderHealthResult;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.ProviderStatus;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.python.PythonProviderClient;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class StubPythonProviderClient implements PythonProviderClient {

    private final boolean enabled;
    private final boolean returnValidRerank;

    public StubPythonProviderClient(boolean enabled, boolean returnValidRerank) {
        this.enabled = enabled;
        this.returnValidRerank = returnValidRerank;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ProviderHealthResult health() {
        if (!enabled) {
            return ProviderHealthResult.unavailable("PYTHON_PROVIDER_DISABLED", "disabled");
        }
        return ProviderHealthResult.up(ProviderConstants.PYTHON_AI_PROVIDER_ID, ProviderConstants.PYTHON_AI_PROVIDER_VERSION);
    }

    @Override
    public ProviderCapabilitiesResult getCapabilities() {
        if (!enabled) {
            return new ProviderCapabilitiesResult(
                    ProviderStatus.MODEL_UNAVAILABLE, null, null, List.of(), "PYTHON_PROVIDER_DISABLED", "disabled");
        }
        return new ProviderCapabilitiesResult(
                ProviderStatus.SUCCESS,
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                List.of(),
                null,
                null);
    }

    @Override
    public ProviderInvocationResult<EmbeddingResult> embed(EmbeddingRequest request) {
        return disabledResult(request.requestId(), request.runtimeId());
    }

    @Override
    public ProviderInvocationResult<RerankResult> rerank(RerankRequest request) {
        String providerCallId = IdGenerator.providerCallId();
        if (!enabled || !returnValidRerank) {
            return disabledResult(providerCallId, request.requestId(), request.runtimeId());
        }
        List<RankedItem> rankedItems = request.items().stream()
                .map(item -> new RankedItem(item.itemId(), 1, 0.9, "symptom_group_match"))
                .toList();
        if (request.items().size() > 1) {
            rankedItems = List.of(
                    new RankedItem("chunk_chest_pain_001", 1, 0.92, "symptom_group_match"),
                    new RankedItem("chunk_fever_001", 2, 0.21, "low_match"));
        }
        RerankResult result = new RerankResult(
                request.requestId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RERANK_MODEL_ID,
                ProviderConstants.RERANK_MODEL_VERSION,
                ProviderConstants.SCHEMA_VERSION,
                request.query().queryId(),
                rankedItems);
        ProviderTrace trace = new ProviderTrace(
                IdGenerator.providerTraceId(),
                providerCallId,
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RERANK_MODEL_ID,
                ProviderConstants.RERANK_MODEL_VERSION,
                Map.of("item_count", request.items().size()),
                Map.of("output_count", rankedItems.size()),
                ProviderStatus.SUCCESS,
                5L,
                false,
                ProviderValidationStatus.ACCEPTED,
                Instant.now());
        return new ProviderInvocationResult<>(
                providerCallId,
                request.requestId(),
                request.runtimeId(),
                ProviderStatus.SUCCESS,
                ProviderValidationStatus.ACCEPTED,
                false,
                null,
                null,
                List.of(),
                trace,
                result);
    }

    private ProviderInvocationResult<EmbeddingResult> disabledResult(String requestId, String runtimeId) {
        return disabledResult(IdGenerator.providerCallId(), requestId, runtimeId);
    }

    private <T> ProviderInvocationResult<T> disabledResult(
            String providerCallId, String requestId, String runtimeId) {
        ProviderTrace trace = new ProviderTrace(
                IdGenerator.providerTraceId(),
                providerCallId,
                runtimeId,
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                null,
                null,
                Map.of(),
                Map.of(),
                ProviderStatus.MODEL_UNAVAILABLE,
                0L,
                true,
                ProviderValidationStatus.DEGRADED,
                Instant.now());
        return new ProviderInvocationResult<>(
                providerCallId,
                requestId,
                runtimeId,
                ProviderStatus.MODEL_UNAVAILABLE,
                ProviderValidationStatus.DEGRADED,
                true,
                "PYTHON_PROVIDER_DISABLED",
                "disabled",
                List.of(),
                trace,
                null);
    }
}
