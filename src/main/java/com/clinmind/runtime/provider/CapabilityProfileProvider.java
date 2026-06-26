package com.clinmind.runtime.provider;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.CapabilityProfileAsset;

public interface CapabilityProfileProvider {

    CapabilityProfileAsset loadCapabilityProfile(String symptomGroup, AssetQueryContext context);
}
