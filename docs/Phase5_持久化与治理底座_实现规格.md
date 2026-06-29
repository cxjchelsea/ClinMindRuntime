# Phase 5：持久化与治理底座实现规格

> 本文档定义 ClinMindRuntime Phase 5-P0 的目标、边界和实现范围。  
> Phase 5-P0 不是继续扩展 AI 能力，也不是直接做 RAG / 模型训练 / 前端大平台，而是把 Phase 1–4 已经形成的 Runtime、Evaluation、Candidate、Review 等核心治理对象从 in-memory / debug 原型推进到可持久化、可审计、可迁移、可回归的工程底座。

---

# 一、Phase 5 定位

Phase 1–4 已经完成：

```text
Phase 1：Runtime 能安全运行。
Phase 2：Asset Provider 能注入版本化资产。
Phase 3：Evaluation 能评估 Runtime 并生成 CapabilityProfile proposal。
Phase 4-P0：Evaluation 结果能沉淀为 ExperienceCandidate / TrainingExampleCandidate。
Phase 4-P1：Candidate 具备脱敏、SourceRef 强校验和 Review 记录。
```

Phase 5 要解决的问题是：

```text
1. Runtime / Trace / Evaluation / Candidate / Review 是否能跨进程保存？
2. 核心治理对象是否有稳定 schema 和 migration？
3. debug API 访问和 review 行为是否能被审计？
4. in-memory store 如何平滑替换为 PostgreSQL store？
5. 如何保证数据库不会绕过 Runtime 主控、Asset 边界和 Candidate review 边界？
```

Phase 5-P0 的一句话目标：

```text
建立 PostgreSQL 持久化 + Flyway migration + Repository 双实现 + AuditLog 的最小治理底座，让已冻结的 Runtime / Evaluation / Candidate / Review 主线具备可保存、可查询、可审计、可回归能力。
```

---

# 二、Phase 5-P0 主链路

Phase 5-P0 主链路：

```text
RuntimeService / RuntimeStore
→ RuntimePersistencePort
→ InMemoryRuntimeStore / PostgresRuntimeStore
→ runtime_sessions / runtime_states / runtime_traces

RuntimeEvaluationRunner / EvaluationRunStore
→ EvaluationPersistencePort
→ InMemoryEvaluationRunStore / PostgresEvaluationRunStore
→ evaluation_runs / evaluation_item_results / runtime_case_executions

CandidateGenerationService / CandidateStore / CandidateReviewStore
→ CandidatePersistencePort
→ InMemoryCandidateStore / PostgresCandidateStore
→ experience_candidates / training_example_candidates / candidate_review_records

Debug / Governance API
→ AuditService
→ audit_logs
```

---

# 三、Phase 5-P0 不是什么

Phase 5-P0 不是：

```text
1. 不是正式医疗合规系统。
2. 不是完整 RBAC / IAM 平台。
3. 不是前端 Console。
4. 不是真实 RAG / GraphRAG。
5. 不是 Python AI Provider。
6. 不是模型训练 / 后训练。
7. 不是 ApprovedExperience 自动生效。
8. 不是 TrainingDatasetVersion 正式发布。
9. 不是自动修改 AssetPackage / CapabilityProfile。
10. 不是把数据库变成 Runtime 主控。
```

Phase 5-P0 只做：

```text
PostgreSQL schema
Flyway migration
Repository / Store 双实现
JSONB 持久化核心对象
最小 AuditLog
最小 API 访问边界
回归测试和数据一致性测试
```

---

# 四、Phase 5-P0 核心能力

## 4.1 PostgreSQL + Flyway

P0 引入：

```text
PostgreSQL
Flyway
JDBC / JdbcTemplate 或 Spring Data JDBC
Testcontainers PostgreSQL（测试）
```

P0 不引入：

```text
pgvector
Redis
Neo4j
Milvus / Qdrant
复杂 ORM 领域模型映射
```

原因：Phase 5-P0 的目标是治理对象持久化，不是检索增强和模型平台。

## 4.2 Runtime 持久化

优先持久化：

```text
runtime_sessions
runtime_states
runtime_traces
```

原则：

```text
1. RuntimeService 仍是主控。
2. 数据库只保存状态，不驱动流程。
3. RuntimeState 可先以 JSONB 保存。
4. RuntimeTrace 必须保留 asset_package_id / asset_package_version / trace_id。
5. 原始用户输入不直接明文入库，保存脱敏摘要或 hash。
```

## 4.3 Evaluation 持久化

优先持久化：

```text
evaluation_runs
evaluation_item_results
runtime_case_executions
capability_profile_proposals
```

原则：

```text
1. Evaluation 仍通过 RuntimeService 执行。
2. EvaluationResult 与 case_set_version、asset_package_version 绑定。
3. 不允许数据库结果反向修改 CapabilityProfile。
4. Proposal 仍然只是 proposal。
```

## 4.4 Candidate / Review 持久化

优先持久化：

```text
experience_candidates
training_example_candidates
candidate_review_records
```

原则：

```text
1. Candidate 仍默认 REVIEW_REQUIRED。
2. TrainingExampleCandidate.input 必须是 sanitizer 处理后的版本。
3. ReviewRecord 只记录人工决策，不触发自动生效。
4. APPROVED candidate 不自动进入 Runtime / Asset / TrainingDataset。
```

## 4.5 AuditLog

P0 引入最小审计：

```text
audit_logs
AuditService
AuditEventType
AuditResourceType
```

P0 审计范围：

```text
1. EvaluationRun 创建。
2. Candidate generation 创建。
3. Candidate review approve / reject / deprecate。
4. Candidate / Review 查询（可选，P0 可只记录写操作）。
5. CapabilityProfile proposal 生成。
```

P0 不实现完整 RBAC，但必须为后续 RBAC 留 actor_id / actor_role / request_id / reason。

---

# 五、持久化切换策略

Phase 5-P0 不应一次性删除 in-memory 实现。

推荐配置：

```text
clinmind.persistence.mode=memory | postgres
```

默认：

```text
memory
```

P0 实现策略：

```text
1. 保留 InMemoryRuntimeStore / InMemoryEvaluationRunStore / InMemoryCandidateStore。
2. 新增 PostgreSQL 实现。
3. 通过 Spring profile 或 property 切换。
4. 回归测试仍覆盖 memory。
5. 新增 integration test 覆盖 postgres。
```

---

# 六、数据安全边界

Phase 5-P0 必须遵守：

```text
docs/数据安全与合规边界规划.md
docs/Phase4_P1冻结记录.md
docs/数据库持久化设计.md
```

安全原则：

```text
1. 不把真实患者原始文本直接写入训练候选表。
2. RuntimeTrace 入库应保存 summary / sanitized / asset refs。
3. Candidate input 只能保存 CandidateSanitizer 输出。
4. AuditLog 不保存完整敏感 payload。
5. Debug API 仍不面向 patient-facing client。
```

---

# 七、P0 成功标准

Phase 5-P0 完成需要满足：

```text
1. Flyway migration 可创建核心表。
2. PostgreSQL store 能保存 / 查询 Runtime、Evaluation、Candidate、Review。
3. memory 与 postgres 两种模式可切换。
4. Candidate review 会写入 audit_logs。
5. Evaluation / Candidate / Review 的既有 API 在 postgres 模式下可跑通。
6. Phase1/2/3/4 回归测试仍通过。
7. PostgreSQL integration test 通过。
8. 不引入 RAG / Python Provider / 模型训练 / 前端 Console。
```

---

# 八、最终结论

Phase 5-P0 的价值不是“功能更多”，而是让前面四个阶段形成的治理对象真正具备工程系统能力：

```text
可保存
可恢复
可审计
可迁移
可回归
仍受 Runtime 主控约束
```

Phase 5-P0 完成后，项目才真正从内存原型走向可展示的系统工程底座。
