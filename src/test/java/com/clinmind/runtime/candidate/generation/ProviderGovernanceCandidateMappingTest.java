package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.evaluation.scorer.JudgeBoundaryAgreementScorer;
import com.clinmind.runtime.evaluation.scorer.ProviderCapabilityProfileScorer;
import com.clinmind.runtime.evaluation.scorer.RiskClassifierTraceScorer;
import org.junit.jupiter.api.Test;

class ProviderGovernanceCandidateMappingTest {

    private final CandidateMappingPolicy mappingPolicy = new CandidateMappingPolicy();

    @Test
    void mapsJudgeBoundaryMetricToPatientBoundaryCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(JudgeBoundaryAgreementScorer.METRIC_ID))
                .contains(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(JudgeBoundaryAgreementScorer.METRIC_ID))
                .contains(TrainingTaskType.PATIENT_SAFE_REWRITE);
    }

    @Test
    void mapsRiskClassifierMetricToRiskSignalTrainingCandidate() {
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(RiskClassifierTraceScorer.METRIC_ID))
                .contains(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
    }

    @Test
    void mapsProviderProfileMetricToTraceGovernanceCandidate() {
        assertThat(mappingPolicy.mapMetricToExperienceType(ProviderCapabilityProfileScorer.METRIC_ID))
                .contains(ExperienceCandidateType.TRACE_QUALITY_LESSON);
        assertThat(mappingPolicy.mapMetricToTrainingTaskType(ProviderCapabilityProfileScorer.METRIC_ID))
                .contains(TrainingTaskType.ASSET_TRACE_EXPECTATION);
    }
}
