# AI 前沿技术选型与接入规划

> 本文档用于说明 MCP、Tool Calling、Agent SDK、LangGraph、GraphRAG、LLM-as-a-Judge、Skills、Agent Memory、Multi-Agent 等 AI / Agent 前沿技术在 ClinMindRuntime 中的位置、接入阶段和禁止边界。  
> 本文档不是要求当前一次性实现所有前沿技术，而是建立技术雷达，避免项目只停留在普通后端 / 规则系统，也避免因为追逐新技术而破坏 Runtime 主控、安全边界和评估闭环。

---

# 一、文档定位

`docs/全局技术栈与架构选型.md` 解决的是工程技术栈问题：

```text
Java / Spring Boot
PostgreSQL
Redis
pgvector
Neo4j
React
Python FastAPI
Docker
消息队列
权限与审计
```

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

---

# 二、总体判断

ClinMindRuntime 应该吸收 AI / Agent 前沿技术，但不能被它们反向改造成普通 Agent Demo。

核心原则：

```text
1. RuntimeState、SafetyGate、DecisionBoundary、RuntimeTrace 是 ClinMindRuntime 的主控核心。
2. MCP / Tool Calling / RAG / GraphRAG / Agent SDK / LangGraph 只能作为 Provider、Adapter、Evaluation 辅助或外部连接协议。
3. LLM 不能直接决定患者端最终输出。
4. Agent 框架不能替代 Runtime 主流程。
5. 医疗安全边界必须由 Runtime 负责，而不是由提示词或 Agent 框架负责。
6. 前沿技术是否接入，取决于它是否增强资产、证据、评估、经验和治理闭环。
```

---

# 三、AI 技术雷达总表

| 技术 | 当前判断 | 在本项目中的定位 | 推荐接入阶段 | 是否允许成为主控 |
|---|---|---|---|---:|
| Structured Output | Adopt | Provider 返回结构化对象的基础能力 | Phase 4 起 | 否 |
| Function Calling / Tool Calling | Adopt | Java / Python Provider 的工具调用机制 | Phase 4/5 | 否 |
| MCP | Trial / Assess | 外部工具、数据源、Provider 的标准连接协议 | Phase 5 或 Provider 实验 | 否 |
| Spring AI MCP | Assess | Java 侧 MCP Client / Server 候选实现 | Phase 5 | 否 |
| OpenAI Agents SDK | Assess | Agent 编排思想参考；不替代 Runtime | Phase 4/5 实验 | 否 |
| LangGraph | Assess | 状态图 / 可恢复 Agent 思想参考 | Phase 4/5 实验 | 否 |
| LangChain / LlamaIndex | Trial | Python RAG / Agent Provider 实验框架 | Phase 4/5 | 否 |
| GraphRAG | Assess / Trial | EvidenceProvider / KG-lite 的增强方向 | Phase 4/5 后置 | 否 |
| LLM-as-a-Judge | Trial | EvaluationScorer 辅助评分器 | Phase 3-P1 起 | 否 |
| Guardrails | Adopt as concept | 输出校验思想；最终边界仍由 DecisionBoundary 控制 | Phase 3/4 | 否 |
| Skills | Assess | 可复用任务能力包，可映射为 Capability Asset / Provider Skill | Phase 5 | 否 |
| Agent Memory | Assess | 与 ExperienceContext / ExperienceCandidate 对照 | Phase 4 | 否 |
| Multi-Agent / Handoffs | Hold / Assess | 医生审核、评估、资产治理的后续协作模式 | Phase 5 后置 | 否 |
| CrewAI / AutoGen | Hold | 多 Agent 实验参考，不作为医疗 Runtime 主线 | 后置 | 否 |
| Computer Use | Hold | 不适合当前医疗 Runtime 主线 | 暂不接 | 否 |
| Voice / Realtime Agent | Hold / Optional | 语音问诊交互层，不改变 Runtime 主控 | 后置 | 否 |
| Codex / Claude Code / OpenHands | Adopt as dev tool | 开发辅助工具，不属于产品 Runtime | 当前可用 | 否 |
| Fine-tuning / SFT / DPO / RFT | Hold | 模型训练方向，不属于 Phase 3-P0 | 后置 | 否 |

---

# 四、MCP 接入规划

## 4.1 MCP 在本项目中的定位

MCP 是外部工具和数据源的标准连接协议。

在 ClinMindRuntime 中，MCP 的定位是：

```text
External Tool / Data Connector Protocol
```

不是：

```text
Runtime 主控
SafetyGate
DecisionBoundary
EvidenceGraph 主控
患者端输出器
```

## 4.2 正确接入路径

```text
ClinMindRuntime Runtime Core
→ Provider Interface
→ MCP Adapter / MCP Client
→ MCP Server
→ External Tool / External Data Source
```

示例：

```text
EvidenceAssetProvider
→ McpEvidenceConnector
→ hospital-guideline-mcp-server
→ guideline documents / evidence database
```

```text
EvaluationCaseRepository
→ McpCaseSetConnector
→ evaluation-dataset-mcp-server
→ external case set source
```

## 4.3 禁止路径

```text
LLM Agent
→ MCP Tool
→ 直接生成患者端最终回答
```

```text
MCP Server
→ 直接修改 RuntimeState
```

```text
MCP Tool Result
→ 绕过 SafetyGate / DecisionBoundary
```

## 4.4 可能的 MCP Server

后续可以设计：

```text
clinmind-evidence-mcp-server
  暴露 evidence resources / guideline resources

clinmind-evaluation-mcp-server
  暴露 EvaluationCaseSet / EvaluationResult resources

clinmind-asset-mcp-server
  暴露 AssetPackage / AssetMetadata / CapabilityProfile resources

clinmind-audit-mcp-server
  暴露 RuntimeTrace / AuditLog 只读资源
```

## 4.5 接入阶段

```text
Phase 1-P0：不接 MCP。
Phase 2-P0：不接 MCP，只建立 Provider 抽象。
Phase 3-P0：不接 MCP，只建立 Evaluation 闭环。
Phase 4-P1：可做只读 MCP Adapter 原型，用于 Evidence / CaseSet 外部读取。
Phase 5-P0：可考虑 Spring AI MCP / MCP Java SDK 接入，作为外部工具和数据源标准协议。
```

---

# 五、Tool Calling / Function Calling

## 5.1 定位

Tool Calling 是 Provider 调用外部函数或服务的基础能力。

在本项目中，它应该用于：

```text
1. Python AI Provider 调用 embedding / retrieval / normalization 工具。
2. Java Provider 调用外部证据检索服务。
3. EvaluationScorer 调用可选 LLM judge 工具。
4. 后续 Agent / MCP Adapter 内部调用。
```

## 5.2 禁止边界

```text
1. Tool Calling 不能直接改 RuntimeState。
2. Tool Calling 不能直接输出患者端诊断。
3. Tool Calling 结果必须进入结构化对象。
4. Tool Calling 失败时必须 fail-safe 或降级。
```

## 5.3 接入阶段

```text
Phase 3-P0：不需要。
Phase 3-P1：可用于 LLM-as-a-Judge 辅助评分。
Phase 4-P0：用于 Python Provider / RAG / embedding。
Phase 5-P0：用于 MCP / Skills / Console 工具调用。
```

---

# 六、OpenAI Agents SDK / Responses API / Agent SDK 类技术

## 6.1 定位

Agents SDK 类技术可以作为 Agent 编排、工具调用、状态管理、handoff、guardrails、tracing、evaluation 的参考。

但本项目不采用它们作为 Runtime 主控。

原因：

```text
1. ClinMindRuntime 已经有自己的 RuntimeState。
2. 医疗安全边界必须由明确的 SafetyGate / DecisionBoundary 控制。
3. Agent SDK 的编排机制更适合作为外部 AI Provider 或实验层。
4. 医疗问诊不能让通用 Agent Loop 决定最终流程和输出。
```

## 6.2 可借鉴的能力

```text
1. run / state / continuation 思想。
2. tool execution 抽象。
3. handoff / specialist ownership。
4. guardrails / human review。
5. tracing / observability。
6. eval workflow。
```

这些能力可以映射为：

| Agent SDK 能力 | ClinMindRuntime 映射 |
|---|---|
| Agent run state | RuntimeState / RuntimeTrace |
| Tools | Provider / MCP Adapter |
| Handoffs | 后续医生审核 / 资产审核流程 |
| Guardrails | SafetyGate / DecisionBoundary |
| Evals | Phase 3 EvaluationRunner / Scorer |
| Observability | RuntimeTrace / AuditLog |

## 6.3 接入阶段

```text
Phase 3-P0：不接。
Phase 3-P1：可参考 eval / trace 思想。
Phase 4/5：可在 Python AI Provider 或外部实验服务中试用。
```

---

# 七、LangGraph

## 7.1 定位

LangGraph 的价值在于状态图、可恢复执行、人机协作、循环控制和 Agent workflow。

ClinMindRuntime 可以借鉴：

```text
1. graph state。
2. checkpoint / resume。
3. human-in-the-loop。
4. 多节点工作流。
5. 可观测执行图。
```

但不应该直接替代：

```text
RuntimeState
RuntimeService
SafetyGateService
DecisionBoundaryService
RuntimeTrace
```

## 7.2 与本项目的关系

```text
LangGraph = Agent workflow 思想参考或 Python Provider 内部实验框架。
ClinMindRuntime = 医疗 Runtime 主控系统。
```

后续可能使用方式：

```text
Python AI Provider 内部使用 LangGraph 做多步证据检索。
Evaluation 实验服务使用 LangGraph 做自动错误分析。
经验候选挖掘服务使用 LangGraph 做离线工作流。
```

## 7.3 接入阶段

```text
Phase 1–3：不接。
Phase 4-P1：可在 Python experiment 中试用。
Phase 5：如确有需要，可作为 AI Provider 内部工作流，不进入 Runtime Core。
```

---

# 八、LangChain / LlamaIndex

## 8.1 定位

LangChain / LlamaIndex 适合作为 Python 侧 RAG、文档索引、检索增强、工具调用和实验编排框架。

在本项目中，它们属于：

```text
Python Provider / Experiment Layer
```

不是：

```text
Runtime Core
SafetyGate
DecisionBoundary
Asset Governance
```

## 8.2 使用场景

```text
1. Evidence 文档切分与索引。
2. embedding 生成与检索。
3. guideline retrieval。
4. similar case retrieval。
5. trace error analysis。
6. 经验候选发现。
```

## 8.3 接入阶段

```text
Phase 4-P0：可用于 embedding / similar case experiment。
Phase 4-P1：可用于 EvidenceProvider / RAG Provider 原型。
Phase 5-P0：可服务化为 Python AI Provider。
```

---

# 九、GraphRAG

## 9.1 定位

GraphRAG 是 RAG 和图结构知识结合的方向。

在 ClinMindRuntime 中，它可以增强：

```text
EvidenceAssetProvider
KG-lite
ClinicalPathwayRef
Ddx explanation
Evaluation evidence grounding
```

但它不应该直接控制诊断输出。

## 9.2 推荐接入方式

```text
EvidenceAssetProvider
→ GraphRAGProvider / KgLiteEvidenceProvider
→ EvidenceAssetRef / EvidenceGraphItem
→ Runtime EvidenceGraph
→ DecisionBoundary
```

## 9.3 接入阶段

```text
Phase 1–3：不接 GraphRAG。
Phase 4-P1：可做静态 GraphRAG / KG-lite 原型。
Phase 5：可接入 pgvector + PostgreSQL KG-lite。
Neo4j / Milvus 后置。
```

---

# 十、LLM-as-a-Judge / Agent Evaluation

## 10.1 定位

LLM-as-a-Judge 可以作为 EvaluationScorer 的辅助评分器。

适合评价：

```text
1. 患者端表达是否安抚过度。
2. 患者端表达是否包含诊断暗示。
3. 医生端解释是否清晰。
4. 下一步动作是否和证据一致。
5. 回答是否存在不当医疗建议。
```

## 10.2 禁止作为唯一评分依据

医疗场景不能只靠 LLM judge。

必须优先使用结构化评分器：

```text
SafetyGateScorer
PatientBoundaryScorer
DdxCoverageScorer
NextActionScorer
TraceCompletenessScorer
AssetVersionTraceScorer
```

LLM-as-a-Judge 只能作为：

```text
辅助评分器
人工复核提示器
错误分析器
```

## 10.3 接入阶段

```text
Phase 3-P0：不接，先完成确定性 Scorer。
Phase 3-P1：可新增 LlmJudgeScorer。
Phase 4/5：可用于评估报告和错误分析。
```

---

# 十一、Guardrails

## 11.1 定位

Guardrails 是重要思想，但在本项目中不能替代 DecisionBoundary。

本项目已有：

```text
SafetyGate
DecisionBoundary
FailurePolicy
PatientOutputMapper
PatientBoundaryScorer
```

外部 Guardrails 框架可以用于：

```text
1. Provider 输出结构校验。
2. LLM 输出候选检查。
3. Evaluation 辅助评分。
4. Python Provider 侧安全过滤。
```

## 11.2 禁止边界

```text
Guardrails 框架不能成为最终医疗安全边界。
最终安全边界仍由 Java Runtime 决定。
```

---

# 十二、Skills

## 12.1 定位

Skills 可以理解为可复用任务能力包。

在 ClinMindRuntime 中可以映射为：

```text
Capability Asset
Provider Skill
Evaluation Skill
Workflow Skill
```

示例：

```text
chest_pain_triage_skill
fever_red_flag_skill
evidence_retrieval_skill
patient_boundary_check_skill
trace_analysis_skill
```

## 12.2 与 Asset Package 的关系

```text
Skill = 可执行能力描述 / 工具使用方式 / 操作规范。
Asset = 医疗知识、规则、经验、能力边界和评估数据。
Provider = Skill 的执行适配层。
Runtime = Skill 的调用约束者。
```

## 12.3 接入阶段

```text
Phase 1–3：不单独实现 Skills。
Phase 4/5：可把高频能力封装为 Provider Skill。
Phase 5：可在 Asset Console / Training Center 中管理 Skill 元数据。
```

---

# 十三、Agent Memory 与 ExperienceContext

## 13.1 区分

通用 Agent Memory 通常关注：

```text
用户偏好
长期上下文
任务历史
个性化记忆
```

ClinMindRuntime 的 ExperienceContext 更关注：

```text
经验证据是否被验证
是否来自可靠病例或医生审核
是否有适用场景
是否有版本
是否可追踪
是否可撤回
```

## 13.2 本项目的记忆路线

```text
RuntimeTrace
→ EvaluationResult
→ ExperienceCandidate
→ Human Review / Evaluation Gate
→ ExperienceUnitAsset
→ ClinicalExperienceProvider
→ ExperienceContext
```

不是：

```text
模型自动记住用户输入
→ 下次直接使用
```

## 13.3 接入阶段

```text
Phase 2-P0：ExperienceContext mock / prototype。
Phase 3-P0：EvaluationResult 作为经验质量证据。
Phase 4-P0：ExperienceCandidate 挖掘和治理。
Phase 5：经验审核、发布、回滚和再认证。
```

---

# 十四、Multi-Agent / Handoffs

## 14.1 定位

Multi-Agent 不适合作为当前医疗 Runtime 主线。

原因：

```text
1. 医疗安全场景需要确定主控边界。
2. 多 Agent 自由协商容易导致责任边界不清。
3. 患者端输出必须稳定、可解释、可审计。
```

但 Multi-Agent / Handoffs 可以用于后续：

```text
1. 医生审核辅助。
2. 资产审核流程。
3. 评估报告分析。
4. 经验候选复核。
5. 多角色后台协作。
```

## 14.2 接入阶段

```text
Phase 1–4：不接 Multi-Agent 主线。
Phase 5-P1：可在后台审核 / 资产治理中试验。
```

---

# 十五、Computer Use / Shell / Browser Agent

## 15.1 当前判断

暂不适合 ClinMindRuntime 主线。

原因：

```text
1. 医疗 Runtime 不需要操作用户电脑。
2. Computer Use 风险较高。
3. 本项目重点是 Runtime、安全、资产、评估和治理。
```

## 15.2 可选后置场景

```text
1. 自动生成测试报告。
2. 自动操作内部管理台进行验收。
3. 开发辅助。
```

但不进入患者端 Runtime。

---

# 十六、Voice / Realtime Agent

## 16.1 定位

语音和实时 Agent 是交互层能力，不是 Runtime 主控能力。

后续可用于：

```text
ASR
TTS
Realtime conversation
voice triage interface
```

但链路仍应是：

```text
Voice Input
→ ASR
→ RuntimeService
→ PatientOutput / ClinicianReport
→ TTS
```

而不是：

```text
Realtime Agent
→ 直接完成医疗问诊和输出
```

## 16.2 接入阶段

```text
Phase 5 后置，或作为独立交互层实验。
```

---

# 十七、开发型 Agent：Codex / Claude Code / OpenHands

## 17.1 定位

这类工具属于开发辅助，不属于 ClinMindRuntime 产品运行时。

允许用于：

```text
1. 生成代码。
2. 写测试。
3. 重构。
4. 生成文档。
5. 做静态检查。
6. 辅助调试。
```

必须受以下文档约束：

```text
docs/AI_IMPLEMENTATION_SKILL.md
docs/Phase*_开发任务清单.md
docs/全局技术栈与架构选型.md
docs/AI前沿技术选型与接入规划.md
```

---

# 十八、Fine-tuning / SFT / DPO / RFT

## 18.1 当前判断

暂不属于 ClinMindRuntime 的 P0 主线。

原因：

```text
1. 当前项目目标是 Runtime / Asset / Evaluation / Experience / Governance。
2. 训练基础模型成本高且不适合作为个人项目主线。
3. 医疗场景更需要可控、可追踪、可评估的系统边界。
```

## 18.2 可能的后续位置

```text
1. 微调结构化抽取模型。
2. 微调 patient-safe expression 模型。
3. 蒸馏小模型用于本地分类。
4. 用 EvaluationCaseSet 作为训练 / 验证数据来源。
```

接入阶段：

```text
Phase 5 后置或独立研究分支。
```

---

# 十九、按 Phase 的 AI 前沿技术接入边界

## Phase 1-P0

启用：

```text
无真实 AI 前沿技术接入。
只建立 Runtime 主控、状态、安全门、Trace。
```

只借鉴：

```text
Agent runtime / guardrails / trace 的思想。
```

## Phase 2-P0

启用：

```text
Provider 抽象
Asset Package
CapabilityProfile
ExperienceContext prototype
```

只借鉴：

```text
Skills / Agent Memory / Tool Provider 的思想。
```

不接：

```text
MCP
LangGraph
真实 RAG
GraphRAG
Python AI Provider
```

## Phase 3-P0

启用：

```text
EvaluationRunner
EvaluationCaseSet
Scorer
EvaluationResult
CapabilityProfileUpdateProposal
```

只借鉴：

```text
Agent Evals
LLM-as-a-Judge 的评估思想
Observability / Trace-driven improvement
```

不接：

```text
LLM-as-a-Judge 真正调用
MCP
Agent SDK
LangGraph
Python evaluation platform
```

## Phase 3-P1

可接：

```text
LlmJudgeScorer
Python offline evaluation notebook
Trace error clustering
Evaluation report visualization
```

## Phase 4-P0

可接：

```text
Python AI Provider prototype
embedding
similar case retrieval
ExperienceCandidate mining
LangChain / LlamaIndex experiment
```

## Phase 4-P1

可接：

```text
RAG EvidenceProvider
GraphRAG / KG-lite prototype
MCP read-only adapter prototype
```

## Phase 5-P0

可接：

```text
MCP Client / Server
Spring AI MCP
Provider Skills
Asset Console
Training Center
Python AI Provider service
pgvector-backed retrieval
```

## Phase 5-P1 或后置

可评估：

```text
Multi-Agent review workflow
Computer Use for internal QA
Voice / Realtime interaction layer
Neo4j / Milvus / Qdrant
Fine-tuning / DPO / RFT
```

---

# 二十、最终架构关系

```text
Patient / Clinician / Console
→ ClinMindRuntime API
→ RuntimeService
→ RuntimeState
→ Module Services
→ Provider Interfaces
→ Java Provider / Python Provider / MCP Adapter
→ External AI / RAG / KG / Tool / Data Source
→ Structured Result
→ Runtime Validation
→ SafetyGate / DecisionBoundary
→ PatientOutput / ClinicianReport
→ RuntimeTrace / Evaluation / Audit
```

核心结论：

```text
前沿 AI 技术负责扩展能力，
ClinMindRuntime 负责约束能力。
```

---

# 二十一、最终结论

```text
ClinMindRuntime 必须体现对 AI / Agent 前沿技术的理解，
但不能为了使用新技术而破坏医疗 Runtime 的主控边界。

MCP 是未来外部工具和数据源的标准协议。
Tool Calling 是 Provider 调用外部能力的基础机制。
LangGraph / Agent SDK 是 Runtime 和编排思想的重要参考。
GraphRAG 是 EvidenceProvider 和 KG-lite 的后续增强方向。
LLM-as-a-Judge 是 Phase 3 评估体系的辅助评分器。
Skills 可以映射为可复用 Capability / Provider 能力包。
Agent Memory 必须经过 ExperienceCandidate / Evaluation / Review 治理后才能成为 ExperienceContext。
Multi-Agent、Computer Use、Voice Agent 和 Fine-tuning 都应后置。

本项目的核心路线是：
先完成 Runtime 主控，
再完成资产接入，
再完成评估授权，
再引入 AI Provider / MCP / RAG / GraphRAG / Skills，
最后进入经验进化和平台治理。
```
