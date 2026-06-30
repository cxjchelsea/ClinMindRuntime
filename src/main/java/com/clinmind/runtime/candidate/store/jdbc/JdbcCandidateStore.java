package com.clinmind.runtime.candidate.store.jdbc;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.candidate.store.CandidateNotFoundException;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.persistence.CandidateSnapshotMapper;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcCandidateStore implements CandidateStore {

    private final JdbcTemplate jdbcTemplate;
    private final CandidateSnapshotMapper candidateSnapshotMapper;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcCandidateStore(
            JdbcTemplate jdbcTemplate,
            CandidateSnapshotMapper candidateSnapshotMapper,
            JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.candidateSnapshotMapper = candidateSnapshotMapper;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    @Transactional
    public void saveGenerationResult(CandidateGenerationResult result) {
        jdbcTemplate.update(
                """
                insert into candidate_generations (
                  generation_id, source_evaluation_run_id, started_at, completed_at,
                  experience_candidate_count, training_candidate_count, skipped_item_count,
                  generation_snapshot
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (generation_id) do update set
                  source_evaluation_run_id = excluded.source_evaluation_run_id,
                  started_at = excluded.started_at,
                  completed_at = excluded.completed_at,
                  experience_candidate_count = excluded.experience_candidate_count,
                  training_candidate_count = excluded.training_candidate_count,
                  skipped_item_count = excluded.skipped_item_count,
                  generation_snapshot = excluded.generation_snapshot
                """,
                result.generationId(),
                result.sourceEvaluationRunId(),
                toTimestamp(result.startedAt()),
                toTimestamp(result.completedAt()),
                result.experienceCandidates().size(),
                result.trainingExampleCandidates().size(),
                result.skippedItems().size(),
                jsonSnapshotMapper.toJsonb(result));

        for (ExperienceCandidate candidate : result.experienceCandidates()) {
            upsertExperienceCandidate(result.generationId(), candidate);
        }
        for (TrainingExampleCandidate candidate : result.trainingExampleCandidates()) {
            upsertTrainingCandidate(result.generationId(), candidate);
        }
    }

    @Override
    public CandidateGenerationResult getGenerationResult(String generationId) {
        List<String> snapshots = jdbcTemplate.query(
                "select generation_snapshot from candidate_generations where generation_id = ?",
                (rs, rowNum) -> jsonSnapshotMapper.readJsonb(rs.getObject("generation_snapshot")),
                generationId);
        if (snapshots.isEmpty()) {
            throw CandidateNotFoundException.generationNotFound(generationId);
        }
        return candidateSnapshotMapper.generationFromJson(snapshots.get(0));
    }

    @Override
    public List<ExperienceCandidate> listExperienceCandidates(String generationId) {
        getGenerationResult(generationId);
        return jdbcTemplate.query(
                """
                select candidate_snapshot from experience_candidates
                where generation_id = ? order by created_at
                """,
                (rs, rowNum) -> candidateSnapshotMapper.experienceFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("candidate_snapshot"))),
                generationId);
    }

    @Override
    public List<TrainingExampleCandidate> listTrainingExampleCandidates(String generationId) {
        getGenerationResult(generationId);
        return jdbcTemplate.query(
                """
                select candidate_snapshot from training_example_candidates
                where generation_id = ? order by created_at
                """,
                (rs, rowNum) -> candidateSnapshotMapper.trainingFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("candidate_snapshot"))),
                generationId);
    }

    @Override
    public ExperienceCandidate getExperienceCandidate(String candidateId) {
        List<ExperienceCandidate> candidates = jdbcTemplate.query(
                "select candidate_snapshot from experience_candidates where candidate_id = ?",
                (rs, rowNum) -> candidateSnapshotMapper.experienceFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("candidate_snapshot"))),
                candidateId);
        if (candidates.isEmpty()) {
            throw CandidateNotFoundException.experienceCandidateNotFound(candidateId);
        }
        return candidates.get(0);
    }

    @Override
    public TrainingExampleCandidate getTrainingExampleCandidate(String candidateId) {
        List<TrainingExampleCandidate> candidates = jdbcTemplate.query(
                "select candidate_snapshot from training_example_candidates where candidate_id = ?",
                (rs, rowNum) -> candidateSnapshotMapper.trainingFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("candidate_snapshot"))),
                candidateId);
        if (candidates.isEmpty()) {
            throw CandidateNotFoundException.trainingExampleCandidateNotFound(candidateId);
        }
        return candidates.get(0);
    }

    @Override
    @Transactional
    public void updateExperienceCandidate(ExperienceCandidate candidate) {
        if (!existsExperienceCandidate(candidate.candidateId())) {
            throw CandidateNotFoundException.experienceCandidateNotFound(candidate.candidateId());
        }
        String generationId = findGenerationIdForExperience(candidate.candidateId());
        upsertExperienceCandidate(generationId, candidate);
        refreshGenerationSnapshot(generationId, candidate, null);
    }

    @Override
    @Transactional
    public void updateTrainingExampleCandidate(TrainingExampleCandidate candidate) {
        if (!existsTrainingCandidate(candidate.candidateId())) {
            throw CandidateNotFoundException.trainingExampleCandidateNotFound(candidate.candidateId());
        }
        String generationId = findGenerationIdForTraining(candidate.candidateId());
        upsertTrainingCandidate(generationId, candidate);
        refreshGenerationSnapshot(generationId, null, candidate);
    }

    private void upsertExperienceCandidate(String generationId, ExperienceCandidate candidate) {
        jdbcTemplate.update(
                """
                insert into experience_candidates (
                  candidate_id, generation_id, candidate_type, risk_level, review_status,
                  asset_package_id, asset_package_version, created_at, candidate_snapshot, source_ref
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (candidate_id) do update set
                  generation_id = excluded.generation_id,
                  candidate_type = excluded.candidate_type,
                  risk_level = excluded.risk_level,
                  review_status = excluded.review_status,
                  asset_package_id = excluded.asset_package_id,
                  asset_package_version = excluded.asset_package_version,
                  created_at = excluded.created_at,
                  candidate_snapshot = excluded.candidate_snapshot,
                  source_ref = excluded.source_ref
                """,
                candidate.candidateId(),
                generationId,
                candidate.candidateType().name(),
                candidate.riskLevel().name(),
                candidate.reviewStatus().name(),
                candidate.sourceRef().assetPackageId(),
                candidate.sourceRef().assetPackageVersion(),
                toTimestamp(candidate.createdAt()),
                jsonSnapshotMapper.toJsonb(candidate),
                jsonSnapshotMapper.toJsonb(candidate.sourceRef()));
    }

    private void upsertTrainingCandidate(String generationId, TrainingExampleCandidate candidate) {
        String policyId = candidate.metadata().get("sanitizer_policy_id") == null
                ? null
                : String.valueOf(candidate.metadata().get("sanitizer_policy_id"));
        String policyVersion = candidate.metadata().get("sanitizer_policy_version") == null
                ? null
                : String.valueOf(candidate.metadata().get("sanitizer_policy_version"));
        jdbcTemplate.update(
                """
                insert into training_example_candidates (
                  candidate_id, generation_id, task_type, risk_level, review_status,
                  sanitization_status, sanitizer_policy_id, sanitizer_policy_version,
                  asset_package_id, asset_package_version, created_at, candidate_snapshot, source_ref
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (candidate_id) do update set
                  generation_id = excluded.generation_id,
                  task_type = excluded.task_type,
                  risk_level = excluded.risk_level,
                  review_status = excluded.review_status,
                  sanitization_status = excluded.sanitization_status,
                  sanitizer_policy_id = excluded.sanitizer_policy_id,
                  sanitizer_policy_version = excluded.sanitizer_policy_version,
                  asset_package_id = excluded.asset_package_id,
                  asset_package_version = excluded.asset_package_version,
                  created_at = excluded.created_at,
                  candidate_snapshot = excluded.candidate_snapshot,
                  source_ref = excluded.source_ref
                """,
                candidate.candidateId(),
                generationId,
                candidate.taskType().name(),
                candidate.riskLevel().name(),
                candidate.reviewStatus().name(),
                candidate.sanitizationStatus().name(),
                policyId,
                policyVersion,
                candidate.sourceRef().assetPackageId(),
                candidate.sourceRef().assetPackageVersion(),
                toTimestamp(candidate.createdAt()),
                jsonSnapshotMapper.toJsonb(candidate),
                jsonSnapshotMapper.toJsonb(candidate.sourceRef()));
    }

    private void refreshGenerationSnapshot(
            String generationId, ExperienceCandidate experienceCandidate, TrainingExampleCandidate trainingCandidate) {
        CandidateGenerationResult current = getGenerationResult(generationId);
        List<ExperienceCandidate> experienceCandidates = new ArrayList<>(current.experienceCandidates());
        List<TrainingExampleCandidate> trainingCandidates = new ArrayList<>(current.trainingExampleCandidates());

        if (experienceCandidate != null) {
            experienceCandidates.replaceAll(candidate -> candidate.candidateId().equals(experienceCandidate.candidateId())
                    ? experienceCandidate
                    : candidate);
        }
        if (trainingCandidate != null) {
            trainingCandidates.replaceAll(candidate -> candidate.candidateId().equals(trainingCandidate.candidateId())
                    ? trainingCandidate
                    : candidate);
        }

        CandidateGenerationResult refreshed = new CandidateGenerationResult(
                current.generationId(),
                current.sourceEvaluationRunId(),
                current.startedAt(),
                current.completedAt(),
                List.copyOf(experienceCandidates),
                List.copyOf(trainingCandidates),
                current.skippedItems(),
                current.warnings());

        jdbcTemplate.update(
                """
                update candidate_generations set
                  experience_candidate_count = ?,
                  training_candidate_count = ?,
                  generation_snapshot = ?
                where generation_id = ?
                """,
                refreshed.experienceCandidates().size(),
                refreshed.trainingExampleCandidates().size(),
                jsonSnapshotMapper.toJsonb(refreshed),
                generationId);
    }

    private boolean existsExperienceCandidate(String candidateId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from experience_candidates where candidate_id = ?",
                Integer.class,
                candidateId);
        return count != null && count > 0;
    }

    private boolean existsTrainingCandidate(String candidateId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from training_example_candidates where candidate_id = ?",
                Integer.class,
                candidateId);
        return count != null && count > 0;
    }

    private String findGenerationIdForExperience(String candidateId) {
        List<String> generationIds = jdbcTemplate.query(
                "select generation_id from experience_candidates where candidate_id = ?",
                (rs, rowNum) -> rs.getString("generation_id"),
                candidateId);
        return generationIds.isEmpty() ? null : generationIds.get(0);
    }

    private String findGenerationIdForTraining(String candidateId) {
        List<String> generationIds = jdbcTemplate.query(
                "select generation_id from training_example_candidates where candidate_id = ?",
                (rs, rowNum) -> rs.getString("generation_id"),
                candidateId);
        return generationIds.isEmpty() ? null : generationIds.get(0);
    }

    private static Timestamp toTimestamp(java.time.Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
