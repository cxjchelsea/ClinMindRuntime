# Phase 7-P1 冻结记录：KG-lite / Graph Evidence 原型

> 冻结日期：2026-07-02  
> 上位规格：`Phase7_P1KG-lite与GraphRAG原型_实现规格.md`  
> 任务清单：`Phase7_P1开发任务清单.md`

---

## 一、冻结结论

Phase 7-P1 **KG-lite / Graph Evidence 原型** 已实现并通过 `mvn test` 验收，现冻结。

核心命题已验证：

```text
EvidenceRef 可映射到 KG-lite graph node；
GraphEvidenceCandidate 经 validation 进入 EvidenceGraph；
图关系不直接生成 PatientOutput，不替代诊断判断。
```

---

## 二、已完成（P7P1-A ~ P7P1-K）

| 编号 | 内容 | 状态 |
|---|---|---|
| P7P1-A | Graph domain（GraphNode/Edge/Path、GraphEvidence*） | 已完成 |
| P7P1-B | `kg_lite_graph.yml` + YamlKgLiteGraphRepository | 已完成 |
| P7P1-C | GraphEvidencePolicy | 已完成 |
| P7P1-D | KgLiteGraphEvidenceProvider（deterministic path expansion） | 已完成 |
| P7P1-E | GraphEvidenceValidationService | 已完成 |
| P7P1-F | EvidenceGraphItem graphRefs/graphPaths/relationSummaries | 已完成 |
| P7P1-G | GraphEvidenceCapabilityOrchestrator + RuntimeService 集成 | 已完成 |
| P7P1-H | Debug API `/api/v1/debug/graph-evidence/**` | 已完成 |
| P7P1-I | AuditActionType / GraphEvidenceTrace | 已完成 |
| P7P1-J | 4 个 Evaluation Scorer（`graph_evidence_eval` 门控） | 已完成 |
| P7P1-K | 测试 + 人工测试结果 + 本冻结记录 | 已完成 |

---

## 三、主要代码位置

```text
src/main/java/com/clinmind/runtime/evidence/graph/
src/main/resources/evidence/phase7-default/kg_lite_graph.yml
src/test/java/com/clinmind/runtime/evidence/graph/
```

Runtime 集成顺序：

```text
SafetyGate → Agent → Evidence Retrieval → DDx → Graph Evidence → EvidenceGraph
```

Provider ID：`kg_lite_graph_evidence_provider` v`0.7.1-p1`

---

## 四、P1 明确未做

```text
Neo4j / Milvus / pgvector
LLM GraphRAG 问答
自动知识抽取入图
Knowledge Console 审核发布
PatientOutput 展示 graph path/score
```

---

## 五、后置任务

1. Phase 8-P0：EmbeddingProvider / Python AI Provider  
2. Phase 10：Knowledge Console  
3. 生产级 graph store（Neo4j 等）后置专项

---

## 六、回归说明

- Phase 7-P0 RagEvidenceProvider 行为保持不变。
- Phase 6 Agent orchestration 不受影响。
- 全量 `mvn test` 通过。
