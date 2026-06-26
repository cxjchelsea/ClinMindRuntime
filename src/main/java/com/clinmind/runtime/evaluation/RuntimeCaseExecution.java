package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.List;
import java.util.Map;

public record RuntimeCaseExecution(
        String caseId,
        String runtimeId,
        RuntimeState finalState,
        List<RuntimeTrace> traces,
        Map<String, Object> operationResponses,
        List<String> errors
) {
    public RuntimeCaseExecution {
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("caseId must not be blank");
        }
        traces = traces == null ? List.of() : List.copyOf(traces);
        operationResponses = operationResponses == null ? Map.of() : Map.copyOf(operationResponses);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
