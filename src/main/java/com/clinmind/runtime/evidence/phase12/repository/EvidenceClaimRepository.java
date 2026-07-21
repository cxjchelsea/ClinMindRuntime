package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.EvidenceClaim;
import java.util.List;
import java.util.Optional;

public interface EvidenceClaimRepository {
    void save(EvidenceClaim claim);

    Optional<EvidenceClaim> findByClaimId(String claimId);

    List<EvidenceClaim> findByVersionId(String versionId);

    List<EvidenceClaim> findAll();
}