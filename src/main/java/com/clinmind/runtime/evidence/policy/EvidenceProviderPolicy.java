package com.clinmind.runtime.evidence.policy;

import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidencePolicyContext;
import com.clinmind.runtime.evidence.EvidencePolicyDecision;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpusRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EvidenceProviderPolicy {

    private final EvidenceCorpusRepository corpusRepository;

    public EvidenceProviderPolicy(EvidenceCorpusRepository corpusRepository) {
        this.corpusRepository = corpusRepository;
    }

    public EvidencePolicyDecision evaluate(EvidencePolicyContext context) {
        List<String> reasons = new ArrayList<>();

        if (context == null) {
            return EvidencePolicyDecision.reject("policy context missing");
        }
        if (context.runtimeId() == null || context.runtimeId().isBlank()) {
            reasons.add("runtime_id missing");
        }
        if (context.symptomGroup() == null || context.symptomGroup().isBlank()) {
            reasons.add("symptom_group missing");
        } else if (!EvidenceConstants.SUPPORTED_SYMPTOM_GROUPS.contains(context.symptomGroup())) {
            reasons.add("unsupported symptom_group: " + context.symptomGroup());
        }
        if (context.safetyGateFailSafe()) {
            reasons.add("safety gate fail-safe required");
        }
        if (!corpusRepository.isAvailable() || !context.corpusAvailable()) {
            reasons.add("evidence corpus unavailable");
        }

        if (!reasons.isEmpty()) {
            return EvidencePolicyDecision.reject(reasons);
        }
        return EvidencePolicyDecision.allow();
    }

    public List<String> highRiskWarnings(EvidencePolicyContext context) {
        if (context == null || context.redFlagSummary().isEmpty()) {
            return List.of();
        }
        if (!corpusRepository.isAvailable()) {
            return List.of("high risk context but evidence corpus unavailable");
        }
        return List.of();
    }
}
