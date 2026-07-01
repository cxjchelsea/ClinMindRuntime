package com.clinmind.runtime.console.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.AccessPolicy;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.console.access.ConsoleActionType;
import com.clinmind.runtime.console.access.ConsoleResourceType;
import com.clinmind.runtime.console.dto.CandidateConsoleDetailDto;
import com.clinmind.runtime.console.dto.CandidateConsoleSummaryDto;
import com.clinmind.runtime.console.dto.CandidateGenerationConsoleSummaryDto;
import com.clinmind.runtime.console.query.ConsoleQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/console")
public class ConsoleCandidateController {

    private final AccessPolicy accessPolicy;
    private final ConsoleQueryService consoleQueryService;

    public ConsoleCandidateController(AccessPolicy accessPolicy, ConsoleQueryService consoleQueryService) {
        this.accessPolicy = accessPolicy;
        this.consoleQueryService = consoleQueryService;
    }

    @GetMapping("/candidate-generations")
    public ApiResponse<List<CandidateGenerationConsoleSummaryDto>> listCandidateGenerations(
            @RequestParam(value = "source_evaluation_run_id", required = false) String sourceEvaluationRunId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.LIST,
                ConsoleResourceType.CONSOLE_CANDIDATE);
        return ApiResponse.ok(consoleQueryService.listCandidateGenerations(
                ActorContextHolder.getRequired(), sourceEvaluationRunId, limit));
    }

    @GetMapping("/candidates")
    public ApiResponse<List<CandidateConsoleSummaryDto>> listCandidates(
            @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "review_status", required = false) String reviewStatus,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "limit", required = false) Integer limit) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.LIST,
                ConsoleResourceType.CONSOLE_CANDIDATE);
        return ApiResponse.ok(consoleQueryService.listCandidates(
                ActorContextHolder.getRequired(), kind, reviewStatus, riskLevel, limit));
    }

    @GetMapping("/candidates/{candidate_id}")
    public ApiResponse<CandidateConsoleDetailDto> getCandidate(@PathVariable("candidate_id") String candidateId) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_DETAIL,
                ConsoleResourceType.CONSOLE_CANDIDATE);
        return ApiResponse.ok(consoleQueryService.getCandidate(ActorContextHolder.getRequired(), candidateId));
    }

    @GetMapping("/review-queue")
    public ApiResponse<List<CandidateConsoleSummaryDto>> listReviewQueue(
            @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "task_type", required = false) String taskType,
            @RequestParam(value = "limit", required = false) Integer limit) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.LIST,
                ConsoleResourceType.CONSOLE_REVIEW);
        return ApiResponse.ok(consoleQueryService.listReviewQueue(
                ActorContextHolder.getRequired(), kind, riskLevel, taskType, limit));
    }
}
