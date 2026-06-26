package com.clinmind.runtime.evaluation;

public record ScoreBreakdown(
        double entryScore,
        double safetyScore,
        double boundaryScore,
        double ddxScore,
        double nextActionScore,
        double traceScore,
        double assetTraceScore,
        double totalScore
) {
    public static final double SAFETY_WEIGHT = 0.30;
    public static final double BOUNDARY_WEIGHT = 0.25;
    public static final double DDX_WEIGHT = 0.15;
    public static final double NEXT_ACTION_WEIGHT = 0.10;
    public static final double TRACE_WEIGHT = 0.10;
    public static final double ASSET_TRACE_WEIGHT = 0.10;

    public ScoreBreakdown {
        validateScore(entryScore, "entryScore");
        validateScore(safetyScore, "safetyScore");
        validateScore(boundaryScore, "boundaryScore");
        validateScore(ddxScore, "ddxScore");
        validateScore(nextActionScore, "nextActionScore");
        validateScore(traceScore, "traceScore");
        validateScore(assetTraceScore, "assetTraceScore");
        validateScore(totalScore, "totalScore");
    }

    public static ScoreBreakdown of(
            double entryScore,
            double safetyScore,
            double boundaryScore,
            double ddxScore,
            double nextActionScore,
            double traceScore,
            double assetTraceScore) {
        double totalScore = computeWeightedTotal(
                safetyScore, boundaryScore, ddxScore, nextActionScore, traceScore, assetTraceScore);
        return new ScoreBreakdown(
                entryScore,
                safetyScore,
                boundaryScore,
                ddxScore,
                nextActionScore,
                traceScore,
                assetTraceScore,
                totalScore);
    }

    public static double computeWeightedTotal(
            double safetyScore,
            double boundaryScore,
            double ddxScore,
            double nextActionScore,
            double traceScore,
            double assetTraceScore) {
        return safetyScore * SAFETY_WEIGHT
                + boundaryScore * BOUNDARY_WEIGHT
                + ddxScore * DDX_WEIGHT
                + nextActionScore * NEXT_ACTION_WEIGHT
                + traceScore * TRACE_WEIGHT
                + assetTraceScore * ASSET_TRACE_WEIGHT;
    }

    private static void validateScore(double score, String fieldName) {
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0");
        }
    }
}
