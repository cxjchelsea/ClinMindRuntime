# Phase 10-P0 开发任务清单：Governance Console / Runtime Console MVP

> 上位实现规格：`docs/3-phase实现/Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase10_P0Console_API与测试设计.md`  
> 前置冻结：`docs/3-phase实现/Phase9_P0冻结记录.md`  
> 当前目标：建立治理控制台与 Runtime 控制台的最小可观察入口，但不做患者端、不做生产级审核平台、不让 Console 接管 Runtime。

---

# 一、Phase 10-P0 总目标

Phase 10-P0 要完成的不是新 Runtime 能力，而是治理可视化最小闭环：

```text
RuntimeState / Trace / Audit / Evaluation / Candidate / Governance Snapshot
→ ConsoleAggregationService
→ Safe DTO
→ Console API
→ console-web 页面
→ Read-only governance observation
```

最终要证明：

```text
Agent / Evidence / Provider / ModelGov / ToolGov / Evaluation / Candidate / Audit 的治理信息可以被统一观察；
Runtime 执行过程可以被 timeline 化解释；
所有 Console 输出都是 Safe DTO；
Console 不能修改 RuntimeState；
Console 不能执行 Provider / Tool / Agent；
Console 不能发布模型、工具、prompt、dataset 或 candidate。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P10P0-A | 建立 Console Safe DTO 与 mapper | 待做 |
| P10P0-B | 实现 Console Overview 聚合 | 待做 |
| P10P0-C | 实现 Runtime Timeline 聚合 | 待做 |
| P10P0-D | 实现 Governance Domain Dashboard | 待做 |
| P10P0-E | 实现 Candidate Inbox 查询 | 待做 |
| P10P0-F | 实现 Audit Browser 查询 | 待做 |
| P10P0-G | 实现 Console API Controller | 待做 |
| P10P0-H | 实现 console-web 页面 | 待做 |
| P10P0-I | 实现 Console Evaluation Scorer | 待做 |
| P10P0-J | 测试、人工验证与冻结记录 | 待做 |

---

# 三、P10P0-A：建立 Console Safe DTO 与 mapper

## 目标

统一定义 Console 输出对象，避免泄露敏感内容。

## 建议包路径

```text
src/main/java/com/clinmind/runtime/console/view/
src/main/java/com/clinmind/runtime/console/view/dto/
src/main/java/com/clinmind/runtime/console/view/mapper/
```

## 任务

```text
[ ] 新增 ConsoleOverviewDto。
[ ] 新增 RuntimeTimelineDto。
[ ] 新增 TimelineNodeDto。
[ ] 新增 GovernanceDomainCardDto。
[ ] 新增 CandidateInboxItemDto。
[ ] 新增 AuditBrowserItemDto。
[ ] 新增 ConsoleSafeDtoMapper。
[ ] 敏感 key 统一过滤：rawPatientDialogue、raw_prompt、prompt_text、secret、api_key、private_key、raw_external_response、internal_chain_of_thought、full_rationale。
```

## 验收标准

```text
[ ] Safe DTO 不包含敏感 key。
[ ] SafeJson metadata 中敏感值显示为 [REDACTED]。
[ ] mapper 单元测试覆盖。
```

---

# 四、P10P0-B：实现 Console Overview 聚合

## 目标

提供治理总览指标。

## 任务

```text
[ ] 新增 ConsoleOverviewService。
[ ] 聚合 runtime_count。
[ ] 聚合 provider_call_count。
[ ] 聚合 tool_invocation_count。
[ ] 聚合 model_governance_record_count。
[ ] 聚合 candidate_count。
[ ] 聚合 audit_event_count。
[ ] 返回 latest_phase。
[ ] 返回 warnings。
```

## 验收标准

```text
[ ] 空数据时返回 0，不抛异常。
[ ] 统计值来自已有 store / service。
[ ] 返回 Safe DTO。
```

---

# 五、P10P0-C：实现 Runtime Timeline 聚合

## 目标

将一次 Runtime 执行过程变成可解释时间线。

## 任务

```text
[ ] 新增 RuntimeTimelineService。
[ ] 从 RuntimeState 生成 timeline。
[ ] 节点覆盖 SafetyGate / Agent / Evidence / Provider / ModelGov / ToolGov / DecisionBoundary / Evaluation / Candidate。
[ ] 节点排序稳定。
[ ] safe_payload 经过 ConsoleSafeDtoMapper。
[ ] 不展示 raw input / prompt / raw external response。
```

## 验收标准

```text
[ ] 有 Runtime 返回 timeline。
[ ] 不存在 Runtime 返回受控错误。
[ ] Timeline node 不泄露敏感字段。
```

---

# 六、P10P0-D：实现 Governance Domain Dashboard

## 目标

横向展示各治理域状态。

## 任务

```text
[ ] 新增 GovernanceDashboardService。
[ ] 生成 Agent Governance card。
[ ] 生成 Evidence Governance card。
[ ] 生成 Provider Governance card。
[ ] 生成 Model Governance card。
[ ] 生成 Tool Governance card。
[ ] 生成 Evaluation Governance card。
[ ] 生成 Candidate Governance card。
[ ] 生成 Audit Governance card。
```

## 验收标准

```text
[ ] 每个 card 包含 status / summary / rejection count / fallback count / review count。
[ ] 空数据时可显示 UNKNOWN / EMPTY，不抛异常。
```

---

# 七、P10P0-E：实现 Candidate Inbox 查询

## 目标

提供 review-required candidate 的只读列表。

## 任务

```text
[ ] 新增 ConsoleCandidateQueryService。
[ ] 支持按 review_status 查询。
[ ] 支持按 risk_level 查询。
[ ] 支持按 candidate_type 查询。
[ ] 返回 CandidateInboxItemDto。
[ ] P0 不提供 approve / reject / publish。
```

## 验收标准

```text
[ ] Candidate list 可读。
[ ] 不包含敏感内容。
[ ] 无写操作。
```

---

# 八、P10P0-F：实现 Audit Browser 查询

## 目标

提供审计日志只读浏览。

## 任务

```text
[ ] 新增 ConsoleAuditQueryService。
[ ] 支持按 action_type 查询。
[ ] 支持按 resource_type 查询。
[ ] 支持按 actor_id 查询。
[ ] 返回 AuditBrowserItemDto。
[ ] metadata 只返回 safe summary。
```

## 验收标准

```text
[ ] Audit list 可读。
[ ] metadata 敏感字段被 [REDACTED]。
[ ] 不返回完整 raw metadata。
```

---

# 九、P10P0-G：实现 Console API Controller

## 目标

提供最小 Console API。

## 任务

```text
[ ] 新增 ConsoleOverviewController 或 ConsoleController。
[ ] GET /api/v1/console/overview。
[ ] GET /api/v1/console/runtimes。
[ ] GET /api/v1/console/runtimes/{runtime_id}/timeline。
[ ] GET /api/v1/console/governance/domains。
[ ] GET /api/v1/console/candidates。
[ ] GET /api/v1/console/audits。
[ ] 接入 ActorContextResolver / AccessPolicy。
[ ] PATIENT 禁止。
```

## 验收标准

```text
[ ] SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER 可读。
[ ] PATIENT 被拒绝。
[ ] 所有响应为 Safe DTO。
```

---

# 十、P10P0-H：实现 console-web 页面

## 目标

提供最小可视化页面。

## 任务

```text
[ ] 新增 consoleApi.ts。
[ ] 新增 OverviewPage。
[ ] 新增 RuntimeTimelinePage。
[ ] 新增 GovernanceDashboardPage。
[ ] 新增 CandidateInboxPage。
[ ] 新增 AuditBrowserPage。
[ ] 新增 MetricCard。
[ ] 新增 TimelineView。
[ ] 新增 GovernanceDomainCard。
[ ] 新增 SafeJsonPanel。
[ ] 增加路由入口。
```

## 验收标准

```text
[ ] 页面能渲染 mock / API 数据。
[ ] SafeJsonPanel 能隐藏敏感 key。
[ ] npm test 通过。
[ ] npm build 通过。
```

---

# 十一、P10P0-I：实现 Console Evaluation Scorer

## 目标

让 Console 可观测性进入 Evaluation。

## 任务

```text
[ ] 新增 ConsoleSafeDtoScorer。
[ ] 新增 ConsoleTimelineCompletenessScorer。
[ ] 支持 console_governance_eval tag。
[ ] 可选 ConsoleAuditVisibilityScorer。
[ ] 可选 ConsoleCandidateInboxScorer。
```

## 验收标准

```text
[ ] DTO 含敏感字段时 scorer failure。
[ ] Timeline 缺 SafetyGate / DecisionBoundary 时 scorer failure。
[ ] 默认 evaluation case 不受影响。
```

---

# 十二、P10P0-J：测试、人工验证与冻结记录

## 目标

完成 Phase 10-P0 收口。

## 任务

```text
[ ] 完成 ConsoleSafeDtoMapperTest。
[ ] 完成 ConsoleAccessPolicyTest。
[ ] 完成 ConsoleOverviewControllerTest。
[ ] 完成 RuntimeTimelineControllerTest。
[ ] 完成 GovernanceDashboardControllerTest。
[ ] 完成 ConsoleCandidateInboxControllerTest。
[ ] 完成 ConsoleAuditBrowserControllerTest。
[ ] 完成 ConsoleScorerTest。
[ ] 如果改 console-web，完成 npm test / npm build。
[ ] 运行 mvn test。
[ ] 编写 Phase10_P0人工测试结果.md。
[ ] 编写 Phase10_P0冻结记录.md。
[ ] 更新 AI_IMPLEMENTATION_SKILL.md 为 Phase 10-P0 已冻结。
```

## 验收标准

```text
[ ] Java 测试通过。
[ ] 前端测试 / build 通过或说明未修改前端。
[ ] Phase 1–9 P0 回归不破坏。
[ ] 人工测试覆盖 Overview / Timeline / Governance / Candidate / Audit / PATIENT denied。
[ ] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十三、推荐实现顺序

建议严格按以下顺序实现：

```text
1. P10P0-A：Safe DTO / mapper。
2. P10P0-B：Overview 聚合。
3. P10P0-C：Runtime Timeline。
4. P10P0-D：Governance Domain Dashboard。
5. P10P0-E：Candidate Inbox。
6. P10P0-F：Audit Browser。
7. P10P0-G：Console API。
8. P10P0-I：Console Evaluation Scorer。
9. P10P0-H：console-web 页面。
10. P10P0-J：测试、人工验证、冻结记录。
```

前端可以在后端 Safe DTO 稳定后再做。

---

# 十四、开发期间禁止事项

```text
1. 不做患者端 UI。
2. 不做正式医生工作站。
3. 不做生产级审核平台。
4. 不做 approve / reject / publish / run 写操作。
5. 不让 Console 修改 RuntimeState。
6. 不让 Console 调用 Provider / Tool / Agent。
7. 不展示 raw patient dialogue。
8. 不展示完整 prompt 原文。
9. 不展示 secret / api key / private key。
10. 不展示 raw external response。
11. 不改写 Phase 1–9 P0 冻结记录。
```

---

# 十五、Phase 10-P0 完成后的后置任务

```text
1. Phase 10-P1：Candidate Review Action Workbench。
2. Phase 10-P1：Provider / Model / Tool release review UI。
3. Phase 10-P2：Runtime graph visualization。
4. Phase 10-P2：Audit advanced search。
5. Phase 10-P3：Role-based production console。
6. Phase 10-P3：真实医生审核工作台。
```

---

# 十六、最终 Definition of Done

Phase 10-P0 完成的最终标准：

```text
[ ] Console Overview API 可用。
[ ] Runtime Timeline API 可用。
[ ] Governance Domains API 可用。
[ ] Candidate Inbox API 可用。
[ ] Audit Browser API 可用。
[ ] Console Safe DTO 不泄露敏感字段。
[ ] PATIENT 访问被拒绝。
[ ] console-web 基础页面可用。
[ ] Console Evaluation Scorer 可识别可观测性缺口。
[ ] Java 测试通过。
[ ] 前端测试 / build 通过或说明未修改前端。
[ ] Phase10_P0冻结记录完成。
```
