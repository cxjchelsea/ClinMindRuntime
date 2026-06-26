package com.clinmind.runtime.provider;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.EvidenceAssetRef;
import com.clinmind.runtime.state.DDxCandidate;
import java.util.List;

public interface EvidenceAssetProvider {

    List<EvidenceAssetRef> retrieveEvidenceRefs(
            String symptomGroup,
            List<DDxCandidate> candidates,
            AssetQueryContext context);
}
