# ClinMindRuntime 完整系统设计

> 项目名称：ClinMindRuntime  
> 当前总设计版本：v3.0  
> 中文定位：受控医疗 AI Agent Runtime 与能力治理平台  
> 阶段路线：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md` v3.0  
> 技术蓝图：`docs/1-总设计/ClinMindRuntime技术实现总方案.md` v3.0  
> 架构决策：`docs/1-总设计/Phase11后架构缺口与路线收敛决策.md`

ClinMindRuntime 不是普通医疗问答、普通 RAG 应用或自由自治式医疗 Agent，而是一个以 Runtime 为控制平面、以临床事实与医学证据为数据平面、以 Agent / Model / Tool 为受控能力单元、以 Evaluation / Audit / Governance 为闭环的医疗 AI 运行与治理平台。

---

# 一、项目定位

ClinMindRuntime 的核心目标不是“让模型直接回答患者”，而是：

```text
让 AI 能力在一个可控、可追踪、可评估、可审计、可恢复、可回滚的医疗 Runtime 中运行。
```

它不是：

```text
普通医疗聊天机器人
普通文档 RAG
自由自治式 AI 医生
由 LLM 直接维护患者状态的 Agent
由模型直接执行处方、病历写入或其他高风险动作的系统
只依赖最终文本审核的 Guardrail Demo
```

它是：

```text
Runtime 主控
+ Clinical Data / Fact Plane
+ Clinical Evidence Engine
+ Controlled Agent / Workflow
+ Model Provider Lifecycle
+ Tool / MCP / Skills Governance
+ Unified Runtime Governance Kernel
+ Role-safe Patient / Clinician Projection
+ Evaluation / Audit / Continuous Improvement
+ Production Governance Platform
```

核心定义：

```text
ClinMindRuntime
= 受控医疗 AI Agent Runtime
+ 临床事实与证据基础设施
+ 能力授权与事务治理
+ 角色化输出与安全边界
+ 评估、审计、发布和回滚平台
```

---

# 二、v3.0 设计升级

v2.x 已经建立 Runtime、Asset、Evaluation、Candidate、Persistence、Audit、Agent、RAG、Model Governance、Tool Governance、Console 和三角色前端原型。

v3.0 不推翻这些设计，而是补齐四个关键缺口：

```text
1. Clinical Evidence Engine
   从“检索到文本”升级为“来源、版本、主张、适用性、冲突和引用验证”。

2. Clinical Data & Fact Plane
   将患者事实、医学知识、本轮证据、系统经验和 Agent 推断正式分离。

3. Unified Runtime Governance Kernel
   将分散的 Agent / RAG / Model / Tool Policy 收束为统一决策、风险、恢复和因果追踪语义。

4. Transactional Action Governance
   对真实写操作和外部副作用建立 Proposal、Shadow、Staged Execution、Commit / Rollback 和人工批准。
```

路线收敛只调整实现顺序，不删除长期设计范围。Multi-Agent、GraphRAG、模型训练、真实 MCP、Experience Memory、生产平台和语音交互仍属于完整系统设计，只是必须在依赖满足后进入。

---

# 三、系统需要回答的问题

系统必须能够回答：

```text
1. 当前输入是否属于临床问诊，系统是否支持处理。
2. 当前患者有哪些已确认事实、患者自述、医生记录、设备结果和 Agent 推断。
3. 这些事实分别来自哪里、在什么时间有效、是否冲突或已被替代。
4. 当前病例属于哪个症状群，缺少哪些关键信息。
5. 哪些风险信号已经出现，风险是否跨轮次累积。
6. 哪些候选诊断应保留、弱化、排除或继续追问。
7. 哪些医学证据支持、反对或不适用于当前患者。
8. 证据来源是否权威、是否过期、是否真正支撑生成主张。
9. 本轮允许调用哪些 Agent、Model、RAG、Tool、MCP 或 Skill。
10. 外部内容能否作为数据使用，是否被禁止成为控制指令。
11. 能力执行后是否产生了新的风险、冲突或不可信状态。
12. 结果应该采纳、部分采纳、降级、人工复核还是阻断。
13. 涉及外部副作用时，动作能否安全提交、回滚或补偿。
14. 患者端、医生端、审核端分别能看到什么。
15. 为什么允许、限制、降级、复核或阻断某项能力。
16. 每一次结果能否被回放、归因、评测和再认证。
17. 医生反馈和失败样例如何形成候选，而不是自动改变系统。
18. 模型、知识、Prompt、Policy、Skill 和 Capability 如何发布、监控和回滚。
```

---

# 四、核心不可混淆关系

```text
Patient facts ≠ Medical knowledge
Patient facts ≠ System experience
Medical evidence ≠ Patient clinical facts
RuntimeState ≠ Longitudinal patient record
EvidenceGraph ≠ ClinicalFactLedger
Agent inference ≠ Clinical truth
Tool succeeded ≠ Result may be committed
Conflict detected ≠ Conflict resolved
Retrieved text ≠ Evidence supports claim
Model output ≠ Runtime instruction
Console approval ≠ Immediate production publication
```

这些区分是 v3.0 的基础。

---

# 五、十个完整能力域

## 5.1 Runtime 状态与流程域

负责一次问诊或临床任务当下如何运行。

核心对象：

```text
RuntimeService
RuntimeState
RuntimeStatus
RuntimeMode
WorkMode
EntryAssessment
CaseFrame
DifferentialDiagnosisBoard
QuestionPolicyState
RuntimeTrace
FailurePolicy
RuntimeCheckpoint
```

职责：

```text
接收患者、医生或系统输入；
创建、继续、暂停和恢复 Runtime；
维护当前会话状态而非完整患者历史；
调度事实、证据、经验、Agent、Model 和 Tool；
提交受控状态变化；
生成患者端、医生端和治理端投影；
记录完整运行轨迹。
```

只有 Runtime 可以提交核心执行状态。

## 5.2 Clinical Data & Fact 域

负责患者原始事件、长期临床事实、时间语义、来源、冲突和当前状态重建。

核心对象：

```text
RawSourceEvent
ClinicalDatum
ClinicalFact
ClinicalFactVersion
ClinicalFactLedger
ClinicalProvenance
SourcePartition
CurrentClinicalStateProjection
FactReconciliationResult
FactSupersessionLink
FactConflictLink
ClinicalFactGraph
```

来源分区：

```text
PatientReportedStream
ClinicianDocumentedStream
StructuredEHRStream
DeviceObservationStream
ExternalToolResultStream
AgentDerivedInferenceStream
```

原则：

```text
原始事件 append-only；
事实可以失效、被反驳或被替代，但不删除历史；
不同来源不能直接互相覆盖；
Agent 推断不能自动转化为临床真相；
FHIR 原生状态与通用治理状态同时保留；
支持 event time、recorded time、valid time 和 point-in-time rebuild。
```

首批资源适配：

```text
Condition
Observation
MedicationRequest
AllergyIntolerance
```

## 5.3 医学知识与证据域

负责医学知识来源、证据资产、临床主张、适用性、证据等级、冲突和引用验证。

核心对象：

```text
SourceRegistry
EvidenceAssetVersion
EvidenceChunk
EvidenceSpan
EvidenceClaim
ClaimEvidenceLink
EvidenceApplicability
EvidenceGrade
CitationVerificationResult
EvidenceConflictSet
EvidenceCandidate
EvidenceRef
RuntimeEvidenceGraph
```

证据链：

```text
Source
→ EvidenceAssetVersion
→ EvidenceChunk / EvidenceSpan
→ EvidenceClaim
→ ClaimEvidenceLink
→ Answer / Decision Claim
```

证据评估维度必须分离：

```text
retrieval_relevance
source_authority
evidence_quality
patient_applicability
freshness
citation_entailment
conflict_status
```

向量相似度不能等价于证据可信度。

完整能力包括：

```text
ClinicalQuestionNormalizer
RetrievalPlanner
BM25 Retrieval
Dense Retrieval
Cross-encoder Rerank
Evidence Extraction
Citation Verification
Quick Evidence Mode
Deep Evidence Mode
KG-lite
GraphRAG Provider
Knowledge Asset Lifecycle
```

RAG / GraphRAG 只能提供 EvidenceCandidate / EvidenceRef，不能直接生成 PatientOutput。

## 5.4 临床经验与记忆域

负责从 RuntimeTrace、Evaluation、医生反馈、失败案例和误诊教训中沉淀系统经验。

核心对象：

```text
ClinicianFeedback
FeedbackEvaluationRecord
ExperienceCandidate
ExperienceUnitAsset
ClinicalExperienceProvider
ExperienceContext
ExperienceMemoryCenter
ExperienceRecertification
ImprovementCandidate
```

经验可以：

```text
提高警觉性；
调整追问优先级；
收紧输出边界；
提示相似失败模式；
触发再次评估。
```

经验不能：

```text
直接决定诊断；
直接修改患者事实；
绕过 Review / Evaluation 自动生效；
因一次医生反馈立即修改 Prompt、Policy、Evidence 或模型。
```

## 5.5 Agent 与受控 Workflow 域

负责在 Runtime 授权范围内执行可校验任务。

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
ControlledWorkflowDefinition
WorkflowCheckpoint
WorkflowTrace
```

规划 Agent：

```text
InquiryPlanningAgent
EvidenceOrganizationAgent
DdxReasoningDraftAgent
PatientRewriteAgent
ClinicianReportDraftAgent
TraceReviewAgent
ExperienceCandidateMiningAgent
LlmJudgeAgent / Scorer Adapter
```

Agent 只能生成：

```text
Proposal
Draft
Candidate
Finding
ScoreDraft
```

Agent 不能直接生成或提交：

```text
Final Diagnosis
Final Patient Answer
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
ApprovedExperience
PublishedTrainingDataset
External Write Commit
```

Multi-Agent / Handoff 仅用于后台证据研究、资产审核、评估复盘和复杂医生辅助任务，不能成为最终医疗结论主控。

## 5.6 Tool / MCP / Skills 与外部集成域

负责连接外部数据源、服务和可复用能力包。

核心对象：

```text
ToolRegistry
ToolVersion
ToolAccessPolicy
ToolExecutionRequest
ToolExecutionResult
ToolExecutionTrace
McpAdapter
McpClient
McpServerMetadata
McpResourceRef
SkillMetadata
SkillVersion
SkillExecutionPolicy
SkillProvider
CredentialReference
```

外部集成范围：

```text
FHIR Server
EHR / EMR
HIS
LIS
PACS
External Guideline / Evidence Service
Embedding / Reranker Service
Internal Review Tool
Browser / Computer Use Adapter（条件实验）
```

原则：

```text
MCP 是连接协议，不是主控；
Skill 是可复用能力资产，不是自动授权；
ToolResult 必须结构化并进入 Runtime Validation；
写操作必须进入 Transactional Action Governance；
Tool 自动发现只能生成 Candidate，不能自动获得权限。
```

## 5.7 模型与 Provider 生命周期域

负责可替换、可评估、可部署和可回滚的模型能力。

规划 Provider：

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
```

治理对象：

```text
ModelRegistry
ModelProviderMetadata
ModelProviderVersion
PromptRegistry
PromptVersion
TrainingDatasetVersion
PreferencePair
ExperimentRun
ModelEvaluationResult
ReleaseCandidate
RollbackPlan
DriftMonitor
```

训练与后训练可包括：

```text
SFT
LoRA / QLoRA
DPO / Preference Optimization
RFT（有可靠奖励时）
Distillation
Pruning / Quantization（收益明确时）
```

模型只能返回结构化 Draft / Candidate / EvidenceRef / ScoreDraft。模型版本必须经过 Evaluation、Review、Release 和 Monitoring 才能进入 Runtime。

## 5.8 Unified Runtime Governance 域

负责所有 Agent、RAG、Model、Tool、MCP、Skill 和输出能力的统一授权、风险、恢复和信息流边界。

核心对象：

```text
CapabilityPolicy / Policy IR
Trigger
Predicate / Preconditions
EnforcementAction
RecoveryAction
CapabilityDecisionEngine
CapabilityDecision
ReasonCode
RuntimeDatum
RuntimeRiskState
CapabilityLease
PostCapabilitySafetyResult
```

统一决策：

```text
ALLOW
DEGRADE
REVIEW_REQUIRED
BLOCK
```

RuntimeDatum 必须记录：

```text
sourceType
sourceId
sourceVersion
trustLevel
instructionAllowed
clinicalAuthority
verificationStatus
```

核心原则：

```text
患者输入、RAG 文档、ToolResult、Memory 和模型输出可以作为数据，
但不能自动成为 Runtime 控制指令。

只有受信任系统策略和 Runtime 配置能够改变控制流和能力授权。
```

RuntimeRiskState 支持风险跨轮次 activate、accumulate、resolve、decay、escalate 和 clear。

## 5.9 Transactional Action Governance 域

负责真实写操作和不可逆外部副作用。

核心对象：

```text
RuntimeActionProposal
readSet
writeSet
externalSideEffects
proposedStatePatch
ShadowRuntimeState
StagedExecution
Precondition
Postcondition
CommitResult
RollbackPlan
CompensationAction
HumanApprovalQueue
KillSwitch
```

执行语义：

```text
Action Intent
→ Authorization
→ Preconditions
→ Shadow / Staged Execution
→ Result Validation
→ Postconditions
→ Commit / Partial Commit / Reject
→ Rollback / Compensation
→ Audit
```

适用动作包括病历写入、报告提交、预约、转诊和其他外部副作用。

自动处方、自主诊疗决定和未经医生批准的高风险写入永久禁止。

## 5.10 输出、评估、审计与平台治理域

负责角色化输出、能力评估、候选治理、发布回滚、生产访问和运维。

核心对象：

```text
SafetyGate
DecisionBoundary
PatientOutput
ClinicianReport
RoleProjection
PatientView / ClinicianView / GovernanceView
EvaluationCaseSet
EvaluationRunner
EvaluationScorer
EvaluationResult
SafetyViolation
RegressionFinding
CapabilityProfileUpdateProposal
CandidateReview
AuditLog
ReleaseCenter
Recertification
```

平台组成：

```text
Runtime Console
Evaluation Center
Candidate / Review Queue
Asset Console
Knowledge Console
Experience Memory Center
Model Registry
Training Center
Tool / MCP / Skill Console
Capability / Policy Console
Release / Rollback Center
Audit Center
Clinician Review Workstation
```

治理平台不能绕过 Runtime、Evaluation、Review 和 Release 直接改变医疗输出权限。

---

# 六、六平面总体架构

v3.0 将原五层结构扩展为六个平面：

```text
1. Platform Governance Plane
2. Shared Asset & Lifecycle Plane
3. Clinical Data & Fact Plane
4. Runtime Orchestration & Governance Plane
5. Provider / Agent / Tool Execution Plane
6. Storage / Integration / Operations Plane
```

## 6.1 Platform Governance Plane

负责身份、权限、审核、评估、发布、回滚、再认证、审计和生产运维治理。

## 6.2 Shared Asset & Lifecycle Plane

承载医学知识、证据来源、路径、规则、经验、模型、Prompt、数据集、Skill、Policy 和 CapabilityProfile 的版本化资产。

## 6.3 Clinical Data & Fact Plane

承载 Raw Event、FHIR 资源、患者事实、双时间、来源、冲突、替代和 Current Clinical State Projection。

## 6.4 Runtime Orchestration & Governance Plane

承载 RuntimeState、CaseFrame、RiskState、CapabilityDecision、SafetyGate、Validation、Action Commit、DecisionBoundary 和 Projection。

## 6.5 Provider / Agent / Tool Execution Plane

承载真实 LLM Agent、Evidence Engine、Model Provider、Tool、MCP、Skill、Workflow 和实验能力。

## 6.6 Storage / Integration / Operations Plane

承载 PostgreSQL、pgvector、Redis、对象存储、向量索引、模型服务、FHIR Server、MCP Server、日志、指标、Tracing、备份和灾难恢复。

依赖方向必须保持：

```text
Governance 管理资产和能力生命周期
Assets / Facts 为 Runtime 提供依据
Runtime 授权并调用受控能力
Capabilities 返回结构化结果
Runtime 校验、提交、输出和记录
Storage / Integration 只提供数据、持久化和连接
```

任何下层能力都不得反向取得 Runtime 主控权。

---

# 七、统一 Runtime 主链路 v3.0

```text
Patient / Clinician / System Input
↓
Runtime API
↓
RawSourceEvent / RuntimeDatum 建立来源与可信度
↓
Clinical Fact / Current State / CaseFrame 构建
↓
EntryAssessment
↓
Pre-Capability SafetyGate
↓
RuntimeRiskState 更新
↓
CapabilityDecisionEngine
↓
Capability Lease / Invocation Plan
↓
Controlled Agent / Evidence Engine / Model Provider / Read-only Tool
↓
Structured Proposal / Evidence / Draft / ToolResult
↓
Runtime Validation
↓
Post-Capability SafetyGate
↓
Controlled Adoption / Recovery / Human Review
↓
如有外部副作用：ActionProposal → Shadow / Staged Execution → Commit / Rollback
↓
DDx Board / Runtime Evidence Graph / Question Policy 更新
↓
DecisionBoundary
↓
Patient / Clinician / Governance Role Projection
↓
RuntimeTrace / Causal Trace / AuditLog
↓
Evaluation / Failure Injection / Recertification
↓
Feedback / Experience / Training / Policy / Asset Candidate
↓
Review → Evaluation → Release / Rollback
```

只有 Runtime 可以提交核心状态和外部动作。

---

# 八、Clinical Fact Graph 与 Runtime Evidence Graph

系统保留两个不同图：

```text
Clinical Fact Graph
= 长期患者事实、时间、来源、派生、替代和冲突。

Runtime Evidence Graph
= 某次执行实际使用的患者事实和医学知识证据，以及它们如何支持或反对 Proposal。
```

关系：

```text
ClinicalFactLedger / Medical Evidence Assets
↓ governed selection
Runtime Evidence Graph
↓
Agent / Safety / Policy / DecisionBoundary
```

这使系统能够分别回答：

```text
患者历史中存在什么；
当前系统相信什么；
本轮能力实际看到了什么；
最终行动基于什么。
```

---

# 九、安全模型

## 9.1 双安全门

```text
Pre-Capability SafetyGate
处理原始输入、当前事实和已有风险。

Post-Capability SafetyGate
处理 Agent、RAG、Model、FHIR 和 Tool 新引入的风险、冲突和组件异常。
```

## 9.2 全流程覆盖

安全控制覆盖：

```text
Input
Plan
Capability Invocation
Tool Result
Evidence Adoption
State Commit
Output
Memory Write
```

## 9.3 恢复优先

阻断不是唯一结果。RecoveryAction 包括：

```text
RULE_FALLBACK
RESTRICTED_RETRY
ASK_CLARIFICATION
SWITCH_TO_READ_ONLY
HUMAN_REVIEW
ROLLBACK_TO_CHECKPOINT
SAFE_HALT
```

系统必须同时衡量安全收益和正常任务损失，特别是 false block rate。

---

# 十、角色化输出

## 10.1 患者端

允许展示：

```text
已确认或明确标注来源的事实摘要
安全追问
风险提示
就医级别建议
健康教育
检查准备说明
系统能力受限或需要医生复核的提示
```

禁止展示：

```text
完整 DDx Board
完整 EvidenceGraph
must_not_miss 内部标签
内部 Policy / Risk / Trace
未经确认的确定性诊断
未经审核的治疗或处方建议
原始 PHI、Prompt、Secret 和 Chain-of-Thought
```

## 10.2 医生端

重点展示：

```text
Case Snapshot
Known / Missing / Conflicting Facts
Candidate DDx
Inquiry Timeline
Supporting / Contradicting Evidence
Source Authority / Version / Span
Applicability / Freshness / Citation Verification
Runtime Decisions / Rejected Proposals
Required Review / Escalation
Draft Report
```

## 10.3 治理端

重点展示：

```text
CapabilityDecision
PolicyRef / ReasonCode
Provider / Agent / Tool Version
Provenance / Trust
Pre / Post Safety Result
Recovery / Fallback
Causal Trace
Evaluation / Violation / Regression
Release / Rollback State
```

所有角色视图来自同一个 Runtime 和受控后端投影。

---

# 十一、评测体系

评测不能只评分最终回答。

## 11.1 临床事实层

```text
fact_extraction_precision / recall
temporal_accuracy
supersession_accuracy
conflict_accuracy
stale_fact_activation
current_state_accuracy
```

## 11.2 证据层

```text
Hit@k / MRR
context_precision
citation_entailment
source_quality
applicability_error
freshness_error
conflict_handling
```

## 11.3 Agent / Tool 轨迹层

```text
question_efficiency
tool_selection_accuracy
argument_validity
invalid_proposal_rate
state_transition_correctness
recovery_success
workflow_completion
```

## 11.4 Runtime 治理层

```text
policy_precision / recall
false_block_rate
unsafe_action_rate
capability_revocation_accuracy
rollback_correctness
audit_completeness
role_leakage_rate
policy_decision_latency
```

## 11.5 系统层

```text
P50 / P95 latency
Token / Provider cost
Tool invocation count
Availability
Fallback rate
Escalation rate
Drift indicators
```

Evaluation 必须支持正常场景、边界场景、故障注入和回归测试。

---

# 十二、能力与资产生命周期

统一生命周期：

```text
Design / Candidate
→ Sanitization / Source Validation
→ Evaluation
→ Human Review
→ Release Candidate
→ Publication / Capability Grant
→ Runtime Monitoring
→ Drift / Regression / Incident
→ Recertification
→ Rollback / Revoke / Deprecate
```

适用于：

```text
Evidence Asset
Clinical Experience
Model / Prompt
Training Dataset
Skill
Tool / MCP Server
Policy
CapabilityProfile
```

任何评估结果或医生反馈都不能自动扩大能力边界。

---

# 十三、生产治理与运维

生产能力包括：

```text
OAuth / OIDC / SSO
Organization / Tenant
RBAC / ABAC
PHI Scope
Consent / Purpose of Use
Secret Management
Credential Rotation
Access Audit
OpenTelemetry
Prometheus / Grafana
Centralized Logs
SLO / SLA / Alerting
Backup / Restore
Disaster Recovery
Data Retention / Deletion
Privacy / Compliance Controls
Provider Cost / Token / Latency Monitoring
Model / Knowledge / Tool Drift Monitoring
```

生产平台只能在 Runtime、Data/Fact、Evidence、Policy、Evaluation 和 Audit 已稳定后建设。

---

# 十四、当前实现基线

截至 v3.0 设计写回时：

```text
Phase 0–11 P0 已完成并形成架构与治理原型；
Phase 11-P1 正在收口 Runtime-backed Role View；
Phase 12 是下一条实现主线。
```

已建立：

```text
Runtime / State / Safety / Boundary / Trace
Asset / Provider
Evaluation / Candidate / Review
Persistence / PostgreSQL / Audit
Controlled Agent 原型
RAG / KG-lite 原型
Python Provider 与模型治理对象
Tool / MCP / Skills 治理对象
Governance Console
Patient / Clinician / Governance 三角色前端和后端投影
```

尚未完成：

```text
真实 Clinical Evidence Engine
真实 LLM-backed Agent 主链路
真实只读 FHIR 数据链
ClinicalFactLedger 与双时间状态
正式 Policy IR / RuntimeRiskState / Capability Lease
真实写动作事务治理
完整受控 Agent / Workflow / Multi-Agent
高级 GraphRAG / Deep Evidence
真实模型训练发布闭环
真实远程 MCP / 医疗系统集成
Clinical Experience Memory 完整闭环
生产认证、租户、审核、发布、运维与合规平台
```

---

# 十五、永久禁止与条件能力

## 15.1 永久禁止

```text
让 LLM 或 Multi-Agent 成为医疗 Runtime 主控；
Agent 直接提交患者事实或 RuntimeState；
RAG 直接生成最终患者医疗结论；
外部文档或 ToolResult 改写系统控制指令；
未授权 Tool 直接执行外部副作用；
自动处方或自主医疗决定；
Candidate、Feedback、Training Trace 自动上线；
Console 绕过 Evaluation / Review / Release 直接扩大权限；
向患者暴露 Chain-of-Thought、Secret、内部 Risk / Policy 或未脱敏 PHI。
```

## 15.2 条件能力

```text
真实写操作
→ 必须先完成 Transactional Action Governance。

Multi-Agent / Handoff
→ 仅在单 Agent、Policy、Trace 和 Evaluation 稳定后进入。

Neo4j / Milvus / Qdrant
→ 仅在规模和评测证明 PostgreSQL / pgvector 不足时进入。

LoRA / DPO / RFT / Distillation
→ 仅在真实数据、明确任务和可靠评测建立后进入。

Browser / Computer Use
→ 仅在隔离、最小权限、完整 Trace 和人工审批条件下实验。
```

---

# 十六、文档与实现关系

```text
ClinMindRuntime完整系统设计.md
定义系统是什么、为什么这样设计、长期能力边界是什么。

ClinMindRuntime阶段拆分路线图.md
定义每项能力在哪个 Phase 实现、进入条件和验收标准。

ClinMindRuntime技术实现总方案.md
定义模块、接口、包结构、数据模型、API、存储、测试和部署如何落地。

专项设计 / Phase 规格
定义当前阶段可直接实现的契约和任务。
```

正确实现链路：

```text
完整系统设计
→ 阶段拆分路线图
→ 技术实现总方案
→ 专项设计
→ Phase 实现规格
→ API 与测试设计
→ 开发任务清单
→ 代码与测试
→ 冻结记录
```

---

# 十七、最终目标架构

ClinMindRuntime 的最终形态不是一个“会回答医学问题的模型”，而是：

```text
一个以 Runtime 为控制平面，
以 Clinical Fact 和 Evidence 为可信数据基础，
以 Agent / Model / Tool 为受控能力，
以 Policy / Risk / Transaction 为执行治理，
以患者端、医生端和治理端为角色化出口，
以 Evaluation / Audit / Release / Rollback 为持续治理闭环的
医疗 AI Agent Runtime 与能力治理平台。
```

核心主线保持不变：

```text
Runtime 主控，
事实与证据分离，
能力最小权限，
结果结构化校验，
风险前后重评估，
动作分阶段提交，
角色输出隔离，
全过程可追踪、可评估、可审核、可恢复、可回滚。
```
