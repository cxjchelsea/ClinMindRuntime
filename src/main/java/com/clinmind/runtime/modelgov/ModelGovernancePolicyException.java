package com.clinmind.runtime.modelgov;

import java.util.List;

public class ModelGovernancePolicyException extends RuntimeException {

    private final List<String> reasons;

    public ModelGovernancePolicyException(List<String> reasons) {
        super(String.join("; ", reasons == null ? List.of("model governance policy rejected") : reasons));
        this.reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public List<String> reasons() {
        return reasons;
    }
}
