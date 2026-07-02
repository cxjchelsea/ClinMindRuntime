package com.clinmind.runtime.agent.api;

import com.clinmind.runtime.agent.api.dto.AgentExecutionSafeDto;
import com.clinmind.runtime.agent.api.dto.AgentRegistryResponse;
import com.clinmind.runtime.agent.api.dto.InquiryPlanningRunRequest;
import com.clinmind.runtime.agent.api.dto.InquiryPlanningRunResponse;
import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/agents")
public class AgentDebugController {

    private final AgentDebugService agentDebugService;
    private final AgentDebugAccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public AgentDebugController(
            AgentDebugService agentDebugService,
            AgentDebugAccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.agentDebugService = agentDebugService;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/inquiry-planning/run")
    public ApiResponse<InquiryPlanningRunResponse> runInquiryPlanning(
            @RequestBody InquiryPlanningRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(agentDebugService.runInquiryPlanning(request), actor.requestId());
    }

    @GetMapping("/executions/{execution_id}")
    public ApiResponse<AgentExecutionSafeDto> getExecution(
            @PathVariable("execution_id") String executionId, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(agentDebugService.getExecution(executionId), actor.requestId());
    }

    @GetMapping("/registry")
    public ApiResponse<AgentRegistryResponse> listRegistry(HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(agentDebugService.listRegistry(), actor.requestId());
    }
}
