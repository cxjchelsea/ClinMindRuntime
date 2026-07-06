package com.clinmind.runtime.modelgov.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret",
            "clinmind.python-provider.enabled=false"
        })
class ModelGovernanceDebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createModelRequiresReviewerOrAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/debug/model-governance/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modelRequest())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void modelRegistryCanBeCreatedAndListed() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/debug/model-governance/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modelRequest())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelId").value("mock_judge_model"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        String modelRegistryId = dataText(created, "modelRegistryId");

        mockMvc.perform(get("/api/v1/debug/model-governance/models")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].modelRegistryId").exists());

        mockMvc.perform(get("/api/v1/debug/model-governance/models/{id}", modelRegistryId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelRegistryId").value(modelRegistryId));
    }

    @Test
    void unsafePatientFacingPromptIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/debug/model-governance/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prompt_id": "patient_boundary_prompt",
                                  "prompt_version": "0.1.0",
                                  "use_case": "patient_facing_output_boundary",
                                  "capability_type": "JUDGE",
                                  "prompt_template_hash": "sha256:test",
                                  "prompt_summary": "summary only",
                                  "safety_tags": ["patient_boundary"],
                                  "forbidden_output_types": ["Final Diagnosis"],
                                  "requires_decision_boundary": false
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MODEL_GOVERNANCE_POLICY_REJECTED"));
    }

    @Test
    void releaseCandidateIsReviewRequiredAndNeverAutoPublishes() throws Exception {
        String modelId = createModel();
        String promptId = createPrompt();
        String datasetId = createDataset();
        String experimentId = createExperiment(modelId, promptId, datasetId);
        String reportId = createReport(experimentId, modelId, promptId, datasetId);
        String rollbackId = createRollbackPlan();

        mockMvc.perform(post("/api/v1/debug/model-governance/release-candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "experiment_id": "%s",
                                  "evaluation_report_id": "%s",
                                  "model_registry_id": "%s",
                                  "prompt_registry_id": "%s",
                                  "dataset_version_id": "%s",
                                  "release_scope": "shadow_test",
                                  "recommended_action": "APPROVE_FOR_SHADOW_TEST",
                                  "risk_level": "MEDIUM",
                                  "rollback_plan_id": "%s",
                                  "auto_publish": true
                                }
                                """.formatted(experimentId, reportId, modelId, promptId, datasetId, rollbackId))
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.data.autoPublish").value(false));
    }

    private String createModel() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/model-governance/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modelRequest())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        return dataText(result, "modelRegistryId");
    }

    private String createPrompt() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/model-governance/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prompt_id": "patient_boundary_prompt_safe",
                                  "prompt_version": "0.1.0",
                                  "use_case": "patient_facing_output_boundary",
                                  "capability_type": "JUDGE",
                                  "prompt_template_hash": "sha256:test",
                                  "prompt_summary": "summary only",
                                  "safety_tags": ["patient_boundary"],
                                  "forbidden_output_types": ["Final Diagnosis"],
                                  "requires_decision_boundary": true
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        return dataText(result, "promptRegistryId");
    }

    private String createDataset() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/model-governance/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dataset_name": "boundary_seed",
                                  "dataset_version": "0.1.0",
                                  "source_candidate_ids": ["candidate_001"],
                                  "source_metric_ids": ["prompt_registry_safety"],
                                  "source_case_ids": ["case_001"],
                                  "data_scope": "evaluation_seed",
                                  "sample_count": 12,
                                  "deidentification_status": "PASSED"
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        return dataText(result, "datasetVersionId");
    }

    private String createExperiment(String modelId, String promptId, String datasetId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/model-governance/experiments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "experiment_name": "judge_model_boundary_eval_001",
                                  "model_registry_id": "%s",
                                  "prompt_registry_id": "%s",
                                  "dataset_version_id": "%s",
                                  "capability_type": "JUDGE",
                                  "use_case": "output_boundary_check",
                                  "evaluation_case_set_id": "case_set_001",
                                  "baseline_model_version": "0.0.1",
                                  "candidate_model_version": "0.1.0"
                                }
                                """.formatted(modelId, promptId, datasetId))
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        return dataText(result, "experimentId");
    }

    private String createReport(String experimentId, String modelId, String promptId, String datasetId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/model-governance/evaluation-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "experiment_id": "%s",
                                  "model_registry_id": "%s",
                                  "prompt_registry_id": "%s",
                                  "dataset_version_id": "%s",
                                  "overall_status": "REVIEW_REQUIRED",
                                  "metric_result_ids": ["metric_001"],
                                  "safety_finding_ids": [],
                                  "regression_finding_ids": [],
                                  "recommendation": "REVIEW_REQUIRED"
                                }
                                """.formatted(experimentId, modelId, promptId, datasetId))
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        return dataText(result, "reportId");
    }

    private String createRollbackPlan() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/model-governance/rollback-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "previous_model_registry_id": "model_reg_previous",
                                  "previous_prompt_registry_id": "prompt_reg_previous",
                                  "rollback_trigger_conditions": ["critical_boundary_violation"],
                                  "rollback_steps": ["disable candidate model"],
                                  "owner": "system_admin"
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        return dataText(result, "rollbackPlanId");
    }

    private String modelRequest() {
        return """
                {
                  "model_id": "mock_judge_model",
                  "model_version": "0.1.0",
                  "provider_id": "python_ai_provider",
                  "provider_version": "0.8.1-p1",
                  "capability_types": ["JUDGE"],
                  "model_family": "mock-rule-based",
                  "model_source": "MOCK_RULE_BASED",
                  "model_runtime": "python-provider",
                  "risk_level": "LOW",
                  "notes": "Phase 8-P1 deterministic judge model"
                }
                """;
    }

    private String dataText(MvcResult result, String fieldName) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path(fieldName).asText();
    }
}
