from __future__ import annotations

from app.state.runtime_state import CaseFrame, PatientProfile, SymptomItem, UserInput

STANDARD_MISSING_SLOTS = (
    "age",
    "sex",
    "symptom_duration",
    "symptom_severity",
    "associated_symptoms",
)

SYMPTOM_PATTERNS: list[tuple[str, str, str | None]] = [
    ("chest_discomfort", "胸", "chest"),
    ("chest_pain", "胸痛", "chest"),
    ("chest_tightness", "胸闷", "chest"),
    ("fever", "发热", None),
    ("fever", "发烧", None),
    ("cough", "咳嗽", None),
    ("headache", "头痛", None),
    ("dizziness", "头晕", None),
    ("sweating", "出汗", None),
    ("nausea", "恶心", None),
]

DURATION_PATTERNS = ("天", "小时", "周", "月", "久", "刚开始")
SEVERITY_PATTERNS = ("轻微", "明显", "严重", "剧烈", "加重")
TRIGGER_PATTERNS = ("活动后", "走路", "休息时", "夜间")
RELIEF_PATTERNS = ("休息后缓解", "休息会缓解", "缓解", "好转")


def build_or_update_case_frame(
    user_input: UserInput,
    existing_case_frame: CaseFrame | None = None,
    basic_info: dict | None = None,
) -> CaseFrame:
    frame = existing_case_frame.model_copy(deep=True) if existing_case_frame else CaseFrame()
    text = user_input.text.strip()

    if basic_info:
        _apply_basic_info(frame.patient_profile, basic_info)

    if text:
        if frame.chief_complaint is None:
            frame.chief_complaint = _extract_chief_complaint(text)
        frame.symptoms = _merge_symptoms(frame.symptoms, _extract_symptoms(text))

    frame.missing_slots = _compute_missing_slots(frame)
    return frame


def _apply_basic_info(profile: PatientProfile, basic_info: dict) -> None:
    age = basic_info.get("age")
    sex = basic_info.get("sex")
    if age is not None:
        profile.age = int(age)
    if sex is not None:
        profile.sex = str(sex)
    risk_factors = basic_info.get("risk_factors")
    if isinstance(risk_factors, list):
        profile.risk_factors = [str(item) for item in risk_factors]


def _extract_chief_complaint(text: str) -> str:
    for separator in ("。", "，", ",", "；", ";", "\n"):
        if separator in text:
            first = text.split(separator, 1)[0].strip()
            if first:
                return first[:120]
    return text[:120]


def _extract_symptoms(text: str) -> list[SymptomItem]:
    symptoms: list[SymptomItem] = []
    seen_names: set[str | None] = set()

    for name, keyword, location in SYMPTOM_PATTERNS:
        if keyword in text and name not in seen_names:
            symptoms.append(
                SymptomItem(
                    name=name,
                    location=location,
                    duration=_extract_first_match(text, DURATION_PATTERNS),
                    severity=_extract_first_match(text, SEVERITY_PATTERNS),
                    trigger=_extract_first_match(text, TRIGGER_PATTERNS),
                    relief=_extract_first_match(text, RELIEF_PATTERNS),
                )
            )
            seen_names.add(name)

    if not symptoms and _looks_like_general_discomfort(text):
        symptoms.append(
            SymptomItem(
                name="general_discomfort",
                duration=_extract_first_match(text, DURATION_PATTERNS),
                severity=_extract_first_match(text, SEVERITY_PATTERNS),
            )
        )

    return symptoms


def _merge_symptoms(existing: list[SymptomItem], new_items: list[SymptomItem]) -> list[SymptomItem]:
    merged = [item.model_copy(deep=True) for item in existing]
    existing_names = {item.name for item in merged if item.name}

    for item in new_items:
        if item.name in existing_names:
            for index, current in enumerate(merged):
                if current.name == item.name:
                    merged[index] = _merge_symptom_item(current, item)
                    break
        else:
            merged.append(item)
            if item.name:
                existing_names.add(item.name)

    return merged


def _merge_symptom_item(current: SymptomItem, incoming: SymptomItem) -> SymptomItem:
    return SymptomItem(
        name=current.name or incoming.name,
        duration=current.duration or incoming.duration,
        severity=current.severity or incoming.severity,
        location=current.location or incoming.location,
        trigger=current.trigger or incoming.trigger,
        frequency=current.frequency or incoming.frequency,
        relief=current.relief or incoming.relief,
    )


def _compute_missing_slots(frame: CaseFrame) -> list[str]:
    missing: list[str] = []
    if frame.patient_profile.age is None:
        missing.append("age")
    if frame.patient_profile.sex is None:
        missing.append("sex")
    if not frame.symptoms:
        missing.append("symptoms")
    elif all(item.duration is None for item in frame.symptoms):
        missing.append("symptom_duration")
    if frame.symptoms and all(item.severity is None for item in frame.symptoms):
        missing.append("symptom_severity")
    if frame.chief_complaint and "伴随" not in frame.chief_complaint and len(frame.symptoms) <= 1:
        missing.append("associated_symptoms")

    return sorted(set(missing))


def _extract_first_match(text: str, patterns: tuple[str, ...]) -> str | None:
    for pattern in patterns:
        if pattern in text:
            return pattern
    return None


def _looks_like_general_discomfort(text: str) -> bool:
    return any(keyword in text for keyword in ("不舒服", "难受", "不适"))
