package com.clinmind.runtime.console.access;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret"
        })
class ConsoleAccessDeniedTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsCandidateReviewerOnReviewProbe() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/access-probe/review")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "reviewer-a")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("allowed"));
    }

    @Test
    void deniesReadOnlyObserverOnReviewProbe() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/access-probe/review")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void rejectsInvalidDebugRoleHeader() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/access-probe/review")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "NOT_A_ROLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_DEBUG_ROLE"));
    }

    @Test
    void allowsAuditReviewerOnAuditProbe() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/access-probe/audit")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("allowed"));
    }
}
