package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.ClaimEvidenceLink;
import java.util.List;
import java.util.Optional;

public interface ClaimEvidenceLinkRepository {
    void save(ClaimEvidenceLink link);

    Optional<ClaimEvidenceLink> findByLinkId(String linkId);

    List<ClaimEvidenceLink> findByClaimId(String claimId);

    List<ClaimEvidenceLink> findBySpanId(String spanId);
}