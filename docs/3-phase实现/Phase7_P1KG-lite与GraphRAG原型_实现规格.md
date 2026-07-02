# Phase 7-P1 KG-lite 与 GraphRAG 原型实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 专项设计：`docs/2-专项设计/医学知识库与RAG构建规划.md`  
> 前置阶段：Phase 7-P0 RAG EvidenceProvider MVP 已冻结  
> 当前 Phase：Phase 7-P1  
> 当前目标：建立 KG-lite / GraphRAG 原型，让症状、诊断、检查、风险信号和 EvidenceRef 形成轻量图关系，用于增强 EvidenceGraph，而不是替代 Runtime 主控。

---

# 一、Phase 定位

Phase 7-P1 的目标不是直接引入完整知识图谱平台，也不是做复杂 GraphRAG 产品。

Phase 7-P1 的目标是：

```text
在 Phase 7-P0 EvidenceProvider 的基础上，
把 EvidenceRef 从“证据列表”升级为“轻量图关系中的证据节点”，
让症状、诊断、检查、风险信号、证据之间形成可追踪关系，
并把这些关系作为 GraphEvidenceCandidate 进入 EvidenceGraph。
```

核心命题：

```text
KG-lite / GraphRAG 只能增强证据组织，不能替代 Runtime、DDx Board、SafetyGate 或 DecisionBoundary。
```

---

# 二、为什么 Phase 7-P1 要先做 KG-lite

Phase 7-P0 已经完成：

```text
RagEvidenceProvider
→ EvidenceCandidate / EvidenceRef
→ EvidenceValidationService
→ EvidenceGraph
```

但 P0 的证据仍然偏“列表式”：

```text
某条 evidence chunk 被召回；
它有 source_id / chunk_id / use_case；
它被合并进 EvidenceGraph。
```

问题是：

```text
1. 证据与哪个症状相关，关系还不够结构化。
2. 证据支持哪个候选诊断，仍然比较粗。
3. 证据提示哪些检查或追问，缺少图关系表达。
4. 多条证据之间缺少 path / relation trace。
5. 后续 GraphRAG、医生端解释和 Knowledge Console 都需要图关系基础。
```

因此 Phase 7-P1 先做 KG-lite，而不是直接上 Neo4j / GraphRAG 大平台。

---

# 三、当前不做什么

Phase 7-P1 明确不做：

```text
1. 不引入 Neo4j。
2. 不引入 Milvus / Qdrant。
3. 不引入 pgvector 作为主线依赖。
4. 不做 LLM GraphRAG 问答。
5. 不让 GraphRAG 直接回答患者。
6. 不让图谱直接决定最终诊断。
7. 不让 graph path 直接进入 PatientOutput。
8. 不做大规模真实医学知识图谱。
9. 不做自动知识抽取入图。
10. 不做 Knowledge Console 审核发布平台。
11. 不做模型训练或 reranker 训练。
12. 不做 MCP / Tool / Skills。
```

P1 可以使用：

```text
YAML / JSON KG-lite 资源
in-memory graph store
deterministic graph traversal
EvidenceRef 与 GraphNode / GraphEdge 绑定
GraphEvidenceCandidate
```

---

# 四、Phase 7-P1 核心链路

目标链路：

```text
RuntimeState / CaseFrame
↓
RagEvidenceProvider 返回 EvidenceCandidate / EvidenceRef
↓
KgLiteRepository 加载 GraphNode / GraphEdge
↓
KgLiteMatcher 将 CaseFrame / EvidenceRef 映射到相关节点
↓
GraphEvidenceProvider 执行轻量 path expansion
↓
GraphEvidenceCandidate 返回 symptom-diagnosis-test-evidence 关系
↓
GraphEvidenceValidationService 校验关系、来源和边界
↓
EvidenceGraphService 合并 GraphEvidenceCandidate
↓
ClinicianReport 展示证据关系摘要
↓
DecisionBoundary 防止患者端泄露内部图推理
↓
RuntimeTrace / AuditLog / Evaluation 记录和评估
```

关键边界：

```text
GraphRAG 不生成患者端答案。
GraphRAG 不替代 DifferentialDiagnosisBoard。
GraphRAG 不绕过 EvidenceGraph。
Graph path 不直接展示给患者。
```

---

# 五、KG-lite 数据模型

## 5.1 GraphNode

建议字段：

```text
node_id
node_type
name
normalized_name
symptom_group
risk_level
tags
source_ref
version
```

node_type 候选：

```text
SYMPTOM
DIAGNOSIS
TEST
RISK_SIGNAL
EVIDENCE
QUESTION_SLOT
```

P1 最小节点：

```text
SYMPTOM
DIAGNOSIS
TEST
RISK_SIGNAL
EVIDENCE
```

## 5.2 GraphEdge

建议字段：

```text
edge_id
from_node_id
to_node_id
relation_type
weight
confidence
source_ref
version
```

relation_type 候选：

```text
SUPPORTS
REFUTES
SUGGESTS_TEST
SUGGESTS_QUESTION
RED_FLAG_FOR
DIFFERENTIAL_OF
EVIDENCE_FOR
ASSOCIATED_WITH
```

P1 最小关系：

```text
SYMPTOM --ASSOCIATED_WITH--> DIAGNOSIS
RISK_SIGNAL --RED_FLAG_FOR--> DIAGNOSIS
DIAGNOSIS --SUGGESTS_TEST--> TEST
EVIDENCE --EVIDENCE_FOR--> DIAGNOSIS
EVIDENCE --SUGGESTS_QUESTION--> QUESTION_SLOT
```

## 5.3 GraphPath

建议字段：

```text
path_id
start_node_id
end_node_id
node_ids
edge_ids
path_score
path_reason
max_depth
```

P1 限制：

```text
max_depth <= 2 或 3。
不做无限图遍历。
不做复杂路径搜索。
```

## 5.4 GraphEvidenceCandidate

建议字段：

```text
graph_candidate_id
runtime_id
evidence_ref
matched_nodes
graph_paths
related_ddx_item
suggested_questions
suggested_tests
risk_flags
confidence
reason_summary
```

GraphEvidenceCandidate 只能进入 EvidenceGraph / ClinicianReport，不能进入 PatientOutput。

---

# 六、KG-lite 资源设计

P1 推荐新增：

```text
src/main/resources/evidence/phase7-default/kg_lite_nodes.yml
src/main/resources/evidence/phase7-default/kg_lite_edges.yml
```

或合并为：

```text
src/main/resources/evidence/phase7-default/kg_lite_graph.yml
```

最小覆盖：

```text
chest_pain
fever
abdominal_pain
```

每个 symptom_group 至少包含：

```text
1. symptom nodes
2. risk signal nodes
3. diagnosis nodes
4. test nodes
5. evidence nodes 对应 Phase 7-P0 chunk_id
6. edges linking evidence / symptoms / diagnosis / tests
```

注意：

```text
KG-lite 资源仍然是 synthetic MVP。
不得声称真实医学知识图谱。
不得用于真实临床决策。
```

---

# 七、GraphEvidenceProvider 设计

## 7.1 输入

```text
GraphEvidenceRequest
- request_id
- runtime_id
- symptom_group
- case_frame_summary
- accepted_evidence_candidates
- current_ddx_summary
- retrieval_snapshot
- max_path_depth
- max_path_count
```

## 7.2 输出

```text
GraphEvidenceResult
- graph_retrieval_id
- provider_id
- provider_version
- graph_version
- status
- graph_candidates
- graph_trace
- warnings
```

## 7.3 Provider 职责

```text
1. 将 EvidenceRef 绑定到 KG-lite evidence node。
2. 从 symptom / risk_signal / evidence node 扩展到 diagnosis / test / question nodes。
3. 生成 GraphPath。
4. 生成 GraphEvidenceCandidate。
5. 记录 GraphEvidenceTrace。
```

## 7.4 Provider 禁止事项

```text
1. 不输出 PatientOutput。
2. 不输出 Final Diagnosis。
3. 不修改 RuntimeState。
4. 不绕过 EvidenceGraph。
5. 不执行 LLM summarization。
6. 不从外部网络检索知识。
```

---

# 八、Graph Evidence Validation

必须新增 GraphEvidenceValidationService。

校验项：

```text
1. graph_candidate_id 必填。
2. evidence_ref 必须已通过 Phase 7-P0 EvidenceValidation。
3. graph_paths 不得为空。
4. graph_path depth 不超过 max_depth。
5. node_id / edge_id 必须存在于 KG-lite graph。
6. relation_type 必须属于允许集合。
7. suggested_tests 不得直接变成患者端检查建议。
8. graph_reason_summary 不得包含“确诊”“一定是”等最终判断表达。
9. graph_version 必须存在。
```

Validation 结果：

```text
ACCEPTED
PARTIALLY_ACCEPTED
REJECTED
DEGRADED
```

---

# 九、与 EvidenceGraph 的集成

Phase 7-P1 不是新增一套图谱主控，而是增强现有 EvidenceGraph。

建议扩展 EvidenceGraphItem：

```text
evidence_refs
graph_refs
graph_paths
relation_summaries
```

或新增：

```text
EvidenceGraphRelationEntry
EvidenceGraphPathEntry
```

集成规则：

```text
accepted GraphEvidenceCandidate
→ EvidenceGraphService
→ 对应 DDxCandidate 的 supporting / missing / nextQuestions / recommendedTests 增强
→ ClinicianReport 展示 graph relation summary
→ PatientOutput 不展示 graph path / graph score
```

重要限制：

```text
Graph evidence 可以提示“这个诊断方向有证据关系支持”。
Graph evidence 不能把 DDxCandidate 状态直接改为 confirmed。
```

---

# 十、与 Runtime / Capability Orchestration 的关系

Phase 7-P1 推荐在 Phase 7-P0 evidence retrieval 之后接入：

```text
Agent Orchestration
↓
Evidence Retrieval Orchestration
↓
Graph Evidence Orchestration
↓
DDx Board
↓
EvidenceGraph
```

但 P1 必须保证：

```text
1. SafetyGate fail-safe 时不执行 graph evidence。
2. 没有 accepted EvidenceCandidate 时不执行 graph evidence。
3. KG-lite graph 不可用时 fail-closed / degrade。
4. GraphEvidenceSnapshot 与 EvidenceRetrievalSnapshot 分开保存。
5. AgentOrchestrationSnapshot 不被覆盖。
```

RuntimeState 可新增：

```text
GraphEvidenceSnapshot graphEvidence
```

---

# 十一、与 Evaluation 的关系

Phase 7-P1 应新增 graph 相关 Scorer。

候选 Scorer：

```text
GraphTraceCompletenessScorer
GraphSourceVersionScorer
GraphPathSafetyScorer
GraphEvidenceDdxAlignmentScorer
GraphPatientBoundaryScorer
```

P1 最小 Scorer：

```text
GraphTraceCompletenessScorer
GraphSourceVersionScorer
GraphPatientBoundaryScorer
```

EvaluationCase 可通过 tag 启用：

```text
graph_evidence_eval
```

评估重点：

```text
1. graph path 是否有 node / edge / version。
2. graph path 是否与 accepted EvidenceRef 对齐。
3. graph relation 是否没有泄露到 PatientOutput。
4. graph relation 是否没有直接确诊。
```

---

# 十二、API 边界

Phase 7-P1 只新增 debug / internal API。

候选 API：

```text
POST /api/v1/debug/graph-evidence/run
GET  /api/v1/debug/graph-evidence/runs/{graph_retrieval_id}
GET  /api/v1/debug/graph-evidence/graph
GET  /api/v1/debug/graph-evidence/nodes/{node_id}
GET  /api/v1/debug/graph-evidence/paths?runtime_id=xxx
```

P1 最小 API：

```text
POST /api/v1/debug/graph-evidence/run
GET  /api/v1/debug/graph-evidence/runs/{graph_retrieval_id}
GET  /api/v1/debug/graph-evidence/graph
```

所有 API：

```text
1. 必须走 DebugTokenFilter / ActorContext。
2. run 需要 SYSTEM_ADMIN / EVALUATION_REVIEWER。
3. read 可允许 READ_ONLY_OBSERVER。
4. 不返回 raw patient dialogue。
5. 不返回 patient-facing answer。
6. 不返回完整内部诊断结论。
```

---

# 十三、存储策略

Phase 7-P1 推荐：

```text
P1：YAML / resource KG-lite graph + in-memory graph evidence trace。
不强制 PostgreSQL graph tables。
不强制 Neo4j。
```

后置可考虑：

```text
kg_nodes
kg_edges
graph_evidence_runs
graph_evidence_paths
```

但不作为 P1 完成条件。

---

# 十四、完成标准

Phase 7-P1 完成时必须满足：

```text
1. 新增 KG-lite node / edge domain 对象。
2. 新增 KG-lite graph resource 与加载器。
3. 新增 GraphEvidenceProvider。
4. GraphEvidenceProvider 能基于 EvidenceRef 找到相关 graph paths。
5. 新增 GraphEvidenceValidationService。
6. accepted GraphEvidenceCandidate 能进入 EvidenceGraph。
7. PatientOutput 不泄露 graph path、graph score、内部诊断推理。
8. ClinicianReport 可以展示图关系摘要。
9. Graph evidence 进入 RuntimeTrace / AuditLog。
10. Graph evidence 进入 Evaluation。
11. `mvn test` 通过。
12. Phase 1–7 P0 既有测试不回归。
```

---

# 十五、Phase 7-P1 后置任务

Phase 7-P1 不完成但可后置：

```text
1. Neo4j 图数据库。
2. pgvector / embedding graph retrieval。
3. LLM GraphRAG answer synthesis。
4. 知识自动抽取入图。
5. Knowledge Console。
6. Graph evidence 人工审核发布。
7. Production graph versioning / rollback。
```

---

# 十六、最终结论

Phase 7-P1 的本质是：

```text
把 Phase 7-P0 的 EvidenceRef 从“列表证据”升级为“轻量图关系证据”，
让症状、风险信号、诊断、检查和证据之间形成可验证路径，
并把这些路径作为 GraphEvidenceCandidate 增强 EvidenceGraph。
```

它不是完整 GraphRAG，也不是图数据库平台，而是 ClinMindRuntime 医学知识与证据域的第二个最小切片。
