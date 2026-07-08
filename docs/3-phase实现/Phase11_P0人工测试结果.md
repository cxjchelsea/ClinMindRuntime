# Phase 11-P0 人工测试结果：Role-based Frontend Suite MVP

> 规格：`Phase11_P0RoleBasedFrontendSuite_实现规格.md`
> API 与测试设计：`Phase11_P0FrontendRBAC_API与测试设计.md`
> 任务清单：`Phase11_P0开发任务清单.md`
> 测试日期：2026-07-08

---

# 一、测试范围

本次验证覆盖 Phase 11-P0 的三角色前端演示闭环：

```text
AppRole / Permission / Portal
-> RoleGuard / DemoRoleSwitcher
-> Patient Portal
-> Clinician Workspace
-> Governance Console under /governance
-> runtime-demo-001 role-specific projections
```

本次验证不覆盖生产级认证、真实患者数据、真实 HIS / EMR / LIS / PACS / 预约 / 支付系统、在线诊疗闭环、处方、转诊、支付、消息发送、Provider / Tool / Agent 直接调用、approve / reject / publish / run 写操作。

---

# 二、自动化测试结果

## TypeScript

命令：

```powershell
& 'D:\cxj\software\nodejs\npm.cmd' run typecheck
```

结果：

```text
PASS
```

## Vitest

命令：

```powershell
& 'D:\cxj\software\nodejs\npm.cmd' test
```

结果：

```text
20 test files passed
44 tests passed
0 failed
```

覆盖：

- RBAC role / permission matrix
- RoleGuard allow / deny behavior
- Governance route access
- `/governance/runtimes/:runtimeId` route param loading
- Patient Safe Summary sensitive-field exclusion
- Clinician Case Workspace sensitive-field exclusion
- Existing Phase10 console-web smoke and page tests

## Production build

命令：

```powershell
& 'D:\cxj\software\nodejs\npm.cmd' run build
```

结果：

```text
PASS
```

---

# 三、人工场景验证

## 场景 1：Demo role switcher

验证点：

- Header 展示当前 Portal role。
- `PATIENT` 切换后进入 `/patient`。
- `CLINICIAN` 切换后进入 `/clinician/cases`。
- `GOVERNANCE_REVIEWER` 切换后进入 `/governance/overview`。

结果：通过。

## 场景 2：Patient Portal

验证点：

- `PATIENT` 可访问 `/patient`。
- `PATIENT` 可访问 `/patient/intake`、`/patient/inquiry`、`/patient/sessions/runtime-demo-001/summary`。
- 患者端展示安全摘要、已收集事实、下一步问询、安全提示和照护导航。
- 页面文案明确 AI 不能替代医生。
- 患者端不展示 DDx、trace、audit、evaluation、candidate、raw evidence、model prompt、internal reasoning。

结果：通过。

## 场景 3：Clinician Workspace

验证点：

- `CLINICIAN` 可访问 `/clinician/cases`。
- `CLINICIAN` 可访问 `/clinician/cases/runtime-demo-001`。
- 医生端展示 Case Frame、Inquiry Timeline、DDx Board、Evidence Panel、Risk Panel、AI Suggestions、Report Draft。
- DDx Board 明确为候选方向而非最终诊断。
- Evidence Panel 只展示摘要、来源和相关性。
- 医生端不展示 raw prompt、secret、api key、private key、raw external response、internal chain-of-thought、full rationale、unredacted patient dialogue。

结果：通过。

## 场景 4：Governance Console

验证点：

- Governance 主导航收敛为 `/governance/overview`、`/governance/runtimes`、`/governance/runtimes/:runtimeId`、`/governance/domains`、`/governance/candidates`、`/governance/audits`、`/governance/evaluations`、`/governance/audit-center`。
- 旧路由保留 redirect，不出现在主导航。
- `/governance/runtimes/runtime-demo-001` 会读取 `runtimeId` 并加载对应 timeline。
- Governance Console 继续复用 Phase10 Safe DTO 和只读页面。
- 不新增 approve / reject / publish / run 写操作。

结果：通过。

## 场景 5：RBAC deny

验证点：

- `PATIENT` 访问 `/clinician/cases` 被拒绝。
- `PATIENT` 访问 `/governance/overview` 被拒绝。
- `CLINICIAN` 访问 `/governance/overview` 被拒绝。
- `GOVERNANCE_REVIEWER` 可访问 `/governance/overview`。

结果：通过。

## 场景 6：runtime-demo-001 三角色投影

验证点：

- Patient projection：`/patient/sessions/runtime-demo-001/summary`。
- Clinician projection：`/clinician/cases/runtime-demo-001`。
- Governance projection：`/governance/runtimes/runtime-demo-001`。
- 三端围绕同一 `runtime-demo-001` 展示不同粒度视图。

结果：通过。

---

# 四、保留说明

- Phase 11-P0 的 RBAC 仅用于前端 Demo，不代表生产级认证。
- Patient / Clinician 数据为 demo-safe mock projection，不接真实患者数据。
- Governance Console 仍依赖 Phase10 Safe DTO 和只读 API。
- 本阶段未引入任何 RuntimeState mutation、Provider run、Tool invocation、Agent invocation 或 Candidate 发布动作。

---

# 五、结论

Phase 11-P0 Role-based Frontend Suite MVP 已通过自动化验证和人工场景验收，可进入冻结。
