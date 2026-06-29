# Phase 5 开发任务清单

> 本清单用于约束 Phase 5-P0 的实现顺序。  
> Phase 5-P0 只做 PostgreSQL 持久化、Repository 双实现、Flyway migration、最小 AuditLog 与回归测试，不实现 RAG、模型训练、前端 Console、正式医生审核平台或自动上线。

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
把 Phase1–4 已经形成的 Runtime / Evaluation / Candidate / Review 核心治理对象，从 in-memory/debug 原型推进到 PostgreSQL 持久化、可迁移、可审计、可回归的工程底座。
```

P0 必须保证：

```text
1. memory mode 保持可用。
2. postgres mode 可通过配置启用。
3. Flyway migration 能创建核心表。
4. Candidate / Review 至少完成 PostgreSQL 持久化。
5. Evaluation / RuntimeTrace 具备最小持久化能力。
6. Candidate review 等治理动作能写 AuditLog。
7. 不引入 RAG / Python Provider / 模型训练 / 前端 Console。
```

---

# 三、Phase5-P0-A：依赖与配置

状态：`[ ]`

任务：

```text
[ ] 添加 PostgreSQL driver
[ ] 添加 Flyway
[ ] 添加 Testcontainers PostgreSQL（test scope）
[ ] 新增 clinmind.persistence.mode 配置
[ ] 新增 application-postgres.yml / application-it.yml
[ ] 保持默认 memory mode
```

测试：

```text
[ ] PersistenceModeConfigTest
```

验收标准：

```text
1. 默认启动不需要数据库。
2. postgres mode 可加载 datasource 配置。
3. memory mode 现有测试不受影响。
```

---

# 四、Phase5-P0-B：Flyway Schema Migration

状态：`[ ]`

任务：

```text
[ ] V1__phase5_core_persistence.sql
[ ] runtime_sessions / runtime_states / runtime_traces
[ ] evaluation_runs / evaluation_item_results / runtime_case_executions
[ ] experience_candidates / training_example_candidates
[ ] candidate_review_records
[ ] capability_profile_proposals
[ ] audit_logs
[ ] 核心索引
```

测试：

```text
[ ] PostgresSchemaMigrationTest
```

验收标准：

```text
1. 空库 migration 成功。
2. 核心表存在。
3. JSONB 默认值可写入。
4. 索引存在。
```

---

# 五、Phase5-P0-C：Candidate / Review PostgreSQL Store

状态：`[ ]`

任务：

```text
[ ] PostgresCandidateStore
[ ] PostgresCandidateReviewStore
[ ] Candidate JSONB serializer / mapper
[ ] ExperienceCandidate save / query / update
[ ] TrainingExampleCandidate save / query / update
[ ] CandidateReviewRecord save / query
[ ] memory / postgres bean 切换
```

测试：

```text
[ ] PostgresCandidateStoreTest
[ ] PostgresCandidateReviewStoreTest
```

验收标准：

```text
1. generation result 可保存和查询。
2. candidate 可保存、查询、更新 review_status。
3. review record 可保存和查询。
4. API response 不因 store 切换而改变。
```

---

# 六、Phase5-P0-D：AuditLog 最小治理链路

状态：`[ ]`

任务：

```text
[ ] AuditLog
[ ] AuditEventType
[ ] AuditResourceType
[ ] AuditLogStore
[ ] PostgresAuditLogStore
[ ] AuditService
[ ] CandidateGenerationService 写 audit（可选 P0）
[ ] CandidateReviewService 写 audit（P0 必做）
[ ] CapabilityProfileProposalService 写 audit（可选 P0）
```

测试：

```text
[ ] AuditServiceTest
[ ] PostgresAuditLogStoreTest
[ ] CandidateReviewAuditIntegrationTest
```

验收标准：

```text
1. Candidate review approve / reject 会写 audit log。
2. audit log 绑定 actor / action / resource / reason。
3. audit log 不保存完整敏感 payload。
4. 审计失败有明确错误。
```

---

# 七、Phase5-P0-E：Evaluation PostgreSQL Store

状态：`[ ]`

任务：

```text
[ ] PostgresEvaluationRunStore
[ ] EvaluationRun save / get
[ ] EvaluationItemResult save / get
[ ] EvaluationResult save / get
[ ] RuntimeCaseExecution save / get
[ ] Evaluation JSONB serializer / mapper
```

测试：

```text
[ ] PostgresEvaluationRunStoreTest
[ ] PostgresEvaluationCandidateIntegrationTest
```

验收标准：

```text
1. EvaluationRun 完成后可持久化。
2. EvaluationResult 可重新查询。
3. RuntimeCaseExecution 可用于 Candidate generation。
4. 现有 Evaluation API 在 postgres mode 下可跑通。
```

---

# 八、Phase5-P0-F：Runtime / Trace 最小持久化

状态：`[ ]`

任务：

```text
[ ] PostgresRuntimeStore
[ ] runtime_sessions 保存 / 查询
[ ] runtime_states 保存 / 更新
[ ] runtime_traces append / query
[ ] RuntimeState JSONB serializer / mapper
[ ] Trace 不保存完整 raw patient input
```

测试：

```text
[ ] PostgresRuntimeStoreTest
[ ] PostgresRuntimeTracePersistenceIntegrationTest
```

验收标准：

```text
1. Runtime start 后 session/state 可保存。
2. RuntimeTrace 可查询。
3. Trace 中有 asset refs。
4. Trace 不保存未脱敏完整 patient input。
```

---

# 九、Phase5-P0-G：端到端验收与文档归档

状态：`[ ]`

任务：

```text
[ ] Postgres mode E2E：Evaluation → Candidate → Review → AuditLog
[ ] memory mode 回归
[ ] docs/Phase5_人工测试API结果.md
[ ] 更新 docs/AI_IMPLEMENTATION_SKILL.md
[ ] 更新 docs/README.md
[ ] 更新 docs/架构文档缺口审查清单.md
```

测试：

```text
[ ] PostgresPersistenceEndToEndIntegrationTest
[ ] 全量 mvn test
```

验收标准：

```text
1. postgres mode 主链路可跑通。
2. memory mode 回归通过。
3. Phase1/2/3/4 已冻结能力不被破坏。
4. 人工验收记录完整。
```

---

# 十、P0 完成定义

Phase5-P0 完成需要满足：

```text
1. P0-A 到 P0-G 全部完成。
2. PostgreSQL schema 和 Flyway migration 可用。
3. Candidate / Review / Evaluation / RuntimeTrace 至少有 PostgreSQL 持久化能力。
4. AuditLog 能记录 review 等治理动作。
5. memory / postgres 两种模式可切换。
6. 全量回归测试通过。
7. 不引入 RAG / 模型训练 / 前端 / 正式 RBAC。
8. 新增 Phase5_人工测试API结果.md。
```

---

# 十一、后置任务

## Phase5-P1 后置

```text
[-] Spring Security / JWT / RBAC
[-] Audit query API / Admin API
[-] Frontend Console 最小页面
[-] Asset publish / rollback governance
[-] CapabilityProfile approval workflow
```

## Phase5-P2 / Phase6 后置

```text
[-] TrainingDatasetVersion 发布
[-] ApprovedExperience 正式生效
[-] pgvector / RAG Provider
[-] Python AI Provider
[-] Model Registry
[-] MCP / Agent SDK 接入
```

---

# 十二、当前下一步

当前下一步：

```text
Phase5-P0-A：依赖与配置
```

开始实现前必须：

```text
1. 将 Phase5-P0-A 状态从 [ ] 改为 [/]。
2. 只实现依赖、配置和最小配置测试。
3. 不直接实现 Store / Schema / Audit / API。
```
