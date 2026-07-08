# Phase 11-P0 开发任务清单：Role-based Frontend Suite MVP

> 上位实现规格：`docs/3-phase实现/Phase11_P0RoleBasedFrontendSuite_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase11_P0FrontendRBAC_API与测试设计.md`  
> 前置冻结：`docs/3-phase实现/Phase10_P0冻结记录.md`  
> 当前目标：完成 Patient Portal / Clinician Workspace / Governance Console 的多角色前端最小演示闭环，但不做生产级登录、不接真实患者数据、不引入高风险医疗写操作。

---

# 一、Phase 11-P0 总目标

Phase 11-P0 要完成的是前端产品化与角色视图分层：

```text
Runtime / DecisionBoundary / Console Safe DTO
→ Role-specific DTO / Demo projection
→ RBAC RoleGuard
→ Patient Portal / Clinician Workspace / Governance Console
→ 同一 Runtime 三角色展示
```

最终要证明：

```text
患者、医生、治理人员围绕同一次 Runtime 看到不同视图；
患者端不看到医生和治理内部信息；
医生端不看到 raw prompt / secret / raw external response / full rationale；
治理端继续沿用 Safe DTO，不泄露患者原文；
前端不直接生成医疗结论，不执行高风险写操作。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P11P0-A | 建立 RBAC role model 与 route matrix | 待做 |
| P11P0-B | 重构 AppShell / PortalLayout / DemoRoleSwitcher | 待做 |
| P11P0-C | 迁移 Governance Console 到 /governance | 待做 |
| P11P0-D | 新增 Patient Portal 静态 MVP | 待做 |
| P11P0-E | 新增 Clinician Workspace 静态 MVP | 待做 |
| P11P0-F | 新增 Role-specific DTO types 与 demo data | 待做 |
| P11P0-G | 打通同一 runtime-demo-001 的三角色视图 | 待做 |
| P11P0-H | 前端测试、人工验证与敏感字段检查 | 待做 |
| P11P0-I | 冻结 Phase 11-P0 | 待做 |

---

# 三、P11P0-A：建立 RBAC role model 与 route matrix

## 目标

建立最小角色、权限和路由访问模型。

## 建议文件

```text
console-web/src/auth/rbac.ts
console-web/src/auth/roleTypes.ts
console-web/src/auth/RoleGuard.tsx
console-web/src/auth/ForbiddenPage.tsx
```

## 任务

```text
[ ] 定义 AppRole：PATIENT / CLINICIAN / GOVERNANCE_REVIEWER / SYSTEM_ADMIN / READ_ONLY_OBSERVER。
[ ] 定义 Portal：patient / clinician / governance / admin。
[ ] 定义 Permission。
[ ] 定义 ROLE_PERMISSIONS。
[ ] 实现 hasPermission(role, permission)。
[ ] 实现 canAccessPortal(role, portal)。
[ ] 实现 getDefaultRouteForRole(role)。
[ ] 实现 RoleGuard。
[ ] 实现 ForbiddenPage。
```

## 验收标准

```text
[ ] PATIENT 不能进入 /clinician 和 /governance。
[ ] CLINICIAN 不能进入 /patient 和 /governance。
[ ] GOVERNANCE_REVIEWER / SYSTEM_ADMIN / READ_ONLY_OBSERVER 能进入 /governance。
[ ] 未授权访问进入 /forbidden。
[ ] RBAC 单元测试覆盖。
```

---

# 四、P11P0-B：重构 AppShell / PortalLayout / DemoRoleSwitcher

## 目标

让前端成为多 Portal 应用，而不是单一 Console 页面集合。

## 建议文件

```text
console-web/src/app/App.tsx
console-web/src/app/routes.tsx
console-web/src/layout/RootShell.tsx
console-web/src/layout/PortalLayout.tsx
console-web/src/auth/DemoRoleSwitcher.tsx
```

## 任务

```text
[ ] 抽出 RootShell。
[ ] 新增 PortalLayout。
[ ] 新增 DemoRoleSwitcher。
[ ] 顶部展示当前角色和当前 Portal。
[ ] 根据角色默认跳转到对应 Portal。
[ ] 保留 Phase10 console 页面，但路由归位到 /governance。
```

## 验收标准

```text
[ ] 可以在本地演示切换 PATIENT / CLINICIAN / GOVERNANCE_REVIEWER。
[ ] 切换角色后导航菜单变化。
[ ] 当前角色清晰可见。
[ ] DemoRoleSwitcher 明确标记为 Demo only。
```

---

# 五、P11P0-C：迁移 Governance Console 到 /governance

## 目标

把 Phase10 Console 正式定位为管理端 / 治理端。

## 当前页面

```text
ConsoleOverviewPage
RuntimeTimelinePage
GovernanceDomainsPage
CandidateInboxPage
AuditBrowserPage
RuntimePage
EvaluationPage
CandidatePage
ReviewQueuePage
AuditCenterPage
```

## 新路由

```text
/governance/overview
/governance/runtimes
/governance/runtimes/:runtimeId
/governance/domains
/governance/candidates
/governance/audits
/governance/evaluations
/governance/audit-center
```

## 任务

```text
[ ] 将 Overview 挂到 /governance/overview。
[ ] 将 Runtime Timeline 挂到 /governance/runtimes。
[ ] 将 Governance Domains 挂到 /governance/domains。
[ ] 将 Candidate Inbox 挂到 /governance/candidates。
[ ] 将 Audit Browser 挂到 /governance/audits。
[ ] 将 Evaluation / Audit Center 归到 /governance/evaluations 和 /governance/audit-center。
[ ] 旧路由保留 redirect 或移除。
[ ] Governance 入口只允许 GOVERNANCE_REVIEWER / SYSTEM_ADMIN / READ_ONLY_OBSERVER。
```

## 验收标准

```text
[ ] Phase10 页面仍可访问。
[ ] Governance Console 仍只读。
[ ] 不新增 approve / reject / publish。
[ ] 不放宽 Safe DTO 边界。
```

---

# 六、P11P0-D：新增 Patient Portal 静态 MVP

## 目标

建立患者端最小产品视图。

## 建议文件

```text
console-web/src/portals/patient/pages/PatientHomePage.tsx
console-web/src/portals/patient/pages/SymptomIntakePage.tsx
console-web/src/portals/patient/pages/GuidedInquiryPage.tsx
console-web/src/portals/patient/pages/PatientSafeSummaryPage.tsx
console-web/src/portals/patient/components/PatientSafetyNotice.tsx
console-web/src/portals/patient/components/CollectedFactsCard.tsx
console-web/src/portals/patient/components/PatientQuestionList.tsx
```

## 任务

```text
[ ] PatientHomePage：展示患者入口、最近会话、安全声明。
[ ] SymptomIntakePage：展示症状输入表单，P0 可 mock。
[ ] GuidedInquiryPage：展示受控多轮问诊界面，P0 可 mock。
[ ] PatientSafeSummaryPage：展示患者安全摘要。
[ ] 页面文案明确 AI 不能替代医生。
[ ] 不展示 DDx、Evidence raw、Trace、Audit、Evaluation、Candidate。
```

## 验收标准

```text
[ ] PATIENT 可访问 /patient。
[ ] 患者页面不出现 ddx、audit、trace、candidate、evaluation 等治理词。
[ ] 患者页面不出现诊断结论化表达。
[ ] 紧急情况提示清楚。
```

---

# 七、P11P0-E：新增 Clinician Workspace 静态 MVP

## 目标

建立医生端最小病例工作台。

## 建议文件

```text
console-web/src/portals/clinician/pages/ClinicianDashboardPage.tsx
console-web/src/portals/clinician/pages/ClinicianCaseInboxPage.tsx
console-web/src/portals/clinician/pages/CaseWorkspacePage.tsx
console-web/src/portals/clinician/pages/ClinicianReportPage.tsx
console-web/src/portals/clinician/components/CaseFramePanel.tsx
console-web/src/portals/clinician/components/DdxBoard.tsx
console-web/src/portals/clinician/components/EvidencePanel.tsx
console-web/src/portals/clinician/components/RiskPanel.tsx
console-web/src/portals/clinician/components/ReportDraftEditor.tsx
```

## 任务

```text
[ ] ClinicianDashboardPage：展示待处理病例和风险概览。
[ ] ClinicianCaseInboxPage：展示病例列表。
[ ] CaseWorkspacePage：展示 CaseFrame / InquiryTimeline / DDxBoard / EvidencePanel / RiskPanel / ReportDraft。
[ ] ClinicianReportPage：展示报告草稿，P0 可以本地编辑。
[ ] 所有 AI 输出标注为辅助建议，医生保留最终判断权。
[ ] 不展示 raw prompt / secret / raw external response / full rationale。
```

## 验收标准

```text
[ ] CLINICIAN 可访问 /clinician/cases。
[ ] 医生端能展示同一个 runtime-demo-001 的病例视图。
[ ] DDx Board 明确为候选方向而非最终诊断。
[ ] Evidence Panel 只展示摘要和来源，不展示 raw payload。
```

---

# 八、P11P0-F：新增 Role-specific DTO types 与 demo data

## 目标

让三端消费不同类型，而不是共享完整 Runtime DTO。

## 建议文件

```text
console-web/src/shared/types/patientViews.ts
console-web/src/shared/types/clinicianViews.ts
console-web/src/shared/types/governanceViews.ts
console-web/src/demo/runtimeDemoData.ts
```

## 任务

```text
[ ] 定义 PatientRuntimeView。
[ ] 定义 PatientSessionSummary。
[ ] 定义 ClinicianCaseView。
[ ] 定义 ClinicianCaseSummary。
[ ] 复用 Governance Console DTO。
[ ] 新增 runtime-demo-001 mock 数据。
[ ] 保证 mock 三端 runtime_id 一致。
[ ] mock 数据不包含真实患者身份信息。
[ ] mock 数据不包含高风险具体治疗方案或处方剂量。
```

## 验收标准

```text
[ ] PatientRuntimeView 类型中不存在 ddx / audit / trace / evaluation 字段。
[ ] ClinicianCaseView 类型中不存在 raw_prompt / secret / raw_external_response 字段。
[ ] demo data 通过敏感字段检查。
```

---

# 九、P11P0-G：打通同一 runtime-demo-001 的三角色视图

## 目标

形成可面试演示的核心链路。

## 演示流程

```text
1. 选择 PATIENT，打开 /patient/sessions/runtime-demo-001/summary。
2. 查看患者安全摘要。
3. 切换 CLINICIAN，打开 /clinician/cases/runtime-demo-001。
4. 查看 CaseFrame / DDxBoard / EvidencePanel / ReportDraft。
5. 切换 GOVERNANCE_REVIEWER，打开 /governance/runtimes/runtime-demo-001。
6. 查看 Runtime Timeline / Audit / Candidate。
```

## 任务

```text
[ ] Runtime ID 在三端一致。
[ ] 三端展示粒度不同。
[ ] 提供一个 Demo Walkthrough 提示条或说明卡。
[ ] 能从页面文案中讲清楚“同一 Runtime 的三角色投影”。
```

## 验收标准

```text
[ ] 面试演示可以在 3 分钟内走完整条链路。
[ ] 不需要解释代码也能看懂三端职责。
[ ] 页面不暗示系统可替代医生。
```

---

# 十、P11P0-H：前端测试、人工验证与敏感字段检查

## 自动测试

```text
[ ] rbac.test.ts 覆盖角色权限。
[ ] RoleGuard.test.tsx 覆盖允许和拒绝。
[ ] PatientSafeSummaryPage.test.tsx 覆盖患者端禁显字段。
[ ] CaseWorkspacePage.test.tsx 覆盖医生端禁显字段。
[ ] Governance route test 覆盖治理端访问。
```

## 手工测试

```text
[ ] PATIENT 访问 /patient 成功。
[ ] PATIENT 访问 /clinician/cases 被拒绝。
[ ] PATIENT 访问 /governance/overview 被拒绝。
[ ] CLINICIAN 访问 /clinician/cases 成功。
[ ] CLINICIAN 访问 /governance/overview 被拒绝。
[ ] GOVERNANCE_REVIEWER 访问 /governance/overview 成功。
[ ] 三角色围绕 runtime-demo-001 展示不同视图。
[ ] 页面搜索敏感词无结果。
```

## 构建验证

```text
[ ] npm run typecheck
[ ] npm run build
[ ] 如执行 npm test / vitest，记录结果和 caveat
```

---

# 十一、P11P0-I：冻结 Phase 11-P0

## 冻结文件

```text
[ ] docs/3-phase实现/Phase11_P0人工测试结果.md
[ ] docs/3-phase实现/Phase11_P0冻结记录.md
[ ] 更新 docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md
```

## 冻结结论必须说明

```text
Phase 11-P0 已实现 Role-based Frontend Suite MVP。
Patient Portal / Clinician Workspace / Governance Console 已形成最小演示链路。
RBAC 只用于 P0 前端演示，不代表生产级认证。
患者端不展示医生和治理内部信息。
医生端不展示 raw prompt / secret / raw external response / full rationale。
治理端继续沿用 Phase10 Safe DTO。
未接真实患者数据，未接真实医疗系统，未实现生产级医生工作站。
```

---

# 十二、最终提醒

Phase 11-P0 的重点不是“页面越多越好”，而是：

```text
角色边界清楚；
三端职责清楚；
同一 Runtime 的多视图关系清楚；
患者安全边界清楚；
医生辅助定位清楚；
治理 Console 不被混成业务操作台。
```

如果实现过程中出现功能膨胀，优先砍掉：

```text
真实登录
真实提交
复杂角色管理
高级图表
在线处方
预约转诊
真实外部系统接入
```

保留：

```text
RBAC route guard
三端路由
三端页面
三端 DTO
runtime-demo-001 多视图演示
安全边界文案
敏感字段检查
```
