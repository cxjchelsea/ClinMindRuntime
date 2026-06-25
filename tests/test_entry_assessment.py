import pytest

from app.entry.entry_assessment import assess_entry
from app.state.runtime_state import UserInput
from app.state.runtime_status import WorkMode


@pytest.mark.parametrize(
    ("text", "expected_mode"),
    [
        ("怎么养生比较好", WorkMode.WELLNESS_MODE),
        ("我最近胸口闷，活动后更明显", WorkMode.CLINICAL_MODE),
        ("胸口闷，活动后加重，出汗", WorkMode.EMERGENCY_HINT),
        ("呼吸困难，快不行了", WorkMode.EMERGENCY_HINT),
        ("帮我写Python代码", WorkMode.UNSUPPORTED),
        ("", WorkMode.UNSUPPORTED),
    ],
)
def test_assess_entry_modes(text: str, expected_mode: WorkMode) -> None:
    result = assess_entry(UserInput(text=text))
    assert result.work_mode == expected_mode


def test_emergency_hint_only_marks_work_mode() -> None:
    result = assess_entry(UserInput(text="胸口闷，活动后加重，出汗"))
    assert result.work_mode == WorkMode.EMERGENCY_HINT
    assert result.work_mode.value != "safety_gate_triggered"


def test_clinical_mode_detects_symptom_group() -> None:
    result = assess_entry(UserInput(text="我发烧两天了"))
    assert result.work_mode == WorkMode.CLINICAL_MODE
    assert result.symptom_group == "fever"


def test_chest_pain_symptom_group() -> None:
    result = assess_entry(UserInput(text="胸口闷，有点不舒服"))
    assert result.work_mode == WorkMode.CLINICAL_MODE
    assert result.symptom_group == "chest_pain"
