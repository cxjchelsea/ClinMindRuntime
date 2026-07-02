package com.clinmind.runtime.evidence.graph.capability;

import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;
import com.clinmind.runtime.evidence.graph.GraphEvidenceSnapshot;
import com.clinmind.runtime.evidence.graph.GraphEvidenceStatus;
import com.clinmind.runtime.evidence.graph.runtime.GraphEvidenceRuntime;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GraphEvidenceCapabilityOrchestrator {

    private final GraphEvidenceRuntime graphEvidenceRuntime;

    public GraphEvidenceCapabilityOrchestrator(GraphEvidenceRuntime graphEvidenceRuntime) {
        this.graphEvidenceRuntime = graphEvidenceRuntime;
    }

    @TraceStep("GraphEvidenceCapabilityOrchestration")
    public GraphEvidenceSnapshot orchestrate(RuntimeState state) {
        if (state == null) {
            return GraphEvidenceSnapshot.skipped();
        }

        SafetyGateResult safetyGate = state.getSafetyGate();
        if (safetyGate != null && safetyGate.failSafeRequired()) {
            return GraphEvidenceSnapshot.skipped();
        }

        EvidenceRetrievalSnapshot evidenceRetrieval = state.getEvidenceRetrieval();
        if (evidenceRetrieval == null || evidenceRetrieval.acceptedCandidates().isEmpty()) {
            return GraphEvidenceSnapshot.skipped();
        }

        String symptomGroup = state.getEntryAssessment() == null
                ? "unknown"
                : state.getEntryAssessment().symptomGroup();

        GraphEvidenceRequest request = buildRequest(state, symptomGroup, evidenceRetrieval);
        GraphEvidenceResult result = graphEvidenceRuntime.run(request);
        List<com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate> accepted =
                graphEvidenceRuntime.acceptedCandidates(result);

        boolean fallback = result.status() == GraphEvidenceStatus.NO_GRAPH_PATH_FOUND
                || result.status() == GraphEvidenceStatus.POLICY_REJECTED
                || result.status() == GraphEvidenceStatus.FAILED
                || result.status() == GraphEvidenceStatus.VALIDATION_REJECTED
                || accepted.isEmpty();

        return new GraphEvidenceSnapshot(
                result.graphRetrievalId(),
                result.providerId(),
                result.status(),
                accepted,
                result.warnings(),
                fallback,
                result.graphTrace());
    }

    private GraphEvidenceRequest buildRequest(
            RuntimeState state, String symptomGroup, EvidenceRetrievalSnapshot evidenceRetrieval) {
        CaseFrame caseFrame = state.getCaseFrame();
        List<String> knownFacts = new ArrayList<>();
        if (caseFrame != null) {
            if (caseFrame.chiefComplaint() != null && !caseFrame.chiefComplaint().isBlank()) {
                knownFacts.add(caseFrame.chiefComplaint());
            }
            caseFrame.symptoms().forEach(symptom -> knownFacts.add(symptom.name()));
        }

        Map<String, Object> caseFrameSummary = new LinkedHashMap<>();
        caseFrameSummary.put("known_facts", List.copyOf(knownFacts));

        List<String> ddxSummary = state.getDifferentialBoard() == null
                ? List.of()
                : state.getDifferentialBoard().candidates().stream().map(DDxCandidate::name).toList();

        return new GraphEvidenceRequest(
                IdGenerator.graphRetrievalId(),
                state.getRuntimeId(),
                symptomGroup,
                caseFrameSummary,
                knownFacts,
                evidenceRetrieval.acceptedCandidates(),
                ddxSummary,
                evidenceRetrieval,
                GraphConstants.DEFAULT_MAX_PATH_DEPTH,
                GraphConstants.DEFAULT_MAX_PATH_COUNT);
    }
}
