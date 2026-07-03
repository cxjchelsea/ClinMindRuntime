package com.clinmind.runtime.provider.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.embedding.EmbeddingItem;
import com.clinmind.runtime.provider.embedding.EmbeddingItemResult;
import com.clinmind.runtime.provider.embedding.EmbeddingRequest;
import com.clinmind.runtime.provider.embedding.EmbeddingResult;
import com.clinmind.runtime.provider.rerank.RankedItem;
import com.clinmind.runtime.provider.rerank.RerankItem;
import com.clinmind.runtime.provider.rerank.RerankQuery;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProviderValidationServiceTest {

    private ProviderValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ProviderValidationService();
    }

    @Test
    void acceptsValidEmbeddingResult() {
        EmbeddingRequest request = new EmbeddingRequest(
                "req_1",
                "rt_1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "test",
                List.of(new EmbeddingItem("chunk_1", "text")),
                ProviderConstants.SCHEMA_VERSION);
        EmbeddingResult result = new EmbeddingResult(
                "req_1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.EMBEDDING_MODEL_ID,
                ProviderConstants.EMBEDDING_MODEL_VERSION,
                ProviderConstants.SCHEMA_VERSION,
                List.of(new EmbeddingItemResult(
                        "chunk_1", List.of(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7),
                        ProviderConstants.EMBEDDING_DIMENSION,
                        "sha256:abc",
                        true)));
        var validation = validationService.validateEmbedding(request, result);
        assertThat(validation.status()).isEqualTo(ProviderValidationStatus.ACCEPTED);
    }

    @Test
    void rejectsMissingModelVersion() {
        EmbeddingRequest request = new EmbeddingRequest(
                "req_1",
                "rt_1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "test",
                List.of(new EmbeddingItem("chunk_1", "text")),
                ProviderConstants.SCHEMA_VERSION);
        EmbeddingResult result = new EmbeddingResult(
                "req_1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.EMBEDDING_MODEL_ID,
                "",
                ProviderConstants.SCHEMA_VERSION,
                List.of(new EmbeddingItemResult(
                        "chunk_1", List.of(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7),
                        ProviderConstants.EMBEDDING_DIMENSION,
                        "sha256:abc",
                        true)));
        var validation = validationService.validateEmbedding(request, result);
        assertThat(validation.status()).isEqualTo(ProviderValidationStatus.REJECTED);
        assertThat(validation.reasons()).anyMatch(reason -> reason.contains("model_version"));
    }

    @Test
    void rejectsRerankScoreOutOfRange() {
        RerankRequest request = new RerankRequest(
                "req_2",
                "rt_1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "test",
                new RerankQuery("q1", "query"),
                List.of(new RerankItem("chunk_1", "text")),
                ProviderConstants.SCHEMA_VERSION);
        RerankResult result = new RerankResult(
                "req_2",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RERANK_MODEL_ID,
                ProviderConstants.RERANK_MODEL_VERSION,
                ProviderConstants.SCHEMA_VERSION,
                "q1",
                List.of(new RankedItem("chunk_1", 1, 1.5, "low_match")));
        var validation = validationService.validateRerank(request, result);
        assertThat(validation.status()).isEqualTo(ProviderValidationStatus.REJECTED);
        assertThat(validation.reasons()).anyMatch(reason -> reason.contains("score"));
    }

    @Test
    void rejectsUnexpectedRerankItemId() {
        RerankRequest request = new RerankRequest(
                "req_2",
                "rt_1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "test",
                new RerankQuery("q1", "query"),
                List.of(new RerankItem("chunk_1", "text")),
                ProviderConstants.SCHEMA_VERSION);
        RerankResult result = new RerankResult(
                "req_2",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RERANK_MODEL_ID,
                ProviderConstants.RERANK_MODEL_VERSION,
                ProviderConstants.SCHEMA_VERSION,
                "q1",
                List.of(new RankedItem("chunk_unknown", 1, 0.5, "low_match")));
        var validation = validationService.validateRerank(request, result);
        assertThat(validation.status()).isEqualTo(ProviderValidationStatus.REJECTED);
    }
}
