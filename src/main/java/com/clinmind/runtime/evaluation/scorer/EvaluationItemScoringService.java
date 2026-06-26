package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EvaluationItemScoringService {

    private final List<EvaluationScorer> scorers;

    public EvaluationItemScoringService(List<EvaluationScorer> scorers) {
        this.scorers = List.copyOf(scorers);
    }

    public EvaluationItemResult score(String runId, EvaluationCase evaluationCase, RuntimeCaseExecution execution) {
        ScorerContext context = new ScorerContext(runId, evaluationCase, execution);
        List<MetricResult> metrics = new ArrayList<>();
        List<SafetyViolation> violations = new ArrayList<>();
        for (EvaluationScorer scorer : scorers) {
            MetricResult metric = scorer.score(context);
            metrics.add(metric);
            violations.addAll(ScorerSupport.toViolations(metric, context));
        }

        ScoreBreakdown breakdown = ScorerSupport.buildBreakdown(metrics);
        boolean passed = metrics.stream().allMatch(MetricResult::passed)
                && violations.stream().noneMatch(violation -> violation.severity() == MetricSeverity.CRITICAL);

        List<String> traceIds = execution.traces().stream()
                .map(RuntimeTrace::getTraceId)
                .toList();
        List<String> notes = new ArrayList<>();
        notes.add("scoring_completed");

        return new EvaluationItemResult(
                runId,
                evaluationCase.caseId(),
                execution.runtimeId(),
                traceIds,
                passed,
                breakdown.totalScore(),
                breakdown,
                metrics,
                violations,
                notes);
    }
}
