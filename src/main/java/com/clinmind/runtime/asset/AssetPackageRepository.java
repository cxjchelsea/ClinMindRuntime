package com.clinmind.runtime.asset;

import java.util.List;

public interface AssetPackageRepository {

    String getDefaultPackageId();

    AssetPackageManifest loadManifest(String packageId);

    AssetPackageManifest loadRuntimeManifest(String packageId);

    List<AssetPackageManifest> listPackages();

    AssetResource loadResource(String packageId, String relativePath);
}
