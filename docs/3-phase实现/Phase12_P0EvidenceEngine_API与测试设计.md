# Phase 12-P0 Evidence Engine API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase12_P0ClinicalEvidenceEngine_实现规格.md`  
> Phase 总体设计：`docs/3-phase实现/Phase12真实临床能力纵切_总体设计.md`  
> 前置冻结：Phase 11-P1  
> 当前阶段：Phase 12-P0 设计阶段  
> 当前目标：固定 Clinical Evidence Engine 的 Java API、Debug API、Python Provider Contract、持久化验证、Evaluation 数据集和冻结阈值。

---

# 一、API 设计原则

```text
1. Runtime 内部优先使用 Java Service / Port，不通过 HTTP 回调自身。
2. 外部 HTTP API 在 P0 仅用于 debug、治理、验收和 Provider 调用。
3. Patient / Clinician 前端不得直接调用 Evidence Debug API。
4. Debug API 不直接生成患者回答或诊断。
5. 所有请求和响应携带 schema_version、trace_ref 或 retrieval_id。
6. 所有写入型 Debug API 必须受现有 DebugRole / token 保护并记录 Audit。
7. Python Provider 必须无状态，返回模型版本和结构化结果。
8. Provider 超时、无响应和 Schema 错误必须显式进入降级或失败关闭。
9. 错误响应不得包含 Stack Trace、Prompt、Secret、完整原始文档或未脱敏患者信息。
```

---

# 二、Java 内部 Service API

## 2.1 ClinicalEvidenceQueryService

```java
public interface ClinicalEvidenceQueryService {
    EvidenceRetrievalResult retrieve(EvidenceQueryRequest request);
}
```

职责：

```text
validate request
normalize question
build retrieval plan
load eligible scope
run lexical / dense retrieval
fuse / deduplicate / rerank
apply authority / freshness / applicability
assemble source-backed claims
verify citations
detect conflicts
validate result
write retrieval trace
audit invocation
```

禁止：

```text
direct RuntimeState mutation
direct PatientOutput generation
asset publication
model release
```

## 2.2 EvidenceIngestionService

```java
public interface EvidenceIngestionService {
    EvidenceIngestionResult ingest(EvidenceIngestionCommand command);
    EvidenceIndexBuildResult buildIndex(EvidenceIndexBuildCommand command);
}
```

P0 只支持受控本地文件或预置资源引用，不提供任意 URL 自动抓取。

## 2.3 EvidenceAssetGovernanceService

```java
public interface EvidenceAssetGovernanceService {
    SourceRegistryEntry registerSource(RegisterEvidenceSourceCommand command);
    EvidenceAssetVersion createAssetVersion(CreateEvidenceAssetVersionCommand command);
    AssetReviewResult reviewAsset(ReviewEvidenceAssetCommand command);
    AssetPublicationResult publishAsset(PublishEvidenceAssetCommand command);
    AssetLifecycleResult deprecateAsset(DeprecateEvidenceAssetCommand command);
    AssetLifecycleResult revokeAsset(RevokeEvidenceAssetCommand command);
}
```

发布前置条件：

```text
source license VERIFIED
source review APPROVED
asset checksum valid
ingestion complete
lexical index complete
dense index complete or explicitly lexical-only evaluation mode
offline evaluation passed
```

## 2.4 ClinicalEvidenceValidationService

```java
public interface ClinicalEvidenceValidationService {
    EvidenceValidationResult validate(
        EvidenceRetrievalResult result,
        EvidenceValidationContext context
    );
}
```

## 2.5 RuntimeEvidenceGraphAdapter

```java
public interface RuntimeEvidenceGraphAdapter {
    RuntimeEvidenceGraphPatch toPatch(
        EvidenceRetrievalResult result,
        EvidenceValidationResult validation
    );
}
```

---

# 三、Debug / Governance HTTP API

Base Path：

```text
/api/v1/debug/evidence
```

## 3.1 Source Registry

```text
POST /api/v1/debug/evidence/sources
GET  /api/v1/debug/evidence/sources
GET  /api/v1/debug/evidence/sources/{sourceId}
```

### POST Source Request

```json
{
  "display_name": "Example Clinical Guideline Publisher",
  "publisher": "Example Publisher",
  "source_type": "GUIDELINE",
  "authority_level": "A",
  "jurisdiction": "GLOBAL",
  "language": "en",
  "homepage": "https://example.invalid",
  "license_status": "VERIFIED",
  "license_name": "verified-by-project-review",
  "license_reference": "internal-review-ref-001",
  "trust_status": "TRUSTED",
  "notes": "P0 curated source"
}
```

### Source Response

```json
{
  "schema_version": "phase12-p0.1",
  "source_id": "src_xxx",
  "review_status": "DRAFT",
  "audit_ref": "audit_xxx"
}
```

规则：

```text
API 不允许客户端直接提交 review_status=APPROVED。
license_status=VERIFIED 仍需要 reviewer 和 review record。
```

## 3.2 Asset Version

```text
POST /api/v1/debug/evidence/assets
GET  /api/v1/debug/evidence/assets
GET  /api/v1/debug/evidence/assets/{versionId}
POST /api/v1/debug/evidence/assets/{versionId}/ingest
POST /api/v1/debug/evidence/assets/{versionId}/review
POST /api/v1/debug/evidence/assets/{versionId}/publish
POST /api/v1/debug/evidence/assets/{versionId}/deprecate
POST /api/v1/debug/evidence/assets/{versionId}/revoke
```

### Create Asset Request

```json
{
  "source_id": "src_xxx",
  "asset_id": "asset_chest_pain_guide",
  "title": "Chest Pain Guidance",
  "document_type": "GUIDELINE",
  "external_reference": "source-reference",
  "specialty": "emergency_medicine",
  "intended_audience": "clinician",
  "jurisdiction": "GLOBAL",
  "language": "en",
  "publication_date": "2026-01-01",
  "effective_from": "2026-01-01T00:00:00Z",
  "effective_to": null,
  "supersedes_version_id": null,
  "content_reference": "classpath:evidence/phase12/source.md",
  "expected_checksum": "sha256:..."
}
```

`content_reference` P0 allowlist：

```text
classpath:
file:（仅项目配置允许的 evidence import directory）
```

禁止：

```text
任意 http / https URL
任意绝对路径
路径穿越
脚本或可执行文件
```

## 3.3 Ingestion Result

```json
{
  "schema_version": "phase12-p0.1",
  "ingestion_id": "ing_xxx",
  "version_id": "ver_xxx",
  "status": "COMPLETED",
  "content_checksum": "sha256:...",
  "parser_version": "markdown-parser-1",
  "chunk_count": 42,
  "span_count": 63,
  "curated_claim_count": 18,
  "lexical_index_status": "READY",
  "dense_index_status": "READY",
  "warnings": [],
  "trace_ref": "trace_xxx"
}
```

## 3.4 Retrieval API

```text
POST /api/v1/debug/evidence/query
GET  /api/v1/debug/evidence/retrievals/{retrievalId}
GET  /api/v1/debug/evidence/retrievals/{retrievalId}/trace
```

### Retrieval Request

```json
{
  "query_id": "query_chest_001",
  "runtime_id": "rt_optional",
  "question_text": "活动后胸痛伴出汗时需要关注哪些高风险征象？",
  "question_type": "RISK_ASSESSMENT",
  "applicability": {
    "age_band": "ADULT_40_64",
    "sex": "MALE",
    "pregnancy": false,
    "care_setting": "OUTPATIENT_OR_HOME",
    "suspected_condition": "acute_chest_pain",
    "jurisdiction": "GLOBAL",
    "intended_audience": "CLINICIAN",
    "as_of_date": "2026-07-14"
  },
  "specialty_filters": ["emergency_medicine", "cardiology"],
  "source_type_filters": ["GUIDELINE", "CONSENSUS"],
  "requested_top_k": 8,
  "trace_ref": "trace_request_xxx"
}
```

### Retrieval Response

```json
{
  "schema_version": "phase12-p0.1",
  "retrieval_id": "ret_xxx",
  "query_id": "query_chest_001",
  "status": "COMPLETE",
  "accepted": [
    {
      "evidence_item_id": "evi_xxx",
      "claim": {
        "claim_id": "claim_xxx",
        "statement": "source-backed claim",
        "origin": "CURATED",
        "evidence_quality": "HIGH",
        "recommendation_strength": "STRONG"
      },
      "citation": {
        "source_id": "src_xxx",
        "asset_id": "asset_xxx",
        "version_id": "ver_xxx",
        "span_id": "span_xxx",
        "locator": "heading:recommendations/item:2",
        "quoted_text": "permitted source span",
        "source_checksum": "sha256:...",
        "span_checksum": "sha256:..."
      },
      "score": {
        "retrieval_relevance": 0.91,
        "source_authority": "A",
        "evidence_quality": "HIGH",
        "patient_applicability": 0.88,
        "freshness": "CURRENT",
        "citation_entailment": 0.94,
        "conflict_status": "NONE"
      },
      "verification": {
        "decision": "VERIFIED_SUPPORT",
        "provider_id": "phase12-citation-entailment",
        "provider_version": "0.1.0",
        "model_id": "configured-model",
        "model_version": "configured-version"
      },
      "warnings": []
    }
  ],
  "rejected": [
    {
      "candidate_id": "candidate_xxx",
      "reason_codes": ["ASSET_SUPERSEDED"]
    }
  ],
  "conflicts": [],
  "warnings": [],
  "trace_ref": "trace_xxx"
}
```

响应约束：

```text
accepted 数量 <= requested_top_k。
quoted_text 只返回许可和长度策略允许的 Span。
不得返回完整文档。
不得返回 Python prompt、raw model response 或内部完整推理。
```

## 3.5 Evaluation API

复用 Phase 3 Evaluation Framework，并增加：

```text
POST /api/v1/debug/evidence/evaluations/runs
GET  /api/v1/debug/evidence/evaluations/runs/{runId}
GET  /api/v1/debug/evidence/evaluations/runs/{runId}/items/{caseId}
```

Evaluation 也可以最终统一进入既有：

```text
/api/v1/debug/evaluations/**
```

具体路径在编码前二选一并固定；禁止同时维护两套不兼容 Evaluation 模型。

---

# 四、Python Provider API

Base Path：

```text
/v1/providers
```

## 4.1 Embedding

```text
POST /v1/providers/embedding
```

Request：

```json
{
  "request_id": "emb_xxx",
  "provider_id": "phase12-embedding",
  "texts": ["normalized query or chunk text"],
  "input_type": "QUERY",
  "normalize": true,
  "trace_ref": "trace_xxx"
}
```

Response：

```json
{
  "schema_version": "provider.embedding.v1",
  "provider_id": "phase12-embedding",
  "provider_version": "0.1.0",
  "model_id": "configured-model",
  "model_version": "configured-version",
  "dimension": 768,
  "normalized": true,
  "embeddings": [[0.01, -0.02]],
  "latency_ms": 23,
  "warnings": []
}
```

要求：

```text
texts 数量和长度有限制；
返回维度必须与 Registry 一致；
NaN / Infinity / 空向量拒绝；
model metadata 缺失拒绝；
不能静默改用 hash embedding。
```

## 4.2 Rerank

```text
POST /v1/providers/rerank
```

Request：

```json
{
  "request_id": "rr_xxx",
  "provider_id": "phase12-reranker",
  "query": "normalized clinical question",
  "candidates": [
    {
      "candidate_id": "chunk_xxx",
      "text": "candidate text",
      "metadata": {
        "source_id": "src_xxx",
        "version_id": "ver_xxx"
      }
    }
  ],
  "top_k": 15,
  "trace_ref": "trace_xxx"
}
```

Response：

```json
{
  "schema_version": "provider.rerank.v1",
  "provider_id": "phase12-reranker",
  "provider_version": "0.1.0",
  "model_id": "configured-model",
  "model_version": "configured-version",
  "results": [
    {
      "candidate_id": "chunk_xxx",
      "score": 0.91,
      "rank": 1
    }
  ],
  "latency_ms": 37,
  "warnings": []
}
```

## 4.3 Citation Entailment

```text
POST /v1/providers/citation-entailment
```

Request：

```json
{
  "request_id": "nli_xxx",
  "provider_id": "phase12-citation-entailment",
  "claim": "atomic clinical claim",
  "source_span": "bounded source span",
  "section_context": "optional heading only",
  "trace_ref": "trace_xxx"
}
```

Response：

```json
{
  "schema_version": "provider.citation-entailment.v1",
  "provider_id": "phase12-citation-entailment",
  "provider_version": "0.1.0",
  "model_id": "configured-model",
  "model_version": "configured-version",
  "relation": "SUPPORTS",
  "entailment_score": 0.94,
  "contradiction_score": 0.02,
  "neutral_score": 0.04,
  "reason_codes": [],
  "latency_ms": 29,
  "warnings": []
}
```

Python Provider 不返回自由文本 rationale 作为权威依据。

## 4.4 Health / Metadata

```text
GET /v1/providers/health
GET /v1/providers/metadata
```

Metadata 必须包含：

```text
provider_id
provider_version
capability_type
model_id
model_version
schema_versions
max_batch_size
max_input_length
dimension（Embedding）
device
determinism_notes
```

---

# 五、错误响应

统一结构：

```json
{
  "schema_version": "api.error.v1",
  "code": "EVIDENCE_PROVIDER_TIMEOUT",
  "message": "Evidence capability is temporarily unavailable.",
  "retryable": true,
  "trace_ref": "trace_xxx",
  "details": {
    "stage": "RERANK"
  }
}
```

禁止在 `message` 或 `details` 中返回：

```text
stack trace
raw SQL
secret
prompt
完整 source content
raw provider response
```

---

# 六、数据库测试设计

使用 PostgreSQL Testcontainers。

## 6.1 Migration

验证：

```text
Flyway migration 可从空库完成；
重复启动幂等；
所有外键、唯一约束和索引存在；
若启用 pgvector，扩展不可用时给出明确启动错误或配置化禁用；
不影响 Phase 5–11 已有表。
```

## 6.2 Source / Asset Lifecycle

用例：

```text
注册 source 成功；
UNKNOWN license 不可 publish；
BLOCKED source 不可参与 retrieval；
asset checksum mismatch ingestion 失败；
PUBLISHED asset 可以检索；
SUPERSEDED asset 默认排除；
REVOKED asset 永远排除；
同一 source + checksum 重复导入幂等。
```

## 6.3 Lexical Retrieval

用例：

```text
中文 / 英文基本检索；
关键临床术语命中；
否定词保留；
未发布 asset 不命中；
有效时间过滤；
source type / specialty / jurisdiction 过滤；
返回 rank 与 score；
相同 checksum 不重复返回。
```

## 6.4 Dense Index

用例：

```text
embedding dimension mismatch 拒绝；
provider/model version mismatch 拒绝；
向量为空、NaN、Infinity 拒绝；
相似候选排序正确；
已撤销 asset 向量不能命中；
index version 可重放。
```

---

# 七、Java 单元测试

建议测试类：

```text
ClinicalQuestionNormalizerTest
RuleBasedRetrievalPlannerTest
EligibleEvidenceScopeServiceTest
PostgresLexicalEvidenceRetrieverTest
DenseEvidenceRetrieverTest
ReciprocalRankFusionServiceTest
EvidenceCandidateDeduplicatorTest
EvidenceRerankerAdapterTest
EvidenceFreshnessEvaluatorTest
EvidenceApplicabilityEvaluatorTest
CitationVerificationServiceTest
EvidenceConflictDetectionServiceTest
ClinicalEvidenceValidationServiceTest
RuntimeEvidenceGraphAdapterTest
EvidenceProviderFallbackTest
EvidenceRetrievalTraceTest
```

关键断言：

```text
RRF 不使用原始分数直接相加；
硬过滤在排序前发生；
未验证 citation 不进入 accepted；
MISMATCH applicability 不进入 accepted；
EXPIRED / REVOKED 不进入 accepted；
每条 accepted evidence provenance 完整；
adapter 不直接修改 RuntimeState；
degraded 状态不会被标记为 COMPLETE。
```

---

# 八、Python Provider 测试

```text
embedding schema test
embedding dimension test
batch size / length limit test
rerank stable ordering test
citation support / contradiction / neutral test
metadata completeness test
timeout test
invalid model response test
NaN / Infinity rejection test
health / readiness test
```

Provider Contract Test 必须由 Java 侧和 Python 侧共同执行，避免只验证各自 DTO。

---

# 九、API 测试

## 9.1 Source / Asset API

```text
无 DebugRole → 403
非法 content_reference → 400
未审核 source publish → 409
checksum mismatch → 422
合法 ingestion → 200 / 202
重复 ingestion → 幂等结果
```

## 9.2 Retrieval API

```text
空 question → 400
requested_top_k 超限 → 400
无可用资产 → EMPTY
Dense Provider timeout → DEGRADED_LEXICAL_ONLY
Reranker timeout → DEGRADED_NO_RERANK
Citation Provider timeout → REVIEW_REQUIRED / UNAVAILABLE
PostgreSQL failure → UNAVAILABLE
完整链路 → COMPLETE
```

## 9.3 Sensitive Field Test

响应 JSON 不得包含字段：

```text
raw_prompt
system_prompt
secret
api_key
private_key
raw_external_response
full_document
chain_of_thought
full_rationale
```

---

# 十、Offline Evaluation CaseSet

建议文件：

```text
src/test/resources/evaluation/phase12-p0-evidence-cases.yaml
```

Case 结构：

```yaml
case_id: evidence_chest_001
question: 活动后胸痛伴出汗的高风险征象是什么
question_type: RISK_ASSESSMENT
applicability:
  age_band: ADULT_40_64
  care_setting: OUTPATIENT_OR_HOME
as_of_date: 2026-07-14
relevant_claim_ids:
  - claim_xxx
acceptable_source_ids:
  - src_xxx
must_not_return_claim_ids:
  - deprecated_claim_xxx
expected_conflict_type: null
expected_status: COMPLETE
tags:
  - chest_pain
  - high_risk
```

CaseSet 至少覆盖：

```text
10 个基础相关性问题
5 个同义词 / 缩写问题
5 个否定和条件性问题
5 个时效 / supersession 问题
5 个 applicability mismatch 问题
5 个 conflict 问题
5 个无答案 / 不足证据问题
5 个 Provider 降级问题
```

P0 冻结前建议不少于 40 个 Case。

---

# 十一、Evaluation 指标

## 11.1 Retrieval

```text
Recall@5 / Recall@10
Hit@5 / Hit@10
MRR@10
nDCG@10
context_precision@10
source_diversity@k
```

## 11.2 Citation

```text
citation_precision
citation_recall
entailment_accuracy
unsupported_citation_rate
contradiction_detection_recall
```

## 11.3 Governance

```text
unlicensed_asset_activation
stale_evidence_activation
revoked_asset_activation
applicability_error_rate
provenance_completeness
degraded_status_accuracy
```

## 11.4 Performance

```text
lexical_latency_ms
dense_latency_ms
rerank_latency_ms
citation_latency_ms
total_latency_ms
provider_timeout_rate
```

---

# 十二、初始冻结阈值

以下是 P0 工程冻结目标，不是临床有效性结论；必须在真实 CaseSet 上记录样本量和置信限制。

```text
Recall@10 >= 0.85
MRR@10 >= 0.70
nDCG@10 >= 0.75
context_precision@10 >= 0.65
citation_precision >= 0.90
unsupported_citation_rate <= 0.05
contradiction_detection_recall >= 0.80
stale_evidence_activation = 0
revoked_asset_activation = 0
unlicensed_asset_activation = 0
critical applicability error = 0
provenance_completeness = 1.00
degraded_status_accuracy = 1.00
```

性能目标：

```text
本地 warm path total p95 <= 5000 ms
单次查询无未受控无限重试
Provider timeout 由配置控制且进入 Trace
```

若模型和本地硬件无法满足延迟目标，可以以明确的已知限制冻结，但不得牺牲 Citation、License、Freshness 或 Provenance 硬约束。

---

# 十三、故障注入测试

```text
PostgreSQL unavailable
pgvector unavailable
Embedding Provider timeout
Embedding dimension drift
Reranker invalid schema
Citation Provider contradiction
Citation Provider timeout
asset checksum corruption
source license revoked
asset superseded during query
empty corpus
all candidates filtered
trace persistence failure
```

期望：

```text
任何故障都有结构化状态和 reason code；
不产生无来源补全；
不将 degraded 标成 complete；
不绕过 Runtime Validation；
不向 PatientOutput 泄露内部错误。
```

---

# 十四、人工验证

至少完成：

```text
1. 人工核对 Source Manifest 与许可证状态。
2. 随机抽查 20 个 Chunk 是否保持上下文和 locator。
3. 随机抽查 20 个 Claim 是否由 Span 支持。
4. 人工复核 20 个 accepted citation。
5. 人工复核 10 个 rejected candidate 的拒绝原因。
6. 验证 expired / superseded / revoked 资产不进入普通结果。
7. 验证 Dense / Rerank / Citation Provider 关闭时的降级界面和 Trace。
8. 验证 Clinician Evidence Panel 可展示安全摘要和引用，不展示 raw provider response。
9. 验证 Patient Portal 不直接展示 raw evidence 或内部评分。
10. 验证同一 query + asset/index/provider version 可重放。
```

---

# 十五、测试命令与记录

编码阶段必须在人工测试结果中记录实际命令和结果：

```text
mvn test
python -m pytest -q
PostgreSQL Testcontainers integration tests
Provider contract tests
Offline evidence evaluation runner
console-web npm run typecheck
console-web npm test
console-web npm run build
```

若 P0 不修改前端，仍需运行最小前端回归，证明 Patient / Clinician / Governance 冻结边界未被破坏。

---

# 十六、冻结所需证据

```text
Phase12_P0人工测试结果.md
Phase12_P0冻结记录.md
Source Manifest
Offline Evaluation Result
Provider Metadata Snapshot
Active Index Version Snapshot
Database Migration Record
API Contract Test Result
Failure Injection Result
```

未形成上述证据前，不得把 Phase 12-P0 标记为 FROZEN，也不得进入 Phase 12-P1。