package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionTraceScorer implements EvaluationScorer {

    public static final String METRIC_ID = "asset_trace";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        if (expected.requiredAssetTrace() == null || !expected.requiredAssetTrace()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Asset Version Trace");
        }

        List<RuntimeTrace> traces = ScorerSupport.traces(context);
        if (traces.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Asset Version Trace",
                    MetricSeverity.CRITICAL,
                    "asset trace",
                    List.of(),
                    "Missing runtime traces for asset version audit");
        }

        List<String> issues = new ArrayList<>();
        for (RuntimeTrace trace : traces) {
            Map<String, Object> summary = trace.getOutputSummary();
            if (summary == null) {
                issues.add("trace " + trace.getTraceId() + " missing output_summary");
                continue;
            }
            if (summary.get("asset_package_id") == null) {
                issues.add("trace " + trace.getTraceId() + " missing asset_package_id");
            }
            if (summary.get("asset_package_version") == null) {
                issues.add("trace " + trace.getTraceId() + " missing asset_package_version");
            }
            if (!containsVersionedAsset(trace.getKnowledgeUsed())) {
                issues.add("trace " + trace.getTraceId() + " missing versioned knowledge_used");
            }
        }

        if (!issues.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Asset Version Trace",
                    MetricSeverity.CRITICAL,
                    "asset_package_id/version and knowledge_used@version",
                    issues,
                    String.join("; ", issues));
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "Asset Version Trace",
                "asset trace required",
                traces.size() + " traces",
                "Asset version trace expectations met");
    }

    private boolean containsVersionedAsset(List<String> assetRefs) {
        if (assetRefs == null || assetRefs.isEmpty()) {
            return false;
        }
        return assetRefs.stream().anyMatch(ref -> ref != null && ref.contains("@"));
    }
}
