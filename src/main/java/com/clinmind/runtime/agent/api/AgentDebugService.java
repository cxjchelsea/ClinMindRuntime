package com.clinmind.runtime.agent.api;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentExecutionResult;
import com.clinmind.runtime.agent.AgentMetadata;
import com.clinmind.runtime.agent.AgentTrace;
import com.clinmind.runtime.agent.api.dto.AgentExecutionSafeDto;
import com.clinmind.runtime.agent.api.dto.AgentPolicyDecisionDto;
import com.clinmind.runtime.agent.api.dto.AgentProposalSafeDto;
import com.clinmind.runtime.agent.api.dto.AgentProposalSummaryDto;
import com.clinmind.runtime.agent.api.dto.AgentRegistryItemDto;
import com.clinmind.runtime.agent.api.dto.AgentRegistryResponse;
import com.clinmind.runtime.agent.api.dto.AgentTraceSummaryDto;
import com.clinmind.runtime.agent.api.dto.AgentValidationResultDto;
import com.clinmind.runtime.agent.api.dto.CaseFrameSummaryRequest;
import com.clinmind.runtime.agent.api.dto.InquiryPlanningRunRequest;
import com.clinmind.runtime.agent.api.dto.InquiryPlanningRunResponse;
import com.clinmind.runtime.agent.api.dto.InquiryQuestionCandidateDto;
import com.clinmind.runtime.agent.inquiry.InquiryPlanProposal;
import com.clinmind.runtime.agent.inquiry.InquiryPlanningInput;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.registry.AgentRegistry;
import com.clinmind.runtime.agent.runtime.AgentExecutionStore;
import com.clinmind.runtime.agent.runtime.AgentRuntime;
import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AgentDebugService {

    private final AgentRuntime agentRuntime;
    private final AgentRegistry agentRegistry;
    private final AgentExecutionStore executionStore;
    private final AuditLogService auditLogService;

    public AgentDebugService(
            AgentRuntime agentRuntime,
            AgentRegistry agentRegistry,
            AgentExecutionStore executionStore,
            AuditLogService auditLogService) {
        this.agentRuntime = agentRuntime;
        this.agentRegistry = agentRegistry;
        this.executionStore = executionStore;
        this.auditLogService = auditLogService;
    }

    public InquiryPlanningRunResponse runInquiryPlanning(InquiryPlanningRunRequest request) {
        validateRunRequest(request);
        InquiryPlanningInput input = toInput(request);
        AgentExecutionResult result = agentRuntime.runInquiryPlanning(input);
        auditLogService.record(
                AuditActionType.RUN_AGENT_INQUIRY_PLANNING,
                AuditResourceType.AGENT_EXECUTION,
                result.executionId(),
                AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", result.runtimeId(),
                        "agent_id", result.agentId(),
                        "status", result.status().name()));
        return toRunResponse(result);
    }

    public AgentExecutionSafeDto getExecution(String executionId) {
        AgentExecutionResult result = executionStore
                .findById(executionId)
                .orElseThrow(() -> new AgentExecutionNotFoundException(executionId));
        auditLogService.record(
                AuditActionType.QUERY_AGENT_EXECUTION,
                AuditResourceType.AGENT_EXECUTION,
                executionId,
                AuditResultStatus.SUCCESS,
                Map.of("status", result.status().name()));
        return toExecutionSafeDto(result);
    }

    public AgentRegistryResponse listRegistry() {
        List<AgentRegistryItemDto> agents = agentRegistry.listAll().stream()
                .map(this::toRegistryItem)
                .toList();
        auditLogService.record(
                AuditActionType.QUERY_AGENT_REGISTRY,
                AuditResourceType.AGENT_EXECUTION,
                "registry",
                AuditResultStatus.SUCCESS,
                Map.of("count", agents.size()));
        return new AgentRegistryResponse(agents);
    }

    private void validateRunRequest(InquiryPlanningRunRequest request) {
        if (request.runtimeId() == null || request.runtimeId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "runtime_id is required");
        }
        if (request.symptomGroup() == null || request.symptomGroup().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "symptom_group is required");
        }
        if (request.caseFrameSummary() == null
                || request.caseFrameSummary().missingFacts() == null
                || request.caseFrameSummary().missingFacts().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "missing_facts must not be empty");
        }
    }

    private InquiryPlanningInput toInput(InquiryPlanningRunRequest request) {
        CaseFrameSummaryRequest summary = request.caseFrameSummary();
        Map<String, Object> caseFrameSummary = new LinkedHashMap<>();
        if (summary.age() != null) {
            caseFrameSummary.put("age", summary.age());
        }
        if (summary.sex() != null) {
            caseFrameSummary.put("sex", summary.sex());
        }
        if (summary.chiefComplaint() != null) {
            caseFrameSummary.put("chief_complaint", summary.chiefComplaint());
        }
        if (summary.knownFacts() != null) {
            caseFrameSummary.put("known_facts", summary.knownFacts());
        }
        caseFrameSummary.put("missing_facts", summary.missingFacts());

        int maxQuestionCount = request.maxQuestionCount() == null
                ? AgentConstants.DEFAULT_MAX_QUESTION_COUNT
                : Math.min(request.maxQuestionCount(), AgentConstants.ABSOLUTE_MAX_QUESTION_COUNT);

        return new InquiryPlanningInput(
                request.runtimeId(),
                null,
                request.symptomGroup(),
                caseFrameSummary,
                summary.knownFacts() == null ? List.of() : summary.knownFacts(),
                summary.missingFacts(),
                request.redFlagCandidates() == null ? List.of() : request.redFlagCandidates(),
                request.currentQuestionsAsked() == null ? List.of() : request.currentQuestionsAsked(),
                null,
                null,
                request.allowedQuestionTypes() == null ? List.of() : request.allowedQuestionTypes(),
                maxQuestionCount,
                Map.of());
    }

    private InquiryPlanningRunResponse toRunResponse(AgentExecutionResult result) {
        AgentProposalSafeDto proposalDto = null;
        if (result.proposal() instanceof InquiryPlanProposal proposal) {
            proposalDto = toProposalDto(proposal);
        }
        AgentValidationResultDto validationDto = result.validationResult() == null
                ? null
                : new AgentValidationResultDto(
                        result.validationResult().status().name(),
                        result.validationResult().acceptedQuestionIds(),
                        result.validationResult().rejectedQuestionIds(),
                        result.validationResult().reasons());
        AgentPolicyDecisionDto policyDto = result.policyDecision() == null
                ? null
                : new AgentPolicyDecisionDto(
                        result.policyDecision().allowed(), result.policyDecision().reasons());
        AgentTrace trace = result.trace();
        AgentTraceSummaryDto traceSummary = trace == null
                ? new AgentTraceSummaryDto(null, false)
                : new AgentTraceSummaryDto(trace.traceId(), true);

        return new InquiryPlanningRunResponse(
                result.executionId(),
                result.runtimeId(),
                result.agentId(),
                resolveAgentVersion(result.agentId()),
                result.status().name(),
                policyDto,
                proposalDto,
                validationDto,
                traceSummary,
                result.errorCode(),
                result.warnings());
    }

    private AgentExecutionSafeDto toExecutionSafeDto(AgentExecutionResult result) {
        AgentProposalSummaryDto proposalSummary = null;
        if (result.proposal() instanceof InquiryPlanProposal proposal) {
            proposalSummary = new AgentProposalSummaryDto(
                    proposal.proposalId(), proposal.proposalType(), proposal.proposedQuestions().size());
        }
        return new AgentExecutionSafeDto(
                result.executionId(),
                result.runtimeId(),
                result.agentId(),
                result.status().name(),
                result.startedAt(),
                result.finishedAt(),
                proposalSummary,
                result.validationResult() == null ? null : result.validationResult().status().name(),
                result.trace() != null,
                result.errorCode(),
                result.warnings());
    }

    private AgentRegistryItemDto toRegistryItem(AgentMetadata metadata) {
        return new AgentRegistryItemDto(
                metadata.agentId(),
                metadata.agentName(),
                metadata.agentVersion(),
                metadata.agentType().name(),
                metadata.enabled(),
                metadata.supportedSymptomGroups(),
                metadata.riskLevel().name(),
                metadata.allowedOutputs());
    }

    private AgentProposalSafeDto toProposalDto(InquiryPlanProposal proposal) {
        List<InquiryQuestionCandidateDto> questions = proposal.proposedQuestions().stream()
                .map(this::toQuestionDto)
                .toList();
        return new AgentProposalSafeDto(
                proposal.proposalId(),
                proposal.proposalType(),
                questions,
                proposal.reasoningSummary(),
                proposal.uncertaintyLevel(),
                proposal.safetyNotes());
    }

    private InquiryQuestionCandidateDto toQuestionDto(InquiryQuestionCandidate question) {
        return new InquiryQuestionCandidateDto(
                question.questionId(),
                question.questionText(),
                question.clinicalPurpose(),
                question.targetMissingFact(),
                question.priority().name(),
                question.riskRelated(),
                question.patientSafeWording(),
                question.expectedAnswerType());
    }

    private String resolveAgentVersion(String agentId) {
        return agentRegistry.findById(agentId).map(AgentMetadata::agentVersion).orElse(null);
    }
}
