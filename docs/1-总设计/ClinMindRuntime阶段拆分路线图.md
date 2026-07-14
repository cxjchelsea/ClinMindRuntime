# ClinMindRuntime 阶段拆分路线图

> 路线图版本：v3.0  
> 当前权威总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md` v3.0  
> 当前权威技术蓝图：`docs/1-总设计/ClinMindRuntime技术实现总方案.md` v3.0  
> 项目定位：受控医疗 AI Agent Runtime 与能力治理平台  
> 当前状态：Phase 1–11 P0 已冻结；Phase 11-P1 正在收口；下一条实现主线为 Phase 12。  
> 路线决策：`docs/1-总设计/Phase11后架构缺口与路线收敛决策.md`

本文档不是只描述“最近准备做什么”，而是 ClinMindRuntime 的**全量实现路线总表**。

它必须覆盖：

```text
完整系统设计中定义的全部能力域和架构层
+ 技术实现总方案中定义的代码模块、API、存储、测试与部署能力
+ 各专项规划中的正式产品能力
+ Phase 11 后新增的 Evidence、Clinical Fact、统一 Runtime Governance 等设计
```

任何已经进入正式设计的能力，都必须在本文档中具有以下五种状态之一：

```text
1. 已实现 / 已冻结
2. 当前实现主线
3. 已规划核心阶段
4. 条件触发或实验阶段
5. 永久禁止或仅作为开发工具，不属于产品 Runtime
```

不得出现“总设计里设计了，但路线图中没有归属”的能力。

---

# 一、路线图定位与完整性原则

ClinMindRuntime 不是普通医疗问答系统，也不是自由自治式医疗 Agent。系统长期定位保持不变：

```text
受控医疗 AI Agent Runtime 与能力治理平台
```

核心权力边界：

```text
Runtime
负责状态、控制流、能力授权、安全判断、结果提交、输出边界、恢复和审计。

Agent / RAG / Model / Tool / MCP / Skill
只能作为受控能力生成 Proposal、Draft、Candidate、EvidenceRef 或 Structured Result。

Evaluation / Review / Governance
负责能力评估、发布授权、回滚、再认证和持续改进。
```

## 1.1 收敛不等于删除设计范围

Phase 11 后的“路线收敛”只表示：

```text
优先完成真实临床纵切，
再按依赖顺序实现其余完整设计。
```

它不表示删除以下长期能力：

- Agent 扩展、受控 Workflow、Multi-Agent / Handoff；
- KG-lite、GraphRAG、Deep Evidence；
- ModelProvider、训练、后训练、蒸馏和模型发布治理；
- 真实 Tool、MCP、Skills 和外部医疗系统；
- Clinical Experience Memory 与医生反馈闭环；
- 完整治理 Console、正式审核、发布和回滚；
- 语音 / Realtime Agent、Browser / Computer Use 等实验能力；
- 正式认证、多租户、可观测性、运维和合规能力。

上述内容都进入后续 Phase，只是不允许抢在其依赖条件之前实现。

## 1.2 阶段状态定义

```text
FROZEN
已完成并形成冻结记录；后续只能通过新 Phase 演进。

CLOSING
代码主链路已建立，正在补文档、测试、投影和冻结记录。

NEXT
完成当前收口后立即进入的实现阶段。

PLANNED
已进入正式路线，但尚未满足进入条件。

CONDITIONAL
只有满足明确触发条件后才启动。

EXPERIMENTAL
允许做隔离实验，不进入患者端主链路，也不计为核心系统完成。

PROHIBITED / NON-PRODUCT
永久禁止，或只作为开发工具而非产品能力。
```

---

# 二、阶段命名、进入与冻结规则

```text
Phase X
= 一个可独立说明系统能力成熟度的阶段。

Phase X-P0
= 证明核心链路成立。

Phase X-P1
= 加入真实数据、治理加固或关键能力扩展。

Phase X-P2
= 形成端到端闭环、平台化、规模化或正式冻结。
```

每个阶段必须具备：

1. 明确进入条件；
2. 明确范围和禁止边界；
3. 领域对象、接口、存储与 API 设计；
4. 单元、集成、回归、故障注入和人工验证；
5. Evaluation 指标和验收标准；
6. 实现状态、测试记录和冻结记录；
7. README、项目地图、技术方案和实现约束同步；
8. 不得通过改写历史冻结记录伪造实现状态。

---

# 三、全量阶段总览

| 阶段 | 名称 | 核心目标 | 状态 |
|---|---|---|---|
| Phase 0 | 设计与工程骨架 | 建立定位、文档体系、代码骨架和基本边界 | FROZEN |
| Phase 1-P0 | Runtime MVP | RuntimeState、SafetyGate、DecisionBoundary、Trace 与角色输出 | FROZEN |
| Phase 2-P0 | 共享能力资产原型 | AssetPackage、Provider、版本和 CapabilityProfile | FROZEN |
| Phase 3-P0 | Evaluation 闭环 | CaseSet、Runner、Scorer、Result、Violation、Regression | FROZEN |
| Phase 4-P0/P1 | Candidate 与 Review 治理 | Experience / Training Candidate、脱敏、来源和人工审核 | FROZEN |
| Phase 5-P0/P1/P2 | Persistence / Audit / Console 基础 | PostgreSQL、Repository、Audit、Safe DTO、最小 Console | FROZEN |
| Phase 6-P0 | 受控 Agent 执行层 | AgentRuntime、Policy、Proposal、Validator、Trace、Evaluation | FROZEN |
| Phase 7-P0/P1 | RAG Evidence / KG-lite 原型 | EvidenceCandidate、Validation、EvidenceGraph、轻量图关系 | FROZEN |
| Phase 8-P0/P1/P2 | Python Provider 与模型治理原型 | Python Service、Model/Prompt/Dataset/Release 治理对象 | FROZEN |
| Phase 9-P0 | Tool / MCP / Skills 治理原型 | Registry、Policy、Validation、Mock / Local Adapter | FROZEN |
| Phase 10-P0 | Governance / Runtime Console MVP | 只读治理聚合、Runtime Timeline 和安全治理视图 | FROZEN |
| Phase 11-P0 | Role-based Frontend Suite | Patient / Clinician / Governance 三角色前端 | FROZEN |
| Phase 11-P1 | Runtime-backed Role View | 后端安全投影、API-first 前端、RuntimeStore 与 fallback | CLOSING |
| Phase 12-P0 | Clinical Evidence Engine MVP | 真实证据资产、混合检索、Claim 和 Citation 验证 | NEXT |
| Phase 12-P1 | Controlled Real Capability Slice | 真实 LLM Agent、只读 FHIR、最小统一决策、Post-Safety、Recovery | PLANNED |
| Phase 12-P2 | Chest-pain Clinical Vertical | 胸痛/胸闷单场景端到端闭环与量化评测 | PLANNED |
| Phase 13-P0/P1/P2 | Clinical Data & Fact Plane | 原始事件、患者事实、双时间、冲突、纵向状态和 Fact Graph | PLANNED |
| Phase 14-P0/P1/P2 | Unified Runtime Governance Kernel | Policy IR、统一能力决定、RiskState、Provenance、Lease、因果 Trace | PLANNED |
| Phase 15-P0/P1/P2 | Transactional Action Governance | ActionProposal、Shadow、Staged Execution、Commit/Rollback、人工批准 | CONDITIONAL |
| Phase 16-P0/P1/P2 | Controlled Agent & Workflow Expansion | Agent 能力扩展、可恢复 Workflow、受控 Multi-Agent / Handoff | PLANNED |
| Phase 17-P0/P1/P2 | Advanced Knowledge & Evidence Platform | Deep Evidence、KG/GraphRAG、知识资产生命周期与知识治理平台 | PLANNED |
| Phase 18-P0/P1/P2 | Model & Training Lifecycle | 真实 ModelProvider、数据集、训练、后训练、发布、回滚和漂移 | PLANNED |
| Phase 19-P0/P1/P2 | Tool / MCP / Skills & Integration | 真实远程 MCP、Skill Registry、FHIR/EHR/HIS/LIS/PACS 和外部服务 | PLANNED |
| Phase 20-P0/P1/P2 | Experience Memory & Continuous Improvement | 医生反馈、Experience Asset、改进 Candidate、再认证 | PLANNED |
| Phase 21-P0/P1/P2 | Production Governance Platform | Auth、Tenant、RBAC/ABAC、审核中心、发布平台、运维与合规 | PLANNED |
| Phase 22-P0/P1/P2 | Advanced Interaction & Experimental Capabilities | Voice/Realtime、Browser/Computer Use 和隔离技术实验 | EXPERIMENTAL / CONDITIONAL |

---

# 四、Phase 0–11：已完成的架构广度验证

Phase 0–11 已经建立：

```text
Runtime 主控
Asset / Provider 版本化
SafetyGate / DecisionBoundary
Agent Proposal / Runtime Validation
RAG EvidenceCandidate / EvidenceGraph
KG-lite 路径原型
Python Provider 与模型治理对象
Tool / MCP / Skills 治理对象
Trace / Audit / Evaluation / Candidate / Review
PostgreSQL 与 InMemory 双实现
Governance Console
Patient / Clinician / Governance 三角色视图
```

必须明确：

```text
已冻结的是架构与治理原型，
不等于真实临床能力、真实医院集成或生产系统已经完成。
```

当前真实限制：

- InquiryPlanningAgent 仍以规则和模板为主；
- RAG 仍以 YAML、关键词和确定性规则为主；
- Embedding、Rerank、Judge、Risk Classifier 含 Mock 或规则实现；
- MCP / Tool / Skill 仍以 Mock / Local Adapter 为主；
- Patient / Clinician 部分投影片段为空、PARTIAL 或 fallback；
- 未形成真实 FHIR/EHR 纵向数据链；
- 未形成真实外部写操作与事务治理；
- 未形成生产认证、正式审核、发布、回滚和合规平台。

---

# 五、Phase 11-P1：当前收口阶段

## 5.1 目标链路

```text
RuntimeState / CaseFrame / PatientOutput / ClinicianReport / DecisionBoundary
→ Role-specific Projection
→ Patient / Clinician Safe DTO
→ Patient / Clinician Read API
→ API-first Frontend
```

## 5.2 冻结前必须完成

1. 更新开发任务清单实际状态；
2. 补齐人工测试与冻结记录；
3. 验证 RuntimeStore 主路径、PARTIAL 与 fallback 标记；
4. 将 Inquiry Timeline、Evidence Panel、AI Suggestions 尽可能从 Runtime 投影；
5. 将 Care Navigation 从 SafetyGate / DecisionBoundary 投影；
6. 运行 Java、Python、TypeScript、Vitest 与生产构建；
7. 同步 README、项目地图、总设计、技术方案和实现约束。

## 5.3 禁止

- 不在本阶段顺手接入真实 LLM、真实 RAG 或 FHIR；
- 不加入生产认证和外部写操作；
- 不改写历史冻结事实。

---

# 六、Phase 12：真实临床能力纵切

Phase 12 是下一条实现主线。它以一个可评估临床场景验证真实 Agent、Evidence、FHIR Tool 和 Runtime 治理是否真正协同。

## 6.1 Phase 12-P0：Clinical Evidence Engine MVP

### 核心对象

```text
SourceRegistry
EvidenceAssetVersion
EvidenceChunk / EvidenceSpan
EvidenceClaim
ClaimEvidenceLink
EvidenceApplicability
EvidenceGrade
CitationVerificationResult
EvidenceConflictSet（最小版）
```

### 主链路

```text
Clinical Question
→ ClinicalQuestionNormalizer（最小版）
→ RetrievalPlanner（最小规则版）
→ BM25 Recall
→ Dense Embedding Recall
→ Candidate Merge
→ Cross-encoder Rerank
→ Evidence Extraction
→ Evidence Grade / Applicability / Freshness
→ Citation Entailment
→ EvidenceValidation
→ EvidenceGraph
```

评分必须拆分：

```text
retrieval_relevance
source_authority
evidence_quality
patient_applicability
freshness
citation_entailment
conflict_status
```

不得用单个相似度替代证据质量和适用性。

### 存储与基础设施

```text
evidence_sources
evidence_asset_versions
evidence_chunks
evidence_claims
claim_evidence_links
citation_verification_results
pgvector embeddings（可选 PostgreSQL 实现）
retrieval traces
```

### 完成标准

- 使用许可清晰的真实公开医学资料；
- 主链路不依赖 hash embedding 和 token-overlap reranker；
- 关键主张可追踪到来源、版本、checksum 和 source span；
- 支持过期、冲突、不适用和低权威证据测试；
- RAG 仍不得直接输出 PatientOutput。

## 6.2 Phase 12-P1：Controlled Real Capability Slice

### 真实能力

```text
LLM-backed InquiryPlanningAgent
Rule-based InquiryPlanningAgent fallback
Read-only FHIR Tool
Clinical Evidence Engine
PatientSafeRewrite / ClinicianReportDraft 的真实 Provider 候选
```

### 最小统一治理对象

```text
CapabilityDecision
- ALLOW
- DEGRADE
- REVIEW_REQUIRED
- BLOCK

RuntimeDatum
- sourceType
- sourceId / sourceVersion
- trustLevel
- instructionAllowed
- clinicalAuthority
- verificationStatus

PostCapabilitySafetyResult
RecoveryAction
- RULE_FALLBACK
- RESTRICTED_RETRY
- ASK_CLARIFICATION
- HUMAN_REVIEW
- SAFE_HALT
```

### 主链路

```text
Input / External Evidence / FHIR Data
→ Provenance & Trust Classification
→ Pre-Capability SafetyGate
→ CapabilityDecision
→ Agent / RAG / Model / Read-only Tool
→ Structured Result
→ Runtime Validation
→ Post-Capability SafetyGate
→ Recovery / Controlled Adoption
→ DecisionBoundary
```

### 只读 FHIR 范围

```text
Condition
Observation
MedicationRequest
AllergyIntolerance
```

Phase 12 只读取和投影，不自动修改正式患者事实。

## 6.3 Phase 12-P2：Chest-pain Clinical Vertical

固定首个领域：

```text
胸痛 / 胸闷风险分层与追问
```

完整演示：

```text
患者输入
→ LLM 追问 Proposal
→ Runtime 校验和部分采纳
→ FHIR 只读查询
→ 真实 Evidence Retrieval
→ Claim / Citation / Applicability
→ Post-Capability Safety
→ PatientOutput
→ Clinician Evidence Report
→ Governance Trace
→ Evaluation
```

### Evaluation

结果层：

```text
task_success
safe_task_completion
clinical_safety_violation
appropriate_escalation
false_reassurance
```

证据层：

```text
Hit@k / MRR
context_precision
citation_entailment
stale_evidence_activation
applicability_error
conflict_handling
```

轨迹层：

```text
question_efficiency
invalid_proposal_rate
tool_selection_accuracy
argument_validity
recovery_success
state_transition_correctness
```

治理层：

```text
policy_precision / recall
false_block_rate
capability_decision_latency
audit_completeness
role_leakage_rate
```

---

# 七、Phase 13：Clinical Data & Fact Plane

该阶段补齐完整系统设计目前缺少的 Patient Clinical Fact Domain。

## 7.1 Phase 13-P0：Raw Event 与 FHIR 事实基础

```text
RawSourceEvent（append-only）
ClinicalDatum
FHIRResourceRef
ClinicalProvenance
SourcePartition
ConditionFactAdapter
ObservationFactAdapter
MedicationFactAdapter
AllergyFactAdapter
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

不同来源不得直接覆盖；Agent 推断不得自动成为临床真相。

## 7.2 Phase 13-P1：ClinicalFactLedger 与 Current State

```text
ClinicalFact
ClinicalFactVersion
ClinicalFactLedger
CurrentClinicalStateProjection
FactReconciliationResult
FactSupersessionLink
FactConflictLink
```

同时保留：

```text
FHIR 原生资源状态
通用治理状态
来源与权威级别
临床确定性
事件时间 / 记录时间
有效时间区间
替代与冲突链
```

不使用一个统一生命周期枚举替代不同 FHIR 资源自身语义。

## 7.3 Phase 13-P2：纵向状态与双图

```text
Bi-temporal Query
Point-in-time State Rebuild
Clinical Fact Graph
Runtime Evidence Graph
Governed Fact Retrieval
Temporal / Conflict-aware Context Assembly
```

明确：

```text
Clinical Fact Graph
= 长期患者事实、时间、来源、替代和冲突。

Runtime Evidence Graph
= 某次执行实际使用的患者事实和医学证据。
```

### 评测

- Fact extraction precision / recall；
- temporal accuracy；
- supersession / conflict accuracy；
- stale fact activation；
- current-state accuracy；
- longitudinal retrieval quality。

---

# 八、Phase 14：Unified Runtime Governance Kernel

Phase 12-P1 只实现最小统一对象；Phase 14 将其收束成正式 Runtime 内核。

## 8.1 Phase 14-P0：Policy IR 与统一能力决定

```text
CapabilityPolicy / Policy IR
Trigger
Predicate / Preconditions
EnforcementAction
RecoveryAction
CapabilityDecisionEngine
CapabilityDecision
ReasonCode
PolicyRef / PolicyVersion
```

Agent、RAG、Model、Tool、MCP、Skill 和输出能力都进入同一决策协议，而不是维护互相割裂的授权语义。

## 8.2 Phase 14-P1：RuntimeRiskState 与全流程 Safety

```text
RuntimeRiskState
accumulatedSignals
unresolvedConflicts
activeSafetyConstraints
componentHealth
evidenceReliability
stateConfidence
blockedCapabilities
degradedCapabilities
lastSafeCheckpoint
recoveryStatus
```

Safety 覆盖：

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

风险状态支持 activate、accumulate、resolve、decay、escalate 和 clear。

## 8.3 Phase 14-P2：Capability Lease 与因果 Trace

```text
CapabilityLease
allowedScopes
allowedActions
expiresAt
maxInvocations
revocationConditions

causation_id
correlation_id
source_event_id
fact_id
projection_version
evidence_graph_id
proposal_id
policy_decision_id
tool_execution_id
output_id
```

目标是完整回答：

```text
某项信息如何进入事实，
如何进入上下文，
被哪个 Agent / Provider 使用，
产生了什么 Proposal，
由什么 Policy 批准或拒绝，
最终是否形成输出或外部动作。
```

---

# 九、Phase 15：Transactional Action Governance

仅在系统开始接入真实写操作或不可逆外部副作用后启动。

## 9.1 Phase 15-P0：RuntimeActionProposal

```text
RuntimeActionProposal
readSet
writeSet
externalSideEffects
proposedStatePatch
evidenceRefs
provenanceRefs
confidence
```

## 9.2 Phase 15-P1：Shadow / Staged Execution

```text
Authorization
Preconditions
ShadowRuntimeState
Staged Tool Execution
Result Validation
Postconditions
Commit / Partial Commit / Reject
Rollback / Compensation
Idempotency
```

## 9.3 Phase 15-P2：高风险动作与人工审批

```text
Human Approval Queue
Four-eyes Review
Kill Switch
Credential Revocation
Action Checkpoint
Write Audit
```

适用动作包括正式病历写入、预约、转诊、报告提交和其他医疗外部副作用；自动处方和自主医疗决定仍不允许。

---

# 十、Phase 16：Controlled Agent & Workflow Expansion

## 10.1 Phase 16-P0：受控 Agent 能力补齐

正式规划：

```text
InquiryPlanningAgent（增强）
EvidenceOrganizationAgent
DdxReasoningDraftAgent
PatientRewriteAgent
ClinicianReportDraftAgent
TraceReviewAgent
ExperienceCandidateMiningAgent
LlmJudgeAgent / Scorer Adapter
```

每个 Agent 必须具备：

```text
Registry
Policy
Context Contract
Structured Proposal / Draft
Validator
Trace
Evaluation Hook
Fallback
```

## 10.2 Phase 16-P1：可恢复 Workflow

```text
Controlled Workflow Definition
Checkpoint
Pause / Resume
Retry
Human Review Node
Fallback Node
State Synchronization
Workflow Trace
```

LangGraph、Agents SDK 或其他框架只允许作为受控 Provider 内部实现或思想参考，不得取代 Java Runtime 主控。

## 10.3 Phase 16-P2：受控 Multi-Agent / Handoff

正式规划但后置：

- 后台证据研究；
- 资产审核；
- Evaluation 复盘；
- Candidate 分析；
- 复杂医生端辅助任务。

禁止：

- 多 Agent 直接决定最终医疗结论；
- Handoff 绕过 CapabilityDecision；
- CrewAI / AutoGen 成为系统主控；
- 多 Agent 自主扩大 Tool 权限。

---

# 十一、Phase 17：Advanced Knowledge & Evidence Platform

## 11.1 Phase 17-P0：Deep Evidence Mode

```text
ClinicalQuestionNormalizer（完整）
RetrievalPlanner
Question Decomposition
Multi-query Retrieval
Source Deduplication
Supporting Evidence Aggregation
Contradicting Evidence Aggregation
Conflict Analysis
Evidence Synthesis
Citation Verification
```

区分：

```text
Quick Evidence Mode
Deep Evidence Mode
```

## 11.2 Phase 17-P1：KG-lite / GraphRAG 增强

```text
KnowledgeNode / KnowledgeEdge
Clinical Pathway Graph
Evidence Relation Graph
Graph Path Expansion
GraphRAG Provider
Graph-enhanced Rerank
EvidenceGraph Enhancement
```

GraphRAG 只增强 Evidence，不成为诊断主控。

## 11.3 Phase 17-P2：知识资产生命周期与平台

```text
Knowledge Console
Source Review
Asset Ingestion Pipeline
Version / Supersession / Deprecation
License / Jurisdiction / Audience
Publication / Rollback
Index Rebuild
Quality Monitoring
```

基础设施：

- PostgreSQL / pgvector 为默认实现；
- Neo4j、Milvus、Qdrant 只有在数据规模和评测证明必要时进入；
- 不为技术展示提前建设复杂图或向量平台。

---

# 十二、Phase 18：Model & Training Lifecycle

## 12.1 Phase 18-P0：真实 ModelProvider 体系

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

所有模型只返回结构化候选。

## 12.2 Phase 18-P1：模型、Prompt 与数据集治理

```text
ModelRegistry
ModelProviderMetadata
ModelProviderVersion
PromptRegistry / PromptVersion
TrainingDatasetVersion
PreferencePair
ExperimentRun
ModelEvaluationResult
ReleaseCandidate
RollbackPlan
DriftMonitor
```

完整上线链路：

```text
TrainingDatasetVersion
→ Training / Post-training
→ ModelProviderVersion
→ Evaluation
→ CapabilityProfileUpdateProposal
→ Human Review
→ Release
→ Monitoring / Rollback
```

## 12.3 Phase 18-P2：训练与后训练能力

正式规划：

```text
Fine-tuning / SFT
LoRA / QLoRA
DPO / Preference Optimization
RFT（有可靠奖励和任务时）
Distillation
Pruning / Quantization（部署收益明确时）
```

限制：

- 不训练替代医生的自由回答模型；
- 不允许训练结果自动扩大患者端权限；
- LLM-as-a-Judge 不是唯一安全判据；
- 未审核 Trace 不自动进入训练集。

---

# 十三、Phase 19：Tool / MCP / Skills & External Integration

## 13.1 Phase 19-P0：真实工具与远程 MCP

```text
ToolRegistry / ToolVersion
ToolAccessPolicy
ToolAdapter
Remote McpClient / McpAdapter
McpServerMetadata
Structured ToolResult
Timeout / Retry / Circuit Breaker
Secret / Credential Reference
Tool Trace / Audit
```

Spring AI MCP 可作为 Java 侧候选实现，但不能成为系统控制平面。

## 13.2 Phase 19-P1：Skill Registry 与外部医疗集成

```text
SkillMetadata
SkillVersion
SkillExecutionPolicy
SkillProvider
Skill Review / Publication / Revocation
```

外部连接规划：

```text
FHIR Server
EHR / EMR
HIS
LIS
PACS
External Guideline / Evidence Service
Internal Review Tool
Embedding / Reranker Service
```

所有正式医疗系统接入必须遵守 Phase 13 数据事实边界和 Phase 14 能力授权。

## 13.3 Phase 19-P2：高级 Tool 能力

- 受控 Browser / Computer Use Adapter；
- 复杂外部研究工具；
- 写操作 Tool 仅在 Phase 15 已冻结后启用；
- Tool 自动发现只能产生 Candidate，不自动授权；
- 高风险 Tool 必须具备人工审批、撤销和 Kill Switch。

---

# 十四、Phase 20：Experience Memory & Continuous Improvement

## 14.1 Phase 20-P0：医生反馈闭环

```text
ClinicianFeedback
- evidenceRelevant
- evidenceOutdated
- answerSupported
- unsafeRecommendation
- missingEvidence
- excessiveEscalation
- preferredSource
- comment

FeedbackEvaluationRecord
FeedbackSourceRef
ImprovementCandidate
```

医生反馈不能直接修改 Prompt、Policy、Evidence 或模型。

## 14.2 Phase 20-P1：Clinical Experience Memory

```text
ExperienceUnitAsset
ExperienceCandidate
ClinicalExperienceProvider
ExperienceContext
ExperienceMemoryCenter
Experience Review
Experience Recertification
```

经验只可改变警觉性、追问优先级和输出收紧，不直接决定诊断。

## 14.3 Phase 20-P2：持续改进与再认证

```text
AssetImprovementCandidate
ModelImprovementCandidate
PromptImprovementCandidate
SkillImprovementCandidate
PolicyImprovementCandidate
CapabilityProfileUpdateProposal
Periodic Recertification
Regression Monitoring
Drift Review
```

所有改进都必须：

```text
Candidate
→ Sanitization / Source Validation
→ Human Review
→ Evaluation
→ Release Candidate
→ 再次 Evaluation
→ Publication / Rollback
```

---

# 十五、Phase 21：Production Governance Platform

## 15.1 Phase 21-P0：身份、租户和数据访问

```text
OAuth / OIDC / SSO
User / Organization / Tenant
Production RBAC / ABAC
Patient / Clinician / Reviewer / Admin Roles
PHI Scope
Consent / Purpose of Use
Secret Management
Credential Rotation
Access Audit
```

## 15.2 Phase 21-P1：完整治理中心

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

Console 不能绕过 Evaluation、Review 和 Release 流程直接改变 Runtime 权限。

## 15.3 Phase 21-P2：部署、运维、合规与可靠性

```text
Docker / Docker Compose / Deployment Scripts
Environment Configuration
OpenTelemetry
Prometheus / Grafana
Centralized Logs / Trace Correlation
SLO / SLA / Alerting
Backup / Restore
Disaster Recovery
Data Retention / Deletion
Privacy / Compliance Controls
Provider Cost / Token / Latency Monitoring
Model / Knowledge / Tool Drift Monitoring
```

---

# 十六、Phase 22：Advanced Interaction & Experimental Capabilities

这些能力属于完整长期设计或技术覆盖范围，但不是当前医疗 Runtime 核心完成条件。

## 16.1 Phase 22-P0：Voice / Realtime Agent

```text
ASR / TTS Adapter
Realtime Session Gateway
Streaming Input / Output
Turn Taking
Interruption Handling
Realtime Safety Signal
Role-safe Streaming Projection
```

语音只是输入输出通道，不改变 Runtime 主控。

## 16.2 Phase 22-P1：Browser / Computer Use 实验

- 仅在隔离环境和最小权限下运行；
- 只返回结构化 Observation / Proposal；
- 不直接操作患者核心状态；
- 涉及外部副作用时必须经过 Phase 15；
- 必须记录页面、动作、结果和证据来源。

## 16.3 Phase 22-P2：框架与前沿技术实验区

可评估：

```text
OpenAI Agents SDK 类框架
LangGraph
LangChain / LlamaIndex
CrewAI / AutoGen
新型 Agent Memory
新型 RAG / GraphRAG
新型 Guardrails
```

实验结论只有进入正式 Phase、通过 Evaluation 和架构审核后，才可成为产品能力。

Codex、Claude Code、OpenHands 等只属于开发辅助工具，不属于 ClinMindRuntime 产品 Runtime。

---

# 十七、八个能力域到阶段的完整追踪矩阵

| 完整系统设计能力域 | 已实现基础 | 后续完整落点 |
|---|---|---|
| Runtime 状态与流程域 | Phase 1、6、11 | Phase 12、13、14、15、16 |
| 医学知识与证据域 | Phase 2、7 | Phase 12、17、21 |
| 临床经验与记忆域 | Phase 4 | Phase 20、21 |
| Agent 受控执行域 | Phase 6 | Phase 12、14、16、22 |
| Tool / MCP / Skills 外部能力域 | Phase 9 | Phase 12、15、19、22 |
| 模型能力与 Provider 域 | Phase 8 | Phase 12、18 |
| 输出边界与安全治理域 | Phase 1、5、10、11 | Phase 12、14、15、21、22 |
| 评估、审计与持续进化域 | Phase 3、4、5、10 | Phase 12–22 持续覆盖，重点 Phase 20、21 |

结论：八个能力域全部具有实现阶段，没有设计孤岛。

---

# 十八、五层架构到阶段的完整追踪矩阵

| 五层架构 | 已实现基础 | 后续完整落点 |
|---|---|---|
| 平台治理层 | Phase 5、10、11 | Phase 20、21 |
| 共享能力资产层 | Phase 2、7、8、9 | Phase 12、17、18、19、20、21 |
| Runtime 执行层 | Phase 1、6 | Phase 12、13、14、15、16 |
| Provider / Agent / Tool 能力层 | Phase 6、7、8、9 | Phase 12、16、17、18、19、22 |
| Storage / Integration 层 | Phase 5、7、8、9 | Phase 12、13、14、15、17、18、19、21 |

---

# 十九、技术实现总方案模块追踪矩阵

| 包 / 技术模块 | 阶段归属 |
|---|---|
| `api` / `console` | Phase 5、10、11、21 |
| `application` / RuntimeService / Command / Query | Phase 1、12、13、14、15 |
| `domain` / RuntimeState / CaseFrame / DDx / EvidenceGraph | Phase 1、7、12、13、14 |
| `trace` / `audit` | Phase 1、5，并贯穿全部阶段；因果 Trace 在 Phase 14 |
| `safety` / `boundary` | Phase 1、11、12、14、15、21、22 |
| `capability` / Orchestration / Validation | Phase 6、12、14、15 |
| `agent` | Phase 6、12、16、22 |
| `provider` | Phase 2、8、12、18 |
| `evidence` / `rag` / `graph` | Phase 7、12、17 |
| `model` / `training` | Phase 8、18 |
| `tool` / `mcp` / `skill` | Phase 9、12、15、19、22 |
| `asset` | Phase 2、12、17、18、19、20、21 |
| `evaluation` | Phase 3，并贯穿 Phase 6–22 |
| `candidate` | Phase 4、18、20、21 |
| `governance` | Phase 4、5、10、20、21 |
| `persistence` | Phase 5，并随 Phase 12–21 扩展 |
| `config` / deployment / observability | Phase 5、8、21 |
| 新增 `clinicaldata` / `clinicalfact` | Phase 13 |
| 新增 `policy` / `risk` / `recovery` | Phase 12、14 |
| 新增 `action` / `transaction` | Phase 15 |

---

# 二十、API、存储与平台能力追踪

## 20.1 API

```text
/api/v1/runtime/**                         Phase 1+
/api/v1/debug/assets/**                    Phase 2+
/api/v1/debug/evaluations/**               Phase 3+
/api/v1/debug/candidates/**                Phase 4+
/api/v1/debug/persistence/**               Phase 5+
/api/v1/console/**                         Phase 5 / 10 / 21
/api/v1/debug/agents/**                    Phase 6 / 16
/api/v1/debug/evidence/**                  Phase 7 / 12 / 17
/api/v1/debug/models/**                    Phase 8 / 18
/api/v1/debug/tools/**                     Phase 9 / 19
/api/v1/debug/skills/**                    Phase 9 / 19
/api/v1/patient/**                         Phase 11+
/api/v1/clinician/**                       Phase 11+
/api/v1/fhir/**                            Phase 12 / 13 / 19
/api/v1/policies/**                        Phase 14 / 21
/api/v1/actions/**                         Phase 15 / 21
/api/v1/feedback/**                        Phase 20
/api/v1/releases/**                        Phase 18 / 20 / 21
```

## 20.2 存储

```text
runtime / evaluation / candidate / audit tables        Phase 5
agent execution / proposal / validation                Phase 6
Evidence refs / KG-lite                                Phase 7
governed model / prompt / dataset metadata             Phase 8
Tool / MCP / Skill metadata and trace                  Phase 9
role projections / read models                         Phase 11
Evidence source / asset / claim / citation             Phase 12
Raw event / ClinicalDatum / ClinicalFact / projection  Phase 13
Policy / decision / risk / lease / causal trace        Phase 14
Action / shadow / approval / commit / rollback         Phase 15
Workflow / checkpoint / handoff                        Phase 16
Deep evidence / knowledge graph / asset lifecycle      Phase 17
Model artifact / experiment / release / drift          Phase 18
Remote integration / credentials / tool versions       Phase 19
Feedback / experience / improvement candidate          Phase 20
Identity / tenant / access / release workflow          Phase 21
```

## 20.3 治理平台

```text
Runtime / Evaluation / Candidate / Audit MVP        Phase 5 / 10
Role Frontends                                      Phase 11
Evidence Panel / Trace                              Phase 12
Fact Timeline / Conflict Review                     Phase 13
Policy / Risk / Capability Console                  Phase 14
Action Approval / Kill Switch                       Phase 15
Workflow / Agent Governance                         Phase 16
Knowledge Console                                   Phase 17
Model Registry / Training Center                    Phase 18
Tool / MCP / Skill Console                          Phase 19
Experience Memory Center                            Phase 20
完整生产 Governance Platform                        Phase 21
```

---

# 二十一、AI 与技术选型完整归属

| 技术 | 正式落点 | 定位 |
|---|---|---|
| Structured Output | Phase 6+ | 所有能力返回结构化对象 |
| Function Calling | Phase 9、19 | Tool 调用机制，不是主控 |
| MCP / Spring AI MCP | Phase 9、19 | 外部连接 Adapter |
| LangChain / LlamaIndex | Phase 12、17 实现候选 | RAG / Index Provider 内部实现 |
| GraphRAG | Phase 17 | Evidence 增强 |
| LangGraph / Agent SDK | Phase 16、22 | Workflow 内部实现或实验 |
| Multi-Agent / Handoff | Phase 16 | 后台受控协作 |
| LLM-as-a-Judge | Phase 8、18 | 辅助 Scorer，非唯一判据 |
| Agent Memory | Phase 20、22 | 受治理 Experience，不自由记忆 |
| Fine-tuning / SFT | Phase 18 | 任务 Provider 优化 |
| LoRA / QLoRA | Phase 18 | 低成本适配 |
| DPO / RFT | Phase 18 条件能力 | 偏好或奖励优化 |
| Distillation / Pruning / Quantization | Phase 18 条件能力 | 部署优化 |
| Embedding / Reranker | Phase 12、18 | Evidence 和检索能力 |
| pgvector | Phase 12、17 | 默认向量基础设施 |
| Neo4j / Milvus / Qdrant | Phase 17 条件能力 | 规模证明必要后进入 |
| Voice / Realtime | Phase 22 | 新交互通道 |
| Browser / Computer Use | Phase 19、22 条件实验 | 受控 Tool Adapter |
| Codex / Claude Code / OpenHands | NON-PRODUCT | 开发辅助工具 |

---

# 二十二、Phase 11 后新增设计追踪矩阵

| 新增设计 | 正式阶段 |
|---|---|
| SourceRegistry / EvidenceAssetVersion | Phase 12、17 |
| EvidenceClaim / ClaimEvidenceLink | Phase 12 |
| Applicability / Grade / Freshness / Citation Verification | Phase 12、17 |
| EvidenceConflictSet | Phase 12、17 |
| ClinicalQuestionNormalizer / RetrievalPlanner | Phase 12 最小版，Phase 17 完整版 |
| Clinician Evidence Panel | Phase 11 收口、Phase 12 实数据、Phase 17 增强 |
| ClinicianFeedback | Phase 20 |
| RawSourceEvent / append-only ledger | Phase 13 |
| ClinicalFactLedger / CurrentClinicalStateProjection | Phase 13 |
| FHIR resource-specific adapters | Phase 12 只读，Phase 13 事实化 |
| Bi-temporal / Supersession / Conflict | Phase 13 |
| Clinical Fact Graph / Runtime Evidence Graph | Phase 13 |
| RuntimeDatum / Provenance / Trust | Phase 12、14 |
| CapabilityDecision / Policy IR | Phase 12 最小版，Phase 14 正式版 |
| Pre / Post Capability Safety | Phase 12、14 |
| RuntimeRiskState | Phase 14 |
| RecoveryPolicy | Phase 12、14 |
| CapabilityLease | Phase 14 |
| Causal Trace / Error Propagation Chain | Phase 14 |
| RuntimeActionProposal | Phase 15 |
| Shadow / Staged Execution / Commit / Rollback | Phase 15 |
| 分层 Evaluation / False Block Rate | Phase 12 起持续，Phase 14 / 15 加固 |

---

# 二十三、永久禁止与条件能力

## 23.1 永久禁止

```text
LLM / Agent / Multi-Agent 成为 Runtime 主控；
Agent 直接写 ClinicalFactLedger 或 RuntimeState；
RAG 直接回答患者或决定诊断；
外部文档 / ToolResult 作为控制指令；
未经授权和验证执行外部副作用；
自动处方或自主医疗决定；
Candidate / Feedback / Training Trace 自动上线；
Console 绕过 Evaluation / Review / Release；
患者端暴露 Chain-of-Thought、Secret、内部 Policy / Risk 或 raw PHI。
```

## 23.2 条件能力

```text
写操作
→ Phase 15 冻结后启用。

Multi-Agent
→ 单 Agent、Policy、Trace 和 Evaluation 稳定后启用。

复杂 Graph / Vector Infrastructure
→ 数据规模和评测证明必要后启用。

训练 / 后训练
→ 真实数据、明确任务和可靠 Evaluation 后启用。

Browser / Computer Use
→ 隔离、最小权限、完整 Trace 和人工审批后实验。
```

---

# 二十四、跨阶段验证要求

任何阶段均需按适用性覆盖：

```text
Backend Unit / Integration
InMemory / PostgreSQL parity
Provider Contract / Schema
Frontend Test / Build
Role Projection Security
Failure Injection
Regression
Manual Clinical Review
Performance / Cost
Audit Completeness
```

重要故障注入：

```text
LLM timeout / malformed JSON
Embedding / Reranker failure
stale / conflicting evidence
citation not entailed
FHIR timeout / unauthorized / malformed resource
patient-reported vs EHR conflict
prompt injection in RAG / ToolResult
illegal AgentProposal
unauthorized state write
role leakage
expired lease
partial external write
rollback failure
revoked model / tool / policy version
```

---

# 二十五、当前实际执行顺序

尽管路线覆盖 Phase 0–22，当前只允许按以下顺序推进：

```text
1. 收口并冻结 Phase 11-P1。
2. 建立 Phase 12-P0 实现规格、API 与测试设计、任务清单。
3. 完成 Clinical Evidence Engine MVP。
4. 完成真实 LLM Agent、只读 FHIR 和最小统一治理。
5. 完成胸痛 / 胸闷纵切与量化评测。
6. 冻结 Phase 12。
7. 再决定进入 Phase 13，禁止并行铺开 Phase 14–22。
```

---

# 二十六、文档同步与完整性验收

路线图每次升级必须检查：

- 完整系统设计中的每个能力域是否有 Phase；
- 技术实现总方案中的每个包、API、存储对象是否有 Phase；
- 专项规划中的正式能力是否进入路线；
- 新增架构对象是否同时进入总设计、技术方案和路线图；
- 历史冻结状态是否保持真实；
- 当前下一阶段是否唯一、明确且满足依赖；
- 永久禁止项和条件后置项是否被区分。

本 v3.0 已完成对当前完整系统设计、技术实现总方案和 Phase 11 后新增三组设计建议的全量映射。

---

# 二十七、最终路线

```text
Phase 0–11
建立 Runtime、治理和原型能力基础。

Phase 12
用真实 Evidence、真实 Agent、只读 FHIR 和胸痛纵切证明系统价值。

Phase 13–15
补齐患者事实、统一 Runtime 治理和事务动作内核。

Phase 16–20
补齐 Agent、Workflow、Knowledge、Model、Tool 和 Experience 完整生命周期。

Phase 21
形成生产治理、审核、发布、运维和合规平台。

Phase 22
在隔离边界内探索高级交互与前沿技术。
```

完整设计范围全部进入路线，但实现始终坚持：

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
