# Phase 5 审计与访问边界设计

> 本文档定义 Phase 5-P0 的最小 AuditLog、访问边界和 debug/governance API 约束。  
> Phase 5-P0 不实现完整 RBAC / IAM / 前端 Console，但必须建立审计事件模型，为后续权限系统和治理后台预留结构。

---

# 一、设计目标

```text
1. 关键治理动作可审计。
2. 审计记录与 resource_id、actor、reason 绑定。
3. Debug API 与 future Governance API 有清晰边界。
4. 审计日志不保存完整敏感 payload。
5. 不把审计系统变成权限系统。
```

---

# 二、角色边界

P0 先定义角色枚举，不做完整权限系统：

```text
SYSTEM
DEBUG_USER
GOVERNANCE_ADMIN
CLINICIAN_REVIEWER
```

默认 actor：

```text
actor_id = debug-user
actor_role = DEBUG_USER
```

后续 Phase5-P1 / P2 再接 Spring Security / JWT / RBAC。

---

# 三、AuditEventType

P0 推荐事件：

```text
EVALUATION_RUN_CREATED
CAPABILITY_PROFILE_PROPOSAL_CREATED
CANDIDATE_GENERATION_CREATED
EXPERIENCE_CANDIDATE_REVIEWED
TRAINING_CANDIDATE_REVIEWED
CANDIDATE_REVIEW_RECORD_VIEWED
CANDIDATE_VIEWED
RUNTIME_TRACE_EXPORTED
```

P0 至少实现：

```text
CANDIDATE_GENERATION_CREATED
EXPERIENCE_CANDIDATE_REVIEWED
TRAINING_CANDIDATE_REVIEWED
CAPABILITY_PROFILE_PROPOSAL_CREATED
```

---

# 四、AuditResourceType

```text
RUNTIME
RUNTIME_TRACE
EVALUATION_RUN
EVALUATION_RESULT
CAPABILITY_PROFILE_PROPOSAL
EXPERIENCE_CANDIDATE
TRAINING_EXAMPLE_CANDIDATE
CANDIDATE_REVIEW_RECORD
ASSET_PACKAGE
```

---

# 五、AuditLog 数据结构

```text
AuditLog
- audit_id
- actor_id
- actor_role
- action_type
- resource_type
- resource_id
- request_id
- reason
- before_snapshot
- after_snapshot
- metadata
- created_at
```

要求：

```text
1. before_snapshot / after_snapshot 必须是脱敏摘要，不是完整对象。
2. metadata 可以保存 run_id / candidate_id / review_id / asset version。
3. request_id 允许为空，但 API 层如果能拿到则写入。
```

---

# 六、AuditService

职责：

```text
1. 构造 AuditLog。
2. 调用 AuditLogStore 保存。
3. 对敏感对象做摘要化。
4. 失败时按策略处理。
```

P0 失败策略：

```text
写操作审计失败 → fail-fast 或至少返回明确错误。
读操作审计失败 → 可记录 warning，不阻断查询。
```

建议 P0 对 review / generation 写操作采用 fail-fast。

---

# 七、访问边界

## 7.1 Patient-facing

不得访问：

```text
/debug/**
/internal/**
/audit/**
/candidates/**
/evaluations/debug/**
```

## 7.2 Debug/Internal

允许访问：

```text
Evaluation debug API
Candidate debug API
Review debug API
Audit debug query API（P0 可只内部测试，不开放 Controller）
```

## 7.3 Governance/Admin

后续 Phase5-P1/P2：

```text
Asset publish / rollback
CapabilityProfile approval
TrainingDataset approval
AuditLog query
User / Role / Permission
```

P0 不做正式 Admin。

---

# 八、审计 API

P0 可以不开放正式 Audit API，只做 Store + Service + 测试。

如果需要 debug API，建议：

```http
GET /api/v1/debug/audit/logs?resource_type=...&resource_id=...
GET /api/v1/debug/audit/logs/{audit_id}
```

但 P0 更推荐先不开放，避免扩大 API 面。

---

# 九、安全限制

```text
1. AuditLog 不保存完整 patient input。
2. AuditLog 不保存完整 RuntimeState。
3. AuditLog 不保存完整 TrainingExampleCandidate.input。
4. AuditLog 不保存完整 patient_output。
5. AuditLog 查询 API 后续必须接 RBAC。
```

---

# 十、测试设计

```text
AuditLogTest
AuditServiceTest
InMemoryAuditLogStoreTest
PostgresAuditLogStoreTest
CandidateReviewAuditIntegrationTest
CapabilityProposalAuditIntegrationTest
```

必须验证：

```text
1. Candidate review 会写 audit log。
2. Candidate generation 会写 audit log。
3. AuditLog 不包含完整 input_jsonb。
4. AuditLog 能通过 resource_type + resource_id 查询。
5. 审计失败时写操作返回明确错误。
```

---

# 十一、最终结论

Phase 5-P0 的审计能力不是为了“权限完整”，而是为了证明系统开始具备治理闭环：

```text
谁
在什么时候
对哪个治理对象
做了什么动作
为什么做
动作前后是什么摘要
```

这一步是从 debug 原型走向治理系统的关键边界。
