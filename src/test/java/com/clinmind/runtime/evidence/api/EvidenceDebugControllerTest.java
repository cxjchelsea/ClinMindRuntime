package com.clinmind.runtime.evidence.api;

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
class EvidenceDebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void retrieveRequiresAuthorizedRole() throws Exception {
        mockMvc.perform(post("/api/v1/debug/evidence/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_debug",
                                  "symptom_group": "fever",
                                  "case_frame_summary": {
                                    "known_facts": ["发热"],
                                    "missing_facts": ["duration"]
                                  }
                                }
                                """)
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isForbidden());
    }

    @Test
    void retrieveSuccessWithReviewerRole() throws Exception {
        mockMvc.perform(post("/api/v1/debug/evidence/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_debug_ok",
                                  "symptom_group": "fever",
                                  "case_frame_summary": {
                                    "known_facts": ["发热", "头痛"],
                                    "missing_facts": ["duration"]
                                  }
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider_id").value("rag_evidence_provider"));
    }
}
