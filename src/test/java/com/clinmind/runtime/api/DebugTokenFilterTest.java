package com.clinmind.runtime.api;

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
class DebugTokenFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsDebugApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/debug/audit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DEBUG_TOKEN_REQUIRED"));
    }

    @Test
    void rejectsDebugApiWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/debug/audit-logs").header(DebugTokenFilter.DEBUG_TOKEN_HEADER, "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_DEBUG_TOKEN"));
    }

    @Test
    void allowsDebugApiWithValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/debug/audit-logs").header(DebugTokenFilter.DEBUG_TOKEN_HEADER, "test-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void doesNotRequireTokenForRuntimeApi() throws Exception {
        mockMvc.perform(get("/api/v1/runtime/rt_missing/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RUNTIME_NOT_FOUND"));
    }
}
