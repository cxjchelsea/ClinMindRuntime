# AI Implementation Skill：ClinMindRuntime Phase 5-P0

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1-P0 Runtime MVP、Phase 2-P0 共享能力资产原型、Phase 3-P0 训练与评估闭环 MVP、Phase 4-P0 候选沉淀机制、Phase 4-P1 候选治理与安全加固、Phase 5-P0 持久化与治理底座均已完成并冻结。  
> 后续修改不得破坏 Runtime 主控、安全门、输出边界、Provider 抽象、资产版本追踪、Evaluation 闭环、Candidate 脱敏、SourceRef 校验、Review 记录边界、持久化双模式和患者端隔离。

---

# 一、当前项目阶段

```text
当前阶段：Phase 5-P0 持久化与治理底座 — 已冻结
下一步：Phase 5-P1 规划（最小 Console / RBAC 强化）
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP，已完成
Phase 2-P0：共享能力资产原型，已完成
Phase 3-P0：训练与评估闭环 MVP，已冻结
Phase 4-P0：候选沉淀机制 + debug API，已冻结
Phase 4-P1：候选治理与安全加固，已冻结
Phase 5-P0：持久化与治理底座，已冻结
```

Phase 5-P0 目标：

```text
为 Runtime、Evaluation、Candidate、Review 和 AuditLog 建立 PostgreSQL 持久化与最小治理底座，同时保持业务行为不因持久化而改变。
```

Phase 5-P0 推荐链路：

```text
RuntimeService / EvaluationRunner / CandidateGenerationService / CandidateReviewService
→ Repository / Store Interface
→ InMemory implementation（保留）
→ PostgreSQL implementation（新增）
→ AuditLogService
→ Debug API 查询与回归测试
```

重要说明：

```text
Phase 5-P0 不是 RAG 阶段。
Phase 5-P0 不是模型训练阶段。
Phase 5-P0 不是前端 Console 阶段。
Phase 5-P0 不是正式 RBAC / 医生审核平台。
Phase 5-P0 只做 PostgreSQL 持久化、Repository 双实现、AuditLog 最小治理和持久化回归。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase5_P0开发任务清单.md
3. docs/Phase5_P0持久化与治理底座_实现规格.md
4. docs/Phase5_P0数据库Schema设计.md
5. docs/Phase5_P0Repository迁移设计.md
6. docs/Phase5_P0审计与权限边界设计.md
7. docs/Phase5_P0_API与测试设计.md
8. docs/Phase4_P1冻结记录.md
9. docs/Phase4_P1人工测试API结果.md
10. docs/Phase4_P0冻结记录.md
11. docs/Phase3_P0冻结记录.md
12. docs/README.md
13. docs/项目展示导读.md
14. docs/架构文档缺口审查清单.md
15. docs/ClinMindRuntime技术实现总方案.md
16. docs/测试与CI总方案.md
17. docs/数据安全与合规边界规划.md
18. docs/数据库持久化设计.md
19. docs/平台前端与Console规划.md
20. docs/部署与运维规划.md
21. docs/模型训练与后训练规划.md
22. docs/医学知识库与RAG构建规划.md
23. docs/ClinMindRuntime阶段拆分路线图.md
24. docs/ClinMindRuntime完整系统设计.md
25. docs/架构模式与设计模式说明.md
```

解释：

```text
Phase 5-P0 文档优先指导当前新增能力。
docs/Phase4_P1冻结记录.md 是 P1 冻结边界依据。
docs/数据库持久化设计.md 是长期持久化规划，Phase5_P0 文档是当前实现规格。
docs/平台前端与Console规划.md 只能指导后续，不是 P0 实现前端的理由。
```

---

# 三、当前允许实现的内容

当前只允许按 Phase5-P0-A 到 Phase5-P0-H 顺序推进。

## 3.1 Phase5-P0-A：依赖与配置

```text
PostgreSQL driver
Flyway
clinmind.persistence.mode
postgres profile / datasource / flyway 配置
in-memory 默认启动保护
```

## 3.2 Phase5-P0-B：Flyway Schema

```text
runtime_sessions / runtime_traces
evaluation_runs / evaluation_items / runtime_case_executions
candidate_generations / experience_candidates / training_example_candidates
candidate_review_records
audit_logs
```

## 3.3 Phase5-P0-C：Snapshot Mapper

```text
JsonSnapshotMapper
RuntimeSnapshotMapper
EvaluationSnapshotMapper
CandidateSnapshotMapper
ReviewSnapshotMapper
```

## 3.4 Phase5-P0-D：AuditLog 基础能力

```text
AuditLogRecord
AuditLogStore
InMemoryAuditLogStore
JdbcAuditLogStore
AuditLogService
AuditLogController
```

## 3.5 Phase5-P0-E：Candidate / Review PostgreSQL Store

```text
JdbcCandidateStore
JdbcCandidateReviewStore
candidate / review postgres persistence
review status update transaction
```

## 3.6 Phase5-P0-F：Evaluation PostgreSQL Store

```text
JdbcEvaluationRunStore
evaluation run / item / execution / result snapshot persistence
```

## 3.7 Phase5-P0-G：Runtime PostgreSQL Store

```text
JdbcRuntimeStore
runtime session / trace / state snapshot persistence
```

## 3.8 Phase5-P0-H：Postgres E2E 与人工验收

```text
Phase5PostgresEndToEndIntegrationTest
persistence health API
audit log API
docs/Phase5_P0人工测试API结果.md
```

---

# 四、当前禁止做的事情

```text
1. 不新增真实 RAG / GraphRAG。
2. 不接 Python AI Provider。
3. 不接 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
4. 不训练基础大模型。
5. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
6. 不做前端 Training Center / Runtime Console。
7. 不做正式 RBAC / 登录系统 / 多租户。
8. 不做正式医生审核平台。
9. 不自动上线 ApprovedExperience。
10. 不发布 TrainingDatasetVersion。
11. 不自动修改 AssetPackage / CapabilityProfile。
12. 不改变患者端输出边界。
13. 不绕过 SafetyGate 或 DecisionBoundary。
14. 不删除 InMemory 实现。
15. 不一次性重构所有 Store，必须按任务清单逐步迁移。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 5-P0 持久化与治理底座。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、Phase 5-P0 架构约束

```text
1. RuntimeService 仍然是 Runtime 主控入口。
2. 持久化层只能保存状态，不能改变 Runtime 决策。
3. Service 只能依赖 Store / Repository interface，不感知 in-memory 或 postgres 实现。
4. InMemory 实现必须保留，用于本地开发和回归测试。
5. PostgreSQL 实现必须通过 clinmind.persistence.mode=postgres 显式启用。
6. CandidateSanitizer 必须在 TrainingExampleCandidate 持久化前生效。
7. Candidate review_status 即使为 APPROVED，也不代表 Runtime 可用。
8. AuditLog 不得保存未脱敏患者原文。
9. Debug API 不得因持久化而扩大数据暴露范围。
10. 所有改动必须保持 Phase1/2/3/4 回归通过。
```

---

# 六、任务清单同步规则

每次实现 Phase 5-P0 代码前，必须同步更新：

```text
docs/Phase5_P0开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase5_P0开发任务清单.md。
2. 确认当前任务属于 Phase5-P0-A 到 Phase5-P0-H 的哪一项。
3. 将正在处理的任务从 [ ] 改为 [/]。
4. 如果任务不在清单中，先补清单，不要直接实现。
```

实现后：

```text
1. 将已完成任务从 [/] 改为 [x]。
2. 如果任务部分完成，保持 [/] 并说明原因。
3. 如果任务被阻塞，将状态改为 [!] 并说明原因。
```

---

# 七、测试约束

Phase 5-P0 必须同时保护 in-memory 回归和 postgres 持久化专项测试。

至少包含：

```text
FlywayMigrationTest
DatabaseSchemaSmokeTest
JsonSnapshotMapperTest
RuntimeSnapshotMapperTest
EvaluationSnapshotMapperTest
ReviewSnapshotMapperTest
JdbcCandidateStoreTest
JdbcCandidateReviewStoreTest
JdbcEvaluationRunStoreTest
JdbcRuntimeStoreTest
JdbcAuditLogStoreTest
AuditLogRecordTest
InMemoryAuditLogStoreTest
AuditLogServiceTest
AuditLogControllerTest
AuditLogIntegrationTest
CandidateGenerationAuditIntegrationTest
CandidateReviewAuditIntegrationTest
DebugTokenFilterTest
PostgresRuntimePersistenceIntegrationTest
PostgresEvaluationPersistenceIntegrationTest
PostgresCandidatePersistenceIntegrationTest
PostgresReviewPersistenceIntegrationTest
Phase5PostgresEndToEndIntegrationTest
```

每次 Phase 5-P0 改动后，必须尽量保持：

```text
Phase 1 Runtime 回归通过。
Phase 2 Asset Provider 回归通过。
Phase 3 Evaluation 回归通过。
Phase 4 Candidate / Review 回归通过。
in-memory 模式可启动。
postgres 模式专项测试通过。
```

如果无法实际运行测试，必须在回复或提交说明中明确：

```text
未在本地运行测试，仅完成代码 / 文档修改。
```

---

# 八、当前最优下一步

当前最优实现任务是：

```text
Phase 5-P1 规划（不在 P0 范围）
```

P0 已完成，后续不应再向 Phase 5-P0 堆新能力。若需继续开发，应：

```text
1. 读取 docs/Phase5_P0冻结记录.md 确认边界。
2. 规划 Phase5-P1（最小 Console API / RBAC 强化）。
3. 保持 in-memory 与 postgres 双模式回归通过。
```

---

# 九、最终约束

```text
当前不是在实现完整产品化平台。
当前不是在实现模型训练平台。
Phase 5-P0 持久化与治理底座已冻结。
Phase 5-P0 的目标是让已有 Runtime / Evaluation / Candidate / Review 治理对象可持久化、可审计、可恢复，但不改变 AI 决策边界。
```
