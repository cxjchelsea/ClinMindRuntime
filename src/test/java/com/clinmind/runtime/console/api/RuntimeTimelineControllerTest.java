package com.clinmind.runtime.console.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.storage.RuntimeStore;
import java.util.List;
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
class RuntimeTimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuntimeStore runtimeStore;

    @Test
    void evaluationReviewerCanReadTimelineWithGovernanceNodes() throws Exception {
        RuntimeState state = RuntimeState.createDefault("phase10_timeline_session");
        state.setSafetyGate(new SafetyGateResult());
        state.setDecisionBoundary(new DecisionBoundaryResult(OutputLevel.O2_RISK_HINT, false, true, "safe", List.of()));
        String runtimeId = runtimeStore.create(state).getRuntimeId();

        mockMvc.perform(get("/api/v1/console/runtimes/{runtime_id}/timeline", runtimeId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_id").value(runtimeId))
                .andExpect(jsonPath("$.data.nodes[*].type", hasItem("SAFETY_GATE")))
                .andExpect(jsonPath("$.data.nodes[*].type", hasItem("DECISION_BOUNDARY")))
                .andExpect(jsonPath("$.data.patient_output").doesNotExist());
    }
}
