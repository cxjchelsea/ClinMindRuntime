package com.clinmind.runtime.provider;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.ExperienceUnitAsset;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.KnowledgeContext;
import java.util.List;

public interface ClinicalExperienceProvider {

    List<ExperienceUnitAsset> retrieveExperienceUnits(
            CaseFrame caseFrame,
            KnowledgeContext knowledgeContext,
            AssetQueryContext context);
}
