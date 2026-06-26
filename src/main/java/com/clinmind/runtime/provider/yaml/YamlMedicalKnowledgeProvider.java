package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetLoadErrorCode;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;
import com.clinmind.runtime.provider.MedicalKnowledgeProvider;
import org.springframework.stereotype.Component;

@Component
public class YamlMedicalKnowledgeProvider implements MedicalKnowledgeProvider {

    private final YamlAssetPackageRepository repository;

    public YamlMedicalKnowledgeProvider(YamlAssetPackageRepository repository) {
        this.repository = repository;
    }

    @Override
    public MedicalKnowledgeAsset loadMedicalKnowledge(String symptomGroup, AssetQueryContext context) {
        String packageId = YamlProviderSupport.resolvePackageId(repository, context);
        AssetPackageManifest manifest = repository.loadRuntimeManifest(packageId);
        String relativePath = YamlAssetParsingSupport.resolveSymptomGroupResourcePath(symptomGroup);
        if (relativePath == null) {
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_NOT_FOUND,
                    "Unsupported symptom group: " + symptomGroup,
                    true,
                    packageId,
                    symptomGroup);
        }

        var root = repository.loadResource(packageId, relativePath).content();
        AssetMetadata metadata = YamlAssetParsingSupport.resolveMetadata(
                root,
                manifest,
                AssetType.SYMPTOM_GROUP,
                "asset_symptom_" + symptomGroup + "_v1",
                symptomGroup,
                true);

        return new MedicalKnowledgeAsset(
                metadata,
                YamlAssetParsingSupport.stringValue(root.get("symptom_group"), symptomGroup),
                YamlAssetParsingSupport.parseDiagnosisRefs(root.get("common_diagnoses")),
                YamlAssetParsingSupport.parseDiagnosisRefs(root.get("must_not_miss")),
                YamlAssetParsingSupport.stringList(root.get("required_questions")),
                YamlAssetParsingSupport.stringList(root.get("recommended_tests")),
                YamlAssetParsingSupport.stringList(root.get("clinical_pathway_refs")),
                YamlAssetParsingSupport.stringList(root.get("evidence_refs")));
    }
}
