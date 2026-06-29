package com.clinmind.runtime.candidate.generation;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import com.clinmind.runtime.candidate.SanitizationStatus;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.evaluation.CaseSeverity;
import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationInputTurn;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TrainingExampleCandidateGenerator {

    private final CandidateMappingPolicy mappingPolicy;

    public TrainingExampleCandidateGenerator(CandidateMappingPolicy mappingPolicy) {
        this.mappingPolicy = mappingPolicy;
    }

    public List<TrainingExampleCandidate> generateFromItemResult(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            EvaluationCase evaluationCase,
            RuntimeCaseExecution execution,
            CandidateGenerationPolicy policy) {
        if (!policy.generateTrainingCandidates()) {
            return List.of();
        }

        Map<String, TrainingExampleCandidate> candidates = new LinkedHashMap<>();
        CaseSeverity caseSeverity = evaluationCase == null ? CaseSeverity.NORMAL : evaluationCase.severity();
        AssetContext assetContext = resolveAssetContext(run);
        ExpectedOutcome expectedOutcome =
                evaluationCase == null ? null : evaluationCase.expectedOutcome();

        for (MetricResult metric : itemResult.metricResults()) {
            if (!mappingPolicy.shouldGenerateTrainingCandidate(metric, policy, itemResult.passed())) {
                continue;
            }
            mappingPolicy.mapMetricToTrainingTaskType(metric.metricId()).ifPresent(taskType -> {
                String dedupKey = dedupKey(itemResult.caseId(), taskType, metric.metricId(), metric.message());
                candidates.putIfAbsent(
                        dedupKey,
                        buildFromMetric(
                                run,
                                itemResult,
                                execution,
                                evaluationCase,
                                metric,
                                taskType,
                                expectedOutcome,
                                caseSeverity,
                                assetContext));
            });
        }

        return trimByPolicy(candidates, policy);
    }

    private TrainingExampleCandidate buildFromMetric(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            EvaluationCase evaluationCase,
            MetricResult metric,
            TrainingTaskType taskType,
            ExpectedOutcome expectedOutcome,
            CaseSeverity caseSeverity,
            AssetContext assetContext) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.METRIC_RESULT,
                execution == null ? itemResult.runtimeId() : execution.runtimeId(),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                null,
                metric.metricId(),
                assetContext.packageId(),
                assetContext.version(),
                "metric_result");

        Map<String, Object> input = buildInput(taskType, evaluationCase, execution, assetContext);
        Map<String, Object> expectedOutput = buildExpectedOutput(taskType, expectedOutcome, assetContext);
        Map<String, Object> negativeOutput = buildNegativeOutput(metric);

        return new TrainingExampleCandidate(
                "train_cand_" + run.runId() + "_" + itemResult.caseId() + "_" + metric.metricId(),
                taskType,
                sourceRef,
                input,
                expectedOutput,
                negativeOutput,
                taskType.name().toLowerCase(),
                metric.message(),
                mappingPolicy.resolveExperienceRiskLevel(metric, caseSeverity),
                CandidateReviewStatus.REVIEW_REQUIRED,
                SanitizationStatus.NEEDS_REVIEW,
                List.of(metric.metricId(), taskType.name().toLowerCase()),
                Instant.now(),
                Map.of("metric_id", metric.metricId()));
    }

    private Map<String, Object> buildInput(
            TrainingTaskType taskType,
            EvaluationCase evaluationCase,
            RuntimeCaseExecution execution,
            AssetContext assetContext) {
        Map<String, Object> input = new HashMap<>();
        if (evaluationCase != null) {
            input.put("case_id", evaluationCase.caseId());
            input.put("symptom_group", evaluationCase.symptomGroup());
            input.put(
                    "input_texts",
                    evaluationCase.inputTurns().stream().map(EvaluationInputTurn::text).toList());
            input.put("basic_info", evaluationCase.basicInfo());
        }

        RuntimeState state = execution == null ? null : execution.finalState();
        if (state != null) {
            CaseFrame caseFrame = state.getCaseFrame();
            if (caseFrame != null) {
                input.put("case_frame_summary", summarizeCaseFrame(caseFrame));
            }
            input.put("runtime_status", state.getRuntimeStatus() == null ? null : state.getRuntimeStatus().getValue());
            PatientOutput patientOutput = state.getPatientOutput();
            if (patientOutput != null && taskType == TrainingTaskType.PATIENT_SAFE_REWRITE) {
                input.put("patient_output", patientOutput.content());
                input.put("patient_output_level", patientOutput.outputLevel() == null ? null : patientOutput.outputLevel().getValue());
            }
        }

        if (taskType == TrainingTaskType.ASSET_TRACE_EXPECTATION) {
            input.put("asset_package_id", assetContext.packageId());
            input.put("asset_package_version", assetContext.version());
            if (execution != null) {
                input.put(
                        "trace_ids",
                        execution.traces().stream().map(RuntimeTrace::getTraceId).toList());
            }
        }

        return Map.copyOf(input);
    }

    private Map<String, Object> buildExpectedOutput(
            TrainingTaskType taskType, ExpectedOutcome expectedOutcome, AssetContext assetContext) {
        Map<String, Object> expected = new HashMap<>();
        if (expectedOutcome == null) {
            if (taskType == TrainingTaskType.ASSET_TRACE_EXPECTATION) {
                expected.put("asset_package_id", assetContext.packageId());
                expected.put("asset_package_version", assetContext.version());
            }
            return Map.copyOf(expected);
        }

        return switch (taskType) {
            case RISK_SIGNAL_CLASSIFICATION -> {
                Map<String, Object> riskExpected = new HashMap<>();
                riskExpected.put("safety_gate_triggered", expectedOutcome.safetyGateTriggered());
                riskExpected.put("expected_matched_rules", expectedOutcome.expectedMatchedRules());
                yield Map.copyOf(riskExpected);
            }
            case PATIENT_SAFE_REWRITE -> Map.of(
                    "required_patient_phrases", expectedOutcome.requiredPatientPhrases(),
                    "forbidden_patient_phrases", expectedOutcome.forbiddenPatientPhrases(),
                    "forbidden_patient_fields", expectedOutcome.forbiddenPatientFields());
            case DDX_EXPECTATION -> Map.of("expected_ddx_contains", expectedOutcome.expectedDdxContains());
            case NEXT_ACTION_EXPECTATION -> Map.of(
                    "expected_next_action_types",
                    expectedOutcome.expectedNextActionTypes().stream()
                            .map(type -> type.getValue())
                            .toList());
            case ASSET_TRACE_EXPECTATION -> {
                Map<String, Object> assetExpected = new HashMap<>();
                assetExpected.put("asset_package_id", assetContext.packageId());
                assetExpected.put("asset_package_version", assetContext.version());
                assetExpected.put("required_asset_trace", expectedOutcome.requiredAssetTrace());
                yield Map.copyOf(assetExpected);
            }
            default -> Map.of();
        };
    }

    private static Map<String, Object> buildNegativeOutput(MetricResult metric) {
        Map<String, Object> negative = new HashMap<>();
        if (metric.actual() != null) {
            negative.put("actual", metric.actual());
        }
        if (metric.message() != null) {
            negative.put("failure_message", metric.message());
        }
        return Map.copyOf(negative);
    }

    private static Map<String, Object> summarizeCaseFrame(CaseFrame caseFrame) {
        Map<String, Object> summary = new HashMap<>();
        if (caseFrame.chiefComplaint() != null) {
            summary.put("chief_complaint", caseFrame.chiefComplaint());
        }
        summary.put("missing_slots", caseFrame.missingSlots());
        summary.put("symptoms", caseFrame.symptoms());
        return Map.copyOf(summary);
    }

    private List<TrainingExampleCandidate> trimByPolicy(
            Map<String, TrainingExampleCandidate> candidates, CandidateGenerationPolicy policy) {
        if (candidates.size() <= policy.maxCandidatesPerCase()) {
            return List.copyOf(candidates.values());
        }
        return candidates.values().stream()
                .sorted(Comparator.comparingInt(
                                (TrainingExampleCandidate candidate) -> trainingCandidatePriority(candidate.taskType()))
                        .reversed())
                .limit(policy.maxCandidatesPerCase())
                .toList();
    }

    private static int trainingCandidatePriority(TrainingTaskType taskType) {
        return switch (taskType) {
            case RISK_SIGNAL_CLASSIFICATION, PATIENT_SAFE_REWRITE -> 100;
            case ASSET_TRACE_EXPECTATION -> 80;
            case DDX_EXPECTATION -> 60;
            case NEXT_ACTION_EXPECTATION -> 40;
            default -> 10;
        };
    }

    private static String dedupKey(String caseId, TrainingTaskType taskType, String metricId, String message) {
        return caseId + "|" + taskType + "|" + metricId + "|" + (message == null ? "" : message);
    }

    private static String firstTraceId(EvaluationItemResult itemResult, RuntimeCaseExecution execution) {
        if (execution != null && !execution.traces().isEmpty()) {
            return execution.traces().get(0).getTraceId();
        }
        return itemResult.traceIds().isEmpty() ? null : itemResult.traceIds().get(0);
    }

    private static AssetContext resolveAssetContext(EvaluationRun run) {
        if (run.result() != null) {
            return new AssetContext(run.result().assetPackageId(), run.result().assetPackageVersion());
        }
        if (run.config() != null) {
            return new AssetContext(run.config().assetPackageId(), run.config().assetPackageVersion());
        }
        return new AssetContext(null, null);
    }

    private record AssetContext(String packageId, String version) {}
}
