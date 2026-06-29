# Phase 4-P1 开发任务清单

> 本清单用于约束 Phase 4-P1 的实现顺序。  
> Phase 4-P1 只做候选治理与安全加固，不实现 RAG、数据库、前端、模型训练、正式医生审核平台或自动上线。

---

# 一、状态标记

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要决策
[-] 后置 / 不在 P1 范围
```

---

# 二、Phase 4-P1 目标

```text
把 Phase4-P0 生成的 Candidate，从“可生成、可查询”提升为“可脱敏、可强校验、可记录 review 决策、仍不自动生效”。
```

P1 必须保证：

```text
1. TrainingExampleCandidate.input 不再直接使用 raw input。
2. CandidateSourceRef 按 source_type 强校验。
3. CandidateNotFoundException 不再依赖 message 字符串判断错误码。
4. CandidateReviewRecord 可以记录人工 review 决策。
5. review 后候选仍不自动进入 Runtime / AssetPackage / CapabilityProfile / TrainingDataset。
```

---

# 三、Phase4-P1-A：CandidateSanitizer 与脱敏策略

状态：`[x]`

任务：

```text
[x] CandidateSanitizationPolicy
[x] CandidateSanitizationResult
[x] CandidateSanitizer
[x] SourceTrustLevel / CandidateInputSourceType（如需要）
[x] TrainingExampleCandidateGenerator 接入 CandidateSanitizer
[x] metadata 写入 sanitizer_policy_id / policy_version
[x] sanitization_status 由 sanitizer 决定
```

测试：

```text
[x] CandidateSanitizerTest
[x] CandidateSanitizationPolicyTest
[x] TrainingExampleCandidateSanitizationIntegrationTest
```

验收标准：

```text
1. synthetic evaluation case input_texts 可保留但截断。
2. real / unknown source input_texts 默认 drop / mask。
3. basic_info 只保留 age_bucket / sex。
4. patient_output 默认 drop。
5. PATIENT_SAFE_REWRITE 可保留截断后的 patient_output。
6. TrainingExampleCandidate.metadata 包含 sanitizer policy 信息。
```

---

# 四、Phase4-P1-B：CandidateSourceRef Factory 与组合校验

状态：`[x]`

任务：

```text
[x] CandidateSourceRefFactory
[x] CandidateSourceRefValidator
[x] CandidateSourceRefValidationException
[x] Generator 从 new CandidateSourceRef 改为 factory 创建
[x] 按 source_type 校验必填字段
[x] 增加 INVALID_CANDIDATE_SOURCE_REF 错误码
```

测试：

```text
[x] CandidateSourceRefFactoryTest
[x] CandidateSourceRefValidatorTest
[x] CandidateGenerationSourceRefIntegrationTest
```

验收标准：

```text
1. METRIC_RESULT 缺 evaluation_run_id / case_id / metric_id 时失败。
2. SAFETY_VIOLATION 缺 safety_violation_id 时失败。
3. REGRESSION_FINDING 缺 regression_finding_id 时失败。
4. 正常 Candidate generation 流程仍通过。
```

---

# 五、Phase4-P1-C：CandidateNotFoundException resourceType

状态：`[x]`

任务：

```text
[x] CandidateResourceType enum
[x] CandidateNotFoundException 增加 resourceType
[x] InMemoryCandidateStore 抛异常时指定 resourceType
[x] ApiExceptionHandler 不再依赖 message 字符串
[x] 错误码映射集中化
```

测试：

```text
[x] CandidateNotFoundExceptionTest
[x] CandidateControllerErrorCodeTest
```

验收标准：

```text
1. unknown generation → CANDIDATE_GENERATION_NOT_FOUND。
2. unknown experience candidate → EXPERIENCE_CANDIDATE_NOT_FOUND。
3. unknown training candidate → TRAINING_EXAMPLE_CANDIDATE_NOT_FOUND。
4. handler 不读取 message 判断类型。
```

---

# 六、Phase4-P1-D：CandidateReview 数据结构

状态：`[x]`

任务：

```text
[x] CandidateKind
[x] CandidateReviewDecision
[x] CandidateReviewRecord
[x] CandidateReviewRequest
[x] CandidateReviewTransitionPolicy
```

测试：

```text
[x] CandidateReviewRecordTest
[x] CandidateReviewTransitionPolicyTest
```

验收标准：

```text
1. review record 必填字段校验。
2. REVIEW_REQUIRED → APPROVED 合法。
3. REVIEW_REQUIRED → REJECTED 合法。
4. APPROVED → DEPRECATED 合法。
5. REJECTED → APPROVED 非法。
```

---

# 七、Phase4-P1-E：CandidateReviewStore 与 Service

状态：`[x]`

任务：

```text
[x] CandidateReviewStore interface
[x] InMemoryCandidateReviewStore
[x] CandidateReviewService
[x] reviewExperienceCandidate
[x] reviewTrainingExampleCandidate
[x] listReviewsByCandidate
[x] getReviewRecord
```

测试：

```text
[x] CandidateReviewStoreTest
[x] CandidateReviewServiceTest
```

验收标准：

```text
1. 可记录 review decision。
2. review 后候选 review_status 更新。
3. review record 可查询。
4. review 不修改 AssetPackage / CapabilityProfile。
5. review 不触发模型训练。
```

---

# 八、Phase4-P1-F：Review Debug API 与端到端测试

状态：`[x]`

任务：

```text
[x] POST /api/v1/debug/candidates/experience-candidates/{candidate_id}/review
[x] POST /api/v1/debug/candidates/training-example-candidates/{candidate_id}/review
[x] GET /api/v1/debug/candidates/reviews/{review_id}
[x] GET /api/v1/debug/candidates/{candidate_id}/reviews
[x] API 错误码
[x] docs/Phase4_P1人工测试API结果.md
```

测试：

```text
[x] CandidateReviewControllerTest
[x] CandidateReviewEndToEndIntegrationTest
```

验收标准：

```text
1. 可从已生成候选记录 APPROVED / REJECTED。
2. review record 可查询。
3. 非法状态流转返回明确错误码。
4. review 后候选仍不自动生效。
5. Phase1/2/3/4-P0 回归测试通过。
```

---

# 九、P1 完成定义

Phase4-P1 完成需要满足：

```text
1. P1-A 到 P1-F 全部完成。 ✅
2. TrainingExampleCandidate.input 经过 CandidateSanitizer。 ✅
3. CandidateSourceRef 通过 Factory / Validator 创建。 ✅
4. CandidateNotFoundException 使用 resourceType。 ✅
5. CandidateReviewRecord 可记录和查询。 ✅
6. review 不导致 Runtime / Asset / Capability / TrainingDataset 自动变化。 ✅
7. 新增 Phase4_P1人工测试API结果.md。 ✅
8. 更新 AI_IMPLEMENTATION_SKILL.md，标记 Phase4-P1 完成或进入 freeze。 ✅
9. docs/Phase4_P1冻结记录.md 已建立。 ✅
```

---

# 十、后置任务

## Phase4-P2 / Phase5 后置

```text
[-] DoctorFeedback source
[-] FollowUpOutcome source
[-] ApprovedExperience 正式生效
[-] ExperienceAssetVersion
[-] TrainingDatasetVersion
[-] Candidate export
[-] PostgreSQL persistence
[-] Frontend Training Center
[-] AuditLog / RBAC
[-] Model Registry
[-] RAG / GraphRAG provider service
```

---

# 十一、当前下一步

当前下一步：

```text
Phase4-P1 已冻结（docs/Phase4_P1冻结记录.md）。
后续可选：Phase4-P2 规划或 Phase 5 专项（ApprovedExperience、持久化、正式审核等）。
```
