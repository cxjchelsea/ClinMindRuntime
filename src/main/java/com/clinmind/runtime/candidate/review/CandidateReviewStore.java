package com.clinmind.runtime.candidate.review;

import java.util.List;

public interface CandidateReviewStore {

    void saveReviewRecord(CandidateReviewRecord record);

    CandidateReviewRecord getReviewRecord(String reviewId);

    List<CandidateReviewRecord> listReviewsByCandidate(String candidateId);
}
