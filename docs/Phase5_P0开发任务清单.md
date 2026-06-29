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

状态：`[ ]`

任务：

```text
[ ] 引入 PostgreSQL driver
[ ] 引入 Flyway
[ ] 配置 clinmind.persistence.mode
[ ] 配置 datasource / flyway
[ ] 本地 in-memory 默认不依赖数据库
[ ] postgres profile / 配置示例
```

测试：

```text
[ ] PersistenceModeConfigTest
[ ] ApplicationContextInMemoryTest
```

验收标准：

```text
1. 默认 in-memory 模式可启动。
2. postgres 模式需要显式配置。
3. 不影响 Phase1/2/3/4 回归。
```

---

# 四、Phase5-P0-B：Flyway Schema

状态：`[ ]`

任务：

```text
[ ] V5_0_0__phase5_p0_governance_schema.sql
[ ] runtime_sessions / runtime_traces
[ ] evaluation_runs / evaluation_items / runtime_case_executions
[ ] candidate_generations / experience_candidates / training_example_candidates
[ ] candidate_review_records
[ ] audit_logs
[ ] indexes
```

测试：

```text
[ ] FlywayMigrationTest
[ ] DatabaseSchemaSmokeTest
```

验收标准：

```text
1. schema 可在 PostgreSQL 中创建。
2. JSONB 字段可写入。
3. 关键索引存在。
```

---

# 五、Phase5-P0-C：Snapshot Mapper

状态：`[ ]`

任务：

```text
[ ] JsonSnapshotMapper
[ ] RuntimeSnapshotMapper
[ ] EvaluationSnapshotMapper
[ ] CandidateSnapshotMapper
[ ] ReviewSnapshotMapper
```

测试：

```text
[ ] RuntimeSnapshotMapperTest
[ ] EvaluationSnapshotMapperTest
[ ] CandidateSnapshotMapperTest
[ ] ReviewSnapshotMapperTest
```

验收标准：

```text
1. 核心对象可序列化为 JSON。
2. JSON 可反序列化或至少可作为快照安全读取。
3. 不丢失 id / status / source_ref / asset version / sanitization_status。
```

---

# 六、Phase5-P0-D：AuditLog 基础能力

状态：`[ ]`

任务：

```text
[ ] AuditLogRecord
[ ] AuditActionType
[ ] AuditResourceType
[ ] AuditResultStatus
[ ] AuditLogStore interface
[ ] InMemoryAuditLogStore
[ ] JdbcAuditLogStore
[ ] AuditLogService
[ ] AuditLogController
```

测试：

```text
[ ] AuditLogRecordTest
[ ] InMemoryAuditLogStoreTest
[ ] JdbcAuditLogStoreTest
[ ] AuditLogServiceTest
[ ] AuditLogControllerTest
```

验收标准：

```text
1. 关键操作可写 AuditLog。
2. AuditLog 可按 id 查询。
3. AuditLog 不保存未脱敏患者原文。
```

---

# 七、Phase5-P0-E：Candidate / Review PostgreSQL Store

状态：`[ ]`

任务：

```text
[ ] JdbcCandidateStore
[ ] JdbcCandidateReviewStore
[ ] candidate generation 持久化
[ ] experience candidate 持久化
[ ] training candidate 持久化
[ ] review record 持久化
[ ] review 后 candidate review_status 更新事务
```

测试：

```text
[ ] JdbcCandidateStoreTest
[ ] JdbcCandidateReviewStoreTest
[ ] PostgresCandidatePersistenceIntegrationTest
[ ] PostgresReviewPersistenceIntegrationTest
```

验收标准：

```text
1. 生成 Candidate 后可从 PostgreSQL 查询。
2. Review 后可从 PostgreSQL 查询 review record。
3. Review status 更新与 review record 保存具备一致性。
4. in-memory CandidateStore 回归仍通过。
```

---

# 八、Phase5-P0-F：Evaluation PostgreSQL Store

状态：`[ ]`

任务：

```text
[ ] JdbcEvaluationRunStore
[ ] evaluation run 持久化
[ ] evaluation item 持久化
[ ] runtime case execution 持久化
[ ] result snapshot 持久化
```

测试：

```text
[ ] JdbcEvaluationRunStoreTest
[ ] PostgresEvaluationPersistenceIntegrationTest
```

验收标准：

```text
1. EvaluationRun 运行后可从 PostgreSQL 查询。
2. EvaluationResult / item / execution 快照完整。
3. Candidate generation 可读取已持久化 EvaluationRun。
```

---

# 九、Phase5-P0-G：Runtime PostgreSQL Store

状态：`[ ]`

任务：

```text
[ ] JdbcRuntimeStore
[ ] runtime session 持久化
[ ] runtime trace 持久化
[ ] operation record / state snapshot 持久化
[ ] runtime start / continue 回归
```

测试：

```text
[ ] JdbcRuntimeStoreTest
[ ] PostgresRuntimePersistenceIntegrationTest
```

验收标准：

```text
1. Runtime start 后可从 PostgreSQL 查询 runtime session。
2. Trace 可持久化查询。
3. Runtime 行为不因持久化改变。
```

---

# 十、Phase5-P0-H：Postgres E2E 与人工验收

状态：`[ ]`

任务：

```text
[ ] Phase5PostgresEndToEndIntegrationTest
[ ] persistence health API
[ ] audit log API
[ ] docs/Phase5_P0人工测试API结果.md
[ ] README / docs 导航状态同步
```

测试：

```text
[ ] Phase5PostgresEndToEndIntegrationTest
[ ] InMemoryFullRegressionTest 或既有全量回归
```

验收标准：

```text
1. postgres 模式完整链路通过。
2. in-memory 模式全量回归通过。
3. AuditLog 可查询关键操作。
4. 人工验收记录补齐。
```

---

# 十一、P0 完成定义

Phase5-P0 完成需要满足：

```text
1. P0-A 到 P0-H 全部完成。
2. PostgreSQL schema / migration 可用。
3. Candidate / Review / Evaluation / Runtime 至少核心快照可持久化。
4. AuditLog 可记录关键操作。
5. in-memory / postgres 双模式可切换。
6. 不引入 RAG、模型训练、前端 Console、正式 RBAC。
7. 新增 Phase5_P0人工测试API结果.md。
8. 更新 AI_IMPLEMENTATION_SKILL.md，标记 Phase5-P0 完成或进入 freeze。
```

---

# 十二、后置任务

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

# 十三、当前下一步

当前下一步：

```text
Phase5-P0-A：依赖与配置
```

开始实现前必须：

```text
1. 将 Phase5-P0-A 状态从 [ ] 改为 [/]。
2. 只引入 PostgreSQL / Flyway 依赖、配置项和启动 profile。
3. 不直接实现所有 Store。
4. 不引入前端、RAG、模型训练或正式 RBAC。
```
