package com.clinmind.runtime.evidence.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.evidence.EvidenceRetrievalStatus;
import com.clinmind.runtime.service.RuntimeService;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret"
        })
class Phase7P0AcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void highRiskChestPainEvidenceRetrievalAccepted() throws Exception {
        var start = runtimeService.startRuntime(new StartRuntimeRequest(
                "phase7-accept-chest",
                null,
                RuntimeMode.CLINICIAN_COPILOT,
                new UserInputRequest("胸口闷，活动后更明显，出汗，已经持续半小时", List.of()),
                null,
                null));
        assertThat(start.state().getEvidenceRetrieval()).isNotNull();
        assertThat(start.state().getEvidenceRetrieval().acceptedCandidates()).isNotEmpty();
        assertThat(start.state().getEvidenceGraph().items()).isNotEmpty();
        assertThat(start.state().getEvidenceGraph().items().get(0).evidenceRefs()).isNotEmpty();
        assertThat(start.state().getPatientOutput().content()).doesNotContain("retrieval_score");

        mockMvc.perform(post("/api/v1/debug/evidence/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_evidence_chest",
                                  "symptom_group": "chest_pain",
                                  "case_frame_summary": {
                                    "chief_complaint": "胸口闷，活动后更明显，出汗",
                                    "known_facts": ["胸闷", "活动后加重", "出汗"],
                                    "missing_facts": ["持续时间", "是否放射痛"]
                                  },
                                  "red_flag_summary": ["活动后胸闷", "出汗"],
                                  "retrieval_limit": 5
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.evidence_candidates").isArray())
                .andExpect(jsonPath("$.data.query_trace.recorded").value(true));
    }

    @Test
    void feverCaseEvidenceRetrievalAccepted() throws Exception {
        var start = runtimeService.startRuntime(new StartRuntimeRequest(
                "phase7-accept-fever",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("发热两天，体温最高 38.5 度，伴轻微头痛", List.of()),
                null,
                null));
        assertThat(start.state().getRuntimeStatus()).isNotEqualTo(RuntimeStatus.ERROR_SAFE_HALTED);
        assertThat(start.state().getEvidenceRetrieval().status()).isIn(
                EvidenceRetrievalStatus.SUCCESS, EvidenceRetrievalStatus.NO_EVIDENCE_FOUND);

        mockMvc.perform(get("/api/v1/debug/evidence/corpus")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chunk_count").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void unknownSymptomGroupPolicyRejected() throws Exception {
        mockMvc.perform(post("/api/v1/debug/evidence/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_evidence_unknown",
                                  "symptom_group": "unknown_group",
                                  "case_frame_summary": {
                                    "known_facts": ["test"],
                                    "missing_facts": ["duration"]
                                  }
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("POLICY_REJECTED"))
                .andExpect(jsonPath("$.data.evidence_candidates").isEmpty());
    }
}
