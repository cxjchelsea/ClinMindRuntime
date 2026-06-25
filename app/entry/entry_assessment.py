from __future__ import annotations

import re

from app.state.runtime_state import EntryAssessmentResult, UserInput
from app.state.runtime_status import WorkMode

WELLNESS_KEYWORDS = ("养生", "保健", "减肥", "健康饮食", "怎么锻炼", "运动建议", "睡眠建议")
UNSUPPORTED_KEYWORDS = ("写代码", "编程", "股票", "天气怎么样", "帮我翻译", "python", "Python")
EMERGENCY_KEYWORDS = ("呼吸困难", "意识不清", "大量出血", "快不行了", "喘不过气", "昏迷")
CLINICAL_KEYWORDS = (
    "痛",
    "疼",
    "闷",
    "热",
    "发烧",
    "发热",
    "咳嗽",
    "头晕",
    "恶心",
    "呕吐",
    "腹泻",
    "不舒服",
    "症状",
    "难受",
)
CHEST_SYMPTOM_KEYWORDS = ("胸", "心口", "胸口", "胸闷", "胸痛")
FEVER_SYMPTOM_KEYWORDS = ("发热", "发烧", "体温", "高烧")
EMERGENCY_FEATURE_KEYWORDS = ("出汗", "出冷汗", "加重", "放射", "濒死", "活动后加重")


def assess_entry(
    user_input: UserInput,
    basic_info: dict | None = None,
) -> EntryAssessmentResult:
    text = user_input.text.strip()
    if not text:
        return EntryAssessmentResult(
            work_mode=WorkMode.UNSUPPORTED,
            reason="empty input",
            confidence=1.0,
        )

    if _is_unsupported_topic(text):
        return EntryAssessmentResult(
            work_mode=WorkMode.UNSUPPORTED,
            reason="unsupported topic",
            confidence=0.9,
        )

    if any(keyword in text for keyword in EMERGENCY_KEYWORDS) or _looks_like_emergency_hint(text):
        return EntryAssessmentResult(
            work_mode=WorkMode.EMERGENCY_HINT,
            symptom_group=_detect_symptom_group(text),
            reason="possible emergency features detected",
            confidence=0.85,
        )

    if any(keyword in text for keyword in WELLNESS_KEYWORDS) and not _has_clinical_signal(text):
        return EntryAssessmentResult(
            work_mode=WorkMode.WELLNESS_MODE,
            reason="wellness consultation",
            confidence=0.8,
        )

    if _has_clinical_signal(text):
        return EntryAssessmentResult(
            work_mode=WorkMode.CLINICAL_MODE,
            symptom_group=_detect_symptom_group(text),
            reason="clinical symptoms detected",
            confidence=0.8,
        )

    if re.fullmatch(r"[\W\d_]+", text):
        return EntryAssessmentResult(
            work_mode=WorkMode.UNSUPPORTED,
            reason="unrecognizable input",
            confidence=0.7,
        )

    return EntryAssessmentResult(
        work_mode=WorkMode.CLINICAL_MODE,
        symptom_group=_detect_symptom_group(text),
        reason="default clinical clarification",
        confidence=0.5,
    )


def _is_unsupported_topic(text: str) -> bool:
    lowered = text.lower()
    if any(keyword.lower() in lowered for keyword in UNSUPPORTED_KEYWORDS):
        return True
    return "代码" in text and ("写" in text or "编程" in text)


def _has_clinical_signal(text: str) -> bool:
    return any(keyword in text for keyword in CLINICAL_KEYWORDS)


def _looks_like_emergency_hint(text: str) -> bool:
    has_chest = any(keyword in text for keyword in CHEST_SYMPTOM_KEYWORDS)
    has_feature = any(keyword in text for keyword in EMERGENCY_FEATURE_KEYWORDS)
    return has_chest and has_feature


def _detect_symptom_group(text: str) -> str | None:
    if any(keyword in text for keyword in CHEST_SYMPTOM_KEYWORDS):
        return "chest_pain"
    if any(keyword in text for keyword in FEVER_SYMPTOM_KEYWORDS):
        return "fever"
    return None
