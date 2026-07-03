# Phase 8-P1 人工测试结果：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP

> 规格：`Phase8_P1ModelProvider与JudgeProvider_实现规格.md`  
> API 与测试设计：`Phase8_P1ProviderCapability_API与测试设计.md`  
> 任务清单：`Phase8_P1开发任务清单.md`  
> 测试日期：2026-07-03

---

# 一、测试范围

本次验证覆盖 Phase 8-P1 的最小闭环：

```text
ProviderCapabilityProfile
-> ProviderCapabilityPolicy
-> Python JudgeProvider / RiskSignalClassifierProvider
-> Java PythonProviderClient
-> ProviderValidationService
-> Provider Debug API
-> Evaluation Scorer
-> Trace / Audit / Candidate governance
```

本次验证不覆盖真实 LLM、外部云模型、ModelRegistry、PromptRegistry、训练流水线或自动发布机制。

---

# 二、自动化测试结果

## Python provider

命令：

```powershell
D:\cxj\software\annaconda\python.exe -m pytest
```

结果：

```text
10 passed
```

覆盖：

- `/health`
- `/v1/providers`
- `/v1/capability-profiles`
- `/v1/embeddings`
- `/v1/rerank`
- `/v1/judge`
- `/v1/classify-risk`

## Java targeted tests

命令：

```powershell
mvn "-Dtest=ProviderGovernanceScorerTest,ProviderGovernanceCandidateMappingTest,ProviderCapabilityPolicyTest,ProviderCapabilityProfileValidationTest,JudgeProviderValidationTest,RiskSignalDraftValidationTest" test
```

结果：

```text
19 passed
```

覆盖：

- ProviderCapabilityPolicy 授权 / 拒绝 / 降级
- CapabilityProfile validation
- JudgeScoreResult validation
- RiskSignalDraft validation
- Judge / Risk / Profile Evaluation Scorer
- P8-P1 metric 到 Candidate mapping 的待审核候选沉淀入口

## Java full regression

命令：

```powershell
mvn test
```

结果：

```text
481 run, 0 failures, 0 errors, 23 skipped
```

---

# 三、人工场景验证

## 场景 1：Provider capability profile 可读取

验证点：

- 返回 JUDGE profile。
- 返回 RISK_CLASSIFICATION profile。
- `forbidden_use_cases` 非空。
- `patient_output_allowed=false`。
- `requires_validation=true`。

结果：通过。

## 场景 2：Judge 正常草稿评分

输入为安全的 patient output draft 摘要。

预期：

- 返回 `overall_score` 和 `dimension_scores`。
- `violations=[]`。
- 不返回 PatientOutput。
- rationale 不进入患者端输出。

结果：通过。

## 场景 3：Judge 检测越界表达

输入包含“你就是”“治疗方案”等确定性诊断或治疗指令表达。

预期：

- `violations` 包含 `final_diagnosis` 或 `treatment_instruction`。
- Java validation / Evaluation scorer 可识别风险。
- 结果只作为治理信号，不进入 PatientOutput。

结果：通过。

## 场景 4：Risk classifier 胸痛风险草稿

输入包含 chest pain、activity、sweating 等风险线索。

预期：

- 返回 `HIGH` draft。
- warnings 包含 `draft_only_not_safety_gate_decision`。
- SafetyGate 不被 Python 结果直接替代。

结果：通过。

## 场景 5：ProviderCapabilityPolicy 拒绝 patient_direct_answer

预期：

- policy 返回 `POLICY_REJECTED`。
- 不允许模型能力直接面向患者输出。
- Audit 可记录 policy reject。

结果：通过。

## 场景 6：Evaluation / Candidate 治理接入

预期：

- `judge_eval`、`risk_classifier_eval`、`provider_profile_eval` tags 可触发对应 scorer。
- scorer failure 可通过既有 CandidateMappingPolicy 映射为 review-required candidate。
- 不自动发布经验、规则或训练数据。

结果：通过。

---

# 四、结论

Phase 8-P1 自动化与人工验收通过。

本阶段实现的是受控 ModelProvider governance MVP，不是完整模型平台。Judge / Risk classifier / CapabilityProfile 均保持为辅助治理信号，Runtime 主控、安全边界、PatientOutput 和自动发布机制未被 Python provider 接管。
