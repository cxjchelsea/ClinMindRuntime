# Phase 5-P1 开发任务清单

> 本清单用于约束 Phase 5-P1 的实现顺序。  
> Phase 5-P1 只做最小 Console API、RBAC-lite 访问治理、Audit Center 查询增强和安全 DTO，不实现 RAG、模型训练、完整前端 Console、正式医生审核平台、ApprovedExperience 自动生效或 TrainingDatasetVersion 发布。

---

# 一、状态标记

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要决策
[-] 后置 / 不在 P1 范围
```

---

# 二、Phase 5-P1 目标

```text
让 Phase5-P0 已持久化的 Runtime / Evaluation / Candidate / Review / AuditLog 可以通过最小 Console API 被安全查询、权限控制和审计复盘。
```

P1 必须保证：

```text
1. Debug token 之后还有 actor / role / action / resource 判断。
2. Console API 不直接返回 raw domain object 或 snapshot JSON。
3. Console API 不泄露患者原文、clinician_report 或未脱敏 candidate input。
4. Candidate review 仍不自动上线经验或进入训练集。
5. 所有关键 Console 查询与 review 行为进入 AuditLog。
```

---

# 三、Phase5-P1-A：ActorContext 与 RBAC-lite 基础

状态：`[x]`

任务：

```text
[x] ActorContext
[x] DebugRole
[x] ConsoleActionType
[x] ConsoleResourceType
[x] ActorContextResolver
[x] RolePolicy / AccessPolicy
[x] AccessDeniedException
[x] ApiExceptionHandler 错误码映射
[x] ActorContextFilter
```

测试：

```text
[x] ActorContextResolverTest
[x] AccessPolicyTest
[x] ConsoleAccessDeniedTest
```

验收标准：

```text
1. X-Debug-Actor / X-Debug-Roles 可解析。
2. 无 roles 默认 READ_ONLY_OBSERVER。
3. token 有效但 role 不足返回 403。
4. 错误码明确：ACCESS_DENIED / INVALID_DEBUG_ROLE。
```

---

# 四、Phase5-P1-B：Safe Console DTO Mapper

状态：`[x]`

任务：

```text
[x] SafeConsoleDtoMapper
[x] Runtime console summary DTO
[x] Evaluation console summary DTO
[x] Candidate console summary/detail DTO
[x] Review console summary DTO
[x] Audit console summary DTO
[x] 敏感字段 denylist / allowlist
```

测试：

```text
[x] SafeConsoleDtoMapperTest
[x] ConsoleSensitiveFieldRedactionTest
```

验收标准：

```text
1. Runtime DTO 不包含患者原文。
2. Candidate DTO 不包含 raw training input。
3. Audit DTO 不包含 sensitive metadata。
4. 不直接返回 snapshot raw json。
```

---

# 五、Phase5-P1-C：Console Runtime / Evaluation 查询 API

状态：`[ ]`

任务：

```text
[ ] ConsoleRuntimeController
[ ] ConsoleEvaluationController
[ ] ConsoleQueryService
[ ] GET /api/v1/debug/console/runtime-sessions
[ ] GET /api/v1/debug/console/runtime-sessions/{runtime_id}
[ ] GET /api/v1/debug/console/evaluation-runs
[ ] GET /api/v1/debug/console/evaluation-runs/{run_id}
[ ] 查询行为写 AuditLog
```

测试：

```text
[ ] ConsoleRuntimeControllerTest
[ ] ConsoleEvaluationControllerTest
[ ] ConsoleRuntimeEvaluationAuditIntegrationTest
```

验收标准：

```text
1. 可按状态 / id 查询 Runtime summary。
2. 可查询 Evaluation summary/detail。
3. role 不足时返回 403。
4. 查询结果不泄露敏感字段。
```

---

# 六、Phase5-P1-D：Console Candidate / Review Queue API

状态：`[ ]`

任务：

```text
[ ] ConsoleCandidateController
[ ] GET /api/v1/debug/console/candidate-generations
[ ] GET /api/v1/debug/console/candidates
[ ] GET /api/v1/debug/console/candidates/{candidate_id}
[ ] GET /api/v1/debug/console/review-queue
[ ] existing review API 接入 AccessPolicy
[ ] 查询 / review 行为写 AuditLog
```

测试：

```text
[ ] ConsoleCandidateControllerTest
[ ] ConsoleReviewQueueTest
[ ] CandidateReviewAccessPolicyIntegrationTest
```

验收标准：

```text
1. Candidate review queue 可按 status / risk / kind 过滤。
2. CANDIDATE_REVIEWER 可 review。
3. READ_ONLY_OBSERVER 不可 review。
4. APPROVED 仍不自动生效。
```

---

# 七、Phase5-P1-E：Audit Center 查询增强

状态：`[ ]`

任务：

```text
[ ] ConsoleAuditCenterController
[ ] AuditCenterService
[ ] GET /api/v1/debug/console/audit-center/audit-logs
[ ] GET /api/v1/debug/console/audit-center/audit-logs/{audit_id}
[ ] GET /api/v1/debug/console/audit-center/summary
[ ] filters: actor/action/resource/result/time/limit
[ ] pagination / limit guard
```

测试：

```text
[ ] AuditCenterQueryTest
[ ] AuditCenterSummaryTest
[ ] AuditCenterAccessPolicyTest
```

验收标准：

```text
1. AUDIT_REVIEWER 可查询 Audit Center。
2. 非 AUDIT_REVIEWER 不可查询 audit detail。
3. 查询支持过滤和 limit。
4. Audit Center 不返回敏感 metadata。
```

---

# 八、Phase5-P1-F：Postgres E2E 与人工验收

状态：`[ ]`

任务：

```text
[ ] Phase5P1ConsolePostgresEndToEndIntegrationTest
[ ] ConsoleAuditTrailIntegrationTest
[ ] ConsoleSensitiveFieldRedactionIntegrationTest
[ ] docs/Phase5_P1人工测试API结果.md
[ ] README / docs 导航状态同步
```

测试：

```text
[ ] in-memory 全量回归
[ ] postgres 专项回归
[ ] Phase5P1ConsolePostgresEndToEndIntegrationTest
```

验收标准：

```text
1. postgres 模式下 Console API 能查询 P0 持久化对象。
2. role 控制生效。
3. Console 查询写入 AuditLog。
4. 敏感字段不泄露。
5. 人工验收记录补齐。
```

---

# 九、P1 完成定义

Phase5-P1 完成需要满足：

```text
1. P1-A 到 P1-F 全部完成。
2. Console API 支持 Runtime / Evaluation / Candidate / Review / Audit 安全查询。
3. RBAC-lite 对 Console API 和 Review API 生效。
4. Safe DTO 不泄露敏感字段。
5. Audit Center 查询增强可用。
6. Candidate review 仍不自动修改 AssetPackage / CapabilityProfile / TrainingDataset。
7. in-memory 与 postgres 双模式回归通过。
8. 新增 Phase5_P1人工测试API结果.md。
9. 更新 AI_IMPLEMENTATION_SKILL.md，标记 Phase5-P1 完成或进入 freeze。
```

---

# 十、后置任务

```text
[-] 完整 React / Vue 前端 Console
[-] 正式登录系统 / JWT / OAuth
[-] 多租户组织模型
[-] 正式医生审核平台
[-] ApprovedExperience 生效机制
[-] TrainingDatasetVersion 发布
[-] RAG / GraphRAG Provider
[-] 模型训练 / 后训练
[-] 完整 Docker Compose / 部署运维
```

---

# 十一、当前下一步

当前下一步：

```text
Phase5-P1-B：Safe Console DTO Mapper
```

开始实现前必须：

```text
1. 将 Phase5-P1-B 状态从 [ ] 改为 [/]。
2. 只实现 SafeConsoleDtoMapper 与安全 DTO。
3. 不直接实现 Console Controller。
4. 不实现前端页面、RAG、模型训练、正式 RBAC 或审核平台。
```
