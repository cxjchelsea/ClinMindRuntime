# 医学知识库与 RAG 构建规划

> 上位总设计：`docs/ClinMindRuntime完整系统设计.md`  
> 文档地图：`docs/00_项目设计地图.md`  
> 对应能力域 / 架构层：医学知识与证据域；Provider / Agent / Tool 能力层；Storage / Integration 层；RAG / KG-lite / GraphRAG / EvidenceGraph 证据治理  
> 当前状态：专项设计 / 后置知识与证据系统规划  
> 当前实现：Phase 2 已实现 YAML Asset / EvidenceRef / Asset Provider 方向；Phase 3–5 已完成 Evaluation、Candidate、Persistence、Console 治理主干；尚未实现真实 RAG EvidenceProvider、KG-lite、GraphRAG、pgvector 知识检索、Knowledge Console  
> 对应 Phase：Phase 7-P0 引入 RAG EvidenceProvider MVP；Phase 7-P1 引入 KG-lite / GraphRAG 原型；Phase 8 可接入 embedding / reranker / Python AI Provider；Phase 10 后置生产级知识资产平台  
> 实现入口：医学知识库与 RAG 能力必须先进入 Phase7 实现规格和开发任务清单。RAG / GraphRAG 只能返回 EvidenceCandidate / EvidenceRef，不能直接回答患者，也不能绕过 Runtime Validation、EvidenceGraph、SafetyGate 和 DecisionBoundary。

> 本文档定义 ClinMindRuntime 中医学知识库、RAG Evidence Library、KG-lite、GraphRAG 与 Runtime 的关系。  
> 本项目的知识库不是普通“文档切块 + 向量检索 + 拼 Prompt”的 RAG，而是受 RuntimeState、EvidenceGraph、SafetyGate、DecisionBoundary 和资产版本治理约束的医学知识资产系统。

---

# 一、文档定位

本文档回答：

```text
医学知识库由哪些知识组成？
哪些知识进入规则资产？
哪些知识进入 RAG Evidence Library？
哪些知识进入 KG-lite / GraphRAG？
知识如何入库、切分、标注、版本化？
检索结果如何进入 EvidenceGraph？
RAG 不能绕过哪些 Runtime 边界？
各 Phase 什么时候接入真实知识库？
```

当前阶段不要求立即实现真实 RAG 或知识库平台。当前已经完成的是 Runtime、Asset、Evaluation、Candidate、Persistence、Console 治理主干；真实 RAG / KG-lite / GraphRAG 应进入 Phase 7。

---

# 二、核心原则

```text
1. 知识库是 Runtime 的能力资产，不是 LLM Prompt Context 的附属品。
2. RAG 只能返回 EvidenceRef / EvidenceCandidate，不能直接生成患者端最终回答。
3. KG-lite / GraphRAG 用于增强证据组织，不用于绕过 EvidenceGraph。
4. 所有知识必须有来源、版本、适用范围、审核状态和风险级别。
5. 高风险医疗知识必须 fail-closed，不能在加载失败时静默降级。
6. 患者端输出必须经过 DecisionBoundary，不能直接使用 RAG 生成内容。
7. RAG / GraphRAG 返回结果必须经过 Runtime Validation，再进入 EvidenceGraph。
```

正确链路：

```text
RuntimeState / CaseFrame
→ KnowledgeContextService
→ EvidenceProvider
→ RAG / KG-lite / GraphRAG
→ EvidenceRef / EvidenceCandidate
→ Runtime Validation
→ EvidenceGraph
→ SafetyGate / QuestionTestPolicy / DecisionBoundary
→ PatientOutput / ClinicianReport
```

错误链路：

```text
User Input
→ RAG
→ LLM
→ Patient Answer
```

---

# 三、知识资产分类

ClinMindRuntime 的知识库不能只是一批文档。它至少分为六类知识资产。

## 3.1 结构化规则资产

```text
Red Flag Rules
Test Recommendation Rules
DecisionBoundary Rules
FailurePolicy Rules
```

特点：

```text
确定性强。
安全相关。
适合 YAML / DB 表结构管理。
由 SafetyGate / QuestionTestPolicy / DecisionBoundary 直接使用。
```

## 3.2 临床路径资产

```text
Clinical Pathway
Symptom Rotation Library
Diagnosis Pathway
Question Pathway
Test Pathway
```

用途：

```text
指导某个症状群下应该如何追问、如何排除高危、何时建议检查。
```

## 3.3 RAG Evidence 文档资产

```text
指南
专家共识
药品说明
检查说明
健康教育材料
临床知识手册
```

用途：

```text
为 EvidenceGraph 提供证据引用，为医生端报告提供依据。
```

## 3.4 KG-lite 关系资产

```text
症状 → 疾病
疾病 → 检查
危险信号 → 处置建议
疾病 → 鉴别诊断
证据 → 支持 / 反对某诊断方向
```

实现优先级：

```text
Phase 7-P0：RAG EvidenceProvider MVP。
Phase 7-P1：PostgreSQL node / edge 或轻量 KG-lite。
后置：Neo4j。
```

## 3.5 临床经验资产

```text
ExperienceUnit
MisdiagnosisLesson
SimilarCaseHint
DoctorReviewedExperience
```

用途：

```text
只能增强警觉性、追问优先级和输出边界收紧，不能直接决定最终诊断。
```

## 3.6 评估知识资产

```text
EvaluationCaseSet
ExpectedOutcome
SafetyViolation
RegressionFinding
```

用途：

```text
验证知识库、RAG、KG-lite 和 Runtime 是否真正提升能力。
```

---

# 四、医学知识入库流程

知识入库必须经过以下步骤：

```text
1. Source Collection：收集指南、共识、规则、路径、说明文档。
2. Source Registration：登记来源、版本、发布机构、发布日期、适用范围。
3. Risk Classification：标记风险等级，例如 LOW / MEDIUM / HIGH / CRITICAL。
4. Asset Type Classification：分类为 rule / pathway / evidence_doc / kg_edge / experience / evaluation。
5. Chunking / Structuring：文档切分或结构化。
6. Metadata Binding：绑定元数据。
7. Review：人工或规则审核。
8. Publish：发布到资产包或知识库版本。
9. Runtime Binding：通过 Provider 被 Runtime 读取。
10. Evaluation：通过病例集评估知识资产效果。
11. Rollback：必要时下线或回滚。
```

知识资产入库不是简单把文档放进向量库，而是要进入资产版本、证据引用、审核状态和评估闭环。

---

# 五、文档切分与 Chunk 规范

RAG 文档切分不能只按固定长度切。

推荐切分维度：

```text
章节标题
疾病名称
症状群
适用人群
风险信号
诊断依据
检查建议
治疗原则
患者教育
禁忌 / 注意事项
```

每个 chunk 必须包含元数据：

```text
chunk_id
source_id
source_title
source_type
source_version
publisher
publish_date
medical_domain
symptom_group
diagnosis_tags
risk_level
audience：patient / clinician / internal
language
section_path
effective_from
effective_to
review_status
asset_package_id
asset_package_version
```

示例：

```json
{
  "chunk_id": "evidence-chest-pain-001",
  "source_id": "guideline-acs-2024",
  "source_type": "clinical_guideline",
  "symptom_group": "chest_pain",
  "diagnosis_tags": ["acute_coronary_syndrome"],
  "risk_level": "CRITICAL",
  "audience": "clinician",
  "section_path": "急性胸痛 > 危险信号 > 立即就医",
  "asset_package_version": "0.4.0"
}
```

---

# 六、EvidenceRef 与 EvidenceGraph 的关系

RAG Provider 不返回自然语言答案，而返回 EvidenceRef / EvidenceCandidate。

## 6.1 EvidenceRef

```text
EvidenceRef
- evidence_id
- source_id
- chunk_id
- source_type
- title
- section_path
- symptom_group
- diagnosis_tags
- evidence_strength
- supports_or_refutes
- risk_level
- asset_package_id
- asset_package_version
- retrieved_by
- retrieval_score
```

## 6.2 EvidenceCandidate

```text
EvidenceCandidate
- evidence_ref
- matched_case_frame_fields
- related_ddx_item
- use_case：support / refute / ask_more / recommend_test / safety_warning
- confidence
```

## 6.3 进入 EvidenceGraph

```text
EvidenceCandidate
→ Runtime Validation
→ EvidenceGraphItem
→ support / refute / missing / conflict / must_ask / must_not_miss
```

原则：

```text
EvidenceRef 只是证据候选。
EvidenceGraph 决定证据在本病例中的作用。
DecisionBoundary 决定哪些证据能给患者端看。
```

---

# 七、RAG Provider 输入输出

## 7.1 输入

```text
RuntimeState
CaseFrame
symptom_group
candidate_ddx_list
missing_evidence_fields
asset_context
role_context
capability_profile
```

## 7.2 输出

```text
EvidenceRetrievalResult
- query_trace
- evidence_candidates
- retrieval_strategy
- asset_package_id
- asset_package_version
- provider_version
- warnings
```

## 7.3 禁止输出

```text
患者最终诊断
治疗方案结论
药物使用建议
未经边界控制的自然语言回答
```

---

# 八、KG-lite 与 GraphRAG 边界

KG-lite 负责轻量结构关系：

```text
node：symptom / diagnosis / test / risk_signal / evidence / pathway
edge：supports / refutes / requires_test / red_flag_for / differential_of
```

GraphRAG 负责把结构关系和文档证据结合起来：

```text
CaseFrame
→ related nodes
→ related evidence chunks
→ evidence candidates
→ Runtime Validation
→ EvidenceGraph
```

禁止：

```text
GraphRAG 直接输出诊断结论。
GraphRAG 直接决定患者端表达。
GraphRAG 替代 DifferentialDiagnosisBoard。
GraphRAG 绕过 EvidenceGraph。
```

---

# 九、技术选型与接入阶段

| 能力 | Phase 1–5 | Phase 7-P0 | Phase 7-P1 | Phase 8 / 后置 |
|---|---|---|---|---|
| YAML rules | 已启用 | 保留 | 可迁移 DB | - |
| Evidence refs | 静态引用方向 | 增强 | 平台化 | - |
| RAG Provider | 未接 | MVP | 服务化 | - |
| Embedding | 未接 | 可选实验 | pgvector | ModelProvider / reranker |
| KG-lite | 未接 | 设计 | PostgreSQL node / edge | Neo4j 后置 |
| GraphRAG | 未接 | 不做主线 | 原型 | 后置增强 |
| Knowledge Console | 未接 | 不做 | 轻量查看 | 生产级后置 |

默认路线：

```text
PostgreSQL + pgvector 优先。
Neo4j / Milvus / Qdrant 后置。
```

---

# 十、知识审核、发布、版本和回滚

知识资产状态：

```text
DRAFT
REVIEWING
APPROVED
PUBLISHED
DEPRECATED
DISABLED
ROLLBACKED
```

发布规则：

```text
1. 高风险知识必须人工审核。
2. 发布必须生成 asset_package_version 或 knowledge_package_version。
3. RuntimeTrace 必须记录使用了哪个资产版本。
4. EvaluationResult 必须绑定知识版本。
5. 回滚必须留下 AuditLog。
```

---

# 十一、Evaluation 指标

RAG / KG-lite / GraphRAG 不是接上就算成功，必须评估：

```text
EvidenceRecall@k
EvidencePrecision@k
RelevantGuidelineHitRate
UnsafeEvidenceLeakageRate
PatientBoundaryViolationRate
DdxSupportCoverage
QuestionPolicyImprovement
TraceCompleteness
AssetVersionTraceCompleteness
```

评估链路：

```text
EvaluationCaseSet
→ Runtime with RAG / KG-lite
→ RuntimeTrace / EvidenceGraph
→ EvidenceScorer / PatientBoundaryScorer / TraceCompletenessScorer
→ EvaluationResult
→ CapabilityProfileUpdateProposal
→ Review / Governance
```

---

# 十二、按 Phase 的接入规划

## Phase 1–5

```text
已完成 Runtime、YAML Asset、Evaluation、Candidate、Persistence、Audit、Console 主干。
不接真实 RAG / GraphRAG。
```

## Phase 7-P0

```text
RAG EvidenceProvider MVP。
建立 EvidenceRetrievalResult、EvidenceCandidate、RAG provider contract、RAG evaluation metrics。
```

## Phase 7-P1

```text
KG-lite / GraphRAG 原型。
优先使用 PostgreSQL node / edge 或轻量图证据关系。
GraphRAG 只增强 EvidenceGraph，不主控问诊。
```

## Phase 8

```text
引入 embedding / reranker / Python AI Provider / ModelProvider。
所有模型能力仍只能作为 Provider 返回结构化候选。
```

## Phase 10

```text
生产级知识资产治理。
正式知识审核、发布、回滚、权限、审计和 Knowledge Console。
```

---

# 十三、禁止边界

```text
1. 不在 Phase 6-P0 前提前实现真实 RAG / GraphRAG。
2. 不让 RAG 直接回答患者。
3. 不让 GraphRAG 直接决定诊断。
4. 不把检索结果直接塞给 LLM 生成患者端答案。
5. 不把未审核知识资产作为高风险医疗依据。
6. 不在缺少 Evaluation 指标时声明 RAG 能力上线。
7. 不让知识库或向量库绕过 Runtime 主控。
8. 不让 EvidenceCandidate 未经 Runtime Validation 进入 PatientOutput。
```

---

# 十四、最终结论

ClinMindRuntime 的医学知识库不是普通 RAG 文档库，而是一个受 Runtime 主控、EvidenceGraph、DecisionBoundary、资产版本和 Evaluation / Governance 约束的医学知识资产系统。

核心路线：

```text
Phase 1–5：YAML Asset + Runtime / Evaluation / Candidate / Persistence / Console 治理主干。
Phase 7-P0：RAG EvidenceProvider MVP。
Phase 7-P1：KG-lite / GraphRAG 原型。
Phase 8：Embedding / reranker / Python AI Provider / ModelProvider。
Phase 10：生产级知识资产治理平台。
```

所有知识增强能力都必须服务统一 Runtime 主链路，而不能替代 Runtime、EvidenceGraph 或 DecisionBoundary。
