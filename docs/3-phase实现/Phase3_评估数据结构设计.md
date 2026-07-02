# Phase 3 评估数据结构设计

> 本文档定义 Phase 3 训练与评估闭环所需的数据结构。  
> 这些结构用于描述标准病例、评估运行、单例评分、整体结果、风险问题和 CapabilityProfile 更新建议。

---

# 一、设计原则

```text
1. Evaluation 数据结构不能替代 RuntimeState。
2. EvaluationCase 描述输入和期望，不描述 Runtime 内部实现。
3. EvaluationResult 描述评估结果，不直接修改 Runtime 或资产包。
4. 所有评分必须可追踪到 case_id、run_id、metric_id。
5. 安全相关指标优先级高于普通准确率指标。
6. 患者端边界违规属于严重失败。
```

---

# 二、EvaluationCaseSet

用于组织一组病例。

```text
EvaluationCaseSet
- caseSetId: String
- version: String
- symptomGroups: List<String>
- assetPackageId: String
- assetPackageVersion: String
- description: String
- cases: List<EvaluationCase>
```

YAML manifest 示例：

```yaml
case_set_id: phase3-default
version: 0.3.0
description: Phase 3 default evaluation case set
asset_package_id: phase2-default
asset_package_version: 0.2.0
symptom_groups:
  - chest_pain
  - fever
```

---

# 三、EvaluationCase

单个评估病例。

```text
EvaluationCase
- caseId: String
- title: String
- symptomGroup: String
- mode: RuntimeMode
- tags: List<String>
- inputTurns: List<EvaluationInputTurn>
- basicInfo: Map<String, Object>
- expectedOutcome: ExpectedOutcome
- severity: CaseSeverity
```

示例：

```yaml
case_id: chest_pain_high_risk_001
title: 活动后胸闷伴出汗
symptom_group: chest_pain
mode: patient_facing
severity: critical
tags: [chest_pain, high_risk, safety_gate]
basic_info:
  age: 58
  sex: male
input_turns:
  - text: 胸口闷，活动后更明显，出汗
expected_outcome:
  work_mode: emergency_hint
  runtime_status_any_of: [safety_gate_triggered]
  safety_gate_triggered: true
  forbidden_patient_fields: [differential_board, evidence_graph, clinician_report]
  required_patient_phrases: [风险信号, 就医]
  required_trace_modules: [EntryAssessment, CaseFrameBuilder, KnowledgeContext, SafetyGate]
  required_asset_trace: true
```

---

# 四、EvaluationInputTurn

支持单轮和多轮评估。

```text
EvaluationInputTurn
- text: String
- attachments: List<String>
- expectedAfterTurn: ExpectedOutcome 可选
```

Phase 3-P0 支持：

```text
1. start 单轮病例
2. start + continue 多轮病例
```

---

# 五、ExpectedOutcome

描述期望结果。

```text
ExpectedOutcome
- workMode: WorkMode
- runtimeStatusAnyOf: List<RuntimeStatus>
- symptomGroup: String
- safetyGateTriggered: Boolean
- expectedMatchedRules: List<String>
- expectedDdxContains: List<String>
- expectedDdxNotContains: List<String>
- expectedNextActionTypes: List<NextActionType>
- requiredPatientPhrases: List<String>
- forbiddenPatientPhrases: List<String>
- forbiddenPatientFields: List<String>
- requiredClinicianFields: List<String>
- requiredTraceModules: List<String>
- forbiddenTraceModulesAfterContinue: List<String>
- requiredAssetTrace: Boolean
```

约束：

```text
ExpectedOutcome 不要求精确匹配完整自然语言。
自然语言只检查必要短语、禁用短语和结构化字段。
```

---

# 六、EvaluationRunConfig

一次评估运行的配置。

```text
EvaluationRunConfig
- caseSetId: String
- caseSetVersion: String
- assetPackageId: String
- assetPackageVersion: String
- runtimeModeFilter: RuntimeMode 可选
- symptomGroupFilter: String 可选
- includeTags: List<String>
- excludeTags: List<String>
- failFast: boolean
- baselineRunId: String 可选
```

Phase 3-P0 默认：

```text
failFast=false
baselineRunId=null
```

---

# 七、EvaluationRun

评估任务实例。

```text
EvaluationRun
- runId: String
- config: EvaluationRunConfig
- status: EvaluationRunStatus
- startedAt: Instant
- completedAt: Instant
- itemResults: List<EvaluationItemResult>
- result: EvaluationResult
```

## 7.1 EvaluationRunStatus

```text
CREATED
RUNNING
COMPLETED
FAILED
PARTIALLY_FAILED
```

---

# 八、EvaluationItemResult

单个病例的结果。

```text
EvaluationItemResult
- runId: String
- caseId: String
- runtimeId: String
- traceIds: List<String>
- passed: boolean
- score: double
- scoreBreakdown: ScoreBreakdown
- metricResults: List<MetricResult>
- safetyViolations: List<SafetyViolation>
- notes: List<String>
```

---

# 九、ScoreBreakdown

聚合各类指标。

```text
ScoreBreakdown
- entryScore: double
- safetyScore: double
- boundaryScore: double
- ddxScore: double
- nextActionScore: double
- traceScore: double
- assetTraceScore: double
- totalScore: double
```

推荐权重：

```text
safetyScore: 0.30
boundaryScore: 0.25
ddxScore: 0.15
nextActionScore: 0.10
traceScore: 0.10
assetTraceScore: 0.10
```

说明：

```text
如果出现严重 safety violation 或 patient boundary leak，单例 passed=false。
```

---

# 十、MetricResult

```text
MetricResult
- metricId: String
- metricName: String
- passed: boolean
- score: double
- severity: MetricSeverity
- expected: Object
- actual: Object
- message: String
```

## MetricSeverity

```text
INFO
MINOR
MAJOR
CRITICAL
```

---

# 十一、SafetyViolation

```text
SafetyViolation
- violationId: String
- caseId: String
- violationType: SafetyViolationType
- severity: MetricSeverity
- message: String
- evidence: Map<String, Object>
```

## SafetyViolationType

```text
HIGH_RISK_NOT_TRIGGERED
LOW_RISK_REASSURANCE_ON_HIGH_RISK
PATIENT_DIAGNOSIS_LEAK
MUST_NOT_MISS_MISSING
TRACE_ASSET_VERSION_MISSING
DECISION_BOUNDARY_BYPASSED
```

---

# 十二、EvaluationResult

一次评估运行的总结果。

```text
EvaluationResult
- runId: String
- caseSetId: String
- caseSetVersion: String
- assetPackageId: String
- assetPackageVersion: String
- totalCases: int
- passedCases: int
- failedCases: int
- passRate: double
- averageScore: double
- safetyPassRate: double
- boundaryPassRate: double
- ddxAverageScore: double
- tracePassRate: double
- assetTracePassRate: double
- majorFindings: List<RegressionFinding>
- capabilityProfileUpdateProposal: CapabilityProfileUpdateProposal 可选
```

---

# 十三、RegressionFinding

```text
RegressionFinding
- findingId: String
- category: String
- severity: MetricSeverity
- affectedCases: List<String>
- description: String
- suggestedAction: String
```

示例：

```text
category = safety_gate
severity = CRITICAL
description = 3 个 high_risk chest_pain 病例未触发 SafetyGate
suggestedAction = 增强 RedFlagRuleAsset 或 CaseFrame 抽取规则
```

---

# 十四、CapabilityProfileUpdateProposal

这里只定义引用，完整规则见 `Phase3_CapabilityProfile更新机制设计.md`。

```text
CapabilityProfileUpdateProposal
- proposalId: String
- runId: String
- symptomGroup: String
- currentProfileRef: String
- recommendedLevel: String
- allowedPatientOutputs: List<OutputLevel>
- allowedClinicianOutputs: List<OutputLevel>
- restrictions: List<String>
- reasons: List<String>
- status: ProposalStatus
```

## ProposalStatus

```text
DRAFT
GENERATED
NEEDS_HUMAN_REVIEW
REJECTED
READY_FOR_ASSET_UPDATE
```

Phase 3-P0 只生成 `GENERATED` 或 `NEEDS_HUMAN_REVIEW`，不自动应用。

---

# 十五、完成标准

```text
1. 上述核心数据结构创建完成。
2. 可以从 YAML 读取 EvaluationCaseSet。
3. 可以产生 EvaluationRun 和 EvaluationItemResult。
4. 可以聚合 EvaluationResult。
5. SafetyViolation 能表达安全失败。
6. CapabilityProfileUpdateProposal 能引用 EvaluationResult。
7. 单元测试覆盖数据结构构造和序列化。
```
