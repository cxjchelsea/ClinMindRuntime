# Phase 4 人工 API 验收记录

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-06-29 |
| 验收人 | 手动验收 |
| 验收结论 | **通过** — Phase 4-P0 候选沉淀 debug API 人工 API 验收合格 |
| 代码基线 | commit 待本次 P0-G 提交 |
| 启动方式 | `set JAVA_HOME=D:\cxj\software\jdk21` → `mvn -DskipTests package` → `java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| Base URL | `http://localhost:8080` |
| 自动化补充 | `CandidateControllerTest` + `CandidateEndToEndIntegrationTest`；全量 `mvn test` **245 项全绿** |

## 用例汇总

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | 从 failure EvaluationRun 生成候选 | `broken-package` + `high_risk`；`experience_count=9`，`training_count=3` | ✅ |
| 2 | 查询 generation result | 返回 `started_at` / `completed_at` / candidates / skipped_items | ✅ |
| 3 | 查询 experience candidates 列表 | 9 条；`review_status=REVIEW_REQUIRED` | ✅ |
| 4 | 查询 training candidates 列表 | 3 条；`review_status=REVIEW_REQUIRED` | ✅ |
| 5 | 从 passed run 生成空候选 | `phase2-default` + `high_risk`；count 均为 0 | ✅ |
| 6 | unknown run_id | HTTP 404，`EVALUATION_RUN_NOT_FOUND` | ✅ |
| 7 | unknown generation_id | HTTP 404，`CANDIDATE_GENERATION_NOT_FOUND` | ✅ |
| 8 | 非法 policy 参数 | `max_candidates_per_case: -1` → HTTP 400，`INVALID_CANDIDATE_GENERATION_REQUEST` | ✅ |
| 9 | 单个 experience / training 候选 | `source_ref.evaluation_run_id` 与源 run 一致 | ✅ |
| 10 | 候选默认待审核 | 全部 `REVIEW_REQUIRED`，不自动生效 | ✅ |

## Phase 4 专项结论

| 验收项 | 结果 |
|--------|------|
| POST 可从 EvaluationRun 触发候选生成 | ✅ |
| GET 可查询 generation / experience / training 候选 | ✅ |
| 候选默认 `REVIEW_REQUIRED`，不自动生效 | ✅ |
| API 只读 EvaluationRunStore，不重新评估 | ✅ |
| API 不修改 AssetPackage / CapabilityProfile | ✅ |
| Phase 1 / 2 / 3 回归（245 项 JUnit） | ✅ |

### 关键 ID 对照

| 用例 | run_id | generation_id | candidate_id | 备注 |
|------|--------|---------------|--------------|------|
| 1 失败 run 生成候选 | `eval_95fc766c8c92` | `cand_gen_023dbd069a33` | — | `status=failed`，`failed_cases=1` |
| 3 experience 列表 | — | `cand_gen_023dbd069a33` | `exp_cand_eval_95fc766c8c92_rf_rf_eval_95fc766c8c92_asset_trace` | 共 9 条 |
| 4 training 列表 | — | `cand_gen_023dbd069a33` | `train_cand_eval_95fc766c8c92_chest_pain_high_risk_001_asset_trace` | 共 3 条 |
| 5 passed run 空候选 | `eval_08fe96f0a487` | （空 generation） | — | experience / training count 均为 0 |

---

## 详细请求与响应

### 用例 1 — 创建失败评估运行

POST `http://localhost:8080/api/v1/debug/evaluations/runs`

```json
{
  "case_set_id": "phase3-default",
  "case_set_version": "0.3.0",
  "asset_package_id": "broken-package",
  "asset_package_version": "0.2.0",
  "include_tags": ["high_risk"],
  "fail_fast": false
}
```

```json
{
  "success": true,
  "data": {
    "run_id": "eval_95fc766c8c92",
    "status": "failed",
    "total_cases": 1,
    "passed_cases": 0,
    "failed_cases": 1,
    "pass_rate": 0.0
  }
}
```

### 用例 2 — 从失败 run 生成候选

POST `http://localhost:8080/api/v1/debug/candidates/generations/from-evaluation/eval_95fc766c8c92`

```json
{
  "generate_experience_candidates": true,
  "generate_training_candidates": true,
  "generate_from_critical_failures": true,
  "generate_from_major_failures": true,
  "generate_from_minor_failures": false,
  "generate_from_passed_cases": false,
  "max_candidates_per_case": 5
}
```

```json
{
  "success": true,
  "data": {
    "generation_id": "cand_gen_023dbd069a33",
    "source_evaluation_run_id": "eval_95fc766c8c92",
    "experience_candidate_count": 9,
    "training_candidate_count": 3,
    "skipped_item_count": 0,
    "warnings": []
  }
}
```

### 用例 3 — 查询 generation 详情

GET `http://localhost:8080/api/v1/debug/candidates/generations/cand_gen_023dbd069a33`

验证：`experience_candidates` 与 `training_example_candidates` 非空；首条 `review_status` 均为 `REVIEW_REQUIRED`。

### 用例 4 — 查询 experience / training 列表与单个候选

GET `http://localhost:8080/api/v1/debug/candidates/generations/cand_gen_023dbd069a33/experience-candidates`

GET `http://localhost:8080/api/v1/debug/candidates/generations/cand_gen_023dbd069a33/training-example-candidates`

GET `http://localhost:8080/api/v1/debug/candidates/experience-candidates/exp_cand_eval_95fc766c8c92_rf_rf_eval_95fc766c8c92_asset_trace`

GET `http://localhost:8080/api/v1/debug/candidates/training-example-candidates/train_cand_eval_95fc766c8c92_chest_pain_high_risk_001_asset_trace`

验证：`source_ref.evaluation_run_id` = `eval_95fc766c8c92`。

### 用例 5 — passed run 空候选

POST 评估（`phase2-default` + `high_risk`）→ `run_id=eval_08fe96f0a487`

POST `.../generations/from-evaluation/eval_08fe96f0a487` body `{}`

验证：`experience_candidate_count=0`，`training_candidate_count=0`。

### 用例 6–8 — 错误码

| 请求 | HTTP | code |
|------|------|------|
| POST `.../from-evaluation/eval_not_exist` | 404 | `EVALUATION_RUN_NOT_FOUND` |
| GET `.../generations/cand_gen_not_exist` | 404 | `CANDIDATE_GENERATION_NOT_FOUND` |
| POST `.../from-evaluation/eval_08fe96f0a487` body `{"max_candidates_per_case": -1}` | 400 | `INVALID_CANDIDATE_GENERATION_REQUEST` |

---

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/debug/candidates/generations/from-evaluation/{run_id}` | 从评估运行生成候选 |
| GET | `/api/v1/debug/candidates/generations/{generation_id}` | 查询生成结果 |
| GET | `/api/v1/debug/candidates/generations/{generation_id}/experience-candidates` | 经验候选列表 |
| GET | `/api/v1/debug/candidates/generations/{generation_id}/training-example-candidates` | 训练候选列表 |
| GET | `/api/v1/debug/candidates/experience-candidates/{candidate_id}` | 单个经验候选 |
| GET | `/api/v1/debug/candidates/training-example-candidates/{candidate_id}` | 单个训练候选 |
