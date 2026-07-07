# Phase 10-P0 Console API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`  
> 前置冻结：`docs/3-phase实现/Phase9_P0冻结记录.md`  
> 当前 Phase：Phase 10-P0  
> 当前目标：定义 Governance Console / Runtime Console 的 Console API、Safe DTO、前端页面、Access Policy、测试和验收方案。

---

# 一、API 设计原则

Phase 10-P0 API 是 console read-only API，不是 patient-facing API，也不是生产级审核 API。

共同原则：

```text
1. PATIENT 角色禁止访问。
2. P0 默认只读，不提供 approve / reject / publish / run 操作。
3. 所有 API 必须返回 Safe DTO。
4. 不返回 raw patient dialogue。
5. 不返回完整 prompt 原文。
6. 不返回 secret / api key / private key。
7. 不返回 raw external response。
8. 不返回完整内部推理链。
9. Console 不触发 Provider / Tool / Agent 执行。
10. Console 不修改 RuntimeState。
```

---

# 二、API 分组

推荐路径前缀：

```text
/api/v1/console
```

端点：

```text
GET /overview
GET /runtimes
GET /runtimes/{runtime_id}/timeline
GET /governance/domains
GET /candidates
GET /audits
```

---

# 三、API 1：GET /overview

用途：治理总览。

响应示例：

```json
{
  "runtime_count": 12,
  "active_runtime_count": 2,
  "agent_invocation_count": 8,
  "provider_call_count": 15,
  "tool_invocation_count": 6,
  "model_governance_record_count": 9,
  "evaluation_result_count": 20,
  "candidate_count": 5,
  "audit_event_count": 80,
  "latest_phase": "Phase 9-P0",
  "health_status": "OK",
  "warnings": []
}
```

测试点：

```text
1. SYSTEM_ADMIN 可访问。
2. READ_ONLY_OBSERVER 可访问。
3. PATIENT 被拒绝。
4. 不包含 raw text / prompt / secret。
```

---

# 四、API 2：GET /runtimes

用途：Runtime 列表。

查询参数：

```text
status
mode
risk_level
limit
offset
```

响应字段：

```text
runtime_id
session_id
status
mode
created_at
updated_at
latest_safety_status
latest_candidate_count
latest_audit_count
summary
```

要求：

```text
summary 必须是脱敏摘要。
不返回 inputHistory 原文。
```

---

# 五、API 3：GET /runtimes/{runtime_id}/timeline

用途：Runtime 详情时间线。

响应示例：

```json
{
  "runtime_id": "runtime_001",
  "session_id": "session_001",
  "status": "COMPLETED",
  "mode": "PATIENT_FACING",
  "nodes": [
    {
      "node_id": "node_safety_001",
      "node_type": "SAFETY_GATE",
      "title": "SafetyGate",
      "status": "PASSED",
      "severity": "INFO",
      "summary": "No high-risk red flag triggered.",
      "safe_payload": {
        "risk_level": "LOW"
      },
      "related_trace_ids": [],
      "related_audit_ids": [],
      "created_at": "2026-07-07T10:00:00Z"
    }
  ],
  "warnings": []
}
```

Timeline node types：

```text
ENTRY_ASSESSMENT
SAFETY_GATE
AGENT_ORCHESTRATION
RAG_EVIDENCE
GRAPH_EVIDENCE
PROVIDER_ENHANCEMENT
PROVIDER_GOVERNANCE
MODEL_GOVERNANCE
TOOL_GOVERNANCE
DECISION_BOUNDARY
PATIENT_OUTPUT
CLINICIAN_REPORT
EVALUATION
CANDIDATE
AUDIT
```

测试点：

```text
1. 存在 Runtime 返回 timeline。
2. 不存在 Runtime 返回受控错误。
3. 不返回 raw input。
4. 不返回 prompt 原文。
5. 不返回 raw external response。
6. 节点顺序稳定。
```

---

# 六、API 4：GET /governance/domains

用途：治理域卡片。

响应：

```json
[
  {
    "domain": "PROVIDER_GOVERNANCE",
    "status": "OK",
    "summary": "Provider governance active.",
    "policy_rejected_count": 1,
    "validation_rejected_count": 0,
    "fallback_count": 2,
    "review_required_count": 1,
    "latest_updated_at": "2026-07-07T10:00:00Z"
  }
]
```

Domain 候选：

```text
AGENT_GOVERNANCE
EVIDENCE_GOVERNANCE
PROVIDER_GOVERNANCE
MODEL_GOVERNANCE
TOOL_GOVERNANCE
EVALUATION_GOVERNANCE
CANDIDATE_GOVERNANCE
AUDIT_GOVERNANCE
```

---

# 七、API 5：GET /candidates

用途：Candidate Inbox。

查询参数：

```text
review_status
risk_level
candidate_type
source_type
limit
offset
```

响应字段：

```text
candidate_id
candidate_type
risk_level
review_status
source_type
source_runtime_id
source_metric_id
summary
created_at
```

P0 只读。

禁止：

```text
approve
reject
publish
apply
```

---

# 八、API 6：GET /audits

用途：Audit Browser。

查询参数：

```text
action_type
resource_type
actor_id
status
limit
offset
```

响应字段：

```text
audit_id
action_type
resource_type
resource_id
actor_id
status
safe_metadata_summary
created_at
```

要求：

```text
safe_metadata_summary 必须裁剪。
敏感 key 统一显示 [REDACTED]。
```

---

# 九、Safe DTO 测试

## 9.1 ConsoleSafeDtoMapperTest

覆盖：

```text
1. rawPatientDialogue 被过滤。
2. raw_prompt / prompt_text 被过滤。
3. secret / api_key / private_key 被过滤。
4. raw_external_response 被过滤。
5. internal_chain_of_thought / full_rationale 被过滤。
6. 普通安全字段保留。
```

## 9.2 ConsoleTimelineSafeDtoTest

覆盖：

```text
1. timeline node safe_payload 不含敏感字段。
2. PatientOutput 节点只显示最终已边界控制后的内容摘要。
3. Provider / Tool 节点不泄露 rationale / raw response。
```

---

# 十、Access Policy 测试

## 10.1 ConsoleAccessPolicyTest

覆盖：

```text
1. SYSTEM_ADMIN 可读全部 Console API。
2. EVALUATION_REVIEWER 可读全部 Console API。
3. READ_ONLY_OBSERVER 可读全部 Console API。
4. PATIENT 访问全部拒绝。
5. 无 actor 访问拒绝或降级为 unauthorized。
```

---

# 十一、Console API 测试

建议测试类：

```text
ConsoleOverviewControllerTest
RuntimeTimelineControllerTest
GovernanceDashboardControllerTest
ConsoleCandidateInboxControllerTest
ConsoleAuditBrowserControllerTest
```

覆盖：

```text
1. API 返回 200。
2. DTO 字段完整。
3. Safe DTO 不泄露敏感信息。
4. PATIENT 被拒绝。
5. 空数据返回空列表，不抛异常。
6. 不存在 runtime 返回受控错误。
```

---

# 十二、前端测试

如果修改 `console-web`，必须覆盖：

```text
npm run test
npm run build
```

推荐测试：

```text
OverviewPage renders metrics。
RuntimeTimelinePage renders timeline nodes。
GovernanceDashboardPage renders domain cards。
CandidateInboxPage renders candidate list。
AuditBrowserPage renders audit list。
SafeJsonPanel masks sensitive keys。
```

---

# 十三、Evaluation Scorer 测试

新增：

```text
ConsoleSafeDtoScorerTest
ConsoleTimelineCompletenessScorerTest
```

可选：

```text
ConsoleAuditVisibilityScorerTest
ConsoleCandidateInboxScorerTest
```

覆盖：

```text
1. Console DTO 含敏感字段时失败。
2. Runtime timeline 缺 SafetyGate / DecisionBoundary 时失败。
3. Candidate Inbox 不可见时失败。
4. Audit Browser 不可见时失败。
```

---

# 十四、人工测试场景

## 场景 1：Overview Dashboard

预期：

```text
可以看到 Runtime / Provider / Tool / Candidate / Audit 汇总指标。
不显示敏感内容。
```

## 场景 2：Runtime Timeline

预期：

```text
按顺序展示 SafetyGate、Agent、Evidence、Provider、Tool、DecisionBoundary、Evaluation、Candidate。
不展示 raw input / prompt / raw external response。
```

## 场景 3：Governance Domain Cards

预期：

```text
展示 Agent / Evidence / Provider / ModelGov / ToolGov / Evaluation / Candidate / Audit 状态。
```

## 场景 4：Candidate Inbox

预期：

```text
只读展示 review-required candidate。
不允许 approve / reject / publish。
```

## 场景 5：Audit Browser

预期：

```text
展示 action / resource / actor / status / safe metadata summary。
敏感字段被 [REDACTED]。
```

## 场景 6：PATIENT 访问 Console

预期：

```text
全部拒绝。
```

---

# 十五、回归测试要求

Phase 10-P0 完成前必须通过：

```text
mvn test
```

如果修改 `console-web`：

```text
npm run test
npm run build
```

不得破坏：

```text
Phase 9-P0 Tool Governance
Phase 8-P2 Model Governance
Phase 8-P1 Provider Governance
Phase 8-P0 Python Provider
Phase 7 Evidence / Graph
Phase 6 Agent Runtime
SafetyGate
DecisionBoundary
EvaluationRunner
Candidate / Review
Persistence / Audit
```

---

# 十六、完成标准

Phase 10-P0 API 与测试完成标准：

```text
1. Console Overview API 可用。
2. Runtime Timeline API 可用。
3. Governance Domains API 可用。
4. Candidate Inbox API 可用。
5. Audit Browser API 可用。
6. Safe DTO mapper 可过滤敏感字段。
7. PATIENT 被拒绝。
8. Console API 测试通过。
9. Console Scorer 测试通过。
10. 如果修改前端，npm test / build 通过。
11. mvn test 通过。
12. Phase 1–9 P0 既有测试不回归。
```

---

# 十七、最终结论

Phase 10-P0 API 与测试重点不是“做一个漂亮后台”，而是证明：

```text
Runtime 和治理闭环可以被统一观察；
所有展示都经过 Safe DTO；
Console 不接管 Runtime，不执行外部能力，不发布治理对象。
```
