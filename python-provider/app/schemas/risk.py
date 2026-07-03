from pydantic import BaseModel, Field

from app.schemas.common import ProviderTraceSummary


class RiskCaseFrameSummary(BaseModel):
    known_facts: list[str] = Field(default_factory=list)
    missing_facts: list[str] = Field(default_factory=list)


class RiskSignalClassificationRequest(BaseModel):
    request_id: str
    runtime_id: str | None = None
    provider_id: str
    symptom_group: str
    case_frame_summary: RiskCaseFrameSummary = Field(default_factory=RiskCaseFrameSummary)
    red_flag_candidates: list[str] = Field(default_factory=list)
    allowed_labels: list[str] = Field(default_factory=lambda: ["LOW", "MEDIUM", "HIGH", "UNKNOWN"])
    schema_version: str = "0.8.1"


class RiskSignalDraftPayload(BaseModel):
    risk_labels: list[str] = Field(min_length=1)
    risk_score: float
    matched_reasons: list[str] = Field(default_factory=list)
    uncertainty: float


class RiskSignalClassificationResponse(BaseModel):
    request_id: str
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    schema_version: str
    status: str
    result: RiskSignalDraftPayload | None = None
    warnings: list[str] = Field(default_factory=list)
    error_code: str | None = None
    latency_ms: int = 0
    trace: ProviderTraceSummary = Field(default_factory=ProviderTraceSummary)
