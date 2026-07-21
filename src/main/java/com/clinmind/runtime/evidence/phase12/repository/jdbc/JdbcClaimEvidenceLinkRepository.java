package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.clinmind.runtime.evidence.phase12.CitationSupportStatus;
import com.clinmind.runtime.evidence.phase12.ClaimEvidenceLink;
import com.clinmind.runtime.evidence.phase12.repository.ClaimEvidenceLinkRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcClaimEvidenceLinkRepository implements ClaimEvidenceLinkRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcClaimEvidenceLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ClaimEvidenceLink link) {
        jdbcTemplate.update("""
                insert into claim_evidence_link (link_id, claim_id, span_id, support_status, rationale)
                values (?, ?, ?, ?, ?)
                on conflict (link_id) do update set
                  claim_id = excluded.claim_id,
                  span_id = excluded.span_id,
                  support_status = excluded.support_status,
                  rationale = excluded.rationale
                """, link.linkId(), link.claimId(), link.spanId(), link.supportStatus().name(), link.rationale());
    }

    @Override
    public Optional<ClaimEvidenceLink> findByLinkId(String linkId) {
        List<ClaimEvidenceLink> rows = jdbcTemplate.query(selectSql() + " where link_id = ?", this::mapRow, linkId);
        return rows.stream().findFirst();
    }

    @Override
    public List<ClaimEvidenceLink> findByClaimId(String claimId) {
        return jdbcTemplate.query(selectSql() + " where claim_id = ? order by link_id", this::mapRow, claimId);
    }

    @Override
    public List<ClaimEvidenceLink> findBySpanId(String spanId) {
        return jdbcTemplate.query(selectSql() + " where span_id = ? order by link_id", this::mapRow, spanId);
    }

    private String selectSql() {
        return """
                select link_id, claim_id, span_id, support_status, rationale
                from claim_evidence_link
                """;
    }

    private ClaimEvidenceLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ClaimEvidenceLink(
                rs.getString("link_id"),
                rs.getString("claim_id"),
                rs.getString("span_id"),
                CitationSupportStatus.valueOf(rs.getString("support_status")),
                rs.getString("rationale"));
    }
}