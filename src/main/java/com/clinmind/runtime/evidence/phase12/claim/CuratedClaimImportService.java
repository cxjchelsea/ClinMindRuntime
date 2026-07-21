package com.clinmind.runtime.evidence.phase12.claim;

public interface CuratedClaimImportService {
    CuratedClaimImportResult importClaims(CuratedClaimImportCommand command);
}