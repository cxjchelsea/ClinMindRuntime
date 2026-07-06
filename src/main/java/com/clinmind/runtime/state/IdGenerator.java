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

    public static String evidenceRetrievalId() {
        return "evidence_ret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String evidenceCandidateId() {
        return "ev_cand_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String evidenceTraceId() {
        return "evidence_trace_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String graphRetrievalId() {
        return "graph_ret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String graphCandidateId() {
        return "graph_cand_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String graphTraceId() {
        return "graph_trace_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String graphPathId() {
        return "graph_path_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String providerCallId() {
        return "provider_call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String providerTraceId() {
        return "provider_trace_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String providerRequestId() {
        return "provider_req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String providerQueryId() {
        return "provider_query_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String modelRegistryId() {
        return "model_reg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String promptRegistryId() {
        return "prompt_reg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String datasetVersionId() {
        return "dataset_ver_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String modelExperimentId() {
        return "model_exp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String modelEvaluationReportId() {
        return "model_report_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String modelReleaseCandidateId() {
        return "model_release_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String modelRollbackPlanId() {
        return "rollback_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
