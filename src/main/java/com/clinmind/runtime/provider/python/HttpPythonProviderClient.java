package com.clinmind.runtime.provider.python;

import com.clinmind.runtime.config.ClinmindPythonProviderProperties;
import com.clinmind.runtime.provider.ProviderCapabilitiesResult;
import com.clinmind.runtime.provider.ProviderCapabilityDescriptor;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderHealthResult;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.ProviderStatus;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.embedding.EmbeddingItemResult;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.python.dto.PythonEmbeddingRequestDto;
import com.clinmind.runtime.provider.python.dto.PythonEmbeddingResponseDto;
import com.clinmind.runtime.provider.python.dto.PythonHealthResponse;
import com.clinmind.runtime.provider.python.dto.PythonProvidersResponse;
import com.clinmind.runtime.provider.python.dto.PythonRerankRequestDto;
import com.clinmind.runtime.provider.python.dto.PythonRerankResponseDto;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.provider.runtime.ProviderCallRecord;
import com.clinmind.runtime.provider.runtime.ProviderCallStore;
import com.clinmind.runtime.provider.validation.ProviderValidationService;
import com.clinmind.runtime.state.IdGenerator;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpPythonProviderClient implements PythonProviderClient {

    private final ClinmindPythonProviderProperties properties;
    private final RestClient restClient;
    private final ProviderValidationService validationService;
    private final ProviderCallStore providerCallStore;

    public HttpPythonProviderClient(
            ClinmindPythonProviderProperties properties,
            RestClient pythonProviderRestClient,
            ProviderValidationService validationService,
            ProviderCallStore providerCallStore) {
        this.properties = properties;
        this.restClient = pythonProviderRestClient;
        this.validationService = validationService;
        this.providerCallStore = providerCallStore;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public ProviderHealthResult health() {
        if (!properties.isEnabled()) {
            return ProviderHealthResult.unavailable(
                    "PYTHON_PROVIDER_DISABLED", "Python provider integration is disabled");
        }
        try {
            PythonHealthResponse response = restClient.get().uri("/health").retrieve().body(PythonHealthResponse.class);
            if (response == null || !"UP".equalsIgnoreCase(response.status())) {
                return ProviderHealthResult.unavailable(
                        "PYTHON_PROVIDER_UNAVAILABLE", "Python provider health check failed");
            }
            return ProviderHealthResult.up(response.providerId(), response.providerVersion());
        } catch (RestClientException ex) {
            return mapConnectionFailure(ex);
        }
    }

    @Override
    public ProviderCapabilitiesResult getCapabilities() {
        if (!properties.isEnabled()) {
            return new ProviderCapabilitiesResult(
                    ProviderStatus.MODEL_UNAVAILABLE,
                    null,
                    null,
                    List.of(),
                    "PYTHON_PROVIDER_DISABLED",
                    "Python provider integration is disabled");
        }
        try {
            PythonProvidersResponse response =
                    restClient.get().uri("/v1/providers").retrieve().body(PythonProvidersResponse.class);
            if (response == null) {
                return new ProviderCapabilitiesResult(
                        ProviderStatus.FAILED,
                        null,
                        null,
                        List.of(),
                        "PYTHON_PROVIDER_INVALID_RESPONSE",
                        "Empty providers response");
            }
            List<ProviderCapabilityDescriptor> capabilities = new ArrayList<>();
            if (response.capabilities() != null) {
                for (var capability : response.capabilities()) {
                    capabilities.add(new ProviderCapabilityDescriptor(
                            ProviderCapabilityType.valueOf(capability.capability()),
                            capability.modelId(),
                            capability.modelVersion(),
                            capability.dimension(),
                            capability.enabled()));
                }
            }
            return new ProviderCapabilitiesResult(
                    ProviderStatus.SUCCESS,
                    response.providerId(),
                    response.providerVersion(),
                    capabilities,
                    null,
                    null);
        } catch (RestClientException ex) {
            ProviderHealthResult failure = mapConnectionFailure(ex);
            return new ProviderCapabilitiesResult(
                    failure.status(),
                    null,
                    null,
                    List.of(),
                    failure.errorCode(),
                    failure.message());
        } catch (IllegalArgumentException ex) {
            return new ProviderCapabilitiesResult(
                    ProviderStatus.FAILED,
                    null,
                    null,
                    List.of(),
                    "PYTHON_PROVIDER_INVALID_CAPABILITY",
                    ex.getMessage());
        }
    }

    @Override
    public ProviderInvocationResult<EmbeddingResult> embed(EmbeddingRequest request) {
        String providerCallId = IdGenerator.providerCallId();
        if (!properties.isEnabled()) {
            return disabledInvocation(providerCallId, request.requestId(), request.runtimeId(), ProviderCapabilityType.EMBEDDING);
        }
        long started = System.currentTimeMillis();
        try {
            PythonEmbeddingRequestDto payload = toEmbeddingPayload(request);
            PythonEmbeddingResponseDto response = restClient
                    .post()
                    .uri("/v1/embeddings")
                    .body(payload)
                    .retrieve()
                    .body(PythonEmbeddingResponseDto.class);
            long latencyMs = System.currentTimeMillis() - started;
            if (response == null || !"SUCCESS".equalsIgnoreCase(response.status())) {
                return failedInvocation(
                        providerCallId,
                        request,
                        ProviderCapabilityType.EMBEDDING,
                        ProviderStatus.FAILED,
                        response == null ? "PYTHON_PROVIDER_INVALID_RESPONSE" : response.errorCode(),
                        "Embedding call failed",
                        latencyMs);
            }
            EmbeddingResult result = toEmbeddingResult(response);
            var validation = validationService.validateEmbedding(request, result);
            ProviderValidationStatus validationStatus = validation.status();
            ProviderStatus status = validationStatus == ProviderValidationStatus.REJECTED
                    ? ProviderStatus.VALIDATION_FAILED
                    : ProviderStatus.SUCCESS;
            ProviderTrace trace = buildTrace(
                    providerCallId,
                    request.runtimeId(),
                    result.providerId(),
                    result.providerVersion(),
                    result.modelId(),
                    result.modelVersion(),
                    Map.of("item_count", request.items().size()),
                    Map.of("output_count", result.items().size()),
                    status,
                    latencyMs,
                    false,
                    validationStatus);
            ProviderInvocationResult<EmbeddingResult> invocation = new ProviderInvocationResult<>(
                    providerCallId,
                    request.requestId(),
                    request.runtimeId(),
                    status,
                    validationStatus,
                    validationStatus != ProviderValidationStatus.ACCEPTED,
                    status == ProviderStatus.VALIDATION_FAILED ? "PROVIDER_SCHEMA_INVALID" : null,
                    validation.reasons().isEmpty() ? null : String.join("; ", validation.reasons()),
                    List.of(),
                    trace,
                    validationStatus == ProviderValidationStatus.ACCEPTED ? result : null);
            providerCallStore.save(toCallRecord(invocation, ProviderCapabilityType.EMBEDDING, validation.reasons()));
            return invocation;
        } catch (RestClientException ex) {
            long latencyMs = System.currentTimeMillis() - started;
            ProviderHealthResult failure = mapConnectionFailure(ex);
            ProviderInvocationResult<EmbeddingResult> invocation = failedInvocation(
                    providerCallId,
                    request,
                    ProviderCapabilityType.EMBEDDING,
                    failure.status(),
                    failure.errorCode(),
                    failure.message(),
                    latencyMs);
            providerCallStore.save(toCallRecord(invocation, ProviderCapabilityType.EMBEDDING, List.of(failure.message())));
            return invocation;
        }
    }

    @Override
    public ProviderInvocationResult<RerankResult> rerank(RerankRequest request) {
        String providerCallId = IdGenerator.providerCallId();
        if (!properties.isEnabled()) {
            return disabledInvocation(providerCallId, request.requestId(), request.runtimeId(), ProviderCapabilityType.RERANK);
        }
        long started = System.currentTimeMillis();
        try {
            PythonRerankRequestDto payload = toRerankPayload(request);
            PythonRerankResponseDto response = restClient
                    .post()
                    .uri("/v1/rerank")
                    .body(payload)
                    .retrieve()
                    .body(PythonRerankResponseDto.class);
            long latencyMs = System.currentTimeMillis() - started;
            if (response == null || !"SUCCESS".equalsIgnoreCase(response.status())) {
                return failedInvocation(
                        providerCallId,
                        request,
                        ProviderCapabilityType.RERANK,
                        ProviderStatus.FAILED,
                        response == null ? "PYTHON_PROVIDER_INVALID_RESPONSE" : response.errorCode(),
                        "Rerank call failed",
                        latencyMs);
            }
            RerankResult result = toRerankResult(response);
            var validation = validationService.validateRerank(request, result);
            ProviderValidationStatus validationStatus = validation.status();
            ProviderStatus status = validationStatus == ProviderValidationStatus.REJECTED
                    ? ProviderStatus.VALIDATION_FAILED
                    : ProviderStatus.SUCCESS;
            ProviderTrace trace = buildTrace(
                    providerCallId,
                    request.runtimeId(),
                    result.providerId(),
                    result.providerVersion(),
                    result.modelId(),
                    result.modelVersion(),
                    Map.of("item_count", request.items().size()),
                    Map.of("output_count", result.rankedItems().size()),
                    status,
                    latencyMs,
                    validationStatus != ProviderValidationStatus.ACCEPTED,
                    validationStatus);
            ProviderInvocationResult<RerankResult> invocation = new ProviderInvocationResult<>(
                    providerCallId,
                    request.requestId(),
                    request.runtimeId(),
                    status,
                    validationStatus,
                    validationStatus != ProviderValidationStatus.ACCEPTED,
                    status == ProviderStatus.VALIDATION_FAILED ? "PROVIDER_SCHEMA_INVALID" : null,
                    validation.reasons().isEmpty() ? null : String.join("; ", validation.reasons()),
                    List.of(),
                    trace,
                    validationStatus == ProviderValidationStatus.ACCEPTED ? result : null);
            providerCallStore.save(toCallRecord(invocation, ProviderCapabilityType.RERANK, validation.reasons()));
            return invocation;
        } catch (RestClientException ex) {
            long latencyMs = System.currentTimeMillis() - started;
            ProviderHealthResult failure = mapConnectionFailure(ex);
            ProviderInvocationResult<RerankResult> invocation = failedInvocation(
                    providerCallId,
                    request,
                    ProviderCapabilityType.RERANK,
                    failure.status(),
                    failure.errorCode(),
                    failure.message(),
                    latencyMs);
            providerCallStore.save(toCallRecord(invocation, ProviderCapabilityType.RERANK, List.of(failure.message())));
            return invocation;
        }
    }

    private ProviderHealthResult mapConnectionFailure(RestClientException ex) {
        if (ex instanceof ResourceAccessException resourceAccessException
                && resourceAccessException.getCause() instanceof SocketTimeoutException) {
            return ProviderHealthResult.unavailable("PYTHON_PROVIDER_TIMEOUT", "Python provider request timed out");
        }
        if (ex instanceof RestClientResponseException responseException) {
            return ProviderHealthResult.unavailable(
                    "PYTHON_PROVIDER_HTTP_" + responseException.getStatusCode().value(),
                    "Python provider returned HTTP " + responseException.getStatusCode().value());
        }
        return ProviderHealthResult.unavailable("PYTHON_PROVIDER_UNAVAILABLE", "Python provider is unavailable");
    }

    private <T> ProviderInvocationResult<T> disabledInvocation(
            String providerCallId, String requestId, String runtimeId, ProviderCapabilityType capability) {
        ProviderTrace trace = buildTrace(
                providerCallId,
                runtimeId,
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                null,
                null,
                Map.of("capability", capability.name()),
                Map.of(),
                ProviderStatus.MODEL_UNAVAILABLE,
                0L,
                true,
                ProviderValidationStatus.DEGRADED);
        ProviderInvocationResult<T> invocation = new ProviderInvocationResult<>(
                providerCallId,
                requestId,
                runtimeId,
                ProviderStatus.MODEL_UNAVAILABLE,
                ProviderValidationStatus.DEGRADED,
                true,
                "PYTHON_PROVIDER_DISABLED",
                "Python provider integration is disabled",
                List.of(),
                trace,
                null);
        providerCallStore.save(toCallRecord(invocation, capability, List.of("Python provider disabled")));
        return invocation;
    }

    private ProviderInvocationResult<EmbeddingResult> failedInvocation(
            String providerCallId,
            EmbeddingRequest request,
            ProviderCapabilityType capability,
            ProviderStatus status,
            String errorCode,
            String message,
            long latencyMs) {
        ProviderTrace trace = buildTrace(
                providerCallId,
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.EMBEDDING_MODEL_ID,
                ProviderConstants.EMBEDDING_MODEL_VERSION,
                Map.of("item_count", request.items().size()),
                Map.of(),
                status,
                latencyMs,
                true,
                ProviderValidationStatus.DEGRADED);
        return new ProviderInvocationResult<>(
                providerCallId,
                request.requestId(),
                request.runtimeId(),
                status,
                ProviderValidationStatus.DEGRADED,
                true,
                errorCode,
                message,
                List.of(),
                trace,
                null);
    }

    private ProviderInvocationResult<RerankResult> failedInvocation(
            String providerCallId,
            RerankRequest request,
            ProviderCapabilityType capability,
            ProviderStatus status,
            String errorCode,
            String message,
            long latencyMs) {
        ProviderTrace trace = buildTrace(
                providerCallId,
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RERANK_MODEL_ID,
                ProviderConstants.RERANK_MODEL_VERSION,
                Map.of("item_count", request.items().size()),
                Map.of(),
                status,
                latencyMs,
                true,
                ProviderValidationStatus.DEGRADED);
        return new ProviderInvocationResult<>(
                providerCallId,
                request.requestId(),
                request.runtimeId(),
                status,
                ProviderValidationStatus.DEGRADED,
                true,
                errorCode,
                message,
                List.of(),
                trace,
                null);
    }

    private ProviderTrace buildTrace(
            String providerCallId,
            String runtimeId,
            String providerId,
            String providerVersion,
            String modelId,
            String modelVersion,
            Map<String, Object> inputSummary,
            Map<String, Object> outputSummary,
            ProviderStatus status,
            long latencyMs,
            boolean fallbackUsed,
            ProviderValidationStatus validationStatus) {
        return new ProviderTrace(
                IdGenerator.providerTraceId(),
                providerCallId,
                runtimeId,
                providerId,
                providerVersion,
                modelId,
                modelVersion,
                inputSummary,
                outputSummary,
                status,
                latencyMs,
                fallbackUsed,
                validationStatus,
                Instant.now());
    }

    private ProviderCallRecord toCallRecord(
            ProviderInvocationResult<?> invocation,
            ProviderCapabilityType capability,
            List<String> reasons) {
        return new ProviderCallRecord(
                invocation.providerCallId(),
                invocation.runtimeId(),
                invocation.requestId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                capability,
                invocation.status(),
                invocation.validationStatus(),
                invocation.fallbackUsed(),
                invocation.errorCode(),
                reasons,
                invocation.trace(),
                Instant.now());
    }

    private PythonEmbeddingRequestDto toEmbeddingPayload(EmbeddingRequest request) {
        List<PythonEmbeddingRequestDto.PythonEmbeddingItemDto> items = request.items().stream()
                .map(item -> new PythonEmbeddingRequestDto.PythonEmbeddingItemDto(item.itemId(), item.text()))
                .toList();
        return new PythonEmbeddingRequestDto(
                request.requestId(),
                request.runtimeId(),
                request.providerId(),
                request.purpose(),
                items,
                request.schemaVersion());
    }

    private PythonRerankRequestDto toRerankPayload(RerankRequest request) {
        List<PythonRerankRequestDto.PythonRerankItemDto> items = request.items().stream()
                .map(item -> new PythonRerankRequestDto.PythonRerankItemDto(item.itemId(), item.text()))
                .toList();
        PythonRerankRequestDto.PythonRerankQueryDto query =
                new PythonRerankRequestDto.PythonRerankQueryDto(request.query().queryId(), request.query().text());
        return new PythonRerankRequestDto(
                request.requestId(),
                request.runtimeId(),
                request.providerId(),
                request.purpose(),
                query,
                items,
                request.schemaVersion());
    }

    private EmbeddingResult toEmbeddingResult(PythonEmbeddingResponseDto response) {
        List<EmbeddingItemResult> items = response.result() == null || response.result().items() == null
                ? List.of()
                : response.result().items().stream()
                        .map(item -> new EmbeddingItemResult(
                                item.itemId(),
                                item.vector(),
                                item.dimension(),
                                item.textHash(),
                                item.normalized()))
                        .toList();
        return new EmbeddingResult(
                response.requestId(),
                response.providerId(),
                response.providerVersion(),
                response.modelId(),
                response.modelVersion(),
                response.schemaVersion(),
                items);
    }

    private RerankResult toRerankResult(PythonRerankResponseDto response) {
        List<RankedItem> rankedItems = response.result() == null || response.result().rankedItems() == null
                ? List.of()
                : response.result().rankedItems().stream()
                        .map(item -> new RankedItem(item.itemId(), item.rank(), item.score(), item.reasonCode()))
                        .toList();
        return new RerankResult(
                response.requestId(),
                response.providerId(),
                response.providerVersion(),
                response.modelId(),
                response.modelVersion(),
                response.schemaVersion(),
                response.result() == null ? null : response.result().queryId(),
                rankedItems);
    }
}
