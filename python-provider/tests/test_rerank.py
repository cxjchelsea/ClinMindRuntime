from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_rerank_prefers_chest_pain_item():
    response = client.post(
        "/v1/rerank",
        json={
            "request_id": "provider_req_002",
            "runtime_id": "runtime_demo_001",
            "provider_id": "python_ai_provider",
            "purpose": "evidence_rerank",
            "query": {"query_id": "query_001", "text": "胸口闷，活动后更明显，出汗"},
            "items": [
                {"item_id": "chunk_chest_pain_001", "text": "胸痛风险信号识别 chest_pain"},
                {"item_id": "chunk_fever_001", "text": "发热相关安全提醒 fever"},
            ],
            "schema_version": "0.8.0",
        },
    )
    assert response.status_code == 200
    body = response.json()
    ranked = body["result"]["ranked_items"]
    assert ranked[0]["item_id"] == "chunk_chest_pain_001"
    assert ranked[0]["rank"] == 1
    assert 0.0 <= ranked[0]["score"] <= 1.0
    assert {item["item_id"] for item in ranked} == {"chunk_chest_pain_001", "chunk_fever_001"}
