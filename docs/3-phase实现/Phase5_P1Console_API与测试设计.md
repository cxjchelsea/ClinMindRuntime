# Phase 5-P1 Console API 与测试设计

> 本文档定义 Phase 5-P1 的 Console API、响应边界、安全 DTO、测试分层和人工验收要求。  
> P1 Console 是 internal/debug governance console API，不是正式前端产品，不提供完整医生审核平台。

---

# 一、API 原则

```text
1. 继续使用 /api/v1/debug/** 前缀。
2. 新增 /api/v1/debug/console/** 作为治理查询入口。
3. 所有 Console API 必须经过 DebugTokenFilter 与 AccessPolicy。
4. 所有输出必须经过 SafeConsoleDtoMapper。
5. 不新增 patient-facing API。
6. 不暴露未脱敏患者输入、clinician_report、raw candidate input。
7. 查询和 review 行为写入 AuditLog。
```

---

# 二、Console Runtime API

## 2.1 List runtime sessions

```http
GET /api/v1/debug/console/runtime-sessions?status=&session_id=&limit=50
```

返回字段：

```text
runtime_id
session_id
mode
runtime_status
asset_package_id
asset_package_version
created_at
updated_at
trace_count
```

## 2.2 Get runtime detail

```http
GET /api/v1/debug/console/runtime-sessions/{runtime_id}
```

返回安全 detail：

```text
runtime summary
status timeline
asset refs
trace module summary
operation count
```

不返回患者原文。

---

# 三、Console Evaluation API

```http
GET /api/v1/debug/console/evaluation-runs?status=&case_set_id=&limit=50
GET /api/v1/debug/console/evaluation-runs/{run_id}
```

返回：

```text
run_id
case_set_id
case_set_version
asset_package_id
asset_package_version
status
pass_rate
total_cases
failed_cases
major_findings summary
```

---

# 四、Console Candidate API

```http
GET /api/v1/debug/console/candidate-generations?source_evaluation_run_id=&limit=50
GET /api/v1/debug/console/candidates?kind=&review_status=&risk_level=&limit=50
GET /api/v1/debug/console/candidates/{candidate_id}
GET /api/v1/debug/console/review-queue?kind=&risk_level=&task_type=&limit=50
```

Candidate summary 返回：

```text
candidate_id
candidate_kind
candidate_type / task_type
risk_level
review_status
sanitization_status
source_type
source_evaluation_run_id
asset_package_id
asset_package_version
created_at
```

Candidate detail 可以返回 source_ref summary 和 sanitized input summary，但不返回 raw input。

---

# 五、Console Review API

P1 review 操作可继续复用既有 Phase4-P1 API，也可以新增 console alias：

```http
POST /api/v1/debug/console/candidates/{candidate_id}/review
```

P1-A/P1-B 阶段可以暂不新增 alias，先对既有 review API 加 AccessPolicy。

---

# 六、Audit Center API

```http
GET /api/v1/debug/console/audit-center/audit-logs
GET /api/v1/debug/console/audit-center/audit-logs/{audit_id}
GET /api/v1/debug/console/audit-center/summary
```

查询参数：

```text
actor
action_type
resource_type
resource_id
result_status
from
to
limit
page 或 cursor
```

---

# 七、Safe DTO Mapper

建议新增：

```text
ConsoleRuntimeDtoMapper
ConsoleEvaluationDtoMapper
ConsoleCandidateDtoMapper
ConsoleReviewDtoMapper
ConsoleAuditDtoMapper
```

或统一为：

```text
SafeConsoleDtoMapper
```

P1 完成标准不是 DTO 是否拆得多，而是：

```text
任何 Console API 都不直接返回 domain object / snapshot raw json。
```

---

# 八、测试分层

## 8.1 Unit Test

```text
ActorContextResolverTest
AccessPolicyTest
SafeConsoleDtoMapperTest
ConsoleQueryParamValidatorTest
AuditCenterQueryMapperTest
```

## 8.2 Controller Test

```text
ConsoleRuntimeControllerTest
ConsoleEvaluationControllerTest
ConsoleCandidateControllerTest
ConsoleAuditCenterControllerTest
ConsoleAccessDeniedControllerTest
```

## 8.3 Integration Test

```text
ConsoleCandidateReviewAccessIntegrationTest
ConsoleAuditTrailIntegrationTest
ConsoleSensitiveFieldRedactionIntegrationTest
```

## 8.4 Postgres E2E

```text
Phase5P1ConsolePostgresEndToEndIntegrationTest
```

覆盖链路：

```text
Postgres mode
→ create evaluation run
→ generate candidates
→ list review queue
→ review candidate with CANDIDATE_REVIEWER
→ query audit center with AUDIT_REVIEWER
→ verify sensitive fields absent
```

---

# 九、人工验收建议

新增：

```text
docs/Phase5_P1人工测试API结果.md
```

验收场景：

```text
1. READ_ONLY_OBSERVER 可查询有限 summary。
2. READ_ONLY_OBSERVER 不能 review candidate。
3. CANDIDATE_REVIEWER 可查询 review queue 并 review。
4. AUDIT_REVIEWER 可查询 audit center。
5. 无 token 请求 debug console 返回 401。
6. token 有效但 role 不足返回 403。
7. Console candidate detail 不返回 raw training input。
8. Console runtime detail 不返回患者原文。
9. Console 查询和 review 产生 AuditLog。
10. postgres 模式 E2E 通过。
```

---

# 十、错误码

```text
CONSOLE_RESOURCE_NOT_FOUND
CONSOLE_QUERY_INVALID
ACCESS_DENIED
ACTOR_CONTEXT_REQUIRED
INVALID_DEBUG_ROLE
AUDIT_QUERY_INVALID
SENSITIVE_FIELD_BLOCKED
```

---

# 十一、最终结论

Phase 5-P1 Console API 的重点不是“页面漂亮”，而是让治理对象可以被安全、可控、可审计地查询。

P1 的完成标志是：

```text
可查、可控、可审计、不泄露、不越界。
```
