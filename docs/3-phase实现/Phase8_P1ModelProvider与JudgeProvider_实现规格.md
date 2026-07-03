# Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 前置专项规划：`docs/2-专项设计/Python_AIProvider接入规划.md`  
> 前置阶段：Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结  
> 当前 Phase：Phase 8-P1  
> 当前目标：在 Phase 8-P0 的 Python AI Provider 基础上，新增 JudgeProvider、RiskSignalClassifierProvider 和 ProviderCapabilityProfile，使模型能力可被评估、授权、降级和治理，但不接管 Runtime。

---

# 一、Phase 定位

Phase 8-P1 的目标不是做完整模型训练平台，也不是把 ClinMindRuntime 改成 LLM 医疗问答系统。

Phase 8-P1 的目标是：

```text
在 Java Runtime 主控下，
把 Python AI Provider 从 embedding / rerank 扩展到 judge / classification / capability profile，
让模型能力可以对 Evidence、Output、Question、Graph relation 进行评分、分类或结构化草稿生成，
但所有结果必须返回 Java ProviderValidation / RuntimeValidation / Evaluation，
不能直接进入 PatientOutput 或最终诊断。
```

核心命题：

```text
模型可以评分、分类、生成 Draft；Runtime 决定是否采纳。
```

---

# 二、前置状态

Phase 8-P0 已冻结，当前已经具备：

```text
python-provider FastAPI 服务
/health
/v1/providers
/v1/embeddings
/v1/rerank
Java HttpPythonProviderClient
ProviderValidationService
EvidenceRerankEnhancementService
Provider Debug API
Provider Trace / Audit / Evaluation Scorer
fallback 机制
```

Phase 8-P1 应复用：

```text
PythonProviderClient
ProviderInvocationResult
ProviderTrace
ProviderValidationService
ProviderCallStore
Provider Debug API
Evaluation Scorer
AuditLog
```

---

# 三、当前不做什么

Phase 8-P1 明确不做：

```text
1. 不做 Python Runtime 主控。
2. 不做 Python Agent 自主循环。
3. 不让 LLM 直接回答患者。
4. 不让 JudgeProvider 决定最终诊断。
5. 不让 RiskSignalClassifier 直接替代 SafetyGate。
6. 不让任何 Provider 直接修改 RuntimeState。
7. 不做 LoRA / DPO / RLHF / RFT 正式训练流水线。
8. 不做完整 ModelRegistry / PromptRegistry 生产平台。
9. 不自动发布 TrainingDatasetVersion。
10. 不做复杂 MLOps。
11. 不接外部大模型云服务作为强依赖主线。
12. 不把 provider rationale 直接展示给患者。
```

P1 可以做：

```text
JudgeProvider MVP
RiskSignalClassifierProvider MVP
ProviderCapabilityProfile
ProviderCapabilityPolicy
ProviderResultValidation 增强
Provider Evaluation 报告
Debug API 扩展
fallback / degrade
```

---

# 四、Phase 8-P1 核心链路

目标链路：

```text
RuntimeState / EvidenceGraph / PatientOutputDraft / ClinicianReportDraft
↓
Java ProviderCapabilityPolicy 判断是否允许调用模型能力
↓
Java PythonProviderClient 构造受控请求
↓
Python AI Provider
  ├── JudgeProvider 返回 JudgeScoreResult
  ├── RiskSignalClassifierProvider 返回 RiskSignalDraft
  └── ModelCapability endpoint 返回 CapabilityProfile
↓
Java ProviderValidationService 校验 schema / 范围 / 越界表达
↓
Runtime 采纳 / 部分采纳 / 拒绝 / 降级
↓
Evaluation / Audit / Trace / Candidate governance
```

核心边界：

```text
Judge 不是事实真相，只是评估信号。
Classifier 不是 SafetyGate，只是风险信号草稿。
CapabilityProfile 不是权限本身，权限仍由 Java Policy 决定。
```

---

# 五、核心对象设计

## 5.1 ProviderCapabilityProfile

ProviderCapabilityProfile 表示某个 Provider / Model / Capability 在当前 Runtime 中被允许做什么。

建议字段：

```text
profile_id
provider_id
provider_version
model_id
model_version
capability_type
schema_version
allowed_use_cases
forbidden_use_cases
max_input_items
max_input_chars
timeout_ms
patient_output_allowed
clinician_output_allowed
requires_validation
fallback_strategy
risk_level
status
created_at
```

capability_type 候选：

```text
EMBEDDING
RERANK
JUDGE
RISK_CLASSIFICATION
CASE_EXTRACTION_DRAFT
OUTPUT_BOUNDARY_CHECK
GRAPH_RELATION_SCORING
```

P1 最小：

```text
JUDGE
RISK_CLASSIFICATION
```

## 5.2 JudgeRequest

JudgeProvider 输入必须是受控、脱敏、结构化的目标对象。

建议字段：

```text
request_id
runtime_id
judge_target_type
judge_target_id
rubric_id
rubric_version
input_summary
dimensions
forbidden_labels
schema_version
```

judge_target_type 候选：

```text
PATIENT_OUTPUT_DRAFT
CLINICIAN_REPORT_DRAFT
EVIDENCE_CANDIDATE
GRAPH_EVIDENCE_CANDIDATE
QUESTION_CANDIDATE
PROVIDER_RESULT
```

P1 最小：

```text
EVIDENCE_CANDIDATE
PATIENT_OUTPUT_DRAFT
```

## 5.3 JudgeScoreResult

JudgeProvider 只能返回评分、维度结果和简短 rationale summary。

建议字段：

```text
request_id
provider_id
provider_version
model_id
model_version
schema_version
status
judge_target_id
overall_score
dimension_scores
violations
rationale_summary
confidence
warnings
```

要求：

```text
overall_score 只能是 0–1。
dimension_scores 只能是约定维度。
violations 只能是枚举或短文本。
rationale_summary 不得包含患者端最终建议。
```

## 5.4 RiskSignalClassificationRequest

用于让模型对病例摘要做风险信号草稿分类。

建议字段：

```text
request_id
runtime_id
case_frame_summary
known_facts
red_flag_candidates
symptom_group
allowed_labels
schema_version
```

注意：

```text
不传完整 raw dialogue。
不传患者身份信息。
不传医生端完整推理链。
```

## 5.5 RiskSignalDraft

分类结果只能是草稿：

```text
request_id
provider_id
provider_version
model_id
model_version
schema_version
status
risk_labels
risk_score
matched_reasons
uncertainty
warnings
```

要求：

```text
risk_labels 必须来自 allowlist。
risk_score 只能是 0–1。
RiskSignalDraft 不能直接触发 SafetyGate。
SafetyGate 是否触发仍由 Java SafetyGate / Runtime 决定。
```

---

# 六、ProviderCapabilityPolicy

必须新增 Java 侧 ProviderCapabilityPolicy。

职责：

```text
1. 判断某个 capability 是否启用。
2. 判断某个 provider/model/version 是否允许。
3. 判断当前 use_case 是否允许。
4. 判断是否允许用于 patient output draft 评估。
5. 判断超限输入是否降级或拒绝。
6. 产生 fallback strategy。
```

Policy 输入：

```text
ProviderCapabilityProfile
RuntimeState summary
use_case
actor_context
request_size
```

Policy 输出：

```text
ALLOWED
POLICY_REJECTED
DEGRADED
SKIPPED
```

默认策略：

```text
默认不允许 Provider 直接面向患者端生成内容。
默认所有 Judge / Classifier 结果都 requires_validation=true。
默认 provider unavailable 时 fallback，不阻断 Runtime。
```

---

# 七、Python Provider API 扩展

Phase 8-P1 推荐在 python-provider 中新增：

```text
POST /v1/judge
POST /v1/classify-risk
GET  /v1/capability-profiles
```

P1 最小必要：

```text
POST /v1/judge
POST /v1/classify-risk
GET  /v1/capability-profiles
```

实现方式：

```text
可以使用 deterministic / mock judge。
可以使用 rule-based risk classifier。
不要求真实 LLM。
不要求外部模型服务。
```

---

# 八、与 Evaluation 的关系

Phase 8-P1 的 JudgeProvider 首先应用于 Evaluation，而不是 Runtime 直接决策。

推荐用途：

```text
1. 评价 PatientOutputDraft 是否越界。
2. 评价 EvidenceCandidate 是否有 source/version/score。
3. 评价 GraphEvidenceCandidate 是否出现“确诊”等表达。
4. 评价 ProviderResult 是否符合 schema 和边界。
```

新增 Scorer：

```text
JudgeTraceCompletenessScorer
JudgeBoundaryAgreementScorer
JudgeViolationDetectionScorer
RiskClassifierTraceScorer
ProviderCapabilityProfileScorer
```

注意：

```text
Judge score 不等于最终评估结论。
EvaluationRunner 可以把 judge score 作为一个 metric，但不能只依赖 judge score。
```

---

# 九、与 SafetyGate 的关系

RiskSignalClassifierProvider 不能替代 SafetyGate。

正确链路：

```text
CaseFrame summary
↓
RiskSignalClassifierProvider 返回 RiskSignalDraft
↓
Java ProviderValidationService 校验
↓
Runtime 将 Draft 作为辅助信号写入 Trace / Evaluation / Candidate
↓
SafetyGate 仍由 Java 安全规则 / SafetyGateService 决定
```

禁止：

```text
RiskSignalDraft.score > 某阈值 → 直接触发 SafetyGate
```

允许：

```text
RiskSignalDraft 可以帮助发现需要新增的 safety rule candidate。
```

---

# 十、与 Candidate / Governance 的关系

Phase 8-P1 可以开始把 Judge / Classifier 结果沉淀为候选，但不能自动上线。

可沉淀：

```text
ProviderCapabilityProfileCandidate
JudgeRubricCandidate
RiskSignalRuleCandidate
EvaluationCaseCandidate
```

禁止：

```text
自动发布 ProviderCapabilityProfile。
自动发布 SafetyRule。
自动发布 TrainingDatasetVersion。
自动改变模型权限。
```

---

# 十一、Trace / Audit

必须记录：

```text
provider_call_id
provider_id
provider_version
model_id
model_version
capability_type
use_case
policy_status
validation_status
fallback_used
latency_ms
judge_target_type / risk_target_type
```

Audit action：

```text
RUN_MODEL_PROVIDER
RUN_JUDGE_PROVIDER
RUN_RISK_CLASSIFIER_PROVIDER
QUERY_PROVIDER_CAPABILITY_PROFILE
PROVIDER_CAPABILITY_POLICY_REJECTED
```

---

# 十二、完成标准

Phase 8-P1 完成时必须满足：

```text
1. 新增 ProviderCapabilityProfile。
2. 新增 ProviderCapabilityPolicy。
3. Python provider 新增 /v1/judge。
4. Python provider 新增 /v1/classify-risk。
5. Python provider 新增 /v1/capability-profiles。
6. Java PythonProviderClient 支持 judge / classify-risk / capability profiles。
7. ProviderValidationService 支持 JudgeScoreResult / RiskSignalDraft。
8. Debug API 可运行 judge / classify-risk。
9. Judge 结果能进入 Evaluation metric。
10. RiskSignalDraft 不直接触发 SafetyGate。
11. Trace / Audit 可见。
12. Python Provider 失败时 fallback。
13. PatientOutput 不泄露 judge rationale / risk internal score。
14. `mvn test` 通过。
15. `python-provider pytest` 通过。
16. Phase 1–8 P0 既有测试不回归。
```

---

# 十三、后置任务

Phase 8-P1 不完成但可后置：

```text
1. Phase 8-P2：ModelRegistry / PromptRegistry。
2. Phase 8-P2：TrainingDatasetVersion 与模型实验记录。
3. Phase 8-P2：真实 LLM Judge 接入。
4. Phase 8-P2：真实 RiskSignalClassifier 模型。
5. Phase 10：Model Console / Provider Console。
6. 后置研究：LoRA / DPO / RFT / distillation。
```

---

# 十四、最终结论

Phase 8-P1 的本质是：

```text
从“Python 可以提供 embedding / rerank”
升级为“Python 模型能力可以被画像、授权、评分、分类和评估”，
但 Runtime 主控、安全边界、采纳决策和治理权仍然留在 Java。
```

它不是模型训练平台，也不是 LLM 医疗问答，而是 ClinMindRuntime 模型能力治理域的第二个最小切片。
