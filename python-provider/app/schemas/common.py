from pydantic import BaseModel, Field


class ProviderTraceSummary(BaseModel):
    input_count: int = 0
    output_count: int = 0


class HealthResponse(BaseModel):
    status: str
    provider_id: str
    provider_version: str
    time: str


class ProviderCapability(BaseModel):
    capability: str
    model_id: str
    model_version: str
    enabled: bool = True
    dimension: int | None = None


class ProvidersResponse(BaseModel):
    provider_id: str
    provider_version: str
    capabilities: list[ProviderCapability]


class ProviderErrorResponse(BaseModel):
    request_id: str | None = None
    provider_id: str
    provider_version: str
    schema_version: str
    status: str
    error_code: str
    message: str
