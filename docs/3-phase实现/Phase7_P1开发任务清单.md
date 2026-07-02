# Phase 7-P1 开发任务清单：KG-lite 与 GraphRAG 原型

> 上位实现规格：`docs/3-phase实现/Phase7_P1KG-lite与GraphRAG原型_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase7_P1GraphEvidence_API与测试设计.md`  
> 当前目标：按最小闭环实现 KG-lite / Graph Evidence 原型，让 EvidenceRef 进入轻量图关系，不引入完整 GraphRAG 平台。

---

# 一、Phase 7-P1 总目标

Phase 7-P1 要完成的不是完整知识图谱系统，而是一个最小闭环：

```text
EvidenceRef
→ KG-lite GraphNode / GraphEdge
→ GraphEvidenceProvider
→ GraphEvidenceCandidate
→ GraphEvidenceValidation
→ EvidenceGraph
→ DecisionBoundary
→ RuntimeTrace / Audit / Evaluation
```

最终要证明：

```text
Phase 7-P0 的 EvidenceRef 可以被映射到轻量图关系；
图关系可以增强 EvidenceGraph 和医生端解释；
图关系不能直接改变诊断结论；
图关系不能直接暴露给患者端。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P7P1-A | 建立 KG-lite domain 基础对象 | 待做 |
| P7P1-B | 建立 KG-lite graph resource 与加载器 | 待做 |
| P7P1-C | 建立 GraphEvidencePolicy | 待做 |
| P7P1-D | 实现 GraphEvidenceProvider MVP | 待做 |
| P7P1-E | 实现 GraphEvidenceValidationService | 待做 |
| P7P1-F | EvidenceGraph 图关系集成 | 待做 |
| P7P1-G | Capability Orchestration 接入 graph evidence | 待做 |
| P7P1-H | Graph Evidence Debug API | 待做 |
| P7P1-I | Trace / Audit 接入 | 待做 |
| P7P1-J | Graph Evidence Evaluation Scorer | 待做 |
| P7P1-K | 测试、人工验证与冻结记录 | 待做 |

---

# 三、P7P1-A：建立 KG-lite domain 基础对象

## 目标

新增 KG-lite 与 GraphEvidence 的核心数据结构。

## 建议包路径

```text
src/main/java/.../evidence/graph/
src/main/java/.../evidence/graph/kg/
src/main/java/.../evidence/graph/api/
src/main/java/.../evidence/graph/validation/
```

## 任务

```text
[ ] 新增 GraphNode。
[ ] 新增 GraphNodeType。
[ ] 新增 GraphEdge。
[ ] 新增 GraphRelationType。
[ ] 新增 GraphPath。
[ ] 新增 KgLiteGraph。
[ ] 新增 GraphEvidenceRequest。
[ ] 新增 GraphEvidenceResult。
[ ] 新增 GraphEvidenceCandidate。
[ ] 新增 GraphEvidenceStatus。
[ ] 新增 GraphEvidenceTrace。
[ ] 新增 GraphEvidenceValidationResult。
[ ] 新增 GraphEvidenceSnapshot。
```

## 验收标准

```text
[ ] GraphNode 包含 node_id / node_type / name / version。
[ ] GraphEdge 包含 from_node_id / to_node_id / relation_type / version。
[ ] GraphPath 包含 node_ids / edge_ids / path_score。
[ ] GraphEvidenceCandidate 不包含 PatientOutput。
[ ] GraphEvidenceResult 不包含 raw patient dialogue。
```

---

# 四、P7P1-B：建立 KG-lite graph resource 与加载器

## 目标

建立最小 KG-lite graph，支持 deterministic graph path expansion。

## 任务

```text
[ ] 新增 kg_lite_graph.yml。
[ ] 覆盖 chest_pain / fever / abdominal_pain。
[ ] 每个 symptom_group 至少包含 symptom / risk_signal / diagnosis / test / evidence nodes。
[ ] 每个 symptom_group 至少包含 associated_with / red_flag_for / suggests_test / evidence_for edges。
[ ] 新增 KgLiteGraphRepository。
[ ] 新增 YamlKgLiteGraphRepository 或 ResourceKgLiteGraphRepository。
[ ] 加载时校验 edge 的 from / to node 是否存在。
[ ] 加载时校验 relation_type 是否在允许集合。
```

## 验收标准

```text
[ ] graph 可以正常加载。
[ ] node_count > 0。
[ ] edge_count > 0。
[ ] 支持 symptom_group 查询。
[ ] 无效 edge 被拒绝或记录 warning。
[ ] 不使用真实敏感病例。
```

---

# 五、P7P1-C：建立 GraphEvidencePolicy

## 目标

控制何时允许 graph evidence。

## 任务

```text
[ ] 新增 GraphEvidencePolicy。
[ ] accepted_evidence_candidates 为空时拒绝。
[ ] graph resource 不可用时 fail-closed。
[ ] unsupported symptom_group 拒绝。
[ ] max_path_depth 超限时降级到最大值。
[ ] SafetyGate fail-safe 时跳过 graph evidence。
```

## 验收标准

```text
[ ] 有 accepted EvidenceRef 时允许。
[ ] 没有 accepted EvidenceRef 时拒绝或 SKIPPED。
[ ] unknown symptom_group 不伪造 path。
[ ] graph unavailable 不静默成功。
[ ] Policy 拒绝有 reasons。
```

---

# 六、P7P1-D：实现 GraphEvidenceProvider MVP

## 目标

实现 deterministic KG-lite path expansion。

## 任务

```text
[ ] 新增 GraphEvidenceProvider 接口。
[ ] 新增 KgLiteGraphEvidenceProvider。
[ ] 根据 EvidenceRef.chunk_id 匹配 EVIDENCE node。
[ ] 根据 symptom_group 匹配 SYMPTOM / RISK_SIGNAL node。
[ ] 从 evidence / symptom / risk_signal node 扩展到 diagnosis / test / question slot。
[ ] 生成 GraphPath。
[ ] 计算 path_score。
[ ] 生成 GraphEvidenceCandidate。
[ ] 限制 max_path_depth / max_path_count。
[ ] 返回 GraphEvidenceResult。
```

## P1 图路径策略

```text
1. evidence node → diagnosis node 优先。
2. risk_signal node → diagnosis node 加权。
3. diagnosis node → test node 可作为医生端建议候选。
4. symptom node → diagnosis node 作为弱支持关系。
5. 不生成超过 3 跳的路径。
```

## 验收标准

```text
[ ] chest_pain EvidenceRef 能找到 graph path。
[ ] fever EvidenceRef 不返回 chest_pain path。
[ ] unknown EvidenceRef 返回 NO_GRAPH_PATH_FOUND。
[ ] path_score 稳定。
[ ] 返回 GraphEvidenceCandidate，不返回患者端回答。
```

---

# 七、P7P1-E：实现 GraphEvidenceValidationService

## 目标

确保 graph evidence candidate 不能越权。

## 任务

```text
[ ] 新增 GraphEvidenceValidationService。
[ ] 校验 graph_candidate_id 非空。
[ ] 校验 evidence_ref 存在且可追踪。
[ ] 校验 graph_paths 非空。
[ ] 校验 path depth 不超过限制。
[ ] 校验 node_id / edge_id 存在。
[ ] 校验 relation_type 允许。
[ ] 校验 graph_version 存在。
[ ] 校验 reason_summary 不含确诊 / 一定是 / 治疗结论。
[ ] 支持 ACCEPTED / PARTIALLY_ACCEPTED / REJECTED / DEGRADED。
```

## 验收标准

```text
[ ] 合法 GraphEvidenceCandidate 通过。
[ ] 缺 path 拒绝。
[ ] 缺 node / edge 拒绝。
[ ] 超过 max_depth 拒绝。
[ ] forbidden wording 拒绝。
[ ] 所有拒绝都有 reasons。
```

---

# 八、P7P1-F：EvidenceGraph 图关系集成

## 目标

让 accepted GraphEvidenceCandidate 增强 EvidenceGraph。

## 任务

```text
[ ] 扩展 EvidenceGraphItem 支持 graph_refs / graph_paths / relation_summaries。
[ ] 新增 GraphEvidenceCandidateToGraphMapper。
[ ] EvidenceGraphService 读取 GraphEvidenceSnapshot。
[ ] accepted GraphEvidenceCandidate 进入 EvidenceGraph。
[ ] rejected GraphEvidenceCandidate 不进入 EvidenceGraph。
[ ] ClinicianReport 可展示 graph relation summary。
[ ] PatientOutput 不展示 graph path / graph score。
[ ] DDxCandidate 状态不因 graph evidence 直接变成 confirmed。
```

## 验收标准

```text
[ ] EvidenceGraph 有 graph relation entry。
[ ] ClinicianReport 可见关系摘要。
[ ] PatientOutput 不泄露 graph path / score。
[ ] DecisionBoundary 仍控制患者端输出。
[ ] DDx 状态不被 graph provider 直接修改为 confirmed。
```

---

# 九、P7P1-G：Capability Orchestration 接入 graph evidence

## 目标

在 Phase 7-P0 evidence retrieval 之后接入 graph evidence。

## 任务

```text
[ ] 新增 GraphEvidenceCapabilityOrchestrator。
[ ] 判断是否存在 accepted EvidenceCandidate。
[ ] 构造 GraphEvidenceRequest。
[ ] 调用 GraphEvidencePolicy。
[ ] 调用 GraphEvidenceProvider。
[ ] 保存 GraphEvidenceSnapshot 到 RuntimeState。
[ ] Graph evidence 失败时 fallback，不阻断 Runtime 主链路。
[ ] 不覆盖 AgentOrchestrationSnapshot。
[ ] 不覆盖 EvidenceRetrievalSnapshot。
```

## 验收标准

```text
[ ] SafetyGate fail-safe 时不执行 graph evidence。
[ ] 没有 accepted evidence 时跳过。
[ ] Graph provider 失败时 Runtime 降级继续。
[ ] RuntimeState 同时保留 agent / evidence / graph snapshots。
```

---

# 十、P7P1-H：Graph Evidence Debug API

## 目标

提供最小 debug API 观察 graph evidence。

## 任务

```text
[ ] 新增 GraphEvidenceDebugController。
[ ] 实现 POST /api/v1/debug/graph-evidence/run。
[ ] 实现 GET /api/v1/debug/graph-evidence/runs/{graph_retrieval_id}。
[ ] 实现 GET /api/v1/debug/graph-evidence/graph。
[ ] 所有响应使用 Safe DTO。
[ ] 接入 DebugTokenFilter / ActorContext / AccessPolicy。
```

## 验收标准

```text
[ ] 未授权访问被拒绝。
[ ] PATIENT 角色不能调用。
[ ] SYSTEM_ADMIN / EVALUATION_REVIEWER 可 run。
[ ] READ_ONLY_OBSERVER 可读 graph metadata。
[ ] API 不返回 raw runtime state。
[ ] API 不返回未脱敏患者原文。
```

---

# 十一、P7P1-I：Trace / Audit 接入

## 目标

让 graph evidence 可复盘。

## 任务

```text
[ ] GraphEvidenceResult 记录 graph_retrieval_id。
[ ] GraphEvidenceTrace 记录 provider_id / graph_version / matched_nodes / path_count。
[ ] RuntimeTrace 记录 graph evidence summary。
[ ] Debug API 调用写入 AuditLog。
[ ] Policy 拒绝也记录 trace / audit。
[ ] Validation 拒绝也记录 trace / audit。
```

## 验收标准

```text
[ ] 每次 graph evidence 有 graph_retrieval_id。
[ ] AuditLog 可看到 RUN_GRAPH_EVIDENCE / QUERY_GRAPH_EVIDENCE。
[ ] Trace 不保存 raw patient dialogue。
[ ] Trace 保留 accepted / rejected graph candidate ids。
```

---

# 十二、P7P1-J：Graph Evidence Evaluation Scorer

## 目标

让 graph evidence 能力进入 Evaluation。

## 任务

```text
[ ] 新增 GraphTraceCompletenessScorer。
[ ] 新增 GraphSourceVersionScorer。
[ ] 新增 GraphPatientBoundaryScorer。
[ ] 新增 GraphPathSafetyScorer。
[ ] 可选新增 GraphEvidenceDdxAlignmentScorer。
[ ] 支持 case tags 含 graph_evidence_eval 时启用 graph scorer。
[ ] EvaluationResult 中加入 graph 相关 metric。
```

## 验收标准

```text
[ ] 缺 graph_version 时得分失败。
[ ] 缺 node / edge / path 时得分失败。
[ ] PatientOutput 泄露 graph path 时得分失败。
[ ] reason_summary 含确诊表达时得分失败。
```

---

# 十三、P7P1-K：测试、人工验证与冻结记录

## 目标

完成 Phase 7-P1 收口。

## 任务

```text
[ ] 完成 KgLiteGraphRepositoryTest。
[ ] 完成 GraphEvidencePolicyTest。
[ ] 完成 KgLiteGraphEvidenceProviderTest。
[ ] 完成 GraphEvidenceValidationServiceTest。
[ ] 完成 EvidenceGraphGraphRelationIntegrationTest。
[ ] 完成 GraphEvidenceDebugControllerTest。
[ ] 完成 GraphEvidenceTraceAuditTest。
[ ] 完成 Graph Evaluation Scorer tests。
[ ] 运行 mvn test。
[ ] 运行 console-web npm run test / npm run build。
[ ] 编写 Phase7_P1人工测试结果.md。
[ ] 编写 Phase7_P1冻结记录.md。
```

## 验收标准

```text
[ ] 所有后端测试通过。
[ ] Phase 1–7 P0 回归不破坏。
[ ] 前端测试 / build 不回归；如存在 flake，必须明确说明与 P7-P1 无直接关联。
[ ] 人工测试覆盖高风险胸痛、普通发热、无 accepted EvidenceRef。
[ ] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十四、推荐实现顺序

建议严格按以下顺序实现：

```text
1. P7P1-A：KG-lite domain 基础对象。
2. P7P1-B：KG-lite graph resource 与加载器。
3. P7P1-C：GraphEvidencePolicy。
4. P7P1-D：GraphEvidenceProvider MVP。
5. P7P1-E：GraphEvidenceValidationService。
6. P7P1-H：Graph Evidence Debug API。
7. P7P1-F：EvidenceGraph 图关系集成。
8. P7P1-G：Capability Orchestration 接入。
9. P7P1-I：Trace / Audit。
10. P7P1-J：Evaluation Scorer。
11. P7P1-K：测试、人工验证、冻结记录。
```

原因：

```text
先保证图关系资源正确，
再让 GraphEvidenceProvider 返回候选，
再接入 EvidenceGraph 和 Runtime，
最后接入 Evaluation 和冻结。
```

---

# 十五、开发期间禁止事项

```text
1. 不引入 Neo4j。
2. 不引入 Milvus / Qdrant / pgvector 作为 P1 主线。
3. 不做 LLM GraphRAG 问答。
4. 不让 GraphRAG 直接回答患者。
5. 不让图谱直接决定最终诊断。
6. 不让 graph path 直接进入 PatientOutput。
7. 不自动抽取知识入图。
8. 不破坏 Phase 7-P0 EvidenceProvider。
9. 不覆盖 Phase 6-P0 Agent runtime。
10. 不改写 Phase 1–7 P0 冻结记录。
```

---

# 十六、Phase 7-P1 完成后的后置任务

```text
1. Phase 8-P0：EmbeddingProvider / Python AI Provider。
2. Phase 8-P1：ModelProvider / Reranker / TrainingDatasetVersion。
3. Phase 10：Knowledge Console。
4. Phase 10：正式知识审核、发布、回滚工作流。
5. 后置专项：Neo4j / production graph store。
```

---

# 十七、最终 Definition of Done

Phase 7-P1 完成的最终标准：

```text
[ ] EvidenceRef 可以映射到 KG-lite graph node。
[ ] GraphEvidenceProvider 可以生成 GraphEvidenceCandidate。
[ ] GraphEvidenceCandidate 必须经过 validation。
[ ] EvidenceGraph 可以采纳 accepted graph relation。
[ ] PatientOutput 不泄露 graph path / score / 内部诊断关系。
[ ] ClinicianReport 可以展示图关系摘要。
[ ] Graph evidence 进入 Trace / Audit。
[ ] Graph evidence 能力进入 Evaluation。
[ ] Debug API 可以安全观察 graph evidence。
[ ] 所有新增测试和既有回归测试通过。
[ ] Phase7_P1冻结记录完成。
```
