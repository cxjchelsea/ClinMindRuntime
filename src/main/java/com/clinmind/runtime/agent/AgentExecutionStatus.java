package com.clinmind.runtime.agent;

public enum AgentExecutionStatus {
    SUCCESS,
    POLICY_REJECTED,
    VALIDATION_REJECTED,
    PARTIALLY_ACCEPTED,
    DEGRADED,
    FAILED
}
