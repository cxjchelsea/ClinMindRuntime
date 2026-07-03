from datetime import datetime, timezone
import time

from fastapi import FastAPI, HTTPException

from app import config
from app.providers.embedding_provider import embed_items
from app.providers.reranker_provider import rerank_items
from app.schemas.common import HealthResponse, ProviderCapability, ProvidersResponse, ProviderTraceSummary
from app.schemas.embedding import EmbeddingRequest, EmbeddingResponse, EmbeddingResultPayload
from app.schemas.rerank import RerankRequest, RerankResponse, RerankResultPayload

app = FastAPI(title="ClinMind Python AI Provider", version=config.PROVIDER_VERSION)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="UP",
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        time=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
    )


@app.get("/v1/providers", response_model=ProvidersResponse)
def providers() -> ProvidersResponse:
    return ProvidersResponse(
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        capabilities=[
            ProviderCapability(
                capability="EMBEDDING",
                model_id=config.EMBEDDING_MODEL_ID,
                model_version=config.EMBEDDING_MODEL_VERSION,
                dimension=config.EMBEDDING_DIMENSION,
                enabled=True,
            ),
            ProviderCapability(
                capability="RERANK",
                model_id=config.RERANK_MODEL_ID,
                model_version=config.RERANK_MODEL_VERSION,
                enabled=True,
            ),
        ],
    )


@app.post("/v1/embeddings", response_model=EmbeddingResponse)
def embeddings(request: EmbeddingRequest) -> EmbeddingResponse:
    started = time.perf_counter()
    if request.provider_id != config.PROVIDER_ID:
        raise HTTPException(status_code=400, detail="INVALID_PROVIDER_ID")
    if not request.items:
        raise HTTPException(status_code=400, detail="EMPTY_ITEMS")

    items = [(item.item_id, item.text) for item in request.items]
    payload = EmbeddingResultPayload(items=embed_items(items))
    latency_ms = int((time.perf_counter() - started) * 1000)
    return EmbeddingResponse(
        request_id=request.request_id,
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        model_id=config.EMBEDDING_MODEL_ID,
        model_version=config.EMBEDDING_MODEL_VERSION,
        schema_version=config.SCHEMA_VERSION,
        status="SUCCESS",
        result=payload,
        latency_ms=latency_ms,
        trace=ProviderTraceSummary(input_count=len(items), output_count=len(items)),
    )


@app.post("/v1/rerank", response_model=RerankResponse)
def rerank(request: RerankRequest) -> RerankResponse:
    started = time.perf_counter()
    if request.provider_id != config.PROVIDER_ID:
        raise HTTPException(status_code=400, detail="INVALID_PROVIDER_ID")
    if not request.items:
        raise HTTPException(status_code=400, detail="EMPTY_ITEMS")

    items = [(item.item_id, item.text) for item in request.items]
    payload = RerankResultPayload(**rerank_items(request.query.text, request.query.query_id, items))
    latency_ms = int((time.perf_counter() - started) * 1000)
    return RerankResponse(
        request_id=request.request_id,
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        model_id=config.RERANK_MODEL_ID,
        model_version=config.RERANK_MODEL_VERSION,
        schema_version=config.SCHEMA_VERSION,
        status="SUCCESS",
        result=payload,
        latency_ms=latency_ms,
        trace=ProviderTraceSummary(input_count=len(items), output_count=len(items)),
    )
