from datetime import datetime, timezone

from app import config
from app.schemas.capability import CapabilityProfile


def capability_profiles() -> list[CapabilityProfile]:
    created_at = datetime(2026, 7, 3, tzinfo=timezone.utc).isoformat().replace("+00:00", "Z")
    return [
        CapabilityProfile(
            profile_id="profile_judge_mock_v1",
            provider_id=config.PROVIDER_ID,
            provider_version=config.PROVIDER_VERSION,
            model_id=config.JUDGE_MODEL_ID,
            model_version=config.JUDGE_MODEL_VERSION,
            capability_type="JUDGE",
            schema_version=config.SCHEMA_VERSION,
            allowed_use_cases=["evaluation", "output_boundary_check"],
            forbidden_use_cases=["patient_direct_answer", "final_diagnosis"],
            max_input_items=5,
            max_input_chars=4000,
            timeout_ms=1500,
            patient_output_allowed=False,
            clinician_output_allowed=True,
            requires_validation=True,
            fallback_strategy="RULE_BASED_SCORER",
            status="ACTIVE",
            created_at=created_at,
        ),
        CapabilityProfile(
            profile_id="profile_risk_classifier_mock_v1",
            provider_id=config.PROVIDER_ID,
            provider_version=config.PROVIDER_VERSION,
            model_id=config.RISK_CLASSIFIER_MODEL_ID,
            model_version=config.RISK_CLASSIFIER_MODEL_VERSION,
            capability_type="RISK_CLASSIFICATION",
            schema_version=config.SCHEMA_VERSION,
            allowed_use_cases=["evaluation", "risk_signal_draft"],
            forbidden_use_cases=["patient_direct_answer", "safety_gate_decision"],
            max_input_items=10,
            max_input_chars=3000,
            timeout_ms=1500,
            patient_output_allowed=False,
            clinician_output_allowed=True,
            requires_validation=True,
            fallback_strategy="JAVA_SAFETY_GATE",
            status="ACTIVE",
            created_at=created_at,
        ),
    ]
