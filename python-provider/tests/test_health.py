from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_up():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "UP"
    assert body["provider_id"] == "python_ai_provider"
    assert body["provider_version"] == "0.8.1-p1"


def test_providers_capabilities():
    response = client.get("/v1/providers")
    assert response.status_code == 200
    body = response.json()
    capabilities = {item["capability"]: item for item in body["capabilities"]}
    assert "EMBEDDING" in capabilities
    assert "RERANK" in capabilities
    assert "JUDGE" in capabilities
    assert "RISK_CLASSIFICATION" in capabilities
    assert capabilities["EMBEDDING"]["dimension"] == 16
    assert capabilities["EMBEDDING"]["model_version"] == "0.1.0"
