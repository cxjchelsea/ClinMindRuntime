from pydantic import BaseModel, Field


class CapabilityProfile(BaseModel):
    profile_id: str
    provider_id: str
    provider_version: str
    model_id: str
    model_version: str
    capability_type: str
    schema_version: str
    allowed_use_cases: list[str] = Field(min_length=1)
    forbidden_use_cases: list[str] = Field(min_length=1)
    max_input_items: int = Field(gt=0)
    max_input_chars: int = Field(gt=0)
    timeout_ms: int = Field(gt=0)
    patient_output_allowed: bool = False
    clinician_output_allowed: bool = True
    requires_validation: bool = True
    fallback_strategy: str
    risk_level: str = "CONTROLLED"
    status: str = "ACTIVE"
    created_at: str


class CapabilityProfilesResponse(BaseModel):
    provider_id: str
    provider_version: str
    schema_version: str
    profiles: list[CapabilityProfile]
