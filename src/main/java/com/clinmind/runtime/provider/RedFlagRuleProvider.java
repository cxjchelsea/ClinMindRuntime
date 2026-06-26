package com.clinmind.runtime.provider;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.RedFlagRuleAsset;
import java.util.List;

public interface RedFlagRuleProvider {

    List<RedFlagRuleAsset> loadRedFlagRules(String symptomGroup, AssetQueryContext context);
}
