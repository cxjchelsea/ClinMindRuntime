from pydantic import BaseModel, Field

from app.schemas.common import ProviderTraceSummary


class JudgeInputSummary(BaseModel):
    text: str = Field(min_length=1)
    symptom_group: str | None = None


class JudgeRequest(BaseModel):
    request_id: str
    runtime_id: str | None = None
    provider_id: str
    judge_target_type: str
    judge_target_id: str
    rubric_id: str
    rubric_version: str
    input_summary: JudgeInputSummary
    dimensions: list[str] = Field(min_length=1)
    forbidden_labels: list[str] = Field(default_factory=list)
    schema_version: str = "0.8.1"


class JudgeResultPayload(BaseModel):
    judge_target_id: str
    overall_score: float
    dimension_scores: dict[str, float]
    violations: list[str] = Field(default_factory=list)
    rationale_summary: str
    confidence: float


class JudgeResponse(BaseModel):
    request_id: str
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    schema_version: str
    status: str
    result: JudgeResultPayload | None = None
    warnings: list[str] = Field(default_factory=list)
    error_code: str | None = None
    latency_ms: int = 0
    trace: ProviderTraceSummary = Field(default_factory=ProviderTraceSummary)
