# AI Implementation Skill：ClinMindRuntime（Phase 6-P0 已冻结）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已于 2026-07-02 冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。

---

# 一、当前项目阶段

```text
当前阶段：Phase 6-P0 已冻结
前置状态：Phase 1–5 已全部冻结
当前实现目标：Phase 6-P1 或 Phase 7-P0（按路线图决策，规格未建立前不可直接编码）
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
Phase 6-P0：受控 Agent 执行层 MVP，已冻结
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
4. docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md
5. docs/3-phase实现/Phase6_P0Agent_API与测试设计.md
6. docs/3-phase实现/Phase6_P0开发任务清单.md
7. docs/3-phase实现/Phase5冻结记录.md
8. docs/3-phase实现/Phase5_P2冻结记录.md
9. docs/3-phase实现/Phase5_P2人工测试结果.md
10. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
11. docs/1-总设计/ClinMindRuntime技术实现总方案.md
```

解释：

```text
docs/0-项目入口/00_项目设计地图.md 是文档体系总入口。
docs/1-总设计/ClinMindRuntime完整系统设计.md 是系统级权威总设计。
Phase 6-P0 三份文档是当前代码实现的直接依据。
docs/3-phase实现/Phase 1–5 冻结记录是已完成能力的边界依据。
docs/2-专项设计/ 下的专项规划文档不是直接编码依据，必须经过 Phase 实现规格转化。
```

---

# 三、当前允许做的事情

当前允许：

```text
1. Phase 1–6 P0 已冻结阶段的 bug fix、测试补强、文档修正。
2. 为 Phase 6-P1 / Phase 7-P0 编写规格与任务清单（不直接编码，除非新 Phase 文档已建立）。
3. 阅读与引用 Phase6_P0冻结记录.md 作为 Agent 层已完成能力边界。
```

当前禁止向 Phase 6-P0 继续堆：

```text
1. 新 Agent 类型、LLM 接入、多 Agent 协作。
2. Agent PostgreSQL 持久化（后置 P6-P1+）。
3. Agent Console 页面（后置 P6-P1）。
```

---

# 四、当前禁止做的事情

```text
1. 不向 Phase 1–6 P0 已冻结阶段继续堆新能力。
2. 不实现自由自治式 Agent。
3. 不实现多 Agent 协作。
4. 不使用 LangGraph / Agent SDK 作为 Runtime 主控。
5. 不提前接入真实 RAG / GraphRAG。
6. 不提前接入 Python AI Provider。
7. 不提前接入 MCP / Tool / Skills。
8. 不提前做模型训练 / 后训练。
9. 不新增正式登录 / OAuth / 多租户 / 生产级 RBAC。
10. 不实现正式医生审核平台。
11. 不让 Agent / RAG / Model / Tool 直接输出 PatientOutput。
12. 不让任何外部能力直接修改 RuntimeState。
13. 不绕过 Runtime Validation。
14. 不绕过 SafetyGate。
15. 不绕过 DecisionBoundary。
16. 不自动上线 ExperienceCandidate。
17. 不自动发布 TrainingDatasetVersion。
18. 不自动扩大 CapabilityProfile 输出权限。
19. 不改写 Phase 1–5 冻结记录中的历史事实。
20. 不把 Agent reasoning_summary 暴露给患者端。
```

---

# 五、Phase 6-P0 冻结边界（已完成，勿再扩展）

Phase 6-P0 冻结记录：

```text
docs/3-phase实现/Phase6_P0冻结记录.md
docs/3-phase实现/Phase6_P0人工测试结果.md
```

Phase 6-P0 已完成 Agent：

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

# 六、实现顺序约束

必须优先按任务清单顺序推进：

```text
1. P6-A：Agent domain 基础对象。
2. P6-B：AgentRegistry / AgentPolicy。
3. P6-D：InquiryPlanningAgent MVP。
4. P6-E：AgentProposalValidator。
5. P6-C：AgentRuntime。
6. P6-H：AgentTrace。
7. P6-G：Debug API。
8. P6-F：Runtime 主链路接入 Capability Orchestration。
9. P6-I：Evaluation Scorer。
10. P6-J：测试、人工验证、冻结记录。
```

不得跳过文档任务清单直接做高级 Agent 能力。

---

# 七、测试要求

Phase 6-P0 实现完成前必须至少覆盖：

```text
AgentRegistryTest
AgentPolicyTest
AgentRuntimeTest
InquiryPlanningAgentTest
AgentProposalValidatorTest
RuntimeValidationServiceTest
AgentDebugControllerTest
AgentTraceIntegrationTest
Agent Evaluation Scorer tests
```

并保持：

```text
mvn test 通过。
Phase 1–5 既有 Runtime / Evaluation / Candidate / Persistence / Console 测试不回归。
console-web npm run test / npm run build 不回归。
```

---

# 八、冻结状态

Phase 6-P0 已于 2026-07-02 冻结，文档：

```text
docs/3-phase实现/Phase6_P0冻结记录.md
docs/3-phase实现/Phase6_P0人工测试结果.md
```

---

# 九、最终结论

当前 AI 实现约束是：

```text
Phase 6-P0 已冻结，勿再向 P0 堆新 Agent 能力；
下一阶段须先建立 Phase 6-P1 或 Phase 7-P0 规格与任务清单；
不可以直接写自由 Agent；
不可以让 Agent / RAG / Model / Tool 替代 Runtime；
不可以绕过 Runtime Validation、SafetyGate、DecisionBoundary、Trace、Audit 和 Evaluation。
```
