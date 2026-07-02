# ClinMindRuntime 技术实现总方案

> 上位总设计：`docs/ClinMindRuntime完整系统设计.md`  
> 文档地图：`docs/00_项目设计地图.md`  
> 当前总设计版本：v2.2  
> 当前项目定位：受控医疗 AI Agent Runtime 与能力治理平台  
> 当前状态：Phase 1–5 已冻结；本文档已同步统一 Runtime 主链路、Capability Orchestration、AgentExecutionLayer 和 Runtime Validation。

> 本文档用于说明 ClinMindRuntime 的代码级落地方式：模块如何分层、包结构如何规划、Runtime 主链路如何实现、Agent / Provider / RAG / Model / Tool 如何受控接入、Evaluation 如何执行、数据存储如何演进。  
> 它连接完整系统设计、阶段拆分路线图、全局技术栈、AI 前沿技术规划、模型训练规划、RAG 规划和各 Phase 详细设计，是后续代码实现的总蓝图。

---

# 一、文档定位

已有文档分别回答：

```text
docs/ClinMindRuntime完整系统设计.md
= 系统是什么、为什么这样设计、长期愿景是什么。

docs/ClinMindRuntime阶段拆分路线图.md
= 每个阶段什么时候做什么。

docs/00_项目设计地图.md
= 文档之间如何关联，专项设计如何进入 Phase 实现。

docs/全局技术栈与架构选型.md
= 用什么技术，以及什么时候接入。

docs/AI前沿技术选型与接入规划.md
= Agent、MCP、Tool、LangGraph、GraphRAG、Skills 等怎么接，哪些不能提前接。

docs/医学知识库与RAG构建规划.md
= RAG / KG-lite / GraphRAG 如何作为 EvidenceProvider 进入 Runtime。

docs/模型训练与后训练规划.md
= 模型训练、后训练、模型部署和 ModelProvider 怎么演进。
```

本文档回答：

```text
这些设计最终如何落到代码、模块、依赖、API、Provider、Agent、Tool、存储、测试和部署中。
```

---

# 二、当前实现状态

当前仓库状态：

```text
Phase 1-P0：Runtime MVP，已完成。
Phase 2-P0：共享能力资产原型，已完成。
Phase 3-P0：Evaluation 闭环，已冻结。
Phase 4-P0：Candidate 生成机制，已冻结。
Phase 4-P1：Candidate 治理与安全加固，已冻结。
Phase 5-P0：Persistence / Audit 治理底座，已冻结。
Phase 5-P1：最小 Console API / Access Governance，已冻结。
Phase 5-P2：最小前端 Console MVP，已冻结。
```

当前已实现主干：

```text
Runtime 执行
→ RuntimeTrace
→ Asset Provider
→ Evaluation
→ Candidate
→ Review
→ Persistence
→ Audit
→ Console
```

当前尚未实现：

```text
AgentExecutionLayer
Capability Orchestration 正式实现
Runtime Validation 对外部能力统一校验
真实 RAG EvidenceProvider
KG-lite / GraphRAG Provider
Python AI Provider
ModelProvider / ModelRegistry / TrainingDatasetVersion
ToolAccessPolicy / MCP Adapter / Skills
Multi-Agent / Handoffs
生产级登录 / 多租户 / 正式 RBAC
正式医生审核平台
```

---

# 三、总体技术架构

代码层面采用五层结构：

```text
1. API / Console Layer
2. Application / Runtime Orchestration Layer
3. Domain Capability Layer
4. Provider / Agent / Tool Layer
5. Storage / Integration Layer
```

依赖方向：

```text
API / Console
→ Application / Runtime Orchestration
→ Domain Capability
→ Provider / Agent / Tool Interface
→ Provider / Agent / Tool Implementation
→ Storage / Integration
```

禁止反向依赖：

```text
Provider 不能反向控制 RuntimeService。
Agent 不能直接修改 RuntimeState。
Tool / MCP 不能直接写入 Domain State。
Storage 不能承载医疗判断逻辑。
Console 不能绕过 Safe DTO 和 AccessPolicy。
Model / RAG / LLM 不能直接输出 PatientOutput。
```

---

# 四、统一 Runtime 主链路

最终设计不是多条并行链路，而是一条统一 Runtime 主链路。

目标链路：

```text
用户 / 医生输入
↓
Runtime API
↓
RuntimeService 创建或继续 Runtime
↓
EntryAssessment 判断是否属于临床问诊 / 是否支持处理
↓
RuntimeState / CaseFrame 更新病例状态
↓
KnowledgeContext / ExperienceContext 构建上下文
↓
SafetyGate 初筛高风险
↓
Capability Orchestration 能力编排
  ├── Agent 生成问诊 / 证据 / 改写 / 复盘 Proposal
  ├── RAG / KG-lite / GraphRAG 返回 EvidenceCandidate
  ├── Model Provider 返回结构化 Draft / Candidate / ScoreDraft
  └── Tool / MCP / Skills 返回 ToolResult / ExternalContext
↓
Runtime Validation 校验所有外部能力结果
↓
Runtime 决定采纳 / 部分采纳 / 拒绝 / 降级
↓
DDx Board / EvidenceGraph / QuestionPolicy 更新
↓
DecisionBoundary 判断患者端和医生端可见内容
↓
PatientOutput / ClinicianReport 分角色输出
↓
RuntimeTrace / AuditLog 记录全过程
↓
Evaluation 评估运行结果
↓
Candidate / TrainingExample / CapabilityProfile Proposal 沉淀
↓
Review / Governance 审核
↓
资产 / 经验 / 模型 / Skill / Capability 更新候选
↓
通过评估后进入下一轮 Runtime 可用能力
```

当前 Phase 1–5 已经实现这条链路的治理主干，Phase 6 之后开始在 Capability Orchestration 节点接入 Agent / RAG / Model / Tool。

---

# 五、Java 包结构规划

推荐后续包结构：

```text
com.clinmind.runtime
  api
  application
  domain
  trace
  safety
  boundary
  capability
  agent
  provider
  evidence
  model
  tool
  asset
  evaluation
  candidate
  governance
  console
  persistence
  audit
  config
```

## 5.1 Runtime Core

```text
com.clinmind.runtime.application
  RuntimeService
  RuntimeCommandService
  RuntimeQueryService
  RuntimeContinuationService

com.clinmind.runtime.domain
  RuntimeState
  RuntimeStatus
  CaseFrame
  DifferentialDiagnosisBoard
  EvidenceGraph
  QuestionPolicyState
```

职责：

```text
RuntimeService 是主控入口。
RuntimeState 是运行状态载体。
Domain 对象不感知 Web、DB、LLM、RAG、MCP、前端。
```

## 5.2 Safety / Boundary

```text
com.clinmind.runtime.safety
  SafetyGateService
  RedFlagRuleEvaluator
  FailurePolicy

com.clinmind.runtime.boundary
  DecisionBoundaryService
  PatientOutputMapper
  ClinicianReportMapper
  BoundaryViolation
```

职责：

```text
SafetyGate 判断高风险和失败兜底。
DecisionBoundary 决定患者端和医生端可见内容。
任何 Agent / RAG / Model / Tool 都不能绕过这两个边界。
```

## 5.3 Capability Orchestration

```text
com.clinmind.runtime.capability
  CapabilityOrchestrationService
  CapabilityInvocationPlan
  CapabilityInvocationPolicy
  CapabilityInvocationResult
  CapabilityType
  RuntimeValidationService
  CapabilityResultValidator
  CapabilityDegradationPolicy
```

职责：

```text
判断当前 RuntimeState 下需要调用哪些外部能力。
统一调度 Agent / RAG / Model / Tool。
统一接收外部能力结果。
统一进入 Runtime Validation。
决定采纳、部分采纳、拒绝或降级。
```

Capability Orchestration 不是 Agent，也不是 LangGraph。它是 Java Runtime 内部的受控能力编排节点。

## 5.4 AgentExecutionLayer

```text
com.clinmind.runtime.agent
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

com.clinmind.runtime.agent.inquiry
  InquiryPlanningAgent
  InquiryPlanProposal
  InquiryQuestionCandidate
  InquiryPlanningPolicy
  InquiryPlanProposalValidator
```

职责：

```text
AgentRegistry 管理可用 Agent。
AgentRuntime 执行受控 Agent。
AgentPolicy 判断是否允许调用 Agent。
AgentProposalValidator 校验 Agent 输出。
AgentTrace 记录执行过程。
AgentEvaluationHook 将 Agent 结果接入 Evaluation。
```

Phase 6-P0 首个 Agent：

```text
InquiryPlanningAgent
```

禁止：

```text
Agent 直接修改 RuntimeState。
Agent 直接决定 SafetyGate。
Agent 直接决定 DecisionBoundary。
Agent 直接输出 PatientOutput。
Agent 直接生成最终诊断。
```

## 5.5 Evidence / RAG Provider

```text
com.clinmind.runtime.evidence
  EvidenceProvider
  EvidenceRetrievalRequest
  EvidenceRetrievalResult
  EvidenceCandidate
  EvidenceRef
  EvidenceProviderPolicy
  EvidenceValidationService
  EvidenceScorer

com.clinmind.runtime.evidence.rag
  RagEvidenceProvider
  RagQueryBuilder
  RagRetrievalTrace

com.clinmind.runtime.evidence.graph
  KgLiteProvider
  GraphRagProvider
  KnowledgeNode
  KnowledgeEdge
```

职责：

```text
RAG / KG-lite / GraphRAG 只能返回 EvidenceCandidate / EvidenceRef。
EvidenceCandidate 必须经过 Runtime Validation 后才能进入 EvidenceGraph。
```

## 5.6 ModelProvider

```text
com.clinmind.runtime.model
  ModelProvider
  ModelProviderMetadata
  ModelProviderVersion
  ModelProviderRequest
  ModelProviderResult
  ModelRegistry
  ModelEvaluationPolicy

com.clinmind.runtime.training
  TrainingExampleCandidate
  TrainingDatasetVersion
  TrainingDatasetReviewPolicy
  PreferencePair
```

职责：

```text
模型能力作为 Provider 接入。
模型输出结构化 Draft / Candidate / EvidenceRef / ScoreDraft。
模型不能直接扩大 CapabilityProfile。
模型训练和后训练必须经过 Evaluation / Governance。
```

## 5.7 Tool / MCP / Skills

```text
com.clinmind.runtime.tool
  ToolAccessPolicy
  ToolExecutionRequest
  ToolExecutionResult
  ToolExecutionTrace
  ToolAdapter

com.clinmind.runtime.mcp
  McpAdapter
  McpServerMetadata
  McpResourceRef
  McpToolResult

com.clinmind.runtime.skill
  SkillMetadata
  SkillExecutionPolicy
  SkillProvider
  SkillExecutionTrace
```

职责：

```text
外部工具只能通过 ToolAccessPolicy 调用。
MCP Server 不能直接写入 RuntimeState。
Skill 是可复用能力描述，不是自动授权。
```

## 5.8 Evaluation

```text
com.clinmind.runtime.evaluation
  EvaluationCaseSet
  EvaluationCase
  RuntimeEvaluationRunner
  EvaluationScorer
  EvaluationResult
  EvaluationItemResult
  MetricResult
  SafetyViolation
  RegressionFinding
  CapabilityProfileUpdateProposal
```

Phase 6 后新增：

```text
AgentProposalScorer
InquiryPlanCoverageScorer
AgentTraceCompletenessScorer
CapabilityInvocationScorer
EvidenceRetrievalScorer
ModelProviderScorer
ToolExecutionScorer
```

## 5.9 Candidate / Governance / Audit / Console

```text
com.clinmind.runtime.candidate
  CandidateGenerationService
  ExperienceCandidate
  TrainingExampleCandidate
  CandidateSanitizer
  CandidateReviewService
  CandidateReviewRecord

com.clinmind.runtime.audit
  AuditLog
  AuditLogService
  AuditLogStore

com.clinmind.runtime.console
  ConsoleQueryService
  SafeConsoleDtoMapper
  AccessPolicy
  ActorContext
  DebugTokenFilter
```

职责：

```text
Candidate 不自动上线。
Console 不暴露 raw snapshot。
Audit 记录所有治理动作。
```

---

# 六、Runtime Validation 设计

Runtime Validation 是 Phase 6 之后必须补上的统一校验边界。

输入来源：

```text
AgentProposal
EvidenceCandidate
ModelProviderResult
ToolExecutionResult
SkillExecutionResult
McpToolResult
```

输出：

```text
ValidationAccepted
ValidationPartiallyAccepted
ValidationRejected
ValidationDegraded
```

校验维度：

```text
1. 是否越权修改 RuntimeState。
2. 是否包含患者端禁止内容。
3. 是否绕过 SafetyGate / DecisionBoundary。
4. 是否缺少 source_ref / evidence_ref / provider_version。
5. 是否违反 CapabilityProfile。
6. 是否违反当前角色可见性。
7. 是否高风险但无 fail-safe。
8. 是否可进入 RuntimeTrace / AuditLog。
```

核心接口建议：

```java
public interface CapabilityResultValidator<T> {
    ValidationResult validate(RuntimeState state, T result, ValidationContext context);
}
```

---

# 七、API 规划

## 7.1 已有 API 类型

```text
/api/v1/runtime/**
/api/v1/debug/assets/**
/api/v1/debug/evaluations/**
/api/v1/debug/candidates/**
/api/v1/debug/persistence/**
/api/v1/console/**
```

## 7.2 Phase 6-P0 可新增 Debug API

```text
POST /api/v1/debug/agents/inquiry-planning/run
GET  /api/v1/debug/agents/executions/{execution_id}
GET  /api/v1/debug/agents/executions/{execution_id}/trace
POST /api/v1/debug/agents/proposals/{proposal_id}/validate
```

限制：

```text
只用于 debug / internal。
必须经过 DebugTokenFilter / AccessPolicy。
不得面向 patient-facing client。
不得直接提交 RuntimeState 修改。
```

## 7.3 后续 API

```text
/api/v1/debug/evidence/**
/api/v1/debug/models/**
/api/v1/debug/tools/**
/api/v1/debug/skills/**
```

这些均后置到对应 Phase。

---

# 八、存储与迁移

已实现：

```text
InMemory Store
PostgreSQL Store / Repository
Flyway migration
AuditLog persistence
Console safe query
```

Phase 6-P0 可新增存储对象：

```text
agent_executions
agent_traces
agent_proposals
agent_validation_results
```

但 P0 可以先使用 in-memory + trace object 验证，再决定是否入库。

后续存储扩展：

```text
Phase 7：evidence_chunks、evidence_refs、knowledge_nodes、knowledge_edges、pgvector embeddings。
Phase 8：model_provider_metadata、model_provider_versions、training_dataset_versions。
Phase 9：tool_execution_traces、mcp_server_metadata、skill_metadata。
Phase 10：users、roles、permissions、tenants、review_workflows。
```

---

# 九、测试策略

每个 Phase 必须保持：

```text
1. 后端单元测试。
2. 后端集成测试。
3. in-memory 与 postgres 回归。
4. Console API 安全字段回归。
5. console-web npm run test / npm run build。
```

Phase 6-P0 新增测试：

```text
AgentRegistryTest
AgentPolicyTest
AgentRuntimeTest
InquiryPlanningAgentTest
AgentProposalValidatorTest
AgentTraceIntegrationTest
AgentCannotModifyRuntimeStateTest
AgentCannotBypassDecisionBoundaryTest
InquiryPlanCoverageScorerTest
```

必须证明：

```text
Agent 不能越权。
Agent 输出必须经过 Runtime Validation。
Runtime 能拒绝危险 Proposal。
AgentTrace 可复盘。
Evaluation 能评估 Agent 质量。
```

---

# 十、当前禁止实现清单

在 Phase 6-P0 规格未建立前，不应直接实现：

```text
1. 自由自治式 Agent。
2. 多 Agent 协作。
3. LangGraph / Agent SDK 作为 Runtime 主控。
4. 真实 RAG / GraphRAG。
5. Python AI Provider。
6. MCP / Tool / Skills。
7. 模型训练 / 后训练。
8. 正式登录 / OAuth / 多租户。
9. 正式医生审核平台。
10. ApprovedExperience 自动上线。
11. TrainingDatasetVersion 发布。
12. CapabilityProfile 自动扩大权限。
```

---

# 十一、从本文档进入实现的规则

本文档是技术总蓝图，不是直接任务清单。

正确实现链路：

```text
ClinMindRuntime完整系统设计.md
→ ClinMindRuntime阶段拆分路线图.md
→ ClinMindRuntime技术实现总方案.md
→ 对应专项设计文档
→ 当前 Phase 实现规格
→ 当前 Phase API 与测试设计
→ 当前 Phase 开发任务清单
→ 代码
→ 测试
→ 冻结记录
```

Phase 6-P0 下一步必须新增：

```text
docs/Phase6_P0受控Agent执行层_实现规格.md
docs/Phase6_P0Agent_API与测试设计.md
docs/Phase6_P0开发任务清单.md
```

---

# 十二、最终结论

ClinMindRuntime 的技术实现路线已经从单纯 Runtime 治理主干，升级为统一 Runtime 主链路下的能力治理平台。

核心实现原则：

```text
RuntimeService 仍是主控。
Capability Orchestration 负责受控调度能力。
Agent / RAG / Model / Tool 只能返回结构化候选。
Runtime Validation 决定采纳、拒绝或降级。
SafetyGate 和 DecisionBoundary 永远不能被绕过。
RuntimeTrace / AuditLog / Evaluation / Governance 记录和约束每一次能力扩展。
```

后续代码实现应先从 Phase 6-P0：受控 Agent 执行层 MVP 开始。
