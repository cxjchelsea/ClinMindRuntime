# AI 前沿技术选型与接入规划

> 上位总设计：`docs/ClinMindRuntime完整系统设计.md`  
> 文档地图：`docs/00_项目设计地图.md`  
> 对应能力域 / 架构层：Agent 受控执行域；Tool / MCP / Skills 外部能力域；医学知识与证据域；模型能力与 Provider 域；评估、审计与持续进化域  
> 当前状态：专项设计 / 技术雷达 / 接入边界规划  
> 当前实现：Phase 1–5 已完成 Runtime、Asset、Evaluation、Candidate、Persistence、Audit、Console 治理主干；本文档列出的 Agent SDK、LangGraph、MCP、GraphRAG、LLM-as-a-Judge、Skills、Multi-Agent、Voice / Realtime、Fine-tuning 等大多尚未正式实现  
> 对应 Phase：Phase 6-P0 引入受控 Agent 执行层；Phase 7 引入 RAG / KG-lite / GraphRAG；Phase 8 引入 Python AI Provider / ModelProvider；Phase 9 引入 Tool / MCP / Skills；Phase 10 后置生产级平台治理  
> 实现入口：本文档不是直接编码依据。任何前沿技术进入代码前，必须先转化为对应 Phase 的实现规格和开发任务清单，并保持 Runtime 主控、Runtime Validation、DecisionBoundary 和 Evaluation / Governance 闭环。

> 本文档用于说明 MCP、Tool Calling、Agent SDK、LangGraph、GraphRAG、LLM-as-a-Judge、Skills、Agent Memory、Multi-Agent 等 AI / Agent 前沿技术在 ClinMindRuntime 中的位置、接入阶段和禁止边界。  
> 本文档不是要求当前一次性实现所有前沿技术，而是建立技术雷达，避免项目只停留在普通后端 / 规则系统，也避免因为追逐新技术而破坏 Runtime 主控、安全边界和评估闭环。

---

# 一、文档定位

`docs/全局技术栈与架构选型.md` 解决的是工程技术栈问题，例如 Java / Spring Boot、PostgreSQL、React、Python Provider、pgvector、Docker 等。

本文档解决的是 AI / Agent 技术路线问题：

```text
MCP 是否要用？
Tool Calling 放在哪里？
Agent SDK 是否替代 Runtime？
LangGraph 和 RuntimeState 是什么关系？
LLM-as-a-Judge 什么时候用？
GraphRAG 是否属于 EvidenceProvider？
Skills 是否可以映射为能力资产？
Agent Memory 和 ExperienceContext 如何区分？
Multi-Agent 是否适合医疗 Runtime？
```

核心结论：

```text
AI 前沿技术负责扩展能力；
ClinMindRuntime 负责约束能力。
```

---

# 二、总体判断

ClinMindRuntime 应该吸收 AI / Agent 前沿技术，但不能被它们反向改造成普通 Agent Demo。

核心原则：

```text
1. RuntimeState、SafetyGate、Runtime Validation、DecisionBoundary、RuntimeTrace 是 ClinMindRuntime 的主控核心。
2. MCP / Tool Calling / RAG / GraphRAG / Agent SDK / LangGraph 只能作为 Provider、Adapter、Evaluation 辅助、Agent 执行层参考或外部连接协议。
3. LLM 不能直接决定患者端最终输出。
4. Agent 框架不能替代 Runtime 主流程。
5. 医疗安全边界必须由 Runtime 负责，而不是由提示词或 Agent 框架负责。
6. 前沿技术是否接入，取决于它是否增强资产、证据、评估、经验和治理闭环。
7. 所有外部能力返回内容都必须经过 Runtime Validation，再由 Runtime 决定采纳、部分采纳、拒绝或降级。
```

---

# 三、AI 技术雷达总表

| 技术 / 能力 | 当前判断 | 在本项目中的定位 | 推荐接入阶段 | 是否允许成为主控 |
|---|---|---|---|---:|
| Structured Output | Adopt | Agent / Provider / Tool 返回结构化对象的基础要求 | Phase 2 起持续使用 | 否 |
| Function Calling / Tool Calling | Adopt | Agent / Provider 内部工具调用机制 | Phase 9-P0 起正式接入 | 否 |
| MCP | Trial / Assess | 外部工具、数据源、Provider 的标准连接协议 | Phase 9-P0 起 | 否 |
| Spring AI MCP | Assess | Java 侧 MCP Client / Server 候选实现 | Phase 9-P0 或后置 | 否 |
| OpenAI Agents SDK / Agents SDK 类技术 | Assess | Agent 编排、handoff、guardrails、trace、eval 思想参考 | Phase 6 起可评估 | 否 |
| LangGraph | Assess | 状态图、可恢复 Agent workflow 的参考或 Python Provider 内部实验 | Phase 6 / Phase 8 后置 | 否 |
| LangChain / LlamaIndex | Trial | Python RAG、索引、检索、工具调用实验框架 | Phase 7 / Phase 8 | 否 |
| GraphRAG | Assess / Trial | EvidenceProvider / KG-lite 增强方向 | Phase 7-P1 | 否 |
| LLM-as-a-Judge | Trial | EvaluationScorer 辅助评分器 | Phase 8-P0 或 Evaluation 增强 | 否 |
| Guardrails | Adopt as concept | Provider 输出检查思想，不能替代 DecisionBoundary | Phase 6 起持续引入 | 否 |
| Skills | Assess | 可复用任务能力包，可映射为 Capability Asset / Provider Skill | Phase 9-P0 | 否 |
| Agent Memory | Assess | 与 ExperienceContext / ExperienceCandidate 对照，不做自由记忆 | Phase 6-P1 / Phase 7 后置 | 否 |
| Multi-Agent / Handoffs | Hold / Assess | 后台审核、资产治理、评估复盘的协作模式 | Phase 6-P1 或后置 | 否 |
| CrewAI / AutoGen | Hold | 多 Agent 实验参考，不作为医疗 Runtime 主线 | 后置 | 否 |
| Computer Use / Browser Agent | Hold | 当前医疗 Runtime 主线暂不需要 | 暂不接 | 否 |
| Voice / Realtime Agent | Hold / Optional | 语音问诊交互层，不改变 Runtime 主控 | 后置 | 否 |
| Codex / Claude Code / OpenHands | Adopt as dev tool | 开发辅助工具，不属于产品 Runtime | 当前可用 | 否 |
| Fine-tuning / SFT / DPO / RFT / Distillation | Hold | 模型训练方向，必须进入 ModelProvider / Evaluation / Governance 链路 | Phase 8-P1 后置 | 否 |

---

# 四、Agent SDK / LangGraph / Multi-Agent 的正确位置

Agent 类技术不应该接管 Runtime，而应该进入：

```text
Agent 受控执行域
```

正确关系：

```text
RuntimeState / CaseFrame
→ Capability Orchestration 判断需要 Agent
→ AgentPolicy 校验是否允许调用
→ AgentRuntime 调用受控 Agent
→ Agent 输出 AgentProposal / Draft / Finding
→ AgentProposalValidator 校验
→ Runtime 采纳 / 部分采纳 / 拒绝 / 降级
→ RuntimeTrace / AuditLog / Evaluation
```

Agent 可以做：

```text
问诊规划 Proposal
证据组织 Proposal
患者安全表达 Draft
运行轨迹复盘 Finding
经验候选挖掘 Draft
评估辅助 ScoreDraft
```

Agent 不能做：

```text
Final Diagnosis
Final Patient Answer
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
ApprovedExperience
PublishedTrainingDataset
```

## 4.1 推荐首个 Agent

Phase 6-P0 推荐实现：

```text
InquiryPlanningAgent
```

原因：

```text
1. 它体现 Agent 的规划价值。
2. 风险相对可控，因为只生成下一步追问 Proposal。
3. 可以用缺失信息覆盖率、问题安全性、红旗问题优先级等指标评估。
4. 不需要真实 RAG / MCP / 多 Agent 即可验证 AgentExecutionLayer。
```

---

# 五、MCP / Tool Calling / Skills 的正确位置

MCP、Tool Calling、Skills 属于：

```text
Tool / MCP / Skills 外部能力域
```

正确接入路径：

```text
Runtime / Agent / Provider
→ ToolAccessPolicy / SkillExecutionPolicy
→ Tool Adapter / MCP Adapter / Skill Provider
→ External Tool / Data Source / MCP Server
→ Structured ToolResult / ExternalContext
→ Runtime Validation
→ Runtime 采纳 / 拒绝 / 降级
→ RuntimeTrace / AuditLog
```

禁止路径：

```text
LLM Agent → MCP Tool → 直接生成患者端最终回答
MCP Server → 直接修改 RuntimeState
ToolResult → 绕过 SafetyGate / DecisionBoundary → PatientOutput
Skill → 绕过 CapabilityProfile 扩大输出权限
```

可能的 MCP Server：

```text
clinmind-evidence-mcp-server：只读 evidence / guideline resources
clinmind-evaluation-mcp-server：只读 EvaluationCaseSet / EvaluationResult resources
clinmind-asset-mcp-server：只读或治理受限的 AssetPackage / CapabilityProfile resources
clinmind-audit-mcp-server：只读 RuntimeTrace / AuditLog resources
```

---

# 六、RAG / GraphRAG / LangChain / LlamaIndex 的正确位置

RAG、KG-lite、GraphRAG 属于：

```text
医学知识与证据域
```

正确链路：

```text
RuntimeState / CaseFrame
→ KnowledgeContextService
→ EvidenceProvider
→ RAG / KG-lite / GraphRAG
→ EvidenceCandidate / EvidenceRef
→ Runtime Validation
→ EvidenceGraph
→ QuestionTestPolicy / SafetyGate / DecisionBoundary
→ PatientOutput / ClinicianReport
```

它们可以：

```text
提供证据候选。
增强 EvidenceGraph。
帮助医生端理解证据来源。
辅助 Evaluation 检查证据命中与适用性。
```

它们不能：

```text
直接回答患者。
直接决定诊断。
直接替代 EvidenceGraph。
直接绕过 DecisionBoundary。
```

LangChain / LlamaIndex 可以作为 Python Provider 或实验层工具，但不能进入 Java Runtime Core 成为主控。

---

# 七、LLM-as-a-Judge / Guardrails 的正确位置

LLM-as-a-Judge 属于 Evaluation 辅助能力。

适合评价：

```text
患者端表达是否安抚过度。
患者端表达是否包含诊断暗示。
医生端解释是否清晰。
下一步动作是否和证据一致。
回答是否存在不当医疗建议。
```

但必须遵守：

```text
1. LLM-as-a-Judge 不能作为唯一评分依据。
2. 必须优先保留 SafetyGateScorer、PatientBoundaryScorer、TraceCompletenessScorer、AssetVersionTraceScorer 等确定性评分器。
3. LLM judge 只能生成 ScoreDraft / Finding，不能直接扩大 CapabilityProfile。
```

Guardrails 可以用于 Provider 输出结构检查、LLM 输出候选检查、Agent Proposal 检查，但不能替代 Runtime Validation 和 DecisionBoundary。

---

# 八、Agent Memory 与 ExperienceContext

通用 Agent Memory 关注用户偏好、长期上下文、任务历史和个性化记忆。

ClinMindRuntime 的 ExperienceContext 更关注：

```text
经验证据是否被验证。
是否来自可靠病例或医生审核。
是否有适用场景。
是否有版本。
是否可追踪。
是否可撤回。
```

正确路线：

```text
RuntimeTrace
→ EvaluationResult
→ ExperienceCandidate
→ Human Review / Evaluation Gate
→ ExperienceUnitAsset
→ ClinicalExperienceProvider
→ ExperienceContext
```

禁止路线：

```text
模型自动记住用户输入
→ 下次直接使用
```

---

# 九、Voice / Realtime Agent / Computer Use / 开发型 Agent

Voice / Realtime Agent 属于交互入口，不改变 Runtime 主控：

```text
Voice Input
→ ASR
→ RuntimeService
→ PatientOutput / ClinicianReport
→ TTS
```

Computer Use / Browser Agent 暂不进入医疗 Runtime 主线，可作为内部 QA 或开发辅助后置评估。

Codex / Claude Code / OpenHands 属于开发辅助工具，不属于产品 Runtime。使用这些工具时必须遵守：

```text
docs/AI_IMPLEMENTATION_SKILL.md
docs/00_项目设计地图.md
当前 Phase 实现规格
当前 Phase 开发任务清单
```

---

# 十、按 Phase 的 AI 前沿技术接入边界

## Phase 1-P0

```text
只建立 Runtime 主控、状态、安全门、Trace。
不接真实 AI 前沿技术。
```

## Phase 2-P0

```text
建立 Provider 抽象、Asset Package、CapabilityProfile、ExperienceContext prototype。
不接 MCP / LangGraph / 真实 RAG / Python AI Provider。
```

## Phase 3-P0

```text
建立 EvaluationRunner、EvaluationCaseSet、Scorer、EvaluationResult、CapabilityProfileUpdateProposal。
不接 LLM-as-a-Judge 真正调用、MCP、Agent SDK、LangGraph、Python evaluation platform。
```

## Phase 4-P0 / P1

```text
已完成 Candidate 生成、脱敏、SourceRef 校验和 Review 记录。
不自动上线经验，不发布训练集，不接真实 RAG / GraphRAG / ModelProvider 主线。
```

## Phase 5-P0 / P1 / P2

```text
已完成 PostgreSQL 持久化、AuditLog、最小 Console API、RBAC-lite、Safe DTO、console-web 最小前端 MVP。
不把 Console 扩展为完整 Training Center。
不接生产级 MCP / RAG / ModelProvider。
```

## Phase 6-P0

```text
引入受控 Agent 执行层 MVP。
实现 AgentRegistry、AgentRuntime、AgentPolicy、AgentProposal、AgentProposalValidator、AgentTrace。
首个 Agent：InquiryPlanningAgent。
不做自由自治式 Agent，不做多 Agent，不接 Agent SDK 作为 Runtime 主控。
```

## Phase 7-P0 / P1

```text
Phase 7-P0：RAG EvidenceProvider MVP。
Phase 7-P1：KG-lite / GraphRAG 原型。
RAG / GraphRAG 只能返回 EvidenceCandidate / EvidenceRef。
```

## Phase 8-P0 / P1

```text
Phase 8-P0：Python AI Provider / ModelProvider MVP。
Phase 8-P1：模型训练与后训练治理。
模型只能作为 Provider 返回结构化候选，不能直接回答患者。
```

## Phase 9-P0

```text
Tool / MCP / Skills 受控接入。
建立 ToolAccessPolicy、McpAdapter、SkillMetadata、ToolExecutionTrace。
```

## Phase 10-P0

```text
生产级平台治理。
正式登录、多租户、生产 RBAC、正式医生审核平台、发布与回滚工作流。
```

---

# 十一、最终架构关系

```text
Patient / Clinician / Console
→ ClinMindRuntime API
→ RuntimeService
→ RuntimeState / CaseFrame
→ KnowledgeContext / ExperienceContext
→ SafetyGate
→ Capability Orchestration
→ Agent / RAG / Model / Tool 受控调用
→ Runtime Validation
→ Runtime Commit / Partial Commit / Reject / Degrade
→ DecisionBoundary
→ PatientOutput / ClinicianReport
→ RuntimeTrace / AuditLog
→ Evaluation / Governance
```

核心结论：

```text
前沿 AI 技术负责扩展能力，
ClinMindRuntime 负责约束能力。
```

---

# 十二、最终结论

ClinMindRuntime 必须体现对 AI / Agent 前沿技术的理解，但不能为了使用新技术而破坏医疗 Runtime 的主控边界。

```text
MCP 是未来外部工具和数据源的标准协议。
Tool Calling 是 Agent / Provider 调用外部能力的基础机制。
Agent SDK / LangGraph 是受控 Agent 执行层的重要参考，但不能替代 Runtime。
GraphRAG 是 EvidenceProvider 和 KG-lite 的后续增强方向。
LLM-as-a-Judge 是评估体系的辅助评分器。
Skills 可以映射为可复用 Capability / Provider 能力包。
Agent Memory 必须经过 ExperienceCandidate / Evaluation / Review 治理后才能成为 ExperienceContext。
Multi-Agent、Computer Use、Voice Agent 和 Fine-tuning 都应后置。
```

本项目的核心路线是：

```text
先完成 Runtime 主控，
再完成资产接入，
再完成评估授权，
再完成候选治理、持久化、审计和 Console，
再引入受控 Agent，
再接入 RAG / GraphRAG、ModelProvider、Tool / MCP / Skills，
最终进入生产级能力治理平台。
```
