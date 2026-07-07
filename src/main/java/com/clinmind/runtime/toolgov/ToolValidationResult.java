package com.clinmind.runtime.toolgov;

import java.util.List;

public record ToolValidationResult(ToolValidationStatus status, List<String> reasons) {

    public ToolValidationResult {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static ToolValidationResult accepted() {
        return new ToolValidationResult(ToolValidationStatus.ACCEPTED, List.of());
    }

    public static ToolValidationResult rejected(List<String> reasons) {
        return new ToolValidationResult(ToolValidationStatus.REJECTED, reasons);
    }

    public boolean acceptedResult() {
        return status == ToolValidationStatus.ACCEPTED;
    }
}
