package com.clinmind.runtime.candidate.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.candidate.review.CandidateReviewRequest;
import com.clinmind.runtime.candidate.review.CandidateReviewService;
import com.clinmind.runtime.console.access.AccessPolicy;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.ActorContextResolver;
import com.clinmind.runtime.console.access.ConsoleActionType;
import com.clinmind.runtime.console.access.ConsoleResourceType;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AccessPolicy accessPolicy;
    private final ActorContextResolver actorContextResolver;

    public CandidateReviewController(
            CandidateReviewService reviewService,
            AccessPolicy accessPolicy,
            ActorContextResolver actorContextResolver) {
        this.reviewService = reviewService;
        this.accessPolicy = accessPolicy;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/experience-candidates/{candidate_id}/review")
    public ApiResponse<?> reviewExperienceCandidate(
            HttpServletRequest request,
            @PathVariable("candidate_id") String candidateId,
            @RequestBody CandidateReviewRequest body) {
        requireReviewAccess(request);
        return ApiResponse.ok(reviewService.reviewExperienceCandidate(candidateId, body));
    }

    @PostMapping("/training-example-candidates/{candidate_id}/review")
    public ApiResponse<?> reviewTrainingExampleCandidate(
            HttpServletRequest request,
            @PathVariable("candidate_id") String candidateId,
            @RequestBody CandidateReviewRequest body) {
        requireReviewAccess(request);
        return ApiResponse.ok(reviewService.reviewTrainingExampleCandidate(candidateId, body));
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

    private void requireReviewAccess(HttpServletRequest request) {
        ActorContext context = actorContextResolver.resolve(request);
        accessPolicy.require(context, ConsoleActionType.REVIEW, ConsoleResourceType.CONSOLE_REVIEW);
        actorContextResolver.bindToLegacyAuditContext(context);
    }
}
