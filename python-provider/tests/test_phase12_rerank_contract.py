from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_phase12_rerank_contract_keeps_candidate_ids_and_marks_test_double():
    response = client.post(
        "/v1/providers/rerank",
        json={
            "request_id": "rr_phase12_001",
            "provider_id": "phase12-reranker",
            "query": "chest pain high risk red flag",
            "candidates": [
                {"candidate_id": "candidate_chest", "text": "chest pain red flag guideline", "metadata": {"version_id": "ver_1"}},
                {"candidate_id": "candidate_fever", "text": "fever safety notice", "metadata": {"version_id": "ver_2"}},
            ],
            "top_k": 2,
            "trace_ref": "trace_rr_001",
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["schema_version"] == "provider.rerank.v1"
    assert body["provider_id"] == "phase12-reranker"
    assert body["results"][0]["candidate_id"] == "candidate_chest"
    assert [item["rank"] for item in body["results"]] == [1, 2]
    assert {item["candidate_id"] for item in body["results"]} == {"candidate_chest", "candidate_fever"}
    assert "deterministic_test_double_not_real_cross_encoder_reranker" in body["warnings"]
    assert body["implementation_kind"] == "DETERMINISTIC_TEST_DOUBLE"


def test_phase12_rerank_rejects_duplicate_ids_and_wrong_provider():
    duplicate = client.post(
        "/v1/providers/rerank",
        json={
            "request_id": "rr_phase12_dup",
            "provider_id": "phase12-reranker",
            "query": "query",
            "candidates": [
                {"candidate_id": "same", "text": "a"},
                {"candidate_id": "same", "text": "b"},
            ],
        },
    )
    assert duplicate.status_code == 422

    wrong_provider = client.post(
        "/v1/providers/rerank",
        json={
            "request_id": "rr_phase12_bad_provider",
            "provider_id": "python_ai_provider",
            "query": "query",
            "candidates": [{"candidate_id": "one", "text": "a"}],
        },
    )
    assert wrong_provider.status_code == 400