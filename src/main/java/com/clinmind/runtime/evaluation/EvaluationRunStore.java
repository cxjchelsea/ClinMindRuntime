package com.clinmind.runtime.evaluation;

import java.util.List;

public interface EvaluationRunStore {

    void save(EvaluationRun run);

    EvaluationRun get(String runId);

    void saveExecution(String runId, String caseId, RuntimeCaseExecution execution);

    RuntimeCaseExecution getExecution(String runId, String caseId);

    List<RuntimeCaseExecution> listExecutions(String runId);
}
