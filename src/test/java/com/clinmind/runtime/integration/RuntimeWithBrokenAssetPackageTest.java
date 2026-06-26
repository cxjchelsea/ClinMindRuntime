package com.clinmind.runtime.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeWithBrokenAssetPackageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void brokenPackageTriggersFailSafeHalt() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_broken_pkg",
                                  "mode": "patient_facing",
                                  "asset_context": {"package_id": "broken-package"},
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_status").value("error_safe_halted"))
                .andExpect(jsonPath("$.data.patient_output.allowed").value(false))
                .andExpect(jsonPath("$.data.patient_output.constraints_applied").value(
                        org.hamcrest.Matchers.hasItem("fail_safe")));
    }
}
