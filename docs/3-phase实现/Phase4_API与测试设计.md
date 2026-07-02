# Phase 4 API 与测试设计

> 本文档定义 Phase 4-P0 的 debug API、响应结构、错误码和测试范围。  
> Phase 4-P0 API 只用于候选生成与查询，不提供审核通过、正式发布、训练数据集创建或资产包修改能力。

---

# 一、API 总原则

```text
1. 只提供 debug/internal API。
2. 不提供 patient-facing API。
3. 不提供正式审核 API。
4. 不提供 Candidate approval API。
5. 不提供 TrainingDataset 发布 API。
6. 不提供 AssetPackage 修改 API。
7. 不提供模型训练 API。
```

API 前缀：

```text
/api/v1/debug/candidates
```

---

# 二、创建候选生成任务

## 2.1 API

```http
POST /api/v1/debug/candidates/generations/from-evaluation/{run_id}
```

## 2.2 请求体

```json
{
  "generate_experience_candidates": true,
  "generate_training_candidates": true,
  "generate_from_critical_failures": true,
  "generate_from_major_failures": true,
  "generate_from_minor_failures": false,
  "generate_from_passed_cases": false,
  "max_candidates_per_case": 5
}
```

请求体可为空，使用默认 CandidateGenerationPolicy。

## 2.3 响应

```json
{
  "generation_id": "cand_gen_001",
  "source_evaluation_run_id": "eval_run_001",
  "experience_candidate_count": 3,
  "training_candidate_count": 2,
  "skipped_item_count": 1,
  "warnings": []
}
```

---

# 三、查询候选生成结果

## 3.1 API

```http
GET /api/v1/debug/candidates/generations/{generation_id}
```

## 3.2 响应

```json
{
  "generation_id": "cand_gen_001",
  "source_evaluation_run_id": "eval_run_001",
  "started_at": "...",
  "completed_at": "...",
  "experience_candidates": [],
  "training_example_candidates": [],
  "skipped_items": [],
  "warnings": []
}
```

---

# 四、查询 ExperienceCandidate

## 4.1 列表

```http
GET /api/v1/debug/candidates/generations/{generation_id}/experience-candidates
```

## 4.2 单个

```http
GET /api/v1/debug/candidates/experience-candidates/{candidate_id}
```

---

# 五、查询 TrainingExampleCandidate

## 5.1 列表

```http
GET /api/v1/debug/candidates/generations/{generation_id}/training-example-candidates
```

## 5.2 单个

```http
GET /api/v1/debug/candidates/training-example-candidates/{candidate_id}
```

---

# 六、错误码

| 场景 | HTTP | code |
|---|---:|---|
| evaluation run 不存在 | 404 | `EVALUATION_RUN_NOT_FOUND` |
| evaluation run 未完成 | 409 | `EVALUATION_RUN_NOT_COMPLETED` |
| generation 不存在 | 404 | `CANDIDATE_GENERATION_NOT_FOUND` |
| experience candidate 不存在 | 404 | `EXPERIENCE_CANDIDATE_NOT_FOUND` |
| training candidate 不存在 | 404 | `TRAINING_EXAMPLE_CANDIDATE_NOT_FOUND` |
| 请求参数非法 | 400 | `INVALID_CANDIDATE_GENERATION_REQUEST` |

---

# 七、响应边界

Phase 4 debug API 可以返回：

```text
candidate_id
candidate_type
task_type
source_ref
risk_level
review_status
summary
reason
metadata
```

但需要注意：

```text
不要返回完整未脱敏患者原始文本。
不要返回不必要的完整 RuntimeState。
不要返回完整医生端内部推理给 patient-facing API。
```

P0 是 debug API，但仍应遵守数据安全文档。

---

# 八、Controller 设计

建议新增：

```text
CandidateController
```

职责：

```text
1. 接收生成请求。
2. 调用 CandidateGenerationService。
3. 查询 CandidateStore。
4. 返回 debug response。
```

不负责：

```text
不执行 Runtime。
不执行 Evaluation。
不审核候选。
不发布资产。
不训练模型。
```

---

# 九、测试设计

## 9.1 数据结构测试

```text
CandidateSourceRefTest
ExperienceCandidateTest
TrainingExampleCandidateTest
CandidateGenerationPolicyTest
CandidateGenerationResultTest
```

## 9.2 生成策略测试

```text
CandidateGenerationPolicyTest
ExperienceCandidateGeneratorTest
TrainingExampleCandidateGeneratorTest
```

覆盖：

```text
CRITICAL failure 生成候选。
MAJOR failure 生成候选。
MINOR failure 默认跳过。
passed case 默认跳过。
not_applicable metric 跳过。
max_candidates_per_case 生效。
```

## 9.3 接入测试

```text
CandidateGenerationServiceTest
CandidateGenerationServiceIntegrationTest
```

覆盖：

```text
从 EvaluationRun 生成 CandidateGenerationResult。
RuntimeCaseExecution 缺失时记录 skipped item。
生成候选包含 CandidateSourceRef。
服务不调用 RuntimeService。
服务不调用 EvaluationRunner。
```

## 9.4 API 测试

```text
CandidateControllerTest
CandidateEndToEndIntegrationTest
```

覆盖 API：

```text
POST /api/v1/debug/candidates/generations/from-evaluation/{run_id}
GET /api/v1/debug/candidates/generations/{generation_id}
GET /api/v1/debug/candidates/generations/{generation_id}/experience-candidates
GET /api/v1/debug/candidates/generations/{generation_id}/training-example-candidates
GET /api/v1/debug/candidates/experience-candidates/{candidate_id}
GET /api/v1/debug/candidates/training-example-candidates/{candidate_id}
```

## 9.5 回归测试

每次 Phase 4-P0 改动后，仍需保护：

```text
Phase 1 Runtime 回归。
Phase 2 Asset Provider 回归。
Phase 3 Evaluation 回归。
PatientOutputAssetIsolationTest。
RuntimeAssetVersionMismatchTest。
EvaluationEndToEndIntegrationTest。
```

---

# 十、人工验收建议

新增：

```text
docs/Phase4_人工测试API结果.md
```

验收场景：

```text
1. 从一个有 failure 的 EvaluationRun 生成候选。
2. 查询 generation result。
3. 查询 experience candidates。
4. 查询 training candidates。
5. 从 passed run 生成空候选。
6. unknown run_id 返回错误。
7. unknown generation_id 返回错误。
8. 验证候选均为 REVIEW_REQUIRED。
9. 验证不会修改 CapabilityProfile。
10. 验证不会修改 AssetPackage。
```

---

# 十一、最终结论

Phase 4-P0 API 只做候选生成与查询。

它证明：

```text
系统能把 Evaluation 暴露的问题沉淀为候选资产，
但不会自动上线、不会自动训练、不会自动改变 Runtime 行为。
```
