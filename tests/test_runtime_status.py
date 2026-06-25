from app.state.runtime_status import (
    CandidateStatus,
    NextActionType,
    OutputLevel,
    RiskLevel,
    RuntimeMode,
    RuntimeStatus,
    WorkMode,
)


def test_runtime_status_values() -> None:
    assert RuntimeStatus.CREATED == "created"
    assert RuntimeStatus.ERROR_SAFE_HALTED == "error_safe_halted"
    assert len(RuntimeStatus) == 14


def test_work_mode_values() -> None:
    assert WorkMode.EMERGENCY_HINT == "emergency_hint"
    assert WorkMode.UNSUPPORTED == "unsupported"


def test_runtime_mode_values() -> None:
    assert RuntimeMode.PATIENT_FACING == "patient_facing"
    assert RuntimeMode.CLINICIAN_COPILOT == "clinician_copilot"


def test_risk_level_values() -> None:
    assert RiskLevel.MEDIUM_HIGH == "medium_high"
    assert RiskLevel.HIGH == "high"


def test_candidate_status_includes_possible_after_exclusion() -> None:
    assert CandidateStatus.POSSIBLE_AFTER_EXCLUSION == "possible_after_exclusion"
    assert "possible_after_exclusion" in {s.value for s in CandidateStatus}


def test_next_action_type_values() -> None:
    assert NextActionType.SAFE_HALT == "safe_halt"
    assert NextActionType.ASK_QUESTION == "ask_question"


def test_output_level_values() -> None:
    assert OutputLevel.O4_LOW_RISK_REFERENCE == "O4_low_risk_reference"
    assert OutputLevel.O7_CLINICIAN_FULL_REPORT == "O7_clinician_full_report"
