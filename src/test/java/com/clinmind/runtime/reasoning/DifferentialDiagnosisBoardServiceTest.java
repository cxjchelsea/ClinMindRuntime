package com.clinmind.runtime.reasoning;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.DifferentialDiagnosisBoard;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.RuntimeTraceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DifferentialDiagnosisBoardServiceTest {

    @Autowired
    private DifferentialDiagnosisBoardService differentialDiagnosisBoardService;

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Autowired
    private SafetyGateService safetyGateService;

    @Autowired
    private RuntimeTraceCollector collector;

    @BeforeEach
    void setUp() {
        collector.clear();
    }

    @Test
    void buildsBoardWithMustNotMissWhenSafetyGateInactive() {
        RuntimeState state = buildChestPainState("胸口闷");

        DifferentialDiagnosisBoard board = differentialDiagnosisBoardService.buildDifferentialBoard(state);

        assertThat(board.candidates()).extracting(DDxCandidate::name)
                .contains("acute_coronary_syndrome", "musculoskeletal_chest_pain");
        DDxCandidate acs = board.candidates().stream()
                .filter(item -> "acute_coronary_syndrome".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(acs.status()).isEqualTo(CandidateStatus.MUST_NOT_MISS);
        assertThat(collector.getSteps()).extracting("moduleName").contains("DifferentialDiagnosisBoard");
    }

    @Test
    void retainsHighRiskAsNeedToRuleOutWhenSafetyGateActive() {
        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗");
        state.setSafetyGate(safetyGateService.evaluateSafety(state));

        DifferentialDiagnosisBoard board = differentialDiagnosisBoardService.buildDifferentialBoard(state);

        DDxCandidate acs = board.candidates().stream()
                .filter(item -> "acute_coronary_syndrome".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(acs.status()).isEqualTo(CandidateStatus.NEED_TO_RULE_OUT);

        DDxCandidate common = board.candidates().stream()
                .filter(item -> "musculoskeletal_chest_pain".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(common.status()).isEqualTo(CandidateStatus.POSSIBLE_AFTER_EXCLUSION);
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
