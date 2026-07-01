# Phase 5-P2 开发任务清单

> 本清单用于约束 Phase 5-P2 的实现顺序。  
> Phase 5-P2 只做最小前端 Console MVP，不实现完整生产前端、正式登录、多租户、RAG、模型训练、正式医生审核平台、ApprovedExperience 自动生效或 TrainingDatasetVersion 发布。

---

# 一、状态标记

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要决策
[-] 后置 / 不在 P2 范围
```

---

# 二、Phase 5-P2 目标

```text
实现一个最小可用的前端治理 Console，用页面展示 Runtime、Evaluation、Candidate、Review Queue 和 Audit Center，让 Phase 5-P1 已有的安全 Console API 具备可视化入口。
```

P2 必须保证：

```text
1. 前端只调用 Safe Console API。
2. 前端不展示患者原文、clinician_report、raw training input。
3. Review 操作仍不自动上线经验或进入训练集。
4. 权限由后端 RBAC-lite 判定，前端只做辅助显示。
5. 前端 MVP 可本地启动、测试和演示。
```

---

# 三、Phase5-P2-A：前端工程初始化

状态：`[x]`

任务：

```text
[x] 新增 console-web/ 目录
[x] Vite + React + TypeScript 初始化
[x] package.json / tsconfig / vite.config
[x] 基础目录结构
[x] AppShell / Sidebar / 基础样式
```

测试：

```text
[x] npm run build
[x] App smoke test
```

验收标准：

```text
1. console-web 可 npm install / npm run dev。
2. 页面可显示基本布局。
3. 不影响后端 mvn test。
```

---

# 四、Phase5-P2-B：Console API Client 与 Debug Context

状态：`[x]`

任务：

```text
[x] consoleClient.ts
[x] ApiResponse / Console DTO types
[x] DebugContextPanel
[x] apiBaseUrl / token / actor / roles 状态
[x] 请求统一 header 注入
[x] 错误码统一映射
```

测试：

```text
[x] consoleClient header test
[x] error mapping test
[x] DebugContextPanelTest
```

验收标准：

```text
1. 所有请求自动带 X-Debug-Token / X-Debug-Actor / X-Debug-Roles。
2. 401 / 403 / 404 / network error 可统一展示。
3. 不做真实登录。
```

---

# 五、Phase5-P2-C：Runtime / Evaluation 页面

状态：`[ ]`

任务：

```text
[ ] RuntimePage
[ ] Runtime list / detail
[ ] EvaluationPage
[ ] Evaluation list / detail
[ ] loading / error / empty state
```

测试：

```text
[ ] RuntimePageTest
[ ] EvaluationPageTest
[ ] SensitiveFieldRenderGuard for runtime
```

验收标准：

```text
1. 可查询 Runtime sessions。
2. 可查看 Runtime safe detail。
3. 可查询 Evaluation runs。
4. Runtime 页面不显示患者原文。
```

---

# 六、Phase5-P2-D：Candidate / Review Queue 页面

状态：`[ ]`

任务：

```text
[ ] CandidatePage
[ ] Candidate list / detail
[ ] ReviewQueuePage
[ ] review form
[ ] approve / reject / deprecate 操作
[ ] 操作后刷新 queue 和 candidate detail
[ ] 安全提示文案
```

测试：

```text
[ ] CandidatePageTest
[ ] ReviewQueueFlowTest
[ ] review endpoint selection test
[ ] SensitiveFieldRenderGuard for candidate
```

验收标准：

```text
1. 可查看 Candidate list/detail。
2. 可查看 Review Queue。
3. CANDIDATE_REVIEWER 可 review。
4. READ_ONLY_OBSERVER 操作返回 403 并展示错误。
5. 页面不显示 raw training input。
6. Review 成功后不显示“已上线”等误导文案。
```

---

# 七、Phase5-P2-E：Audit Center 页面

状态：`[ ]`

任务：

```text
[ ] AuditCenterPage
[ ] audit summary
[ ] audit log list
[ ] filters
[ ] audit detail
[ ] permission error display
```

测试：

```text
[ ] AuditCenterPageTest
[ ] AuditCenterFlowTest
[ ] PermissionErrorFlowTest
```

验收标准：

```text
1. AUDIT_REVIEWER 可查询 Audit Center。
2. 非 AUDIT_REVIEWER 返回 403 并展示错误。
3. 支持基础过滤。
4. 不展示敏感 metadata。
```

---

# 八、Phase5-P2-F：前端集成验收与归档

状态：`[ ]`

任务：

```text
[ ] ConsoleAppSmokeTest
[ ] SensitiveFieldRedactionRenderTest
[ ] npm run test
[ ] npm run build
[ ] docs/Phase5_P2人工测试结果.md
[ ] README / docs 导航状态同步
```

验收标准：

```text
1. 前端 build 通过。
2. 前端测试通过。
3. 连接本地后端可完成 Runtime / Evaluation / Candidate / Review / Audit Center 演示。
4. 后端回归记录补齐。
5. 人工验收记录补齐。
```

---

# 九、P2 完成定义

Phase5-P2 完成需要满足：

```text
1. P2-A 到 P2-F 全部完成。
2. console-web 可本地启动。
3. Runtime / Evaluation / Candidate / Review Queue / Audit Center 页面可用。
4. Review 操作可通过前端完成。
5. 前端不展示患者原文、clinician_report 或 raw training input。
6. 权限不足 / token 错误有明确 UI 反馈。
7. npm run test / npm run build 通过。
8. 后端 Phase 1–5 回归不被破坏。
9. 新增 Phase5_P2人工测试结果.md。
```

---

# 十、后置任务

```text
[-] 正式登录系统 / JWT / OAuth
[-] 多租户组织模型
[-] 完整设计系统
[-] Docker Compose 一键编排
[-] Nginx / 静态资源部署
[-] 正式医生审核平台
[-] ApprovedExperience 生效机制
[-] TrainingDatasetVersion 发布
[-] RAG / GraphRAG Provider
[-] 模型训练 / 后训练
```

---

# 十一、当前下一步

当前下一步：

```text
Phase5-P2-C：Runtime / Evaluation 页面
```

开始实现前必须：

```text
1. 将 Phase5-P2-C 状态从 [ ] 改为 [/]。
2. 只实现 RuntimePage 与 EvaluationPage 列表/详情。
3. 不直接实现 Candidate / Review / Audit 全部页面。
4. 不实现正式登录、Docker Compose、RAG、模型训练或正式审核平台。
```
