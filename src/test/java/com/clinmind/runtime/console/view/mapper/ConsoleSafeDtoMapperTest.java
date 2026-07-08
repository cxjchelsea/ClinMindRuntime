package com.clinmind.runtime.console.view.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.console.view.dto.CandidateInboxItemDto;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConsoleSafeDtoMapperTest {

    private final ConsoleSafeDtoMapper mapper = new ConsoleSafeDtoMapper();

    @Test
    void candidateInboxItemStripsSensitiveMetadata() {
        ExperienceCandidate candidate = new ExperienceCandidate(
                "exp_phase10_safe",
                ExperienceCandidateType.SAFETY_LESSON,
                "Safe lesson",
                "Governance summary",
                sourceRef(),
                CandidateRiskLevel.HIGH,
                null,
                "review",
                Map.of(),
                List.of("phase10"),
                Instant.parse("2026-07-07T00:00:00Z"),
                "test",
                Map.of("api_key", "secret", "safe_signal", "ok"));

        CandidateInboxItemDto dto = mapper.toCandidateInboxItem(candidate);

        assertThat(dto.metadata()).containsEntry("safe_signal", "ok");
        assertThat(dto.metadata()).doesNotContainKeys("api_key");
    }

    @Test
    void timelineContainsSafetyGateWithoutPatientInput() {
        RuntimeState state = RuntimeState.createDefault("phase10_safe_session");
        state.setSafetyGate(new SafetyGateResult());

        var timeline = mapper.toTimeline(state, List.of());

        assertThat(timeline.nodes()).extracting("type").contains("SAFETY_GATE");
        assertThat(timeline.nodes().toString()).doesNotContain("patient_input");
    }

    private CandidateSourceRef sourceRef() {
        return new CandidateSourceRef(
                CandidateSourceType.EVALUATION_RUN,
                "rt_phase10",
                "eval_phase10",
                "case_phase10",
                null,
                null,
                null,
                null,
                "metric",
                "phase10",
                "0.10.0",
                "test");
    }
}
