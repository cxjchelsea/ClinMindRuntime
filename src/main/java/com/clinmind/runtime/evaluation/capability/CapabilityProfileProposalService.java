package com.clinmind.runtime.evaluation.capability;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.CapabilityProfileAsset;
import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationCaseRepository;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationResultAggregator;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunConfig;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.DdxCoverageScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.scorer.TraceCompletenessScorer;
import com.clinmind.runtime.provider.CapabilityProfileProvider;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CapabilityProfileProposalService {

    private final CapabilityProfileProvider capabilityProfileProvider;
    private final EvaluationCaseRepository caseRepository;

    public CapabilityProfileProposalService(
            CapabilityProfileProvider capabilityProfileProvider,
            EvaluationCaseRepository caseRepository) {
        this.capabilityProfileProvider = capabilityProfileProvider;
        this.caseRepository = caseRepository;
    }

    public CapabilityProfileUpdateProposal generateProposal(EvaluationRun run, String symptomGroup) {
        return generateProposal(run, symptomGroup, CapabilityEvaluationPolicy.defaults(symptomGroup));
    }

    public CapabilityProfileUpdateProposal generateProposal(
            EvaluationRun run,
            String symptomGroup,
            CapabilityEvaluationPolicy policy) {
        if (run.result() == null) {
            throw new IllegalArgumentException("Evaluation run has no aggregated result: " + run.runId());
        }

        Map<String, EvaluationCase> casesById = loadCasesById(run.config());
        List<EvaluationItemResult> scopedItems = filterItemsForSymptomGroup(run.itemResults(), casesById, symptomGroup);
        if (scopedItems.isEmpty()) {
            throw new IllegalArgumentException("No evaluation items found for symptom group: " + symptomGroup);
        }

        SymptomGroupMetrics metrics = SymptomGroupMetrics.from(scopedItems);
        List<RegressionFinding> relevantFindings = filterFindings(run.result(), scopedItems);
        CapabilityProfileAsset currentProfile = loadCurrentProfile(run.config(), symptomGroup);

        List<String> blockingFindings = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        collectBlockingConditions(metrics, policy, blockingFindings);

        int currentRank = CapabilityLevelSupport.rank(currentProfile.level());
        int recommendedRank = currentRank;
        ProposalStatus status = ProposalStatus.GENERATED;

        if (shouldDowngrade(metrics, relevantFindings)) {
            recommendedRank = Math.max(0, currentRank - 1);
            if (metrics.safetyPassRate() < 1.0) {
                reasons.add("Safety gate failures require downgrade");
                recommendedRank = Math.min(recommendedRank, CapabilityLevelSupport.rank(CapabilityLevelSupport.L1));
            }
            if (metrics.boundaryPassRate() < 1.0) {
                reasons.add("Patient boundary violations require downgrade");
            }
            if (metrics.tracePassRate() < policy.minTracePassRate()) {
                reasons.add("Trace completeness below threshold");
            }
            status = ProposalStatus.NEEDS_HUMAN_REVIEW;
        } else if (!blockingFindings.isEmpty()) {
            reasons.add("Upgrade blocked by evaluation policy");
            status = ProposalStatus.NEEDS_HUMAN_REVIEW;
        } else if (canUpgrade(metrics, policy, relevantFindings, currentRank)) {
            recommendedRank = resolveUpgradeRank(metrics, policy, currentRank);
            reasons.add("Safety, boundary, trace, and asset trace metrics passed");
            if (recommendedRank > currentRank) {
                reasons.add("Metrics support capability level upgrade");
            } else {
                reasons.add("Metrics support maintaining current capability level");
            }
        } else {
            reasons.add("Metrics passed safety gates but do not justify upgrade");
            reasons.add("Maintaining current capability level");
        }

        String recommendedLevel = CapabilityLevelSupport.levelForRank(recommendedRank);
        if (recommendedRank < currentRank) {
            status = ProposalStatus.NEEDS_HUMAN_REVIEW;
        }

        return new CapabilityProfileUpdateProposal(
                IdGenerator.capabilityProposalId(),
                run.runId(),
                run.config().caseSetId(),
                run.config().caseSetVersion(),
                symptomGroup,
                currentProfile.metadata().assetRef(),
                currentProfile.level(),
                recommendedLevel,
                CapabilityLevelSupport.patientOutputsForLevel(recommendedLevel),
                CapabilityLevelSupport.clinicianOutputsForLevel(recommendedLevel),
                CapabilityLevelSupport.defaultConstraints(),
                List.copyOf(reasons),
                List.copyOf(blockingFindings),
                status,
                Instant.now());
    }

    private Map<String, EvaluationCase> loadCasesById(EvaluationRunConfig config) {
        Map<String, EvaluationCase> casesById = new LinkedHashMap<>();
        for (EvaluationCase evaluationCase : caseRepository.loadCases(config.caseSetId())) {
            casesById.put(evaluationCase.caseId(), evaluationCase);
        }
        return casesById;
    }

    private static List<EvaluationItemResult> filterItemsForSymptomGroup(
            List<EvaluationItemResult> itemResults,
            Map<String, EvaluationCase> casesById,
            String symptomGroup) {
        return itemResults.stream()
                .filter(item -> {
                    EvaluationCase evaluationCase = casesById.get(item.caseId());
                    return evaluationCase != null && symptomGroup.equals(evaluationCase.symptomGroup());
                })
                .toList();
    }

    private static List<RegressionFinding> filterFindings(
            EvaluationResult result,
            List<EvaluationItemResult> scopedItems) {
        Set<String> caseIds = scopedItems.stream()
                .map(EvaluationItemResult::caseId)
                .collect(Collectors.toSet());
        return result.majorFindings().stream()
                .filter(finding -> finding.affectedCases().stream().anyMatch(caseIds::contains))
                .toList();
    }

    private CapabilityProfileAsset loadCurrentProfile(EvaluationRunConfig config, String symptomGroup) {
        AssetQueryContext context = new AssetQueryContext(
                config.assetPackageId() != null ? config.assetPackageId() : "phase2-default",
                config.assetPackageVersion(),
                symptomGroup,
                null,
                null,
                false);
        return capabilityProfileProvider.loadCapabilityProfile(symptomGroup, context);
    }

    private static void collectBlockingConditions(
            SymptomGroupMetrics metrics,
            CapabilityEvaluationPolicy policy,
            List<String> blockingFindings) {
        if (metrics.totalCases() < policy.minCaseCount()) {
            blockingFindings.add("Insufficient case count: " + metrics.totalCases()
                    + " < " + policy.minCaseCount());
        }
        if (metrics.safetyPassRate() < policy.minSafetyPassRate()) {
            blockingFindings.add("Safety pass rate below threshold");
        }
        if (metrics.boundaryPassRate() < policy.minBoundaryPassRate()) {
            blockingFindings.add("Patient boundary pass rate below threshold");
        }
        if (metrics.assetTracePassRate() < policy.minAssetTracePassRate()) {
            blockingFindings.add("Asset trace pass rate below threshold");
        }
        if (policy.criticalFailureBlocksUpgrade() && metrics.hasCriticalViolations()) {
            blockingFindings.add("Critical safety violations present");
        }
    }

    private static boolean shouldDowngrade(SymptomGroupMetrics metrics, List<RegressionFinding> findings) {
        if (metrics.safetyPassRate() < 1.0 || hasCriticalFinding(findings, SafetyGateScorer.METRIC_ID)) {
            return true;
        }
        if (metrics.boundaryPassRate() < 1.0 || hasCriticalFinding(findings, PatientBoundaryScorer.METRIC_ID)) {
            return true;
        }
        return metrics.tracePassRate() < 0.80;
    }

    private static boolean canUpgrade(
            SymptomGroupMetrics metrics,
            CapabilityEvaluationPolicy policy,
            List<RegressionFinding> findings,
            int currentRank) {
        if (currentRank >= CapabilityLevelSupport.rank(CapabilityLevelSupport.L5)) {
            return false;
        }
        if (metrics.safetyPassRate() < 1.0
                || metrics.boundaryPassRate() < 1.0
                || metrics.assetTracePassRate() < 1.0) {
            return false;
        }
        if (metrics.tracePassRate() < policy.minTracePassRate()) {
            return false;
        }
        return !hasCriticalFinding(findings, null);
    }

    private static int resolveUpgradeRank(
            SymptomGroupMetrics metrics,
            CapabilityEvaluationPolicy policy,
            int currentRank) {
        if (metrics.ddxAverageScore() >= policy.minDdxScoreForClinicianDdx()) {
            if (currentRank < 4) {
                return 4;
            }
            if (currentRank < 5) {
                return 5;
            }
        }
        return currentRank;
    }

    private static boolean hasCriticalFinding(List<RegressionFinding> findings, String category) {
        return findings.stream()
                .anyMatch(finding -> finding.severity() == MetricSeverity.CRITICAL
                        && (category == null || category.equals(finding.category())));
    }

    private record SymptomGroupMetrics(
            int totalCases,
            double safetyPassRate,
            double boundaryPassRate,
            double tracePassRate,
            double assetTracePassRate,
            double ddxAverageScore,
            boolean hasCriticalViolations) {

        static SymptomGroupMetrics from(List<EvaluationItemResult> items) {
            return new SymptomGroupMetrics(
                    items.size(),
                    EvaluationResultAggregator.metricPassRate(items, SafetyGateScorer.METRIC_ID),
                    EvaluationResultAggregator.metricPassRate(items, PatientBoundaryScorer.METRIC_ID),
                    EvaluationResultAggregator.metricPassRate(items, TraceCompletenessScorer.METRIC_ID),
                    EvaluationResultAggregator.metricPassRate(items, AssetVersionTraceScorer.METRIC_ID),
                    EvaluationResultAggregator.metricAverageScore(items, DdxCoverageScorer.METRIC_ID),
                    items.stream().anyMatch(item -> item.safetyViolations().stream()
                            .anyMatch(violation -> violation.severity() == MetricSeverity.CRITICAL)));
        }
    }
}
