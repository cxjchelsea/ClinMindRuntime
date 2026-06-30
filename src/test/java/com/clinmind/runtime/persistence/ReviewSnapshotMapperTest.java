package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.review.CandidateKind;
import com.clinmind.runtime.candidate.review.CandidateReviewDecision;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewSnapshotMapperTest {

    private ReviewSnapshotMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new ReviewSnapshotMapper(new JsonSnapshotMapper(objectMapper));
    }

    @Test
    void roundTripsCandidateReviewRecord() {
        CandidateReviewRecord record = new CandidateReviewRecord(
                "cand_rev_snap_001",
                "exp_cand_001",
                CandidateKind.EXPERIENCE_CANDIDATE,
                CandidateReviewStatus.REVIEW_REQUIRED,
                CandidateReviewStatus.APPROVED,
                CandidateReviewDecision.APPROVE,
                "Valid safety lesson",
                "debug-reviewer",
                Instant.parse("2026-06-29T10:00:00Z"),
                CandidateTestFixtures.sampleSourceRef(),
                Map.of("note", "ok"));

        String json = mapper.toJson(record);
        CandidateReviewRecord restored = mapper.fromJson(json);

        assertThat(restored.reviewId()).isEqualTo("cand_rev_snap_001");
        assertThat(restored.toStatus()).isEqualTo(CandidateReviewStatus.APPROVED);
        assertThat(restored.sourceRef().assetPackageId()).isEqualTo("phase2-default");
    }
}
