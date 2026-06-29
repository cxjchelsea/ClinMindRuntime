# Phase 4 数据结构设计

> 本文档定义 Phase 4-P0 中 ExperienceCandidate、TrainingExampleCandidate、CandidateSourceRef、CandidateGenerationPolicy 等核心数据结构。  
> 这些结构只用于候选沉淀，不代表正式经验库、正式训练集或生产资产。

---

# 一、设计目标

Phase 4-P0 数据结构必须支持：

```text
1. 候选来源可追踪。
2. 候选风险可标注。
3. 候选状态可审核。
4. 候选与 Runtime / Evaluation / Asset 版本绑定。
5. 候选可被 debug API 查询。
6. 候选不自动生效。
```

---

# 二、包结构建议

建议新增包：

```text
com.clinmind.runtime.candidate
com.clinmind.runtime.candidate.generation
com.clinmind.runtime.candidate.store
com.clinmind.runtime.candidate.api
```

或如果希望更语义化：

```text
com.clinmind.runtime.experience.candidate
com.clinmind.runtime.training.candidate
```

P0 推荐先用统一 `candidate` 包，避免过早拆得过细。

---

# 三、CandidateSourceRef

## 3.1 作用

CandidateSourceRef 用于记录候选的来源。

它回答：

```text
这个候选从哪里来？
来自哪个 Runtime？
来自哪个 EvaluationRun？
来自哪个 case？
来自哪个 finding / violation？
用了哪个 asset package？
```

## 3.2 字段设计

```text
CandidateSourceRef
- source_type: CandidateSourceType
- runtime_id
- evaluation_run_id
- case_id
- item_result_id
- trace_id
- regression_finding_id
- safety_violation_id
- metric_id
- asset_package_id
- asset_package_version
- created_from
```

## 3.3 CandidateSourceType

```text
RUNTIME_TRACE
EVALUATION_RUN
EVALUATION_ITEM_RESULT
REGRESSION_FINDING
SAFETY_VIOLATION
METRIC_RESULT
MANUAL_DEBUG_NOTE   # P1/P2 后置
DOCTOR_FEEDBACK     # 后置
FOLLOW_UP_OUTCOME   # 后置
```

P0 只需要支持：

```text
EVALUATION_RUN
EVALUATION_ITEM_RESULT
REGRESSION_FINDING
SAFETY_VIOLATION
METRIC_RESULT
```

---

# 四、ExperienceCandidate

## 4.1 作用

ExperienceCandidate 表示一个待审核的经验候选。

它不是正式经验，不能被 Runtime 自动使用。

## 4.2 字段设计

```text
ExperienceCandidate
- candidate_id
- candidate_type: ExperienceCandidateType
- title
- summary
- source_ref: CandidateSourceRef
- risk_level: CandidateRiskLevel
- review_status: CandidateReviewStatus
- suggested_action
- evidence
- tags
- created_at
- created_by
- metadata
```

## 4.3 ExperienceCandidateType

```text
SAFETY_LESSON
PATIENT_BOUNDARY_LESSON
MISSING_DDX_LESSON
NEXT_ACTION_LESSON
TRACE_QUALITY_LESSON
ASSET_VERSION_LESSON
FAIL_SAFE_LESSON
RUNTIME_ERROR_LESSON
```

## 4.4 示例

```json
{
  "candidate_id": "exp_cand_001",
  "candidate_type": "SAFETY_LESSON",
  "title": "High-risk chest pain did not trigger expected red flag",
  "summary": "A chest pain evaluation case failed the SafetyGate metric.",
  "risk_level": "CRITICAL",
  "review_status": "REVIEW_REQUIRED",
  "source_ref": {
    "source_type": "SAFETY_VIOLATION",
    "evaluation_run_id": "eval_run_001",
    "case_id": "chest_pain_high_risk_001",
    "safety_violation_id": "sv_001",
    "asset_package_id": "phase2-default",
    "asset_package_version": "0.2.0"
  }
}
```

---

# 五、TrainingExampleCandidate

## 5.1 作用

TrainingExampleCandidate 表示未来可进入训练数据集的候选样本。

它不是正式训练样本，不能直接用于训练。

## 5.2 字段设计

```text
TrainingExampleCandidate
- candidate_id
- task_type: TrainingTaskType
- source_ref: CandidateSourceRef
- input
- expected_output
- negative_output
- label
- reason
- risk_level: CandidateRiskLevel
- review_status: CandidateReviewStatus
- sanitization_status: SanitizationStatus
- tags
- created_at
- metadata
```

## 5.3 TrainingTaskType

```text
INTENT_CLASSIFICATION
SYMPTOM_GROUP_CLASSIFICATION
RISK_SIGNAL_CLASSIFICATION
CASE_FRAME_EXTRACTION
PATIENT_SAFE_REWRITE
DDX_EXPECTATION
NEXT_ACTION_EXPECTATION
TRACE_QUALITY_EXPECTATION
ASSET_TRACE_EXPECTATION
```

P0 推荐优先支持：

```text
RISK_SIGNAL_CLASSIFICATION
PATIENT_SAFE_REWRITE
DDX_EXPECTATION
NEXT_ACTION_EXPECTATION
ASSET_TRACE_EXPECTATION
```

## 5.4 SanitizationStatus

```text
UNKNOWN
SANITIZED
NEEDS_REVIEW
REJECTED_FOR_PRIVACY
```

P0 默认：

```text
NEEDS_REVIEW
```

原因：P0 不做正式数据脱敏平台。

---

# 六、CandidateRiskLevel

```text
LOW
MEDIUM
HIGH
CRITICAL
```

映射规则：

```text
MetricSeverity.CRITICAL → CRITICAL
MetricSeverity.MAJOR → HIGH
MetricSeverity.MINOR → MEDIUM
MetricSeverity.INFO → LOW
```

特殊规则：

```text
PATIENT_DIAGNOSIS_LEAK → CRITICAL
HIGH_RISK_NOT_TRIGGERED → CRITICAL
TRACE_ASSET_VERSION_MISSING → HIGH
```

---

# 七、CandidateReviewStatus

```text
GENERATED
REVIEW_REQUIRED
APPROVED      # P1/P2 后置，仅枚举保留
REJECTED      # P1/P2 后置，仅枚举保留
DEPRECATED    # P1/P2 后置，仅枚举保留
```

P0 生成后默认：

```text
REVIEW_REQUIRED
```

P0 不实现正式 review workflow。

---

# 八、CandidateGenerationPolicy

## 8.1 作用

CandidateGenerationPolicy 决定哪些 Evaluation 结果可以生成候选。

## 8.2 字段设计

```text
CandidateGenerationPolicy
- generate_from_critical_failures: boolean
- generate_from_major_failures: boolean
- generate_from_minor_failures: boolean
- generate_from_passed_cases: boolean
- generate_training_candidates: boolean
- generate_experience_candidates: boolean
- max_candidates_per_case
- allowed_metric_ids
- blocked_metric_ids
```

## 8.3 默认值

```text
generate_from_critical_failures = true
generate_from_major_failures = true
generate_from_minor_failures = false
generate_from_passed_cases = false
generate_training_candidates = true
generate_experience_candidates = true
max_candidates_per_case = 5
```

---

# 九、CandidateGenerationResult

## 9.1 作用

CandidateGenerationResult 是一次候选生成任务的结果。

## 9.2 字段设计

```text
CandidateGenerationResult
- generation_id
- source_evaluation_run_id
- started_at
- completed_at
- experience_candidates
- training_example_candidates
- skipped_items
- warnings
```

---

# 十、CandidateStore

P0 使用 in-memory store。

建议接口：

```text
CandidateStore
- saveGenerationResult(CandidateGenerationResult result)
- getGenerationResult(String generationId)
- listExperienceCandidates(String generationId)
- listTrainingExampleCandidates(String generationId)
- getExperienceCandidate(String candidateId)
- getTrainingExampleCandidate(String candidateId)
```

P0 实现：

```text
InMemoryCandidateStore
```

禁止 P0 接 PostgreSQL。

---

# 十一、校验规则

## 11.1 ExperienceCandidate 校验

```text
candidate_id 不能为空。
candidate_type 不能为空。
source_ref 不能为空。
risk_level 不能为空。
review_status 不能为空。
summary 不能为空。
```

## 11.2 TrainingExampleCandidate 校验

```text
candidate_id 不能为空。
task_type 不能为空。
source_ref 不能为空。
input 不能为空。
risk_level 不能为空。
review_status 不能为空。
sanitization_status 不能为空。
```

---

# 十二、与现有结构的关系

Phase 4 读取但不修改：

```text
EvaluationRun
EvaluationResult
EvaluationItemResult
MetricResult
SafetyViolation
RegressionFinding
RuntimeCaseExecution
RuntimeTrace
```

Phase 4 不应改变：

```text
RuntimeState
CapabilityProfile
AssetPackage
EvaluationResult
```

---

# 十三、最终结论

Phase 4-P0 数据结构的核心不是复杂，而是边界清楚：

```text
Candidate 有来源。
Candidate 有风险。
Candidate 有审核状态。
Candidate 不自动生效。
Candidate 未来可进入经验治理或训练数据治理。
```
