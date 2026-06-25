package com.clinmind.runtime.boundary;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.reasoning.DifferentialDiagnosisBoardService;
import com.clinmind.runtime.reasoning.EvidenceGraphService;
import com.clinmind.runtime.reasoning.QuestionTestPolicyService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.RuntimeTraceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DecisionBoundaryServiceTest {

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

    @Autowired
    private RuntimeTraceCollector collector;

    @BeforeEach
    void setUp() {
        collector.clear();
    }

    @Test
    void tightensPatientOutputWhenSafetyGateTriggered() {
        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗", RuntimeMode.PATIENT_FACING);
        state.setSafetyGate(safetyGateService.evaluateSafety(state));
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
        state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));

        DecisionBoundaryResult result = decisionBoundaryService.decideOutputBoundary(state);

        assertThat(result.allowedOutputLevel()).isEqualTo(OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION);
        assertThat(result.patientDiagnosisLabelAllowed()).isFalse();
        assertThat(result.constraints()).contains("no_low_risk_reassurance");
        assertThat(collector.getSteps()).extracting("moduleName").contains("DecisionBoundary");
    }

    @Test
    void allowsClinicianDdxInClinicianMode() {
        RuntimeState state = buildChestPainState("胸口闷，活动后更明显", RuntimeMode.CLINICIAN_COPILOT);
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
        state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));

        DecisionBoundaryResult result = decisionBoundaryService.decideOutputBoundary(state);

        assertThat(result.clinicianDdxAllowed()).isTrue();
        assertThat(result.allowedOutputLevel()).isEqualTo(OutputLevel.O1_CONTINUE_QUESTIONING);
    }

    @Test
    void failSafeBoundaryBlocksDiagnosisLabels() {
        DecisionBoundaryResult result = decisionBoundaryService.failSafeBoundary("test failure");

        assertThat(result.patientDiagnosisLabelAllowed()).isFalse();
        assertThat(result.clinicianDdxAllowed()).isFalse();
        assertThat(result.constraints()).contains("fail_safe");
    }

    private RuntimeState buildChestPainState(String text, RuntimeMode mode) {
        RuntimeState state = RuntimeState.createDefault("s_001");
        state.setMode(mode);
        state.getInputHistory().add(new UserInput(text, java.util.List.of()));
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical", 0.8);
        state.setEntryAssessment(entry);
        state.setCaseFrame(new CaseFrame(text, null, null, null, null, null, null, null));
        state.setKnowledgeContext(knowledgeContextService.buildKnowledgeContext(state.getCaseFrame(), entry));
        return state;
    }
}
