package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class EvidenceSourceVersionScorer implements EvaluationScorer {

    public static final String METRIC_ID = "evidence_source_version";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isEvidenceEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Evidence Source Version");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        EvidenceRetrievalSnapshot snapshot = state == null ? null : state.getEvidenceRetrieval();
        if (snapshot == null || snapshot.acceptedCandidates().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Evidence Source Version");
        }

        for (EvidenceCandidate candidate : snapshot.acceptedCandidates()) {
            EvidenceRef ref = candidate.evidenceRef();
            if (ref == null
                    || ref.sourceId() == null
                    || ref.sourceId().isBlank()
                    || ref.chunkId() == null
                    || ref.chunkId().isBlank()) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Evidence Source Version",
                        MetricSeverity.MAJOR,
                        true,
                        false,
                        "Accepted evidence missing source_id or chunk_id");
            }
            String version = ref.evidenceCorpusVersion() != null && !ref.evidenceCorpusVersion().isBlank()
                    ? ref.evidenceCorpusVersion()
                    : ref.assetPackageVersion();
            if (version == null || version.isBlank()) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Evidence Source Version",
                        MetricSeverity.MAJOR,
                        true,
                        false,
                        "Accepted evidence missing version");
            }
        }
        return ScorerSupport.pass(
                METRIC_ID, "Evidence Source Version", true, true, "Evidence source/version complete");
    }

    private boolean isEvidenceEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("evidence_eval");
    }
}
