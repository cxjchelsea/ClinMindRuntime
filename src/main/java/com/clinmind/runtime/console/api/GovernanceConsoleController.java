package com.clinmind.runtime.console.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.console.access.Phase10ConsoleAccessPolicy;
import com.clinmind.runtime.console.view.ConsoleOverviewService;
import com.clinmind.runtime.console.view.ConsoleReadService;
import com.clinmind.runtime.console.view.dto.AuditBrowserItemDto;
import com.clinmind.runtime.console.view.dto.CandidateInboxItemDto;
import com.clinmind.runtime.console.view.dto.ConsoleOverviewDto;
import com.clinmind.runtime.console.view.dto.GovernanceDomainCardDto;
import com.clinmind.runtime.console.view.dto.RuntimeListItemDto;
import com.clinmind.runtime.console.view.dto.RuntimeTimelineDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/console")
public class GovernanceConsoleController {

    private final Phase10ConsoleAccessPolicy accessPolicy;
    private final ConsoleOverviewService overviewService;
    private final ConsoleReadService readService;

    public GovernanceConsoleController(
            Phase10ConsoleAccessPolicy accessPolicy,
            ConsoleOverviewService overviewService,
            ConsoleReadService readService) {
        this.accessPolicy = accessPolicy;
        this.overviewService = overviewService;
        this.readService = readService;
    }

    @GetMapping("/overview")
    public ApiResponse<ConsoleOverviewDto> overview() {
        requireRead();
        return ApiResponse.ok(overviewService.overview());
    }

    @GetMapping("/runtimes")
    public ApiResponse<List<RuntimeListItemDto>> runtimes(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "session_id", required = false) String sessionId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        requireRead();
        return ApiResponse.ok(readService.listRuntimes(status, sessionId, limit));
    }

    @GetMapping("/runtimes/{runtime_id}/timeline")
    public ApiResponse<RuntimeTimelineDto> timeline(@PathVariable("runtime_id") String runtimeId) {
        requireRead();
        return ApiResponse.ok(readService.timeline(runtimeId));
    }

    @GetMapping("/governance/domains")
    public ApiResponse<List<GovernanceDomainCardDto>> domains() {
        requireRead();
        return ApiResponse.ok(readService.domains());
    }

    @GetMapping("/candidates")
    public ApiResponse<List<CandidateInboxItemDto>> candidates(
            @RequestParam(value = "review_status", required = false) String reviewStatus,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "candidate_type", required = false) String candidateType,
            @RequestParam(value = "limit", required = false) Integer limit) {
        requireRead();
        return ApiResponse.ok(readService.candidates(reviewStatus, riskLevel, candidateType, limit));
    }

    @GetMapping("/audits")
    public ApiResponse<List<AuditBrowserItemDto>> audits(
            @RequestParam(value = "action_type", required = false) String actionType,
            @RequestParam(value = "resource_type", required = false) String resourceType,
            @RequestParam(value = "actor_id", required = false) String actorId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", required = false) Integer limit) {
        requireRead();
        return ApiResponse.ok(readService.audits(actionType, resourceType, actorId, status, limit));
    }

    private void requireRead() {
        ActorContext context = ActorContextHolder.getRequired();
        accessPolicy.requireRead(context);
    }
}
