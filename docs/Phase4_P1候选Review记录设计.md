# Phase 4-P1 候选 Review 记录设计

> 本文档定义 Phase 4-P1 中最小人工 review 记录能力。  
> 它不是正式医生审核平台，也不会让候选自动进入 Runtime、资产包或训练集。

---

# 一、设计目标

Phase 4-P0 只生成 REVIEW_REQUIRED 候选。

Phase 4-P1 增加最小 review 记录能力，用于回答：

```text
1. 这个候选是否被人看过？
2. review 决策是什么？
3. 谁在什么时候做了什么决策？
4. 决策理由是什么？
5. review 是否改变 Runtime 行为？答案必须是：否。
```

---

# 二、Review 与正式审核平台的区别

Phase 4-P1 的 review 是后端治理记录：

```text
Candidate → ReviewDecision → CandidateReviewRecord → Candidate.review_status 更新
```

它不是：

```text
医生工作台
临床审核平台
前端 Training Center
正式标注系统
正式训练数据发布系统
```

---

# 三、核心数据结构

## 3.1 CandidateKind

```text
EXPERIENCE_CANDIDATE
TRAINING_EXAMPLE_CANDIDATE
```

## 3.2 CandidateReviewDecision

```text
APPROVE
REJECT
DEPRECATE
REQUEST_CHANGES   # P2 后置，可先保留枚举
```

## 3.3 CandidateReviewRecord

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

必填字段：

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
```

## 3.4 CandidateReviewRequest

Debug API 请求：

```json
{
  "decision": "APPROVE",
  "reason": "The candidate is a valid safety lesson from a synthetic evaluation failure.",
  "reviewer": "debug-reviewer"
}
```

---

# 四、允许的状态流转

```text
REVIEW_REQUIRED → APPROVED
REVIEW_REQUIRED → REJECTED
APPROVED → DEPRECATED
```

不允许：

```text
REJECTED → APPROVED
DEPRECATED → APPROVED
APPROVED → REVIEW_REQUIRED
```

如需重新审核，后续 P2 可引入 revision / new review cycle。

---

# 五、CandidateReviewService

## 5.1 职责

```text
1. 根据 candidate_id 查询候选。
2. 校验 review decision。
3. 校验状态流转。
4. 创建 CandidateReviewRecord。
5. 更新候选 review_status。
6. 保存 review record。
```

## 5.2 不负责

```text
不修改 AssetPackage。
不修改 CapabilityProfile。
不创建 TrainingDatasetVersion。
不创建 ExperienceAssetVersion。
不调用 RuntimeService。
不调用模型。
```

---

# 六、CandidateReviewStore

P1 使用 in-memory store。

接口建议：

```text
saveReviewRecord(CandidateReviewRecord record)
getReviewRecord(String reviewId)
listReviewsByCandidate(String candidateId)
```

候选本身仍由 CandidateStore 管理。

---

# 七、Debug API

## 7.1 Review experience candidate

```http
POST /api/v1/debug/candidates/experience-candidates/{candidate_id}/review
```

## 7.2 Review training example candidate

```http
POST /api/v1/debug/candidates/training-example-candidates/{candidate_id}/review
```

## 7.3 Get review record

```http
GET /api/v1/debug/candidates/reviews/{review_id}
```

## 7.4 List candidate reviews

```http
GET /api/v1/debug/candidates/{candidate_id}/reviews
```

---

# 八、错误码

```text
CANDIDATE_REVIEW_NOT_FOUND
INVALID_CANDIDATE_REVIEW_DECISION
INVALID_CANDIDATE_REVIEW_TRANSITION
CANDIDATE_NOT_REVIEWABLE
```

---

# 九、测试设计

必须新增：

```text
CandidateReviewRecordTest
CandidateReviewServiceTest
CandidateReviewStoreTest
CandidateReviewControllerTest
CandidateReviewEndToEndIntegrationTest
```

覆盖：

```text
1. REVIEW_REQUIRED → APPROVED 成功。
2. REVIEW_REQUIRED → REJECTED 成功。
3. APPROVED → DEPRECATED 成功。
4. REJECTED → APPROVED 失败。
5. review 后候选状态更新。
6. review 后不修改 AssetPackage。
7. review 后不修改 CapabilityProfile。
8. review 记录可查询。
```

---

# 十、最终结论

Phase 4-P1 的 review 只做“记录人工决策”。

它不意味着：

```text
候选已经进入生产经验库。
候选已经进入训练集。
候选已经影响 Runtime 行为。
```

Review 是治理链路的起点，不是自动上线。
