# Phase 12-P0 开发任务清单：Clinical Evidence Engine MVP

> 上位实现规格：`docs/3-phase实现/Phase12_P0ClinicalEvidenceEngine_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase12_P0EvidenceEngine_API与测试设计.md`  
> Phase 总体设计：`docs/3-phase实现/Phase12真实临床能力纵切_总体设计.md`  
> 前置冻结：Phase 11-P1  
> 当前状态：设计已建立，代码尚未开始  
> 总原则：按依赖顺序逐项实现；每个任务必须同时提交代码、测试、Trace/审计和文档状态，不允许先堆空目录或一次性生成未验证的大量代码。

---

# 一、Phase 12-P0 总目标

```text
真实许可医学资料
→ Source / Asset Version Governance
→ Chunk / Span / Curated Claim
→ PostgreSQL Lexical Recall + Real Dense Recall
→ RRF + Rerank
→ Authority / Freshness / Applicability
→ Citation Entailment / Conflict
→ EvidenceValidation
→ RuntimeEvidenceGraph Adapter
→ Evaluation / Trace / Audit
```

最终证明：

```text
Clinical Evidence Engine 可以作为受控 EvidenceProvider 接入 Runtime，
所有 accepted evidence 都有完整 provenance，
任何模型结果都不能绕过 Java Runtime Validation 与 DecisionBoundary。
```

---

# 二、进入编码前检查

| 编号 | 检查项 | 状态 |
|---|---|---|
| PRE-01 | Phase 12 总体设计完成审阅 | 已审阅，阻塞项已收口 |
| PRE-02 | P0 实现规格完成审阅 | 已审阅，接口兼容与评测闭环已收口 |
| PRE-03 | P0 API 与测试设计完成审阅 | 已审阅，Evaluation API 已固定 |
| PRE-04 | 本任务清单完成审阅 | 已审阅，任务依赖已调整 |
| PRE-05 | 初始 Source Manifest 范围确定 | 已完成：`src/main/resources/evidence/phase12-p0/source-manifest.yml` |
| PRE-06 | 许可证审核责任与记录格式确定 | 已完成：`src/main/resources/evidence/phase12-p0/license-review-record.yml` |
| PRE-07 | Offline Evaluation CaseSet 结构确定 | 已完成：`src/test/resources/evaluation/case-sets/phase12-p0/`，45 个 seed case |
| PRE-08 | Embedding / Rerank / Citation Provider 候选确定 | 已完成：`src/main/resources/evidence/phase12-p0/provider-candidates.yml` |
| PRE-09 | pgvector 可用性验证或替代 DenseIndexPort 方案确定 | 已完成：首版以 DenseIndexPort 隔离，测试允许 InMemory；冻结前必须验证 pgvector 或真实替代实现 |
| PRE-10 | Feature Flag 与 shadow/active 策略确认 | 已完成：`clinmind.evidence.engine.mode=legacy|shadow|active`，开发默认 shadow，active 仅 debug |

PRE-01 至 PRE-10 已关闭，可以开始 P12P0-A；仍不得跳过任务依赖或直接冻结 Phase 12-P0。

---

# 三、任务总览

| 编号 | 任务 | 依赖 | 状态 |
|---|---|---|---|
| P12P0-A | Source Manifest、许可证与语料范围冻结 | PRE | 已完成：loader / validation / unit test 通过 |
| P12P0-B | Evidence Domain Model 与 Repository Port | A | 已完成：domain model / repository port / deterministic query skeleton / unit test 通过 |
| P12P0-C | PostgreSQL Migration 与资产生命周期持久化 | B | 已完成：Flyway v12 schema / JDBC source+asset+trace repository / Testcontainers 通过 |
| P12P0-D | Ingestion / Parse / Chunk / Span Pipeline | B、C | 已完成：classpath allowlist / checksum / markdown parser / chunk+span JDBC / failure quarantine 测试通过 |
| P12P0-E | Curated Claim 与 Claim-Span Link | D | 已完成：curated-claims YAML / PICO metadata / review+origin / claim-span link / checksum gate 测试通过 |
| P12P0-F | PostgreSQL Lexical Retrieval | A、B、C、D | 未开始 |
| P12P0-G | Python Embedding Provider 与 DenseIndexPort | A、B、C、D | 未开始 |
| P12P0-H | Hybrid Fusion、Dedup 与 Retrieval Planner | F、G | 未开始 |
| P12P0-I | Python Rerank Provider 与 Java Adapter | H | 未开始 |
| P12P0-J | Authority / Freshness / Applicability | A、B、I | 未开始 |
| P12P0-K | Citation Entailment Provider 与验证服务 | E、I | 未开始 |
| P12P0-L | Conflict Detection 与 EvidenceValidation | J、K | 未开始 |
| P12P0-M | ClinicalEvidenceRetrievalResult、Trace 与 Runtime Adapter | L | 未开始 |
| P12P0-N | Debug / Governance API | C、D、M | 未开始 |
| P12P0-O | Offline Evaluation CaseSet 与 Scorer | F–M | 未开始 |
| P12P0-P | 故障注入、安全与回归测试 | C–O | 未开始 |
| P12P0-Q | Shadow / Active 集成验收 | M–P | 未开始 |
| P12P0-R | 人工测试、文档同步与冻结 | A–Q | 未开始 |

---

# 四、P12P0-A：Source Manifest、许可证与语料范围

## 目标

建立 P0 Published Corpus 的权威来源清单。

## 任务

```text
A1. 定义 Source Manifest YAML Schema。
A2. 定义 license_status / allowed_use / content_storage 规则。
A3. 确定初始胸痛 / 胸闷证据主题范围。
A4. 选择 5–15 个版本明确、许可状态可记录的高质量资产。
A5. 记录 publisher、jurisdiction、language、publication/effective date。
A6. 定义 Source reviewer 和 review record。
A7. 区分 full_text / permitted_excerpt / metadata_only。
A8. 为每个资产计算 checksum。
A9. 建立 rejected / unknown license 清单，证明其不会进入 Published Corpus。
```

## 交付物

```text
Source Manifest
Source Manifest Schema
License Review Record
Initial Corpus Scope
```

## 验收

```text
所有 Published 候选 license_status=VERIFIED；
UNKNOWN / RESTRICTED 未被误标为可全文使用；
每个 asset 具有 version 和 checksum；
不包含真实患者数据。
```

---

# 五、P12P0-B：Evidence Domain Model 与 Repository Port

## 任务

实现领域对象：

```text
SourceRegistryEntry
EvidenceAssetVersion
EvidenceChunk
EvidenceSpan
EvidenceClaim
ClaimEvidenceLink
CitationVerificationResult
EvidenceApplicabilityContext
EvidenceScore
EvidenceConflictSet
ClinicalEvidenceRetrievalResult
EvidenceRetrievalTrace
ClinicalEvidenceProviderAdapter
EligibleEvidenceScopePolicy
```

实现 Port：

```text
EvidenceSourceRepository
EvidenceAssetVersionRepository
EvidenceChunkRepository
EvidenceSpanRepository
EvidenceClaimRepository
ClaimEvidenceLinkRepository
CitationVerificationRepository
EvidenceRetrievalTraceRepository
DenseIndexPort
```

## 约束

```text
领域对象不依赖 JDBC / pgvector / FastAPI DTO；
ID、checksum、version、status 不允许空；
枚举值必须固定并有 unknown handling；
不创建 Phase 13 ClinicalFact 对象。
```

## 测试

```text
constructor validation
equality / identity
lifecycle transition
checksum validation
serialization contract
```

## 当前实现状态（2026-07-15）

```text
已新增 Phase12 evidence domain model：
SourceRegistryEntry / EvidenceAssetVersion / EvidenceChunk / EvidenceSpan /
EvidenceClaim / ClaimEvidenceLink / CitationVerificationResult /
EvidenceApplicabilityContext / EvidenceScore / EvidenceConflictSet /
ClinicalEvidenceRetrievalResult / EvidenceRetrievalTrace。

已新增 Repository Port：
EvidenceSourceRepository / EvidenceAssetVersionRepository / EvidenceChunkRepository /
EvidenceSpanRepository / EvidenceClaimRepository / ClaimEvidenceLinkRepository /
CitationVerificationRepository / EvidenceRetrievalTraceRepository / DenseIndexPort。

已新增最小 deterministic ClinicalEvidenceQueryService skeleton：
仅用于 P0 本地 evaluation/debug 骨架验证；
读取 Phase12 Source Manifest；
不会把 CLAIMED/DRAFT seed 资产提升为 PRODUCTION accepted evidence；
不替代 P12P0-C 之后的 PostgreSQL、真实 Lexical/Dense、RRF、Rerank、Citation Provider。

验证：
Docker JDK17 Maven 测试通过：
ClinicalEvidenceDomainModelTest
DeterministicClinicalEvidenceQueryServiceTest
YamlPhase12SourceManifestRepositoryTest
共 10 tests / 0 failures / 0 errors。
```

---

# 六、P12P0-C：PostgreSQL Migration 与持久化

## 任务

新增 Flyway migration：

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

可选：

```text
evidence_conflict_set
evidence_conflict_member
evidence_chunk_embedding
```

实现 JDBC Repository。

## 验收

```text
空库 migration 通过；
从现有 schema 升级通过；
Testcontainers 通过；
唯一约束、外键、GIN 索引存在；
重复 ingestion 幂等；
不修改历史 migration；
不破坏 Phase 5–11 repository tests。
```
## 当前实现状态（2026-07-21）

```text
已新增 Flyway migration：
V12_0_0__phase12_p0_evidence_engine_schema.sql

已创建 P12P0-C 要求的核心表：
evidence_source / evidence_asset_version / evidence_chunk / evidence_span /
evidence_claim / claim_evidence_link / citation_verification_result /
evidence_conflict_set / evidence_conflict_member /
retrieval_trace / retrieval_candidate_trace /
embedding_index_metadata / evidence_chunk_embedding。

已新增最小 JDBC persistence 闭环：
JdbcEvidenceSourceRepository
JdbcEvidenceAssetVersionRepository
JdbcEvidenceRetrievalTraceRepository

验证：
RUN_POSTGRES_TESTS=true mvn -Dtest=JdbcPhase12EvidenceRepositoryTest test
通过，2 tests / 0 failures / 0 errors；Flyway 从空库迁移到 v12.0.0 成功。

边界：
当前仅完成 source / asset lifecycle / retrieval trace 的 JDBC 闭环；
chunk / span / claim / link / citation 的写入管线属于 P12P0-D/E/K 后续接入，表结构已就绪但尚未由 ingestion pipeline 生产数据。
```
---

# 七、P12P0-D：Ingestion / Parse / Chunk / Span

## 任务

```text
D1. 实现 EvidenceIngestionService。
D2. 建立 content_reference allowlist。
D3. 校验 MIME、大小、checksum、路径穿越。
D4. 实现 Markdown / plain text 最小 parser。
D5. 保留 heading / section path。
D6. 建立 structural segmentation。
D7. 生成 Chunk 与 Span。
D8. 生成 locator 与 checksum。
D9. 建立 ingestion trace。
D10. parse 失败资产进入 quarantine / failed，不进入 index。
```

## P0 Chunk 原则

```text
优先按章节、段落和列表项切分；
不先按固定 token 粗暴切碎；
超长 section 再按配置窗口切分；
Span 必须能定位回原始资产版本；
Chunk 是召回单位，Span 是引用单位。
```

## 验收

```text
20 个随机 Chunk 人工核对；
locator 可回溯；
checksum 重放一致；
恶意路径和异常文件被拒绝。
```

## 当前实现状态（2026-07-21）

```text
已新增 EvidenceIngestionService 最小实现：
LocalEvidenceIngestionService
EvidenceIngestionCommand
EvidenceIngestionResult
EvidenceIngestionStatus
MinimalMarkdownEvidenceParser

已完成 P0 ingestion 边界：
仅允许 classpath:evidence/phase12-p0/ 下的 .md / .txt；
拒绝路径穿越、反斜杠路径、非 allowlist、非 md/txt；
校验内容大小与 sha256 checksum；
checksum mismatch 时返回 QUARANTINED，并将 asset lifecycle 更新为 QUARANTINED；
parse / allowlist 失败返回 FAILED，不写入 chunk/span；
成功时按 heading、段落、列表项生成 EvidenceChunk 与 EvidenceSpan；
保留 sectionPath、locator、parser_version、content_reference、chunk/span checksum；
成功 ingestion 后 asset lifecycle 更新为 INGESTED。

已新增 JDBC persistence：
JdbcEvidenceChunkRepository
JdbcEvidenceSpanRepository

验证：
Docker JDK17 Maven：
-Dtest=ClinicalEvidenceDomainModelTest,DeterministicClinicalEvidenceQueryServiceTest,YamlPhase12SourceManifestRepositoryTest,MinimalMarkdownEvidenceParserTest test
通过，12 tests / 0 failures / 0 errors。

PostgreSQL / Testcontainers：
RUN_POSTGRES_TESTS=true mvn -Dtest=JdbcPhase12EvidenceRepositoryTest,JdbcPhase12EvidenceIngestionServiceTest test
通过，5 tests / 0 failures / 0 errors。

边界：
P12P0-D 只生产 chunk/span；
curated claim、claim-span link、citation verification 属于 P12P0-E/K；
lexical/dense index 仍属于 P12P0-F/G。
```

---
# 八、P12P0-E：Curated Claim 与 Claim-Span Link

## 任务

```text
E1. 建立 curated claim YAML / JSON import schema。
E2. 每个 Claim 必须绑定 primary Span。
E3. 支持 population / intervention / comparator / outcome 元数据。
E4. 支持 evidence quality / recommendation strength。
E5. 建立 ClaimReviewStatus。
E6. 建立 ClaimEvidenceLink。
E7. EXTRACTED_CANDIDATE 与 CURATED 分离。
E8. 未审核模型提取 Claim 不进入 Published Corpus。
```

## 验收

```text
20 个 Claim 人工复核；
100% Published Claim 有 Span；
span checksum mismatch 拒绝；
claim checksum 可重放。
```

## 当前实现状态（2026-07-21）

```text
已新增 curated claim import schema：
src/main/resources/evidence/phase12-p0/curated-claims.yml

已新增 Claim 领域字段：
population / intervention / comparator / outcome / evidence_quality /
recommendation_strength / review_status / origin_type / claim_checksum。

已新增 Claim governance enum：
ClaimReviewStatus
ClaimOriginType
EvidenceQuality
RecommendationStrength

已新增 import service：
YamlCuratedClaimImportService
CuratedClaimImportCommand
CuratedClaimImportResult

已新增 JDBC persistence：
JdbcEvidenceClaimRepository
JdbcClaimEvidenceLinkRepository

已完成 P0 claim/link 边界：
每个 curated claim 必须绑定 primary_span_id；
primary span 必须存在；
primary_span_checksum 必须与 EvidenceSpan.spanChecksum 匹配；
claim_checksum 必须可重放；
EXTRACTED_CANDIDATE 不允许作为 APPROVED/PUBLISHED curated claim 导入；
导入后写入 evidence_claim 与 claim_evidence_link。

验证：
Docker JDK17 Maven 非 Postgres 回归：
-Dtest=ClinicalEvidenceDomainModelTest,DeterministicClinicalEvidenceQueryServiceTest,YamlPhase12SourceManifestRepositoryTest,MinimalMarkdownEvidenceParserTest test
通过，12 tests / 0 failures / 0 errors。

PostgreSQL / Testcontainers：
RUN_POSTGRES_TESTS=true mvn -Dtest=JdbcPhase12EvidenceRepositoryTest,JdbcPhase12EvidenceIngestionServiceTest,YamlCuratedClaimImportServiceTest test
通过，8 tests / 0 failures / 0 errors。

边界：
P12P0-E 只完成 curated claim 与 primary span link；
自动模型抽取 claim、citation entailment provider、conflict detection、lexical/dense retrieval 均属于后续任务。
```

---
# 九、P12P0-F：PostgreSQL Lexical Retrieval

## 任务

```text
F1. 建立 search_vector。
F2. 实现 PostgresLexicalEvidenceRetriever。
F3. 支持 lexical query normalization。
F4. 通过 EligibleEvidenceScopePolicy 执行检索前硬过滤。
F5. 支持 source / specialty / jurisdiction / effective time 过滤。
F6. 返回 rank、score、provenance。
F7. 实现 InMemoryLexicalEvidenceRetriever 测试基线。
```

## 验收

```text
真实 tsvector / tsquery；
不以 Java contains 作为正式实现；
未发布、撤销、过期资产不命中；
中文/英文测试语料有明确行为记录。
```

---

# 十、P12P0-G：Embedding Provider 与 Dense Retrieval

## Python 任务

```text
G1. 实现 /v1/providers/embedding。
G2. 返回 provider/model/schema metadata。
G3. 校验 batch、length、dimension、NaN/Infinity。
G4. 加入 health / metadata。
G5. 编写 pytest 与 contract tests。
```

## Java 任务

```text
G6. 实现 EmbeddingProviderClient / Adapter。
G7. 实现 DenseIndexPort。
G8. 实现 pgvector exact search 或评审通过的替代实现。
G9. 记录 embedding model/index version。
G10. 禁止 hash embedding 冒充真实能力。
G11. Dense 检索使用与 Lexical 相同的 EligibleEvidenceScopePolicy。
```

## 验收

```text
真实 embedding；
维度和 Registry 一致；
provider failure 显式；
同一模型版本结果可重放到允许范围；
撤销资产不会被 Dense 检索返回。
```

---

# 十一、P12P0-H：Retrieval Planner、RRF 与 Dedup

## 任务

```text
H1. ClinicalQuestionNormalizer 最小规则实现。
H2. RuleBasedRetrievalPlanner。
H3. ReciprocalRankFusionService。
H4. EvidenceCandidateDeduplicator。
H5. source diversity / max-per-asset 策略。
H6. 参数配置化并进入 Trace。
```

## 验收

```text
BM25 与 cosine 原始分数不直接相加；
RRF rank 计算单元测试；
所有召回通道 provenance 保留；
Lexical / Dense 均使用同一检索前硬过滤 scope；
同一 Span 不重复返回。
```

---

# 十二、P12P0-I：Rerank Provider

## 任务

```text
I1. Python /v1/providers/rerank。
I2. Java EvidenceReranker Adapter。
I3. candidate id round-trip。
I4. provider metadata validation。
I5. timeout / invalid schema / duplicate id 处理。
I6. rerank disabled baseline。
```

## 验收

```text
真实 cross-encoder 或评审通过的真实 reranker；
不使用 token overlap 冒充最终实现；
reranker 不修改文本和来源；
故障进入 DEGRADED_NO_RERANK。
```

---

# 十三、P12P0-J：Authority / Freshness / Applicability

## 任务

```text
J1. SourceAuthorityPolicy。
J2. EvidenceFreshnessEvaluator。
J3. EvidenceApplicabilityEvaluator。
J4. reason code 与 warning。
J5. UNKNOWN 与 CURRENT 分离。
J6. post-rerank quality gate 与 Trace。
```

## 验收

```text
来源级别来自 Registry，不由模型擅自提升；
EXPIRED / SUPERSEDED 默认拒绝；
MISMATCH applicability 默认拒绝；
UNKNOWN 明确告警；
所有拒绝原因进入 Trace。
```

---

# 十四、P12P0-K：Citation Entailment

## 任务

```text
K1. 结构与 checksum 校验。
K2. 数值、否定、比较方向规则校验。
K3. Python /v1/providers/citation-entailment。
K4. Java CitationVerifier Adapter。
K5. CitationVerificationResult 持久化。
K6. threshold 配置与 Evaluation 校准。
K7. provider unavailable fail-closed。
```

## 验收

```text
未验证 Claim 不进入 accepted；
支持/部分支持/矛盾/无关可区分；
Provider metadata 完整；
不保存自由文本 chain-of-thought；
unsupported citation rate 达标。
```

---

# 十五、P12P0-L：Conflict 与 EvidenceValidation

## 任务

```text
L1. 最小 ConflictType。
L2. EvidenceConflictDetectionService。
L3. ClinicalEvidenceValidationService。
L4. accepted / rejected / review required 分类。
L5. critical conflict 进入 REVIEW_REQUIRED。
L6. provenance completeness validator。
```

## 验收

```text
矛盾不会被单一高相似度覆盖；
关键冲突不自动选择唯一答案；
所有 accepted item 通过全部硬约束；
degraded / review required 状态正确。
```

---

# 十六、P12P0-M：Result、Trace 与 Runtime Adapter

## 任务

```text
M1. ClinicalEvidenceRetrievalResult。
M2. AcceptedEvidenceItem / RejectedEvidenceItem。
M3. EvidenceRetrievalTrace。
M4. Retrieval stage latency。
M5. RuntimeEvidenceGraphAdapter。
M6. RuntimeEvidenceGraphPatch。
M7. legacy / shadow / active Feature Flag。
M8. Audit / Evaluation Hook。
M9. ClinicalEvidenceProviderAdapter 映射到既有 Phase 7 EvidenceRetrievalResult。
```

## 验收

```text
Adapter 不直接修改 RuntimeState；
只有 validation accepted 形成 patch；
shadow 不影响 PatientOutput；
active debug 仍经过 DecisionBoundary；
同一版本组合可重放。
```

---

# 十七、P12P0-N：Debug / Governance API

## 任务

```text
N1. SourceController。
N2. EvidenceAssetController。
N3. EvidenceIngestionController。
N4. EvidenceQueryController。
N5. RetrievalTraceController。
N6. DebugRole / token / audit。
N7. Safe Error DTO。
N8. 分页与过滤。
N9. request_id 幂等与 expected_version 并发控制。
```

## 验收

```text
无权限 403；
路径穿越拒绝；
API 不返回 full document / prompt / secret；
Patient / Clinician client 不依赖 Debug API；
OpenAPI 或等价契约与文档一致。
```

---

# 十八、P12P0-O：Offline Evaluation

## 任务

```text
O1. phase12-p0-evidence-cases.yaml。
O2. 不少于 40 个 Case。
O3. Retrieval Scorer。
O4. Citation Scorer。
O5. Freshness / License / Applicability Scorer。
O6. Conflict Scorer。
O7. Provenance Scorer。
O8. Latency / degradation Scorer。
O9. Evaluation Report。
```

## 冻结阈值

```text
Recall@10 >= 0.85
MRR@10 >= 0.70
nDCG@10 >= 0.75
context_precision@10 >= 0.65
citation_precision >= 0.90
unsupported_citation_rate <= 0.05
contradiction_detection_recall >= 0.80
stale / revoked / unlicensed activation = 0
critical applicability error = 0
provenance completeness = 1.00
```

阈值未达到时不得通过减少困难 Case 或删除失败样本伪造通过。

---

# 十九、P12P0-P：故障、安全与回归

## 故障注入

```text
DB unavailable
pgvector unavailable
Embedding timeout / dimension drift
Reranker invalid schema
Citation timeout / contradiction
checksum corruption
license revoked
asset superseded during query
empty corpus
all candidates rejected
trace persistence failure
```

## 安全

```text
source text prompt injection
HTML / script content
oversized file
path traversal
malformed encoding
sensitive field serialization
DebugRole bypass
```

## 回归

```text
Phase 1 Runtime
Phase 3 Evaluation
Phase 5 persistence/audit
Phase 7 baseline evidence
Phase 8 Python Provider
Phase 10 Governance Console
Phase 11 Patient / Clinician projection
```

---

# 二十、P12P0-Q：Shadow / Active 集成验收

## Shadow

```text
真实 Engine 执行；
结果进入 Trace / Evaluation；
不改变现有 Runtime EvidenceGraph / PatientOutput；
对比 legacy baseline。
```

## Active Debug

```text
真实 Engine 结果经 EvidenceValidation；
生成 RuntimeEvidenceGraphPatch；
由 Runtime 提交；
Clinician Evidence Panel 可查看安全摘要与引用；
Patient Portal 不显示 raw evidence；
所有阶段和 Provider 版本可追踪。
```

---

# 二十一、P12P0-R：人工验收与冻结

## 必须生成

```text
Phase12_P0人工测试结果.md
Phase12_P0冻结记录.md
Source Manifest
License Review Record
Offline Evaluation Report
Provider Metadata Snapshot
Index Version Snapshot
Migration Result
Failure Injection Result
```

## 文档同步

```text
README.md
docs/0-项目入口/00_项目设计地图.md
docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
docs/1-总设计/ClinMindRuntime技术实现总方案.md
docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md
```

## 冻结后

```text
Phase 12-P0 只允许修复回归；
不再扩展 Deep Evidence / GraphRAG；
下一步先设计 Phase 12-P1；
不得直接跳到 Phase 13 或 Multi-Agent。
```

---

# 二十二、推荐提交顺序

```text
1. source manifest + domain model
2. migrations + repositories
3. ingestion + chunk/span
4. curated claim import
5. lexical retrieval
6. embedding + dense index
7. RRF + dedup
8. rerank
9. authority/freshness/applicability
10. citation entailment
11. conflict + validation
12. result/trace/runtime adapter
13. debug API
14. evaluation（复用 /api/v1/debug/evaluations/**）
15. failure/security/regression
16. shadow/active integration
17. manual validation + freeze docs
```

禁止把以上任务压缩成一个无法审阅的大提交。
