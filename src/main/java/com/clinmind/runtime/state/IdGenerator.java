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

    public static String candidateGenerationId() {
        return "cand_gen_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String candidateReviewId() {
        return "cand_rev_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String auditId() {
        return "audit_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String agentExecutionId() {
        return "agent_exec_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String agentProposalId() {
        return "proposal_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String agentTraceId() {
        return "agent_trace_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
