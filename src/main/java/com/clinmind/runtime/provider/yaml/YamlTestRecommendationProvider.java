package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.TestRecommendationAsset;
import com.clinmind.runtime.provider.TestRecommendationProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class YamlTestRecommendationProvider implements TestRecommendationProvider {

    private final YamlAssetPackageRepository repository;

    public YamlTestRecommendationProvider(YamlAssetPackageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TestRecommendationAsset> loadTestRecommendations(String symptomGroup, AssetQueryContext context) {
        String packageId = YamlProviderSupport.resolvePackageId(repository, context);
        AssetPackageManifest manifest = repository.loadRuntimeManifest(packageId);
        Map<String, Object> root = repository.loadResource(packageId, "test-recommendation-rules.yml").content();
        List<Map<String, Object>> rules = YamlAssetParsingSupport.mapList(root.get("test_recommendation_rules"));

        List<TestRecommendationAsset> result = new ArrayList<>();
        for (Map<String, Object> item : rules) {
            String group = YamlAssetParsingSupport.stringValue(item.get("symptom_group"), null);
            if (symptomGroup != null && !symptomGroup.equals(group)) {
                continue;
            }
            String ruleId = YamlAssetParsingSupport.stringValue(item.get("rule_id"), null);
            AssetMetadata metadata = YamlAssetParsingSupport.resolveMetadata(
                    item,
                    manifest,
                    AssetType.TEST_RECOMMENDATION,
                    "asset_test_rec_" + ruleId,
                    group,
                    false);
            result.add(new TestRecommendationAsset(
                    metadata,
                    ruleId,
                    group,
                    YamlAssetParsingSupport.parseCandidateStatus(item.get("target_status")),
                    YamlAssetParsingSupport.stringList(item.get("recommended_tests")),
                    YamlAssetParsingSupport.stringValue(item.get("purpose"), null),
                    YamlAssetParsingSupport.booleanValue(item.get("patient_visible_default"), false)));
        }
        return List.copyOf(result);
    }
}
