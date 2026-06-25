package com.clinmind.runtime.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeTraceTest {

    @Test
    void createAndRecordFields() {
        RuntimeTrace trace = RuntimeTrace.create("rt_001", 1, "胸口闷");

        assertThat(trace.getTraceId()).startsWith("trace_");
        assertThat(trace.getRuntimeId()).isEqualTo("rt_001");
        assertThat(trace.getInput()).isEqualTo("胸口闷");

        trace.recordModule("EntryAssessment");
        trace.recordModule("EntryAssessment");
        trace.recordKnowledge("assets/red_flag_rules.yml");
        trace.setSafetyGateResult(new SafetyGateResult(
                true, RiskLevel.HIGH, List.of("rf_001"), "activity related", null, null, false));
        trace.setDdxChange(Map.of("added", List.of("high_risk_a")));
        trace.setEvidenceGraphChange(Map.of("missing_evidence", List.of("sweating")));
        trace.setDecisionBoundaryResult(new DecisionBoundaryResult(
                OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION,
                false,
                true,
                "high risk",
                List.of("no_low_risk_reassurance")));
        trace.setOutputSummary(Map.of("patient_output_level", "O5_visit_or_urgent_care_recommendation"));

        assertThat(trace.getModulesExecuted()).containsExactly("EntryAssessment");
        assertThat(trace.getKnowledgeUsed()).containsExactly("assets/red_flag_rules.yml");
        assertThat(trace.getSafetyGateResult().triggered()).isTrue();
        assertThat(trace.getDdxChange()).containsKey("added");
        assertThat(trace.getOutputSummary()).isNotNull();
    }
}
