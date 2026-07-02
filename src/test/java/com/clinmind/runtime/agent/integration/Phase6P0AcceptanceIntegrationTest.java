package com.clinmind.runtime.agent.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.console.access.ActorContextResolver;
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
class Phase6P0AcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void highRiskChestPainAgentPlanningAccepted() throws Exception {
        var start = runtimeService.startRuntime(new StartRuntimeRequest(
                "phase6-accept-chest",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("胸口闷，活动后更明显，出汗，已经持续半小时", List.of()),
                null,
                null));
        assertThat(start.state().getAgentOrchestration()).isNotNull();
        assertThat(start.state().getAgentOrchestration().acceptedQuestions()).isNotEmpty();
        assertThat(start.state().getQuestionTestPolicy().nextAction().content())
                .doesNotContain("心梗");

        mockMvc.perform(post("/api/v1/debug/agents/inquiry-planning/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_accept_chest",
                                  "symptom_group": "chest_pain",
                                  "case_frame_summary": {
                                    "known_facts": ["胸闷", "活动后加重", "出汗"],
                                    "missing_facts": ["持续时间", "是否放射痛", "是否呼吸困难"]
                                  },
                                  "red_flag_candidates": ["活动后胸闷", "出汗"],
                                  "max_question_count": 3
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.proposal.proposed_questions").isArray())
                .andExpect(jsonPath("$.data.trace_summary.recorded").value(true));
    }

    @Test
    void feverCaseAgentPlanningAccepted() throws Exception {
        var start = runtimeService.startRuntime(new StartRuntimeRequest(
                "phase6-accept-fever",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("发热两天，体温最高 38.5 度，伴轻微头痛", List.of()),
                null,
                null));
        assertThat(start.state().getRuntimeStatus()).isNotEqualTo(RuntimeStatus.ERROR_SAFE_HALTED);

        mockMvc.perform(post("/api/v1/debug/agents/inquiry-planning/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_accept_fever",
                                  "symptom_group": "fever",
                                  "case_frame_summary": {
                                    "known_facts": ["发热", "头痛"],
                                    "missing_facts": ["持续时间", "severity"]
                                  },
                                  "max_question_count": 2
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void emptyMissingFactsRejectedByPolicy() throws Exception {
        mockMvc.perform(post("/api/v1/debug/agents/inquiry-planning/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "runtime_accept_empty",
                                  "symptom_group": "chest_pain",
                                  "case_frame_summary": {
                                    "known_facts": ["胸闷"],
                                    "missing_facts": []
                                  }
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void registryListsInquiryPlanningAgent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/api/v1/debug/agents/registry")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agents[0].agent_id").value("inquiry_planning_agent"));
    }
}
