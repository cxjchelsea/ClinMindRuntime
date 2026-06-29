package com.clinmind.runtime.candidate.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CandidateNotFoundExceptionTest {

    @Test
    void generationNotFoundUsesGenerationResourceType() {
        CandidateNotFoundException exception = CandidateNotFoundException.generationNotFound("cand_gen_001");

        assertThat(exception.getResourceType()).isEqualTo(CandidateResourceType.GENERATION);
        assertThat(exception.getGenerationId()).isEqualTo("cand_gen_001");
    }

    @Test
    void experienceCandidateNotFoundUsesExperienceResourceType() {
        CandidateNotFoundException exception =
                CandidateNotFoundException.experienceCandidateNotFound("exp_cand_001");

        assertThat(exception.getResourceType()).isEqualTo(CandidateResourceType.EXPERIENCE_CANDIDATE);
        assertThat(exception.getCandidateId()).isEqualTo("exp_cand_001");
    }

    @Test
    void trainingCandidateNotFoundUsesTrainingResourceType() {
        CandidateNotFoundException exception =
                CandidateNotFoundException.trainingExampleCandidateNotFound("train_cand_001");

        assertThat(exception.getResourceType()).isEqualTo(CandidateResourceType.TRAINING_EXAMPLE_CANDIDATE);
        assertThat(exception.getCandidateId()).isEqualTo("train_cand_001");
    }
}
