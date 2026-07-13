# ClinMindRuntime 阶段拆分路线图

> 路线图版本：v3.0  
> 当前权威总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md` v2.2  
> 当前权威技术蓝图：`docs/1-总设计/ClinMindRuntime技术实现总方案.md` v2.2  
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
source / asset / claim / citation / vector index       Phase 12
raw events / clinical facts / provenance / conflicts   Phase 13
policy decisions / risk states / leases / causal links Phase 14
action proposals / checkpoints / approvals             Phase 15
workflow / handoff / agent registry versions           Phase 16
knowledge graph / ingestion / index versions           Phase 17
training runs / releases / drift / rollback             Phase 18
tool credentials refs / integration metadata           Phase 19
feedback / experience assets / recertification          Phase 20
users / tenants / roles / permissions / release audit  Phase 21
realtime sessions / browser action trace               Phase 22
```

---

# 二十一、AI 技术覆盖矩阵

| 技术 / 能力 | 正式归属 | 阶段 | 约束 |
|---|---|---|---|
| Structured Output | 全部外部能力基础 | Phase 2 起持续 | 不能绕过 Runtime |
| Function / Tool Calling | Tool 能力 | Phase 9、19 | 最小权限、结构化结果 |
| MCP / Spring AI MCP | 外部连接 | Phase 9、19 | 不是主控 |
| Agents SDK 类框架 | Agent 内部实现参考 | Phase 16 / 22 | 不能接管 Runtime |
| LangGraph | 可恢复 Workflow 参考 | Phase 16 / 22 | 只能位于 Provider / Agent 内部 |
| LangChain / LlamaIndex | RAG / Tool 实验实现 | Phase 12、17、22 | 不是领域边界 |
| GraphRAG | Evidence 增强 | Phase 17 | 不直接诊断 |
| LLM-as-a-Judge | Evaluation 辅助 | Phase 8、18 | 不是唯一安全判据 |
| Guardrails | Provider / Output 校验 | Phase 12、14 | 不替代 SafetyGate / Boundary |
| Skills | 可复用能力资产 | Phase 9、19 | 不自动授权 |
| Agent Memory | Experience / Fact / Agent 上下文 | Phase 13、20 | 不做无治理自由记忆 |
| Multi-Agent / Handoff | 后台和复杂受控 Workflow | Phase 16 | 不主控医疗输出 |
| CrewAI / AutoGen | 实验框架 | Phase 16-P2 / 22 | 实验隔离 |
| Voice / Realtime | 交互入口 | Phase 22 | 不改变 Runtime 权力 |
| Browser / Computer Use | 高风险工具实验 | Phase 19-P2 / 22 | 需 Phase 15 治理 |
| Fine-tuning / SFT / LoRA | ModelProvider 优化 | Phase 18 | 输出仍需 Validation |
| DPO / RFT | 偏好和任务优化 | Phase 18-P2 | 不自动扩权 |
| Distillation / Pruning / Quantization | 部署和成本优化 | Phase 18-P2 | 需效果评测 |
| Embedding / Reranker | Evidence / Memory 检索 | Phase 12、17、18 | 与证据质量分离 |
| pgvector | 默认向量基础设施 | Phase 12、17 | 优先于复杂平台 |
| Neo4j / Milvus / Qdrant | 规模化候选 | Phase 17-P2 | 由评测和规模触发 |
| Codex / Claude Code / OpenHands | 开发辅助 | 当前可用 | 非产品 Runtime 能力 |

---

# 二十二、Phase 11 后新增设计追踪矩阵

| 新增设计 | 路线阶段 |
|---|---|
| SourceRegistry / EvidenceAssetVersion | Phase 12、17 |
| EvidenceClaim / ClaimEvidenceLink | Phase 12、17 |
| 证据质量、适用性、新鲜度、冲突、引用验证 | Phase 12、17 |
| Quick / Deep Evidence Mode | Phase 12、17 |
| Clinician Feedback | Phase 20 |
| Raw Clinical Event Ledger | Phase 13 |
| ClinicalFactLedger / Current State Projection | Phase 13 |
| FHIR 资源适配器和原生状态语义 | Phase 12、13、19 |
| 双时间、替代、冲突、纵向重建 | Phase 13 |
| Clinical Fact Graph / Runtime Evidence Graph 分离 | Phase 13、17 |
| Data Provenance / Trust / Instruction Boundary | Phase 12、14 |
| CapabilityDecision / Policy IR | Phase 12、14 |
| Post-Capability SafetyGate | Phase 12、14 |
| RuntimeRiskState | Phase 14 |
| RecoveryPolicy | Phase 12、14、16 |
| Capability Lease | Phase 14 |
| ActionProposal / Shadow / Staged Commit | Phase 15 |
| 错误传播链和因果 Trace | Phase 14 |
| 分层 Evaluation 与 False Block Rate | Phase 12 起持续 |

---

# 二十三、永久禁止与非产品能力

以下内容不是“以后再做”，而是系统永久边界：

```text
Agent / Model / RAG 直接生成不受控患者最终医疗结论
Agent、Tool、MCP、Skill 直接修改 RuntimeState
RAG 检索结果直接拼接成患者答案
外部文档、ToolResult 或患者输入成为 Runtime 控制指令
Candidate / Experience / Dataset / Model / Prompt / Asset 自动发布
未经 Evaluation 和 Review 扩大 CapabilityProfile
LLM-as-a-Judge 成为唯一医疗安全判据
自由自治式 AI 医生取代医生
未授权自动处方或自主治疗决策
Console 绕过 Runtime 和发布流程改变医疗能力
```

开发辅助工具不属于产品能力完成度：

```text
Codex
Claude Code
OpenHands
IDE Agent
CI 自动代码生成工具
```

---

# 二十四、当前执行顺序

虽然路线图覆盖完整系统，实际执行仍必须保持优先级：

```text
1. 收口并冻结 Phase 11-P1。
2. 进入 Phase 12-P0 Clinical Evidence Engine。
3. 完成 Phase 12-P1 真实 Agent、FHIR 和最小统一治理。
4. 完成 Phase 12-P2 胸痛纵切和量化 Evaluation。
5. 根据纵切暴露的问题进入 Phase 13 和 Phase 14。
6. 只有出现真实写操作才启动 Phase 15。
7. 再依次扩展 Agent、Knowledge、Model、Tool、Experience 和 Production Platform。
8. Phase 22 始终保持条件触发和实验隔离。
```

下一步文档顺序：

```text
Phase 11-P1 冻结记录
→ ClinMindRuntime 完整系统设计 v3.0 同步
→ ClinMindRuntime 技术实现总方案 v3.0 同步
→ 项目设计地图 / README / AI_IMPLEMENTATION_SKILL 同步
→ Phase 12-P0 实现规格
→ Phase 12-P0 API 与测试设计
→ Phase 12-P0 开发任务清单
→ 代码与测试
```

---

# 二十五、最终结论

ClinMindRuntime 的路线不再只是：

```text
下一阶段做真实 RAG 和一个 FHIR Tool。
```

而是形成一条覆盖完整设计的长期实现路径：

```text
Phase 0–11
建立 Runtime、治理和原型能力广度

Phase 12
完成真实临床证据、真实 Agent、只读 FHIR 和单场景闭环

Phase 13–15
补齐患者事实、统一 Runtime 内核和有副作用动作事务治理

Phase 16–20
完成 Agent、Knowledge、Model、Tool、Experience 的全部能力域

Phase 21
完成生产级治理、发布、运维和合规平台

Phase 22
承载语音、Realtime、Browser 和前沿框架的受控实验
```

这样既保证当前工作不会继续无边界横向扩张，也保证完整系统设计、技术实现总方案和新增架构建议中的每一项，都在正式路线图中拥有明确归属。