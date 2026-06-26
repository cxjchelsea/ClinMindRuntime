package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.EvidenceAssetRef;
import com.clinmind.runtime.provider.EvidenceAssetProvider;
import com.clinmind.runtime.state.DDxCandidate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StaticEvidenceAssetProvider implements EvidenceAssetProvider {

    private final YamlAssetPackageRepository repository;

    public StaticEvidenceAssetProvider(YamlAssetPackageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EvidenceAssetRef> retrieveEvidenceRefs(
            String symptomGroup,
            List<DDxCandidate> candidates,
            AssetQueryContext context) {
        String packageId = YamlProviderSupport.resolvePackageId(repository, context);
        AssetPackageManifest manifest = repository.loadRuntimeManifest(packageId);
        Map<String, Object> root;
        try {
            root = repository.loadResource(packageId, "evidence-refs.yml").content();
        } catch (com.clinmind.runtime.asset.AssetLoadException error) {
            if (!error.isRiskCritical()) {
                return List.of();
            }
            throw error;
        }

        List<Map<String, Object>> refs = YamlAssetParsingSupport.mapList(root.get("evidence_refs"));
        List<EvidenceAssetRef> result = new ArrayList<>();
        for (Map<String, Object> item : refs) {
            String group = YamlAssetParsingSupport.stringValue(item.get("symptom_group"), null);
            if (symptomGroup != null && group != null && !symptomGroup.equals(group)) {
                continue;
            }
            String refId = YamlAssetParsingSupport.stringValue(item.get("ref_id"), null);
            AssetMetadata metadata = YamlAssetParsingSupport.resolveMetadata(
                    item,
                    manifest,
                    AssetType.EVIDENCE_REF,
                    "asset_evidence_" + refId,
                    group,
                    false);
            result.add(new EvidenceAssetRef(
                    metadata,
                    refId,
                    group,
                    YamlAssetParsingSupport.stringValue(item.get("title"), null),
                    YamlAssetParsingSupport.stringValue(item.get("source_type"), null),
                    YamlAssetParsingSupport.stringValue(item.get("source_uri"), null),
                    YamlAssetParsingSupport.stringValue(item.get("summary"), null),
                    YamlAssetParsingSupport.stringList(item.get("linked_diagnoses"))));
        }
        return List.copyOf(result);
    }
}
