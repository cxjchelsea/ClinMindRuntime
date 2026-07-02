package com.clinmind.runtime.evidence.validation;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import com.clinmind.runtime.evidence.EvidenceValidationResult;
import com.clinmind.runtime.agent.ProposalValidationStatus;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class EvidenceValidationService {

    private static final Set<EvidenceUseCase> ALLOWED_USE_CASES = Set.of(
            EvidenceUseCase.SUPPORT,
            EvidenceUseCase.ASK_MORE,
            EvidenceUseCase.SAFETY_WARNING);

    public EvidenceValidationResult validate(
            List<EvidenceCandidate> candidates, EvidenceRetrievalRequest request) {
        if (candidates == null || candidates.isEmpty()) {
            return new EvidenceValidationResult(
                    ProposalValidationStatus.REJECTED,
                    List.of(),
                    List.of(),
                    List.of("no evidence candidates"));
        }

        List<String> acceptedIds = new ArrayList<>();
        List<String> rejectedIds = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (EvidenceCandidate candidate : candidates) {
            List<String> candidateReasons = validateCandidate(candidate, request);
            if (candidateReasons.isEmpty()) {
                acceptedIds.add(candidate.candidateId());
            } else {
                rejectedIds.add(candidate.candidateId());
                reasons.addAll(candidateReasons);
            }
        }

        if (acceptedIds.isEmpty()) {
            return new EvidenceValidationResult(
                    ProposalValidationStatus.REJECTED, List.of(), rejectedIds, reasons);
        }

        if (acceptedIds.size() > EvidenceConstants.MAX_ACCEPTED_CANDIDATES) {
            List<String> trimmedAccepted = acceptedIds.subList(0, EvidenceConstants.MAX_ACCEPTED_CANDIDATES);
            rejectedIds.addAll(acceptedIds.subList(EvidenceConstants.MAX_ACCEPTED_CANDIDATES, acceptedIds.size()));
            reasons.add("accepted candidate count trimmed to max limit");
            acceptedIds = trimmedAccepted;
        }

        if (!rejectedIds.isEmpty()) {
            return new EvidenceValidationResult(
                    ProposalValidationStatus.PARTIALLY_ACCEPTED, acceptedIds, rejectedIds, reasons);
        }
        return new EvidenceValidationResult(
                ProposalValidationStatus.ACCEPTED, acceptedIds, List.of(), List.of());
    }

    private List<String> validateCandidate(EvidenceCandidate candidate, EvidenceRetrievalRequest request) {
        List<String> reasons = new ArrayList<>();
        if (candidate == null) {
            reasons.add("candidate missing");
            return reasons;
        }
        if (candidate.candidateId() == null || candidate.candidateId().isBlank()) {
            reasons.add("candidate_id missing");
        }
        EvidenceRef ref = candidate.evidenceRef();
        if (ref == null) {
            reasons.add("evidence_ref missing");
            return reasons;
        }
        if (ref.evidenceId() == null || ref.evidenceId().isBlank()) {
            reasons.add("evidence_id missing");
        }
        if (ref.sourceId() == null || ref.sourceId().isBlank()) {
            reasons.add("source_id missing");
        }
        if (ref.chunkId() == null || ref.chunkId().isBlank()) {
            reasons.add("chunk_id missing");
        }
        if (ref.symptomGroup() == null || ref.symptomGroup().isBlank()) {
            reasons.add("symptom_group missing");
        }
        if (ref.sourceType() == null || ref.sourceType().isBlank()) {
            reasons.add("source_type missing");
        }
        if (Double.isNaN(ref.retrievalScore()) || ref.retrievalScore() < 0 || ref.retrievalScore() > 1) {
            reasons.add("retrieval_score out of range");
        }
        String version = ref.evidenceCorpusVersion() != null && !ref.evidenceCorpusVersion().isBlank()
                ? ref.evidenceCorpusVersion()
                : ref.assetPackageVersion();
        if (version == null || version.isBlank()) {
            reasons.add("evidence version missing");
        }
        if (candidate.useCase() == null) {
            reasons.add("use_case missing");
        } else if (!ALLOWED_USE_CASES.contains(candidate.useCase())) {
            reasons.add("unsupported use_case: " + candidate.useCase());
        }
        if (candidate.useCase() == EvidenceUseCase.RECOMMEND_TEST
                || "patient_direct_answer".equalsIgnoreCase(String.valueOf(candidate.useCase()))) {
            reasons.add("forbidden use_case");
        }
        if (request != null && request.roleContext() != null
                && "patient_direct_answer".equalsIgnoreCase(request.roleContext())) {
            reasons.add("forbidden role_context");
        }
        return reasons;
    }
}
