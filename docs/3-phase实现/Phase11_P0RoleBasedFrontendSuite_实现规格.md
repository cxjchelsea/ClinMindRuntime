# Phase 11-P0 Role-based Frontend Suite 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 前置冻结：Phase 10-P0 Governance Console / Runtime Console 已冻结  
> 当前 Phase：Phase 11-P0 设计阶段  
> 当前目标：在既有 Runtime / Governance Console 的基础上，设计一个基于 RBAC 的多角色前端套件，使患者、医生、治理人员围绕同一次 Runtime 获得不同粒度、不同权限、不同安全边界的视图。

---

# 一、Phase 定位

Phase 11-P0 不是新增医疗推理能力，也不是重新实现 Runtime。

Phase 11-P0 的目标是：

```text
把 ClinMindRuntime 从“后端治理 Runtime 原型 + 管理端 Console”
推进为“面向患者、医生、治理人员的多角色产品前端原型”。
```

核心命题：

```text
同一次 Runtime，不同角色只能看到不同投影。

Patient View：只看到 DecisionBoundary 允许的患者安全摘要、补问问题和安全提醒。
Clinician View：看到结构化病例、证据、风险、候选诊断方向和医生可编辑报告草稿。
Governance View：看到 Runtime Timeline、Trace、Audit、Evaluation、Candidate 与治理域状态。
```

Phase 11-P0 的重点不是页面数量，而是建立一套清楚的前端角色边界：

```text
RBAC Role
↓
Route Access Control
↓
API Scope Control
↓
Role-specific DTO Projection
↓
Frontend Rendering
```

---

# 二、前置状态

当前已经具备：

```text
Phase 1–5：Runtime / CaseFrame / SafetyGate / DecisionBoundary / Trace / Audit 基础已冻结
Phase 6-P0：Agent Runtime / Agent Proposal 受控执行层已冻结
Phase 7-P0：RAG EvidenceProvider MVP 已冻结
Phase 7-P1：KG-lite / Graph Evidence 原型已冻结
Phase 8-P0：Python Provider / EmbeddingProvider MVP 已冻结
Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结
Phase 8-P2：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 已冻结
Phase 9-P0：Tool / MCP / Skills 受控接入 MVP 已冻结
Phase 10-P0：Governance Console / Runtime Console MVP 已冻结
```

Phase 10-P0 已经把 Runtime、Trace、Audit、Evaluation、Candidate、Provider、Model、Tool 等治理信息聚合到 Console 层。

Phase 11-P0 在此基础上做前端产品化设计，但不能改变前面已经冻结的治理原则：

```text
Agent / RAG / Model / Tool 不能直接产生患者端最终输出。
患者端不能绕过 DecisionBoundary。
医生端不能看到 secret、raw prompt、raw external response 或完整内部推理链。
管理端 P0 仍以治理观察为主，不能直接接管 Runtime。
```

---

# 三、当前不做什么

Phase 11-P0 明确不做：

```text
1. 不做生产级登录、注册、身份认证系统。
2. 不接入真实患者数据。
3. 不接入真实 HIS / EMR / LIS / PACS / 预约 / 支付系统。
4. 不做真实在线诊疗闭环。
5. 不做患者端自动诊断、自动开药、自动处方、自动转诊。
6. 不让患者端看到 DDx Board、内部风险评分、Evidence 原始内容、Trace、Audit、Evaluation。
7. 不让医生端看到 raw prompt、secret、raw external response、完整 chain-of-thought。
8. 不让前端仅靠隐藏字段实现安全隔离。
9. 不把 Governance Console 的 approve / reject / publish 写操作混入 Phase 11-P0。
10. 不做生产级 RBAC 后台管理系统。
11. 不做复杂多租户、组织架构、真实权限同步。
12. 不做可拖拽低代码工作台。
```

P0 可以做：

```text
Patient Portal 信息架构与静态 / 半动态 MVP
Clinician Workspace 信息架构与静态 / 半动态 MVP
Governance Console 路由归位与管理端定位
Demo Role Switcher
RoleGuard / RBAC route matrix
Role-specific DTO 设计
同一 Runtime 的三角色投影演示
Mock / Seed Demo Data
前端测试与冻结记录
```

---

# 四、总体产品结构

Phase 11-P0 的前端套件由三端组成：

```text
ClinMind Frontend Suite
├── Patient Portal
│   ├── Patient Home
│   ├── Symptom Intake
│   ├── Guided Inquiry
│   ├── Patient Safe Summary
│   └── Session History
│
├── Clinician Workspace
│   ├── Clinician Dashboard
│   ├── Case Inbox
│   ├── Case Workspace
│   ├── Case Frame
│   ├── DDx Board
│   ├── Evidence Panel
│   ├── Risk & Safety Panel
│   └── Clinician Report Draft
│
└── Governance Console
    ├── Console Overview
    ├── Runtime Timeline
    ├── Governance Domains
    ├── Candidate Inbox
    ├── Audit Browser
    ├── Evaluation Runs
    └── Audit Center
```

推荐采用单一 React 应用，多 Portal 路由结构：

```text
frontend / console-web
└── src
    ├── app
    │   ├── App.tsx
    │   ├── routes.tsx
    │   └── RoleRouter.tsx
    │
    ├── auth
    │   ├── RoleContextProvider.tsx
    │   ├── RoleGuard.tsx
    │   ├── DemoRoleSwitcher.tsx
    │   └── rbac.ts
    │
    ├── portals
    │   ├── patient
    │   │   ├── pages
    │   │   ├── components
    │   │   └── api
    │   │
    │   ├── clinician
    │   │   ├── pages
    │   │   ├── components
    │   │   └── api
    │   │
    │   └── governance
    │       ├── pages
    │       ├── components
    │       └── api
    │
    └── shared
        ├── components
        ├── layout
        ├── api
        ├── types
        └── utils
```

说明：

```text
1. P0 可以在现有 console-web 上演进，不必立即新建 patient-web / clinician-web。
2. 当前 console-web 中的 Phase10 页面迁移到 portals/governance。
3. DemoRoleSwitcher 只用于本地演示，不代表生产认证。
4. 真实安全隔离必须依赖后端 Role-specific DTO，而不是前端隐藏字段。
```

---

# 五、角色模型

P0 最小角色模型：

```text
PATIENT
CLINICIAN
GOVERNANCE_REVIEWER
SYSTEM_ADMIN
READ_ONLY_OBSERVER
```

角色定义：

```text
PATIENT：患者本人，只能查看自己的患者安全视图。
CLINICIAN：医生 / 临床人员，查看病例工作台和医生侧 AI 辅助信息。
GOVERNANCE_REVIEWER：治理审核人员，查看 Console、Audit、Evaluation、Candidate。
SYSTEM_ADMIN：系统管理员，可查看治理信息和系统配置入口；P0 不做真实配置写入。
READ_ONLY_OBSERVER：只读观察者，可查看脱敏治理视图，不能做任何写操作。
```

---

# 六、RBAC 权限矩阵

| 能力 | PATIENT | CLINICIAN | GOVERNANCE_REVIEWER | SYSTEM_ADMIN | READ_ONLY_OBSERVER |
|---|---:|---:|---:|---:|---:|
| 访问 Patient Portal | ✅ | ❌ | ❌ | ❌ | ❌ |
| 发起问诊 / 症状输入 | ✅ | ❌ | ❌ | ❌ | ❌ |
| 查看本人 Patient Safe Summary | ✅ | ❌ | ❌ | ❌ | ❌ |
| 查看患者补问问题 | ✅ | ✅ | ❌ | ❌ | ❌ |
| 访问 Clinician Workspace | ❌ | ✅ | ❌ | ❌ | ❌ |
| 查看 Case Frame | ❌ | ✅ | ❌ | ❌ | ❌ |
| 查看 DDx Board | ❌ | ✅ | ❌ | ❌ | ❌ |
| 查看 Evidence Summary | ❌ | ✅ | ✅ | ✅ | ✅ |
| 查看 Risk & Safety Panel | ❌ | ✅ | ✅ | ✅ | ✅ |
| 编辑 Clinician Report Draft | ❌ | ✅ | ❌ | ❌ | ❌ |
| 访问 Governance Console | ❌ | ❌ | ✅ | ✅ | ✅ |
| 查看 Runtime Timeline | ❌ | ❌ | ✅ | ✅ | ✅ |
| 查看 Audit Browser | ❌ | ❌ | ✅ | ✅ | ✅ |
| 查看 Candidate Inbox | ❌ | ❌ | ✅ | ✅ | ✅ |
| 执行 Candidate approve/reject/publish | ❌ | ❌ | ❌ | ❌ | ❌ |
| 查看系统配置入口 | ❌ | ❌ | ❌ | ✅ | ❌ |
| 执行系统配置写入 | ❌ | ❌ | ❌ | ❌ | ❌ |

P0 原则：

```text
所有角色均不允许通过 Phase 11-P0 前端执行高风险写操作。
所有医疗相关输出必须来自 Runtime 已经生成的 role-specific projection。
患者端、医生端、治理端不得共享同一个完整 Runtime DTO。
```

---

# 七、路由设计

## 7.1 根路由

```text
/
/login-demo
/forbidden
```

P0 可以不做真实登录页，用 `DemoRoleSwitcher` 选择演示角色。

角色默认落点：

```text
PATIENT → /patient
CLINICIAN → /clinician/cases
GOVERNANCE_REVIEWER → /governance/overview
SYSTEM_ADMIN → /governance/overview
READ_ONLY_OBSERVER → /governance/overview
```

## 7.2 Patient Portal 路由

```text
/patient
/patient/intake
/patient/sessions
/patient/sessions/:sessionId
/patient/sessions/:sessionId/summary
```

页面定位：

```text
PatientHomePage：患者首页，展示最近问诊和新建入口。
SymptomIntakePage：症状输入页，采集主诉、持续时间、伴随症状。
GuidedInquiryPage：受控多轮问诊页面。
PatientSafeSummaryPage：患者安全摘要，只展示 DecisionBoundary 允许内容。
PatientSessionHistoryPage：历史会话列表。
```

## 7.3 Clinician Workspace 路由

```text
/clinician
/clinician/cases
/clinician/cases/:caseId
/clinician/cases/:caseId/report
```

页面定位：

```text
ClinicianDashboardPage：医生首页，展示待处理病例和高风险提醒。
CaseInboxPage：病例列表。
CaseWorkspacePage：单病例工作台，聚合病例、证据、风险、候选诊断、报告草稿。
ClinicianReportPage：医生报告草稿查看与编辑。
```

## 7.4 Governance Console 路由

```text
/governance
/governance/overview
/governance/runtimes
/governance/runtimes/:runtimeId
/governance/domains
/governance/candidates
/governance/audits
/governance/evaluations
/governance/audit-center
```

说明：

```text
现有 Phase10 Console 页面应迁移或包装到 /governance 下。
旧 /overview、/runtime-timeline 等路由可临时保留 redirect，最终收敛到 /governance/*。
```

---

# 八、同一 Runtime 的三角色投影

Phase 11-P0 最核心的展示对象是同一个 Runtime 在三种角色下的不同视图。

目标链路：

```text
runtime-demo-001
├── PatientRuntimeView
│   ├── safe_summary
│   ├── next_questions
│   ├── safety_notices
│   └── allowed_actions
│
├── ClinicianCaseView
│   ├── case_frame
│   ├── inquiry_timeline
│   ├── ddx_board
│   ├── evidence_panel
│   ├── risk_panel
│   ├── ai_suggestions
│   └── report_draft
│
└── GovernanceRuntimeView
    ├── runtime_timeline
    ├── governance_domains
    ├── candidates
    ├── audits
    └── evaluation_signals
```

前端展示原则：

```text
PatientRuntimeView 不包含 DDx Board、Evidence raw、Trace、Audit、Evaluation。
ClinicianCaseView 不包含 raw prompt、secret、raw external response、完整内部推理链。
GovernanceRuntimeView 不包含患者可识别原文或未脱敏业务内容。
```

---

# 九、Role-specific DTO 设计

## 9.1 PatientRuntimeViewDto

```text
session_id: string
runtime_id: string
status: string
started_at: string
updated_at: string
safe_summary: string
collected_facts: PatientFactSummary[]
next_questions: PatientQuestion[]
safety_notices: SafetyNotice[]
care_navigation: CareNavigationSuggestion[]
allowed_actions: string[]
disclaimer: string
```

字段说明：

```text
safe_summary：患者可理解的安全总结，不包含诊断结论化表达。
collected_facts：已收集信息的通俗摘要。
next_questions：Runtime 决定可继续询问的问题。
safety_notices：何时需要及时就医、转人工或急诊。
care_navigation：科室建议、观察建议、非处方级别的安全提醒。
allowed_actions：继续回答、结束会话、联系医生等安全动作。
```

禁止字段：

```text
ddx_candidates
confidence_score
model_prompt
internal_reasoning
raw_evidence
raw_tool_result
audit_events
evaluation_result
candidate_governance
```

## 9.2 ClinicianCaseViewDto

```text
case_id: string
runtime_id: string
patient_summary: PatientSummary
case_frame: CaseFrameView
inquiry_timeline: InquiryTurnView[]
ddx_board: DdxCandidateView[]
evidence_panel: EvidenceItemView[]
risk_panel: RiskSignalView[]
ai_suggestions: ClinicianSuggestionView[]
report_draft: ClinicianReportDraftView
runtime_boundary_summary: RuntimeBoundarySummary
```

字段说明：

```text
case_frame：结构化病例，不含不必要的个人隐私原文。
ddx_board：候选鉴别诊断方向，明确标记为 AI 辅助，不是最终诊断。
evidence_panel：证据摘要和来源引用，不展示 raw retrieval payload。
risk_panel：风险信号、红旗症状、需要医生重点确认的内容。
ai_suggestions：补问建议、检查方向、报告结构建议。
report_draft：医生可编辑草稿，P0 可以前端本地编辑，不做正式提交。
runtime_boundary_summary：说明哪些内容患者可见、哪些仅医生可见。
```

禁止字段：

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

## 9.3 GovernanceRuntimeViewDto

P0 复用 Phase 10-P0 的 Console DTO：

```text
ConsoleOverviewDto
RuntimeTimelineDto
RuntimeTimelineNodeDto
GovernanceDomainCardDto
CandidateInboxItemDto
AuditBrowserItemDto
```

新增要求：

```text
所有 Governance DTO 继续走 Safe DTO mapper。
不得因为 Phase 11 前端整合而放宽 Phase 10 的敏感字段过滤。
```

---

# 十、页面设计规格

## 10.1 PatientHomePage

目标：给患者一个安全、低压力的入口。

布局：

```text
Header：ClinMind Patient Portal
主卡片：今天哪里不舒服？
最近会话：最近一次问诊状态
安全声明：AI 不能替代医生，紧急情况请及时就医
入口按钮：开始新的症状填写 / 查看历史记录
```

不得展示：

```text
诊断排行榜
置信度
治理数据
模型名称
内部链路
```

## 10.2 SymptomIntakePage

目标：采集主诉与基础信息。

字段：

```text
主要不适
持续时间
严重程度
伴随症状
既往相关情况
是否有紧急危险信号
```

P0 可使用前端 mock submit，生成 demo session。

## 10.3 GuidedInquiryPage

目标：展示 Runtime 受控多轮问诊。

布局：

```text
左侧：对话流
右侧：当前已收集信息 / 安全提醒 / 下一步
底部：患者输入框
```

P0 限制：

```text
不直接调用 LLM。
不直接调用 Agent。
不让前端生成医疗建议。
可以使用 mock response 或读取后端 PatientRuntimeView。
```

## 10.4 PatientSafeSummaryPage

目标：展示 DecisionBoundary 允许的患者摘要。

内容：

```text
你描述的主要问题
目前已经了解的信息
建议补充的信息
日常观察建议
何时需要及时就医
免责声明
```

## 10.5 ClinicianDashboardPage

目标：医生首页。

内容：

```text
待处理病例数量
高风险病例数量
最近更新病例
需要医生确认的 AI 建议
```

## 10.6 CaseInboxPage

目标：病例列表。

字段：

```text
case_id
status
risk_level
chief_complaint_summary
updated_at
assigned_clinician
```

## 10.7 CaseWorkspacePage

目标：医生核心工作台。

布局：

```text
顶部：病例状态、风险等级、Runtime ID
左列：Case Frame / Inquiry Timeline
中列：DDx Board / AI Suggested Questions
右列：Evidence Panel / Risk Panel
底部：Clinician Report Draft
```

核心边界：

```text
DDx Board 只能表达候选方向。
Evidence Panel 只能展示摘要和来源。
Report Draft 必须标记为医生待确认。
```

## 10.8 Governance Portal

目标：承接 Phase10 Console。

改造：

```text
Console Overview → /governance/overview
Runtime Timeline → /governance/runtimes/:runtimeId
Governance Domains → /governance/domains
Candidate Inbox → /governance/candidates
Audit Browser → /governance/audits
Evaluation Runs → /governance/evaluations
Audit Center → /governance/audit-center
```

---

# 十一、前端安全边界

Phase 11-P0 必须遵循：

```text
1. 所有 Portal 均使用 RoleGuard。
2. Patient Portal 不请求 Governance API。
3. Clinician Workspace 不请求 Governance Audit API。
4. Governance Console 不展示未脱敏患者原文。
5. DemoRoleSwitcher 只能用于本地演示。
6. 不在 localStorage 保存敏感医疗内容。
7. 不在 URL query 中放患者原文、症状详情、prompt、tool result。
8. 不在 console.log 打印完整 DTO。
9. 不在前端硬编码真实患者数据。
10. Mock 数据必须标记为 demo。
```

---

# 十二、Phase 11-P0 最小演示链路

P0 最小闭环：

```text
Demo Role Switcher
↓
选择 PATIENT
↓
查看 Patient Portal / Patient Safe Summary
↓
切换 CLINICIAN
↓
查看同一个 runtime-demo-001 的 Clinician Case Workspace
↓
切换 GOVERNANCE_REVIEWER
↓
查看同一个 runtime-demo-001 的 Runtime Timeline / Audit / Candidate
↓
证明：同一 Runtime 被投影成三种不同角色视图
```

这条链路必须能讲清楚：

```text
前端不是绕过 Runtime 做医疗判断；
前端只是消费 Runtime 已经生成、后端已经裁剪过的 Role-specific DTO；
角色不同，视图不同，权限不同，安全边界不同。
```

---

# 十三、实现边界

Phase 11-P0 实现优先级：

```text
P11P0-A：建立 RBAC role model 与 route matrix
P11P0-B：重构 AppShell，支持 PortalLayout 与 DemoRoleSwitcher
P11P0-C：迁移 Governance Console 到 /governance
P11P0-D：新增 Patient Portal 静态 MVP
P11P0-E：新增 Clinician Workspace 静态 MVP
P11P0-F：新增 Role-specific DTO types 与 mock data
P11P0-G：打通同一 runtime-demo-001 的三角色视图
P11P0-H：补充前端测试与人工验证记录
P11P0-I：冻结 Phase 11-P0
```

P0 不要求：

```text
真实登录
真实后端患者 API
真实医生编辑提交
真实在线问诊
真实生产权限后台
```

---

# 十四、冻结标准

Phase 11-P0 可冻结条件：

```text
1. 三端信息架构明确并实现最小页面。
2. DemoRoleSwitcher 可切换 PATIENT / CLINICIAN / GOVERNANCE_REVIEWER / SYSTEM_ADMIN / READ_ONLY_OBSERVER。
3. RoleGuard 能阻止无权限访问路由。
4. Patient Portal 不展示 DDx / Trace / Audit / Evaluation。
5. Clinician Workspace 不展示 raw prompt / secret / raw external response / full rationale。
6. Governance Console 保持 Phase10 Safe DTO 边界。
7. 同一个 runtime-demo-001 可被三端以不同视图展示。
8. 页面文案明确 AI 不是最终诊断主体。
9. 前端 typecheck / build 通过。
10. 人工测试记录覆盖三角色切换、Forbidden 页面、敏感字段不展示。
```

---

# 十五、最终结论

Phase 11-P0 的目标不是做一个更复杂的后台，而是把 ClinMindRuntime 的核心思想产品化：

```text
Runtime 是统一主控层；
RBAC 决定用户能进入哪个 Portal；
Role-specific DTO 决定用户能看到什么；
Patient / Clinician / Governance 三端围绕同一次 Runtime 形成不同视图；
前端不直接生成医疗结论，不绕过 SafetyGate / DecisionBoundary，不泄露治理内部信息。
```

Phase 11-P0 完成后，ClinMindRuntime 将从“后端治理 Runtime 原型”升级为“可被用户角色理解的医疗 AI 产品原型”。
