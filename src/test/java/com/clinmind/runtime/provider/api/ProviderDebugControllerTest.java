package com.clinmind.runtime.provider.api;

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
            "clinmind.debug-api.debug-token=test-secret",
            "clinmind.python-provider.enabled=false"
        })
class ProviderDebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReadableByObserver() throws Exception {
        mockMvc.perform(get("/api/v1/debug/providers/health")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MODEL_UNAVAILABLE"));
    }

    @Test
    void rerankRunRequiresAuthorizedRole() throws Exception {
        mockMvc.perform(post("/api/v1/debug/providers/rerank/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_provider",
                                  "query_text": "胸口闷",
                                  "items": [{
                                    "item_id": "chunk_chest_pain_001",
                                    "text": "胸痛风险"
                                  }]
                                }
                                """)
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rerankRunFallbackWithReviewerRole() throws Exception {
        mockMvc.perform(post("/api/v1/debug/providers/rerank/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_provider_ok",
                                  "query_text": "胸口闷，活动后更明显",
                                  "items": [{
                                    "item_id": "chunk_chest_pain_001",
                                    "text": "胸痛风险"
                                  }]
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fallback_used").value(true))
                .andExpect(jsonPath("$.data.provider_call_id").exists());
    }
}
