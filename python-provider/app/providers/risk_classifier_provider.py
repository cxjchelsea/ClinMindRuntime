HIGH_RISK_TOKENS = {
    "activity_related_chest_pain",
    "sweating",
    "出汗",
    "活动后加重",
    "activity",
    "exertion",
    "chest_pain",
}


def classify_risk(symptom_group: str, known_facts: list[str], red_flag_candidates: list[str], allowed_labels: list[str]) -> dict:
    facts = " ".join(known_facts + red_flag_candidates).lower()
    labels = set(allowed_labels)
    if symptom_group != "chest_pain":
        label = "UNKNOWN" if "UNKNOWN" in labels else next(iter(labels))
        return {
            "risk_labels": [label],
            "risk_score": 0.0,
            "matched_reasons": ["unknown_symptom_group"],
            "uncertainty": 0.88,
        }

    matched = [token for token in HIGH_RISK_TOKENS if token.lower() in facts]
    if len(matched) >= 2 and "HIGH" in labels:
        return {
            "risk_labels": ["HIGH"],
            "risk_score": 0.86,
            "matched_reasons": sorted(matched)[:5],
            "uncertainty": 0.21,
        }
    if matched and "MEDIUM" in labels:
        return {
            "risk_labels": ["MEDIUM"],
            "risk_score": 0.55,
            "matched_reasons": sorted(matched)[:5],
            "uncertainty": 0.35,
        }
    label = "LOW" if "LOW" in labels else next(iter(labels))
    return {
        "risk_labels": [label],
        "risk_score": 0.18,
        "matched_reasons": [],
        "uncertainty": 0.42,
    }
