# Phase 5-P0 开发任务清单

> 本清单用于约束 Phase 5-P0 的实现顺序。  
> Phase 5-P0 只做 PostgreSQL 持久化与最小治理底座，不实现 RAG、模型训练、前端 Console、正式 RBAC、正式医生审核平台、ApprovedExperience 自动生效或 TrainingDatasetVersion 发布。

---

# 一、状态标记

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要决策
[-] 后置 / 不在 P0 范围
```

---

# 二、Phase 5-P0 目标

```text
把 Runtime / Evaluation / Candidate / Review 从 in-memory/debug 原型推进到 PostgreSQL-backed governance runtime。
```

P0 必须保证：

```text
1. in-memory 模式仍可运行。
2. postgres 模式可通过配置启用。
3. 核心治理对象可持久化和查询。
4. 关键写操作进入 AuditLog。
5. 持久化不改变 Runtime / Evaluation / Candidate / Review 的业务语义。
```

---

# 三、Phase5-P0-A：依赖与配置

状态：`[x]`

任务：

```text
[x] 引入 PostgreSQL driver
[x] 引入 Flyway
[x] 配置 clinmind.persistence.mode
[x] 配置 datasource / flyway
[x] 本地 in-memory 默认不依赖数据库
[x] postgres profile / 配置示例
```

测试：

```text
[x] PersistenceModeConfigTest
[x] ApplicationContextInMemoryTest
```

---

# 四、Phase5-P0-B：Flyway Schema

状态：`[x]`

任务：

```text
[x] V5_0_0__phase5_p0_governance_schema.sql
[x] runtime_sessions / runtime_traces
[x] evaluation_runs / evaluation_items / runtime_case_executions
[x] candidate_generations / experience_candidates / training_example_candidates
[x] candidate_review_records
[x] audit_logs
[x] indexes
```

测试：

```text
[x] FlywayMigrationTest（需 RUN_POSTGRES_TESTS=true + Docker）
[x] DatabaseSchemaSmokeTest
```

---

# 五、Phase5-P0-C：Snapshot Mapper

状态：`[x]`

任务：

```text
[x] JsonSnapshotMapper
[x] RuntimeSnapshotMapper
[x] EvaluationSnapshotMapper
[x] CandidateSnapshotMapper
[x] ReviewSnapshotMapper
```

测试：

```text
[x] JsonSnapshotMapperTest
[x] CandidateSnapshotMapperTest
[x] RuntimeSnapshotMapperTest
[x] EvaluationSnapshotMapperTest
[x] ReviewSnapshotMapperTest
```

---

# 六、Phase5-P0-D：AuditLog 基础能力

状态：`[x]`

任务：

```text
[x] AuditLogRecord
[x] AuditActionType
[x] AuditResourceType
[x] AuditResultStatus
[x] AuditLogStore interface
[x] InMemoryAuditLogStore
[x] JdbcAuditLogStore
[x] AuditLogService
[x] AuditLogController
[x] DebugTokenFilter（require-debug-token 保护）
```

测试：

```text
[x] AuditLogRecordTest
[x] InMemoryAuditLogStoreTest
[x] JdbcAuditLogStoreTest（需 postgres 专项）
[x] AuditLogServiceTest
[x] AuditLogControllerTest
[x] AuditLogIntegrationTest
[x] CandidateGenerationAuditIntegrationTest
[x] CandidateReviewAuditIntegrationTest
[x] DebugTokenFilterTest
```

---

# 七、Phase5-P0-E：Candidate / Review PostgreSQL Store

状态：`[x]`

任务：

```text
[x] JdbcCandidateStore
[x] JdbcCandidateReviewStore
[x] candidate generation 持久化
[x] experience candidate 持久化
[x] training candidate 持久化
[x] review record 持久化
[x] review 后 candidate review_status 更新事务
```

测试：

```text
[x] JdbcCandidateStoreTest（需 postgres 专项）
[x] JdbcCandidateReviewStoreTest（需 postgres 专项）
[x] PostgresCandidatePersistenceIntegrationTest
[x] PostgresReviewPersistenceIntegrationTest
```

---

# 八、Phase5-P0-F：Evaluation PostgreSQL Store

状态：`[x]`

任务：

```text
[x] JdbcEvaluationRunStore
[x] evaluation run 持久化
[x] evaluation item 持久化
[x] runtime case execution 持久化
[x] result snapshot 持久化
```

测试：

```text
[x] JdbcEvaluationRunStoreTest（需 postgres 专项）
[x] PostgresEvaluationPersistenceIntegrationTest
```

---

# 九、Phase5-P0-G：Runtime PostgreSQL Store

状态：`[x]`

任务：

```text
[x] JdbcRuntimeStore
[x] runtime session 持久化
[x] runtime trace 持久化
[x] operation record / state snapshot 持久化
[x] runtime start / continue 回归
```

测试：

```text
[x] JdbcRuntimeStoreTest（需 postgres 专项）
[x] PostgresRuntimePersistenceIntegrationTest
```

---

# 十、Phase5-P0-H：Postgres E2E 与人工验收

状态：`[x]`

任务：

```text
[x] Phase5PostgresEndToEndIntegrationTest
[x] persistence health API
[x] audit log API
[x] docs/Phase5_P0人工测试API结果.md
[x] README / docs 导航状态同步
[x] docs/Phase5_P0冻结记录.md
```

测试：

```text
[x] Phase5PostgresEndToEndIntegrationTest（需 RUN_POSTGRES_TESTS=true + Docker）
[x] InMemory 全量回归 369 项通过（含 postgres 专项 22 项，需 RUN_POSTGRES_TESTS=true + Docker）
```

---

# 十一、P0 完成定义

Phase5-P0 完成需要满足：

```text
1. P0-A 到 P0-H 核心实现已完成。
2. PostgreSQL schema / migration 可用。
3. Candidate / Review / Evaluation / Runtime 核心快照可持久化。
4. AuditLog 可记录关键操作。
5. in-memory / postgres 双模式可切换。
6. 不引入 RAG、模型训练、前端 Console、正式 RBAC。
7. 新增 Phase5_P0人工测试API结果.md。
8. 更新 AI_IMPLEMENTATION_SKILL.md，标记 Phase5-P0 完成或进入 freeze。
```

**当前状态：P0 完成定义已满足，Phase 5-P0 已冻结（commit 2ad656d）。**

---

# 十二、归档状态

```text
归档日期：2026-06-30
代码基线：commit 2ad656d
冻结记录：docs/Phase5_P0冻结记录.md
人工验收：docs/Phase5_P0人工测试API结果.md
测试结论：347 in-memory + 22 postgres = 369 项全绿
```

# 十三、后置任务

```text
[-] Phase5-P1：最小 Console API / 页面
[-] Phase5-P1：正式 RBAC / Debug token 强化
[-] Phase5-P2：ApprovedExperience 正式生效机制
[-] Phase5-P2：TrainingDatasetVersion 发布机制
[-] Phase5-P2：ModelRegistry / Training Job
[-] Phase5-P2：RAG / GraphRAG Provider
[-] Phase5-P2：Docker Compose / 部署运维
```

---

# 十四、当前下一步

```text
Phase 5-P0 已冻结（docs/Phase5_P0冻结记录.md，commit 2ad656d）。
后续可选：Phase 5-P1 规划（最小 Console / RBAC 强化）。
```
