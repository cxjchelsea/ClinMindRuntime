import pytest

from app.state.runtime_state import RuntimeState
from app.state.runtime_status import RuntimeStatus
from app.state.runtime_trace import RuntimeTrace
from app.storage.runtime_store import RuntimeNotFoundError, RuntimeStore


@pytest.fixture
def store() -> RuntimeStore:
    return RuntimeStore()


def test_create_and_get(store: RuntimeStore) -> None:
    state = RuntimeState(session_id="s_001")
    created = store.create(state)
    assert store.exists(created.runtime_id)
    fetched = store.get(created.runtime_id)
    assert fetched.session_id == "s_001"
    assert fetched.runtime_status == RuntimeStatus.CREATED


def test_update(store: RuntimeStore) -> None:
    state = store.create(RuntimeState(session_id="s_001"))
    state.runtime_status = RuntimeStatus.CLINICAL_MODE
    state.bump_version()
    updated = store.update(state)
    assert updated.runtime_status == RuntimeStatus.CLINICAL_MODE
    assert updated.version == 2
    assert store.get(state.runtime_id).version == 2


def test_exists(store: RuntimeStore) -> None:
    state = RuntimeState(session_id="s_001")
    assert store.exists(state.runtime_id) is False
    store.create(state)
    assert store.exists(state.runtime_id) is True


def test_get_not_found(store: RuntimeStore) -> None:
    with pytest.raises(RuntimeNotFoundError) as exc_info:
        store.get("rt_missing")
    assert exc_info.value.runtime_id == "rt_missing"


def test_update_not_found(store: RuntimeStore) -> None:
    state = RuntimeState(session_id="s_001", runtime_id="rt_missing")
    with pytest.raises(RuntimeNotFoundError):
        store.update(state)


def test_create_duplicate_raises(store: RuntimeStore) -> None:
    state = RuntimeState(session_id="s_001")
    store.create(state)
    with pytest.raises(ValueError, match="已存在"):
        store.create(state)


def test_add_and_get_traces(store: RuntimeStore) -> None:
    state = store.create(RuntimeState(session_id="s_001"))
    trace = RuntimeTrace(runtime_id=state.runtime_id, input="test input")
    store.add_trace(trace)
    traces = store.get_traces(state.runtime_id)
    assert len(traces) == 1
    assert traces[0].input == "test input"


def test_add_trace_not_found(store: RuntimeStore) -> None:
    trace = RuntimeTrace(runtime_id="rt_missing", input="test")
    with pytest.raises(RuntimeNotFoundError):
        store.add_trace(trace)
