package com.clinmind.runtime.candidate.review;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateReviewService {

    private final CandidateStore candidateStore;
    private final CandidateReviewStore reviewStore;
    private final CandidateReviewTransitionPolicy transitionPolicy;
    private final AuditLogService auditLogService;

    public CandidateReviewService(
            CandidateStore candidateStore,
            CandidateReviewStore reviewStore,
            CandidateReviewTransitionPolicy transitionPolicy,
            AuditLogService auditLogService) {
        this.candidateStore = candidateStore;
        this.reviewStore = reviewStore;
        this.transitionPolicy = transitionPolicy;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CandidateReviewRecord reviewExperienceCandidate(String candidateId, CandidateReviewRequest request) {
        ExperienceCandidate candidate = candidateStore.getExperienceCandidate(candidateId);
        validateReviewable(candidate.reviewStatus());
        CandidateReviewStatus toStatus = transitionPolicy.resolveTargetStatus(candidate.reviewStatus(), request.decision());

        CandidateReviewRecord record = new CandidateReviewRecord(
                IdGenerator.candidateReviewId(),
                candidateId,
                CandidateKind.EXPERIENCE_CANDIDATE,
                candidate.reviewStatus(),
                toStatus,
                request.decision(),
                request.reason(),
                request.reviewer(),
                Instant.now(),
                candidate.sourceRef(),
                Map.of("candidate_type", candidate.candidateType().name()));

        ExperienceCandidate updated = new ExperienceCandidate(
                candidate.candidateId(),
                candidate.candidateType(),
                candidate.title(),
                candidate.summary(),
                candidate.sourceRef(),
                candidate.riskLevel(),
                toStatus,
                candidate.suggestedAction(),
                candidate.evidence(),
                candidate.tags(),
                candidate.createdAt(),
                candidate.createdBy(),
                candidate.metadata());

        candidateStore.updateExperienceCandidate(updated);
        reviewStore.saveReviewRecord(record);
        auditLogService.record(
                AuditActionType.REVIEW_EXPERIENCE_CANDIDATE,
                AuditResourceType.EXPERIENCE_CANDIDATE,
                candidateId,
                AuditResultStatus.SUCCESS,
                Map.of("review_id", record.reviewId(), "to_status", toStatus.name()));
        return record;
    }

    @Transactional
    public CandidateReviewRecord reviewTrainingExampleCandidate(String candidateId, CandidateReviewRequest request) {
        TrainingExampleCandidate candidate = candidateStore.getTrainingExampleCandidate(candidateId);
        validateReviewable(candidate.reviewStatus());
        CandidateReviewStatus toStatus = transitionPolicy.resolveTargetStatus(candidate.reviewStatus(), request.decision());

        CandidateReviewRecord record = new CandidateReviewRecord(
                IdGenerator.candidateReviewId(),
                candidateId,
                CandidateKind.TRAINING_EXAMPLE_CANDIDATE,
                candidate.reviewStatus(),
                toStatus,
                request.decision(),
                request.reason(),
                request.reviewer(),
                Instant.now(),
                candidate.sourceRef(),
                Map.of("task_type", candidate.taskType().name()));

        TrainingExampleCandidate updated = new TrainingExampleCandidate(
                candidate.candidateId(),
                candidate.taskType(),
                candidate.sourceRef(),
                candidate.input(),
                candidate.expectedOutput(),
                candidate.negativeOutput(),
                candidate.label(),
                candidate.reason(),
                candidate.riskLevel(),
                toStatus,
                candidate.sanitizationStatus(),
                candidate.tags(),
                candidate.createdAt(),
                candidate.metadata());

        candidateStore.updateTrainingExampleCandidate(updated);
        reviewStore.saveReviewRecord(record);
        auditLogService.record(
                AuditActionType.REVIEW_TRAINING_CANDIDATE,
                AuditResourceType.TRAINING_EXAMPLE_CANDIDATE,
                candidateId,
                AuditResultStatus.SUCCESS,
                Map.of("review_id", record.reviewId(), "to_status", toStatus.name()));
        return record;
    }

    public CandidateReviewRecord getReviewRecord(String reviewId) {
        return reviewStore.getReviewRecord(reviewId);
    }

    public List<CandidateReviewRecord> listReviewsByCandidate(String candidateId) {
        return reviewStore.listReviewsByCandidate(candidateId);
    }

    private static void validateReviewable(CandidateReviewStatus status) {
        if (status != CandidateReviewStatus.REVIEW_REQUIRED && status != CandidateReviewStatus.APPROVED) {
            throw new CandidateReviewException(
                    "CANDIDATE_NOT_REVIEWABLE",
                    "Candidate is not reviewable in status: " + status);
        }
    }
}
