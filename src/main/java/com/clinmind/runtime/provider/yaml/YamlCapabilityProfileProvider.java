package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetLoadErrorCode;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.CapabilityProfileAsset;
import com.clinmind.runtime.provider.CapabilityProfileProvider;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class YamlCapabilityProfileProvider implements CapabilityProfileProvider {

    private final YamlAssetPackageRepository repository;

    public YamlCapabilityProfileProvider(YamlAssetPackageRepository repository) {
        this.repository = repository;
    }

    @Override
    public CapabilityProfileAsset loadCapabilityProfile(String symptomGroup, AssetQueryContext context) {
        String packageId = YamlProviderSupport.resolvePackageId(repository, context);
        AssetPackageManifest manifest = repository.loadRuntimeManifest(packageId);
        Map<String, Object> root = repository.loadResource(packageId, "capability-profiles.yml").content();
        List<Map<String, Object>> profiles = YamlAssetParsingSupport.mapList(root.get("capability_profiles"));

        for (Map<String, Object> item : profiles) {
            String group = YamlAssetParsingSupport.stringValue(item.get("symptom_group"), null);
            if (symptomGroup != null && symptomGroup.equals(group)) {
                AssetMetadata metadata = YamlAssetParsingSupport.resolveMetadata(
                        item,
                        manifest,
                        AssetType.CAPABILITY_PROFILE,
                        "asset_capability_" + group + "_v1",
                        group,
                        true);
                return new CapabilityProfileAsset(
                        metadata,
                        group,
                        YamlAssetParsingSupport.stringValue(item.get("level"), null),
                        YamlAssetParsingSupport.stringList(item.get("patient_allowed_outputs")),
                        YamlAssetParsingSupport.stringList(item.get("clinician_allowed_outputs")),
                        YamlAssetParsingSupport.stringList(item.get("constraints")));
            }
        }

        throw new AssetLoadException(
                AssetLoadErrorCode.ASSET_NOT_FOUND,
                "Capability profile not found for symptom group: " + symptomGroup,
                true,
                packageId,
                "capability-profiles.yml");
    }
}
