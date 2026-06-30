package com.clinmind.runtime.persistence;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunConfig;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import org.springframework.stereotype.Component;

@Component
public class EvaluationSnapshotMapper {

    private final JsonSnapshotMapper jsonSnapshotMapper;

    public EvaluationSnapshotMapper(JsonSnapshotMapper jsonSnapshotMapper) {
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    public String toJson(EvaluationRun run) {
        return jsonSnapshotMapper.toJson(run);
    }

    public EvaluationRun runFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, EvaluationRun.class);
    }

    public String configToJson(EvaluationRunConfig config) {
        return jsonSnapshotMapper.toJson(config);
    }

    public EvaluationRunConfig configFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, EvaluationRunConfig.class);
    }

    public String resultToJson(EvaluationResult result) {
        return jsonSnapshotMapper.toJson(result);
    }

    public EvaluationResult resultFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, EvaluationResult.class);
    }

    public String toJson(EvaluationItemResult item) {
        return jsonSnapshotMapper.toJson(item);
    }

    public EvaluationItemResult itemFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, EvaluationItemResult.class);
    }

    public String toJson(RuntimeCaseExecution execution) {
        return jsonSnapshotMapper.toJson(execution);
    }

    public RuntimeCaseExecution executionFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, RuntimeCaseExecution.class);
    }
}
