from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_phase12_embedding_contract_returns_metadata_and_warning():
    response = client.post(
        "/v1/providers/embedding",
        json={
            "request_id": "phase12_emb_001",
            "provider_id": "phase12-embedding",
            "texts": ["acute chest pain with sweating", "胸痛 出汗"],
            "input_type": "QUERY",
            "normalize": True,
            "trace_ref": "trace_phase12_emb_001",
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["schema_version"] == "provider.embedding.v1"
    assert body["provider_id"] == "phase12-embedding"
    assert body["dimension"] == 16
    assert len(body["embeddings"]) == 2
    assert all(len(vector) == 16 for vector in body["embeddings"])
    assert body["implementation_kind"] == "DETERMINISTIC_TEST_DOUBLE"
    assert "deterministic_test_double_not_real_dense_embedding" in body["warnings"]
    assert body["trace_ref"] == "trace_phase12_emb_001"


def test_phase12_embedding_rejects_wrong_provider_and_oversized_text():
    wrong_provider = client.post(
        "/v1/providers/embedding",
        json={"request_id": "bad_provider", "provider_id": "python_ai_provider", "texts": ["abc"]},
    )
    assert wrong_provider.status_code == 400

    too_long = client.post(
        "/v1/providers/embedding",
        json={"request_id": "too_long", "provider_id": "phase12-embedding", "texts": ["x" * 4097]},
    )
    assert too_long.status_code == 422


def test_phase12_provider_metadata_is_complete():
    response = client.get("/v1/providers/metadata")

    assert response.status_code == 200
    body = response.json()
    assert body["provider_id"] == "phase12-embedding"
    assert body["capability_type"] == "EMBEDDING"
    assert body["dimension"] == 16
    assert "provider.embedding.v1" in body["schema_versions"]
    assert body["implementation_kind"] == "DETERMINISTIC_TEST_DOUBLE"