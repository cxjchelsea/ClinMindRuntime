from pydantic import BaseModel, Field

from app.schemas.common import ProviderTraceSummary


class RerankQuery(BaseModel):
    query_id: str
    text: str = Field(min_length=1)


class RerankInputItem(BaseModel):
    item_id: str
    text: str = Field(min_length=1)


class RerankRequest(BaseModel):
    request_id: str
    runtime_id: str | None = None
    provider_id: str
    purpose: str = "evidence_rerank"
    query: RerankQuery
    items: list[RerankInputItem] = Field(min_length=1)
    schema_version: str = "0.8.0"


class RankedItem(BaseModel):
    item_id: str
    rank: int
    score: float
    reason_code: str


class RerankResultPayload(BaseModel):
    query_id: str
    ranked_items: list[RankedItem]


class RerankResponse(BaseModel):
    request_id: str
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    schema_version: str
    status: str
    result: RerankResultPayload | None = None
    warnings: list[str] = Field(default_factory=list)
    error_code: str | None = None
    latency_ms: int = 0
    trace: ProviderTraceSummary = Field(default_factory=ProviderTraceSummary)


class Phase12RerankCandidate(BaseModel):
    candidate_id: str = Field(min_length=1)
    text: str = Field(min_length=1, max_length=4096)
    metadata: dict[str, str] = Field(default_factory=dict)


class Phase12RerankRequest(BaseModel):
    request_id: str = Field(min_length=1)
    provider_id: str = Field(min_length=1)
    query: str = Field(min_length=1, max_length=4096)
    candidates: list[Phase12RerankCandidate] = Field(min_length=1, max_length=64)
    top_k: int = Field(default=15, ge=1, le=64)
    trace_ref: str | None = None


class Phase12RerankResult(BaseModel):
    candidate_id: str
    score: float
    rank: int


class Phase12RerankResponse(BaseModel):
    schema_version: str = "provider.rerank.v1"
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    results: list[Phase12RerankResult]
    latency_ms: int = 0
    warnings: list[str] = Field(default_factory=list)
    trace_ref: str | None = None
    implementation_kind: str = "DETERMINISTIC_TEST_DOUBLE"