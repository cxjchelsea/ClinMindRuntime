# Phase 5-P1 冻结记录

> 本文档用于记录 ClinMindRuntime Phase 5-P1 的冻结状态、冻结依据、当前边界、已知限制和后续进入 Phase 5-P2 前的检查项。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 5-P1 的最小 Console 与访问治理 MVP 已完成，后续不再继续向 Phase 5-P1 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 5-P1
冻结状态：已冻结
当前项目阶段：Phase 5-P1 freeze complete / Phase 5-P2 planning pending
冻结日期：2026-07-01
代码基线：commit 84c2122
```

Phase 5-P1 已完成的主线：

```text
ActorContext / DebugRole / AccessPolicy / ActorContextFilter
→ SafeConsoleDtoMapper + 安全 Console DTO
→ Console Runtime / Evaluation / Candidate / Review Queue API
→ Candidate review API 接入 AccessPolicy
→ Audit Center 查询增强（summary / audit-logs / filters）
→ Console 审计轨迹与敏感字段脱敏集成测试
→ Phase5P1ConsolePostgresEndToEndIntegrationTest
```

Phase 5-P1 的目标已经达到：

```text
Phase5-P0 已持久化的治理对象可通过最小 Console API 被安全查询、权限控制和审计复盘，且不泄露敏感字段、不改变 AI 决策边界。
```

---

# 二、冻结依据

## 2.1 任务清单依据

冻结依据：

```text
docs/Phase5_P1开发任务清单.md
```

当前任务清单显示 Phase5-P1-A 到 Phase5-P1-F 均已完成。

## 2.2 自动化测试依据

```text
in-memory：405 项测试通过（含 ConsoleAuditTrailIntegrationTest、ConsoleSensitiveFieldRedactionIntegrationTest）
postgres 专项：23 项（RUN_POSTGRES_TESTS=true + Docker 时执行，含 Phase5P1ConsolePostgresEndToEndIntegrationTest）
```

覆盖：

```text
ActorContextResolverTest / AccessPolicyTest
SafeConsoleDtoMapperTest
ConsoleRuntimeControllerTest / ConsoleEvaluationControllerTest
ConsoleCandidateControllerTest / ConsoleAuditCenterControllerTest
ConsoleAccessDeniedControllerTest / CandidateReviewAccessPolicyIntegrationTest
ConsoleAuditTrailIntegrationTest / ConsoleSensitiveFieldRedactionIntegrationTest
Phase5P1ConsolePostgresEndToEndIntegrationTest
Phase 1–5-P0 全量回归
```

冻结时默认 `mvn test`（无 `RUN_POSTGRES_TESTS`）：**405 项全绿**（Java 21）。

## 2.3 人工验收依据

```text
docs/Phase5_P1人工测试API结果.md
```

人工 / E2E 验收覆盖：

```text
1. READ_ONLY_OBSERVER / CANDIDATE_REVIEWER / AUDIT_REVIEWER role 边界。
2. Debug token 与 role 不足时的 401 / 403。
3. Console 响应不泄露患者原文与未脱敏 candidate input。
4. Console 查询与 review 写入 AuditLog。
5. postgres 模式 Console E2E（Testcontainers）。
```

## 2.4 文档治理依据

冻结前已完成文档同步：

```text
docs/README.md
docs/AI_IMPLEMENTATION_SKILL.md
docs/Phase5_P1冻结记录.md（本文档）
docs/Phase5_P1开发任务清单.md
docs/Phase5_P1人工测试API结果.md
README.md
```

---

# 三、冻结范围

Phase 5-P1 冻结范围包括：

```text
1. console/access/（ActorContext、AccessPolicy、ActorContextFilter）。
2. console/dto/ + SafeConsoleDtoMapper。
3. console/query/ConsoleQueryService。
4. console/audit/AuditCenterService。
5. console/api/（Runtime / Evaluation / Candidate / AuditCenter Controller）。
6. CandidateReviewController 接入 AccessPolicy。
7. AuditLog 新增 QUERY_CONSOLE_* action types。
8. Phase 1–5-P0 回归保护 + postgres Console E2E 测试。
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

不再向 Phase 5-P1 中新增大能力。

---

# 四、冻结后的禁止事项

冻结后不允许在 Phase 5-P1 中继续加入：

```text
1. 完整 React / Vue 前端 Console。
2. 正式登录系统 / JWT / OAuth / 多租户。
3. 正式医生审核平台（多级审批 / 工作流）。
4. ApprovedExperience 自动生效 / 自动上线。
5. TrainingDatasetVersion 正式发布。
6. RAG / GraphRAG / 模型训练。
7. 直接返回 raw snapshot JSON 给 Console API。
8. 绕过 AccessPolicy 或 DebugTokenFilter 的 Console 端点。
```

这些能力属于 Phase 5-P2 或更后阶段。

---

# 五、已知限制

```text
1. RBAC-lite 仅通过 debug header 传递 actor / role，不是生产级身份认证。
2. Console API 为 debug 路径，不面向患者端。
3. Audit Center 查询有 limit guard，不支持完整分页 UI。
4. postgres 专项测试依赖 Docker + RUN_POSTGRES_TESTS=true。
5. Candidate review 仍不自动修改 AssetPackage / CapabilityProfile / TrainingDataset。
```

---

# 六、P2 前 Cleanup Backlog（已完成）

以下项已在 P1 冻结后、P2 启动前完成：

## 6.1 RolePolicy 与设计矩阵对齐

`EVALUATION_REVIEWER` 对 Runtime 已收紧为 **summary/list only**（与 `docs/Phase5_P1_RBAC与AuditCenter设计.md` §五矩阵一致）；Evaluation 仍保留 summary/detail。

## 6.2 Review API 读路径接入 AccessPolicy

`CandidateReviewController` 的 `GET /reviews/{review_id}` 与 `GET /{candidate_id}/reviews` 已接入 `AccessPolicy`（`READ_DETAIL` / `LIST` + `CONSOLE_REVIEW`）；默认无 role 时为 `READ_ONLY_OBSERVER`，返回 403。

## 6.3 Audit Center 自审计 resource 语义

`AuditCenterService.recordConsoleAuditQuery` 已改用 `AuditResourceType.AUDIT_LOG` + `resourceId=audit-center`（新增 enum 值，schema 无需 migration）。

---

# 七、进入 Phase 5-P2 前的条件

进入下一阶段前需要先完成：

```text
1. 明确 Phase 5-P2 范围（如前端 Console MVP、Docker Compose、正式 RBAC 等）。
2. 新增或升级 Phase5-P2 详细设计与任务清单。
3. 保持 Phase 1–5 in-memory 回归与 postgres 专项测试通过。
4. 仍禁止自动上线经验、自动训练模型、自动改资产包。
```

Phase 5-P2 推荐主题（backlog，未启动）：

```text
最小前端 Console 页面
Docker Compose 本地编排
正式 RBAC / 登录集成
Audit Center UI
```

---

# 八、最终结论

Phase 5-P1 已完成「可查、可控、可审计、不泄露、不越界」的最小 Console 治理边界。  
后续工作应在新 Phase 文档中立项，而不是继续向 P1 范围追加能力。
