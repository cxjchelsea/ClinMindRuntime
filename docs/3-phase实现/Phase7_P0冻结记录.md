# Phase 7-P0 冻结记录：RAG EvidenceProvider MVP

> 冻结日期：2026-07-02  
> 上位规格：`Phase7_P0RAG_EvidenceProvider_实现规格.md`  
> 任务清单：`Phase7_P0开发任务清单.md`

---

## 一、冻结结论

Phase 7-P0 **RAG EvidenceProvider MVP** 已实现并通过 `mvn test` 验收，现冻结。

核心命题已验证：

```text
RAG 提供 EvidenceCandidate / EvidenceRef，
经 EvidenceValidationService 校验后进入 EvidenceGraph，
不直接生成 PatientOutput。
```

---

## 二、已完成（P7-A ~ P7-K）

| 编号 | 内容 | 状态 |
|---|---|---|
| P7-A | Evidence domain（EvidenceRef、EvidenceCandidate、EvidenceRetrieval* 等） | 已完成 |
| P7-B | `evidence/phase7-default/evidence_chunks.yml` + YamlEvidenceCorpusRepository | 已完成 |
| P7-C | EvidenceProviderPolicy（symptom_group 白名单、corpus fail-closed） | 已完成 |
| P7-D | RagEvidenceProvider（keyword/rule-based MVP） | 已完成 |
| P7-E | EvidenceValidationService（ACCEPTED/REJECTED/PARTIALLY_ACCEPTED） | 已完成 |
| P7-F | EvidenceGraphItem.evidenceRefs + EvidenceCandidateToGraphMapper | 已完成 |
| P7-G | EvidenceCapabilityOrchestrator + RuntimeService 集成 | 已完成 |
| P7-H | Debug API `/api/v1/debug/evidence/**` | 已完成 |
| P7-I | AuditActionType / Trace（EvidenceRetrievalTrace） | 已完成 |
| P7-J | 4 个 Evaluation Scorer（`evidence_eval` 门控） | 已完成 |
| P7-K | 测试 + 人工测试结果 + 本冻结记录 | 已完成 |

---

## 三、主要代码位置

```text
src/main/java/com/clinmind/runtime/evidence/
src/main/resources/evidence/phase7-default/evidence_chunks.yml
src/test/java/com/clinmind/runtime/evidence/
```

Runtime 集成点：

- `RuntimeService.runClinicalPipeline()`：SafetyGate 后调用 Agent + Evidence orchestration
- `RuntimeState.evidenceRetrieval`：EvidenceRetrievalSnapshot
- `EvidenceGraphService`：合并 accepted RAG 证据到 EvidenceGraph

Debug API：

- `POST /api/v1/debug/evidence/retrieve`
- `GET /api/v1/debug/evidence/retrievals/{retrieval_id}`
- `GET /api/v1/debug/evidence/corpus`

Provider ID：`rag_evidence_provider` v`0.7.0-p0`

---

## 四、P0 明确未做（后置）

```text
GraphRAG / KG-lite 正式图谱
Neo4j / Milvus / pgvector
Embedding / LLM reranker
真实大规模医学指南入库
Knowledge Console 审核发布流程
MCP / Tool / Skills
patient_direct_answer use_case
```

---

## 五、后置任务（Phase 7-P1+）

1. KG-lite node / edge
2. GraphRAG Provider prototype
3. Phase 8：EmbeddingProvider / Python AI Provider
4. Phase 10：Knowledge Console 知识审核与发布

---

## 六、回归说明

- Phase 6 Agent orchestration 行为保持不变。
- 新增 Evaluation Scorer 仅对 `evidence_eval` 标签 case 生效。
- 全量 `mvn test` 通过。
