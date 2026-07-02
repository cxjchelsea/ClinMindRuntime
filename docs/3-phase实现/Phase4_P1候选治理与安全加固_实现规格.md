# Phase 4-P1：候选治理与安全加固实现规格

> 本文档定义 ClinMindRuntime Phase 4-P1 的目标、边界和实现范围。  
> Phase 4-P0 已完成候选沉淀机制；Phase 4-P1 不继续扩大候选生成能力，而是补齐候选安全、来源校验、错误码语义和最小人工治理边界。

---

# 一、Phase 4-P1 定位

Phase 4-P0 已经完成：

```text
EvaluationRun / EvaluationResult / EvaluationItemResult / MetricResult
→ CandidateGenerationPolicy
→ CandidateMappingPolicy
→ ExperienceCandidateGenerator
→ TrainingExampleCandidateGenerator
→ CandidateGenerationService
→ CandidateStore
→ CandidateController（debug API）
```

Phase 4-P1 要解决的问题不是“生成更多候选”，而是：

```text
1. 候选里哪些字段允许进入训练样本 input？
2. 接真实 RuntimeTrace 前如何做脱敏和字段白名单？
3. CandidateSourceRef 如何从弱校验变成按 source_type 的组合校验？
4. CandidateNotFoundException 如何避免依赖 message 字符串判断错误码？
5. 候选如何被记录一次人工 review 决策，但仍不自动生效？
6. review 记录如何可追踪、可查询、可回滚到候选来源？
```

Phase 4-P1 的一句话目标：

```text
把 Phase 4-P0 生成的候选，从“可查询的候选”提升为“安全脱敏、来源严格、错误语义清晰、可记录人工决策但不自动生效的候选治理对象”。
```

---

# 二、Phase 4-P1 主链路

Phase 4-P1 的主链路：

```text
EvaluationRun / RuntimeCaseExecution
→ CandidateGenerationService
→ CandidateSanitizer
→ CandidateSourceRefFactory / Validator
→ ExperienceCandidate / TrainingExampleCandidate
→ CandidateStore
→ CandidateReviewService
→ CandidateReviewRecord
→ Review-aware Debug API
```

其中：

```text
CandidateSanitizer：控制候选 input 中哪些字段可保留、掩码、删除。
CandidateSourceRefFactory：按 source_type 创建并校验来源引用。
CandidateReviewService：记录人工 review 决策，但不让候选自动生效。
CandidateReviewRecord：保存 review 行为的元数据、理由、操作者、时间和目标状态。
```

---

# 三、Phase 4-P1 不是什么

Phase 4-P1 不是：

```text
1. 不是正式医生审核平台。
2. 不是前端 Training Center。
3. 不是 ExperienceMemory 正式生效机制。
4. 不是 TrainingDatasetVersion 发布机制。
5. 不是模型训练 / 后训练。
6. 不是 RAG / GraphRAG。
7. 不是 PostgreSQL 持久化。
8. 不是自动上线经验。
9. 不是自动修改 AssetPackage。
10. 不是自动更新 CapabilityProfile。
```

Phase 4-P1 只做：

```text
候选脱敏
候选来源强校验
候选错误码语义增强
候选 review 记录
候选 review 状态可查询
候选仍不自动生效
```

---

# 四、P1 核心能力

## 4.1 CandidateSanitizer

CandidateSanitizer 是 Phase 4-P1 最重要的安全加固。

它负责：

```text
1. 对 TrainingExampleCandidate.input 做字段级过滤。
2. 对 patient_output、input_texts、basic_info 等字段做保守处理。
3. 标记 sanitization_status。
4. 记录 sanitizer_policy_version。
5. 生成 sanitization_warnings。
```

P1 默认策略：

```text
synthetic evaluation case：允许保留，但标记 source=synthetic。
unknown source：默认 NEEDS_REVIEW。
real runtime trace：默认不得直接进入 training input，必须经过 mask / drop。
```

## 4.2 CandidateSourceRefFactory / Validator

Phase 4-P0 中 CandidateSourceRef 只强校验 sourceType。P1 要升级为按 source_type 校验组合字段。

例如：

```text
METRIC_RESULT：必须有 evaluation_run_id + case_id + metric_id + asset_package_id + asset_package_version。
SAFETY_VIOLATION：必须有 evaluation_run_id + case_id + safety_violation_id。
REGRESSION_FINDING：必须有 evaluation_run_id + regression_finding_id。
EVALUATION_ITEM_RESULT：必须有 evaluation_run_id + case_id + item_result_id。
```

## 4.3 CandidateNotFoundException resourceType

Phase 4-P0 的部分错误码判断依赖异常 message。P1 要将其改为显式 resourceType：

```text
CANDIDATE_GENERATION
EXPERIENCE_CANDIDATE
TRAINING_EXAMPLE_CANDIDATE
REVIEW_RECORD
```

这样 handler 不再通过字符串判断错误码。

## 4.4 CandidateReviewRecord

CandidateReviewRecord 记录人工 review 动作。

它不是医生审核平台，只是后端最小治理记录。

字段建议：

```text
review_id
candidate_id
candidate_kind
from_status
to_status
decision
reason
reviewer
reviewed_at
source_ref
metadata
```

## 4.5 CandidateReviewService

CandidateReviewService 负责：

```text
1. 接收 review decision。
2. 校验状态流转是否合法。
3. 生成 CandidateReviewRecord。
4. 更新 in-memory candidate review_status。
5. 保存 review record。
```

它不能做：

```text
不能修改 AssetPackage。
不能修改 CapabilityProfile。
不能发布 TrainingDataset。
不能让 Runtime 自动使用 approved candidate。
```

---

# 五、Review 状态流转

Phase 4-P1 允许的状态流转：

```text
REVIEW_REQUIRED → APPROVED
REVIEW_REQUIRED → REJECTED
APPROVED → DEPRECATED
```

P1 不允许：

```text
APPROVED → Runtime 自动生效
APPROVED → AssetPackage 自动修改
APPROVED → TrainingDatasetVersion 自动发布
REJECTED → 自动删除来源记录
```

---

# 六、Debug API 范围

Phase 4-P1 可以新增 internal debug API：

```http
POST /api/v1/debug/candidates/experience-candidates/{candidate_id}/review
POST /api/v1/debug/candidates/training-example-candidates/{candidate_id}/review
GET  /api/v1/debug/candidates/reviews/{review_id}
GET  /api/v1/debug/candidates/{candidate_id}/reviews
```

这些 API 只用于记录和查询 review，不用于正式产品化审核流。

---

# 七、P1 禁止事项

```text
1. 不接真实 RAG / GraphRAG。
2. 不接 Python AI Provider。
3. 不接 PostgreSQL / Redis / pgvector。
4. 不做前端 Console。
5. 不训练模型。
6. 不发布 TrainingDatasetVersion。
7. 不创建正式 ExperienceMemory。
8. 不自动修改 AssetPackage。
9. 不自动修改 CapabilityProfile。
10. 不接真实医生系统。
```

---

# 八、与数据安全规划的关系

Phase 4-P1 必须遵守：

```text
docs/数据安全与合规边界规划.md
```

尤其是：

```text
1. 不把真实患者原始文本无脱敏写入训练候选。
2. Debug API 也不能成为敏感数据泄漏通道。
3. 候选数据默认只是内部治理数据。
4. 未来进入持久化前必须补数据库表结构、访问控制和审计设计。
```

---

# 九、P1 完成标准

Phase 4-P1 完成需要满足：

```text
1. CandidateSanitizer 生效，TrainingExampleCandidate.input 经过策略处理。
2. CandidateSourceRef 按 source_type 做组合字段校验。
3. CandidateNotFoundException 使用 resourceType，不依赖 message 字符串。
4. CandidateReviewRecord / CandidateReviewService 可记录 review 决策。
5. Debug API 可记录和查询 review。
6. review 后候选仍不自动进入 Runtime、AssetPackage、CapabilityProfile 或 TrainingDataset。
7. Phase1/2/3/4-P0 回归测试通过。
```

---

# 十、最终结论

Phase 4-P1 的核心不是继续扩功能，而是治理 Phase 4-P0 产生的候选：

```text
先安全，
再校验，
再记录人工决策，
仍然不自动生效。
```

它让候选从“能生成”走向“可治理”。
