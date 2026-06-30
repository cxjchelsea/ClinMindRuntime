package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.store.jdbc.JdbcCandidateStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresCandidatePersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcCandidateStore candidateStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsGenerationAndCandidates() {
        candidateStore.saveGenerationResult(CandidateTestFixtures.sampleGenerationResult());

        Integer generationCount = jdbcTemplate.queryForObject(
                "select count(*) from candidate_generations where generation_id = ?",
                Integer.class,
                "cand_gen_001");
        Integer experienceCount = jdbcTemplate.queryForObject(
                "select count(*) from experience_candidates where generation_id = ?",
                Integer.class,
                "cand_gen_001");
        Integer trainingCount = jdbcTemplate.queryForObject(
                "select count(*) from training_example_candidates where generation_id = ?",
                Integer.class,
                "cand_gen_001");

        assertThat(generationCount).isEqualTo(1);
        assertThat(experienceCount).isEqualTo(1);
        assertThat(trainingCount).isEqualTo(1);
    }
}
