package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.clinmind.runtime.evidence.phase12.ClaimOriginType;
import com.clinmind.runtime.evidence.phase12.ClaimReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceClaim;
import com.clinmind.runtime.evidence.phase12.EvidenceClaimType;
import com.clinmind.runtime.evidence.phase12.EvidenceQuality;
import com.clinmind.runtime.evidence.phase12.RecommendationStrength;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceClaimRepository;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvidenceClaimRepository implements EvidenceClaimRepository {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcEvidenceClaimRepository(JdbcTemplate jdbcTemplate, JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    public void save(EvidenceClaim claim) {
        jdbcTemplate.update("""
                insert into evidence_claim (
                  claim_id, version_id, claim_type, normalized_claim, intended_audience, tags,
                  population, intervention, comparator, outcome, evidence_quality, recommendation_strength,
                  review_status, origin_type, claim_checksum
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (claim_id) do update set
                  version_id = excluded.version_id,
                  claim_type = excluded.claim_type,
                  normalized_claim = excluded.normalized_claim,
                  intended_audience = excluded.intended_audience,
                  tags = excluded.tags,
                  population = excluded.population,
                  intervention = excluded.intervention,
                  comparator = excluded.comparator,
                  outcome = excluded.outcome,
                  evidence_quality = excluded.evidence_quality,
                  recommendation_strength = excluded.recommendation_strength,
                  review_status = excluded.review_status,
                  origin_type = excluded.origin_type,
                  claim_checksum = excluded.claim_checksum
                """,
                claim.claimId(), claim.versionId(), claim.claimType().name(), claim.normalizedClaim(), claim.intendedAudience(),
                jsonSnapshotMapper.toJsonb(claim.tags()), claim.population(), claim.intervention(), claim.comparator(), claim.outcome(),
                claim.evidenceQuality().name(), claim.recommendationStrength().name(), claim.reviewStatus().name(),
                claim.originType().name(), claim.claimChecksum());
    }

    @Override
    public Optional<EvidenceClaim> findByClaimId(String claimId) {
        List<EvidenceClaim> rows = jdbcTemplate.query(selectSql() + " where claim_id = ?", this::mapRow, claimId);
        return rows.stream().findFirst();
    }

    @Override
    public List<EvidenceClaim> findByVersionId(String versionId) {
        return jdbcTemplate.query(selectSql() + " where version_id = ? order by claim_id", this::mapRow, versionId);
    }

    @Override
    public List<EvidenceClaim> findAll() {
        return jdbcTemplate.query(selectSql() + " order by claim_id", this::mapRow);
    }

    private String selectSql() {
        return """
                select claim_id, version_id, claim_type, normalized_claim, intended_audience, tags,
                       population, intervention, comparator, outcome, evidence_quality, recommendation_strength,
                       review_status, origin_type, claim_checksum
                from evidence_claim
                """;
    }

    private EvidenceClaim mapRow(ResultSet rs, int rowNum) throws SQLException {
        String tagsJson = jsonSnapshotMapper.readJsonb(rs.getObject("tags"));
        List<String> tags = tagsJson == null ? List.of() : jsonSnapshotMapper.fromJson(tagsJson, STRING_LIST);
        return new EvidenceClaim(
                rs.getString("claim_id"),
                rs.getString("version_id"),
                EvidenceClaimType.valueOf(rs.getString("claim_type")),
                rs.getString("normalized_claim"),
                rs.getString("intended_audience"),
                tags,
                rs.getString("population"),
                rs.getString("intervention"),
                rs.getString("comparator"),
                rs.getString("outcome"),
                EvidenceQuality.valueOf(rs.getString("evidence_quality")),
                RecommendationStrength.valueOf(rs.getString("recommendation_strength")),
                ClaimReviewStatus.valueOf(rs.getString("review_status")),
                ClaimOriginType.valueOf(rs.getString("origin_type")),
                rs.getString("claim_checksum"));
    }
}