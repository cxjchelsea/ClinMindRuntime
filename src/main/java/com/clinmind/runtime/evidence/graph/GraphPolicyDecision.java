package com.clinmind.runtime.evidence.graph;

import java.util.List;

public record GraphPolicyDecision(
        boolean allowed,
        List<String> reasons
) {
    public GraphPolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static GraphPolicyDecision allow() {
        return new GraphPolicyDecision(true, List.of());
    }

    public static GraphPolicyDecision reject(String reason) {
        return new GraphPolicyDecision(false, List.of(reason));
    }

    public static GraphPolicyDecision reject(List<String> reasons) {
        return new GraphPolicyDecision(false, reasons);
    }
}
