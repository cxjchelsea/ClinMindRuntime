package com.clinmind.runtime.experience;

import com.clinmind.runtime.asset.AssetRuntimeSupport;
import com.clinmind.runtime.asset.ExperienceUnitAsset;
import com.clinmind.runtime.provider.ClinicalExperienceProvider;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ExperienceContext;
import com.clinmind.runtime.state.ExperienceUnit;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExperienceContextService {

    private final ClinicalExperienceProvider clinicalExperienceProvider;

    public ExperienceContextService(ClinicalExperienceProvider clinicalExperienceProvider) {
        this.clinicalExperienceProvider = clinicalExperienceProvider;
    }

    @TraceStep("ExperienceContext")
    public ExperienceContext buildExperienceContext(CaseFrame caseFrame, KnowledgeContext knowledgeContext) {
        return buildExperienceContext(caseFrame, knowledgeContext, null);
    }

    @TraceStep("ExperienceContext")
    public ExperienceContext buildExperienceContext(
            CaseFrame caseFrame,
            KnowledgeContext knowledgeContext,
            RuntimeState state) {
        if (knowledgeContext == null || knowledgeContext.symptomGroup() == null) {
            return new ExperienceContext();
        }

        List<ExperienceUnitAsset> assets = clinicalExperienceProvider.retrieveExperienceUnits(
                caseFrame,
                knowledgeContext,
                state == null
                        ? com.clinmind.runtime.asset.AssetQueryContext.defaults(knowledgeContext.symptomGroup())
                        : AssetRuntimeSupport.queryContext(state, knowledgeContext.symptomGroup()));

        List<ExperienceUnit> units = new ArrayList<>();
        List<String> alerts = new ArrayList<>();
        for (ExperienceUnitAsset asset : assets) {
            AssetRuntimeSupport.recordAssetUsed(state, asset.metadata(), "ExperienceContext");
            units.add(new ExperienceUnit(
                    asset.experienceId(),
                    asset.summary(),
                    asset.summary()));
            alerts.addAll(asset.suggestedCautions());
        }

        if (units.isEmpty()) {
            return new ExperienceContext();
        }
        return new ExperienceContext(List.copyOf(units), List.copyOf(alerts), "provider");
    }
}
