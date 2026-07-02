# ClinMindRuntime 完整系统设计

> 项目名称：ClinMindRuntime  
> 当前总设计版本：v2.0  
> 中文定位：受控医疗 AI Agent Runtime 与能力治理平台  
> 核心定义：ClinMindRuntime 不是普通医疗问答、普通 RAG 应用或自由自治式医疗 Agent，而是一个以 Runtime 为主控、以 Agent / RAG / Model / Tool 为受控能力单元、以 Evaluation / Audit / Governance 为闭环的医疗 AI 运行与治理平台。

---

# 一、项目定位

ClinMindRuntime 是一个面向医疗问诊场景的 **受控医疗 AI Agent Runtime 与能力治理系统**。

它的核心目标不是“让模型直接回答患者”，而是：

```text
让 AI 能力在一个可控、可追踪、可评估、可审计、可回滚的医疗 Runtime 中运行。
```

它不是：

```text
普通 RAG 医疗问答
普通 Prompt Chain
普通多 Agent Demo
自由自治式 AI 医生
直接训练一个模型替代医生
让 LLM 直接决定患者端最终输出
```

它是：

```text
Runtime 主控
+ Agent 受控执行
+ 医学知识与 EvidenceProvider
+ RAG / KG-lite / GraphRAG 证据增强
+ Model Provider 能力接入
+ Tool / MCP / Skills 受控外部能力
+ Evaluation 评估授权
+ Candidate / Experience 治理
+ Persistence / Audit / Console 平台治理
```

当前项目已经完成的主干是：

```text
Runtime 执行
→ Trace 记录
→ Asset Provider
→ Evaluation 评估
→ Candidate 候选沉淀
→ Review 治理
→ Persistence 持久化
→ Audit 审计
→ Console 可视化治理入口
```

下一阶段的核心不是继续堆 Console 或边界声明，而是把 Agent / RAG / Model / Tool 等能力纳入统一总架构和可执行 Phase。

---

# 二、系统核心目标

系统需要在医疗 AI 场景中回答以下问题：

```text
1. 当前输入是否属于临床问诊，是否支持处理。
2. 当前病例属于哪个症状群。
3. 当前病例已知信息、缺失信息和冲突信息是什么。
4. 哪些高风险信号必须优先识别。
5. 哪些候选诊断需要保留、排除或继续追问。
6. 哪些证据支持、反对或仍然缺失。
7. 下一步应该追问、建议检查，还是触发安全分诊。
8. 哪些内容允许患者端看到，哪些只能医生端看到。
9. 哪些 Agent 可以参与本轮任务，Agent 只能生成什么 Proposal。
10. 哪些 Tool / MCP / Skills 可以被调用，调用结果如何校验。
11. 哪些模型能力可以作为 Provider 接入，哪些不能越过 Runtime。
12. 当前能力是否通过 Evaluation 授权。
13. 运行结果是否可以生成经验候选、训练样本候选或知识资产改进建议。
14. 所有过程是否可追踪、可审计、可复盘、可回滚。
```

系统角色定位：

```text
患者端：风险提示、信息补全、就医建议、健康教育、检查准备说明。
医生端：候选诊断、证据状态、相似经验提醒、检查建议、医生摘要。
后台端：病例复盘、错误归因、候选治理、训练数据沉淀、经验候选发现、再认证评估、审计治理。
```

---

# 三、核心设计：八个能力域

ClinMindRuntime 的完整系统能力不应只按 Runtime、RAG、Agent、模型等技术名词拆分，而应按“医疗 AI 运行需要什么能力”拆分。

完整能力域为八个：

```text
1. Runtime 状态与流程域
2. 医学知识与证据域
3. 临床经验与记忆域
4. Agent 受控执行域
5. Tool / MCP / Skills 外部能力域
6. 模型能力与 Provider 域
7. 输出边界与安全治理域
8. 评估、审计与持续进化域
```

核心公式：

```text
受控医疗 AI Agent Runtime
= Runtime 主控
+ 医学知识资产
+ 临床经验治理
+ Agent 受控执行
+ Tool / MCP / Skills 受控调用
+ Model Provider
+ Safety / DecisionBoundary
+ Evaluation / Audit / Governance
```

## 3.1 Runtime 状态与流程域

Runtime 状态与流程域负责一次问诊当下如何运行。

核心对象：

```text
RuntimeService
RuntimeState
RuntimeStatus
RuntimeMode
WorkMode
EntryAssessment
CaseFrame
Short-term Context
RuntimeTrace
FailurePolicy
```

职责：

```text
1. 接收用户 / 医生输入。
2. 创建或继续一次 Runtime session。
3. 更新病例结构化状态。
4. 调度知识、经验、Agent、Provider、Tool 等能力。
5. 保持所有能力都在 Runtime 边界内运行。
6. 记录 Trace，供 Evaluation、Audit、Candidate 复盘使用。
```

## 3.2 医学知识与证据域

医学知识与证据域负责提供稳定、可版本化、可追踪的医学知识资产。

核心对象：

```text
Symptom Rotation Library
Clinical Pathway
Diagnosis Pathway
Question Pathway
Test Recommendation Rules
Red Flag Rules
RAG Evidence Library
KG-lite
GraphRAG Evidence
MedicalKnowledgeProvider
EvidenceAssetProvider
EvidenceRef
EvidenceCandidate
EvidenceGraph
```

定位：

```text
知识库不是“文档切块 + 向量检索 + 拼 Prompt”。
知识库是受 RuntimeState、EvidenceGraph、SafetyGate、DecisionBoundary 和资产版本治理约束的医学知识资产系统。
```

正确链路：

```text
RuntimeState / CaseFrame
→ KnowledgeContextService
→ EvidenceAssetProvider
→ RAG / KG-lite / GraphRAG
→ EvidenceRef / EvidenceCandidate
→ EvidenceGraph
→ SafetyGate / QuestionTestPolicy / DecisionBoundary
→ PatientOutput / ClinicianReport
```

禁止链路：

```text
User Input
→ RAG
→ LLM
→ Patient Answer
```

## 3.3 临床经验与记忆域

临床经验与记忆域负责从病例、评估、医生反馈、误诊教训和复盘中沉淀经验。

核心对象：

```text
Clinical Experience Memory
ExperienceUnitAsset
ExperienceCandidate
TrainingExampleCandidate
ClinicalExperienceProvider
ExperienceContext
ExperienceCandidateMiner
Experience Memory Governance
```

原则：

```text
经验可以提高系统警觉性、追问优先级和输出边界收紧；
经验不能直接决定最终诊断，不能绕过 Evaluation / Review / Governance 自动生效。
```

经验路线：

```text
RuntimeTrace
→ EvaluationResult
→ RegressionFinding / SafetyViolation
→ ExperienceCandidate
→ Human Review / Evaluation Gate
→ ExperienceUnitAsset
→ ClinicalExperienceProvider
→ ExperienceContext
```

## 3.4 Agent 受控执行域

Agent 受控执行域是 v2.0 总设计新增的核心能力域。

它负责让 Agent 在 Runtime 授权范围内执行受限任务，例如问诊规划、证据组织、医生摘要、错误复盘、候选挖掘等。

Agent 不是 Runtime 主控替代物。

Agent 的定位是：

```text
Runtime 授权下的临床任务执行单元。
```

Agent 只能生成：

```text
Proposal
Draft
Candidate
Finding
ScoreDraft
```

Agent 不能直接生成：

```text
Final Diagnosis
Final Patient Answer
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
ApprovedExperience
PublishedTrainingDataset
```

核心对象：

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
```

推荐首个 Agent：

```text
InquiryPlanningAgent
```

其职责：

```text
根据 RuntimeState、CaseFrame、symptom_group、missing_facts、red_flag_candidates、EvidenceGraph 和 CapabilityProfile，生成下一轮追问计划 Proposal。
```

运行关系：

```text
RuntimeState
→ AgentExecutionRequest
→ AgentRuntime
→ Controlled Agent
→ AgentProposal
→ AgentProposalValidator
→ Runtime Commit / Partial Commit / Reject / Degrade
→ RuntimeTrace / AuditLog
```

## 3.5 Tool / MCP / Skills 外部能力域

Tool / MCP / Skills 外部能力域负责连接外部数据源、工具服务和可复用能力包。

核心对象：

```text
ToolAccessPolicy
ToolExecutionRequest
ToolExecutionResult
ToolExecutionTrace
McpAdapter
McpClient
McpResourceRef
SkillMetadata
SkillExecutionPolicy
ProviderSkill
```

定位：

```text
Tool Calling = Agent / Provider 内部工具调用机制。
MCP = 外部工具和数据源连接协议。
Skills = 可复用任务能力包或 Provider 能力包。
```

正确链路：

```text
Runtime / Agent / Provider
→ ToolAccessPolicy
→ Tool / MCP / Skill Adapter
→ External Tool / Data Source
→ Structured Tool Result
→ Runtime Validation
→ Trace / Audit
```

禁止链路：

```text
LLM Agent
→ Tool / MCP
→ 直接修改 RuntimeState
```

```text
Tool Result
→ 绕过 SafetyGate / DecisionBoundary
→ PatientOutput
```

## 3.6 模型能力与 Provider 域

模型能力与 Provider 域负责承载可训练、可替换、可评估、可部署的 AI 能力。

核心对象：

```text
IntentClassifierProvider
WorkModeClassifierProvider
SymptomGroupClassifierProvider
RiskSignalClassifierProvider
CaseFrameExtractorProvider
EmbeddingModelProvider
EvidenceRetrieverProvider
EvidenceRerankerProvider
PatientSafeRewriteProvider
ClinicianReportDraftProvider
LlmJudgeScorer
ExperienceCandidateMiner
ModelProviderMetadata
ModelProviderVersion
TrainingDatasetVersion
ModelRegistry
```

模型训练不是训练一个替代医生的大模型。

正确关系：

```text
Training / Post-training
→ Model Provider
→ Structured Draft / Candidate / EvidenceRef
→ Runtime Validation
→ SafetyGate
→ DecisionBoundary
→ PatientOutput / ClinicianReport
→ RuntimeTrace
→ EvaluationResult
→ 再训练 / 资产更新 / 经验治理
```

禁止关系：

```text
训练一个模型
→ 模型直接回答患者
→ 绕过 Runtime / SafetyGate / DecisionBoundary
```

## 3.7 输出边界与安全治理域

输出边界与安全治理域负责决定系统当前能不能说、能说到什么程度、患者端和医生端分别能看到什么。

核心对象：

```text
CapabilityProfile
SafetyGate
DecisionBoundary
FailurePolicy
PatientOutputService
ClinicianReportService
PatientSafeExpressionDraft
PatientBoundaryScorer
Role / Permission / AccessPolicy
```

原则：

```text
不是先生成答案再加免责声明，
而是先判断当前是否允许输出。
```

患者端禁止直接暴露：

```text
DDx Board
完整 EvidenceGraph
must_not_miss 内部标签
医生端推理链
未经边界控制的诊断结论
未经审核的治疗建议
未脱敏候选输入
```

## 3.8 评估、审计与持续进化域

评估、审计与持续进化域负责证明能力、发现错误、沉淀候选、治理上线和回滚。

核心对象：

```text
EvaluationCaseSet
EvaluationRunner
EvaluationScorer
EvaluationResult
EvaluationItemResult
MetricResult
SafetyViolation
RegressionFinding
CapabilityProfileUpdateProposal
CandidateGenerationService
ExperienceCandidate
TrainingExampleCandidate
CandidateReview
AuditLog
Runtime Console
Audit Center
Review Queue
Recertification
```

能力上线必须经过：

```text
EvaluationResult
→ CapabilityProfileUpdateProposal
→ Review / Governance
→ CapabilityProfileAsset
→ DecisionBoundary
```

候选经验上线必须经过：

```text
ExperienceCandidate
→ Sanitization
→ SourceRef Validation
→ Review
→ Evaluation / Governance
→ ExperienceUnitAsset
→ Runtime 可用经验
```

---

# 四、总体设计原则

## 4.1 Runtime 主控

```text
RuntimeService、RuntimeState、SafetyGate、DecisionBoundary、RuntimeTrace 是系统主控核心。
```

模型、RAG、MCP、Tool Calling、Agent SDK、LangGraph、GraphRAG、Skills、Multi-Agent 都不能替代 Runtime 主控。

## 4.2 Agent 受控

```text
Agent 可以提出下一步行动建议；
Runtime 决定是否采纳、拒绝、降级或写入状态。
```

Agent 不允许直接修改 RuntimeState，不允许直接决定 SafetyGate 或 DecisionBoundary，不允许直接生成患者端最终医疗结论。

## 4.3 RAG 证据化

```text
RAG / GraphRAG 只能返回 EvidenceRef / EvidenceCandidate；
EvidenceGraph 决定证据在本病例中的作用；
DecisionBoundary 决定证据能否暴露给患者端。
```

## 4.4 模型 Provider 化

```text
模型能力只能作为 Provider 返回结构化候选，不能成为医疗输出主控。
```

模型上线必须有 EvaluationResult、版本记录、回滚机制和 CapabilityProfile 授权。

## 4.5 Tool / MCP / Skills 最小权限

```text
外部工具调用必须经过 ToolAccessPolicy，结果必须进入结构化对象，失败必须 fail-safe 或降级。
```

MCP 是连接协议，不是系统主控。

Skills 是可复用能力包，不是绕过 Runtime 的执行入口。

## 4.6 安全优先

SafetyGate 和 DecisionBoundary 优先于自然语言生成。

高风险医疗场景必须 fail-closed，而不是在资产、模型、工具失败时静默降级。

## 4.7 证据优先

系统不直接输出疾病结论，而是维护候选诊断与证据之间的关系。

```text
候选诊断
支持证据
反对证据
缺失证据
冲突证据
必须追问
推荐检查
输出权限
```

## 4.8 评估授权

能力不是口头声明，而要通过评估授权。

```text
Runtime / Agent / RAG / Model / Tool 能力
→ EvaluationResult
→ Governance Review
→ CapabilityProfile / Asset / Provider Version
→ Runtime 可用能力
```

## 4.9 可追踪、可复盘、可回滚

每一次问诊都必须记录：

```text
输入是什么
医学知识使用了什么
RAG / GraphRAG 返回了什么 EvidenceCandidate
模型 Provider 输出了什么候选
Agent 生成了什么 Proposal
Tool / MCP / Skills 调用了什么
临床经验触发了什么
CaseFrame 如何变化
SafetyGate 是否触发
DDx 如何变化
EvidenceGraph 如何变化
为什么输出被限制
医生是否采纳
最终结局是什么
```

---

# 五、总体架构

## 5.1 五层结构

完整系统由五层组成：

```text
1. 平台治理层
   负责 Console、评估、审核、发布、回滚、权限、审计、模型注册和资产治理。

2. 共享能力资产层
   承载医学知识、规则、路径、经验、能力边界、评估病例、模型版本和 Skill 元数据。

3. Runtime 执行层
   负责一次问诊当下从输入、状态更新、Agent 调度、证据组织到安全输出的主流程。

4. Provider / Agent / Tool 能力层
   承载 Agent、Model Provider、RAG Provider、Tool Adapter、MCP Adapter、Skills 等可替换能力。

5. Storage / Integration 层
   承载 PostgreSQL、pgvector、Redis、外部 LLM API、Python AI Provider、MCP Server、外部知识库等。
```

## 5.2 层间关系

```text
平台治理层
  ↓ 管理 / 评估 / 审核 / 发布 / 回滚 / 审计
共享能力资产层
  ↓ 被 Runtime 读取 / 检索 / 约束 / 版本追踪
Runtime 执行层
  ↓ 授权调用受控能力，产生 Trace / Evaluation / Candidate / Audit
Provider / Agent / Tool 能力层
  ↓ 返回结构化 Proposal / Draft / Candidate / EvidenceRef / ToolResult
Runtime 执行层继续校验、采纳、拒绝、降级和输出
Storage / Integration 层提供持久化与外部服务连接
```

## 5.3 Runtime 执行层内部结构

```text
Runtime 执行层
├── Runtime API
├── RuntimeService
├── RuntimeState / RuntimeStatus
├── EntryAssessment
├── CaseFrame
├── KnowledgeContext
├── ExperienceContext
├── SafetyGate
├── DifferentialDiagnosisBoard
├── EvidenceGraph
├── QuestionTestPolicy
├── Agent Execution Layer
│   ├── AgentRegistry
│   ├── AgentRuntime
│   ├── AgentPolicy
│   ├── AgentContext
│   ├── AgentProposalValidator
│   ├── AgentTrace
│   └── AgentEvaluationHook
├── Tool / Provider Access Layer
│   ├── ProviderInterface
│   ├── ToolAccessPolicy
│   ├── McpAdapter
│   └── SkillExecutionPolicy
├── DecisionBoundary
├── PatientOutputService
├── ClinicianReportService
├── FailurePolicy
└── RuntimeTrace
```

---

# 六、核心运行链路

## 6.1 当前 Runtime 主链路

当前已落地的 Runtime-first 主链路是：

```text
用户 / 医生输入
↓
Runtime API 创建或继续 Runtime
↓
EntryAssessment 判断工作态
↓
RuntimeState / CaseFrame 更新
↓
Knowledge Context 查询医学知识资产
↓
Experience Context 检索已验证经验
↓
SafetyGate 危险信号识别
↓
Differential Diagnosis Board 构建候选诊断状态
↓
EvidenceGraph 组织证据关系
↓
Question / Test Policy 决定下一步追问或检查建议
↓
DecisionBoundary 判断当前允许输出什么
↓
PatientOutput / ClinicianReport 生成分角色输出
↓
RuntimeTrace 记录知识、经验、模型候选、资产版本和输出边界
↓
Evaluation / Feedback / Outcome 进入复盘与训练数据候选
```

## 6.2 目标 Agent Runtime 链路

下一阶段目标链路是：

```text
用户 / 医生输入
↓
Runtime API 创建或继续 Runtime
↓
EntryAssessment 判断工作态
↓
RuntimeState / CaseFrame 更新
↓
SafetyGate 初筛
↓
KnowledgeContext / ExperienceContext 构建
↓
AgentPolicy 判断本轮是否允许调用 Agent
↓
AgentRuntime 调用一个或多个受控 Agent
↓
Agent 输出结构化 Proposal
↓
AgentProposalValidator 校验 Proposal
↓
Runtime 决定采纳 / 部分采纳 / 拒绝 / 降级
↓
DDx Board / EvidenceGraph / QuestionPolicy 局部更新
↓
DecisionBoundary 判断患者端和医生端可见内容
↓
PatientOutput / ClinicianReport 生成分角色输出
↓
RuntimeTrace 记录 Agent 输入、输出、采纳结果、拒绝原因
↓
Evaluation / Feedback / Audit 进入治理闭环
```

核心原则：

```text
Agent 不替代 Runtime。
Agent 不直接修改 RuntimeState。
Agent 不直接输出患者端最终内容。
Agent 只生成可校验、可拒绝、可追踪的 Proposal。
```

## 6.3 目标 RAG / Evidence 链路

```text
RuntimeState / CaseFrame
↓
KnowledgeContextService
↓
EvidenceAssetProvider
↓
RAG / KG-lite / GraphRAG Provider
↓
EvidenceRetrievalResult
↓
EvidenceCandidate
↓
EvidenceGraphItem
↓
EvidenceGraph
↓
QuestionTestPolicy / SafetyGate / DecisionBoundary
↓
PatientOutput / ClinicianReport
```

## 6.4 目标 Model Provider 链路

```text
TrainingDatasetVersion / EvaluationCaseSet / RuntimeTrace
↓
Model Training / Post-training
↓
ModelProviderVersion
↓
EvaluationResult
↓
CapabilityProfileUpdateProposal
↓
Review / Governance
↓
Runtime 可用 Provider
↓
Provider 输出 Structured Draft / Candidate / EvidenceRef
↓
Runtime Validation
```

---

# 七、实现状态总览

## 7.1 已完成主干

当前已经完成并形成治理闭环的主干包括：

```text
Phase 1：Runtime MVP
Phase 2：共享能力资产原型
Phase 3：Evaluation 闭环
Phase 4-P0：Candidate 沉淀机制
Phase 4-P1：Candidate 脱敏、来源校验、Review 记录
Phase 5-P0：Persistence / PostgreSQL / AuditLog 治理底座
Phase 5-P1：最小 Console API / RBAC-lite / Safe DTO / Audit Center
Phase 5-P2：最小前端 Console MVP
```

已实现主线：

```text
Runtime 执行
→ Trace
→ Asset Provider
→ Evaluation
→ Candidate
→ Review
→ Persistence
→ Audit
→ Console
```

## 7.2 已设计但尚未完整实现

```text
Agent Execution Layer
RAG EvidenceProvider / GraphRAG Provider
Python AI Provider
ModelProvider / ModelRegistry / TrainingDatasetVersion 正式链路
ToolAccessPolicy / MCP Adapter / Skills
Multi-Agent / Handoffs 后台协作
正式医生审核平台
正式登录 / 多租户 / 生产级 RBAC
```

## 7.3 当前最大架构缺口

当前最大缺口不是 Runtime 治理，而是：

```text
Agent 被写在 AI 前沿技术规划中，
但还没有进入总设计能力域、运行链路、接口契约和 Phase 实现闭环。
```

因此下一阶段应优先补：

```text
Phase 6-P0：受控 Agent 执行层 MVP
```

---

# 八、阶段路线总览

## 8.1 已完成阶段

```text
Phase 1-P0：Runtime MVP
  证明受控医疗 Runtime 能跑通。

Phase 2-P0：共享能力资产原型
  证明 Runtime 能通过 Provider 读取可替换、可版本化、可追踪资产。

Phase 3-P0：训练与评估闭环
  证明能力可以通过 EvaluationCaseSet、Scorer 和 EvaluationResult 评估。

Phase 4-P0：经验候选与训练数据候选沉淀
  从 Evaluation 暴露的问题中生成可追踪、可审核、不可自动生效的候选。

Phase 4-P1：候选治理与安全加固
  加入脱敏、来源校验和 review 记录。

Phase 5-P0：持久化与治理底座
  PostgreSQL、Repository 双实现、AuditLog、Persistence health。

Phase 5-P1：最小 Console API 与访问治理
  RBAC-lite、Safe DTO、Audit Center、Console API。

Phase 5-P2：最小前端 Console MVP
  Runtime / Evaluation / Candidate / Review Queue / Audit Center 可视化入口。
```

## 8.2 建议后续阶段

```text
Phase 6-P0：受控 Agent 执行层 MVP
  引入 AgentRegistry、AgentRuntime、AgentPolicy、AgentProposal、AgentTrace。
  首个 Agent 建议为 InquiryPlanningAgent。

Phase 6-P1：Agent 能力扩展
  增加 EvidenceOrganizationAgent、PatientRewriteAgent、TraceReviewAgent 等受控 Agent。

Phase 7-P0：RAG EvidenceProvider MVP
  建立真实 RAG EvidenceProvider、EvidenceRetrievalResult、RAG 评估指标。

Phase 7-P1：KG-lite / GraphRAG 原型
  引入 PostgreSQL node / edge 或轻量图证据关系，GraphRAG 只增强 EvidenceGraph。

Phase 8-P0：Python AI Provider / ModelProvider MVP
  服务化模型能力，先接入 intent、caseframe、risk、patient-safe rewrite、judge 等 Provider。

Phase 8-P1：模型训练与后训练治理
  TrainingDatasetVersion、ModelProviderVersion、ModelRegistry、模型评估与回滚。

Phase 9-P0：Tool / MCP / Skills 受控接入
  ToolAccessPolicy、McpAdapter、SkillMetadata、ToolExecutionTrace。

Phase 10-P0：生产级平台治理
  正式登录、多租户、生产 RBAC、正式医生审核平台、发布与回滚工作流。
```

---

# 九、专项规划文档与总设计关系

现有专项规划文档不应独立漂在总设计之外，而应作为本总设计的子系统展开。

```text
docs/AI前沿技术选型与接入规划.md
→ 对应 Agent 受控执行域、Tool / MCP / Skills 外部能力域、Evaluation 辅助能力。

docs/医学知识库与RAG构建规划.md
→ 对应医学知识与证据域、EvidenceProvider、RAG / KG-lite / GraphRAG。

docs/模型训练与后训练规划.md
→ 对应模型能力与 Provider 域、TrainingDatasetVersion、ModelProviderVersion、ModelRegistry。

docs/数据安全与合规边界规划.md
→ 对应输出边界与安全治理域、Audit、脱敏、患者端隔离。

docs/数据库持久化设计.md
→ 对应 Persistence、AuditLog、Repository、PostgreSQL、pgvector 后续扩展。
```

总设计负责定义它们在系统中的正式位置；专项文档负责展开具体接口、数据结构、接入阶段和禁止边界。

---

# 十、当前不做什么

即使总设计已纳入 Agent / RAG / Model / Tool / MCP，也不代表当前阶段一次性实现这些能力。

当前仍不做：

```text
1. 不让 Agent 接管 Runtime 主控。
2. 不让 Agent 直接生成患者端最终诊断。
3. 不让 RAG / GraphRAG 直接回答患者。
4. 不让模型直接修改 RuntimeState。
5. 不让 Tool / MCP 直接写入系统核心状态。
6. 不自动上线 ExperienceCandidate。
7. 不自动发布 TrainingDatasetVersion。
8. 不自动修改 AssetPackage / CapabilityProfile。
9. 不把 LLM-as-a-Judge 作为唯一评分依据。
10. 不把专项规划当作当前实现完成状态。
```

---

# 十一、系统最终闭环

完整目标闭环：

```text
医学知识资产 / 症状群资产 / 评估病例 / 模型版本 / Agent 能力 / Tool 能力
  ↓
Runtime 主控执行
  ↓
Agent / RAG / Model / Tool 受控产生 Proposal / Candidate / EvidenceRef / Draft
  ↓
Runtime 校验、采纳、拒绝或降级
  ↓
SafetyGate / EvidenceGraph / DecisionBoundary 控制输出
  ↓
PatientOutput / ClinicianReport
  ↓
RuntimeTrace / AuditLog
  ↓
EvaluationRunner / Scorer
  ↓
EvaluationResult / SafetyViolation / RegressionFinding
  ↓
CapabilityProfileUpdateProposal / ExperienceCandidate / TrainingExampleCandidate
  ↓
Review / Governance
  ↓
Asset / Experience / Model / Skill / Capability 更新候选
  ↓
再次 Evaluation
  ↓
通过后进入 Runtime 可用能力
```

---

# 十二、最终结论

ClinMindRuntime 的最终形态不是一个“会回答医学问题的模型”，也不是一个自由自治式医疗 Agent。

它的最终形态是：

```text
受控医疗 AI Agent Runtime 与能力治理平台
```

核心路线是：

```text
先让 Runtime 站起来，
再让资产接进来，
再让评估管住它，
再让候选和经验沉淀出来，
再让持久化、审计和 Console 完成治理闭环，
然后引入受控 Agent，
再接入 RAG / GraphRAG、ModelProvider、Tool / MCP / Skills，
最终形成可评估、可审计、可回滚、可持续进化的医疗 AI 能力平台。
```

所有 AI 能力都必须服务这条主线，而不能反过来取代 Runtime 主控。
