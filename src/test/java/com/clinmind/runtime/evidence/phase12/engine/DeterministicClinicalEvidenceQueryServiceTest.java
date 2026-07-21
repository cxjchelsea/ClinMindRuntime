package com.clinmind.runtime.evidence.phase12.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.ClinicalEvidenceRetrievalResult;
import com.clinmind.runtime.evidence.phase12.ClinicalEvidenceRetrievalStatus;
import com.clinmind.runtime.evidence.phase12.ClinicalQuestionType;
import com.clinmind.runtime.evidence.phase12.EvidenceApplicabilityContext;
import com.clinmind.runtime.evidence.phase12.EvidenceItem;
import com.clinmind.runtime.evidence.phase12.EvidenceQueryRequest;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import com.clinmind.runtime.evidence.phase12.EvidenceValidationDecision;
import com.clinmind.runtime.evidence.phase12.source.YamlPhase12SourceManifestRepository;
import org.junit.jupiter.api.Test;

class DeterministicClinicalEvidenceQueryServiceTest {

    private final DeterministicClinicalEvidenceQueryService service =
            new DeterministicClinicalEvidenceQueryService(new YamlPhase12SourceManifestRepository());

    @Test
    void retrievesSourceBackedChestPainEvidenceInEvaluationScope() {
        ClinicalEvidenceRetrievalResult result = service.retrieve(new EvidenceQueryRequest(
                "req_phase12_eval_001",
                "rt_test",
                "chest discomfort with exertion and sweating high risk",
                ClinicalQuestionType.RISK_ASSESSMENT,
                "chest_pain",
                adultHomeContext(),
                EvidenceRetrievalScope.EVALUATION,
                3,
                null));

        assertThat(result.status()).isEqualTo(ClinicalEvidenceRetrievalStatus.COMPLETE);
        assertThat(result.evidenceBundle().acceptedItems()).isNotEmpty();
        EvidenceItem first = result.evidenceBundle().acceptedItems().get(0);
        assertThat(first.claim().claimId()).isEqualTo("claim_chest_pain_exertion_sweating_high_risk");
        assertThat(first.sourceRef().sourceId()).startsWith("src_phase12_curated_");
        assertThat(first.sourceRef().assetId()).isEqualTo("asset_phase12_chest_pain_safety_brief");
        assertThat(first.citationVerification().supportStatus().name()).isEqualTo("SUPPORTS");
        assertThat(first.validationDecision()).isEqualTo(EvidenceValidationDecision.ACCEPTED);
        assertThat(first.safeSummary()).doesNotContain("This project-authored brief is for development");
        assertThat(result.trace().providerId()).isEqualTo(DeterministicClinicalEvidenceQueryService.PROVIDER_ID);
        assertThat(result.trace().eligibleVersionIds()).isNotEmpty();
        assertThat(result.warnings()).contains("phase12_p0_evaluation_scope_only");
    }

    @Test
    void productionScopeDoesNotPromoteDraftClaimedSeedAssets() {
        ClinicalEvidenceRetrievalResult result = service.retrieve(new EvidenceQueryRequest(
                "req_phase12_prod_001",
                "rt_test",
                "chest pain exertion sweating",
                ClinicalQuestionType.RISK_ASSESSMENT,
                "chest_pain",
                adultHomeContext(),
                EvidenceRetrievalScope.PRODUCTION,
                3,
                null));

        assertThat(result.status()).isEqualTo(ClinicalEvidenceRetrievalStatus.UNAVAILABLE);
        assertThat(result.errorCode()).isEqualTo("NO_ELIGIBLE_PHASE12_EVIDENCE_ASSET");
        assertThat(result.evidenceBundle().acceptedItems()).isEmpty();
        assertThat(result.trace().eligibleVersionIds()).isEmpty();
    }

    @Test
    void outOfScopeQuestionReturnsEmptyWithoutInventedCitation() {
        ClinicalEvidenceRetrievalResult result = service.retrieve(new EvidenceQueryRequest(
                "req_phase12_empty_001",
                "rt_test",
                "tooth pain dental abscess antibiotic prescription",
                ClinicalQuestionType.NO_ANSWER,
                "dental_pain",
                adultHomeContext(),
                EvidenceRetrievalScope.EVALUATION,
                3,
                null));

        assertThat(result.status()).isEqualTo(ClinicalEvidenceRetrievalStatus.EMPTY);
        assertThat(result.evidenceBundle().acceptedItems()).isEmpty();
        assertThat(result.trace().matchedClaimIds()).isEmpty();
        assertThat(result.trace().rejectedClaimIds()).contains("claim_chest_pain_exertion_sweating_high_risk");
    }

    @Test
    void applicabilityMismatchIsReviewRequiredRatherThanAccepted() {
        ClinicalEvidenceRetrievalResult result = service.retrieve(new EvidenceQueryRequest(
                "req_phase12_applicability_001",
                "rt_test",
                "applicability mismatch for age and jurisdiction",
                ClinicalQuestionType.APPLICABILITY,
                "chest_pain",
                adultHomeContext(),
                EvidenceRetrievalScope.EVALUATION,
                3,
                null));

        assertThat(result.status()).isEqualTo(ClinicalEvidenceRetrievalStatus.REVIEW_REQUIRED);
        assertThat(result.evidenceBundle().acceptedItems()).isEmpty();
        assertThat(result.evidenceBundle().reviewRequiredItems())
                .extracting(item -> item.claim().claimId())
                .contains("claim_applicability_review_required");
    }

    private EvidenceApplicabilityContext adultHomeContext() {
        return new EvidenceApplicabilityContext(
                "ADULT_40_64",
                "UNKNOWN",
                "OUTPATIENT_OR_HOME",
                "GLOBAL",
                "CLINICIAN",
                null);
    }
}
