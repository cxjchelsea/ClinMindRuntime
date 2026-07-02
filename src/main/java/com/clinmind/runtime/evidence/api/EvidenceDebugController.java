package com.clinmind.runtime.evidence.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.evidence.api.dto.EvidenceCorpusResponse;
import com.clinmind.runtime.evidence.api.dto.EvidenceRetrieveRequest;
import com.clinmind.runtime.evidence.api.dto.EvidenceRetrieveResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/evidence")
public class EvidenceDebugController {

    private final EvidenceDebugService evidenceDebugService;
    private final EvidenceDebugAccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public EvidenceDebugController(
            EvidenceDebugService evidenceDebugService,
            EvidenceDebugAccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.evidenceDebugService = evidenceDebugService;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/retrieve")
    public ApiResponse<EvidenceRetrieveResponse> retrieve(
            @RequestBody EvidenceRetrieveRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(evidenceDebugService.retrieve(request), actor.requestId());
    }

    @GetMapping("/retrievals/{retrieval_id}")
    public ApiResponse<EvidenceRetrieveResponse> getRetrieval(
            @PathVariable("retrieval_id") String retrievalId, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(evidenceDebugService.getRetrieval(retrievalId), actor.requestId());
    }

    @GetMapping("/corpus")
    public ApiResponse<EvidenceCorpusResponse> getCorpus(HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(evidenceDebugService.getCorpus(), actor.requestId());
    }
}
