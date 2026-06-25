package com.clinmind.runtime.reasoning;

import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.DifferentialDiagnosisBoard;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RiskLevel;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DifferentialDiagnosisBoardService {

    @TraceStep("DifferentialDiagnosisBoard")
    public DifferentialDiagnosisBoard buildDifferentialBoard(RuntimeState state) {
        KnowledgeContext knowledge = state.getKnowledgeContext();
        SafetyGateResult safetyGate = state.getSafetyGate();
        boolean highRiskActive = safetyGate != null && safetyGate.triggered();

        List<DDxCandidate> candidates = new ArrayList<>();
        Map<String, DDxCandidate> byName = new LinkedHashMap<>();

        for (DiagnosisRef diagnosis : knowledge.mustNotMiss()) {
            CandidateStatus status = highRiskActive ? CandidateStatus.NEED_TO_RULE_OUT : CandidateStatus.MUST_NOT_MISS;
            DDxCandidate candidate = new DDxCandidate(
                    diagnosis.name(),
                    status,
                    diagnosis.riskLevel(),
                    highRiskActive ? "high risk not ruled out" : "must not miss diagnosis",
                    false);
            candidates.add(candidate);
            byName.put(diagnosis.name(), candidate);
        }

        for (DiagnosisRef diagnosis : knowledge.commonDiagnoses()) {
            if (byName.containsKey(diagnosis.name())) {
                continue;
            }
            CandidateStatus status = highRiskActive
                    ? CandidateStatus.POSSIBLE_AFTER_EXCLUSION
                    : CandidateStatus.POSSIBLE;
            candidates.add(new DDxCandidate(
                    diagnosis.name(),
                    status,
                    diagnosis.riskLevel(),
                    highRiskActive ? "retained after high risk exclusion" : "common diagnosis candidate",
                    false));
        }

        String updatedReason = highRiskActive
                ? "safety gate active; high risk candidates retained"
                : "initial differential board from static rules";

        return new DifferentialDiagnosisBoard(candidates, updatedReason);
    }
}
