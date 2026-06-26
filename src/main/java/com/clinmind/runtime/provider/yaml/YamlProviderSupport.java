package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetQueryContext;

final class YamlProviderSupport {

    private YamlProviderSupport() {
    }

    static String resolvePackageId(YamlAssetPackageRepository repository, AssetQueryContext context) {
        if (context != null && context.packageId() != null && !context.packageId().isBlank()) {
            return context.packageId();
        }
        return repository.getDefaultPackageId();
    }
}
