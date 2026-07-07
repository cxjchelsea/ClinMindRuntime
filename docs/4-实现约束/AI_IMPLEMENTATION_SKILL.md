# AI Implementation Skill：ClinMindRuntime（Phase 9-P0 可实现但受限）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结；Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结；Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结；Phase 8-P2 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 已冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 9-P0 三份实现级文档已经建立，可以按任务清单进入 **Tool / MCP / Skills 受控接入 MVP** 的受限代码实现。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 9-P0 可实现但受限 |
| 前置状态 | Phase 1–8 P2 已冻结 |
| 前置冻结记录 | `docs/3-phase实现/Phase8_P2冻结记录.md` |
| 当前实现目标 | Tool / MCP / Skills 受控接入 MVP |

已完成主线：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，已冻结
- Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP，已冻结
- Phase 8-P2：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP，已冻结
- Phase 9-P0：设计文档已建立，可受限实现

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统、不是模型直接诊断系统、不是自动训练/自动发布平台、不是外部工具自治调用平台

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider / Tool-MCP-Skills → Policy / Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation → Candidate / Governance`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase9_P0Tool_MCP_Skills受控接入_实现规格.md`
5. `docs/3-phase实现/Phase9_P0Tool_MCP_Skills_API与测试设计.md`
6. `docs/3-phase实现/Phase9_P0开发任务清单.md`
7. `docs/3-phase实现/Phase8_P2冻结记录.md`
8. `docs/3-phase实现/Phase8_P2人工测试结果.md`
9. `docs/3-phase实现/Phase8_P1冻结记录.md`
10. `docs/3-phase实现/Phase8_P0冻结记录.md`
11. Phase 1–7 各阶段冻结记录

---

# 三、当前允许做的事情

1. 按 Phase9_P0 开发任务清单实现 Tool / MCP / Skills 受控接入 MVP。
2. 新增 tool governance domain 对象：ToolRegistryEntry、McpServerRegistryEntry、SkillRegistryEntry、ToolInvocationRequest、ToolInvocationResult、ToolGovernanceSnapshot。
3. 新增 ToolRegistryPolicy、McpServerRegistryPolicy、SkillRegistryPolicy、ToolInvocationPolicy。
4. 新增 mock/local ToolAdapter：MockGuidelineLookupTool、LocalClinicalCalculatorTool、MockSkillSummarizer。
5. 新增 ToolInvocationRuntime 与 ToolResultValidationService。
6. 新增 Tool Governance Debug API。
7. 新增 Tool Governance Evaluation Scorer。
8. 扩展 CandidateMappingPolicy，将工具治理缺口沉淀为 review-required candidate。
9. 新增 Trace / Audit 记录。
10. 已冻结阶段的 bug fix、测试补强、文档修正。

---

# 四、当前禁止做的事情

1. 向 Phase 1–8 P2 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不接真实第三方医疗系统。
3. 不接真实 EHR / HIS / LIS / PACS。
4. 不接真实远程 MCP Server 作为强依赖。
5. 不允许 Agent 自主发现和调用任意工具。
6. 不允许 Tool / MCP / Skill 直接修改 RuntimeState。
7. 不允许 Tool / MCP / Skill 直接写入 PatientOutput。
8. 不允许 Tool / MCP / Skill 决定诊断、治疗、转诊或用药。
9. 不做真实支付、预约、处方、消息发送等高风险工具。
10. 不做 Browser Agent / Computer Use / RPA。
11. 不做生产级 Secret 管理。
12. 不保存未脱敏患者原文、完整患者对话、真实外部凭证、可执行脚本或真实外部系统响应原文。
13. 不绕过 ToolInvocationPolicy、ToolResultValidationService、SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 或 Candidate Governance。
14. 不改写历史冻结记录中的事实。

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

Phase 6–8 已冻结能力只能被后续阶段复用，不能继续向已冻结范围堆新能力。

---

# 六、Phase 9-P0 实现边界

Phase 9-P0 当前直接依据：

```text
docs/3-phase实现/Phase9_P0Tool_MCP_Skills受控接入_实现规格.md
docs/3-phase实现/Phase9_P0Tool_MCP_Skills_API与测试设计.md
docs/3-phase实现/Phase9_P0开发任务清单.md
```

Phase 9-P0 核心目标：

```text
证明 Tool / MCP / Skills 可以被登记、授权、调用、校验、降级、追踪、审计、评估和候选治理；
外部能力不能直接输出 PatientOutput；
外部能力不能修改 RuntimeState 决策；
外部能力不能执行高风险写操作；
外部能力失败时可以 fallback，不阻断 Runtime 主链路。
```

Phase 9-P0 可以涉及：

```text
ToolRegistryEntry
McpServerRegistryEntry
SkillRegistryEntry
ToolInvocationRequest
ToolInvocationResult
ToolInvocationPolicy
ToolInvocationRuntime
ToolResultValidationService
ToolGovernanceSnapshot
ToolGovernanceDebugController
Tool Governance Evaluation Scorer
Tool Governance Candidate Mapping
```

Phase 9-P0 只能输出或创建：

```text
ToolRegistryEntry
McpServerRegistryEntry
SkillRegistryEntry
ToolInvocationResult
ExternalContext
SkillResult
ToolGovernanceSnapshot
ToolGovernanceTrace
ToolGovernanceAuditRecord
ReviewRequiredCandidate
```

Phase 9-P0 不能输出或触发：

```text
PatientOutput
Final Diagnosis
Final Treatment Advice
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
External Write Operation
Real MCP Remote Invocation
Prescription / Appointment / Payment / Messaging Action
Browser Automation / Computer Use
Automatic Tool Publication
```

---

# 七、实现顺序约束

必须优先按任务清单顺序推进：

1. P9P0-A：tool governance domain。
2. P9P0-B：in-memory store / registry。
3. P9P0-C：registry policy。
4. P9P0-D：invocation policy。
5. P9P0-E：mock/local adapters。
6. P9P0-F：ToolInvocationRuntime。
7. P9P0-G：ToolResultValidationService。
8. P9P0-H：Debug / Governance API。
9. P9P0-I：Trace / Audit。
10. P9P0-J：Evaluation Scorer。
11. P9P0-K：Candidate Mapping。
12. P9P0-L：测试、人工验证、冻结记录。

不得跳过文档任务清单直接接真实外部系统、真实 MCP、Computer Use、RPA 或高风险写工具。

---

# 八、测试要求

Phase 9-P0 实现完成前必须至少覆盖：

- Java：ToolRegistryPolicyTest
- Java：McpServerRegistryPolicyTest
- Java：SkillRegistryPolicyTest
- Java：ToolInvocationPolicyTest
- Java：ToolResultValidationServiceTest
- Java：ToolInvocationRuntimeTest
- Java：ToolGovernanceDebugControllerTest
- Java：ToolGovernanceScorerTest
- Java：ToolGovernanceCandidateMappingTest

并保持：

- `mvn test` 通过
- Phase 1–8 P2 既有 Runtime / Agent / Evidence / Graph / Provider / ModelGov / Evaluation / Candidate / Persistence / Console 测试不回归
- 涉及前端改动时，`console-web npm run test && npm run build` 通过

---

# 九、冻结要求

Phase 9-P0 完成后必须新增：

```text
docs/3-phase实现/Phase9_P0人工测试结果.md
docs/3-phase实现/Phase9_P0冻结记录.md
```

冻结记录必须说明：

1. 已实现哪些 Tool / MCP / Skill registry 能力。
2. ToolInvocationPolicy 如何拒绝 forbidden / patient_direct_answer / high-risk write。
3. ToolResultValidationService 如何拒绝越界结果。
4. ToolInvocationRuntime 如何处理 adapter failure 与 fallback。
5. Trace / Audit 如何记录 create / run / reject / fallback。
6. Evaluation Scorer 如何识别工具治理缺口。
7. Candidate Mapping 如何只沉淀待审核候选。
8. API 与测试结果。
9. 后置到 Phase 9-P1 / Phase 9-P2 / Phase 10 的任务。

---

# 十、最终结论

当前 AI 实现约束是：

```text
可以进入 Phase 9-P0 的 Tool / MCP / Skills 受控接入 MVP 实现；
只能按三份 Phase 9-P0 文档受限实现；
不可以接真实第三方医疗系统、真实远程 MCP 或高风险写工具；
不可以让 Agent 自主发现和调用任意工具；
不可以让 Tool / MCP / Skill 接管 Runtime 或直接输出 PatientOutput；
所有结果必须继续遵守 Registry → Policy → Invocation → Validation → Trace / Audit → Evaluation → Candidate Governance。
```
