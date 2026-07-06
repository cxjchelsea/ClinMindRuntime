package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.evaluation.scorer.ModelRegistryCompletenessScorer;
import com.clinmind.runtime.evaluation.scorer.ModelReleaseReadinessScorer;
import com.clinmind.runtime.evaluation.scorer.PromptRegistrySafetyScorer;
import com.clinmind.runtime.evaluation.scorer.TrainingDatasetGovernanceScorer;
import org.junit.jupiter.api.Test;

class ModelGovernanceCandidateMappingTest {

    private final CandidateMappingPolicy mappingPolicy = new CandidateMappingPolicy();

    @Test
    void mapsModelRegistryFailureToTraceGovernanceCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(ModelRegistryCompletenessScorer.METRIC_ID))
                .contains(ExperienceCandidateType.TRACE_QUALITY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(ModelRegistryCompletenessScorer.METRIC_ID))
                .contains(TrainingTaskType.ASSET_TRACE_EXPECTATION);
    }

    @Test
    void mapsPromptSafetyFailureToPatientBoundaryCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(PromptRegistrySafetyScorer.METRIC_ID))
                .contains(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(PromptRegistrySafetyScorer.METRIC_ID))
                .contains(TrainingTaskType.PATIENT_SAFE_REWRITE);
    }

    @Test
    void mapsDatasetAndReleaseFailuresToReviewRequiredGovernanceCandidates() {
        assertThat(mappingPolicy.mapMetricToExperienceType(TrainingDatasetGovernanceScorer.METRIC_ID))
                .contains(ExperienceCandidateType.TRACE_QUALITY_LESSON);
        assertThat(mappingPolicy.mapMetricToExperienceType(ModelReleaseReadinessScorer.METRIC_ID))
                .contains(ExperienceCandidateType.TRACE_QUALITY_LESSON);
    }
}
