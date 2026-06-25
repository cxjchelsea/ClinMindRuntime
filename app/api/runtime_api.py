from __future__ import annotations

from fastapi import APIRouter, HTTPException, Request

from app.api.schemas import ApiError, ApiResponse, ContinueRuntimeRequest, StartRuntimeRequest
from app.runtime.runtime_service import (
    RuntimeService,
    build_result_payload,
    build_start_continue_payload,
    build_status_payload,
    build_trace_payload,
)
from app.storage.runtime_store import RuntimeNotFoundError, RuntimeStore

router = APIRouter(prefix="/api/v1/runtime", tags=["runtime"])


def get_store(request: Request) -> RuntimeStore:
    return request.app.state.runtime_store


def get_service(request: Request) -> RuntimeService:
    return request.app.state.runtime_service


def _success(data: dict, trace_id: str | None = None) -> ApiResponse:
    return ApiResponse(success=True, data=data, error=None, trace_id=trace_id)


def _error(code: str, message: str, status_code: int) -> HTTPException:
    return HTTPException(
        status_code=status_code,
        detail=ApiResponse(success=False, data=None, error=ApiError(code=code, message=message), trace_id=None).model_dump(),
    )


@router.post("/start", response_model=ApiResponse)
def start_runtime(request_body: StartRuntimeRequest, request: Request) -> ApiResponse:
    if not request_body.input.text.strip():
        raise _error("INVALID_REQUEST", "input.text 不能为空", 400)

    service = get_service(request)
    state, trace = service.start_runtime(
        session_id=request_body.session_id,
        user_id=request_body.user_id,
        mode=request_body.mode,
        text=request_body.input.text,
        attachments=request_body.input.attachments,
        basic_info=request_body.basic_info,
    )
    return _success(build_start_continue_payload(state), trace_id=trace.trace_id)


@router.post("/continue", response_model=ApiResponse)
def continue_runtime(request_body: ContinueRuntimeRequest, request: Request) -> ApiResponse:
    if not request_body.input.text.strip():
        raise _error("INVALID_REQUEST", "input.text 不能为空", 400)

    service = get_service(request)
    try:
        state, trace = service.continue_runtime(
            runtime_id=request_body.runtime_id,
            text=request_body.input.text,
            attachments=request_body.input.attachments,
        )
    except RuntimeNotFoundError:
        raise _error("RUNTIME_NOT_FOUND", "Runtime 不存在", 404) from None

    return _success(build_start_continue_payload(state), trace_id=trace.trace_id)


@router.get("/{runtime_id}/status", response_model=ApiResponse)
def get_runtime_status(runtime_id: str, request: Request) -> ApiResponse:
    service = get_service(request)
    try:
        state = service.get_status(runtime_id)
    except RuntimeNotFoundError:
        raise _error("RUNTIME_NOT_FOUND", "Runtime 不存在", 404) from None
    return _success(build_status_payload(state))


@router.get("/{runtime_id}/result", response_model=ApiResponse)
def get_runtime_result(runtime_id: str, request: Request) -> ApiResponse:
    service = get_service(request)
    try:
        state = service.get_result(runtime_id)
    except RuntimeNotFoundError:
        raise _error("RUNTIME_NOT_FOUND", "Runtime 不存在", 404) from None
    return _success(build_result_payload(state))


@router.get("/{runtime_id}/trace", response_model=ApiResponse)
def get_runtime_trace(runtime_id: str, request: Request) -> ApiResponse:
    service = get_service(request)
    try:
        traces = service.get_traces(runtime_id)
    except RuntimeNotFoundError:
        raise _error("RUNTIME_NOT_FOUND", "Runtime 不存在", 404) from None
    return _success(build_trace_payload(runtime_id, traces))
