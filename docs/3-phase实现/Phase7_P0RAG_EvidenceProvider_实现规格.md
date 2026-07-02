# Phase 7-P0 RAG EvidenceProvider MVP 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 专项设计：`docs/2-专项设计/医学知识库与RAG构建规划.md`  
> AI 实现约束：`docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`  
> 当前 Phase：Phase 7-P0  
> 当前目标：建立 RAG EvidenceProvider MVP，让检索能力以 EvidenceCandidate / EvidenceRef 的形式受控进入 Runtime，而不是直接回答患者。

---

# 一、Phase 定位

Phase 7-P0 的目标不是做一个普通“文档切块 + 向量检索 + 拼 Prompt + LLM 回答”的 RAG 问答系统。

Phase 7-P0 的目标是：

```text
在 Runtime 主控下，建立一个最小 RAG EvidenceProvider，
让医学知识检索结果作为 EvidenceCandidate / EvidenceRef 返回，
再经过 Runtime Validation，进入 EvidenceGraph，
最终只影响医生端证据、问诊策略和输出边界，
不能直接回答患者。
```

核心命题：

```text
RAG 提供证据候选，Runtime 决定证据如何使用。
```

---

# 二、前置状态

已完成：

```text
Phase 1-P0：Runtime MVP 已完成。
Phase 2-P0：AssetPackage / Provider / CapabilityProfile 原型已完成。
Phase 3-P0：Evaluation 闭环已冻结。
Phase 4-P0 / P1：Candidate 治理已冻结。
Phase 5-P0 / P1 / P2：Persistence / Audit / Console 治理底座已冻结。
Phase 6-P0：受控 Agent 执行层 MVP 已冻结。
```

Phase 7-P0 可以复用：

```text
RuntimeState
CaseFrame
KnowledgeContext
EvidenceGraph
RuntimeTrace
EvaluationScorer
AuditLog
Capability Orchestration
Runtime Validation
PostgreSQL / Flyway 基础设施
```

---

# 三、当前不做什么

Phase 7-P0 明确不做：

```text
1. 不做 GraphRAG。
2. 不做 KG-lite node / edge 正式图谱。
3. 不接 Neo4j。
4. 不接 Milvus / Qdrant。
5. 不做复杂向量检索平台。
6. 不做 LLM 生成患者回答。
7. 不把 RAG 结果直接拼 Prompt 给患者端。
8. 不让 RAG 直接决定诊断。
9. 不让 RAG 直接修改 RuntimeState。
10. 不让 EvidenceCandidate 绕过 Runtime Validation。
11. 不做知识资产审核平台完整流程。
12. 不做 MCP / Tool / Skills。
13. 不做模型训练或 reranker 训练。
```

P0 可以先使用：

```text
YAML / JSON / in-memory evidence corpus
简单 keyword / semantic-lite matcher
可选 PostgreSQL evidence table，但不强制 pgvector
```

---

# 四、Phase 7-P0 核心链路

目标链路：

```text
RuntimeState / CaseFrame
↓
KnowledgeContextService 构建基础上下文
↓
CapabilityOrchestrationService 判断是否需要 evidence retrieval
↓
EvidenceProviderPolicy 判断是否允许检索
↓
EvidenceRetrievalRequest 构造受控检索请求
↓
RagEvidenceProvider 检索 evidence corpus
↓
EvidenceRetrievalResult 返回 EvidenceCandidate / EvidenceRef
↓
EvidenceValidationService / RuntimeValidationService 校验证据候选
↓
EvidenceGraphService 采纳可用证据
↓
QuestionTestPolicy / ClinicianReport 使用证据摘要
↓
DecisionBoundary 控制患者端可见内容
↓
RuntimeTrace / AuditLog / Evaluation 记录和评估
```

关键边界：

```text
RAG 不产生 PatientOutput。
RAG 不产生 Final Diagnosis。
RAG 不绕过 EvidenceGraph。
RAG 不绕过 DecisionBoundary。
```

---

# 五、核心对象设计

## 5.1 EvidenceRef

`EvidenceRef` 表示一个可追踪的证据引用。

建议字段：

```text
evidence_id
source_id
chunk_id
source_type
title
section_path
symptom_group
diagnosis_tags
evidence_strength
supports_or_refutes
risk_level
asset_package_id
asset_package_version
retrieved_by
retrieval_score
```

P0 要求：

```text
1. evidence_id 必填。
2. source_id / chunk_id 必填。
3. symptom_group 必填。
4. source_type 必填。
5. retrieval_score 必须存在。
6. asset_package_version 或 evidence_corpus_version 必须存在。
```

## 5.2 EvidenceCandidate

`EvidenceCandidate` 表示 EvidenceRef 与当前病例状态的关系候选。

建议字段：

```text
evidence_ref
matched_case_frame_fields
related_ddx_item
use_case：support / refute / ask_more / recommend_test / safety_warning
confidence
reason_summary
```

P0 使用场景：

```text
support：支持某个医生端候选方向。
ask_more：提示还需要追问某个信息。
safety_warning：提示红旗风险或就医建议边界。
```

P0 不建议做：

```text
refute 的复杂判断。
recommend_test 的强推荐。
治疗建议。
```

## 5.3 EvidenceRetrievalRequest

建议字段：

```text
request_id
runtime_id
symptom_group
case_frame_summary
known_facts
missing_facts
candidate_ddx_summary
red_flag_summary
asset_package_id
asset_package_version
retrieval_limit
role_context
```

输入限制：

```text
1. 不传完整 raw patient dialogue。
2. 不传未脱敏患者身份信息。
3. 不传完整医生端推理链。
4. 不允许 request 指定“直接回答患者”。
```

## 5.4 EvidenceRetrievalResult

建议字段：

```text
request_id
runtime_id
provider_id
provider_version
evidence_corpus_version
status
evidence_candidates
query_trace
warnings
error_code
started_at
finished_at
```

状态：

```text
SUCCESS
NO_EVIDENCE_FOUND
POLICY_REJECTED
VALIDATION_REJECTED
DEGRADED
FAILED
```

## 5.5 EvidenceProvider

接口职责：

```text
接收 EvidenceRetrievalRequest。
返回 EvidenceRetrievalResult。
不直接访问 RuntimeService。
不直接修改 RuntimeState。
不直接生成 PatientOutput。
```

建议接口：

```java
public interface EvidenceProvider {
    EvidenceRetrievalResult retrieve(EvidenceRetrievalRequest request);
}
```

## 5.6 RagEvidenceProvider

P0 实现可以是 deterministic / rule-based / keyword-based，不要求 embedding。

职责：

```text
从 evidence corpus 中检索相关证据 chunk。
构造 EvidenceRef。
构造 EvidenceCandidate。
记录 query_trace。
```

## 5.7 EvidenceProviderPolicy

职责：

```text
判断当前 RuntimeState 是否允许 evidence retrieval。
判断 symptom_group 是否支持。
判断 evidence corpus 是否可用。
判断当前风险状态是否允许降级。
```

fail-closed 规则：

```text
高风险知识资产不可用时，不应静默声称已有证据。
```

## 5.8 EvidenceValidationService

职责：

```text
校验 EvidenceCandidate 是否结构完整。
校验证据是否来自可追踪 source。
校验证据版本是否匹配。
校验证据 use_case 是否允许。
校验证据是否试图直接生成患者端回答。
校验证据是否缺失 retrieval_score。
```

Validation 结果：

```text
ACCEPTED
PARTIALLY_ACCEPTED
REJECTED
DEGRADED
```

---

# 六、Evidence Corpus P0 设计

Phase 7-P0 不要求真实医学指南大规模入库。

推荐先做最小 corpus：

```text
src/main/resources/evidence/phase7-default/evidence_chunks.yml
```

或：

```text
src/main/resources/assets/phase7-default/evidence_chunks.yml
```

P0 corpus 至少覆盖：

```text
chest_pain
fever
abdominal_pain
```

每个 symptom_group 至少 3 条 chunk：

```text
1. red flag / safety warning
2. ask more / missing info
3. clinician evidence summary
```

EvidenceChunk 建议字段：

```text
chunk_id
source_id
title
source_type
section_path
symptom_group
diagnosis_tags
risk_level
audience
content_summary
evidence_strength
supports_or_refutes
use_cases
version
```

P0 corpus 只能使用：

```text
合成医学知识样例
公开常识级医学安全建议摘要
项目自造脱敏样例
```

不得声称：

```text
真实临床指南完整覆盖。
真实临床合规知识库。
可用于临床决策。
```

---

# 七、与 EvidenceGraph 的集成

Phase 7-P0 应让 EvidenceGraph 接收 EvidenceCandidate，但仍由 Runtime / EvidenceGraphService 决定是否采纳。

建议新增：

```text
EvidenceGraphEvidenceRefItem
EvidenceCandidateToGraphMapper
EvidenceGraphValidationResult
```

P0 集成方式：

```text
EvidenceRetrievalResult.evidence_candidates
→ EvidenceValidationService
→ EvidenceGraphService.buildEvidenceGraph(state)
→ EvidenceGraph.items 增加 evidence_refs / candidate_evidence_summary
```

注意：

```text
EvidenceGraph 可以引用证据。
PatientOutput 不直接展示 EvidenceGraph 全量内容。
ClinicianReport 可以展示证据摘要与来源。
```

---

# 八、与 Capability Orchestration 的集成

Phase 7-P0 可以扩展 `CapabilityOrchestrationService`，但不得破坏 Phase 6-P0 Agent 能力。

建议设计：

```text
CapabilityOrchestrationService
  → runAgentInquiryPlanningIfNeeded
  → runEvidenceRetrievalIfNeeded
  → merge capability snapshots
```

或拆分：

```text
AgentCapabilityOrchestrator
EvidenceCapabilityOrchestrator
CapabilityOrchestrationService 统一编排
```

P0 推荐更稳方式：

```text
先新增 EvidenceCapabilityOrchestrator，
由 CapabilityOrchestrationService 调用，
并生成 EvidenceRetrievalSnapshot。
```

RuntimeState 可新增：

```text
EvidenceRetrievalSnapshot evidenceRetrieval
```

但注意：

```text
Snapshot 是运行结果摘要，不是医疗主控。
```

---

# 九、与 DecisionBoundary 的关系

DecisionBoundary 必须继续控制患者端输出。

规则：

```text
1. EvidenceCandidate 不直接进入 PatientOutput。
2. PatientOutput 可以使用非常有限的安全表达，例如“存在需要进一步确认的风险信号”。
3. ClinicianReport 可以展示 evidence_ref、source、strength、use_case。
4. 患者端不得展示内部 retrieval_score、诊断支持强度、医生端候选推理链。
```

---

# 十、与 Evaluation 的关系

Phase 7-P0 必须新增 evidence 相关 Scorer。

候选 Scorer：

```text
EvidenceRecallScorer
EvidenceTraceCompletenessScorer
EvidenceSourceVersionScorer
EvidencePatientBoundaryScorer
EvidenceUseCaseSafetyScorer
```

P0 最小 Scorer：

```text
EvidenceTraceCompletenessScorer
EvidencePatientBoundaryScorer
EvidenceSourceVersionScorer
```

EvaluationCase 可通过 metadata 承载：

```text
expected_evidence_use_cases
expected_evidence_symptom_group
expected_source_types
forbidden_patient_evidence_leakage
```

---

# 十一、存储策略

Phase 7-P0 推荐：

```text
P0：YAML / resource evidence corpus + in-memory retrieval trace。
不强制 PostgreSQL evidence table。
不强制 pgvector。
```

如果代码已有成熟 PostgreSQL 基础，也可以后置准备表设计，但不作为 P0 完成条件：

```text
evidence_sources
evidence_chunks
evidence_retrieval_runs
evidence_retrieval_items
```

严禁因为表结构、向量库、embedding 复杂度拖慢 P0 主链路验证。

---

# 十二、完成标准

Phase 7-P0 完成时必须满足：

```text
1. 新增 EvidenceRef / EvidenceCandidate / EvidenceRetrievalRequest / EvidenceRetrievalResult。
2. 新增 EvidenceProvider 接口。
3. 新增 RagEvidenceProvider 的 deterministic / keyword MVP。
4. 新增 EvidenceProviderPolicy。
5. 新增 EvidenceValidationService。
6. EvidenceCandidate 能进入 Runtime Validation 或 evidence validation。
7. EvidenceCandidate 能被 EvidenceGraphService 采纳为医生端证据引用。
8. PatientOutput 不泄露 EvidenceGraph 内部证据评分和医生端推理链。
9. ClinicianReport 可以展示证据摘要和来源引用。
10. RuntimeTrace / AuditLog 记录 evidence retrieval。
11. Evaluation 能评估 evidence trace / source version / patient boundary。
12. `mvn test` 通过。
13. Phase 1–6 P0 既有测试不回归。
```

---

# 十三、Phase 7-P0 后置任务

Phase 7-P0 不完成但可后置：

```text
1. Phase 7-P1：KG-lite node / edge。
2. Phase 7-P1：GraphRAG prototype。
3. Phase 8：EmbeddingProvider / RerankerProvider。
4. Phase 8：Python AI Provider。
5. Phase 10：Knowledge Console。
6. Phase 10：正式知识审核、发布、回滚工作流。
```

---

# 十四、最终结论

Phase 7-P0 的本质是：

```text
让 RAG 从“回答生成器”变成“证据 Provider”，
让证据以 EvidenceCandidate / EvidenceRef 进入 Runtime，
再由 Runtime Validation、EvidenceGraph、DecisionBoundary、Trace、Audit 和 Evaluation 共同约束。
```

它不是 RAG Demo，而是 ClinMindRuntime 医学知识与证据域的最小可运行切片。
