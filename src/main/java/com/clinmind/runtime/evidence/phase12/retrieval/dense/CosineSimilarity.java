package com.clinmind.runtime.evidence.phase12.retrieval.dense;

import java.util.List;

public final class CosineSimilarity {
    private CosineSimilarity() {
    }

    public static double similarity(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || left.size() != right.size()) {
            throw new IllegalArgumentException("vectors must be non-empty and have the same dimension");
        }
        double dot = 0.0d;
        double leftNorm = 0.0d;
        double rightNorm = 0.0d;
        for (int i = 0; i < left.size(); i++) {
            double l = left.get(i);
            double r = right.get(i);
            if (Double.isNaN(l) || Double.isInfinite(l) || Double.isNaN(r) || Double.isInfinite(r)) {
                throw new IllegalArgumentException("vectors contain invalid value");
            }
            dot += l * r;
            leftNorm += l * l;
            rightNorm += r * r;
        }
        if (leftNorm == 0.0d || rightNorm == 0.0d) {
            return 0.0d;
        }
        double cosine = dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
        return Math.max(0.0d, Math.min(1.0d, (cosine + 1.0d) / 2.0d));
    }
}