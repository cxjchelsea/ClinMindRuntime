# Phase 7-P0 RAG EvidenceProvider API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase7_P0RAG_EvidenceProvider_实现规格.md`  
> 上位专项设计：`docs/2-专项设计/医学知识库与RAG构建规划.md`  
> 当前 Phase：Phase 7-P0  
> 当前目标：定义 RAG EvidenceProvider MVP 的 Debug API、DTO、安全边界、Evaluation 指标和测试方案。

---

# 一、API 设计原则

Phase 7-P0 只开放 debug / internal evidence API，不开放 patient-facing RAG API。

原则：

```text
1. Evidence API 只用于开发、调试、评估和治理观察。
2. Patient-facing client 不允许直接调用 Evidence API。
3. Evidence API 不返回 raw RuntimeState。
4. Evidence API 不返回未脱敏患者原文。
5. Evidence API 不允许直接生成 PatientOutput。
6. EvidenceCandidate 必须经过 EvidenceValidation / RuntimeValidation。
7. API 返回 Safe DTO，不暴露内部敏感字段。
8. API 必须记录 Trace / Audit。
```

---

# 二、API 路径规划

Phase 7-P0 新增路径统一放在：

```text
/api/v1/debug/evidence/**
```

候选 API：

```text
POST /api/v1/debug/evidence/retrieve
GET  /api/v1/debug/evidence/retrievals/{retrieval_id}
GET  /api/v1/debug/evidence/corpus
GET  /api/v1/debug/evidence/corpus/{chunk_id}
POST /api/v1/debug/evidence/candidates/validate
```

P0 最小必要 API：

```text
POST /api/v1/debug/evidence/retrieve
GET  /api/v1/debug/evidence/retrievals/{retrieval_id}
GET  /api/v1/debug/evidence/corpus
```

---

# 三、API 1：运行 Evidence Retrieval

## 3.1 请求

```http
POST /api/v1/debug/evidence/retrieve
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
    "chief_complaint": "胸口闷，活动后更明显，出汗",
    "known_facts": ["胸闷", "活动后加重", "出汗"],
    "missing_facts": ["持续时间", "是否放射痛", "是否呼吸困难"]
  },
  "candidate_ddx_summary": ["急性冠脉综合征需排除", "非心源性胸痛"],
  "red_flag_summary": ["活动后胸闷", "出汗"],
  "asset_package_id": "phase2-default",
  "asset_package_version": "0.2.0",
  "retrieval_limit": 5,
  "role_context": "clinician_debug"
}
```

## 3.2 请求约束

```text
1. runtime_id 必填。
2. symptom_group 必填。
3. case_frame_summary 必填。
4. retrieval_limit 默认 5，最大不超过 10。
5. 请求体不允许包含 raw_runtime_state。
6. 请求体不允许包含 patient_raw_dialogue。
7. 请求体不允许包含 final_diagnosis。
8. role_context 不能是 patient_direct_answer。
```

## 3.3 响应

```json
{
  "retrieval_id": "evidence_ret_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "rag_evidence_provider",
  "provider_version": "0.7.0-p0",
  "evidence_corpus_version": "phase7-default-0.1.0",
  "status": "SUCCESS",
  "evidence_candidates": [
    {
      "candidate_id": "ev_cand_001",
      "evidence_ref": {
        "evidence_id": "ev_chest_pain_red_flag_001",
        "source_id": "synthetic_safety_guide_chest_pain",
        "chunk_id": "chunk_chest_pain_001",
        "source_type": "synthetic_safety_knowledge",
        "title": "胸痛风险信号识别",
        "section_path": "胸痛 > 风险信号",
        "symptom_group": "chest_pain",
        "diagnosis_tags": ["acute_coronary_syndrome_rule_out"],
        "evidence_strength": "SAFETY_RULE",
        "supports_or_refutes": "ASK_MORE",
        "risk_level": "HIGH",
        "asset_package_version": "0.2.0",
        "retrieved_by": "rag_evidence_provider",
        "retrieval_score": 0.91
      },
      "matched_case_frame_fields": ["胸闷", "活动后加重", "出汗"],
      "related_ddx_item": "急性冠脉综合征需排除",
      "use_case": "safety_warning",
      "confidence": 0.86,
      "reason_summary": "当前输入包含胸痛相关高风险信号，应提示医生端关注并补充追问。"
    }
  ],
  "validation_result": {
    "status": "ACCEPTED",
    "accepted_candidate_ids": ["ev_cand_001"],
    "rejected_candidate_ids": [],
    "reasons": []
  },
  "query_trace": {
    "query_terms": ["chest_pain", "胸闷", "出汗"],
    "matched_chunk_count": 3,
    "recorded": true
  },
  "warnings": []
}
```

---

# 四、API 2：查询 Evidence Retrieval

```http
GET /api/v1/debug/evidence/retrievals/{retrieval_id}
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: EVALUATION_REVIEWER
```

响应：

```json
{
  "retrieval_id": "evidence_ret_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "rag_evidence_provider",
  "status": "SUCCESS",
  "candidate_count": 3,
  "accepted_candidate_count": 2,
  "rejected_candidate_count": 1,
  "evidence_corpus_version": "phase7-default-0.1.0",
  "safe_trace_available": true,
  "warnings": []
}
```

返回限制：

```text
不返回完整 raw input。
不返回患者原始对话。
不返回患者身份信息。
不返回用于患者端的直接诊断文本。
```

---

# 五、API 3：查询 Evidence Corpus

```http
GET /api/v1/debug/evidence/corpus
```

响应：

```json
{
  "corpus_id": "phase7-default",
  "corpus_version": "0.1.0",
  "chunk_count": 9,
  "supported_symptom_groups": ["chest_pain", "fever", "abdominal_pain"],
  "chunks": [
    {
      "chunk_id": "chunk_chest_pain_001",
      "source_id": "synthetic_safety_guide_chest_pain",
      "title": "胸痛风险信号识别",
      "source_type": "synthetic_safety_knowledge",
      "symptom_group": "chest_pain",
      "risk_level": "HIGH",
      "audience": "clinician",
      "use_cases": ["safety_warning", "ask_more"]
    }
  ]
}
```

---

# 六、错误响应设计

## 6.1 Policy 拒绝

```json
{
  "retrieval_id": "evidence_ret_002",
  "status": "POLICY_REJECTED",
  "error_code": "EVIDENCE_POLICY_REJECTED",
  "message": "Evidence retrieval is not allowed for current context.",
  "reasons": ["unsupported symptom_group"]
}
```

## 6.2 Validation 拒绝

```json
{
  "retrieval_id": "evidence_ret_003",
  "status": "VALIDATION_REJECTED",
  "error_code": "EVIDENCE_VALIDATION_REJECTED",
  "message": "Evidence candidate is missing source reference.",
  "reasons": ["source_id is required", "retrieval_score is required"]
}
```

## 6.3 No Evidence Found

```json
{
  "retrieval_id": "evidence_ret_004",
  "status": "NO_EVIDENCE_FOUND",
  "warnings": ["no evidence chunk matched current symptom_group"]
}
```

---

# 七、权限与安全边界

API 必须遵守：

```text
1. 必须经过 DebugTokenFilter。
2. 必须解析 ActorContext。
3. retrieve 需要 SYSTEM_ADMIN / EVALUATION_REVIEWER。
4. read 可以允许 READ_ONLY_OBSERVER。
5. PATIENT 角色不得调用。
6. 所有调用写入 AuditLog 或 RuntimeTrace。
7. 所有响应走 Safe DTO。
```

禁止：

```text
1. 不提供 /api/v1/runtime/rag/** patient-facing API。
2. 不提供“RAG 直接回答患者”接口。
3. 不允许 API 请求传入任意 Prompt。
4. 不允许 API 返回完整内部 EvidenceGraph。
5. 不允许 API 返回未脱敏患者对话。
```

---

# 八、DTO 规划

建议 DTO：

```text
EvidenceRetrievalRunRequest
EvidenceRetrievalRunResponse
EvidenceRetrievalSafeDto
EvidenceCandidateSafeDto
EvidenceRefDto
EvidenceValidationResultDto
EvidenceQueryTraceDto
EvidenceCorpusDto
EvidenceChunkSafeDto
```

所有 DTO 必须是 Safe DTO。

---

# 九、测试总览

Phase 7-P0 测试分为：

```text
1. Evidence corpus loading tests
2. Evidence provider policy tests
3. RagEvidenceProvider tests
4. Evidence validation tests
5. Capability orchestration integration tests
6. EvidenceGraph integration tests
7. Debug API tests
8. Trace / Audit tests
9. Evaluation scorer tests
10. Regression tests for Phase 1–6 P0
```

---

# 十、单元测试设计

## 10.1 EvidenceCorpusRepositoryTest

覆盖：

```text
1. 可以加载 phase7-default evidence corpus。
2. corpus 至少包含 chest_pain / fever / abdominal_pain。
3. 每个 chunk 有 source_id / chunk_id / symptom_group / version。
4. 无效 chunk 会被拒绝或跳过并记录 warning。
```

## 10.2 EvidenceProviderPolicyTest

覆盖：

```text
1. 支持的 symptom_group 允许检索。
2. 不支持的 symptom_group 拒绝。
3. corpus unavailable 时 fail-closed。
4. retrieval_limit 超限时降级到最大值。
```

## 10.3 RagEvidenceProviderTest

覆盖：

```text
1. chest_pain 输入能召回 chest_pain chunks。
2. fever 输入能召回 fever chunks。
3. unknown symptom_group 返回 NO_EVIDENCE_FOUND 或 POLICY_REJECTED。
4. retrieval_score 存在且排序稳定。
5. 返回 EvidenceCandidate 而不是自然语言回答。
```

## 10.4 EvidenceValidationServiceTest

覆盖：

```text
1. 合法 EvidenceCandidate 通过。
2. 缺少 source_id 拒绝。
3. 缺少 chunk_id 拒绝。
4. 缺少 retrieval_score 拒绝。
5. patient_direct_answer use_case 拒绝。
6. candidate_count 超过限制时部分采纳。
```

## 10.5 EvidenceGraphIntegrationTest

覆盖：

```text
1. accepted EvidenceCandidate 能进入 EvidenceGraph。
2. rejected EvidenceCandidate 不进入 EvidenceGraph。
3. EvidenceGraph item 保留 evidence_ref。
4. PatientOutput 不泄露 evidence retrieval score。
5. ClinicianReport 可看到证据摘要。
```

---

# 十一、API 测试设计

## 11.1 正常 retrieval

```text
POST /api/v1/debug/evidence/retrieve
```

期望：

```text
HTTP 200
status = SUCCESS
evidence_candidates 非空
validation_result.status = ACCEPTED 或 PARTIALLY_ACCEPTED
query_trace.recorded = true
```

## 11.2 未授权访问

期望：

```text
HTTP 401 / 403
不执行 evidence retrieval
不生成 retrieval result
```

## 11.3 READ_ONLY_OBSERVER 访问 run

期望：

```text
HTTP 403
```

## 11.4 READ_ONLY_OBSERVER 访问 corpus

期望：

```text
HTTP 200
只返回 Safe DTO
```

## 11.5 缺 source_id 的 candidate

期望：

```text
validation_result.status = REJECTED
reasons 包含 source_id is required
```

---

# 十二、Evaluation 测试设计

新增 evidence 相关 Scorer：

```text
EvidenceTraceCompletenessScorer
EvidenceSourceVersionScorer
EvidencePatientBoundaryScorer
EvidenceUseCaseSafetyScorer
EvidenceRecallScorer（可选）
```

## 12.1 EvidenceTraceCompletenessScorer

衡量：

```text
是否记录 retrieval_id、provider_id、provider_version、corpus_version、accepted/rejected candidate ids。
```

## 12.2 EvidenceSourceVersionScorer

衡量：

```text
EvidenceRef 是否包含 source_id、chunk_id、evidence_corpus_version 或 asset_package_version。
```

## 12.3 EvidencePatientBoundaryScorer

衡量：

```text
PatientOutput 是否泄露 retrieval_score、source internal details、医生端推理链。
```

## 12.4 EvidenceUseCaseSafetyScorer

衡量：

```text
EvidenceCandidate use_case 是否属于允许集合。
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
RAG EvidenceProvider 返回 chest_pain 相关 safety_warning / ask_more candidate。
EvidenceRef 有 source_id / chunk_id / version。
PatientOutput 不泄露 retrieval_score。
ClinicianReport 可见证据摘要。
```

## 场景 2：普通发热

输入：

```text
发烧两天，咽痛，有点咳嗽。
```

期望：

```text
返回 fever 相关 evidence candidates。
不会返回 chest_pain evidence。
```

## 场景 3：不支持症状组

输入：

```text
symptom_group = unknown
```

期望：

```text
POLICY_REJECTED 或 NO_EVIDENCE_FOUND。
不伪造证据。
```

---

# 十四、回归测试要求

Phase 7-P0 完成前必须通过：

```text
mvn test
```

并且不得破坏：

```text
1. Runtime start / continue API。
2. SafetyGate 高风险兜底。
3. Phase 6-P0 Agent Runtime / Agent Debug API。
4. PatientOutput / ClinicianReport 输出隔离。
5. EvaluationRunner。
6. Candidate 生成与 Review。
7. PostgreSQL / in-memory 双模式测试。
8. Console API Safe DTO。
9. console-web npm run test / npm run build。
```

---

# 十五、完成标准

Phase 7-P0 API 与测试完成标准：

```text
1. Debug API 可以运行 Evidence Retrieval。
2. Corpus API 可以查看 Safe evidence corpus metadata。
3. 未授权访问被拒绝。
4. PATIENT 角色无法调用 Evidence API。
5. EvidenceCandidate 缺 source / version / score 时被拒绝。
6. EvidenceRetrievalResult 可被查询。
7. Evidence Retrieval 进入 Trace / Audit。
8. Evidence 相关 Scorer 可以参与 Evaluation。
9. PatientOutput 不泄露 RAG 内部证据与评分。
10. 所有新增测试和既有回归测试通过。
```

---

# 十六、最终结论

Phase 7-P0 的 API 与测试重点不是“让 RAG 回答得更像人”，而是证明：

```text
RAG 被允许时才能检索，
检索结果只能成为 EvidenceCandidate，
EvidenceCandidate 必须可追踪、可校验、可拒绝，
证据进入 EvidenceGraph 而不是直接进入 PatientOutput，
患者端边界不会被破坏，
证据能力可以被 Evaluation 衡量。
```
