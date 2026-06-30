package com.clinmind.runtime.evaluation.jdbc;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.persistence.EvaluationSnapshotMapper;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvaluationRunStore implements EvaluationRunStore {

    private final JdbcTemplate jdbcTemplate;
    private final EvaluationSnapshotMapper evaluationSnapshotMapper;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcEvaluationRunStore(
            JdbcTemplate jdbcTemplate,
            EvaluationSnapshotMapper evaluationSnapshotMapper,
            JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.evaluationSnapshotMapper = evaluationSnapshotMapper;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    @Transactional
    public void save(EvaluationRun run) {
        jdbcTemplate.update(
                """
                insert into evaluation_runs (
                  run_id, case_set_id, case_set_version, asset_package_id, asset_package_version,
                  status, started_at, completed_at, config_snapshot, result_snapshot
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (run_id) do update set
                  case_set_id = excluded.case_set_id,
                  case_set_version = excluded.case_set_version,
                  asset_package_id = excluded.asset_package_id,
                  asset_package_version = excluded.asset_package_version,
                  status = excluded.status,
                  started_at = excluded.started_at,
                  completed_at = excluded.completed_at,
                  config_snapshot = excluded.config_snapshot,
                  result_snapshot = excluded.result_snapshot
                """,
                run.runId(),
                run.config().caseSetId(),
                run.config().caseSetVersion(),
                run.config().assetPackageId(),
                run.config().assetPackageVersion(),
                run.status().name(),
                toTimestamp(run.startedAt()),
                toTimestamp(run.completedAt()),
                jsonSnapshotMapper.toJsonb(run.config()),
                run.result() == null ? null : jsonSnapshotMapper.toJsonb(run.result()));

        jdbcTemplate.update("delete from evaluation_items where run_id = ?", run.runId());
        for (EvaluationItemResult item : run.itemResults()) {
            jdbcTemplate.update(
                    """
                    insert into evaluation_items (
                      item_id, run_id, case_id, runtime_id, passed, total_score, severity,
                      created_at, item_snapshot
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    itemId(run.runId(), item.caseId()),
                    run.runId(),
                    item.caseId(),
                    item.runtimeId(),
                    item.passed(),
                    item.score(),
                    null,
                    Timestamp.from(Instant.now()),
                    jsonSnapshotMapper.toJsonb(item));
        }
    }

    @Override
    public EvaluationRun get(String runId) {
        List<EvaluationRun> runs = jdbcTemplate.query(
                """
                select run_id, status, started_at, completed_at, config_snapshot, result_snapshot
                from evaluation_runs where run_id = ?
                """,
                (rs, rowNum) -> {
                    String configJson = jsonSnapshotMapper.readJsonb(rs.getObject("config_snapshot"));
                    String resultJson = jsonSnapshotMapper.readJsonb(rs.getObject("result_snapshot"));
                    List<EvaluationItemResult> items = loadItems(runId);
                    return new EvaluationRun(
                            rs.getString("run_id"),
                            evaluationSnapshotMapper.configFromJson(configJson),
                            com.clinmind.runtime.evaluation.EvaluationRunStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("started_at") == null
                                    ? null
                                    : rs.getTimestamp("started_at").toInstant(),
                            rs.getTimestamp("completed_at") == null
                                    ? null
                                    : rs.getTimestamp("completed_at").toInstant(),
                            items,
                            resultJson == null ? null : evaluationSnapshotMapper.resultFromJson(resultJson));
                },
                runId);
        if (runs.isEmpty()) {
            throw new EvaluationLoadException("Evaluation run not found: " + runId, null);
        }
        return runs.get(0);
    }

    @Override
    public void saveExecution(String runId, String caseId, RuntimeCaseExecution execution) {
        jdbcTemplate.update(
                """
                insert into runtime_case_executions (
                  execution_id, run_id, case_id, runtime_id, created_at, execution_snapshot
                ) values (?, ?, ?, ?, ?, ?)
                on conflict (execution_id) do update set
                  run_id = excluded.run_id,
                  case_id = excluded.case_id,
                  runtime_id = excluded.runtime_id,
                  created_at = excluded.created_at,
                  execution_snapshot = excluded.execution_snapshot
                """,
                executionId(runId, caseId),
                runId,
                caseId,
                execution.runtimeId(),
                Timestamp.from(Instant.now()),
                jsonSnapshotMapper.toJsonb(execution));
    }

    @Override
    public RuntimeCaseExecution getExecution(String runId, String caseId) {
        List<RuntimeCaseExecution> executions = jdbcTemplate.query(
                """
                select execution_snapshot from runtime_case_executions
                where run_id = ? and case_id = ?
                """,
                (rs, rowNum) -> evaluationSnapshotMapper.executionFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("execution_snapshot"))),
                runId,
                caseId);
        if (executions.isEmpty()) {
            throw new EvaluationLoadException(
                    "Evaluation case execution not found: " + runId + "/" + caseId,
                    null);
        }
        return executions.get(0);
    }

    @Override
    public List<RuntimeCaseExecution> listExecutions(String runId) {
        return jdbcTemplate.query(
                """
                select execution_snapshot from runtime_case_executions
                where run_id = ? order by created_at
                """,
                (rs, rowNum) -> evaluationSnapshotMapper.executionFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("execution_snapshot"))),
                runId);
    }

    private List<EvaluationItemResult> loadItems(String runId) {
        return jdbcTemplate.query(
                "select item_snapshot from evaluation_items where run_id = ? order by created_at",
                (rs, rowNum) -> evaluationSnapshotMapper.itemFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("item_snapshot"))),
                runId);
    }

    private static String itemId(String runId, String caseId) {
        return runId + ":" + caseId;
    }

    private static String executionId(String runId, String caseId) {
        return runId + ":" + caseId + ":exec";
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
