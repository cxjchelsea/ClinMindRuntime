from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field

from app.state.runtime_status import RuntimeMode


class UserInputRequest(BaseModel):
    text: str
    attachments: list[str] = Field(default_factory=list)


class StartRuntimeRequest(BaseModel):
    session_id: str
    user_id: str | None = None
    mode: RuntimeMode
    input: UserInputRequest
    basic_info: dict[str, Any] | None = None


class ContinueRuntimeRequest(BaseModel):
    runtime_id: str
    input: UserInputRequest


class ApiError(BaseModel):
    code: str
    message: str


class ApiResponse(BaseModel):
    success: bool
    data: dict[str, Any] | None = None
    error: ApiError | None = None
    trace_id: str | None = None
