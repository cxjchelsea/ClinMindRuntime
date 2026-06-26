# Phase 2 资产数据结构与版本设计

> 本文档定义 Phase 2 共享能力资产原型的数据结构、版本字段、状态字段和资产包组织方式。  
> Phase 2 的核心是让知识、规则、能力档案和经验单元从“临时 YAML”升级为“可识别、可替换、可追踪的资产”。

---

# 一、设计原则

```text
1. 每个资产必须有唯一 asset_id。
2. 每个资产必须有 version。
3. 每个资产必须有 status。
4. 每个资产必须知道属于哪个 package。
5. RuntimeTrace 必须能记录本轮使用过的资产。
6. 患者端不能暴露内部诊断资产细节。
7. Phase 2 先用 YAML 实现，不引入数据库。
```

---

# 二、资产包结构

正式默认资产包放在 `main/resources`，用于 Runtime 默认运行；替代资产包和错误资产包放在 `test/resources`，只用于替换测试和 fail-safe 测试。

推荐目录：

```text
src/main/resources/assets/packages/phase2-default/
  manifest.yml
  symptom-groups/
    chest-pain.yml
    fever.yml
  red-flag-rules.yml
  test-recommendation-rules.yml
  capability-profiles.yml
  experience-units.yml
  evidence-refs.yml

src/test/resources/assets/packages/phase2-alt/
  manifest.yml
  symptom-groups/
  red-flag-rules.yml
  test-recommendation-rules.yml
  capability-profiles.yml
  experience-units.yml
  evidence-refs.yml

src/test/resources/assets/packages/broken-package/
  manifest.yml
  缺失或损坏的安全关键资产，用于验证 fail-safe
```

约束：

```text
phase2-default：正式默认资产包，进入 main/resources。
phase2-alt：替代资产包，只用于测试 Runtime 不改代码也能切换资产。
broken-package：错误资产包，只用于测试安全关键资产加载失败时 fail-closed。
```

---

# 三、AssetPackageManifest

## 3.1 字段

```text
packageId: String
version: String
status: AssetStatus
displayName: String
description: String
createdAt: Instant
updatedAt: Instant
source: String
owner: String
supportedSymptomGroups: List<String>
defaultPackage: boolean
```

## 3.2 YAML 示例

```yaml
package_id: phase2-default
version: 0.2.0
status: active
display_name: Phase 2 Default Asset Package
description: Minimal shared assets for Phase 2 prototype
created_at: 2026-06-26T00:00:00Z
updated_at: 2026-06-26T00:00:00Z
source: internal_phase2_yaml
owner: ClinMindRuntime
supported_symptom_groups:
  - chest_pain
  - fever
default_package: true
```

---

# 四、AssetMetadata

所有资产共享元数据。

```text
assetId: String
assetType: AssetType
packageId: String
version: String
status: AssetStatus
symptomGroup: String
source: String
createdAt: Instant
updatedAt: Instant
reviewStatus: ReviewStatus
riskCritical: boolean
```

说明：

```text
riskCritical=true 的资产加载失败必须 fail-closed。
```

---

# 五、枚举设计

## 5.1 AssetType

```text
SYMPTOM_GROUP
RED_FLAG_RULE
TEST_RECOMMENDATION
CAPABILITY_PROFILE
EXPERIENCE_UNIT
EVIDENCE_REF
CLINICAL_PATHWAY
KG_LITE_REF
```

## 5.2 AssetStatus

```text
DRAFT
ACTIVE
DISABLED
DEPRECATED
ARCHIVED
```

Phase 2 只允许 Runtime 使用 `ACTIVE` 资产。

## 5.3 ReviewStatus

```text
UNREVIEWED
MOCK_VERIFIED
HUMAN_VERIFIED
REJECTED
```

Phase 2 可以使用 `MOCK_VERIFIED`，不做真实医生审核。

---

# 六、SymptomGroupAsset

```text
metadata: AssetMetadata
symptomGroup: String
commonDiagnoses: List<DiagnosisRef>
mustNotMiss: List<DiagnosisRef>
requiredQuestions: List<String>
recommendedTests: List<String>
clinicalPathwayRefs: List<String>
evidenceRefs: List<String>
```

YAML 示例：

```yaml
metadata:
  asset_id: asset_symptom_chest_pain_v1
  asset_type: symptom_group
  package_id: phase2-default
  version: 0.2.0
  status: active
  symptom_group: chest_pain
  source: internal_yaml
  review_status: mock_verified
  risk_critical: true
symptom_group: chest_pain
common_diagnoses:
  - name: low_risk_chest_discomfort
    risk_level: low
must_not_miss:
  - name: high_risk_chest_pain_condition
    risk_level: high
required_questions:
  - 是否与活动相关？
recommended_tests:
  - 基础检查 A
clinical_pathway_refs: []
evidence_refs: []
```

---

# 七、RedFlagRuleAsset

```text
metadata: AssetMetadata
ruleId: String
symptomGroup: String
features: List<String>
riskLevel: RiskLevel
action: String
patientConstraint: String
```

约束：

```text
RedFlagRuleAsset 是安全关键资产，riskCritical 必须为 true。
加载失败必须 fail-closed。
```

---

# 八、TestRecommendationAsset

```text
metadata: AssetMetadata
ruleId: String
symptomGroup: String
targetStatus: CandidateStatus
recommendedTests: List<String>
purpose: String
patientVisibleDefault: boolean
```

说明：

```text
patientVisibleDefault 不代表患者端一定可见，最终仍由 DecisionBoundary 决定。
```

---

# 九、CapabilityProfileAsset

```text
metadata: AssetMetadata
symptomGroup: String
level: String
patientAllowedOutputs: List<OutputLevel>
clinicianAllowedOutputs: List<OutputLevel>
constraints: List<String>
```

约束：

```text
CapabilityProfileAsset 是安全关键资产。
加载失败时 DecisionBoundary 必须 fail-safe。
```

---

# 十、ExperienceUnitAsset

```text
metadata: AssetMetadata
experienceId: String
symptomGroup: String
triggerFeatures: List<String>
summary: String
suggestedQuestions: List<String>
suggestedCautions: List<String>
affectedModules: List<String>
confidence: double
```

约束：

```text
1. Phase 2 只允许 MOCK_VERIFIED 或 HUMAN_VERIFIED 的经验单元进入 ExperienceContext。
2. 经验单元不能直接决定诊断。
3. 经验单元不能绕过 SafetyGate。
4. 经验单元不能绕过 DecisionBoundary。
```

---

# 十一、EvidenceAssetRef

Phase 2 只做引用，不做完整 RAG。

```text
metadata: AssetMetadata
refId: String
symptomGroup: String
title: String
sourceType: String
sourceUri: String
summary: String
linkedDiagnoses: List<String>
```

说明：

```text
Phase 2 的 EvidenceAssetRef 可以作为 KnowledgeContext / EvidenceGraph 的 source ref，
但不做向量检索、不做全文证据库、不做自动引用生成。
```

---

# 十二、AssetUsedRecord

RuntimeTrace 应记录资产使用情况。

```text
runtimeId: String
traceId: String
packageId: String
assetId: String
assetType: AssetType
version: String
symptomGroup: String
moduleName: String
usedAt: Instant
```

Phase 2 可以先写入：

```text
RuntimeTrace.knowledgeUsed
RuntimeTrace.outputSummary.asset_package_id
RuntimeTrace.outputSummary.asset_versions
```

后续 Phase 5 再拆为正式审计表。

---

# 十三、版本规则

```text
1. version 使用语义化版本，例如 0.2.0。
2. package version 与 asset version 可以相同，也可以不同。
3. Runtime 默认使用 active package。
4. 如果指定 package_id，则必须校验 package status=active。
5. disabled / deprecated / archived 资产不能进入 Runtime。
```

---

# 十四、迁移策略

Phase 1 当前资产：

```text
src/main/resources/assets/symptom-groups/*.yml
src/main/resources/assets/red-flag-rules.yml
src/main/resources/assets/test-recommendation-rules.yml
src/main/resources/assets/capability-profiles.yml
```

Phase 2 迁移后：

```text
src/main/resources/assets/packages/phase2-default/...
```

测试替代资产和错误资产放在：

```text
src/test/resources/assets/packages/phase2-alt/...
src/test/resources/assets/packages/broken-package/...
```

迁移过程允许短期保留旧目录，但 Runtime 模块应逐步改为读取资产包。

---

# 十五、完成标准

```text
1. AssetMetadata、AssetType、AssetStatus、ReviewStatus 数据结构创建完成。
2. AssetPackageManifest 可以从 YAML 读取。
3. 默认资产包和替代资产包都能加载。
4. RuntimeTrace 能记录 asset package 和 asset version。
5. 错误版本、禁用资产、缺失资产能触发 fail-safe 或安全降级。
6. Phase 1 测试仍然通过。
```
