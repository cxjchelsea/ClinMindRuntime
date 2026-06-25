package com.clinmind.runtime.reasoning;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.RuntimeTraceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuestionTestPolicyServiceTest {

    @Autowired
    private QuestionTestPolicyService questionTestPolicyService;

    @Autowired
    private EvidenceGraphService evidenceGraphService;

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Autowired
    private DifferentialDiagnosisBoardService differentialDiagnosisBoardService;

    @Autowired
    private SafetyGateService safetyGateService;

    @Autowired
    private RuntimeTraceCollector collector;

    @BeforeEach
    void setUp() {
        collector.clear();
    }

    @Test
    void asksQuestionWhenEvidenceMissing() {
        RuntimeState state = buildChestPainState("胸口闷，活动后更明显");
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));

        QuestionTestPolicyResult result = questionTestPolicyService.decideNextAction(state);

        assertThat(result.nextAction().type()).isEqualTo(NextActionType.ASK_QUESTION);
        assertThat(result.nextAction().content()).isNotBlank();
        assertThat(collector.getSteps()).extracting("moduleName").contains("QuestionTestPolicy");
    }

    @Test
    void recommendsVisitWhenSafetyGateTriggered() {
        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗");
        state.setSafetyGate(safetyGateService.evaluateSafety(state));
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));

        QuestionTestPolicyResult result = questionTestPolicyService.decideNextAction(state);

        assertThat(result.nextAction().type()).isEqualTo(NextActionType.RECOMMEND_VISIT);
        assertThat(result.nextAction().priority()).isEqualTo("high");
    }

    @Test
    void recommendsTestForHighRiskExclusion() {
        RuntimeState state = buildChestPainState("胸口闷");
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));

        QuestionTestPolicyResult result = questionTestPolicyService.decideNextAction(state);

        assertThat(result.nextAction().type()).isIn(NextActionType.ASK_QUESTION, NextActionType.RECOMMEND_TEST);
    }

    private RuntimeState buildChestPainState(String text) {
        RuntimeState state = RuntimeState.createDefault("s_001");
        state.getInputHistory().add(new UserInput(text, java.util.List.of()));
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical", 0.8);
        state.setEntryAssessment(entry);
        state.setCaseFrame(new CaseFrame(text, null, null, null, null, null, null, null));
        state.setKnowledgeContext(knowledgeContextService.buildKnowledgeContext(state.getCaseFrame(), entry));
        return state;
    }
}
