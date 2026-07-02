package com.clinmind.runtime.agent.api;

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
class AgentDebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsWithoutDebugToken() throws Exception {
        mockMvc.perform(get("/api/v1/debug/agents/registry"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsRegistryForAuthorizedActor() throws Exception {
        mockMvc.perform(get("/api/v1/debug/agents/registry")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.agents[0].agent_id").value("inquiry_planning_agent"));
    }

    @Test
    void runsInquiryPlanningForDeveloperRole() throws Exception {
        String body =
                """
                {
                  "runtime_id": "runtime_demo_001",
                  "symptom_group": "chest_pain",
                  "case_frame_summary": {
                    "known_facts": ["胸闷"],
                    "missing_facts": ["持续时间", "是否放射痛"]
                  },
                  "red_flag_candidates": ["活动后胸闷"],
                  "max_question_count": 2
                }
                """;

        mockMvc.perform(post("/api/v1/debug/agents/inquiry-planning/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.proposal.proposed_questions").isArray());
    }

    @Test
    void rejectsRunForReadOnlyObserver() throws Exception {
        String body =
                """
                {
                  "runtime_id": "runtime_demo_001",
                  "symptom_group": "chest_pain",
                  "case_frame_summary": {
                    "missing_facts": ["持续时间"]
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/debug/agents/inquiry-planning/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }
}
