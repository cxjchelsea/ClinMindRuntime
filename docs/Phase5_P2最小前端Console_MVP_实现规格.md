# Phase 5-P2：最小前端 Console MVP 实现规格

> 本文档定义 ClinMindRuntime Phase 5-P2 的目标、边界、主链路和实现范围。  
> Phase 5-P2 选择的主线是 **最小前端 Console MVP**。原因是 Phase 5-P0 已完成持久化底座，Phase 5-P1 已完成 Console API / RBAC-lite / Audit Center；下一步最需要把这些治理能力变成可展示、可操作、可复盘的界面，而不是继续堆后端概念。

---

# 一、为什么 Phase 5-P2 选择最小前端 Console

当前系统已经具备：

```text
Runtime / Evaluation / Candidate / Review / AuditLog 持久化
Console API
RBAC-lite
Safe DTO
Audit Center 查询
Postgres E2E
```

但仍存在一个明显短板：

```text
治理对象虽然已经可查询、可审计、可权限控制，但只能通过 API / 测试验证，缺少可以直接展示系统治理能力的最小界面。
```

因此 Phase 5-P2 的优先级判断是：

```text
最小前端 Console > Docker Compose > 正式 RBAC > RAG / 模型训练
```

理由：

```text
1. P1 已经有 Console API，前端是自然下一步。
2. 前端 Console 对面试 / 展示价值最高。
3. 它能把 Runtime、Evaluation、Candidate、AuditLog 的治理链路串起来。
4. 它不需要改变 Runtime 决策边界，也不需要接入模型能力。
5. Docker Compose 很重要，但对当前“项目可见度”的提升不如 Console MVP。
```

Phase 5-P2 的一句话目标：

```text
实现一个最小可用的前端治理 Console，用页面展示 Runtime、Evaluation、Candidate、Review Queue 和 Audit Center，让 Phase 5-P1 已有的安全 Console API 具备可视化入口，但不实现完整产品化后台。
```

---

# 二、Phase 5-P2 主链路

```text
Frontend Console
→ Debug Token / Actor / Roles 输入
→ Console API Client
→ /api/v1/debug/console/**
→ Safe DTO Response
→ 页面列表 / 详情 / Review 操作
→ AuditLog 记录查询与操作
```

前端只消费 Phase 5-P1 已经暴露的安全 DTO，不直接访问数据库、不调用非 Console API 获取敏感 raw data。

---

# 三、Phase 5-P2 不是什么

Phase 5-P2 不是：

```text
1. 不是完整生产前端。
2. 不是正式登录系统。
3. 不是 OAuth / JWT / 多租户。
4. 不是正式医生审核平台。
5. 不是完整设计系统 / 组件库。
6. 不是移动端适配。
7. 不是 RAG / GraphRAG。
8. 不是模型训练 / 后训练。
9. 不是 ApprovedExperience 自动上线。
10. 不是 TrainingDatasetVersion 发布。
```

Phase 5-P2 只做：

```text
最小前端工程
Console API client
Debug token / actor / roles 配置栏
Runtime 页面
Evaluation 页面
Candidate / Review Queue 页面
Audit Center 页面
基础错误态 / loading 态 / 空态
最小前端测试与手工验收
```

---

# 四、推荐技术选型

推荐：

```text
Frontend：React + TypeScript + Vite
UI：原生 CSS / 简单组件，不引入重型 UI 框架
State：React useState / useMemo / lightweight hooks
HTTP：fetch 封装
Routing：react-router-dom 或轻量 hash routing
Test：Vitest + React Testing Library
```

P2 不建议一开始使用复杂方案：

```text
Next.js SSR
完整权限系统
复杂组件库
全局状态管理 Redux / MobX
微前端
后端 BFF
```

理由：当前目标是治理 Console MVP，不是产品级前端平台。

---

# 五、前端目录建议

推荐新增目录：

```text
console-web/
  package.json
  index.html
  vite.config.ts
  tsconfig.json
  src/
    main.tsx
    App.tsx
    api/
      consoleClient.ts
      types.ts
    auth/
      DebugContextPanel.tsx
      debugContext.ts
    layout/
      AppShell.tsx
      Sidebar.tsx
    pages/
      RuntimePage.tsx
      EvaluationPage.tsx
      CandidatePage.tsx
      ReviewQueuePage.tsx
      AuditCenterPage.tsx
    components/
      DataTable.tsx
      DetailPanel.tsx
      StatusBadge.tsx
      ErrorBanner.tsx
      EmptyState.tsx
    tests/
```

P2 不要求前端代码进入 Spring Boot 静态资源打包。

---

# 六、页面范围

## 6.1 Runtime 页面

```text
GET /api/v1/debug/console/runtime-sessions
GET /api/v1/debug/console/runtime-sessions/{runtime_id}
```

展示：

```text
runtime_id
session_id
runtime_status
mode
asset_package_id / version
trace_count
created_at / updated_at
```

详情展示：

```text
runtime summary
safety_triggered
trace_count
asset refs
```

不展示患者原文。

## 6.2 Evaluation 页面

```text
GET /api/v1/debug/console/evaluation-runs
GET /api/v1/debug/console/evaluation-runs/{run_id}
```

展示：

```text
run_id
case_set_id / version
asset_package_id / version
status
pass_rate
total / passed / failed
item summary
```

## 6.3 Candidate 页面

```text
GET /api/v1/debug/console/candidate-generations
GET /api/v1/debug/console/candidates
GET /api/v1/debug/console/candidates/{candidate_id}
```

展示：

```text
candidate_id
candidate_kind
candidate_type / task_type
risk_level
review_status
sanitization_status
source_ref summary
created_at
```

详情只展示 safe detail，不展示 raw training input。

## 6.4 Review Queue 页面

```text
GET /api/v1/debug/console/review-queue
POST /api/v1/debug/candidates/.../review
```

支持：

```text
按 kind / risk_level / task_type 过滤
查看候选安全详情
approve / reject / deprecate 操作
显示 review 结果
```

注意：Review 操作仍不代表候选自动上线。

## 6.5 Audit Center 页面

```text
GET /api/v1/debug/console/audit-center/audit-logs
GET /api/v1/debug/console/audit-center/audit-logs/{audit_id}
GET /api/v1/debug/console/audit-center/summary
```

展示：

```text
count_by_action_type
count_by_resource_type
count_by_result_status
recent_failures
recent_review_actions
audit log list
```

---

# 七、安全边界

P2 前端必须遵守：

```text
1. 所有请求都带 X-Debug-Token。
2. 所有请求都带 X-Debug-Actor / X-Debug-Roles。
3. 前端不得调用非 Console API 获取 raw RuntimeState / raw Candidate input。
4. 前端不得缓存患者原文。
5. 前端不得伪造“正式医生审核平台”语义。
6. Review 按钮文案必须说明“仅更新 candidate review_status，不自动生效”。
```

---

# 八、P2 完成标准

Phase 5-P2 完成需要满足：

```text
1. console-web 可本地启动。
2. 可配置 API base URL、debug token、actor、roles。
3. Runtime / Evaluation / Candidate / Review Queue / Audit Center 页面可访问。
4. Review Queue 可以执行最小 review 操作。
5. 所有页面只消费 Safe DTO。
6. 无权限 / token 错误 / 空数据 / 后端异常有明确 UI 提示。
7. 前端基础测试通过。
8. 后端 Phase 1–5 回归不被破坏。
9. 新增 docs/Phase5_P2人工测试结果.md。
```

---

# 九、最终结论

Phase 5-P2 的核心不是继续扩 Runtime，也不是引入模型，而是把已经完成的治理能力变成一个可见、可演示、可复盘的最小 Console。

```text
P0：治理对象可持久化
P1：治理对象可安全查询和审计
P2：治理对象可通过最小前端 Console 展示和操作
```
