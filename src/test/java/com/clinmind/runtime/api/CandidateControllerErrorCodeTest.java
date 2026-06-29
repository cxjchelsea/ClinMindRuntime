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
class CandidateControllerErrorCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownGenerationUsesResourceTypeErrorCode() throws Exception {
        mockMvc.perform(get("/api/v1/debug/candidates/generations/cand_gen_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CANDIDATE_GENERATION_NOT_FOUND"));
    }

    @Test
    void unknownExperienceCandidateUsesResourceTypeErrorCode() throws Exception {
        mockMvc.perform(get("/api/v1/debug/candidates/experience-candidates/exp_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("EXPERIENCE_CANDIDATE_NOT_FOUND"));
    }

    @Test
    void unknownTrainingCandidateUsesResourceTypeErrorCode() throws Exception {
        mockMvc.perform(get("/api/v1/debug/candidates/training-example-candidates/train_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TRAINING_EXAMPLE_CANDIDATE_NOT_FOUND"));
    }
}
