# Phase 4-P1 人工 API 验收记录

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-06-29 |
| 验收人 | 手动验收 + 自动化 E2E 补充 |
| 验收结论 | **通过** — Phase 4-P1 候选治理（脱敏 / SourceRef 校验 / Review API）验收合格 |
| 代码基线 | P1 实现完成，全量 `mvn test` **292 项全绿** |
| 启动方式 | `set JAVA_HOME=D:\cxj\software\jdk21` → `mvn -DskipTests package` → `java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| Base URL | `http://localhost:8080` |
| 自动化补充 | `CandidateReviewControllerTest` + `CandidateReviewEndToEndIntegrationTest` + P1 单元/集成测试 |

## 用例汇总

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | 生成 training candidate 后检查脱敏 | `metadata.sanitizer_policy_id=phase4-p1-default`；`input.input_source_type=SYNTHETIC_EVALUATION` | ✅ |
| 2 | basic_info 脱敏 | 只保留 `age_bucket` / `sex`，不含原始 `age` | ✅ |
| 3 | PATIENT_SAFE_REWRITE 保留截断 patient_output | 含 `patient_output`；不含 `patient_output_level` | ✅ |
| 4 | POST training candidate review APPROVE | `to_status=APPROVED`；返回 `review_id` | ✅ |
| 5 | GET reviews by candidate_id | 列表含 `candidate_kind=TRAINING_EXAMPLE_CANDIDATE` | ✅ |
| 6 | POST experience candidate review APPROVE | `review_status` 更新为 `APPROVED` | ✅ |
| 7 | GET review by review_id | 返回完整 `CandidateReviewRecord` | ✅ |
| 8 | 非法状态流转 REJECT → APPROVE | HTTP 400，`CANDIDATE_NOT_REVIEWABLE` | ✅ |
| 9 | unknown candidate review | HTTP 404，resourceType 映射错误码 | ✅ |
| 10 | Review 后不自动生效 | 候选 `APPROVED` 但不修改 AssetPackage / Runtime | ✅ |

## Phase 4-P1 专项结论

| 验收项 | 结果 |
|--------|------|
| TrainingExampleCandidate.input 经 CandidateSanitizer 处理 | ✅ |
| metadata 含 sanitizer policy 信息 | ✅ |
| CandidateSourceRef 经 Factory / Validator 创建 | ✅ |
| CandidateNotFoundException 使用 resourceType 映射错误码 | ✅ |
| Review API 可记录 APPROVE / REJECT 决策 | ✅ |
| Review record 可查询 | ✅ |
| 非法 review 流转返回明确错误码 | ✅ |
| Review 不触发 AssetPackage / CapabilityProfile / Runtime 变更 | ✅ |
| Phase 1 / 2 / 3 / 4-P0 回归（292 项 JUnit） | ✅ |

---

## 详细请求与响应（E2E 流程）

### 用例 1 — 创建评估运行并生成候选

POST `/api/v1/debug/evaluations/runs`

```json
{
  "case_set_id": "phase3-default",
  "case_set_version": "0.3.0",
  "asset_package_id": "broken-package",
  "asset_package_version": "0.2.0",
  "include_tags": ["safety_gate"],
  "fail_fast": false
}
```

POST `/api/v1/debug/candidates/generations/from-evaluation/{run_id}`

```json
{}
```

预期：`training_candidate_count > 0`。

### 用例 2 — 查询 training candidate 脱敏结果

GET `/api/v1/debug/candidates/training-example-candidates/{candidate_id}`

预期响应字段：

```json
{
  "success": true,
  "data": {
    "metadata": {
      "sanitizer_policy_id": "phase4-p1-default",
      "sanitizer_policy_version": "0.4.1",
      "sanitizer_warnings": ["Converted basic_info.age to age_bucket"]
    },
    "input": {
      "input_source_type": "SYNTHETIC_EVALUATION",
      "basic_info": { "age_bucket": "50-59", "sex": "male" }
    },
    "sanitization_status": "NEEDS_REVIEW",
    "review_status": "REVIEW_REQUIRED"
  }
}
```

### 用例 3 — Review training candidate

POST `/api/v1/debug/candidates/training-example-candidates/{candidate_id}/review`

```json
{
  "decision": "APPROVE",
  "reason": "Synthetic training candidate approved for governance test",
  "reviewer": "debug-reviewer"
}
```

预期：

```json
{
  "success": true,
  "data": {
    "review_id": "cand_rev_...",
    "to_status": "APPROVED"
  }
}
```

### 用例 4 — 查询 review 记录

GET `/api/v1/debug/candidates/reviews/{review_id}`

GET `/api/v1/debug/candidates/{candidate_id}/reviews`

预期：列表首条 `decision=APPROVE`，`candidate_kind=TRAINING_EXAMPLE_CANDIDATE`。

### 用例 5 — Experience candidate review

POST `/api/v1/debug/candidates/experience-candidates/{candidate_id}/review`

```json
{
  "decision": "APPROVE",
  "reason": "Valid synthetic safety lesson",
  "reviewer": "debug-reviewer"
}
```

GET `/api/v1/debug/candidates/experience-candidates/{candidate_id}`

预期：`review_status=APPROVED`。

### 用例 6 — 非法 review 流转

先 POST `decision=REJECT`，再 POST `decision=APPROVE`。

预期：第二次 HTTP 400，`error.code=CANDIDATE_NOT_REVIEWABLE`。

---

## 边界说明

```text
1. APPROVED 候选仍不代表 Runtime 可用，不自动进入 AssetPackage / CapabilityProfile。
2. Review API 为 debug 接口，非正式医生审核平台。
3. 脱敏策略默认 drop real/unknown source 的 input_texts。
4. patient_output_level 在脱敏后移除，仅 PATIENT_SAFE_REWRITE 保留截断后的 patient_output。
```
