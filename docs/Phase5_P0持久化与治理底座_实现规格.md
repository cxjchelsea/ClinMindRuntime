# Phase 5-P0：持久化与治理底座实现规格

> 本文档定义 ClinMindRuntime Phase 5-P0 的目标、边界、主流程和实现范围。  
> Phase 5-P0 不做 RAG、模型训练、前端 Console 或正式临床审核平台，而是把 Phase 1–4 已形成的 Runtime / Evaluation / Candidate / Review 核心治理对象从 in-memory/debug 原型推进到可持久化、可审计、可恢复的工程底座。

---

# 一、Phase 5-P0 定位

Phase 1–4 已经完成：

```text
Phase 1：Runtime 能安全运行。
Phase 2：Asset Provider 能注入能力资产并追踪版本。
Phase 3：Evaluation 能评估 Runtime，并生成 CapabilityProfileUpdateProposal。
Phase 4-P0：Evaluation 结果能沉淀为 ExperienceCandidate / TrainingExampleCandidate。
Phase 4-P1：Candidate 具备脱敏、SourceRef 强校验和最小 Review 记录能力。
```

Phase 5-P0 要解决的问题是：

```text
1. Runtime / Trace / Evaluation / Candidate / Review 不能只存在内存中。
2. Debug API 的关键操作需要进入 AuditLog。
3. 持久化不能破坏 Runtime 主控、安全门和患者端输出边界。
4. Repository / Store 需要从 in-memory 抽象到可替换 PostgreSQL 实现。
5. Phase 5 后续的 Console、RBAC、ApprovedExperience、TrainingDatasetVersion 必须有底层数据基础。
```

Phase 5-P0 的一句话目标：

```text
为 Runtime、Evaluation、Candidate、Review 和 AuditLog 建立 PostgreSQL 持久化与最小治理底座，同时保持业务行为不因持久化而改变。
```

---

# 二、Phase 5-P0 主链路

Phase 5-P0 主链路：

```text
RuntimeService / EvaluationRunner / CandidateGenerationService / CandidateReviewService
→ Repository / Store Interface
→ InMemory implementation（保留测试与 fallback）
→ PostgreSQL implementation（P0 新增）
→ AuditLogService
→ Debug API 查询与回归测试
```

关键原则：

```text
1. 先持久化已有治理对象，不新增 AI 能力。
2. 先建立 Repository 双实现，不一次性替换所有调用点。
3. 先保证读写一致和回归通过，再考虑 Console / RBAC。
4. 持久化层只能保存状态，不能改变 Runtime 决策。
```

---

# 三、Phase 5-P0 不是什么

Phase 5-P0 不是：

```text
1. 不是前端 Console。
2. 不是正式 RBAC 权限系统。
3. 不是正式医生审核平台。
4. 不是 ApprovedExperience 自动生效机制。
5. 不是 TrainingDatasetVersion 发布机制。
6. 不是 RAG / GraphRAG。
7. 不是 Python AI Provider。
8. 不是模型训练 / 后训练。
9. 不是生产部署完整方案。
10. 不是把 debug API 直接产品化。
```

Phase 5-P0 只做：

```text
PostgreSQL schema
Repository / Store 持久化实现
AuditLog 最小审计链
配置化切换 in-memory / postgres
数据迁移脚本
持久化回归测试
```

---

# 四、Phase 5-P0 持久化对象

P0 优先持久化：

```text
1. RuntimeState summary / RuntimeTrace / RuntimeOperationRecord
2. EvaluationRun / EvaluationItemResult / EvaluationResult / RuntimeCaseExecution
3. CandidateGenerationResult / ExperienceCandidate / TrainingExampleCandidate
4. CandidateReviewRecord
5. AuditLogRecord
```

P0 暂不持久化或只做引用记录：

```text
1. YAML AssetPackage 正文（仍由 asset package 文件管理）
2. 正式 ExperienceMemory（后置）
3. TrainingDatasetVersion（后置）
4. ModelRegistry / ModelProviderVersion（后置）
5. 用户账号 / 组织 / 多租户（后置）
```

---

# 五、技术选型

Phase 5-P0 推荐选型：

```text
Database：PostgreSQL
Migration：Flyway
Persistence access：Spring JDBC / JdbcTemplate（P0 推荐）或 Spring Data JDBC
Test DB：Testcontainers PostgreSQL 或 H2 compatibility fallback
Serialization：JSONB 存储复杂结构快照
```

P0 推荐 `JdbcTemplate + JSONB`，原因：

```text
1. 当前核心对象很多是 record / DTO / Map / List，完整 ORM 映射成本高。
2. P0 的目标是治理对象可持久化，不是复杂关系查询。
3. JSONB 可以保留 RuntimeTrace、metric breakdown、candidate input 等半结构化快照。
4. 后续 Console 需要查询时，再逐步把高频字段列化。
```

---

# 六、Repository / Store 迁移策略

Phase 5-P0 不应一次性把所有 in-memory store 删除。

推荐策略：

```text
1. 保留现有 InMemory 实现，用于单元测试和本地快速启动。
2. 新增 PostgreSQL 实现。
3. 使用配置切换：clinmind.persistence.mode=in-memory | postgres。
4. 默认仍可 in-memory 启动，postgres 作为 P0 验收模式。
5. 逐个迁移 Store：RuntimeStore → EvaluationRunStore → CandidateStore → CandidateReviewStore → AuditLogStore。
```

---

# 七、AuditLog 最小治理链

Phase 5-P0 需要新增最小审计能力。

审计对象：

```text
1. 创建 Runtime。
2. 继续 Runtime。
3. 创建 EvaluationRun。
4. 生成 CandidateGenerationResult。
5. Review Candidate。
6. 查询敏感 debug API（P0 可选）。
```

AuditLog 只记录：

```text
action_type
resource_type
resource_id
actor
request_id
result_status
timestamp
metadata
```

P0 不做：

```text
正式 RBAC
多租户
用户登录
权限审批
复杂合规报表
```

---

# 八、数据安全边界

Phase 5-P0 持久化必须遵守：

```text
1. 患者端输出边界不因持久化改变。
2. CandidateSanitizer 仍在 TrainingExampleCandidate 持久化前生效。
3. Debug API 不新增未脱敏数据返回。
4. RuntimeTrace / Candidate input / Review metadata 使用 JSONB 保存时必须保留 source_ref 与 sanitization_status。
5. 真实患者输入接入前必须继续遵守 docs/数据安全与合规边界规划.md。
```

---

# 九、Phase 5-P0 API 边界

P0 可新增 internal/debug 管理 API：

```http
GET /api/v1/debug/persistence/health
GET /api/v1/debug/audit-logs
GET /api/v1/debug/audit-logs/{audit_id}
```

P0 不新增：

```text
前端 Console API
用户登录 API
权限管理 API
正式医生审核 API
TrainingDataset 发布 API
ApprovedExperience 生效 API
```

---

# 十、Phase 5-P0 完成标准

Phase 5-P0 完成需要满足：

```text
1. PostgreSQL schema 与 Flyway migration 建立。
2. Runtime / Evaluation / Candidate / Review 至少核心快照可持久化。
3. Store / Repository 支持 in-memory 与 postgres 双实现。
4. Candidate review 与 candidate generation 经过 postgres 模式后仍可查询。
5. AuditLog 可记录关键 debug 操作。
6. Phase 1 / 2 / 3 / 4 回归测试通过。
7. 新增 Phase5_P0人工测试API结果.md。
8. 不引入 RAG、模型训练、前端 Console 或正式 RBAC。
```

---

# 十一、最终结论

Phase 5-P0 的核心不是继续增强 AI，而是增强工程治理：

```text
从 in-memory prototype
→ PostgreSQL-backed governance runtime
```

它为后续 Console、正式审核、ApprovedExperience、TrainingDatasetVersion、模型训练和部署运维打基础，但 P0 自身不实现这些后置能力。
