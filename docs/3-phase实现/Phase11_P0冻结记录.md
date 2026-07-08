# Phase 11-P0 冻结记录：Role-based Frontend Suite MVP

> 上位规格：`Phase11_P0RoleBasedFrontendSuite_实现规格.md`
> API 与测试设计：`Phase11_P0FrontendRBAC_API与测试设计.md`
> 任务清单：`Phase11_P0开发任务清单.md`
> 人工测试：`Phase11_P0人工测试结果.md`
> 冻结日期：2026-07-08

---

# 一、冻结结论

Phase 11-P0 **Role-based Frontend Suite MVP** 已实现并通过前端类型检查、Vitest、生产构建和人工验收，现冻结。

本阶段完成的是三角色前端产品原型层：

```text
Role-specific DTO / demo projection
-> DemoRoleSwitcher
-> RoleGuard
-> Patient Portal
-> Clinician Workspace
-> Governance Console
-> runtime-demo-001 three-role walkthrough
```

Phase 11-P0 只提供前端演示闭环，不提供生产级认证，不接入真实患者数据，不执行诊疗、处方、转诊、预约、支付或外部消息动作。

---

# 二、已实现能力

## RBAC model

新增：

- `AppRole`
- `Permission`
- `Portal`
- `ROLE_PERMISSIONS`
- `hasPermission`
- `canAccessPortal`
- `getDefaultRouteForRole`

角色：

- `PATIENT`
- `CLINICIAN`
- `GOVERNANCE_REVIEWER`
- `SYSTEM_ADMIN`
- `READ_ONLY_OBSERVER`

权限矩阵：

- `PATIENT`：只访问 Patient Portal。
- `CLINICIAN`：只访问 Clinician Workspace。
- `GOVERNANCE_REVIEWER` / `SYSTEM_ADMIN` / `READ_ONLY_OBSERVER`：访问 Governance Console。

## Frontend guard and shell

新增：

- `DemoRoleProvider`
- `DemoRoleSwitcher`
- `RoleGuard`
- `ForbiddenPage`

能力：

- 当前演示角色显示在 AppShell header。
- 切换角色后跳转到该角色默认入口。
- 未授权路由跳转 `/forbidden`。
- RoleGuard 只控制前端路由，不作为数据安全边界。

## Patient Portal

新增页面：

- `PatientHomePage`
- `SymptomIntakePage`
- `GuidedInquiryPage`
- `PatientSafeSummaryPage`

新增组件：

- `PatientSafetyNotice`
- `CollectedFactsCard`
- `PatientQuestionList`

患者端只展示：

- 安全摘要
- 已收集事实
- 下一步问询
- 安全提示
- 照护导航
- AI 不能替代医生的免责声明

患者端不展示：

- DDx Board
- confidence_score
- raw_evidence
- raw_tool_result
- model_prompt
- internal_reasoning
- Trace
- Audit
- Evaluation
- Candidate Governance

## Clinician Workspace

新增页面：

- `ClinicianDashboardPage`
- `ClinicianCaseInboxPage`
- `CaseWorkspacePage`
- `ClinicianReportPage`

新增组件：

- `CaseFramePanel`
- `DdxBoard`
- `EvidencePanel`
- `RiskPanel`
- `ReportDraftEditor`

医生端展示：

- Case Frame
- Inquiry Timeline
- DDx Board as candidate directions
- Evidence summary
- Risk / safety panel
- AI suggested questions
- Local report draft

医生端不展示：

- raw_prompt
- secret
- api_key
- private_key
- raw_external_response
- internal_chain_of_thought
- full_rationale
- unredacted_patient_dialogue
- raw patient dialogue

## Governance Console migration

Phase10 Console 页面迁移到 `/governance/*` 下：

- `/governance/overview`
- `/governance/runtimes`
- `/governance/runtimes/:runtimeId`
- `/governance/domains`
- `/governance/candidates`
- `/governance/audits`
- `/governance/evaluations`
- `/governance/audit-center`

旧路由保留 redirect：

- `/overview`
- `/runtime-timeline`
- `/governance/runtime-timeline`
- `/governance/runtime`
- `/candidate-inbox`
- `/governance/candidate-inbox`
- `/runtime`
- `/evaluation`
- `/candidates`
- `/review-queue`
- `/audit-center`

治理端继续沿用 Phase10 Safe DTO 与只读 Console API，不新增写动作。

## runtime-demo-001 walkthrough

新增 demo-safe projection data：

- `PatientSessionSummary`
- `PatientRuntimeView`
- `ClinicianCaseSummary`
- `ClinicianCaseView`
- `GovernanceRuntimeProjection`

三端入口：

- Patient：`/patient/sessions/runtime-demo-001/summary`
- Clinician：`/clinician/cases/runtime-demo-001`
- Governance：`/governance/runtimes/runtime-demo-001`

`RuntimeTimelinePage` 已读取 route param `runtimeId`，访问治理投影时会自动选中并加载对应 runtime timeline。

---

# 三、治理边界

Phase 11-P0 冻结后仍禁止：

- 生产级登录 / 注册 / 认证 / 权限后台
- 真实患者数据
- 真实 HIS / EMR / LIS / PACS / 预约 / 支付系统接入
- 真实在线诊疗闭环
- 患者端自动诊断、自动开药、自动处方、自动转诊
- Provider / Tool / Agent 直接调用
- RuntimeState mutation
- approve / reject / publish / run 写操作
- Candidate 自动发布
- raw patient dialogue
- raw prompt
- secret / api key / private key
- raw external response
- internal chain-of-thought / full rationale

---

# 四、测试结果

## TypeScript

```text
npm run typecheck
PASS
```

## Vitest

```text
npm test
20 test files passed
44 tests passed
0 failed
```

覆盖：

- `rbac.test.ts`
- `RoleGuard.test.tsx`
- `GovernanceRouteAccess.test.tsx`
- `RuntimeTimelineRouteParams.test.tsx`
- `PatientSafeSummaryPage.test.tsx`
- `CaseWorkspacePage.test.tsx`
- Existing console-web tests

## Build

```text
npm run build
PASS
```

---

# 五、提交记录

Phase 11-P0 前端实现提交：

```text
91e02d6 feat: implement phase11 role based frontend
```

Phase 11-P0 路由收敛与敏感字段测试补强：

```text
913cdea fix: tighten phase11 governance frontend routes
```

---

# 六、后置任务

Phase 11-P1 / Phase 12 可继续推进：

- 生产级 AuthN / AuthZ 设计
- 后端 role-specific DTO / API scope 实装
- Patient Portal 与真实 Runtime session 的受控连接
- Clinician Workspace 与真实病例工作流的只读连接
- Governance Console 更完整的只读可观测图谱
- 真实 UI 可用性测试与审计记录

上述后续阶段开始前，必须重新打开新的 phase 设计与实现约束；不得在 Phase 11-P0 冻结范围内追加生产级认证、真实医疗系统接入或写操作。

---

# 七、最终结论

Phase 11-P0 已冻结。

冻结范围仅限 **Role-based Frontend Suite MVP**：前端 RBAC demo、Patient Portal、Clinician Workspace、Governance Console 路由迁移、role-specific demo projection、`runtime-demo-001` 三角色 walkthrough 和前端测试。
