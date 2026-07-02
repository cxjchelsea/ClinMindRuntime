# Phase 5-P1：最小 Console 与访问治理实现规格

> 本文档定义 ClinMindRuntime Phase 5-P1 的目标、边界和实现范围。  
> Phase 5-P0 已完成 PostgreSQL 持久化与 AuditLog 最小治理；Phase 5-P1 不继续扩展 AI 能力，而是在持久化对象之上建立最小治理 Console API、RBAC-lite 访问边界和 Audit Center 查询能力。

---

# 一、Phase 5-P1 定位

Phase 5-P0 已完成：

```text
Runtime / Evaluation / Candidate / Review
→ InMemory / PostgreSQL Store 双模式
→ AuditLogService
→ Persistence Health API
→ Audit Log debug API
```

Phase 5-P1 要解决的问题是：

```text
1. 已持久化的 Runtime / Evaluation / Candidate / Review 如何被安全查询？
2. Debug API 如何从 token-only 过渡到最小角色边界？
3. AuditLog 如何从“能查”提升为“可过滤、可分页、可复盘”？
4. Candidate review 如何有最小后台治理入口，但仍不成为正式医生审核平台？
5. Console API 如何避免泄露患者原文、clinician_report、未脱敏 candidate input？
```

Phase 5-P1 的一句话目标：

```text
在 Phase 5-P0 持久化底座之上，提供最小可用的治理 Console API 与 RBAC-lite 访问边界，让 Runtime、Evaluation、Candidate、Review、AuditLog 可以被安全查询和复盘，但不改变 AI 决策、不上线经验、不发布训练集。
```

---

# 二、Phase 5-P1 主链路

Phase 5-P1 主链路：

```text
Console / Debug Request
→ DebugTokenFilter
→ ActorContextResolver
→ AccessPolicy / RolePolicy
→ ConsoleQueryService
→ Store / Repository
→ Safe DTO Mapper
→ AuditLogService
→ Console API Response
```

关键模块：

```text
ActorContextResolver：解析 actor、roles、request_id。
AccessPolicy：判断 actor 是否能执行 action / 访问 resource。
ConsoleQueryService：只读聚合查询 Runtime / Evaluation / Candidate / Review。
SafeConsoleDtoMapper：输出安全摘要，不暴露敏感原文。
AuditCenterService：增强 AuditLog 查询、过滤、分页与统计。
```

---

# 三、Phase 5-P1 不是什么

Phase 5-P1 不是：

```text
1. 不是完整前端 Console。
2. 不是正式登录系统。
3. 不是企业级 RBAC / 多租户。
4. 不是医生审核平台。
5. 不是 ApprovedExperience 生效机制。
6. 不是 TrainingDatasetVersion 发布机制。
7. 不是 RAG / GraphRAG。
8. 不是 Python AI Provider。
9. 不是模型训练 / 后训练。
10. 不是生产级部署运维平台。
```

Phase 5-P1 只做：

```text
最小 actor / role 上下文
RBAC-lite action/resource 权限判断
Console summary / detail API
Audit Center 查询增强
Candidate review queue API
敏感字段安全映射
访问审计
```

---

# 四、角色与访问边界

P1 角色建议：

```text
SYSTEM_ADMIN
EVALUATION_REVIEWER
CANDIDATE_REVIEWER
AUDIT_REVIEWER
READ_ONLY_OBSERVER
```

P1 不做真实登录。actor 与 roles 仍从 debug header 或配置中获取：

```text
X-Debug-Actor
X-Debug-Roles
X-Debug-Token
```

P1 的目标是把“所有 debug token 请求都可访问所有资源”升级为：

```text
有 token 只是通过入口门禁；是否能访问某类资源，还要经过 role/action policy。
```

---

# 五、Console API 范围

P1 可以新增 internal console API：

```http
GET /api/v1/debug/console/runtime-sessions
GET /api/v1/debug/console/runtime-sessions/{runtime_id}
GET /api/v1/debug/console/evaluation-runs
GET /api/v1/debug/console/evaluation-runs/{run_id}
GET /api/v1/debug/console/candidate-generations
GET /api/v1/debug/console/candidates
GET /api/v1/debug/console/candidates/{candidate_id}
GET /api/v1/debug/console/review-queue
GET /api/v1/debug/console/audit-center/audit-logs
GET /api/v1/debug/console/audit-center/summary
```

P1 的 Console API 只返回安全摘要和治理信息：

```text
id
status
type
risk_level
review_status
sanitization_status
asset_package_id / version
created_at / updated_at
source_ref summary
counts / metrics summary
```

不返回：

```text
完整患者输入
完整 patient_output
完整 clinician_report
未脱敏 training candidate input
内部 DDx 全量推理细节
```

---

# 六、Audit Center 范围

P1 增强 AuditLog 查询：

```text
action_type
resource_type
resource_id
actor
role
result_status
created_at from/to
limit / cursor or page
```

P1 可新增统计：

```text
按 action_type 统计
按 resource_type 统计
按 result_status 统计
最近失败操作
最近 review 操作
```

Audit Center 仍不是完整合规报表系统。

---

# 七、Candidate Review Queue 范围

P1 可以提供候选审核队列 API：

```text
按 review_status 过滤
按 risk_level 过滤
按 candidate_kind 过滤
按 task_type / candidate_type 过滤
按 source_evaluation_run_id 过滤
```

Review 操作仍复用 Phase4-P1 / Phase5-P0 的 CandidateReviewService。

即使候选被 APPROVED，仍然：

```text
不自动进入 Runtime
不修改 AssetPackage
不修改 CapabilityProfile
不发布 TrainingDatasetVersion
```

---

# 八、数据安全边界

Phase 5-P1 必须遵守：

```text
1. 所有 Console API 必须经过 SafeConsoleDtoMapper。
2. Audit Center 不返回敏感 metadata。
3. Candidate detail 不返回未脱敏 input。
4. Runtime detail 不返回患者原文，除非未来引入明确权限和脱敏策略。
5. Console API 查询也要写 AuditLog。
```

---

# 九、P1 完成标准

Phase 5-P1 完成需要满足：

```text
1. ActorContext / RolePolicy / AccessPolicy 建立。
2. Console API 查询 Runtime / Evaluation / Candidate / Review 的安全摘要。
3. Candidate review queue 可查询。
4. Audit Center 支持过滤、分页和 summary。
5. Unauthorized / forbidden 请求返回明确错误码。
6. Console API 不泄露患者原文、clinician_report 或未脱敏 candidate input。
7. 关键 Console 查询与 review 操作进入 AuditLog。
8. Phase1–5 回归与 postgres 专项测试通过。
9. 新增 docs/Phase5_P1人工测试API结果.md。
```

---

# 十、最终结论

Phase 5-P1 的重点不是继续增强 AI，而是让已经持久化的治理对象可以被安全地看见、筛选、复盘和最小审核。

```text
P0：对象可持久化、可审计
P1：对象可安全治理、可权限控制、可 Console 查询
```

它是走向产品化 Console 的第一步，但不是完整产品化 Console。
