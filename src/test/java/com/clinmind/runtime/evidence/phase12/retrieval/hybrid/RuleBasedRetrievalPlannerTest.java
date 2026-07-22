package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.ClinicalQuestionType;
import com.clinmind.runtime.evidence.phase12.EvidenceApplicabilityContext;
import com.clinmind.runtime.evidence.phase12.EvidenceQueryRequest;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RuleBasedRetrievalPlannerTest {

    @Test
    void buildsStructuredPlanFromChestPainRiskRequest() {
        RuleBasedRetrievalPlanner planner = new RuleBasedRetrievalPlanner();

        RetrievalPlan plan = planner.plan(new EvidenceQueryRequest(
                "req_plan_001",
                "rt_plan_001",
                "活动后胸痛伴出汗有哪些 high risk signs?",
                ClinicalQuestionType.RISK_ASSESSMENT,
                "chest_pain",
                new EvidenceApplicabilityContext("adult", "male", "home", "GLOBAL", "clinician", java.util.Map.of()),
                EvidenceRetrievalScope.PRODUCTION,
                8,
                java.util.List.of()), Instant.parse("2026-07-22T00:00:00Z"));

        assertThat(plan.normalizedQuestion()).contains("chest pain");
        assertThat(plan.lexicalQueries()).hasSize(2);
        assertThat(plan.denseQueries()).containsExactly(plan.normalizedQuestion());
        assertThat(plan.eligibleSourceTypes()).contains(EvidenceSourceType.GUIDELINE, EvidenceSourceType.CLINICAL_PATHWAY);
        assertThat(plan.eligibleSpecialties()).contains("emergency_medicine", "cardiology");
        assertThat(plan.scope().retrievalScope()).isEqualTo(EvidenceRetrievalScope.PRODUCTION);
        assertThat(plan.scope().jurisdictionFilters()).containsExactly("GLOBAL");
        assertThat(plan.scope().intendedAudienceFilters()).containsExactly("clinician");
        assertThat(plan.rrfK()).isEqualTo(60);
        assertThat(plan.finalTopK()).isEqualTo(8);
    }
}