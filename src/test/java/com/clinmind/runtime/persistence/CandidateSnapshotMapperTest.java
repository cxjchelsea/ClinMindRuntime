package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunConfig;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.state.RuntimeState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateSnapshotMapperTest {

    private CandidateSnapshotMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new CandidateSnapshotMapper(new JsonSnapshotMapper(objectMapper));
    }

    @Test
    void roundTripsExperienceCandidate() {
        ExperienceCandidate candidate = CandidateTestFixtures.sampleExperienceCandidate();

        String json = mapper.toJson(candidate);
        ExperienceCandidate restored = mapper.experienceFromJson(json);

        assertThat(restored.candidateId()).isEqualTo(candidate.candidateId());
        assertThat(restored.reviewStatus()).isEqualTo(candidate.reviewStatus());
        assertThat(restored.sourceRef().assetPackageId()).isEqualTo(candidate.sourceRef().assetPackageId());
    }
}
