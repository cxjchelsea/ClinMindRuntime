from datetime import datetime, timezone
import time

from fastapi import FastAPI, HTTPException

from app import config
from app.providers.capability_profile_provider import capability_profiles
from app.providers.embedding_provider import embed_items
from app.providers.judge_provider import judge_text
from app.providers.reranker_provider import rerank_items
from app.providers.risk_classifier_provider import classify_risk
from app.schemas.capability import CapabilityProfilesResponse
from app.schemas.common import HealthResponse, ProviderCapability, ProvidersResponse, ProviderTraceSummary
from app.schemas.embedding import EmbeddingRequest, EmbeddingResponse, EmbeddingResultPayload
from app.schemas.judge import JudgeRequest, JudgeResponse, JudgeResultPayload
from app.schemas.rerank import RerankRequest, RerankResponse, RerankResultPayload
from app.schemas.risk import RiskSignalClassificationRequest, RiskSignalClassificationResponse, RiskSignalDraftPayload

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
            ProviderCapability(
                capability="JUDGE",
                model_id=config.JUDGE_MODEL_ID,
                model_version=config.JUDGE_MODEL_VERSION,
                enabled=True,
            ),
            ProviderCapability(
                capability="RISK_CLASSIFICATION",
                model_id=config.RISK_CLASSIFIER_MODEL_ID,
                model_version=config.RISK_CLASSIFIER_MODEL_VERSION,
                enabled=True,
            ),
        ],
    )


@app.get("/v1/capability-profiles", response_model=CapabilityProfilesResponse)
def get_capability_profiles() -> CapabilityProfilesResponse:
    return CapabilityProfilesResponse(
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        schema_version=config.SCHEMA_VERSION,
        profiles=capability_profiles(),
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


@app.post("/v1/judge", response_model=JudgeResponse)
def judge(request: JudgeRequest) -> JudgeResponse:
    started = time.perf_counter()
    if request.provider_id != config.PROVIDER_ID:
        raise HTTPException(status_code=400, detail="INVALID_PROVIDER_ID")
    payload = JudgeResultPayload(
        **judge_text(
            request.input_summary.text,
            request.judge_target_id,
            request.dimensions,
            request.forbidden_labels,
        )
    )
    latency_ms = int((time.perf_counter() - started) * 1000)
    return JudgeResponse(
        request_id=request.request_id,
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        model_id=config.JUDGE_MODEL_ID,
        model_version=config.JUDGE_MODEL_VERSION,
        schema_version=config.SCHEMA_VERSION,
        status="SUCCESS",
        result=payload,
        latency_ms=latency_ms,
        trace=ProviderTraceSummary(input_count=1, output_count=1),
    )


@app.post("/v1/classify-risk", response_model=RiskSignalClassificationResponse)
def classify_risk_signal(request: RiskSignalClassificationRequest) -> RiskSignalClassificationResponse:
    started = time.perf_counter()
    if request.provider_id != config.PROVIDER_ID:
        raise HTTPException(status_code=400, detail="INVALID_PROVIDER_ID")
    payload = RiskSignalDraftPayload(
        **classify_risk(
            request.symptom_group,
            request.case_frame_summary.known_facts,
            request.red_flag_candidates,
            request.allowed_labels,
        )
    )
    latency_ms = int((time.perf_counter() - started) * 1000)
    return RiskSignalClassificationResponse(
        request_id=request.request_id,
        provider_id=config.PROVIDER_ID,
        provider_version=config.PROVIDER_VERSION,
        model_id=config.RISK_CLASSIFIER_MODEL_ID,
        model_version=config.RISK_CLASSIFIER_MODEL_VERSION,
        schema_version=config.SCHEMA_VERSION,
        status="SUCCESS",
        result=payload,
        warnings=["draft_only_not_safety_gate_decision"],
        latency_ms=latency_ms,
        trace=ProviderTraceSummary(input_count=1, output_count=1),
    )
