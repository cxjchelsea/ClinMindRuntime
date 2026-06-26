package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.ExperienceUnitAsset;
import com.clinmind.runtime.asset.ReviewStatus;
import com.clinmind.runtime.provider.ClinicalExperienceProvider;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.KnowledgeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class YamlClinicalExperienceProvider implements ClinicalExperienceProvider {

    private final YamlAssetPackageRepository repository;

    public YamlClinicalExperienceProvider(YamlAssetPackageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ExperienceUnitAsset> retrieveExperienceUnits(
            CaseFrame caseFrame,
            KnowledgeContext knowledgeContext,
            AssetQueryContext context) {
        String packageId = YamlProviderSupport.resolvePackageId(repository, context);
        AssetPackageManifest manifest = repository.loadRuntimeManifest(packageId);
        Map<String, Object> root = repository.loadResource(packageId, "experience-units.yml").content();
        List<Map<String, Object>> units = YamlAssetParsingSupport.mapList(root.get("experience_units"));

        String symptomGroup = knowledgeContext == null ? null : knowledgeContext.symptomGroup();
        List<ExperienceUnitAsset> result = new ArrayList<>();
        for (Map<String, Object> item : units) {
            String group = YamlAssetParsingSupport.stringValue(item.get("symptom_group"), null);
            if (symptomGroup != null && !symptomGroup.equals(group)) {
                continue;
            }
            ReviewStatus reviewStatus = YamlAssetParsingSupport.parseReviewStatus(
                    item.get("review_status"), ReviewStatus.UNREVIEWED);
            if (!reviewStatus.isExperienceUsable()) {
                continue;
            }
            String experienceId = YamlAssetParsingSupport.stringValue(item.get("experience_id"), null);
            AssetMetadata metadata = YamlAssetParsingSupport.resolveMetadata(
                    item,
                    manifest,
                    AssetType.EXPERIENCE_UNIT,
                    "asset_experience_" + experienceId,
                    group,
                    false);
            result.add(new ExperienceUnitAsset(
                    metadata,
                    experienceId,
                    group,
                    YamlAssetParsingSupport.stringList(item.get("trigger_features")),
                    YamlAssetParsingSupport.stringValue(item.get("summary"), null),
                    YamlAssetParsingSupport.stringList(item.get("suggested_questions")),
                    YamlAssetParsingSupport.stringList(item.get("suggested_cautions")),
                    YamlAssetParsingSupport.stringList(item.get("affected_modules")),
                    parseConfidence(item.get("confidence"))));
        }
        return List.copyOf(result);
    }

    private double parseConfidence(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
