package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Phase12RerankerAdapter implements EvidenceReranker {

    public static final String SCHEMA_VERSION = "provider.rerank.v1";

    private final Phase12RerankProviderPort providerPort;

    public Phase12RerankerAdapter(Phase12RerankProviderPort providerPort) {
        this.providerPort = providerPort;
    }

    @Override
    public RerankOutcome rerank(RerankCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (providerPort == null) {
            return fallback(command, RerankOutcomeStatus.DEGRADED_NO_RERANK, List.of("rerank provider disabled"), List.of());
        }
        try {
            Phase12RerankProviderPort.Phase12RerankProviderResponse response = providerPort.rerank(
                    new Phase12RerankProviderPort.Phase12RerankProviderRequest(
                            command.requestId(), command.providerId(), command.query(), command.candidates(), command.topK(), command.traceRef()));
            List<String> reasons = validate(command, response);
            if (!reasons.isEmpty()) {
                return fallback(command, RerankOutcomeStatus.PROVIDER_SCHEMA_INVALID, reasons,
                        response == null ? List.of() : response.warnings());
            }
            return success(command, response);
        } catch (RuntimeException ex) {
            return fallback(command, RerankOutcomeStatus.DEGRADED_NO_RERANK,
                    List.of(ex.getMessage() == null ? "rerank provider unavailable" : ex.getMessage()), List.of());
        }
    }

    private RerankOutcome success(
            RerankCommand command,
            Phase12RerankProviderPort.Phase12RerankProviderResponse response) {
        Map<String, RankedInput> inputById = inputById(command);
        List<RerankedCandidate> ranked = response.results().stream()
                .sorted(Comparator.comparingInt(Phase12RerankProviderPort.Phase12RerankResult::rank))
                .limit(command.topK())
                .map(result -> {
                    RankedInput input = inputById.get(result.candidateId());
                    return new RerankedCandidate(input.candidate(), input.originalRank(), result.rank(), result.score());
                })
                .toList();
        return new RerankOutcome(
                RerankOutcomeStatus.SUCCESS,
                ranked,
                false,
                response.providerId(),
                response.providerVersion(),
                response.modelId(),
                response.modelVersion(),
                response.latencyMs(),
                response.warnings(),
                List.of());
    }

    private List<String> validate(
            RerankCommand command,
            Phase12RerankProviderPort.Phase12RerankProviderResponse response) {
        List<String> reasons = new ArrayList<>();
        if (response == null) {
            return List.of("rerank response missing");
        }
        if (!SCHEMA_VERSION.equals(response.schemaVersion())) {
            reasons.add("schema_version mismatch");
        }
        if (!command.providerId().equals(response.providerId())) {
            reasons.add("provider_id mismatch");
        }
        if (blank(response.providerVersion())) {
            reasons.add("provider_version missing");
        }
        if (blank(response.modelId())) {
            reasons.add("model_id missing");
        }
        if (blank(response.modelVersion())) {
            reasons.add("model_version missing");
        }
        List<Phase12RerankProviderPort.Phase12RerankResult> results = response.results() == null ? List.of() : response.results();
        if (results.isEmpty()) {
            reasons.add("rerank results missing");
        }
        if (results.size() > command.topK()) {
            reasons.add("rerank result count exceeds top_k");
        }
        Set<String> expectedIds = inputById(command).keySet();
        Set<String> seenIds = new HashSet<>();
        Set<Integer> seenRanks = new HashSet<>();
        for (Phase12RerankProviderPort.Phase12RerankResult result : results) {
            if (blank(result.candidateId())) {
                reasons.add("candidate_id missing");
                continue;
            }
            if (!expectedIds.contains(result.candidateId())) {
                reasons.add("unexpected candidate_id: " + result.candidateId());
            }
            if (!seenIds.add(result.candidateId())) {
                reasons.add("duplicate candidate_id: " + result.candidateId());
            }
            if (Double.isNaN(result.score()) || Double.isInfinite(result.score()) || result.score() < 0.0d || result.score() > 1.0d) {
                reasons.add("rerank score out of range for " + result.candidateId());
            }
            if (result.rank() <= 0) {
                reasons.add("invalid rank for " + result.candidateId());
            }
            if (!seenRanks.add(result.rank())) {
                reasons.add("duplicate rank " + result.rank());
            }
        }
        return reasons;
    }

    private RerankOutcome fallback(RerankCommand command, RerankOutcomeStatus status, List<String> reasons, List<String> warnings) {
        List<RerankedCandidate> ranked = new ArrayList<>();
        for (int i = 0; i < Math.min(command.topK(), command.candidates().size()); i++) {
            RerankCandidate candidate = command.candidates().get(i);
            ranked.add(new RerankedCandidate(candidate, i + 1, i + 1, 0.0d));
        }
        List<String> mergedWarnings = new ArrayList<>();
        if (warnings != null) {
            mergedWarnings.addAll(warnings);
        }
        mergedWarnings.add(status == RerankOutcomeStatus.DEGRADED_NO_RERANK ? "DEGRADED_NO_RERANK" : "PROVIDER_SCHEMA_INVALID");
        return new RerankOutcome(status, ranked, true, command.providerId(), null, null, null, 0L, mergedWarnings, reasons);
    }

    private Map<String, RankedInput> inputById(RerankCommand command) {
        Map<String, RankedInput> map = new HashMap<>();
        for (int i = 0; i < command.candidates().size(); i++) {
            map.put(command.candidates().get(i).candidateId(), new RankedInput(command.candidates().get(i), i + 1));
        }
        return map;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private record RankedInput(RerankCandidate candidate, int originalRank) {}
}