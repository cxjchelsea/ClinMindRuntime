# Phase 5-P0 冻结记录

> 本文档用于记录 ClinMindRuntime Phase 5-P0 的冻结状态、冻结依据、当前边界、已知限制和后续进入 Phase 5-P1 前的检查项。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 5-P0 的持久化与治理底座 MVP 已完成，后续不再继续向 Phase 5-P0 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 5-P0
冻结状态：已冻结
当前项目阶段：Phase 5-P0 freeze complete / Phase 5-P1 planning pending
冻结日期：2026-06-30
代码基线：未提交（工作区实现完成）
```

Phase 5-P0 已完成的主线：

```text
clinmind.persistence.mode（in-memory / postgres 双模式）
→ Flyway V5_0_0 governance schema
→ JsonSnapshotMapper + 领域 SnapshotMapper
→ AuditLog（Store / Service / Controller / DebugTokenFilter）
→ JdbcRuntimeStore / JdbcEvaluationRunStore / JdbcCandidateStore / JdbcCandidateReviewStore
→ Persistence Health API + Audit Log debug API
```

Phase 5-P0 的目标已经达到：

```text
Runtime / Evaluation / Candidate / Review 治理对象可持久化、可审计、可恢复，且业务语义不因持久化而改变。
```

---

# 二、冻结依据

## 2.1 任务清单依据

冻结依据：

```text
docs/Phase5_P0开发任务清单.md
```

当前任务清单显示 Phase5-P0-A 到 Phase5-P0-H 均已完成。

## 2.2 测试依据

```text
in-memory：347 项测试通过
postgres 专项：22 项（RUN_POSTGRES_TESTS=true + Docker 时执行，2026-06-30 已全绿）
全量（含 postgres）：369 项
```

覆盖：

```text
FlywayMigrationTest / DatabaseSchemaSmokeTest
JsonSnapshotMapperTest / RuntimeSnapshotMapperTest / EvaluationSnapshotMapperTest / ReviewSnapshotMapperTest
Jdbc*StoreTest（5 个）
AuditLog*Test + DebugTokenFilterTest
Postgres*PersistenceIntegrationTest（4 个）
Phase5PostgresEndToEndIntegrationTest
```

## 2.3 人工验收依据

```text
docs/Phase5_P0人工测试API结果.md
```

---

# 三、Phase 5-P0 边界

## 3.1 已实现

```text
1. PostgreSQL + Flyway migration（V5_0_0）。
2. in-memory / postgres 双模式 Store 切换。
3. Runtime / Evaluation / Candidate / Review / AuditLog 持久化。
4. 关键写操作 AuditLog（Runtime / Evaluation / Candidate 生成 / Review）。
5. GET /api/v1/debug/persistence/health。
6. GET /api/v1/debug/audit-logs。
7. Debug token 最小保护（X-Debug-Token，仅 debug 路径）。
8. X-Debug-Actor 审计上下文。
```

## 3.2 明确未实现（后置 Phase）

```text
1. 前端 Console / 审核平台 UI。
2. 正式 RBAC / 登录 / 多租户。
3. ApprovedExperience 自动生效。
4. TrainingDatasetVersion 发布。
5. RAG / GraphRAG / 模型训练。
6. Docker Compose 生产部署。
```

---

# 四、已知限制

```text
1. JdbcRuntimeStore 使用内存 delegate 缓存 + DB 持久化混合模式；重启后通过 exists() hydrate。
2. postgres 专项测试依赖 Testcontainers + Docker，CI 需单独 profile 或环境变量。
3. Debug token 为最小保护，非完整 RBAC。
4. ReviewSnapshotMapper 用于序列化测试；JdbcCandidateReviewStore 使用列映射（与 schema 一致）。
```

---

# 五、进入 Phase 5-P1 前检查项

```text
1. 在有 Docker 的环境跑通 RUN_POSTGRES_TESTS=true 全量 postgres 专项。
2. 确认 postgres live 启动 + persistence health 人工抽测。
3. Git commit 当前 Phase5-P0 代码与文档。
4. 规划 Phase5-P1（最小 Console API / RBAC 强化）。
```

---

# 六、最终结论

Phase 5-P0 冻结表示：持久化与最小治理底座已落地，系统从纯内存 debug 原型升级为可 PostgreSQL 持久化、可审计的 governance runtime 基础，但不改变 AI 决策边界与患者端输出边界。
