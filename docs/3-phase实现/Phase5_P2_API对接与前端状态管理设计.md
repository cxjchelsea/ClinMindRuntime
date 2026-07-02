# Phase 5-P2 API 对接与前端状态管理设计

> 本文档定义 Phase 5-P2 前端如何对接 Phase 5-P1 Console API、如何管理 debug context、请求状态、错误状态和 review 操作。  
> P2 不新增后端 API，优先消费已有 `/api/v1/debug/console/**` 与既有 candidate review API。

---

# 一、API Client 原则

前端统一通过 `consoleClient.ts` 访问后端。

所有请求必须带：

```text
X-Debug-Token
X-Debug-Actor
X-Debug-Roles
X-Request-Id
```

P2 不做：

```text
cookie session
JWT refresh
OAuth callback
CSRF
多租户 token
```

---

# 二、Debug Context 状态

建议前端状态：

```ts
interface DebugContext {
  apiBaseUrl: string;
  debugToken: string;
  actor: string;
  roles: string[];
}
```

默认值：

```text
apiBaseUrl: http://localhost:8080
actor: console-user
roles: READ_ONLY_OBSERVER
```

可选：将 debug context 保存到 `localStorage`，但不建议默认保存 debug token。若保存 token，必须在 UI 中明确提示。

---

# 三、API 响应类型

后端统一响应：

```ts
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
  };
}
```

前端必须统一处理：

```text
success=false
HTTP 401
HTTP 403
HTTP 404
HTTP 400
network error
JSON parse error
```

---

# 四、API Client 方法

推荐方法：

```ts
listRuntimeSessions(params)
getRuntimeSession(runtimeId)
listEvaluationRuns(params)
getEvaluationRun(runId)
listCandidateGenerations(params)
listCandidates(params)
getCandidate(candidateId)
listReviewQueue(params)
reviewExperienceCandidate(candidateId, body)
reviewTrainingExampleCandidate(candidateId, body)
listAuditLogs(params)
getAuditLog(auditId)
getAuditSummary()
```

Review API 注意：

```text
experience candidate 与 training example candidate 的 review endpoint 不同。
前端需要根据 candidate_kind 选择 endpoint。
```

---

# 五、页面状态模型

每个页面使用统一状态：

```ts
interface PageState<T> {
  loading: boolean;
  data: T | null;
  error: ConsoleError | null;
  selectedId?: string;
}
```

列表 + 详情页面建议拆分：

```text
listState
detailState
filters
selectedId
```

---

# 六、错误处理策略

错误码映射：

```text
DEBUG_TOKEN_REQUIRED：请填写 X-Debug-Token
INVALID_DEBUG_TOKEN：Debug token 错误
ACCESS_DENIED：当前角色无权访问该资源
CONSOLE_RESOURCE_NOT_FOUND：资源不存在
CONSOLE_QUERY_INVALID：查询参数错误
AUDIT_QUERY_INVALID：Audit 查询参数错误
NETWORK_ERROR：后端未启动或 API 地址错误
```

UI 展示建议：

```text
401/403：显示权限错误和当前 roles
404：详情面板显示资源不存在
400：过滤器附近显示参数错误
network error：全局 ErrorBanner
```

---

# 七、Review 操作状态

Review 操作状态：

```ts
interface ReviewFormState {
  decision: 'APPROVE' | 'REJECT' | 'DEPRECATE';
  reason: string;
  reviewer: string;
  submitting: boolean;
  error: ConsoleError | null;
}
```

提交后：

```text
1. 调用对应 review API。
2. 成功后刷新 review queue。
3. 成功后刷新 candidate detail。
4. 显示 review_status 更新结果。
5. 不显示“已上线 / 已进入训练集”等误导性文案。
```

---

# 八、过滤参数规范

Runtime：

```text
status
session_id
limit
```

Evaluation：

```text
status
case_set_id
limit
```

Candidates：

```text
kind
review_status
risk_level
limit
```

Review Queue：

```text
kind
risk_level
task_type
limit
```

Audit Center：

```text
actor
action_type
resource_type
resource_id
result_status
from
to
limit
```

P2 前端只做简单表单，不做复杂查询构造器。

---

# 九、测试策略

## 9.1 API Client Test

```text
请求 header 正确
success=false 正确转错误
401 / 403 / 404 正确映射
review endpoint 根据 candidate_kind 选择正确
```

## 9.2 Hook / Page State Test

```text
loading 状态
error 状态
空态
刷新详情
review 成功后刷新列表
```

## 9.3 Mock API Test

使用 mock fetch 或 MSW：

```text
Runtime page list/detail
Candidate page list/detail
Review queue approve/reject
Audit center summary/list
```

---

# 十、最终结论

P2 的前端状态管理不需要复杂化。

正确原则是：

```text
统一 API Client
统一 Debug Context
统一错误处理
页面局部状态
最小 review mutation
```
