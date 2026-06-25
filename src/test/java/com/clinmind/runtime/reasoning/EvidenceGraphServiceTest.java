package com.clinmind.runtime.reasoning;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.EvidenceGraphItem;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.RuntimeTraceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EvidenceGraphServiceTest {

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
    void buildsMissingEvidenceAndNextQuestions() {
        RuntimeState state = buildChestPainState("胸口闷，活动后更明显");

        EvidenceGraph graph = evidenceGraphService.buildEvidenceGraph(state);

        assertThat(graph.items()).isNotEmpty();
        EvidenceGraphItem acs = graph.items().stream()
                .filter(item -> "acute_coronary_syndrome".equals(item.diagnosis()))
                .findFirst()
                .orElseThrow();
        assertThat(acs.missingEvidence()).isNotEmpty();
        assertThat(acs.nextQuestions()).anyMatch(question -> question.contains("出汗"));
        assertThat(collector.getSteps()).extracting("moduleName").contains("EvidenceGraph");
    }

    @Test
    void recommendsTestsForNeedToRuleOutCandidate() {
        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗");
        state.setSafetyGate(safetyGateService.evaluateSafety(state));
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));

        EvidenceGraph graph = evidenceGraphService.buildEvidenceGraph(state);

        EvidenceGraphItem acs = graph.items().stream()
                .filter(item -> "acute_coronary_syndrome".equals(item.diagnosis()))
                .findFirst()
                .orElseThrow();
        assertThat(acs.status()).isEqualTo(CandidateStatus.NEED_TO_RULE_OUT);
        assertThat(acs.recommendedTests()).contains("心电图", "心肌酶");
    }

    private RuntimeState buildChestPainState(String text) {
        RuntimeState state = RuntimeState.createDefault("s_001");
        state.getInputHistory().add(new UserInput(text, java.util.List.of()));
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical", 0.8);
        state.setEntryAssessment(entry);
        state.setCaseFrame(new CaseFrame(text, null, null, null, null, null, null, null));
        state.setKnowledgeContext(knowledgeContextService.buildKnowledgeContext(state.getCaseFrame(), entry));
        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        return state;
    }
}
