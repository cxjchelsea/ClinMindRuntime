package com.clinmind.runtime.state;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeStateTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createDefaultObject() {
        RuntimeState state = RuntimeState.createDefault("s_001");

        assertThat(state.getRuntimeId()).startsWith("rt_");
        assertThat(state.getSessionId()).isEqualTo("s_001");
        assertThat(state.getRuntimeStatus()).isEqualTo(RuntimeStatus.CREATED);
        assertThat(state.getMode()).isEqualTo(RuntimeMode.PATIENT_FACING);
        assertThat(state.getInputHistory()).isEmpty();
        assertThat(state.getCaseFrame()).isNotNull();
        assertThat(state.getExperienceContext().implementationMode()).isEqualTo("empty");
    }

    @Test
    void jsonRoundTrip() throws Exception {
        RuntimeState state = RuntimeState.createDefault("s_001");
        state.setUserId("u_001");
        state.setRuntimeStatus(RuntimeStatus.CLINICAL_MODE);
        state.setWorkMode(WorkMode.CLINICAL_MODE);
        state.setEntryAssessment(new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical symptoms detected", 0.8));
        state.getInputHistory().add(new UserInput("胸口闷"));
        state.setSafetyGate(new SafetyGateResult(
                true, RiskLevel.HIGH, List.of("rf_001"), "high risk", null, null, false));
        state.setDifferentialBoard(new DifferentialDiagnosisBoard(
                List.of(new DDxCandidate("high_risk_a", CandidateStatus.MUST_NOT_MISS, RiskLevel.HIGH, null, false)),
                null));
        state.setDecisionBoundary(new DecisionBoundaryResult(
                OutputLevel.O1_CONTINUE_QUESTIONING, false, true, "high risk not ruled out", List.of()));

        String json = objectMapper.writeValueAsString(state);
        RuntimeState restored = objectMapper.readValue(json, RuntimeState.class);

        assertThat(restored.getRuntimeId()).isEqualTo(state.getRuntimeId());
        assertThat(restored.getEntryAssessment().symptomGroup()).isEqualTo("chest_pain");
        assertThat(restored.getSafetyGate().triggered()).isTrue();
        assertThat(restored.getDifferentialBoard().candidates()).hasSize(1);
    }

    @Test
    void bumpVersionUpdatesTimestamp() {
        RuntimeState state = RuntimeState.createDefault("s_001");
        int version = state.getVersion();
        var updatedAt = state.getUpdatedAt();

        state.bumpVersion();

        assertThat(state.getVersion()).isEqualTo(version + 1);
        assertThat(state.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
    }
}
