package com.clinmind.runtime.toolgov;

import java.util.List;

public class ToolGovernancePolicyException extends RuntimeException {

    private final List<String> reasons;

    public ToolGovernancePolicyException(List<String> reasons) {
        super(String.join("; ", reasons));
        this.reasons = List.copyOf(reasons);
    }

    public List<String> reasons() {
        return reasons;
    }
}
