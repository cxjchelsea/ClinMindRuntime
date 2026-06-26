package com.clinmind.runtime.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsAssetPackages() throws Exception {
        mockMvc.perform(get("/api/v1/assets/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.packages[?(@.package_id=='phase2-default')]").exists())
                .andExpect(jsonPath("$.data.packages[?(@.default_package==true)]").exists());
    }

    @Test
    void returnsPackageDetail() throws Exception {
        mockMvc.perform(get("/api/v1/assets/packages/phase2-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.package_id").value("phase2-default"))
                .andExpect(jsonPath("$.data.version").isNotEmpty())
                .andExpect(jsonPath("$.data.supported_symptom_groups").isArray());
    }

    @Test
    void returnsSymptomGroupAssetSummary() throws Exception {
        mockMvc.perform(get("/api/v1/assets/packages/phase2-default/symptom-groups/chest_pain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.symptom_group").value("chest_pain"))
                .andExpect(jsonPath("$.data.must_not_miss_count").value(2))
                .andExpect(jsonPath("$.data.asset_ref").value(org.hamcrest.Matchers.containsString("@")));
    }

    @Test
    void returnsNotFoundForUnknownPackage() throws Exception {
        mockMvc.perform(get("/api/v1/assets/packages/unknown-package"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
