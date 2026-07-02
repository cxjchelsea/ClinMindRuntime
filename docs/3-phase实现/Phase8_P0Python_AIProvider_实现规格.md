# Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 前置专项规划：`docs/2-专项设计/Python_AIProvider接入规划.md`  
> 前置阶段：Phase 6-P0 / Phase 7-P0 / Phase 7-P1 已冻结  
> 当前 Phase：Phase 8-P0  
> 当前目标：建立 Java Runtime 调用 Python AI Provider 的最小闭环，优先接入 EmbeddingProvider / RerankerProvider，不让 Python 成为 Runtime 主控。

---

# 一、Phase 定位

Phase 8-P0 的目标不是做完整模型平台，也不是把 ClinMindRuntime 改成 Python Agent 系统。

Phase 8-P0 的目标是：

```text
建立 Java Runtime → Python AI Provider → Structured ProviderResult → Java ProviderValidation 的最小闭环，
让 Python 提供 embedding / rerank 等模型能力，
增强 Phase 7 的 evidence retrieval 和 evidence ordering，
并保持 Runtime Validation、Trace、Audit、Evaluation 的主控边界。
```

核心命题：

```text
Java Runtime 主控，Python AI Provider 供能。
```

---

# 二、前置状态

已完成：

```text
Phase 6-P0：受控 Agent 执行层 MVP 已冻结。
Phase 7-P0：RAG EvidenceProvider MVP 已冻结。
Phase 7-P1：KG-lite / Graph Evidence 原型已冻结。
Python_AIProvider接入规划 已建立。
```

当前系统已经具备：

```text
RuntimeState
Capability Orchestration
AgentRuntime
EvidenceRetrievalRuntime
GraphEvidenceRuntime
RuntimeTrace
AuditLog
Evaluation
DecisionBoundary
```

Phase 8-P0 应复用这些治理能力，而不是新建 Python 主控链路。

---

# 三、当前不做什么

Phase 8-P0 明确不做：

```text
1. 不做 Python Runtime 主控。
2. 不做 Python Agent 自主循环。
3. 不让 Python 直接输出 PatientOutput。
4. 不让 Python 直接判断最终诊断。
5. 不让 Python 直接修改 RuntimeState。
6. 不做 LLM 直接回答患者。
7. 不做大规模模型训练平台。
8. 不做 LoRA / DPO / RLHF / RFT 正式训练流水线。
9. 不做完整 ModelRegistry / PromptRegistry / MLOps。
10. 不做复杂向量数据库平台。
11. 不引入外部云模型强依赖作为 P0 主线。
12. 不让 ProviderResult 绕过 Java ProviderValidation。
```

P0 可以做：

```text
Python FastAPI Provider
/health
/v1/embeddings
/v1/rerank
/v1/providers
Java ProviderClient
ProviderValidationService
ProviderTrace / Audit
Evidence retrieval enhancement
Evaluation scorer
fallback to deterministic retrieval
```

---

# 四、Phase 8-P0 核心链路

目标链路：

```text
RuntimeState / EvidenceRetrievalRequest
↓
Java ProviderInvocationPolicy
↓
Java PythonProviderClient
↓
Python FastAPI AI Provider
↓
EmbeddingProvider / RerankerProvider
↓
ProviderResult（结构化）
↓
Java ProviderValidationService
↓
Java 采纳 / 部分采纳 / 拒绝 / 降级
↓
EvidenceRetrieval / RAG ordering / Evaluation
↓
RuntimeTrace / AuditLog
```

关键边界：

```text
Python 只返回结构化 ProviderResult。
Java 负责是否采纳。
Java 负责 PatientOutput / ClinicianReport 边界。
Python 失败时 Java Runtime 必须 fallback。
```

---

# 五、Python Provider 服务设计

## 5.1 目录建议

```text
python-provider/
  pyproject.toml
  README.md
  app/
    main.py
    config.py
    providers/
      embedding_provider.py
      reranker_provider.py
    schemas/
      common.py
      embedding.py
      rerank.py
    tests/
      test_health.py
      test_embeddings.py
      test_rerank.py
```

## 5.2 FastAPI 端点

P0 最小端点：

```text
GET  /health
GET  /v1/providers
POST /v1/embeddings
POST /v1/rerank
```

后置端点：

```text
POST /v1/judge
POST /v1/classify-risk
POST /v1/extract-case-frame
```

## 5.3 Python Provider 允许能力

P0 允许：

```text
EmbeddingProvider
RerankerProvider
```

P0 可选：

```text
MockJudgeProvider
```

P0 不做真实大模型生成。

---

# 六、Java 侧 Provider 模块设计

建议新增包：

```text
src/main/java/com/clinmind/runtime/provider/
src/main/java/com/clinmind/runtime/provider/python/
src/main/java/com/clinmind/runtime/provider/embedding/
src/main/java/com/clinmind/runtime/provider/rerank/
src/main/java/com/clinmind/runtime/provider/validation/
```

核心对象：

```text
ProviderInvocationRequest
ProviderInvocationResult
ProviderStatus
ProviderTrace
ProviderValidationResult
PythonProviderClient
ProviderInvocationPolicy
ProviderValidationService
EmbeddingRequest
EmbeddingResult
RerankRequest
RerankResult
ProviderCapabilityDescriptor
```

Java 侧职责：

```text
1. 构造受控 Provider request。
2. 调用 Python Provider。
3. 处理 timeout / failed / unavailable。
4. 校验 ProviderResult。
5. 将结果用于 evidence retrieval / rerank。
6. 记录 ProviderTrace / Audit。
7. Evaluation 归因。
```

---

# 七、结构化结果设计

## 7.1 Provider 通用响应

Python Provider 必须返回：

```text
request_id
provider_id
provider_version
model_id
model_version
schema_version
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

## 7.2 EmbeddingResult

```text
EmbeddingResult
- items
  - item_id
  - vector
  - dimension
  - text_hash
- model_id
- model_version
- normalized
```

要求：

```text
1. vector 维度必须一致。
2. dimension 必须与 provider metadata 一致。
3. 不返回原始敏感文本。
4. text_hash 用于追踪，不保存 raw text。
```

## 7.3 RerankResult

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

要求：

```text
1. score 在 0–1 或约定范围内。
2. item_id 必须来自 Java 传入候选集合。
3. 不能新增候选证据。
4. 不能输出新的诊断结论。
5. 不能输出患者端回答。
```

---

# 八、Provider Validation

必须新增 Java 侧 ProviderValidationService。

校验项：

```text
1. provider_id 是否在 allowlist。
2. provider_version 是否匹配。
3. model_id / model_version 是否存在。
4. schema_version 是否匹配。
5. request_id 是否对齐。
6. embedding dimension 是否正确。
7. vector 是否为空 / NaN / 维度不一致。
8. rerank item_id 是否来自原候选集合。
9. rerank score 是否范围合法。
10. 返回内容是否包含禁止字段。
11. latency 是否超阈值。
```

Validation 结果：

```text
ACCEPTED
PARTIALLY_ACCEPTED
REJECTED
DEGRADED
```

---

# 九、与 Evidence Retrieval 的集成

Phase 8-P0 主要增强 Phase 7-P0 evidence retrieval。

推荐方式：

```text
RagEvidenceProvider 先生成 candidate chunks
↓
Java 构造 RerankRequest
↓
Python RerankerProvider 返回 ranked_items
↓
Java ProviderValidation
↓
按 rerank score 更新 evidence candidate ordering
↓
EvidenceValidationService
↓
EvidenceGraph
```

EmbeddingProvider 可先用于：

```text
1. 为 evidence chunks 生成 embedding。
2. 为 query / case summary 生成 embedding。
3. 计算 cosine similarity。
4. 作为 keyword retrieval 的辅助分数。
```

P0 不要求持久化 embedding index。

可选 P0 策略：

```text
in-memory embedding cache
resource corpus embedding on startup
no pgvector
no external vector database
```

---

# 十、降级策略

必须支持：

```text
Python Provider 不可用 → fallback 到 keyword retrieval。
EmbeddingProvider 超时 → fallback 到原 retrieval_score。
RerankerProvider 超时 → fallback 到原排序。
ProviderValidation 失败 → reject provider result + fallback。
Python 返回异常 schema → reject + fallback。
模型结果为空 → fallback。
```

Runtime 不应因为 Python 失败而中断。

---

# 十一、Trace / Audit / Evaluation

## 11.1 Trace

每次调用记录：

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
validation_status
```

不得记录：

```text
完整患者原文
敏感身份信息
完整医生端推理链
```

## 11.2 Audit

新增候选 action：

```text
RUN_PYTHON_PROVIDER
QUERY_PROVIDER_RESULT
PROVIDER_VALIDATION_REJECTED
PROVIDER_FALLBACK_USED
```

## 11.3 Evaluation

新增 Provider 相关 scorer：

```text
ProviderTraceCompletenessScorer
ProviderSchemaValidationScorer
EmbeddingRetrievalGainScorer
RerankOrderingQualityScorer
ProviderFallbackSafetyScorer
ProviderBoundaryViolationScorer
```

P0 最小：

```text
ProviderTraceCompletenessScorer
ProviderSchemaValidationScorer
ProviderFallbackSafetyScorer
```

---

# 十二、配置设计

Java 配置：

```text
python.provider.enabled
python.provider.base-url
python.provider.timeout-ms
python.provider.embedding.enabled
python.provider.rerank.enabled
python.provider.fail-open=false
```

Python 配置：

```text
PROVIDER_ID
PROVIDER_VERSION
EMBEDDING_MODEL_ID
EMBEDDING_MODEL_VERSION
EMBEDDING_DIMENSION
RERANKER_MODEL_ID
RERANKER_MODEL_VERSION
```

P0 默认：

```text
Java 能在 python.provider.enabled=false 时完整运行。
```

---

# 十三、完成标准

Phase 8-P0 完成时必须满足：

```text
1. 新增 python-provider FastAPI 服务。
2. Python 提供 /health、/v1/providers、/v1/embeddings、/v1/rerank。
3. Java 新增 PythonProviderClient。
4. Java 新增 ProviderValidationService。
5. Java 可以调用 Python EmbeddingProvider。
6. Java 可以调用 Python RerankerProvider。
7. Provider 结果可增强 evidence retrieval ordering。
8. Python 失败时 Java fallback。
9. Provider 调用进入 Trace / Audit。
10. Provider 能力进入 Evaluation。
11. Python 不直接输出 PatientOutput。
12. Python 不修改 RuntimeState。
13. 后端 `mvn test` 通过。
14. Python provider tests 通过。
15. Phase 1–7 P1 既有测试不回归。
```

---

# 十四、后置任务

Phase 8-P0 不完成但可后置：

```text
1. JudgeProvider。
2. RiskSignalClassifierProvider。
3. CaseFrameExtractorProvider。
4. ModelRegistry。
5. PromptRegistry。
6. TrainingDatasetVersion。
7. LoRA / DPO / RFT / distillation。
8. pgvector / Milvus / Qdrant。
9. Model Console。
10. Production model deployment / rollback。
```

---

# 十五、最终结论

Phase 8-P0 的本质是：

```text
把模型能力从 Java Runtime 中解耦为 Python AI Provider，
但不把医疗主控权交给 Python。
```

它完成后，ClinMindRuntime 的能力链路将从：

```text
Runtime + Agent + Evidence + KG-lite
```

升级为：

```text
Runtime + Agent + Evidence + KG-lite + Python AI Provider
```

但主控仍然是 Java Runtime。
