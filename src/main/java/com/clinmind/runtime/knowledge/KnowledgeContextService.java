package com.clinmind.runtime.knowledge;

import com.clinmind.runtime.asset.AssetRuntimeSupport;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;
import com.clinmind.runtime.asset.RedFlagRuleAsset;
import com.clinmind.runtime.asset.TestRecommendationAsset;
import com.clinmind.runtime.provider.MedicalKnowledgeProvider;
import com.clinmind.runtime.provider.RedFlagRuleProvider;
import com.clinmind.runtime.provider.TestRecommendationProvider;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RedFlagRule;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeContextService {

    private final MedicalKnowledgeProvider medicalKnowledgeProvider;
    private final RedFlagRuleProvider redFlagRuleProvider;
    private final TestRecommendationProvider testRecommendationProvider;

    public KnowledgeContextService(
            MedicalKnowledgeProvider medicalKnowledgeProvider,
            RedFlagRuleProvider redFlagRuleProvider,
            TestRecommendationProvider testRecommendationProvider) {
        this.medicalKnowledgeProvider = medicalKnowledgeProvider;
        this.redFlagRuleProvider = redFlagRuleProvider;
        this.testRecommendationProvider = testRecommendationProvider;
    }

    @TraceStep("KnowledgeContext")
    public KnowledgeContext buildKnowledgeContext(CaseFrame caseFrame, EntryAssessmentResult entryAssessment) {
        return buildKnowledgeContext(caseFrame, entryAssessment, null);
    }

    @TraceStep("KnowledgeContext")
    public KnowledgeContext buildKnowledgeContext(
            CaseFrame caseFrame,
            EntryAssessmentResult entryAssessment,
            RuntimeState state) {
        if (entryAssessment == null || entryAssessment.workMode() == WorkMode.UNSUPPORTED) {
            return new KnowledgeContext();
        }

        String symptomGroup = entryAssessment.symptomGroup();
        if (symptomGroup == null) {
            return new KnowledgeContext();
        }

        AssetQueryContext context = state == null
                ? AssetQueryContext.defaults(symptomGroup)
                : AssetRuntimeSupport.queryContext(state, symptomGroup);

        MedicalKnowledgeAsset medicalKnowledge = medicalKnowledgeProvider.loadMedicalKnowledge(
                symptomGroup, context);
        List<RedFlagRuleAsset> redFlagAssets = redFlagRuleProvider.loadRedFlagRules(symptomGroup, context);
        List<TestRecommendationAsset> testAssets = testRecommendationProvider.loadTestRecommendations(
                symptomGroup, context);

        List<String> sourceAssets = new ArrayList<>();
        sourceAssets.add(medicalKnowledge.metadata().assetRef());
        AssetRuntimeSupport.recordAssetUsed(state, medicalKnowledge.metadata(), "KnowledgeContext");

        List<RedFlagRule> redFlags = new ArrayList<>();
        for (RedFlagRuleAsset asset : redFlagAssets) {
            redFlags.add(AssetRuntimeSupport.toRedFlagRule(asset));
            sourceAssets.add(asset.metadata().assetRef());
            AssetRuntimeSupport.recordAssetUsed(state, asset.metadata(), "KnowledgeContext");
        }

        for (TestRecommendationAsset asset : testAssets) {
            sourceAssets.add(asset.metadata().assetRef());
            AssetRuntimeSupport.recordAssetUsed(state, asset.metadata(), "KnowledgeContext");
        }

        return new KnowledgeContext(
                symptomGroup,
                medicalKnowledge.commonDiagnoses(),
                medicalKnowledge.mustNotMiss(),
                List.copyOf(redFlags),
                medicalKnowledge.requiredQuestions(),
                medicalKnowledge.recommendedTests(),
                List.copyOf(sourceAssets));
    }
}
