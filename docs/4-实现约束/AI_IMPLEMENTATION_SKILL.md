# AI Implementation Skill：ClinMindRuntime（Phase 11-P0 已冻结）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结；Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结；Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结；Phase 8-P2 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 已冻结；Phase 9-P0 Tool / MCP / Skills 受控接入 MVP 已冻结；Phase 10-P0 Governance Console / Runtime Console MVP 已冻结；Phase 11-P0 Role-based Frontend Suite MVP 已冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> 当前 Phase 11-P0 已冻结；在新的 Phase 12 或 Phase 11-P1 设计冻结前，只允许进行必要 bug fix、测试补强或文档修正，不得继续向 Phase 11-P0 增加新能力。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 11-P0 已冻结 |
| 前置状态 | Phase 1–11 P0 已冻结 |
| 当前冻结记录 | `docs/3-phase实现/Phase11_P0冻结记录.md` |
| 当前人工测试 | `docs/3-phase实现/Phase11_P0人工测试结果.md` |
| 当前设计依据 | `docs/3-phase实现/Phase11_P0RoleBasedFrontendSuite_实现规格.md` |
| API 与测试依据 | `docs/3-phase实现/Phase11_P0FrontendRBAC_API与测试设计.md` |
| 开发任务清单 | `docs/3-phase实现/Phase11_P0开发任务清单.md` |
| 当前实现目标 | 已完成并冻结：Role-based Frontend Suite MVP |

已完成主线：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，已冻结
- Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP，已冻结
- Phase 8-P2：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP，已冻结
- Phase 9-P0：Tool / MCP / Skills 受控接入 MVP，已冻结
- Phase 10-P0：Governance Console / Runtime Console MVP，已冻结
- Phase 11-P0：Role-based Frontend Suite MVP，已冻结

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- Phase 11-P0 = 基于 RBAC 的多角色前端产品原型层，已冻结
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统、不是模型直接诊断系统、不是自动训练/自动发布平台、不是外部工具自治调用平台、不是生产级在线诊疗系统、不是生产级医生工作站、不是生产级认证与权限系统

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider / Tool-MCP-Skills → Policy / Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation → Candidate / Governance → Role-specific DTO → Patient / Clinician / Governance Frontend Projection`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase11_P0冻结记录.md`
5. `docs/3-phase实现/Phase11_P0人工测试结果.md`
6. `docs/3-phase实现/Phase11_P0RoleBasedFrontendSuite_实现规格.md`
7. `docs/3-phase实现/Phase11_P0FrontendRBAC_API与测试设计.md`
8. `docs/3-phase实现/Phase11_P0开发任务清单.md`
9. `docs/3-phase实现/Phase10_P0冻结记录.md`
10. `docs/3-phase实现/Phase10_P0人工测试结果.md`
11. `docs/3-phase实现/Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`
12. `docs/3-phase实现/Phase10_P0Console_API与测试设计.md`
13. `docs/3-phase实现/Phase10_P0开发任务清单.md`
14. `docs/3-phase实现/Phase9_P0冻结记录.md`
15. `docs/3-phase实现/Phase9_P0人工测试结果.md`
16. `docs/3-phase实现/Phase8_P2冻结记录.md`
17. `docs/3-phase实现/Phase8_P1冻结记录.md`
18. `docs/3-phase实现/Phase8_P0冻结记录.md`
19. Phase 1–7 各阶段冻结记录

---

# 三、当前允许做的事情

Phase 11-P0 已冻结；当前只允许：

1. 对 Phase 1–11 已冻结阶段进行必要 bug fix、测试补强、文档修正，但不得借 bug fix 增加新能力。
2. 修复 Phase 11-P0 前端 RBAC / route / sensitive-field test 的缺陷，且必须保持冻结边界。
3. 为后续 Phase 11-P1 或 Phase 12 进行设计文档编写，但在新 skill 更新前不得实现新能力。
4. 运行并记录既有回归测试。

---

# 四、当前禁止做的事情

1. 向 Phase 1–11 P0 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不做生产级登录、注册、身份认证系统。
3. 不接入真实患者数据。
4. 不接入真实 HIS / EMR / LIS / PACS / 预约 / 支付系统。
5. 不做真实在线诊疗闭环。
6. 不做患者端自动诊断、自动开药、自动处方、自动转诊。
7. 不让患者端看到 DDx Board、内部风险评分、Evidence 原始内容、Trace、Audit、Evaluation、Candidate Governance。
8. 不让医生端看到 raw prompt、secret、raw external response、完整 chain-of-thought 或 full rationale。
9. 不让前端仅靠隐藏字段实现安全隔离。
10. 不把 Governance Console 的 approve / reject / publish / run 写操作混入 Phase 11-P0。
11. 不做生产级 RBAC 后台管理系统。
12. 不做复杂多租户、组织架构、真实权限同步。
13. 不做可拖拽低代码工作台。
14. 不让任何前端页面直接调用 Provider / Tool / Agent。
15. 不让任何前端页面绕过 SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 或 Candidate Governance。
16. 不在 localStorage、URL query、console.log 中保存或输出敏感医疗内容。
17. 不展示 raw patient dialogue。
18. 不展示完整 prompt 原文。
19. 不展示 secret / api key / private key。
20. 不展示 raw external response。
21. 不展示完整内部推理链或 full rationale。
22. 不改写历史冻结记录中的事实。

---

# 五、已冻结能力边界（勿再扩展）

| Phase | 边界 |
|---|---|
| Phase 6-P0 | InquiryPlanningAgent / AgentRuntime / AgentProposalValidator |
| Phase 7-P0 | RagEvidenceProvider / EvidenceValidation |
| Phase 7-P1 | KG-lite / GraphEvidenceProvider |
| Phase 8-P0 | `python-provider`、PythonProviderClient、ProviderValidation、Evidence rerank 增强、Provider Debug API |
| Phase 8-P1 | ProviderCapabilityProfile、ProviderCapabilityPolicy、JudgeProvider、RiskSignalClassifierProvider、Judge / Risk / Profile Validation、Debug API、Evaluation Scorer、ProviderGovernanceSnapshot、Candidate 映射 |
| Phase 8-P2 | ModelRegistryEntry、PromptRegistryEntry、TrainingDatasetVersion、ModelExperimentRecord、ModelEvaluationReport、ModelReleaseCandidate、ModelRollbackPlan、ModelGovernanceService、Model Governance Debug API、Evaluation Scorer、Candidate 映射 |
| Phase 9-P0 | ToolRegistryEntry、McpServerRegistryEntry、SkillRegistryEntry、ToolInvocationRuntime、ToolResultValidationService、Tool Governance Debug API、Evaluation Scorer、Candidate 映射 |
| Phase 10-P0 | Governance Console / Runtime Console 只读 API、Safe DTO、Runtime Timeline、Governance Domain Dashboard、Candidate Inbox、Audit Browser、Console Evaluation Scorer、console-web 只读页面 |
| Phase 11-P0 | Role-based Frontend Suite MVP、前端 RBAC demo、Patient Portal、Clinician Workspace、Governance Console 路由迁移、role-specific demo projection、runtime-demo-001 三角色 walkthrough |

Phase 6–11 P0 已冻结能力只能被后续新阶段按设计观察、复用和扩展，不能继续向已冻结范围堆新能力。

---

# 六、Phase 11-P0 可涉及范围

Phase 11-P0 已冻结范围包括：

```text
AppRole
Permission
Portal
ROLE_PERMISSIONS
RoleGuard
DemoRoleSwitcher
ForbiddenPage
RootShell
PortalLayout
Patient Portal pages
Clinician Workspace pages
Governance Portal routes
PatientRuntimeView
PatientSessionSummary
ClinicianCaseView
ClinicianCaseSummary
Governance Runtime View reuse
runtime-demo-001 seed data
RBAC route tests
Sensitive field render tests
```

Phase 11-P0 已输出：

```text
Role-specific frontend types
Demo-safe mock data
Patient Safe Summary View
Clinician Case Workspace View
Governance Console View
RBAC route matrix
Forbidden page
Demo role switcher
Frontend tests
Manual verification record
Freeze record
```

Phase 11-P0 仍不能输出或触发：

```text
Final diagnosis
Prescription
Medication dosage
Treatment plan execution
Referral order
Appointment booking
Payment
External message sending
RuntimeState mutation
Provider run
Tool invocation
Agent invocation
Candidate approval / rejection / publishing
Model / Prompt / Dataset / Tool / Skill publication
Raw Patient Dialogue
Raw Prompt
Secret / API Key / Private Key
Raw External Response
Internal Chain-of-thought / Full Rationale
```

---

# 七、三端边界

## 7.1 Patient Portal

允许：

```text
Patient Home
Symptom Intake demo form
Guided Inquiry demo view
Patient Safe Summary
Collected facts summary
Next safe questions
Safety notices
Care navigation suggestions
Disclaimer
```

禁止：

```text
DDx Board
Confidence ranking
Risk score internals
Evidence raw content
Trace nodes
Audit events
Evaluation results
Candidate governance
Model / Prompt / Tool / Provider internals
```

## 7.2 Clinician Workspace

允许：

```text
Clinician Dashboard
Case Inbox
Case Workspace
Case Frame
Inquiry Timeline
DDx Board as candidate directions
Evidence Summary Panel
Risk & Safety Panel
AI Suggested Questions
Clinician Report Draft as editable demo
```

禁止：

```text
raw prompt
secret
api key
private key
raw external response
internal chain-of-thought
full rationale
unredacted patient dialogue
```

## 7.3 Governance Console

允许：

```text
Phase 10-P0 Safe DTO 页面迁移到 /governance
Console Overview
Runtime Timeline
Governance Domains
Candidate Inbox
Audit Browser
Evaluation Runs
Audit Center
```

禁止：

```text
approve / reject / publish / run actions
RuntimeState mutation
Provider / Tool / Agent invocation
raw patient dialogue
raw prompt
secret
raw external response
full rationale
```

---

# 八、Phase 11-P0 已完成实现顺序

Phase 11-P0 已按以下顺序完成：

1. P11P0-A：建立 RBAC role model 与 route matrix。
2. P11P0-B：重构 AppShell / PortalLayout / DemoRoleSwitcher。
3. P11P0-C：迁移 Governance Console 到 `/governance`。
4. P11P0-D：新增 Patient Portal 静态 MVP。
5. P11P0-E：新增 Clinician Workspace 静态 MVP。
6. P11P0-F：新增 Role-specific DTO types 与 demo data。
7. P11P0-G：打通同一 `runtime-demo-001` 的三角色视图。
8. P11P0-H：前端测试、人工验证与敏感字段检查。
9. P11P0-I：冻结 Phase 11-P0。

冻结后不得跳过新阶段设计，继续向 Phase 11-P0 直接堆页面或新能力。

---

# 九、回归测试要求

修复 Phase 11-P0 前端时，至少回归：

```text
npm run typecheck
npm run build
```

如项目中已有 Vitest 或新增测试，至少覆盖：

```text
rbac.test.ts
RoleGuard.test.tsx
PatientSafeSummaryPage sensitive-field test
CaseWorkspacePage sensitive-field test
Governance route access test
```

如果修改 Phase 10 Console 页面或 Console API 相关代码，必须额外回归：

- Java：ConsoleSafeDtoMapperTest
- Java：ConsoleAccessPolicyTest
- Java：ConsoleOverviewControllerTest
- Java：RuntimeTimelineControllerTest
- Java：GovernanceDashboardControllerTest
- Java：ConsoleCandidateInboxControllerTest
- Java：ConsoleAuditBrowserControllerTest
- Java：ConsoleScorerTest

并保持 Phase 1–9 P0 既有 Runtime / Agent / Evidence / Graph / Provider / ModelGov / ToolGov / Evaluation / Candidate / Persistence / Audit 测试不回归。

---

# 十、冻结记录

Phase 11-P0 已新增：

```text
docs/3-phase实现/Phase11_P0人工测试结果.md
docs/3-phase实现/Phase11_P0冻结记录.md
```

冻结记录已说明：

1. 已实现哪些 Patient Portal 页面。
2. 已实现哪些 Clinician Workspace 页面。
3. Phase10 Console 如何迁移到 Governance Portal。
4. RBAC role / permission / portal matrix 如何工作。
5. 同一 `runtime-demo-001` 如何形成三角色投影。
6. Patient Portal 如何避免展示 DDx / Trace / Audit / Evaluation。
7. Clinician Workspace 如何避免展示 raw prompt / secret / raw external response / full rationale。
8. Governance Console 如何继续沿用 Safe DTO。
9. 前端 typecheck / build / test 结果。
10. P0 未实现的生产级能力和后置任务。

---

# 十一、最终结论

当前 AI 实现约束是：

```text
Phase 10-P0 Governance Console / Runtime Console MVP 已冻结；
Phase 11-P0 Role-based Frontend Suite MVP 已冻结；
当前不允许继续向 Phase 11-P0 增加新能力；
后续新能力必须先完成 Phase 11-P1 或 Phase 12 设计与 skill 更新；
不可以接真实患者数据、真实医疗系统或生产级认证；
不可以让患者端看到医生或治理内部信息；
不可以让医生端看到 raw prompt、secret、raw external response 或 full rationale；
不可以在 Phase 11-P0 中加入 approve / reject / publish / run 写操作；
不可以让任何前端页面绕过 Runtime、SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 或 Candidate Governance。
```
