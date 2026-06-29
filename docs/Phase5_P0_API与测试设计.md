# Phase 5-P0 API 与测试设计

> 本文档定义 Phase 5-P0 的 internal/debug API、测试分层、人工验收和回归要求。  
> Phase 5-P0 API 只服务于持久化健康检查、审计查询和持久化回归，不提供正式 Console、RBAC、训练发布或经验上线能力。

---

# 一、API 原则

```text
1. 继续使用 /api/v1/debug/** 前缀。
2. 不新增 patient-facing API。
3. 不新增正式 Console API。
4. 不新增用户登录 / RBAC API。
5. 不新增 ApprovedExperience / TrainingDataset 发布 API。
6. 所有会产生持久化副作用的 debug API 应写 AuditLog。
```

---

# 二、Persistence Health API

## 2.1 API

```http
GET /api/v1/debug/persistence/health
```

## 2.2 响应

```json
{
  "success": true,
  "data": {
    "mode": "postgres",
    "database": "PostgreSQL",
    "schema_version": "5.0.0",
    "runtime_store": "jdbc",
    "evaluation_store": "jdbc",
    "candidate_store": "jdbc",
    "review_store": "jdbc",
    "audit_log_store": "jdbc"
  }
}
```

---

# 三、AuditLog API

## 3.1 查询列表

```http
GET /api/v1/debug/audit-logs?resource_type=CANDIDATE&resource_id=xxx&limit=50
```

## 3.2 查询单条

```http
GET /api/v1/debug/audit-logs/{audit_id}
```

## 3.3 错误码

```text
AUDIT_LOG_NOT_FOUND
INVALID_AUDIT_LOG_QUERY
```

---

# 四、现有 API 的 Phase5 行为变化

以下 API 行为不改变，但新增持久化与审计副作用：

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/{runtime_id}/continue
POST /api/v1/debug/evaluations/runs
POST /api/v1/debug/candidates/generations/from-evaluation/{run_id}
POST /api/v1/debug/candidates/experience-candidates/{candidate_id}/review
POST /api/v1/debug/candidates/training-example-candidates/{candidate_id}/review
```

要求：

```text
1. 响应结构不因为持久化改变。
2. in-memory / postgres 两种模式 API 语义一致。
3. postgres 模式下重启后仍可查询关键对象。
4. 关键写操作进入 AuditLog。
```

---

# 五、测试分层

## 5.1 Schema / Migration Test

```text
FlywayMigrationTest
DatabaseSchemaSmokeTest
```

验证：

```text
1. PostgreSQL schema 可创建。
2. 表 / 索引存在。
3. JSONB 字段可写入与读取。
```

## 5.2 Repository Test

```text
JdbcRuntimeStoreTest
JdbcEvaluationRunStoreTest
JdbcCandidateStoreTest
JdbcCandidateReviewStoreTest
JdbcAuditLogStoreTest
```

## 5.3 Serialization Test

```text
RuntimeSnapshotMapperTest
EvaluationSnapshotMapperTest
CandidateSnapshotMapperTest
ReviewSnapshotMapperTest
```

## 5.4 Integration Test

```text
PostgresRuntimePersistenceIntegrationTest
PostgresEvaluationPersistenceIntegrationTest
PostgresCandidatePersistenceIntegrationTest
PostgresReviewPersistenceIntegrationTest
AuditLogIntegrationTest
```

## 5.5 E2E Test

```text
Phase5PostgresEndToEndIntegrationTest
```

覆盖链路：

```text
Runtime start
→ EvaluationRun
→ CandidateGeneration
→ CandidateReview
→ AuditLog query
→ Restart/reload simulation（如可行）
```

---

# 六、人工验收建议

新增：

```text
docs/Phase5_P0人工测试API结果.md
```

验收场景：

```text
1. postgres 模式启动成功，Flyway migration 完成。
2. GET persistence health 返回 postgres / schema_version。
3. 创建 Runtime 后数据库可查询 runtime_sessions / runtime_traces。
4. 创建 EvaluationRun 后数据库可查询 evaluation_runs / evaluation_items。
5. 生成 Candidate 后数据库可查询 candidate_generations / candidates。
6. Review Candidate 后数据库可查询 candidate_review_records。
7. AuditLog 可查询到 candidate generation / review 操作。
8. 重启服务后仍可查询已持久化对象。
9. in-memory 模式仍可启动并通过既有回归。
10. 没有新增 RAG / 模型训练 / 前端 / 正式 RBAC。
```

---

# 七、CI 建议

P0 可以分两阶段：

```text
阶段 1：保留现有 mvn test，in-memory 全量回归。
阶段 2：新增 postgres profile，使用 Testcontainers 运行持久化专项测试。
```

Maven Profile 建议：

```text
-Ppostgres-it
```

---

# 八、错误码

新增错误码：

```text
PERSISTENCE_NOT_AVAILABLE
PERSISTENCE_MODE_NOT_SUPPORTED
AUDIT_LOG_NOT_FOUND
DATABASE_MIGRATION_FAILED
DEBUG_TOKEN_REQUIRED
INVALID_DEBUG_TOKEN
```

---

# 九、完成标准

```text
1. 所有新增 Repository 单元 / 集成测试通过。
2. PostgreSQL 模式 E2E 通过。
3. in-memory 模式既有回归通过。
4. 人工 API 验收记录补齐。
5. AuditLog 不泄露未脱敏字段。
6. 入口文档同步 Phase5-P0 状态。
```

---

# 十、最终结论

Phase 5-P0 的 API 和测试重点是证明：

```text
系统的治理对象不再只是内存态，
关键操作有审计，
持久化不改变 Runtime / Evaluation / Candidate / Review 的业务语义。
```
