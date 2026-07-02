# AI Implementation Skill：ClinMindRuntime（Phase 5 已冻结，Phase 6-P0 设计准备）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1–5 均已落地并冻结（Phase 5 含 P0 持久化、P1 Console API、P2 前端 MVP）。  
> 总设计已升级为 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。后续实现必须遵循“统一 Runtime 主链路 + Capability Orchestration + Runtime Validation”的主控结构。  
> 当前 `docs/` 已完成分层目录重构，本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。

---

# 一、当前项目阶段

```text
当前阶段：Phase 5 已全部冻结（P0 / P1 / P2）
当前文档状态：总设计 v2.2 已完成，docs 已完成分层目录重构
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
1. docs/0-项目入口/00_项目设计地图.md
2. docs/1-总设计/ClinMindRuntime完整系统设计.md
3. docs/README.md
4. docs/3-phase实现/Phase5冻结记录.md
5. docs/3-phase实现/Phase5_P2冻结记录.md
6. docs/3-phase实现/Phase5_P2人工测试结果.md
7. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
8. docs/1-总设计/ClinMindRuntime技术实现总方案.md
9. 当前 Phase 实现规格（若已存在）
10. 当前 Phase 开发任务清单（若已存在）
```

解释：

```text
docs/0-项目入口/00_项目设计地图.md 是文档体系总入口。
docs/1-总设计/ClinMindRuntime完整系统设计.md 是系统级权威总设计。
docs/3-phase实现/Phase 1–5 冻结记录是已完成能力的边界依据。
docs/2-专项设计/ 下的专项规划文档不是直接编码依据，必须经过 Phase 实现规格转化。
```

---

# 三、当前允许做的事情

当前允许：

```text
1. 文档同步。
2. README / docs/README / AI_IMPLEMENTATION_SKILL 状态更新。
3. 修复迁移后的文档路径引用。
4. 新增 docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md。
5. 新增 docs/3-phase实现/Phase6_P0Agent_API与测试设计.md。
6. 新增 docs/3-phase实现/Phase6_P0开发任务清单.md。
7. 已冻结阶段的 bug fix、测试补强、文档修正。
```

当前不应直接实现 Agent 代码，除非 Phase6_P0 实现规格、API 与测试设计、开发任务清单已经建立。

---

# 四、当前禁止做的事情

```text
1. 不向 Phase 1–5 已冻结阶段继续堆新能力。
2. 不在 Phase 6-P0 规格未建立前直接实现 Agent。
3. 不实现自由自治式 Agent。
4. 不实现多 Agent 协作。
5. 不使用 LangGraph / Agent SDK 作为 Runtime 主控。
6. 不提前接入真实 RAG / GraphRAG。
7. 不提前接入 Python AI Provider。
8. 不提前接入 MCP / Tool / Skills。
9. 不提前做模型训练 / 后训练。
10. 不新增正式登录 / OAuth / 多租户 / 生产级 RBAC。
11. 不实现正式医生审核平台。
12. 不让 Agent / RAG / Model / Tool 直接输出 PatientOutput。
13. 不让任何外部能力直接修改 RuntimeState。
14. 不绕过 Runtime Validation。
15. 不绕过 SafetyGate。
16. 不绕过 DecisionBoundary。
17. 不自动上线 ExperienceCandidate。
18. 不自动发布 TrainingDatasetVersion。
19. 不自动扩大 CapabilityProfile 输出权限。
20. 不改写 Phase 1–5 冻结记录中的历史事实。
```

---

# 五、Phase 6-P0 前置要求

进入 Phase 6-P0 代码实现前，必须先建立：

```text
docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md
docs/3-phase实现/Phase6_P0Agent_API与测试设计.md
docs/3-phase实现/Phase6_P0开发任务清单.md
```

这三份文档建立前，只能做文档设计，不应创建 Agent 代码。

Phase 6-P0 首个 Agent：

```text
InquiryPlanningAgent
```

Phase 6-P0 核心目标：

```text
证明 Agent 可以作为 Runtime 授权下的受控执行单元，
生成可校验、可拒绝、可追踪的 Proposal，
并进入 RuntimeTrace / Evaluation / Audit 闭环。
```

Phase 6-P0 可以涉及的对象：

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

Agent 只能输出：

```text
Proposal
Draft
Candidate
Finding
ScoreDraft
```

Agent 不能输出：

```text
Final Diagnosis
Final Patient Answer
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
ApprovedExperience
PublishedTrainingDataset
```

---

# 六、后续实现顺序

当前最优顺序：

```text
1. 新增 Phase6_P0受控Agent执行层_实现规格.md。
2. 新增 Phase6_P0Agent_API与测试设计.md。
3. 新增 Phase6_P0开发任务清单.md。
4. 复查本文档，将状态从“Phase 6-P0 设计准备”改为“Phase 6-P0 可实现但受限”。
5. 按开发任务清单进入代码实现。
6. 完成测试和人工验证后新增 Phase6_P0冻结记录.md。
```

---

# 七、最终结论

当前 AI 实现约束是：

```text
可以继续做 Phase 6-P0 的文档落地；
不可以直接写自由 Agent；
不可以让 Agent / RAG / Model / Tool 替代 Runtime；
不可以绕过 Runtime Validation、SafetyGate、DecisionBoundary、Trace、Audit 和 Evaluation。
```
