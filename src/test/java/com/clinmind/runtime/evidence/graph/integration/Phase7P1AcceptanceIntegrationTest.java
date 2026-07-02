package com.clinmind.runtime.evidence.graph.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.evidence.graph.GraphEvidenceStatus;
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
class Phase7P1AcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void highRiskChestPainGraphEvidenceAccepted() throws Exception {
        var start = runtimeService.startRuntime(new StartRuntimeRequest(
                "phase7p1-accept-chest",
                null,
                RuntimeMode.CLINICIAN_COPILOT,
                new UserInputRequest("胸口闷，活动后更明显，出汗，已经持续半小时", List.of()),
                null,
                null));
        assertThat(start.state().getGraphEvidence()).isNotNull();
        assertThat(start.state().getGraphEvidence().acceptedCandidates()).isNotEmpty();
        assertThat(start.state().getEvidenceGraph().items().get(0).graphPaths()).isNotEmpty();
        assertThat(start.state().getPatientOutput().content()).doesNotContain("path_score");

        mockMvc.perform(post("/api/v1/debug/graph-evidence/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_graph_chest",
                                  "symptom_group": "chest_pain",
                                  "case_frame_summary": {
                                    "known_facts": ["胸闷", "活动后加重", "出汗"]
                                  },
                                  "accepted_evidence_refs": [{
                                    "evidence_id": "ev_chunk_chest_pain_001",
                                    "source_id": "synthetic_safety_guide_chest_pain",
                                    "chunk_id": "chunk_chest_pain_001",
                                    "symptom_group": "chest_pain",
                                    "use_case": "safety_warning"
                                  }]
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.graph_candidates").isArray())
                .andExpect(jsonPath("$.data.graph_trace.recorded").value(true));
    }

    @Test
    void feverCaseGraphEvidenceOrSkipped() throws Exception {
        var start = runtimeService.startRuntime(new StartRuntimeRequest(
                "phase7p1-accept-fever",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("发热两天，体温最高 38.5 度，伴轻微头痛", List.of()),
                null,
                null));
        assertThat(start.state().getRuntimeStatus()).isNotEqualTo(RuntimeStatus.ERROR_SAFE_HALTED);
        GraphEvidenceStatus status = start.state().getGraphEvidence().status();
        assertThat(status).isIn(GraphEvidenceStatus.SUCCESS, GraphEvidenceStatus.SKIPPED, GraphEvidenceStatus.NO_GRAPH_PATH_FOUND);
    }

    @Test
    void noAcceptedEvidenceRefsPolicyRejected() throws Exception {
        mockMvc.perform(post("/api/v1/debug/graph-evidence/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_graph_empty",
                                  "symptom_group": "chest_pain",
                                  "case_frame_summary": {
                                    "known_facts": ["胸闷"]
                                  },
                                  "accepted_evidence_refs": []
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isBadRequest());
    }
}
