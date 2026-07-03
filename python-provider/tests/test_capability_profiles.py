from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_capability_profiles_include_judge_and_risk():
    response = client.get("/v1/capability-profiles")
    assert response.status_code == 200
    body = response.json()
    profiles = {item["capability_type"]: item for item in body["profiles"]}
    assert profiles["JUDGE"]["profile_id"]
    assert profiles["JUDGE"]["patient_output_allowed"] is False
    assert profiles["JUDGE"]["requires_validation"] is True
    assert profiles["JUDGE"]["forbidden_use_cases"]
    assert profiles["RISK_CLASSIFICATION"]["forbidden_use_cases"]
