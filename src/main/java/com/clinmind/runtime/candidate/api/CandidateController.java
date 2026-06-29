package com.clinmind.runtime.candidate.api;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.candidate.generation.CandidateGenerationService;
import com.clinmind.runtime.candidate.store.CandidateStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/candidates")
public class CandidateController {

    private final CandidateGenerationService generationService;
    private final CandidateStore candidateStore;

    public CandidateController(CandidateGenerationService generationService, CandidateStore candidateStore) {
        this.generationService = generationService;
        this.candidateStore = candidateStore;
    }

    @PostMapping("/generations/from-evaluation/{run_id}")
    public ApiResponse<?> generateFromEvaluation(
            @PathVariable("run_id") String runId,
            @RequestBody(required = false) CandidateGenerationRequest request) {
        CandidateGenerationPolicy policy = resolvePolicy(request);
        CandidateGenerationResult result = generationService.generateFromEvaluationRun(runId, policy);
        return ApiResponse.ok(toGenerationSummary(result));
    }

    @GetMapping("/generations/{generation_id}")
    public ApiResponse<?> getGeneration(@PathVariable("generation_id") String generationId) {
        return ApiResponse.ok(candidateStore.getGenerationResult(generationId));
    }

    @GetMapping("/generations/{generation_id}/experience-candidates")
    public ApiResponse<?> listExperienceCandidates(@PathVariable("generation_id") String generationId) {
        List<ExperienceCandidate> candidates = candidateStore.listExperienceCandidates(generationId);
        return ApiResponse.ok(candidates);
    }

    @GetMapping("/generations/{generation_id}/training-example-candidates")
    public ApiResponse<?> listTrainingExampleCandidates(@PathVariable("generation_id") String generationId) {
        List<TrainingExampleCandidate> candidates = candidateStore.listTrainingExampleCandidates(generationId);
        return ApiResponse.ok(candidates);
    }

    @GetMapping("/experience-candidates/{candidate_id}")
    public ApiResponse<?> getExperienceCandidate(@PathVariable("candidate_id") String candidateId) {
        return ApiResponse.ok(candidateStore.getExperienceCandidate(candidateId));
    }

    @GetMapping("/training-example-candidates/{candidate_id}")
    public ApiResponse<?> getTrainingExampleCandidate(@PathVariable("candidate_id") String candidateId) {
        return ApiResponse.ok(candidateStore.getTrainingExampleCandidate(candidateId));
    }

    private static CandidateGenerationPolicy resolvePolicy(CandidateGenerationRequest request) {
        if (request == null) {
            return CandidateGenerationPolicy.defaults();
        }
        try {
            return request.toPolicy();
        } catch (IllegalArgumentException exception) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CANDIDATE_GENERATION_REQUEST",
                    exception.getMessage());
        }
    }

    private static Map<String, Object> toGenerationSummary(CandidateGenerationResult result) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("generation_id", result.generationId());
        summary.put("source_evaluation_run_id", result.sourceEvaluationRunId());
        summary.put("experience_candidate_count", result.experienceCandidates().size());
        summary.put("training_candidate_count", result.trainingExampleCandidates().size());
        summary.put("skipped_item_count", result.skippedItems().size());
        summary.put("warnings", result.warnings());
        return summary;
    }
}
