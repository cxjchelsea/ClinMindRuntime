# AI Implementation Skill：ClinMindRuntime（Phase 5 已冻结，Phase 6-P0 设计准备）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1–5 均已落地并冻结（Phase 5 含 P0 持久化、P1 Console API、P2 前端 MVP）。  
> 总设计已升级为 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。后续实现必须遵循“统一 Runtime 主链路 + Capability Orchestration + Runtime Validation”的主控结构。

---

# 一、当前项目阶段

```text
当前阶段：Phase 5 已全部冻结（P0 / P1 / P2）
当前文档状态：总设计 v2.2 已完成，docs/00_项目设计地图.md 已建立
下一步：Phase 6-P0 受控 Agent 执行层 MVP 的文档设计准备
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
Phase 5-P2：最小前端 Console MVP，已冻结
```

当前项目权威定位：

```text
ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台。

不是普通 RAG 医疗问答。
不是自由自治式医疗 Agent。
不是模型直接回答患者。
不是多 Agent Demo。
```

当前统一主链路：

```text
用户 / 医生输入
→ Runtime API
→ RuntimeService
→ EntryAssessment
→ RuntimeState / CaseFrame
→ KnowledgeContext / ExperienceContext
→ SafetyGate
→ Capability Orchestration
→ Agent / RAG / Model / Tool 受控调用
→ Runtime Validation
→ Runtime 采纳 / 部分采纳 / 拒绝 / 降级
→ DDx Board / EvidenceGraph / QuestionPolicy
→ DecisionBoundary
→ PatientOutput / ClinicianReport
→ RuntimeTrace / AuditLog
→ Evaluation
→ Candidate / TrainingExample / CapabilityProfile Proposal
→ Review / Governance
→ 通过评估后进入下一轮 Runtime 可用能力
```

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档，优先级从高到低：

```text
1. docs/00_项目设计地图.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/README.md
4. docs/Phase5冻结记录.md
5. docs/Phase5_P2冻结记录.md
6. docs/Phase5_P2人工测试结果.md
7. docs/ClinMindRuntime阶段拆分路线图.md
8. docs/ClinMindRuntime技术实现总方案.md
9. 当前 Phase 实现规格（若已存在）
10. 当前 Phase 开发任务清单（若已存在）
```

解释：

```text
docs/00_项目设计地图.md 是文档体系总入口。
docs/ClinMindRuntime完整系统设计.md 是系统级权威总设计。
Phase 1–5 冻结记录是已完成能力的边界依据。
专项规划文档不是直接编码依据，必须经过 Phase 实现规格转化。
```

---

# 三、当前允许做的事情

当前允许：

```text
1. 文档同步。
2. README / docs/README / AI_IMPLEMENTATION_SKILL 状态更新。
3. 将专项设计文档挂接到总设计和 docs/00_项目设计地图.md。
4. 更新阶段路线图，使其与总设计 v2.2 对齐。
5. 更新技术实现总方案，补充 Capability Orchestration、AgentExecutionLayer、Runtime Validation 的实现结构。
6. 新增 Phase6_P0 受控 Agent 执行层实现规格。
7. 新增 Phase6_P0 开发任务清单。
8. 已冻结阶段的 bug fix、测试补强、文档修正。
```

当前不应直接实现 Agent 代码，除非 Phase6_P0 实现规格与任务清单已经建立。

---

# 四、当前禁止做的事情

```text
1. 不向 Phase 1–5 已冻结阶段继续堆新能力。
2. 不在 Phase 6-P0 规格未建立前直接实现 Agent。
3. 不实现自由自治式 Agent。
4. 不实现多 Agent 协作。
5. 不接 LangGraph / Agent SDK 作为 Runtime 主控。
6. 不新增真实 RAG / GraphRAG。
7. 不接 Python AI Provider。
8. 不接 MCP / Tool / Skills 正式能力。
9. 不训练基础大模型。
10. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
11. 不做完整产品化 Training Center / 正式 Runtime Console。
12. 不做正式登录系统 / JWT / OAuth / 多租户。
13. 不做正式医生审核平台。
14. 不自动上线 ApprovedExperience。
15. 不发布 TrainingDatasetVersion。
16. 不自动修改 AssetPackage / CapabilityProfile。
17. 不改变患者端输出边界。
18. 不绕过 SafetyGate、Runtime Validation 或 DecisionBoundary。
19. 不删除 InMemory 实现或强制 postgres-only。
20. 不直接返回 raw snapshot JSON 给 Console API。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前已冻结阶段或 Phase6-P0 前置设计范围。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、Phase 5 冻结边界

Phase 5 已全部冻结。

已交付能力：

```text
Phase 5-P0：PostgreSQL、Repository 双实现、AuditLog、Persistence health / Audit API
Phase 5-P1：ActorContext、RBAC-lite、AccessPolicy、Safe DTO、Console API、Audit Center
Phase 5-P2：console-web/ 最小前端 Console MVP
```

维护约束：

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

# 六、Phase 6-P0 前置设计约束

Phase 6-P0 的目标不是“做一个自由 Agent”，而是：

```text
引入受控 Agent 执行层，证明 Agent 可以在 Runtime 授权下生成可校验、可拒绝、可追踪的 Proposal，并进入 RuntimeTrace / Evaluation / Audit 闭环。
```

Phase 6-P0 推荐首个 Agent：

```text
InquiryPlanningAgent
```

Phase 6-P0 允许设计的对象：

```text
AgentRegistry
AgentRuntime
AgentPolicy
AgentContext
AgentExecutionRequest
AgentExecutionResult
AgentProposal
AgentProposalValidator
AgentTrace
AgentEvaluationHook
Capability Orchestration
Runtime Validation
InquiryPlanningAgent
```

Phase 6-P0 必须坚持：

```text
Agent 只能生成 Proposal / Draft / Candidate / Finding / ScoreDraft。
Agent 不能直接修改 RuntimeState。
Agent 不能决定 SafetyGate。
Agent 不能决定 DecisionBoundary。
Agent 不能直接输出 PatientOutput。
Agent 执行过程必须进入 RuntimeTrace / AuditLog。
Agent 输出必须经过 Runtime Validation。
```

---

# 七、专项文档使用规则

专项设计文档必须通过 Phase 实现规格进入代码。

正确链路：

```text
总设计
↓
项目设计地图
↓
专项设计
↓
Phase 实现规格
↓
Phase 开发任务清单
↓
代码实现
↓
测试与人工验证
↓
冻结记录
```

禁止：

```text
直接拿专项规划文档写代码。
把长期规划当作当前任务。
把 AI 技术雷达中的技术直接接入 Runtime Core。
```

后续每个专项文档应在开头加入“文档定位块”：

```text
上位总设计：docs/ClinMindRuntime完整系统设计.md
对应能力域：xxx
当前状态：长期规划 / 子系统设计 / 当前 Phase 实现依据 / 已实现冻结
当前实现：已实现 xxx，未实现 xxx
对应 Phase：Phase x
实现入口：进入对应 Phase 后，以 Phase*_实现规格.md 和 Phase*_开发任务清单.md 为直接开发依据。
```

---

# 八、测试约束

Phase 5 必须同时保护后端 in-memory / postgres 回归与 console-web 前端测试。

后端至少保护：

```text
ActorContextResolverTest / AccessPolicyTest
SafeConsoleDtoMapperTest
Console*ControllerTest
ConsoleAuditTrailIntegrationTest
ConsoleSensitiveFieldRedactionIntegrationTest
Phase5P1ConsolePostgresEndToEndIntegrationTest
```

前端至少保护：

```text
ConsoleAppSmoke.test.tsx
SensitiveFieldRedactionRender.test.tsx
RuntimePage / EvaluationPage / CandidatePage / ReviewQueueFlow
AuditCenterPage / AuditCenterFlow / PermissionErrorFlow
npm run build && npm run test
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

# 九、当前最优下一步

当前最优下一步是文档同步与 Phase 6-P0 设计准备：

```text
1. 更新 ClinMindRuntime阶段拆分路线图.md。
2. 更新 ClinMindRuntime技术实现总方案.md。
3. 为专项文档增加“文档定位块”。
4. 新增 Phase6_P0受控Agent执行层_实现规格.md。
5. 新增 Phase6_P0开发任务清单.md。
```

当前不应直接进入 Agent 代码实现。

---

# 十、最终约束

```text
ClinMindRuntime 的核心不是让 AI 自由发挥，
而是让 AI 能力在 Runtime 主控、Capability Orchestration、Runtime Validation、DecisionBoundary、Evaluation、Audit、Governance 之内运行。

任何新能力都必须先进入总设计和 Phase 实现规格，
再进入代码。
```
