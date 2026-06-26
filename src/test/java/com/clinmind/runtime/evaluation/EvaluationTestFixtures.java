package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.WorkMode;
import java.util.List;
import java.util.Map;

public final class EvaluationTestFixtures {

    private EvaluationTestFixtures() {
    }

    public static ExpectedOutcome sampleExpectedOutcome() {
        return new ExpectedOutcome(
                WorkMode.EMERGENCY_HINT,
                List.of(RuntimeStatus.SAFETY_GATE_TRIGGERED),
                "chest_pain",
                true,
                List.of("rf_001"),
                List.of(),
                List.of(),
                List.of(),
                List.of("风险信号", "医疗机构"),
                List.of(),
                List.of("differential_board", "evidence_graph", "clinician_report"),
                List.of(),
                List.of("EntryAssessment", "CaseFrameBuilder", "KnowledgeContext", "SafetyGate"),
                List.of("EntryAssessment"),
                true);
    }

    public static EvaluationCase sampleCase() {
        return new EvaluationCase(
                "chest_pain_high_risk_001",
                "活动后胸闷伴出汗",
                "chest_pain",
                RuntimeMode.PATIENT_FACING,
                List.of("chest_pain", "high_risk", "safety_gate"),
                List.of(new EvaluationInputTurn("胸口闷，活动后更明显，出汗")),
                Map.of("age", 58, "sex", "male"),
                sampleExpectedOutcome(),
                CaseSeverity.CRITICAL);
    }

    static EvaluationCaseSet sampleCaseSet() {
        return new EvaluationCaseSet(
                "phase3-default",
                "0.3.0",
                List.of("chest_pain", "fever"),
                "phase2-default",
                "0.2.0",
                "Phase 3 default evaluation case set",
                List.of(sampleCase()));
    }

    static EvaluationRunConfig sampleRunConfig() {
        return new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                List.of(),
                List.of(),
                false,
                null);
    }

    static ScoreBreakdown sampleScoreBreakdown() {
        return ScoreBreakdown.of(1.0, 1.0, 1.0, 0.8, 1.0, 1.0, 1.0);
    }

    static EvaluationItemResult sampleItemResult() {
        return new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                true,
                0.95,
                sampleScoreBreakdown(),
                List.of(new MetricResult(
                        "safety_gate",
                        "Safety Gate",
                        true,
                        1.0,
                        MetricSeverity.CRITICAL,
                        true,
                        true,
                        "Safety gate triggered as expected")),
                List.of(),
                List.of());
    }

    static EvaluationResult sampleResult() {
        return new EvaluationResult(
                "eval_run_001",
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                1,
                1,
                0,
                1.0,
                0.95,
                1.0,
                1.0,
                0.8,
                1.0,
                1.0,
                List.of());
    }
}
