# Phase 8-P1 ProviderCapability / JudgeProvider API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase8_P1ModelProvider与JudgeProvider_实现规格.md`  
> 前置专项规划：`docs/2-专项设计/Python_AIProvider接入规划.md`  
> 前置冻结：`docs/3-phase实现/Phase8_P0冻结记录.md`  
> 当前 Phase：Phase 8-P1  
> 当前目标：定义 ProviderCapabilityProfile、JudgeProvider、RiskSignalClassifierProvider 的 API、DTO、安全边界、测试和验收方案。

---

# 一、API 设计原则

Phase 8-P1 涉及两类 API：

```text
1. Python Provider API：供 Java Runtime 调用。
2. Java Debug API：供开发、调试、评估和治理观察。
```

共同原则：

```text
1. 所有 API 都不是 patient-facing API。
2. JudgeProvider 不输出最终诊断。
3. RiskSignalClassifierProvider 不直接触发 SafetyGate。
4. ProviderCapabilityProfile 只是能力画像，不是自动授权。
5. Java 必须做 Policy + Validation。
6. 所有调用必须 Trace / Audit / Evaluation。
7. Python 不可用时必须 fallback。
```

---

# 二、Python Provider API 扩展

在 Phase 8-P0 基础上新增：

```text
POST /v1/judge
POST /v1/classify-risk
GET  /v1/capability-profiles
```

保留：

```text
GET  /health
GET  /v1/providers
POST /v1/embeddings
POST /v1/rerank
```

---

# 三、API 1：GET /v1/capability-profiles

响应示例：

```json
{
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.1-p1",
  "profiles": [
    {
      "profile_id": "profile_judge_mock_v1",
      "capability_type": "JUDGE",
      "model_id": "mock_judge_model",
      "model_version": "0.1.0",
      "schema_version": "0.8.1",
      "allowed_use_cases": ["evaluation", "output_boundary_check"],
      "forbidden_use_cases": ["patient_direct_answer", "final_diagnosis"],
      "max_input_items": 5,
      "max_input_chars": 4000,
      "patient_output_allowed": false,
      "requires_validation": true,
      "fallback_strategy": "RULE_BASED_SCORER",
      "status": "ACTIVE"
    }
  ]
}
```

约束：

```text
1. profile_id 必填。
2. capability_type 必须属于允许枚举。
3. allowed_use_cases / forbidden_use_cases 必须存在。
4. patient_output_allowed 默认 false。
5. requires_validation 默认 true。
```

---

# 四、API 2：POST /v1/judge

## 4.1 请求

```json
{
  "request_id": "judge_req_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "python_ai_provider",
  "judge_target_type": "PATIENT_OUTPUT_DRAFT",
  "judge_target_id": "patient_output_draft_001",
  "rubric_id": "patient_boundary_rubric",
  "rubric_version": "0.1.0",
  "input_summary": {
    "text": "建议尽快线下就医进一步评估，不要自行判断为普通胸闷。",
    "symptom_group": "chest_pain"
  },
  "dimensions": ["boundary_safety", "medical_certainty", "patient_readability"],
  "forbidden_labels": ["final_diagnosis", "treatment_instruction"],
  "schema_version": "0.8.1"
}
```

## 4.2 响应

```json
{
  "request_id": "judge_req_001",
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.1-p1",
  "model_id": "mock_judge_model",
  "model_version": "0.1.0",
  "schema_version": "0.8.1",
  "status": "SUCCESS",
  "result": {
    "judge_target_id": "patient_output_draft_001",
    "overall_score": 0.88,
    "dimension_scores": {
      "boundary_safety": 0.92,
      "medical_certainty": 0.84,
      "patient_readability": 0.87
    },
    "violations": [],
    "rationale_summary": "No final diagnosis or treatment instruction detected.",
    "confidence": 0.81
  },
  "warnings": [],
  "error_code": null,
  "latency_ms": 18,
  "trace": {
    "input_count": 1,
    "output_count": 1
  }
}
```

约束：

```text
1. overall_score 必须在 0–1。
2. dimension_scores key 必须来自 request.dimensions。
3. violations 必须来自约定枚举或短文本。
4. rationale_summary 不允许包含最终诊断、治疗建议或患者端指令。
5. Judge 结果不能直接进入 PatientOutput。
```

---

# 五、API 3：POST /v1/classify-risk

## 5.1 请求

```json
{
  "request_id": "risk_req_001",
  "runtime_id": "runtime_demo_001",
  "provider_id": "python_ai_provider",
  "symptom_group": "chest_pain",
  "case_frame_summary": {
    "known_facts": ["胸闷", "活动后加重", "出汗"],
    "missing_facts": ["持续时间", "是否放射痛"]
  },
  "red_flag_candidates": ["activity_related_chest_pain", "sweating"],
  "allowed_labels": ["LOW", "MEDIUM", "HIGH", "UNKNOWN"],
  "schema_version": "0.8.1"
}
```

## 5.2 响应

```json
{
  "request_id": "risk_req_001",
  "provider_id": "python_ai_provider",
  "provider_version": "0.8.1-p1",
  "model_id": "mock_risk_classifier",
  "model_version": "0.1.0",
  "schema_version": "0.8.1",
  "status": "SUCCESS",
  "result": {
    "risk_labels": ["HIGH"],
    "risk_score": 0.86,
    "matched_reasons": ["activity_related_chest_pain", "sweating"],
    "uncertainty": 0.21
  },
  "warnings": ["draft_only_not_safety_gate_decision"],
  "error_code": null,
  "latency_ms": 14
}
```

约束：

```text
1. risk_labels 必须来自 allowed_labels。
2. risk_score 必须在 0–1。
3. matched_reasons 不允许包含未脱敏患者原文。
4. 结果只能作为 RiskSignalDraft。
5. Java SafetyGate 不得被 Python 结果直接替代。
```

---

# 六、Java Debug API 扩展

新增路径：

```text
GET  /api/v1/debug/providers/capability-profiles
POST /api/v1/debug/providers/judge/run
POST /api/v1/debug/providers/risk-classifier/run
GET  /api/v1/debug/providers/calls/{provider_call_id}
```

权限：

```text
run：SYSTEM_ADMIN / EVALUATION_REVIEWER
read：SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER
PATIENT 禁止
```

响应必须是 Safe DTO。

禁止返回：

```text
raw patient dialogue
完整医生端推理链
LLM prompt
完整 provider rationale
可直接给患者的诊断结论
```

---

# 七、Java DTO 规划

建议 DTO：

```text
ProviderCapabilityProfileDto
ProviderCapabilityProfilesResponse
JudgeRunRequest
JudgeRunResponse
JudgeScoreSafeDto
RiskClassifierRunRequest
RiskClassifierRunResponse
RiskSignalDraftSafeDto
ProviderCapabilityPolicyDecisionDto
```

所有 DTO 必须经过 Safe DTO 裁剪。

---

# 八、Provider Validation 测试

## 8.1 JudgeValidationServiceTest

覆盖：

```text
1. 合法 JudgeScoreResult 通过。
2. 缺 model_version 拒绝。
3. overall_score 越界拒绝。
4. dimension key 不在请求 dimensions 中拒绝。
5. rationale_summary 包含 final diagnosis / treatment instruction 拒绝。
6. request_id 不一致拒绝。
```

## 8.2 RiskSignalDraftValidationTest

覆盖：

```text
1. 合法 RiskSignalDraft 通过。
2. risk_label 不在 allowed_labels 中拒绝。
3. risk_score 越界拒绝。
4. matched_reasons 含 raw dialogue 拒绝或脱敏。
5. 缺 schema_version 拒绝。
```

## 8.3 ProviderCapabilityProfileValidationTest

覆盖：

```text
1. 合法 profile 通过。
2. patient_output_allowed=true 时 requires_validation=false 拒绝。
3. forbidden_use_cases 缺失拒绝。
4. unknown capability_type 拒绝。
5. max_input_chars <= 0 拒绝。
```

---

# 九、Policy 测试

## 9.1 ProviderCapabilityPolicyTest

覆盖：

```text
1. allowed use_case 通过。
2. forbidden use_case 拒绝。
3. provider disabled 时 SKIPPED。
4. request input 超过 max_input_chars 时 DEGRADED 或 REJECTED。
5. unknown provider/model 拒绝。
6. patient_direct_answer 一律拒绝。
```

---

# 十、Python 侧测试

新增 pytest：

```text
test_capability_profiles.py
test_judge.py
test_risk_classifier.py
```

覆盖：

```text
1. /v1/capability-profiles 返回 profile。
2. /v1/judge 返回 score 0–1。
3. /v1/judge 不返回患者端结论。
4. /v1/classify-risk 返回 allowlist label。
5. 异常输入返回可归因错误。
```

---

# 十一、Integration 测试

## 11.1 JudgeProviderIntegrationTest

覆盖：

```text
1. Java 调用 Python /v1/judge 成功。
2. ProviderValidation ACCEPTED。
3. Judge result 进入 ProviderCallStore。
4. Judge result 可被 Evaluation Scorer 使用。
5. PatientOutput 不泄露 judge rationale。
```

## 11.2 RiskClassifierIntegrationTest

覆盖：

```text
1. Java 调用 Python /v1/classify-risk 成功。
2. RiskSignalDraft 被 validation 接受。
3. RiskSignalDraft 不直接触发 SafetyGate。
4. classifier unavailable 时 fallback。
```

## 11.3 ProviderCapabilityProfileIntegrationTest

覆盖：

```text
1. Java 可读取 capability profiles。
2. Policy 可基于 profile 允许 / 拒绝调用。
3. profile 缺少版本时被拒绝。
```

---

# 十二、Evaluation Scorer 测试

新增：

```text
JudgeTraceCompletenessScorerTest
JudgeBoundaryAgreementScorerTest
RiskClassifierTraceScorerTest
ProviderCapabilityProfileScorerTest
```

覆盖：

```text
1. judge call 缺 trace 得分失败。
2. judge violations 命中 forbidden label 得分失败。
3. risk classifier draft 有 trace 得分通过。
4. capability profile 缺 forbidden_use_cases 得分失败。
```

---

# 十三、人工测试场景

## 场景 1：Judge 评估 PatientOutputDraft

预期：

```text
返回 overall_score / dimension_scores。
无 final diagnosis violation。
不进入 PatientOutput。
```

## 场景 2：Judge 发现越界表达

输入包含：

```text
你就是急性冠脉综合征。
```

预期：

```text
violations 包含 final_diagnosis。
ProviderValidation 或 Evaluation 标记风险。
```

## 场景 3：Risk classifier 分类胸痛风险

预期：

```text
返回 HIGH draft。
SafetyGate 不被直接改写。
Trace 可见 draft_only_not_safety_gate_decision。
```

## 场景 4：Provider capability policy 拒绝 patient_direct_answer

预期：

```text
POLICY_REJECTED。
不调用 Python。
Audit 可见。
```

---

# 十四、回归测试要求

Phase 8-P1 完成前必须通过：

```text
mvn test
python-provider pytest
```

并且不得破坏：

```text
Phase 8-P0 ProviderClient / Rerank / Embedding
Phase 7 Evidence / Graph
Phase 6 Agent Runtime
SafetyGate
DecisionBoundary
EvaluationRunner
Candidate / Review
Persistence / Audit
Console Safe DTO
```

---

# 十五、完成标准

Phase 8-P1 API 与测试完成标准：

```text
1. Python /v1/judge 可用。
2. Python /v1/classify-risk 可用。
3. Python /v1/capability-profiles 可用。
4. Java Debug API 可运行 judge / risk classifier。
5. ProviderCapabilityPolicy 可拒绝 forbidden use case。
6. ProviderValidation 可拒绝非法 judge / risk result。
7. Judge 结果可进入 Evaluation。
8. RiskSignalDraft 不直接触发 SafetyGate。
9. Trace / Audit 完整。
10. PatientOutput 不泄露 judge rationale / risk internal score。
11. Java 和 Python 测试通过。
12. Phase 1–8 P0 既有测试不回归。
```

---

# 十六、最终结论

Phase 8-P1 API 与测试重点不是“模型判断得多聪明”，而是证明：

```text
模型能力可以被画像、授权、调用、验证、追踪、审计和评估；
Judge / Classifier 只能提供辅助信号；
Runtime 主控、安全边界和治理闭环不会被破坏。
```
