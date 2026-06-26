package com.clinmind.runtime.state;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static String runtimeId() {
        return "rt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String traceId() {
        return "trace_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String evalRunId() {
        return "eval_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String capabilityProposalId() {
        return "cap_prop_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
