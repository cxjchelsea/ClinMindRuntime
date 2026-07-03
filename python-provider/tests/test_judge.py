from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def _request(text: str):
    return {
        "request_id": "judge_req_001",
        "runtime_id": "runtime_demo_001",
        "provider_id": "python_ai_provider",
        "judge_target_type": "PATIENT_OUTPUT_DRAFT",
        "judge_target_id": "patient_output_draft_001",
        "rubric_id": "patient_boundary_rubric",
        "rubric_version": "0.1.0",
        "input_summary": {"text": text, "symptom_group": "chest_pain"},
        "dimensions": ["boundary_safety", "medical_certainty", "patient_readability"],
        "forbidden_labels": ["final_diagnosis", "treatment_instruction"],
        "schema_version": "0.8.1",
    }


def test_judge_returns_high_score_for_safe_draft():
    response = client.post("/v1/judge", json=_request("Please seek in-person care for further assessment."))
    assert response.status_code == 200
    result = response.json()["result"]
    assert result["overall_score"] > 0.8
    assert result["violations"] == []
    assert "take" not in result["rationale_summary"].lower()


def test_judge_detects_final_diagnosis_language():
    response = client.post("/v1/judge", json=_request("你就是急性冠脉综合征，需要立即开始治疗方案。"))
    assert response.status_code == 200
    result = response.json()["result"]
    assert "final_diagnosis" in result["violations"]
    assert result["overall_score"] < 0.8
