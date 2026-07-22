package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Phase12RerankerAdapterTest {

    @Test
    void appliesProviderRanksByCandidateIdWithoutChangingCandidatePayload() {
        RerankCommand command = command();
        Phase12RerankerAdapter adapter = new Phase12RerankerAdapter(request -> new Phase12RerankProviderPort.Phase12RerankProviderResponse(
                Phase12RerankerAdapter.SCHEMA_VERSION,
                "phase12-reranker",
                "0.8.1-p1",
                "mock_reranker_model",
                "0.1.0",
                List.of(
                        new Phase12RerankProviderPort.Phase12RerankResult("candidate_b", 0.91d, 1),
                        new Phase12RerankProviderPort.Phase12RerankResult("candidate_a", 0.41d, 2)),
                12L,
                List.of("deterministic_test_double_not_real_cross_encoder_reranker")));

        RerankOutcome outcome = adapter.rerank(command);

        assertThat(outcome.status()).isEqualTo(RerankOutcomeStatus.SUCCESS);
        assertThat(outcome.fallbackUsed()).isFalse();
        assertThat(outcome.rankedCandidates()).extracting(item -> item.candidate().candidateId())
                .containsExactly("candidate_b", "candidate_a");
        assertThat(outcome.rankedCandidates().get(0).candidate().text()).isEqualTo("high risk chest pain guideline");
        assertThat(outcome.rankedCandidates().get(0).candidate().metadata()).containsEntry("version_id", "ver_b");
        assertThat(outcome.rankedCandidates().get(0).originalRank()).isEqualTo(2);
        assertThat(outcome.rankedCandidates().get(0).rerankScore()).isEqualTo(0.91d);
        assertThat(outcome.warnings()).contains("deterministic_test_double_not_real_cross_encoder_reranker");
    }

    @Test
    void rejectsUnexpectedCandidateIdAndFallsBackToOriginalOrder() {
        RerankCommand command = command();
        Phase12RerankerAdapter adapter = new Phase12RerankerAdapter(request -> new Phase12RerankProviderPort.Phase12RerankProviderResponse(
                "wrong.schema",
                "phase12-reranker",
                "0.8.1-p1",
                "mock_reranker_model",
                "0.1.0",
                List.of(new Phase12RerankProviderPort.Phase12RerankResult("candidate_x", 0.9d, 1)),
                3L,
                List.of()));

        RerankOutcome outcome = adapter.rerank(command);

        assertThat(outcome.status()).isEqualTo(RerankOutcomeStatus.PROVIDER_SCHEMA_INVALID);
        assertThat(outcome.fallbackUsed()).isTrue();
        assertThat(outcome.validationReasons()).contains("schema_version mismatch", "unexpected candidate_id: candidate_x");
        assertThat(outcome.rankedCandidates()).extracting(item -> item.candidate().candidateId())
                .containsExactly("candidate_a", "candidate_b");
    }

    @Test
    void degradesToOriginalOrderWhenProviderUnavailable() {
        RerankCommand command = command();
        Phase12RerankerAdapter adapter = new Phase12RerankerAdapter(request -> {
            throw new RuntimeException("provider timeout");
        });

        RerankOutcome outcome = adapter.rerank(command);

        assertThat(outcome.status()).isEqualTo(RerankOutcomeStatus.DEGRADED_NO_RERANK);
        assertThat(outcome.fallbackUsed()).isTrue();
        assertThat(outcome.validationReasons()).containsExactly("provider timeout");
        assertThat(outcome.rankedCandidates()).extracting(item -> item.candidate().candidateId())
                .containsExactly("candidate_a", "candidate_b");
    }

    @Test
    void disabledAdapterUsesNoRerankFallback() {
        RerankOutcome outcome = new Phase12RerankerAdapter(null).rerank(command());

        assertThat(outcome.status()).isEqualTo(RerankOutcomeStatus.DEGRADED_NO_RERANK);
        assertThat(outcome.warnings()).contains("DEGRADED_NO_RERANK");
        assertThat(outcome.rankedCandidates()).extracting(item -> item.candidate().candidateId())
                .containsExactly("candidate_a", "candidate_b");
    }

    private RerankCommand command() {
        return new RerankCommand(
                "rr_java_001",
                "phase12-reranker",
                "chest pain high risk",
                List.of(
                        new RerankCandidate("candidate_a", "general fever safety notice", Map.of("version_id", "ver_a")),
                        new RerankCandidate("candidate_b", "high risk chest pain guideline", Map.of("version_id", "ver_b"))),
                2,
                "trace_rr_java_001");
    }
}