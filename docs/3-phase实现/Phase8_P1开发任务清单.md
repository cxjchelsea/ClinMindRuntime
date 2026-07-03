# Phase 8-P1 开发任务清单：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP

> 上位实现规格：`docs/3-phase实现/Phase8_P1ModelProvider与JudgeProvider_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase8_P1ProviderCapability_API与测试设计.md`  
> 前置冻结：`docs/3-phase实现/Phase8_P0冻结记录.md`  
> 当前目标：在 Phase 8-P0 Python AI Provider 基础上，新增 JudgeProvider、RiskSignalClassifierProvider 与 ProviderCapabilityProfile，但不让模型接管 Runtime。

---

# 一、Phase 8-P1 总目标

Phase 8-P1 要完成的不是完整模型平台，而是一个最小闭环：

```text
ProviderCapabilityProfile
→ ProviderCapabilityPolicy
→ Python JudgeProvider / RiskSignalClassifierProvider
→ JudgeScoreResult / RiskSignalDraft
→ ProviderValidation
→ Evaluation / Trace / Audit / Candidate
```

最终要证明：

```text
模型能力可以被画像和授权；
Judge 可以作为 Evaluation 辅助信号；
Risk classifier 可以作为风险草稿信号；
所有模型结果都必须被 Java 校验和治理；
模型不能直接回答患者或决定诊断。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P8P1-A | 扩展 Python provider 配置与 capability profile | 已完成 |
| P8P1-B | 实现 Python /v1/capability-profiles | 已完成 |
| P8P1-C | 实现 Python JudgeProvider MVP | 已完成 |
| P8P1-D | 实现 Python RiskSignalClassifierProvider MVP | 已完成 |
| P8P1-E | 建立 Java capability profile domain | 已完成 |
| P8P1-F | 实现 ProviderCapabilityPolicy | 已完成 |
| P8P1-G | 扩展 Java PythonProviderClient | 已完成 |
| P8P1-H | 扩展 ProviderValidationService | 已完成 |
| P8P1-I | Provider Debug API 扩展 | 已完成 |
| P8P1-J | Evaluation Scorer 接入 Judge / Risk / Profile | 已完成 |
| P8P1-K | Trace / Audit / Candidate 治理接入 | 已完成 |
| P8P1-L | 测试、人工验证与冻结记录 | 已完成 |

---

# 三、P8P1-A：扩展 Python provider 配置与 capability profile

## 目标

让 Python provider 声明 Judge / Risk classifier 的模型能力。

## 任务

```text
[x] 更新 python-provider app/config.py。
[x] 新增 JUDGE_MODEL_ID / JUDGE_MODEL_VERSION。
[x] 新增 RISK_CLASSIFIER_MODEL_ID / RISK_CLASSIFIER_MODEL_VERSION。
[x] 新增 SCHEMA_VERSION=0.8.1 或兼容版本。
[x] 新增 capability profile schema。
```

## 验收标准

```text
[x] Python provider 启动不破坏 P8-P0 /health / providers。
[x] 新能力不会影响 embedding / rerank。
```

---

# 四、P8P1-B：实现 /v1/capability-profiles

## 目标

提供 provider / model / capability 的结构化画像。

## 任务

```text
[x] 新增 capability profile schema。
[x] 新增 GET /v1/capability-profiles。
[x] 返回 JUDGE profile。
[x] 返回 RISK_CLASSIFICATION profile。
[x] profile 包含 allowed_use_cases / forbidden_use_cases。
[x] patient_output_allowed=false。
[x] requires_validation=true。
```

## 验收标准

```text
[x] profile_id 非空。
[x] capability_type 合法。
[x] forbidden_use_cases 不为空。
[x] pytest 通过。
```

---

# 五、P8P1-C：实现 Python JudgeProvider MVP

## 目标

提供 deterministic / mock judge，用于 Evaluation 和边界检查。

## 任务

```text
[x] 新增 judge schema。
[x] 新增 JudgeProvider。
[x] 实现 POST /v1/judge。
[x] 根据 input_summary 文本做规则评分。
[x] 检测 final_diagnosis / treatment_instruction 等 violation。
[x] 返回 overall_score / dimension_scores / violations / rationale_summary。
[x] 不返回患者端最终回答。
```

## P1 实现方式

```text
可以使用 rule-based / keyword judge。
不要求真实 LLM。
不要求外部模型。
```

## 验收标准

```text
[x] 合法 draft 返回高分。
[x] 含“确诊”“一定是”“你就是”等表达返回 violation。
[x] score 在 0–1。
[x] rationale_summary 不包含患者端指令。
[x] pytest 通过。
```

---

# 六、P8P1-D：实现 Python RiskSignalClassifierProvider MVP

## 目标

提供风险分类草稿，不替代 SafetyGate。

## 任务

```text
[x] 新增 risk classifier schema。
[x] 新增 RiskSignalClassifierProvider。
[x] 实现 POST /v1/classify-risk。
[x] 根据 symptom_group / known_facts / red_flag_candidates 返回 LOW / MEDIUM / HIGH / UNKNOWN。
[x] 返回 risk_score / matched_reasons / uncertainty。
[x] warnings 固定包含 draft_only_not_safety_gate_decision。
```

## 验收标准

```text
[x] chest_pain + 出汗 / 活动后加重可返回 HIGH。
[x] unknown symptom_group 返回 UNKNOWN。
[x] risk_labels 必须来自 allowed_labels。
[x] pytest 通过。
```

---

# 七、P8P1-E：建立 Java capability profile domain

## 目标

新增 Java 侧能力画像对象。

## 建议包路径

```text
src/main/java/com/clinmind/runtime/provider/capability/
src/main/java/com/clinmind/runtime/provider/judge/
src/main/java/com/clinmind/runtime/provider/risk/
```

## 任务

```text
[x] 新增 ProviderCapabilityProfile。
[x] 新增 ProviderCapabilityProfileStatus。
[x] 新增 ProviderCapabilityPolicyDecision。
[x] 新增 JudgeRequest / JudgeScoreResult。
[x] 新增 JudgeTargetType。
[x] 新增 RiskSignalClassificationRequest / RiskSignalDraft。
[x] 新增 RiskSignalLabel。
```

## 验收标准

```text
[x] 所有对象不包含 PatientOutput。
[x] 所有对象包含 provider/model/schema version。
[x] Draft / Result 均不可直接修改 RuntimeState。
```

---

# 八、P8P1-F：实现 ProviderCapabilityPolicy

## 目标

Java 控制哪些 provider capability 可以被调用。

## 任务

```text
[x] 新增 ProviderCapabilityPolicy。
[x] 根据 profile 判断 allowed_use_cases。
[x] 拒绝 forbidden_use_cases。
[x] 校验 max_input_chars。
[x] 校验 provider/model/version。
[x] patient_direct_answer 一律拒绝。
[x] provider disabled 时 SKIPPED。
```

## 验收标准

```text
[x] allowed use_case 通过。
[x] forbidden use_case 拒绝。
[x] patient_direct_answer 拒绝。
[x] 超限输入降级或拒绝。
[x] 所有拒绝有 reasons。
```

---

# 九、P8P1-G：扩展 Java PythonProviderClient

## 目标

支持调用 judge / risk / capability profiles。

## 任务

```text
[x] 新增 getCapabilityProfiles()。
[x] 新增 judge(JudgeRequest)。
[x] 新增 classifyRisk(RiskSignalClassificationRequest)。
[x] 处理 provider disabled。
[x] 处理 timeout / unavailable / invalid response。
[x] 保存 ProviderCallRecord。
```

## 验收标准

```text
[x] Python 可用时调用成功。
[x] Python 不可用时 fallback。
[x] 不抛未处理异常到 RuntimeService。
[x] ProviderCallStore 可查。
```

---

# 十、P8P1-H：扩展 ProviderValidationService

## 目标

校验 JudgeScoreResult / RiskSignalDraft / CapabilityProfile。

## 任务

```text
[x] validateJudge。
[x] validateRiskSignalDraft。
[x] validateCapabilityProfile。
[x] 校验 score 0–1。
[x] 校验 labels allowlist。
[x] 校验 schema_version。
[x] 校验 request_id 对齐。
[x] 校验 rationale_summary 禁止表达。
```

## 验收标准

```text
[x] 合法 JudgeScoreResult 通过。
[x] 合法 RiskSignalDraft 通过。
[x] 非法 label / score / schema 被拒绝。
[x] rationale 越界被拒绝。
```

---

# 十一、P8P1-I：Provider Debug API 扩展

## 目标

提供最小 debug API 观察 judge / risk / profile。

## 任务

```text
[x] GET /api/v1/debug/providers/capability-profiles。
[x] POST /api/v1/debug/providers/judge/run。
[x] POST /api/v1/debug/providers/risk-classifier/run。
[x] GET /api/v1/debug/providers/calls/{provider_call_id} 兼容新 capability。
[x] 所有响应 Safe DTO。
[x] 接入 ProviderDebugAccessPolicy。
```

## 验收标准

```text
[x] 未授权访问被拒绝。
[x] PATIENT 不能调用。
[x] SYSTEM_ADMIN / EVALUATION_REVIEWER 可 run。
[x] READ_ONLY_OBSERVER 可 read。
[x] 不返回 raw patient text。
```

---

# 十二、P8P1-J：Evaluation Scorer 接入

## 目标

让 Judge / Risk / Profile 进入 Evaluation。

## 任务

```text
[x] 新增 JudgeTraceCompletenessScorer。
[x] 新增 JudgeBoundaryAgreementScorer。
[x] 新增 JudgeViolationDetectionScorer。
[x] 新增 RiskClassifierTraceScorer。
[x] 新增 ProviderCapabilityProfileScorer。
[x] 支持 judge_eval / risk_classifier_eval / provider_profile_eval tags。
```

## 验收标准

```text
[x] 缺 trace 得分失败。
[x] 命中 forbidden violation 得分失败。
[x] profile 缺 forbidden_use_cases 得分失败。
[x] 默认 case 不受新 scorer 影响。
```

---

# 十三、P8P1-K：Trace / Audit / Candidate 治理接入

## 目标

让 judge / risk / profile 调用可复盘，并沉淀候选但不自动上线。

## 任务

```text
[x] Audit action 增加 RUN_JUDGE_PROVIDER。
[x] Audit action 增加 RUN_RISK_CLASSIFIER_PROVIDER。
[x] Audit action 增加 QUERY_PROVIDER_CAPABILITY_PROFILE。
[x] ProviderCallRecord 支持 capability_type=JUDGE / RISK_CLASSIFICATION。
[x] 可选沉淀 ProviderCapabilityProfileCandidate。
[x] 可选沉淀 RiskSignalRuleCandidate。
```

## 验收标准

```text
[x] 每次调用有 provider_call_id。
[x] AuditLog 可见 capability_type。
[x] Candidate 不自动上线。
```

---

# 十四、P8P1-L：测试、人工验证与冻结记录

## 目标

完成 Phase 8-P1 收口。

## 任务

```text
[x] 完成 Python pytest。
[x] 完成 ProviderCapabilityPolicyTest。
[x] 完成 ProviderCapabilityProfileValidationTest。
[x] 完成 JudgeProviderIntegrationTest。
[x] 完成 RiskClassifierIntegrationTest。
[x] 完成 ProviderDebugController P8-P1 tests。
[x] 完成 Judge / Risk / Profile Evaluation Scorer tests。
[x] 运行 mvn test。
[x] 运行 python-provider pytest。
[x] 编写 Phase8_P1人工测试结果.md。
[x] 编写 Phase8_P1冻结记录.md。
```

## 验收标准

```text
[x] Java 测试通过。
[x] Python 测试通过。
[x] Phase 1–8 P0 回归不破坏。
[x] 人工测试覆盖 judge 正常 / judge violation / risk classifier / policy reject。
[x] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十五、推荐实现顺序

建议严格按以下顺序实现：

```text
1. P8P1-A：Python 配置与 capability profile schema。
2. P8P1-B：/v1/capability-profiles。
3. P8P1-C：JudgeProvider MVP。
4. P8P1-D：RiskSignalClassifierProvider MVP。
5. P8P1-E：Java capability / judge / risk domain。
6. P8P1-F：ProviderCapabilityPolicy。
7. P8P1-G：PythonProviderClient 扩展。
8. P8P1-H：ProviderValidationService 扩展。
9. P8P1-I：Debug API 扩展。
10. P8P1-J：Evaluation Scorer。
11. P8P1-K：Trace / Audit / Candidate。
12. P8P1-L：测试、人工验证、冻结记录。
```

---

# 十六、开发期间禁止事项

```text
1. 不让 JudgeProvider 直接决定最终诊断。
2. 不让 RiskSignalClassifier 直接触发 SafetyGate。
3. 不让 Python 直接输出 PatientOutput。
4. 不让 Python 修改 RuntimeState。
5. 不绕过 ProviderCapabilityPolicy。
6. 不绕过 ProviderValidationService。
7. 不把 rationale 直接展示给患者。
8. 不做真实 LLM / 外部云模型强依赖。
9. 不做训练流水线。
10. 不改写 Phase 1–8 P0 冻结记录。
```

---

# 十七、Phase 8-P1 完成后的后置任务

```text
1. Phase 8-P2：ModelRegistry / PromptRegistry。
2. Phase 8-P2：TrainingDatasetVersion 与模型实验记录。
3. Phase 8-P2：真实 LLM Judge 可选接入。
4. Phase 10：Model Console / Provider Console。
5. 后置研究：LoRA / DPO / RFT / distillation。
```

---

# 十八、最终 Definition of Done

Phase 8-P1 完成的最终标准：

```text
[x] ProviderCapabilityProfile 可读取。
[x] ProviderCapabilityPolicy 可拒绝 forbidden use case。
[x] JudgeProvider 返回结构化 JudgeScoreResult。
[x] RiskSignalClassifierProvider 返回结构化 RiskSignalDraft。
[x] ProviderValidation 可拒绝非法结果。
[x] Judge 结果进入 Evaluation。
[x] RiskSignalDraft 不直接触发 SafetyGate。
[x] PatientOutput 不泄露 judge rationale / risk internal score。
[x] Trace / Audit 可见。
[x] Java / Python 测试通过。
[x] Phase8_P1冻结记录完成。
```
