package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderGovernanceSnapshot;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class JudgeBoundaryAgreementScorer implements EvaluationScorer {

    public static final String METRIC_ID = "judge_boundary_agreement";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("judge_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Judge Boundary Agreement");
        }
        ProviderGovernanceSnapshot snapshot = providerGovernance(context);
        if (snapshot == null || snapshot.capability() != ProviderCapabilityType.JUDGE) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Judge Boundary Agreement",
                    MetricSeverity.MAJOR,
                    true,
                    false,
                    "Judge governance snapshot missing");
        }
        if (snapshot.validationStatus() == ProviderValidationStatus.REJECTED) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Judge Boundary Agreement",
                    MetricSeverity.MAJOR,
                    "validation accepted",
                    "validation rejected",
                    "Judge result failed provider validation");
        }
        return ScorerSupport.pass(
                METRIC_ID,
                "Judge Boundary Agreement",
                "validation accepted",
                snapshot.validationStatus(),
                "Judge result remained inside validation boundary");
    }

    private ProviderGovernanceSnapshot providerGovernance(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getProviderGovernance();
    }
}
