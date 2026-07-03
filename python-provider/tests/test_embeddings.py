from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_embedding_stable_vector():
    payload = {
        "request_id": "provider_req_001",
        "runtime_id": "runtime_demo_001",
        "provider_id": "python_ai_provider",
        "purpose": "evidence_embedding",
        "items": [
            {
                "item_id": "chunk_chest_pain_001",
                "text": "胸痛伴活动后加重和出汗时，应关注高风险信号。",
            }
        ],
        "schema_version": "0.8.0",
    }
    first = client.post("/v1/embeddings", json=payload).json()
    second = client.post("/v1/embeddings", json=payload).json()
    assert first["status"] == "SUCCESS"
    assert first["result"]["items"][0]["vector"] == second["result"]["items"][0]["vector"]
    assert first["result"]["items"][0]["dimension"] == 16
    assert first["result"]["items"][0]["text_hash"].startswith("sha256:")
    assert "text" not in first["result"]["items"][0]


def test_embedding_rejects_empty_items():
    response = client.post(
        "/v1/embeddings",
        json={
            "request_id": "provider_req_empty",
            "provider_id": "python_ai_provider",
            "items": [],
        },
    )
    assert response.status_code == 422
