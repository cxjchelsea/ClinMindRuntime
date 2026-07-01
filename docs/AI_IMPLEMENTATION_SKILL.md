# AI Implementation Skill：ClinMindRuntime Phase 5-P1

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1-P0 Runtime MVP、Phase 2-P0 共享能力资产原型、Phase 3-P0 训练与评估闭环 MVP、Phase 4-P0 候选沉淀机制、Phase 4-P1 候选治理与安全加固、Phase 5-P0 持久化与治理底座均已完成并冻结。  
> 当前进入 Phase 5-P1：最小 Console 与访问治理设计。后续修改不得破坏 Runtime 主控、安全门、输出边界、Provider 抽象、资产版本追踪、Evaluation 闭环、Candidate 脱敏、SourceRef 校验、Review 记录边界、持久化双模式、AuditLog 和患者端隔离。

---

# 一、当前项目阶段

```text
当前阶段：Phase 5-P1 最小 Console 与访问治理 — P1-C 已完成
下一步：Phase5-P1-D Console Candidate / Review Queue API
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP，已完成
Phase 2-P0：共享能力资产原型，已完成
Phase 3-P0：训练与评估闭环 MVP，已冻结
Phase 4-P0：候选沉淀机制 + debug API，已冻结
Phase 4-P1：候选治理与安全加固，已冻结
Phase 5-P0：持久化与治理底座，已冻结
Phase 5-P1：最小 Console 与访问治理 — P1-C 已完成，准备进入 P1-D
```

Phase 5-P1 目标：

```text
让 Phase5-P0 已持久化的 Runtime / Evaluation / Candidate / Review / AuditLog 可以通过最小 Console API 被安全查询、权限控制和审计复盘。
```

Phase 5-P1 推荐链路：

```text
Console / Debug Request
→ DebugTokenFilter
→ ActorContextResolver
→ AccessPolicy / RolePolicy
→ ConsoleQueryService
→ Store / Repository
→ Safe DTO Mapper
→ AuditLogService
→ Console API Response
```

重要说明：

```text
Phase 5-P1 不是 RAG 阶段。
Phase 5-P1 不是模型训练阶段。
Phase 5-P1 不是完整前端 Console 阶段。
Phase 5-P1 不是正式医生审核平台。
Phase 5-P1 不实现 ApprovedExperience 自动生效。
Phase 5-P1 只做最小 Console API、RBAC-lite、Audit Center 查询增强和安全 DTO。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase5_P1开发任务清单.md
3. docs/Phase5_P1最小Console与访问治理_实现规格.md
4. docs/Phase5_P1_RBAC与AuditCenter设计.md
5. docs/Phase5_P1Console_API与测试设计.md
6. docs/Phase5_P0冻结记录.md
7. docs/Phase5_P0人工测试API结果.md
8. docs/Phase5_P0开发任务清单.md
9. docs/Phase5_P0持久化与治理底座_实现规格.md
10. docs/Phase4_P1冻结记录.md
11. docs/Phase4_P0冻结记录.md
12. docs/Phase3_P0冻结记录.md
13. docs/README.md
14. docs/项目展示导读.md
15. docs/架构文档缺口审查清单.md
16. docs/ClinMindRuntime技术实现总方案.md
17. docs/测试与CI总方案.md
18. docs/数据安全与合规边界规划.md
19. docs/数据库持久化设计.md
20. docs/平台前端与Console规划.md
21. docs/部署与运维规划.md
22. docs/模型训练与后训练规划.md
23. docs/医学知识库与RAG构建规划.md
24. docs/ClinMindRuntime阶段拆分路线图.md
25. docs/ClinMindRuntime完整系统设计.md
26. docs/架构模式与设计模式说明.md
```

解释：

```text
Phase 5-P1 文档优先指导当前新增能力。
docs/Phase5_P0冻结记录.md 是 P0 冻结边界依据。
docs/平台前端与Console规划.md 是长期规划，不是 P1 实现完整前端的理由。
Phase5-P1 只实现最小 Console API 与访问治理。
```

---

# 三、当前允许实现的内容

当前只允许按 Phase5-P1-A 到 Phase5-P1-F 顺序推进。

## 3.1 Phase5-P1-A：ActorContext 与 RBAC-lite 基础

```text
ActorContext
DebugRole
ConsoleActionType
ConsoleResourceType
ActorContextResolver
RolePolicy / AccessPolicy
AccessDeniedException
ApiExceptionHandler 错误码映射
```

## 3.2 Phase5-P1-B：Safe Console DTO Mapper

```text
SafeConsoleDtoMapper
Runtime / Evaluation / Candidate / Review / Audit console DTO
敏感字段 denylist / allowlist
```

## 3.3 Phase5-P1-C：Console Runtime / Evaluation 查询 API

```text
GET /api/v1/debug/console/runtime-sessions
GET /api/v1/debug/console/runtime-sessions/{runtime_id}
GET /api/v1/debug/console/evaluation-runs
GET /api/v1/debug/console/evaluation-runs/{run_id}
```

## 3.4 Phase5-P1-D：Console Candidate / Review Queue API

```text
GET /api/v1/debug/console/candidate-generations
GET /api/v1/debug/console/candidates
GET /api/v1/debug/console/candidates/{candidate_id}
GET /api/v1/debug/console/review-queue
existing review API 接入 AccessPolicy
```

## 3.5 Phase5-P1-E：Audit Center 查询增强

```text
GET /api/v1/debug/console/audit-center/audit-logs
GET /api/v1/debug/console/audit-center/audit-logs/{audit_id}
GET /api/v1/debug/console/audit-center/summary
filters / pagination / limit guard
```

## 3.6 Phase5-P1-F：Postgres E2E 与人工验收

```text
Phase5P1ConsolePostgresEndToEndIntegrationTest
ConsoleAuditTrailIntegrationTest
ConsoleSensitiveFieldRedactionIntegrationTest
docs/Phase5_P1人工测试API结果.md
```

---

# 四、当前禁止做的事情

```text
1. 不新增真实 RAG / GraphRAG。
2. 不接 Python AI Provider。
3. 不接 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
4. 不训练基础大模型。
5. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
6. 不做完整前端 Training Center / Runtime Console。
7. 不做正式登录系统 / JWT / OAuth / 多租户。
8. 不做正式医生审核平台。
9. 不自动上线 ApprovedExperience。
10. 不发布 TrainingDatasetVersion。
11. 不自动修改 AssetPackage / CapabilityProfile。
12. 不改变患者端输出边界。
13. 不绕过 SafetyGate 或 DecisionBoundary。
14. 不删除 InMemory 实现或强制 postgres-only。
15. 不直接返回 raw snapshot JSON 给 Console API。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 5-P1 最小 Console 与访问治理。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、Phase 5-P1 架构约束

```text
1. RuntimeService 仍然是 Runtime 主控入口。
2. Console API 只能查询和治理已有对象，不能改变 Runtime 决策。
3. Console API 必须经过 DebugTokenFilter 和 AccessPolicy。
4. Service 只能依赖 Store / Repository interface，不感知 in-memory 或 postgres 实现。
5. Console API 不得直接返回 domain object / snapshot raw json。
6. SafeConsoleDtoMapper 必须过滤患者原文、clinician_report、未脱敏 candidate input。
7. Candidate review_status 即使为 APPROVED，也不代表 Runtime 可用。
8. Console 查询和 review 操作必须写 AuditLog。
9. AuditLog 不得保存未脱敏患者原文。
10. 所有改动必须保持 Phase1/2/3/4/5-P0 回归通过。
```

---

# 六、任务清单同步规则

每次实现 Phase 5-P1 代码前，必须同步更新：

```text
docs/Phase5_P1开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase5_P1开发任务清单.md。
2. 确认当前任务属于 Phase5-P1-A 到 Phase5-P1-F 的哪一项。
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

Phase 5-P1 必须同时保护 in-memory 回归和 postgres 持久化专项测试。

至少包含：

```text
ActorContextResolverTest
AccessPolicyTest
SafeConsoleDtoMapperTest
ConsoleRuntimeControllerTest
ConsoleEvaluationControllerTest
ConsoleCandidateControllerTest
ConsoleAuditCenterControllerTest
ConsoleAccessDeniedControllerTest
ConsoleAuditTrailIntegrationTest
ConsoleSensitiveFieldRedactionIntegrationTest
Phase5P1ConsolePostgresEndToEndIntegrationTest
```

每次 Phase 5-P1 改动后，必须尽量保持：

```text
Phase 1 Runtime 回归通过。
Phase 2 Asset Provider 回归通过。
Phase 3 Evaluation 回归通过。
Phase 4 Candidate / Review 回归通过。
Phase 5-P0 Persistence / AuditLog 回归通过。
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
Phase5-P1-D：Console Candidate / Review Queue API
```

只应实现：

```text
1. ConsoleCandidateController。
2. GET /api/v1/debug/console/candidate-generations / candidates / review-queue。
3. 接入 AccessPolicy 与 SafeConsoleDtoMapper。
4. existing review API 接入 AccessPolicy。
5. 查询 / review 行为写 AuditLog。
6. 同步更新 docs/Phase5_P1开发任务清单.md。
```

不应在 P1-D 中实现：

```text
全部 Console Controller
Candidate review AccessPolicy 接入
Audit Center 增强
前端页面
RAG / 模型训练
```

---

# 九、最终约束

```text
当前不是在实现完整产品化平台。
当前不是在实现模型训练平台。
当前是在设计并准备实现 Phase 5-P1 最小 Console 与访问治理。
Phase 5-P1 的目标是让已有治理对象可以被安全查询、权限控制和审计复盘，但不改变 AI 决策边界。
```
