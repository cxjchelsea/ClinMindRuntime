package com.clinmind.runtime.provider;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.TestRecommendationAsset;
import java.util.List;

public interface TestRecommendationProvider {

    List<TestRecommendationAsset> loadTestRecommendations(String symptomGroup, AssetQueryContext context);
}
