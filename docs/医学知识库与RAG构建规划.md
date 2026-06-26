# 医学知识库与 RAG 构建规划

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

本文件不要求 Phase 3-P0 立即实现 RAG 或知识库平台。当前阶段仍以 YAML 资产和 Evaluation 闭环为主。

---

# 二、核心原则

```text
1. 知识库是 Runtime 的能力资产，不是 LLM Prompt Context 的附属品。
2. RAG 只能返回 EvidenceRef / EvidenceCandidate，不能直接生成患者端最终回答。
3. KG-lite / GraphRAG 用于增强证据组织，不用于绕过 EvidenceGraph。
4. 所有知识必须有来源、版本、适用范围、审核状态和风险级别。
5. 高风险医疗知识必须 fail-closed，不能在加载失败时静默降级。
6. 患者端输出必须经过 DecisionBoundary，不能直接使用 RAG 生成内容。
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
确定性强
安全相关
适合 YAML / DB 表结构管理
由 SafetyGate / QuestionTestPolicy / DecisionBoundary 直接使用
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
Phase 2/3：YAML ref
Phase 4/5：PostgreSQL node / edge tables
后置：Neo4j
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
→ EvidenceGraph
```

禁止：

```text
GraphRAG 直接输出诊断结论。
GraphRAG 直接决定患者端表达。
GraphRAG 替代 DifferentialDiagnosisBoard。
```

---

# 九、技术选型与接入阶段

| 能力 | Phase 1–3 | Phase 4-P1 | Phase 5 | 后置 |
|---|---|---|---|---|
| YAML rules | 启用 | 保留 | 可迁移 DB | - |
| Evidence refs | 静态引用 | 增强 | 平台化 | - |
| RAG Provider | 不接 | 原型 | 服务化 | - |
| Embedding | 不接 | 实验 | pgvector | Milvus / Qdrant |
| KG-lite | YAML ref | PostgreSQL 原型 | node/edge 管理 | Neo4j |
| GraphRAG | 不接 | 原型 | 可选服务化 | 后置增强 |
| Asset Console | 不接 | 不接 | 接入 | - |

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
2. 发布必须生成 asset_package_version。
3. RuntimeTrace 必须记录使用的知识版本。
4. Evaluation 必须能回放某个知识版本下的表现。
5. 下线知识不得被新 Runtime 使用。
6. 回滚必须可审计。
```

---

# 十一、知识库评估指标

RAG / 知识库评估不能只看相似度。

指标包括：

```text
retrieval_hit_rate
must_not_miss_coverage
evidence_relevance
evidence_strength_correctness
asset_version_trace_completeness
patient_boundary_safety
clinician_evidence_usefulness
false_reassurance_rate
unsafe_evidence_exposure_rate
```

与 Phase 3 Evaluation 的关系：

```text
Phase 3-P0：先检查资产版本与 trace 完整性。
Phase 4/5：再加入 RAG 检索质量、证据命中和证据适用性评分。
```

---

# 十二、各 Phase 接入计划

## Phase 2-P0

```text
建立 Provider 接口。
建立 YAML Asset Package。
保留 RAG evidence refs / KG-lite refs。
不接真实 RAG。
```

## Phase 3-P0

```text
Evaluation 检查资产版本和 evidence trace。
不搭真实知识库。
不接向量数据库。
```

## Phase 4-P1

```text
RAG EvidenceProvider 原型。
Embedding / reranker / query rewrite 实验。
KG-lite / GraphRAG 原型。
```

## Phase 5-P0

```text
PostgreSQL + pgvector。
Evidence Library 管理。
Asset Console。
知识审核、发布、回滚。
RAG Provider 服务化。
```

---

# 十三、最终结论

ClinMindRuntime 的知识库不是普通 RAG 知识库，而是受控医学知识资产系统。

核心路线：

```text
先把知识资产化，
再让 Provider 读取，
再让 EvidenceGraph 组织证据，
再让 SafetyGate / DecisionBoundary 控制输出，
最后再做 RAG / GraphRAG / pgvector / 知识平台化。
```
