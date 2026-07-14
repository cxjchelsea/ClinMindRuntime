# Phase 12 真实临床能力纵切总体设计

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md` v3.0  
> 技术蓝图：`docs/1-总设计/ClinMindRuntime技术实现总方案.md` v3.0  
> 阶段路线：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md` v3.0  
> 架构决策：`docs/1-总设计/Phase11后架构缺口与路线收敛决策.md`  
> 前置冻结：Phase 1–11 P1 已冻结  
> 当前状态：Phase 12 设计阶段，尚未进入代码实现

---

# 一、Phase 12 定位

Phase 12 是 ClinMindRuntime 从“完整架构与治理原型”进入“真实临床能力纵切”的第一个阶段。

它不追求一次性建成完整医疗 AI 平台，而是用一个范围受控、可重复评测的临床纵切证明：

```text
真实医学证据
+ 真实模型能力
+ 只读临床数据
+ Runtime 授权、校验、安全与恢复
+ Patient / Clinician / Governance 三角色投影
= 可追踪、可评估、不可绕过 Runtime 的临床能力闭环
```

Phase 12 的核心问题不是“模型回答得是否像医生”，而是：

```text
1. 系统能否从许可明确、版本明确的真实医学资料中检索证据；
2. 每条可采用的临床主张能否追踪到来源、版本和原文位置；
3. LLM、FHIR 与 Evidence 能否作为受控能力进入同一 Runtime；
4. 能力失败、证据冲突或安全风险出现时，系统能否降级、恢复或安全停止；
5. 最终患者端和医生端输出是否继续遵守既有角色边界。
```

---

# 二、阶段拆分

## 2.1 Phase 12-P0：Clinical Evidence Engine MVP

目标：

```text
建立真实、版本化、可追踪、可评测的医学证据引擎，
替换 Phase 7 中以 YAML、关键词、规则、Mock Provider 为主的证据能力。
```

核心能力：

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

BM25 / PostgreSQL Full-text Recall
Dense Embedding Recall
Reciprocal Rank Fusion
Cross-encoder Rerank
Authority / Freshness / Applicability Filter
Citation Entailment
Conflict Detection
EvidenceValidation
RuntimeEvidenceGraph Adapter
```

P0 必须形成：

```text
实现规格
API 与测试设计
开发任务清单
真实许可语料清单与 Source Manifest
离线 Evaluation CaseSet
自动化测试与人工验收记录
冻结记录
```

## 2.2 Phase 12-P1：Controlled Real Capability Slice

进入条件：Phase 12-P0 已冻结。

目标：

```text
将真实 LLM-backed InquiryPlanningAgent、只读 FHIR Tool 和 Clinical Evidence Engine
接入最小统一 Runtime 治理链路。
```

核心能力：

```text
LLM-backed InquiryPlanningAgent
Rule-based InquiryPlanningAgent fallback
Read-only FHIR Tool
Condition / Observation / MedicationRequest / AllergyIntolerance
CapabilityDecision：ALLOW / DEGRADE / REVIEW_REQUIRED / BLOCK
RuntimeDatum 与 Provenance / Trust 分类
Pre-Capability Safety
Post-Capability Safety
RecoveryAction
```

P1 只实现足够支撑纵切的最小统一治理对象，不提前实现 Phase 14 的完整 Policy IR、RuntimeRiskState、CapabilityLease 与因果治理平台。

## 2.3 Phase 12-P2：Chest-pain Clinical Vertical

进入条件：Phase 12-P1 已冻结。

固定首个临床场景：

```text
胸痛 / 胸闷风险分层与受控追问
```

端到端链路：

```text
患者输入
→ Runtime Entry Assessment
→ LLM InquiryPlan Proposal
→ Runtime 校验与部分采纳
→ 只读 FHIR 查询
→ Clinical Evidence Engine
→ Claim / Citation / Applicability / Conflict
→ Post-Capability Safety
→ DecisionBoundary
→ PatientOutput
→ Clinician Evidence Report
→ Governance Trace
→ Layered Evaluation
```

P2 的目标是验证各能力协同，不是构建自动诊断、自动处方或真实医院写入系统。

---

# 三、Phase 12 总体架构

```text
API / Runtime Command
        ↓
RuntimeService
        ↓
EntryAssessment / Existing SafetyGate
        ↓
Capability Orchestration
        ↓
┌──────────────────────────────────────────┐
│ Phase 12-P0: Clinical Evidence Engine    │
│ Phase 12-P1: LLM Agent / Read-only FHIR │
└──────────────────────────────────────────┘
        ↓
Structured Capability Result
        ↓
Runtime Validation
        ↓
Post-Capability Safety / Recovery
        ↓
RuntimeState Commit
        ↓
DecisionBoundary
        ↓
Patient / Clinician / Governance Projection
        ↓
Trace / Audit / Evaluation
```

控制权边界：

```text
Java Runtime
负责调用顺序、能力授权、超时、结果校验、部分采纳、状态提交、输出边界和审计。

Python Provider
只负责 Embedding、Rerank、Citation Entailment、LLM Structured Output 等无状态模型推理。

PostgreSQL
负责证据资产、版本、Claim、Citation、检索 Trace 与词法索引等持久化。

Agent / Evidence / FHIR Tool
只能返回结构化结果，不得直接修改 RuntimeState 或 PatientOutput。
```

---

# 四、数据边界

Phase 12 必须继续区分：

```text
Medical Evidence
= 指南、共识、系统综述、权威医学资料中的来源化证据。

Patient Clinical Data
= 当前请求或只读 FHIR 返回的患者相关数据。

Clinical Fact
= 经过长期事实治理、时间与冲突处理后的正式患者事实。

Agent Inference
= Agent 生成的 Proposal / Draft / Candidate。

Runtime Evidence Graph
= 某次 Runtime 实际检索并经验证后采用的证据快照。
```

Phase 12-P1 可以读取 FHIR 并形成当前 Runtime 使用的数据，但不得把读取结果自动写成长期 ClinicalFact。

ClinicalFactLedger、双时间状态、事实替代与冲突治理属于 Phase 13。

---

# 五、Evidence 与患者状态的隔离

禁止：

```text
医学指南文本 → 直接成为患者事实
FHIR 原始资源 → 直接覆盖 RuntimeState
Embedding 相似度 → 直接决定临床结论
LLM 生成内容 → 直接进入 EvidenceGraph
EvidenceGraph → 直接输出患者诊断
```

允许：

```text
Evidence Source
→ Versioned Asset
→ Chunk / Span / Source-backed Claim
→ Retrieval / Rerank
→ Citation / Applicability / Conflict Validation
→ EvidenceCandidate
→ RuntimeEvidenceGraph
→ Runtime 决策与角色输出边界
```

---

# 六、技术责任划分

## 6.1 Java 侧

```text
evidence source / asset governance
ingestion orchestration
retrieval planning
lexical retrieval port
hybrid fusion
source authority / freshness / applicability policy
evidence validation
claim-citation link validation
conflict set construction
RuntimeEvidenceGraph mapping
API / persistence / trace / evaluation
```

## 6.2 Python Provider 侧

```text
embedding generation
cross-encoder rerank
citation entailment / NLI
Phase 12-P1 LLM structured inquiry planning
model metadata / version / latency / warnings
```

Python 不保存权威证据资产，不决定资产是否发布，不拥有 Runtime 提交权。

## 6.3 PostgreSQL 侧

```text
source registry
asset versions
chunks / spans / claims
claim-evidence links
citation verification results
retrieval traces
lexical index
optional pgvector dense index
```

P0 默认不引入 Neo4j、Milvus、Qdrant、Elasticsearch 或独立搜索集群。

---

# 七、Phase 12-P0 与既有能力的关系

## 7.1 复用

复用以下冻结能力：

```text
Phase 3 Evaluation Framework
Phase 5 PostgreSQL / Repository / Audit
Phase 7 EvidenceCandidate / EvidenceValidation / EvidenceGraph contract
Phase 8 Python Provider / Provider Validation / Model Metadata
Phase 10 Governance Console Safe DTO
Phase 11 Patient / Clinician Runtime-backed Projection
```

## 7.2 演进而非改写

```text
Phase 7 RagEvidenceProvider
保留为 deterministic baseline / fallback，不删除历史实现。

Phase 7 EvidenceGraph
通过 adapter 接收新的 EvidenceRetrievalResult，不由新引擎绕过 Runtime 直接写入。

Phase 8 Embedding / Rerank Provider
在既有 Provider 协议上增加真实模型实现和版本信息，不改成 Python 主控。
```

## 7.3 Feature Flag

建议：

```text
clinmind.evidence.engine.mode=legacy | shadow | active
```

定义：

```text
legacy：继续使用 Phase 7 baseline。
shadow：真实引擎执行并记录评测，但不影响 Runtime 正式采用结果。
active：真实引擎结果进入 Runtime Validation；仍不得绕过 DecisionBoundary。
```

P0 开发期默认 `shadow`，冻结前才允许在 debug/demo 场景切换为 `active`。

---

# 八、跨阶段依赖

```text
Phase 12-P0 Evidence Engine
        ↓
Phase 12-P1 LLM Agent + FHIR + Minimal Governance
        ↓
Phase 12-P2 Chest-pain Vertical
        ↓
Phase 13 Clinical Data & Fact Plane
        ↓
Phase 14 Full Governance Kernel
        ↓
Phase 15 Transactional Action Governance
```

不得倒置以下依赖：

```text
不先做 Phase 13 Fact Plane 再回头补 Evidence；
不先做 Phase 15 写操作再补 CapabilityDecision；
不先做 Multi-Agent 再补单能力可评测性；
不先做 GraphRAG 平台再证明基础混合检索有效。
```

---

# 九、Phase 12 总体 Evaluation

## 9.1 结果层

```text
task_success
safe_task_completion
clinical_safety_violation
appropriate_escalation
false_reassurance
```

## 9.2 证据层

```text
Recall@k / Hit@k
MRR / nDCG
context_precision
citation_entailment
source_authority_error
stale_evidence_activation
applicability_error
conflict_handling
provenance_completeness
```

## 9.3 轨迹层

```text
question_efficiency
invalid_proposal_rate
tool_selection_accuracy
argument_validity
recovery_success
state_transition_correctness
```

## 9.4 治理层

```text
policy_precision / recall
false_block_rate
capability_decision_latency
audit_completeness
role_leakage_rate
```

---

# 十、永久边界

Phase 12 不允许：

```text
1. LLM 直接生成最终诊断并写入 PatientOutput。
2. RAG 直接面向患者回答。
3. 未验证引用进入 accepted EvidenceGraph。
4. 使用单一相似度作为证据质量总分。
5. 将网页或文档中的指令当作系统指令执行。
6. 未确认许可证的全文进入正式发布资产。
7. FHIR 读取结果自动写入正式患者事实。
8. 真实病历写入、预约、转诊、处方或其他外部副作用。
9. 自动训练、自动发布模型或自动扩大 Capability 权限。
10. Multi-Agent 自主协商医疗结论。
11. GraphRAG 取代 Runtime 临床决策。
12. 患者端暴露 DDx、Raw Evidence、Prompt、Trace 或完整推理链。
```

---

# 十一、Phase 12 文档体系

```text
Phase12真实临床能力纵切_总体设计.md
    ├── Phase12_P0ClinicalEvidenceEngine_实现规格.md
    ├── Phase12_P0EvidenceEngine_API与测试设计.md
    └── Phase12_P0开发任务清单.md
```

Phase 12-P1 和 P2 只有在前置阶段冻结后，才能分别建立独立实现规格、API/测试设计和任务清单。

---

# 十二、进入 Phase 12-P0 编码的条件

必须全部满足：

```text
1. 本总体设计完成评审；
2. P0 实现规格完成评审；
3. P0 API 与测试设计完成评审；
4. P0 开发任务清单完成评审；
5. Source Manifest 与许可证状态字段确定；
6. 初始胸痛证据语料范围确定；
7. Offline Evaluation CaseSet 结构确定；
8. Python Provider 与 Java Runtime 的责任边界冻结；
9. Feature Flag、降级和失败关闭策略冻结；
10. 未并行启动 Phase 12-P1、P2 或 Phase 13–22。
```

在上述条件完成前，只允许继续完善设计文档和调研语料，不进入正式编码。