# Phase 11-P1 人工测试结果

> 测试日期：2026-07-14  
> 测试范围：Runtime-backed Role-specific View API / Frontend BFF 收口  
> 结论：代码、自动化验证和 Patient / Clinician 浏览器人工交互均已通过，满足正式冻结条件。

## 一、人工核对结果

| 场景 | 结果 | 证据 |
|---|---|---|
| Patient / Clinician Projection Service 不直接依赖 seed provider | 通过 | 服务仅依赖 PatientViewSource / ClinicianViewSource |
| RuntimeStore 主路径优先 | 通过 | RoleSpecificViewSource 优先调用 RuntimeStoreViewSource |
| Runtime 缺段返回 PARTIAL | 通过 | patientMissing / clinicianMissing 与 RoleSpecificViewSourceTest |
| 无 Runtime 时 seed fallback | 通过 | DemoRuntimeSeedViewSource 强制标记 FALLBACK |
| Patient Care Navigation | 通过 | 从 SafetyGate.requiredAction 与 DecisionBoundary reason/constraints 投影 |
| Clinician Inquiry Timeline | 通过 | 从 RuntimeState.inputHistory 与 AgentOrchestration.acceptedQuestions 投影 |
| Clinician Evidence Panel | 通过 | 从 EvidenceRetrieval.acceptedCandidates 或 EvidenceGraph.evidenceRefs 投影 |
| Clinician AI Suggestions | 通过 | 从 Agent acceptedQuestions，缺失时使用 ClinicianReport.recommendedQuestions |
| Safe DTO / policy / sanitizer / audit 链路 | 通过 | 既有 Projection Service 测试与 hardening 测试 |
| 前端 API-first / fallback 标记 | 代码核对通过 | patient/clinician clients 与 hooks 保留显式 fallback |
| 浏览器人工交互 | 通过 | Patient / Clinician API-first、RuntimeStore、PARTIAL、seed/local fallback、Report Draft 与敏感字段边界均已核对 |

## 二、自动化验证

| 命令 | 结果 |
|---|---|
| Docker JDK17 Maven：全量测试 | 通过，559 tests，0 failures，0 errors，23 skipped；P11-P1 定向 11/11 |
| python -m pytest -q | 通过，10 passed |
| npm run typecheck | 通过 |
| npm run build | 通过，Vite production build 成功 |
| npm test | 通过，27 test files、55 tests，19.88s，正常退出 |

## 三、浏览器人工验证证据

- 环境：Windows 浏览器 + Vite 5173；后端使用 Docker JDK 17，监听 8080。
- seed fallback：RuntimeStore 为空时，Patient API 返回 200 与 `projection_status=FALLBACK`；页面未误报为本地 API fallback。
- PARTIAL：编码异常输入形成 Runtime `rt_e9f3c50911c2`，状态 `ERROR_SAFE_HALTED`，投影为 `PARTIAL`，并明确列出 `patient_output`、`decision_boundary`、`safety_gate` 缺段。
- Patient RuntimeStore 主路径：Runtime `rt_9eb593e85851`，状态 `SAFETY_GATE_TRIGGERED`，投影 `COMPLETE`，Care Navigation 6 项，无 `missing_sections`，无本地 fallback。
- Clinician RuntimeStore 主路径：同一 Runtime 投影 `COMPLETE`；Inquiry Timeline 3 条、Evidence Panel 3 条、AI Suggestions 1 条、DDx Board 4 条，无 `missing_sections`。
- Report Draft：浏览器只读草稿展示通过，无保存、提交或签发能力。
- local fallback：不存在的 case/session 页面显示受控 Demo fallback，页面未白屏。
- 角色同步：Patient 请求使用 `PATIENT`，Clinician 请求使用 `CLINICIAN`。
- 敏感字段：Patient 顶层 DTO 未出现 DDx / Trace / Audit / Evaluation / Candidate；Clinician DTO 未出现 `raw_prompt` / `secret` / `raw_external_response` / `full_rationale`。

## 四、已知限制

1. 本机 Maven 默认使用 JDK 8；Java 17 测试通过 Docker 执行。
2. Evidence Panel 只投影当前 Runtime 的 evidence snapshot / graph ref；真实 Evidence Engine 属于 Phase 12。
3. AI Suggestions 来自当前受控 Agent snapshot 或 clinician report，不代表真实 LLM 建议。
4. seed fallback 仅为演示连续性，始终标记 FALLBACK。
5. 本轮浏览器验收使用本地 in-memory RuntimeStore 与演示数据，不代表生产环境或真实临床数据验收。