package com.clinmind.runtime.evidence.graph.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
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
class GraphEvidenceDebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void runRequiresAuthorizedRole() throws Exception {
        mockMvc.perform(post("/api/v1/debug/graph-evidence/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_graph",
                                  "symptom_group": "chest_pain",
                                  "accepted_evidence_refs": [{
                                    "evidence_id": "ev_chunk_chest_pain_001",
                                    "source_id": "synthetic_safety_guide_chest_pain",
                                    "chunk_id": "chunk_chest_pain_001",
                                    "symptom_group": "chest_pain",
                                    "use_case": "safety_warning"
                                  }]
                                }
                                """)
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isForbidden());
    }

    @Test
    void runSuccessWithReviewerRole() throws Exception {
        mockMvc.perform(post("/api/v1/debug/graph-evidence/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_graph_ok",
                                  "symptom_group": "chest_pain",
                                  "case_frame_summary": {
                                    "known_facts": ["胸闷", "出汗", "活动后加重"]
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
                .andExpect(jsonPath("$.data.provider_id").value("kg_lite_graph_evidence_provider"));
    }

    @Test
    void graphMetadataReadableByObserver() throws Exception {
        mockMvc.perform(get("/api/v1/debug/graph-evidence/graph")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.node_count").isNumber());
    }
}
