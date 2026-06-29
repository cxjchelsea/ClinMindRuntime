# Phase 4 开发任务清单

> 本清单用于约束 Phase 4-P0 的实现顺序。  
> Phase 4-P0 只实现经验候选与训练数据候选沉淀机制，不实现自动经验上线、模型训练、RAG、数据库、前端或医生审核平台。

---

# 一、状态标记

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要决策
[-] 后置 / 不在 P0 范围
```

---

# 二、Phase 4-P0 目标

```text
从 EvaluationRun / EvaluationResult / EvaluationItemResult / MetricResult / SafetyViolation / RegressionFinding / RuntimeCaseExecution 中生成 ExperienceCandidate 与 TrainingExampleCandidate。
```

要求：

```text
1. 候选必须有来源。
2. 候选必须有风险等级。
3. 候选必须有审核状态。
4. 候选默认 REVIEW_REQUIRED。
5. 候选不自动生效。
6. 候选不自动进入训练集。
7. 候选不修改 AssetPackage / CapabilityProfile。
```

---

# 三、Phase4-P0-A：Candidate 数据结构

状态：`[x]`

任务：

```text
[x] CandidateSourceRef
[x] CandidateSourceType
[x] CandidateRiskLevel
[x] CandidateReviewStatus
[x] ExperienceCandidate
[x] ExperienceCandidateType
[x] TrainingExampleCandidate
[x] TrainingTaskType
[x] SanitizationStatus
[x] CandidateGenerationPolicy
[x] CandidateGenerationResult
[x] CandidateSkippedItem
```

测试：

```text
[x] CandidateSourceRefTest
[x] ExperienceCandidateTest
[x] TrainingExampleCandidateTest
[x] CandidateGenerationPolicyTest
[x] CandidateGenerationResultTest
```

验收标准：

```text
1. 所有数据结构可创建、不可变或安全封装。
2. 必填字段校验有效。
3. 默认 review_status 为 REVIEW_REQUIRED。
4. 默认 sanitization_status 为 NEEDS_REVIEW。
5. 不引入数据库。
```

---

# 四、Phase4-P0-B：CandidateStore

状态：`[x]`

任务：

```text
[x] CandidateStore interface
[x] InMemoryCandidateStore
[x] saveGenerationResult
[x] getGenerationResult
[x] listExperienceCandidates
[x] listTrainingExampleCandidates
[x] getExperienceCandidate
[x] getTrainingExampleCandidate
```

测试：

```text
[x] InMemoryCandidateStoreTest
```

验收标准：

```text
1. 可保存和查询 CandidateGenerationResult。
2. 可按 generation_id 查询 experience / training candidates。
3. unknown id 返回明确异常。
4. 不接 PostgreSQL。
```

---

# 五、Phase4-P0-C：CandidateGenerationPolicy 与映射策略

状态：`[x]`

任务：

```text
[x] 默认 CandidateGenerationPolicy
[x] MetricSeverity → CandidateRiskLevel 映射
[x] MetricResult → ExperienceCandidateType 映射
[x] MetricResult → TrainingTaskType 映射
[x] SafetyViolation → ExperienceCandidate 映射
[x] RegressionFinding → ExperienceCandidate 映射
[x] skipped item reason 设计
[x] max_candidates_per_case 限制
```

测试：

```text
[x] CandidateGenerationPolicyTest
[x] CandidateMappingPolicyTest
```

验收标准：

```text
1. CRITICAL / MAJOR failure 默认生成候选。
2. MINOR failure 默认不生成。
3. passed case 默认不生成。
4. not_applicable metric 不生成。
5. 单病例候选数量受限。
```

---

# 六、Phase4-P0-D：ExperienceCandidateGenerator

状态：`[ ]`

任务：

```text
[ ] ExperienceCandidateGenerator
[ ] 从 failed MetricResult 生成 ExperienceCandidate
[ ] 从 SafetyViolation 生成 ExperienceCandidate
[ ] 从 RegressionFinding 生成聚合型 ExperienceCandidate
[ ] CandidateSourceRef 绑定 run_id / case_id / metric_id / asset version
[ ] risk_level 计算
[ ] review_status 默认 REVIEW_REQUIRED
```

测试：

```text
[ ] ExperienceCandidateGeneratorTest
```

验收标准：

```text
1. SafetyGate failure 生成 SAFETY_LESSON。
2. PatientBoundary failure 生成 PATIENT_BOUNDARY_LESSON。
3. Asset trace failure 生成 ASSET_VERSION_LESSON。
4. 候选不修改 Runtime / Evaluation。
```

---

# 七、Phase4-P0-E：TrainingExampleCandidateGenerator

状态：`[ ]`

任务：

```text
[ ] TrainingExampleCandidateGenerator
[ ] SafetyGate failure → RISK_SIGNAL_CLASSIFICATION
[ ] PatientBoundary failure → PATIENT_SAFE_REWRITE
[ ] Ddx failure → DDX_EXPECTATION
[ ] NextAction failure → NEXT_ACTION_EXPECTATION
[ ] Asset trace failure → ASSET_TRACE_EXPECTATION
[ ] sanitization_status 默认 NEEDS_REVIEW
```

测试：

```text
[ ] TrainingExampleCandidateGeneratorTest
```

验收标准：

```text
1. 只生成训练样本候选，不生成正式训练集。
2. 不调用模型训练。
3. 不调用 LLM。
4. 候选绑定来源和风险。
```

---

# 八、Phase4-P0-F：CandidateGenerationService

状态：`[ ]`

任务：

```text
[ ] CandidateGenerationService
[ ] 从 EvaluationRunStore 读取 EvaluationRun
[ ] 检查 run 是否完成
[ ] 遍历 EvaluationItemResult
[ ] 读取 RuntimeCaseExecution
[ ] 调用 ExperienceCandidateGenerator
[ ] 调用 TrainingExampleCandidateGenerator
[ ] 聚合 CandidateGenerationResult
[ ] 保存到 CandidateStore
[ ] 记录 skipped_items / warnings
```

测试：

```text
[ ] CandidateGenerationServiceTest
[ ] CandidateGenerationServiceIntegrationTest
```

验收标准：

```text
1. 不调用 RuntimeService。
2. 不调用 EvaluationRunner。
3. 不修改 EvaluationRun。
4. RuntimeCaseExecution 缺失时记录 skipped item。
5. 可生成空 CandidateGenerationResult。
```

---

# 九、Phase4-P0-G：Debug API 与端到端测试

状态：`[ ]`

任务：

```text
[ ] CandidateController
[ ] POST /api/v1/debug/candidates/generations/from-evaluation/{run_id}
[ ] GET /api/v1/debug/candidates/generations/{generation_id}
[ ] GET /api/v1/debug/candidates/generations/{generation_id}/experience-candidates
[ ] GET /api/v1/debug/candidates/generations/{generation_id}/training-example-candidates
[ ] GET /api/v1/debug/candidates/experience-candidates/{candidate_id}
[ ] GET /api/v1/debug/candidates/training-example-candidates/{candidate_id}
[ ] API 错误码
[ ] docs/Phase4_人工测试API结果.md
```

测试：

```text
[ ] CandidateControllerTest
[ ] CandidateEndToEndIntegrationTest
```

验收标准：

```text
1. 可从 EvaluationRun 生成候选。
2. 可查询 generation result。
3. 可查询 experience candidates。
4. 可查询 training candidates。
5. unknown run_id / generation_id / candidate_id 返回明确错误。
6. API 不修改 AssetPackage / CapabilityProfile。
```

---

# 十、P0 完成定义

Phase4-P0 完成需要满足：

```text
1. P0-A 到 P0-G 全部完成。
2. 所有候选默认 REVIEW_REQUIRED。
3. 不引入数据库 / 前端 / RAG / Python Provider / 模型训练。
4. Phase1/2/3 回归测试通过。
5. 新增 Phase4 人工 API 验收记录。
6. 更新 AI_IMPLEMENTATION_SKILL.md，标记 Phase4-P0 完成或进入 freeze。
```

---

# 十一、后置任务

## Phase4-P1 后置

```text
[-] DoctorFeedback source
[-] FollowUpOutcome source
[-] Candidate review workflow
[-] ApprovedExperience
[-] ExperienceAssetVersion
[-] TrainingDatasetVersion
[-] Candidate export
```

## Phase5 后置

```text
[-] PostgreSQL persistence
[-] Frontend Training Center
[-] AuditLog
[-] RBAC
[-] Model Registry
[-] RAG / GraphRAG provider service
```

---

# 十二、当前下一步

当前下一步：

```text
Phase4-P0-D：ExperienceCandidateGenerator
```

开始实现前必须：

```text
1. 将 Phase4-P0-D 状态从 [ ] 改为 [/]。
2. 只实现 ExperienceCandidateGenerator 及其单元测试。
3. 不实现 TrainingExampleCandidateGenerator / Service / API。
```
