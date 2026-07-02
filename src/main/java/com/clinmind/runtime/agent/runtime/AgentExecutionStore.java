package com.clinmind.runtime.agent.runtime;

import com.clinmind.runtime.agent.AgentExecutionResult;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AgentExecutionStore {

    private final java.util.concurrent.ConcurrentHashMap<String, AgentExecutionResult> executions =
            new java.util.concurrent.ConcurrentHashMap<>();

    public void save(AgentExecutionResult result) {
        if (result != null && result.executionId() != null) {
            executions.put(result.executionId(), result);
        }
    }

    public Optional<AgentExecutionResult> findById(String executionId) {
        return Optional.ofNullable(executions.get(executionId));
    }
}
