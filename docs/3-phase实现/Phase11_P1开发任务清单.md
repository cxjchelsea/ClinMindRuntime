# Phase 11-P1 开发任务清单：Role-specific View API / Frontend BFF

> 上位实现规格：`docs/3-phase实现/Phase11_P1RoleSpecificViewAPI_BFF_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase11_P1ViewAPI与测试设计.md`  
> 前置阶段：Phase 11-P0 Role-based Frontend Suite MVP 设计/实现收口中  
> 当前目标：新增 Patient / Clinician Role-specific View API，使前端从纯 mock projection 推进到 API-first 的 Runtime 投影读取。

---

# 一、Phase 11-P1 总目标

Phase 11-P1 要完成的是后端视图投影层：

```text
RuntimeState / CaseFrame / PatientOutput / ClinicianReport / DecisionBoundary
→ PatientViewProjectionService / ClinicianCaseProjectionService
→ Role-specific Safe DTO
→ Patient API / Clinician API
→ Patient Portal / Clinician Workspace API-first 展示
```

最终要证明：

```text
Patient Portal 读取后端 PatientRuntimeView，而不是主要依赖前端 mock。
Clinician Workspace 读取后端 ClinicianCaseView，而不是主要依赖前端 mock。
后端 projection service 负责字段裁剪与安全边界。
前端 RoleGuard 只负责路由，不承担数据安全隔离。
Governance Console 继续沿用 Phase10 Safe DTO。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P11P1-A | 建立 role-specific view DTO | 已完成 |
| P11P1-B | 建立 view safety policy / sanitizer | 已完成 |
| P11P1-C | 实现 PatientViewProjectionService | 已完成 |
| P11P1-D | 实现 ClinicianCaseProjectionService | 已完成 |
| P11P1-E | 实现 PatientViewController | 已完成 |
| P11P1-F | 实现 ClinicianViewController | 已完成 |
| P11P1-G | 建立 runtime-demo-001 seed runtime adapter | 已完成 |
| P11P1-H | 前端 patientClient / clinicianClient API-first 改造 | 已完成 |
| P11P1-I | Java / Frontend 自动测试 | 已完成 |
| P11P1-J | 人工验证与冻结记录 | 已完成 |

---

# 三、P11P1-A：建立 role-specific view DTO

## 目标

定义 Patient / Clinician 后端输出 DTO，避免复用完整 Runtime DTO。

## 建议包路径

```text
src/main/java/com/clinmind/runtime/view/patient/dto/
src/main/java/com/clinmind/runtime/view/clinician/dto/
src/main/java/com/clinmind/runtime/view/common/dto/
```

## 任务

```text
[x] 新增 PatientSessionSummaryDto。
[x] 新增 PatientRuntimeViewDto。
[x] 新增 PatientSafeSummaryDto。
[x] 新增 PatientFactSummaryDto。
[x] 新增 PatientQuestionDto。
[x] 新增 SafetyNoticeDto。
[x] 新增 CareNavigationDto。
[x] 新增 ClinicianCaseSummaryDto。
[x] 新增 ClinicianCaseViewDto。
[x] 新增 PatientSummaryDto。
[x] 新增 CaseFrameViewDto。
[x] 新增 InquiryTurnViewDto。
[x] 新增 DdxCandidateViewDto。
[x] 新增 EvidenceItemViewDto。
[x] 新增 RiskSignalViewDto。
[x] 新增 ClinicianSuggestionDto。
[x] 新增 ClinicianReportDraftViewDto。
[x] 新增 RuntimeBoundarySummaryDto。
[x] 所有 DTO 增加 projection_status / missing_sections。
```

## 验收标准

```text
[x] Patient DTO 不含 ddx / trace / audit / evaluation / candidate 字段。
[x] Clinician DTO 不含 raw prompt / secret / raw external response / full rationale 字段。
[x] DTO 字段命名与前端类型一致或有明确 mapper。
```

---

# 四、P11P1-B：建立 view safety policy / sanitizer

## 目标

统一定义 role-specific view 的安全过滤规则。

## 建议文件

```text
RoleSpecificViewSafetyPolicy.java
RoleSpecificViewSanitizer.java
ViewProjectionAuditService.java
ViewProjectionException.java
```

## 任务

```text
[x] 定义 Patient DTO 禁止字段列表。
[x] 定义 Clinician DTO 禁止字段列表。
[x] 实现 sanitizeMap / sanitizeMetadata。
[x] 实现 validatePatientViewDto。
[x] 实现 validateClinicianViewDto。
[x] 实现 projection read audit 记录。
[x] 对 policy reject 输出受控错误码。
```

## 验收标准

```text
[x] sanitizer 单测覆盖所有敏感字段。
[x] 错误响应不泄露 raw payload。
[x] projection audit metadata 不包含患者原文、prompt、raw external response。
```

---

# 五、P11P1-C：实现 PatientViewProjectionService

## 目标

从 Runtime 相关对象生成患者端安全视图。

## 建议文件

```text
PatientViewProjectionService.java
PatientRuntimeViewMapper.java
PatientSessionQueryService.java
PatientViewPolicy.java
```

## 任务

```text
[x] listSessions(actorContext)：返回 PatientSessionSummaryDto[]。
[x] getRuntimeView(sessionId, actorContext)：返回 PatientRuntimeViewDto。
[x] getSafeSummary(sessionId, actorContext)：返回 PatientSafeSummaryDto。
[x] 从 RuntimeState / CaseFrame / PatientOutput / DecisionBoundary 读取数据。
[x] PatientOutput 等 Runtime 段缺失时返回 PARTIAL；Runtime 不存在时才使用显式 seed FALLBACK。
[x] SafetyGate high-risk 时优先返回 urgent safety notice。
[x] 不返回 DDx / Trace / Audit / Evaluation / Candidate。
[x] 记录 PATIENT_VIEW_READ / PATIENT_SUMMARY_READ audit。
```

## 验收标准

```text
[x] 正常 Runtime 返回 COMPLETE。
[x] 缺失 PatientOutput 返回 FALLBACK 或 PARTIAL。
[x] 不存在 session 返回 PATIENT_VIEW_NOT_FOUND。
[x] forbidden 返回 PATIENT_VIEW_FORBIDDEN。
[x] Patient DTO 禁止字段测试通过。
```

---

# 六、P11P1-D：实现 ClinicianCaseProjectionService

## 目标

从 Runtime 相关对象生成医生端病例工作台视图。

## 建议文件

```text
ClinicianCaseProjectionService.java
ClinicianCaseViewMapper.java
ClinicianCaseQueryService.java
ClinicianReportDraftQueryService.java
ClinicianViewPolicy.java
```

## 任务

```text
[x] listCases(actorContext)：返回 ClinicianCaseSummaryDto[]。
[x] getCaseView(caseId, actorContext)：返回 ClinicianCaseViewDto。
[x] getReportDraft(caseId, actorContext)：返回 ClinicianReportDraftViewDto。
[x] 从 RuntimeState / CaseFrame / DDxBoard / EvidenceGraph / ClinicianReport 读取数据。
[x] DDxBoard 只输出候选方向。
[x] EvidencePanel 只输出 summary / source / relevance。
[x] ReportDraft submit_enabled 固定 false。
[x] 不返回 raw prompt / secret / raw external response / full rationale。
[x] 记录 CLINICIAN_CASE_VIEW_READ / CLINICIAN_REPORT_DRAFT_READ audit。
```

## 验收标准

```text
[x] 正常 Runtime 返回 COMPLETE。
[x] 缺失 DDx / Evidence / ReportDraft 返回 PARTIAL。
[x] 不存在 case 返回 CLINICIAN_CASE_NOT_FOUND。
[x] forbidden 返回 CLINICIAN_CASE_FORBIDDEN。
[x] Clinician DTO 禁止字段测试通过。
```

---

# 七、P11P1-E：实现 PatientViewController

## 目标

提供患者端只读 view API。

## 建议路径

```text
src/main/java/com/clinmind/runtime/api/patient/PatientViewController.java
```

## API

```text
GET /api/v1/patient/sessions
GET /api/v1/patient/sessions/{sessionId}
GET /api/v1/patient/sessions/{sessionId}/summary
```

## 任务

```text
[x] 新增 controller。
[x] 复用 ActorContext。
[x] 调用 PatientViewProjectionService。
[x] 使用统一 ApiResponse / CommonResult。
[x] 错误码受控。
[x] controller 测试覆盖。
```

## 验收标准

```text
[x] 三个 GET 接口可返回 JSON。
[x] 不存在 session 返回受控错误。
[x] response body 不含敏感字段。
```

---

# 八、P11P1-F：实现 ClinicianViewController

## 目标

提供医生端只读 case view API。

## 建议路径

```text
src/main/java/com/clinmind/runtime/api/clinician/ClinicianViewController.java
```

## API

```text
GET /api/v1/clinician/cases
GET /api/v1/clinician/cases/{caseId}
GET /api/v1/clinician/cases/{caseId}/report-draft
```

## 任务

```text
[x] 新增 controller。
[x] 复用 ActorContext。
[x] 调用 ClinicianCaseProjectionService。
[x] 使用统一 ApiResponse / CommonResult。
[x] 错误码受控。
[x] controller 测试覆盖。
```

## 验收标准

```text
[x] 三个 GET 接口可返回 JSON。
[x] 不存在 case 返回受控错误。
[x] response body 不含 raw provider/tool/model 字段。
[x] report draft 不可提交。
```

---

# 九、P11P1-G：建立 runtime-demo-001 seed runtime adapter

## 目标

让 runtime-demo-001 从后端 view API 返回，而不是只存在于前端 mock。

## 任务

```text
[x] 建立 DemoRuntimeSeedProvider 或 SeedRuntimeAdapter。
[x] 提供 runtime-demo-001 的 RuntimeState / CaseFrame / PatientOutput / ClinicianReport 近似结构。
[x] 确保该 seed 数据不包含真实患者身份信息。
[x] 确保该 seed 数据不包含真实处方、剂量、治疗方案。
[x] seed 数据只通过 DemoRuntimeSeedViewSource 后备路径使用，并始终标记 FALLBACK。
```

## 验收标准

```text
[x] Patient API 可以读取 runtime-demo-001。
[x] Clinician API 可以读取 runtime-demo-001。
[x] Governance Console 仍可使用自己的 Console API 查看 runtime-demo-001 或显示缺失状态。
```

---

# 十、P11P1-H：前端 patientClient / clinicianClient API-first 改造

## 目标

让前端从 mock-first 改成 API-first。

## 建议文件

```text
console-web/src/portals/patient/api/patientClient.ts
console-web/src/portals/patient/hooks/usePatientRuntimeView.ts
console-web/src/portals/clinician/api/clinicianClient.ts
console-web/src/portals/clinician/hooks/useClinicianCaseView.ts
console-web/src/shared/api/httpClient.ts
```

## 任务

```text
[x] 新增 patientClient。
[x] 新增 clinicianClient。
[x] PatientHomePage 改为优先 listPatientSessions。
[x] PatientSafeSummaryPage 改为优先 getPatientSafeSummary / getPatientRuntimeView。
[x] ClinicianCaseInboxPage 改为优先 listClinicianCases。
[x] CaseWorkspacePage 改为优先 getClinicianCase。
[x] API failure 时显示受控错误或 demo fallback 标记。
[x] runtimeDemoData.ts 降级为 fallback/seed，不再作为主要数据源。
```

## 验收标准

```text
[x] 后端可用时页面展示 API 数据。
[x] 后端不可用时页面明确 demo fallback。
[x] 不在 console.log 打印完整医疗 DTO。
[x] 前端敏感字段测试仍通过。
```

---

# 十一、P11P1-I：Java / Frontend 自动测试

## Java tests

```text
[x] PatientViewProjectionServiceTest
[x] Patient policy coverage：PatientViewProjectionServiceTest / RoleSpecificViewSafetyPolicyTest
[x] PatientViewSanitizerTest
[x] PatientViewControllerTest
[x] ClinicianCaseProjectionServiceTest
[x] Clinician policy coverage：ClinicianCaseProjectionServiceTest / RoleSpecificViewSafetyPolicyTest
[x] ClinicianViewSanitizerTest
[x] ClinicianViewControllerTest
[x] RoleSpecificViewSafetyPolicyTest
```

## Frontend tests

```text
[x] patientClient.test.ts
[x] clinicianClient.test.ts
[x] PatientPortalApiFallback.test.tsx
[x] ClinicianWorkspaceApiFallback.test.tsx
[x] PatientApiProjectionRender.test.tsx
[x] ClinicianApiProjectionRender.test.tsx
```

## Regression

```text
[x] mvn test：Docker JDK17 全量 559 tests，0 failures，0 errors，23 skipped。
[x] npm run typecheck。
[x] npm run build。
[x] npm run test：27 test files、55 tests，19.88s，正常退出。
```

---

# 十二、P11P1-J：人工验证与冻结记录

## 人工验证文件

```text
docs/3-phase实现/Phase11_P1人工测试结果.md
```

必须覆盖：

```text
[x] Patient session list API。
[x] Patient safe summary API。
[x] Clinician case list API。
[x] Clinician case workspace API。
[x] 后端不可用 fallback。
[x] 三角色 runtime-demo-001 展示。
[x] 敏感字段页面检索。
```

## 冻结记录文件

```text
docs/3-phase实现/Phase11_P1冻结记录.md
```

必须说明：

```text
1. PatientViewProjectionService 如何生成患者视图。
2. ClinicianCaseProjectionService 如何生成医生视图。
3. Patient DTO 禁止了哪些字段。
4. Clinician DTO 禁止了哪些字段。
5. 前端如何从 mock-first 切到 API-first。
6. runtime-demo-001 seed runtime 如何工作。
7. Java / frontend 测试结果。
8. P1 仍不具备哪些生产级能力。
9. 后续 Phase 11-P2 的入口。
```

---

# 十三、不得扩展项

实现 Phase 11-P1 时不得顺手加入：

```text
真实登录
真实患者注册
真实医生提交
真实报告保存
处方
药品剂量
转诊
预约
支付
外部消息发送
Candidate approve / reject / publish
Provider / Tool / Agent 前端触发
生产级权限后台
```

如确实需要这些能力，必须后置到 Phase 11-P2 / P11-P3 并重新设计。

---

# 十四、最终结论

Phase 11-P1 的任务不是继续堆页面，而是补上 Phase 11-P0 最大缺口：

```text
让 Patient / Clinician 前端从 mock projection 变成后端 role-specific projection。
```

完成 P11-P1 后，系统可以更准确地表达为：

```text
ClinMindRuntime 已具备受控 Runtime、治理 Console、多角色前端，以及 Patient / Clinician 的后端受控视图 API 原型。
```

但仍不能表达为生产级医疗系统。

---

# 当前实现状态补充（Seed-backed 第一版）

当前 Phase 11-P1 实现的数据流为 DemoRuntimeSeedProvider 到 Projection Service，再到 Role-specific Safe DTO 与 API-first Portal。

当前实现已经具备后端 API、policy、sanitizer 与 audit，但数据源仍为 DemoRuntimeSeedProvider，尚未完成真实 RuntimeState / CaseFrame / PatientOutput / ClinicianReport 到 Projection Service 的投影。

因此当前状态为 **Seed-backed Role-specific View API**，不能标记为完整的 Runtime-backed projection，也暂不满足冻结条件。后续必须接入真实 RuntimeStore 及相关领域输出。

本轮冻结前修复项：

- [x] DemoRoleSwitcher 同步 DebugContext.roles。
- [x] Patient / Clinician policy reject 记录 VIEW_PROJECTION_POLICY_REJECTED。
- [x] listSessions / listCases 支持空列表，并以 UNAVAILABLE 记录成功查询。
- [x] 接入 RuntimeStore / CaseFrame / PatientOutput / ClinicianReport，替代 DemoRuntimeSeedProvider。
## 第二阶段：Runtime-backed source adapter

已建立 PatientViewSource / ClinicianViewSource 抽象及以下实现：

- RuntimeStoreViewSource：从 RuntimeStore 和 RuntimeState 生成角色视图，缺失领域段显式标记 PARTIAL。
- DemoRuntimeSeedViewSource：保留演示数据作为 fallback，所有 fallback DTO 显式标记 FALLBACK。
- RoleSpecificViewSource：RuntimeStore 主路径优先，真实来源无结果时才使用 seed fallback。

Projection Service 已改为依赖 source 接口，不再直接依赖 DemoRuntimeSeedProvider。fallback 状态会随现有 projection read audit 写入 projection_status，避免被误认为真实 Runtime 投影。
---

# 十五、2026-07-14 收口状态（权威回写）

| 收口项 | 状态 |
|---|---|
| RuntimeStore 主路径 | 已完成 |
| PARTIAL missing_sections | 已完成 |
| seed fallback 显式 FALLBACK | 已完成 |
| Patient Care Navigation | 已完成 |
| Clinician Inquiry Timeline | 已完成 |
| Clinician Evidence Panel | 已完成（当前 Runtime snapshot / graph ref） |
| Clinician AI Suggestions | 已完成（当前 Agent snapshot / report） |
| Java 全量测试 | 559 tests，0 failures，0 errors，23 skipped；P11-P1 定向 11/11 |
| Python 测试 | 10/10 通过 |
| TypeScript | 通过 |
| 前端生产构建 | 通过 |
| Vitest 全量 | 通过：27 test files、55 tests，19.88s，正常退出 |
| 浏览器人工验证 | 通过：Patient / Clinician RuntimeStore、PARTIAL、seed/local fallback、Report Draft 与敏感字段边界 |
| Phase 11-P1 冻结 | **FROZEN** |

冻结判定以 Phase11_P1冻结记录.md 为准。不得因路线图写有“收口”或“即将完成”而标记为已冻结。