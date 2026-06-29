package com.clinmind.runtime.candidate.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCandidateReviewStore implements CandidateReviewStore {

    private final Map<String, CandidateReviewRecord> reviewRecords = new ConcurrentHashMap<>();
    private final Map<String, List<String>> reviewIdsByCandidate = new ConcurrentHashMap<>();

    @Override
    public void saveReviewRecord(CandidateReviewRecord record) {
        reviewRecords.put(record.reviewId(), record);
        reviewIdsByCandidate
                .computeIfAbsent(record.candidateId(), ignored -> new ArrayList<>())
                .add(record.reviewId());
    }

    @Override
    public CandidateReviewRecord getReviewRecord(String reviewId) {
        CandidateReviewRecord record = reviewRecords.get(reviewId);
        if (record == null) {
            throw new CandidateReviewException("CANDIDATE_REVIEW_NOT_FOUND", "Review record not found: " + reviewId);
        }
        return record;
    }

    @Override
    public List<CandidateReviewRecord> listReviewsByCandidate(String candidateId) {
        List<String> reviewIds = reviewIdsByCandidate.getOrDefault(candidateId, List.of());
        return reviewIds.stream().map(reviewRecords::get).toList();
    }
}
