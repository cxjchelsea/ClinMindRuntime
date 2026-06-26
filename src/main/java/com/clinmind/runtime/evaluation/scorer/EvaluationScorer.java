package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;

public interface EvaluationScorer {

    String metricId();

    MetricResult score(ScorerContext context);
}
