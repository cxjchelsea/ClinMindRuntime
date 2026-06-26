package com.clinmind.runtime.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.WorkMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KnowledgeContextServiceTest {

    @Autowired
    private KnowledgeContextService knowledgeContextService;

    @Test
    void buildsKnowledgeContextForChestPain() {
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.CLINICAL_MODE, "chest_pain", "clinical", 0.8);
        CaseFrame caseFrame = new CaseFrame("胸口闷", null, null, null, null, null, null, null);

        KnowledgeContext context = knowledgeContextService.buildKnowledgeContext(caseFrame, entry);

        assertThat(context.symptomGroup()).isEqualTo("chest_pain");
        assertThat(context.mustNotMiss()).hasSize(2);
        assertThat(context.redFlags()).isNotEmpty();
        assertThat(context.sourceAssets())
                .isNotEmpty()
                .allMatch(ref -> ref.contains("@"));
    }

    @Test
    void returnsEmptyContextForUnsupportedEntry() {
        EntryAssessmentResult entry = new EntryAssessmentResult(
                WorkMode.UNSUPPORTED, null, "unsupported", 0.9);

        KnowledgeContext context = knowledgeContextService.buildKnowledgeContext(new CaseFrame(), entry);

        assertThat(context.symptomGroup()).isNull();
        assertThat(context.sourceAssets()).isEmpty();
    }
}
