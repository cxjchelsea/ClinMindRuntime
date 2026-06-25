import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.storage.runtime_store import RuntimeStore


@pytest.fixture
def client() -> TestClient:
    store = RuntimeStore()
    app.state.runtime_store = store
    from app.runtime.runtime_service import RuntimeService

    app.state.runtime_service = RuntimeService(store)
    return TestClient(app)


def test_start_runtime_creates_state(client: TestClient) -> None:
    response = client.post(
        "/api/v1/runtime/start",
        json={
            "session_id": "s_001",
            "user_id": "u_001",
            "mode": "patient_facing",
            "input": {"text": "我最近胸口闷，活动后更明显", "attachments": []},
            "basic_info": {"age": 58, "sex": "male"},
        },
    )
    assert response.status_code == 200
    body = response.json()
    assert body["success"] is True
    assert body["trace_id"] is not None
    data = body["data"]
    assert data["runtime_id"].startswith("rt_")
    assert data["work_mode"] == "clinical_mode"
    assert data["entry_assessment"]["symptom_group"] == "chest_pain"
    assert data["case_frame"]["chief_complaint"] == "我最近胸口闷"
    assert "age" not in data["case_frame"]["missing_slots"]


def test_continue_runtime_updates_state(client: TestClient) -> None:
    start = client.post(
        "/api/v1/runtime/start",
        json={
            "session_id": "s_001",
            "mode": "patient_facing",
            "input": {"text": "胸口闷"},
            "basic_info": {"age": 58, "sex": "male"},
        },
    )
    runtime_id = start.json()["data"]["runtime_id"]

    response = client.post(
        "/api/v1/runtime/continue",
        json={
            "runtime_id": runtime_id,
            "input": {"text": "有点出汗，走路快的时候更明显，休息会缓解"},
        },
    )
    assert response.status_code == 200
    data = response.json()["data"]
    assert data["runtime_status"] == "waiting_for_user"
    assert any(item["name"] == "sweating" for item in data["case_frame"]["symptoms"])


def test_runtime_not_found(client: TestClient) -> None:
    response = client.get("/api/v1/runtime/rt_missing/status")
    assert response.status_code == 404
    body = response.json()["detail"]
    assert body["success"] is False
    assert body["error"]["code"] == "RUNTIME_NOT_FOUND"


def test_get_status_and_trace(client: TestClient) -> None:
    start = client.post(
        "/api/v1/runtime/start",
        json={
            "session_id": "s_001",
            "mode": "patient_facing",
            "input": {"text": "我发烧两天了"},
        },
    )
    runtime_id = start.json()["data"]["runtime_id"]

    status = client.get(f"/api/v1/runtime/{runtime_id}/status")
    assert status.status_code == 200
    assert status.json()["data"]["runtime_status"] == "collecting_case_info"

    trace = client.get(f"/api/v1/runtime/{runtime_id}/trace")
    assert trace.status_code == 200
    assert len(trace.json()["data"]["traces"]) == 1


def test_emergency_hint_work_mode_only(client: TestClient) -> None:
    response = client.post(
        "/api/v1/runtime/start",
        json={
            "session_id": "s_001",
            "mode": "patient_facing",
            "input": {"text": "胸口闷，活动后加重，出汗"},
        },
    )
    data = response.json()["data"]
    assert data["work_mode"] == "emergency_hint"
    assert data["runtime_status"] == "collecting_case_info"
    assert data["runtime_status"] != "safety_gate_triggered"


def test_invalid_request_empty_input(client: TestClient) -> None:
    response = client.post(
        "/api/v1/runtime/start",
        json={
            "session_id": "s_001",
            "mode": "patient_facing",
            "input": {"text": "   "},
        },
    )
    assert response.status_code == 400
    assert response.json()["detail"]["error"]["code"] == "INVALID_REQUEST"
