# Phase 8-P0 Provider API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase8_P0Python_AIProvider_实现规格.md`  
> 前置专项规划：`docs/2-专项设计/Python_AIProvider接入规划.md`  
> 当前 Phase：Phase 8-P0  
> 当前目标：定义 Java Runtime 与 Python AI Provider 的 API、DTO、错误响应、安全边界和测试方案。

---

# 一、API 设计原则

Phase 8-P0 涉及两类 API：

```text
1. Python Provider API：供 Java Runtime 调用。
2. Java Debug API：供开发、调试、评估查看 Provider 调用。
```

共同原则：

```text
1. Python Provider API 不面向患者端开放。
2. Java Debug API 不面向患者端开放。
3. Python 只返回结构化 ProviderResult。
4. Java 必须做 ProviderValidation。
5. ProviderResult 不直接进入 PatientOutput。
6. 所有调用必须可 Trace / Audit / Evaluation。
7. Python 失败时 Java 必须 fallback。
```

---

# 二、Python Provider API

Python 服务基础路径：

```text
http://localhost:18080
```

P0 端点：

```text
GET  /health
GET  /v1/providers
POST /v1/embeddings
POST /v1/rerank
```

## 2.1 GET /health

响应：

```json
{
  "status": "UP",
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.0-p0",
  "time": "2026-07-02T10:00:00Z"
}
```

## 2.2 GET /v1/providers

响应：

```json
{
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.0-p0",
  "capabilities": [
    {
      "capability": "EMBEDDING",
      "model_id": "mock_embedding_model",
      "model_version": "0.1.0",
      "dimension": 16,
      "enabled": true
    },
    {
      "capability": "RERANK",
      "model_id": "mock_reranker_model",
      "model_version": "0.1.0",
      "enabled": true
    }
  ]
}
```

## 2.3 POST /v1/embeddings

请求：

```json
{
  "request_id": "provider_req_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "python_ai_provider",
  "purpose": "evidence_embedding",
  "items": [
    {
      "item_id": "chunk_chest_pain_001",
      "text": "胸痛伴活动后加重和出汗时，应关注高风险信号。"
    }
  ],
  "schema_version": "0.8.0"
}
```

响应：

```json
{
  "request_id": "provider_req_001",
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.0-p0",
  "model_id": "mock_embedding_model",
  "model_version": "0.1.0",
  "schema_version": "0.8.0",
  "status": "SUCCESS",
  "result": {
    "items": [
      {
        "item_id": "chunk_chest_pain_001",
        "vector": [0.12, 0.03, 0.44, 0.09],
        "dimension": 4,
        "text_hash": "sha256:xxxx",
        "normalized": true
      }
    ]
  },
  "warnings": [],
  "error_code": null,
  "latency_ms": 12,
  "trace": {
    "input_count": 1,
    "output_count": 1
  }
}
```

约束：

```text
1. Python 可以接收 text，但不得在响应中返回 raw text。
2. item_id 必须原样返回。
3. vector 维度必须一致。
4. text_hash 必须存在。
5. status 不为 SUCCESS 时 result 可为空。
```

## 2.4 POST /v1/rerank

请求：

```json
{
  "request_id": "provider_req_002",
  "runtime_id": "runtime_demo_001",
  "provider_id": "python_ai_provider",
  "purpose": "evidence_rerank",
  "query": {
    "query_id": "query_001",
    "text": "胸口闷，活动后更明显，出汗"
  },
  "items": [
    {
      "item_id": "chunk_chest_pain_001",
      "text": "胸痛风险信号识别"
    },
    {
      "item_id": "chunk_fever_001",
      "text": "发热相关安全提醒"
    }
  ],
  "schema_version": "0.8.0"
}
```

响应：

```json
{
  "request_id": "provider_req_002",
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.0-p0",
  "model_id": "mock_reranker_model",
  "model_version": "0.1.0",
  "schema_version": "0.8.0",
  "status": "SUCCESS",
  "result": {
    "query_id": "query_001",
    "ranked_items": [
      {
        "item_id": "chunk_chest_pain_001",
        "rank": 1,
        "score": 0.92,
        "reason_code": "symptom_group_match"
      },
      {
        "item_id": "chunk_fever_001",
        "rank": 2,
        "score": 0.21,
        "reason_code": "low_match"
      }
    ]
  },
  "warnings": [],
  "error_code": null,
  "latency_ms": 15,
  "trace": {
    "input_count": 2,
    "output_count": 2
  }
}
```

约束：

```text
1. ranked_items 只能包含 Java 传入的 item_id。
2. score 必须在 0–1。
3. Python 不能新增证据 chunk。
4. Python 不能输出诊断结论。
5. Python 不能输出患者端回答。
```

---

# 三、Java Debug API

Java Runtime 侧新增 debug API：

```text
POST /api/v1/debug/providers/embeddings/run
POST /api/v1/debug/providers/rerank/run
GET  /api/v1/debug/providers/calls/{provider_call_id}
GET  /api/v1/debug/providers/health
GET  /api/v1/debug/providers/capabilities
```

P0 最小必要：

```text
GET  /api/v1/debug/providers/health
GET  /api/v1/debug/providers/capabilities
POST /api/v1/debug/providers/rerank/run
GET  /api/v1/debug/providers/calls/{provider_call_id}
```

权限：

```text
run：SYSTEM_ADMIN / EVALUATION_REVIEWER
read：SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER
PATIENT 禁止
```

---

# 四、Java DTO 设计

建议 DTO：

```text
ProviderHealthDto
ProviderCapabilityDto
ProviderCallSafeDto
EmbeddingRunRequest
EmbeddingRunResponse
RerankRunRequest
RerankRunResponse
ProviderValidationResultDto
ProviderTraceDto
ProviderFallbackDto
```

所有 Java Debug API 响应必须是 Safe DTO。

禁止返回：

```text
raw patient dialogue
完整敏感输入文本
完整医生端推理链
Python prompt
未脱敏内部文本
```

---

# 五、错误响应设计

## 5.1 Python Provider 不可用

Java 响应：

```json
{
  "provider_call_id": "provider_call_001",
  "status": "MODEL_UNAVAILABLE",
  "validation_status": "DEGRADED",
  "fallback_used": true,
  "error_code": "PYTHON_PROVIDER_UNAVAILABLE",
  "message": "Python provider is unavailable; fallback used."
}
```

## 5.2 Python 超时

```json
{
  "provider_call_id": "provider_call_002",
  "status": "TIMEOUT",
  "fallback_used": true,
  "error_code": "PYTHON_PROVIDER_TIMEOUT"
}
```

## 5.3 Schema 不合法

```json
{
  "provider_call_id": "provider_call_003",
  "status": "VALIDATION_FAILED",
  "validation_status": "REJECTED",
  "fallback_used": true,
  "error_code": "PROVIDER_SCHEMA_INVALID",
  "reasons": ["model_version missing", "rerank score out of range"]
}
```

---

# 六、Provider Validation 测试

## 6.1 ProviderValidationServiceTest

覆盖：

```text
1. 合法 embedding result 通过。
2. 缺 provider_id 拒绝。
3. 缺 model_version 拒绝。
4. embedding dimension 不一致拒绝。
5. vector 包含 NaN 拒绝。
6. rerank item_id 不在原候选集合中拒绝。
7. rerank score 越界拒绝。
8. response request_id 不一致拒绝。
```

## 6.2 ProviderFallbackTest

覆盖：

```text
1. Python provider unavailable 时 fallback。
2. Python timeout 时 fallback。
3. validation rejected 时 fallback。
4. fallback 不阻断 Runtime 主链路。
5. fallback trace 可见。
```

---

# 七、Python 侧测试

Python provider tests：

```text
test_health.py
test_providers.py
test_embeddings.py
test_rerank.py
test_error_response.py
```

覆盖：

```text
1. /health 返回 UP。
2. /v1/providers 返回 enabled capabilities。
3. /v1/embeddings 返回固定维度 vector。
4. /v1/embeddings 不返回 raw text。
5. /v1/rerank 只返回输入 item_id。
6. /v1/rerank score 合法。
7. 异常输入返回结构化 error。
```

---

# 八、Java / Python 契约测试

必须建立 contract 测试，至少覆盖：

```text
1. Java DTO 能被 Python schema 接收。
2. Python response 能被 Java DTO 解析。
3. 缺字段时 Java validation 拒绝。
4. 错误码 / status 枚举一致。
5. schema_version 不匹配时拒绝或降级。
```

P0 可以用手写 JSON fixtures：

```text
src/test/resources/provider/embedding_success.json
src/test/resources/provider/rerank_success.json
src/test/resources/provider/rerank_invalid_item.json
python-provider/tests/fixtures/
```

---

# 九、Evidence Retrieval 集成测试

## 9.1 ProviderEnhancedEvidenceRetrievalTest

覆盖：

```text
1. RAG candidate chunks 生成后调用 reranker。
2. reranker score 改变 candidate ordering。
3. validation 通过后排序生效。
4. reranker 失败时保持原排序。
5. EvidenceCandidate 仍进入 EvidenceValidation。
6. PatientOutput 不泄露 rerank score。
```

## 9.2 EmbeddingProviderSmokeTest

覆盖：

```text
1. Java 可调用 Python embedding endpoint。
2. 返回 dimension 合法。
3. text_hash 存在。
4. trace 记录 provider_call_id。
```

---

# 十、Evaluation Scorer 测试

新增 scorer：

```text
ProviderTraceCompletenessScorer
ProviderSchemaValidationScorer
ProviderFallbackSafetyScorer
```

可选 scorer：

```text
EmbeddingRetrievalGainScorer
RerankOrderingQualityScorer
ProviderBoundaryViolationScorer
```

测试：

```text
1. 缺 provider_id / model_version 得分失败。
2. fallback_used 但没有 trace 得分失败。
3. Python 结果进入 PatientOutput 时 boundary scorer 失败。
4. rerank 后 relevant evidence 排名提升时 ordering scorer 通过。
```

---

# 十一、人工测试场景

## 场景 1：Python Provider 正常

```text
启动 Python provider。
Java 调用 /api/v1/debug/providers/health。
Java 调用 rerank debug API。
```

期望：

```text
Provider status UP。
rerank result SUCCESS。
ProviderValidation ACCEPTED。
Trace / Audit 有记录。
```

## 场景 2：Python Provider 未启动

期望：

```text
Java 返回 provider unavailable。
fallback_used = true。
Runtime 主链路不中断。
```

## 场景 3：非法 rerank item_id

期望：

```text
ProviderValidation REJECTED。
fallback_used = true。
非法 item 不进入 evidence ordering。
```

## 场景 4：PatientOutput 边界

期望：

```text
PatientOutput 不展示 embedding vector。
PatientOutput 不展示 rerank score。
PatientOutput 不展示 model rationale。
```

---

# 十二、回归测试要求

Phase 8-P0 完成前必须通过：

```text
mvn test
python-provider pytest
```

并且不得破坏：

```text
Phase 6-P0 Agent Runtime
Phase 7-P0 EvidenceProvider
Phase 7-P1 Graph Evidence
SafetyGate
DecisionBoundary
EvaluationRunner
Candidate / Review
Persistence / Audit
Console Safe DTO
```

---

# 十三、完成标准

Phase 8-P0 API 与测试完成标准：

```text
1. Python Provider API 可启动并通过 health check。
2. Java 可以调用 Python embeddings / rerank。
3. ProviderValidation 可以拒绝非法结果。
4. Python 不可用时 Java fallback。
5. Debug API 可以查看 provider health / call result。
6. ProviderTrace / Audit 可见。
7. Evaluation 能评估 provider trace / fallback / schema。
8. PatientOutput 不泄露 provider 内部结果。
9. Java 和 Python 测试通过。
10. Phase 1–7 P1 既有回归不破坏。
```

---

# 十四、最终结论

Phase 8-P0 API 与测试的重点不是“Python 模型多强”，而是证明：

```text
Python 可以作为外部 AI Provider 被 Java Runtime 安全调用；
ProviderResult 可被结构化校验；
失败可降级；
结果可追踪、可审计、可评估；
患者端边界不会被破坏。
```
