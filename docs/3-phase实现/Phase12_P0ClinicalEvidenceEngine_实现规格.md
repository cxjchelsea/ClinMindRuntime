# Phase 12-P0 Clinical Evidence Engine 实现规格

> 上位总体设计：`docs/3-phase实现/Phase12真实临床能力纵切_总体设计.md`  
> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md` v3.0  
> 技术蓝图：`docs/1-总设计/ClinMindRuntime技术实现总方案.md` v3.0  
> 前置冻结：Phase 11-P1  
> 当前阶段：Phase 12-P0 设计阶段  
> 当前目标：建立真实、版本化、可追踪、可评测并受 Runtime 控制的 Clinical Evidence Engine MVP。

---

# 一、Phase 定位

Phase 12-P0 要解决 Phase 7 证据原型的真实能力缺口：

```text
Phase 7
证明 EvidenceProvider / EvidenceCandidate / EvidenceValidation / EvidenceGraph
可以进入受控 Runtime。

Phase 12-P0
证明系统可以从真实、许可明确、版本明确的医学资料中，
完成混合检索、重排、来源治理、适用性判断、引用验证、冲突处理和可追踪采用。
```

P0 的完成标准不是“可以把文档放进向量库”，而是：

```text
每一个 accepted evidence item
都能说明：
从哪里来、哪个版本、原文在哪里、为什么相关、是否适用、是否过期、
是否支持当前主张、是否存在冲突、由哪些 Provider 处理、最终为何被采用或拒绝。
```

---

# 二、当前不做什么

Phase 12-P0 明确不做：

```text
1. 不做 LLM-backed InquiryPlanningAgent；属于 Phase 12-P1。
2. 不接 FHIR / EHR；属于 Phase 12-P1 / Phase 13。
3. 不建立 ClinicalFactLedger、双时间事实和纵向患者状态；属于 Phase 13。
4. 不建立完整 Policy IR、RuntimeRiskState、CapabilityLease 平台；属于 Phase 14。
5. 不做真实写操作、Shadow Execution、Commit / Rollback；属于 Phase 15。
6. 不做 Multi-Agent / Handoff；属于 Phase 16。
7. 不做完整 Deep Evidence、多问题研究和 GraphRAG 平台；属于 Phase 17。
8. 不做模型训练、微调、DPO、蒸馏；属于 Phase 18。
9. 不接远程 MCP 或医院系统；属于 Phase 19。
10. 不做生产级认证、租户和合规平台；属于 Phase 21。
11. 不让 Evidence Engine 直接生成 PatientOutput。
12. 不让 Python Provider 成为证据资产或 Runtime 的权威存储。
13. 不使用未经许可证确认的全文作为正式发布资产。
14. 不用一个综合相似度替代来源权威性、证据质量、适用性和引用支持度。
```

---

# 三、核心成功命题

P0 必须证明：

```text
1. Source 和 Asset Version 可治理。
2. Chunk / Span / Claim 之间可追踪。
3. BM25 与 Dense Retrieval 可独立评测。
4. Hybrid Fusion 不依赖不可比较的原始分数。
5. Reranker 只改变排序，不改变来源事实。
6. Citation Entailment 能识别支持、部分支持、矛盾和无关。
7. Freshness / Applicability / Authority 是独立维度。
8. 已废弃或未发布资产不会进入 accepted result。
9. Provider 失败时有显式 DEGRADE / UNAVAILABLE，而非静默回退。
10. EvidenceRetrievalResult 只有经过 Runtime Validation 后才能进入 RuntimeEvidenceGraph。
11. 同一资产版本、索引版本和 Provider 版本可重放。
12. 患者端不直接暴露原始证据和内部评分。
```

---

# 四、总体实现链路

## 4.1 Ingestion

```text
Source Registration
→ License / Jurisdiction / Authority Review
→ EvidenceAssetVersion Creation
→ Content Acquisition
→ Checksum / MIME / Size Validation
→ Parse / Normalize
→ Structural Segmentation
→ EvidenceChunk / EvidenceSpan
→ Metadata Extraction
→ Curated or Candidate Claim Extraction
→ Claim Validation
→ Lexical Index
→ Embedding Generation
→ Dense Index
→ Offline Evaluation
→ Publish Asset Version
```

P0 允许人工准备少量高质量语料，不要求自动抓取互联网或自动更新。

## 4.2 Retrieval

```text
EvidenceQueryRequest
→ Request Validation
→ ClinicalQuestionNormalizer（最小规则版）
→ RetrievalPlanner（最小规则版）
→ Eligible Asset Filter
→ Lexical Recall
→ Dense Recall
→ Reciprocal Rank Fusion
→ Candidate Deduplication
→ Cross-encoder Rerank
→ Authority / Freshness / Applicability Evaluation
→ Source-backed Claim Assembly
→ Citation Entailment
→ Conflict Detection
→ EvidenceValidation
→ EvidenceRetrievalResult
→ RuntimeEvidenceGraphAdapter
```

---

# 五、核心领域对象

## 5.1 SourceRegistryEntry

```java
public record SourceRegistryEntry(
    String sourceId,
    String displayName,
    String publisher,
    EvidenceSourceType sourceType,
    AuthorityLevel authorityLevel,
    String jurisdiction,
    String language,
    String homepage,
    LicenseStatus licenseStatus,
    String licenseName,
    String licenseReference,
    SourceTrustStatus trustStatus,
    ReviewStatus reviewStatus,
    Instant reviewedAt,
    String reviewedBy,
    String notes
) {}
```

关键规则：

```text
licenseStatus != VERIFIED → 不允许 PUBLISHED。
trustStatus == BLOCKED → 不允许参与检索。
reviewStatus != APPROVED → 只能进入 draft / evaluation corpus。
```

建议枚举：

```text
EvidenceSourceType
GUIDELINE / CONSENSUS / SYSTEMATIC_REVIEW / PUBLIC_HEALTH / DRUG_REFERENCE /
CLINICAL_PATHWAY / PATIENT_EDUCATION / OTHER

AuthorityLevel
A / B / C / UNVERIFIED

LicenseStatus
VERIFIED / RESTRICTED / UNKNOWN / REJECTED

SourceTrustStatus
TRUSTED / WATCH / BLOCKED
```

## 5.2 EvidenceAssetVersion

```java
public record EvidenceAssetVersion(
    String assetId,
    String versionId,
    String sourceId,
    String title,
    String documentType,
    String externalReference,
    String specialty,
    String intendedAudience,
    String jurisdiction,
    String language,
    LocalDate publicationDate,
    Instant effectiveFrom,
    Instant effectiveTo,
    String supersedesVersionId,
    AssetLifecycleStatus lifecycleStatus,
    ReviewStatus reviewStatus,
    String checksum,
    String mimeType,
    long contentLength,
    String parserVersion,
    String schemaVersion,
    Instant ingestedAt
) {}
```

生命周期：

```text
DRAFT
→ INGESTED
→ INDEXED
→ EVALUATED
→ PUBLISHED
→ SUPERSEDED / DEPRECATED / REVOKED
```

硬约束：

```text
只有 PUBLISHED 且在 effective interval 内的版本进入默认检索。
REVOKED 永不参与检索。
SUPERSEDED 默认不参与普通检索，可在冲突和历史回放模式中显式使用。
```

## 5.3 EvidenceChunk

```java
public record EvidenceChunk(
    String chunkId,
    String versionId,
    String sectionPath,
    int ordinal,
    String normalizedText,
    String textChecksum,
    int tokenCount,
    Map<String, String> metadata
) {}
```

Chunk 用于召回，不作为最终引用单位。

## 5.4 EvidenceSpan

```java
public record EvidenceSpan(
    String spanId,
    String chunkId,
    String versionId,
    int startOffset,
    int endOffset,
    String quotedText,
    String spanChecksum,
    String locator,
    SpanType spanType
) {}
```

`locator` 示例：

```text
section:4.2/paragraph:3
page:17/paragraph:2
heading:Recommendations/item:5
```

Span 是 Citation 的最小可验证来源位置。

## 5.5 EvidenceClaim

```java
public record EvidenceClaim(
    String claimId,
    String versionId,
    String primarySpanId,
    String statement,
    ClaimOrigin origin,
    String population,
    String intervention,
    String comparator,
    String outcome,
    RecommendationStrength recommendationStrength,
    EvidenceQuality evidenceQuality,
    ClaimReviewStatus reviewStatus,
    String claimChecksum,
    String schemaVersion
) {}
```

P0 支持两类 Claim：

```text
CURATED
由人工从原文 Span 提取并审核。

EXTRACTED_CANDIDATE
由规则或模型提取，只能进入评测或人工审核，不能默认成为 accepted claim。
```

P0 的正式发布语料优先使用 CURATED Claim，以降低 Claim Extraction 对 MVP 的不确定性。

## 5.6 ClaimEvidenceLink

```java
public record ClaimEvidenceLink(
    String linkId,
    String claimId,
    String spanId,
    ClaimEvidenceRelation relation,
    double entailmentScore,
    double applicabilityScore,
    ConflictStatus conflictStatus,
    VerificationStatus verificationStatus,
    String verifierId,
    String verifierVersion,
    Instant verifiedAt
) {}
```

关系：

```text
SUPPORTS
PARTIALLY_SUPPORTS
CONTRADICTS
CONTEXT_ONLY
OUT_OF_SCOPE
INSUFFICIENT
```

## 5.7 CitationVerificationResult

```java
public record CitationVerificationResult(
    String verificationId,
    String claimId,
    String spanId,
    ClaimEvidenceRelation relation,
    double entailmentScore,
    VerificationDecision decision,
    List<String> reasonCodes,
    String providerId,
    String providerVersion,
    String modelId,
    String modelVersion,
    String inputChecksum,
    long latencyMs,
    Instant verifiedAt
) {}
```

决策：

```text
VERIFIED_SUPPORT
VERIFIED_PARTIAL
VERIFIED_CONTRADICTION
UNVERIFIED
REVIEW_REQUIRED
```

## 5.8 EvidenceApplicability

P0 使用显式、请求级上下文，不读取长期患者事实：

```java
public record EvidenceApplicabilityContext(
    String ageBand,
    String sex,
    Boolean pregnancy,
    String careSetting,
    String suspectedCondition,
    String jurisdiction,
    String intendedAudience,
    LocalDate asOfDate
) {}
```

该对象只用于当前检索，不自动进入 ClinicalFactLedger。

## 5.9 EvidenceScore

```java
public record EvidenceScore(
    double retrievalRelevance,
    AuthorityLevel sourceAuthority,
    EvidenceQuality evidenceQuality,
    double patientApplicability,
    FreshnessStatus freshness,
    double citationEntailment,
    ConflictStatus conflictStatus
) {}
```

禁止新增一个无解释的 `finalScore` 作为唯一采用依据。

## 5.10 EvidenceRetrievalResult

```java
public record EvidenceRetrievalResult(
    String retrievalId,
    String queryId,
    RetrievalStatus status,
    List<AcceptedEvidenceItem> accepted,
    List<RejectedEvidenceItem> rejected,
    List<EvidenceConflictSet> conflicts,
    List<String> warnings,
    EvidenceRetrievalTrace trace,
    String schemaVersion
) {}
```

状态：

```text
COMPLETE
DEGRADED_LEXICAL_ONLY
DEGRADED_NO_RERANK
REVIEW_REQUIRED
EMPTY
UNAVAILABLE
```

---

# 六、检索请求与规划

## 6.1 EvidenceQueryRequest

```java
public record EvidenceQueryRequest(
    String queryId,
    String runtimeId,
    String questionText,
    EvidenceQuestionType questionType,
    EvidenceApplicabilityContext applicability,
    Set<String> specialtyFilters,
    Set<String> sourceTypeFilters,
    int requestedTopK,
    Instant requestedAt,
    String traceRef
) {}
```

## 6.2 ClinicalQuestionNormalizer

P0 只做确定性规范化：

```text
Unicode / whitespace normalize
medical abbreviation mapping（显式字典）
question type detection（规则）
negation preservation
age / setting / temporal phrase extraction
unsafe instruction removal
```

不使用自由生成式 LLM 改写查询，以避免查询语义漂移。

## 6.3 RetrievalPlanner

P0 Planner 只输出结构化计划：

```java
public record RetrievalPlan(
    String normalizedQuestion,
    List<String> lexicalQueries,
    List<String> denseQueries,
    Set<String> eligibleSourceTypes,
    Set<String> eligibleSpecialties,
    int lexicalTopK,
    int denseTopK,
    int rerankTopK,
    int finalTopK,
    boolean requireCitationVerification,
    boolean includeContradictingEvidence
) {}
```

初始工程默认值：

```text
lexicalTopK = 30
denseTopK = 30
fusionTopK = 30
rerankTopK = 15
finalTopK = 8
maxPerAssetVersion = 3
```

上述参数必须配置化，并通过 Evaluation 调整。

---

# 七、混合检索设计

## 7.1 LexicalRetriever

```java
public interface LexicalEvidenceRetriever {
    List<RetrievalCandidate> retrieve(
        RetrievalPlan plan,
        EligibleEvidenceScope scope,
        EvidenceTraceContext trace
    );
}
```

P0 目标实现：

```text
PostgreSQL tsvector / tsquery Full-text Search
```

原因：

```text
复用已有 PostgreSQL；
避免 P0 引入 Elasticsearch / OpenSearch / Lucene 服务；
支持事务化资产发布和过滤；
语料规模小，足以满足 MVP。
```

测试实现：

```text
InMemoryLexicalEvidenceRetriever
```

## 7.2 DenseRetriever

```java
public interface DenseEvidenceRetriever {
    List<RetrievalCandidate> retrieve(
        RetrievalPlan plan,
        EligibleEvidenceScope scope,
        EvidenceTraceContext trace
    );
}
```

职责分离：

```text
Python Embedding Provider：生成 query embedding。
DenseIndexPort：保存并检索 chunk embedding。
Java Retrieval Orchestrator：负责 scope、topK、trace 和结果合并。
```

P0 允许实现：

```text
PostgreSQL + pgvector exact cosine search
```

若 pgvector 在本地环境不可用：

```text
InMemoryDenseIndex 用于测试；
正式冻结必须明确实际使用的 Dense Index 实现；
不得把 hash embedding 描述为真实 Dense Retrieval。
```

P0 小语料默认使用 exact search，不急于建立 HNSW / IVF；索引优化进入后续规模化阶段。

## 7.3 Reciprocal Rank Fusion

禁止直接相加 BM25 原始分数和 cosine score。

P0 使用 RRF：

```text
RRF_score(d) = Σ 1 / (k + rank_i(d))
```

初始 `k=60`，配置化。

记录：

```text
lexical_rank
lexical_score
dense_rank
dense_score
rrf_score
```

## 7.4 Deduplication

依次按以下键去重：

```text
span_checksum
claim_checksum
chunk text_checksum
asset version + locator
```

去重后保留各召回通道的 provenance，不丢失为何被召回的信息。

## 7.5 Cross-encoder Rerank

```java
public interface EvidenceReranker {
    RerankResult rerank(
        String normalizedQuestion,
        List<RetrievalCandidate> candidates,
        ProviderTraceContext trace
    );
}
```

Python Provider 返回：

```text
candidate_id
rerank_score
rank
provider_id / version
model_id / version
latency_ms
warnings
```

Reranker 只能重排候选，不得：

```text
修改来源文本；
伪造 Claim；
删除 provenance；
直接标记临床可采用。
```

---

# 八、来源、时效与适用性治理

## 8.1 Eligible Asset Filter

检索前硬过滤：

```text
source.review_status == APPROVED
source.license_status == VERIFIED
source.trust_status != BLOCKED
asset.lifecycle_status == PUBLISHED
asset.effective_from <= asOfTime
asset.effective_to is null or asset.effective_to >= asOfTime
language / jurisdiction / audience 满足请求约束
```

硬过滤必须发生在语义排序之前。

## 8.2 Authority

Authority 不由模型自由判断，优先来自 Source Registry 的审核字段。

模型可以生成 `authority_warning`，但不能擅自提升来源级别。

## 8.3 Freshness

```text
CURRENT
NEAR_EXPIRY
EXPIRED
SUPERSEDED
UNKNOWN
```

规则：

```text
EXPIRED / SUPERSEDED 默认不进入 accepted；
冲突分析或历史回放可显式纳入；
UNKNOWN 必须带 warning，不能等价于 CURRENT。
```

## 8.4 Applicability

Applicability 是结构化匹配结果：

```text
MATCH
PARTIAL_MATCH
MISMATCH
UNKNOWN
```

至少考虑：

```text
population / age band
sex / pregnancy（若资料相关）
care setting
condition / intervention scope
jurisdiction
intended audience
```

P0 不允许 LLM 单独决定 applicability；规则和人工元数据为主，模型仅可提供候选判断。

---

# 九、Citation Entailment

## 9.1 输入

```text
claim statement
source span text
必要的 section heading
```

不得将完整文档、无关段落或原始用户 prompt 发送给 Citation Provider。

## 9.2 验证层次

```text
Level 1：结构校验
claim / span / asset version / checksum 是否存在且一致。

Level 2：规则校验
否定、数值、单位、比较方向、绝对/条件性表述是否明显冲突。

Level 3：Model Entailment
SUPPORT / PARTIAL / CONTRADICT / IRRELEVANT。

Level 4：Policy Decision
是否允许进入 accepted evidence。
```

## 9.3 初始采用规则

以下为工程默认值，冻结前必须由 Evaluation 校准：

```text
VERIFIED_SUPPORT：entailment >= 0.80
VERIFIED_PARTIAL：0.60 <= entailment < 0.80
低于 0.60：UNVERIFIED 或 CONTRADICTION
```

即使分数达到阈值，以下任一情况仍不得自动接受：

```text
source 未发布；
span checksum 不一致；
asset 已撤销；
applicability == MISMATCH；
freshness == EXPIRED；
存在未解决的关键数值冲突；
provider metadata 缺失。
```

---

# 十、Conflict Detection

P0 支持最小冲突类型：

```text
DIRECT_CONTRADICTION
RECOMMENDATION_STRENGTH_DIFFERENCE
POPULATION_SCOPE_DIFFERENCE
JURISDICTION_DIFFERENCE
TEMPORAL_SUPERSESSION
INSUFFICIENT_CONTEXT
```

```java
public record EvidenceConflictSet(
    String conflictSetId,
    String topicKey,
    ConflictType conflictType,
    List<String> claimIds,
    ConflictSeverity severity,
    ConflictResolutionStatus resolutionStatus,
    List<String> reasonCodes
) {}
```

P0 不自动综合出唯一正确答案。

关键冲突进入：

```text
REVIEW_REQUIRED
```

并由 Runtime 控制是否继续、降级或仅向医生端展示。

---

# 十一、EvidenceValidation

```java
public interface ClinicalEvidenceValidationService {
    EvidenceValidationResult validate(
        EvidenceRetrievalResult result,
        EvidenceValidationContext context
    );
}
```

验证规则至少包括：

```text
source_exists
source_approved
license_verified
asset_published
asset_effective
checksum_valid
span_within_chunk
claim_has_source_span
provider_metadata_complete
citation_verified
applicability_not_mismatch
freshness_not_expired
critical_conflict_resolved_or_flagged
result_count_within_limit
provenance_complete
```

采用结果：

```text
ACCEPT
ACCEPT_WITH_WARNINGS
REVIEW_REQUIRED
REJECT
UNAVAILABLE
```

---

# 十二、Runtime 集成

## 12.1 Provider 接口

复用并演进 EvidenceProvider：

```java
public interface EvidenceProvider {
    EvidenceRetrievalResult retrieve(EvidenceQueryRequest request);
}
```

Phase 12-P0 不要求正式 CapabilityLease；该参数在 Phase 12-P1 最小治理或 Phase 14 正式治理中加入。

为避免提前伪造 Phase 14 能力，P0 使用现有 Runtime capability policy、provider validation 和 trace context。

## 12.2 RuntimeEvidenceGraphAdapter

```java
public interface RuntimeEvidenceGraphAdapter {
    RuntimeEvidenceGraphPatch toPatch(
        EvidenceRetrievalResult result,
        EvidenceValidationResult validation
    );
}
```

只有：

```text
validation = ACCEPT / ACCEPT_WITH_WARNINGS
```

的 Evidence Item 可以形成 Patch。

Patch 仍由 RuntimeStateCommitService 或现有 Runtime 主链路提交，Adapter 不直接修改 RuntimeState。

## 12.3 Shadow Mode

P0 开发默认：

```text
legacy provider 形成现有 Runtime 结果；
clinical evidence engine 并行执行；
真实引擎结果只写 retrieval trace / evaluation；
不改变 PatientOutput。
```

冻结前必须完成一次 `active` debug 场景验证，但患者输出仍必须经过既有 DecisionBoundary。

---

# 十三、失败与降级

## 13.1 Dense Provider 不可用

```text
状态：DEGRADED_LEXICAL_ONLY
允许：继续 BM25 + validation
要求：显式 warning、provider failure trace、Evaluation 记录
```

## 13.2 Reranker 不可用

```text
状态：DEGRADED_NO_RERANK
允许：使用 RRF 排序
要求：不得伪装为完整结果
```

## 13.3 Citation Provider 不可用

```text
状态：REVIEW_REQUIRED 或 UNAVAILABLE
要求：未验证 Claim 不进入 accepted
```

## 13.4 PostgreSQL / Asset Repository 不可用

```text
状态：UNAVAILABLE
策略：fail closed
```

## 13.5 无结果

```text
状态：EMPTY
不得生成无来源补全；
Runtime 可选择澄清、换查询、人工审核或安全停止。
```

## 13.6 Provider 版本漂移

```text
metadata 与 Registry 不一致 → REJECT
```

---

# 十四、持久化设计

P0 必须表：

```text
evidence_source
evidence_asset_version
evidence_chunk
evidence_span
evidence_claim
claim_evidence_link
citation_verification_result
retrieval_trace
retrieval_candidate_trace
embedding_index_metadata
```

P0 可选但建议：

```text
evidence_conflict_set
evidence_conflict_member
evidence_chunk_embedding
```

关键唯一约束：

```text
evidence_source.source_id
evidence_asset_version.version_id
evidence_asset_version(source_id, checksum)
evidence_chunk.chunk_id
evidence_chunk(version_id, ordinal)
evidence_span.span_id
evidence_claim.claim_id
evidence_claim(version_id, claim_checksum)
claim_evidence_link(claim_id, span_id, verifier_version)
```

索引：

```text
evidence_asset_version(source_id, lifecycle_status, effective_from, effective_to)
evidence_chunk(version_id)
evidence_claim(version_id, review_status)
retrieval_trace(query_id, created_at)
GIN evidence_chunk.search_vector
```

向量存储通过 `DenseIndexPort` 隔离，不让领域模型依赖 pgvector 类型。

---

# 十五、Source Manifest

每个初始语料包必须具有 manifest：

```yaml
source_id: string
publisher: string
source_type: GUIDELINE
jurisdiction: string
language: zh-CN | en
homepage: string
license_status: VERIFIED
license_name: string
license_reference: string
allowed_use: retrieval_and_citation
content_storage: full_text | permitted_excerpt | metadata_only
reviewed_by: string
reviewed_at: timestamp
assets:
  - asset_id: string
    version_id: string
    title: string
    publication_date: date
    effective_from: date
    effective_to: date | null
    external_reference: string
    checksum: string
```

`license_status=UNKNOWN` 的资产不得进入 Published Corpus。

P0 不要求大量语料，优先 5–15 个高质量、可验证版本的来源资产。

---

# 十六、包结构

建议按实际实现逐步建立：

```text
src/main/java/com/clinmind/runtime/evidence/
├── source/
├── asset/
├── ingestion/
├── query/
├── retrieval/
│   ├── lexical/
│   ├── dense/
│   ├── fusion/
│   └── rerank/
├── claim/
├── citation/
├── applicability/
├── conflict/
├── validation/
├── graph/
├── trace/
├── persistence/
├── provider/
└── config/

src/main/java/com/clinmind/runtime/api/debug/evidence/
```

禁止一次性创建全部空目录；按任务清单逐批建立。

Python 侧只新增 Provider endpoint 和模型 adapter，不建立第二套 Evidence Domain。

---

# 十七、配置

建议配置：

```yaml
clinmind:
  evidence:
    engine:
      mode: shadow
      lexical-top-k: 30
      dense-top-k: 30
      rerank-top-k: 15
      final-top-k: 8
      max-per-asset: 3
      rrf-k: 60
      require-citation-verification: true
      reject-expired: true
      reject-unlicensed: true
      request-timeout-ms: 5000
    dense:
      implementation: pgvector
      embedding-provider-id: phase12-embedding
    rerank:
      provider-id: phase12-reranker
    citation:
      provider-id: phase12-citation-entailment
```

所有配置必须进入 trace snapshot。

---

# 十八、可观测性与 Trace

每次查询至少记录：

```text
retrieval_id
query_id / runtime_id
normalized_question checksum
asset scope
index version
lexical candidates and ranks
dense candidates and ranks
fusion result
rerank result
filter reasons
citation verification
accepted / rejected reasons
provider versions
latency by stage
warnings / degraded state
final validation decision
```

禁止在普通日志中输出：

```text
完整患者输入
未脱敏上下文
secret
完整 raw external response
完整模型 prompt
```

---

# 十九、完成标准

Phase 12-P0 只有同时满足以下条件才可冻结：

```text
1. SourceRegistry 与 AssetVersion 可持久化、审核、发布、废弃。
2. 至少一套许可状态 VERIFIED 的真实医学语料可导入。
3. BM25 / Full-text Recall 使用真实实现。
4. Dense Recall 使用真实 embedding，不使用 hash embedding 冒充。
5. Hybrid Fusion 使用可解释的 RRF。
6. Rerank 使用真实 Provider 或明确标记的可替换真实实现。
7. Citation Verification 能输出结构化关系和版本信息。
8. 所有 accepted item 均有 source → asset version → span → claim provenance。
9. 过期、撤销、未授权和不适用证据测试通过。
10. Provider 故障的降级与 fail-closed 测试通过。
11. Offline Evaluation 达到 API/测试设计中的冻结阈值。
12. Shadow 与 active debug 集成测试通过。
13. Java / Python / PostgreSQL / API 自动化测试通过。
14. 人工证据审阅通过。
15. README、项目地图、AI Implementation Skill、任务清单和冻结记录同步。
```

---

# 二十、进入 Phase 12-P1 的条件

```text
Phase 12-P0 正式冻结；
真实 Evidence Engine 可稳定返回结构化结果；
Citation / Provenance / Failure semantics 已固定；
不再依赖 Phase 7 Mock 作为主路径；
Evidence Engine 尚未直接输出 PatientOutput；
之后才可设计 LLM-backed InquiryPlanningAgent、只读 FHIR 和最小统一治理。
```