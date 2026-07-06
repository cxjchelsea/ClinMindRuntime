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
| P8P2-A | 建立 model governance domain 对象 | 待做 |
| P8P2-B | 建立 in-memory store / repository | 待做 |
| P8P2-C | 实现 ModelRegistryPolicy / PromptRegistryPolicy | 待做 |
| P8P2-D | 实现 TrainingDatasetVersionPolicy | 待做 |
| P8P2-E | 实现 ModelExperiment / EvaluationReport / ReleasePolicy | 待做 |
| P8P2-F | 实现 ModelGovernanceService | 待做 |
| P8P2-G | 实现 Debug / Governance API | 待做 |
| P8P2-H | Trace / Audit 接入 | 待做 |
| P8P2-I | Evaluation Scorer 接入 | 待做 |
| P8P2-J | Candidate Mapping 接入 | 待做 |
| P8P2-K | 测试、人工验证与冻结记录 | 待做 |

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
[ ] 新增 ModelRegistryEntry。
[ ] 新增 ModelRegistryStatus。
[ ] 新增 ModelSource。
[ ] 新增 PromptRegistryEntry。
[ ] 新增 PromptRegistryStatus。
[ ] 新增 TrainingDatasetVersion。
[ ] 新增 DatasetReviewStatus。
[ ] 新增 DeidentificationStatus。
[ ] 新增 ModelExperimentRecord。
[ ] 新增 ModelExperimentStatus。
[ ] 新增 ModelEvaluationReport。
[ ] 新增 ModelReleaseCandidate。
[ ] 新增 ModelReleaseReviewStatus。
[ ] 新增 ModelRollbackPlan。
```

## 验收标准

```text
[ ] 所有对象包含 id / version / status。
[ ] 不保存 raw patient dialogue。
[ ] 不保存完整 prompt 原文。
[ ] ReleaseCandidate 默认 REVIEW_REQUIRED。
```

---

# 四、P8P2-B：建立 in-memory store / repository

## 目标

先用 in-memory store 完成 P2 MVP，不直接做生产级持久化。

## 任务

```text
[ ] 新增 ModelRegistryStore。
[ ] 新增 PromptRegistryStore。
[ ] 新增 TrainingDatasetVersionStore。
[ ] 新增 ModelExperimentStore。
[ ] 新增 ModelEvaluationReportStore。
[ ] 新增 ModelReleaseCandidateStore。
[ ] 新增 ModelRollbackPlanStore。
[ ] 支持 create / findById / findAll。
```

## 验收标准

```text
[ ] 所有 store 线程安全或使用 ConcurrentHashMap。
[ ] 不暴露可变内部集合。
[ ] 单元测试覆盖 create / find。
```

---

# 五、P8P2-C：实现 ModelRegistryPolicy / PromptRegistryPolicy

## 目标

控制模型和 prompt 登记边界。

## 任务

```text
[ ] 新增 ModelRegistryPolicy。
[ ] 校验 model_id / model_version。
[ ] 校验 provider_id / provider_version。
[ ] 校验 capability_types 非空。
[ ] 校验 model_source 合法。
[ ] BLOCKED model 不允许 release。
[ ] 新增 PromptRegistryPolicy。
[ ] patient-facing prompt requires_decision_boundary 必须 true。
[ ] patient-facing prompt forbidden_output_types 不得为空。
[ ] prompt_template_hash 必须存在。
```

## 验收标准

```text
[ ] 缺版本拒绝。
[ ] unsafe prompt 拒绝。
[ ] 所有拒绝带 reasons。
```

---

# 六、P8P2-D：实现 TrainingDatasetVersionPolicy

## 目标

确保 dataset version 只作为治理候选，不自动训练。

## 任务

```text
[ ] 新增 TrainingDatasetVersionPolicy。
[ ] 未脱敏数据不能 APPROVED_FOR_EXPERIMENT。
[ ] raw patient dialogue 字段存在时拒绝。
[ ] source_candidate_ids / source_metric_ids 缺失时降级或拒绝。
[ ] publish_status 默认 DRAFT。
[ ] safety_review_status 默认 REVIEW_REQUIRED。
```

## 验收标准

```text
[ ] deidentification_status != PASSED 时不能通过。
[ ] 不允许 auto_publish。
[ ] 不允许 raw text。
```

---

# 七、P8P2-E：实现 ModelExperiment / EvaluationReport / ReleasePolicy

## 目标

让模型实验和发布候选具备治理边界。

## 任务

```text
[ ] 新增 ModelExperimentPolicy。
[ ] experiment 必须关联 model / prompt / dataset。
[ ] 新增 ModelEvaluationReportPolicy。
[ ] report 必须关联 experiment。
[ ] 有 CRITICAL safety finding 时 recommendation 不能 approve。
[ ] 新增 ModelReleasePolicy。
[ ] release candidate 必须关联 evaluation report。
[ ] release candidate 必须有 rollback plan。
[ ] auto_publish 永远 false。
```

## 验收标准

```text
[ ] 缺 evaluation report 拒绝 release。
[ ] 缺 rollback plan 拒绝 release。
[ ] 有 critical finding 拒绝 release。
```

---

# 八、P8P2-F：实现 ModelGovernanceService

## 目标

统一编排 model governance 对象创建、policy、store、audit。

## 任务

```text
[ ] 新增 ModelGovernanceService。
[ ] createModelRegistryEntry。
[ ] createPromptRegistryEntry。
[ ] createTrainingDatasetVersion。
[ ] createModelExperimentRecord。
[ ] createModelEvaluationReport。
[ ] createModelRollbackPlan。
[ ] createModelReleaseCandidate。
[ ] 每个 create 调用对应 policy。
[ ] policy rejected 时不写入 store。
```

## 验收标准

```text
[ ] Service 不绕过 policy。
[ ] Service 不触发真实训练。
[ ] Service 不自动发布。
```

---

# 九、P8P2-G：实现 Debug / Governance API

## 目标

提供最小治理 API。

## 任务

```text
[ ] 新增 ModelGovernanceDebugController。
[ ] POST /api/v1/debug/model-governance/models。
[ ] GET /api/v1/debug/model-governance/models。
[ ] POST /api/v1/debug/model-governance/prompts。
[ ] GET /api/v1/debug/model-governance/prompts。
[ ] POST /api/v1/debug/model-governance/datasets。
[ ] GET /api/v1/debug/model-governance/datasets。
[ ] POST /api/v1/debug/model-governance/experiments。
[ ] GET /api/v1/debug/model-governance/experiments/{id}。
[ ] POST /api/v1/debug/model-governance/evaluation-reports。
[ ] POST /api/v1/debug/model-governance/rollback-plans。
[ ] POST /api/v1/debug/model-governance/release-candidates。
[ ] 所有响应 Safe DTO。
[ ] 接入 AccessPolicy。
```

## 验收标准

```text
[ ] SYSTEM_ADMIN / EVALUATION_REVIEWER 可写。
[ ] READ_ONLY_OBSERVER 只读。
[ ] PATIENT 禁止。
[ ] Safe DTO 不泄露敏感内容。
```

---

# 十、P8P2-H：Trace / Audit 接入

## 目标

让治理操作可复盘。

## 任务

```text
[ ] AuditActionType 新增 CREATE_MODEL_REGISTRY_ENTRY。
[ ] AuditActionType 新增 CREATE_PROMPT_REGISTRY_ENTRY。
[ ] AuditActionType 新增 CREATE_TRAINING_DATASET_VERSION。
[ ] AuditActionType 新增 CREATE_MODEL_EXPERIMENT_RECORD。
[ ] AuditActionType 新增 CREATE_MODEL_EVALUATION_REPORT。
[ ] AuditActionType 新增 CREATE_MODEL_RELEASE_CANDIDATE。
[ ] AuditActionType 新增 CREATE_MODEL_ROLLBACK_PLAN。
[ ] AuditActionType 新增 MODEL_GOVERNANCE_POLICY_REJECTED。
[ ] Service 写操作记录 Audit。
```

## 验收标准

```text
[ ] 每个写操作有 audit record。
[ ] policy rejected 有 audit record。
[ ] audit 不包含 raw text。
```

---

# 十一、P8P2-I：Evaluation Scorer 接入

## 目标

让 model governance 进入 Evaluation。

## 任务

```text
[ ] 新增 ModelRegistryCompletenessScorer。
[ ] 新增 PromptRegistrySafetyScorer。
[ ] 新增 TrainingDatasetGovernanceScorer。
[ ] 新增 ModelExperimentTraceScorer。
[ ] 新增 ModelReleaseReadinessScorer。
[ ] 可选 RollbackPlanCompletenessScorer。
[ ] 支持 model_governance_eval tag。
```

## 验收标准

```text
[ ] 缺 model_version 得分失败。
[ ] unsafe prompt 得分失败。
[ ] dataset 未脱敏得分失败。
[ ] release 缺 rollback plan 得分失败。
[ ] 默认 case 不受新 scorer 影响。
```

---

# 十二、P8P2-J：Candidate Mapping 接入

## 目标

治理缺口可以沉淀为待审核 candidate。

## 任务

```text
[ ] 扩展 CandidateMappingPolicy。
[ ] model registry failure → governance candidate。
[ ] prompt safety failure → patient boundary / prompt governance candidate。
[ ] dataset governance failure → dataset review candidate。
[ ] release readiness failure → release review candidate。
[ ] 确保所有 candidate review_required。
```

## 验收标准

```text
[ ] 不自动发布。
[ ] candidate risk level 合理。
[ ] CandidateMappingPolicyTest 覆盖。
```

---

# 十三、P8P2-K：测试、人工验证与冻结记录

## 目标

完成 Phase 8-P2 收口。

## 任务

```text
[ ] 完成 ModelRegistryPolicyTest。
[ ] 完成 PromptRegistryPolicyTest。
[ ] 完成 TrainingDatasetVersionPolicyTest。
[ ] 完成 ModelReleasePolicyTest。
[ ] 完成 ModelGovernanceDebugControllerTest。
[ ] 完成 ModelGovernanceScorerTest。
[ ] 完成 ModelGovernanceCandidateMappingTest。
[ ] 运行 mvn test。
[ ] 若修改 python-provider，运行 pytest。
[ ] 编写 Phase8_P2人工测试结果.md。
[ ] 编写 Phase8_P2冻结记录.md。
[ ] 更新 AI_IMPLEMENTATION_SKILL.md 为 Phase 8-P2 已冻结。
```

## 验收标准

```text
[ ] Java 测试通过。
[ ] Phase 1–8 P1 回归不破坏。
[ ] 人工测试覆盖模型登记、prompt policy、dataset 脱敏、experiment、release candidate、rollback。
[ ] 冻结记录明确已做 / 未做 / 后置任务。
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
[ ] ModelRegistryEntry 可创建 / 查询。
[ ] PromptRegistryEntry 可创建 / 查询。
[ ] TrainingDatasetVersion 可创建 / 查询。
[ ] ModelExperimentRecord 可创建 / 查询。
[ ] ModelEvaluationReport 可创建。
[ ] ModelReleaseCandidate 可创建但 review-required。
[ ] RollbackPlan 可创建并被 release candidate 引用。
[ ] Policy 能拒绝不安全 prompt / 未脱敏 dataset / 缺 rollback release。
[ ] Evaluation Scorer 可识别治理缺口。
[ ] Candidate Mapping 可沉淀待审核治理候选。
[ ] Audit / Trace 可见。
[ ] Java 测试通过。
[ ] Phase8_P2冻结记录完成。
```
