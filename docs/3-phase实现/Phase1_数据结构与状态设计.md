# Phase 1 数据结构与状态设计

> 本文档是 Phase 1 Runtime MVP 的低层设计文档之一，负责定义 Runtime MVP 的核心数据结构、枚举、状态流转和字段读写关系。  
> 当前 Phase 1 Runtime Core 采用 Java / Spring Boot。数据结构应以 Java enum / record / class 表达。

---

# 一、设计原则

```text
1. RuntimeState 是诊断状态唯一事实源。
2. 对话历史只服务语言连续性，不能替代结构化病例状态。
3. 每个模块只读写明确字段。
4. 关键判断必须写入 RuntimeTrace。
5. 患者端输出和医生端输出必须结构化分离。
6. Phase 1 先保证可运行、可测试、可扩展。
```

Phase 1 的数据结构是完整系统数据结构的最小可运行子集。总设计文档中的字段如果没有在 Phase 1 中完整展开，不代表被取消，而是留到 Phase 2–5 扩展。

---

# 二、核心枚举

Java 枚举命名建议使用大写下划线。

## 2.1 RuntimeStatus

```java
CREATED
ENTRY_ASSESSING
WELLNESS_MODE
CLINICAL_MODE
COLLECTING_CASE_INFO
SAFETY_GATE_TRIGGERED
BUILDING_DIFFERENTIAL
COLLECTING_EVIDENCE
RECOMMENDING_TESTS
WAITING_FOR_USER
READY_FOR_PATIENT_OUTPUT
READY_FOR_CLINICIAN_REPORT
COMPLETED
ERROR_SAFE_HALTED
```

说明：

```text
EMERGENCY_HINT 不是 RuntimeStatus，而是 EntryAssessmentResult.workMode 的一种取值。
EntryAssessment 识别到 EMERGENCY_HINT 时，RuntimeStatus 进入 CLINICAL_MODE，并在下一步优先执行 SafetyGate。
只有 SafetyGate 真正命中危险信号后，RuntimeStatus 才进入 SAFETY_GATE_TRIGGERED。
```

## 2.2 WorkMode

```java
WELLNESS_MODE
CLINICAL_MODE
EMERGENCY_HINT
UNSUPPORTED
```

## 2.3 RuntimeMode

```java
PATIENT_FACING
CLINICIAN_COPILOT
DEBUG
```

## 2.4 RiskLevel

```java
NONE
LOW
MEDIUM
MEDIUM_HIGH
HIGH
UNKNOWN
```

## 2.5 CandidateStatus

```java
PRIMARY_HYPOTHESIS
MAIN_ALTERNATIVE
MUST_NOT_MISS
NEED_TO_RULE_OUT
POSSIBLE
POSSIBLE_AFTER_EXCLUSION
UNLIKELY
INSUFFICIENT_EVIDENCE
```

## 2.6 NextActionType

```java
ASK_QUESTION
RECOMMEND_TEST
RECOMMEND_VISIT
WAIT_FOR_USER
GENERATE_PATIENT_OUTPUT
GENERATE_CLINICIAN_REPORT
SAFE_HALT
```

## 2.7 OutputLevel

```java
O1_CONTINUE_QUESTIONING
O2_RISK_HINT
O3_CLINICIAN_CANDIDATE_DIAGNOSIS
O4_LOW_RISK_REFERENCE
O5_VISIT_OR_URGENT_CARE_RECOMMENDATION
O6_TRANSFER_TO_DOCTOR
O7_CLINICIAN_FULL_REPORT
```

---

# 三、RuntimeStatus 状态流转

## 3.1 主状态流转

```text
CREATED
  ↓
ENTRY_ASSESSING
  ↓
WELLNESS_MODE / CLINICAL_MODE / ERROR_SAFE_HALTED
  ↓
COLLECTING_CASE_INFO
  ↓
BUILDING_DIFFERENTIAL
  ↓
COLLECTING_EVIDENCE
  ↓
SAFETY_GATE_TRIGGERED / RECOMMENDING_TESTS / WAITING_FOR_USER / READY_FOR_PATIENT_OUTPUT / READY_FOR_CLINICIAN_REPORT
  ↓
COMPLETED
```

## 3.2 状态迁移规则

| 当前状态 | 触发条件 | 下一状态 |
|---|---|---|
| CREATED | 调用 start API | ENTRY_ASSESSING |
| ENTRY_ASSESSING | 判断为健康咨询 | WELLNESS_MODE |
| ENTRY_ASSESSING | 判断为临床问诊 | CLINICAL_MODE |
| ENTRY_ASSESSING | 判断为疑似高风险 | CLINICAL_MODE，并将 workMode 标记为 EMERGENCY_HINT，下一步优先执行 SafetyGate |
| ENTRY_ASSESSING | 判断为 unsupported | ERROR_SAFE_HALTED |
| CLINICAL_MODE | 病例信息不足 | COLLECTING_CASE_INFO |
| COLLECTING_CASE_INFO | 已识别症状群 | BUILDING_DIFFERENTIAL |
| BUILDING_DIFFERENTIAL | 候选诊断构建完成 | COLLECTING_EVIDENCE |
| COLLECTING_EVIDENCE | SafetyGate 真正命中危险信号 | SAFETY_GATE_TRIGGERED |
| COLLECTING_EVIDENCE | 需要继续补充信息 | WAITING_FOR_USER |
| COLLECTING_EVIDENCE | 需要建议检查或就医评估 | RECOMMENDING_TESTS |
| COLLECTING_EVIDENCE | 可生成患者端安全输出 | READY_FOR_PATIENT_OUTPUT |
| COLLECTING_EVIDENCE | 可生成医生端报告 | READY_FOR_CLINICIAN_REPORT |
| SAFETY_GATE_TRIGGERED | DecisionBoundary 允许风险提示或就医建议 | READY_FOR_PATIENT_OUTPUT 或 RECOMMENDING_TESTS |
| 任意状态 | 安全模块或边界模块失败 | ERROR_SAFE_HALTED |

---

# 四、RuntimeState

Java 类建议：

```text
src/main/java/com/clinmind/runtime/state/RuntimeState.java
```

字段建议：

| 字段 | Java 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| runtimeId | String | 是 | UUID | Runtime 唯一标识 |
| sessionId | String | 是 | 无 | 会话 ID |
| userId | String | 否 | null | 用户 ID |
| version | int | 是 | 1 | 状态版本号 |
| runtimeStatus | RuntimeStatus | 是 | CREATED | 当前状态 |
| workMode | WorkMode | 否 | null | 入口工作态 |
| mode | RuntimeMode | 是 | PATIENT_FACING | 运行模式 |
| inputHistory | List<UserInput> | 是 | empty list | 输入历史，Phase 1 暂时代替 Short-term Context |
| entryAssessment | EntryAssessmentResult | 否 | null | 入口判断结果 |
| caseFrame | CaseFrame | 是 | empty object | 病例结构化状态 |
| knowledgeContext | KnowledgeContext | 是 | empty object | 医学知识上下文 |
| experienceContext | ExperienceContext | 是 | empty object | 经验上下文 |
| safetyGate | SafetyGateResult | 否 | null | 危险信号判断 |
| differentialBoard | DifferentialDiagnosisBoard | 是 | empty object | 候选诊断状态板 |
| evidenceGraph | EvidenceGraph | 是 | empty object | 证据图 |
| questionTestPolicy | QuestionTestPolicyResult | 否 | null | 下一步动作 |
| decisionBoundary | DecisionBoundaryResult | 否 | null | 输出边界 |
| patientOutput | PatientOutput | 否 | null | 患者端输出 |
| clinicianReport | ClinicianReport | 否 | null | 医生端报告 |
| runtimeTraceIds | List<String> | 是 | empty list | Trace ID 列表 |
| createdAt | Instant | 是 | now | 创建时间 |
| updatedAt | Instant | 是 | now | 更新时间 |

---

# 五、Short-term Context 的 Phase 1 降级说明

Phase 1 暂不单独实现 Redis 或独立 ShortTermContextStore，而是使用 `RuntimeState.inputHistory` 作为最小降级实现：

```text
1. inputHistory 保存最近若干轮用户输入。
2. inputHistory 只用于语言连续性和 CaseFrame 更新。
3. 诊断判断必须基于结构化 RuntimeState，而不能直接依赖历史文本。
4. 后续如接入 Redis，可将 inputHistory 外拆为 ShortTermContextStore。
```

---

# 六、核心子结构

以下结构可以根据实现习惯使用 Java record、class 或 Lombok class。Phase 1 优先清晰，不追求过度抽象。

## 6.1 CaseFrame

```text
chiefComplaint: String
patientProfile: PatientProfile
symptoms: List<SymptomItem>
pastHistory: List<String>
medicationHistory: List<String>
examinationResults: List<String>
missingSlots: List<String>
conflictingSlots: List<String>
```

## 6.2 KnowledgeContext

```text
symptomGroup: String
commonDiagnoses: List<DiagnosisRef>
mustNotMiss: List<DiagnosisRef>
redFlags: List<RedFlagRule>
requiredQuestions: List<String>
recommendedTests: List<String>
sourceAssets: List<String>
```

Phase 1 的 KnowledgeContext 来自静态 YAML / JSON 规则。总设计中的 Clinical Pathway、KG-lite、RAG Evidence Library 会在 Phase 2 接入。

## 6.3 ExperienceContext

```text
matchedExperienceUnits: List<ExperienceUnit>
experienceAlerts: List<String>
implementationMode: String // empty 或 mock
```

Phase 1 的 ExperienceContext 是空实现或 mock 实现，不接入真实 Clinical Experience Memory。

## 6.4 SafetyGateResult

```text
triggered: boolean
riskLevel: RiskLevel
matchedRules: List<String>
reason: String
requiredAction: String
patientOutputConstraint: String
failSafeRequired: boolean
```

## 6.5 DifferentialDiagnosisBoard

```text
candidates: List<DDxCandidate>
updatedReason: String
```

DDxCandidate：

```text
name: String
status: CandidateStatus
riskLevel: RiskLevel
reason: String
patientVisible: boolean
```

## 6.6 EvidenceGraph

```text
items: List<EvidenceGraphItem>
```

EvidenceGraphItem：

```text
diagnosis: String
supportingEvidence: List<String>
opposingEvidence: List<String>
missingEvidence: List<String>
conflictingEvidence: List<String>
status: CandidateStatus
nextQuestions: List<String>
recommendedTests: List<String>
```

## 6.7 QuestionTestPolicyResult

```text
nextAction: NextAction
reason: String
```

NextAction：

```text
type: NextActionType
content: String
purpose: String
targetDiagnosis: String
priority: String // low / medium / high
```

## 6.8 DecisionBoundaryResult

```text
allowedOutputLevel: OutputLevel
patientDiagnosisLabelAllowed: boolean
clinicianDdxAllowed: boolean
reason: String
constraints: List<String>
```

## 6.9 PatientOutput

```text
allowed: boolean
content: String
outputLevel: OutputLevel
constraintsApplied: List<String>
```

## 6.10 ClinicianReport

```text
allowed: boolean
caseSummary: String
safetySummary: String
ddxSummary: List<DDxCandidate>
evidenceSummary: EvidenceGraph
recommendedQuestions: List<String>
recommendedTests: List<String>
```

## 6.11 RuntimeTrace

```text
traceId: String
runtimeId: String
step: int
input: String
modulesExecuted: List<String>
knowledgeUsed: List<String>
experienceUsed: List<String>
safetyGateResult: SafetyGateResult
ddxChange: Map<String, Object>
evidenceGraphChange: Map<String, Object>
decisionBoundaryResult: DecisionBoundaryResult
outputSummary: Map<String, Object>
createdAt: Instant
```

---

# 七、AOP Trace 补充结构

除 RuntimeTrace 主对象外，Phase 1 需要支持模块级 AOP Trace。

建议注解：

```java
@TraceStep("SafetyGate")
```

TraceStep 最小记录字段：

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

AOP Trace 不能替代业务模块显式写回 RuntimeState，它只负责横切追踪和审计辅助。

---

# 八、字段读写关系

| 模块 | 主要读取 | 主要写入 |
|---|---|---|
| RuntimeController | 请求参数 | RuntimeState 基础字段、inputHistory |
| EntryAssessmentService | inputHistory、basicInfo | entryAssessment、workMode、runtimeStatus |
| CaseFrameService | inputHistory、旧 caseFrame | caseFrame |
| KnowledgeContextService | caseFrame、entryAssessment | knowledgeContext |
| ExperienceContextService | caseFrame、knowledgeContext | experienceContext |
| SafetyGateService | caseFrame、knowledgeContext、experienceContext | safetyGate、runtimeStatus |
| DDxBoardService | caseFrame、knowledgeContext、safetyGate | differentialBoard |
| EvidenceGraphService | caseFrame、differentialBoard、knowledgeContext | evidenceGraph |
| QuestionTestPolicyService | evidenceGraph、safetyGate | questionTestPolicy、runtimeStatus |
| DecisionBoundaryService | mode、safetyGate、differentialBoard、evidenceGraph、questionTestPolicy | decisionBoundary |
| OutputService | decisionBoundary、questionTestPolicy、evidenceGraph | patientOutput、clinicianReport |
| RuntimeTraceAspect | 模块输入输出、异常、耗时 | runtimeTraceIds 或 TraceStore |

---

# 九、完成标准

```text
1. 所有核心结构可以用 Java enum / record / class 表达。
2. RuntimeState 可以被 Jackson 正常序列化和反序列化。
3. RuntimeStatus 有明确枚举和迁移规则。
4. 每个模块的读写字段清楚。
5. PatientOutput 和 ClinicianReport 结构分离。
6. RuntimeTrace 能复盘一次 Runtime 的核心判断。
7. AOP Trace 能记录模块级执行过程。
8. 后续 Phase 2–4 可以在不推翻结构的前提下扩展字段。
```
