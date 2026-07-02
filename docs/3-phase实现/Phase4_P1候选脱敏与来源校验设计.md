# Phase 4-P1 候选脱敏与来源校验设计

> 本文档细化 Phase 4-P1 中 CandidateSanitizer、CandidateSanitizationPolicy、CandidateSourceRefFactory 和 CandidateSourceRefValidator 的设计。  
> 这部分是进入真实 RuntimeTrace、真实用户输入或未来训练数据治理前必须优先完成的 hardening。

---

# 一、为什么 P1 必须先做脱敏

Phase 4-P0 中 TrainingExampleCandidate 可以从 EvaluationCase、RuntimeCaseExecution、RuntimeState、PatientOutput 中构造 input。

P0 可接受的前提是：

```text
1. 当前主要来自 synthetic evaluation cases。
2. 候选默认 REVIEW_REQUIRED。
3. sanitization_status 默认 NEEDS_REVIEW。
4. 候选不会自动进入训练集。
```

但如果未来接入真实 RuntimeTrace 或真实用户数据，风险会变成：

```text
真实患者输入 → TrainingExampleCandidate.input → Debug API / export / review → 潜在敏感信息泄漏
```

因此 P1 必须在继续扩展来源前增加脱敏层。

---

# 二、CandidateSanitizer 设计

## 2.1 职责

CandidateSanitizer 负责：

```text
1. 接收候选生成前的 raw input map。
2. 根据 source_type / task_type / source_trust_level 选择策略。
3. 对字段做 keep / mask / drop / summarize。
4. 输出 sanitized input。
5. 输出 sanitization_status。
6. 输出 warnings 和 policy_version。
```

## 2.2 不负责

CandidateSanitizer 不负责：

```text
不判断候选是否有价值。
不决定候选是否通过 review。
不生成 expected_output。
不训练模型。
不调用 LLM 自动脱敏。
不修改 RuntimeTrace。
```

---

# 三、CandidateSanitizationPolicy

## 3.1 字段

```text
CandidateSanitizationPolicy
- policy_id
- policy_version
- allow_synthetic_input_texts
- allow_real_input_texts
- mask_basic_info
- drop_patient_output_by_default
- allow_patient_output_for_safe_rewrite
- max_input_text_length
- blocked_fields
- allowed_fields_by_task_type
```

## 3.2 默认策略

```text
policy_id = phase4-p1-default
policy_version = 0.4.1
allow_synthetic_input_texts = true
allow_real_input_texts = false
mask_basic_info = true
drop_patient_output_by_default = true
allow_patient_output_for_safe_rewrite = true
max_input_text_length = 300
```

解释：

```text
synthetic case 的文本可以保留，但仍需标记 source=synthetic。
real source 的 input_texts 默认不保留原文。
basic_info 默认只保留年龄段和性别，不保留可识别字段。
patient_output 只有 PATIENT_SAFE_REWRITE 任务允许保留必要片段。
```

---

# 四、字段处理规则

## 4.1 input_texts

```text
synthetic evaluation case：可保留，长度截断。
real runtime trace：默认 drop 或 mask。
unknown source：drop。
```

## 4.2 basic_info

保留：

```text
age_bucket
sex
```

不保留：

```text
name
phone
id_card
address
exact_birthdate
free_text_notes
```

## 4.3 patient_output

默认 drop。

仅当 task_type 为：

```text
PATIENT_SAFE_REWRITE
```

可以保留必要片段，并进行长度截断。

## 4.4 case_frame_summary

允许保留结构化字段：

```text
chief_complaint
symptom_group
missing_slots
symptoms
```

但应避免原始自由文本过长。

## 4.5 trace_ids / asset refs

允许保留：

```text
trace_id
asset_package_id
asset_package_version
asset_id
asset_version
```

这些是审计和复盘必要字段。

---

# 五、SanitizationStatus

Phase 4-P0 已有：

```text
UNKNOWN
SANITIZED
NEEDS_REVIEW
REJECTED_FOR_PRIVACY
```

P1 使用规则：

```text
所有字段均按 policy 处理且无敏感字段 → SANITIZED
存在被 drop / mask 的字段，但仍可 review → NEEDS_REVIEW
发现禁止字段且无法安全处理 → REJECTED_FOR_PRIVACY
来源未知 → NEEDS_REVIEW
```

---

# 六、CandidateSourceRefFactory

## 6.1 为什么需要 Factory

P0 代码中可以直接 new CandidateSourceRef。

P1 应改为通过 Factory 创建，原因：

```text
1. 集中校验必填字段。
2. 集中处理 asset version 绑定。
3. 避免不同 generator 生成不一致 source_ref。
4. 便于后续扩展 DoctorFeedback / FollowUpOutcome。
```

## 6.2 Factory 方法建议

```text
fromMetricResult(run, itemResult, execution, metric)
fromSafetyViolation(run, itemResult, execution, violation)
fromRegressionFinding(run, finding)
fromRuntimeExecution(run, itemResult, execution)
fromManualReview(candidate, reviewRecord)
```

---

# 七、CandidateSourceRefValidator

## 7.1 校验规则

```text
METRIC_RESULT:
  evaluation_run_id required
  case_id required
  metric_id required
  asset_package_id required
  asset_package_version required

SAFETY_VIOLATION:
  evaluation_run_id required
  case_id required
  safety_violation_id required

REGRESSION_FINDING:
  evaluation_run_id required
  regression_finding_id required

EVALUATION_ITEM_RESULT:
  evaluation_run_id required
  case_id required
  item_result_id required

RUNTIME_TRACE:
  runtime_id required
  trace_id required
```

## 7.2 错误码

```text
INVALID_CANDIDATE_SOURCE_REF
MISSING_CANDIDATE_SOURCE_FIELD
INCONSISTENT_CANDIDATE_SOURCE_REF
```

---

# 八、测试设计

必须新增：

```text
CandidateSanitizerTest
CandidateSanitizationPolicyTest
CandidateSourceRefFactoryTest
CandidateSourceRefValidatorTest
TrainingExampleCandidateSanitizationIntegrationTest
```

覆盖场景：

```text
1. synthetic case input_texts 可以保留。
2. real runtime trace input_texts 默认 drop / mask。
3. basic_info 只保留 age_bucket / sex。
4. patient_output 默认 drop。
5. PATIENT_SAFE_REWRITE 可保留截断后的 patient_output。
6. METRIC_RESULT 缺 evaluation_run_id 抛错。
7. SAFETY_VIOLATION 缺 safety_violation_id 抛错。
8. REGRESSION_FINDING 缺 regression_finding_id 抛错。
```

---

# 九、P1-A / P1-B 完成标准

P1-A CandidateSanitizer 完成标准：

```text
1. TrainingExampleCandidateGenerator 不再直接写 raw input。
2. input 必须先经过 CandidateSanitizer。
3. sanitization_status 根据策略生成。
4. metadata 中记录 sanitizer_policy_id / policy_version。
5. 测试覆盖 synthetic / real / unknown source。
```

P1-B CandidateSourceRef 校验完成标准：

```text
1. 生成器不再直接 new CandidateSourceRef，改用 Factory。
2. 不同 source_type 的必填字段校验生效。
3. 缺字段时抛出明确异常。
4. 现有 Candidate generation 流程回归通过。
```

---

# 十、最终结论

Phase 4-P1 的安全加固优先级高于新增来源和新增审核能力。

原因是：

```text
没有脱敏和来源强校验，候选越多，治理风险越大。
```

所以 P1 应先做：

```text
CandidateSanitizer
CandidateSourceRefFactory / Validator
```

再做 review 记录能力。
