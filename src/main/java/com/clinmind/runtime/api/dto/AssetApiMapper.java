package com.clinmind.runtime.api.dto;

import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetUsedRecord;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;
import com.clinmind.runtime.state.RuntimeState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssetApiMapper {

    private AssetApiMapper() {
    }

    public static Map<String, Object> toPackageSummary(AssetPackageManifest manifest) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("package_id", manifest.packageId());
        item.put("version", manifest.version());
        item.put("status", manifest.status() == null ? null : manifest.status().getValue());
        item.put("display_name", manifest.displayName());
        item.put("supported_symptom_groups", manifest.supportedSymptomGroups());
        item.put("default_package", manifest.defaultPackage());
        return item;
    }

    public static Map<String, Object> toPackageDetail(AssetPackageManifest manifest) {
        Map<String, Object> detail = toPackageSummary(manifest);
        detail.put("description", manifest.description());
        detail.put("created_at", manifest.createdAt());
        detail.put("updated_at", manifest.updatedAt());
        detail.put("source", manifest.source());
        detail.put("owner", manifest.owner());
        return detail;
    }

    public static Map<String, Object> toSymptomGroupSummary(MedicalKnowledgeAsset asset) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("package_id", asset.metadata().packageId());
        summary.put("asset_id", asset.metadata().assetId());
        summary.put("asset_ref", asset.metadata().assetRef());
        summary.put("asset_type", asset.metadata().assetType().getValue());
        summary.put("version", asset.metadata().version());
        summary.put("symptom_group", asset.symptomGroup());
        summary.put("common_diagnosis_count", asset.commonDiagnoses().size());
        summary.put("must_not_miss_count", asset.mustNotMiss().size());
        summary.put("required_question_count", asset.requiredQuestions().size());
        summary.put("recommended_test_count", asset.recommendedTests().size());
        summary.put("source_asset_ids", List.of(asset.metadata().assetRef()));
        return summary;
    }

    public static Map<String, Object> toAssetsUsedResponse(RuntimeState state) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runtime_id", state.getRuntimeId());
        response.put("package_id", state.getAssetPackageId());
        response.put("package_version", state.getAssetPackageVersion());
        response.put("assets", state.getAssetsUsed() == null
                ? List.of()
                : state.getAssetsUsed().stream().map(AssetApiMapper::toAssetUsedItem).toList());
        return response;
    }

    private static Map<String, Object> toAssetUsedItem(AssetUsedRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("asset_id", record.assetId());
        item.put("asset_type", record.assetType() == null ? null : record.assetType().getValue());
        item.put("version", record.version());
        item.put("module_name", record.moduleName());
        item.put("package_id", record.packageId());
        item.put("symptom_group", record.symptomGroup());
        item.put("asset_ref", record.assetRef());
        return item;
    }
}
