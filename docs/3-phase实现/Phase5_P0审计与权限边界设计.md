# Phase 5-P0 审计与权限边界设计

> 本文档定义 Phase 5-P0 的最小 AuditLog 与 debug API 访问边界。  
> P0 不做完整 RBAC、登录、多租户或正式审计平台，只建立后续治理必须依赖的审计记录底座。

---

# 一、为什么 Phase 5-P0 必须做 AuditLog

Phase 1–4 已经有大量 debug API：

```text
Runtime debug
Asset debug
Evaluation debug
Candidate debug
Review debug
```

当系统进入持久化阶段后，debug API 不再只是临时内存观察工具，而会对数据库中的治理对象产生持久影响。

因此必须记录：

```text
谁在什么时候，对哪个资源，做了什么操作，结果是什么。
```

---

# 二、P0 AuditLog 范围

P0 必须记录：

```text
1. CREATE_RUNTIME
2. CONTINUE_RUNTIME
3. CREATE_EVALUATION_RUN
4. GENERATE_CANDIDATES
5. REVIEW_EXPERIENCE_CANDIDATE
6. REVIEW_TRAINING_CANDIDATE
7. READ_SENSITIVE_DEBUG_RESOURCE（可选）
```

P0 不记录或后置：

```text
登录事件
权限授权事件
前端页面访问
模型训练事件
部署事件
多租户组织切换
```

---

# 三、AuditLogRecord 数据结构

建议字段：

```text
AuditLogRecord
- audit_id
- request_id
- actor
- action_type
- resource_type
- resource_id
- result_status
- created_at
- metadata
```

枚举建议：

```text
AuditActionType
AuditResourceType
AuditResultStatus
```

P0 actor 获取策略：

```text
1. 优先从 header X-Debug-Actor 获取。
2. 如果没有，使用 system-debug。
3. 不做真实登录认证。
```

---

# 四、AuditLogService

职责：

```text
1. 接收 action/resource/result/metadata。
2. 生成 audit_id。
3. 写入 AuditLogStore。
4. 不阻断主业务，除非审计写入失败被配置为 fail-closed。
```

P0 推荐策略：

```text
默认 fail-open，但记录日志。
医疗生产化前再评估 fail-closed。
```

---

# 五、访问边界

Phase 5-P0 不做完整 RBAC，但需要明确 debug API 边界。

建议新增配置：

```yaml
clinmind:
  debug-api:
    enabled: true
    require-debug-token: false
    debug-token: ""
```

P0 行为：

```text
本地默认 enabled=true。
如果 require-debug-token=true，则请求必须带 X-Debug-Token。
```

P0 不做：

```text
用户登录
角色权限
组织租户
医生身份认证
临床审批流
```

---

# 六、Audit API

P0 可新增：

```http
GET /api/v1/debug/audit-logs
GET /api/v1/debug/audit-logs/{audit_id}
```

查询参数：

```text
actor
action_type
resource_type
resource_id
from
to
limit
```

P0 可只实现按 id 与简单列表查询。

---

# 七、与 Candidate Review 的关系

CandidateReviewService 中的 review 操作必须写 AuditLog：

```text
REVIEW_EXPERIENCE_CANDIDATE
REVIEW_TRAINING_CANDIDATE
```

AuditLog 不是 ReviewRecord 的替代。

区别：

```text
CandidateReviewRecord：业务决策记录。
AuditLogRecord：系统操作审计记录。
```

---

# 八、与数据安全的关系

AuditLog metadata 不应保存完整敏感输入。

允许保存：

```text
run_id
runtime_id
candidate_id
review_id
case_id
status
error_code
asset_package_id
asset_package_version
```

不应保存：

```text
完整患者输入
完整 patient_output
完整 clinician_report
未脱敏 candidate input
```

---

# 九、测试设计

必须新增：

```text
AuditLogRecordTest
InMemoryAuditLogStoreTest
JdbcAuditLogStoreTest
AuditLogServiceTest
AuditLogControllerTest
CandidateReviewAuditIntegrationTest
CandidateGenerationAuditIntegrationTest
```

覆盖：

```text
1. Candidate review 产生 AuditLog。
2. Candidate generation 产生 AuditLog。
3. AuditLog 不包含未脱敏 input。
4. audit_id 可查询。
5. 按 resource_id 可查询。
6. Debug token 开启时无 token 请求被拒绝。
```

---

# 十、完成标准

```text
1. AuditLogRecord / AuditLogStore / AuditLogService 建立。
2. Candidate generation / review 至少写入 AuditLog。
3. AuditLog 可通过 debug API 查询。
4. AuditLog 不泄露未脱敏患者原文。
5. Debug API 可通过配置开启最小 token 保护。
6. 不引入完整 RBAC / 登录系统。
```

---

# 十一、最终结论

Phase 5-P0 的权限与审计目标是“最小可治理”，不是“完整权限平台”。

它为后续 RBAC、Audit Center、正式审核平台和部署运维打基础，但 P0 只实现最小审计链与 debug API 保护边界。
