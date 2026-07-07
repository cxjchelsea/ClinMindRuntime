# Phase 10-P0 Governance Console / Runtime Console 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 前置阶段：Phase 9-P0 Tool / MCP / Skills 受控接入 MVP 已冻结  
> 当前 Phase：Phase 10-P0  
> 当前目标：建立 Governance Console / Runtime Console 的最小可视化与聚合查询入口，把 Runtime、Trace、Audit、Evaluation、Candidate、Agent、Evidence、Provider、ModelGov、ToolGov 的治理信息统一展示出来，但不做患者端产品、不做生产级审核平台、不允许 Console 绕过 Runtime 主控。

---

# 一、Phase 定位

Phase 10-P0 不是新业务功能，也不是重新设计 Runtime。

Phase 10-P0 的目标是：

```text
在 Phase 6–9 已经具备多类治理能力之后，
新增一个受控 Console 层，
把 Runtime 执行过程、能力调用、治理快照、评估结果、候选问题和审计记录做成统一可观察入口，
用于开发调试、架构展示、面试演示和后续人工治理平台的基础。
```

核心命题：

```text
Console 只观察和解释 Runtime，不接管 Runtime。
Console 只展示 Safe DTO，不泄露患者原文、prompt 原文、secret 或外部系统原文。
Console P0 以 read-only 为主，不做生产级审批流。
```

---

# 二、前置状态

当前已经具备：

```text
Phase 6-P0：Agent Runtime / AgentProposal / Agent Trace
Phase 7-P0：RAG EvidenceProvider / EvidenceGraph
Phase 7-P1：Graph Evidence / KG-lite
Phase 8-P0：Python Provider / Embedding / Rerank / Provider Debug
Phase 8-P1：Judge / Risk / ProviderCapabilityProfile / ProviderGovernanceSnapshot
Phase 8-P2：ModelRegistry / PromptRegistry / DatasetVersion / Model Governance
Phase 9-P0：Tool / MCP / Skills Governance / ToolGovernanceSnapshot
Evaluation / Candidate / Audit / Review-required governance
```

Phase 10-P0 应复用这些后端对象和 Debug / Governance API，不得绕开既有 Policy、Validation、Trace、Audit、Evaluation、Candidate Governance。

---

# 三、当前不做什么

Phase 10-P0 明确不做：

```text
1. 不做患者端 UI。
2. 不做医生正式工作站。
3. 不做生产级审核平台。
4. 不做真实医疗业务操作入口。
5. 不允许 Console 直接修改 RuntimeState。
6. 不允许 Console 直接触发诊断、治疗、转诊、处方、预约、支付、消息发送。
7. 不允许 Console 绕过 SafetyGate / DecisionBoundary。
8. 不允许 Console 展示未脱敏患者原文、完整 prompt 原文、secret、raw external response。
9. 不做复杂权限系统，只复用现有 ActorContext / AccessPolicy。
10. 不做实时大屏或复杂图数据库可视化。
11. 不做生产级审计检索系统。
```

P0 可以做：

```text
Runtime Overview
Runtime Detail Timeline
Capability Governance Dashboard
Evaluation / Candidate Inbox
Audit Browser
Agent / Evidence / Provider / ModelGov / ToolGov Snapshot Cards
Safe DTO / Console BFF API
console-web 基础页面
测试与冻结记录
```

---

# 四、Phase 10-P0 核心链路

目标链路：

```text
RuntimeState / RuntimeTrace / AuditLog / EvaluationResult / Candidate
↓
ConsoleAggregationService / ConsoleQueryService
↓
Safe DTO 裁剪
↓
Console API
↓
console-web 页面
  ├── Overview Dashboard
  ├── Runtime Detail Timeline
  ├── Governance Domain Cards
  ├── Evaluation & Candidate Inbox
  └── Audit Browser
↓
只读观察 / 不接管 Runtime
```

核心边界：

```text
Console 不生成 PatientOutput。
Console 不改变 RuntimeState。
Console 不执行 Provider / Tool / Agent。
Console 不发布模型、prompt、dataset、tool、skill。
Console 只展示已经存在的治理结果。
```

---

# 五、核心页面设计

## 5.1 Console Overview Dashboard

用途：项目治理总览。

展示：

```text
runtime_count
active_runtime_count
high_risk_runtime_count
agent_invocation_count
provider_call_count
tool_invocation_count
evaluation_case_count
candidate_count
audit_event_count
latest_freeze_phase
```

P0 可以使用内存统计或从既有 store 聚合。

## 5.2 Runtime Detail Timeline

用途：展示一次 Runtime 的完整执行轨迹。

时间线节点：

```text
EntryAssessment
SafetyGate
Agent Orchestration
RAG Evidence Retrieval
Graph Evidence Retrieval
Provider Enhancement
Provider Governance
Model Governance
Tool Governance
DecisionBoundary
PatientOutput
ClinicianReport
Evaluation
Candidate
Audit
```

要求：

```text
只展示 Safe DTO。
患者原始输入需要摘要化或脱敏。
不展示完整 prompt / rationale / raw external response。
```

## 5.3 Governance Domain Cards

用途：横向展示治理域状态。

卡片：

```text
Agent Governance
Evidence Governance
Provider Governance
Model Governance
Tool Governance
Evaluation Governance
Candidate Governance
Audit Governance
```

每张卡片展示：

```text
status
latest_event_time
risk_count
validation_rejected_count
policy_rejected_count
fallback_count
review_required_count
```

## 5.4 Evaluation / Candidate Inbox

用途：展示 Evaluation 失败项与候选问题。

展示：

```text
metric_id
severity
risk_level
candidate_type
review_status
source_runtime_id
source_trace_id
created_at
```

P0 只读，不做正式 approve / reject。

## 5.5 Audit Browser

用途：查看关键治理操作。

展示：

```text
audit_id
action_type
resource_type
resource_id
actor_id
status
created_at
safe_metadata_summary
```

要求：

```text
metadata 必须裁剪。
不展示 secret、raw patient text、raw external response。
```

---

# 六、后端对象设计

## 6.1 ConsoleOverviewDto

建议字段：

```text
runtimeCount
activeRuntimeCount
agentInvocationCount
providerCallCount
toolInvocationCount
modelGovernanceRecordCount
evaluationResultCount
candidateCount
auditEventCount
latestPhase
healthStatus
warnings
```

## 6.2 RuntimeTimelineDto

建议字段：

```text
runtimeId
sessionId
createdAt
updatedAt
status
mode
nodes
warnings
```

TimelineNode：

```text
nodeId
nodeType
title
status
severity
summary
safePayload
relatedTraceIds
relatedAuditIds
createdAt
```

## 6.3 GovernanceDomainCardDto

建议字段：

```text
domain
status
summary
policyRejectedCount
validationRejectedCount
fallbackCount
reviewRequiredCount
latestUpdatedAt
```

## 6.4 CandidateInboxItemDto

建议字段：

```text
candidateId
candidateType
riskLevel
reviewStatus
sourceType
sourceRuntimeId
sourceMetricId
summary
createdAt
```

## 6.5 AuditBrowserItemDto

建议字段：

```text
auditId
actionType
resourceType
resourceId
actorId
status
safeMetadataSummary
createdAt
```

---

# 七、Console Service 设计

建议新增：

```text
ConsoleOverviewService
RuntimeTimelineService
GovernanceDashboardService
ConsoleCandidateQueryService
ConsoleAuditQueryService
ConsoleSafeDtoMapper
```

职责：

```text
ConsoleOverviewService：聚合总体统计。
RuntimeTimelineService：把 RuntimeState / Trace / Snapshot 转为 timeline。
GovernanceDashboardService：聚合 Agent / Evidence / Provider / ModelGov / ToolGov 卡片。
ConsoleCandidateQueryService：读取 review-required candidate。
ConsoleAuditQueryService：读取并裁剪 audit。
ConsoleSafeDtoMapper：统一脱敏和 Safe DTO 映射。
```

---

# 八、Console API 设计

推荐 API：

```text
GET /api/v1/console/overview
GET /api/v1/console/runtimes
GET /api/v1/console/runtimes/{runtime_id}/timeline
GET /api/v1/console/governance/domains
GET /api/v1/console/candidates
GET /api/v1/console/audits
```

权限：

```text
SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER 可读。
PATIENT 禁止。
P0 不提供写操作。
```

---

# 九、前端 console-web 设计

Phase 10-P0 可以在 `console-web` 中新增：

```text
/src/pages/console/OverviewPage.tsx
/src/pages/console/RuntimeTimelinePage.tsx
/src/pages/console/GovernanceDashboardPage.tsx
/src/pages/console/CandidateInboxPage.tsx
/src/pages/console/AuditBrowserPage.tsx
/src/services/consoleApi.ts
/src/components/console/MetricCard.tsx
/src/components/console/TimelineView.tsx
/src/components/console/GovernanceDomainCard.tsx
/src/components/console/SafeJsonPanel.tsx
```

UI 要求：

```text
只展示 Safe DTO。
敏感字段显示为 [REDACTED]。
不要展示 PatientOutput 以外的患者原文。
不要展示 prompt 原文。
不要展示 secret 或 raw external response。
```

P0 不追求复杂美术效果，以信息结构清晰为主。

---

# 十、Access / Safe DTO 规则

最低规则：

```text
1. PATIENT 不能访问 Console API。
2. 所有 API 返回 Safe DTO。
3. rawPatientDialogue / rawPrompt / secret / apiKey / privateKey / rawExternalResponse 字段必须过滤。
4. metadata 只展示 summary。
5. Timeline 节点只显示摘要和状态，不显示完整内部推理链。
6. Candidate 只读。
7. Audit 只读。
```

---

# 十一、Evaluation 与 Candidate 的关系

Phase 10-P0 不新增新的治理逻辑，但可以新增 Console 可观测性 Scorer：

```text
ConsoleSafeDtoScorer
ConsoleTimelineCompletenessScorer
ConsoleAuditVisibilityScorer
ConsoleCandidateInboxScorer
```

P0 最小：

```text
ConsoleSafeDtoScorer
ConsoleTimelineCompletenessScorer
```

支持 tag：

```text
console_governance_eval
```

---

# 十二、Trace / Audit

Console P0 本身是 read-only，不需要大量 Audit。

最低要求：

```text
CONSOLE_OVERVIEW_READ
CONSOLE_RUNTIME_TIMELINE_READ
CONSOLE_CANDIDATE_INBOX_READ
CONSOLE_AUDIT_BROWSER_READ
```

如果 P0 暂不审计 read 操作，也必须在冻结记录中说明。

---

# 十三、完成标准

Phase 10-P0 完成时必须满足：

```text
1. Console Overview API 可用。
2. Runtime Timeline API 可用。
3. Governance Domain API 可用。
4. Candidate Inbox API 可用。
5. Audit Browser API 可用。
6. 所有 Console API 返回 Safe DTO。
7. PATIENT 访问被拒绝。
8. console-web 至少有 Overview / Runtime Timeline / Governance Dashboard 页面。
9. Console Safe DTO 测试通过。
10. Console API 测试通过。
11. 如果修改前端，npm test / npm build 通过。
12. mvn test 通过。
13. Phase 1–9 P0 既有测试不回归。
```

---

# 十四、后置任务

Phase 10-P0 不完成但可后置：

```text
1. Phase 10-P1：Candidate Review Action Workbench。
2. Phase 10-P1：Model / Tool / Provider Release Review UI。
3. Phase 10-P2：Runtime Graph Visualization。
4. Phase 10-P2：Audit advanced search。
5. Phase 10-P3：Role-based production console。
6. Phase 10-P3：真实医生审核工作台。
7. Phase 10-P3：Console frontend visual polish。
```

---

# 十五、最终结论

Phase 10-P0 的本质是：

```text
从“后端治理能力已经具备”
升级为“治理能力可以被统一观察、解释和展示”。
```

它完成后，ClinMindRuntime 将具备一个最小治理控制台：

```text
Runtime Timeline
+ Governance Dashboard
+ Evaluation / Candidate Inbox
+ Audit Browser
```

但它仍然不接管 Runtime，不做生产级审批，不做患者端产品。
