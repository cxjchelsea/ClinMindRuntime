package com.clinmind.runtime.provider.validation;

import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderValidationResult;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.capability.ProviderCapabilityProfile;
import com.clinmind.runtime.provider.judge.JudgeRequest;
import com.clinmind.runtime.provider.judge.JudgeScoreResult;
import com.clinmind.runtime.provider.embedding.EmbeddingItemResult;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.provider.risk.RiskSignalClassificationRequest;
import com.clinmind.runtime.provider.risk.RiskSignalDraft;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProviderValidationService {

    private static final List<String> RATIONALE_FORBIDDEN_TERMS = List.of(
            "final diagnosis:", "treatment instruction:", "you have", "you should take", "确诊为", "治疗方案：", "你就是");

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

    public ProviderValidationResult validateCapabilityProfile(ProviderCapabilityProfile profile) {
        List<String> reasons = new ArrayList<>();
        if (profile == null) {
            return rejected(List.of(), List.of(), List.of("capability profile missing"));
        }
        if (blank(profile.profileId())) {
            reasons.add("profile_id missing");
        }
        if (!ProviderConstants.PYTHON_AI_PROVIDER_ID.equals(profile.providerId())) {
            reasons.add("provider_id mismatch");
        }
        if (blank(profile.providerVersion())) {
            reasons.add("provider_version missing");
        }
        if (blank(profile.modelId())) {
            reasons.add("model_id missing");
        }
        if (blank(profile.modelVersion())) {
            reasons.add("model_version missing");
        }
        if (!ProviderConstants.SCHEMA_VERSION.equals(profile.schemaVersion())) {
            reasons.add("schema_version mismatch");
        }
        if (profile.capabilityType() == null
                || (profile.capabilityType() != ProviderCapabilityType.JUDGE
                && profile.capabilityType() != ProviderCapabilityType.RISK_CLASSIFICATION)) {
            reasons.add("unsupported capability_type");
        }
        if (profile.allowedUseCases().isEmpty()) {
            reasons.add("allowed_use_cases missing");
        }
        if (profile.forbiddenUseCases().isEmpty()) {
            reasons.add("forbidden_use_cases missing");
        }
        if (profile.maxInputChars() <= 0) {
            reasons.add("max_input_chars invalid");
        }
        if (profile.patientOutputAllowed() && !profile.requiresValidation()) {
            reasons.add("patient_output_allowed requires validation");
        }
        if (!reasons.isEmpty()) {
            return rejected(List.of(), List.of(profile.profileId()), reasons);
        }
        return new ProviderValidationResult(
                ProviderValidationStatus.ACCEPTED,
                List.of(profile.profileId()),
                List.of(),
                List.of());
    }

    public ProviderValidationResult validateJudge(JudgeRequest request, JudgeScoreResult result) {
        List<String> reasons = new ArrayList<>();
        if (request == null || result == null) {
            return rejected(List.of(), List.of(), List.of("judge request/result missing"));
        }
        validateCommonProviderFields(result.requestId(), result.providerId(), result.providerVersion(),
                result.modelId(), result.modelVersion(), result.schemaVersion(), request.requestId(), reasons);
        if (!request.judgeTargetId().equals(result.judgeTargetId())) {
            reasons.add("judge_target_id mismatch");
        }
        if (outOfRange(result.overallScore())) {
            reasons.add("overall_score out of range");
        }
        if (outOfRange(result.confidence())) {
            reasons.add("confidence out of range");
        }
        for (String key : result.dimensionScores().keySet()) {
            if (!request.dimensions().contains(key)) {
                reasons.add("unexpected dimension score: " + key);
            }
        }
        for (Double score : result.dimensionScores().values()) {
            if (score == null || outOfRange(score)) {
                reasons.add("dimension score out of range");
            }
        }
        if (containsForbiddenRationale(result.rationaleSummary())) {
            reasons.add("rationale_summary contains forbidden patient-facing language");
        }
        if (!reasons.isEmpty()) {
            return rejected(List.of(), List.of(request.judgeTargetId()), reasons);
        }
        return new ProviderValidationResult(
                ProviderValidationStatus.ACCEPTED,
                List.of(request.judgeTargetId()),
                List.of(),
                List.of());
    }

    public ProviderValidationResult validateRiskSignalDraft(
            RiskSignalClassificationRequest request,
            RiskSignalDraft draft) {
        List<String> reasons = new ArrayList<>();
        if (request == null || draft == null) {
            return rejected(List.of(), List.of(), List.of("risk request/draft missing"));
        }
        validateCommonProviderFields(draft.requestId(), draft.providerId(), draft.providerVersion(),
                draft.modelId(), draft.modelVersion(), draft.schemaVersion(), request.requestId(), reasons);
        if (draft.riskLabels().isEmpty()) {
            reasons.add("risk_labels missing");
        }
        for (var label : draft.riskLabels()) {
            if (!request.allowedLabels().contains(label)) {
                reasons.add("risk_label not allowed: " + label);
            }
        }
        if (outOfRange(draft.riskScore())) {
            reasons.add("risk_score out of range");
        }
        if (outOfRange(draft.uncertainty())) {
            reasons.add("uncertainty out of range");
        }
        if (!draft.warnings().contains("draft_only_not_safety_gate_decision")) {
            reasons.add("draft safety warning missing");
        }
        if (!reasons.isEmpty()) {
            return rejected(List.of(), draft.riskLabels().stream().map(Enum::name).toList(), reasons);
        }
        return new ProviderValidationResult(
                ProviderValidationStatus.ACCEPTED,
                draft.riskLabels().stream().map(Enum::name).toList(),
                List.of(),
                List.of());
    }

    private void validateCommonProviderFields(
            String resultRequestId,
            String providerId,
            String providerVersion,
            String modelId,
            String modelVersion,
            String schemaVersion,
            String expectedRequestId,
            List<String> reasons) {
        if (!ProviderConstants.PYTHON_AI_PROVIDER_ID.equals(providerId)) {
            reasons.add("provider_id mismatch");
        }
        if (blank(providerVersion)) {
            reasons.add("provider_version missing");
        }
        if (blank(modelId)) {
            reasons.add("model_id missing");
        }
        if (blank(modelVersion)) {
            reasons.add("model_version missing");
        }
        if (!ProviderConstants.SCHEMA_VERSION.equals(schemaVersion)) {
            reasons.add("schema_version mismatch");
        }
        if (!expectedRequestId.equals(resultRequestId)) {
            reasons.add("request_id mismatch");
        }
    }

    private boolean containsForbiddenRationale(String rationale) {
        if (rationale == null) {
            return false;
        }
        String lowered = rationale.toLowerCase();
        return RATIONALE_FORBIDDEN_TERMS.stream().anyMatch(term -> lowered.contains(term.toLowerCase()));
    }

    private boolean outOfRange(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) || value < 0.0 || value > 1.0;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private ProviderValidationResult rejected(
            List<String> accepted, List<String> rejected, List<String> reasons) {
        return new ProviderValidationResult(ProviderValidationStatus.REJECTED, accepted, rejected, reasons);
    }
}
