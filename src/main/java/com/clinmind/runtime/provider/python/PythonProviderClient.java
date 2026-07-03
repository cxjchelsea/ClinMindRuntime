package com.clinmind.runtime.provider.python;

import com.clinmind.runtime.provider.ProviderCapabilitiesResult;
import com.clinmind.runtime.provider.ProviderHealthResult;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;

public interface PythonProviderClient {

    boolean isEnabled();

    ProviderHealthResult health();

    ProviderCapabilitiesResult getCapabilities();

    ProviderInvocationResult<EmbeddingResult> embed(EmbeddingRequest request);

    ProviderInvocationResult<RerankResult> rerank(RerankRequest request);
}
