# AI Implementation Skill：ClinMindRuntime Phase 5-P2

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1–5-P2 均已落地；Phase 5-P0/P1 已冻结，Phase 5-P2 最小前端 Console MVP 已完成。后续修改不得破坏 Runtime 主控、安全门、输出边界、Provider 抽象、Evaluation 闭环、Candidate 脱敏、持久化双模式、AuditLog、Console 访问治理和患者端隔离。

---

# 一、当前项目阶段

```text
当前阶段：Phase 5-P2 最小前端 Console MVP — 已完成
下一步：无强制主线（见后置任务：正式登录、Docker Compose、RAG 等）
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP，已完成
Phase 2-P0：共享能力资产原型，已完成
Phase 3-P0：训练与评估闭环 MVP，已冻结
Phase 4-P0：候选沉淀机制 + debug API，已冻结
Phase 4-P1：候选治理与安全加固，已冻结
Phase 5-P0：持久化与治理底座，已冻结
Phase 5-P1：最小 Console 与访问治理，已冻结
Phase 5-P2：最小前端 Console MVP — 已完成
```

Phase 5-P2 目标：

```text
实现 console-web/ 最小治理界面，只消费 Phase 5-P1 Safe Console API，不暴露敏感字段、不改变 Runtime 决策边界。
```

Phase 5-P2 推荐链路：

```text
Frontend Console (console-web/)
→ Debug Token / Actor / Roles 输入
→ Console API Client
→ /api/v1/debug/console/**
→ Safe DTO Response
→ 列表 / 详情 / Review 操作 / Audit Center
```

重要说明：

```text
Phase 5-P2 不是完整生产前端。
Phase 5-P2 不是正式登录 / OAuth / JWT 阶段。
Phase 5-P2 不是 RAG 或模型训练阶段。
Phase 5-P2 只做最小前端 Console MVP，对接已有 Console API。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase5_P2开发任务清单.md
3. docs/Phase5_P2最小前端Console_MVP_实现规格.md
4. docs/Phase5_P2前端信息架构与页面设计.md
5. docs/Phase5_P2_API对接与前端状态管理设计.md
6. docs/Phase5_P2前端安全边界与测试设计.md
7. docs/Phase5_P1冻结记录.md
8. docs/Phase5_P1最小Console与访问治理_实现规格.md
9. docs/Phase5_P0冻结记录.md
10. docs/README.md
```

解释：

```text
Phase 5-P2 已完成；P2 规格与验收记录是维护与演进时的首要约束。
Phase 5-P1/P0 冻结记录是 Console API 与持久化边界依据。
docs/平台前端与Console规划.md 约束完整产品化前端，不否定已落地的 console-web/ 最小 MVP。
```

---

# 三、当前允许实现的内容

Phase 5-P2 已全部完成。当前无强制实现任务；若继续演进，只能从 Phase5_P2开发任务清单 §十 后置任务中立项。

## 3.1 已交付：Phase 5-P1 Console API（冻结）

```text
ActorContext / RBAC-lite / AccessPolicy
SafeConsoleDtoMapper
GET /api/v1/debug/console/runtime-sessions
GET /api/v1/debug/console/evaluation-runs
GET /api/v1/debug/console/candidates / review-queue
GET /api/v1/debug/console/audit-center/**
Candidate review API + AccessPolicy
```

## 3.2 已交付：Phase 5-P2 前端 Console MVP（归档）

```text
console-web/ — Vite + React + TypeScript
Runtime / Evaluation / Candidate / Review Queue / Audit Center 页面
DebugContextPanel、consoleClient、SensitiveFieldRenderGuard
Review 表单（APPROVE / REJECT / DEPRECATE）
35 项 vitest；npm run build 通过
```

## 3.3 允许的维护类改动

```text
bug fix、测试补强、文档同步
不破坏 Safe DTO、RBAC-lite、AuditLog 与前端敏感字段过滤
保持 mvn test 与 console-web npm run test 回归
```

---

# 四、当前禁止做的事情

```text
1. 不新增真实 RAG / GraphRAG。
2. 不接 Python AI Provider。
3. 不接 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
4. 不训练基础大模型。
5. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
6. 不做完整产品化 Training Center / 正式 Runtime Console（最小 console-web/ MVP 已交付，不在此禁）。
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
该能力属于后续 Phase，不属于当前 Phase 5-P2 归档范围。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、Phase 5 架构约束（P1 Console API + P2 前端 MVP）

```text
1. RuntimeService 仍然是 Runtime 主控入口。
2. Console API 只能查询和治理已有对象，不能改变 Runtime 决策。
3. Console API 必须经过 DebugTokenFilter 和 AccessPolicy。
4. console-web/ 只调用 Safe Console API，不得绕过 DTO 获取 raw 数据。
5. Service 只能依赖 Store / Repository interface，不感知 in-memory 或 postgres 实现。
6. Console API 不得直接返回 domain object / snapshot raw json。
7. SafeConsoleDtoMapper 与 SensitiveFieldRenderGuard 必须过滤患者原文、clinician_report、未脱敏 candidate input。
8. Candidate review_status 即使为 APPROVED，也不代表 Runtime 可用。
9. Console 查询和 review 操作必须写 AuditLog。
10. 所有改动必须保持 Phase1/2/3/4/5-P0/P1/P2 回归通过。
```

---

# 六、任务清单同步规则

若新开 Phase 或做后置任务立项，必须同步更新对应任务清单与 `docs/README.md`。

对 Phase 5-P2 归档后的维护改动：

```text
1. 不擅自新增未立项的 Phase 子任务。
2. 若修复 bug 或补测试，在 commit / PR 说明中注明影响范围。
3. 若改动 Console API 或 console-web/，同步检查 Safe DTO 与 SensitiveField 测试。
```

---

# 七、测试约束

Phase 5 必须同时保护后端 in-memory / postgres 回归与 console-web 前端测试。

后端至少包含（P1 已交付，P2 不得破坏）：

```text
ActorContextResolverTest / AccessPolicyTest
SafeConsoleDtoMapperTest
Console*ControllerTest
ConsoleAuditTrailIntegrationTest
ConsoleSensitiveFieldRedactionIntegrationTest
Phase5P1ConsolePostgresEndToEndIntegrationTest
```

前端至少包含（P2 已交付）：

```text
ConsoleAppSmoke.test.tsx
SensitiveFieldRedactionRender.test.tsx
RuntimePage / EvaluationPage / CandidatePage / ReviewQueueFlow
AuditCenterPage / AuditCenterFlow / PermissionErrorFlow
npm run build && npm run test（35 项）
```

每次改动后，必须尽量保持：

```text
Phase 1–5 后端 mvn test 回归通过。
console-web npm run test / npm run build 通过。
in-memory 与 postgres 专项测试可复现。
```

如果无法实际运行测试，必须在回复或提交说明中明确：

```text
未在本地运行测试，仅完成代码 / 文档修改。
```

---

# 八、当前最优下一步

当前最优实现任务是：

```text
无强制 P2 子阶段 — Phase 5-P2 已完成。
```

若需继续演进，只应评估后置任务（见 Phase5_P2开发任务清单 §十）：

```text
1. 正式登录 / JWT / OAuth
2. Docker Compose 一键编排
3. 正式医生审核平台
4. RAG / GraphRAG / 模型训练
```

不应在未立项的新 Phase 中：

```text
正式登录系统
Docker Compose
RAG / 模型训练
后端 Console API 大改
ApprovedExperience 自动生效
```

---

# 九、最终约束

```text
当前不是在实现完整产品化平台。
当前不是在实现模型训练平台。
Phase 5-P1 最小 Console 与访问治理已完成并冻结。
Phase 5-P1 的目标是让已有治理对象可以被安全查询、权限控制和审计复盘，但不改变 AI 决策边界。
```
