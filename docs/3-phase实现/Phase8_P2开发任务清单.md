# Phase 8-P2 开发任务清单：ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP

> 上位实现规格：`docs/3-phase实现/Phase8_P2ModelRegistry与PromptRegistry_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase8_P2ModelGovernance_API与测试设计.md`  
> 前置冻结：`docs/3-phase实现/Phase8_P1冻结记录.md`  
> 当前目标：建立模型能力治理注册层的最小闭环，但不做真实训练、不做自动发布、不做生产级 MLOps。

---

# 一、Phase 8-P2 总目标

Phase 8-P2 要完成的不是训练平台，而是模型治理最小闭环：

```text
ModelRegistryEntry
→ PromptRegistryEntry
→ TrainingDatasetVersion
→ ModelExperimentRecord
→ ModelEvaluationReport
→ ModelReleaseCandidate
→ RollbackPlan
→ Review-required governance
```

最终要证明：

```text
模型能力可以被版本化登记；
prompt 可以被版本化登记；
训练数据集可以作为候选版本被治理；
模型实验可以被记录；
评估报告可以归因；
发布候选必须进入人工审核；
任何训练、发布、上线都不会自动发生。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P8P2-A | 建立 model governance domain 对象 | 已完成 |
| P8P2-B | 建立 in-memory store / repository | 已完成 |
| P8P2-C | 实现 ModelRegistryPolicy / PromptRegistryPolicy | 已完成 |
| P8P2-D | 实现 TrainingDatasetVersionPolicy | 已完成 |
| P8P2-E | 实现 ModelExperiment / EvaluationReport / ReleasePolicy | 已完成 |
| P8P2-F | 实现 ModelGovernanceService | 已完成 |
| P8P2-G | 实现 Debug / Governance API | 已完成 |
| P8P2-H | Trace / Audit 接入 | 已完成 |
| P8P2-I | Evaluation Scorer 接入 | 已完成 |
| P8P2-J | Candidate Mapping 接入 | 已完成 |
| P8P2-K | 测试、人工验证与冻结记录 | 已完成 |

---

# 三、P8P2-A：建立 model governance domain 对象

## 目标

新增模型治理对象。

## 建议包路径

```text
src/main/java/com/clinmind/runtime/modelgov/
src/main/java/com/clinmind/runtime/modelgov/model/
src/main/java/com/clinmind/runtime/modelgov/prompt/
src/main/java/com/clinmind/runtime/modelgov/dataset/
src/main/java/com/clinmind/runtime/modelgov/experiment/
src/main/java/com/clinmind/runtime/modelgov/release/
```

## 任务

```text
[x] 新增 ModelRegistryEntry。
[x] 新增 ModelRegistryStatus。
[x] 新增 ModelSource。
[x] 新增 PromptRegistryEntry。
[x] 新增 PromptRegistryStatus。
[x] 新增 TrainingDatasetVersion。
[x] 新增 DatasetReviewStatus。
[x] 新增 DeidentificationStatus。
[x] 新增 ModelExperimentRecord。
[x] 新增 ModelExperimentStatus。
[x] 新增 ModelEvaluationReport。
[x] 新增 ModelReleaseCandidate。
[x] 新增 ModelReleaseReviewStatus。
[x] 新增 ModelRollbackPlan。
```

## 验收标准

```text
[x] 所有对象包含 id / version / status。
[x] 不保存 raw patient dialogue。
[x] 不保存完整 prompt 原文。
[x] ReleaseCandidate 默认 REVIEW_REQUIRED。
```

---

# 四、P8P2-B：建立 in-memory store / repository

## 目标

先用 in-memory store 完成 P2 MVP，不直接做生产级持久化。

## 任务

```text
[x] 新增 ModelRegistryStore。
[x] 新增 PromptRegistryStore。
[x] 新增 TrainingDatasetVersionStore。
[x] 新增 ModelExperimentStore。
[x] 新增 ModelEvaluationReportStore。
[x] 新增 ModelReleaseCandidateStore。
[x] 新增 ModelRollbackPlanStore。
[x] 支持 create / findById / findAll。
```

## 验收标准

```text
[x] 所有 store 线程安全或使用 ConcurrentHashMap。
[x] 不暴露可变内部集合。
[x] 单元测试覆盖 create / find。
```

---

# 五、P8P2-C：实现 ModelRegistryPolicy / PromptRegistryPolicy

## 目标

控制模型和 prompt 登记边界。

## 任务

```text
[x] 新增 ModelRegistryPolicy。
[x] 校验 model_id / model_version。
[x] 校验 provider_id / provider_version。
[x] 校验 capability_types 非空。
[x] 校验 model_source 合法。
[x] BLOCKED model 不允许 release。
[x] 新增 PromptRegistryPolicy。
[x] patient-facing prompt requires_decision_boundary 必须 true。
[x] patient-facing prompt forbidden_output_types 不得为空。
[x] prompt_template_hash 必须存在。
```

## 验收标准

```text
[x] 缺版本拒绝。
[x] unsafe prompt 拒绝。
[x] 所有拒绝带 reasons。
```

---

# 六、P8P2-D：实现 TrainingDatasetVersionPolicy

## 目标

确保 dataset version 只作为治理候选，不自动训练。

## 任务

```text
[x] 新增 TrainingDatasetVersionPolicy。
[x] 未脱敏数据不能 APPROVED_FOR_EXPERIMENT。
[x] raw patient dialogue 字段存在时拒绝。
[x] source_candidate_ids / source_metric_ids 缺失时降级或拒绝。
[x] publish_status 默认 DRAFT。
[x] safety_review_status 默认 REVIEW_REQUIRED。
```

## 验收标准

```text
[x] deidentification_status != PASSED 时不能通过。
[x] 不允许 auto_publish。
[x] 不允许 raw text。
```

---

# 七、P8P2-E：实现 ModelExperiment / EvaluationReport / ReleasePolicy

## 目标

让模型实验和发布候选具备治理边界。

## 任务

```text
[x] 新增 ModelExperimentPolicy。
[x] experiment 必须关联 model / prompt / dataset。
[x] 新增 ModelEvaluationReportPolicy。
[x] report 必须关联 experiment。
[x] 有 CRITICAL safety finding 时 recommendation 不能 approve。
[x] 新增 ModelReleasePolicy。
[x] release candidate 必须关联 evaluation report。
[x] release candidate 必须有 rollback plan。
[x] auto_publish 永远 false。
```

## 验收标准

```text
[x] 缺 evaluation report 拒绝 release。
[x] 缺 rollback plan 拒绝 release。
[x] 有 critical finding 拒绝 release。
```

---

# 八、P8P2-F：实现 ModelGovernanceService

## 目标

统一编排 model governance 对象创建、policy、store、audit。

## 任务

```text
[x] 新增 ModelGovernanceService。
[x] createModelRegistryEntry。
[x] createPromptRegistryEntry。
[x] createTrainingDatasetVersion。
[x] createModelExperimentRecord。
[x] createModelEvaluationReport。
[x] createModelRollbackPlan。
[x] createModelReleaseCandidate。
[x] 每个 create 调用对应 policy。
[x] policy rejected 时不写入 store。
```

## 验收标准

```text
[x] Service 不绕过 policy。
[x] Service 不触发真实训练。
[x] Service 不自动发布。
```

---

# 九、P8P2-G：实现 Debug / Governance API

## 目标

提供最小治理 API。

## 任务

```text
[x] 新增 ModelGovernanceDebugController。
[x] POST /api/v1/debug/model-governance/models。
[x] GET /api/v1/debug/model-governance/models。
[x] POST /api/v1/debug/model-governance/prompts。
[x] GET /api/v1/debug/model-governance/prompts。
[x] POST /api/v1/debug/model-governance/datasets。
[x] GET /api/v1/debug/model-governance/datasets。
[x] POST /api/v1/debug/model-governance/experiments。
[x] GET /api/v1/debug/model-governance/experiments/{id}。
[x] POST /api/v1/debug/model-governance/evaluation-reports。
[x] POST /api/v1/debug/model-governance/rollback-plans。
[x] POST /api/v1/debug/model-governance/release-candidates。
[x] 所有响应 Safe DTO。
[x] 接入 AccessPolicy。
```

## 验收标准

```text
[x] SYSTEM_ADMIN / EVALUATION_REVIEWER 可写。
[x] READ_ONLY_OBSERVER 只读。
[x] PATIENT 禁止。
[x] Safe DTO 不泄露敏感内容。
```

---

# 十、P8P2-H：Trace / Audit 接入

## 目标

让治理操作可复盘。

## 任务

```text
[x] AuditActionType 新增 CREATE_MODEL_REGISTRY_ENTRY。
[x] AuditActionType 新增 CREATE_PROMPT_REGISTRY_ENTRY。
[x] AuditActionType 新增 CREATE_TRAINING_DATASET_VERSION。
[x] AuditActionType 新增 CREATE_MODEL_EXPERIMENT_RECORD。
[x] AuditActionType 新增 CREATE_MODEL_EVALUATION_REPORT。
[x] AuditActionType 新增 CREATE_MODEL_RELEASE_CANDIDATE。
[x] AuditActionType 新增 CREATE_MODEL_ROLLBACK_PLAN。
[x] AuditActionType 新增 MODEL_GOVERNANCE_POLICY_REJECTED。
[x] Service 写操作记录 Audit。
```

## 验收标准

```text
[x] 每个写操作有 audit record。
[x] policy rejected 有 audit record。
[x] audit 不包含 raw text。
```

---

# 十一、P8P2-I：Evaluation Scorer 接入

## 目标

让 model governance 进入 Evaluation。

## 任务

```text
[x] 新增 ModelRegistryCompletenessScorer。
[x] 新增 PromptRegistrySafetyScorer。
[x] 新增 TrainingDatasetGovernanceScorer。
[x] 新增 ModelExperimentTraceScorer。
[x] 新增 ModelReleaseReadinessScorer。
[x] 可选 RollbackPlanCompletenessScorer。
[x] 支持 model_governance_eval tag。
```

## 验收标准

```text
[x] 缺 model_version 得分失败。
[x] unsafe prompt 得分失败。
[x] dataset 未脱敏得分失败。
[x] release 缺 rollback plan 得分失败。
[x] 默认 case 不受新 scorer 影响。
```

---

# 十二、P8P2-J：Candidate Mapping 接入

## 目标

治理缺口可以沉淀为待审核 candidate。

## 任务

```text
[x] 扩展 CandidateMappingPolicy。
[x] model registry failure → governance candidate。
[x] prompt safety failure → patient boundary / prompt governance candidate。
[x] dataset governance failure → dataset review candidate。
[x] release readiness failure → release review candidate。
[x] 确保所有 candidate review_required。
```

## 验收标准

```text
[x] 不自动发布。
[x] candidate risk level 合理。
[x] CandidateMappingPolicyTest 覆盖。
```

---

# 十三、P8P2-K：测试、人工验证与冻结记录

## 目标

完成 Phase 8-P2 收口。

## 任务

```text
[x] 完成 ModelRegistryPolicyTest。
[x] 完成 PromptRegistryPolicyTest。
[x] 完成 TrainingDatasetVersionPolicyTest。
[x] 完成 ModelReleasePolicyTest。
[x] 完成 ModelGovernanceDebugControllerTest。
[x] 完成 ModelGovernanceScorerTest。
[x] 完成 ModelGovernanceCandidateMappingTest。
[x] 运行 mvn test。
[x] 未修改 python-provider，pytest 不适用。
[x] 编写 Phase8_P2人工测试结果.md。
[x] 编写 Phase8_P2冻结记录.md。
[x] 更新 AI_IMPLEMENTATION_SKILL.md 为 Phase 8-P2 已冻结。
```

## 验收标准

```text
[x] Java 测试通过。
[x] Phase 1–8 P1 回归不破坏。
[x] 人工测试覆盖模型登记、prompt policy、dataset 脱敏、experiment、release candidate、rollback。
[x] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十四、推荐实现顺序

建议严格按以下顺序实现：

```text
1. P8P2-A：model governance domain。
2. P8P2-B：in-memory store。
3. P8P2-C：Model / Prompt policy。
4. P8P2-D：Dataset policy。
5. P8P2-E：Experiment / Report / Release policy。
6. P8P2-F：ModelGovernanceService。
7. P8P2-G：Debug / Governance API。
8. P8P2-H：Trace / Audit。
9. P8P2-I：Evaluation Scorer。
10. P8P2-J：Candidate Mapping。
11. P8P2-K：测试、人工验证、冻结记录。
```

---

# 十五、开发期间禁止事项

```text
1. 不做真实模型训练。
2. 不做 LoRA / DPO / RFT / distillation。
3. 不自动发布 dataset / model / prompt。
4. 不自动切换 ProviderCapabilityProfile。
5. 不绕过 ProviderCapabilityPolicy。
6. 不绕过 ProviderValidationService。
7. 不保存 raw patient dialogue。
8. 不暴露完整 prompt 原文。
9. 不让 ModelRegistry 决定 Runtime 主控。
10. 不改写 Phase 1–8 P1 冻结记录。
```

---

# 十六、Phase 8-P2 完成后的后置任务

```text
1. Phase 8-P3 或 Phase 10：Model Console / Provider Console。
2. Phase 9：Tool / MCP / Skills 受控接入。
3. 后置研究：真实模型训练 / LoRA / DPO / RFT。
4. 后置生产能力：灰度发布 / 回滚 / MLOps。
```

---

# 十七、最终 Definition of Done

Phase 8-P2 完成的最终标准：

```text
[x] ModelRegistryEntry 可创建 / 查询。
[x] PromptRegistryEntry 可创建 / 查询。
[x] TrainingDatasetVersion 可创建 / 查询。
[x] ModelExperimentRecord 可创建 / 查询。
[x] ModelEvaluationReport 可创建。
[x] ModelReleaseCandidate 可创建但 review-required。
[x] RollbackPlan 可创建并被 release candidate 引用。
[x] Policy 能拒绝不安全 prompt / 未脱敏 dataset / 缺 rollback release。
[x] Evaluation Scorer 可识别治理缺口。
[x] Candidate Mapping 可沉淀待审核治理候选。
[x] Audit / Trace 可见。
[x] Java 测试通过。
[x] Phase8_P2冻结记录完成。
```
