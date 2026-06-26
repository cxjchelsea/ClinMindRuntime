# Phase 3 CapabilityProfile 更新机制设计

> 本文档定义 Phase 3 如何根据 EvaluationResult 生成 CapabilityProfile 更新建议。  
> Phase 3 只生成 Proposal，不自动修改生产资产包，不自动上线能力档案。

---

# 一、设计目标

Phase 2 中 CapabilityProfile 已经资产化，但仍主要来自手写配置。Phase 3 要解决：

```text
CapabilityProfile 的能力边界应该由评估结果支撑，
而不是只靠开发者主观声明。
```

目标：

```text
EvaluationResult
→ CapabilityEvaluationPolicy
→ CapabilityProfileUpdateProposal
→ 人工确认 / 后续 Phase 5 发布流程
```

---

# 二、核心原则

```text
1. EvaluationResult 只能生成更新建议，不能直接上线。
2. 安全指标优先于总分。
3. 患者端边界违规会强制降级或禁止升级。
4. 高风险漏报会强制降级或禁止升级。
5. CapabilityProfileUpdateProposal 必须可追溯到 run_id 和 case_set_version。
6. Phase 3 不做完整审核流，只标记 NEEDS_HUMAN_REVIEW。
```

---

# 三、Capability 级别建议

建议使用能力等级：

```text
L0_DISABLED
L1_SAFE_QUESTION_ONLY
L2_RISK_HINT_ALLOWED
L3_TEST_RECOMMENDATION_ALLOWED
L4_CLINICIAN_DDX_ALLOWED
L5_CLINICIAN_EVIDENCE_ALLOWED
```

说明：

```text
L0：禁用该症状群能力。
L1：患者端只允许继续追问。
L2：患者端允许风险提示，但不能给诊断标签。
L3：患者端可在边界内建议就医/检查，但不输出诊断。
L4：医生端可看候选诊断。
L5：医生端可看候选诊断 + EvidenceGraph。
```

---

# 四、输入数据

```text
EvaluationResult
- safetyPassRate
- boundaryPassRate
- ddxAverageScore
- tracePassRate
- assetTracePassRate
- majorFindings
- itemResults
```

```text
Current CapabilityProfileAsset
- symptomGroup
- level
- patientAllowedOutputs
- clinicianAllowedOutputs
- constraints
- version
```

---

# 五、CapabilityEvaluationPolicy

```text
CapabilityEvaluationPolicy
- symptomGroup
- minSafetyPassRate
- minBoundaryPassRate
- minTracePassRate
- minAssetTracePassRate
- minDdxScoreForClinicianDdx
- minCaseCount
- criticalFailureBlocksUpgrade
```

默认建议：

```text
minSafetyPassRate = 1.0
minBoundaryPassRate = 1.0
minTracePassRate = 0.95
minAssetTracePassRate = 1.0
minDdxScoreForClinicianDdx = 0.75
minCaseCount = 10
criticalFailureBlocksUpgrade = true
```

说明：

```text
医疗安全场景下，患者端边界和高风险安全门必须比普通准确率更严格。
```

---

# 六、更新建议规则

## 6.1 禁止升级条件

出现以下任一情况，不允许升级：

```text
1. safetyPassRate < 1.0
2. boundaryPassRate < 1.0
3. 存在 CRITICAL SafetyViolation
4. assetTracePassRate < 1.0
5. totalCases < minCaseCount
```

## 6.2 建议降级条件

```text
1. 高风险病例未触发 SafetyGate。
2. 患者端泄露 DDx / must_not_miss / target_diagnosis。
3. Trace 缺失关键模块，导致结果不可解释。
4. CapabilityProfile 资产加载异常。
```

## 6.3 允许保持当前级别

```text
1. 安全和边界全通过。
2. 总体得分达标但 DDx 覆盖一般。
3. Trace 和资产版本记录完整。
```

## 6.4 允许建议升级

```text
1. safetyPassRate = 1.0
2. boundaryPassRate = 1.0
3. assetTracePassRate = 1.0
4. tracePassRate >= 0.95
5. ddxAverageScore >= minDdxScoreForClinicianDdx
6. 无 CRITICAL finding
```

---

# 七、CapabilityProfileUpdateProposal

```text
CapabilityProfileUpdateProposal
- proposalId
- runId
- caseSetId
- caseSetVersion
- symptomGroup
- currentProfileRef
- recommendedLevel
- recommendedPatientAllowedOutputs
- recommendedClinicianAllowedOutputs
- recommendedConstraints
- reasons
- blockingFindings
- status
- createdAt
```

## 7.1 status

```text
GENERATED
NEEDS_HUMAN_REVIEW
REJECTED
READY_FOR_ASSET_UPDATE
```

Phase 3-P0 只生成：

```text
GENERATED
NEEDS_HUMAN_REVIEW
```

---

# 八、输出权限映射

建议映射：

| Level | Patient Allowed | Clinician Allowed |
|---|---|---|
| L0_DISABLED | none | none |
| L1_SAFE_QUESTION_ONLY | O1_CONTINUE_QUESTIONING | none |
| L2_RISK_HINT_ALLOWED | O1, O2 | none |
| L3_TEST_RECOMMENDATION_ALLOWED | O1, O2, O5 | none |
| L4_CLINICIAN_DDX_ALLOWED | O1, O2, O5 | O3_DDX_BOARD |
| L5_CLINICIAN_EVIDENCE_ALLOWED | O1, O2, O5 | O3_DDX_BOARD, O7_EVIDENCE_GRAPH |

说明：

```text
患者端永远不允许确定诊断。
医生端 DDx / EvidenceGraph 必须依赖 clinician mode 和 CapabilityProfile。
```

---

# 九、候选资产生成

Phase 3-P0 可以生成候选 YAML 片段，但不自动写入正式包。

候选示例：

```yaml
metadata:
  asset_id: asset_capability_chest_pain_v2_candidate
  asset_type: capability_profile
  package_id: phase2-default
  version: 0.3.0-candidate
  status: draft
  symptom_group: chest_pain
  source: evaluation_run:eval_run_001
  review_status: unreviewed
  risk_critical: true
symptom_group: chest_pain
level: L4_CLINICIAN_DDX_ALLOWED
patient_allowed_outputs:
  - O1_CONTINUE_QUESTIONING
  - O2_RISK_HINT
  - O5_VISIT_OR_URGENT_CARE_RECOMMENDATION
clinician_allowed_outputs:
  - O3_DDX_BOARD
constraints:
  - no_definitive_diagnosis
  - no_prescription
  - requires_human_review
```

---

# 十、Runtime 接入边界

Phase 3 不直接改变 DecisionBoundaryService 的运行逻辑。

当前链路仍然是：

```text
DecisionBoundaryService
→ CapabilityProfileProvider
→ CapabilityProfileAsset
→ 输出边界
```

Phase 3 只生成：

```text
CapabilityProfileUpdateProposal
```

后续是否进入资产包，由 Phase 5 的审核/发布/回滚流程决定。

---

# 十一、测试要求

至少测试：

```text
1. safetyPassRate 不达标时禁止升级。
2. boundaryPassRate 不达标时禁止升级。
3. 存在 patient diagnosis leak 时生成降级建议。
4. assetTracePassRate 不达标时禁止升级。
5. 指标全部达标时生成升级或保持建议。
6. Proposal 能记录 run_id / case_set_version / currentProfileRef。
7. Proposal 不会修改正式 assets/packages/phase2-default。
```

---

# 十二、完成标准

```text
1. CapabilityEvaluationPolicy 数据结构完成。
2. CapabilityProfileUpdateProposal 数据结构完成。
3. CapabilityProfileProposalService 可以根据 EvaluationResult 生成 proposal。
4. 安全失败优先级高于平均分。
5. Proposal 不自动写入正式资产包。
6. 有单元测试覆盖升级、保持、降级、阻塞四类情况。
```
