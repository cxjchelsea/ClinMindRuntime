package com.clinmind.runtime.candidate.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.candidate.review.CandidateReviewRequest;
import com.clinmind.runtime.candidate.review.CandidateReviewService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/candidates")
public class CandidateReviewController {

    private final CandidateReviewService reviewService;

    public CandidateReviewController(CandidateReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/experience-candidates/{candidate_id}/review")
    public ApiResponse<?> reviewExperienceCandidate(
            @PathVariable("candidate_id") String candidateId, @RequestBody CandidateReviewRequest request) {
        return ApiResponse.ok(reviewService.reviewExperienceCandidate(candidateId, request));
    }

    @PostMapping("/training-example-candidates/{candidate_id}/review")
    public ApiResponse<?> reviewTrainingExampleCandidate(
            @PathVariable("candidate_id") String candidateId, @RequestBody CandidateReviewRequest request) {
        return ApiResponse.ok(reviewService.reviewTrainingExampleCandidate(candidateId, request));
    }

    @GetMapping("/reviews/{review_id}")
    public ApiResponse<?> getReview(@PathVariable("review_id") String reviewId) {
        return ApiResponse.ok(reviewService.getReviewRecord(reviewId));
    }

    @GetMapping("/{candidate_id}/reviews")
    public ApiResponse<?> listReviews(@PathVariable("candidate_id") String candidateId) {
        List<CandidateReviewRecord> records = reviewService.listReviewsByCandidate(candidateId);
        return ApiResponse.ok(records);
    }
}
