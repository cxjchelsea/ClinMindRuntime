# Phase 10-P0 冻结记录：Governance Console / Runtime Console MVP

> 上位规格：`Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`
> API 与测试设计：`Phase10_P0Console_API与测试设计.md`
> 任务清单：`Phase10_P0开发任务清单.md`
> 人工测试：`Phase10_P0人工测试结果.md`
> 冻结日期：2026-07-08

---

# 一、冻结结论

Phase 10-P0 **Governance Console / Runtime Console MVP** 已实现并通过核心自动化测试、前端构建验证和人工验收，现冻结。

本阶段完成的是治理可观测层的最小闭环：

```text
RuntimeState / Trace / Audit / Evaluation / Candidate / Governance Snapshot
-> Console read aggregation
-> Safe DTO
-> /api/v1/console read-only API
-> console-web observation pages
-> Console Evaluation Scorer
```

Phase 10-P0 只提供观察能力，不接管 Runtime，不执行外部能力，不提供生产级审核动作。

---

# 二、已实现能力

## Safe DTO / mapper

新增：

- `ConsoleOverviewDto`
- `RuntimeListItemDto`
- `RuntimeTimelineDto`
- `RuntimeTimelineNodeDto`
- `GovernanceDomainCardDto`
- `CandidateInboxItemDto`
- `AuditBrowserItemDto`
- `console.view.mapper.ConsoleSafeDtoMapper`

扩展：

- `SensitiveFieldPolicy`

过滤范围包括：

- raw patient dialogue / raw patient text
- raw prompt / prompt_text
- secret / api_key / private_key
- raw_external_response
- internal_chain_of_thought / chain_of_thought / full_rationale
- patient_output / clinician_report / raw_input / patient_input

## Read aggregation

新增：

- `ConsoleOverviewService`
- `ConsoleReadService`

能力：

- Overview 计数聚合
- Runtime list
- Runtime timeline
- Governance domain cards
- Candidate inbox query
- Audit browser query

所有查询均复用既有 store / service，不新增 Runtime 写入链路。

## Console API

新增正式只读 API 前缀：

- `GET /api/v1/console/overview`
- `GET /api/v1/console/runtimes`
- `GET /api/v1/console/runtimes/{runtime_id}/timeline`
- `GET /api/v1/console/governance/domains`
- `GET /api/v1/console/candidates`
- `GET /api/v1/console/audits`

`DebugTokenFilter` 与 `ActorContextFilter` 已覆盖 `/api/v1/console/`。

## Access policy

新增：

- `Phase10ConsoleAccessPolicy`
- `DebugRole.PATIENT`

冻结规则：

- `SYSTEM_ADMIN` 可读。
- `EVALUATION_REVIEWER` 可读。
- `READ_ONLY_OBSERVER` 可读。
- `PATIENT` 禁止访问 Phase10 console。
- Phase10 console 不暴露 REVIEW / approve / reject / publish / run 动作。

## Console evaluation scorer

新增：

- `ConsoleSafeDtoScorer`
- `ConsoleTimelineCompletenessScorer`

支持 tag：

- `console_governance_eval`

冻结规则：

- Console response 包含敏感 key 时 scorer failure。
- Timeline 缺失 `SAFETY_GATE` / `DECISION_BOUNDARY` 时 scorer failure。
- 默认 evaluation case 不受影响。

## console-web

新增页面：

- `ConsoleOverviewPage`
- `RuntimeTimelinePage`
- `GovernanceDomainsPage`
- `CandidateInboxPage`
- `AuditBrowserPage`

新增路由：

- `/overview`
- `/runtime-timeline`
- `/governance-domains`
- `/candidate-inbox`
- `/audit-browser`

这些页面只调用 `/api/v1/console` 读接口，不提供写按钮或审核动作。

---

# 三、治理边界

Phase 10-P0 冻结后仍禁止：

- 患者端 UI
- 正式医生工作站
- 生产级审核平台
- approve / reject / publish / run 写操作
- Console 修改 RuntimeState
- Console 调用 Provider / Tool / Agent
- Console 触发诊断、治疗、转诊、处方、预约、支付、消息发送
- 展示 raw patient dialogue
- 展示完整 prompt 原文
- 展示 secret / api key / private key
- 展示 raw external response
- 展示 internal chain-of-thought / full rationale

---

# 四、测试结果

## Java compile

```text
mvn -DskipTests compile
PASS
```

## Phase10 targeted Java tests

```text
ConsoleSafeDtoMapperTest
ConsoleAccessPolicyTest
ConsoleOverviewControllerTest
RuntimeTimelineControllerTest
GovernanceDashboardControllerTest
ConsoleCandidateInboxControllerTest
ConsoleAuditBrowserControllerTest
ConsoleScorerTest

13 run, 0 failures, 0 errors, 0 skipped
```

## console-web

```text
node .\node_modules\typescript\bin\tsc --noEmit
PASS

node .\node_modules\vite\bin\vite.js build
PASS
```

说明：

- `npm run test` 与 direct Vitest entry 在当前本机环境中启动超时。
- `console-web/src` 下未发现现有 `*.test.*` / `*.spec.*` 文件。

---

# 五、后置任务

Phase 10-P1 / P10-P2 可继续推进：

- Candidate Review Action Workbench
- Provider / Model / Tool release review UI
- Runtime graph visualization
- Audit advanced search
- Role-based production console

上述后续阶段开始前，必须重新打开新的 phase 设计与实现约束；不得在 Phase 10-P0 冻结范围内追加写操作。

---

# 六、最终结论

Phase 10-P0 已冻结。

冻结范围仅限 **read-only Governance Console / Runtime Console MVP**，包括 Safe DTO、只读 API、console-web 观察页面、访问控制和 Console Evaluation Scorer。
