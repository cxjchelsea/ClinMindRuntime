package com.clinmind.runtime.api;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunConfig;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.evaluation.EvaluationRunner;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.evaluation.capability.CapabilityProfileProposalService;
import com.clinmind.runtime.evaluation.capability.CapabilityProfileUpdateProposal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/evaluations")
public class EvaluationController {

    private final EvaluationRunner evaluationRunner;
    private final EvaluationRunStore runStore;
    private final CapabilityProfileProposalService proposalService;

    public EvaluationController(
            EvaluationRunner evaluationRunner,
            EvaluationRunStore runStore,
            CapabilityProfileProposalService proposalService) {
        this.evaluationRunner = evaluationRunner;
        this.runStore = runStore;
        this.proposalService = proposalService;
    }

    @PostMapping("/runs")
    public ApiResponse<?> createRun(@RequestBody EvaluationRunConfig config) {
        EvaluationRun run = evaluationRunner.run(config);
        return ApiResponse.ok(toRunSummary(run));
    }

    @GetMapping("/runs/{run_id}")
    public ApiResponse<?> getRun(@PathVariable("run_id") String runId) {
        EvaluationRun run = runStore.get(runId);
        Map<String, Object> response = new LinkedHashMap<>(toRunSummary(run));
        response.put("config", run.config());
        response.put("started_at", run.startedAt());
        response.put("completed_at", run.completedAt());
        return ApiResponse.ok(response);
    }

    @GetMapping("/runs/{run_id}/result")
    public ApiResponse<?> getRunResult(@PathVariable("run_id") String runId) {
        EvaluationRun run = runStore.get(runId);
        if (run.result() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "EVALUATION_RUN_NOT_FOUND", "Evaluation result not ready");
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", run.result());
        response.put("item_summaries", run.itemResults().stream().map(this::toItemSummary).toList());
        return ApiResponse.ok(response);
    }

    @GetMapping("/runs/{run_id}/items/{case_id}")
    public ApiResponse<?> getItemResult(
            @PathVariable("run_id") String runId,
            @PathVariable("case_id") String caseId) {
        EvaluationRun run = runStore.get(runId);
        EvaluationItemResult item = run.itemResults().stream()
                .filter(result -> caseId.equals(result.caseId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "EVALUATION_RUN_NOT_FOUND",
                        "Evaluation item not found: " + caseId));
        RuntimeCaseExecution execution = runStore.getExecution(runId, caseId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("item", item);
        response.put("execution", execution);
        return ApiResponse.ok(response);
    }

    @PostMapping("/runs/{run_id}/capability-profile-proposal")
    public ApiResponse<?> createCapabilityProfileProposal(
            @PathVariable("run_id") String runId,
            @RequestParam("symptom_group") String symptomGroup) {
        if (!StringUtils.hasText(symptomGroup)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "symptom_group 不能为空");
        }
        EvaluationRun run = runStore.get(runId);
        CapabilityProfileUpdateProposal proposal = proposalService.generateProposal(run, symptomGroup);
        return ApiResponse.ok(proposal);
    }

    private Map<String, Object> toRunSummary(EvaluationRun run) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("run_id", run.runId());
        summary.put("status", run.status().getValue());
        EvaluationResult result = run.result();
        if (result != null) {
            summary.put("total_cases", result.totalCases());
            summary.put("passed_cases", result.passedCases());
            summary.put("failed_cases", result.failedCases());
            summary.put("pass_rate", result.passRate());
        } else {
            summary.put("total_cases", run.itemResults().size());
            summary.put("passed_cases", countPassed(run.itemResults()));
            summary.put("failed_cases", run.itemResults().size() - countPassed(run.itemResults()));
            summary.put("pass_rate", run.itemResults().isEmpty()
                    ? 0.0
                    : (double) countPassed(run.itemResults()) / run.itemResults().size());
        }
        return summary;
    }

    private Map<String, Object> toItemSummary(EvaluationItemResult item) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("case_id", item.caseId());
        summary.put("passed", item.passed());
        summary.put("score", item.score());
        summary.put("runtime_id", item.runtimeId());
        summary.put("safety_violation_count", item.safetyViolations().size());
        return summary;
    }

    private static int countPassed(List<EvaluationItemResult> items) {
        return (int) items.stream().filter(EvaluationItemResult::passed).count();
    }
}
