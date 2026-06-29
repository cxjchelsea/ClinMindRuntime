package com.clinmind.runtime.candidate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CandidateTestFixtures {

    private CandidateTestFixtures() {
    }

    public static CandidateSourceRef sampleSourceRef() {
        return new CandidateSourceRef(
                CandidateSourceType.SAFETY_VIOLATION,
                "rt_sample001",
                "eval_run_001",
                "chest_pain_high_risk_001",
                "item_result_001",
                "trace_001",
                null,
                "sv_001",
                "safety_gate",
                "phase2-default",
                "0.2.0",
                "evaluation");
    }

    public static ExperienceCandidate sampleExperienceCandidate() {
        return new ExperienceCandidate(
                "exp_cand_001",
                ExperienceCandidateType.SAFETY_LESSON,
                "High-risk chest pain did not trigger expected red flag",
                "A chest pain evaluation case failed the SafetyGate metric.",
                sampleSourceRef(),
                CandidateRiskLevel.CRITICAL,
                null,
                "Review SafetyGate rules for chest pain high-risk cases.",
                Map.of("metric_id", "safety_gate"),
                List.of("chest_pain", "safety_gate"),
                Instant.parse("2026-06-25T10:00:00Z"),
                "candidate-generator",
                Map.of());
    }

    public static TrainingExampleCandidate sampleTrainingExampleCandidate() {
        return new TrainingExampleCandidate(
                "train_cand_001",
                TrainingTaskType.RISK_SIGNAL_CLASSIFICATION,
                sampleSourceRef(),
                Map.of("text", "胸口闷，活动后更明显，出汗"),
                Map.of("risk_level", "high", "red_flags", List.of("chest_pain", "sweating")),
                Map.of(),
                "high_risk_chest_pain",
                "SafetyGate failure on chest pain high-risk case.",
                CandidateRiskLevel.CRITICAL,
                null,
                null,
                List.of("chest_pain"),
                Instant.parse("2026-06-25T10:00:00Z"),
                Map.of());
    }

    public static CandidateGenerationResult sampleGenerationResult() {
        return new CandidateGenerationResult(
                "cand_gen_001",
                "eval_run_001",
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:00:05Z"),
                List.of(sampleExperienceCandidate()),
                List.of(sampleTrainingExampleCandidate()),
                List.of(new CandidateSkippedItem(
                        "chest_pain_low_risk_001",
                        "item_result_002",
                        "ddx_recall",
                        CandidateSkippedReason.PASSED_CASE_SKIPPED,
                        "Case passed all metrics.")),
                List.of());
    }
}
