package com.clinmind.runtime.agent;

import java.util.List;

public record AgentPolicyDecision(
        boolean allowed,
        List<String> reasons
) {
    public AgentPolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static AgentPolicyDecision allow() {
        return new AgentPolicyDecision(true, List.of());
    }

    public static AgentPolicyDecision reject(String reason) {
        return new AgentPolicyDecision(false, List.of(reason));
    }

    public static AgentPolicyDecision reject(List<String> reasons) {
        return new AgentPolicyDecision(false, reasons);
    }
}
