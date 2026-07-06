# Phase 8-P2 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 前置专项规划：`docs/2-专项设计/Python_AIProvider接入规划.md`  
> 前置阶段：Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结；Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结  
> 当前 Phase：Phase 8-P2  
> 当前目标：建立 ModelRegistry、PromptRegistry、TrainingDatasetVersion 与 ModelExperimentRecord 的最小治理闭环，让 Provider 能力具备版本登记、实验记录、评估归因和候选发布前治理，但不做真实训练平台和自动上线。

---

# 一、Phase 定位

Phase 8-P2 不是训练平台，也不是模型自动发布系统。

Phase 8-P2 的目标是：

```text
在 Phase 8-P0 / P1 已经具备 Python Provider、Judge、Risk classifier、ProviderCapabilityProfile 的基础上，
新增模型治理注册层，
把 model_id / model_version / prompt_version / dataset_version / experiment_id 纳入 Runtime Trace、Evaluation 和 Candidate governance，
让每一次 Provider 输出都能回答：

它来自哪个模型？
它使用哪个 prompt？
它基于哪个数据集版本？
它在哪个实验中评估过？
它是否允许进入候选发布流程？
```

核心命题：

```text
模型能力不仅要能调用，还要能登记、追踪、比较、评估和治理。
```

---

# 二、前置状态

当前已经具备：

```text
Phase 8-P0：Python AI Provider / EmbeddingProvider / RerankerProvider
Phase 8-P1：JudgeProvider / RiskSignalClassifierProvider / ProviderCapabilityProfile
```

已完成能力包括：

```text
PythonProviderClient
ProviderValidationService
ProviderCapabilityPolicy
ProviderGovernanceSnapshot
Judge / Risk / Profile Evaluation Scorer
Trace / Audit / Candidate governance
```

Phase 8-P2 应复用这些对象，不得绕开既有 ProviderValidation、ProviderCapabilityPolicy、Evaluation 和 Candidate governance。

---

# 三、当前不做什么

Phase 8-P2 明确不做：

```text
1. 不做真实模型训练流水线。
2. 不做 LoRA / DPO / RLHF / RFT / distillation。
3. 不自动发布 TrainingDatasetVersion。
4. 不自动上线 ModelVersion。
5. 不自动替换 ProviderCapabilityProfile。
6. 不接真实外部云模型作为强依赖主线。
7. 不做完整 MLOps / 模型部署平台。
8. 不做 Model Console / Provider Console 的正式产品化界面。
9. 不让 ModelRegistry 决定 Runtime 主控。
10. 不让 PromptRegistry 绕过 DecisionBoundary。
11. 不让 TrainingDatasetVersion 包含未治理的 raw patient dialogue。
```

P2 可以做：

```text
ModelRegistry MVP
PromptRegistry MVP
TrainingDatasetVersion MVP
ModelExperimentRecord MVP
ModelEvaluationReport MVP
ModelReleaseCandidate MVP
ModelRollbackPlan MVP
Debug API / Governance API
Trace / Audit / Evaluation / Candidate 接入
```

---

# 四、Phase 8-P2 核心链路

目标链路：

```text
ProviderCapabilityProfile / ProviderCallRecord / EvaluationResult
↓
ModelRegistry 记录 model_id / model_version / capability_type / status
↓
PromptRegistry 记录 prompt_id / prompt_version / use_case / safety_tags
↓
TrainingDatasetVersion 记录 dataset_version / source candidates / governance status
↓
ModelExperimentRecord 记录实验配置、输入版本、输出指标
↓
ModelEvaluationReport 汇总 Provider / Judge / Risk / Evidence 指标
↓
ModelReleaseCandidate 形成待审核发布候选
↓
Review-required Candidate governance
↓
不自动发布、不自动上线、不自动替换 Runtime 能力
```

关键边界：

```text
Registry 只登记，不自动授权。
Experiment 只记录，不自动发布。
DatasetVersion 只候选化，不自动训练。
ReleaseCandidate 只进入 review，不自动上线。
```

---

# 五、核心对象设计

## 5.1 ModelRegistryEntry

用于登记模型版本。

建议字段：

```text
model_registry_id
model_id
model_version
provider_id
provider_version
capability_types
model_family
model_source
model_runtime
status
risk_level
created_at
created_by
notes
```

status 候选：

```text
DRAFT
ACTIVE
DEPRECATED
BLOCKED
ARCHIVED
```

model_source 候选：

```text
MOCK_RULE_BASED
LOCAL_MODEL
OPEN_SOURCE_MODEL
EXTERNAL_API
FINE_TUNED_MODEL
```

P2 默认以：

```text
MOCK_RULE_BASED
LOCAL_DETERMINISTIC
```

为主，不接真实训练模型。

## 5.2 PromptRegistryEntry

用于登记 prompt 版本。

建议字段：

```text
prompt_registry_id
prompt_id
prompt_version
use_case
capability_type
prompt_template_hash
prompt_summary
safety_tags
forbidden_output_types
requires_decision_boundary
status
created_at
created_by
```

注意：

```text
P2 不要求保存完整 prompt 原文。
可以只保存 prompt_summary / hash。
真实 prompt 原文后续可受权限控制存储。
```

## 5.3 TrainingDatasetVersion

用于登记训练数据集候选版本。

建议字段：

```text
dataset_version_id
dataset_name
dataset_version
source_candidate_ids
source_metric_ids
source_case_ids
data_scope
sample_count
safety_review_status
deidentification_status
publish_status
created_at
created_by
```

状态候选：

```text
DRAFT
REVIEW_REQUIRED
APPROVED_FOR_EXPERIMENT
REJECTED
ARCHIVED
```

约束：

```text
TrainingDatasetVersion 不自动发布。
不得包含未脱敏原始患者对话。
只能引用已治理 candidate / case / metric。
```

## 5.4 ModelExperimentRecord

用于记录一次模型能力实验。

建议字段：

```text
experiment_id
experiment_name
model_registry_id
prompt_registry_id
dataset_version_id
capability_type
use_case
evaluation_case_set_id
baseline_model_version
candidate_model_version
metrics_summary
failure_summary
boundary_violations
status
started_at
finished_at
created_by
```

status 候选：

```text
PLANNED
RUNNING
COMPLETED
FAILED
CANCELLED
```

## 5.5 ModelEvaluationReport

用于汇总模型实验评估结果。

建议字段：

```text
report_id
experiment_id
model_registry_id
prompt_registry_id
dataset_version_id
overall_status
metric_results
safety_findings
regression_findings
recommendation
created_at
```

recommendation 候选：

```text
NO_RELEASE
REVIEW_REQUIRED
APPROVE_FOR_SHADOW_TEST
APPROVE_FOR_LIMITED_USE
```

## 5.6 ModelReleaseCandidate

用于形成待审核发布候选。

建议字段：

```text
release_candidate_id
experiment_id
model_registry_id
prompt_registry_id
dataset_version_id
release_scope
recommended_action
risk_level
review_status
rollback_plan_id
created_at
```

约束：

```text
ModelReleaseCandidate 不能自动上线。
必须进入 Review / Governance。
```

## 5.7 ModelRollbackPlan

用于描述回滚计划。

建议字段：

```text
rollback_plan_id
release_candidate_id
previous_model_registry_id
previous_prompt_registry_id
rollback_trigger_conditions
rollback_steps
owner
status
```

---

# 六、Policy 与 Validation

Phase 8-P2 必须新增治理策略：

```text
ModelRegistryPolicy
PromptRegistryPolicy
TrainingDatasetVersionPolicy
ModelExperimentPolicy
ModelReleasePolicy
```

最低要求：

```text
1. 未登记 model_id / model_version 不能形成 release candidate。
2. patient-facing use_case 的 prompt 必须 requires_decision_boundary=true。
3. dataset 未通过脱敏检查不能 APPROVED_FOR_EXPERIMENT。
4. experiment 没有 EvaluationReport 不能形成 release candidate。
5. 存在 CRITICAL safety finding 时不能 approve。
6. release candidate 必须包含 rollback plan。
```

---

# 七、与 Provider 的关系

Phase 8-P2 不替代 Phase 8-P0 / P1，而是把它们纳入登记和归因。

Provider 调用时应逐步补充：

```text
model_registry_id
prompt_registry_id
dataset_version_id
experiment_id
```

P2 最小接入：

```text
ProviderGovernanceSnapshot
→ ModelExperimentRecord
→ ModelEvaluationReport
→ ModelReleaseCandidate / TrainingDatasetVersion candidate
```

注意：

```text
ProviderCapabilityPolicy 仍然决定能不能调用。
ProviderValidationService 仍然决定结果是否可采纳。
ModelRegistry 不能绕过二者。
```

---

# 八、与 Candidate / Governance 的关系

Phase 8-P2 可以新增候选类型或复用现有 Candidate 机制。

建议最小候选：

```text
ModelRegistryCandidate
PromptRegistryCandidate
TrainingDatasetVersionCandidate
ModelReleaseCandidate
RollbackPlanCandidate
```

如果暂时不新增强类型，也可以通过既有 CandidateGenerationService 先映射为 review-required governance candidate。

禁止：

```text
自动发布 ModelVersion。
自动发布 PromptVersion。
自动发布 DatasetVersion。
自动切换 ProviderCapabilityProfile。
```

---

# 九、Trace / Audit

新增 Audit action：

```text
CREATE_MODEL_REGISTRY_ENTRY
UPDATE_MODEL_REGISTRY_ENTRY
CREATE_PROMPT_REGISTRY_ENTRY
CREATE_TRAINING_DATASET_VERSION
CREATE_MODEL_EXPERIMENT_RECORD
CREATE_MODEL_EVALUATION_REPORT
CREATE_MODEL_RELEASE_CANDIDATE
CREATE_MODEL_ROLLBACK_PLAN
MODEL_GOVERNANCE_POLICY_REJECTED
```

Trace 至少记录：

```text
model_registry_id
model_id
model_version
prompt_registry_id
prompt_version
dataset_version_id
experiment_id
release_candidate_id
policy_status
validation_status
review_status
```

不得记录：

```text
未脱敏患者原文
完整患者对话
未经权限控制的完整 prompt
训练样本原文
```

---

# 十、Debug / Governance API

P2 推荐提供最小 debug / governance API：

```text
POST /api/v1/debug/model-governance/models
GET  /api/v1/debug/model-governance/models
GET  /api/v1/debug/model-governance/models/{model_registry_id}

POST /api/v1/debug/model-governance/prompts
GET  /api/v1/debug/model-governance/prompts

POST /api/v1/debug/model-governance/datasets
GET  /api/v1/debug/model-governance/datasets

POST /api/v1/debug/model-governance/experiments
GET  /api/v1/debug/model-governance/experiments/{experiment_id}

POST /api/v1/debug/model-governance/release-candidates
GET  /api/v1/debug/model-governance/release-candidates/{release_candidate_id}
```

权限：

```text
write：SYSTEM_ADMIN / EVALUATION_REVIEWER
read：SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER
PATIENT 禁止
```

---

# 十一、Evaluation Scorer

新增或扩展：

```text
ModelRegistryCompletenessScorer
PromptRegistrySafetyScorer
TrainingDatasetGovernanceScorer
ModelExperimentTraceScorer
ModelReleaseReadinessScorer
RollbackPlanCompletenessScorer
```

P2 最小：

```text
ModelRegistryCompletenessScorer
PromptRegistrySafetyScorer
TrainingDatasetGovernanceScorer
ModelExperimentTraceScorer
ModelReleaseReadinessScorer
```

---

# 十二、完成标准

Phase 8-P2 完成时必须满足：

```text
1. 可以登记 ModelRegistryEntry。
2. 可以登记 PromptRegistryEntry。
3. 可以生成 TrainingDatasetVersion 草稿。
4. 可以创建 ModelExperimentRecord。
5. 可以生成 ModelEvaluationReport。
6. 可以生成 ModelReleaseCandidate。
7. release candidate 必须 review-required，不自动上线。
8. dataset 未脱敏不能进入 approved-for-experiment。
9. patient-facing prompt 必须 requires_decision_boundary=true。
10. Evaluation Scorer 可以评估 model governance 完整性。
11. Audit / Trace 可见。
12. Debug / Governance API 可用。
13. `mvn test` 通过。
14. Phase 1–8 P1 既有测试不回归。
```

---

# 十三、后置任务

Phase 8-P2 不完成但可后置：

```text
1. 真实训练流水线。
2. LoRA / DPO / RFT / distillation。
3. 真实 LLM Judge。
4. Production ModelRegistry。
5. Prompt 原文加密存储与权限控制。
6. Model Console / Provider Console。
7. 自动灰度发布。
8. 在线回滚系统。
```

---

# 十四、最终结论

Phase 8-P2 的本质是：

```text
从“模型能力可以被调用和评估”
升级为“模型能力可以被版本化、实验化、候选化和治理化”。
```

它完成后，ClinMindRuntime 的模型能力域将具备最小治理闭环：

```text
Provider Capability
→ Model / Prompt / Dataset version
→ Experiment
→ Evaluation Report
→ Release Candidate
→ Review-required Governance
```

但仍然不做自动训练、自动发布或自动上线。
