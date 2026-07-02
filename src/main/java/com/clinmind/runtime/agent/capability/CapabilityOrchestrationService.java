package com.clinmind.runtime.agent.capability;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentExecutionResult;
import com.clinmind.runtime.agent.AgentExecutionStatus;
import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.agent.inquiry.InquiryPlanningInput;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.runtime.AgentRuntime;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.DifferentialDiagnosisBoard;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CapabilityOrchestrationService {

    private final AgentRuntime agentRuntime;

    public CapabilityOrchestrationService(AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }

    @TraceStep("CapabilityOrchestration")
    public AgentOrchestrationSnapshot orchestrate(RuntimeState state) {
        if (state == null) {
            return AgentOrchestrationSnapshot.skipped();
        }

        SafetyGateResult safetyGate = state.getSafetyGate();
        if (safetyGate != null && safetyGate.failSafeRequired()) {
            return AgentOrchestrationSnapshot.skipped();
        }

        InquiryPlanningInput input = buildInput(state);
        if (input.missingFacts().isEmpty()) {
            return AgentOrchestrationSnapshot.skipped();
        }

        AgentExecutionResult result = agentRuntime.runInquiryPlanning(input);
        List<InquiryQuestionCandidate> accepted = agentRuntime.acceptedQuestions(result);

        if (result.status() == AgentExecutionStatus.POLICY_REJECTED
                || result.status() == AgentExecutionStatus.VALIDATION_REJECTED
                || result.status() == AgentExecutionStatus.FAILED) {
            return new AgentOrchestrationSnapshot(
                    result.executionId(),
                    result.agentId(),
                    result.status(),
                    List.of(),
                    result.warnings(),
                    true,
                    result.trace());
        }

        return new AgentOrchestrationSnapshot(
                result.executionId(),
                result.agentId(),
                result.status(),
                accepted,
                result.warnings(),
                accepted.isEmpty(),
                result.trace());
    }

    private InquiryPlanningInput buildInput(RuntimeState state) {
        CaseFrame caseFrame = state.getCaseFrame();
        List<String> missingFacts = caseFrame == null || caseFrame.missingSlots() == null
                ? List.of()
                : new ArrayList<>(caseFrame.missingSlots());

        List<String> knownFacts = new ArrayList<>();
        if (caseFrame != null) {
            if (caseFrame.chiefComplaint() != null && !caseFrame.chiefComplaint().isBlank()) {
                knownFacts.add(caseFrame.chiefComplaint());
            }
            caseFrame.symptoms().forEach(symptom -> knownFacts.add(symptom.name()));
        }

        List<String> redFlags = new ArrayList<>();
        KnowledgeContext knowledge = state.getKnowledgeContext();
        if (knowledge != null) {
            knowledge.mustNotMiss().forEach(ref -> redFlags.add(ref.name()));
            knowledge.redFlags().forEach(rule -> redFlags.add(rule.ruleId()));
        }

        Map<String, Object> caseFrameSummary = new LinkedHashMap<>();
        if (caseFrame != null) {
            if (caseFrame.chiefComplaint() != null) {
                caseFrameSummary.put("chief_complaint", caseFrame.chiefComplaint());
            }
            if (caseFrame.patientProfile() != null) {
                if (caseFrame.patientProfile().age() != null) {
                    caseFrameSummary.put("age", caseFrame.patientProfile().age());
                }
                if (caseFrame.patientProfile().sex() != null) {
                    caseFrameSummary.put("sex", caseFrame.patientProfile().sex());
                }
            }
            caseFrameSummary.put("known_facts", List.copyOf(knownFacts));
            caseFrameSummary.put("missing_facts", List.copyOf(missingFacts));
        }

        String symptomGroup = state.getEntryAssessment() == null
                ? "unknown"
                : state.getEntryAssessment().symptomGroup();

        return new InquiryPlanningInput(
                state.getRuntimeId(),
                state.getSessionId(),
                symptomGroup,
                caseFrameSummary,
                knownFacts,
                missingFacts,
                redFlags,
                List.of(),
                summarizeDdx(state.getDifferentialBoard()),
                summarizeEvidence(state.getEvidenceGraph()),
                List.of("duration", "severity", "associated_symptom", "history", "red_flag"),
                AgentConstants.DEFAULT_MAX_QUESTION_COUNT,
                Map.of());
    }

    private String summarizeDdx(DifferentialDiagnosisBoard board) {
        if (board == null || board.candidates().isEmpty()) {
            return "";
        }
        return board.candidates().size() + " candidates";
    }

    private String summarizeEvidence(EvidenceGraph graph) {
        if (graph == null || graph.items().isEmpty()) {
            return "";
        }
        return graph.items().size() + " items";
    }
}
