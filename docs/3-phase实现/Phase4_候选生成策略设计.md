# Phase 4 候选生成策略设计

> 本文档定义 Phase 4-P0 中如何从 EvaluationRun、EvaluationItemResult、MetricResult、RegressionFinding、SafetyViolation 和 RuntimeCaseExecution 生成 ExperienceCandidate 与 TrainingExampleCandidate。  
> 生成策略必须保守，优先从失败和风险中生成候选，不从成功病例中大规模生成噪声候选。

---

# 一、策略目标

CandidateGenerationPolicy 的目标是：

```text
1. 把 Evaluation 中暴露的问题转化为可复盘候选。
2. 把关键失败样本转化为训练数据候选。
3. 保留来源、风险、版本和原因。
4. 避免把所有运行记录都变成经验，造成噪声。
5. 避免自动学习错误信息。
```

---

# 二、输入来源

Phase 4-P0 允许的输入来源：

```text
EvaluationRun
EvaluationResult
EvaluationItemResult
MetricResult
RegressionFinding
SafetyViolation
RuntimeCaseExecution
RuntimeTrace
```

P0 不直接接入：

```text
DoctorFeedback
FollowUpOutcome
真实病历
线上用户反馈
LLM 自动总结
```

---

# 三、默认生成策略

默认策略：

```text
1. CRITICAL metric failure 必须生成 ExperienceCandidate。
2. CRITICAL metric failure 可以生成 TrainingExampleCandidate。
3. MAJOR metric failure 默认生成 ExperienceCandidate。
4. MINOR metric failure 默认不生成，除非配置开启。
5. passed case 默认不生成候选。
6. not_applicable metric 不生成候选。
```

默认配置：

```text
generate_from_critical_failures = true
generate_from_major_failures = true
generate_from_minor_failures = false
generate_from_passed_cases = false
generate_training_candidates = true
generate_experience_candidates = true
max_candidates_per_case = 5
```

---

# 四、按 Metric 映射 ExperienceCandidate

## 4.1 SafetyGateScorer failure

生成：

```text
ExperienceCandidateType = SAFETY_LESSON
CandidateRiskLevel = CRITICAL
```

建议标题：

```text
SafetyGate failed for high-risk case
```

来源：

```text
MetricResult
SafetyViolation
EvaluationItemResult
RuntimeCaseExecution
```

## 4.2 PatientBoundaryScorer failure

生成：

```text
ExperienceCandidateType = PATIENT_BOUNDARY_LESSON
CandidateRiskLevel = CRITICAL
```

说明：患者端输出边界违规必须作为高优先级复盘候选。

## 4.3 DdxCoverageScorer failure

生成：

```text
ExperienceCandidateType = MISSING_DDX_LESSON
CandidateRiskLevel = HIGH 或 MEDIUM
```

风险等级取决于 ExpectedOutcome severity。

## 4.4 NextActionScorer failure

生成：

```text
ExperienceCandidateType = NEXT_ACTION_LESSON
CandidateRiskLevel = MEDIUM
```

如果 case severity 为 HIGH_RISK，则提升为 HIGH。

## 4.5 TraceCompletenessScorer failure

生成：

```text
ExperienceCandidateType = TRACE_QUALITY_LESSON
CandidateRiskLevel = LOW / MEDIUM
```

## 4.6 AssetVersionTraceScorer failure

生成：

```text
ExperienceCandidateType = ASSET_VERSION_LESSON
CandidateRiskLevel = HIGH
```

原因：资产版本不可追踪会影响复盘和回滚。

## 4.7 runtime_execution failure

生成：

```text
ExperienceCandidateType = RUNTIME_ERROR_LESSON
CandidateRiskLevel = HIGH / CRITICAL
```

如果 Runtime 进入 ERROR_SAFE_HALTED，则可生成：

```text
FAIL_SAFE_LESSON
```

---

# 五、按 Metric 映射 TrainingExampleCandidate

## 5.1 SafetyGate failure

生成训练候选：

```text
TrainingTaskType = RISK_SIGNAL_CLASSIFICATION
```

输入：

```text
case input text
basic_info
symptom_group
```

期望输出：

```text
safety_gate_triggered = true
expected matched rules
```

## 5.2 PatientBoundary failure

生成训练候选：

```text
TrainingTaskType = PATIENT_SAFE_REWRITE
```

输入：

```text
unsafe patient output
case context summary
```

期望输出：

```text
safe patient-facing expression
forbidden fields removed
```

注意：P0 不调用 LLM 生成安全改写，只记录候选结构。

## 5.3 DdxCoverage failure

生成训练候选：

```text
TrainingTaskType = DDX_EXPECTATION
```

输入：

```text
case frame summary
symptom group
```

期望输出：

```text
expected_ddx_contains
```

## 5.4 NextAction failure

生成训练候选：

```text
TrainingTaskType = NEXT_ACTION_EXPECTATION
```

输入：

```text
case frame summary
current runtime state summary
```

期望输出：

```text
expected_next_action_types
```

## 5.5 AssetVersionTrace failure

生成训练候选：

```text
TrainingTaskType = ASSET_TRACE_EXPECTATION
```

输入：

```text
runtime trace summary
asset context
```

期望输出：

```text
required asset_package_id / asset_package_version / asset_id@version
```

---

# 六、候选去重策略

同一个 case 中可能出现多个 metric failure。

去重原则：

```text
1. 同一 case + same metric_id + same failure message 只生成一个 ExperienceCandidate。
2. SafetyViolation 与对应 MetricResult 不重复生成两个同类 ExperienceCandidate。
3. RegressionFinding 聚合层候选与 item 层候选允许共存，但类型要不同。
4. max_candidates_per_case 限制单病例候选数量。
```

---

# 七、候选优先级

候选优先级：

```text
CRITICAL safety / boundary > asset trace > runtime execution > ddx > next action > trace quality
```

当超过 `max_candidates_per_case` 时，保留优先级更高的候选。

---

# 八、生成结果中的 skipped_items

CandidateGenerationResult 应记录跳过原因。

跳过原因：

```text
PASSED_CASE_SKIPPED
NOT_APPLICABLE_METRIC_SKIPPED
MINOR_FAILURE_DISABLED
DUPLICATE_CANDIDATE_SKIPPED
MAX_CANDIDATES_PER_CASE_REACHED
UNSUPPORTED_METRIC_SKIPPED
```

---

# 九、安全边界

候选生成策略不得：

```text
1. 根据候选修改 RuntimeState。
2. 根据候选修改 Asset Package。
3. 根据候选修改 CapabilityProfile。
4. 自动判断候选通过审核。
5. 自动进入训练数据集。
6. 自动生成患者端表达。
```

---

# 十、P0 推荐实现顺序

```text
1. CandidateGenerationPolicy 默认配置。
2. MetricResult → ExperienceCandidate 映射。
3. MetricResult → TrainingExampleCandidate 映射。
4. SafetyViolation → ExperienceCandidate 映射。
5. RegressionFinding → ExperienceCandidate 聚合候选。
6. CandidateGenerationResult 聚合。
7. skipped_items 与 warnings。
```

---

# 十一、最终结论

Phase 4-P0 的候选生成策略应当保守：

```text
先从失败中学习，
先生成候选，
先进入审核，
不自动生效。
```
