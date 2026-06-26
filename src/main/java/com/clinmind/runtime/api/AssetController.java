package com.clinmind.runtime.api;

import com.clinmind.runtime.api.dto.AssetApiMapper;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;
import com.clinmind.runtime.provider.MedicalKnowledgeProvider;
import com.clinmind.runtime.provider.yaml.YamlAssetPackageRepository;
import com.clinmind.runtime.state.RuntimeMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final YamlAssetPackageRepository assetPackageRepository;
    private final MedicalKnowledgeProvider medicalKnowledgeProvider;

    public AssetController(
            YamlAssetPackageRepository assetPackageRepository,
            MedicalKnowledgeProvider medicalKnowledgeProvider) {
        this.assetPackageRepository = assetPackageRepository;
        this.medicalKnowledgeProvider = medicalKnowledgeProvider;
    }

    @GetMapping("/packages")
    public ApiResponse<?> listPackages() {
        List<Map<String, Object>> packages = assetPackageRepository.listPackages().stream()
                .map(AssetApiMapper::toPackageSummary)
                .toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("packages", packages);
        return ApiResponse.ok(data);
    }

    @GetMapping("/packages/{package_id}")
    public ApiResponse<?> getPackage(@PathVariable("package_id") String packageId) {
        AssetPackageManifest manifest = loadManifestOrNotFound(packageId);
        return ApiResponse.ok(AssetApiMapper.toPackageDetail(manifest));
    }

    @GetMapping("/packages/{package_id}/symptom-groups/{symptom_group}")
    public ApiResponse<?> getSymptomGroupAsset(
            @PathVariable("package_id") String packageId,
            @PathVariable("symptom_group") String symptomGroup) {
        loadManifestOrNotFound(packageId);
        AssetQueryContext context = new AssetQueryContext(
                packageId, null, symptomGroup, null, RuntimeMode.DEBUG, false);
        try {
            MedicalKnowledgeAsset asset = medicalKnowledgeProvider.loadMedicalKnowledge(symptomGroup, context);
            return ApiResponse.ok(AssetApiMapper.toSymptomGroupSummary(asset));
        } catch (AssetLoadException error) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "ASSET_NOT_FOUND",
                    error.getMessage());
        }
    }

    private AssetPackageManifest loadManifestOrNotFound(String packageId) {
        try {
            return assetPackageRepository.loadManifest(packageId);
        } catch (AssetLoadException error) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "PACKAGE_NOT_FOUND",
                    "Asset package not found: " + packageId);
        }
    }
}
