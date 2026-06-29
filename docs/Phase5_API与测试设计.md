# Phase 5 API 与测试设计

> 本文档定义 Phase 5-P0 的 API 边界、配置验收、PostgreSQL integration test 和回归测试策略。  
> Phase 5-P0 不新增复杂业务 API，重点是让现有 Runtime / Evaluation / Candidate / Review API 在 memory 与 postgres 两种模式下保持一致。

---

# 一、API 总原则

```text
1. 不改变已有 patient-facing API 响应边界。
2. 不新增面向患者的数据库查询 API。
3. 不把 audit / persistence API 暴露给 patient-facing client。
4. Debug API 继续使用 /api/v1/debug 前缀。
5. PostgreSQL mode 下现有 API 行为应与 memory mode 一致。
```

---

# 二、P0 API 范围

Phase 5-P0 不需要新增大量 Controller。

优先保证现有 API 在 postgres mode 可用：

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/{runtime_id}/continue
GET  /api/v1/debug/runtime/{runtime_id}/trace
POST /api/v1/debug/evaluations/runs
GET  /api/v1/debug/evaluations/runs/{run_id}
GET  /api/v1/debug/evaluations/runs/{run_id}/result
GET  /api/v1/debug/evaluations/runs/{run_id}/items/{case_id}
POST /api/v1/debug/candidates/generations/from-evaluation/{run_id}
GET  /api/v1/debug/candidates/generations/{generation_id}
POST /api/v1/debug/candidates/experience-candidates/{candidate_id}/review
POST /api/v1/debug/candidates/training-example-candidates/{candidate_id}/review
```

可选新增 debug API：

```http
GET /api/v1/debug/persistence/health
GET /api/v1/debug/audit/logs?resource_type=...&resource_id=...
```

P0 可先不开放 audit query controller，只做 AuditLogStore + integration test。

---

# 三、配置验收

必须支持：

```properties
clinmind.persistence.mode=memory
clinmind.persistence.mode=postgres
```

memory mode：

```text
1. 不需要数据库。
2. 现有测试默认使用 memory。
3. 启动速度快。
```

postgres mode：

```text
1. 需要 PostgreSQL datasource。
2. Flyway migration 必须先执行。
3. Store bean 切换为 Postgres 实现。
4. integration test 使用 Testcontainers。
```

---

# 四、测试分层

## 4.1 Memory regression tests

目标：保护 Phase1–4 已冻结行为。

```text
RuntimeServiceTest
PatientOutputAssetIsolationTest
RuntimeAssetVersionMismatchTest
EvaluationEndToEndIntegrationTest
CandidateEndToEndIntegrationTest
CandidateReviewEndToEndIntegrationTest
```

## 4.2 Schema tests

```text
PostgresSchemaMigrationTest
```

验证：

```text
1. Flyway migration 成功。
2. 核心表存在。
3. 核心索引存在。
4. JSONB 默认值可写入。
```

## 4.3 Store tests

```text
PostgresCandidateStoreTest
PostgresCandidateReviewStoreTest
PostgresEvaluationRunStoreTest
PostgresRuntimeStoreTest
PostgresAuditLogStoreTest
```

## 4.4 Integration tests

```text
PostgresCandidateReviewAuditIntegrationTest
PostgresEvaluationCandidateIntegrationTest
PostgresRuntimeTracePersistenceIntegrationTest
```

最小 E2E：

```text
EvaluationRun
→ CandidateGeneration
→ CandidateReview
→ AuditLog
→ restart-like reload / query
```

---

# 五、验收场景

## 5.1 Postgres migration

```text
Given empty PostgreSQL
When application starts with postgres mode
Then Flyway creates all core tables
```

## 5.2 Candidate persistence

```text
Given candidate generation created candidates
When stored in postgres mode
Then generation result and candidates can be queried by API
```

## 5.3 Review persistence

```text
Given training candidate REVIEW_REQUIRED
When review APPROVE
Then candidate review_status becomes APPROVED
And CandidateReviewRecord is persisted
And AuditLog is persisted
```

## 5.4 Evaluation persistence

```text
Given evaluation run completed
When querying run result after store reload
Then EvaluationResult and item results remain queryable
```

## 5.5 Runtime trace persistence

```text
Given runtime started
When trace is generated
Then runtime_traces contains trace refs and asset refs
And does not persist raw patient input as full text
```

---

# 六、错误码

建议新增：

```text
PERSISTENCE_WRITE_FAILED
PERSISTENCE_READ_FAILED
PERSISTENCE_SERIALIZATION_FAILED
PERSISTENCE_MODE_NOT_SUPPORTED
DATABASE_MIGRATION_NOT_READY
AUDIT_WRITE_FAILED
```

HTTP 映射：

```text
PERSISTENCE_*：500
PERSISTENCE_MODE_NOT_SUPPORTED：400
DATABASE_MIGRATION_NOT_READY：503
AUDIT_WRITE_FAILED：500
```

---

# 七、人工验收文档

Phase 5-P0 完成时新增：

```text
docs/Phase5_人工测试API结果.md
```

验收记录至少包含：

```text
1. memory mode 启动。
2. postgres mode migration。
3. postgres mode Evaluation → Candidate → Review。
4. AuditLog 写入。
5. Review 后不自动修改 AssetPackage / Runtime / TrainingDataset。
6. 回归测试结果。
```

---

# 八、最终结论

Phase 5-P0 的测试重点不是“新增多少 API”，而是：

```text
同一条治理链路，memory mode 和 postgres mode 行为一致。
```

这能证明 ClinMindRuntime 已经从内存原型进入可持久化系统阶段。
