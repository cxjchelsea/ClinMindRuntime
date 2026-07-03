package com.clinmind.runtime.provider.rerank;

public record RankedItem(String itemId, int rank, double score, String reasonCode) {
}
