# Phase 8-P1 冻结记录：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP

> 上位规格：`Phase8_P1ModelProvider与JudgeProvider_实现规格.md`  
> API 与测试设计：`Phase8_P1ProviderCapability_API与测试设计.md`  
> 任务清单：`Phase8_P1开发任务清单.md`  
> 人工测试：`Phase8_P1人工测试结果.md`  
> 冻结日期：2026-07-03

---

# 一、冻结结论

Phase 8-P1 **ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP** 已实现并通过自动化测试、回归测试和人工验收，现冻结。

本阶段完成的是模型能力治理的最小闭环：

```text
ProviderCapabilityProfile
-> ProviderCapabilityPolicy
-> JudgeProvider / RiskSignalClassifierProvider
-> ProviderValidation
-> Debug API
-> Evaluation Scorer
-> Trace / Audit / Candidate governance
```

---

# 二、已实现能力

## Python provider

新增：

- `GET /v1/capability-profiles`
- `POST /v1/judge`
- `POST /v1/classify-risk`

实现方式：

- deterministic / rule-based JudgeProvider。
- deterministic / rule-based RiskSignalClassifierProvider。
- 无真实 LLM / 外部云模型强依赖。

## Java Runtime provider domain

新增：

- `ProviderCapabilityProfile`
- `ProviderCapabilityPolicy`
- `ProviderCapabilityPolicyDecision`
- `ProviderGovernanceSnapshot`
- `JudgeRequest`
- `JudgeScoreResult`
- `RiskSignalClassificationRequest`
- `RiskSignalDraft`

## Java client / validation / debug

扩展：

- `PythonProviderClient#getCapabilityProfiles`
- `PythonProviderClient#judge`
- `PythonProviderClient#classifyRisk`
- `ProviderValidationService#validateCapabilityProfile`
- `ProviderValidationService#validateJudge`
- `ProviderValidationService#validateRiskSignalDraft`

新增 Debug API：

- `GET /api/v1/debug/providers/capability-profiles`
- `POST /api/v1/debug/providers/judge/run`
- `POST /api/v1/debug/providers/risk-classifier/run`

## Evaluation scorer

新增：

- `JudgeTraceCompletenessScorer`
- `JudgeBoundaryAgreementScorer`
- `JudgeViolationDetectionScorer`
- `RiskClassifierTraceScorer`
- `ProviderCapabilityProfileScorer`

支持 tags：

- `judge_eval`
- `risk_classifier_eval`
- `provider_profile_eval`

## Trace / Audit / Candidate governance

已实现：

- `ProviderCallRecord` 可记录 JUDGE / RISK_CLASSIFICATION capability。
- Audit action 新增：
  - `RUN_JUDGE_PROVIDER`
  - `RUN_RISK_CLASSIFIER_PROVIDER`
  - `QUERY_PROVIDER_CAPABILITY_PROFILE`
  - `PROVIDER_CAPABILITY_POLICY_REJECTED`
- Evaluation metric 可通过既有 CandidateGenerationService 映射为 review-required candidate。

未实现自动发布，所有候选仍需人工审核。

---

# 三、治理边界

## ProviderCapabilityPolicy

已冻结规则：

- `patient_direct_answer` 一律拒绝。
- forbidden use case 拒绝。
- disabled profile 返回 `SKIPPED`。
- 超过 `max_input_chars` 返回 `DEGRADED`。
- 所有拒绝均带 reasons。

## JudgeProvider

冻结边界：

- Judge 只输出 `JudgeScoreResult`。
- Judge 不输出 PatientOutput。
- Judge 不决定最终诊断。
- Judge rationale 不展示给患者。
- Judge 结果必须经过 Java validation / evaluation。

## RiskSignalClassifierProvider

冻结边界：

- Risk classifier 只输出 `RiskSignalDraft`。
- RiskSignalDraft 不直接触发 SafetyGate。
- SafetyGate 仍由 Java SafetyGateService / Runtime 主控决定。
- 风险草稿可进入 Trace / Evaluation / Candidate governance。

## ProviderCapabilityProfile

冻结边界：

- CapabilityProfile 只是能力画像，不是自动授权。
- 授权由 Java ProviderCapabilityPolicy 决定。
- P1 profile 必须 `patient_output_allowed=false` 且 `requires_validation=true`。

---

# 四、测试结果

Python provider：

```text
10 passed
```

Phase 8-P1 targeted Java tests：

```text
19 passed
```

完整 Java 回归：

```text
481 run, 0 failures, 0 errors, 23 skipped
```

---

# 五、未做事项

以下事项明确后置，不属于 Phase 8-P1 冻结范围：

1. 真实 LLM Judge 接入。
2. 真实 RiskSignalClassifier 模型接入。
3. ModelRegistry / PromptRegistry。
4. TrainingDatasetVersion 自动发布。
5. LoRA / DPO / RFT / distillation。
6. Model Console / Provider Console。
7. 生产级医生审核平台。

---

# 六、最终冻结状态

Phase 8-P1 已冻结。

冻结后的约束：

- 不继续向 Phase 8-P1 堆叠真实 LLM / 训练平台 / 自动发布能力。
- 后续能力进入 Phase 8-P2 或 Phase 10。
- 已冻结代码可接受 bug fix、测试补强和文档修正，但不得突破 Runtime 主控、SafetyGate 主控和 PatientOutput 边界。
