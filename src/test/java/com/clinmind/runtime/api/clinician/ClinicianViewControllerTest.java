package com.clinmind.runtime.api.clinician;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
class ClinicianViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clinicianCanReadCasesAndReportDraft() throws Exception {
        mockMvc.perform(get("/api/v1/clinician/cases")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CLINICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].case_id").exists())
                .andExpect(content().string(not(containsString("raw_prompt"))))
                .andExpect(content().string(not(containsString("raw_external_response"))));

        mockMvc.perform(get("/api/v1/clinician/cases/runtime-demo-001/report-draft")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CLINICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.submit_enabled").value(false))
                .andExpect(content().string(not(containsString("full_rationale"))));
    }

    @Test
    void patientCannotReadClinicianCaseView() throws Exception {
        mockMvc.perform(get("/api/v1/clinician/cases")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "PATIENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CLINICIAN_CASE_FORBIDDEN"));
    }
}
