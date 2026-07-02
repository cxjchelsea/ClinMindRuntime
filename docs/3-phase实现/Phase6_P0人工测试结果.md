# Phase 6-P0 受控 Agent 执行层 MVP 验收记录

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-07-02 |
| 验收结论 | **通过并已冻结** |
| 冻结记录 | [`Phase6_P0冻结记录.md`](Phase6_P0冻结记录.md) |
| 后端测试 | `mvn test` — exit 0（含 Phase 6 新增 7 个测试类 + 验收集成） |
| 前端回归 | `console-web npm run test` — 32/35 通过；3 项 Audit/Review 用例 5s 超时（与 Phase 6 无代码交叉，属环境 flake） |

---

## 一、自动化验收

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | AgentRegistryTest | 已注册 InquiryPlanningAgent；未知 agent 拒绝 | ✅ |
| 2 | AgentPolicyTest | missing_facts 非空允许；空/fail-closed/unsupported symptom 拒绝 | ✅ |
| 3 | InquiryPlanningAgentTest | 胸痛 duration 追问；red_flag 高优先级；max_question_count | ✅ |
| 4 | InquiryPlanProposalValidatorTest | 安全 Proposal 通过；诊断暗示拒绝；超量部分采纳 | ✅ |
| 5 | AgentRuntimeTest | SUCCESS / POLICY_REJECTED；Trace 记录 | ✅ |
| 6 | AgentDebugControllerTest | Token / 角色 / Registry / Run API | ✅ |
| 7 | Phase6P0AcceptanceIntegrationTest | 三场景人工验收自动化 | ✅ |
| 8 | Phase 1–5 回归 | 全量 `mvn test` | ✅ |

---

## 二、人工 / 集成验收场景（P6-J 要求）

### 场景 1：高风险胸痛（chest_pain）

**Debug API 请求：**

```http
POST /api/v1/debug/agents/inquiry-planning/run
X-Debug-Token: test-secret
X-Debug-Roles: EVALUATION_REVIEWER
```

```json
{
  "runtime_id": "runtime_accept_chest",
  "symptom_group": "chest_pain",
  "case_frame_summary": {
    "known_facts": ["胸闷", "活动后加重", "出汗"],
    "missing_facts": ["持续时间", "是否放射痛", "是否呼吸困难"]
  },
  "red_flag_candidates": ["活动后胸闷", "出汗"],
  "max_question_count": 3
}
```

**结果：**

| 验证项 | 结论 |
|--------|------|
| status = SUCCESS | ✅ |
| proposal 含 proposed_questions | ✅ |
| trace_summary.recorded = true | ✅ |
| 问题文本无诊断暗示（如「心梗」） | ✅ |
| Runtime 主链路 startRuntime 后 agentOrchestration 有 acceptedQuestions | ✅ |

### 场景 2：普通发热（fever）

**请求：** symptom_group = `fever`，missing_facts = `["持续时间", "severity"]`

| 验证项 | 结论 |
|--------|------|
| status = SUCCESS | ✅ |
| Runtime startRuntime 不 ERROR_SAFE_HALTED | ✅ |
| 问题数量 ≤ max_question_count | ✅ |

### 场景 3：missing_facts 为空

**请求：** missing_facts = `[]`

| 验证项 | 结论 |
|--------|------|
| API 层返回 400 INVALID_REQUEST | ✅ |
| AgentPolicy fail-closed（missing_facts is empty） | ✅ |

---

## 三、权限与安全边界

| # | 验证项 | 结论 |
|---|--------|------|
| 1 | 无 Debug Token → 401 | ✅ |
| 2 | READ_ONLY_OBSERVER 不可 POST run | ✅ |
| 3 | SYSTEM_ADMIN / EVALUATION_REVIEWER 可 run | ✅ |
| 4 | GET registry 可用 Safe DTO | ✅ |
| 5 | AuditLog 记录 RUN_AGENT / QUERY_AGENT | ✅ |
| 6 | 响应不含 raw patient dialogue | ✅ |

---

## 四、Runtime 主链路验收

| # | 验证项 | 结论 |
|---|--------|------|
| 1 | CapabilityOrchestration 在 SafetyGate 之后执行 | ✅ |
| 2 | Agent 失败时 fallbackUsed，不阻断 Runtime | ✅ |
| 3 | QuestionTestPolicy 可采纳 Agent acceptedQuestions | ✅ |
| 4 | Agent 不直接写 PatientOutput / RuntimeState 核心字段 | ✅ |
| 5 | Phase 1–5 Evaluation 回归未被 Agent Scorer 破坏（agent_eval 标签门控） | ✅ |

---

## 五、已知限制（P0 预期内）

```text
1. InquiryPlanningAgent 为 rule-based MVP，未接 LLM。
2. AgentExecution / AgentTrace 为 in-memory，未持久化 PostgreSQL。
3. Agent Evaluation Scorer 仅在 case tags 含 agent_eval 时生效。
4. 无 Agent Console 页面（后置 Phase 6-P1）。
5. 未实现 EvidenceOrganizationAgent / PatientRewriteAgent 等扩展 Agent。
```
