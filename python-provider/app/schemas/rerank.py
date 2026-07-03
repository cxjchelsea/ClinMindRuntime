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
