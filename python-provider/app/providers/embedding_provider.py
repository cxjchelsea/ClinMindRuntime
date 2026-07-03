import hashlib
import math
import re

from app.config import EMBEDDING_DIMENSION


def text_hash(text: str) -> str:
    digest = hashlib.sha256(text.encode("utf-8")).hexdigest()
    return f"sha256:{digest}"


def embed_text(text: str, dimension: int = EMBEDDING_DIMENSION) -> list[float]:
    seed = hashlib.sha256(text.encode("utf-8")).digest()
    raw = []
    for index in range(dimension):
        value = seed[index % len(seed)]
        raw.append((value / 255.0) * 2.0 - 1.0)
    norm = math.sqrt(sum(value * value for value in raw))
    if norm == 0:
        return [0.0] * dimension
    normalized = [round(value / norm, 4) for value in raw]
    return normalized


def embed_items(items: list[tuple[str, str]], dimension: int = EMBEDDING_DIMENSION) -> list[dict]:
    output = []
    for item_id, text in items:
        vector = embed_text(text, dimension)
        output.append(
            {
                "item_id": item_id,
                "vector": vector,
                "dimension": dimension,
                "text_hash": text_hash(text),
                "normalized": True,
            }
        )
    return output
