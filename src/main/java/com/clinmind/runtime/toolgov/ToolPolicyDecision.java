package com.clinmind.runtime.toolgov;

import java.util.List;

public record ToolPolicyDecision(boolean allowed, boolean skipped, List<String> reasons) {

    public ToolPolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static ToolPolicyDecision allow() {
        return new ToolPolicyDecision(true, false, List.of());
    }

    public static ToolPolicyDecision reject(List<String> reasons) {
        return new ToolPolicyDecision(false, false, reasons);
    }

    public static ToolPolicyDecision skip(List<String> reasons) {
        return new ToolPolicyDecision(false, true, reasons);
    }
}
