package com.clinmind.runtime.knowledge;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RedFlagRule;
import com.clinmind.runtime.state.WorkMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeContextService {

    private final StaticRuleProvider staticRuleProvider;

    public KnowledgeContextService(StaticRuleProvider staticRuleProvider) {
        this.staticRuleProvider = staticRuleProvider;
    }

    public KnowledgeContext buildKnowledgeContext(CaseFrame caseFrame, EntryAssessmentResult entryAssessment) {
        if (entryAssessment == null || entryAssessment.workMode() == WorkMode.UNSUPPORTED) {
            return new KnowledgeContext();
        }

        String symptomGroup = entryAssessment.symptomGroup();
        if (symptomGroup == null) {
            return new KnowledgeContext();
        }

        List<String> sourceAssets = new ArrayList<>();
        SymptomGroupRule symptomGroupRule = staticRuleProvider.loadSymptomGroupRules(symptomGroup);
        List<RedFlagRule> redFlags = staticRuleProvider.loadRedFlagRules(symptomGroup);

        if (symptomGroupRule == null) {
            return new KnowledgeContext(symptomGroup, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }

        sourceAssets.add("assets/symptom-groups/" + toAssetFileName(symptomGroup));
        if (!redFlags.isEmpty()) {
            sourceAssets.add("assets/red-flag-rules.yml");
        }

        return new KnowledgeContext(
                symptomGroup,
                symptomGroupRule.commonDiagnoses(),
                symptomGroupRule.mustNotMiss(),
                redFlags,
                symptomGroupRule.requiredQuestions(),
                symptomGroupRule.recommendedTests(),
                sourceAssets);
    }

    private String toAssetFileName(String symptomGroup) {
        return switch (symptomGroup) {
            case "chest_pain" -> "chest-pain.yml";
            case "fever" -> "fever.yml";
            default -> symptomGroup + ".yml";
        };
    }
}
