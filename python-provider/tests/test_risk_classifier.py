from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_chest_pain_with_red_flags_returns_high_draft():
    response = client.post(
        "/v1/classify-risk",
        json={
            "request_id": "risk_req_001",
            "runtime_id": "runtime_demo_001",
            "provider_id": "python_ai_provider",
            "symptom_group": "chest_pain",
            "case_frame_summary": {"known_facts": ["chest pain", "activity", "sweating"], "missing_facts": []},
            "red_flag_candidates": ["activity_related_chest_pain", "sweating"],
            "allowed_labels": ["LOW", "MEDIUM", "HIGH", "UNKNOWN"],
            "schema_version": "0.8.1",
        },
    )
    assert response.status_code == 200
    body = response.json()
    assert body["result"]["risk_labels"] == ["HIGH"]
    assert 0.0 <= body["result"]["risk_score"] <= 1.0
    assert "draft_only_not_safety_gate_decision" in body["warnings"]


def test_unknown_symptom_group_returns_unknown():
    response = client.post(
        "/v1/classify-risk",
        json={
            "request_id": "risk_req_002",
            "provider_id": "python_ai_provider",
            "symptom_group": "unknown_group",
            "case_frame_summary": {"known_facts": [], "missing_facts": []},
            "red_flag_candidates": [],
            "allowed_labels": ["LOW", "MEDIUM", "HIGH", "UNKNOWN"],
            "schema_version": "0.8.1",
        },
    )
    assert response.status_code == 200
    assert response.json()["result"]["risk_labels"] == ["UNKNOWN"]
