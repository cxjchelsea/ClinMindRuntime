from __future__ import annotations

from app.state.runtime_state import RuntimeState
from app.state.runtime_trace import RuntimeTrace


class RuntimeNotFoundError(Exception):
    def __init__(self, runtime_id: str) -> None:
        self.runtime_id = runtime_id
        super().__init__(f"Runtime 不存在: {runtime_id}")


class RuntimeStore:
    """Phase 1 内存版 Runtime 状态存储。"""

    def __init__(self) -> None:
        self._states: dict[str, RuntimeState] = {}
        self._traces: dict[str, list[RuntimeTrace]] = {}

    def create(self, state: RuntimeState) -> RuntimeState:
        if self.exists(state.runtime_id):
            raise ValueError(f"Runtime 已存在: {state.runtime_id}")
        stored = state.model_copy(deep=True)
        self._states[stored.runtime_id] = stored
        self._traces.setdefault(stored.runtime_id, [])
        return stored

    def get(self, runtime_id: str) -> RuntimeState:
        if not self.exists(runtime_id):
            raise RuntimeNotFoundError(runtime_id)
        return self._states[runtime_id].model_copy(deep=True)

    def update(self, state: RuntimeState) -> RuntimeState:
        if not self.exists(state.runtime_id):
            raise RuntimeNotFoundError(state.runtime_id)
        stored = state.model_copy(deep=True)
        self._states[state.runtime_id] = stored
        return stored

    def exists(self, runtime_id: str) -> bool:
        return runtime_id in self._states

    def add_trace(self, trace: RuntimeTrace) -> RuntimeTrace:
        if not self.exists(trace.runtime_id):
            raise RuntimeNotFoundError(trace.runtime_id)
        self._traces.setdefault(trace.runtime_id, []).append(trace.model_copy(deep=True))
        return trace

    def get_traces(self, runtime_id: str) -> list[RuntimeTrace]:
        if not self.exists(runtime_id):
            raise RuntimeNotFoundError(runtime_id)
        return [t.model_copy(deep=True) for t in self._traces.get(runtime_id, [])]
