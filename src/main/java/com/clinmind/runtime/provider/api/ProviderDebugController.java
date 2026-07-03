package com.clinmind.runtime.provider.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.provider.api.dto.EmbeddingRunRequest;
import com.clinmind.runtime.provider.api.dto.EmbeddingRunResponse;
import com.clinmind.runtime.provider.api.dto.JudgeRunRequest;
import com.clinmind.runtime.provider.api.dto.JudgeRunResponse;
import com.clinmind.runtime.provider.api.dto.ProviderCallSafeDto;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilitiesResponse;
import com.clinmind.runtime.provider.api.dto.ProviderCapabilityProfilesDebugResponse;
import com.clinmind.runtime.provider.api.dto.ProviderHealthDto;
import com.clinmind.runtime.provider.api.dto.RiskClassifierRunRequest;
import com.clinmind.runtime.provider.api.dto.RiskClassifierRunResponse;
import com.clinmind.runtime.provider.api.dto.RerankRunRequest;
import com.clinmind.runtime.provider.api.dto.RerankRunResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/providers")
public class ProviderDebugController {

    private final ProviderDebugService providerDebugService;
    private final ProviderDebugAccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public ProviderDebugController(
            ProviderDebugService providerDebugService,
            ProviderDebugAccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.providerDebugService = providerDebugService;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @GetMapping("/health")
    public ApiResponse<ProviderHealthDto> health(HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(providerDebugService.health(), actor.requestId());
    }

    @GetMapping("/capabilities")
    public ApiResponse<ProviderCapabilitiesResponse> capabilities(HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(providerDebugService.capabilities(), actor.requestId());
    }

    @GetMapping("/capability-profiles")
    public ApiResponse<ProviderCapabilityProfilesDebugResponse> capabilityProfiles(HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(providerDebugService.capabilityProfiles(), actor.requestId());
    }

    @PostMapping("/embeddings/run")
    public ApiResponse<EmbeddingRunResponse> runEmbeddings(
            @RequestBody EmbeddingRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(providerDebugService.runEmbeddings(request), actor.requestId());
    }

    @PostMapping("/rerank/run")
    public ApiResponse<RerankRunResponse> runRerank(
            @RequestBody RerankRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(providerDebugService.runRerank(request), actor.requestId());
    }

    @PostMapping("/judge/run")
    public ApiResponse<JudgeRunResponse> runJudge(
            @RequestBody JudgeRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(providerDebugService.runJudge(request), actor.requestId());
    }

    @PostMapping("/risk-classifier/run")
    public ApiResponse<RiskClassifierRunResponse> runRiskClassifier(
            @RequestBody RiskClassifierRunRequest request, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireRunAccess(actor);
        return ApiResponse.ok(providerDebugService.runRiskClassifier(request), actor.requestId());
    }

    @GetMapping("/calls/{provider_call_id}")
    public ApiResponse<ProviderCallSafeDto> getCall(
            @PathVariable("provider_call_id") String providerCallId, HttpServletRequest httpRequest) {
        ActorContext actor = actorContextResolver.resolve(httpRequest);
        actorContextResolver.bindToLegacyAuditContext(actor);
        accessPolicy.requireReadAccess(actor);
        return ApiResponse.ok(providerDebugService.getCall(providerCallId), actor.requestId());
    }
}
