# Phase 11-P0 Frontend RBAC / API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase11_P0RoleBasedFrontendSuite_实现规格.md`  
> 前置冻结：`docs/3-phase实现/Phase10_P0冻结记录.md`  
> 当前 Phase：Phase 11-P0 设计阶段  
> 当前目标：明确 Patient Portal、Clinician Workspace、Governance Console 的角色路由、DTO 投影、前端 API 分层与测试策略。

---

# 一、设计目标

Phase 11-P0 的 API 与测试设计要证明：

```text
1. 前端按角色进入不同 Portal。
2. 不同 Portal 调用不同 API scope。
3. 不同角色消费不同 DTO projection。
4. 患者端、医生端、治理端不共享完整 Runtime DTO。
5. 敏感字段不能通过页面、类型、mock、console 输出泄露。
6. P0 不引入生产级认证，仅提供 DemoRoleSwitcher 与 RoleGuard。
```

---

# 二、前端 RBAC 模型

## 2.1 Role enum

```ts
export type AppRole =
  | 'PATIENT'
  | 'CLINICIAN'
  | 'GOVERNANCE_REVIEWER'
  | 'SYSTEM_ADMIN'
  | 'READ_ONLY_OBSERVER';
```

## 2.2 Portal enum

```ts
export type Portal = 'patient' | 'clinician' | 'governance' | 'admin';
```

## 2.3 Permission enum

```ts
export type Permission =
  | 'patient:read_self'
  | 'patient:create_intake'
  | 'clinician:read_case'
  | 'clinician:edit_report_draft'
  | 'governance:read_overview'
  | 'governance:read_runtime_timeline'
  | 'governance:read_candidate_inbox'
  | 'governance:read_audit'
  | 'admin:read_settings';
```

P0 不定义任何高风险写权限：

```text
candidate:approve
candidate:reject
candidate:publish
runtime:modify
tool:execute
model:release
prescription:create
referral:create
appointment:create
payment:create
message:send
```

这些权限在 Phase 11-P0 中不存在。

---

# 三、RBAC 配置

建议新增：

```text
console-web/src/auth/rbac.ts
```

内容：

```ts
export const ROLE_PERMISSIONS: Record<AppRole, Permission[]> = {
  PATIENT: ['patient:read_self', 'patient:create_intake'],
  CLINICIAN: ['clinician:read_case', 'clinician:edit_report_draft'],
  GOVERNANCE_REVIEWER: [
    'governance:read_overview',
    'governance:read_runtime_timeline',
    'governance:read_candidate_inbox',
    'governance:read_audit',
  ],
  SYSTEM_ADMIN: [
    'governance:read_overview',
    'governance:read_runtime_timeline',
    'governance:read_candidate_inbox',
    'governance:read_audit',
    'admin:read_settings',
  ],
  READ_ONLY_OBSERVER: [
    'governance:read_overview',
    'governance:read_runtime_timeline',
    'governance:read_candidate_inbox',
    'governance:read_audit',
  ],
};
```

工具函数：

```ts
export function hasPermission(role: AppRole, permission: Permission): boolean;
export function canAccessPortal(role: AppRole, portal: Portal): boolean;
export function getDefaultRouteForRole(role: AppRole): string;
```

---

# 四、RouteGuard 设计

建议新增：

```text
console-web/src/auth/RoleGuard.tsx
```

职责：

```text
1. 读取当前 DemoRole / AuthRole。
2. 校验 route 所需 portal 或 permission。
3. 允许访问则 render children。
4. 不允许访问则跳转 /forbidden。
5. 不在 RoleGuard 中做字段过滤。
```

示例：

```tsx
<RoleGuard portal="patient">
  <PatientHomePage />
</RoleGuard>

<RoleGuard permission="governance:read_audit">
  <AuditBrowserPage />
</RoleGuard>
```

边界：

```text
RoleGuard 只能控制前端路由，不是数据安全边界。
真正的数据安全必须由后端 Role-specific DTO 和 API scope 保证。
```

---

# 五、API scope 设计

Phase 11-P0 推荐 API scope：

```text
/api/v1/patient/*
/api/v1/clinician/*
/api/v1/console/*
/api/v1/debug/*        P0 legacy / demo only
```

P0 可以先用 mock / seed data，但类型和 client 要按真实分层设计。

## 5.1 Patient API

```text
GET  /api/v1/patient/sessions
GET  /api/v1/patient/sessions/{sessionId}
GET  /api/v1/patient/sessions/{sessionId}/summary
POST /api/v1/patient/intake       P0 可不接真实后端
```

返回 DTO：

```ts
export interface PatientSessionSummary {
  session_id: string;
  runtime_id: string;
  status: string;
  chief_complaint_summary: string;
  risk_hint: 'LOW' | 'MEDIUM' | 'HIGH' | 'UNKNOWN';
  updated_at: string;
}

export interface PatientRuntimeView {
  session_id: string;
  runtime_id: string;
  status: string;
  safe_summary: string;
  collected_facts: PatientFactSummary[];
  next_questions: PatientQuestion[];
  safety_notices: SafetyNotice[];
  care_navigation: CareNavigationSuggestion[];
  allowed_actions: string[];
  disclaimer: string;
}
```

禁止返回：

```text
ddx_candidates
raw_evidence
trace_nodes
audit_events
evaluation_result
candidate_governance
raw_prompt
internal_reasoning
```

## 5.2 Clinician API

```text
GET /api/v1/clinician/cases
GET /api/v1/clinician/cases/{caseId}
GET /api/v1/clinician/cases/{caseId}/report-draft
PUT /api/v1/clinician/cases/{caseId}/report-draft    P0 可本地 mock，不接真实提交
```

返回 DTO：

```ts
export interface ClinicianCaseSummary {
  case_id: string;
  runtime_id: string;
  status: string;
  risk_level: string;
  chief_complaint_summary: string;
  updated_at: string;
  assigned_clinician?: string;
}

export interface ClinicianCaseView {
  case_id: string;
  runtime_id: string;
  patient_summary: PatientSummary;
  case_frame: CaseFrameView;
  inquiry_timeline: InquiryTurnView[];
  ddx_board: DdxCandidateView[];
  evidence_panel: EvidenceItemView[];
  risk_panel: RiskSignalView[];
  ai_suggestions: ClinicianSuggestionView[];
  report_draft: ClinicianReportDraftView;
  runtime_boundary_summary: RuntimeBoundarySummary;
}
```

禁止返回：

```text
raw_prompt
secret
api_key
private_key
raw_external_response
internal_chain_of_thought
full_rationale
unredacted_patient_dialogue
```

## 5.3 Governance API

沿用 Phase 10-P0：

```text
GET /api/v1/console/overview
GET /api/v1/console/runtimes
GET /api/v1/console/runtimes/{runtimeId}/timeline
GET /api/v1/console/governance/domains
GET /api/v1/console/candidates
GET /api/v1/console/audits
```

Phase 11-P0 的变化是前端路由归位：

```text
/governance/overview 调用 /api/v1/console/overview
/governance/runtimes 调用 /api/v1/console/runtimes
/governance/runtimes/:runtimeId 调用 /api/v1/console/runtimes/{runtimeId}/timeline
/governance/candidates 调用 /api/v1/console/candidates
/governance/audits 调用 /api/v1/console/audits
```

不新增 Console 写接口。

---

# 六、前端 client 分层

建议目录：

```text
console-web/src/portals/patient/api/patientClient.ts
console-web/src/portals/clinician/api/clinicianClient.ts
console-web/src/portals/governance/api/governanceClient.ts
console-web/src/shared/api/httpClient.ts
```

P0 可以继续复用现有 `consoleClient`，但要逐步拆分：

```text
patientClient 不允许 import governance API。
clinicianClient 不允许 import audit browser / candidate inbox API。
governanceClient 不允许提供患者问诊提交接口。
```

---

# 七、Mock / Seed Demo Data 设计

P0 允许使用 mock 数据，但必须：

```text
1. 所有 mock case 标记为 demo。
2. 不使用真实患者姓名、身份证、电话、住址、病历号。
3. 不包含真实医疗建议。
4. 不包含处方、剂量、用药调整等高风险内容。
5. 同一个 runtime-demo-001 需要同时有 Patient / Clinician / Governance 三种投影。
```

建议新增：

```text
console-web/src/demo/runtimeDemoData.ts
```

包含：

```ts
export const DEMO_RUNTIME_ID = 'runtime-demo-001';
export const demoPatientRuntimeView: PatientRuntimeView;
export const demoClinicianCaseView: ClinicianCaseView;
export const demoGovernanceRuntimeView: RuntimeTimeline;
```

禁止在 mock 中出现：

```text
真实人名
真实手机号
真实身份证号
真实住址
真实病历号
完整治疗方案
具体处方剂量
raw prompt
chain-of-thought
```

---

# 八、页面测试策略

## 8.1 RBAC 单元测试

覆盖：

```text
PATIENT 可以访问 /patient，不可以访问 /clinician /governance。
CLINICIAN 可以访问 /clinician，不可以访问 /patient /governance。
GOVERNANCE_REVIEWER 可以访问 /governance，不可以访问 /patient /clinician。
SYSTEM_ADMIN 可以访问 /governance /admin 只读入口。
READ_ONLY_OBSERVER 可以访问 /governance，不可以访问 /admin 写入口。
未知角色默认 Forbidden。
```

建议测试文件：

```text
console-web/src/auth/rbac.test.ts
console-web/src/auth/RoleGuard.test.tsx
```

## 8.2 Portal 渲染测试

覆盖：

```text
PatientHomePage 渲染安全声明。
PatientSafeSummaryPage 不渲染 ddx / trace / audit / evaluation 字样。
CaseWorkspacePage 渲染 Case Frame / DDx Board / Evidence Panel / Report Draft。
GovernanceOverviewPage 渲染 Runtime / Candidate / Audit 指标。
ForbiddenPage 渲染无权限说明。
```

## 8.3 敏感字段测试

必须检查页面文本中不出现：

```text
raw_prompt
secret
api_key
private_key
raw_external_response
internal_chain_of_thought
full_rationale
unredacted_patient_dialogue
```

可写统一测试 helper：

```ts
expectNoSensitiveText(container);
```

## 8.4 Build / Typecheck

冻结前至少执行：

```text
npm run typecheck
npm run build
```

如果 Vitest 配置仍有 timeout 或 no specs 问题，冻结记录必须如实记录。

---

# 九、人工测试场景

## 场景 1：患者访问患者端

```text
Given 当前角色为 PATIENT
When 访问 /patient
Then 展示 Patient Home
And 可以进入 Symptom Intake
And 不展示 DDx / Audit / Runtime Timeline
```

## 场景 2：患者访问医生端被拒绝

```text
Given 当前角色为 PATIENT
When 访问 /clinician/cases
Then 跳转 /forbidden
```

## 场景 3：医生查看病例工作台

```text
Given 当前角色为 CLINICIAN
When 访问 /clinician/cases/runtime-demo-001
Then 展示 Case Frame / DDx Board / Evidence Panel / Risk Panel / Report Draft
And 不展示 raw prompt / secret / raw external response
```

## 场景 4：治理人员查看管理端

```text
Given 当前角色为 GOVERNANCE_REVIEWER
When 访问 /governance/overview
Then 展示 Console Overview
And 可以进入 Runtime Timeline / Candidate Inbox / Audit Browser
```

## 场景 5：同一 Runtime 三视图

```text
Given runtime-demo-001
When 依次切换 PATIENT / CLINICIAN / GOVERNANCE_REVIEWER
Then 三个 Portal 显示同一个 runtime_id
And 三个 Portal 的字段粒度不同
And Patient View 不含医生和治理字段
And Clinician View 不含治理敏感字段
And Governance View 只展示 safe governance DTO
```

---

# 十、验收标准

Phase 11-P0 的 API 与测试验收标准：

```text
[ ] RBAC role / permission / portal matrix 有单元测试。
[ ] RoleGuard 能拦截无权限路由。
[ ] Patient Portal 不调用 Governance API。
[ ] Clinician Workspace 不调用 Audit / Candidate Governance API。
[ ] Governance Console 不调用 Patient Intake API。
[ ] 三端 DTO 类型分离。
[ ] demo runtime 能在三端展示不同投影。
[ ] 页面不渲染敏感 key。
[ ] npm run typecheck 通过。
[ ] npm run build 通过。
[ ] 人工测试记录完成。
```

---

# 十一、最终边界

Phase 11-P0 的前端 RBAC 设计只能证明：

```text
角色路由清晰；
页面边界清晰；
DTO 投影清晰；
演示链路完整。
```

不能声称：

```text
已经具备生产级认证系统；
已经具备真实医疗数据权限隔离；
已经具备正式医生工作站；
已经具备在线诊疗能力；
已经具备合规审方、处方、转诊、支付、消息发送能力。
```

Phase 11-P0 仍然是产品原型层，而不是生产医疗系统。
