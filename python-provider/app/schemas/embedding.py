from pydantic import BaseModel, Field

from app.schemas.common import ProviderTraceSummary


class EmbeddingInputItem(BaseModel):
    item_id: str
    text: str = Field(min_length=1)


class EmbeddingRequest(BaseModel):
    request_id: str
    runtime_id: str | None = None
    provider_id: str
    purpose: str = "evidence_embedding"
    items: list[EmbeddingInputItem] = Field(min_length=1)
    schema_version: str = "0.8.0"


class EmbeddingOutputItem(BaseModel):
    item_id: str
    vector: list[float]
    dimension: int
    text_hash: str
    normalized: bool = True


class EmbeddingResultPayload(BaseModel):
    items: list[EmbeddingOutputItem]


class EmbeddingResponse(BaseModel):
    request_id: str
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    schema_version: str
    status: str
    result: EmbeddingResultPayload | None = None
    warnings: list[str] = Field(default_factory=list)
    error_code: str | None = None
    latency_ms: int = 0
    trace: ProviderTraceSummary = Field(default_factory=ProviderTraceSummary)


class Phase12EmbeddingRequest(BaseModel):
    request_id: str
    provider_id: str
    texts: list[str] = Field(min_length=1, max_length=32)
    input_type: str = "QUERY"
    normalize: bool = True
    trace_ref: str | None = None


class Phase12EmbeddingResponse(BaseModel):
    schema_version: str = "provider.embedding.v1"
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    dimension: int
    normalized: bool
    embeddings: list[list[float]]
    latency_ms: int = 0
    warnings: list[str] = Field(default_factory=list)
    implementation_kind: str = "DETERMINISTIC_TEST_DOUBLE"
    trace_ref: str | None = None