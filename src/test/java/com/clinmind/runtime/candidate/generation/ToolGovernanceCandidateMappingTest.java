package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.evaluation.scorer.ToolFallbackSafetyScorer;
import com.clinmind.runtime.evaluation.scorer.ToolInvocationTraceScorer;
import com.clinmind.runtime.evaluation.scorer.ToolRegistryCompletenessScorer;
import com.clinmind.runtime.evaluation.scorer.ToolResultBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.ToolSideEffectPolicyScorer;
import org.junit.jupiter.api.Test;

class ToolGovernanceCandidateMappingTest {

    private final CandidateMappingPolicy mappingPolicy = new CandidateMappingPolicy();

    @Test
    void mapsToolRegistryAndTraceFailuresToTraceCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(ToolRegistryCompletenessScorer.METRIC_ID))
                .contains(ExperienceCandidateType.TRACE_QUALITY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(ToolInvocationTraceScorer.METRIC_ID))
                .contains(TrainingTaskType.ASSET_TRACE_EXPECTATION);
        assertThat(mappingPolicy.mapMetricToExperienceType(ToolFallbackSafetyScorer.METRIC_ID))
                .contains(ExperienceCandidateType.TRACE_QUALITY_LESSON);
    }

    @Test
    void mapsBoundaryFailureToPatientBoundaryCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(ToolResultBoundaryScorer.METRIC_ID))
                .contains(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(ToolResultBoundaryScorer.METRIC_ID))
                .contains(TrainingTaskType.PATIENT_SAFE_REWRITE);
    }

    @Test
    void mapsSideEffectFailureToSafetyGovernanceCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(ToolSideEffectPolicyScorer.METRIC_ID))
                .contains(ExperienceCandidateType.SAFETY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(ToolSideEffectPolicyScorer.METRIC_ID))
                .contains(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
    }
}
