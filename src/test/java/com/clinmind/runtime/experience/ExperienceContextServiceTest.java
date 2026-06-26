package com.clinmind.runtime.experience;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.ExperienceContext;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.WorkMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExperienceContextServiceTest {

    @Autowired
    private ExperienceContextService experienceContextService;

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Test
    void returnsEmptyExperienceContextWhenNoSymptomGroup() {
        ExperienceContext context = experienceContextService.buildExperienceContext(
                new CaseFrame(), new KnowledgeContext());

        assertThat(context.matchedExperienceUnits()).isEmpty();
        assertThat(context.experienceAlerts()).isEmpty();
        assertThat(context.implementationMode()).isEqualTo("empty");
    }

    @Test
    void returnsVerifiedExperienceUnitsForChestPain() {
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical", 0.8);
        CaseFrame caseFrame = new CaseFrame("胸口闷", null, null, null, null, null, null, null);
        KnowledgeContext knowledge = knowledgeContextService.buildKnowledgeContext(caseFrame, entry);

        ExperienceContext context = experienceContextService.buildExperienceContext(caseFrame, knowledge);

        assertThat(context.matchedExperienceUnits()).hasSize(1);
        assertThat(context.matchedExperienceUnits().get(0).unitId()).isEqualTo("exp_chest_activity_001");
        assertThat(context.implementationMode()).isEqualTo("provider");
    }
}
