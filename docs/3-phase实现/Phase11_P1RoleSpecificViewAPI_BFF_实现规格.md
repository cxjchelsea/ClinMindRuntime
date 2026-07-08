# Phase 11-P1 Role-specific View API / Frontend BFF 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 前置阶段：Phase 11-P0 Role-based Frontend Suite MVP 设计/实现收口中  
> 当前 Phase：Phase 11-P1 设计阶段  
> 当前目标：在 Phase 11-P0 的三角色前端原型基础上，新增后端 Role-specific View API / Frontend BFF，把 Patient Portal 与 Clinician Workspace 从 mock projection 推进到可读取 Runtime 真实状态的受控视图层。

---

# 一、Phase 定位

Phase 11-P1 不是生产级在线诊疗系统，也不是新增医疗推理能力。

Phase 11-P1 的目标是：

```text
将 Phase 11-P0 中写死的 runtime-demo-001 mock projection
替换为由后端 Runtime / CaseFrame / PatientOutput / ClinicianReport / DecisionBoundary 生成的
role-specific view projection。
```

核心命题：

```text
前端不再自己拼医疗视图；
前端不再直接使用 mock 作为主要数据源；
前端只消费后端已经裁剪好的 PatientRuntimeView / ClinicianCaseView / Governance Safe DTO。
```

目标链路：

```text
RuntimeState / CaseFrame / DDxBoard / EvidenceGraph / SafetyGate / DecisionBoundary / PatientOutput / ClinicianReport
↓
PatientViewProjectionService / ClinicianCaseProjectionService / GovernanceViewProjectionService
↓
Role-specific Safe DTO
↓
/api/v1/patient/* /api/v1/clinician/* /api/v1/console/*
↓
Patient Portal / Clinician Workspace / Governance Console
```

Phase 11-P1 解决的问题：

```text
Phase 11-P0：页面结构有了，但患者端和医生端主要依赖 mock 数据。
Phase 11-P1：后端提供受控视图 API，让患者端和医生端可以读取 Runtime 真实投影。
```

---

# 二、当前不做什么

Phase 11-P1 明确不做：

```text
1. 不做生产级登录、注册、真实认证。
2. 不接真实 HIS / EMR / LIS / PACS / 预约 / 支付系统。
3. 不做真实在线诊疗闭环。
4. 不做患者端自动诊断、自动开药、自动处方、自动转诊。
5. 不做医生端正式报告提交到真实医疗系统。
6. 不做真实处方、检查、检验、预约、支付、消息发送。
7. 不做生产级患者数据权限隔离；P1 只做 role-specific DTO 与 demo actor 边界。
8. 不让前端直接调用 Agent / Provider / Tool / Model。
9. 不让 Patient API 返回 DDx Board、Trace、Audit、Evaluation、Candidate Governance。
10. 不让 Clinician API 返回 raw prompt、secret、raw external response、full rationale、unredacted patient dialogue。
11. 不让 Governance API 放宽 Phase 10 Safe DTO 边界。
12. 不做 Candidate approve / reject / publish。
13. 不改写 Phase 1–11 P0 的冻结事实。
```

P1 可以做：

```text
Patient session read API
Patient runtime safe summary API
Patient view projection service
Clinician case list API
Clinician case workspace API
Clinician report draft read API
Clinician view projection service
Role-specific DTO mapper
View projection safety policy
Mock fallback / seed runtime adapter
Frontend client 从 mock 切换到 API-first
API tests / projection tests / frontend integration tests
```

---

# 三、核心边界

Phase 11-P1 的关键边界是：

```text
RoleGuard 只保护路由；
真正的数据边界必须由后端 projection service 保证。
```

也就是说，前端不能拿完整 Runtime DTO 再按角色隐藏字段。

禁止链路：

```text
RuntimeState Full DTO
↓
Frontend
↓
if role === PATIENT hide ddx/audit/trace
```

允许链路：

```text
RuntimeState / CaseFrame / DecisionBoundary
↓
PatientViewProjectionService
↓
PatientRuntimeViewDto
↓
Patient Portal
```

医生端同理：

```text
RuntimeState / CaseFrame / DDxBoard / EvidenceGraph / ClinicianReport
↓
ClinicianCaseProjectionService
↓
ClinicianCaseViewDto
↓
Clinician Workspace
```

---

# 四、后端模块设计

建议新增包路径：

```text
src/main/java/com/clinmind/runtime/view/
src/main/java/com/clinmind/runtime/view/patient/
src/main/java/com/clinmind/runtime/view/clinician/
src/main/java/com/clinmind/runtime/view/common/
src/main/java/com/clinmind/runtime/api/patient/
src/main/java/com/clinmind/runtime/api/clinician/
```

## 4.1 Common View Safety

核心类：

```text
RoleSpecificViewSafetyPolicy
RoleSpecificViewSanitizer
ViewProjectionAuditService
ViewProjectionException
```

职责：

```text
1. 定义 role-specific DTO 禁止字段。
2. 统一过滤 sensitive metadata。
3. 阻止 raw prompt / raw external response / full rationale 等字段进入 view DTO。
4. 记录 projection read audit。
5. 对缺失 Runtime / CaseFrame / DecisionBoundary 做受控降级。
```

## 4.2 Patient View Projection

核心类：

```text
PatientViewProjectionService
PatientRuntimeViewMapper
PatientSessionQueryService
PatientViewPolicy
PatientViewController
```

输入来源：

```text
RuntimeState
CaseFrame
PatientOutput
DecisionBoundary
SafetyGateResult
QuestionPolicy / InquiryPlan
```

输出：

```text
PatientSessionSummaryDto
PatientRuntimeViewDto
PatientSafeSummaryDto
PatientQuestionDto
SafetyNoticeDto
CareNavigationDto
```

核心规则：

```text
1. PatientRuntimeViewDto 只能包含 DecisionBoundary 允许患者可见的内容。
2. 不返回 DDx Board。
3. 不返回 Evidence raw。
4. 不返回 Trace / Audit / Evaluation / Candidate。
5. 不返回模型名、prompt、provider、tool、registry 信息。
6. 如果 SafetyGate 为 high risk，患者视图优先展示安全提醒和就医建议，不继续扩展解释。
7. 如果 PatientOutput 不存在，返回 safe fallback view，而不是抛出原始异常。
```

## 4.3 Clinician View Projection

核心类：

```text
ClinicianCaseProjectionService
ClinicianCaseViewMapper
ClinicianCaseQueryService
ClinicianReportDraftQueryService
ClinicianViewPolicy
ClinicianViewController
```

输入来源：

```text
RuntimeState
CaseFrame
InquiryTimeline
DDxBoard
EvidenceGraph
RiskSignal
ClinicianReport
DecisionBoundary
SafetyGateResult
```

输出：

```text
ClinicianCaseSummaryDto
ClinicianCaseViewDto
CaseFrameViewDto
InquiryTurnViewDto
DdxCandidateViewDto
EvidenceItemViewDto
RiskSignalViewDto
ClinicianSuggestionViewDto
ClinicianReportDraftViewDto
RuntimeBoundarySummaryDto
```

核心规则：

```text
1. ClinicianCaseViewDto 可以展示候选 DDx，但必须标记为 AI 辅助方向，不是最终诊断。
2. EvidencePanel 只能展示摘要、来源、relevance 和安全引用，不返回 raw retrieval payload。
3. ReportDraft 只能作为医生待确认草稿，不写入真实医疗系统。
4. 不返回 raw prompt、secret、api key、raw external response、internal chain-of-thought、full rationale。
5. 不允许医生端 API 调用 Provider / Tool / Agent。
6. 如果 Runtime 不完整，返回 partial view，并标记 missing sections。
```

## 4.4 Governance View Projection

P1 不重做 Governance API，继续沿用 Phase 10-P0 的 Console Safe DTO：

```text
/api/v1/console/overview
/api/v1/console/runtimes
/api/v1/console/runtimes/{runtimeId}/timeline
/api/v1/console/governance/domains
/api/v1/console/candidates
/api/v1/console/audits
```

新增要求：

```text
1. Patient / Clinician API 不得复用 Governance Console DTO。
2. Governance Console 不得因为 P1 而展示未脱敏患者原文。
3. Governance API 可在 timeline 中引用 patient/clinician projection 是否生成成功，但不得暴露其敏感 payload。
```

---

# 五、API 设计

## 5.1 Patient API

```text
GET /api/v1/patient/sessions
GET /api/v1/patient/sessions/{sessionId}
GET /api/v1/patient/sessions/{sessionId}/summary
```

P1 暂不实现真实 submit 闭环；如果保留 intake 页面，可以继续使用 demo submit 或 mock adapter。

```text
POST /api/v1/patient/intake   P1 可设计，不必须实现真实 Runtime 创建
```

## 5.2 Clinician API

```text
GET /api/v1/clinician/cases
GET /api/v1/clinician/cases/{caseId}
GET /api/v1/clinician/cases/{caseId}/report-draft
```

P1 暂不做真实保存：

```text
PUT /api/v1/clinician/cases/{caseId}/report-draft   后置到 Phase 11-P2
```

## 5.3 API 统一响应

沿用现有 `CommonResult` / `ApiResponse` 风格：

```text
success: boolean
data: T
error: { code, message }
trace_id: string
```

错误必须受控：

```text
PATIENT_VIEW_NOT_FOUND
PATIENT_VIEW_FORBIDDEN
PATIENT_VIEW_UNAVAILABLE
CLINICIAN_CASE_NOT_FOUND
CLINICIAN_CASE_FORBIDDEN
CLINICIAN_VIEW_UNAVAILABLE
VIEW_PROJECTION_POLICY_REJECTED
VIEW_PROJECTION_SANITIZATION_FAILED
```

---

# 六、DTO 设计原则

## 6.1 PatientRuntimeViewDto

```text
session_id
runtime_id
status
started_at
updated_at
safe_summary
collected_facts[]
next_questions[]
safety_notices[]
care_navigation[]
allowed_actions[]
disclaimer
projection_status
missing_sections[]
```

禁止字段：

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
```

## 6.2 ClinicianCaseViewDto

```text
case_id
runtime_id
status
patient_summary
case_frame
inquiry_timeline[]
ddx_board[]
evidence_panel[]
risk_panel[]
ai_suggestions[]
report_draft
runtime_boundary_summary
projection_status
missing_sections[]
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
provider_secret_metadata
tool_raw_response
```

## 6.3 Projection Status

所有 view DTO 必须带 projection 状态：

```text
COMPLETE
PARTIAL
FALLBACK
UNAVAILABLE
```

并可携带：

```text
missing_sections: string[]
safety_notes: string[]
```

这样前端可以清楚知道：

```text
这是完整视图，还是受限/降级视图。
```

---

# 七、数据来源与降级策略

P1 需要支持三种数据来源：

```text
1. Real Runtime Store：优先读取真实 Runtime / CaseFrame / PatientOutput / ClinicianReport。
2. Seed Runtime Adapter：用于 runtime-demo-001 演示，模拟真实 Runtime store 结构。
3. Safe Fallback View：当 Runtime 不完整时返回安全降级视图。
```

禁止：

```text
直接在 Controller 中硬编码完整患者/医生视图。
直接从前端 mock 生成医疗结论。
直接把 RuntimeState 原样返回给前端。
```

降级示例：

```text
PatientOutput 缺失 → 返回 safe_summary = “当前信息仍在整理中”，allowed_actions = continue_inquiry / contact_clinician。
ClinicianReport 缺失 → report_draft 标记为 unavailable，但 CaseFrame / EvidencePanel 仍可展示。
DDxBoard 缺失 → ddx_board = []，missing_sections 包含 ddx_board。
SafetyGate high risk → Patient view 优先返回 urgent safety notice。
```

---

# 八、前端改造目标

P1 前端目标：

```text
把 Patient Portal / Clinician Workspace 从 runtimeDemoData.ts 切到 API-first。
```

推荐模式：

```text
Patient API success → 展示后端 PatientRuntimeView
Patient API failure → 展示受控错误或 demo fallback
Clinician API success → 展示后端 ClinicianCaseView
Clinician API failure → 展示受控错误或 demo fallback
Governance Console → 继续使用 Phase10 Console API
```

前端文件建议：

```text
console-web/src/portals/patient/api/patientClient.ts
console-web/src/portals/patient/hooks/usePatientRuntimeView.ts
console-web/src/portals/clinician/api/clinicianClient.ts
console-web/src/portals/clinician/hooks/useClinicianCaseView.ts
console-web/src/shared/api/httpClient.ts
```

P1 之后，`runtimeDemoData.ts` 只能作为 fallback/demo seed，不能作为主要页面数据来源。

---

# 九、安全与审计

Phase 11-P1 API 必须记录 read audit：

```text
PATIENT_VIEW_READ
PATIENT_SUMMARY_READ
CLINICIAN_CASE_VIEW_READ
CLINICIAN_REPORT_DRAFT_READ
VIEW_PROJECTION_POLICY_REJECTED
VIEW_PROJECTION_SANITIZED
```

审计 metadata 只能包含：

```text
actor_id
role
request_id
runtime_id
session_id / case_id
projection_type
projection_status
result_status
created_at
```

不得包含：

```text
raw patient dialogue
raw prompt
raw external response
full rationale
clinical free text beyond safe summary
```

---

# 十、冻结标准

Phase 11-P1 可冻结条件：

```text
1. PatientViewProjectionService 实现。
2. ClinicianCaseProjectionService 实现。
3. Patient API 可以返回 PatientSessionSummary / PatientRuntimeView / PatientSafeSummary。
4. Clinician API 可以返回 ClinicianCaseSummary / ClinicianCaseView / ReportDraftView。
5. Patient DTO 不包含 DDx / Trace / Audit / Evaluation / Candidate。
6. Clinician DTO 不包含 raw prompt / secret / raw external response / full rationale。
7. 前端 Patient Portal 优先读取 Patient API。
8. 前端 Clinician Workspace 优先读取 Clinician API。
9. runtime-demo-001 可通过 API 而不是纯前端 mock 展示三角色投影。
10. Java projection tests / controller tests 通过。
11. console-web typecheck / build / tests 通过或如实记录 caveat。
12. 新增人工测试结果与冻结记录。
```

---

# 十一、最终结论

Phase 11-P1 的目标是把 Phase 11-P0 的多角色前端原型推进到“可读取真实 Runtime 投影”的状态：

```text
Phase 11-P0：前端三端结构 + RBAC + mock projection。
Phase 11-P1：后端 role-specific view API + 前端 API-first projection。
Phase 11-P2：患者问诊推进 / 医生报告草稿保存等可操作 workflow。
Phase 11-P3：生产级认证、权限、真实医疗系统接入。
```

Phase 11-P1 完成后，前端仍不是生产医疗系统，但它将不再只是静态 mock 展示，而是可以通过后端受控 API 读取 Runtime 的患者端和医生端投影。
