package com.clinmind.runtime.evidence.graph.policy;

import com.clinmind.runtime.evidence.graph.GraphConstants;
import com.clinmind.runtime.evidence.graph.GraphPolicyContext;
import com.clinmind.runtime.evidence.graph.GraphPolicyDecision;
import com.clinmind.runtime.evidence.graph.kg.KgLiteGraphRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GraphEvidencePolicy {

    private final KgLiteGraphRepository graphRepository;

    public GraphEvidencePolicy(KgLiteGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    public GraphPolicyDecision evaluate(GraphPolicyContext context) {
        List<String> reasons = new ArrayList<>();
        if (context == null) {
            return GraphPolicyDecision.reject("policy context missing");
        }
        if (context.runtimeId() == null || context.runtimeId().isBlank()) {
            reasons.add("runtime_id missing");
        }
        if (context.symptomGroup() == null || context.symptomGroup().isBlank()) {
            reasons.add("symptom_group missing");
        } else if (!GraphConstants.SUPPORTED_SYMPTOM_GROUPS.contains(context.symptomGroup())) {
            reasons.add("unsupported symptom_group: " + context.symptomGroup());
        }
        if (context.safetyGateFailSafe()) {
            reasons.add("safety gate fail-safe required");
        }
        if (!graphRepository.isAvailable() || !context.graphAvailable()) {
            reasons.add("kg lite graph unavailable");
        }
        if (context.acceptedEvidenceRefIds().isEmpty()) {
            reasons.add("accepted_evidence_refs is empty");
        }
        if (!reasons.isEmpty()) {
            return GraphPolicyDecision.reject(reasons);
        }
        return GraphPolicyDecision.allow();
    }
}
