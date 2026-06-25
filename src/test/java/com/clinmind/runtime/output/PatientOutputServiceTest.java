package com.clinmind.runtime.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.boundary.DecisionBoundaryService;
import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.reasoning.DifferentialDiagnosisBoardService;
import com.clinmind.runtime.reasoning.EvidenceGraphService;
import com.clinmind.runtime.reasoning.QuestionTestPolicyService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PatientOutputServiceTest {

    @Autowired
    private PatientOutputService patientOutputService;

    @Autowired
    private DecisionBoundaryService decisionBoundaryService;

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Autowired
    private SafetyGateService safetyGateService;

    @Autowired
    private DifferentialDiagnosisBoardService differentialDiagnosisBoardService;

    @Autowired
    private EvidenceGraphService evidenceGraphService;

    @Autowired
    private QuestionTestPolicyService questionTestPolicyService;

    @Test
    void asksFollowUpQuestionWithoutDiagnosisLabel() {
        RuntimeState state = buildChestPainState("胸口闷，活动后更明显");
        preparePolicy(state);

        PatientOutput output = patientOutputService.buildPatientOutput(state);

        assertThat(output.allowed()).isTrue();
        assertThat(output.content()).contains("确认一个问题");
        assertThat(output.content()).doesNotContain("acute_coronary_syndrome");
        assertThat(output.outputLevel()).isEqualTo(OutputLevel.O1_CONTINUE_QUESTIONING);
    }

    @Test
    void doesNotProvideLowRiskReassuranceWhenSafetyGateTriggered() {
        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗");
        state.setSafetyGate(safetyGateService.evaluateSafety(state));
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
        state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));
        state.setDecisionBoundary(decisionBoundaryService.decideOutputBoundary(state));

        PatientOutput output = patientOutputService.buildPatientOutput(state);

        assertThat(output.allowed()).isTrue();
        assertThat(output.content()).contains("风险信号");
        assertThat(output.content()).doesNotContain("不用担心");
        assertThat(output.outputLevel()).isEqualTo(OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION);
    }

    private void preparePolicy(RuntimeState state) {
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
        state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));
        state.setDecisionBoundary(decisionBoundaryService.decideOutputBoundary(state));
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
