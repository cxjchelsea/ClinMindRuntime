# ClinMindRuntime 技术实现总方案

> 当前技术蓝图版本：v3.0  
> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md` v3.0  
> 阶段路线：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md` v3.0  
> 架构决策：`docs/1-总设计/Phase11后架构缺口与路线收敛决策.md`  
> 当前实现状态：Phase 0–11 P0 已冻结；Phase 11-P1 收口中；下一实现主线为 Phase 12。

本文档说明 ClinMindRuntime 的完整系统设计如何落到代码、模块、依赖、领域对象、接口、API、存储、测试、部署和运维中。

它不是当前阶段任务清单。任何实现仍必须经过：

```text
阶段路线
→ 当前 Phase 实现规格
→ API 与测试设计
→ 开发任务清单
→ 代码
→ 测试
→ 冻结记录
```

---

# 一、技术实现目标

技术实现必须确保：

```text
1. Runtime 是唯一主控。
2. Clinical Fact、Medical Evidence、System Experience 和 Agent Inference 分离。
3. Agent / RAG / Model / Tool 只能返回结构化候选。
4. 外部数据不能成为控制指令。
5. 所有能力调用均有授权、版本、来源、Trace 和 Evaluation。
6. 能力执行前后均可进行安全判断。
7. 真实写操作具备 Shadow / Staged Execution、Commit 和 Rollback。
8. 患者、医生和治理端由后端进行角色安全投影。
9. 所有资产、模型、Policy、Skill 和 Capability 可发布、撤销和回滚。
10. 生产系统具备认证、租户、审计、可观测性、备份和合规控制。
```

---

# 二、当前实现基线

已实现或形成原型：

```text
RuntimeService / RuntimeState / SafetyGate / DecisionBoundary / RuntimeTrace
AssetPackage / Provider / CapabilityProfile
Evaluation / Candidate / Review
PostgreSQL / Repository / AuditLog
Controlled Agent Runtime 与 InquiryPlanningAgent 规则实现
RAG EvidenceCandidate / EvidenceValidation / KG-lite 原型
Python Provider 与 Model / Prompt / Dataset Governance 对象
Tool / MCP / Skills Registry、Policy、Validation 和 Mock / Local Adapter
Governance Console
Patient / Clinician / Governance 三角色前端
Runtime-backed Role View API 与 fallback
```

仍需升级：

```text
真实 Clinical Evidence Engine
真实 LLM-backed Agent
只读 FHIR 数据链
ClinicalFactLedger / Bi-temporal State
统一 Policy IR / RuntimeRiskState / CapabilityLease
ActionProposal / Shadow / Staged Execution
完整 Agent / Workflow / Multi-Agent
高级 Knowledge / GraphRAG
真实 Model Training Lifecycle
真实远程 MCP / 医疗系统集成
Experience Memory 完整闭环
生产认证、租户、治理、发布、运维和合规
```

---

# 三、总体技术架构

## 3.1 逻辑分层

```text
1. API / Role Projection / Governance UI Layer
2. Application / Runtime Orchestration Layer
3. Clinical Domain & Governance Kernel Layer
4. Provider / Agent / Tool Execution Layer
5. Storage / Integration Layer
6. Operations / Security Layer
```

## 3.2 依赖方向

```text
API / UI
→ Application / Runtime Orchestration
→ Domain Interfaces / Governance Kernel
→ Provider / Agent / Tool Ports
→ Adapter Implementations
→ Storage / External Integration
```

禁止：

```text
Provider 反向控制 RuntimeService；
Agent 直接写 RuntimeState 或 ClinicalFactLedger；
Tool / MCP 直接提交外部副作用；
Storage 层承载医疗决策逻辑；
Frontend 读取 raw snapshot、Prompt、Secret 或内部推理；
Console 绕过 Evaluation / Review / Release 修改生产权限。
```

## 3.3 建议仓库模块

当前可保持单仓库，逐步演进为：

```text
backend/ 或现有 Spring Boot 根模块
  Runtime、Domain、Governance、API、Persistence

ai-provider/
  Python FastAPI 模型 Provider、Embedding、Reranker、LLM Adapter

evidence-service/（Phase 12 后按复杂度决定是否独立）
  ingestion、index、retrieval、claim、citation verification

training/
  dataset build、experiment、evaluation、model release tools

console-web/
patient-web/
clinician-web/

docs/
assets/
deployment/
```

Phase 12 P0 不要求拆微服务。优先保持模块化单体，通过接口和 Provider 隔离能力；只有负载、部署或技术栈差异明确时再独立服务。

---

# 四、Java 包结构 v3.0

```text
com.clinmind.runtime
├── api
│   ├── runtime
│   ├── patient
│   ├── clinician
│   ├── governance
│   ├── debug
│   ├── fhir
│   ├── policy
│   ├── action
│   ├── feedback
│   └── release
│
├── application
│   ├── RuntimeService
│   ├── RuntimeCommandService
│   ├── RuntimeQueryService
│   ├── RuntimeContinuationService
│   ├── RuntimeRecoveryService
│   └── RuntimeProjectionService
│
├── domain
│   ├── runtime
│   ├── caseframe
│   ├── ddx
│   ├── question
│   └── evidencegraph
│
├── clinicaldata
│   ├── event
│   ├── datum
│   ├── fhir
│   ├── provenance
│   └── source
│
├── clinicalfact
│   ├── fact
│   ├── ledger
│   ├── adapter
│   ├── reconciliation
│   ├── projection
│   ├── temporal
│   └── graph
│
├── evidence
│   ├── source
│   ├── asset
│   ├── claim
│   ├── retrieval
│   ├── rerank
│   ├── applicability
│   ├── citation
│   ├── conflict
│   ├── graph
│   └── ingestion
│
├── capability
│   ├── orchestration
│   ├── invocation
│   ├── validation
│   ├── decision
│   └── lease
│
├── policy
│   ├── model
│   ├── engine
│   ├── predicate
│   ├── enforcement
│   └── repository
│
├── risk
│   ├── RuntimeRiskState
│   ├── RiskSignal
│   ├── RiskTransition
│   └── RiskStateService
│
├── safety
│   ├── PreCapabilitySafetyGate
│   ├── PostCapabilitySafetyGate
│   ├── RedFlagRuleEvaluator
│   └── SafetyInvariant
│
├── recovery
│   ├── RecoveryAction
│   ├── RecoveryPolicy
│   ├── RecoveryPlanner
│   └── RecoveryResult
│
├── action
│   ├── proposal
│   ├── shadow
│   ├── staged
│   ├── approval
│   ├── commit
│   ├── rollback
│   └── compensation
│
├── agent
│   ├── registry
│   ├── runtime
│   ├── policy
│   ├── proposal
│   ├── validation
│   ├── trace
│   ├── inquiry
│   ├── evidence
│   ├── ddx
│   ├── rewrite
│   ├── report
│   ├── review
│   └── workflow
│
├── provider
│   ├── llm
│   ├── embedding
│   ├── reranker
│   ├── classifier
│   ├── rewrite
│   └── judge
│
├── model
│   ├── registry
│   ├── metadata
│   ├── version
│   ├── prompt
│   ├── experiment
│   ├── release
│   ├── rollback
│   └── drift
│
├── training
│   ├── dataset
│   ├── preference
│   ├── candidate
│   └── evaluation
│
├── tool
│   ├── registry
│   ├── policy
│   ├── adapter
│   ├── execution
│   └── trace
│
├── mcp
│   ├── client
│   ├── adapter
│   ├── metadata
│   └── resource
│
├── skill
│   ├── metadata
│   ├── registry
│   ├── policy
│   ├── provider
│   └── release
│
├── experience
│   ├── feedback
│   ├── candidate
│   ├── asset
│   ├── provider
│   ├── context
│   └── recertification
│
├── boundary
│   ├── DecisionBoundaryService
│   ├── PatientOutputMapper
│   ├── ClinicianReportMapper
│   ├── GovernanceProjectionMapper
│   └── BoundaryViolation
│
├── projection
│   ├── patient
│   ├── clinician
│   ├── governance
│   ├── policy
│   └── sanitizer
│
├── trace
│   ├── runtime
│   ├── capability
│   ├── causal
│   └── correlation
│
├── evaluation
├── candidate
├── asset
├── governance
├── audit
├── persistence
├── security
├── observability
└── config
```

包可以按阶段逐步建立，禁止一次性创建全部空目录。

---

# 五、统一 Runtime 主链路实现

## 5.1 目标调用顺序

```text
RuntimeController
→ RuntimeService.start / continue
→ RawSourceEventService.append
→ RuntimeDatumFactory.classifyProvenance
→ ClinicalStateContextService.load
→ EntryAssessmentService.assess
→ PreCapabilitySafetyGate.evaluate
→ RuntimeRiskStateService.transition
→ CapabilityOrchestrationService.plan
→ CapabilityDecisionEngine.decide
→ CapabilityLeaseService.issue
→ Agent / Evidence / Model / Tool Adapter
→ CapabilityResultValidator.validate
→ PostCapabilitySafetyGate.evaluate
→ RuntimeRecoveryService.recover if needed
→ RuntimeStateCommitService.apply
→ ActionRuntime when external side effect exists
→ DecisionBoundaryService.project
→ RoleProjectionService
→ RuntimeTrace / CausalTrace / AuditLog
→ EvaluationHook
```

## 5.2 RuntimeService 边界

```java
public interface RuntimeService {
    RuntimeResponse start(StartRuntimeCommand command);
    RuntimeResponse continueRuntime(ContinueRuntimeCommand command);
    RuntimeSnapshot getSnapshot(RuntimeId runtimeId, ActorContext actor);
}
```

RuntimeService 负责：

```text
协调流程；
持有提交权；
不直接实现 LLM、RAG、FHIR 或模型算法；
不把外部 Provider 结果无校验写入状态；
不向 API 返回 raw internal state。
```

## 5.3 Runtime 状态边界

RuntimeState 保存当前执行状态：

```text
runtime_id
status / mode
case_frame
current risk summary
ddx board
runtime evidence graph
question policy
capability execution summary
output boundary state
projection version
checkpoint ref
```

RuntimeState 不保存完整纵向患者历史。长期事实由 ClinicalFactLedger 保存。

---

# 六、Clinical Data & Fact 实现

## 6.1 RawSourceEvent

```java
public record RawSourceEvent(
    String eventId,
    String patientRef,
    SourceType sourceType,
    String sourceId,
    String sourceVersion,
    Instant eventTime,
    Instant recordedTime,
    JsonNode payload,
    String checksum,
    ProvenanceRef provenance
) {}
```

要求：

```text
append-only；
原始 payload 加密或安全引用；
记录 checksum；
支持去重和幂等；
错误纠正通过新事件完成，不覆盖旧事件。
```

## 6.2 ClinicalDatum

```java
public record ClinicalDatum<T>(
    String datumId,
    T value,
    SourceType sourceType,
    String sourceId,
    String sourceVersion,
    TrustLevel trustLevel,
    boolean instructionAllowed,
    ClinicalAuthority clinicalAuthority,
    VerificationStatus verificationStatus,
    Instant eventTime,
    Instant recordedTime,
    ProvenanceRef provenance
) {}
```

默认：USER、RAG、MODEL、TOOL、MEMORY 的 `instructionAllowed=false`。

## 6.3 ClinicalFact

```text
fact_id
patient_ref
fact_type
resource_type
native_clinical_status
native_verification_status
governance_state
clinical_certainty
authority_level
event_time
recorded_time
valid_from
valid_to
source_event_id
derived_from
supersedes
conflicts_with
version
```

## 6.4 FHIR Adapter

接口：

```java
public interface ClinicalFactAdapter<R> {
    boolean supports(String resourceType);
    List<ClinicalFactCandidate> extract(R resource, ClinicalProvenance provenance);
    FactValidationResult validate(ClinicalFactCandidate candidate);
}
```

首批实现：

```text
ConditionFactAdapter
ObservationFactAdapter
MedicationRequestFactAdapter
AllergyIntoleranceFactAdapter
```

不得用一个统一状态机替代资源原生状态。

## 6.5 Reconciliation

```text
Candidate Facts
→ source / authority comparison
→ temporal validity check
→ native status semantics
→ supersession detection
→ conflict classification
→ accepted / disputed / unresolved / unusable
→ CurrentClinicalStateProjection
```

高风险冲突不得自动选择唯一真相，进入 REVIEW_REQUIRED。

## 6.6 检索顺序

```text
candidate recall
→ patient / tenant / permission filter
→ point-in-time validity filter
→ native resource status filter
→ supersession / conflict handling
→ relevance ranking
→ context budget assembly
```

硬约束不能被相似度覆盖。

---

# 七、Clinical Evidence Engine 实现

## 7.1 领域模型

```text
SourceRegistry
- source_id
- publisher
- source_type
- authority_level
- jurisdiction
- license
- homepage
- review_policy
- trust_status

EvidenceAssetVersion
- asset_id / version_id
- title
- publication_date
- effective_from / effective_to
- supersedes
- specialty
- intended_audience
- review_status
- checksum

EvidenceClaim
- claim_id
- statement
- population
- intervention
- comparator
- outcome
- recommendation_strength
- evidence_quality
- source_span

ClaimEvidenceLink
- generated_claim_id
- evidence_claim_id
- relation
- entailment_score
- applicability_score
- conflict_status
```

关系：

```text
SUPPORTS
PARTIALLY_SUPPORTS
CONTRADICTS
OUT_OF_SCOPE
INSUFFICIENT
```

## 7.2 Ingestion Pipeline

```text
Source Registration
→ License / Jurisdiction Check
→ Asset Version Creation
→ Download / Parse
→ Structural Segmentation
→ Chunk / Span
→ Metadata Extraction
→ Claim Extraction Candidate
→ Human / Rule Validation
→ Embedding / Lexical Index
→ Publish Candidate
→ Evaluation
→ Release
```

P0 可以人工准备少量高质量语料，不要求自动化全流程。

## 7.3 Retrieval Pipeline

```text
ClinicalQuestionNormalizer
→ RetrievalPlanner
→ BM25 Recall
→ Dense Recall
→ Candidate Merge / Dedup
→ Cross-encoder Rerank
→ Source Authority Filter
→ Freshness Check
→ Patient Applicability
→ Evidence Claim Extraction
→ Citation Entailment
→ Conflict Detection
→ EvidenceValidation
→ RuntimeEvidenceGraph
```

Provider 接口：

```java
public interface EvidenceProvider {
    EvidenceRetrievalResult retrieve(
        EvidenceRetrievalRequest request,
        CapabilityLease lease
    );
}
```

## 7.4 评分结构

```java
public record EvidenceScore(
    double retrievalRelevance,
    AuthorityLevel sourceAuthority,
    EvidenceQuality evidenceQuality,
    double patientApplicability,
    FreshnessStatus freshness,
    double citationEntailment,
    ConflictStatus conflictStatus
) {}
```

不提供单一总分作为唯一采用依据。

## 7.5 Quick / Deep Mode

```text
Quick Evidence Mode
单问题、单轮混合检索和引用验证。

Deep Evidence Mode
问题拆分、多查询、来源去重、支持与反对证据聚合、冲突分析和综合报告。
```

Deep Mode 进入 Phase 17，不阻塞 Phase 12 P0。

---

# 八、Unified Runtime Governance Kernel 实现

## 8.1 Policy IR

```java
public record CapabilityPolicy(
    String policyId,
    String policyVersion,
    CapabilityType capabilityType,
    TriggerPhase triggerPhase,
    List<PolicyPredicate> requiredConditions,
    List<PolicyPredicate> forbiddenConditions,
    Set<String> permittedInputScopes,
    Set<String> permittedOutputTypes,
    EnforcementAction enforcementAction,
    RecoveryAction recoveryAction
) {}
```

P0 使用 Java 对象和配置，不提前建设复杂 DSL。后续可增加 YAML / JSON Policy 定义和静态校验。

## 8.2 CapabilityDecision

```java
public record CapabilityDecision(
    String decisionId,
    String capabilityId,
    Decision decision,
    List<String> reasonCodes,
    Set<String> inputScopes,
    Set<String> outputConstraints,
    List<String> requiredValidators,
    Instant expiresAt,
    List<PolicyRef> policyRefs
) {}
```

```text
Decision = ALLOW / DEGRADE / REVIEW_REQUIRED / BLOCK
```

## 8.3 Capability Lease

```java
public record CapabilityLease(
    String leaseId,
    String runtimeId,
    String actorId,
    String capabilityId,
    Set<String> allowedScopes,
    Set<String> allowedActions,
    Instant issuedAt,
    Instant expiresAt,
    int maxInvocations,
    List<String> revocationConditions,
    LeaseStatus status
) {}
```

每次执行使用短期授权，不因 Registry 注册能力而永久获得权限。

## 8.4 RuntimeRiskState

```text
current_risk_level
accumulated_signals
unresolved_conflicts
active_safety_constraints
component_health
evidence_reliability
state_confidence
blocked_capabilities
degraded_capabilities
last_safe_checkpoint
recovery_status
```

RiskTransition 必须记录原因、来源、时间和关联 Trace。

## 8.5 双安全门

```java
public interface PreCapabilitySafetyGate {
    SafetyGateResult evaluate(RuntimeContext context);
}

public interface PostCapabilitySafetyGate {
    PostCapabilitySafetyResult evaluate(
        RuntimeContext before,
        CapabilityExecutionBundle results
    );
}
```

Post Gate 检查：

```text
新红旗信号；
异常 FHIR Observation；
药物 / 过敏冲突；
证据冲突或过期；
Agent 非法 Proposal；
ToolResult 注入指令；
Provider 降级或版本异常；
角色数据泄漏风险。
```

## 8.6 Recovery

```java
public enum RecoveryAction {
    RULE_FALLBACK,
    RESTRICTED_RETRY,
    ASK_CLARIFICATION,
    SWITCH_TO_READ_ONLY,
    HUMAN_REVIEW,
    ROLLBACK_TO_CHECKPOINT,
    SAFE_HALT
}
```

RecoveryResult 进入 RuntimeTrace 和 Evaluation。

---

# 九、Transactional Action Governance 实现

仅当存在真实外部副作用时启用。

## 9.1 ActionProposal

```java
public record RuntimeActionProposal(
    String proposalId,
    String runtimeId,
    String capabilityId,
    ActionType actionType,
    Set<ResourceRef> readSet,
    Set<ResourceRef> writeSet,
    List<ExternalSideEffect> externalSideEffects,
    StatePatch proposedStatePatch,
    List<EvidenceRef> evidenceRefs,
    List<ProvenanceRef> provenanceRefs,
    double confidence
) {}
```

## 9.2 执行器

```text
ActionAuthorizationService
ActionPreconditionValidator
ShadowRuntimeService
StagedToolExecutor
ActionResultValidator
ActionPostconditionValidator
ActionCommitService
RollbackService
CompensationService
HumanApprovalService
```

## 9.3 幂等与补偿

所有写动作必须具有：

```text
idempotency_key
external_operation_id
staged_result_ref
commit_status
rollback_status
compensation_status
approval_ref
```

## 9.4 高风险动作

```text
病历写入
报告提交
转诊
预约
其他医院系统写操作
```

必须具备人工审批、Kill Switch、凭证撤销和 Write Audit。

---

# 十、Agent 与 Workflow 实现

## 10.1 Agent 统一接口

```java
public interface ControlledAgent<C, P> {
    AgentMetadata metadata();
    P execute(C context, CapabilityLease lease);
}
```

所有 Agent 必须配置：

```text
Registry
Policy
Context Contract
Structured Output Schema
Validator
Trace
Evaluation Hook
Fallback
Version
```

## 10.2 LLM-backed InquiryPlanningAgent

```text
RuntimeState / CaseFrame / Missing Facts / RiskState
→ LLM Provider structured output
→ InquiryPlanProposal
→ Schema Validation
→ Clinical / Policy Validation
→ Partial Accept / Reject / Fallback
```

规则 Agent 保留为 fallback 和基线。

## 10.3 Workflow

Workflow Definition 只描述受控节点：

```text
Node
Transition
Checkpoint
Retry Policy
Timeout
Fallback Node
Human Review Node
Pause / Resume
State Synchronization
```

LangGraph 或其他框架只能作为 Provider 内部实现，Java Runtime 仍控制授权、状态提交和边界。

## 10.4 Multi-Agent

Multi-Agent 必须通过统一 CapabilityDecision 和 Handoff Contract：

```text
source_agent
 target_agent
handoff_reason
shared_context_scope
forbidden_context
expected_output_type
lease_ref
trace_ref
```

不允许共享全量患者状态或自主扩大 Tool 权限。

---

# 十一、ModelProvider 与训练生命周期

## 11.1 Provider 协议

```java
public interface ModelProvider<I, O> {
    ModelProviderMetadata metadata();
    O invoke(I input, CapabilityLease lease, ProviderTraceContext trace);
}
```

## 11.2 Python Provider API

建议统一：

```text
POST /v1/providers/embedding
POST /v1/providers/rerank
POST /v1/providers/inquiry-plan
POST /v1/providers/caseframe
POST /v1/providers/risk-classify
POST /v1/providers/patient-rewrite
POST /v1/providers/clinician-report
POST /v1/providers/judge
GET  /v1/providers/health
GET  /v1/providers/metadata
```

返回必须包含：

```text
provider_id
provider_version
model_id
model_version
prompt_version
schema_version
latency_ms
usage / cost
structured_result
warnings
```

## 11.3 治理流程

```text
TrainingExampleCandidate
→ Sanitization / Source Validation
→ TrainingDatasetVersion
→ ExperimentRun
→ Training / Post-training
→ ModelProviderVersion
→ Offline Evaluation
→ Safety / Regression Evaluation
→ ReleaseCandidate
→ Human Review
→ Canary / Capability Grant
→ Monitoring
→ Rollback / Revoke
```

## 11.4 训练实现

训练脚本和运行环境与在线 Runtime 隔离：

```text
training/data
training/pipelines
training/experiments
training/evaluation
training/artifacts
training/release
```

任何训练产物都不能直接覆盖在线模型文件或生产 Registry。

---

# 十二、Tool / MCP / Skills 实现

## 12.1 Tool Adapter

```java
public interface ToolAdapter<I, O> {
    ToolMetadata metadata();
    O execute(I request, CapabilityLease lease, ToolTraceContext trace);
}
```

## 12.2 MCP

```text
McpServerMetadata
McpClientConfig
McpResourceRef
McpToolDefinition
McpExecutionResult
McpHealthStatus
```

要求：

```text
Server allowlist；
Tool allowlist；
超时、重试、熔断；
Secret 引用而非明文；
输入输出 Schema；
PHI Scope；
完整 Trace；
结果重新校验。
```

Spring AI MCP 可以作为 Java 适配实现，但核心接口不得绑定具体框架。

## 12.3 FHIR Tool

Phase 12 先提供只读接口：

```text
readConditions
readObservations
readMedicationRequests
readAllergies
```

Phase 13 将结果进入 Clinical Data / Fact Plane；Phase 15 冻结前不得启用写操作。

## 12.4 Skill

Skill 是版本化能力包：

```text
skill_id
version
input_schema
output_schema
required_scopes
allowed_tools
risk_level
review_status
release_status
revocation_status
```

Skill 发布必须经过 Evaluation 和 Review。

---

# 十三、角色投影与 API 实现

## 13.1 后端投影

```text
Runtime / ClinicalState / Evidence / Governance Objects
→ RoleProjectionPolicy
→ Sanitizer
→ Safe DTO
→ Patient / Clinician / Governance API
```

禁止前端根据 raw JSON 自行隐藏敏感字段。

## 13.2 Patient DTO

```text
session_summary
known_facts
safe_questions
risk_notice
care_navigation
health_education
projection_status
limitations
```

## 13.3 Clinician DTO

```text
case_snapshot
known / missing / conflicting facts
inquiry_timeline
ddx_candidates
evidence_panel
runtime_decisions
rejected_proposals
ai_suggestions
report_draft
projection_status
```

## 13.4 Governance DTO

```text
capability_decisions
policy_refs
risk_transitions
provider / agent / tool versions
provenance
pre / post safety
recovery
causal_trace
evaluation
release_state
```

---

# 十四、API 总规划

```text
/api/v1/runtime/**
/api/v1/patient/**
/api/v1/clinician/**
/api/v1/governance/**

/api/v1/fhir/**
/api/v1/facts/**
/api/v1/evidence/**
/api/v1/policies/**
/api/v1/capabilities/**
/api/v1/actions/**
/api/v1/feedback/**
/api/v1/releases/**

/api/v1/debug/assets/**
/api/v1/debug/evaluations/**
/api/v1/debug/candidates/**
/api/v1/debug/agents/**
/api/v1/debug/evidence/**
/api/v1/debug/models/**
/api/v1/debug/tools/**
/api/v1/debug/skills/**
/api/v1/debug/persistence/**
```

原则：

```text
Debug API 仅内部可用；
生产 API 使用正式认证和最小权限；
写 API 必须进入 Action Governance；
所有 API 返回 schema_version 和 trace_ref；
列表 API 使用分页、租户和角色过滤；
错误响应不得泄露内部 Prompt、Secret 或 Stack Trace。
```

---

# 十五、持久化设计

## 15.1 已有基础

```text
runtime
runtime_trace
evaluation
candidate
review
audit_log
asset
provider metadata
role projection read models
```

## 15.2 Phase 12 Evidence

```text
evidence_source
evidence_asset_version
evidence_chunk
evidence_claim
claim_evidence_link
citation_verification_result
evidence_conflict_set
retrieval_trace
embedding_index metadata
```

## 15.3 Phase 13 Clinical Fact

```text
raw_source_event
clinical_datum
clinical_provenance
clinical_fact
clinical_fact_version
fact_supersession_link
fact_conflict_link
current_clinical_state_projection
fact_reconciliation_result
```

## 15.4 Phase 14 Governance Kernel

```text
capability_policy
policy_version
capability_decision
runtime_risk_state
risk_transition
capability_lease
recovery_execution
causal_trace_link
```

## 15.5 Phase 15 Action

```text
action_proposal
action_stage
action_approval
action_commit
action_rollback
compensation_execution
external_operation_ref
```

## 15.6 Phase 16–20

```text
agent_workflow_definition
workflow_execution
workflow_checkpoint
agent_handoff

knowledge_node / knowledge_edge
evidence_synthesis_run

model_registry
model_provider_version
prompt_version
training_dataset_version
experiment_run
release_candidate
rollback_plan
drift_finding

tool_registry
mcp_server_metadata
skill_registry
skill_version

clinician_feedback
experience_candidate
experience_unit_asset
experience_recertification
improvement_candidate
```

## 15.7 Phase 21

```text
user
organization
tenant
role
permission
policy_binding
consent
purpose_of_use
secret_reference
release_workflow
access_audit
retention_policy
```

所有 PHI 表需要明确加密、租户、访问审计和保留策略。

---

# 十六、Trace、Audit 与错误传播链

## 16.1 统一关联字段

```text
correlation_id
causation_id
runtime_id
source_event_id
fact_id
projection_version
evidence_graph_id
capability_decision_id
lease_id
agent_execution_id
proposal_id
tool_execution_id
action_id
output_id
evaluation_id
release_id
```

## 16.2 Trace 与 Audit 区分

```text
Trace
记录系统执行过程、输入输出摘要、延迟、版本和技术状态。

Audit
记录谁在什么权限下执行、查看、审核、发布、撤销或修改了什么治理对象。
```

## 16.3 因果追踪目标

系统必须能够重建：

```text
原始输入
→ Extractor / Adapter
→ Clinical Fact
→ Current Projection
→ Context Assembly
→ Agent / Provider / Tool
→ Proposal / Evidence / Result
→ Policy Decision
→ State / Action Commit
→ Role Output
→ Evaluation / Feedback / Candidate
```

---

# 十七、Evaluation 与测试实现

## 17.1 分层 Scorer

```text
FactExtractionScorer
TemporalStateScorer
ConflictResolutionScorer
EvidenceRetrievalScorer
CitationEntailmentScorer
ApplicabilityScorer
InquiryPlanCoverageScorer
ToolSelectionScorer
ArgumentValidityScorer
CapabilityDecisionScorer
RecoveryScorer
ActionCommitScorer
RoleLeakageScorer
AuditCompletenessScorer
```

## 17.2 测试类型

每个 Phase 必须按适用范围包含：

```text
Unit Test
Contract Test
Schema Test
Integration Test
PostgreSQL / InMemory Parity Test
Provider Test
Frontend Component Test
Frontend Build Test
End-to-End Test
Failure Injection
Security / Permission Test
Regression Test
Manual Clinical Review
```

## 17.3 关键故障注入

```text
LLM timeout / malformed JSON
Embedding / Reranker unavailable
stale evidence
evidence contradiction
citation not entailed
FHIR timeout / unauthorized / malformed resource
patient-reported vs EHR conflict
prompt injection in document / tool result
illegal AgentProposal
unauthorized state write
role leakage
expired CapabilityLease
policy conflict
partial external write
commit failure / rollback failure
model version revoked
```

## 17.4 核心指标

```text
task_success
safe_task_completion
clinical_safety_violation
appropriate_escalation
false_reassurance
false_block_rate
recovery_success
citation_entailment
stale_evidence_activation
current_state_accuracy
invalid_proposal_rate
tool_selection_accuracy
rollback_correctness
audit_completeness
P50 / P95 latency
cost
```

---

# 十八、安全、隐私与访问控制

## 18.1 认证与授权

```text
OAuth / OIDC / SSO
Tenant Isolation
RBAC / ABAC
PHI Scope
Purpose of Use
Consent
Capability Lease
Tool / Resource Scope
```

## 18.2 数据保护

```text
TLS
Encryption at Rest
Field-level Encryption for sensitive identifiers
Secret Manager
Credential Rotation
Data Minimization
De-identification for Evaluation / Training
Retention / Deletion Policy
Access Audit
```

## 18.3 Prompt Injection / Data Flow

```text
external data instructionAllowed=false；
control instructions only from trusted policy/config；
separate structured data from system instructions；
Tool and RAG outputs validated before reuse；
no raw external content can directly select high-risk tools；
patient-facing output built from approved structured fields。
```

---

# 十九、部署与可观测性

## 19.1 环境

```text
local
integration
staging
production
isolated experiment
```

每个环境使用独立配置、凭证、数据库和 CapabilityProfile。

## 19.2 部署

```text
Docker / Docker Compose for local and integration
CI build / test / migration check
Artifact and image versioning
Database migration gate
Canary provider release
Rollback scripts
Backup / restore verification
```

## 19.3 可观测性

```text
OpenTelemetry Trace
Prometheus Metrics
Grafana Dashboard
Structured Logs
Trace Correlation
Provider latency / error / cost
Retrieval quality
Policy decisions
Recovery and fallback rate
Tool / Action status
Model / Knowledge / Tool drift
```

禁止在日志中记录未脱敏 PHI、Prompt Secret 或凭证。

---

# 二十、前端实现规划

## 20.1 patient-web

```text
安全会话摘要
问题回答
风险提示
Care Navigation
健康教育
状态与局限提示
```

## 20.2 clinician-web

```text
Case Snapshot
Fact Timeline
Missing / Conflict Panel
DDx Board
Evidence Panel
Inquiry Timeline
Runtime Decision
Draft Report
Feedback
Human Review Action
```

## 20.3 console-web / governance-web

```text
Runtime Timeline
Capability Decision
Policy / Risk
Evaluation
Candidate / Review
Knowledge / Evidence
Model / Prompt / Dataset
Tool / MCP / Skill
Experience Memory
Release / Rollback
Audit
Operations
```

前端只消费 Safe DTO，不读取数据库或 raw Runtime snapshot。

---

# 二十一、阶段到技术实现映射

```text
Phase 11-P1
收口 Projection / API / Frontend / Freeze。

Phase 12
Evidence Engine、真实 LLM Agent、只读 FHIR、最小 CapabilityDecision、Post-Safety、Recovery 和胸痛纵切。

Phase 13
Raw Event、ClinicalFactLedger、FHIR Adapter、Reconciliation、Bi-temporal Projection、Fact Graph。

Phase 14
Policy IR、RuntimeRiskState、CapabilityLease、统一因果 Trace。

Phase 15
ActionProposal、Shadow / Staged Execution、Commit / Rollback、Approval。

Phase 16
完整 Agent、Workflow、Checkpoint、Handoff、Multi-Agent。

Phase 17
Deep Evidence、KG / GraphRAG、Knowledge Lifecycle、Knowledge Console。

Phase 18
真实 ModelProvider、Training、Post-training、Release、Rollback、Drift。

Phase 19
真实 Tool、Remote MCP、Skill Registry、FHIR/EHR/HIS/LIS/PACS Integration。

Phase 20
Clinician Feedback、Experience Memory、Improvement Candidate、Recertification。

Phase 21
Production Auth、Tenant、Governance Center、Operations、Compliance。

Phase 22
Voice / Realtime、Browser / Computer Use 和隔离技术实验。
```

---

# 二十二、当前最优实现顺序

```text
1. 完成 Phase 11-P1 文档、投影、测试和冻结。
2. 编写 Phase 12-P0 Clinical Evidence Engine 实现规格。
3. 建立 SourceRegistry、EvidenceAssetVersion、EvidenceClaim 和 ClaimEvidenceLink。
4. 接入真实 BM25 + Embedding + Reranker + Citation Verification。
5. 接入 LLM-backed InquiryPlanningAgent，保留规则 fallback。
6. 接入只读 FHIR Condition / Observation / MedicationRequest / AllergyIntolerance。
7. 建立最小 CapabilityDecision、RuntimeDatum、Post-Safety 和 Recovery。
8. 打通胸痛 / 胸闷纵切并形成分层 Evaluation。
9. 冻结 Phase 12 后再进入 Phase 13。
```

当前不应同时启动 Phase 13–22。

---

# 二十三、实现约束

永久约束：

```text
RuntimeService 保持主控；
ClinicalFactLedger 不能由 Agent 直接写入；
RAG / Model / Tool 不直接输出患者最终医疗结论；
外部数据不能控制流程；
未经授权和验证的外部副作用不能执行；
DecisionBoundary 之后不得自由扩写医疗内容；
患者端不得暴露内部推理、Trace、Policy、Secret 或未脱敏 PHI；
Candidate / Feedback / Evaluation 不自动发布；
模型、知识、Policy、Skill 和 Capability 必须可撤销和回滚。
```

---

# 二十四、最终结论

ClinMindRuntime v3.0 的技术内核是：

```text
Clinical Data / Fact 提供患者状态事实；
Clinical Evidence Engine 提供可验证医学依据；
Controlled Agent / Model / Tool 提供受控能力；
Unified Runtime Governance 决定能否调用和采用；
Transactional Action Governance 决定能否产生外部副作用；
DecisionBoundary 和 Role Projection 决定谁能看到什么；
Trace / Evaluation / Audit / Release 决定系统如何被证明、治理和演进。
```

所有代码实现都必须服务这条主线，而不能让任何模型、框架、Agent、RAG、MCP、Tool 或 Console 反向取代 Runtime 主控。
