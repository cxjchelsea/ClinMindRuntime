package com.clinmind.runtime.asset;

import com.clinmind.runtime.knowledge.CapabilityProfile;
import com.clinmind.runtime.state.RedFlagRule;
import com.clinmind.runtime.state.RuntimeState;
import java.time.Instant;

public final class AssetRuntimeSupport {

    private AssetRuntimeSupport() {
    }

    public static AssetQueryContext queryContext(RuntimeState state) {
        if (state == null) {
            return AssetQueryContext.defaults(null);
        }
        String packageId = state.getAssetPackageId();
        if (packageId == null || packageId.isBlank()) {
            packageId = "phase2-default";
        }
        String symptomGroup = state.getEntryAssessment() == null
                ? null
                : state.getEntryAssessment().symptomGroup();
        return new AssetQueryContext(
                packageId,
                state.getAssetPackageVersion(),
                symptomGroup,
                state.getRuntimeId(),
                state.getMode(),
                false);
    }

    public static AssetQueryContext queryContext(RuntimeState state, String symptomGroup) {
        AssetQueryContext base = queryContext(state);
        return new AssetQueryContext(
                base.packageId(),
                base.version(),
                symptomGroup,
                base.runtimeId(),
                base.runtimeMode(),
                base.fallbackAllowed());
    }

    public static RedFlagRule toRedFlagRule(RedFlagRuleAsset asset) {
        return new RedFlagRule(
                asset.ruleId(),
                asset.symptomGroup(),
                asset.features(),
                asset.riskLevel(),
                asset.action(),
                asset.patientConstraint());
    }

    public static CapabilityProfile toCapabilityProfile(CapabilityProfileAsset asset) {
        return new CapabilityProfile(
                asset.symptomGroup(),
                asset.level(),
                asset.patientAllowedOutputs(),
                asset.clinicianAllowedOutputs());
    }

    public static void recordAssetUsed(
            RuntimeState state,
            AssetMetadata metadata,
            String moduleName) {
        if (state == null || metadata == null) {
            return;
        }
        state.getAssetsUsed().add(new AssetUsedRecord(
                state.getRuntimeId(),
                null,
                metadata.packageId(),
                metadata.assetId(),
                metadata.assetType(),
                metadata.version(),
                metadata.symptomGroup(),
                moduleName,
                Instant.now()));
    }
}
