# AI Implementation Skill：ClinMindRuntime（Phase 10-P0 可实现但受限）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结；Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结；Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结；Phase 8-P2 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 已冻结；Phase 9-P0 Tool / MCP / Skills 受控接入 MVP 已冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 10-P0 三份实现级文档已经建立，可以按任务清单进入 **Governance Console / Runtime Console MVP** 的受限代码实现。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 10-P0 可实现但受限 |
| 前置状态 | Phase 1–9 P0 已冻结 |
| 前置冻结记录 | `docs/3-phase实现/Phase9_P0冻结记录.md` |
| 当前实现目标 | Governance Console / Runtime Console MVP |

已完成主线：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，已冻结
- Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP，已冻结
- Phase 8-P2：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP，已冻结
- Phase 9-P0：Tool / MCP / Skills 受控接入 MVP，已冻结
- Phase 10-P0：设计文档已建立，可受限实现

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统、不是模型直接诊断系统、不是自动训练/自动发布平台、不是外部工具自治调用平台、不是患者端产品或生产级医生工作站

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider / Tool-MCP-Skills → Policy / Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation → Candidate / Governance → Console Observation`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`
5. `docs/3-phase实现/Phase10_P0Console_API与测试设计.md`
6. `docs/3-phase实现/Phase10_P0开发任务清单.md`
7. `docs/3-phase实现/Phase9_P0冻结记录.md`
8. `docs/3-phase实现/Phase9_P0人工测试结果.md`
9. `docs/3-phase实现/Phase8_P2冻结记录.md`
10. `docs/3-phase实现/Phase8_P1冻结记录.md`
11. `docs/3-phase实现/Phase8_P0冻结记录.md`
12. Phase 1–7 各阶段冻结记录

---

# 三、当前允许做的事情

1. 按 Phase10_P0 开发任务清单实现 Governance Console / Runtime Console MVP。
2. 新增 Console Safe DTO：ConsoleOverviewDto、RuntimeTimelineDto、TimelineNodeDto、GovernanceDomainCardDto、CandidateInboxItemDto、AuditBrowserItemDto。
3. 新增 ConsoleSafeDtoMapper，统一过滤 rawPatientDialogue、raw_prompt、secret、api_key、private_key、raw_external_response、internal_chain_of_thought、full_rationale 等敏感字段。
4. 新增 ConsoleOverviewService、RuntimeTimelineService、GovernanceDashboardService、ConsoleCandidateQueryService、ConsoleAuditQueryService。
5. 新增只读 Console API：overview、runtimes、timeline、governance domains、candidates、audits。
6. 新增 console-web 基础页面：Overview、RuntimeTimeline、GovernanceDashboard、CandidateInbox、AuditBrowser。
7. 新增 Console Evaluation Scorer：ConsoleSafeDtoScorer、ConsoleTimelineCompletenessScorer。
8. 已冻结阶段的 bug fix、测试补强、文档修正。

---

# 四、当前禁止做的事情

1. 向 Phase 1–9 P0 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不做患者端 UI。
3. 不做正式医生工作站。
4. 不做生产级审核平台。
5. 不做 approve / reject / publish / run 写操作。
6. 不让 Console 修改 RuntimeState。
7. 不让 Console 调用 Provider / Tool / Agent。
8. 不让 Console 触发诊断、治疗、转诊、处方、预约、支付、消息发送。
9. 不展示 raw patient dialogue。
10. 不展示完整 prompt 原文。
11. 不展示 secret / api key / private key。
12. 不展示 raw external response。
13. 不展示完整内部推理链或 full rationale。
14. 不绕过 SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 或 Candidate Governance。
15. 不改写历史冻结记录中的事实。

---

# 五、已冻结能力边界（勿再扩展）

| Phase | 边界 |
|---|---|
| Phase 6-P0 | InquiryPlanningAgent / AgentRuntime / AgentProposalValidator |
| Phase 7-P0 | RagEvidenceProvider / EvidenceValidation |
| Phase 7-P1 | KG-lite / GraphEvidenceProvider |
| Phase 8-P0 | `python-provider`、PythonProviderClient、ProviderValidation、Evidence rerank 增强、Provider Debug API |
| Phase 8-P1 | ProviderCapabilityProfile、ProviderCapabilityPolicy、JudgeProvider、RiskSignalClassifierProvider、Judge / Risk / Profile Validation、Debug API、Evaluation Scorer、ProviderGovernanceSnapshot、Candidate 映射 |
| Phase 8-P2 | ModelRegistryEntry、PromptRegistryEntry、TrainingDatasetVersion、ModelExperimentRecord、ModelEvaluationReport、ModelReleaseCandidate、ModelRollbackPlan、ModelGovernanceService、Model Governance Debug API、Evaluation Scorer、Candidate 映射 |
| Phase 9-P0 | ToolRegistryEntry、McpServerRegistryEntry、SkillRegistryEntry、ToolInvocationRuntime、ToolResultValidationService、Tool Governance Debug API、Evaluation Scorer、Candidate 映射 |

Phase 6–9 P0 已冻结能力只能被 Phase 10-P0 观察和复用，不能继续向已冻结范围堆新能力。

---

# 六、Phase 10-P0 实现边界

Phase 10-P0 当前直接依据：

```text
docs/3-phase实现/Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md
docs/3-phase实现/Phase10_P0Console_API与测试设计.md
docs/3-phase实现/Phase10_P0开发任务清单.md
```

Phase 10-P0 核心目标：

```text
证明 Agent / Evidence / Provider / ModelGov / ToolGov / Evaluation / Candidate / Audit 的治理信息可以被统一观察；
Runtime 执行过程可以被 timeline 化解释；
所有 Console 输出都是 Safe DTO；
Console 不能修改 RuntimeState；
Console 不能执行 Provider / Tool / Agent；
Console 不能发布模型、工具、prompt、dataset 或 candidate。
```

Phase 10-P0 可以涉及：

```text
ConsoleOverviewDto
RuntimeTimelineDto
TimelineNodeDto
GovernanceDomainCardDto
CandidateInboxItemDto
AuditBrowserItemDto
ConsoleSafeDtoMapper
ConsoleOverviewService
RuntimeTimelineService
GovernanceDashboardService
ConsoleCandidateQueryService
ConsoleAuditQueryService
ConsoleController
ConsoleSafeDtoScorer
ConsoleTimelineCompletenessScorer
console-web Overview / Timeline / Governance / Candidate / Audit pages
```

Phase 10-P0 只能输出或创建：

```text
Safe DTO
Console Overview
Runtime Timeline
Governance Domain Card
Candidate Inbox Item
Audit Browser Item
Console Evaluation Metric
Console Read Audit（可选）
```

Phase 10-P0 不能输出或触发：

```text
Raw Patient Dialogue
Raw Prompt
Secret / API Key / Private Key
Raw External Response
Internal Chain-of-thought / Full Rationale
RuntimeState Mutation
Provider Run
Tool Invocation
Agent Invocation
Candidate Approval / Rejection / Publishing
Model / Prompt / Dataset / Tool / Skill Publication
PatientOutput Generation
Final Diagnosis / Treatment Advice
```

---

# 七、实现顺序约束

必须优先按任务清单顺序推进：

1. P10P0-A：Safe DTO / mapper。
2. P10P0-B：Overview 聚合。
3. P10P0-C：Runtime Timeline。
4. P10P0-D：Governance Domain Dashboard。
5. P10P0-E：Candidate Inbox。
6. P10P0-F：Audit Browser。
7. P10P0-G：Console API。
8. P10P0-I：Console Evaluation Scorer。
9. P10P0-H：console-web 页面。
10. P10P0-J：测试、人工验证、冻结记录。

不得跳过 Safe DTO 直接做前端展示；不得在 Console 中增加写操作。

---

# 八、测试要求

Phase 10-P0 实现完成前必须至少覆盖：

- Java：ConsoleSafeDtoMapperTest
- Java：ConsoleAccessPolicyTest
- Java：ConsoleOverviewControllerTest
- Java：RuntimeTimelineControllerTest
- Java：GovernanceDashboardControllerTest
- Java：ConsoleCandidateInboxControllerTest
- Java：ConsoleAuditBrowserControllerTest
- Java：ConsoleScorerTest

并保持：

- `mvn test` 通过
- 如果修改 `console-web`，`npm run test && npm run build` 通过
- Phase 1–9 P0 既有 Runtime / Agent / Evidence / Graph / Provider / ModelGov / ToolGov / Evaluation / Candidate / Persistence / Audit 测试不回归

---

# 九、冻结要求

Phase 10-P0 完成后必须新增：

```text
docs/3-phase实现/Phase10_P0人工测试结果.md
docs/3-phase实现/Phase10_P0冻结记录.md
```

冻结记录必须说明：

1. 已实现哪些 Console API 与页面。
2. Safe DTO 如何过滤敏感字段。
3. Runtime Timeline 如何展示治理链路但不泄露原文。
4. Candidate Inbox 如何保持只读。
5. Audit Browser 如何裁剪 metadata。
6. PATIENT 访问如何被拒绝。
7. Console Scorer 如何识别可观测性缺口。
8. Java / 前端测试结果。
9. 后置到 Phase 10-P1 / P10-P2 的任务。

---

# 十、最终结论

当前 AI 实现约束是：

```text
可以进入 Phase 10-P0 的 Governance Console / Runtime Console MVP 实现；
只能按三份 Phase 10-P0 文档受限实现；
不可以做患者端、医生正式工作站或生产级审核平台；
不可以在 Console 中增加 approve / reject / publish / run 写操作；
不可以让 Console 修改 RuntimeState 或调用 Provider / Tool / Agent；
所有 Console 输出必须经过 Safe DTO，不得泄露 raw patient dialogue、raw prompt、secret、raw external response 或 full rationale。
```
