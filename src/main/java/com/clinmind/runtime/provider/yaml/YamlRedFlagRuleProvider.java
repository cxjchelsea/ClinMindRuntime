package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.RedFlagRuleAsset;
import com.clinmind.runtime.provider.RedFlagRuleProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class YamlRedFlagRuleProvider implements RedFlagRuleProvider {

    private final YamlAssetPackageRepository repository;

    public YamlRedFlagRuleProvider(YamlAssetPackageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RedFlagRuleAsset> loadRedFlagRules(String symptomGroup, AssetQueryContext context) {
        String packageId = YamlProviderSupport.resolvePackageId(repository, context);
        AssetPackageManifest manifest = repository.loadRuntimeManifest(packageId);
        Map<String, Object> root = repository.loadResource(packageId, "red-flag-rules.yml").content();
        List<Map<String, Object>> rules = YamlAssetParsingSupport.mapList(root.get("red_flag_rules"));
        if (rules.isEmpty()) {
            throw new com.clinmind.runtime.asset.AssetLoadException(
                    com.clinmind.runtime.asset.AssetLoadErrorCode.ASSET_NOT_FOUND,
                    "No red flag rules found in package",
                    true,
                    packageId,
                    "red-flag-rules.yml");
        }

        List<RedFlagRuleAsset> result = new ArrayList<>();
        for (Map<String, Object> item : rules) {
            String group = YamlAssetParsingSupport.stringValue(item.get("symptom_group"), null);
            if (symptomGroup != null && !symptomGroup.equals(group)) {
                continue;
            }
            String ruleId = YamlAssetParsingSupport.stringValue(item.get("rule_id"), null);
            AssetMetadata metadata = YamlAssetParsingSupport.resolveMetadata(
                    item,
                    manifest,
                    AssetType.RED_FLAG_RULE,
                    "asset_red_flag_" + ruleId,
                    group,
                    true);
            result.add(new RedFlagRuleAsset(
                    metadata,
                    ruleId,
                    group,
                    YamlAssetParsingSupport.stringList(item.get("features")),
                    YamlAssetParsingSupport.parseRiskLevel(item.get("risk_level")),
                    YamlAssetParsingSupport.stringValue(item.get("action"), null),
                    YamlAssetParsingSupport.stringValue(item.get("patient_constraint"), null)));
        }
        return List.copyOf(result);
    }
}
