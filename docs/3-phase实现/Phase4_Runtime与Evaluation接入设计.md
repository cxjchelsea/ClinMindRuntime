# Phase 4 Runtime 与 Evaluation 接入设计

> 本文档定义 Phase 4-P0 如何读取 Runtime 与 Evaluation 的结果，并在不改变 Runtime 行为、不修改 Evaluation 结果的前提下生成候选资产。  
> Phase 4 是 Evaluation 的下游消费者，不是 Runtime 主控，也不是 Evaluation Scorer 的替代者。

---

# 一、接入原则

```text
1. Phase 4 只读取 Runtime / Evaluation 结果，不反向修改它们。
2. Phase 4 不绕过 RuntimeService。
3. Phase 4 不重新评分病例。
4. Phase 4 不改变 EvaluationResult。
5. Phase 4 不改变 CapabilityProfileUpdateProposal。
6. Phase 4 生成的 Candidate 不自动生效。
```

---

# 二、接入对象

Phase 4-P0 读取以下对象：

```text
EvaluationRun
EvaluationResult
EvaluationItemResult
MetricResult
SafetyViolation
RegressionFinding
RuntimeCaseExecution
RuntimeTrace
```

读取入口：

```text
EvaluationRunStore
```

P0 不新增数据库，不读取外部临床系统。

---

# 三、接入链路

```text
CandidateController
→ CandidateGenerationService
→ EvaluationRunStore.get(runId)
→ EvaluationRun / EvaluationResult / EvaluationItemResult
→ EvaluationRunStore.getExecution(runId, caseId)
→ RuntimeCaseExecution / RuntimeTrace
→ CandidateGenerationPolicy
→ ExperienceCandidateGenerator
→ TrainingExampleCandidateGenerator
→ CandidateStore
```

---

# 四、CandidateGenerationService

## 4.1 职责

CandidateGenerationService 是 Phase 4-P0 的应用服务。

职责：

```text
1. 接收 evaluation_run_id。
2. 从 EvaluationRunStore 读取 EvaluationRun。
3. 遍历 EvaluationItemResult。
4. 读取 RuntimeCaseExecution。
5. 调用候选生成器。
6. 聚合 CandidateGenerationResult。
7. 写入 CandidateStore。
```

## 4.2 不负责

CandidateGenerationService 不负责：

```text
不执行 Runtime。
不执行 Evaluation。
不重新评分。
不修改 RuntimeState。
不修改 EvaluationRun。
不修改 CapabilityProfile。
不审核候选。
```

---

# 五、ExperienceCandidateGenerator

## 5.1 输入

```text
EvaluationRun
EvaluationItemResult
MetricResult
SafetyViolation
RegressionFinding
RuntimeCaseExecution
CandidateGenerationPolicy
```

## 5.2 输出

```text
List<ExperienceCandidate>
```

## 5.3 生成来源

优先来源：

```text
SafetyViolation
RegressionFinding
failed MetricResult
runtime_execution failure
```

不从 not_applicable metric 生成候选。

---

# 六、TrainingExampleCandidateGenerator

## 6.1 输入

```text
EvaluationRun
EvaluationItemResult
MetricResult
RuntimeCaseExecution
CandidateGenerationPolicy
```

## 6.2 输出

```text
List<TrainingExampleCandidate>
```

## 6.3 生成来源

P0 只从失败的 EvaluationItemResult 生成训练候选。

不从全部通过病例批量生成。

---

# 七、RuntimeCaseExecution 的作用

RuntimeCaseExecution 提供：

```text
runtime_id
final_state
traces
operation_responses
errors
```

Phase 4 使用它来：

```text
1. 绑定 runtime_id。
2. 读取 patient-facing / clinician-facing 输出摘要。
3. 读取 RuntimeTrace 中的 asset refs。
4. 读取错误信息。
5. 生成 CandidateSourceRef。
```

Phase 4 不修改 RuntimeCaseExecution。

---

# 八、RuntimeTrace 的使用边界

RuntimeTrace 可以用于：

```text
1. 生成 trace quality 类候选。
2. 生成 asset version trace 类候选。
3. 记录 CandidateSourceRef。
4. 辅助描述候选摘要。
```

RuntimeTrace 不可以：

```text
1. 直接成为正式经验。
2. 直接进入训练数据集。
3. 未脱敏导出。
4. 被模型直接使用。
```

---

# 九、与 EvaluationResult 的关系

EvaluationResult 是 Phase 4 的输入之一。

Phase 4 可以读取：

```text
total_cases
pass_rate
average_score
safety_pass_rate
boundary_pass_rate
ddx_average_score
trace_pass_rate
asset_trace_pass_rate
major_findings
```

Phase 4 不能修改 EvaluationResult。

---

# 十、与 CapabilityProfileUpdateProposal 的关系

Phase 4-P0 不读取或修改 CapabilityProfileUpdateProposal。

原因：

```text
CapabilityProfileUpdateProposal 是能力授权建议。
ExperienceCandidate / TrainingExampleCandidate 是复盘候选。
二者都来自 Evaluation，但治理路径不同。
```

未来 P1/P2 可以在 CandidateSourceRef 中记录相关 proposal_id，但 P0 不需要。

---

# 十一、错误处理

## 11.1 evaluation_run_id 不存在

返回：

```text
EVALUATION_RUN_NOT_FOUND
```

## 11.2 EvaluationRun 未完成

返回：

```text
EVALUATION_RUN_NOT_COMPLETED
```

或生成 warning：

```text
RUN_NOT_COMPLETED_SKIPPED
```

P0 推荐直接拒绝未完成 run。

## 11.3 RuntimeCaseExecution 缺失

记录 skipped item：

```text
RUNTIME_EXECUTION_NOT_FOUND
```

不要让整个 generation 失败。

## 11.4 没有候选生成

返回空 CandidateGenerationResult，不视为错误。

---

# 十二、测试重点

必须测试：

```text
1. CandidateGenerationService 不调用 RuntimeService。
2. CandidateGenerationService 不调用 EvaluationRunner。
3. SafetyViolation 可以生成 ExperienceCandidate。
4. failed MetricResult 可以生成 TrainingExampleCandidate。
5. passed case 默认不生成候选。
6. not_applicable metric 不生成候选。
7. RuntimeCaseExecution 缺失时记录 skipped item。
8. CandidateSourceRef 包含 run_id / case_id / asset version。
```

---

# 十三、最终结论

Phase 4 应作为 Evaluation 的下游治理层：

```text
Runtime 负责运行。
Evaluation 负责评分。
Phase 4 负责把值得复盘的结果变成候选。
```

Phase 4 不能倒过来控制 Runtime 或 Evaluation。
