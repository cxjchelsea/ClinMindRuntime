# Phase 1 开发任务清单

> 本文档用于跟踪 ClinMindRuntime Phase 1 Runtime MVP 的实现进度。  
> 当前 Phase 1 Runtime Core 采用 Java / Spring Boot。  
> AI / Cursor / Claude Code / Codex 每完成一个实现任务后，必须同步更新本文档。

---

# 一、使用规则

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要人工确认
```

```text
1. 每次只实现一个小任务或一个小模块，不允许一次性实现整个 Phase 1。
2. 每次实现前，先查看当前任务所属阶段。
3. 每次实现后，必须更新任务状态。
4. 如果实现过程中发现设计缺口，可以在“问题记录”中补充，但不要擅自扩大实现范围。
5. 标记完成前，必须有对应代码和 JUnit 测试，或在备注中说明为什么暂时没有测试。
```

---

# 二、MVP-P0-A：Runtime 状态骨架

目标：先让 Java Runtime Core 的状态对象、状态枚举、Trace、AOP Trace 和最小存储能力站起来。

## 2.1 Spring Boot 工程基础

- [x] 创建 `pom.xml`
- [x] 引入 Spring Boot 3.x
- [x] 引入 Spring Web
- [x] 引入 Jakarta Validation
- [x] 引入 Spring AOP
- [x] 引入 JUnit 5 / AssertJ / Mockito
- [x] 创建 `src/main/java/com/clinmind/runtime/ClinMindRuntimeApplication.java`
- [x] 创建 `src/main/resources/application.yml`
- [x] 创建 `src/test/java/com/clinmind/runtime/` 测试目录

## 2.2 Runtime 枚举

- [x] 创建 `state/RuntimeStatus.java`
- [x] 创建 `state/WorkMode.java`
- [x] 创建 `state/RuntimeMode.java`
- [x] 创建 `state/RiskLevel.java`
- [x] 创建 `state/CandidateStatus.java`，包含 `POSSIBLE_AFTER_EXCLUSION`
- [x] 创建 `state/NextActionType.java`
- [x] 创建 `state/OutputLevel.java`
- [x] 编写枚举单元测试

## 2.3 RuntimeState

- [x] 创建 `state/RuntimeState.java`
- [x] 创建 `state/UserInput.java`
- [x] 创建 `state/EntryAssessmentResult.java`
- [x] 创建 `state/CaseFrame.java`
- [x] 创建 `state/KnowledgeContext.java`
- [x] 创建 `state/ExperienceContext.java`
- [x] 创建 `state/SafetyGateResult.java`
- [x] 创建 `state/DifferentialDiagnosisBoard.java`
- [x] 创建 `state/EvidenceGraph.java`
- [x] 创建 `state/QuestionTestPolicyResult.java`
- [x] 创建 `state/DecisionBoundaryResult.java`
- [x] 创建 `state/PatientOutput.java`
- [x] 创建 `state/ClinicianReport.java`
- [x] RuntimeState 包含 `inputHistory` 作为 Phase 1 Short-term Context 降级实现
- [x] 编写 RuntimeState 序列化 / 反序列化测试

## 2.4 RuntimeTrace 与 AOP Trace

- [x] 创建 `state/RuntimeTrace.java`
- [x] 创建 `trace/TraceStep.java`
- [x] 创建 `trace/RuntimeTraceAspect.java`
- [x] AOP Trace 支持记录 moduleName
- [x] AOP Trace 支持记录 runtimeId
- [x] AOP Trace 支持记录耗时
- [x] AOP Trace 支持记录异常信息
- [x] 编写 RuntimeTrace 单元测试
- [x] 编写 TraceStep / RuntimeTraceAspect 基础测试

## 2.5 RuntimeStore

- [x] 创建 `storage/RuntimeStore.java`
- [x] 实现内存版 RuntimeStore
- [x] 实现 `create(RuntimeState state)`
- [x] 实现 `get(String runtimeId)`
- [x] 实现 `update(RuntimeState state)`
- [x] 实现 `exists(String runtimeId)`
- [x] 定义 runtime 不存在时的异常或错误返回
- [x] 编写 RuntimeStore 单元测试

## 2.6 MVP-P0-A 验收

- [x] Spring Boot 工程能启动
- [x] 所有核心模型能正常编译
- [x] RuntimeState 能创建默认对象
- [x] RuntimeState 能 JSON 序列化和反序列化
- [x] RuntimeTrace 能创建并保存关键字段
- [x] RuntimeStore 能完成 create / get / update / exists
- [x] AOP Trace 基础能力可运行
- [x] 运行测试通过

---

# 三、MVP-P0-B：病例结构化与入口判断

目标：让系统可以通过 API 创建 Runtime，并完成入口判断和 CaseFrame 初始化。

## 3.1 Runtime API 基础

- [x] 创建 `api/RuntimeController.java`
- [x] 创建统一响应对象 `ApiResponse`
- [x] 实现 `POST /api/v1/runtime/start`
- [x] 实现 `POST /api/v1/runtime/continue`
- [x] 实现 `GET /api/v1/runtime/{runtime_id}/status`
- [x] 实现 `GET /api/v1/runtime/{runtime_id}/result`
- [x] 实现 `GET /api/v1/runtime/{runtime_id}/trace`
- [x] 统一使用 `runtime_id`，不要使用 `runtimeId`
- [x] 实现基础错误码
- [x] 编写 RuntimeController 测试

## 3.2 EntryAssessmentService

- [x] 创建 `entry/EntryAssessmentService.java`
- [x] 实现 `assessEntry(UserInput input, Map<String, Object> basicInfo)`
- [x] 支持 `WELLNESS_MODE`
- [x] 支持 `CLINICAL_MODE`
- [x] 支持 `EMERGENCY_HINT`
- [x] 支持 `UNSUPPORTED`
- [x] emergency_hint 不直接设置 `SAFETY_GATE_TRIGGERED`，只标记 workMode
- [x] 编写 EntryAssessmentService 单元测试

## 3.3 CaseFrameService

- [x] 创建 `caseframe/CaseFrameService.java`
- [x] 实现 `buildOrUpdateCaseFrame(...)`
- [x] 支持主诉提取
- [x] 支持基础症状提取
- [x] 支持 basicInfo 写入 patientProfile
- [x] 支持 missingSlots 生成
- [x] 编写 CaseFrameService 单元测试

## 3.4 MVP-P0-B 验收

- [x] start API 能创建 Runtime
- [x] continue API 能读取并更新 Runtime
- [x] EntryAssessment 结果能写入 RuntimeState
- [x] CaseFrame 能写入 RuntimeState
- [x] runtime_id 不存在时返回统一错误
- [x] API 基础测试通过

---

# 四、MVP-P0-C：安全门和候选诊断

目标：让系统能读取静态规则，识别风险，并构建候选诊断状态板。

## 4.1 StaticRuleProvider

- [x] 创建 `knowledge/StaticRuleProvider.java`
- [x] 创建 `src/main/resources/assets/symptom-groups/chest-pain.yml`
- [x] 创建 `src/main/resources/assets/symptom-groups/fever.yml`
- [x] 创建 `src/main/resources/assets/red-flag-rules.yml`
- [x] 创建 `src/main/resources/assets/test-recommendation-rules.yml`
- [x] 创建 `src/main/resources/assets/capability-profiles.yml`
- [x] 实现症状群规则读取
- [x] 实现危险信号规则读取
- [x] 实现检查建议规则读取
- [x] 实现静态 Capability Profile 读取
- [x] 编写规则读取测试

## 4.2 KnowledgeContextService

- [x] 创建 `knowledge/KnowledgeContextService.java`
- [x] 实现 `buildKnowledgeContext(...)`
- [x] 将静态规则聚合为 KnowledgeContext
- [x] 记录 sourceAssets
- [x] 编写 KnowledgeContextService 单元测试

## 4.3 ExperienceContextService

- [x] 创建 `experience/ExperienceContextService.java`
- [x] 实现空 ExperienceContext
- [x] 可选实现 mock ExperienceContext
- [x] 明确不接入真实 Clinical Experience Memory
- [x] 编写 ExperienceContextService 单元测试

## 4.4 SafetyGateService

- [x] 创建 `safety/SafetyGateService.java`
- [x] 实现 `evaluateSafety(...)`
- [x] 支持危险信号规则匹配
- [x] 命中高风险时设置输出限制
- [x] SafetyGate 失败时进入保守策略
- [x] 使用 `@TraceStep("SafetyGate")`
- [x] 编写 SafetyGateService 单元测试

## 4.5 DifferentialDiagnosisBoardService

- [x] 创建 `reasoning/DifferentialDiagnosisBoardService.java`
- [x] 实现 `buildDifferentialBoard(...)`
- [x] 支持 commonDiagnoses
- [x] 支持 mustNotMiss
- [x] 支持 `NEED_TO_RULE_OUT`
- [x] 支持 `POSSIBLE_AFTER_EXCLUSION`
- [x] 高风险候选不能被删除
- [x] 使用 `@TraceStep("DifferentialDiagnosisBoard")`
- [x] 编写 DDx Board 单元测试

## 4.6 MVP-P0-C 验收

- [x] 静态规则能被读取
- [x] KnowledgeContext 能生成
- [x] ExperienceContext 能以空实现参与链路
- [x] SafetyGate 能识别配置好的高风险规则
- [x] DDx Board 能生成候选诊断状态
- [x] 高风险候选保留为 MUST_NOT_MISS 或 NEED_TO_RULE_OUT

---

# 五、MVP-P0-D：证据图与下一步动作

目标：让系统能根据候选诊断和缺失证据决定下一步追问或检查建议。

## 5.1 EvidenceGraphService

- [x] 创建 `reasoning/EvidenceGraphService.java`
- [x] 实现 `buildEvidenceGraph(...)`
- [x] 支持 supportingEvidence
- [x] 支持 opposingEvidence
- [x] 支持 missingEvidence
- [x] 支持 nextQuestions
- [x] 支持 recommendedTests
- [x] 使用 `@TraceStep("EvidenceGraph")`
- [x] 编写 EvidenceGraphService 单元测试

## 5.2 QuestionTestPolicyService

- [x] 创建 `reasoning/QuestionTestPolicyService.java`
- [x] 实现 `decideNextAction(...)`
- [x] 高风险优先
- [x] 缺失证据优先
- [x] 支持 ASK_QUESTION
- [x] 支持 RECOMMEND_TEST
- [x] 支持 RECOMMEND_VISIT
- [x] 使用 `@TraceStep("QuestionTestPolicy")`
- [x] 编写 QuestionTestPolicyService 单元测试

## 5.3 MVP-P0-D 验收

- [x] EvidenceGraph 不只是解释文本，而能影响下一步动作
- [x] 缺失证据能触发追问
- [x] 高风险候选能触发检查或就医评估建议
- [x] Question / Test Policy 输出结构化 NextAction

---

# 六、MVP-P0-E：输出边界与分角色表达

目标：让系统能够根据风险和能力边界区分患者端与医生端输出。

## 6.1 DecisionBoundaryService

- [x] 创建 `boundary/DecisionBoundaryService.java`
- [x] 创建 `boundary/CapabilityProfileProvider.java`
- [x] 实现 `decideOutputBoundary(...)`
- [x] 读取静态 Capability Profile
- [x] 高风险未排除时禁止低风险安抚
- [x] 患者端默认不展示完整候选诊断
- [x] 医生端允许展示 DDx 和 EvidenceGraph
- [x] 使用 `@TraceStep("DecisionBoundary")`
- [x] 编写 DecisionBoundaryService 单元测试

## 6.2 PatientOutputService

- [x] 创建 `output/PatientOutputService.java`
- [x] 实现 `buildPatientOutput(...)`
- [x] 输出继续追问
- [x] 输出风险提示
- [x] 输出线下评估建议
- [x] 禁止确定诊断
- [x] 禁止处方建议
- [x] 编写 PatientOutputService 单元测试

## 6.3 ClinicianReportService

- [x] 创建 `output/ClinicianReportService.java`
- [x] 实现 `buildClinicianReport(...)`
- [x] 展示 CaseFrame Summary
- [x] 展示 SafetyGate Result
- [x] 展示 DDx Board
- [x] 展示 EvidenceGraph
- [x] 展示 Recommended Questions / Tests
- [x] 编写 ClinicianReportService 单元测试

## 6.4 FailurePolicyService

- [x] 创建 `boundary/FailurePolicyService.java`
- [x] 实现安全模块失败时的保守输出
- [x] SafetyGate 失败时进入 `ERROR_SAFE_HALTED`
- [x] DecisionBoundary 失败时进入 `ERROR_SAFE_HALTED`
- [x] 编写 FailurePolicyService 单元测试

## 6.5 MVP-P0-E 验收

- [x] PatientOutput 和 ClinicianReport 明确分离
- [x] 所有患者端输出经过 DecisionBoundary
- [x] 高风险场景不会输出低风险安抚
- [x] 医生端可以看到候选和证据图
- [x] 患者端看不到医生端完整内容

---

# 七、MVP-P0-F：最小测试集与集成验证

目标：用测试病例验证完整 Runtime MVP 闭环。

## 7.1 测试病例

- [x] 创建 `src/test/resources/cases/chest-pain-cases.yml`
- [x] 创建 `src/test/resources/cases/fever-cases.yml`
- [x] 至少 5 个胸痛 / 胸闷病例
- [x] 至少 5 个发热病例
- [x] 至少包含普通病例
- [x] 至少包含高风险病例
- [x] 至少包含信息缺失病例
- [x] 至少包含误导表达病例

## 7.2 集成测试

- [x] 创建 `RuntimeFlowIntegrationTest`
- [x] 测试 start API 完整链路
- [x] 测试 continue API 完整链路
- [x] 测试高风险链路
- [x] 测试患者端 / 医生端输出分离
- [x] 测试 RuntimeTrace 记录
- [x] 测试静态规则替换不影响核心链路
- [x] 测试 continue 轮次 Trace 不含 EntryAssessment
- [x] 测试 WELLNESS_MODE 不进入临床管线
- [x] 测试 DecisionBoundary fail-safe 进入 ERROR_SAFE_HALTED
- [x] 测试患者端 API 不泄露诊断字段

## 7.3 Phase 1 总体验收

- [x] 10–20 个测试病例可以跑通
- [x] 高风险病例不会输出低风险安抚
- [x] 信息缺失病例会继续追问
- [x] 医生端可以看到结构化候选和证据图
- [x] 患者端和医生端输出不同（含 knowledge_context / next_action 诊断字段隔离）
- [x] RuntimeTrace 基于 AOP 实际执行模块，不再虚报未运行步骤
- [x] 静态规则读取失败触发 fail-safe，不再 fail-open
- [x] WELLNESS_MODE 保持 wellness 状态，不跑临床管线
- [x] 所有测试通过
- [x] 人工 Postman API 验收通过（见 7.4、`docs/API测试.md`）

## 7.4 人工 API 验收（Postman）

| 项目 | 内容 |
|---|---|
| 验收日期 | 2026-06-26 |
| 验收方式 | `java -jar target/clinmind-runtime-0.1.0-SNAPSHOT.jar` 启动后，Postman 调用 REST API |
| 服务地址 | `http://localhost:8080` |
| 运行环境 | Java 21.0.9（Temurin），Spring Boot 3.3.5 |
| 详细记录 | [`docs/API测试.md`](API测试.md) |
| 代码基线 | commit `2abe52d`（验收漏洞修复） |

| 用例 | 场景 | 结论 |
|---|---|---|
| 1 | 患者端 · 普通胸痛 | ✅ 通过 |
| 2 | 患者端 · 高风险胸痛 | ✅ 通过 |
| 3 | 医生端 · 可见 DDx / 证据图 | ✅ 通过 |
| 4 | continue + `/trace` 模块真实性 | ✅ 通过 |
| 5 | WELLNESS_MODE 不进临床管线 | ✅ 通过 |
| 6 | `/status` `/result` `/trace` / 404 | ✅ 通过 |

**验收结论：** Phase 1 后端 MVP 人工 API 验收通过，可与自动化测试（`mvn test`）一并作为 Phase 1 正式结项依据。

**Phase 2 遗留观察（不阻塞结项）：**

- `/result` 对患者端 session 仍返回 `clinician_report`（`allowed: false`），Phase 2 可按角色统一过滤
- 高风险场景 `constraints_applied` 中 `no_low_risk_reassurance` 可能重复
- `wellness_mode` 暂无专用 `patient_output` 引导文案

---

# 八、问题记录

| 编号 | 问题 | 影响模块 | 状态 | 处理结论 |
|---|---|---|---|---|
| Q1 | RuntimeStore Trace 并发追加非线程安全 | storage | 待 Phase 2 | Phase 1 单线程 MVP 可接受 |
| Q2 | version 字段未用于乐观锁 | storage | 待 Phase 2 | 当前内存 Store 无并发写 |
| Q3 | CaseFrame「胸痛」同时命中 chest_discomfort / chest_pain | caseframe | 待优化 | 不影响 P0 验收主路径 |
| Q4 | StaticRuleProvider 未抽象为 MedicalKnowledgeProvider | knowledge | 待 Phase 2 | 已通过 @Primary 替换测试验证可替换性 |

---

# 九、变更记录

| 日期 | 变更 | 说明 |
|---|---|---|
| 2026-06-25 | 创建任务清单 | 用于约束 Phase 1 实现进度 |
| 2026-06-25 | 调整为 Java Runtime Core | Phase 1 Runtime Core 改为 Java / Spring Boot，Python 作为后续可选 Provider |
| 2026-06-25 | 完成 Python MVP-P0-A 原型 | `app/` 下已实现 Runtime 状态骨架原型，待迁移至 Java Runtime Core |
| 2026-06-25 | 完成 Python MVP-P0-B 原型 | `app/` 下已实现 Runtime API、EntryAssessment 与 CaseFrame 原型，待迁移至 Java Runtime Core |
| 2026-06-25 | 完成 Java MVP-P0-A | Spring Boot 状态骨架、RuntimeStore、AOP Trace 及 JUnit 测试 |
| 2026-06-25 | 完成 Java MVP-P0-E | DecisionBoundary、PatientOutput、ClinicianReport、FailurePolicy 及分角色 API |
| 2026-06-25 | 完成 Java MVP-P0-F | 12 个 YAML 集成病例与 RuntimeFlowIntegrationTest，Phase 1 MVP 闭环验收通过 |
| 2026-06-26 | Phase 1 验收漏洞修复 | 封堵患者端诊断泄漏；AOP Trace 与 /trace 合并；StaticRule fail-closed；WELLNESS_MODE 隔离；DecisionBoundary fail-safe → ERROR_SAFE_HALTED；补充替换规则与专项集成测试 |
| 2026-06-26 | Phase 1 人工 Postman 验收通过 | 6 条 API 冒烟用例全部通过；记录见 `docs/API测试.md`；Phase 1 后端 MVP 正式结项 |
