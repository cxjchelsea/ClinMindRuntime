package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSourceRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvidenceSourceRepository implements EvidenceSourceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEvidenceSourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SourceRegistryEntry source) {
        jdbcTemplate.update("""
                insert into evidence_source (
                  source_id, display_name, publisher, source_type, authority_level, jurisdiction, language,
                  homepage, license_status, license_name, license_reference, trust_status, review_status,
                  reviewed_at, reviewed_by, notes, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                on conflict (source_id) do update set
                  display_name = excluded.display_name,
                  publisher = excluded.publisher,
                  source_type = excluded.source_type,
                  authority_level = excluded.authority_level,
                  jurisdiction = excluded.jurisdiction,
                  language = excluded.language,
                  homepage = excluded.homepage,
                  license_status = excluded.license_status,
                  license_name = excluded.license_name,
                  license_reference = excluded.license_reference,
                  trust_status = excluded.trust_status,
                  review_status = excluded.review_status,
                  reviewed_at = excluded.reviewed_at,
                  reviewed_by = excluded.reviewed_by,
                  notes = excluded.notes,
                  updated_at = now()
                """,
                source.sourceId(), source.displayName(), source.publisher(), source.sourceType().name(),
                source.authorityLevel().name(), source.jurisdiction(), source.language(), source.homepage(),
                source.licenseStatus().name(), source.licenseName(), source.licenseReference(),
                source.trustStatus().name(), source.reviewStatus().name(), timestamp(source.reviewedAt()),
                source.reviewedBy(), source.notes());
    }

    @Override
    public Optional<SourceRegistryEntry> findBySourceId(String sourceId) {
        List<SourceRegistryEntry> rows = jdbcTemplate.query("""
                select source_id, display_name, publisher, source_type, authority_level, jurisdiction, language,
                       homepage, license_status, license_name, license_reference, trust_status, review_status,
                       reviewed_at, reviewed_by, notes
                from evidence_source where source_id = ?
                """, this::mapRow, sourceId);
        return rows.stream().findFirst();
    }

    @Override
    public List<SourceRegistryEntry> findAll() {
        return jdbcTemplate.query("""
                select source_id, display_name, publisher, source_type, authority_level, jurisdiction, language,
                       homepage, license_status, license_name, license_reference, trust_status, review_status,
                       reviewed_at, reviewed_by, notes
                from evidence_source order by source_id
                """, this::mapRow);
    }

    private SourceRegistryEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        return new SourceRegistryEntry(
                rs.getString("source_id"),
                rs.getString("display_name"),
                rs.getString("publisher"),
                EvidenceSourceType.valueOf(rs.getString("source_type")),
                AuthorityLevel.valueOf(rs.getString("authority_level")),
                rs.getString("jurisdiction"),
                rs.getString("language"),
                rs.getString("homepage"),
                LicenseStatus.valueOf(rs.getString("license_status")),
                rs.getString("license_name"),
                rs.getString("license_reference"),
                SourceTrustStatus.valueOf(rs.getString("trust_status")),
                EvidenceReviewStatus.valueOf(rs.getString("review_status")),
                reviewedAt == null ? null : reviewedAt.toInstant(),
                rs.getString("reviewed_by"),
                rs.getString("notes"));
    }

    private Timestamp timestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }
}
