# Phase 4：经验候选与训练数据候选沉淀机制实现规格

> 本文档定义 ClinMindRuntime Phase 4-P0 的目标、边界、主流程和实现范围。  
> Phase 4 的核心不是“自动学习”或“自动训练模型”，而是把 RuntimeTrace、EvaluationResult、RegressionFinding、SafetyViolation 等结果沉淀为可审核的候选资产：ExperienceCandidate 与 TrainingExampleCandidate。

---

# 一、Phase 4 定位

Phase 1–3 已经完成：

```text
Phase 1：Runtime 能安全运行。
Phase 2：能力资产能被 Provider 注入并追踪版本。
Phase 3：Runtime 能被病例集评估，并生成 EvaluationResult 与 CapabilityProfileUpdateProposal。
```

Phase 4 要解决的问题是：

```text
系统跑完一次 Runtime 或 Evaluation 后，哪些信息值得被沉淀？
哪些失败可以转化为经验候选？
哪些样本可以转化为训练数据候选？
哪些候选必须人工审核？
哪些候选绝对不能自动上线？
```

Phase 4-P0 的一句话目标：

```text
从 EvaluationResult / RegressionFinding / SafetyViolation / RuntimeTrace 中生成可追踪、可审核、不可自动生效的 ExperienceCandidate 与 TrainingExampleCandidate。
```

---

# 二、Phase 4-P0 主链路

Phase 4-P0 主链路：

```text
EvaluationRun
→ EvaluationResult
→ EvaluationItemResult
→ RegressionFinding / SafetyViolation / RuntimeCaseExecution
→ CandidateGenerationService
→ ExperienceCandidate
→ TrainingExampleCandidate
→ CandidateStore
→ Debug API 查询
→ Review Required
```

更具体地说：

```text
1. 从 EvaluationRun 中读取失败项、关键指标、SafetyViolation、RegressionFinding。
2. 从 RuntimeCaseExecution 中读取 runtime_id、RuntimeTrace、operationResponses、errors。
3. 根据 CandidateGenerationPolicy 判断是否生成候选。
4. 生成 ExperienceCandidate，用于未来经验库或规则改进。
5. 生成 TrainingExampleCandidate，用于未来训练数据集候选池。
6. 所有候选默认为 REVIEW_REQUIRED，不自动生效。
```

---

# 三、Phase 4-P0 不是什么

Phase 4-P0 不是：

```text
1. 不是自动经验进化。
2. 不是医生审核平台。
3. 不是训练数据集正式发布。
4. 不是模型训练 / 后训练。
5. 不是 RAG / GraphRAG。
6. 不是 PostgreSQL 持久化。
7. 不是前端 Training Center。
8. 不是自动修改 Asset Package。
9. 不是自动更新 CapabilityProfile。
10. 不是把 RuntimeTrace 直接变成可用医疗经验。
```

Phase 4-P0 只做：

```text
候选生成
候选归因
候选风险标注
候选来源追踪
候选查询
候选进入 REVIEW_REQUIRED 状态
```

---

# 四、核心概念

## 4.1 ExperienceCandidate

ExperienceCandidate 是“经验候选”，表示一次 Runtime 或 Evaluation 中出现了值得复盘的模式。

它可能来自：

```text
SafetyViolation
RegressionFinding
RuntimeTrace error
高风险病例 fail-safe
患者端边界违规
DDx 缺失
NextAction 不符合预期
Trace / asset version 缺失
```

它不是正式经验，不会被 Runtime 自动使用。

## 4.2 TrainingExampleCandidate

TrainingExampleCandidate 是“训练数据候选”，表示某个输入、标签、期望输出或错误案例未来可能成为模型训练 / 后训练样本。

它可能用于未来：

```text
intent classification
symptom group classification
risk signal classification
case frame extraction
patient safe rewrite
scorer debugging
retrieval evaluation
```

但 Phase 4-P0 不训练模型，也不发布 TrainingDatasetVersion。

## 4.3 CandidateSourceRef

CandidateSourceRef 负责记录候选来自哪里：

```text
runtime_id
evaluation_run_id
case_id
item_result_id
trace_id
regression_finding_id
safety_violation_id
asset_package_id
asset_package_version
```

候选必须能回溯到来源。

## 4.4 Review Required

所有候选默认进入：

```text
REVIEW_REQUIRED
```

这表示：

```text
候选可能有价值，但未被确认。
不能直接用于 Runtime。
不能直接进入训练集。
不能直接修改资产包。
不能直接改变 CapabilityProfile。
```

---

# 五、候选类型

## 5.1 ExperienceCandidateType

建议类型：

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

## 5.2 TrainingExampleCandidateType

建议类型：

```text
INTENT_CLASSIFICATION
SYMPTOM_GROUP_CLASSIFICATION
RISK_SIGNAL_CLASSIFICATION
CASE_FRAME_EXTRACTION
PATIENT_SAFE_REWRITE
DDX_EXPECTATION
NEXT_ACTION_EXPECTATION
TRACE_QUALITY_EXPECTATION
```

---

# 六、候选生成原则

候选生成必须遵守：

```text
1. 只从已存在的 Runtime / Evaluation 结果生成，不凭空生成。
2. 候选必须绑定来源。
3. 候选必须绑定资产版本。
4. 候选必须标注风险等级。
5. 候选必须进入 REVIEW_REQUIRED。
6. 候选不得自动影响 Runtime 行为。
7. 候选不得自动进入训练集。
```

优先从失败生成候选：

```text
CRITICAL / MAJOR failure > MINOR failure > passed case
```

P0 阶段默认不从全部成功病例中生成候选，避免噪声过大。

---

# 七、风险等级

CandidateRiskLevel：

```text
LOW
MEDIUM
HIGH
CRITICAL
```

映射建议：

```text
SafetyViolation.CRITICAL → CandidateRiskLevel.CRITICAL
PatientBoundary violation → CandidateRiskLevel.CRITICAL
Asset trace missing → CandidateRiskLevel.HIGH
DDx missing → CandidateRiskLevel.MEDIUM / HIGH
NextAction mismatch → CandidateRiskLevel.MEDIUM
Trace quality issue → CandidateRiskLevel.LOW / MEDIUM
```

高风险候选必须保持 REVIEW_REQUIRED，不能被自动通过。

---

# 八、Phase 4-P0 允许实现范围

允许实现：

```text
1. ExperienceCandidate 数据结构。
2. TrainingExampleCandidate 数据结构。
3. CandidateSourceRef 数据结构。
4. CandidateGenerationPolicy。
5. CandidateGenerationService。
6. ExperienceCandidateGenerator。
7. TrainingExampleCandidateGenerator。
8. InMemoryCandidateStore。
9. Debug API 查询候选。
10. 基础单元测试和集成测试。
```

---

# 九、Phase 4-P0 禁止实现范围

禁止实现：

```text
1. Doctor review workflow。
2. Candidate approval API。
3. 正式 ExperienceMemory 生效机制。
4. TrainingDatasetVersion 发布机制。
5. Model training / post-training。
6. PostgreSQL 持久化。
7. 前端 Training Center。
8. 自动更新 CapabilityProfile。
9. 自动修改 YAML Asset Package。
10. 使用 LLM 自动判断候选是否可信。
```

---

# 十、与 Phase 3 的关系

Phase 3 负责：

```text
评估 Runtime。
产生 EvaluationResult。
产生 EvaluationItemResult。
产生 MetricResult。
产生 SafetyViolation。
产生 RegressionFinding。
生成 CapabilityProfileUpdateProposal。
```

Phase 4 负责：

```text
读取 Phase 3 结果。
生成候选经验。
生成候选训练样本。
标注来源和风险。
进入审核等待状态。
```

Phase 4 不修改 Phase 3 的评分结果。

---

# 十一、与模型训练的关系

Phase 4-P0 只生成 TrainingExampleCandidate。

不生成：

```text
TrainingDatasetVersion
ModelProviderVersion
PreferenceDataset
SFT dataset
DPO dataset
```

未来训练链路必须是：

```text
TrainingExampleCandidate
→ Review / Filter
→ TrainingDatasetVersion
→ Model Training / Post-training
→ ModelProviderVersion
→ EvaluationRun
→ CapabilityProfileUpdateProposal
```

---

# 十二、与经验系统的关系

Phase 4-P0 只生成 ExperienceCandidate。

不生成正式：

```text
ExperienceMemory
ExperienceAssetPackage
ClinicalExperienceProvider production asset
```

未来经验生效链路必须是：

```text
ExperienceCandidate
→ Review
→ ApprovedExperience
→ ExperienceAssetVersion
→ Provider
→ Runtime
→ Trace
→ Evaluation
```

---

# 十三、最终结论

Phase 4-P0 的重点不是“让系统自己变聪明”，而是：

```text
让系统知道哪些运行结果值得复盘，
把这些结果变成可追踪、可审核、不可自动生效的候选资产。
```

Phase 4-P0 成功的标准：

```text
1. 能从 EvaluationRun 生成 ExperienceCandidate。
2. 能从失败病例生成 TrainingExampleCandidate。
3. 每个候选都有来源、风险、状态和版本。
4. 候选默认 REVIEW_REQUIRED。
5. Runtime 行为不因候选生成而改变。
```
