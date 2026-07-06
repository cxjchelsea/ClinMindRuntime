package com.clinmind.runtime.modelgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.modelgov.policy.ModelEvaluationReportPolicy;
import com.clinmind.runtime.modelgov.policy.ModelRegistryPolicy;
import com.clinmind.runtime.modelgov.policy.ModelReleasePolicy;
import com.clinmind.runtime.modelgov.policy.PromptRegistryPolicy;
import com.clinmind.runtime.modelgov.policy.TrainingDatasetVersionPolicy;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelGovernancePolicyTest {

    @Test
    void modelRegistryRejectsMissingVersion() {
        var entry = new ModelRegistryEntry(
                null,
                "mock_judge_model",
                "",
                "python_ai_provider",
                "0.8.1-p1",
                List.of(ProviderCapabilityType.JUDGE),
                "mock",
                ModelSource.MOCK_RULE_BASED,
                "python-provider",
                ModelRegistryStatus.DRAFT,
                "LOW",
                null,
                "tester",
                null);

        var decision = new ModelRegistryPolicy().validateCreate(entry);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("model_version missing");
    }

    @Test
    void promptRegistryRejectsPatientFacingPromptWithoutDecisionBoundary() {
        var prompt = new PromptRegistryEntry(
                null,
                "patient_boundary_prompt",
                "0.1.0",
                "patient_facing_output_boundary",
                ProviderCapabilityType.JUDGE,
                "sha256:test",
                "summary only",
                List.of("patient_boundary"),
                List.of("Final Diagnosis"),
                false,
                PromptRegistryStatus.DRAFT,
                null,
                "tester");

        var decision = new PromptRegistryPolicy().validateCreate(prompt);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("patient-facing prompt requires decision boundary");
    }

    @Test
    void datasetPolicyRejectsRawPatientDialogueAndAutoPublish() {
        var dataset = new TrainingDatasetVersion(
                null,
                "boundary_seed",
                "0.1.0",
                List.of("candidate_001"),
                List.of(),
                List.of(),
                "evaluation_seed",
                12,
                DatasetReviewStatus.REVIEW_REQUIRED,
                DeidentificationStatus.PASSED,
                DatasetPublishStatus.DRAFT,
                true,
                true,
                null,
                "tester");

        var decision = new TrainingDatasetVersionPolicy().validateCreate(dataset);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("raw patient dialogue is not allowed");
        assertThat(decision.reasons()).contains("training dataset version cannot auto publish");
    }

    @Test
    void releasePolicyRejectsMissingRollbackPlan() {
        var report = new ModelEvaluationReport(
                "report_001",
                "exp_001",
                "model_reg_001",
                "prompt_reg_001",
                "dataset_ver_001",
                ModelReportStatus.REVIEW_REQUIRED,
                List.of("metric_001"),
                List.of(),
                List.of(),
                ModelReportRecommendation.APPROVE_FOR_SHADOW_TEST,
                null);
        var candidate = new ModelReleaseCandidate(
                null,
                "exp_001",
                "report_001",
                "model_reg_001",
                "prompt_reg_001",
                "dataset_ver_001",
                "shadow_test",
                ModelReportRecommendation.APPROVE_FOR_SHADOW_TEST,
                "MEDIUM",
                ModelReleaseReviewStatus.REVIEW_REQUIRED,
                "",
                false,
                null);

        var decision = new ModelReleasePolicy().validateCreate(candidate, report, null);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("rollback_plan_id missing");
        assertThat(decision.reasons()).contains("rollback plan required");
    }

    @Test
    void reportPolicyRejectsCriticalFindingApproval() {
        var report = new ModelEvaluationReport(
                null,
                "exp_001",
                "model_reg_001",
                "prompt_reg_001",
                "dataset_ver_001",
                ModelReportStatus.REVIEW_REQUIRED,
                List.of("metric_001"),
                List.of("critical_boundary_violation"),
                List.of(),
                ModelReportRecommendation.APPROVE_FOR_LIMITED_USE,
                null);

        var decision = new ModelEvaluationReportPolicy().validateCreate(report);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("critical safety finding cannot be approved");
    }
}
