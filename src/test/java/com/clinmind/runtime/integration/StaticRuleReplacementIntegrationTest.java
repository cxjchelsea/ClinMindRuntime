package com.clinmind.runtime.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clinmind.runtime.knowledge.StaticRuleProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class StaticRuleReplacementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaticRuleProvider staticRuleProvider;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void alternateRuleProviderLoadsDifferentAssets() {
        assertThat(staticRuleProvider.loadSymptomGroupRules("chest_pain").mustNotMiss()).hasSize(1);
        assertThat(staticRuleProvider.loadRedFlagRules("chest_pain")).hasSize(1);
    }

    @Test
    void runtimeFlowWorksWithAlternateRules() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_alt",
                                  "mode": "clinician_copilot",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clinician_report.ddx_summary.length()").value(2));
    }

    @TestConfiguration
    static class AlternateRuleConfig {
        @Bean
        @Primary
        StaticRuleProvider alternateStaticRuleProvider() {
            return new StaticRuleProvider("assets-alt/");
        }
    }
}
