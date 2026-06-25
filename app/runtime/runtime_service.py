from __future__ import annotations

from typing import Any

from app.case.case_frame import build_or_update_case_frame
from app.entry.entry_assessment import assess_entry
from app.state.runtime_state import RuntimeState, UserInput
from app.state.runtime_status import RuntimeStatus, WorkMode
from app.state.runtime_trace import RuntimeTrace
from app.storage.runtime_store import RuntimeNotFoundError, RuntimeStore


def status_after_entry(work_mode: WorkMode) -> RuntimeStatus:
    if work_mode == WorkMode.UNSUPPORTED:
        return RuntimeStatus.ERROR_SAFE_HALTED
    if work_mode == WorkMode.WELLNESS_MODE:
        return RuntimeStatus.WELLNESS_MODE
    return RuntimeStatus.COLLECTING_CASE_INFO


class RuntimeService:
    def __init__(self, store: RuntimeStore) -> None:
        self._store = store

    def start_runtime(
        self,
        *,
        session_id: str,
        user_id: str | None,
        mode,
        text: str,
        attachments: list[str] | None = None,
        basic_info: dict | None = None,
    ) -> tuple[RuntimeState, RuntimeTrace]:
        state = RuntimeState(session_id=session_id, user_id=user_id, mode=mode)
        state.runtime_status = RuntimeStatus.ENTRY_ASSESSING

        user_input = UserInput(text=text, attachments=attachments or [])
        state.input_history.append(user_input)

        entry = assess_entry(user_input, basic_info)
        state.entry_assessment = entry
        state.work_mode = entry.work_mode
        state.runtime_status = status_after_entry(entry.work_mode)

        if entry.work_mode != WorkMode.UNSUPPORTED:
            state.case_frame = build_or_update_case_frame(
                user_input,
                state.case_frame,
                basic_info,
            )

        trace = self._build_trace(state, step=1, user_input=user_input, basic_info=basic_info)
        self._store.create(state)
        self._store.add_trace(trace)
        state.runtime_trace_ids.append(trace.trace_id)
        state.bump_version()
        self._store.update(state)
        return state, trace

    def continue_runtime(
        self,
        *,
        runtime_id: str,
        text: str,
        attachments: list[str] | None = None,
    ) -> tuple[RuntimeState, RuntimeTrace]:
        state = self._store.get(runtime_id)
        user_input = UserInput(text=text, attachments=attachments or [])
        state.input_history.append(user_input)

        state.case_frame = build_or_update_case_frame(
            user_input,
            state.case_frame,
            None,
        )
        if state.runtime_status not in {
            RuntimeStatus.ERROR_SAFE_HALTED,
            RuntimeStatus.COMPLETED,
        }:
            state.runtime_status = RuntimeStatus.WAITING_FOR_USER

        step = len(self._store.get_traces(runtime_id)) + 1
        trace = self._build_trace(state, step=step, user_input=user_input, basic_info=None)
        self._store.add_trace(trace)
        state.runtime_trace_ids.append(trace.trace_id)
        state.bump_version()
        self._store.update(state)
        return state, trace

    def get_status(self, runtime_id: str) -> RuntimeState:
        return self._store.get(runtime_id)

    def get_result(self, runtime_id: str) -> RuntimeState:
        return self._store.get(runtime_id)

    def get_traces(self, runtime_id: str) -> list[RuntimeTrace]:
        return self._store.get_traces(runtime_id)

    def _build_trace(
        self,
        state: RuntimeState,
        *,
        step: int,
        user_input: UserInput,
        basic_info: dict | None,
    ) -> RuntimeTrace:
        trace = RuntimeTrace(
            runtime_id=state.runtime_id,
            step=step,
            input=user_input.text,
        )
        trace.record_module("EntryAssessment")
        if state.entry_assessment is not None:
            trace.output_summary = {
                "work_mode": state.entry_assessment.work_mode.value,
                "symptom_group": state.entry_assessment.symptom_group,
            }
        if state.work_mode != WorkMode.UNSUPPORTED:
            trace.record_module("CaseFrameBuilder")
            trace.output_summary = {
                **(trace.output_summary or {}),
                "chief_complaint": state.case_frame.chief_complaint,
                "missing_slots": state.case_frame.missing_slots,
            }
        if basic_info:
            trace.output_summary = {
                **(trace.output_summary or {}),
                "basic_info_applied": True,
            }
        return trace


def build_start_continue_payload(state: RuntimeState) -> dict[str, Any]:
    risk_level = None
    if state.safety_gate is not None:
        risk_level = state.safety_gate.risk_level.value

    payload: dict[str, Any] = {
        "runtime_id": state.runtime_id,
        "runtime_status": state.runtime_status.value,
        "work_mode": state.work_mode.value if state.work_mode else None,
        "risk_level": risk_level,
        "entry_assessment": (
            state.entry_assessment.model_dump(mode="json") if state.entry_assessment else None
        ),
        "case_frame": {
            "chief_complaint": state.case_frame.chief_complaint,
            "missing_slots": state.case_frame.missing_slots,
            "symptoms": [item.model_dump(mode="json") for item in state.case_frame.symptoms],
            "patient_profile": state.case_frame.patient_profile.model_dump(mode="json"),
        },
        "next_action": (
            state.question_test_policy.next_action.model_dump(mode="json")
            if state.question_test_policy
            else None
        ),
        "patient_output": (
            state.patient_output.model_dump(mode="json") if state.patient_output else None
        ),
        "clinician_report": (
            state.clinician_report.model_dump(mode="json") if state.clinician_report else None
        ),
    }
    return payload


def build_status_payload(state: RuntimeState) -> dict[str, Any]:
    risk_level = "unknown"
    if state.safety_gate is not None:
        risk_level = state.safety_gate.risk_level.value

    return {
        "runtime_id": state.runtime_id,
        "runtime_status": state.runtime_status.value,
        "work_mode": state.work_mode.value if state.work_mode else None,
        "risk_level": risk_level,
        "updated_at": state.updated_at.isoformat(),
    }


def build_result_payload(state: RuntimeState) -> dict[str, Any]:
    return {
        "runtime_id": state.runtime_id,
        "runtime_status": state.runtime_status.value,
        "patient_output": (
            state.patient_output.model_dump(mode="json") if state.patient_output else None
        ),
        "clinician_report": (
            state.clinician_report.model_dump(mode="json") if state.clinician_report else None
        ),
        "decision_boundary": (
            state.decision_boundary.model_dump(mode="json") if state.decision_boundary else None
        ),
    }


def build_trace_payload(runtime_id: str, traces: list[RuntimeTrace]) -> dict[str, Any]:
    return {
        "runtime_id": runtime_id,
        "traces": [trace.model_dump(mode="json") for trace in traces],
    }
