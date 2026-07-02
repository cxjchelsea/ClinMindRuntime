# Phase 5-P0 冻结记录

> 本文档用于记录 ClinMindRuntime Phase 5-P0 的冻结状态、冻结依据、当前边界、已知限制和后续进入 Phase 5-P1 前的检查项。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 5-P0 的持久化与治理底座 MVP 已完成，后续不再继续向 Phase 5-P0 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 5-P0
冻结状态：已冻结
当前项目阶段：Phase 5-P0 已冻结（后续 Phase 5-P1/P2 均已完成，见各自冻结/验收记录）
冻结日期：2026-06-30
代码基线：commit 2ad656d
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

## 2.2 自动化测试依据

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

冻结时全量 `mvn test`（`RUN_POSTGRES_TESTS=true`）：**369 项全绿**（Java 21）。

## 2.3 人工验收依据

```text
docs/Phase5_P0人工测试API结果.md
```

人工 / E2E 验收覆盖：

```text
1. in-memory persistence health 返回 in-memory stores。
2. Evaluation / Candidate generation / Review 产生 AuditLog。
3. GET audit-logs 可按 resource 查询。
4. Debug token 开启时无 token 返回 401。
5. postgres 专项 Testcontainers E2E 全绿。
```

## 2.4 文档治理依据

冻结前已完成文档同步：

```text
docs/README.md
docs/AI_IMPLEMENTATION_SKILL.md
docs/架构文档缺口审查清单.md
docs/Phase5_P0冻结记录.md（本文档）
docs/Phase5_P0开发任务清单.md
docs/Phase5_P0人工测试API结果.md
```

## 2.5 相对 Phase 4-P1 Hardening Backlog 的关闭项

Phase4-P1 冻结记录 6.3「Review 与 Store 均为内存实现」已在 P0 关闭：

```text
6.3 InMemory Store → Phase5-P0 Jdbc*Store + Flyway schema
6.3 Review API 无 AuditLog → Phase5-P0 AuditLogService + debug API
```

---

# 三、冻结范围

Phase 5-P0 冻结范围包括：

```text
1. config/（ClinmindPersistenceProperties / ClinmindDebugApiProperties / InMemoryPersistenceAutoConfiguration）。
2. persistence/（JsonSnapshotMapper + 领域 SnapshotMapper）。
3. audit/（AuditLog 全模块 + Jdbc / InMemory Store）。
4. storage/jdbc/、evaluation/jdbc/、candidate/store/jdbc/、candidate/review/jdbc/。
5. api/（PersistenceHealthController / AuditLogController / DebugActorFilter / DebugTokenFilter）。
6. db/migration/V5_0_0__phase5_p0_governance_schema.sql。
7. application-postgres.yml + clinmind.persistence.mode 双模式切换。
8. Phase 1–4 回归保护 + postgres 专项测试。
```

冻结后，以上能力只允许做：

```text
bug fix
测试补强
文档同步
错误码修正
小型一致性修复
postgres CI / Docker 兼容性修复
```

不再向 Phase 5-P0 中新增大能力。

---

# 四、冻结前质量清理状态

| 清理项 | 状态 | 说明 |
|---|---|---|
| P0-A 至 P0-H 任务清单 | 已完成 | 全部标记 `[x]` |
| 人工 API 验收记录 | 已完成 | `Phase5_P0人工测试API结果.md` |
| AI 实现约束状态同步 | 已完成 | `AI_IMPLEMENTATION_SKILL.md` 标记 Phase5-P0 freeze |
| 文档导航状态同步 | 已完成 | `docs/README.md` 已更新 |
| 架构缺口审查状态同步 | 已完成 | `架构文档缺口审查清单.md` 已更新 |
| in-memory 回归 | 已完成 | 347 项全绿 |
| postgres 专项测试 | 已完成 | 22 项全绿（Docker + RUN_POSTGRES_TESTS=true） |
| Git 代码基线 | 已完成 | commit `2ad656d` |
| P4-P1 hardening 6.3 持久化 | 已关闭 | 见本文档 2.5 |

---

# 五、冻结后的禁止事项

冻结后不允许在 Phase 5-P0 中继续加入：

```text
1. 真实 RAG / GraphRAG。
2. Python AI Provider。
3. 前端 Training Center / Console UI。
4. 模型训练 / 后训练 / SFT / RLHF。
5. 正式医生审核平台（RBAC / 工作流 / 多级审批）。
6. ApprovedExperience 正式生效 / 自动上线。
7. TrainingDatasetVersion 正式发布。
8. AssetPackage / CapabilityProfile 自动修改。
9. MCP / LangGraph / Agent SDK 作为 Runtime 主控。
10. 删除 InMemory 实现或强制 postgres-only 启动。
```

这些能力属于 Phase 5-P1 / P2 或更后阶段。

---

# 六、已知限制与 P0 Hardening Backlog

以下项 **不阻塞 Phase 5-P0 冻结**，但应在进入 Phase 5-P1 或生产化前优先处理：

## 6.1 JdbcRuntimeStore 混合缓存模式

现状：

```text
JdbcRuntimeStore 使用内存 delegate 缓存 + DB 持久化；重启后通过 exists() hydrate。
```

后续 hardening：

```text
评估是否改为纯 JDBC 读写或增加显式 reload API；补 restart live 验收。
```

## 6.2 postgres live 启动未做 JAR 抽测

现状：

```text
Testcontainers 自动化 E2E 已全绿；--spring.profiles.active=postgres + 本地 PostgreSQL 的 live JAR 抽测为可选增强。
```

后续 hardening：

```text
接本地 PostgreSQL 或 Docker Compose 后补 persistence health live 验收截图/记录。
```

## 6.3 Debug token 为最小保护

现状：

```text
仅保护 /api/v1/debug/**；无登录、无 RBAC、无多租户。
```

后续 hardening：

```text
Phase5-P1 强化 RBAC 与 Audit Center。
```

## 6.4 APPROVED 候选仍不代表 Runtime 可用

现状：

```text
review_status=APPROVED 仅表示人工决策已记录，不触发 Asset / Capability / Training 变更。
```

这是 **设计边界**，不是 bug。

---

# 七、进入 Phase 5-P1 前的条件

进入下一阶段前需要先完成：

```text
1. 明确 Phase 5-P1 范围（最小 Console API / RBAC 强化，仍不做完整审核平台）。
2. 新增或升级 Phase5-P1 详细设计与任务清单（如需要）。
3. 保持 Phase 1–5 in-memory 回归与 postgres 专项测试通过。
4. 仍禁止自动上线经验、自动训练模型、自动改资产包。
```

Phase 5-P1 推荐主题（backlog，未启动）：

```text
最小 Console API / 页面
正式 RBAC / Debug token 强化
Audit Center 查询增强
Docker Compose 本地编排（可选）
```

---

# 八、测试说明

冻结记录创建时，已在 Java 21 + Docker Desktop 环境下运行：

```text
默认 mvn test：347 项全绿（postgres 专项跳过）
RUN_POSTGRES_TESTS=true mvn test：369 项全绿
```

已有测试与验收依据：

```text
docs/Phase5_P0开发任务清单.md
docs/Phase5_P0人工测试API结果.md
docs/Phase5_P0持久化与治理底座_实现规格.md
docs/Phase5_P0_API与测试设计.md
```

后续如果再次修改 Phase 5-P0 范围内代码，应重新运行测试并视情况更新验收记录。

---

# 九、最终结论

Phase 5-P0 已经可以冻结。

当前项目下一步不是继续向 Phase 5-P0 加功能，而是：

```text
1. 保持 Phase 5-P0 冻结边界。
2. 规划 Phase 5-P1（最小 Console / RBAC 强化）。
3. 可选：补 postgres live JAR 抽测与 Docker Compose。
```
