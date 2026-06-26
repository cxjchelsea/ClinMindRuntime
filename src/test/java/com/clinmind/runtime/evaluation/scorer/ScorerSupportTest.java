package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScorerSupportTest {

    @Test
    void notApplicableDoesNotInflateWeightedTotal() {
        List<MetricResult> metrics = List.of(
                metric(DdxCoverageScorer.METRIC_ID, false),
                metric(SafetyGateScorer.METRIC_ID, true),
                metric(PatientBoundaryScorer.METRIC_ID, true),
                metric(NextActionScorer.METRIC_ID, false),
                metric(TraceCompletenessScorer.METRIC_ID, true),
                metric(AssetVersionTraceScorer.METRIC_ID, true));

        ScoreBreakdown breakdown = ScorerSupport.buildBreakdown(metrics);

        assertThat(breakdown.ddxScore()).isZero();
        assertThat(breakdown.totalScore()).isEqualTo(1.0);
    }

    @Test
    void allNotApplicableWeightedMetricsYieldZeroTotal() {
        List<MetricResult> metrics = List.of(
                metric(DdxCoverageScorer.METRIC_ID, false),
                metric(NextActionScorer.METRIC_ID, false));

        ScoreBreakdown breakdown = ScorerSupport.buildBreakdown(metrics);

        assertThat(breakdown.totalScore()).isZero();
    }

    private static MetricResult metric(String metricId, boolean applicable) {
        if (!applicable) {
            return ScorerSupport.notApplicable(metricId, metricId);
        }
        return ScorerSupport.pass(metricId, metricId, "expected", "actual", "ok");
    }
}
