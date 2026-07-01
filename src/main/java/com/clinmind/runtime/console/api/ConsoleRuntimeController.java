package com.clinmind.runtime.console.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.AccessPolicy;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.console.access.ConsoleActionType;
import com.clinmind.runtime.console.access.ConsoleResourceType;
import com.clinmind.runtime.console.dto.RuntimeConsoleDetailDto;
import com.clinmind.runtime.console.dto.RuntimeConsoleSummaryDto;
import com.clinmind.runtime.console.query.ConsoleQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/console/runtime-sessions")
public class ConsoleRuntimeController {

    private final AccessPolicy accessPolicy;
    private final ConsoleQueryService consoleQueryService;

    public ConsoleRuntimeController(AccessPolicy accessPolicy, ConsoleQueryService consoleQueryService) {
        this.accessPolicy = accessPolicy;
        this.consoleQueryService = consoleQueryService;
    }

    @GetMapping
    public ApiResponse<List<RuntimeConsoleSummaryDto>> listRuntimeSessions(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "session_id", required = false) String sessionId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.LIST,
                ConsoleResourceType.CONSOLE_RUNTIME);
        return ApiResponse.ok(consoleQueryService.listRuntimeSessions(
                ActorContextHolder.getRequired(), status, sessionId, limit));
    }

    @GetMapping("/{runtime_id}")
    public ApiResponse<RuntimeConsoleDetailDto> getRuntimeSession(@PathVariable("runtime_id") String runtimeId) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_DETAIL,
                ConsoleResourceType.CONSOLE_RUNTIME);
        return ApiResponse.ok(consoleQueryService.getRuntimeSession(ActorContextHolder.getRequired(), runtimeId));
    }
}
