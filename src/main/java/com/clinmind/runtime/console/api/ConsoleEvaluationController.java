package com.clinmind.runtime.console.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.AccessPolicy;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.console.access.ConsoleActionType;
import com.clinmind.runtime.console.access.ConsoleResourceType;
import com.clinmind.runtime.console.dto.EvaluationConsoleDetailDto;
import com.clinmind.runtime.console.dto.EvaluationConsoleSummaryDto;
import com.clinmind.runtime.console.query.ConsoleQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/console/evaluation-runs")
public class ConsoleEvaluationController {

    private final AccessPolicy accessPolicy;
    private final ConsoleQueryService consoleQueryService;

    public ConsoleEvaluationController(AccessPolicy accessPolicy, ConsoleQueryService consoleQueryService) {
        this.accessPolicy = accessPolicy;
        this.consoleQueryService = consoleQueryService;
    }

    @GetMapping
    public ApiResponse<List<EvaluationConsoleSummaryDto>> listEvaluationRuns(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "case_set_id", required = false) String caseSetId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.LIST,
                ConsoleResourceType.CONSOLE_EVALUATION);
        return ApiResponse.ok(consoleQueryService.listEvaluationRuns(
                ActorContextHolder.getRequired(), status, caseSetId, limit));
    }

    @GetMapping("/{run_id}")
    public ApiResponse<EvaluationConsoleDetailDto> getEvaluationRun(@PathVariable("run_id") String runId) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_DETAIL,
                ConsoleResourceType.CONSOLE_EVALUATION);
        return ApiResponse.ok(consoleQueryService.getEvaluationRun(ActorContextHolder.getRequired(), runId));
    }
}
