package com.clinmind.runtime.asset;

import java.util.List;

public interface AssetPackageRepository {

    AssetPackageManifest loadManifest(String packageId);

    List<AssetPackageManifest> listPackages();

    AssetResource loadResource(String packageId, String relativePath);
}
