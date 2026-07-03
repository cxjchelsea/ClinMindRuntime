package com.clinmind.runtime.provider.api;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.provider.ProviderCapabilitiesResult;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderHealthResult;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.capability.ProviderCapabilityPolicy;
import com.clinmind.runtime.provider.capability.ProviderCapabilityPolicyDecision;
import com.clinmind.runtime.provider.capability.ProviderCapabilityPolicyStatus;
import com.clinmind.runtime.provider.capability.ProviderCapabilityProfile;
import com.clinmind.runtime.provider.api.dto.EmbeddingRunRequest;
import com.clinmind.runtime.provider.api.dto.EmbeddingRunResponse;
import com.clinmind.runtime.provider.api.dto.JudgeRunRequest;
import com.clinmind.runtime.provider.api.dto.JudgeRunResponse;
import com.clinmind.runtime.provider.api.dto.JudgeScoreSafeDto;
import com.clinmind.runtime.provider.api.dto.ProviderCallSafeDto;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilitiesResponse;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilityDto;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilityProfileDto;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilityProfilesDebugResponse;
import com.clinmind.runtime.provider.api.dto.ProviderHealthDto;
import com.clinmind.runtime.provider.api.dto.RiskClassifierRunRequest;
import com.clinmind.runtime.provider.api.dto.RiskClassifierRunResponse;
import com.clinmind.runtime.provider.api.dto.RiskSignalDraftSafeDto;
import com.clinmind.runtime.provider.api.dto.ProviderTraceDto;
import com.clinmind.runtime.provider.api.dto.RerankRunRequest;
import com.clinmind.runtime.provider.api.dto.RerankRunResponse;
import com.clinmind.runtime.provider.embedding.EmbeddingItem;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.judge.JudgeInputSummary;
import com.clinmind.runtime.provider.judge.JudgeRequest;
import com.clinmind.runtime.provider.judge.JudgeScoreResult;
import com.clinmind.runtime.provider.judge.JudgeTargetType;
import com.clinmind.runtime.provider.python.PythonProviderClient;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankItem;
import com.clinmind.runtime.provider.rerank.RerankQuery;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.provider.risk.RiskCaseFrameSummary;
import com.clinmind.runtime.provider.risk.RiskSignalClassificationRequest;
import com.clinmind.runtime.provider.risk.RiskSignalDraft;
import com.clinmind.runtime.provider.risk.RiskSignalLabel;
import com.clinmind.runtime.provider.runtime.ProviderCallRecord;
import com.clinmind.runtime.provider.runtime.ProviderCallStore;
import com.clinmind.runtime.provider.runtime.ProviderCallNotFoundException;
import com.clinmind.runtime.state.IdGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProviderDebugService {

    private final PythonProviderClient pythonProviderClient;
    private final ProviderCallStore providerCallStore;
    private final AuditLogService auditLogService;
    private final ProviderCapabilityPolicy capabilityPolicy;

    public ProviderDebugService(
            PythonProviderClient pythonProviderClient,
            ProviderCallStore providerCallStore,
            AuditLogService auditLogService,
            ProviderCapabilityPolicy capabilityPolicy) {
        this.pythonProviderClient = pythonProviderClient;
        this.providerCallStore = providerCallStore;
        this.auditLogService = auditLogService;
        this.capabilityPolicy = capabilityPolicy;
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

    public ProviderCapabilityProfilesDebugResponse capabilityProfiles() {
        ProviderInvocationResult<List<ProviderCapabilityProfile>> invocation =
                pythonProviderClient.getCapabilityProfiles("debug_provider_profile");
        List<ProviderCapabilityProfileDto> profiles = invocation.result() == null
                ? List.of()
                : invocation.result().stream().map(this::toProfileDto).toList();
        auditLogService.record(
                AuditActionType.QUERY_PROVIDER_CAPABILITY_PROFILE,
                AuditResourceType.PYTHON_PROVIDER,
                invocation.providerCallId(),
                invocation.fallbackUsed() ? AuditResultStatus.FAILURE : AuditResultStatus.SUCCESS,
                Map.of("profile_count", profiles.size(), "fallback_used", invocation.fallbackUsed()));
        return new ProviderCapabilityProfilesDebugResponse(
                invocation.providerCallId(),
                invocation.status().name(),
                invocation.validationStatus().name(),
                invocation.fallbackUsed(),
                invocation.errorCode(),
                profiles,
                toTraceDto(invocation.trace()));
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

    public JudgeRunResponse runJudge(JudgeRunRequest request) {
        validateRuntimeId(request.runtimeId());
        if (request.inputText() == null || request.inputText().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "input_text is required");
        }
        String useCase = request.useCase() == null || request.useCase().isBlank()
                ? "evaluation"
                : request.useCase();
        ProviderCapabilityPolicyDecision policyDecision =
                evaluatePolicy(ProviderCapabilityType.JUDGE, useCase, request.inputText().length());
        if (!policyDecision.allowed()) {
            auditPolicyRejected(ProviderCapabilityType.JUDGE, request.runtimeId(), policyDecision);
            return new JudgeRunResponse(
                    null,
                    policyDecision.status().name(),
                    "DEGRADED",
                    true,
                    "PROVIDER_CAPABILITY_POLICY_REJECTED",
                    null,
                    policyDecision.reasons(),
                    null);
        }
        JudgeRequest providerRequest = new JudgeRequest(
                IdGenerator.providerRequestId(),
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                parseJudgeTargetType(request.judgeTargetType()),
                defaultString(request.judgeTargetId(), "debug_judge_target"),
                "debug_boundary_rubric",
                "0.1.0",
                new JudgeInputSummary(request.inputText(), request.symptomGroup()),
                listOrDefault(request.dimensions(), List.of("boundary_safety", "medical_certainty")),
                listOrDefault(request.forbiddenLabels(), List.of("final_diagnosis", "treatment_instruction")),
                ProviderConstants.SCHEMA_VERSION);
        ProviderInvocationResult<JudgeScoreResult> invocation = pythonProviderClient.judge(providerRequest);
        auditLogService.record(
                AuditActionType.RUN_JUDGE_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                invocation.providerCallId(),
                invocation.fallbackUsed() ? AuditResultStatus.FAILURE : AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", request.runtimeId(),
                        "capability", "JUDGE",
                        "fallback_used", invocation.fallbackUsed()));
        JudgeScoreSafeDto result = invocation.result() == null ? null : toJudgeSafeDto(invocation.result());
        return new JudgeRunResponse(
                invocation.providerCallId(),
                invocation.status().name(),
                invocation.validationStatus().name(),
                invocation.fallbackUsed(),
                invocation.errorCode(),
                result,
                invocation.warnings(),
                toTraceDto(invocation.trace()));
    }

    public RiskClassifierRunResponse runRiskClassifier(RiskClassifierRunRequest request) {
        validateRuntimeId(request.runtimeId());
        if (request.symptomGroup() == null || request.symptomGroup().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "symptom_group is required");
        }
        String useCase = request.useCase() == null || request.useCase().isBlank()
                ? "risk_signal_draft"
                : request.useCase();
        int inputChars = String.join(" ", listOrDefault(request.knownFacts(), List.of())).length()
                + String.join(" ", listOrDefault(request.redFlagCandidates(), List.of())).length();
        ProviderCapabilityPolicyDecision policyDecision =
                evaluatePolicy(ProviderCapabilityType.RISK_CLASSIFICATION, useCase, inputChars);
        if (!policyDecision.allowed()) {
            auditPolicyRejected(ProviderCapabilityType.RISK_CLASSIFICATION, request.runtimeId(), policyDecision);
            return new RiskClassifierRunResponse(
                    null,
                    policyDecision.status().name(),
                    "DEGRADED",
                    true,
                    "PROVIDER_CAPABILITY_POLICY_REJECTED",
                    null,
                    policyDecision.reasons(),
                    null);
        }
        RiskSignalClassificationRequest providerRequest = new RiskSignalClassificationRequest(
                IdGenerator.providerRequestId(),
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                request.symptomGroup(),
                new RiskCaseFrameSummary(
                        listOrDefault(request.knownFacts(), List.of()),
                        listOrDefault(request.missingFacts(), List.of())),
                listOrDefault(request.redFlagCandidates(), List.of()),
                parseRiskLabels(request.allowedLabels()),
                ProviderConstants.SCHEMA_VERSION);
        ProviderInvocationResult<RiskSignalDraft> invocation = pythonProviderClient.classifyRisk(providerRequest);
        auditLogService.record(
                AuditActionType.RUN_RISK_CLASSIFIER_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                invocation.providerCallId(),
                invocation.fallbackUsed() ? AuditResultStatus.FAILURE : AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", request.runtimeId(),
                        "capability", "RISK_CLASSIFICATION",
                        "fallback_used", invocation.fallbackUsed()));
        RiskSignalDraftSafeDto result = invocation.result() == null ? null : toRiskSafeDto(invocation.result());
        return new RiskClassifierRunResponse(
                invocation.providerCallId(),
                invocation.status().name(),
                invocation.validationStatus().name(),
                invocation.fallbackUsed(),
                invocation.errorCode(),
                result,
                invocation.warnings(),
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

    private ProviderCapabilityPolicyDecision evaluatePolicy(
            ProviderCapabilityType capabilityType,
            String useCase,
            int inputChars) {
        ProviderInvocationResult<List<ProviderCapabilityProfile>> profiles =
                pythonProviderClient.getCapabilityProfiles("debug_policy");
        if (profiles.result() == null) {
            return new ProviderCapabilityPolicyDecision(
                    ProviderCapabilityPolicyStatus.SKIPPED,
                    null,
                    List.of("capability profiles unavailable"));
        }
        ProviderCapabilityProfile profile = profiles.result().stream()
                .filter(item -> item.capabilityType() == capabilityType)
                .findFirst()
                .orElse(null);
        return capabilityPolicy.evaluate(profile, useCase, inputChars);
    }

    private void auditPolicyRejected(
            ProviderCapabilityType capabilityType,
            String runtimeId,
            ProviderCapabilityPolicyDecision policyDecision) {
        auditLogService.record(
                AuditActionType.PROVIDER_CAPABILITY_POLICY_REJECTED,
                AuditResourceType.PYTHON_PROVIDER,
                runtimeId,
                AuditResultStatus.FAILURE,
                Map.of(
                        "capability", capabilityType.name(),
                        "policy_status", policyDecision.status().name(),
                        "reasons", policyDecision.reasons()));
    }

    private ProviderCapabilityProfileDto toProfileDto(ProviderCapabilityProfile profile) {
        return new ProviderCapabilityProfileDto(
                profile.profileId(),
                profile.providerId(),
                profile.providerVersion(),
                profile.modelId(),
                profile.modelVersion(),
                profile.capabilityType().name(),
                profile.schemaVersion(),
                profile.allowedUseCases(),
                profile.forbiddenUseCases(),
                profile.maxInputItems(),
                profile.maxInputChars(),
                profile.patientOutputAllowed(),
                profile.requiresValidation(),
                profile.fallbackStrategy(),
                profile.status().name());
    }

    private JudgeScoreSafeDto toJudgeSafeDto(JudgeScoreResult result) {
        return new JudgeScoreSafeDto(
                result.judgeTargetId(),
                result.overallScore(),
                result.dimensionScores(),
                result.violations(),
                result.confidence());
    }

    private RiskSignalDraftSafeDto toRiskSafeDto(RiskSignalDraft draft) {
        return new RiskSignalDraftSafeDto(
                draft.riskLabels().stream().map(Enum::name).toList(),
                draft.riskScore(),
                draft.matchedReasons(),
                draft.uncertainty());
    }

    private JudgeTargetType parseJudgeTargetType(String value) {
        if (value == null || value.isBlank()) {
            return JudgeTargetType.PATIENT_OUTPUT_DRAFT;
        }
        return JudgeTargetType.valueOf(value);
    }

    private List<RiskSignalLabel> parseRiskLabels(List<String> labels) {
        List<String> values = listOrDefault(labels, List.of("LOW", "MEDIUM", "HIGH", "UNKNOWN"));
        List<RiskSignalLabel> parsed = new ArrayList<>();
        for (String value : values) {
            parsed.add(RiskSignalLabel.valueOf(value));
        }
        return parsed;
    }

    private <T> List<T> listOrDefault(List<T> values, List<T> defaults) {
        return values == null || values.isEmpty() ? defaults : values;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
