package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEvaluationRunStore implements EvaluationRunStore {

    private final Map<String, EvaluationRun> runs = new ConcurrentHashMap<>();
    private final Map<String, Map<String, RuntimeCaseExecution>> executions = new ConcurrentHashMap<>();

    @Override
    public void save(EvaluationRun run) {
        runs.put(run.runId(), run);
        executions.putIfAbsent(run.runId(), new ConcurrentHashMap<>());
    }

    @Override
    public EvaluationRun get(String runId) {
        EvaluationRun run = runs.get(runId);
        if (run == null) {
            throw new EvaluationLoadException("Evaluation run not found: " + runId, null);
        }
        return run;
    }

    @Override
    public void saveExecution(String runId, String caseId, RuntimeCaseExecution execution) {
        executions.computeIfAbsent(runId, ignored -> new ConcurrentHashMap<>())
                .put(caseId, execution);
    }

    @Override
    public RuntimeCaseExecution getExecution(String runId, String caseId) {
        Map<String, RuntimeCaseExecution> runExecutions = executions.get(runId);
        if (runExecutions == null || !runExecutions.containsKey(caseId)) {
            throw new EvaluationLoadException(
                    "Evaluation case execution not found: " + runId + "/" + caseId,
                    null);
        }
        return runExecutions.get(caseId);
    }

    @Override
    public List<RuntimeCaseExecution> listExecutions(String runId) {
        Map<String, RuntimeCaseExecution> runExecutions = executions.get(runId);
        if (runExecutions == null) {
            return List.of();
        }
        return List.copyOf(runExecutions.values());
    }

    @Override
    public List<EvaluationRun> list(String caseSetId, EvaluationRunStatus status, int limit) {
        return runs.values().stream()
                .filter(run -> caseSetId == null
                        || caseSetId.isBlank()
                        || caseSetId.equals(run.config().caseSetId()))
                .filter(run -> status == null || status == run.status())
                .sorted(Comparator.comparing(
                                EvaluationRun::startedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(EvaluationRun::runId, Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }
}
