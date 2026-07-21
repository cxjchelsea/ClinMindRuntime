package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvidenceAssetVersionRepository implements EvidenceAssetVersionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEvidenceAssetVersionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(EvidenceAssetVersion assetVersion) {
        jdbcTemplate.update("""
                insert into evidence_asset_version (
                  version_id, asset_id, source_id, title, document_type, external_reference, specialty,
                  intended_audience, jurisdiction, language, publication_date, effective_from, effective_to,
                  supersedes_version_id, lifecycle_status, review_status, checksum, mime_type, content_length,
                  parser_version, schema_version, ingested_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                on conflict (version_id) do update set
                  asset_id = excluded.asset_id,
                  source_id = excluded.source_id,
                  title = excluded.title,
                  document_type = excluded.document_type,
                  external_reference = excluded.external_reference,
                  specialty = excluded.specialty,
                  intended_audience = excluded.intended_audience,
                  jurisdiction = excluded.jurisdiction,
                  language = excluded.language,
                  publication_date = excluded.publication_date,
                  effective_from = excluded.effective_from,
                  effective_to = excluded.effective_to,
                  supersedes_version_id = excluded.supersedes_version_id,
                  lifecycle_status = excluded.lifecycle_status,
                  review_status = excluded.review_status,
                  checksum = excluded.checksum,
                  mime_type = excluded.mime_type,
                  content_length = excluded.content_length,
                  parser_version = excluded.parser_version,
                  schema_version = excluded.schema_version,
                  ingested_at = excluded.ingested_at,
                  updated_at = now()
                """,
                assetVersion.versionId(), assetVersion.assetId(), assetVersion.sourceId(), assetVersion.title(),
                assetVersion.documentType(), assetVersion.externalReference(), assetVersion.specialty(),
                assetVersion.intendedAudience(), assetVersion.jurisdiction(), assetVersion.language(),
                date(assetVersion.publicationDate()), timestamp(assetVersion.effectiveFrom()), timestamp(assetVersion.effectiveTo()),
                assetVersion.supersedesVersionId(), assetVersion.lifecycleStatus().name(), assetVersion.reviewStatus().name(),
                assetVersion.checksum(), assetVersion.mimeType(), assetVersion.contentLength(), assetVersion.parserVersion(),
                assetVersion.schemaVersion(), timestamp(assetVersion.ingestedAt()));
    }

    @Override
    public Optional<EvidenceAssetVersion> findByVersionId(String versionId) {
        List<EvidenceAssetVersion> rows = jdbcTemplate.query(selectSql() + " where version_id = ?", this::mapRow, versionId);
        return rows.stream().findFirst();
    }

    @Override
    public List<EvidenceAssetVersion> findAll() {
        return jdbcTemplate.query(selectSql() + " order by version_id", this::mapRow);
    }

    @Override
    public List<EvidenceAssetVersion> findBySourceId(String sourceId) {
        return jdbcTemplate.query(selectSql() + " where source_id = ? order by version_id", this::mapRow, sourceId);
    }

    private String selectSql() {
        return """
                select asset_id, version_id, source_id, title, document_type, external_reference, specialty,
                       intended_audience, jurisdiction, language, publication_date, effective_from, effective_to,
                       supersedes_version_id, lifecycle_status, review_status, checksum, mime_type, content_length,
                       parser_version, schema_version, ingested_at
                from evidence_asset_version
                """;
    }

    private EvidenceAssetVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date publicationDate = rs.getDate("publication_date");
        Timestamp effectiveFrom = rs.getTimestamp("effective_from");
        Timestamp effectiveTo = rs.getTimestamp("effective_to");
        Timestamp ingestedAt = rs.getTimestamp("ingested_at");
        return new EvidenceAssetVersion(
                rs.getString("asset_id"),
                rs.getString("version_id"),
                rs.getString("source_id"),
                rs.getString("title"),
                rs.getString("document_type"),
                rs.getString("external_reference"),
                rs.getString("specialty"),
                rs.getString("intended_audience"),
                rs.getString("jurisdiction"),
                rs.getString("language"),
                publicationDate == null ? null : publicationDate.toLocalDate(),
                effectiveFrom == null ? null : effectiveFrom.toInstant(),
                effectiveTo == null ? null : effectiveTo.toInstant(),
                rs.getString("supersedes_version_id"),
                AssetLifecycleStatus.valueOf(rs.getString("lifecycle_status")),
                EvidenceReviewStatus.valueOf(rs.getString("review_status")),
                rs.getString("checksum"),
                rs.getString("mime_type"),
                rs.getLong("content_length"),
                rs.getString("parser_version"),
                rs.getString("schema_version"),
                ingestedAt == null ? null : ingestedAt.toInstant());
    }

    private Date date(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private Timestamp timestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }
}
