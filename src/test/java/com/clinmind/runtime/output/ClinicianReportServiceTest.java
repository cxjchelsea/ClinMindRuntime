package com.clinmind.runtime.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.boundary.DecisionBoundaryService;
import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.reasoning.DifferentialDiagnosisBoardService;
import com.clinmind.runtime.reasoning.EvidenceGraphService;
import com.clinmind.runtime.reasoning.QuestionTestPolicyService;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ClinicianReport;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClinicianReportServiceTest {

    @Autowired
    private ClinicianReportService clinicianReportService;

    @Autowired
    private DecisionBoundaryService decisionBoundaryService;

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Autowired
    private DifferentialDiagnosisBoardService differentialDiagnosisBoardService;

    @Autowired
    private EvidenceGraphService evidenceGraphService;

    @Autowired
    private QuestionTestPolicyService questionTestPolicyService;

    @Test
    void buildsFullReportInClinicianMode() {
        RuntimeState state = buildChestPainState(RuntimeMode.CLINICIAN_COPILOT);
        prepareClinicalState(state);

        ClinicianReport report = clinicianReportService.buildClinicianReport(state);

        assertThat(report.allowed()).isTrue();
        assertThat(report.caseSummary()).contains("主诉");
        assertThat(report.ddxSummary()).isNotEmpty();
        assertThat(report.evidenceSummary().items()).isNotEmpty();
        assertThat(report.recommendedQuestions()).isNotEmpty();
    }

    @Test
    void doesNotExposeReportInPatientMode() {
        RuntimeState state = buildChestPainState(RuntimeMode.PATIENT_FACING);
        prepareClinicalState(state);

        ClinicianReport report = clinicianReportService.buildClinicianReport(state);

        assertThat(report.allowed()).isFalse();
    }

    private void prepareClinicalState(RuntimeState state) {
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
        state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));
        state.setDecisionBoundary(decisionBoundaryService.decideOutputBoundary(state));
    }

    private RuntimeState buildChestPainState(RuntimeMode mode) {
        RuntimeState state = RuntimeState.createDefault("s_001");
        state.setMode(mode);
        String text = "胸口闷，活动后更明显";
        state.getInputHistory().add(new UserInput(text, java.util.List.of()));
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical", 0.8);
        state.setEntryAssessment(entry);
        state.setCaseFrame(new CaseFrame(text, null, null, null, null, null, null, null));
        state.setKnowledgeContext(knowledgeContextService.buildKnowledgeContext(state.getCaseFrame(), entry));
        return state;
    }
}
