package com.clinmind.runtime.evaluation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class EvaluationRunStore {

    private final Map<String, EvaluationRun> runs = new ConcurrentHashMap<>();
    private final Map<String, Map<String, RuntimeCaseExecution>> executions = new ConcurrentHashMap<>();

    public void save(EvaluationRun run) {
        runs.put(run.runId(), run);
        executions.putIfAbsent(run.runId(), new ConcurrentHashMap<>());
    }

    public EvaluationRun get(String runId) {
        EvaluationRun run = runs.get(runId);
        if (run == null) {
            throw new EvaluationLoadException("Evaluation run not found: " + runId, null);
        }
        return run;
    }

    public void saveExecution(String runId, String caseId, RuntimeCaseExecution execution) {
        executions.computeIfAbsent(runId, ignored -> new ConcurrentHashMap<>())
                .put(caseId, execution);
    }

    public RuntimeCaseExecution getExecution(String runId, String caseId) {
        Map<String, RuntimeCaseExecution> runExecutions = executions.get(runId);
        if (runExecutions == null || !runExecutions.containsKey(caseId)) {
            throw new EvaluationLoadException(
                    "Evaluation case execution not found: " + runId + "/" + caseId,
                    null);
        }
        return runExecutions.get(caseId);
    }

    public List<RuntimeCaseExecution> listExecutions(String runId) {
        Map<String, RuntimeCaseExecution> runExecutions = executions.get(runId);
        if (runExecutions == null) {
            return List.of();
        }
        return List.copyOf(runExecutions.values());
    }
}
