package com.clinmind.runtime.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class CandidateGenerationResultTest {

    @Test
    void buildsSampleGenerationResult() {
        CandidateGenerationResult result = CandidateTestFixtures.sampleGenerationResult();

        assertThat(result.generationId()).isEqualTo("cand_gen_001");
        assertThat(result.sourceEvaluationRunId()).isEqualTo("eval_run_001");
        assertThat(result.experienceCandidates()).hasSize(1);
        assertThat(result.trainingExampleCandidates()).hasSize(1);
        assertThat(result.skippedItems()).hasSize(1);
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void defaultsCollectionsToEmptyLists() {
        CandidateGenerationResult result = new CandidateGenerationResult(
                "cand_gen_002",
                "eval_run_002",
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:00:01Z"),
                null,
                null,
                null,
                null);

        assertThat(result.experienceCandidates()).isEmpty();
        assertThat(result.trainingExampleCandidates()).isEmpty();
        assertThat(result.skippedItems()).isEmpty();
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void rejectsBlankGenerationId() {
        assertThatThrownBy(() -> new CandidateGenerationResult(
                        " ",
                        "eval_run_002",
                        Instant.parse("2026-06-25T10:00:00Z"),
                        Instant.parse("2026-06-25T10:00:01Z"),
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("generationId");
    }
}
