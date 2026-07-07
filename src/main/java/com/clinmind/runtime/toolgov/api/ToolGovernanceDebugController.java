package com.clinmind.runtime.toolgov.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.provider.api.ProviderDebugAccessPolicy;
import com.clinmind.runtime.toolgov.McpServerRegistryEntry;
import com.clinmind.runtime.toolgov.SkillRegistryEntry;
import com.clinmind.runtime.toolgov.ToolGovernanceService;
import com.clinmind.runtime.toolgov.ToolInvocationResult;
import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import com.clinmind.runtime.toolgov.api.dto.McpServerRegistryCreateRequest;
import com.clinmind.runtime.toolgov.api.dto.SkillRegistryCreateRequest;
import com.clinmind.runtime.toolgov.api.dto.ToolInvocationRunRequest;
import com.clinmind.runtime.toolgov.api.dto.ToolRegistryCreateRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/tool-governance")
public class ToolGovernanceDebugController {

    private final ToolGovernanceService service;
    private final ProviderDebugAccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public ToolGovernanceDebugController(
            ToolGovernanceService service,
            ProviderDebugAccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.service = service;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/tools")
    public ApiResponse<ToolRegistryEntry> createTool(@RequestBody ToolRegistryCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createTool(request.toEntry(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/tools")
    public ApiResponse<List<ToolRegistryEntry>> listTools(HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.listTools(), actor.requestId());
    }

    @GetMapping("/tools/{tool_registry_id}")
    public ApiResponse<ToolRegistryEntry> getTool(@PathVariable("tool_registry_id") String id, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.getTool(id), actor.requestId());
    }

    @PostMapping("/mcp-servers")
    public ApiResponse<McpServerRegistryEntry> createMcpServer(
            @RequestBody McpServerRegistryCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createMcpServer(request.toEntry(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/mcp-servers")
    public ApiResponse<List<McpServerRegistryEntry>> listMcpServers(HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.listMcpServers(), actor.requestId());
    }

    @PostMapping("/skills")
    public ApiResponse<SkillRegistryEntry> createSkill(@RequestBody SkillRegistryCreateRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.createSkill(request.toEntry(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/skills")
    public ApiResponse<List<SkillRegistryEntry>> listSkills(HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.listSkills(), actor.requestId());
    }

    @PostMapping("/invocations/run")
    public ApiResponse<ToolInvocationResult> runInvocation(
            @RequestBody ToolInvocationRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(service.runInvocation(request.toRequest(), actor.actorId()), actor.requestId());
    }

    @GetMapping("/invocations/{invocation_id}")
    public ApiResponse<ToolInvocationResult> getInvocation(@PathVariable("invocation_id") String id, HttpServletRequest httpRequest) {
        ActorContext actor = actor(httpRequest);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(service.getInvocation(id), actor.requestId());
    }

    private ActorContext actor(HttpServletRequest request) {
        ActorContext actor = actorContextResolver.resolve(request);
        actorContextResolver.bindToLegacyAuditContext(actor);
        return actor;
    }
}
