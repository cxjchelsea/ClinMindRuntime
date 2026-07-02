package com.clinmind.runtime.evidence;

import java.util.List;

public record EvidencePolicyDecision(
        boolean allowed,
        List<String> reasons
) {
    public EvidencePolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static EvidencePolicyDecision allow() {
        return new EvidencePolicyDecision(true, List.of());
    }

    public static EvidencePolicyDecision reject(String reason) {
        return new EvidencePolicyDecision(false, List.of(reason));
    }

    public static EvidencePolicyDecision reject(List<String> reasons) {
        return new EvidencePolicyDecision(false, reasons);
    }
}
