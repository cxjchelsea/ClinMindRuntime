package com.clinmind.runtime.output;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ClinicianReport;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.EvidenceGraphItem;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ClinicianReportService {

    public ClinicianReport buildClinicianReport(RuntimeState state) {
        DecisionBoundaryResult boundary = state.getDecisionBoundary();
        if (!isClinicianReportAllowed(state.getMode(), boundary)) {
            return new ClinicianReport();
        }

        CaseFrame caseFrame = state.getCaseFrame();
        SafetyGateResult safetyGate = state.getSafetyGate();
        KnowledgeContext knowledge = state.getKnowledgeContext();
        QuestionTestPolicyResult policy = state.getQuestionTestPolicy();

        return new ClinicianReport(
                true,
                buildCaseSummary(caseFrame),
                buildSafetySummary(safetyGate),
                state.getDifferentialBoard().candidates(),
                state.getEvidenceGraph(),
                collectRecommendedQuestions(policy, knowledge, state.getEvidenceGraph()),
                collectRecommendedTests(state.getEvidenceGraph(), knowledge, policy));
    }

    private boolean isClinicianReportAllowed(RuntimeMode mode, DecisionBoundaryResult boundary) {
        if (mode != RuntimeMode.CLINICIAN_COPILOT && mode != RuntimeMode.DEBUG) {
            return false;
        }
        return boundary != null && boundary.clinicianDdxAllowed();
    }

    private String buildCaseSummary(CaseFrame caseFrame) {
        if (caseFrame == null) {
            return null;
        }
        StringBuilder summary = new StringBuilder();
        if (caseFrame.chiefComplaint() != null) {
            summary.append("主诉：").append(caseFrame.chiefComplaint()).append('。');
        }
        if (!caseFrame.symptoms().isEmpty()) {
            summary.append(" 已识别症状数：").append(caseFrame.symptoms().size()).append('。');
        }
        if (!caseFrame.missingSlots().isEmpty()) {
            summary.append(" 缺失信息：").append(String.join(", ", caseFrame.missingSlots())).append('。');
        }
        return summary.isEmpty() ? null : summary.toString().strip();
    }

    private String buildSafetySummary(SafetyGateResult safetyGate) {
        if (safetyGate == null || !safetyGate.triggered()) {
            return "未触发危险信号规则。";
        }
        return "已触发危险信号。匹配规则："
                + String.join(", ", safetyGate.matchedRules())
                + "。建议动作：" + safetyGate.requiredAction();
    }

    private List<String> collectRecommendedQuestions(
            QuestionTestPolicyResult policy,
            KnowledgeContext knowledge,
            EvidenceGraph evidenceGraph) {
        Set<String> questions = new LinkedHashSet<>();
        if (policy != null && policy.nextAction() != null
                && policy.nextAction().type() == NextActionType.ASK_QUESTION) {
            questions.add(policy.nextAction().content());
        }
        if (knowledge != null) {
            questions.addAll(knowledge.requiredQuestions());
        }
        if (evidenceGraph != null) {
            for (EvidenceGraphItem item : evidenceGraph.items()) {
                questions.addAll(item.nextQuestions());
            }
        }
        return List.copyOf(questions);
    }

    private List<String> collectRecommendedTests(
            EvidenceGraph evidenceGraph,
            KnowledgeContext knowledge,
            QuestionTestPolicyResult policy) {
        Set<String> tests = new LinkedHashSet<>();
        if (policy != null && policy.nextAction() != null
                && policy.nextAction().type() == NextActionType.RECOMMEND_TEST) {
            for (String part : policy.nextAction().content().split("、")) {
                tests.add(part.strip());
            }
        }
        if (evidenceGraph != null) {
            for (EvidenceGraphItem item : evidenceGraph.items()) {
                tests.addAll(item.recommendedTests());
            }
        }
        if (knowledge != null) {
            tests.addAll(knowledge.recommendedTests());
        }
        return List.copyOf(tests);
    }
}
