# Phase 1 模块接口设计

> 本文档是 Phase 1 Runtime MVP 的低层设计文档之一。  
> 它负责定义 Phase 1 Java Runtime Core 的模块职责、上下游关系、输入输出、核心接口和失败策略。  
> 数据结构见 `Phase1_数据结构与状态设计.md`，技术栈见 `Phase1_技术栈与工程架构决策.md`。

---

# 一、模块设计原则

```text
1. 每个模块只处理一个清晰职责。
2. 模块之间通过 Java 结构化对象传递信息，而不是通过长 Prompt 隐式传递。
3. 所有模块都围绕 RuntimeState 读写。
4. 关键模块输出必须写入 RuntimeTrace。
5. 医疗安全相关模块失败时，必须进入保守策略。
6. Phase 1 允许静态规则和 mock 实现，但接口要为后续扩展保留。
7. Spring AOP 只负责横切追踪，不能替代业务状态更新。
```

---

# 二、Phase 1 模块执行顺序

```text
RuntimeController
  ↓
RuntimeStore
  ↓
EntryAssessmentService
  ↓
CaseFrameService
  ↓
StaticRuleProvider
  ↓
KnowledgeContextService
  ↓
ExperienceContextService
  ↓
SafetyGateService
  ↓
DifferentialDiagnosisBoardService
  ↓
EvidenceGraphService
  ↓
QuestionTestPolicyService
  ↓
DecisionBoundaryService
  ↓
PatientOutputService / ClinicianReportService
  ↓
RuntimeTraceAspect / RuntimeTraceStore
```

---

# 三、模块总览表

| 模块 | 主职责 | 主要输入 | 主要输出 |
|---|---|---|---|
| RuntimeController | 接收请求、返回 Runtime 结果 | HTTP 请求 | ApiResponse |
| RuntimeStore | 保存和读取 RuntimeState | runtimeId、RuntimeState | RuntimeState |
| EntryAssessmentService | 判断入口工作态 | UserInput、basicInfo | EntryAssessmentResult |
| CaseFrameService | 构建病例状态 | UserInput、旧 CaseFrame | CaseFrame |
| StaticRuleProvider | 读取静态规则 | symptomGroup | 静态规则对象 |
| KnowledgeContextService | 生成知识上下文 | CaseFrame、规则对象 | KnowledgeContext |
| ExperienceContextService | 生成经验上下文 | CaseFrame、KnowledgeContext | ExperienceContext |
| SafetyGateService | 识别危险信号 | CaseFrame、KnowledgeContext、ExperienceContext | SafetyGateResult |
| DifferentialDiagnosisBoardService | 构建候选诊断状态板 | CaseFrame、KnowledgeContext、SafetyGateResult | DifferentialDiagnosisBoard |
| EvidenceGraphService | 构建证据图 | CaseFrame、DDxBoard、KnowledgeContext | EvidenceGraph |
| QuestionTestPolicyService | 决定下一步动作 | EvidenceGraph、SafetyGateResult | QuestionTestPolicyResult |
| DecisionBoundaryService | 控制输出边界 | RuntimeState 中关键状态 | DecisionBoundaryResult |
| PatientOutputService | 生成患者端安全表达 | DecisionBoundary、NextAction | PatientOutput |
| ClinicianReportService | 生成医生端报告 | CaseFrame、DDx、EvidenceGraph | ClinicianReport |
| RuntimeTraceAspect | 横切记录模块执行 | 注解方法输入输出 | TraceStep 记录 |
| FailurePolicyService | 处理失败策略 | 异常、模块名、RuntimeState | 保守结果 / ERROR_SAFE_HALTED |

---

# 四、RuntimeController

## 4.1 职责

```text
1. 接收 start / continue / status / result / trace 请求。
2. 创建或读取 RuntimeState。
3. 调用 Runtime 执行链路。
4. 返回患者端或医生端结果。
```

## 4.2 核心接口

```java
ApiResponse<RuntimeResponse> startRuntime(StartRuntimeRequest request);

ApiResponse<RuntimeResponse> continueRuntime(ContinueRuntimeRequest request);

ApiResponse<RuntimeStatusResponse> getRuntimeStatus(String runtimeId);

ApiResponse<RuntimeResultResponse> getRuntimeResult(String runtimeId);

ApiResponse<RuntimeTraceResponse> getRuntimeTrace(String runtimeId);
```

## 4.3 读写字段

```text
读取：HTTP request
写入：RuntimeState 基础字段、inputHistory、RuntimeTrace
```

---

# 五、RuntimeStore

## 5.1 职责

保存、读取、更新 RuntimeState。

Phase 1 使用内存版实现，后续可替换为数据库或 Redis。

## 5.2 核心接口

```java
RuntimeState create(RuntimeState state);

RuntimeState get(String runtimeId);

RuntimeState update(RuntimeState state);

boolean exists(String runtimeId);
```

## 5.3 失败策略

```text
runtimeId 不存在：返回统一错误 RUNTIME_NOT_FOUND。
状态读取失败：不得继续生成输出。
状态更新失败：不得继续生成输出。
```

---

# 六、EntryAssessmentService

## 6.1 职责

判断用户输入是否进入临床问诊 Runtime，以及建议的症状群。

## 6.2 输入

```text
UserInput
basicInfo
旧 RuntimeState，可选
```

## 6.3 输出

```text
EntryAssessmentResult
```

## 6.4 核心接口

```java
EntryAssessmentResult assessEntry(UserInput input, Map<String, Object> basicInfo);
```

## 6.5 Phase 1 实现策略

```text
先用关键词和规则判断。
可选使用后续 AI Provider 辅助分类，但不能让 Provider 结果直接绕过规则。
```

## 6.6 写入 RuntimeState

```text
entryAssessment
workMode
runtimeStatus
```

---

# 七、CaseFrameService

## 7.1 职责

把用户输入转换为结构化病例状态，并在多轮对话中更新已有 CaseFrame。

## 7.2 输入

```text
UserInput
existing CaseFrame
basicInfo
```

## 7.3 输出

```text
CaseFrame
```

## 7.4 核心接口

```java
CaseFrame buildOrUpdateCaseFrame(
    UserInput input,
    CaseFrame existingCaseFrame,
    Map<String, Object> basicInfo
);
```

## 7.5 写入 RuntimeState

```text
caseFrame
```

## 7.6 失败策略

```text
抽取失败时保留原 CaseFrame，并将关键信息加入 missingSlots。
不能因为抽取失败而输出诊断方向。
```

---

# 八、StaticRuleProvider

## 8.1 职责

从 `src/main/resources/assets/` 中读取症状群规则、危险信号规则、检查建议规则和静态能力档案。

## 8.2 核心接口

```java
SymptomGroupRule loadSymptomGroupRules(String symptomGroup);

List<RedFlagRule> loadRedFlagRules(String symptomGroup);

List<TestRecommendationRule> loadTestRecommendationRules(String symptomGroup);

CapabilityProfile loadCapabilityProfile(String symptomGroup);
```

## 8.3 失败策略

```text
规则缺失：KnowledgeContext 标记 sourceAssets 为空。
危险信号规则读取失败：SafetyGate 应进入 fail-safe。
能力档案读取失败：DecisionBoundary 默认收紧输出。
```

---

# 九、KnowledgeContextService

## 9.1 职责

根据 CaseFrame 和症状群，聚合 Phase 1 静态医学规则。

## 9.2 输入

```text
CaseFrame
EntryAssessmentResult
StaticRuleProvider
```

## 9.3 输出

```text
KnowledgeContext
```

## 9.4 核心接口

```java
KnowledgeContext buildKnowledgeContext(
    CaseFrame caseFrame,
    EntryAssessmentResult entryAssessment
);
```

## 9.5 写入 RuntimeState

```text
knowledgeContext
```

---

# 十、ExperienceContextService

## 10.1 职责

为 Phase 1 保留经验上下文接口。

## 10.2 输入

```text
CaseFrame
KnowledgeContext
```

## 10.3 输出

```text
ExperienceContext
```

## 10.4 核心接口

```java
ExperienceContext buildExperienceContext(
    CaseFrame caseFrame,
    KnowledgeContext knowledgeContext
);
```

## 10.5 Phase 1 实现策略

```text
默认 empty 实现：返回空经验。
可选 mock 实现：返回少量手写经验提醒，用于验证 Trace 和边界链路。
不接入真实 Clinical Experience Memory。
```

---

# 十一、SafetyGateService

## 11.1 职责

识别危险信号，并给出风险等级和输出限制。

## 11.2 输入

```text
CaseFrame
KnowledgeContext.redFlags
ExperienceContext
CapabilityProfile
```

## 11.3 输出

```text
SafetyGateResult
```

## 11.4 核心接口

```java
@TraceStep("SafetyGate")
SafetyGateResult evaluateSafety(RuntimeState state);
```

## 11.5 写入 RuntimeState

```text
safetyGate
runtimeStatus，可选更新为 SAFETY_GATE_TRIGGERED 或 ERROR_SAFE_HALTED
```

## 11.6 Trace 记录点

```text
matchedRules
riskLevel
requiredAction
patientOutputConstraint
```

## 11.7 失败策略

```text
SafetyGate 失败时必须 fail-safe。
不得在 SafetyGate 失败后继续生成诊断方向。
```

---

# 十二、DifferentialDiagnosisBoardService

## 12.1 职责

构建候选诊断状态板，保留高风险候选，不提前给出唯一诊断。

## 12.2 输入

```text
CaseFrame
KnowledgeContext.commonDiagnoses
KnowledgeContext.mustNotMiss
SafetyGateResult
```

## 12.3 输出

```text
DifferentialDiagnosisBoard
```

## 12.4 核心接口

```java
@TraceStep("DifferentialDiagnosisBoard")
DifferentialDiagnosisBoard buildDifferentialBoard(RuntimeState state);
```

## 12.5 约束

```text
高风险候选必须保留为 MUST_NOT_MISS 或 NEED_TO_RULE_OUT。
患者端是否可见由 DecisionBoundary 决定。
```

---

# 十三、EvidenceGraphService

## 13.1 职责

为每个候选诊断组织支持证据、反对证据和缺失证据。

## 13.2 输入

```text
CaseFrame
DifferentialDiagnosisBoard
KnowledgeContext
ExperienceContext
```

## 13.3 输出

```text
EvidenceGraph
```

## 13.4 核心接口

```java
@TraceStep("EvidenceGraph")
EvidenceGraph buildEvidenceGraph(RuntimeState state);
```

## 13.5 约束

```text
EvidenceGraph 不是解释层，而是控制层。
QuestionTestPolicyService 必须读取 EvidenceGraph 的 missingEvidence。
```

---

# 十四、QuestionTestPolicyService

## 14.1 职责

根据 EvidenceGraph 和 SafetyGate 决定下一步追问、检查建议或就医评估建议。

## 14.2 输入

```text
EvidenceGraph
SafetyGateResult
KnowledgeContext.requiredQuestions
KnowledgeContext.recommendedTests
```

## 14.3 输出

```text
QuestionTestPolicyResult
```

## 14.4 核心接口

```java
@TraceStep("QuestionTestPolicy")
QuestionTestPolicyResult decideNextAction(RuntimeState state);
```

## 14.5 优先级

```text
危险信号
> 高风险候选排除
> 关键缺失证据
> 检查建议
> 普通病史补全
```

---

# 十五、DecisionBoundaryService

## 15.1 职责

根据风险、证据状态、能力档案和运行模式决定当前允许输出什么。

## 15.2 输入

```text
RuntimeMode
CapabilityProfile
SafetyGateResult
DifferentialDiagnosisBoard
EvidenceGraph
QuestionTestPolicyResult
FailurePolicy 状态
```

## 15.3 输出

```text
DecisionBoundaryResult
```

## 15.4 核心接口

```java
@TraceStep("DecisionBoundary")
DecisionBoundaryResult decideOutputBoundary(RuntimeState state);
```

## 15.5 失败策略

```text
DecisionBoundary 失败时必须 fail-safe。
患者端默认不允许诊断标签。
```

---

# 十六、PatientOutputService

## 16.1 职责

在 DecisionBoundary 允许的范围内生成患者端安全表达。

## 16.2 输入

```text
DecisionBoundaryResult
QuestionTestPolicyResult
SafetyGateResult
```

## 16.3 输出

```text
PatientOutput
```

## 16.4 核心接口

```java
PatientOutput buildPatientOutput(RuntimeState state);
```

## 16.5 约束

```text
不能输出确定诊断。
不能输出处方。
不能在高风险未排除时输出低风险安抚。
必须解释为什么继续追问或建议就医评估。
```

---

# 十七、ClinicianReportService

## 17.1 职责

生成医生端结构化报告。

## 17.2 输入

```text
CaseFrame
SafetyGateResult
DifferentialDiagnosisBoard
EvidenceGraph
QuestionTestPolicyResult
DecisionBoundaryResult
```

## 17.3 输出

```text
ClinicianReport
```

## 17.4 核心接口

```java
ClinicianReport buildClinicianReport(RuntimeState state);
```

## 17.5 约束

```text
医生端可以展示候选诊断和证据状态。
必须标记不确定性和缺失证据。
不能把医生端内容泄露到患者端。
```

---

# 十八、RuntimeTraceAspect

## 18.1 职责

使用 Spring AOP 横切记录模块执行过程。

## 18.2 注解

```java
@TraceStep("ModuleName")
```

## 18.3 记录内容

```text
runtimeId
moduleName
inputSummary
outputSummary
startTime
endTime
durationMs
success
errorMessage
```

## 18.4 约束

```text
AOP Trace 不能替代业务模块显式写回 RuntimeState。
它只负责横切追踪、耗时统计、异常记录和审计辅助。
```

---

# 十九、FailurePolicyService

## 19.1 职责

统一处理关键模块失败时的保守策略。

## 19.2 核心接口

```java
RuntimeState handleFailure(
    String moduleName,
    Exception error,
    RuntimeState state
);
```

## 19.3 规则

```text
SafetyGate 失败：进入 ERROR_SAFE_HALTED。
DecisionBoundary 失败：进入 ERROR_SAFE_HALTED。
EvidenceGraph 失败：不得输出候选诊断。
KnowledgeContext 失败：不得引用知识来源。
PatientOutput 失败：返回结构化保守提示。
```

---

# 二十、模块接口完成标准

```text
1. 每个模块都有明确输入输出。
2. 每个模块读写 RuntimeState 字段清楚。
3. 安全相关模块有 fail-safe 策略。
4. AOP Trace 记录点清楚。
5. RuntimeTrace 记录关键判断。
6. Phase 1 可先使用规则和 mock，但接口可以无缝升级到 Phase 2–4。
```
