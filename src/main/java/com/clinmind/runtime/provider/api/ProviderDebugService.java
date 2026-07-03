package com.clinmind.runtime.provider.api;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.provider.ProviderCapabilitiesResult;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderHealthResult;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.api.dto.EmbeddingRunRequest;
import com.clinmind.runtime.provider.api.dto.EmbeddingRunResponse;
import com.clinmind.runtime.provider.api.dto.ProviderCallSafeDto;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilitiesResponse;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilityDto;
import com.clinmind.runtime.provider.api.dto.ProviderHealthDto;
import com.clinmind.runtime.provider.api.dto.ProviderTraceDto;
import com.clinmind.runtime.provider.api.dto.RerankRunRequest;
import com.clinmind.runtime.provider.api.dto.RerankRunResponse;
import com.clinmind.runtime.provider.embedding.EmbeddingItem;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.python.PythonProviderClient;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankItem;
import com.clinmind.runtime.provider.rerank.RerankQuery;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.provider.runtime.ProviderCallRecord;
import com.clinmind.runtime.provider.runtime.ProviderCallStore;
import com.clinmind.runtime.provider.runtime.ProviderCallNotFoundException;
import com.clinmind.runtime.state.IdGenerator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProviderDebugService {

    private final PythonProviderClient pythonProviderClient;
    private final ProviderCallStore providerCallStore;
    private final AuditLogService auditLogService;

    public ProviderDebugService(
            PythonProviderClient pythonProviderClient,
            ProviderCallStore providerCallStore,
            AuditLogService auditLogService) {
        this.pythonProviderClient = pythonProviderClient;
        this.providerCallStore = providerCallStore;
        this.auditLogService = auditLogService;
    }

    public ProviderHealthDto health() {
        ProviderHealthResult health = pythonProviderClient.health();
        auditLogService.record(
                AuditActionType.QUERY_PYTHON_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                health.status().name().equals("SUCCESS") ? AuditResultStatus.SUCCESS : AuditResultStatus.FAILURE,
                Map.of("status", health.status().name()));
        return new ProviderHealthDto(
                health.status().name(),
                health.providerId(),
                health.providerVersion(),
                health.errorCode(),
                health.message());
    }

    public ProviderCapabilitiesResponse capabilities() {
        ProviderCapabilitiesResult capabilities = pythonProviderClient.getCapabilities();
        List<ProviderCapabilityDto> items = capabilities.capabilities().stream()
                .map(item -> new ProviderCapabilityDto(
                        item.capability().name(),
                        item.modelId(),
                        item.modelVersion(),
                        item.dimension(),
                        item.enabled()))
                .toList();
        auditLogService.record(
                AuditActionType.QUERY_PYTHON_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                capabilities.status().name().equals("SUCCESS")
                        ? AuditResultStatus.SUCCESS
                        : AuditResultStatus.FAILURE,
                Map.of("capability_count", items.size()));
        return new ProviderCapabilitiesResponse(
                capabilities.status().name(),
                capabilities.providerId(),
                capabilities.providerVersion(),
                items,
                capabilities.errorCode(),
                capabilities.message());
    }

    public EmbeddingRunResponse runEmbeddings(EmbeddingRunRequest request) {
        validateRuntimeId(request.runtimeId());
        if (request.items() == null || request.items().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "items is required");
        }
        List<EmbeddingItem> items = request.items().stream()
                .map(item -> new EmbeddingItem(item.itemId(), item.text()))
                .toList();
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(
                IdGenerator.providerRequestId(),
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "debug_embedding",
                items,
                ProviderConstants.SCHEMA_VERSION);
        ProviderInvocationResult<EmbeddingResult> invocation = pythonProviderClient.embed(embeddingRequest);
        auditLogService.record(
                AuditActionType.RUN_PYTHON_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                invocation.providerCallId(),
                invocation.fallbackUsed() ? AuditResultStatus.FAILURE : AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", request.runtimeId(),
                        "capability", "EMBEDDING",
                        "fallback_used", invocation.fallbackUsed()));
        List<Integer> dimensions = invocation.result() == null
                ? List.of()
                : invocation.result().items().stream()
                        .map(item -> item.dimension())
                        .toList();
        return new EmbeddingRunResponse(
                invocation.providerCallId(),
                invocation.status().name(),
                invocation.validationStatus().name(),
                invocation.fallbackUsed(),
                invocation.errorCode(),
                dimensions.size(),
                dimensions,
                toTraceDto(invocation.trace()));
    }

    public RerankRunResponse runRerank(RerankRunRequest request) {
        validateRuntimeId(request.runtimeId());
        if (request.queryText() == null || request.queryText().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "query_text is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "items is required");
        }
        List<RerankItem> items = request.items().stream()
                .map(item -> new RerankItem(item.itemId(), item.text()))
                .toList();
        RerankRequest rerankRequest = new RerankRequest(
                IdGenerator.providerRequestId(),
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "debug_rerank",
                new RerankQuery(IdGenerator.providerQueryId(), request.queryText()),
                items,
                ProviderConstants.SCHEMA_VERSION);
        ProviderInvocationResult<RerankResult> invocation = pythonProviderClient.rerank(rerankRequest);
        auditLogService.record(
                AuditActionType.RUN_PYTHON_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                invocation.providerCallId(),
                invocation.fallbackUsed() ? AuditResultStatus.FAILURE : AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", request.runtimeId(),
                        "capability", "RERANK",
                        "fallback_used", invocation.fallbackUsed()));
        List<String> rankedItemIds = invocation.result() == null
                ? List.of()
                : invocation.result().rankedItems().stream()
                        .sorted((left, right) -> Integer.compare(left.rank(), right.rank()))
                        .map(RankedItem::itemId)
                        .toList();
        return new RerankRunResponse(
                invocation.providerCallId(),
                invocation.status().name(),
                invocation.validationStatus().name(),
                invocation.fallbackUsed(),
                invocation.errorCode(),
                rankedItemIds,
                toTraceDto(invocation.trace()));
    }

    public ProviderCallSafeDto getCall(String providerCallId) {
        ProviderCallRecord record = providerCallStore
                .findById(providerCallId)
                .orElseThrow(() -> new ProviderCallNotFoundException(providerCallId));
        auditLogService.record(
                AuditActionType.QUERY_PYTHON_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                providerCallId,
                AuditResultStatus.SUCCESS,
                Map.of("capability", record.capability().name()));
        return new ProviderCallSafeDto(
                record.providerCallId(),
                record.runtimeId(),
                record.requestId(),
                record.providerId(),
                record.capability().name(),
                record.status().name(),
                record.validationStatus().name(),
                record.fallbackUsed(),
                record.errorCode(),
                record.reasons(),
                toTraceDto(record.trace()));
    }

    private void validateRuntimeId(String runtimeId) {
        if (runtimeId == null || runtimeId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "runtime_id is required");
        }
    }

    private ProviderTraceDto toTraceDto(ProviderTrace trace) {
        if (trace == null) {
            return null;
        }
        return new ProviderTraceDto(
                trace.traceId(),
                trace.providerCallId(),
                trace.runtimeId(),
                trace.providerId(),
                trace.providerVersion(),
                trace.modelId(),
                trace.modelVersion(),
                trace.status().name(),
                trace.latencyMs(),
                trace.fallbackUsed(),
                trace.validationStatus().name());
    }
}
