# Phase 3 训练与评估闭环实现规格

> 本文档定义 ClinMindRuntime Phase 3 的目标、范围、核心流程、工程边界和验收标准。  
> Phase 3 的核心不是继续增强问诊功能，也不是训练大模型，而是建立“病例集考试 → Runtime 执行 → 指标评分 → EvaluationResult → CapabilityProfile 更新建议”的最小闭环。

---

# 一、阶段定位

Phase 1 已经证明：

```text
受控诊断 Runtime 可以运行。
RuntimeState、SafetyGate、DDx Board、EvidenceGraph、DecisionBoundary、RuntimeTrace 可以形成最小闭环。
```

Phase 2 已经证明：

```text
Runtime 可以通过 Provider 读取可替换、可版本化、可追踪的共享能力资产。
```

Phase 3 要证明的新问题是：

```text
一个症状群能力包是否“能用”，不能只靠人工声明或手写 CapabilityProfile，
而要通过标准病例集、评估指标和 EvaluationResult 来证明，
并让评估结果反过来约束 CapabilityProfile 和 DecisionBoundary。
```

Phase 3 的定位：

```text
训练与评估闭环 MVP。
```

这里的“训练”不是训练基础大模型，而是训练/校准 ClinMindRuntime 的能力资产、规则资产、病例集和能力边界。

---

# 二、Phase 3 核心目标

```text
建立最小可运行的评估授权闭环。
```

具体目标：

```text
1. 定义标准评估病例集 EvaluationCaseSet。
2. 定义 EvaluationRun，用于批量执行病例。
3. 让 EvaluationRunner 调用 RuntimeService，而不是绕过 Runtime。
4. 定义多类评分器 Scorer。
5. 生成 EvaluationResult。
6. 基于 EvaluationResult 生成 CapabilityProfileUpdateProposal。
7. 不直接自动修改生产资产，只生成候选更新建议。
8. Phase 1 / Phase 2 测试继续通过。
```

---

# 三、Phase 3 的核心闭环

```text
EvaluationCaseSet
→ EvaluationRunner
→ RuntimeService.startRuntime / continueRuntime
→ RuntimeTrace / RuntimeState / API Response
→ Scorers
→ EvaluationItemResult
→ EvaluationResult
→ CapabilityProfileUpdateProposal
→ 人工确认后进入后续资产更新流程
```

说明：

```text
Phase 3 的 EvaluationRunner 是 Runtime 的调用者，不是 Runtime 的替代者。
Scorer 只评估 Runtime 输出，不直接修改 RuntimeState。
CapabilityProfileUpdateProposal 只生成建议，不自动上线。
```

---

# 四、Phase 3 做什么

## 4.1 Evaluation Case Set

```text
EvaluationCase
EvaluationCaseSet
ExpectedOutcome
EvaluationInputTurn
EvaluationCaseTag
```

最小支持：

```text
chest_pain
fever
unsupported / wellness 回归病例
patient_facing / clinician_copilot 两种模式
```

## 4.2 Evaluation Runner

```text
EvaluationRunner
EvaluationRunConfig
EvaluationRun
EvaluationRunStore
```

Phase 3-P0 可以使用内存存储，不引入数据库。

## 4.3 Scorers

最小评分器：

```text
EntryAssessmentScorer
SafetyGateScorer
PatientBoundaryScorer
DdxCoverageScorer
NextActionScorer
TraceCompletenessScorer
AssetVersionTraceScorer
```

## 4.4 Evaluation Result

```text
EvaluationItemResult
ScoreBreakdown
MetricResult
EvaluationResult
SafetyViolation
RegressionFinding
```

## 4.5 CapabilityProfile 更新建议

```text
CapabilityProfileUpdateProposal
CapabilityGateDecision
CapabilityLevelRecommendation
```

Phase 3-P0 只生成建议，不自动写入生产资产包。

---

# 五、Phase 3 不做什么

```text
不训练基础大模型。
不做 RLHF / DPO / SFT 训练链路。
不做真实临床有效性认证。
不做真实医生审核平台。
不自动上线 CapabilityProfile。
不自动修改 phase2-default 资产包。
不做完整 Training Center 前端。
不做复杂权限系统。
不引入向量数据库。
不做完整 RAG 评估平台。
不做经验自动进化。
不让评估器绕过 Runtime 直接判断最终输出。
```

---

# 六、推荐工程目录

```text
src/main/java/com/clinmind/runtime/evaluation/
  EvaluationCase.java
  EvaluationCaseSet.java
  EvaluationInputTurn.java
  ExpectedOutcome.java
  EvaluationRunConfig.java
  EvaluationRun.java
  EvaluationItemResult.java
  EvaluationResult.java
  ScoreBreakdown.java
  MetricResult.java
  SafetyViolation.java
  RegressionFinding.java
  EvaluationRunner.java
  EvaluationRunStore.java
  EvaluationCaseRepository.java

src/main/java/com/clinmind/runtime/evaluation/scorer/
  EvaluationScorer.java
  EntryAssessmentScorer.java
  SafetyGateScorer.java
  PatientBoundaryScorer.java
  DdxCoverageScorer.java
  NextActionScorer.java
  TraceCompletenessScorer.java
  AssetVersionTraceScorer.java

src/main/java/com/clinmind/runtime/evaluation/capability/
  CapabilityProfileUpdateProposal.java
  CapabilityEvaluationPolicy.java
  CapabilityProfileProposalService.java

src/main/java/com/clinmind/runtime/api/
  EvaluationController.java

src/main/resources/evaluation/case-sets/
  phase3-default/
    manifest.yml
    chest-pain-cases.yml
    fever-cases.yml
    regression-cases.yml
```

---

# 七、Phase 3-P0 开发顺序

```text
Phase3-P0-A：Evaluation 数据结构
Phase3-P0-B：病例集 Repository 与 YAML 病例格式
Phase3-P0-C：EvaluationRunner 执行 Runtime
Phase3-P0-D：Scorer 评分器体系
Phase3-P0-E：EvaluationResult 聚合与报告
Phase3-P0-F：CapabilityProfile 更新建议
Phase3-P0-G：Evaluation API 与测试验收
```

---

# 八、完成标准

Phase 3-P0 完成时必须满足：

```text
1. 至少支持 chest_pain、fever、wellness、unsupported 四类病例。
2. EvaluationRunner 可以批量执行病例集。
3. 每个病例能产生 EvaluationItemResult。
4. 每次运行能产生 EvaluationResult。
5. EvaluationResult 至少包含 safety、boundary、ddx、next_action、trace、asset_version 指标。
6. PatientBoundaryScorer 能识别患者端诊断泄漏。
7. SafetyGateScorer 能识别高风险病例没有触发安全门的错误。
8. AssetVersionTraceScorer 能识别 trace 中缺失资产版本记录的错误。
9. CapabilityProfileProposalService 能根据阈值生成更新建议。
10. 不自动修改生产 CapabilityProfile。
11. Phase 1 / Phase 2 回归测试继续通过。
```

---

# 九、Phase 3 验收用例

至少覆盖：

```text
1. 运行默认评估病例集成功。
2. 高风险胸痛病例必须触发 SafetyGate。
3. 患者端普通胸痛不得泄露 DDx / must_not_miss / target_diagnosis。
4. 医生端胸痛必须看到 DDx Board。
5. fever 病例能正确进入 fever symptom_group。
6. wellness 病例不进入临床管线。
7. unsupported 病例进入安全停止或不支持状态。
8. Trace 必须包含关键模块。
9. Trace 必须包含 asset_package_id / asset_package_version。
10. EvaluationResult 可以生成 CapabilityProfileUpdateProposal。
```

---

# 十、与后续阶段关系

| Phase 3 产物 | 后续阶段扩展 |
|---|---|
| EvaluationCaseSet | Phase 5 可进入 Training Center 后台管理 |
| EvaluationRunner | Phase 4 可用于经验候选回归验证 |
| EvaluationResult | Phase 4/5 用于再认证、审计和发布门禁 |
| CapabilityProfileUpdateProposal | Phase 5 可接入审核、发布、回滚流程 |
| Scorer 体系 | 后续可扩展人工评分、LLM-as-judge、对照实验 |

---

# 十一、最终约束

```text
Phase 3 是训练与评估闭环 MVP，不是完整训练平台。
Phase 3 的重点是评估 Runtime 能力，而不是继续堆问诊功能。
Evaluation 只能评估和生成建议，不能绕过 Runtime，也不能直接上线资产。
CapabilityProfile 的更新必须先以 Proposal 形式存在。
```
