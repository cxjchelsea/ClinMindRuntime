# Phase 7-P0 人工测试结果

> 测试日期：2026-07-02  
> 测试范围：RAG EvidenceProvider MVP  
> 后端测试：`mvn test` 全绿（431 tests，23 skipped）

---

## 一、自动化验收

| 场景 | 测试类 | 结果 |
|---|---|---|
| Corpus 加载（9 chunks，三组 symptom_group） | `EvidenceCorpusRepositoryTest` | 通过 |
| Policy 白名单 / unknown 拒绝 | `EvidenceProviderPolicyTest` | 通过 |
| RagProvider 胸痛召回 / unknown 无伪造 | `RagEvidenceProviderTest` | 通过 |
| Validation 合法通过 / 缺字段拒绝 / 禁止 role | `EvidenceValidationServiceTest` | 通过 |
| Debug API 鉴权 / 检索 | `EvidenceDebugControllerTest` | 通过 |
| 端到端：胸痛 + 发热 + unknown symptom_group | `Phase7P0AcceptanceIntegrationTest` | 通过 |
| Phase 1–6 回归 | 既有测试套件 | 通过 |

---

## 二、人工场景核对

### 1. 高风险胸痛（chest_pain）

- Runtime 启动后 `EvidenceRetrievalSnapshot.acceptedCandidates` 非空。
- `EvidenceGraph.items[].evidenceRefs` 含 source_id / chunk_id / use_case。
- `PatientOutput` 不含 `retrieval_score` 或 RAG 内部字段。
- Debug API `POST /api/v1/debug/evidence/retrieve` 返回 `SUCCESS` 与 evidence_candidates。

### 2. 普通发热（fever）

- Runtime 正常完成，未进入 ERROR_SAFE_HALTED。
- Evidence retrieval 状态为 SUCCESS 或 NO_EVIDENCE_FOUND（不伪造）。
- Debug API `GET /api/v1/debug/evidence/corpus` 返回 chunk_count > 0。

### 3. unknown symptom_group

- Debug API 返回 `POLICY_REJECTED`，evidence_candidates 为空。
- 不伪造证据 chunk。

---

## 三、边界确认

- RAG 仅输出 EvidenceCandidate / EvidenceRef，不生成 PatientOutput。
- Evaluation Scorer 使用 `evidence_eval` 标签门控，不影响 Phase 1–6 默认 case。
- AgentOrchestrationSnapshot 与 EvidenceRetrievalSnapshot 独立存储，互不覆盖。
- SafetyGate fail-safe 时跳过 evidence retrieval（orchestrator 返回 SKIPPED）。

---

## 四、已知限制（P0 预期内）

- 检索为 keyword / rule-based，非向量检索。
- 无 GraphRAG、Neo4j、Milvus、LLM reranker。
- Corpus 为 synthetic 测试数据，非真实临床指南。
- 前端 console-web 未针对 P7 新增 UI（仅后端 Debug API）。
