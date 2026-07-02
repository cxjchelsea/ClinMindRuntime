package com.clinmind.runtime.evidence.graph.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceRunRequest;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceRunResponse;
import com.clinmind.runtime.evidence.graph.api.dto.KgLiteGraphResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/graph-evidence")
public class GraphEvidenceDebugController {

    private final GraphEvidenceDebugService graphEvidenceDebugService;
    private final GraphEvidenceDebugAccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public GraphEvidenceDebugController(
            GraphEvidenceDebugService graphEvidenceDebugService,
            GraphEvidenceDebugAccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.graphEvidenceDebugService = graphEvidenceDebugService;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/run")
    public ApiResponse<GraphEvidenceRunResponse> run(
            @RequestBody GraphEvidenceRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(graphEvidenceDebugService.run(request), actor.requestId());
    }

    @GetMapping("/runs/{graph_retrieval_id}")
    public ApiResponse<GraphEvidenceRunResponse> getRun(
            @PathVariable("graph_retrieval_id") String graphRetrievalId, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(graphEvidenceDebugService.getRun(graphRetrievalId), actor.requestId());
    }

    @GetMapping("/graph")
    public ApiResponse<KgLiteGraphResponse> getGraph(HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(graphEvidenceDebugService.getGraph(), actor.requestId());
    }
}
