package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.CandidateSkippedReason;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.evaluation.CaseSeverity;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.SafetyViolationType;
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.DdxCoverageScorer;
import com.clinmind.runtime.evaluation.scorer.NextActionScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.scorer.TraceCompletenessScorer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateMappingPolicyTest {

    private CandidateMappingPolicy policy;
    private CandidateGenerationPolicy defaultPolicy;

    @BeforeEach
    void setUp() {
        policy = new CandidateMappingPolicy();
        defaultPolicy = CandidateGenerationPolicy.defaults();
    }

    @Test
    void mapsMetricSeverityToCandidateRiskLevel() {
        assertThat(policy.mapSeverity(MetricSeverity.CRITICAL)).isEqualTo(CandidateRiskLevel.CRITICAL);
        assertThat(policy.mapSeverity(MetricSeverity.MAJOR)).isEqualTo(CandidateRiskLevel.HIGH);
        assertThat(policy.mapSeverity(MetricSeverity.MINOR)).isEqualTo(CandidateRiskLevel.MEDIUM);
        assertThat(policy.mapSeverity(MetricSeverity.INFO)).isEqualTo(CandidateRiskLevel.LOW);
    }

    @Test
    void mapsMetricToExperienceAndTrainingTypes() {
        assertThat(policy.mapMetricToExperienceType(SafetyGateScorer.METRIC_ID))
                .contains(ExperienceCandidateType.SAFETY_LESSON);
        assertThat(policy.mapMetricToTrainingTaskType(SafetyGateScorer.METRIC_ID))
                .contains(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
        assertThat(policy.mapMetricToExperienceType(PatientBoundaryScorer.METRIC_ID))
                .contains(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
        assertThat(policy.mapMetricToTrainingTaskType(DdxCoverageScorer.METRIC_ID))
                .contains(TrainingTaskType.DDX_EXPECTATION);
        assertThat(policy.mapMetricToTrainingTaskType(NextActionScorer.METRIC_ID))
                .contains(TrainingTaskType.NEXT_ACTION_EXPECTATION);
        assertThat(policy.mapMetricToTrainingTaskType(AssetVersionTraceScorer.METRIC_ID))
                .contains(TrainingTaskType.ASSET_TRACE_EXPECTATION);
    }

    @Test
    void generatesFromCriticalAndMajorFailuresByDefault() {
        MetricResult criticalFailure = failedMetric(SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL);
        MetricResult majorFailure = failedMetric(DdxCoverageScorer.METRIC_ID, MetricSeverity.MAJOR);

        assertThat(policy.shouldGenerateExperienceCandidate(criticalFailure, defaultPolicy, false))
                .isTrue();
        assertThat(policy.shouldGenerateExperienceCandidate(majorFailure, defaultPolicy, false))
                .isTrue();
    }

    @Test
    void skipsMinorFailurePassedCaseAndNotApplicableByDefault() {
        MetricResult minorFailure = failedMetric(NextActionScorer.METRIC_ID, MetricSeverity.MINOR);
        MetricResult passedMetric = passedMetric(SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL);
        MetricResult notApplicable = new MetricResult(
                TraceCompletenessScorer.METRIC_ID,
                "Trace Completeness",
                false,
                0.0,
                MetricSeverity.MAJOR,
                null,
                null,
                "not applicable",
                false);

        assertThat(policy.resolveSkipReason(minorFailure, defaultPolicy, false))
                .contains(CandidateSkippedReason.MINOR_FAILURE_DISABLED);
        assertThat(policy.resolveSkipReason(passedMetric, defaultPolicy, false))
                .contains(CandidateSkippedReason.PASSED_CASE_SKIPPED);
        assertThat(policy.resolveSkipReason(notApplicable, defaultPolicy, false))
                .contains(CandidateSkippedReason.NOT_APPLICABLE_METRIC_SKIPPED);
    }

    @Test
    void mapsSafetyViolationAndRegressionFinding() {
        SafetyViolation violation = new SafetyViolation(
                "sv_001",
                "case_001",
                SafetyViolationType.PATIENT_DIAGNOSIS_LEAK,
                MetricSeverity.CRITICAL,
                "patient diagnosis leaked",
                null);

        assertThat(policy.mapSafetyViolation(violation)).isEqualTo(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
        assertThat(policy.mapSafetyViolationRisk(violation)).isEqualTo(CandidateRiskLevel.CRITICAL);

        RegressionFinding finding = new RegressionFinding(
                "rf_001",
                "safety_regression",
                MetricSeverity.CRITICAL,
                List.of("case_001"),
                "Safety regression across cases",
                "Review SafetyGate rules");

        assertThat(policy.mapRegressionFinding(finding)).isEqualTo(ExperienceCandidateType.SAFETY_LESSON);
    }

    @Test
    void prioritizesCriticalSafetyCandidates() {
        assertThat(policy.experienceCandidatePriority(ExperienceCandidateType.SAFETY_LESSON))
                .isGreaterThan(policy.experienceCandidatePriority(ExperienceCandidateType.TRACE_QUALITY_LESSON));
        assertThat(policy.experienceCandidatePriority(ExperienceCandidateType.ASSET_VERSION_LESSON))
                .isGreaterThan(policy.experienceCandidatePriority(ExperienceCandidateType.NEXT_ACTION_LESSON));
    }

    @Test
    void resolvesExperienceRiskLevelForHighRiskCases() {
        MetricResult nextActionFailure = failedMetric(NextActionScorer.METRIC_ID, MetricSeverity.MAJOR);

        assertThat(policy.resolveExperienceRiskLevel(nextActionFailure, CaseSeverity.CRITICAL))
                .isEqualTo(CandidateRiskLevel.HIGH);
        assertThat(policy.resolveExperienceRiskLevel(nextActionFailure, CaseSeverity.NORMAL))
                .isEqualTo(CandidateRiskLevel.MEDIUM);
    }

    private static MetricResult failedMetric(String metricId, MetricSeverity severity) {
        return new MetricResult(metricId, metricId, false, 0.0, severity, null, null, "failed", true);
    }

    private static MetricResult passedMetric(String metricId, MetricSeverity severity) {
        return new MetricResult(metricId, metricId, true, 1.0, severity, null, null, "passed", true);
    }
}
