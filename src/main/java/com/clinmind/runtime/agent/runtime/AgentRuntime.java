package com.clinmind.runtime.agent.runtime;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentContext;
import com.clinmind.runtime.agent.AgentExecutionRequest;
import com.clinmind.runtime.agent.AgentExecutionResult;
import com.clinmind.runtime.agent.AgentExecutionStatus;
import com.clinmind.runtime.agent.AgentMetadata;
import com.clinmind.runtime.agent.AgentPolicyContext;
import com.clinmind.runtime.agent.AgentPolicyDecision;
import com.clinmind.runtime.agent.AgentProposal;
import com.clinmind.runtime.agent.AgentTrace;
import com.clinmind.runtime.agent.AgentValidationResult;
import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.agent.inquiry.InquiryPlanProposal;
import com.clinmind.runtime.agent.inquiry.InquiryPlanningAgent;
import com.clinmind.runtime.agent.inquiry.InquiryPlanningInput;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.policy.AgentPolicy;
import com.clinmind.runtime.agent.registry.AgentRegistry;
import com.clinmind.runtime.agent.validation.RuntimeValidationService;
import com.clinmind.runtime.agent.validation.ValidationContext;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.trace.TraceStep;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AgentRuntime {

    private final AgentRegistry agentRegistry;
    private final AgentPolicy agentPolicy;
    private final InquiryPlanningAgent inquiryPlanningAgent;
    private final RuntimeValidationService runtimeValidationService;
    private final AgentExecutionStore executionStore;

    public AgentRuntime(
            AgentRegistry agentRegistry,
            AgentPolicy agentPolicy,
            InquiryPlanningAgent inquiryPlanningAgent,
            RuntimeValidationService runtimeValidationService,
            AgentExecutionStore executionStore) {
        this.agentRegistry = agentRegistry;
        this.agentPolicy = agentPolicy;
        this.inquiryPlanningAgent = inquiryPlanningAgent;
        this.runtimeValidationService = runtimeValidationService;
        this.executionStore = executionStore;
    }

    @TraceStep("AgentRuntime")
    public AgentExecutionResult runInquiryPlanning(InquiryPlanningInput input) {
        Instant startedAt = Instant.now();
        String executionId = IdGenerator.agentExecutionId();
        String agentId = AgentConstants.INQUIRY_PLANNING_AGENT_ID;

        AgentMetadata metadata = agentRegistry.findById(agentId).orElse(null);
        if (metadata == null) {
            return saveAndReturn(failedResult(
                    executionId,
                    input.runtimeId(),
                    agentId,
                    startedAt,
                    "AGENT_NOT_REGISTERED",
                    List.of("agent not registered"),
                    null));
        }

        AgentPolicyContext policyContext = new AgentPolicyContext(
                input.runtimeId(),
                input.sessionId(),
                input.symptomGroup(),
                input.missingFacts(),
                input.redFlagCandidates(),
                false,
                metadata.enabled(),
                input.capabilityProfileSnapshot());

        AgentPolicyDecision policyDecision = agentPolicy.evaluate(agentId, policyContext);
        if (!policyDecision.allowed()) {
            AgentTrace trace = buildTrace(
                    executionId,
                    input,
                    metadata,
                    policyDecision,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    policyDecision.reasons());
            AgentExecutionResult result = new AgentExecutionResult(
                    executionId,
                    input.runtimeId(),
                    agentId,
                    AgentExecutionStatus.POLICY_REJECTED,
                    null,
                    null,
                    policyDecision,
                    trace,
                    List.of(),
                    "AGENT_POLICY_REJECTED",
                    startedAt,
                    Instant.now());
            return saveAndReturn(result);
        }

        try {
            InquiryPlanProposal proposal = inquiryPlanningAgent.plan(input);
            ValidationContext validationContext = new ValidationContext(
                    input.runtimeId(),
                    input.missingFacts(),
                    input.redFlagCandidates(),
                    input.maxQuestionCount());
            AgentValidationResult validationResult =
                    runtimeValidationService.validateProposal(proposal, policyContext, validationContext);

            AgentExecutionStatus status = mapValidationStatus(validationResult.status());
            AgentTrace trace = buildTrace(
                    executionId,
                    input,
                    metadata,
                    policyDecision,
                    validationResult.status(),
                    proposal,
                    validationResult.acceptedQuestionIds(),
                    validationResult.rejectedQuestionIds(),
                    validationResult.reasons());

            AgentExecutionResult result = new AgentExecutionResult(
                    executionId,
                    input.runtimeId(),
                    agentId,
                    status,
                    proposal,
                    validationResult,
                    policyDecision,
                    trace,
                    validationResult.reasons(),
                    status == AgentExecutionStatus.VALIDATION_REJECTED
                            ? "AGENT_PROPOSAL_VALIDATION_REJECTED"
                            : null,
                    startedAt,
                    Instant.now());
            return saveAndReturn(result);
        } catch (RuntimeException ex) {
            AgentTrace trace = buildTrace(
                    executionId,
                    input,
                    metadata,
                    policyDecision,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of(ex.getMessage()));
            return saveAndReturn(new AgentExecutionResult(
                    executionId,
                    input.runtimeId(),
                    agentId,
                    AgentExecutionStatus.FAILED,
                    null,
                    null,
                    policyDecision,
                    trace,
                    List.of(ex.getMessage()),
                    "AGENT_EXECUTION_FAILED",
                    startedAt,
                    Instant.now()));
        }
    }

    public AgentExecutionResult validateExistingProposal(
            InquiryPlanProposal proposal, AgentPolicyContext policyContext, ValidationContext validationContext) {
        AgentValidationResult validationResult =
                runtimeValidationService.validateProposal(proposal, policyContext, validationContext);
        AgentExecutionStatus status = mapValidationStatus(validationResult.status());
        return new AgentExecutionResult(
                IdGenerator.agentExecutionId(),
                proposal.runtimeId(),
                proposal.agentId(),
                status,
                proposal,
                validationResult,
                agentPolicy.evaluate(proposal.agentId(), policyContext),
                null,
                validationResult.reasons(),
                status == AgentExecutionStatus.VALIDATION_REJECTED
                        ? "AGENT_PROPOSAL_VALIDATION_REJECTED"
                        : null,
                Instant.now(),
                Instant.now());
    }

    private AgentExecutionStatus mapValidationStatus(ProposalValidationStatus status) {
        return switch (status) {
            case ACCEPTED -> AgentExecutionStatus.SUCCESS;
            case PARTIALLY_ACCEPTED -> AgentExecutionStatus.PARTIALLY_ACCEPTED;
            case DEGRADED -> AgentExecutionStatus.DEGRADED;
            case REJECTED -> AgentExecutionStatus.VALIDATION_REJECTED;
        };
    }

    private AgentTrace buildTrace(
            String executionId,
            InquiryPlanningInput input,
            AgentMetadata metadata,
            AgentPolicyDecision policyDecision,
            ProposalValidationStatus validationDecision,
            AgentProposal proposal,
            List<String> acceptedQuestionIds,
            List<String> rejectedQuestionIds,
            List<String> rejectionReasons) {
        Map<String, Object> inputSummary = new LinkedHashMap<>();
        inputSummary.put("runtime_id", input.runtimeId());
        inputSummary.put("symptom_group", input.symptomGroup());
        inputSummary.put("missing_fact_count", input.missingFacts().size());
        inputSummary.put("red_flag_count", input.redFlagCandidates().size());

        Map<String, Object> outputSummary = new LinkedHashMap<>();
        if (proposal instanceof InquiryPlanProposal inquiryPlan) {
            outputSummary.put("proposal_id", inquiryPlan.proposalId());
            outputSummary.put("question_count", inquiryPlan.proposedQuestions().size());
        }

        return new AgentTrace(
                IdGenerator.agentTraceId(),
                executionId,
                input.runtimeId(),
                metadata.agentId(),
                metadata.agentVersion(),
                inputSummary,
                outputSummary,
                policyDecision,
                validationDecision,
                acceptedQuestionIds,
                rejectedQuestionIds,
                rejectionReasons,
                Instant.now());
    }

    private AgentExecutionResult failedResult(
            String executionId,
            String runtimeId,
            String agentId,
            Instant startedAt,
            String errorCode,
            List<String> warnings,
            AgentPolicyDecision policyDecision) {
        return new AgentExecutionResult(
                executionId,
                runtimeId,
                agentId,
                AgentExecutionStatus.FAILED,
                null,
                null,
                policyDecision,
                null,
                warnings,
                errorCode,
                startedAt,
                Instant.now());
    }

    private AgentExecutionResult saveAndReturn(AgentExecutionResult result) {
        executionStore.save(result);
        return result;
    }

    public List<InquiryQuestionCandidate> acceptedQuestions(AgentExecutionResult result) {
        if (result == null || result.proposal() == null || result.validationResult() == null) {
            return List.of();
        }
        if (!(result.proposal() instanceof InquiryPlanProposal inquiryPlan)) {
            return List.of();
        }
        List<String> acceptedIds = result.validationResult().acceptedQuestionIds();
        return inquiryPlan.proposedQuestions().stream()
                .filter(question -> acceptedIds.contains(question.questionId()))
                .toList();
    }
}
