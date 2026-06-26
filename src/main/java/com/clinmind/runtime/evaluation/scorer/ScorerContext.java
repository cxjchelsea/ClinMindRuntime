package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;

public record ScorerContext(
        String runId,
        EvaluationCase evaluationCase,
        RuntimeCaseExecution execution
) {
}
