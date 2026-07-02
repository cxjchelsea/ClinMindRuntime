# Phase 7-P1 人工测试结果

> 测试日期：2026-07-02  
> 测试范围：KG-lite / Graph Evidence 原型  
> 后端测试：`mvn test` 全绿

---

## 一、自动化验收

| 场景 | 测试类 | 结果 |
|---|---|---|
| KG-lite graph 加载 | `KgLiteGraphRepositoryTest` | 通过 |
| Graph Policy | `GraphEvidencePolicyTest` | 通过 |
| KgLiteGraphEvidenceProvider | `KgLiteGraphEvidenceProviderTest` | 通过 |
| Graph Validation | `GraphEvidenceValidationServiceTest` | 通过 |
| Debug API | `GraphEvidenceDebugControllerTest` | 通过 |
| 端到端：胸痛 + 发热 + 无 evidence ref | `Phase7P1AcceptanceIntegrationTest` | 通过 |
| Phase 1–7 P0 回归 | 既有测试套件 | 通过 |

---

## 二、人工场景核对

### 1. 高风险胸痛（chest_pain）

- Runtime 启动后 `GraphEvidenceSnapshot.acceptedCandidates` 非空。
- `EvidenceGraph.items[].graphPaths` / `relationSummaries` 含图关系摘要。
- `PatientOutput` 不含 `path_score` / `[GRAPH]` 内部字段。
- Debug API `POST /api/v1/debug/graph-evidence/run` 返回 `SUCCESS`。

### 2. 普通发热（fever）

- Runtime 正常完成。
- Graph evidence 为 SUCCESS / SKIPPED / NO_GRAPH_PATH_FOUND（不伪造 path）。

### 3. 无 accepted EvidenceRef

- Debug API 空 `accepted_evidence_refs` 返回 400。
- Policy 拒绝无 accepted evidence 的 graph run。

---

## 三、边界确认

- Graph 证据仅增强 EvidenceGraph / ClinicianReport，不直接生成 PatientOutput。
- DDxCandidate 状态不被 graph provider 直接改为 confirmed。
- Evaluation Scorer 使用 `graph_evidence_eval` 标签门控。
- Agent / Evidence / Graph 三个 snapshot 独立存储。

---

## 四、P1 预期内限制

- 使用 YAML in-memory KG-lite，非 Neo4j / 向量库。
- deterministic BFS path expansion，非 LLM GraphRAG。
- synthetic 测试图数据，非真实临床知识图谱。
