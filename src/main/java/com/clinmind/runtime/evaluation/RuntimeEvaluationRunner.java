package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.api.AssetContextRequest;
import com.clinmind.runtime.api.ContinueRuntimeRequest;
import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.api.dto.ApiResponseMapper;
import com.clinmind.runtime.evaluation.scorer.EvaluationItemScoringService;
import com.clinmind.runtime.service.RuntimeExecutionResult;
import com.clinmind.runtime.service.RuntimeService;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RuntimeEvaluationRunner implements EvaluationRunner {

    static final String RUNTIME_EXECUTION_METRIC = "runtime_execution";

    private final RuntimeService runtimeService;
    private final EvaluationCaseRepository caseRepository;
    private final EvaluationRunStore runStore;
    private final EvaluationItemScoringService scoringService;
    private final EvaluationResultAggregator resultAggregator;

    public RuntimeEvaluationRunner(
            RuntimeService runtimeService,
            EvaluationCaseRepository caseRepository,
            EvaluationRunStore runStore,
            EvaluationItemScoringService scoringService,
            EvaluationResultAggregator resultAggregator) {
        this.runtimeService = runtimeService;
        this.caseRepository = caseRepository;
        this.runStore = runStore;
        this.scoringService = scoringService;
        this.resultAggregator = resultAggregator;
    }

    @Override
    public EvaluationRun run(EvaluationRunConfig config) {
        String runId = IdGenerator.evalRunId();
        Instant startedAt = Instant.now();
        EvaluationRun running = new EvaluationRun(
                runId,
                config,
                EvaluationRunStatus.RUNNING,
                startedAt,
                null,
                List.of(),
                null);
        runStore.save(running);

        List<EvaluationCase> cases = filterCases(config, caseRepository.loadCases(config.caseSetId()));
        List<EvaluationItemResult> itemResults = new ArrayList<>();
        boolean aborted = false;

        for (EvaluationCase evaluationCase : cases) {
            EvaluationItemResult itemResult = executeCase(runId, config, evaluationCase);
            itemResults.add(itemResult);
            if (!itemResult.passed() && config.failFast()) {
                aborted = true;
                break;
            }
        }

        EvaluationRunStatus status = resolveStatus(itemResults, aborted);
        EvaluationRun completedWithoutResult = new EvaluationRun(
                runId,
                config,
                status,
                startedAt,
                Instant.now(),
                List.copyOf(itemResults),
                null);
        EvaluationResult result = resultAggregator.aggregate(completedWithoutResult);
        EvaluationRun completed = new EvaluationRun(
                runId,
                config,
                status,
                startedAt,
                completedWithoutResult.completedAt(),
                List.copyOf(itemResults),
                result);
        runStore.save(completed);
        return completed;
    }

    private EvaluationItemResult executeCase(
            String runId,
            EvaluationRunConfig config,
            EvaluationCase evaluationCase) {
        Map<String, Object> operationResponses = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        try {
            StartRuntimeRequest startRequest = buildStartRequest(runId, config, evaluationCase);
            RuntimeExecutionResult startResult = runtimeService.startRuntime(startRequest);
            String runtimeId = startResult.state().getRuntimeId();
            operationResponses.put("start", ApiResponseMapper.toOperationResponse(startResult.state()));

            List<EvaluationInputTurn> turns = evaluationCase.inputTurns();
            for (int index = 1; index < turns.size(); index++) {
                EvaluationInputTurn turn = turns.get(index);
                ContinueRuntimeRequest continueRequest = new ContinueRuntimeRequest(
                        runtimeId,
                        new UserInputRequest(turn.text(), turn.attachments()));
                RuntimeExecutionResult continueResult = runtimeService.continueRuntime(continueRequest);
                operationResponses.put("continue_" + index, ApiResponseMapper.toOperationResponse(continueResult.state()));
            }

            RuntimeState finalState = runtimeService.getResult(runtimeId);
            List<RuntimeTrace> traces = runtimeService.getTraces(runtimeId);
            RuntimeCaseExecution execution = new RuntimeCaseExecution(
                    evaluationCase.caseId(),
                    runtimeId,
                    finalState,
                    traces,
                    operationResponses,
                    errors);
            runStore.saveExecution(runId, evaluationCase.caseId(), execution);
            return scoringService.score(runId, evaluationCase, execution);
        } catch (Exception error) {
            errors.add(error.getMessage());
            EvaluationItemResult failed = buildFailedItemResult(runId, evaluationCase.caseId(), error.getMessage());
            runStore.saveExecution(
                    runId,
                    evaluationCase.caseId(),
                    new RuntimeCaseExecution(
                            evaluationCase.caseId(),
                            null,
                            null,
                            List.of(),
                            operationResponses,
                            errors));
            return failed;
        }
    }

    private StartRuntimeRequest buildStartRequest(
            String runId,
            EvaluationRunConfig config,
            EvaluationCase evaluationCase) {
        EvaluationInputTurn firstTurn = evaluationCase.inputTurns().get(0);
        String sessionId = "eval_" + runId + "_" + evaluationCase.caseId();
        return new StartRuntimeRequest(
                sessionId,
                null,
                evaluationCase.mode(),
                new UserInputRequest(firstTurn.text(), firstTurn.attachments()),
                evaluationCase.basicInfo(),
                buildAssetContext(config));
    }

    private AssetContextRequest buildAssetContext(EvaluationRunConfig config) {
        if (config.assetPackageId() == null || config.assetPackageId().isBlank()) {
            return null;
        }
        return new AssetContextRequest(config.assetPackageId(), config.assetPackageVersion());
    }

    static List<EvaluationCase> filterCases(EvaluationRunConfig config, List<EvaluationCase> cases) {
        return cases.stream()
                .filter(evaluationCase -> config.runtimeModeFilter() == null
                        || evaluationCase.mode() == config.runtimeModeFilter())
                .filter(evaluationCase -> config.symptomGroupFilter() == null
                        || config.symptomGroupFilter().equals(evaluationCase.symptomGroup()))
                .filter(evaluationCase -> config.includeTags().isEmpty()
                        || config.includeTags().stream().anyMatch(tag -> evaluationCase.tags().contains(tag)))
                .filter(evaluationCase -> config.excludeTags().isEmpty()
                        || config.excludeTags().stream().noneMatch(tag -> evaluationCase.tags().contains(tag)))
                .toList();
    }

    private EvaluationItemResult buildFailedItemResult(String runId, String caseId, String message) {
        return new EvaluationItemResult(
                runId,
                caseId,
                null,
                List.of(),
                false,
                0.0,
                ScoreBreakdown.of(0, 0, 0, 0, 0, 0, 0),
                List.of(new MetricResult(
                        RUNTIME_EXECUTION_METRIC,
                        "Runtime Execution",
                        false,
                        0.0,
                        MetricSeverity.CRITICAL,
                        "successful execution",
                        message,
                        message)),
                List.of(),
                List.of(message));
    }

    private EvaluationRunStatus resolveStatus(List<EvaluationItemResult> itemResults, boolean aborted) {
        if (itemResults.isEmpty()) {
            return EvaluationRunStatus.COMPLETED;
        }
        long failedCount = itemResults.stream().filter(item -> !item.passed()).count();
        if (failedCount == 0) {
            return EvaluationRunStatus.COMPLETED;
        }
        if (aborted || failedCount == itemResults.size()) {
            return EvaluationRunStatus.FAILED;
        }
        return EvaluationRunStatus.PARTIALLY_FAILED;
    }
}
