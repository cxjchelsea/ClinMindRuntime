# Phase 3 Runtime 评估接入设计

> 本文档定义 Phase 3 的 EvaluationRunner 如何接入 Phase 1/2 已完成的 Runtime。  
> 核心原则：评估系统是 Runtime 的外部调用者，不是 Runtime 的替代者；评估器不能绕过 Runtime 直接调用 SafetyGate、DDx、EvidenceGraph 等内部模块。

---

# 一、设计定位

Phase 3 不重写 Runtime，也不改动 Runtime 主控逻辑。

Phase 3 的接入方式是：

```text
EvaluationRunner
→ RuntimeService.startRuntime / continueRuntime
→ RuntimeState / RuntimeTrace
→ Scorers
→ EvaluationResult
```

而不是：

```text
EvaluationRunner
→ EntryAssessmentService / SafetyGateService / EvidenceGraphService
```

---

# 二、为什么不能绕过 Runtime

如果评估器绕过 Runtime 直接调用内部模块，会导致：

```text
1. 评估结果无法反映真实 API 行为。
2. 可能绕过 DecisionBoundary。
3. 可能绕过 Patient / Clinician 输出隔离。
4. 可能绕过 RuntimeTrace。
5. 可能绕过 AssetPackage 绑定和版本追踪。
```

所以 Phase 3 的评估必须从 Runtime 的公开服务入口进入。

---

# 三、接入方式

## 3.1 EvaluationRunner 调用 RuntimeService

推荐：

```java
RuntimeExecutionResult startResult = runtimeService.startRuntime(startRequest);
RuntimeExecutionResult continueResult = runtimeService.continueRuntime(continueRequest);
```

EvaluationRunner 收集：

```text
final RuntimeState
all RuntimeTrace
operation response map
runtime_id
trace_ids
exceptions
```

## 3.2 RuntimeCaseExecution

```text
RuntimeCaseExecution
- caseId
- runtimeId
- finalState
- traces
- operationResponses
- errors
```

用于 Scorer 统一读取。

---

# 四、asset_context 接入

EvaluationRunConfig 应支持：

```text
assetPackageId
assetPackageVersion
```

每个 EvaluationCase 可以选择覆盖：

```text
asset_context.package_id
asset_context.version
```

默认策略：

```text
1. 如果 case 明确指定 asset_context，则使用 case 的配置。
2. 否则使用 EvaluationRunConfig 的 assetPackageId / assetPackageVersion。
3. 如果两者都没有，则使用 Runtime 默认 active package。
```

约束：

```text
assetPackageVersion 必须和 manifest.version 匹配。
版本不匹配应作为 EvaluationItemResult.failed，而不是让整个 run 崩溃。
```

---

# 五、患者端和医生端评估

同一病例可以用不同 mode 执行：

```text
patient_facing
clinician_copilot
debug（仅内部评估）
```

PatientBoundaryScorer 必须检查：

```text
patient_facing 输出中不出现 differential_board
evidence_graph
clinician_report
must_not_miss
common_diagnoses
target_diagnosis
asset internal details
```

Clinician 评估可以检查：

```text
DDx Board 是否存在
EvidenceGraph 是否存在
ClinicianReport 是否存在
sourceAssets 是否包含 asset_id@version
```

---

# 六、Trace 接入

EvaluationRunner 不直接构造 trace，只读取 Runtime 产生的 trace。

必须读取：

```text
runtimeService.getTraces(runtimeId)
```

TraceCompletenessScorer 检查：

```text
modulesExecuted
outputSummary
knowledgeUsed
experienceUsed
decisionBoundaryResult
safetyGateResult
```

AssetVersionTraceScorer 检查：

```text
output_summary.asset_package_id
output_summary.asset_package_version
knowledge_used 中是否包含 @version
experience_used 中是否包含 @version
```

---

# 七、错误处理

## 7.1 Runtime 正常返回 ERROR_SAFE_HALTED

这不一定是失败。

例如：

```text
broken asset package
unsupported input
安全兜底场景
```

是否失败由 ExpectedOutcome 决定。

## 7.2 Runtime 抛异常

EvaluationRunner 应捕获异常并生成：

```text
EvaluationItemResult.failed=true
MetricResult(metricId=runtime_execution, passed=false)
```

除非 `failFast=true`，否则继续执行后续病例。

---

# 八、与 Runtime 代码的边界

Phase 3-P0 不建议修改：

```text
EntryAssessmentService
CaseFrameService
KnowledgeContextService
SafetyGateService
DifferentialDiagnosisBoardService
EvidenceGraphService
QuestionTestPolicyService
DecisionBoundaryService
PatientOutputService
ClinicianReportService
```

可以新增：

```text
EvaluationRunner
EvaluationController
EvaluationScorer
EvaluationRunStore
EvaluationCaseRepository
```

如果评估结果显示 Runtime 某模块不足，先记录 `RegressionFinding`，不要在 Phase 3 实现中顺手重写 Phase 1/2 模块。

---

# 九、完成标准

```text
1. EvaluationRunner 只通过 RuntimeService 执行病例。
2. EvaluationRunner 能执行 start 和 continue。
3. EvaluationRunner 能读取 RuntimeTrace。
4. EvaluationRunner 能处理 ERROR_SAFE_HALTED。
5. EvaluationRunner 能把异常转为 EvaluationItemResult。
6. PatientBoundaryScorer 使用真实 API/Mapper 输出进行检查。
7. AssetVersionTraceScorer 使用真实 RuntimeTrace 进行检查。
8. 不破坏 Phase 1 / Phase 2 回归测试。
```
