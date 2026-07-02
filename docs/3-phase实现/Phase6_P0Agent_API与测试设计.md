# Phase 6-P0 Agent API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md`  
> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 当前 Phase：Phase 6-P0  
> 当前目标：定义受控 Agent 执行层 MVP 的 Debug API、DTO、安全边界和测试方案。

---

# 一、API 设计原则

Phase 6-P0 只开放 debug / internal API，不开放 patient-facing Agent API。

原则：

```text
1. Agent API 只用于开发、调试、评估和治理观察。
2. Patient-facing client 不允许直接调用 Agent API。
3. Agent API 不返回 raw RuntimeState。
4. Agent API 不返回未脱敏患者原文。
5. Agent API 不允许直接修改 RuntimeState。
6. Agent Proposal 必须经过 Validator 和 Runtime Validation。
7. API 返回 Safe DTO，不暴露内部敏感字段。
```

---

# 二、API 路径规划

Phase 6-P0 新增路径统一放在：

```text
/api/v1/debug/agents/**
```

候选 API：

```text
POST /api/v1/debug/agents/inquiry-planning/run
GET  /api/v1/debug/agents/executions/{execution_id}
GET  /api/v1/debug/agents/executions/{execution_id}/trace
POST /api/v1/debug/agents/proposals/{proposal_id}/validate
GET  /api/v1/debug/agents/registry
```

P0 最小必要 API：

```text
POST /api/v1/debug/agents/inquiry-planning/run
GET  /api/v1/debug/agents/executions/{execution_id}
GET  /api/v1/debug/agents/registry
```

---

# 三、API 1：运行 InquiryPlanningAgent

## 3.1 请求

```http
POST /api/v1/debug/agents/inquiry-planning/run
Content-Type: application/json
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: DEVELOPER,EVALUATOR
```

请求体：

```json
{
  "runtime_id": "runtime_demo_001",
  "symptom_group": "chest_pain",
  "case_frame_summary": {
    "age": 58,
    "sex": "male",
    "chief_complaint": "胸口闷，活动后更明显，出汗",
    "known_facts": ["胸闷", "活动后加重", "出汗"],
    "missing_facts": ["持续时间", "是否放射痛", "是否呼吸困难", "既往心血管病史"]
  },
  "red_flag_candidates": ["活动后胸闷", "出汗"],
  "current_questions_asked": [],
  "allowed_question_types": ["duration", "severity", "associated_symptom", "history", "red_flag"],
  "max_question_count": 3
}
```

## 3.2 请求约束

```text
1. runtime_id 必填。
2. symptom_group 必填。
3. missing_facts 必须非空，否则 AgentPolicy 可以拒绝调用。
4. max_question_count 默认 3，最大不超过 5。
5. 请求体不允许包含 raw_runtime_state。
6. 请求体不允许包含 patient_raw_dialogue。
7. 请求体不允许包含 diagnosis_final 字段。
```

## 3.3 响应

```json
{
  "execution_id": "agent_exec_001",
  "runtime_id": "runtime_demo_001",
  "agent_id": "inquiry_planning_agent",
  "agent_version": "0.6.0-p0",
  "status": "SUCCESS",
  "policy_decision": {
    "allowed": true,
    "reasons": []
  },
  "proposal": {
    "proposal_id": "proposal_001",
    "proposal_type": "INQUIRY_PLAN",
    "proposed_questions": [
      {
        "question_id": "q1",
        "question_text": "这种胸闷大概持续了多久？是几分钟、半小时以上，还是更久？",
        "clinical_purpose": "clarify_duration",
        "target_missing_fact": "持续时间",
        "priority": "HIGH",
        "risk_related": true,
        "patient_safe_wording": true,
        "expected_answer_type": "duration"
      }
    ],
    "reasoning_summary": "当前存在活动后胸闷和出汗，需要优先补充持续时间、放射痛和呼吸困难等高风险相关信息。",
    "uncertainty_level": "MEDIUM",
    "safety_notes": ["不向患者暗示具体诊断。"]
  },
  "validation_result": {
    "status": "ACCEPTED",
    "accepted_question_ids": ["q1"],
    "rejected_question_ids": [],
    "reasons": []
  },
  "trace_summary": {
    "trace_id": "agent_trace_001",
    "recorded": true
  }
}
```

---

# 四、API 2：查询 Agent Execution

```http
GET /api/v1/debug/agents/executions/{execution_id}
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: DEVELOPER,EVALUATOR
```

响应：

```json
{
  "execution_id": "agent_exec_001",
  "runtime_id": "runtime_demo_001",
  "agent_id": "inquiry_planning_agent",
  "status": "SUCCESS",
  "started_at": "2026-07-02T10:00:00Z",
  "finished_at": "2026-07-02T10:00:01Z",
  "proposal_summary": {
    "proposal_id": "proposal_001",
    "proposal_type": "INQUIRY_PLAN",
    "question_count": 3
  },
  "validation_status": "ACCEPTED",
  "safe_trace_available": true
}
```

返回限制：

```text
不返回完整 raw input。
不返回未脱敏患者原文。
不返回完整医生端 DDx 推理链。
```

---

# 五、API 3：查询 Agent Registry

```http
GET /api/v1/debug/agents/registry
X-Debug-Token: <debug-token>
X-Actor-Id: dev-user
X-Actor-Roles: DEVELOPER,EVALUATOR
```

响应：

```json
{
  "agents": [
    {
      "agent_id": "inquiry_planning_agent",
      "agent_name": "InquiryPlanningAgent",
      "agent_version": "0.6.0-p0",
      "agent_type": "INQUIRY_PLANNING",
      "enabled": true,
      "supported_symptom_groups": ["chest_pain", "abdominal_pain", "fever"],
      "risk_level": "CONTROLLED",
      "allowed_outputs": ["INQUIRY_PLAN_PROPOSAL"]
    }
  ]
}
```

---

# 六、错误响应设计

## 6.1 Policy 拒绝

```json
{
  "execution_id": "agent_exec_002",
  "status": "POLICY_REJECTED",
  "error_code": "AGENT_POLICY_REJECTED",
  "message": "Agent is not allowed for current runtime state.",
  "reasons": ["missing_facts is empty", "agent disabled"]
}
```

## 6.2 Validation 拒绝

```json
{
  "execution_id": "agent_exec_003",
  "status": "VALIDATION_REJECTED",
  "error_code": "AGENT_PROPOSAL_VALIDATION_REJECTED",
  "message": "Agent proposal contains unsafe patient-facing wording.",
  "reasons": ["question_text contains diagnosis hint"]
}
```

## 6.3 Degraded

```json
{
  "execution_id": "agent_exec_004",
  "status": "DEGRADED",
  "error_code": "AGENT_EXECUTION_FAILED",
  "message": "Agent execution failed, fallback question policy used.",
  "fallback_used": true
}
```

---

# 七、权限与安全边界

API 必须遵守：

```text
1. 必须经过 DebugTokenFilter。
2. 必须解析 ActorContext。
3. 需要 DEVELOPER / EVALUATOR / ADMIN 中至少一个角色。
4. 不允许 PATIENT 角色调用。
5. 所有调用写入 AuditLog 或 RuntimeTrace。
6. Safe DTO 过滤敏感字段。
```

禁止：

```text
1. 不提供 /api/v1/runtime/agents/** patient-facing API。
2. 不允许 API 请求指定“直接采纳 Proposal 并输出给患者”。
3. 不允许 API 请求传入任意 Prompt 并让 Agent 执行。
4. 不允许 API 请求携带 Tool / MCP 权限。
```

---

# 八、DTO 规划

建议 DTO：

```text
InquiryPlanningRunRequest
InquiryPlanningRunResponse
AgentExecutionSafeDto
AgentRegistryItemDto
AgentPolicyDecisionDto
AgentProposalSafeDto
InquiryQuestionCandidateDto
AgentValidationResultDto
AgentTraceSummaryDto
```

所有 DTO 必须是 Safe DTO。

---

# 九、测试总览

Phase 6-P0 测试分为：

```text
1. Agent registry tests
2. Agent policy tests
3. Agent runtime tests
4. InquiryPlanningAgent tests
5. Proposal validator tests
6. Runtime validation tests
7. API tests
8. Trace / Audit tests
9. Evaluation scorer tests
10. Regression tests for Phase 1–5
```

---

# 十、单元测试设计

## 10.1 AgentRegistryTest

覆盖：

```text
1. 可以查询已注册 InquiryPlanningAgent。
2. 未注册 agent_id 返回空或拒绝。
3. disabled agent 不可被调用。
4. unsupported symptom_group 不可被调用。
```

## 10.2 AgentPolicyTest

覆盖：

```text
1. missing_facts 非空时允许调用。
2. missing_facts 为空时拒绝。
3. 高风险状态下只允许生成问诊 Proposal，不允许诊断 Proposal。
4. agent disabled 时拒绝。
5. unknown runtime state 时 fail-closed。
```

## 10.3 InquiryPlanningAgentTest

覆盖：

```text
1. 输入 chest_pain + missing duration，输出持续时间问题。
2. 输入 red_flag_candidates，优先输出红旗相关追问。
3. 输出问题数量不超过 max_question_count。
4. 输出不包含诊断暗示。
5. 输出包含 target_missing_fact。
```

## 10.4 AgentProposalValidatorTest

覆盖：

```text
1. 合法 InquiryPlanProposal 通过。
2. question_text 包含诊断暗示时拒绝。
3. target_missing_fact 不在 missing_facts 中时拒绝或降级。
4. question_count 超过限制时部分采纳。
5. 缺少 clinical_purpose 时拒绝。
6. high-risk missing fact 被低优先级处理时给出 warning 或拒绝。
```

## 10.5 RuntimeValidationServiceTest

覆盖：

```text
1. AgentProposal 不能修改 RuntimeState。
2. AgentProposal 不能产生 PatientOutput。
3. AgentProposal 缺少 source / agent version 时拒绝。
4. AgentProposal 可被部分采纳。
5. rejected proposal 有明确 reason。
```

---

# 十一、API 测试设计

## 11.1 正常运行

```text
POST /api/v1/debug/agents/inquiry-planning/run
```

期望：

```text
HTTP 200
status = SUCCESS
proposal.proposed_questions 非空
validation_result.status = ACCEPTED 或 PARTIALLY_ACCEPTED
trace_summary.recorded = true
```

## 11.2 未带 Debug Token

期望：

```text
HTTP 401 / 403
不执行 Agent
不生成 Proposal
```

## 11.3 PATIENT 角色调用

期望：

```text
HTTP 403
不执行 Agent
```

## 11.4 非法 Proposal

构造包含诊断暗示的问题：

```text
“你是不是心梗？”
```

期望：

```text
validation_result.status = REJECTED
reasons 包含 diagnosis hint / unsafe wording
```

## 11.5 max_question_count 超限

期望：

```text
只采纳允许数量内的问题，或返回 PARTIALLY_ACCEPTED。
```

---

# 十二、Evaluation 测试设计

新增 Agent 相关 Scorer：

```text
InquiryPlanCoverageScorer
RedFlagQuestionPriorityScorer
PatientSafeQuestionScorer
AgentTraceCompletenessScorer
AgentBoundaryViolationScorer
```

## 12.1 InquiryPlanCoverageScorer

衡量：

```text
proposed_questions 覆盖了多少 expected_missing_facts。
```

## 12.2 RedFlagQuestionPriorityScorer

衡量：

```text
red_flag_candidates 是否被高优先级问题覆盖。
```

## 12.3 PatientSafeQuestionScorer

衡量：

```text
问题是否没有泄露诊断、治疗建议或医生端推理。
```

## 12.4 AgentTraceCompletenessScorer

衡量：

```text
是否记录 agent_id、agent_version、policy_decision、validation_decision、accepted/rejected questions。
```

---

# 十三、回归测试要求

Phase 6-P0 完成前必须通过：

```text
mvn test
```

并且不得破坏：

```text
1. Runtime start / continue API。
2. SafetyGate 高风险兜底。
3. PatientOutput / ClinicianReport 输出隔离。
4. EvaluationRunner。
5. Candidate 生成与 Review。
6. PostgreSQL / in-memory 双模式测试。
7. Console API Safe DTO。
8. console-web npm run test / npm run build。
```

---

# 十四、人工测试场景

建议人工测试 3 个场景：

## 场景 1：高风险胸痛

输入：

```text
胸口闷，活动后更明显，出汗。
```

期望：

```text
Agent 优先追问持续时间、放射痛、呼吸困难、既往心血管史。
不输出心梗等具体诊断暗示给患者。
```

## 场景 2：普通发热

输入：

```text
发烧两天，咽痛，有点咳嗽。
```

期望：

```text
Agent 追问体温、持续时间、呼吸困难、皮疹、基础疾病等。
问题数量不超过限制。
```

## 场景 3：缺失信息为空

输入：

```text
missing_facts = []
```

期望：

```text
AgentPolicy 拒绝调用或返回无需追问。
```

---

# 十五、完成标准

Phase 6-P0 API 与测试完成标准：

```text
1. Debug API 可以运行 InquiryPlanningAgent。
2. Registry API 可以查看已注册 Agent。
3. 未授权访问被拒绝。
4. PATIENT 角色无法调用 Agent API。
5. 非法 Proposal 被 Validator 拒绝。
6. AgentTrace 可查询或进入 RuntimeTrace。
7. Agent 相关 Scorer 可以参与 Evaluation。
8. 所有新增测试通过。
9. Phase 1–5 既有测试不回归。
10. 人工测试记录可写入 Phase6_P0人工测试结果.md 或冻结记录。
```

---

# 十六、最终结论

Phase 6-P0 的 API 与测试重点不是“让 Agent 更聪明”，而是证明：

```text
Agent 被允许时才能执行，
Agent 执行后只能返回 Proposal，
Proposal 必须被校验，
危险 Proposal 可以被拒绝，
执行过程可以追踪，
结果可以被评估，
患者端边界不会被破坏。
```
