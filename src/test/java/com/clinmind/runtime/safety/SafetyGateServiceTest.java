package com.clinmind.runtime.safety;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.provider.CapabilityProfileProvider;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.RuntimeTraceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SafetyGateServiceTest {

    @Autowired
    private SafetyGateService safetyGateService;

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Autowired
    private RuntimeTraceCollector collector;

    @BeforeEach
    void setUp() {
        collector.clear();
    }

    @Test
    void triggersWhenActivityRelatedAndSweatingPresent() {
        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗");

        SafetyGateResult result = safetyGateService.evaluateSafety(state);

        assertThat(result.triggered()).isTrue();
        assertThat(result.matchedRules()).contains("rf_001");
        assertThat(result.riskLevel().name()).isEqualTo("HIGH");
        assertThat(result.patientOutputConstraint()).isEqualTo("no_low_risk_reassurance");
        assertThat(collector.getSteps()).extracting("moduleName").contains("SafetyGate");
    }

    @Test
    void doesNotTriggerWhenOnlyOneFeaturePresent() {
        RuntimeState state = buildChestPainState("胸口闷，活动后更明显");

        SafetyGateResult result = safetyGateService.evaluateSafety(state);

        assertThat(result.triggered()).isFalse();
    }

    @Test
    void failSafeOnException() {
        CapabilityProfileProvider brokenProvider = mock(CapabilityProfileProvider.class);
        when(brokenProvider.loadCapabilityProfile(anyString(), any()))
                .thenThrow(new RuntimeException("broken"));
        SafetyGateService service = new SafetyGateService(brokenProvider);

        RuntimeState state = buildChestPainState("胸口闷，活动后加重，出汗");
        state.setKnowledgeContext(knowledgeContextService.buildKnowledgeContext(
                state.getCaseFrame(), state.getEntryAssessment()));

        SafetyGateResult result = service.evaluateSafety(state);

        assertThat(result.failSafeRequired()).isTrue();
        assertThat(result.triggered()).isTrue();
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
