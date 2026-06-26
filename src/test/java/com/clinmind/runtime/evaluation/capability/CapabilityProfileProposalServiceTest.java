package com.clinmind.runtime.evaluation.capability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunConfig;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.SafetyViolationType;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.DdxCoverageScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.scorer.TraceCompletenessScorer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CapabilityProfileProposalServiceTest {

    @Autowired
    private CapabilityProfileProposalService proposalService;

    @Test
    void blocksUpgradeWhenSafetyPassRateBelowThreshold() {
        EvaluationRun run = runWithItems(
                failedMetricItem("chest_pain_high_risk_001", SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL),
                passedMetricItem("chest_pain_normal_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.forTesting("chest_pain"));

        assertThat(proposal.recommendedLevel()).isEqualTo("L1");
        assertThat(proposal.status()).isEqualTo(ProposalStatus.NEEDS_HUMAN_REVIEW);
        assertThat(proposal.reasons()).anyMatch(reason -> reason.contains("downgrade"));
    }

    @Test
    void blocksUpgradeWhenBoundaryPassRateBelowThreshold() {
        EvaluationRun run = runWithItems(
                failedMetricItem("chest_pain_high_risk_001", PatientBoundaryScorer.METRIC_ID, MetricSeverity.CRITICAL),
                passedMetricItem("chest_pain_normal_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.forTesting("chest_pain"));

        assertThat(proposal.recommendedLevel()).isEqualTo("L1");
        assertThat(proposal.reasons()).anyMatch(reason -> reason.contains("downgrade"));
    }

    @Test
    void blocksUpgradeWhenAssetTracePassRateBelowThreshold() {
        EvaluationRun run = runWithItems(
                failedMetricItem("chest_pain_high_risk_001", AssetVersionTraceScorer.METRIC_ID, MetricSeverity.CRITICAL),
                passedMetricItem("chest_pain_normal_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.forTesting("chest_pain"));

        assertThat(proposal.blockingFindings())
                .anyMatch(finding -> finding.contains("Asset trace"));
        assertThat(proposal.status()).isEqualTo(ProposalStatus.NEEDS_HUMAN_REVIEW);
    }

    @Test
    void blocksUpgradeWhenCaseCountBelowMinimum() {
        EvaluationRun run = runWithItems(passedMetricItem("chest_pain_high_risk_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.defaults("chest_pain"));

        assertThat(proposal.blockingFindings())
                .anyMatch(finding -> finding.contains("Insufficient case count"));
        assertThat(proposal.recommendedLevel()).isEqualTo("L2");
    }

    @Test
    void generatesUpgradeWhenAllMetricsPass() {
        EvaluationRun run = runWithItems(
                highPerformingItem("chest_pain_high_risk_001"),
                highPerformingItem("chest_pain_normal_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.forTesting("chest_pain"));

        assertThat(proposal.recommendedLevel()).isIn("L4", "L5");
        assertThat(CapabilityLevelSupport.rank(proposal.recommendedLevel()))
                .isGreaterThan(CapabilityLevelSupport.rank(proposal.currentLevel()));
        assertThat(proposal.blockingFindings()).isEmpty();
        assertThat(proposal.status()).isEqualTo(ProposalStatus.GENERATED);
    }

    @Test
    void generatesKeepWhenMetricsPassButDoNotJustifyUpgrade() {
        EvaluationRun run = runWithItems(
                passedMetricItem("chest_pain_high_risk_001"),
                passedMetricItem("chest_pain_normal_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.forTesting("chest_pain"));

        assertThat(proposal.recommendedLevel()).isEqualTo("L2");
        assertThat(proposal.reasons()).anyMatch(reason -> reason.toLowerCase().contains("maintaining"));
    }

    @Test
    void proposalReferencesRunAndCurrentProfile() {
        EvaluationRun run = runWithItems(passedMetricItem("chest_pain_high_risk_001"));

        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(
                run,
                "chest_pain",
                CapabilityEvaluationPolicy.forTesting("chest_pain"));

        assertThat(proposal.runId()).isEqualTo(run.runId());
        assertThat(proposal.caseSetVersion()).isEqualTo("0.3.0");
        assertThat(proposal.currentProfileRef()).contains("@");
        assertThat(proposal.proposalId()).startsWith("cap_prop_");
    }

    @Test
    void rejectsUnknownSymptomGroupItems() {
        EvaluationRun run = runWithItems(passedMetricItem("fever_normal_001"));

        assertThatThrownBy(() -> proposalService.generateProposal(
                        run, "chest_pain", CapabilityEvaluationPolicy.forTesting("chest_pain")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No evaluation items");
    }

    private static EvaluationRun runWithItems(EvaluationItemResult... items) {
        return runWithItems(List.of(), items);
    }

    private static EvaluationRun runWithItems(
            List<RegressionFinding> majorFindings,
            EvaluationItemResult... items) {
        List<EvaluationItemResult> itemResults = List.of(items);
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                List.of(),
                List.of(),
                false,
                null);
        EvaluationResult result = new EvaluationResult(
                "eval_run_prop",
                config.caseSetId(),
                config.caseSetVersion(),
                config.assetPackageId(),
                config.assetPackageVersion(),
                itemResults.size(),
                (int) itemResults.stream().filter(EvaluationItemResult::passed).count(),
                (int) itemResults.stream().filter(item -> !item.passed()).count(),
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                majorFindings);
        return new EvaluationRun(
                "eval_run_prop",
                config,
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-26T00:00:00Z"),
                Instant.parse("2026-06-26T00:01:00Z"),
                itemResults,
                result);
    }

    private static EvaluationItemResult passedMetricItem(String caseId) {
        return metricItem(caseId, true, 0.85, List.of(
                metric(SafetyGateScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(PatientBoundaryScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(TraceCompletenessScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(AssetVersionTraceScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(DdxCoverageScorer.METRIC_ID, true, MetricSeverity.INFO, 0.5)));
    }

    private static EvaluationItemResult highPerformingItem(String caseId) {
        return metricItem(caseId, true, 0.95, List.of(
                metric(SafetyGateScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(PatientBoundaryScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(TraceCompletenessScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(AssetVersionTraceScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(DdxCoverageScorer.METRIC_ID, true, MetricSeverity.INFO, 0.9)));
    }

    private static EvaluationItemResult failedMetricItem(
            String caseId,
            String metricId,
            MetricSeverity severity) {
        List<MetricResult> metrics = new java.util.ArrayList<>(List.of(
                metric(SafetyGateScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(PatientBoundaryScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(TraceCompletenessScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(AssetVersionTraceScorer.METRIC_ID, true, MetricSeverity.INFO),
                metric(DdxCoverageScorer.METRIC_ID, true, MetricSeverity.INFO, 0.5)));
        metrics.set(indexForMetric(metricId), metric(metricId, false, severity, 0.0));
        return metricItem(caseId, false, 0.4, metrics);
    }

    private static int indexForMetric(String metricId) {
        return switch (metricId) {
            case SafetyGateScorer.METRIC_ID -> 0;
            case PatientBoundaryScorer.METRIC_ID -> 1;
            case TraceCompletenessScorer.METRIC_ID -> 2;
            case AssetVersionTraceScorer.METRIC_ID -> 3;
            default -> 4;
        };
    }

    private static EvaluationItemResult metricItem(
            String caseId,
            boolean passed,
            double score,
            List<MetricResult> metrics) {
        List<SafetyViolation> violations = passed
                ? List.of()
                : List.of(new SafetyViolation(
                        "sv_" + caseId,
                        caseId,
                        SafetyViolationType.HIGH_RISK_NOT_TRIGGERED,
                        MetricSeverity.CRITICAL,
                        "failed",
                        java.util.Map.of()));
        return new EvaluationItemResult(
                "eval_run_prop",
                caseId,
                "rt_" + caseId,
                List.of("trace_1"),
                passed,
                score,
                ScoreBreakdown.of(1.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0),
                metrics,
                violations,
                List.of());
    }

    private static MetricResult metric(String metricId, boolean passed, MetricSeverity severity) {
        return metric(metricId, passed, severity, passed ? 1.0 : 0.0);
    }

    private static MetricResult metric(
            String metricId,
            boolean passed,
            MetricSeverity severity,
            double score) {
        return new MetricResult(
                metricId,
                metricId,
                passed,
                score,
                severity,
                "expected",
                passed ? "actual" : "violation",
                passed ? "ok" : "failed",
                true);
    }
}
