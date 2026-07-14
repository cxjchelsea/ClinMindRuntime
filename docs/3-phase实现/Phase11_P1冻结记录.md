# Phase 11-P1 冻结记录

> 记录日期：2026-07-14  
> 冻结状态：**BLOCKED / NOT FROZEN**  
> 原因：Vitest 全量验证未获得完成结果；按照 Phase 11-P1 冻结规则，不以路线图状态替代测试证据。

## 一、实际实现

- PatientViewSource / ClinicianViewSource 抽象。
- RuntimeStoreViewSource 真实 Runtime 主路径。
- DemoRuntimeSeedViewSource 显式 FALLBACK。
- RoleSpecificViewSource Runtime 优先、seed 后备。
- Patient Runtime view、safe summary、session list。
- Clinician case list、workspace、report draft。
- Policy、sanitizer、read/reject audit。
- 前端 Patient / Clinician API-first client、hooks 与显式 demo fallback。
- DemoRole 与 DebugRole 同步。

## 二、PARTIAL 能力

以下真实 Runtime 段缺失时返回 PARTIAL，并在 missing_sections 中说明：

- Patient：CaseFrame、PatientOutput、DecisionBoundary、SafetyGate。
- Clinician：CaseFrame、ClinicianReport、DecisionBoundary、DDx Board、Evidence Panel、Inquiry Timeline、AI Suggestions。

## 三、fallback

- RuntimeStore 无对应数据时使用 DemoRuntimeSeedViewSource。
- fallback DTO 的 projection_status 必须为 FALLBACK。
- fallback 状态通过现有 projection read audit 记录。
- fallback 不得描述为真实业务或真实临床数据。

## 四、不属于 Phase 11

- 真实 LLM-backed Agent。
- Phase 12 Clinical Evidence Engine、真实 BM25/Embedding/Rerank/Citation Verification。
- 真实 FHIR/EHR 接入。
- 生产认证、患者注册、医生提交、报告保存。
- 处方、剂量、转诊、预约、支付和外部写操作。
- Candidate approve/reject/publish 与治理写操作。

## 五、验证结果

- Java 全量测试：559 tests，0 failures，0 errors，23 skipped（Docker JDK17）；其中 P11-P1 定向测试 11/11 通过。
- Python：10/10 通过。
- TypeScript：通过。
- 前端生产构建：通过。
- Vitest：BLOCKED，三种运行方式超时，未获得完整结果。
- 浏览器人工交互：未执行。

## 六、已知限制

- 本机默认 JDK 8，不符合项目 Java 17 要求。
- 当前 evidence 与 agent 内容仍可能来自规则、mock 或 fallback provider；视图只保证来源于 Runtime snapshot，不宣称外部能力真实。
- Vitest runner/环境问题尚待定位。

## 七、冻结判定与下一阶段入口

Phase 11-P1 当前**不得标记为已冻结**。解除阻塞必须：

1. Vitest 全量套件完成并通过；
2. 完成 Patient / Clinician API-first 浏览器人工验证；
3. 将结果回写人工测试记录；
4. 将本记录状态改为 FROZEN，并同步 README、项目设计地图与 AI_IMPLEMENTATION_SKILL。

完成上述条件后，下一阶段入口为 Phase 12-P0 Clinical Evidence Engine 实现规格、API/测试设计和任务清单。