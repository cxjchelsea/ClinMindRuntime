from __future__ import annotations

from datetime import datetime, timezone
from typing import Any
from uuid import uuid4

from pydantic import BaseModel, Field

from app.state.runtime_state import DecisionBoundaryResult, SafetyGateResult


def _utc_now() -> datetime:
    return datetime.now(timezone.utc)


def _generate_trace_id() -> str:
    return f"trace_{uuid4().hex[:12]}"


class RuntimeTrace(BaseModel):
    trace_id: str = Field(default_factory=_generate_trace_id)
    runtime_id: str
    step: int = 1
    input: str = ""
    modules_executed: list[str] = Field(default_factory=list)
    knowledge_used: list[str] = Field(default_factory=list)
    experience_used: list[str] = Field(default_factory=list)
    safety_gate_result: SafetyGateResult | None = None
    ddx_change: dict[str, Any] | None = None
    evidence_graph_change: dict[str, Any] | None = None
    decision_boundary_result: DecisionBoundaryResult | None = None
    output_summary: dict[str, Any] | None = None
    created_at: datetime = Field(default_factory=_utc_now)

    def record_module(self, module_name: str) -> None:
        if module_name not in self.modules_executed:
            self.modules_executed.append(module_name)

    def record_knowledge(self, asset: str) -> None:
        if asset not in self.knowledge_used:
            self.knowledge_used.append(asset)

    def record_experience(self, unit_id: str) -> None:
        if unit_id not in self.experience_used:
            self.experience_used.append(unit_id)
