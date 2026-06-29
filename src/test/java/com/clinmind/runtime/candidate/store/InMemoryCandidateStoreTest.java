package com.clinmind.runtime.candidate.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCandidateStoreTest {

    private InMemoryCandidateStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryCandidateStore();
    }

    @Test
    void savesAndRetrievesGenerationResult() {
        CandidateGenerationResult result = CandidateTestFixtures.sampleGenerationResult();

        store.saveGenerationResult(result);

        assertThat(store.getGenerationResult("cand_gen_001")).isEqualTo(result);
        assertThat(store.listExperienceCandidates("cand_gen_001")).hasSize(1);
        assertThat(store.listTrainingExampleCandidates("cand_gen_001")).hasSize(1);
    }

    @Test
    void retrievesCandidatesByCandidateId() {
        store.saveGenerationResult(CandidateTestFixtures.sampleGenerationResult());

        assertThat(store.getExperienceCandidate("exp_cand_001").candidateId()).isEqualTo("exp_cand_001");
        assertThat(store.getTrainingExampleCandidate("train_cand_001").candidateId())
                .isEqualTo("train_cand_001");
    }

    @Test
    void throwsWhenGenerationResultNotFound() {
        assertThatThrownBy(() -> store.getGenerationResult("missing_generation"))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("missing_generation")
                .satisfies(ex -> assertThat(((CandidateNotFoundException) ex).getGenerationId())
                        .isEqualTo("missing_generation"));
    }

    @Test
    void throwsWhenExperienceCandidateNotFound() {
        assertThatThrownBy(() -> store.getExperienceCandidate("missing_candidate"))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("missing_candidate")
                .satisfies(ex -> assertThat(((CandidateNotFoundException) ex).getCandidateId())
                        .isEqualTo("missing_candidate"));
    }

    @Test
    void throwsWhenTrainingExampleCandidateNotFound() {
        assertThatThrownBy(() -> store.getTrainingExampleCandidate("missing_candidate"))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("missing_candidate");
    }
}
