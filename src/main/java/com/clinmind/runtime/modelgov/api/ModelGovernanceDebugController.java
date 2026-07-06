package com.clinmind.runtime.modelgov.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.modelgov.ModelEvaluationReport;
import com.clinmind.runtime.modelgov.ModelExperimentRecord;
import com.clinmind.runtime.modelgov.ModelGovernanceService;
import com.clinmind.runtime.modelgov.ModelRegistryEntry;
import com.clinmind.runtime.modelgov.ModelReleaseCandidate;
import com.clinmind.runtime.modelgov.ModelRollbackPlan;
import com.clinmind.runtime.modelgov.PromptRegistryEntry;
import com.clinmind.runtime.modelgov.TrainingDatasetVersion;
import com.clinmind.runtime.modelgov.api.dto.ModelEvaluationReportCreateRequest;
import com.clinmind.runtime.modelgov.api.dto.ModelExperimentCreateRequest;
import com.clinmind.runtime.modelgov.api.dto.ModelRegistryCreateRequest;
import com.clinmind.runtime.modelgov.api.dto.ModelReleaseCandidateCreateRequest;
import com.clinmind.runtime.modelgov.api.dto.ModelRollbackPlanCreateRequest;
import com.clinmind.runtime.modelgov.api.dto.PromptRegistryCreateRequest;
import com.clinmind.runtime.modelgov.api.dto.TrainingDatasetVersionCreateRequest;
import com.clinmind.runtime.provider.api.ProviderDebugAccessPolicy;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/model-governance")
public class ModelGovernanceDebugController {

    private final ModelGovernanceService service;
    private final ProviderDebugAccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public ModelGovernanceDebugController(
            ModelGovernanceService service,
            ProviderDebugAccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.service = service;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/models")
    public ApiResponse<ModelRegistryEntry> createModel(
            @RequestBody ModelRegistryCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createModelRegistryEntry(request.toEntry(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/models")
    public ApiResponse<List<ModelRegistryEntry>> listModels(HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.listModels(), actor.requestId());
    }

    @GetMapping("/models/{model_registry_id}")
    public ApiResponse<ModelRegistryEntry> getModel(
            @PathVariable("model_registry_id") String modelRegistryId, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.getModel(modelRegistryId), actor.requestId());
    }

    @PostMapping("/prompts")
    public ApiResponse<PromptRegistryEntry> createPrompt(
            @RequestBody PromptRegistryCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createPromptRegistryEntry(request.toEntry(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/prompts")
    public ApiResponse<List<PromptRegistryEntry>> listPrompts(HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.listPrompts(), actor.requestId());
    }

    @PostMapping("/datasets")
    public ApiResponse<TrainingDatasetVersion> createDataset(
            @RequestBody TrainingDatasetVersionCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createTrainingDatasetVersion(request.toDataset(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/datasets")
    public ApiResponse<List<TrainingDatasetVersion>> listDatasets(HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.listDatasets(), actor.requestId());
    }

    @PostMapping("/experiments")
    public ApiResponse<ModelExperimentRecord> createExperiment(
            @RequestBody ModelExperimentCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createModelExperimentRecord(request.toExperiment(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/experiments/{experiment_id}")
    public ApiResponse<ModelExperimentRecord> getExperiment(
            @PathVariable("experiment_id") String experimentId, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.getExperiment(experimentId), actor.requestId());
    }

    @PostMapping("/evaluation-reports")
    public ApiResponse<ModelEvaluationReport> createReport(
            @RequestBody ModelEvaluationReportCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createModelEvaluationReport(request.toReport(), actor.actorId()), actor.requestId());
    }

    @PostMapping("/rollback-plans")
    public ApiResponse<ModelRollbackPlan> createRollbackPlan(
            @RequestBody ModelRollbackPlanCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createModelRollbackPlan(request.toPlan(), actor.actorId()), actor.requestId());
    }

    @PostMapping("/release-candidates")
    public ApiResponse<ModelReleaseCandidate> createReleaseCandidate(
            @RequestBody ModelReleaseCandidateCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createModelReleaseCandidate(request.toCandidate(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/release-candidates/{release_candidate_id}")
    public ApiResponse<ModelReleaseCandidate> getReleaseCandidate(
            @PathVariable("release_candidate_id") String releaseCandidateId, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.getReleaseCandidate(releaseCandidateId), actor.requestId());
    }

    private ActorContext actor(HttpServletRequest request) {
        ActorContext actor = actorContextResolver.resolve(request);
        actorContextResolver.bindToLegacyAuditContext(actor);
        return actor;
    }
}
