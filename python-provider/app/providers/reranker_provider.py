import re

CHEST_PAIN_KEYWORDS = {"胸", "闷", "痛", "活动", "出汗", "压榨", "chest", "pain"}
FEVER_KEYWORDS = {"发热", "发烧", "体温", "fever", "temperature"}
ABDOMINAL_KEYWORDS = {"腹", "肚子", "abdominal", "pain"}


def _tokenize(text: str) -> set[str]:
    lowered = text.lower()
    tokens = set(re.findall(r"[a-z0-9]+", lowered))
    for char in text:
        if "\u4e00" <= char <= "\u9fff":
            tokens.add(char)
    return tokens


def _keyword_bonus(text: str) -> tuple[float, str]:
    lowered = text.lower()
    if "chest_pain" in lowered or any(keyword in text for keyword in CHEST_PAIN_KEYWORDS):
        return 0.35, "symptom_group_match"
    if "fever" in lowered or any(keyword in text for keyword in FEVER_KEYWORDS):
        return 0.2, "symptom_group_match"
    if "abdominal" in lowered or any(keyword in text for keyword in ABDOMINAL_KEYWORDS):
        return 0.2, "symptom_group_match"
    return 0.0, "low_match"


def score_item(query_text: str, item_text: str) -> tuple[float, str]:
    query_tokens = _tokenize(query_text)
    item_tokens = _tokenize(item_text)
    overlap = len(query_tokens & item_tokens)
    base = overlap / max(len(query_tokens), 1)
    bonus, reason = _keyword_bonus(item_text)
    score = min(1.0, max(0.0, round(base * 0.55 + bonus, 2)))
    if score >= 0.5:
        return score, reason
    return score, "low_match"


def rerank_items(query_text: str, query_id: str, items: list[tuple[str, str]]) -> dict:
    scored = []
    for item_id, text in items:
        score, reason_code = score_item(query_text, text)
        scored.append({"item_id": item_id, "score": score, "reason_code": reason_code})
    scored.sort(key=lambda item: (-item["score"], item["item_id"]))
    ranked_items = []
    for index, item in enumerate(scored, start=1):
        ranked_items.append(
            {
                "item_id": item["item_id"],
                "rank": index,
                "score": item["score"],
                "reason_code": item["reason_code"],
            }
        )
    return {"query_id": query_id, "ranked_items": ranked_items}
