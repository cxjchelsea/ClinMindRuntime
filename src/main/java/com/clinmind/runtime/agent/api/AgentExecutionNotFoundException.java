package com.clinmind.runtime.agent.api;

public class AgentExecutionNotFoundException extends RuntimeException {

    public AgentExecutionNotFoundException(String executionId) {
        super("Agent execution not found: " + executionId);
    }
}
