package com.clinmind.runtime.modelgov;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.modelgov.policy.ModelEvaluationReportPolicy;
import com.clinmind.runtime.modelgov.policy.ModelExperimentPolicy;
import com.clinmind.runtime.modelgov.policy.ModelRegistryPolicy;
import com.clinmind.runtime.modelgov.policy.ModelReleasePolicy;
import com.clinmind.runtime.modelgov.policy.PromptRegistryPolicy;
import com.clinmind.runtime.modelgov.policy.TrainingDatasetVersionPolicy;
import com.clinmind.runtime.modelgov.store.ModelEvaluationReportStore;
import com.clinmind.runtime.modelgov.store.ModelExperimentStore;
import com.clinmind.runtime.modelgov.store.ModelRegistryStore;
import com.clinmind.runtime.modelgov.store.ModelReleaseCandidateStore;
import com.clinmind.runtime.modelgov.store.ModelRollbackPlanStore;
import com.clinmind.runtime.modelgov.store.PromptRegistryStore;
import com.clinmind.runtime.modelgov.store.TrainingDatasetVersionStore;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ModelGovernanceService {

    private final ModelRegistryStore modelRegistryStore;
    private final PromptRegistryStore promptRegistryStore;
    private final TrainingDatasetVersionStore datasetVersionStore;
    private final ModelExperimentStore experimentStore;
    private final ModelEvaluationReportStore reportStore;
    private final ModelRollbackPlanStore rollbackPlanStore;
    private final ModelReleaseCandidateStore releaseCandidateStore;
    private final ModelRegistryPolicy modelRegistryPolicy;
    private final PromptRegistryPolicy promptRegistryPolicy;
    private final TrainingDatasetVersionPolicy datasetVersionPolicy;
    private final ModelExperimentPolicy experimentPolicy;
    private final ModelEvaluationReportPolicy reportPolicy;
    private final ModelReleasePolicy releasePolicy;
    private final AuditLogService auditLogService;

    public ModelGovernanceService(
            ModelRegistryStore modelRegistryStore,
            PromptRegistryStore promptRegistryStore,
            TrainingDatasetVersionStore datasetVersionStore,
            ModelExperimentStore experimentStore,
            ModelEvaluationReportStore reportStore,
            ModelRollbackPlanStore rollbackPlanStore,
            ModelReleaseCandidateStore releaseCandidateStore,
            ModelRegistryPolicy modelRegistryPolicy,
            PromptRegistryPolicy promptRegistryPolicy,
            TrainingDatasetVersionPolicy datasetVersionPolicy,
            ModelExperimentPolicy experimentPolicy,
            ModelEvaluationReportPolicy reportPolicy,
            ModelReleasePolicy releasePolicy,
            AuditLogService auditLogService) {
        this.modelRegistryStore = modelRegistryStore;
        this.promptRegistryStore = promptRegistryStore;
        this.datasetVersionStore = datasetVersionStore;
        this.experimentStore = experimentStore;
        this.reportStore = reportStore;
        this.rollbackPlanStore = rollbackPlanStore;
        this.releaseCandidateStore = releaseCandidateStore;
        this.modelRegistryPolicy = modelRegistryPolicy;
        this.promptRegistryPolicy = promptRegistryPolicy;
        this.datasetVersionPolicy = datasetVersionPolicy;
        this.experimentPolicy = experimentPolicy;
        this.reportPolicy = reportPolicy;
        this.releasePolicy = releasePolicy;
        this.auditLogService = auditLogService;
    }

    public ModelRegistryEntry createModelRegistryEntry(ModelRegistryEntry request, String actor) {
        ModelRegistryEntry entry = new ModelRegistryEntry(
                IdGenerator.modelRegistryId(),
                request.modelId(),
                request.modelVersion(),
                request.providerId(),
                request.providerVersion(),
                request.capabilityTypes(),
                request.modelFamily(),
                request.modelSource(),
                request.modelRuntime(),
                request.status() == null ? ModelRegistryStatus.DRAFT : request.status(),
                request.riskLevel(),
                Instant.now(),
                actor,
                request.notes());
        enforce(modelRegistryPolicy.validateCreate(entry), AuditActionType.CREATE_MODEL_REGISTRY_ENTRY, entry.modelRegistryId(), actor);
        modelRegistryStore.save(entry.modelRegistryId(), entry);
        audit(AuditActionType.CREATE_MODEL_REGISTRY_ENTRY, entry.modelRegistryId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "model_id", entry.modelId(),
                "model_version", entry.modelVersion(),
                "status", entry.status().name()));
        return entry;
    }

    public PromptRegistryEntry createPromptRegistryEntry(PromptRegistryEntry request, String actor) {
        PromptRegistryEntry entry = new PromptRegistryEntry(
                IdGenerator.promptRegistryId(),
                request.promptId(),
                request.promptVersion(),
                request.useCase(),
                request.capabilityType(),
                request.promptTemplateHash(),
                request.promptSummary(),
                request.safetyTags(),
                request.forbiddenOutputTypes(),
                request.requiresDecisionBoundary(),
                request.status() == null ? PromptRegistryStatus.DRAFT : request.status(),
                Instant.now(),
                actor);
        enforce(promptRegistryPolicy.validateCreate(entry), AuditActionType.CREATE_PROMPT_REGISTRY_ENTRY, entry.promptRegistryId(), actor);
        promptRegistryStore.save(entry.promptRegistryId(), entry);
        audit(AuditActionType.CREATE_PROMPT_REGISTRY_ENTRY, entry.promptRegistryId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "prompt_id", entry.promptId(),
                "prompt_version", entry.promptVersion(),
                "requires_decision_boundary", entry.requiresDecisionBoundary()));
        return entry;
    }

    public TrainingDatasetVersion createTrainingDatasetVersion(TrainingDatasetVersion request, String actor) {
        TrainingDatasetVersion dataset = new TrainingDatasetVersion(
                IdGenerator.datasetVersionId(),
                request.datasetName(),
                request.datasetVersion(),
                request.sourceCandidateIds(),
                request.sourceMetricIds(),
                request.sourceCaseIds(),
                request.dataScope(),
                request.sampleCount(),
                request.safetyReviewStatus() == null ? DatasetReviewStatus.REVIEW_REQUIRED : request.safetyReviewStatus(),
                request.deidentificationStatus(),
                DatasetPublishStatus.DRAFT,
                request.rawPatientDialoguePresent(),
                false,
                Instant.now(),
                actor);
        enforce(datasetVersionPolicy.validateCreate(dataset), AuditActionType.CREATE_TRAINING_DATASET_VERSION, dataset.datasetVersionId(), actor);
        datasetVersionStore.save(dataset.datasetVersionId(), dataset);
        audit(AuditActionType.CREATE_TRAINING_DATASET_VERSION, dataset.datasetVersionId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "dataset_name", dataset.datasetName(),
                "dataset_version", dataset.datasetVersion(),
                "deidentification_status", dataset.deidentificationStatus().name()));
        return dataset;
    }

    public ModelExperimentRecord createModelExperimentRecord(ModelExperimentRecord request, String actor) {
        ModelExperimentRecord experiment = new ModelExperimentRecord(
                IdGenerator.modelExperimentId(),
                request.experimentName(),
                request.modelRegistryId(),
                request.promptRegistryId(),
                request.datasetVersionId(),
                request.capabilityType(),
                request.useCase(),
                request.evaluationCaseSetId(),
                request.baselineModelVersion(),
                request.candidateModelVersion(),
                request.status() == null ? ModelExperimentStatus.PLANNED : request.status(),
                Instant.now(),
                request.finishedAt(),
                actor);
        enforce(experimentPolicy.validateCreate(experiment), AuditActionType.CREATE_MODEL_EXPERIMENT_RECORD, experiment.experimentId(), actor);
        experimentStore.save(experiment.experimentId(), experiment);
        audit(AuditActionType.CREATE_MODEL_EXPERIMENT_RECORD, experiment.experimentId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "model_registry_id", experiment.modelRegistryId(),
                "prompt_registry_id", experiment.promptRegistryId(),
                "dataset_version_id", experiment.datasetVersionId()));
        return experiment;
    }

    public ModelEvaluationReport createModelEvaluationReport(ModelEvaluationReport request, String actor) {
        ModelEvaluationReport report = new ModelEvaluationReport(
                IdGenerator.modelEvaluationReportId(),
                request.experimentId(),
                request.modelRegistryId(),
                request.promptRegistryId(),
                request.datasetVersionId(),
                request.overallStatus(),
                request.metricResultIds(),
                request.safetyFindingIds(),
                request.regressionFindingIds(),
                request.recommendation(),
                Instant.now());
        enforce(reportPolicy.validateCreate(report), AuditActionType.CREATE_MODEL_EVALUATION_REPORT, report.reportId(), actor);
        reportStore.save(report.reportId(), report);
        audit(AuditActionType.CREATE_MODEL_EVALUATION_REPORT, report.reportId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "experiment_id", report.experimentId(),
                "recommendation", report.recommendation().name()));
        return report;
    }

    public ModelRollbackPlan createModelRollbackPlan(ModelRollbackPlan request, String actor) {
        ModelRollbackPlan plan = new ModelRollbackPlan(
                IdGenerator.modelRollbackPlanId(),
                request.releaseCandidateId(),
                request.previousModelRegistryId(),
                request.previousPromptRegistryId(),
                request.rollbackTriggerConditions(),
                request.rollbackSteps(),
                request.owner() == null || request.owner().isBlank() ? actor : request.owner(),
                request.status() == null ? ModelRollbackPlanStatus.REVIEW_REQUIRED : request.status(),
                Instant.now());
        rollbackPlanStore.save(plan.rollbackPlanId(), plan);
        audit(AuditActionType.CREATE_MODEL_ROLLBACK_PLAN, plan.rollbackPlanId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "previous_model_registry_id", value(plan.previousModelRegistryId()),
                "previous_prompt_registry_id", value(plan.previousPromptRegistryId())));
        return plan;
    }

    public ModelReleaseCandidate createModelReleaseCandidate(ModelReleaseCandidate request, String actor) {
        ModelEvaluationReport report = reportStore.findById(request.evaluationReportId()).orElse(null);
        ModelRollbackPlan rollbackPlan = rollbackPlanStore.findById(request.rollbackPlanId()).orElse(null);
        ModelReleaseCandidate candidate = new ModelReleaseCandidate(
                IdGenerator.modelReleaseCandidateId(),
                request.experimentId(),
                request.evaluationReportId(),
                request.modelRegistryId(),
                request.promptRegistryId(),
                request.datasetVersionId(),
                request.releaseScope(),
                request.recommendedAction(),
                request.riskLevel(),
                ModelReleaseReviewStatus.REVIEW_REQUIRED,
                request.rollbackPlanId(),
                false,
                Instant.now());
        enforce(releasePolicy.validateCreate(candidate, report, rollbackPlan),
                AuditActionType.CREATE_MODEL_RELEASE_CANDIDATE,
                candidate.releaseCandidateId(),
                actor);
        releaseCandidateStore.save(candidate.releaseCandidateId(), candidate);
        audit(AuditActionType.CREATE_MODEL_RELEASE_CANDIDATE, candidate.releaseCandidateId(), actor, AuditResultStatus.SUCCESS, Map.of(
                "experiment_id", candidate.experimentId(),
                "review_status", candidate.reviewStatus().name(),
                "auto_publish", candidate.autoPublish()));
        return candidate;
    }

    public List<ModelRegistryEntry> listModels() {
        return modelRegistryStore.findAll();
    }

    public ModelRegistryEntry getModel(String id) {
        return modelRegistryStore.findById(id).orElseThrow(() -> new IllegalArgumentException("model registry entry not found"));
    }

    public List<PromptRegistryEntry> listPrompts() {
        return promptRegistryStore.findAll();
    }

    public List<TrainingDatasetVersion> listDatasets() {
        return datasetVersionStore.findAll();
    }

    public ModelExperimentRecord getExperiment(String id) {
        return experimentStore.findById(id).orElseThrow(() -> new IllegalArgumentException("experiment not found"));
    }

    public ModelReleaseCandidate getReleaseCandidate(String id) {
        return releaseCandidateStore.findById(id).orElseThrow(() -> new IllegalArgumentException("release candidate not found"));
    }

    private void enforce(PolicyDecision decision, AuditActionType attemptedAction, String resourceId, String actor) {
        if (decision.allowed()) {
            return;
        }
        audit(AuditActionType.MODEL_GOVERNANCE_POLICY_REJECTED, resourceId, actor, AuditResultStatus.FAILURE, Map.of(
                "attempted_action", attemptedAction.name(),
                "reasons", decision.reasons()));
        throw new ModelGovernancePolicyException(decision.reasons());
    }

    private void audit(
            AuditActionType actionType,
            String resourceId,
            String actor,
            AuditResultStatus status,
            Map<String, Object> metadata) {
        auditLogService.record(actionType, AuditResourceType.MODEL_GOVERNANCE, resourceId, actor, status, metadata);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
