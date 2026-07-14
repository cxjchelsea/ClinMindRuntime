# AI Implementation Skill：ClinMindRuntime（Phase 11-P1 已冻结 / Phase 12-P0 设计入口）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0/P1 已冻结；Phase 8-P0/P1/P2 已冻结；Phase 9-P0 已冻结；Phase 10-P0 已冻结；Phase 11-P0/P1 已冻结。
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 11-P1 已冻结；当前只允许修复明确回归，Phase 12-P0 必须先完成 Clinical Evidence Engine 的实现规格、API/测试设计和任务清单，再进入代码实现。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 11-P1 已冻结；Phase 12-P0 设计准备入口 |
| 前置状态 | Phase 1–11 P1 已冻结 |
| 前置冻结记录 | `docs/3-phase实现/Phase11_P1冻结记录.md` |
| 前置人工测试 | `docs/3-phase实现/Phase11_P1人工测试结果.md` |
| 当前设计依据 | `docs/3-phase实现/Phase11_P1RoleSpecificViewAPI_BFF_实现规格.md` |
| API 与测试依据 | `docs/3-phase实现/Phase11_P1ViewAPI与测试设计.md` |
| 开发任务清单 | `docs/3-phase实现/Phase11_P1开发任务清单.md` |
| 当前实现目标 | 保持 Phase 11-P1 冻结边界；下一步先设计 Phase 12-P0 Clinical Evidence Engine |

已完成主线：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，已冻结
- Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP，已冻结
- Phase 8-P2：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP，已冻结
- Phase 9-P0：Tool / MCP / Skills 受控接入 MVP，已冻结
- Phase 10-P0：Governance Console / Runtime Console MVP，已冻结
- Phase 11-P0：Role-based Frontend Suite MVP，已冻结
- Phase 11-P1：Role-specific View API / Frontend BFF 已完成设计，当前可进入受限实现

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- Phase 11-P1 = 后端 role-specific view projection + 前端 API-first BFF 改造
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统、不是模型直接诊断系统、不是自动训练/自动发布平台、不是外部工具自治调用平台、不是生产级在线诊疗系统、不是生产级医生工作站、不是生产级认证与权限系统

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider / Tool-MCP-Skills → Policy / Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation → Candidate / Governance → PatientViewProjectionService / ClinicianCaseProjectionService → Role-specific DTO → Patient / Clinician / Governance Frontend Projection`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase11_P1RoleSpecificViewAPI_BFF_实现规格.md`
5. `docs/3-phase实现/Phase11_P1ViewAPI与测试设计.md`
6. `docs/3-phase实现/Phase11_P1开发任务清单.md`
7. `docs/3-phase实现/Phase11_P0冻结记录.md`
8. `docs/3-phase实现/Phase11_P0人工测试结果.md`
9. `docs/3-phase实现/Phase11_P0RoleBasedFrontendSuite_实现规格.md`
10. `docs/3-phase实现/Phase11_P0FrontendRBAC_API与测试设计.md`
11. `docs/3-phase实现/Phase11_P0开发任务清单.md`
12. `docs/3-phase实现/Phase10_P0冻结记录.md`
13. `docs/3-phase实现/Phase10_P0人工测试结果.md`
14. `docs/3-phase实现/Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`
15. `docs/3-phase实现/Phase10_P0Console_API与测试设计.md`
16. `docs/3-phase实现/Phase9_P0冻结记录.md`
17. `docs/3-phase实现/Phase9_P0人工测试结果.md`
18. Phase 1–8 各阶段冻结记录

---

# 三、当前允许做的事情

当前只允许围绕 Phase 11-P1 任务清单进行受限实现：

1. 建立 Patient / Clinician role-specific view DTO。
2. 建立 view safety policy / sanitizer / projection read audit。
3. 实现 `PatientViewProjectionService`，从 Runtime / CaseFrame / PatientOutput / DecisionBoundary 生成患者端安全视图。
4. 实现 `ClinicianCaseProjectionService`，从 Runtime / CaseFrame / DDxBoard / EvidenceGraph / ClinicianReport 生成医生端病例工作台视图。
5. 实现只读 `PatientViewController`。
6. 实现只读 `ClinicianViewController`。
7. 建立 `runtime-demo-001` seed runtime adapter，使 P0 demo projection 可由后端 API 读取。
8. 新增前端 `patientClient` / `clinicianClient` / hooks，使 Patient Portal 与 Clinician Workspace 从 mock-first 改为 API-first。
9. 保留 demo fallback，但必须明确标记为 demo/fallback，不能当作真实业务数据。
10. 编写 Java projection tests、controller tests、view sanitizer tests。
11. 编写前端 API-first / fallback / sensitive-field tests。
12. 运行并记录 Java targeted tests、`npm run typecheck`、`npm run build`、`npm run test`。
13. 对 Phase 1–11 P0 已冻结阶段进行必要 bug fix、测试补强、文档修正，但不得借 bug fix 增加新能力。

---

# 四、当前禁止做的事情

1. 向 Phase 1–11 P0 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不做生产级登录、注册、身份认证系统。
3. 不接入真实患者数据。
4. 不接入真实 HIS / EMR / LIS / PACS / 预约 / 支付系统。
5. 不做真实在线诊疗闭环。
6. 不做患者端自动诊断、自动开药、自动处方、自动转诊。
7. 不做医生端正式报告提交到真实医疗系统。
8. 不做真实处方、检查、检验、预约、支付、外部消息发送。
9. 不做生产级患者数据权限隔离；P1 只做 role-specific DTO 与 demo actor 边界。
10. 不让前端直接调用 Agent / Provider / Tool / Model。
11. 不让 Patient API 返回 DDx Board、Trace、Audit、Evaluation、Candidate Governance。
12. 不让 Clinician API 返回 raw prompt、secret、api key、private key、raw external response、internal chain-of-thought、full rationale、unredacted patient dialogue。
13. 不让 Governance API 放宽 Phase 10 Safe DTO 边界。
14. 不做 Candidate approve / reject / publish。
15. 不让前端拿完整 Runtime DTO 后再靠 role 隐藏字段。
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

Phase 6–11 P0 已冻结能力只能被 Phase 11-P1 按设计观察、复用和扩展，不能继续向已冻结范围堆新能力。

---

# 六、Phase 11-P1 可涉及范围

Phase 11-P1 可以涉及：

```text
PatientSessionSummaryDto
PatientRuntimeViewDto
PatientSafeSummaryDto
PatientFactSummaryDto
PatientQuestionDto
SafetyNoticeDto
CareNavigationDto
ClinicianCaseSummaryDto
ClinicianCaseViewDto
PatientSummaryDto
CaseFrameViewDto
InquiryTurnViewDto
DdxCandidateViewDto
EvidenceItemViewDto
RiskSignalViewDto
ClinicianSuggestionDto
ClinicianReportDraftViewDto
RuntimeBoundarySummaryDto
RoleSpecificViewSafetyPolicy
RoleSpecificViewSanitizer
ViewProjectionAuditService
ViewProjectionException
PatientViewProjectionService
PatientRuntimeViewMapper
PatientSessionQueryService
PatientViewPolicy
PatientViewController
ClinicianCaseProjectionService
ClinicianCaseViewMapper
ClinicianCaseQueryService
ClinicianReportDraftQueryService
ClinicianViewPolicy
ClinicianViewController
DemoRuntimeSeedProvider / SeedRuntimeAdapter
patientClient
clinicianClient
usePatientRuntimeView
useClinicianCaseView
```

Phase 11-P1 可以输出或创建：

```text
Patient role-specific DTO
Clinician role-specific DTO
Patient read-only API
Clinician read-only API
Role-specific projection services
Projection sanitizer / safety policy
Projection read audit events
runtime-demo-001 backend seed adapter
API-first frontend client / hooks
API fallback demo mode
Projection service tests
Controller tests
Frontend API-first tests
Manual verification record
Freeze record
```

Phase 11-P1 不能输出或触发：

```text
Final diagnosis
Prescription
Medication dosage
Treatment plan execution
Referral order
Appointment booking
Payment
External message sending
RuntimeState mutation beyond controlled read/projection
Provider run from frontend
Tool invocation from frontend
Agent invocation from frontend
Candidate approval / rejection / publishing
Model / Prompt / Dataset / Tool / Skill publication
Raw Patient Dialogue
Raw Prompt
Secret / API Key / Private Key
Raw External Response
Internal Chain-of-thought / Full Rationale
```

---

# 七、三端 API 边界

## 7.1 Patient API

允许：

```text
GET /api/v1/patient/sessions
GET /api/v1/patient/sessions/{sessionId}
GET /api/v1/patient/sessions/{sessionId}/summary
PatientSessionSummaryDto
PatientRuntimeViewDto
PatientSafeSummaryDto
PatientFactSummaryDto
PatientQuestionDto
SafetyNoticeDto
CareNavigationDto
safe_summary
collected_facts
next_questions
safety_notices
care_navigation
allowed_actions
disclaimer
projection_status
missing_sections
```

禁止：

```text
ddx_candidates
confidence_score
risk_score_internal
raw_evidence
raw_tool_result
trace_nodes
audit_events
evaluation_result
candidate_governance
model_prompt
internal_reasoning
provider_metadata
tool_metadata
raw_prompt
raw_external_response
secret
api_key
private_key
```

## 7.2 Clinician API

允许：

```text
GET /api/v1/clinician/cases
GET /api/v1/clinician/cases/{caseId}
GET /api/v1/clinician/cases/{caseId}/report-draft
ClinicianCaseSummaryDto
ClinicianCaseViewDto
CaseFrameViewDto
InquiryTurnViewDto
DdxCandidateViewDto as candidate direction
EvidenceItemViewDto as summary/source/relevance
RiskSignalViewDto
ClinicianSuggestionDto
ClinicianReportDraftViewDto
RuntimeBoundarySummaryDto
projection_status
missing_sections
```

禁止：

```text
raw_prompt
prompt_text
secret
api_key
private_key
raw_external_response
raw_tool_result
internal_chain_of_thought
full_rationale
unredacted_patient_dialogue
provider_secret_metadata
tool_raw_response
real report submit
prescription / referral / appointment / payment / external message
```

## 7.3 Governance Console

允许：

```text
继续沿用 Phase 10-P0 Safe DTO
/api/v1/console/overview
/api/v1/console/runtimes
/api/v1/console/runtimes/{runtimeId}/timeline
/api/v1/console/governance/domains
/api/v1/console/candidates
/api/v1/console/audits
```

禁止：

```text
因为 P11-P1 放宽 Safe DTO
展示未脱敏患者原文
展示 raw prompt
展示 secret
展示 raw external response
展示 full rationale
approve / reject / publish / run actions
```

---

# 八、Phase 11-P1 实现顺序

必须按以下顺序推进：

1. P11P1-A：建立 role-specific view DTO。
2. P11P1-B：建立 view safety policy / sanitizer。
3. P11P1-C：实现 PatientViewProjectionService。
4. P11P1-D：实现 ClinicianCaseProjectionService。
5. P11P1-E：实现 PatientViewController。
6. P11P1-F：实现 ClinicianViewController。
7. P11P1-G：建立 runtime-demo-001 seed runtime adapter。
8. P11P1-H：前端 patientClient / clinicianClient API-first 改造。
9. P11P1-I：Java / Frontend 自动测试。
10. P11P1-J：人工验证与冻结记录。

不得跳过 DTO / policy / sanitizer，直接在 Controller 或前端页面拼装医疗视图。

---

# 九、回归测试要求

Phase 11-P1 修改后端时，至少新增或回归：

```text
PatientViewProjectionServiceTest
PatientViewPolicyTest
PatientViewSanitizerTest
PatientViewControllerTest
ClinicianCaseProjectionServiceTest
ClinicianViewPolicyTest
ClinicianViewSanitizerTest
ClinicianViewControllerTest
RoleSpecificViewSafetyPolicyTest
```

Phase 11-P1 修改前端时，至少回归：

```text
npm run typecheck
npm run build
npm run test
```

前端新增或回归：

```text
patientClient.test.ts
clinicianClient.test.ts
PatientPortalApiFallback.test.tsx
ClinicianWorkspaceApiFallback.test.tsx
PatientApiProjectionRender.test.tsx
ClinicianApiProjectionRender.test.tsx
PatientSafeSummaryPage sensitive-field test
CaseWorkspacePage sensitive-field test
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

并保持 Phase 1–11 P0 既有 Runtime / Agent / Evidence / Graph / Provider / ModelGov / ToolGov / Evaluation / Candidate / Persistence / Audit / Frontend RBAC 测试不回归。

---

# 十、冻结记录要求

Phase 11-P1 完成后必须新增：

```text
docs/3-phase实现/Phase11_P1人工测试结果.md
docs/3-phase实现/Phase11_P1冻结记录.md
```

冻结记录必须说明：

1. PatientViewProjectionService 如何生成患者视图。
2. ClinicianCaseProjectionService 如何生成医生视图。
3. Patient DTO 禁止了哪些字段。
4. Clinician DTO 禁止了哪些字段。
5. Patient / Clinician API scope 如何分离。
6. 前端如何从 mock-first 切到 API-first。
7. runtime-demo-001 seed runtime 如何工作。
8. Projection read audit 如何记录。
9. Java / frontend 测试结果。
10. P1 仍不具备哪些生产级能力和后续 Phase 11-P2 入口。

---

# 十一、最终结论

当前 AI 实现约束是：

```text
Phase 10-P0 Governance Console / Runtime Console MVP 已冻结；
Phase 11-P0 Role-based Frontend Suite MVP 已冻结；
Phase 11-P1 Role-specific View API / Frontend BFF 已完成设计，当前可进入受限实现；
允许实现 Patient / Clinician 只读 role-specific view API；
允许将 Patient / Clinician 前端从 mock-first 改为 API-first；
必须由后端 projection service 保证数据边界，前端 RoleGuard 只保护路由；
不可以接真实患者数据、真实医疗系统或生产级认证；
不可以让 Patient API 返回 DDx / Trace / Audit / Evaluation / Candidate Governance；
不可以让 Clinician API 返回 raw prompt、secret、raw external response 或 full rationale；
不可以在 Phase 11-P1 中加入 approve / reject / publish / run 写操作；
不可以让任何前端页面绕过 Runtime、SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 或 Candidate Governance。
```

# 十二、2026-07-14 状态覆盖

Phase 11-P1 的 RuntimeStore 主路径、PARTIAL、FALLBACK、Patient Care Navigation、Clinician Inquiry Timeline / Evidence Panel / AI Suggestions 已完成代码收口。

当前冻结状态：**FROZEN**。

Phase 11-P1 的 RuntimeStore 主路径、PARTIAL、FALLBACK、安全边界、自动化测试和浏览器人工验证均已完成。冻结后只允许修复明确回归，不再扩展 Phase 11 范围。

下一阶段入口为 Phase 12-P0 Clinical Evidence Engine。进入代码实现前，必须先建立与 v3.0 总设计一致的实现规格、API/测试设计和开发任务清单。