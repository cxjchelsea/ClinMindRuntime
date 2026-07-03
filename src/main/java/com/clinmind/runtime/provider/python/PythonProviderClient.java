package com.clinmind.runtime.provider.python;

import com.clinmind.runtime.provider.ProviderCapabilitiesResult;
import com.clinmind.runtime.provider.ProviderHealthResult;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.capability.ProviderCapabilityProfile;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.judge.JudgeRequest;
import com.clinmind.runtime.provider.judge.JudgeScoreResult;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.provider.risk.RiskSignalClassificationRequest;
import com.clinmind.runtime.provider.risk.RiskSignalDraft;
import java.util.List;

public interface PythonProviderClient {

    boolean isEnabled();

    ProviderHealthResult health();

    ProviderCapabilitiesResult getCapabilities();

    ProviderInvocationResult<EmbeddingResult> embed(EmbeddingRequest request);

    ProviderInvocationResult<RerankResult> rerank(RerankRequest request);

    ProviderInvocationResult<List<ProviderCapabilityProfile>> getCapabilityProfiles(String runtimeId);

    ProviderInvocationResult<JudgeScoreResult> judge(JudgeRequest request);

    ProviderInvocationResult<RiskSignalDraft> classifyRisk(RiskSignalClassificationRequest request);
}
