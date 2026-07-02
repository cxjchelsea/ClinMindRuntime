# Phase 5-P1 RBAC-lite 与 Audit Center 设计

> 本文档细化 Phase 5-P1 的 actor / role 上下文、访问策略、错误码、Audit Center 查询和安全审计要求。  
> P1 不是正式登录系统或企业级 RBAC，而是在 DebugToken 基础上建立最小访问治理边界。

---

# 一、为什么需要 RBAC-lite

Phase 5-P0 已经实现 DebugTokenFilter，但它主要解决入口保护问题：

```text
是否允许访问 /api/v1/debug/**
```

Phase 5-P1 需要进一步回答：

```text
1. 这个 actor 能不能看 Runtime summary？
2. 能不能看 Candidate detail？
3. 能不能 review candidate？
4. 能不能查询 AuditLog？
5. 查询行为本身是否被审计？
```

因此 P1 增加 RBAC-lite：

```text
Debug token：入口门禁
ActorContext：谁在请求
RolePolicy：这个人是什么角色
AccessPolicy：这个角色能对什么资源做什么动作
AuditLog：这个访问是否被记录
```

---

# 二、ActorContext

字段建议：

```text
actor_id
actor_name
roles
request_id
source_ip（可选）
user_agent（可选）
resolved_at
```

P1 解析方式：

```text
X-Debug-Actor: alice
X-Debug-Roles: CANDIDATE_REVIEWER,AUDIT_REVIEWER
X-Request-Id: req_xxx
```

默认策略：

```text
没有 actor：system-debug
没有 roles：READ_ONLY_OBSERVER
```

---

# 三、角色定义

P1 支持：

```text
SYSTEM_ADMIN
EVALUATION_REVIEWER
CANDIDATE_REVIEWER
AUDIT_REVIEWER
READ_ONLY_OBSERVER
```

角色含义：

```text
SYSTEM_ADMIN：P1 内所有 debug console 能力。
EVALUATION_REVIEWER：查看 evaluation run / result / item summary。
CANDIDATE_REVIEWER：查看 candidate、review queue，并执行 candidate review。
AUDIT_REVIEWER：查看 audit logs 和 audit summary。
READ_ONLY_OBSERVER：查看有限 summary，不允许 review 或 audit detail。
```

---

# 四、资源与动作

资源类型：

```text
CONSOLE_RUNTIME
CONSOLE_EVALUATION
CONSOLE_CANDIDATE
CONSOLE_REVIEW
CONSOLE_AUDIT
CONSOLE_SYSTEM
```

动作类型：

```text
READ_SUMMARY
READ_DETAIL
LIST
REVIEW
READ_AUDIT
READ_HEALTH
```

---

# 五、AccessPolicy 建议矩阵

| Role | Runtime | Evaluation | Candidate | Review | Audit | System health |
|---|---|---|---|---|---|---|
| SYSTEM_ADMIN | summary/detail | summary/detail | summary/detail | review | audit | health |
| EVALUATION_REVIEWER | summary | summary/detail | none | none | none | health |
| CANDIDATE_REVIEWER | none | summary | summary/detail | review | none | health |
| AUDIT_REVIEWER | none | none | none | none | audit | health |
| READ_ONLY_OBSERVER | summary | summary | summary | none | none | health |

P1 可以用 hard-coded policy，不接数据库权限表。

---

# 六、错误码

新增错误码：

```text
ACTOR_CONTEXT_REQUIRED
INVALID_DEBUG_ROLE
ACCESS_DENIED
CONSOLE_RESOURCE_NOT_FOUND
CONSOLE_QUERY_INVALID
AUDIT_QUERY_INVALID
```

状态码建议：

```text
401：缺 token / token 错误
403：token 有效但 role 不允许
404：资源不存在
400：查询参数非法
```

---

# 七、Audit Center 查询设计

## 7.1 查询参数

```text
actor
action_type
resource_type
resource_id
result_status
from
to
limit
cursor 或 page
```

## 7.2 返回字段

```text
audit_id
actor
action_type
resource_type
resource_id
result_status
created_at
metadata_summary
```

不返回：

```text
完整 raw request
完整 patient input
完整 candidate input
完整 clinician report
```

## 7.3 Summary API

```http
GET /api/v1/debug/console/audit-center/summary
```

可返回：

```text
total_count
count_by_action_type
count_by_resource_type
count_by_result_status
recent_failures
recent_review_actions
```

---

# 八、访问行为审计

P1 中以下行为必须写 AuditLog：

```text
1. Console 查询 candidate detail。
2. Console 查询 review queue。
3. Console 查询 audit logs。
4. Console 执行 candidate review。
5. Access denied 事件（可选但建议）。
```

AuditLog metadata 只记录：

```text
query filters
resource id
result count
error code
```

不得记录敏感 payload。

---

# 九、测试设计

必须新增：

```text
ActorContextResolverTest
AccessPolicyTest
RolePolicyTest
ConsoleAccessDeniedTest
AuditCenterQueryTest
AuditCenterSummaryTest
ConsoleAuditIntegrationTest
```

覆盖：

```text
1. 无 token 返回 401。
2. token 有效但 role 不足返回 403。
3. CANDIDATE_REVIEWER 可看 candidate queue 并 review。
4. READ_ONLY_OBSERVER 不能 review。
5. AUDIT_REVIEWER 可查 audit logs。
6. AuditLog 查询不返回敏感 metadata。
7. Console 查询行为进入 AuditLog。
```

---

# 十、最终结论

Phase 5-P1 的 RBAC-lite 是最小治理边界，不是完整权限平台。

正确边界是：

```text
先区分 actor / role / action / resource，
再控制 Console API 能力，
最后让访问本身可审计。
```
