package com.clinmind.runtime.candidate.review.jdbc;

import com.clinmind.runtime.candidate.review.CandidateReviewException;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.candidate.review.CandidateReviewStore;
import com.clinmind.runtime.persistence.CandidateSnapshotMapper;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcCandidateReviewStore implements CandidateReviewStore {

    private final JdbcTemplate jdbcTemplate;
    private final CandidateSnapshotMapper candidateSnapshotMapper;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcCandidateReviewStore(
            JdbcTemplate jdbcTemplate,
            CandidateSnapshotMapper candidateSnapshotMapper,
            JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.candidateSnapshotMapper = candidateSnapshotMapper;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    public void saveReviewRecord(CandidateReviewRecord record) {
        jdbcTemplate.update(
                """
                insert into candidate_review_records (
                  review_id, candidate_id, candidate_kind, from_status, to_status,
                  decision, reviewer, reviewed_at, reason, source_ref, metadata
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (review_id) do update set
                  candidate_id = excluded.candidate_id,
                  candidate_kind = excluded.candidate_kind,
                  from_status = excluded.from_status,
                  to_status = excluded.to_status,
                  decision = excluded.decision,
                  reviewer = excluded.reviewer,
                  reviewed_at = excluded.reviewed_at,
                  reason = excluded.reason,
                  source_ref = excluded.source_ref,
                  metadata = excluded.metadata
                """,
                record.reviewId(),
                record.candidateId(),
                record.candidateKind().name(),
                record.fromStatus().name(),
                record.toStatus().name(),
                record.decision().name(),
                record.reviewer(),
                Timestamp.from(record.reviewedAt()),
                record.reason(),
                record.sourceRef() == null ? null : jsonSnapshotMapper.toJsonb(record.sourceRef()),
                jsonSnapshotMapper.toJsonb(record.metadata()));
    }

    @Override
    public CandidateReviewRecord getReviewRecord(String reviewId) {
        List<CandidateReviewRecord> records = jdbcTemplate.query(
                """
                select review_id, candidate_id, candidate_kind, from_status, to_status,
                       decision, reviewer, reviewed_at, reason, source_ref, metadata
                from candidate_review_records where review_id = ?
                """,
                (rs, rowNum) -> new CandidateReviewRecord(
                        rs.getString("review_id"),
                        rs.getString("candidate_id"),
                        com.clinmind.runtime.candidate.review.CandidateKind.valueOf(rs.getString("candidate_kind")),
                        com.clinmind.runtime.candidate.CandidateReviewStatus.valueOf(rs.getString("from_status")),
                        com.clinmind.runtime.candidate.CandidateReviewStatus.valueOf(rs.getString("to_status")),
                        com.clinmind.runtime.candidate.review.CandidateReviewDecision.valueOf(rs.getString("decision")),
                        rs.getString("reason"),
                        rs.getString("reviewer"),
                        rs.getTimestamp("reviewed_at").toInstant(),
                        readSourceRef(rs),
                        readMetadata(rs)),
                reviewId);
        if (records.isEmpty()) {
            throw new CandidateReviewException("CANDIDATE_REVIEW_NOT_FOUND", "Review record not found: " + reviewId);
        }
        return records.get(0);
    }

    @Override
    public List<CandidateReviewRecord> listReviewsByCandidate(String candidateId) {
        return jdbcTemplate.query(
                """
                select review_id, candidate_id, candidate_kind, from_status, to_status,
                       decision, reviewer, reviewed_at, reason, source_ref, metadata
                from candidate_review_records where candidate_id = ?
                order by reviewed_at
                """,
                (rs, rowNum) -> new CandidateReviewRecord(
                        rs.getString("review_id"),
                        rs.getString("candidate_id"),
                        com.clinmind.runtime.candidate.review.CandidateKind.valueOf(rs.getString("candidate_kind")),
                        com.clinmind.runtime.candidate.CandidateReviewStatus.valueOf(rs.getString("from_status")),
                        com.clinmind.runtime.candidate.CandidateReviewStatus.valueOf(rs.getString("to_status")),
                        com.clinmind.runtime.candidate.review.CandidateReviewDecision.valueOf(rs.getString("decision")),
                        rs.getString("reason"),
                        rs.getString("reviewer"),
                        rs.getTimestamp("reviewed_at").toInstant(),
                        readSourceRef(rs),
                        readMetadata(rs)),
                candidateId);
    }

    private com.clinmind.runtime.candidate.CandidateSourceRef readSourceRef(java.sql.ResultSet rs)
            throws java.sql.SQLException {
        String json = jsonSnapshotMapper.readJsonb(rs.getObject("source_ref"));
        return json == null ? null : candidateSnapshotMapper.sourceRefFromJson(json);
    }

    private java.util.Map<String, Object> readMetadata(java.sql.ResultSet rs) throws java.sql.SQLException {
        String json = jsonSnapshotMapper.readJsonb(rs.getObject("metadata"));
        if (json == null || json.isBlank()) {
            return java.util.Map.of();
        }
        return jsonSnapshotMapper.fromJson(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
    }
}
