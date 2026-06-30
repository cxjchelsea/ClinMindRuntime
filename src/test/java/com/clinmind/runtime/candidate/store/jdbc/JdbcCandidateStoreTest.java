package com.clinmind.runtime.candidate.store.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.store.CandidateNotFoundException;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcCandidateStoreTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcCandidateStore store;

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
                .isInstanceOf(CandidateNotFoundException.class);
    }
}
