# Python AI Provider 接入规划

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 对应能力域 / 架构层：模型能力与 Provider 域；医学知识与证据域；评估、审计与持续进化域；Provider / Agent / Tool 能力层  
> 当前状态：Phase 8-P0 前置专项规划  
> 当前前置阶段：Phase 6-P0 受控 Agent 执行层已冻结；Phase 7-P0 RAG EvidenceProvider 已冻结；Phase 7-P1 KG-lite / Graph Evidence 已冻结  
> 后续 Phase：Phase 8-P0 Python AI Provider / EmbeddingProvider MVP

---

# 一、文档定位

本文档用于规划 ClinMindRuntime 中 Java Runtime 与 Python AI Provider 的跨语言协同架构。

它不是 Phase 8-P0 的实现规格，也不是 Python 服务的代码设计文档，而是用于回答：

```text
1. 为什么需要 Python AI Provider。
2. Java 和 Python 的职责边界是什么。
3. Python 可以提供哪些模型能力。
4. Java 如何调用 Python 并保持 Runtime 主控。
5. Provider 返回结果如何被验证、追踪、审计和评估。
6. Phase 8-P0 应该如何切入。
```

本文档的核心结论：

```text
Java 是 Runtime 主控。
Python 是 AI Provider。
Python 不能成为 Runtime、Agent 主控或患者端回答生成器。
```

---

# 二、为什么需要 Python AI Provider

前面阶段已经建立了 Java 侧的受控运行底座：

```text
Phase 6-P0：受控 Agent 执行层 MVP
Phase 7-P0：RAG EvidenceProvider MVP
Phase 7-P1：KG-lite / Graph Evidence 原型
```

这些阶段证明了：

```text
Agent 可以作为受控能力接入 Runtime。
RAG 可以作为 EvidenceProvider 接入 Runtime。
KG-lite 可以作为 Graph Evidence 增强层接入 Runtime。
```

但目前这些能力仍然主要是 Java 内部 deterministic / rule-based MVP：

```text
1. InquiryPlanningAgent 是 rule-based MVP。
2. RagEvidenceProvider 是 keyword / rule-based retrieval。
3. KG-lite Graph Evidence 是 deterministic path expansion。
4. Evaluation scorer 主要是规则判断。
```

后续要提升智能性，需要 Python 承载更适合模型侧的能力：

```text
embedding
reranker
文本分类
医学文本结构化抽取
LLM-as-a-Judge
轻量模型推理
后续 fine-tuning / distillation / preference optimization 实验
```

因此 Python 接入是必要的，但必须以 Provider 方式接入，而不是把主控权交给 Python。

---

# 三、总体架构原则

正确架构：

```text
Java Runtime
→ ProviderClient
→ Python AI Provider
→ Structured ProviderResult
→ Java ProviderValidation
→ Java Runtime 采纳 / 部分采纳 / 拒绝 / 降级
→ RuntimeTrace / AuditLog / Evaluation
```

错误架构：

```text
用户输入
→ Python Agent
→ Python 自己检索 / 推理 / 判断
→ Python 自己生成患者回答
→ Java 只负责转发
```

核心原则：

```text
1. Java Runtime 负责流程、状态、安全、边界、审计和评估。
2. Python Provider 只负责模型能力计算。
3. Python 返回结构化结果，不返回最终患者端回答。
4. Java 必须验证 Python 返回结果。
5. Java 必须决定是否采纳 Python 结果。
6. Python 失败时 Java Runtime 必须可降级。
7. 所有 Python 调用必须进入 Trace / Audit / Evaluation。
```

---

# 四、Java / Python 职责边界

## 4.1 Java Runtime 职责

Java 侧负责：

```text
RuntimeState
CaseFrame
SafetyGate
DecisionBoundary
Capability Orchestration
AgentRuntime
EvidenceRetrievalRuntime
GraphEvidenceRuntime
ProviderClient
ProviderValidation
Trace / Audit
Evaluation
Candidate / Governance
Console API
```

Java 侧拥有最终控制权：

```text
1. 是否调用 Python。
2. 调用哪个 Provider。
3. 传入哪些受控字段。
4. 接收哪些结构化结果。
5. 是否采纳 / 部分采纳 / 拒绝 / 降级。
6. 哪些内容可以进入 PatientOutput / ClinicianReport。
```

## 4.2 Python AI Provider 职责

Python 侧负责：

```text
EmbeddingProvider
RerankerProvider
LlmJudgeProvider
TextClassifierProvider
CaseFrameExtractorProvider
RiskSignalClassifierProvider
后续 ModelExperimentProvider
```

Python 侧只能返回：

```text
EmbeddingResult
RerankResult
JudgeScoreResult
ClassificationResult
ExtractionDraft
RiskSignalScore
ProviderTrace
ProviderWarning
```

Python 侧不能返回：

```text
Final Diagnosis
Final Patient Answer
Final Treatment Advice
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
ApprovedExperience
PublishedTrainingDataset
```

---

# 五、Phase 8-P0 推荐切入点

Phase 8-P0 不应一开始做完整模型平台，而应先做最小闭环：

```text
Python AI Provider / EmbeddingProvider MVP
```

推荐优先级：

```text
1. EmbeddingProvider
2. RerankerProvider
3. LlmJudgeProvider（可选）
4. RiskSignalClassifierProvider（后置）
5. CaseFrameExtractorProvider（后置）
```

最推荐 Phase 8-P0 先做：

```text
EmbeddingProvider + RerankerProvider
```

原因：

```text
Phase 7-P0 / P1 已经完成 EvidenceProvider 和 KG-lite。
当前最需要增强的是 evidence retrieval 的召回与排序质量。
Embedding / Reranker 可以直接增强 RAG EvidenceProvider，且风险低于让 LLM 生成内容。
```

Phase 8-P0 最小目标：

```text
Java Runtime 可以通过 ProviderClient 调用 Python 服务；
Python 返回 embedding / rerank 结构化结果；
Java 对结果做 ProviderValidation；
结果可进入 Evidence retrieval / Evaluation；
调用过程可 Trace / Audit；
Python 失败时 Java 可 fallback 到 keyword / deterministic retrieval。
```

---

# 六、跨语言调用方式

P0 推荐使用 HTTP / FastAPI。

原因：

```text
1. 实现简单。
2. 本地开发和调试方便。
3. 与后续模型服务化兼容。
4. Java Spring Boot 调用成本低。
5. 方便记录 request_id、provider_id、model_version、latency。
```

Phase 8-P0 推荐服务：

```text
python-provider/
  app/
    main.py
    providers/
      embedding_provider.py
      reranker_provider.py
      judge_provider.py
    schemas/
    config/
    tests/
```

推荐接口：

```text
GET  /health
POST /v1/embeddings
POST /v1/rerank
POST /v1/judge        # 可选
GET  /v1/providers
```

Java 调用侧：

```text
PythonProviderClient
EmbeddingProviderClient
RerankerProviderClient
ProviderHealthClient
```

---

# 七、结构化协议设计

## 7.1 通用请求头 / metadata

Java 调用 Python 时必须携带：

```text
request_id
runtime_id
provider_id
provider_version
caller
purpose
input_schema_version
timeout_ms
```

## 7.2 通用响应字段

Python 返回必须包含：

```text
request_id
provider_id
provider_version
model_id
model_version
status
result
warnings
error_code
latency_ms
trace
```

status 候选：

```text
SUCCESS
PARTIAL_SUCCESS
VALIDATION_FAILED
MODEL_UNAVAILABLE
TIMEOUT
FAILED
```

## 7.3 EmbeddingResult

```text
EmbeddingResult
- items
  - item_id
  - embedding
  - dimension
  - text_hash
- model_id
- model_version
- normalized
```

Java 侧不得长期保存原始敏感文本；必要时保存 text_hash / item_id。

## 7.4 RerankResult

```text
RerankResult
- query_id
- ranked_items
  - item_id
  - rank
  - score
  - reason_code
- model_id
- model_version
```

Reranker 不能输出新的医学结论，只能对候选证据排序。

## 7.5 JudgeScoreResult

```text
JudgeScoreResult
- target_id
- score
- dimensions
- rationale_summary
- violations
- model_id
- model_version
```

Judge rationale 只能进入 debug / evaluation，不得直接进入 PatientOutput。

---

# 八、Provider Validation 与降级策略

Java 侧必须新增 ProviderValidation。

校验项：

```text
1. provider_id 是否在 allowlist 中。
2. provider_version 是否匹配。
3. model_id / model_version 是否存在。
4. result schema 是否完整。
5. embedding dimension 是否符合预期。
6. rerank score 是否在合法范围。
7. judge score 是否在合法范围。
8. 返回内容是否包含禁止字段。
9. response latency 是否超过阈值。
10. 是否携带 request_id / runtime_id 对齐信息。
```

降级策略：

```text
EmbeddingProvider 失败 → fallback 到 keyword retrieval。
RerankerProvider 失败 → fallback 到原始 retrieval_score 排序。
JudgeProvider 失败 → fallback 到规则 scorer。
Python 服务不可用 → Java Runtime 不中断主链路。
Python 返回非法 schema → reject provider result。
Python 超时 → timeout + fallback。
```

---

# 九、Trace / Audit / Evaluation 贯通

## 9.1 Trace

每次 Python Provider 调用必须记录：

```text
provider_call_id
runtime_id
provider_id
provider_version
model_id
model_version
input_summary
output_summary
status
latency_ms
fallback_used
```

不得记录：

```text
未脱敏患者原文
完整敏感文本
完整内部诊断推理链
```

## 9.2 Audit

Debug API 或运行时调用应记录：

```text
RUN_PYTHON_PROVIDER
QUERY_PROVIDER_RESULT
PROVIDER_FALLBACK_USED
PROVIDER_VALIDATION_REJECTED
```

## 9.3 Evaluation

Evaluation 应能回答：

```text
1. 使用 embedding 后 evidence recall 是否提升。
2. 使用 reranker 后 evidence ordering 是否更合理。
3. Python Provider 是否增加了越界风险。
4. Provider fallback 是否可追踪。
5. 不同 model_version 的结果是否可比较。
```

候选 Scorer：

```text
ProviderTraceCompletenessScorer
ProviderSchemaValidationScorer
EmbeddingRetrievalGainScorer
RerankOrderingQualityScorer
ProviderFallbackSafetyScorer
ProviderBoundaryViolationScorer
```

---

# 十、版本管理

Python Provider 必须显式管理版本。

至少包含：

```text
provider_id
provider_version
model_id
model_version
schema_version
prompt_version   # 若使用 LLM/Judge
embedding_dimension
```

Java 侧应保存：

```text
ProviderCapabilityProfile
ProviderCallTrace
ProviderValidationResult
ProviderEvaluationResult
```

后续可引入：

```text
ModelRegistry
PromptRegistry
EmbeddingIndexVersion
TrainingDatasetVersion
```

但 Phase 8-P0 不做完整模型注册平台。

---

# 十一、本地开发与部署方式

Phase 8-P0 推荐本地结构：

```text
clinmind-runtime/          # Java Spring Boot Runtime
python-provider/           # Python FastAPI Provider
```

本地启动顺序：

```text
1. 启动 Python Provider。
2. Java 调用 /health 检查 Provider 可用性。
3. Java Runtime 启动。
4. Debug API 触发 provider call。
5. Evaluation 验证 provider 能力。
```

配置项：

```text
PYTHON_PROVIDER_BASE_URL
PYTHON_PROVIDER_TIMEOUT_MS
PYTHON_PROVIDER_ENABLED
EMBEDDING_PROVIDER_ENABLED
RERANKER_PROVIDER_ENABLED
```

Docker 后置，但可以预留：

```text
docker-compose.yml
  java-runtime
  python-provider
  postgres
```

---

# 十二、测试策略

## 12.1 Java 侧测试

```text
ProviderClientTest
ProviderValidationServiceTest
ProviderFallbackTest
EmbeddingProviderIntegrationTest
RerankerProviderIntegrationTest
ProviderTraceAuditTest
EvaluationProviderScorerTest
```

## 12.2 Python 侧测试

```text
test_health.py
test_embeddings.py
test_rerank.py
test_schema_validation.py
test_error_response.py
```

## 12.3 契约测试

必须建立 Java / Python schema contract：

```text
schemas/provider_request.schema.json
schemas/provider_response.schema.json
schemas/embedding_response.schema.json
schemas/rerank_response.schema.json
```

Phase 8-P0 可以先手写 DTO + 测试，不强制完整 OpenAPI 生成。

---

# 十三、当前不做什么

本专项规划明确禁止 Phase 8-P0 直接做：

```text
1. Python Runtime 主控。
2. Python Agent 自主循环。
3. Python 直接输出 PatientOutput。
4. Python 直接判断最终诊断。
5. Python 直接修改 RuntimeState。
6. LLM 直接生成医疗建议给患者。
7. 大规模模型训练平台。
8. LoRA / DPO / RLHF / RFT 正式训练流水线。
9. 自动发布 TrainingDatasetVersion。
10. 完整 ModelRegistry / PromptRegistry 生产平台。
11. 复杂向量数据库平台。
12. 生产级 MLOps。
```

---

# 十四、后续 Phase 演进

## Phase 8-P0

```text
Python AI Provider / EmbeddingProvider MVP
EmbeddingProvider
RerankerProvider
ProviderClient
ProviderValidation
Trace / Audit / Evaluation
Fallback
```

## Phase 8-P1

```text
ModelProvider / JudgeProvider / RiskSignalClassifier
ProviderCapabilityProfile
ProviderEvaluationReport
Model version comparison
```

## Phase 8-P2 或 Phase 10

```text
ModelRegistry
PromptRegistry
TrainingDatasetVersion
Model release / rollback
Knowledge Console / Model Console
```

## 后置研究方向

```text
fine-tuning
RFT / DPO / preference optimization
distillation
medical reranker training
LLM-as-a-Judge calibration
```

---

# 十五、与现有 Phase 的关系

## 与 Phase 6-P0 的关系

Python Provider 不接管 AgentRuntime。

后续可增强：

```text
InquiryPlanningAgent
→ 调用 Java ProviderClient
→ Python Classification / Judge / Rerank
→ Java AgentProposalValidator
```

但 Phase 8-P0 不直接做 Python Agent。

## 与 Phase 7-P0 的关系

Python EmbeddingProvider / RerankerProvider 可增强 Evidence Retrieval：

```text
RagEvidenceProvider
→ candidate evidence chunks
→ Python reranker
→ EvidenceValidation
→ EvidenceGraph
```

## 与 Phase 7-P1 的关系

Python 后续可增强 graph path scoring：

```text
GraphEvidenceProvider
→ graph paths
→ Python reranker / judge
→ GraphEvidenceValidation
→ EvidenceGraph
```

但 Phase 8-P0 不让 Python 生成 GraphRAG answer。

---

# 十六、最终结论

Python 应该从 Phase 8-P0 开始接入，但必须以 AI Provider 形式接入。

最终边界：

```text
Java Runtime 主控，Python AI Provider 供能。
Python 负责模型计算，Java 负责医疗流程、安全边界、采纳决策、审计和评估。
```

Phase 8-P0 的最合理切入点是：

```text
EmbeddingProvider + RerankerProvider
```

它可以增强 Phase 7 的 evidence retrieval / graph evidence，但不会破坏 ClinMindRuntime 的 Runtime-first 架构。
