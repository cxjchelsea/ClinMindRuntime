package com.clinmind.runtime.api.patient;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
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
class PatientViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patientCanReadSessionsAndSafeSummary() throws Exception {
        mockMvc.perform(get("/api/v1/patient/sessions")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].runtime_id").exists())
                .andExpect(content().string(not(containsString("ddx_candidates"))))
                .andExpect(content().string(not(containsString("raw_evidence"))));

        mockMvc.perform(get("/api/v1/patient/sessions/runtime-demo-001/summary")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.safe_summary").exists())
                .andExpect(content().string(not(containsString("trace_nodes"))))
                .andExpect(content().string(not(containsString("candidate_governance"))));
    }

    @Test
    void observerCannotReadPatientView() throws Exception {
        mockMvc.perform(get("/api/v1/patient/sessions")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("PATIENT_VIEW_FORBIDDEN"));
    }
}
