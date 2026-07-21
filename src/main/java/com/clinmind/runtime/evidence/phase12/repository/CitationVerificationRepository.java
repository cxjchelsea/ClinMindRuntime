package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.CitationVerificationResult;
import java.util.List;
import java.util.Optional;

public interface CitationVerificationRepository {
    Optional<CitationVerificationResult> findByVerificationId(String verificationId);

    List<CitationVerificationResult> findByClaimId(String claimId);
}
