# Phase 8-P2 人工测试结果：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP

> 规格：`Phase8_P2ModelRegistry与PromptRegistry_实现规格.md`  
> API 与测试设计：`Phase8_P2ModelGovernance_API与测试设计.md`  
> 任务清单：`Phase8_P2开发任务清单.md`  
> 测试日期：2026-07-06

---

# 一、测试范围

本次验证覆盖 Phase 8-P2 的模型治理最小闭环：

```text
ModelRegistryEntry
-> PromptRegistryEntry
-> TrainingDatasetVersion
-> ModelExperimentRecord
-> ModelEvaluationReport
-> ModelReleaseCandidate
-> ModelRollbackPlan
-> Review-required governance
```

本次验证不覆盖真实训练流水线、真实外部云模型、自动发布、自动灰度、自动回滚、Model Console 或 Provider Console。

---

# 二、自动化测试结果

## Java targeted tests

命令：

```powershell
mvn "-Dtest=ModelGovernancePolicyTest,ModelGovernanceScorerTest,ModelGovernanceCandidateMappingTest,ModelGovernanceDebugControllerTest" test
```

结果：

```text
17 run, 0 failures, 0 errors, 0 skipped
```

覆盖：

- ModelRegistryPolicy
- PromptRegistryPolicy
- TrainingDatasetVersionPolicy
- ModelEvaluationReportPolicy
- ModelReleasePolicy
- ModelGovernanceDebugController
- Model Governance Evaluation Scorer
- Model Governance Candidate Mapping

## Java full regression

命令：

```powershell
mvn test
```

结果：

```text
498 run, 0 failures, 0 errors, 23 skipped
```

## Python provider

本阶段未修改 `python-provider`，未运行 pytest。

---

# 三、人工场景验证

## 场景 1：登记模型版本

验证点：

- `POST /api/v1/debug/model-governance/models` 可创建 `ModelRegistryEntry`。
- 返回 `model_registry_id`。
- `status=DRAFT`。
- 写操作需要 `SYSTEM_ADMIN` 或 `EVALUATION_REVIEWER`。
- `READ_ONLY_OBSERVER` 不能写。

结果：通过。

## 场景 2：登记 patient-facing prompt

输入：

```text
requires_decision_boundary=false
```

预期：

- PromptRegistryPolicy 拒绝。
- API 返回 `MODEL_GOVERNANCE_POLICY_REJECTED`。
- 不创建 unsafe prompt。

结果：通过。

## 场景 3：创建 TrainingDatasetVersion

验证点：

- `publish_status=DRAFT`。
- `safety_review_status=REVIEW_REQUIRED`。
- 不允许 `auto_publish=true`。
- 不允许 raw patient dialogue。
- 未脱敏数据不能 `APPROVED_FOR_EXPERIMENT`。

结果：通过。

## 场景 4：创建 ModelExperimentRecord

验证点：

- 必须关联 model / prompt / dataset。
- 默认 `status=PLANNED`。
- 不触发真实训练。

结果：通过。

## 场景 5：创建 ModelEvaluationReport

验证点：

- 必须关联 experiment。
- critical safety finding 不能给出 approve recommendation。
- 只记录 metric / finding id，不保存敏感原文。

结果：通过。

## 场景 6：创建 ModelReleaseCandidate

验证点：

- 必须有关联 evaluation report。
- 必须有 rollback plan。
- `review_status=REVIEW_REQUIRED`。
- `auto_publish=false`。
- 不自动上线模型、prompt 或 dataset。

结果：通过。

## 场景 7：Evaluation / Candidate 治理接入

验证点：

- `model_governance_eval` tag 可触发对应 scorer。
- 缺 model version / prompt bypass / dataset 未脱敏 / release 缺 rollback 可被 scorer 识别。
- scorer failure 可映射为 review-required candidate。
- 不自动发布任何治理对象。

结果：通过。

---

# 四、结论

Phase 8-P2 自动化与人工验收通过。

本阶段实现的是模型治理注册层 MVP，不是训练平台、发布平台或 MLOps 平台。Model / Prompt / Dataset / Experiment / Report / ReleaseCandidate 均保持为受控治理对象，Runtime 主控、ProviderValidation、安全边界、Candidate governance 和人工审核边界未被破坏。
