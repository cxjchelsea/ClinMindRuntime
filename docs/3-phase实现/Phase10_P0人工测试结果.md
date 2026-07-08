# Phase 10-P0 人工测试结果：Governance Console / Runtime Console MVP

> 规格：`Phase10_P0GovernanceConsole_RuntimeConsole_实现规格.md`
> API 与测试设计：`Phase10_P0Console_API与测试设计.md`
> 任务清单：`Phase10_P0开发任务清单.md`
> 测试日期：2026-07-08

---

# 一、测试范围

本次验证覆盖 Phase 10-P0 的只读治理可观测闭环：

```text
RuntimeState / Trace / Audit / Evaluation / Candidate / Governance Snapshot
-> ConsoleOverviewService / ConsoleReadService
-> Safe DTO
-> /api/v1/console read-only API
-> console-web read-only pages
-> Console Evaluation Scorer
```

本次验证不覆盖患者端 UI、正式医生工作站、生产级审核平台、approve / reject / publish / run 写操作、RuntimeState 修改、Provider / Tool / Agent 调用、诊断/治疗/转诊/处方/预约/支付/消息发送。

---

# 二、自动化测试结果

## Java compile

命令：

```powershell
$env:JAVA_HOME='D:\cxj\software\jdk21'
& 'D:\cxj\software\maven\apache-maven-3.9.12\bin\mvn.cmd' -DskipTests compile
```

结果：通过。

## Java targeted tests

命令：

```powershell
mvn "-Dtest=ConsoleSafeDtoMapperTest,ConsoleAccessPolicyTest,ConsoleOverviewControllerTest,RuntimeTimelineControllerTest,GovernanceDashboardControllerTest,ConsoleCandidateInboxControllerTest,ConsoleAuditBrowserControllerTest,ConsoleScorerTest" test
```

结果：

```text
13 run, 0 failures, 0 errors, 0 skipped
```

覆盖：

- Console Safe DTO mapper
- Phase10 read-only access policy
- Console overview API
- Runtime timeline API
- Governance domain dashboard API
- Candidate inbox API
- Audit browser API
- Console evaluation scorer

## console-web

命令：

```powershell
node .\node_modules\typescript\bin\tsc --noEmit
node .\node_modules\vite\bin\vite.js build
```

结果：

```text
TypeScript check: PASS
Vite build: PASS
```

说明：

- `npm run test` 与直接 Vitest entry 在当前环境中启动超时。
- 已确认 `console-web/src` 下无 `*.test.*` / `*.spec.*` 前端测试文件。
- 本轮前端验证以 TypeScript check 与 Vite production build 为准。

---

# 三、人工场景验证

## 场景 1：Console Overview

验证点：

- `GET /api/v1/console/overview` 可返回 Phase10-P0 总览。
- 包含 runtime / provider / tool / model / candidate / audit 计数。
- 空数据时返回 0，不抛异常。
- 响应为 Safe DTO。

结果：通过。

## 场景 2：Runtime Timeline

验证点：

- `GET /api/v1/console/runtimes/{runtime_id}/timeline` 可返回 runtime timeline。
- timeline 包含 `SAFETY_GATE`、`DECISION_BOUNDARY`、Provider / Model / Tool governance 等节点。
- 缺失节点以 `MISSING` 标记，不暴露 raw input。
- 不修改 RuntimeState。

结果：通过。

## 场景 3：Governance Domains

验证点：

- `GET /api/v1/console/governance/domains` 返回治理域卡片。
- 覆盖 runtime、provider、model、tool、candidate、evaluation、audit、console 域。
- 空数据时仍返回稳定卡片。

结果：通过。

## 场景 4：Candidate Inbox

验证点：

- `GET /api/v1/console/candidates` 支持 `review_status`、`risk_level`、`candidate_type`、`limit` 过滤。
- 只返回只读 `CandidateInboxItemDto`。
- 不提供 approve / reject / publish 入口。
- metadata 中 `raw_external_response` 等敏感字段被过滤。

结果：通过。

## 场景 5：Audit Browser

验证点：

- `GET /api/v1/console/audits` 支持 `action_type`、`resource_type`、`actor_id`、`status`、`limit` 过滤。
- 只返回裁剪后的 audit summary。
- metadata 中 `prompt_text`、secret、raw external response 等敏感字段被过滤。

结果：通过。

## 场景 6：Access Policy

验证点：

- `SYSTEM_ADMIN` 可读 Phase10 console。
- `EVALUATION_REVIEWER` 可读 Phase10 console。
- `READ_ONLY_OBSERVER` 可读 Phase10 console。
- `PATIENT` 被拒绝，返回 `ACCESS_DENIED`。

结果：通过。

## 场景 7：Console Evaluation Scorer

验证点：

- `ConsoleSafeDtoScorer` 对包含敏感 key 的 console response 判失败。
- `ConsoleTimelineCompletenessScorer` 可识别 timeline 中的 `SAFETY_GATE` / `DECISION_BOUNDARY`。
- 未带 `console_governance_eval` tag 的普通 evaluation case 不受影响。

结果：通过。

---

# 四、结论

Phase 10-P0 的只读治理控制台能力已通过本轮自动化验证和人工场景验收。

保留说明：

- 当前 console-web 前端测试入口在本机环境中超时，且项目下无现有前端测试文件；已用 TypeScript check 与 Vite build 完成前端构建验证。
- Phase 10-P0 不引入任何 Console 写操作，不执行 Provider / Tool / Agent，不修改 RuntimeState。
