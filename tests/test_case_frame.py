from app.case.case_frame import build_or_update_case_frame
from app.state.runtime_state import CaseFrame, UserInput


def test_build_case_frame_from_basic_info() -> None:
    frame = build_or_update_case_frame(
        UserInput(text="胸口闷，活动后更明显"),
        None,
        {"age": 58, "sex": "male"},
    )
    assert frame.patient_profile.age == 58
    assert frame.patient_profile.sex == "male"
    assert frame.chief_complaint == "胸口闷"
    assert any(item.name == "chest_discomfort" for item in frame.symptoms)
    assert "symptom_duration" in frame.missing_slots


def test_update_existing_case_frame() -> None:
    existing = build_or_update_case_frame(
        UserInput(text="胸口闷"),
        None,
        {"age": 58, "sex": "male"},
    )
    updated = build_or_update_case_frame(
        UserInput(text="有点出汗，走路快的时候更明显，休息会缓解"),
        existing,
        None,
    )
    assert updated.chief_complaint == "胸口闷"
    assert any(item.name == "sweating" for item in updated.symptoms)
    assert updated.patient_profile.age == 58


def test_missing_slots_generated() -> None:
    frame = build_or_update_case_frame(UserInput(text="今天有点不舒服"), None, None)
    assert "age" in frame.missing_slots
    assert "sex" in frame.missing_slots
    assert frame.chief_complaint == "今天有点不舒服"
