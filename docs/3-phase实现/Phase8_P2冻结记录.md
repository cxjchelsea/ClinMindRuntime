# Phase 8-P2 冻结记录：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP

> 上位规格：`Phase8_P2ModelRegistry与PromptRegistry_实现规格.md`  
> API 与测试设计：`Phase8_P2ModelGovernance_API与测试设计.md`  
> 任务清单：`Phase8_P2开发任务清单.md`  
> 人工测试：`Phase8_P2人工测试结果.md`  
> 冻结日期：2026-07-06

---

# 一、冻结结论

Phase 8-P2 **ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP** 已实现并通过自动化测试、全量回归和人工验收，现冻结。

本阶段完成的是模型治理注册层的最小闭环：

```text
ModelRegistryEntry
-> PromptRegistryEntry
-> TrainingDatasetVersion
-> ModelExperimentRecord
-> ModelEvaluationReport
-> ModelReleaseCandidate
-> ModelRollbackPlan
-> Evaluation / Audit / Candidate governance
```

---

# 二、已实现能力

## Model governance domain

新增：

- `ModelRegistryEntry`
- `PromptRegistryEntry`
- `TrainingDatasetVersion`
- `ModelExperimentRecord`
- `ModelEvaluationReport`
- `ModelReleaseCandidate`
- `ModelRollbackPlan`
- `ModelGovernanceSnapshot`

## Store / service / policy

新增：

- in-memory model governance stores
- `ModelGovernanceService`
- `ModelRegistryPolicy`
- `PromptRegistryPolicy`
- `TrainingDatasetVersionPolicy`
- `ModelExperimentPolicy`
- `ModelEvaluationReportPolicy`
- `ModelReleasePolicy`

## Debug / Governance API

新增：

- `POST /api/v1/debug/model-governance/models`
- `GET /api/v1/debug/model-governance/models`
- `GET /api/v1/debug/model-governance/models/{model_registry_id}`
- `POST /api/v1/debug/model-governance/prompts`
- `GET /api/v1/debug/model-governance/prompts`
- `POST /api/v1/debug/model-governance/datasets`
- `GET /api/v1/debug/model-governance/datasets`
- `POST /api/v1/debug/model-governance/experiments`
- `GET /api/v1/debug/model-governance/experiments/{experiment_id}`
- `POST /api/v1/debug/model-governance/evaluation-reports`
- `POST /api/v1/debug/model-governance/rollback-plans`
- `POST /api/v1/debug/model-governance/release-candidates`
- `GET /api/v1/debug/model-governance/release-candidates/{release_candidate_id}`

## Trace / Audit

新增 audit action：

- `CREATE_MODEL_REGISTRY_ENTRY`
- `UPDATE_MODEL_REGISTRY_ENTRY`
- `CREATE_PROMPT_REGISTRY_ENTRY`
- `CREATE_TRAINING_DATASET_VERSION`
- `CREATE_MODEL_EXPERIMENT_RECORD`
- `CREATE_MODEL_EVALUATION_REPORT`
- `CREATE_MODEL_RELEASE_CANDIDATE`
- `CREATE_MODEL_ROLLBACK_PLAN`
- `MODEL_GOVERNANCE_POLICY_REJECTED`

新增 audit resource：

- `MODEL_GOVERNANCE`

## Evaluation scorer

新增：

- `ModelRegistryCompletenessScorer`
- `PromptRegistrySafetyScorer`
- `TrainingDatasetGovernanceScorer`
- `ModelExperimentTraceScorer`
- `ModelReleaseReadinessScorer`

支持 tag：

- `model_governance_eval`

## Candidate governance

已扩展 `CandidateMappingPolicy`：

- model registry failure -> trace/governance review candidate
- prompt safety failure -> patient boundary / prompt governance candidate
- dataset governance failure -> dataset review candidate
- release readiness failure -> release review candidate

所有 candidate 仍需人工 review，不自动发布。

---

# 三、治理边界

## ModelRegistryPolicy

冻结规则：

- `model_id` / `model_version` 必填。
- `provider_id` / `provider_version` 必填。
- `capability_types` 不得为空。
- `model_source` 必须合法。
- `BLOCKED` model 不能形成 release candidate。

## PromptRegistryPolicy

冻结规则：

- patient-facing prompt 必须 `requires_decision_boundary=true`。
- patient-facing prompt 必须声明 `forbidden_output_types`。
- `prompt_template_hash` 必填。
- P2 不保存完整 prompt 原文。

## TrainingDatasetVersionPolicy

冻结规则：

- 不允许 raw patient dialogue。
- 不允许 `auto_publish=true`。
- `publish_status` 必须保持 `DRAFT`。
- 未脱敏 dataset 不能 `APPROVED_FOR_EXPERIMENT`。
- dataset 必须引用 candidate 或 metric 来源。

## ModelExperiment / Report / Release

冻结规则：

- experiment 必须关联 model / prompt / dataset。
- evaluation report 必须关联 experiment。
- critical safety finding 不能 approve。
- release candidate 必须有关联 evaluation report。
- release candidate 必须有 rollback plan。
- release candidate 必须 `REVIEW_REQUIRED`。
- release candidate 永远 `auto_publish=false`。

---

# 四、测试结果

Phase 8-P2 targeted Java tests：

```text
17 run, 0 failures, 0 errors, 0 skipped
```

完整 Java 回归：

```text
498 run, 0 failures, 0 errors, 23 skipped
```

Python provider：

```text
未修改 python-provider，pytest 不适用
```

---

# 五、未做事项

以下事项明确后置，不属于 Phase 8-P2 冻结范围：

1. 真实模型训练流水线。
2. LoRA / DPO / RLHF / RFT / distillation。
3. 真实外部云模型强依赖主线。
4. Production ModelRegistry。
5. Prompt 原文加密存储与权限控制。
6. Model Console / Provider Console。
7. 自动灰度发布。
8. 自动上线 ModelVersion / PromptVersion / TrainingDatasetVersion。
9. 在线自动回滚系统。
10. Phase 9 Tool / MCP / Skills 受控接入。

---

# 六、最终冻结状态

Phase 8-P2 已冻结。

冻结后的约束：

- 不继续向 Phase 8-P2 堆叠真实训练、自动发布、自动上线、自动回滚能力。
- 后续能力进入 Phase 8-P3 / Phase 9 / Phase 10。
- 已冻结代码可接受 bug fix、测试补强和文档修正。
- 不得突破 Runtime 主控、ProviderCapabilityPolicy、ProviderValidation、SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 和 Candidate Governance 边界。
