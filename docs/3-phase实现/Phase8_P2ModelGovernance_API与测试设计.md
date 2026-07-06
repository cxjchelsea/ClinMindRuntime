# Phase 8-P2 Model Governance API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase8_P2ModelRegistry与PromptRegistry_实现规格.md`  
> 前置冻结：`docs/3-phase实现/Phase8_P1冻结记录.md`  
> 当前 Phase：Phase 8-P2  
> 当前目标：定义 ModelRegistry、PromptRegistry、TrainingDatasetVersion、ModelExperimentRecord、ModelEvaluationReport、ModelReleaseCandidate 的 API、DTO、Policy、Evaluation Scorer 与测试方案。

---

# 一、API 设计原则

Phase 8-P2 API 是 governance / debug API，不是 patient-facing API。

共同原则：

```text
1. PATIENT 角色禁止访问。
2. 所有写操作必须 Audit。
3. 所有对象必须携带 version / status。
4. 所有 release candidate 必须 review-required。
5. API 不返回未脱敏患者原文。
6. API 不返回未经权限控制的完整 prompt 原文。
7. API 不触发真实训练。
8. API 不自动发布模型、prompt 或 dataset。
```

---

# 二、API 分组

P2 推荐路径前缀：

```text
/api/v1/debug/model-governance
```

端点分组：

```text
Model Registry API
Prompt Registry API
Training Dataset Version API
Model Experiment API
Model Evaluation Report API
Model Release Candidate API
Rollback Plan API
```

---

# 三、Model Registry API

## 3.1 POST /models

请求：

```json
{
  "model_id": "mock_judge_model",
  "model_version": "0.1.0",
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.1-p1",
  "capability_types": ["JUDGE"],
  "model_family": "mock-rule-based",
  "model_source": "MOCK_RULE_BASED",
  "model_runtime": "python-provider",
  "risk_level": "LOW",
  "notes": "Phase 8-P1 deterministic judge model"
}
```

响应：

```json
{
  "model_registry_id": "model_reg_001",
  "model_id": "mock_judge_model",
  "model_version": "0.1.0",
  "status": "DRAFT",
  "created_at": "2026-07-03T10:00:00Z"
}
```

## 3.2 GET /models

返回 Safe DTO 列表。

## 3.3 GET /models/{model_registry_id}

返回模型登记详情，不包含敏感训练数据。

---

# 四、Prompt Registry API

## 4.1 POST /prompts

请求：

```json
{
  "prompt_id": "patient_boundary_judge_prompt",
  "prompt_version": "0.1.0",
  "use_case": "output_boundary_check",
  "capability_type": "JUDGE",
  "prompt_template_hash": "sha256:xxxx",
  "prompt_summary": "Judge patient-facing draft for diagnosis leakage and treatment instruction leakage.",
  "safety_tags": ["patient_boundary", "diagnosis_leak_check"],
  "forbidden_output_types": ["Final Diagnosis", "Treatment Instruction"],
  "requires_decision_boundary": true
}
```

约束：

```text
patient-facing use_case 必须 requires_decision_boundary=true。
prompt_summary 可以存，完整 prompt 原文 P2 不强制存储。
```

---

# 五、TrainingDatasetVersion API

## 5.1 POST /datasets

请求：

```json
{
  "dataset_name": "patient_boundary_judge_seed",
  "dataset_version": "0.1.0",
  "source_candidate_ids": ["candidate_001"],
  "source_metric_ids": ["judge_boundary_agreement"],
  "data_scope": "evaluation_seed",
  "sample_count": 12,
  "deidentification_status": "PASSED"
}
```

响应：

```json
{
  "dataset_version_id": "dataset_ver_001",
  "dataset_name": "patient_boundary_judge_seed",
  "dataset_version": "0.1.0",
  "safety_review_status": "REVIEW_REQUIRED",
  "publish_status": "DRAFT"
}
```

约束：

```text
未脱敏数据不能进入 APPROVED_FOR_EXPERIMENT。
DatasetVersion 只能引用 candidate / metric / case id。
不得直接保存 raw patient dialogue。
```

---

# 六、Model Experiment API

## 6.1 POST /experiments

请求：

```json
{
  "experiment_name": "judge_model_boundary_eval_001",
  "model_registry_id": "model_reg_001",
  "prompt_registry_id": "prompt_reg_001",
  "dataset_version_id": "dataset_ver_001",
  "capability_type": "JUDGE",
  "use_case": "output_boundary_check",
  "evaluation_case_set_id": "case_set_001",
  "baseline_model_version": "0.0.1",
  "candidate_model_version": "0.1.0"
}
```

响应：

```json
{
  "experiment_id": "model_exp_001",
  "status": "PLANNED"
}
```

---

# 七、Model Evaluation Report API

## 7.1 POST /evaluation-reports

请求：

```json
{
  "experiment_id": "model_exp_001",
  "model_registry_id": "model_reg_001",
  "prompt_registry_id": "prompt_reg_001",
  "dataset_version_id": "dataset_ver_001",
  "overall_status": "REVIEW_REQUIRED",
  "metric_result_ids": ["metric_001", "metric_002"],
  "safety_finding_ids": [],
  "regression_finding_ids": [],
  "recommendation": "REVIEW_REQUIRED"
}
```

约束：

```text
存在 CRITICAL safety finding 时 recommendation 不能为 APPROVE_FOR_LIMITED_USE。
没有 experiment_id 时拒绝。
```

---

# 八、Model Release Candidate API

## 8.1 POST /release-candidates

请求：

```json
{
  "experiment_id": "model_exp_001",
  "model_registry_id": "model_reg_001",
  "prompt_registry_id": "prompt_reg_001",
  "dataset_version_id": "dataset_ver_001",
  "release_scope": "shadow_test",
  "recommended_action": "APPROVE_FOR_SHADOW_TEST",
  "risk_level": "MEDIUM",
  "rollback_plan_id": "rollback_001"
}
```

响应：

```json
{
  "release_candidate_id": "model_release_candidate_001",
  "review_status": "REVIEW_REQUIRED",
  "auto_publish": false
}
```

约束：

```text
review_status 必须是 REVIEW_REQUIRED。
auto_publish 永远 false。
缺 rollback_plan_id 时拒绝。
```

---

# 九、Rollback Plan API

## 9.1 POST /rollback-plans

请求：

```json
{
  "previous_model_registry_id": "model_reg_previous",
  "previous_prompt_registry_id": "prompt_reg_previous",
  "rollback_trigger_conditions": ["critical_boundary_violation", "provider_error_rate_high"],
  "rollback_steps": ["disable candidate model", "restore previous profile", "run regression eval"],
  "owner": "system_admin"
}
```

---

# 十、Policy 测试

## 10.1 ModelRegistryPolicyTest

覆盖：

```text
1. model_id / model_version 缺失拒绝。
2. capability_types 为空拒绝。
3. unknown model_source 拒绝。
4. BLOCKED model 不允许创建 release candidate。
```

## 10.2 PromptRegistryPolicyTest

覆盖：

```text
1. patient-facing prompt requires_decision_boundary=false 拒绝。
2. forbidden_output_types 为空时对 patient-facing use_case 拒绝。
3. prompt_template_hash 缺失时降级或拒绝。
```

## 10.3 TrainingDatasetVersionPolicyTest

覆盖：

```text
1. deidentification_status != PASSED 时不能 APPROVED_FOR_EXPERIMENT。
2. raw patient dialogue 字段存在时拒绝。
3. source_candidate_ids / source_metric_ids 为空时降级。
```

## 10.4 ModelReleasePolicyTest

覆盖：

```text
1. 无 evaluation report 拒绝。
2. 有 CRITICAL safety finding 拒绝。
3. 缺 rollback plan 拒绝。
4. auto_publish=true 拒绝。
```

---

# 十一、API Controller 测试

建议测试类：

```text
ModelGovernanceDebugControllerTest
ModelRegistryApiTest
PromptRegistryApiTest
TrainingDatasetVersionApiTest
ModelExperimentApiTest
ModelReleaseCandidateApiTest
```

覆盖：

```text
1. SYSTEM_ADMIN / EVALUATION_REVIEWER 可写。
2. READ_ONLY_OBSERVER 只读。
3. PATIENT 禁止访问。
4. 写操作记录 Audit。
5. Safe DTO 不泄露 raw text / prompt 原文。
```

---

# 十二、Evaluation Scorer 测试

新增测试：

```text
ModelRegistryCompletenessScorerTest
PromptRegistrySafetyScorerTest
TrainingDatasetGovernanceScorerTest
ModelExperimentTraceScorerTest
ModelReleaseReadinessScorerTest
RollbackPlanCompletenessScorerTest
```

覆盖：

```text
1. 缺 model_version 得分失败。
2. prompt requires_decision_boundary=false 得分失败。
3. dataset 未脱敏得分失败。
4. experiment 缺 dataset_version_id 得分失败。
5. release candidate 缺 rollback plan 得分失败。
```

---

# 十三、Candidate Mapping 测试

新增或扩展：

```text
ModelGovernanceCandidateMappingTest
```

覆盖：

```text
1. model registry metric failure → review-required governance candidate。
2. prompt safety metric failure → prompt governance candidate。
3. dataset governance failure → dataset review candidate。
4. release readiness failure → release review candidate。
5. 不自动发布任何 candidate。
```

---

# 十四、人工测试场景

## 场景 1：登记模型版本

预期：

```text
model_registry_id 生成。
status=DRAFT。
Audit 记录 CREATE_MODEL_REGISTRY_ENTRY。
```

## 场景 2：登记 patient-facing prompt

输入：

```text
requires_decision_boundary=false
```

预期：

```text
Policy rejected。
不创建 ACTIVE prompt。
```

## 场景 3：创建未脱敏 dataset version

预期：

```text
不能 APPROVED_FOR_EXPERIMENT。
只能 REVIEW_REQUIRED / REJECTED。
```

## 场景 4：创建 model experiment

预期：

```text
experiment 记录 model / prompt / dataset 版本。
不触发真实训练。
```

## 场景 5：生成 release candidate

缺 rollback plan 时：

```text
拒绝。
```

有 rollback plan 时：

```text
生成 REVIEW_REQUIRED candidate。
auto_publish=false。
```

---

# 十五、回归测试要求

Phase 8-P2 完成前必须通过：

```text
mvn test
```

如修改 python-provider：

```text
python-provider pytest
```

不得破坏：

```text
Phase 8-P0 ProviderClient / Embedding / Rerank
Phase 8-P1 Judge / Risk / CapabilityProfile
Phase 7 Evidence / Graph
Phase 6 Agent Runtime
SafetyGate
DecisionBoundary
EvaluationRunner
Candidate / Review
Persistence / Audit
Console Safe DTO
```

---

# 十六、完成标准

Phase 8-P2 API 与测试完成标准：

```text
1. ModelRegistryEntry 可创建 / 查询。
2. PromptRegistryEntry 可创建 / 查询。
3. TrainingDatasetVersion 可创建 / 查询。
4. ModelExperimentRecord 可创建 / 查询。
5. ModelEvaluationReport 可创建。
6. ModelReleaseCandidate 可创建但 review-required。
7. RollbackPlan 可创建并被 release candidate 引用。
8. Policy 能拒绝不安全 prompt、未脱敏 dataset、缺 rollback 的 release。
9. Evaluation Scorer 能识别治理缺口。
10. Audit / Trace 可见。
11. Safe DTO 不泄露敏感内容。
12. `mvn test` 通过。
13. Phase 1–8 P1 既有测试不回归。
```

---

# 十七、最终结论

Phase 8-P2 API 与测试重点不是“训练模型”，而是证明：

```text
模型、prompt、数据集、实验、评估报告和发布候选都可以被最小治理化；
任何上线、发布、训练或替换能力都必须进入 review-required；
Runtime 主控、ProviderValidation、安全边界和 Candidate governance 不被破坏。
```
