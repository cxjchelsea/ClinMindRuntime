package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class PostgresLexicalEvidenceRetriever implements LexicalEvidenceRetriever {

    private final JdbcTemplate jdbcTemplate;
    private final ClinicalQuestionLexicalNormalizer normalizer;

    @Autowired
    public PostgresLexicalEvidenceRetriever(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new ClinicalQuestionLexicalNormalizer());
    }

    public PostgresLexicalEvidenceRetriever(JdbcTemplate jdbcTemplate, ClinicalQuestionLexicalNormalizer normalizer) {
        this.jdbcTemplate = jdbcTemplate;
        this.normalizer = normalizer == null ? new ClinicalQuestionLexicalNormalizer() : normalizer;
    }

    @Override
    public List<LexicalRetrievalCandidate> retrieve(LexicalRetrievalRequest request) {
        String query = normalizer.toPostgresWebSearchQuery(request.queryText());
        if (query.isBlank()) {
            return List.of();
        }
        SqlParts sql = buildSql(request.scope());
        List<Object> params = new ArrayList<>();
        params.add(query);
        params.add(query);
        params.add(query);
        Timestamp asOf = Timestamp.from(request.scope().asOf());
        params.add(asOf);
        params.add(asOf);
        applyFilterParams(params, request.scope().sourceTypeFilters());
        applyFilterParams(params, request.scope().specialtyFilters());
        applyFilterParams(params, request.scope().jurisdictionFilters());
        applyFilterParams(params, request.scope().languageFilters());
        applyFilterParams(params, request.scope().intendedAudienceFilters());
        params.add(request.topK());
        return jdbcTemplate.query(sql.sql(), this::mapRow, params.toArray());
    }

    private SqlParts buildSql(EligibleEvidenceScope scope) {
        StringBuilder sql = new StringBuilder("""
                select
                  concat('lex_', c.chunk_id) as candidate_id,
                  s.source_id,
                  a.asset_id,
                  a.version_id,
                  c.chunk_id,
                  sp.span_id,
                  c.section_path,
                  sp.locator,
                  sp.quoted_text,
                  c.text_checksum as chunk_text_checksum,
                  sp.span_checksum,
                  a.checksum as asset_checksum,
                  s.source_type,
                  s.authority_level,
                  a.specialty,
                  a.jurisdiction,
                  a.language,
                  a.intended_audience,
                  a.publication_date,
                  a.effective_from,
                  a.effective_to,
                  ts_rank_cd(to_tsvector('simple', c.normalized_text), websearch_to_tsquery('simple', ?))::float8 as lexical_score,
                  row_number() over (
                    order by ts_rank_cd(to_tsvector('simple', c.normalized_text), websearch_to_tsquery('simple', ?)) desc, c.chunk_id asc
                  )::int as lexical_rank
                from evidence_chunk c
                join evidence_asset_version a on a.version_id = c.version_id
                join evidence_source s on s.source_id = a.source_id
                join lateral (
                  select span_id, locator, quoted_text, span_checksum
                  from evidence_span
                  where chunk_id = c.chunk_id
                  order by start_offset asc, span_id asc
                  limit 1
                ) sp on true
                where to_tsvector('simple', c.normalized_text) @@ websearch_to_tsquery('simple', ?)
                  and s.review_status = 'APPROVED'
                  and s.license_status = 'VERIFIED'
                  and s.trust_status <> 'BLOCKED'
                """);
        if (scope.isProduction()) {
            sql.append("""
                  and a.lifecycle_status = 'PUBLISHED'
                  and a.review_status = 'PUBLISHED'
                  and (a.effective_from is null or a.effective_from <= ?)
                  and (a.effective_to is null or a.effective_to >= ?)
                """);
        } else {
            sql.append("""
                  and a.lifecycle_status not in ('REVOKED', 'DEPRECATED')
                  and (a.effective_from is null or a.effective_from <= ?)
                  and (a.effective_to is null or a.effective_to >= ?)
                """);
        }
        appendFilter(sql, "s.source_type", scope.sourceTypeFilters());
        appendFilter(sql, "a.specialty", scope.specialtyFilters());
        appendFilter(sql, "a.jurisdiction", scope.jurisdictionFilters());
        appendFilter(sql, "a.language", scope.languageFilters());
        appendFilter(sql, "a.intended_audience", scope.intendedAudienceFilters());
        sql.append(" order by lexical_score desc, c.chunk_id asc limit ?");
        return new SqlParts(sql.toString());
    }

    private void appendFilter(StringBuilder sql, String column, Set<String> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        sql.append(" and lower(").append(column).append(") in (");
        sql.append(String.join(", ", java.util.Collections.nCopies(filters.size(), "?")));
        sql.append(")\n");
    }

    private void applyFilterParams(List<Object> params, Set<String> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        filters.stream().map(value -> value.toLowerCase(java.util.Locale.ROOT)).forEach(params::add);
    }

    private LexicalRetrievalCandidate mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp effectiveFrom = rs.getTimestamp("effective_from");
        Timestamp effectiveTo = rs.getTimestamp("effective_to");
        java.sql.Date publicationDate = rs.getDate("publication_date");
        return new LexicalRetrievalCandidate(
                rs.getString("candidate_id"),
                rs.getString("source_id"),
                rs.getString("asset_id"),
                rs.getString("version_id"),
                rs.getString("chunk_id"),
                rs.getString("span_id"),
                rs.getString("section_path"),
                rs.getString("locator"),
                rs.getString("quoted_text"),
                rs.getString("chunk_text_checksum"),
                rs.getString("span_checksum"),
                rs.getString("asset_checksum"),
                EvidenceSourceType.valueOf(rs.getString("source_type")),
                AuthorityLevel.valueOf(rs.getString("authority_level")),
                rs.getString("specialty"),
                rs.getString("jurisdiction"),
                rs.getString("language"),
                rs.getString("intended_audience"),
                publicationDate == null ? null : publicationDate.toLocalDate(),
                toInstant(effectiveFrom),
                toInstant(effectiveTo),
                rs.getDouble("lexical_score"),
                rs.getInt("lexical_rank"));
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private record SqlParts(String sql) {}
}

