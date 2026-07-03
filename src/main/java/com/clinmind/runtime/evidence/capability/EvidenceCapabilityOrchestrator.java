package com.clinmind.runtime.evidence.capability;

import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evidence.EvidenceRetrievalStatus;
import com.clinmind.runtime.evidence.runtime.EvidenceRetrievalRuntime;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EvidenceCapabilityOrchestrator {

    private final EvidenceRetrievalRuntime evidenceRetrievalRuntime;

    public EvidenceCapabilityOrchestrator(EvidenceRetrievalRuntime evidenceRetrievalRuntime) {
        this.evidenceRetrievalRuntime = evidenceRetrievalRuntime;
    }

    @TraceStep("EvidenceCapabilityOrchestration")
    public EvidenceRetrievalSnapshot orchestrate(RuntimeState state) {
        if (state == null) {
            return EvidenceRetrievalSnapshot.skipped();
        }

        SafetyGateResult safetyGate = state.getSafetyGate();
        if (safetyGate != null && safetyGate.failSafeRequired()) {
            return EvidenceRetrievalSnapshot.skipped();
        }

        String symptomGroup = state.getEntryAssessment() == null
                ? "unknown"
                : state.getEntryAssessment().symptomGroup();

        EvidenceRetrievalRequest request = buildRequest(state, symptomGroup);
        EvidenceRetrievalResult result = evidenceRetrievalRuntime.retrieve(request);
        state.setProviderEnhancement(evidenceRetrievalRuntime.consumeLastProviderEnhancement());
        List<com.clinmind.runtime.evidence.EvidenceCandidate> accepted =
                evidenceRetrievalRuntime.acceptedCandidates(result);

        boolean fallback = result.status() == EvidenceRetrievalStatus.NO_EVIDENCE_FOUND
                || result.status() == EvidenceRetrievalStatus.POLICY_REJECTED
                || result.status() == EvidenceRetrievalStatus.FAILED
                || result.status() == EvidenceRetrievalStatus.VALIDATION_REJECTED
                || accepted.isEmpty();

        return new EvidenceRetrievalSnapshot(
                result.retrievalId(),
                result.providerId(),
                result.status(),
                accepted,
                result.warnings(),
                fallback,
                result.queryTrace());
    }

    private EvidenceRetrievalRequest buildRequest(RuntimeState state, String symptomGroup) {
        CaseFrame caseFrame = state.getCaseFrame();
        List<String> knownFacts = new ArrayList<>();
        List<String> missingFacts = new ArrayList<>();
        if (caseFrame != null) {
            if (caseFrame.chiefComplaint() != null && !caseFrame.chiefComplaint().isBlank()) {
                knownFacts.add(caseFrame.chiefComplaint());
            }
            caseFrame.symptoms().forEach(symptom -> knownFacts.add(symptom.name()));
            if (caseFrame.missingSlots() != null) {
                missingFacts.addAll(caseFrame.missingSlots());
            }
        }

        List<String> redFlags = new ArrayList<>();
        KnowledgeContext knowledge = state.getKnowledgeContext();
        if (knowledge != null) {
            knowledge.mustNotMiss().forEach(ref -> redFlags.add(ref.name()));
            knowledge.redFlags().forEach(rule -> redFlags.add(rule.ruleId()));
        }
        SafetyGateResult safetyGate = state.getSafetyGate();
        if (safetyGate != null && safetyGate.triggered()) {
            redFlags.addAll(safetyGate.matchedRules());
        }

        Map<String, Object> caseFrameSummary = new LinkedHashMap<>();
        if (caseFrame != null) {
            if (caseFrame.chiefComplaint() != null) {
                caseFrameSummary.put("chief_complaint", caseFrame.chiefComplaint());
            }
            caseFrameSummary.put("known_facts", List.copyOf(knownFacts));
            caseFrameSummary.put("missing_facts", List.copyOf(missingFacts));
        }

        return new EvidenceRetrievalRequest(
                IdGenerator.evidenceRetrievalId(),
                state.getRuntimeId(),
                symptomGroup,
                caseFrameSummary,
                knownFacts,
                missingFacts,
                List.of(),
                redFlags,
                state.getAssetPackageId(),
                state.getAssetPackageVersion(),
                EvidenceConstants.DEFAULT_RETRIEVAL_LIMIT,
                "clinician_runtime");
    }
}
