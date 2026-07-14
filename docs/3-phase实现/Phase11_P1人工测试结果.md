# Phase 11-P1 人工测试结果

> 测试日期：2026-07-14  
> 测试范围：Runtime-backed Role-specific View API / Frontend BFF 收口  
> 结论：代码与主要自动化验证通过；Vitest 全量套件未获得完成结果，当前不满足正式冻结条件。

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
| 浏览器人工交互 | 未执行 | 当前回合未启动长期前后端服务及浏览器 |

## 二、自动化验证

| 命令 | 结果 |
|---|---|
| Docker JDK17 Maven：全量测试 | 通过，559 tests，0 failures，0 errors，23 skipped；P11-P1 定向 11/11 |
| python -m pytest -q | 通过，10 passed |
| npm run typecheck | 通过 |
| npm run build | 通过，Vite production build 成功 |
| npm test / 直接 vitest / Docker Node Vitest | 未完成：分别超时，未返回断言失败结果 |

## 三、已知限制

1. 本机 Maven 默认使用 JDK 8；Java 17 测试通过 Docker 执行。
2. Vitest 在本机 npm、直接 executable 和隔离 Node 容器三条路径均未正常结束，需单独诊断测试运行器或环境。
3. Evidence Panel 只投影当前 Runtime 的 evidence snapshot / graph ref；真实 Evidence Engine 属于 Phase 12。
4. AI Suggestions 来自当前受控 Agent snapshot 或 clinician report，不代表真实 LLM 建议。
5. seed fallback 仅为演示连续性，始终标记 FALLBACK。