package com.clinmind.runtime.provider;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;

public interface MedicalKnowledgeProvider {

    MedicalKnowledgeAsset loadMedicalKnowledge(String symptomGroup, AssetQueryContext context);
}
