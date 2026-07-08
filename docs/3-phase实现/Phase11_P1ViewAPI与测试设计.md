# Phase 11-P1 View API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase11_P1RoleSpecificViewAPI_BFF_实现规格.md`  
> 前置阶段：Phase 11-P0 Role-based Frontend Suite MVP 设计/实现收口中  
> 当前 Phase：Phase 11-P1 设计阶段  
> 当前目标：细化 Patient / Clinician Role-specific View API、DTO、Controller、Projection Service、前端 client、测试与人工验证策略。

---

# 一、设计目标

Phase 11-P1 的 API 与测试设计要证明：

```text
1. Patient Portal 不再主要依赖前端 runtimeDemoData.ts。
2. Clinician Workspace 不再主要依赖前端 runtimeDemoData.ts。
3. 后端提供 PatientRuntimeViewDto 与 ClinicianCaseViewDto。
4. Patient / Clinician / Governance 三类 API scope 分离。
5. Patient API 不返回医生与治理内部数据。
6. Clinician API 不返回 raw prompt / secret / raw external response / full rationale。
7. Governance API 继续沿用 Phase 10 Safe DTO。
8. runtime-demo-001 可以通过后端 seed/projection API 形成三角色演示。
```

---

# 二、API Scope

## 2.1 Patient API

```text
GET /api/v1/patient/sessions
GET /api/v1/patient/sessions/{sessionId}
GET /api/v1/patient/sessions/{sessionId}/summary
```

可选设计，不作为 P1 必须实现：

```text
POST /api/v1/patient/intake
```

说明：

```text
P1 以 read projection 为主。
如果实现 intake，也只能创建 demo Runtime 或提交到受控 Runtime API，不能直接调用模型或生成诊断。
```

## 2.2 Clinician API

```text
GET /api/v1/clinician/cases
GET /api/v1/clinician/cases/{caseId}
GET /api/v1/clinician/cases/{caseId}/report-draft
```

后置到 P2：

```text
PUT /api/v1/clinician/cases/{caseId}/report-draft
POST /api/v1/clinician/cases/{caseId}/confirm
POST /api/v1/clinician/cases/{caseId}/send-message
```

## 2.3 Governance API

继续沿用：

```text
GET /api/v1/console/overview
GET /api/v1/console/runtimes
GET /api/v1/console/runtimes/{runtimeId}/timeline
GET /api/v1/console/governance/domains
GET /api/v1/console/candidates
GET /api/v1/console/audits
```

P1 不新增 Governance 写接口。

---

# 三、Controller 设计

## 3.1 PatientViewController

建议路径：

```text
src/main/java/com/clinmind/runtime/api/patient/PatientViewController.java
```

方法：

```text
GET /api/v1/patient/sessions
→ listMySessions(actorContext)

GET /api/v1/patient/sessions/{sessionId}
→ getPatientRuntimeView(sessionId, actorContext)

GET /api/v1/patient/sessions/{sessionId}/summary
→ getPatientSafeSummary(sessionId, actorContext)
```

Controller 规则：

```text
1. 不拼业务字段。
2. 不访问 raw provider/tool/model payload。
3. 只调用 PatientViewProjectionService。
4. 所有错误转成受控 ApiError。
5. 记录 read audit。
```

## 3.2 ClinicianViewController

建议路径：

```text
src/main/java/com/clinmind/runtime/api/clinician/ClinicianViewController.java
```

方法：

```text
GET /api/v1/clinician/cases
→ listCases(actorContext)

GET /api/v1/clinician/cases/{caseId}
→ getClinicianCaseView(caseId, actorContext)

GET /api/v1/clinician/cases/{caseId}/report-draft
→ getReportDraft(caseId, actorContext)
```

Controller 规则：

```text
1. 不执行 Runtime。
2. 不执行 Provider / Tool / Agent。
3. 不生成新诊断。
4. 不写真实报告。
5. 只读取 projection。
6. 所有 AI 内容都保持辅助提示定位。
```

---

# 四、Projection Service 设计

## 4.1 PatientViewProjectionService

方法：

```java
List<PatientSessionSummaryDto> listSessions(ActorContext actorContext);
PatientRuntimeViewDto getRuntimeView(String sessionId, ActorContext actorContext);
PatientSafeSummaryDto getSafeSummary(String sessionId, ActorContext actorContext);
```

内部流程：

```text
validateActorCanReadPatientSession
↓
load RuntimeState / CaseFrame / DecisionBoundary / PatientOutput
↓
apply PatientViewPolicy
↓
map to PatientRuntimeViewDto
↓
sanitize DTO
↓
audit projection read
↓
return
```

Policy 规则：

```text
PATIENT_VIEW_ALLOW_SAFE_SUMMARY
PATIENT_VIEW_ALLOW_COLLECTED_FACTS
PATIENT_VIEW_ALLOW_NEXT_QUESTIONS
PATIENT_VIEW_ALLOW_SAFETY_NOTICES
PATIENT_VIEW_FORBID_DDX
PATIENT_VIEW_FORBID_TRACE
PATIENT_VIEW_FORBID_AUDIT
PATIENT_VIEW_FORBID_EVALUATION
PATIENT_VIEW_FORBID_CANDIDATE
```

## 4.2 ClinicianCaseProjectionService

方法：

```java
List<ClinicianCaseSummaryDto> listCases(ActorContext actorContext);
ClinicianCaseViewDto getCaseView(String caseId, ActorContext actorContext);
ClinicianReportDraftViewDto getReportDraft(String caseId, ActorContext actorContext);
```

内部流程：

```text
validateActorCanReadClinicianCase
↓
load RuntimeState / CaseFrame / DDxBoard / EvidenceGraph / ClinicianReport
↓
apply ClinicianViewPolicy
↓
map to ClinicianCaseViewDto
↓
sanitize DTO
↓
audit projection read
↓
return
```

Policy 规则：

```text
CLINICIAN_VIEW_ALLOW_CASE_FRAME
CLINICIAN_VIEW_ALLOW_DDX_CANDIDATE_DIRECTION
CLINICIAN_VIEW_ALLOW_EVIDENCE_SUMMARY
CLINICIAN_VIEW_ALLOW_RISK_PANEL
CLINICIAN_VIEW_ALLOW_REPORT_DRAFT
CLINICIAN_VIEW_FORBID_RAW_PROMPT
CLINICIAN_VIEW_FORBID_SECRET
CLINICIAN_VIEW_FORBID_RAW_EXTERNAL_RESPONSE
CLINICIAN_VIEW_FORBID_FULL_RATIONALE
```

---

# 五、DTO 字段设计

## 5.1 PatientSessionSummaryDto

```text
session_id: string
runtime_id: string
status: string
chief_complaint_summary: string
risk_hint: LOW | MEDIUM | HIGH | UNKNOWN
safe_next_step: string
updated_at: string
projection_status: COMPLETE | PARTIAL | FALLBACK | UNAVAILABLE
```

## 5.2 PatientRuntimeViewDto

```text
session_id: string
runtime_id: string
status: string
safe_summary: string
collected_facts: PatientFactSummaryDto[]
next_questions: PatientQuestionDto[]
safety_notices: SafetyNoticeDto[]
care_navigation: CareNavigationDto[]
allowed_actions: string[]
disclaimer: string
projection_status: string
missing_sections: string[]
```

## 5.3 PatientSafeSummaryDto

```text
session_id: string
runtime_id: string
safe_summary: string
safety_notices: SafetyNoticeDto[]
care_navigation: CareNavigationDto[]
disclaimer: string
projection_status: string
```

## 5.4 ClinicianCaseSummaryDto

```text
case_id: string
runtime_id: string
status: string
risk_level: LOW | WATCH | URGENT | UNKNOWN
chief_complaint_summary: string
updated_at: string
assigned_clinician: string | null
projection_status: string
```

## 5.5 ClinicianCaseViewDto

```text
case_id: string
runtime_id: string
status: string
patient_summary: PatientSummaryDto
case_frame: CaseFrameViewDto
inquiry_timeline: InquiryTurnViewDto[]
ddx_board: DdxCandidateViewDto[]
evidence_panel: EvidenceItemViewDto[]
risk_panel: RiskSignalViewDto[]
ai_suggestions: ClinicianSuggestionDto[]
report_draft: ClinicianReportDraftViewDto
runtime_boundary_summary: RuntimeBoundarySummaryDto
projection_status: string
missing_sections: string[]
```

## 5.6 ClinicianReportDraftViewDto

```text
case_id: string
runtime_id: string
impression: string
suggested_questions: string[]
clinician_note: string
editable: boolean
submit_enabled: false
projection_status: string
```

P1 中 `submit_enabled` 必须为 false，真实保存后置到 P2。

---

# 六、Sanitization 测试清单

## 6.1 Patient DTO 禁止字段

任何 Patient DTO 中不得出现：

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

## 6.2 Clinician DTO 禁止字段

任何 Clinician DTO 中不得出现：

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
```

## 6.3 Error DTO 禁止字段

错误返回也不得包含：

```text
患者原文
prompt 原文
provider 原始异常堆栈
tool 原始返回
secret
完整内部推理链
```

---

# 七、前端 Client 设计

## 7.1 patientClient

```ts
export function listPatientSessions(): Promise<PatientSessionSummary[]>;
export function getPatientRuntimeView(sessionId: string): Promise<PatientRuntimeView>;
export function getPatientSafeSummary(sessionId: string): Promise<PatientSafeSummary>;
```

## 7.2 clinicianClient

```ts
export function listClinicianCases(): Promise<ClinicianCaseSummary[]>;
export function getClinicianCase(caseId: string): Promise<ClinicianCaseView>;
export function getClinicianReportDraft(caseId: string): Promise<ClinicianReportDraftView>;
```

## 7.3 fallback 策略

前端可以保留 demo fallback，但必须写清楚：

```text
API-first：先请求后端 Role-specific View API。
Fallback：后端未启动或 P1 API 不存在时，使用 demo seed data 继续展示。
禁止：把 demo seed data 当作真实业务数据。
```

---

# 八、自动测试设计

## 8.1 Java 单元测试

建议新增：

```text
PatientViewProjectionServiceTest
PatientViewPolicyTest
PatientViewSanitizerTest
ClinicianCaseProjectionServiceTest
ClinicianViewPolicyTest
ClinicianViewSanitizerTest
RoleSpecificViewSafetyPolicyTest
```

覆盖：

```text
Patient view 不含 DDx / Audit / Trace / Evaluation。
Patient high-risk 时优先返回 SafetyNotice。
PatientOutput 缺失时返回 fallback view。
Clinician view 不含 raw prompt / secret / raw external response / full rationale。
Clinician EvidencePanel 只返回 summary/source/relevance。
缺失 DDx / Evidence / ReportDraft 时返回 partial view。
```

## 8.2 Java Controller 测试

建议新增：

```text
PatientViewControllerTest
ClinicianViewControllerTest
```

覆盖：

```text
GET /api/v1/patient/sessions 返回 PatientSessionSummaryDto[]。
GET /api/v1/patient/sessions/{sessionId}/summary 返回 Safe Summary。
GET /api/v1/clinician/cases 返回 ClinicianCaseSummaryDto[]。
GET /api/v1/clinician/cases/{caseId} 返回 ClinicianCaseViewDto。
不存在的 session/case 返回受控错误。
Patient API 不暴露医生/治理字段。
Clinician API 不暴露 raw provider/tool/model 字段。
```

## 8.3 前端测试

建议新增：

```text
patientClient.test.ts
clinicianClient.test.ts
PatientPortalApiFallback.test.tsx
ClinicianWorkspaceApiFallback.test.tsx
PatientApiProjectionRender.test.tsx
ClinicianApiProjectionRender.test.tsx
```

覆盖：

```text
Patient pages 优先使用 API 返回数据。
API failure 时展示安全错误或 demo fallback 标记。
Clinician pages 优先使用 API 返回数据。
Clinician report draft submit disabled。
Patient 页面仍不展示 DDx / Trace / Audit。
Clinician 页面仍不展示 raw prompt / secret / full rationale。
```

---

# 九、人工测试场景

## 场景 1：Patient API 读取 session list

```text
Given 后端启动并存在 runtime-demo-001 seed runtime
When PATIENT 访问 /patient
Then 页面通过 /api/v1/patient/sessions 获取列表
And 不显示治理字段
```

## 场景 2：Patient safe summary

```text
Given sessionId = runtime-demo-001
When PATIENT 访问 /patient/sessions/runtime-demo-001/summary
Then 页面展示后端 PatientSafeSummaryDto
And 不展示 DDx / Audit / Trace / Evaluation / Candidate
```

## 场景 3：Clinician case workspace

```text
Given caseId = runtime-demo-001
When CLINICIAN 访问 /clinician/cases/runtime-demo-001
Then 页面展示后端 ClinicianCaseViewDto
And DDx Board 明确为候选方向
And Evidence Panel 只展示摘要和来源
```

## 场景 4：后端不可用 fallback

```text
Given 后端不可用
When 打开 Patient / Clinician 页面
Then 页面使用 demo fallback 或受控错误态
And 明确标记 demo/fallback
```

## 场景 5：Governance 继续独立

```text
Given GOVERNANCE_REVIEWER 访问 /governance/runtimes/runtime-demo-001
Then 仍使用 /api/v1/console/runtimes/{runtimeId}/timeline
And 不改用 Patient / Clinician DTO
```

---

# 十、验收标准

Phase 11-P1 API 与测试验收标准：

```text
[ ] Patient API 三个 GET 接口可用。
[ ] Clinician API 三个 GET 接口可用。
[ ] Projection Service 不直接返回 RuntimeState。
[ ] Patient DTO 禁止字段测试通过。
[ ] Clinician DTO 禁止字段测试通过。
[ ] Controller 受控错误测试通过。
[ ] read audit 记录 projection read。
[ ] 前端 Patient Portal 优先读取 Patient API。
[ ] 前端 Clinician Workspace 优先读取 Clinician API。
[ ] runtime-demo-001 可通过 API 展示 Patient / Clinician / Governance 三角色视图。
[ ] npm run typecheck 通过。
[ ] npm run build 通过。
[ ] Java targeted tests 通过。
[ ] 人工测试记录完成。
```

---

# 十一、最终边界

Phase 11-P1 完成后可以说：

```text
前端已经不再只是静态 mock；
Patient / Clinician 页面可以通过后端 Role-specific View API 读取受控 Runtime 投影；
三端数据边界由后端 projection service 保证。
```

仍然不能说：

```text
已经是生产级患者端；
已经是真实医生工作站；
已经支持真实在线诊疗；
已经支持医生确认、报告提交、处方、转诊、预约、支付；
已经具备生产级认证与真实 RBAC。
```
