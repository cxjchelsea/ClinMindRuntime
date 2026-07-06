package com.clinmind.runtime.modelgov;

import java.util.List;

public record PolicyDecision(
        boolean allowed,
        List<String> reasons
) {
    public PolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static PolicyDecision allow() {
        return new PolicyDecision(true, List.of());
    }

    public static PolicyDecision reject(List<String> reasons) {
        return new PolicyDecision(false, reasons);
    }
}
