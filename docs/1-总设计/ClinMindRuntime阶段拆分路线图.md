# ClinMindRuntime 阶段拆分路线图

> 路线图版本：v3.0  
> 当前权威总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md` v2.2  
> 项目定位：受控医疗 AI Agent Runtime 与能力治理平台  
> 当前状态：Phase 1–11 P0 已冻结；Phase 11-P1 正在收口；下一条主线为 Phase 12 真实临床能力纵切。  
> 路线收敛决策：`docs/1-总设计/Phase11后架构缺口与路线收敛决策.md`

本文档负责把 ClinMindRuntime 的长期愿景拆分为可进入、可验收、可冻结的阶段。v3.0 不再以“继续增加模块数量”为目标，而是将项目从横向原型覆盖转向真实能力、真实数据、真实评测和纵向临床闭环。

---

# 一、路线图定位

ClinMindRuntime 不是普通医疗问答系统，也不是自由自治式医疗 Agent。它的核心定位保持不变：

```text
受控医疗 AI Agent Runtime 与能力治理平台
```

Runtime 负责控制状态、能力授权、安全门、结果校验、输出边界、审计和恢复；Agent、RAG、Model、Tool 只能作为受控能力单元产生 Proposal、Evidence、Draft 或 Structured Result。

v3.0 的阶段重点从：

```text
补齐 Agent / RAG / Model / Tool / Console 能力域
```

转向：

```text
真实临床能力纵切
→ 统一 Runtime 执行治理
→ 患者事实与纵向状态
→ 有副作用动作的事务治理
→ 深度证据综合
→ 生产级平台化
```

---

# 二、阶段命名与进入规则

```text
Phase X
= 一个可独立说明系统能力成熟度的阶段。

Phase X-P0
= 证明该阶段核心链路成立。

Phase X-P1
= 加固治理、补齐真实数据或扩展关键场景。

Phase X-P2
= 形成端到端闭环、可视化或正式冻结。
```

每个阶段必须同时具备：

1. 明确的进入条件；
2. 明确的实现范围；
3. 明确的禁止边界；
4. 自动化测试与人工验证；
5. 冻结记录；
6. 不得通过修改历史冻结记录来伪造阶段状态。

---

# 三、总体阶段路线

| 阶段 | 名称 | 核心目标 | 当前状态 |
|---|---|---|---|
| Phase 0–5 | Runtime 与治理基础 | Runtime、Asset、Evaluation、Candidate、Persistence、Audit、Console 基础闭环 | 已冻结 |
| Phase 6-P0 | 受控 Agent 执行层 MVP | Agent 只能生成可校验 Proposal，由 Runtime 决定采纳、拒绝或降级 | 已冻结 |
| Phase 7-P0/P1 | RAG Evidence / KG-lite 原型 | EvidenceCandidate、EvidenceValidation、EvidenceGraph、KG-lite 路径原型 | 已冻结 |
| Phase 8-P0/P1/P2 | Python Provider 与模型治理原型 | Provider 接入、能力画像、模型/Prompt/数据集治理对象 | 已冻结 |
| Phase 9-P0 | Tool / MCP / Skills 治理原型 | Tool Registry、Policy、Validation、Mock/Local Adapter | 已冻结 |
| Phase 10-P0 | Governance / Runtime Console MVP | Safe DTO、只读治理 API、Runtime Timeline 与治理观察页面 | 已冻结 |
| Phase 11-P0 | Role-based Frontend Suite | Patient / Clinician / Governance 三角色前端与前端 RBAC Demo | 已冻结 |
| Phase 11-P1 | Runtime-backed Role-specific View API | Patient / Clinician 后端安全投影、API-first 前端、RuntimeStore 优先与 fallback | 收口中 |
| Phase 12-P0 | Clinical Evidence Engine MVP | 真实公开医学资料、混合检索、证据资产、引用验证 | 下一主阶段 |
| Phase 12-P1 | Controlled Capability Runtime | 真实 LLM Agent、统一能力决定、来源可信度、结果后安全重评估与恢复 | 规划中 |
| Phase 12-P2 | Clinical Vertical Slice | 胸痛/胸闷场景端到端真实能力闭环与量化评测 | 规划中 |
| Phase 13-P0 | Clinical Data / Fact Foundation | 患者事实、医学知识、系统经验分离；只读 FHIR 事实投影 | 条件后置 |
| Phase 13-P1 | Temporal Reconciliation | 双时间、来源冲突、替代关系、当前状态重建 | 条件后置 |
| Phase 14 | Transactional Action Governance | Shadow State、Staged Execution、Commit/Rollback、Capability Lease | 有写操作后启动 |
| Phase 15 | Deep Evidence Synthesis | 问题拆分、多查询检索、支持/反对证据与冲突综合 | 条件后置 |
| Phase 16 | Production Platformization | 正式认证、多租户、真实审核、运维、合规与外部医疗系统 | 长期后置 |

---

# 四、Phase 0–11：已完成的架构广度验证

Phase 0–11 已经证明以下设计可以在同一工程中成立：

```text
Runtime 主控
Asset 与 Provider 版本化
SafetyGate / DecisionBoundary
Agent Proposal / Runtime Validation
RAG EvidenceCandidate / EvidenceGraph
Model / Prompt / Dataset Governance
Tool / MCP / Skills Governance
Trace / Audit / Evaluation / Candidate
PostgreSQL / Repository 双实现
Governance Console
Patient / Clinician / Governance 三角色视图
```

但必须明确：

```text
Phase 0–11 完成的是架构和治理原型，
不等于真实临床能力、真实模型能力或生产级医疗系统已经完成。
```

当前主要限制包括：

- InquiryPlanningAgent 仍以规则和模板为主；
- RAG 主要使用 YAML 语料与关键词/规则检索；
- Embedding、Rerank、Judge、Risk Classifier 仍包含 Mock 或确定性实现；
- Tool / MCP / Skills 主要为 Mock 或 Local Adapter；
- Patient / Clinician 视图虽已接入 RuntimeStore，但部分领域段仍为 PARTIAL 或 fallback；
- 没有真实 FHIR/EHR 数据链；
- 没有真实外部写操作与事务提交；
- 没有生产级认证、患者数据隔离和医生审核平台。

---

# 五、Phase 11-P1：当前收口阶段

## 目标

```text
RuntimeState / CaseFrame / PatientOutput / ClinicianReport / DecisionBoundary
→ Role-specific Projection
→ Patient / Clinician Safe DTO
→ Patient / Clinician Read API
→ API-first Frontend
```

## 当前已建立

- PatientViewSource / ClinicianViewSource；
- RuntimeStoreViewSource；
- DemoRuntimeSeedViewSource；
- RuntimeStore 优先、Demo fallback；
- COMPLETE / PARTIAL / FALLBACK 投影状态；
- Patient / Clinician View API；
- patientClient / clinicianClient；
- Projection Policy、Sanitizer、Audit 与测试。

## 冻结前必须完成

1. 更新 Phase 11-P1 开发任务清单的实际状态；
2. 补齐人工测试结果；
3. 补齐冻结记录；
4. 验证 RuntimeStore 主路径和 fallback 标记；
5. 将医生端 Evidence Panel、Inquiry Timeline、AI Suggestions 尽可能从真实 Runtime 对象投影；
6. 将患者端 Care Navigation 从 SafetyGate / DecisionBoundary 生成；
7. 运行 Java、Python、TypeScript、Vitest 和生产构建验证；
8. 同步 README、项目地图和实现约束。

## 禁止

- 不在 Phase 11-P1 顺手接入真实 LLM、真实 RAG 或 FHIR；
- 不引入正式登录和生产级权限；
- 不加入处方、转诊、预约、支付或外部写操作；
- 不修改历史冻结记录中的事实。

---

# 六、Phase 12：真实临床能力纵切

Phase 12 是 v3.0 的主线。目标不是继续增加治理对象，而是让真实 Agent、真实 Evidence 和真实患者数据接口在既有 Runtime 治理下工作。

## Phase 12-P0：Clinical Evidence Engine MVP

### 核心目标

```text
将 EvidenceProvider 从规则化检索原型升级为可追踪、可分级、可验证的真实临床证据引擎。
```

### 核心对象

```text
SourceRegistry
EvidenceAssetVersion
EvidenceClaim
ClaimEvidenceLink
```

### 核心链路

```text
Clinical Question
→ Question Normalization
→ BM25 Recall
→ Embedding Recall
→ Candidate Merge
→ Cross-encoder Rerank
→ Evidence Extraction
→ Evidence Validation
→ Citation Verification
→ EvidenceGraph
```

### 评分维度

```text
retrieval_relevance
source_authority
evidence_quality
patient_applicability
freshness
citation_entailment
conflict_status
```

上述维度不得被简单压缩成一个模糊综合分数；硬性禁止条件必须优先于相似度。

### 首个领域

```text
胸痛 / 胸闷风险分层与追问
```

### 允许

- 使用公开且许可清晰的医学指南、患者教育资料或开放医学文献；
- 真实 embedding；
- BM25 + dense hybrid retrieval；
- Cross-encoder reranker；
- 来源版本、source span 和 checksum；
- 医生端证据展示；
- Evidence Evaluation。

### 禁止

- 全病种知识库；
- 自动知识发布；
- RAG 直接生成 PatientOutput；
- 复杂 GraphRAG 平台；
- Multi-Agent 证据辩论；
- 未解决版权或授权边界的全文资料入库。

### 完成标准

1. 关键 EvidenceClaim 可追踪到 SourceRegistry、AssetVersion 和 SourceSpan；
2. 医生端关键主张具有来源、版本和适用性；
3. 患者端不暴露原始证据、DDx、Trace 和内部推理；
4. 有检索、引用一致性、过期证据和冲突证据测试；
5. 主链路不依赖 hash embedding 或 token-overlap reranker。

---

## Phase 12-P1：Controlled Capability Runtime

### 核心目标

```text
让真实 LLM Agent、RAG、Model 和 Tool 使用一套最小但统一的 Runtime 决策与恢复语义。
```

### 核心对象

```text
CapabilityDecision
RuntimeDatum
PostCapabilitySafetyResult
RecoveryAction
```

### 统一决策

```text
ALLOW
DEGRADE
REVIEW_REQUIRED
BLOCK
```

### 建议主链路

```text
Input / FHIR / External Data
→ Provenance & Trust Classification
→ Pre-Capability SafetyGate
→ CapabilityDecision
→ Agent / RAG / Model / Tool
→ Result Validation
→ Post-Capability SafetyGate
→ Recovery / Controlled Commit
→ DecisionBoundary
```

### RuntimeDatum 最小字段

```text
source_type
source_id
source_version
trust_level
instruction_allowed
clinical_authority
verification_status
```

关键原则：

```text
患者输入、RAG 文档、Tool Result 和 Memory 可以作为数据，
但不能自动成为 Runtime 控制指令。
```

### 首个真实 Agent

```text
LLM-backed InquiryPlanningAgent
```

要求：

- 严格结构化输出；
- 仍只生成 InquiryPlanProposal；
- Runtime 负责授权和校验；
- 规则 Agent 保留为 fallback；
- 非法 Proposal 可以部分拒绝或整体降级。

### RecoveryAction

```text
RULE_FALLBACK
RESTRICTED_RETRY
ASK_CLARIFICATION
HUMAN_REVIEW
SAFE_HALT
```

### 禁止

- 完整 Policy DSL；
- 通用 Capability Lease 平台；
- Agent 自由循环；
- Agent 直接修改 RuntimeState；
- Agent 或 Tool 直接生成患者端最终结论；
- 为尚不存在的写操作提前实现复杂事务引擎。

---

## Phase 12-P2：Clinical Vertical Slice

### 核心目标

完成一个真实、可运行、可故障注入、可评测的胸痛/胸闷场景：

```text
患者输入
→ LLM 结构化追问 Proposal
→ Runtime 校验
→ 只读 FHIR 数据查询
→ 真实 Evidence Engine
→ Post-Capability SafetyGate
→ PatientOutput
→ Clinician Evidence Report
→ Governance Trace
→ Evaluation
```

### 只读 FHIR Tool

首批仅支持：

```text
read_conditions
read_observations
read_medication_requests
read_allergies
```

### 三角色输出

Patient：

```text
已收集事实
下一步安全追问
风险提醒
照护导航
安全免责声明
```

Clinician：

```text
Case Snapshot
Missing Facts
Candidate DDx
Supporting Evidence
Contradicting Evidence
Source Version
Applicability
Runtime Decisions
Rejected Proposals
```

Governance：

```text
CapabilityDecision
Provider / Tool 调用
Evidence 使用
Post-Safety 结果
Fallback / Recovery
最终输出边界
```

### 故障注入

- Provider 超时；
- 非法 Agent Proposal；
- 过期证据；
- 冲突证据；
- Tool Result 含指令注入；
- 未授权数据请求；
- 敏感字段泄露；
- fallback 被误标为真实来源。

### Evaluation

结果层：

```text
task_success
clinical_safety_violation
appropriate_escalation
```

Evidence 层：

```text
hit_at_k
citation_entailment
patient_applicability
stale_evidence_activation
```

轨迹层：

```text
question_efficiency
invalid_proposal_rate
recovery_success
state_transition_correctness
```

治理层：

```text
policy_precision
policy_recall
false_block_rate
audit_completeness
```

### 完成标准

1. 主演示链路不依赖 Mock Provider；
2. Mock 只能作为明确标记的 fallback；
3. 每个关键医生端主张都有证据来源和版本；
4. Provider / Tool 故障后可以安全降级；
5. 有可复现的自动化 Evaluation 报告；
6. 三角色视图来自同一真实 Runtime，而非独立演示数据。

---

# 七、Phase 13：患者事实与纵向状态

Phase 13 必须在 Phase 12 形成真实单次临床纵切后再启动。

## Phase 13-P0：Clinical Data / Fact Foundation

### 目标

```text
患者事实 ≠ 医学知识
患者事实 ≠ 系统经验
RuntimeState ≠ 完整纵向病历
Agent 推断 ≠ 临床真相
```

### 最小对象

```text
RawSourceEvent
ClinicalDatum
CurrentClinicalStateProjection
ProvenanceRef
```

### 首批资源

```text
Condition
Observation
MedicationRequest
AllergyIntolerance
```

### 原则

- 原始事件 append-only；
- 患者自述、医生记录、FHIR 资源、设备数据和 Agent 推断分别标记来源；
- Agent 推断不能直接提升为确认事实；
- EvidenceGraph 不承担长期患者事实存储；
- RuntimeState 只保存当前执行状态。

## Phase 13-P1：Temporal Reconciliation

在确有纵向场景需求后增加：

```text
event_time
recorded_time
valid_from / valid_to
supersedes
contradicts
retracted
current_state_rebuild
```

并支持：

- patient-reported 与 clinician-documented 并存；
- 冲突保持 unresolved；
- 高风险冲突进入人工确认；
- 指定时间点状态重建；
- 事实失效但不删除。

### 禁止

- 用统一状态机覆盖所有 FHIR 资源原生状态；
- 旧 Observation 因为出现新值而被删除或标记为错误；
- 患者自述自动修改正式 MedicationRequest；
- Agent 推断覆盖医生确认事实。

---

# 八、Phase 14：事务型动作治理

Phase 14 不是固定时间点任务，而是条件触发阶段。

## 启动条件

```text
系统出现真实外部写操作、不可逆副作用或高风险状态修改。
```

例如：

- 写入病历；
- 提交医生报告；
- 创建转诊或预约；
- 发送外部消息；
- 更新处方或医嘱。

## 核心对象

```text
RuntimeActionProposal
read_set
write_set
external_side_effects
ShadowRuntimeState
Precondition
Postcondition
StagedExecution
Commit
Rollback
CapabilityLease
```

## 核心原则

```text
Tool 调用成功 ≠ 结果允许提交
Agent Proposal 合法 ≠ 外部动作已授权
```

在系统仍只有只读 FHIR Tool 时，不启动完整 Phase 14。

---

# 九、Phase 15：Deep Evidence Synthesis

## 启动条件

单次混合检索和证据校验已经稳定，但复杂问题需要多步证据综合。

## 核心链路

```text
ClinicalQuestionNormalizer
→ RetrievalPlanner
→ Question Decomposition
→ Multi-query Retrieval
→ Source Deduplication
→ Supporting / Contradicting Evidence
→ Conflict Analysis
→ Citation Verification
→ Evidence Synthesis
```

## 原则

- 不以 Multi-Agent 数量作为先进性指标；
- 优先结构化 EvidenceResearchWorkflow；
- 复杂综合结果仍必须经过 Runtime Validation 和 DecisionBoundary；
- 不允许证据综合流程直接输出患者端确定性诊断。

---

# 十、Phase 16：生产级平台化

只有真实能力、真实评测和真实纵切稳定后，才进入生产级平台化。

候选范围：

```text
正式 AuthN / AuthZ
OAuth / SSO
多租户
生产级 RBAC / ABAC
Secret 管理
正式医生审核平台
真实患者数据隔离
资产发布与回滚
模型灰度、漂移和在线回滚
OpenTelemetry
Prometheus / Grafana
Docker / Kubernetes
合规审计
HIS / EHR / LIS / PACS 接入
```

Phase 16 不得建立在主要依赖 Mock Agent、Mock RAG 和 Mock Tool 的基础上。

---

# 十一、触发式后置规则

```text
需要长期患者状态
→ 启动 Phase 13

出现真实外部写操作
→ 启动 Phase 14

单次检索不能支持复杂临床问题
→ 启动 Phase 15

真实能力和评测稳定
→ 启动 Phase 16
```

这些阶段不是必须连续完成的产品待办，而是由真实需求触发的能力升级。

---

# 十二、当前明确不应优先做的事情

在 Phase 12 真实纵切完成前，不优先扩展：

1. Multi-Agent / Handoff；
2. 更多 Governance Console 页面；
3. 真实远程 MCP Server；
4. Skill Store；
5. Browser Agent / Computer Use；
6. LoRA / DPO / RLHF / RFT / distillation；
7. 生产级多租户；
8. 全病种知识库；
9. 完整双时间 Clinical Fact Ledger；
10. 写入型医疗工具；
11. 自动模型、Prompt、数据集、Evidence 或 Candidate 发布。

---

# 十三、当前最优下一步

```text
1. 收口 Phase 11-P1：任务状态、测试、人工验证、冻结记录。
2. 同步 README、项目地图、总设计和 AI_IMPLEMENTATION_SKILL。
3. 将完整系统设计升级到 v3.0，明确控制平面与真实能力平面。
4. 新增 Phase 12-P0 Clinical Evidence Engine 实现规格。
5. 新增 Phase 12-P0 API 与测试设计。
6. 新增 Phase 12-P0 开发任务清单。
7. 只选择胸痛 / 胸闷一个领域启动真实证据纵切。
```

在 Phase 11-P1 未冻结、Phase 12-P0 规格未建立前，不直接开始真实能力编码。

---

# 十四、最终结论

ClinMindRuntime 已经完成：

```text
架构广度验证
+ 治理闭环原型
+ 多角色视图原型
```

下一阶段必须转向：

```text
真实 Agent
+ 真实 Evidence
+ 真实只读 FHIR 数据
+ 统一 Runtime 决策与恢复
+ 可复现量化评测
```

v3.0 路线的核心不是继续增加模块，而是证明：

> 一个真实 LLM Agent、一个真实 Clinical Evidence Engine 和一个真实只读 FHIR Tool，可以在 ClinMindRuntime 的授权、安全重评估、输出边界、审计与恢复机制下完成可验证的临床任务。
