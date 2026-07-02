# Phase 7-P1 Graph Evidence API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase7_P1KG-lite与GraphRAG原型_实现规格.md`  
> 前置阶段：Phase 7-P0 RAG EvidenceProvider MVP 已冻结  
> 当前 Phase：Phase 7-P1  
> 当前目标：定义 KG-lite / Graph Evidence 原型的 Debug API、DTO、安全边界、Evaluation 指标和测试方案。

---

# 一、API 设计原则

Phase 7-P1 只开放 debug / internal API，不开放 patient-facing GraphRAG API。

原则：

```text
1. Graph Evidence API 只用于开发、调试、评估和治理观察。
2. Patient-facing client 不允许直接调用 Graph Evidence API。
3. Graph Evidence API 不返回 raw RuntimeState。
4. Graph Evidence API 不返回未脱敏患者原文。
5. Graph Evidence API 不允许直接生成 PatientOutput。
6. GraphEvidenceCandidate 必须经过 GraphEvidenceValidation。
7. API 返回 Safe DTO，不暴露内部敏感字段。
8. API 必须记录 Trace / Audit。
```

---

# 二、API 路径规划

Phase 7-P1 新增路径统一放在：

```text
/api/v1/debug/graph-evidence/**
```

候选 API：

```text
POST /api/v1/debug/graph-evidence/run
GET  /api/v1/debug/graph-evidence/runs/{graph_retrieval_id}
GET  /api/v1/debug/graph-evidence/graph
GET  /api/v1/debug/graph-evidence/nodes/{node_id}
GET  /api/v1/debug/graph-evidence/paths?runtime_id=xxx
```

P1 最小必要 API：

```text
POST /api/v1/debug/graph-evidence/run
GET  /api/v1/debug/graph-evidence/runs/{graph_retrieval_id}
GET  /api/v1/debug/graph-evidence/graph
```

---

# 三、API 1：运行 Graph Evidence

## 3.1 请求

```http
POST /api/v1/debug/graph-evidence/run
Content-Type: application/json
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: SYSTEM_ADMIN,EVALUATION_REVIEWER
```

请求体：

```json
{
  "runtime_id": "runtime_demo_001",
  "symptom_group": "chest_pain",
  "case_frame_summary": {
    "known_facts": ["胸闷", "活动后加重", "出汗"],
    "missing_facts": ["持续时间", "是否放射痛"]
  },
  "accepted_evidence_refs": [
    {
      "evidence_id": "ev_chunk_chest_pain_001",
      "source_id": "synthetic_safety_guide_chest_pain",
      "chunk_id": "chunk_chest_pain_001",
      "symptom_group": "chest_pain",
      "use_case": "safety_warning"
    }
  ],
  "current_ddx_summary": ["acute_coronary_syndrome_rule_out"],
  "max_path_depth": 2,
  "max_path_count": 5
}
```

## 3.2 请求约束

```text
1. runtime_id 必填。
2. symptom_group 必填。
3. accepted_evidence_refs 必须来自已验证 EvidenceRef。
4. max_path_depth 默认 2，最大不超过 3。
5. max_path_count 默认 5，最大不超过 10。
6. 请求体不允许包含 raw_runtime_state。
7. 请求体不允许包含 patient_raw_dialogue。
8. 请求体不允许包含 final_diagnosis。
9. 请求体不允许要求生成 patient_answer。
```

## 3.3 响应

```json
{
  "graph_retrieval_id": "graph_ret_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "kg_lite_graph_evidence_provider",
  "provider_version": "0.7.1-p1",
  "graph_version": "phase7-kg-lite-0.1.0",
  "status": "SUCCESS",
  "graph_candidates": [
    {
      "graph_candidate_id": "graph_cand_001",
      "evidence_ref": {
        "evidence_id": "ev_chunk_chest_pain_001",
        "source_id": "synthetic_safety_guide_chest_pain",
        "chunk_id": "chunk_chest_pain_001"
      },
      "matched_nodes": ["symptom_chest_pain", "risk_signal_sweating"],
      "graph_paths": [
        {
          "path_id": "path_001",
          "node_ids": ["risk_signal_sweating", "diagnosis_acs_rule_out", "test_ecg"],
          "edge_ids": ["edge_sweating_red_flag_for_acs", "edge_acs_suggests_ecg"],
          "path_score": 0.86,
          "path_reason": "出汗与活动后胸闷共同提示需排除急性冠脉综合征，并建议医生端关注心电图等检查。"
        }
      ],
      "related_ddx_item": "acute_coronary_syndrome_rule_out",
      "suggested_questions": ["是否有放射痛或呼吸困难？"],
      "suggested_tests": ["ECG"],
      "risk_flags": ["activity_related_chest_pain", "sweating"],
      "confidence": 0.84,
      "reason_summary": "KG-lite path links risk signal to rule-out diagnosis and test suggestion."
    }
  ],
  "validation_result": {
    "status": "ACCEPTED",
    "accepted_candidate_ids": ["graph_cand_001"],
    "rejected_candidate_ids": [],
    "reasons": []
  },
  "graph_trace": {
    "trace_id": "graph_trace_001",
    "matched_node_count": 3,
    "path_count": 1,
    "recorded": true
  },
  "warnings": []
}
```

---

# 四、API 2：查询 Graph Evidence Run

```http
GET /api/v1/debug/graph-evidence/runs/{graph_retrieval_id}
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: EVALUATION_REVIEWER
```

响应：

```json
{
  "graph_retrieval_id": "graph_ret_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "kg_lite_graph_evidence_provider",
  "status": "SUCCESS",
  "graph_candidate_count": 2,
  "accepted_candidate_count": 2,
  "rejected_candidate_count": 0,
  "graph_version": "phase7-kg-lite-0.1.0",
  "safe_trace_available": true,
  "warnings": []
}
```

---

# 五、API 3：查询 KG-lite Graph

```http
GET /api/v1/debug/graph-evidence/graph
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: READ_ONLY_OBSERVER
```

响应：

```json
{
  "graph_id": "phase7-kg-lite",
  "graph_version": "phase7-kg-lite-0.1.0",
  "node_count": 30,
  "edge_count": 45,
  "supported_symptom_groups": ["chest_pain", "fever", "abdominal_pain"],
  "node_type_counts": {
    "SYMPTOM": 6,
    "DIAGNOSIS": 9,
    "TEST": 6,
    "RISK_SIGNAL": 6,
    "EVIDENCE": 9
  }
}
```

返回限制：

```text
可以返回 graph metadata。
可以返回 safe node / edge summary。
不返回 patient raw dialogue。
不返回 diagnosis final decision。
不返回 patient-facing graph answer。
```

---

# 六、错误响应设计

## 6.1 Policy 拒绝

```json
{
  "graph_retrieval_id": "graph_ret_002",
  "status": "POLICY_REJECTED",
  "error_code": "GRAPH_EVIDENCE_POLICY_REJECTED",
  "message": "Graph evidence is not allowed for current context.",
  "reasons": ["no accepted evidence refs"]
}
```

## 6.2 Validation 拒绝

```json
{
  "graph_retrieval_id": "graph_ret_003",
  "status": "VALIDATION_REJECTED",
  "error_code": "GRAPH_EVIDENCE_VALIDATION_REJECTED",
  "message": "Graph path is invalid.",
  "reasons": ["path depth exceeds max", "edge_id not found"]
}
```

## 6.3 No Path Found

```json
{
  "graph_retrieval_id": "graph_ret_004",
  "status": "NO_GRAPH_PATH_FOUND",
  "warnings": ["no KG-lite path matched accepted evidence refs"]
}
```

---

# 七、权限与安全边界

API 必须遵守：

```text
1. 必须经过 DebugTokenFilter。
2. 必须解析 ActorContext。
3. run 需要 SYSTEM_ADMIN / EVALUATION_REVIEWER。
4. read 可以允许 READ_ONLY_OBSERVER。
5. PATIENT 角色不得调用。
6. 所有调用写入 AuditLog 或 RuntimeTrace。
7. 所有响应走 Safe DTO。
```

禁止：

```text
1. 不提供 /api/v1/runtime/graphrag/** patient-facing API。
2. 不提供“GraphRAG 直接回答患者”接口。
3. 不允许 API 请求传入任意 Prompt。
4. 不允许 API 返回完整内部诊断推理链。
5. 不允许 API 返回未脱敏患者对话。
```

---

# 八、DTO 规划

建议 DTO：

```text
GraphEvidenceRunRequest
GraphEvidenceRunResponse
GraphEvidenceSafeDto
GraphEvidenceCandidateSafeDto
GraphRefSafeDto
GraphPathSafeDto
GraphValidationResultDto
GraphTraceDto
KgLiteGraphSummaryDto
KgLiteNodeSafeDto
KgLiteEdgeSafeDto
```

所有 DTO 必须是 Safe DTO。

---

# 九、测试总览

Phase 7-P1 测试分为：

```text
1. KG-lite graph loading tests
2. Graph evidence policy tests
3. GraphEvidenceProvider tests
4. Graph evidence validation tests
5. EvidenceGraph integration tests
6. Debug API tests
7. Trace / Audit tests
8. Evaluation scorer tests
9. Regression tests for Phase 1–7 P0
```

---

# 十、单元测试设计

## 10.1 KgLiteGraphRepositoryTest

覆盖：

```text
1. 可以加载 phase7-default kg_lite_graph。
2. graph 至少包含 chest_pain / fever / abdominal_pain。
3. node_id / edge_id / relation_type / version 必填。
4. edge from / to node 必须存在。
5. 无效 edge 被拒绝或记录 warning。
```

## 10.2 GraphEvidenceProviderTest

覆盖：

```text
1. accepted chest_pain EvidenceRef 能匹配到 EVIDENCE node。
2. risk signal 能扩展到 diagnosis node。
3. diagnosis 能扩展到 test node。
4. max_depth 生效。
5. unknown evidence_ref 返回 NO_GRAPH_PATH_FOUND。
6. 返回 GraphEvidenceCandidate，不返回患者端回答。
```

## 10.3 GraphEvidenceValidationServiceTest

覆盖：

```text
1. 合法 GraphEvidenceCandidate 通过。
2. graph_paths 为空拒绝。
3. path depth 超限拒绝。
4. node_id 不存在拒绝。
5. edge_id 不存在拒绝。
6. forbidden relation_type 拒绝。
7. reason_summary 含“确诊”“一定是”时拒绝。
```

## 10.4 EvidenceGraphGraphRelationIntegrationTest

覆盖：

```text
1. accepted GraphEvidenceCandidate 能进入 EvidenceGraph。
2. rejected GraphEvidenceCandidate 不进入 EvidenceGraph。
3. ClinicianReport 可看到 graph relation summary。
4. PatientOutput 不泄露 graph path / graph score。
5. DDxCandidate 不因 graph evidence 直接变成 confirmed。
```

---

# 十一、API 测试设计

## 11.1 正常 graph evidence run

```text
POST /api/v1/debug/graph-evidence/run
```

期望：

```text
HTTP 200
status = SUCCESS
graph_candidates 非空
validation_result.status = ACCEPTED 或 PARTIALLY_ACCEPTED
graph_trace.recorded = true
```

## 11.2 未授权访问

期望：

```text
HTTP 401 / 403
不执行 graph evidence
不生成 graph candidate
```

## 11.3 READ_ONLY_OBSERVER 访问 run

期望：

```text
HTTP 403
```

## 11.4 READ_ONLY_OBSERVER 访问 graph summary

期望：

```text
HTTP 200
只返回 Safe DTO
```

## 11.5 no accepted evidence refs

期望：

```text
POLICY_REJECTED 或 NO_GRAPH_PATH_FOUND
不伪造 graph path
```

---

# 十二、Evaluation 测试设计

新增 graph 相关 Scorer：

```text
GraphTraceCompletenessScorer
GraphSourceVersionScorer
GraphPatientBoundaryScorer
GraphPathSafetyScorer
GraphEvidenceDdxAlignmentScorer（可选）
```

通过 tag 启用：

```text
graph_evidence_eval
```

## 12.1 GraphTraceCompletenessScorer

衡量：

```text
是否记录 graph_retrieval_id、provider_id、graph_version、node_ids、edge_ids、accepted/rejected candidate ids。
```

## 12.2 GraphSourceVersionScorer

衡量：

```text
GraphNode / GraphEdge / GraphPath 是否包含 graph_version 或 source_ref。
```

## 12.3 GraphPatientBoundaryScorer

衡量：

```text
PatientOutput 是否泄露 graph path、graph score、内部诊断关系。
```

## 12.4 GraphPathSafetyScorer

衡量：

```text
Graph reason summary 是否包含确诊、一定是、治疗结论等越界表达。
```

---

# 十三、人工测试场景

## 场景 1：高风险胸痛

输入：

```text
胸口闷，活动后更明显，出汗。
```

期望：

```text
EvidenceRef 匹配 chest_pain evidence node。
Graph path 能连接 risk_signal → diagnosis → test。
ClinicianReport 可见关系摘要。
PatientOutput 不泄露 graph path / graph score。
```

## 场景 2：普通发热

输入：

```text
发烧两天，咽痛，有点咳嗽。
```

期望：

```text
Graph path 限定在 fever symptom_group。
不会返回 chest_pain graph path。
```

## 场景 3：无 accepted EvidenceRef

输入：

```text
accepted_evidence_refs = []
```

期望：

```text
Policy 拒绝或返回 NO_GRAPH_PATH_FOUND。
不伪造 graph path。
```

---

# 十四、回归测试要求

Phase 7-P1 完成前必须通过：

```text
mvn test
```

并且不得破坏：

```text
1. Runtime start / continue API。
2. SafetyGate 高风险兜底。
3. Phase 6-P0 Agent Runtime。
4. Phase 7-P0 EvidenceProvider。
5. PatientOutput / ClinicianReport 输出隔离。
6. EvaluationRunner。
7. Candidate 生成与 Review。
8. PostgreSQL / in-memory 双模式测试。
9. Console API Safe DTO。
```

---

# 十五、完成标准

Phase 7-P1 API 与测试完成标准：

```text
1. Debug API 可以运行 Graph Evidence。
2. Graph API 可以查看 KG-lite graph metadata。
3. 未授权访问被拒绝。
4. PATIENT 角色无法调用 Graph Evidence API。
5. Graph path 缺 node / edge / version 时被拒绝。
6. GraphEvidenceResult 可被查询。
7. Graph Evidence 进入 Trace / Audit。
8. Graph 相关 Scorer 可以参与 Evaluation。
9. PatientOutput 不泄露 graph path / score / 内部诊断关系。
10. 所有新增测试和既有回归测试通过。
```

---

# 十六、最终结论

Phase 7-P1 的 API 与测试重点不是“实现完整 GraphRAG”，而是证明：

```text
KG-lite graph 被允许时才能参与 evidence enhancement，
graph path 只能成为 GraphEvidenceCandidate，
GraphEvidenceCandidate 必须可追踪、可校验、可拒绝，
图关系进入 EvidenceGraph 而不是直接进入 PatientOutput，
患者端边界不会被破坏，
图关系能力可以被 Evaluation 衡量。
```
