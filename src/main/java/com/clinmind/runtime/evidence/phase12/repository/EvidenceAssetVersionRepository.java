package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import java.util.List;
import java.util.Optional;

public interface EvidenceAssetVersionRepository {
    void save(EvidenceAssetVersion assetVersion);

    Optional<EvidenceAssetVersion> findByVersionId(String versionId);

    List<EvidenceAssetVersion> findAll();

    List<EvidenceAssetVersion> findBySourceId(String sourceId);
}
