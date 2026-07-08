package com.clinmind.runtime.console.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.console.access.ActorContextResolver;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "clinmind.debug-api.require-debug-token=true",
        "clinmind.debug-api.debug-token=test-secret"
})
class ConsoleCandidateInboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateStore candidateStore;

    @Test
    void observerCanReadSanitizedCandidateInbox() throws Exception {
        candidateStore.saveGenerationResult(new CandidateGenerationResult(
                "gen_phase10_inbox",
                "eval_phase10_inbox",
                Instant.now(),
                Instant.now(),
                List.of(new ExperienceCandidate(
                        "exp_phase10_inbox",
                        ExperienceCandidateType.SAFETY_LESSON,
                        "Inbox item",
                        "Only governance summary",
                        sourceRef(),
                        CandidateRiskLevel.HIGH,
                        null,
                        "review",
                        Map.of(),
                        List.of("phase10"),
                        Instant.now(),
                        "test",
                        Map.of("raw_external_response", "secret", "safe", "ok"))),
                List.of(),
                List.of(),
                List.of()));

        mockMvc.perform(get("/api/v1/console/candidates")
                        .param("candidate_type", "EXPERIENCE_CANDIDATE")
                        .param("limit", "1")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].candidate_id").value("exp_phase10_inbox"))
                .andExpect(jsonPath("$.data[0].metadata.raw_external_response").doesNotExist())
                .andExpect(jsonPath("$.data[0].metadata.safe").value("ok"));
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
