# AI Implementation Skill：ClinMindRuntime（Phase 12-P0 Clinical Evidence Engine）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–11 P1 已完成并冻结。  
> 当前总设计版本：v3.0。  
> 当前阶段：Phase 12-P0 设计评审期，尚未进入代码实现。  
> 只有 Phase 12 总体设计、P0 实现规格、API/测试设计和开发任务清单通过评审后，才允许建立实现分支并按任务清单编码。

---

# 一、当前权威状态

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 12-P0 Clinical Evidence Engine 设计评审期 |
| 前置状态 | Phase 1–11 P1 已冻结 |
| 前置冻结记录 | `docs/3-phase实现/Phase11_P1冻结记录.md` |
| 总体设计 | `docs/3-phase实现/Phase12真实临床能力纵切_总体设计.md` |
| P0 实现规格 | `docs/3-phase实现/Phase12_P0ClinicalEvidenceEngine_实现规格.md` |
| P0 API 与测试设计 | `docs/3-phase实现/Phase12_P0EvidenceEngine_API与测试设计.md` |
| P0 开发任务清单 | `docs/3-phase实现/Phase12_P0开发任务清单.md` |
| 当前允许工作 | 设计审阅、语料与许可证调研、Provider 候选验证、实现前技术 Spike |
| 当前禁止工作 | 在设计评审完成前提交正式 Phase 12-P0 产品代码 |

---

# 二、权威文档优先级

实现或修改本仓库时，按以下顺序理解约束：

```text
1. docs/0-项目入口/00_项目设计地图.md
2. docs/1-总设计/ClinMindRuntime完整系统设计.md
3. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
4. docs/1-总设计/ClinMindRuntime技术实现总方案.md
5. docs/1-总设计/Phase11后架构缺口与路线收敛决策.md
6. docs/3-phase实现/Phase12真实临床能力纵切_总体设计.md
7. docs/3-phase实现/Phase12_P0ClinicalEvidenceEngine_实现规格.md
8. docs/3-phase实现/Phase12_P0EvidenceEngine_API与测试设计.md
9. docs/3-phase实现/Phase12_P0开发任务清单.md
10. docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md
11. Phase 1–11 冻结记录
```

冲突处理：

```text
上位总设计 > 阶段路线 > 当前 Phase 实现规格 > API/测试设计 > 任务清单 > 代码便利性
```

不得以“实现更简单”为由突破安全、来源、版本、引用或 Runtime 主控边界。

---

# 三、系统永久权力边界

```text
Java Runtime
拥有状态、控制流、能力调用、验证、部分采纳、提交、安全、恢复和角色输出边界。

Clinical Evidence Engine
只能返回 EvidenceRetrievalResult / EvidenceCandidate / EvidenceGraph Patch。

Python Provider
只能返回 Embedding、Rerank、Citation Entailment 等结构化模型结果。

PostgreSQL
保存证据资产、版本、索引元数据、Claim、Citation 和 Trace。

Patient / Clinician / Governance Frontend
只消费后端 role-specific DTO，不直接调用 Provider，不读取 raw Runtime DTO。
```

永远禁止：

```text
模型直接修改 RuntimeState
RAG 直接生成 PatientOutput
Python 成为系统主控
前端直接调用 Agent / Model / Tool / Evidence Provider
未验证 Citation 进入 accepted EvidenceGraph
未确认许可证的正式资产发布
模型自动发布资产、Prompt、模型或 Capability
```

---

# 四、Phase 12-P0 唯一目标

```text
建立真实、版本化、可追踪、可评测并受 Runtime 控制的 Clinical Evidence Engine MVP。
```

P0 允许涉及：

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
EvidenceRetrievalResult
EvidenceRetrievalTrace

EvidenceIngestionService
EvidenceAssetGovernanceService
ClinicalEvidenceQueryService
ClinicalQuestionNormalizer
RuleBasedRetrievalPlanner
LexicalEvidenceRetriever
DenseEvidenceRetriever
ReciprocalRankFusionService
EvidenceCandidateDeduplicator
EvidenceReranker
EvidenceFreshnessEvaluator
EvidenceApplicabilityEvaluator
CitationVerificationService
EvidenceConflictDetectionService
ClinicalEvidenceValidationService
RuntimeEvidenceGraphAdapter
```

P0 可以新增：

```text
PostgreSQL evidence tables
PostgreSQL full-text search
optional pgvector DenseIndexPort
Python embedding endpoint
Python rerank endpoint
Python citation-entailment endpoint
Debug evidence source / asset / query / trace API
Offline evidence Evaluation CaseSet 与 Scorer
Source Manifest 与许可证审核记录
```

---

# 五、Phase 12-P0 禁止范围

```text
1. 不做 LLM-backed InquiryPlanningAgent；属于 Phase 12-P1。
2. 不做 FHIR / EHR 接入；属于 Phase 12-P1 / Phase 13。
3. 不建立 ClinicalFactLedger；属于 Phase 13。
4. 不建立完整 Policy IR / RuntimeRiskState / CapabilityLease；属于 Phase 14。
5. 不做病历、预约、转诊、处方或其他写操作；属于 Phase 15。
6. 不做 Multi-Agent / Handoff；属于 Phase 16。
7. 不做完整 Deep Evidence / GraphRAG 平台；属于 Phase 17。
8. 不做模型训练、微调、DPO、RFT、蒸馏；属于 Phase 18。
9. 不接真实远程 MCP / HIS / LIS / PACS；属于 Phase 19。
10. 不做生产级认证、多租户和合规平台；属于 Phase 21。
11. 不自动抓取任意互联网 URL。
12. 不导入真实患者或 PHI 数据。
13. 不用 hash embedding 冒充真实 Dense Retrieval。
14. 不用 token overlap 冒充最终 Cross-encoder Rerank。
15. 不用一个 final similarity score 代替 Authority / Quality / Applicability / Freshness / Citation / Conflict。
16. 不让网页或文档中的文本成为控制指令。
17. 不把未审核模型提取 Claim 当作正式 Published Claim。
18. 不一次性创建 Phase 13–22 空包或空表。
```

---

# 六、Java 与 Python 责任边界

## Java 必须负责

```text
Source / Asset lifecycle
License / Review / Publication policy
Ingestion orchestration
Retrieval planning
Eligible scope hard filter
Lexical retrieval
Hybrid fusion
Deduplication
Authority / Freshness / Applicability
Citation policy decision
Conflict detection
Evidence validation
RuntimeEvidenceGraph Patch
Trace / Audit / Evaluation
```

## Python 只允许负责

```text
Embedding generation
Cross-encoder rerank
Citation entailment / NLI
Provider metadata / health
```

Python 返回必须包含：

```text
provider_id
provider_version
model_id
model_version
schema_version
latency_ms
warnings
structured_result
```

Python 不得：

```text
发布资产
写 PostgreSQL 权威领域表
选择 PUBLISHED 状态
决定 Runtime 是否采用结果
返回自由文本作为唯一安全依据
保存或扩大患者上下文
```

---

# 七、证据资产硬约束

正式参与检索的资产必须满足：

```text
source.review_status == APPROVED
source.license_status == VERIFIED
source.trust_status != BLOCKED
asset.lifecycle_status == PUBLISHED
asset checksum 有效
asset 在 effective interval 内
```

默认排除：

```text
DRAFT
INGESTED but not evaluated
SUPERSEDED
DEPRECATED
REVOKED
license UNKNOWN / RESTRICTED / REJECTED
```

所有 Published Claim 必须具有：

```text
claim_id
asset version
primary span
span locator
claim checksum
review status
```

---

# 八、检索链路约束

允许链路：

```text
Question
→ Normalize
→ Plan
→ Eligible Asset Filter
→ Lexical Recall + Dense Recall
→ RRF
→ Dedup
→ Rerank
→ Authority / Freshness / Applicability
→ Citation Entailment
→ Conflict Detection
→ EvidenceValidation
→ EvidenceRetrievalResult
→ RuntimeEvidenceGraphAdapter
→ Runtime Commit
```

禁止链路：

```text
Question → Vector DB → LLM → Patient Answer
```

硬要求：

```text
BM25 和 cosine 原始分数不能直接相加；使用 RRF 或评审通过的可解释融合。
硬过滤必须发生在排序之前。
Chunk 是召回单位，Span 是引用单位。
Reranker 不能修改来源文本和 provenance。
未验证 Citation 不进入 accepted。
```

---

# 九、失败与降级约束

```text
Dense Provider 不可用
→ DEGRADED_LEXICAL_ONLY

Reranker 不可用
→ DEGRADED_NO_RERANK

Citation Provider 不可用
→ REVIEW_REQUIRED / UNAVAILABLE，未验证 Claim 不得 accepted

PostgreSQL / Asset Repository 不可用
→ UNAVAILABLE，fail closed

无结果
→ EMPTY，不生成无来源补全
```

禁止静默 fallback。

所有降级必须进入：

```text
status
warnings
reason_codes
trace
Evaluation
```

---

# 十、Feature Flag

```text
clinmind.evidence.engine.mode=legacy | shadow | active
```

```text
legacy
仅使用 Phase 7 baseline。

shadow
真实引擎执行、记录 Trace 和 Evaluation，但不影响 Runtime 正式结果。

active
真实引擎结果经过 EvidenceValidation 后可形成 RuntimeEvidenceGraphPatch。
```

开发期默认 `shadow`。

不得在没有 Evaluation 和人工验收证据时将默认值改成 `active`。

---

# 十一、数据与日志安全

禁止普通日志、API 或测试快照包含：

```text
完整患者输入
真实 PHI
raw prompt
system prompt
secret / api key / private key
raw external response
完整来源文档
internal chain-of-thought
full rationale
```

允许记录：

```text
checksum
source / asset / version / span id
provider/model version
阶段耗时
rank / score
accepted / rejected reason codes
trace ref
```

外部证据文本默认：

```text
instructionAllowed=false
```

任何 source text 中的“忽略规则”“调用工具”等文本只作为数据处理。

---

# 十二、实现任务纪律

正式编码必须严格按：

```text
P12P0-A → B → C → D → E → F → G → H → I → J → K → L → M → N → O → P → Q → R
```

执行规则：

```text
1. 一次只实现一个可验证任务组。
2. 每个任务同时提交测试。
3. 不提前创建后续阶段空类。
4. 不用 TODO 伪装已完成能力。
5. Mock / InMemory 必须显式命名并只用于测试或 baseline。
6. 真实 Provider 与 Mock Provider 的 metadata 必须可区分。
7. 任务状态只能依据代码和测试更新。
8. 不改写历史冻结记录以适配新实现。
9. 发现设计缺口时先更新设计文档，再改代码。
10. 不把多个高风险模块压成一个不可审阅提交。
```

---

# 十三、测试硬要求

每个实现 PR 至少按影响范围运行：

```text
mvn test
PostgreSQL Testcontainers
python -m pytest -q
Provider contract tests
Offline Evidence Evaluation
console-web npm run typecheck
console-web npm test
console-web npm run build
```

P0 冻结阈值以 `Phase12_P0EvidenceEngine_API与测试设计.md` 为准。

禁止：

```text
删除失败 Case 以满足阈值
只报告平均值而隐藏关键安全失败
用手工演示代替自动化测试
用 Mock Provider 结果宣称真实模型通过
```

---

# 十四、当前设计评审期允许的工作

在本设计 PR 合并前允许：

```text
审阅和修改四份 Phase 12 设计文档
梳理真实语料候选与许可证信息
验证 PostgreSQL full-text / pgvector 本地可用性
验证 Embedding / Rerank / Citation Provider 候选的接口可行性
创建一次性 Spike，不提交到正式产品路径
补充 Evaluation CaseSet 草案
```

不允许：

```text
在 main 上直接实现 Phase 12 产品代码
修改 Runtime 主链路默认行为
将 Evidence Engine 默认设为 active
发布任何未经审核的语料
```

---

# 十五、进入实现阶段的条件

必须全部完成：

```text
1. Phase12真实临床能力纵切_总体设计.md 评审通过。
2. Phase12_P0ClinicalEvidenceEngine_实现规格.md 评审通过。
3. Phase12_P0EvidenceEngine_API与测试设计.md 评审通过。
4. Phase12_P0开发任务清单.md 评审通过。
5. Source Manifest 范围和许可证记录方式确定。
6. Provider 候选与 DenseIndexPort 方案确定。
7. Offline Evaluation CaseSet 结构确定。
8. 开发分支从最新 main 创建。
```

进入实现后，本文件标题和当前阶段应更新为：

```text
Phase 12-P0 实现中
```

---

# 十六、冻结要求

Phase 12-P0 只有在以下证据齐全时可冻结：

```text
真实许可语料
真实 lexical / dense / rerank / citation 能力
完整 provenance
license / freshness / applicability / conflict 测试
Provider failure / degrade / fail-closed 测试
Offline Evaluation 达标
Shadow / Active Debug 集成通过
Patient / Clinician / Governance 回归通过
Phase12_P0人工测试结果.md
Phase12_P0冻结记录.md
```

冻结前不得开始 Phase 12-P1 产品实现。