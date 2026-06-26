# Phase 2 开发任务清单

> 本文档用于跟踪 ClinMindRuntime Phase 2 共享能力资产原型的实现进度。  
> Phase 2 的目标是 Provider 抽象、资产包、资产版本、Runtime 资产接入和测试验收。  
> AI / Cursor / Claude Code / Codex 每完成一个实现任务后，必须同步更新本文档。

---

# 一、使用规则

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要人工确认
```

```text
1. 每次只实现一个小任务或一个小模块。
2. 每次实现前，先确认任务属于 Phase2-P0-A 到 Phase2-P0-G 的哪一项。
3. 每次实现后，必须更新任务状态。
4. 不允许把 Phase 3–5 的训练、评估、经验进化、平台后台提前塞进 Phase 2。
5. 标记完成前，必须有对应代码和 JUnit 测试，或在备注中说明原因。
6. Phase 2 修改不能破坏 Phase 1 测试。
```

---

# 二、Phase2-P0-A：资产元数据与 Provider 接口

目标：先建立稳定接口和数据结构，不改 Runtime 主流程。

## 2.1 资产基础结构

- [x] 创建 `asset/AssetMetadata.java`
- [x] 创建 `asset/AssetType.java`
- [x] 创建 `asset/AssetStatus.java`
- [x] 创建 `asset/ReviewStatus.java`
- [x] 创建 `asset/AssetVersion.java`
- [x] 创建 `asset/AssetPackageManifest.java`
- [x] 创建 `asset/AssetQueryContext.java`
- [x] 创建 `asset/AssetUsedRecord.java`
- [x] 创建 `asset/AssetLoadException.java`
- [x] 编写资产基础结构单元测试

## 2.2 Provider 接口

- [x] 创建 `provider/MedicalKnowledgeProvider.java`
- [x] 创建 `provider/RedFlagRuleProvider.java`
- [x] 创建 `provider/TestRecommendationProvider.java`
- [x] 创建 `provider/CapabilityProfileProvider.java`
- [x] 创建 `provider/ClinicalExperienceProvider.java`
- [x] 创建 `provider/EvidenceAssetProvider.java`
- [x] 创建 `asset/AssetPackageRepository.java`
- [x] 编写 Provider 接口编译测试或最小 mock 测试

## 2.3 Phase2-P0-A 验收

- [x] Provider 接口不依赖具体 YAML 实现
- [x] Provider 返回对象包含 AssetMetadata
- [x] AssetLoadException 能表达安全关键资产加载失败
- [x] 所有测试通过

---

# 三、Phase2-P0-B：YAML Asset Package Repository

目标：把旧 assets 目录升级为资产包读取机制。

## 3.1 资产包目录

- [x] 创建 `src/main/resources/assets/packages/phase2-default/manifest.yml`
- [x] 创建 `src/main/resources/assets/packages/phase2-default/symptom-groups/chest-pain.yml`
- [x] 创建 `src/main/resources/assets/packages/phase2-default/symptom-groups/fever.yml`
- [x] 创建 `src/main/resources/assets/packages/phase2-default/red-flag-rules.yml`
- [x] 创建 `src/main/resources/assets/packages/phase2-default/test-recommendation-rules.yml`
- [x] 创建 `src/main/resources/assets/packages/phase2-default/capability-profiles.yml`
- [x] 创建 `src/main/resources/assets/packages/phase2-default/experience-units.yml`
- [x] 创建 `src/test/resources/assets/packages/phase2-alt/` 替代资产包
- [x] 创建 `src/test/resources/assets/packages/broken-package/` 错误资产包

## 3.2 Repository 实现

- [x] 创建 `provider/yaml/YamlAssetPackageRepository.java`
- [x] 实现 `loadManifest(packageId)`
- [x] 实现 `listPackages()`
- [x] 实现 `loadResource(packageId, relativePath)`
- [x] 支持默认 active package
- [x] disabled / deprecated package 不能被 Runtime 使用
- [x] 缺失安全关键资产时抛出 AssetLoadException
- [x] 编写 YamlAssetPackageRepository 单元测试

## 3.3 Phase2-P0-B 验收

- [x] 默认资产包能加载
- [x] 替代资产包能加载
- [x] broken package 能触发错误
- [x] manifest 字段能被解析
- [x] 所有测试通过

---

# 四、Phase2-P0-C：YAML Provider 实现

目标：用 Provider 接口包装 YAML 资产包。

## 4.1 Provider 实现

- [x] 创建 `provider/yaml/YamlMedicalKnowledgeProvider.java`
- [x] 创建 `provider/yaml/YamlRedFlagRuleProvider.java`
- [x] 创建 `provider/yaml/YamlTestRecommendationProvider.java`
- [x] 创建 `provider/yaml/YamlCapabilityProfileProvider.java`
- [x] 创建 `provider/yaml/YamlClinicalExperienceProvider.java`
- [x] 创建 `provider/yaml/StaticEvidenceAssetProvider.java` 或空实现

## 4.2 行为约束

- [x] MedicalKnowledgeProvider 返回 SymptomGroupAsset / MedicalKnowledgeAsset
- [x] RedFlagRuleProvider 返回带 AssetMetadata 的 RedFlagRuleAsset
- [x] TestRecommendationProvider 返回带 AssetMetadata 的 TestRecommendationAsset
- [x] CapabilityProfileProvider 返回带 AssetMetadata 的 CapabilityProfileAsset
- [x] ClinicalExperienceProvider 只返回 MOCK_VERIFIED / HUMAN_VERIFIED 经验单元
- [x] EvidenceAssetProvider 只返回静态 ref 或空列表，不做 RAG

## 4.3 测试

- [x] 编写 YamlMedicalKnowledgeProviderTest
- [x] 编写 YamlRedFlagRuleProviderTest
- [x] 编写 YamlTestRecommendationProviderTest
- [x] 编写 YamlCapabilityProfileProviderTest
- [x] 编写 YamlClinicalExperienceProviderTest

---

# 五、Phase2-P0-D：Runtime 接入 Provider

目标：Runtime 模块不再直接依赖 StaticRuleProvider 具体类。

## 5.1 模块改造

- [x] KnowledgeContextService 改为依赖 MedicalKnowledgeProvider / RedFlagRuleProvider / TestRecommendationProvider
- [x] SafetyGateService 不再直接依赖 StaticRuleProvider
- [x] EvidenceGraphService 不再直接依赖 StaticRuleProvider
- [x] DecisionBoundaryService 使用标准化 CapabilityProfileProvider 接口
- [x] ExperienceContextService 使用 ClinicalExperienceProvider
- [x] 删除或降级 StaticRuleProvider 为兼容 Adapter，不再作为 Runtime 主依赖

## 5.2 Trace 接入

- [x] RuntimeTrace 记录 packageId
- [x] RuntimeTrace 记录 assetId
- [x] RuntimeTrace 记录 assetVersion
- [x] KnowledgeContext.sourceAssets 记录 asset_id@version
- [x] 新增 AssetUsedRecord 或等价结构

## 5.3 Phase2-P0-D 验收

- [x] Runtime 核心服务不再注入 StaticRuleProvider
- [x] 替换资产包不需要改 Runtime 核心代码
- [x] Phase 1 测试继续通过

---

# 六、Phase2-P0-E：ExperienceContext 原型

目标：让 ExperienceContext 不再只是空对象，而是能返回少量 verified/mock experience units。

## 6.1 经验资产

- [x] 创建 `experience-units.yml`
- [x] 创建 `asset/ExperienceUnitAsset.java`
- [x] 支持 triggerFeatures
- [x] 支持 suggestedQuestions
- [x] 支持 suggestedCautions
- [x] 支持 affectedModules
- [x] 支持 reviewStatus

## 6.2 Runtime 接入

- [x] ExperienceContextService 调用 ClinicalExperienceProvider
- [x] ExperienceContext 写入 matchedExperienceUnits
- [x] RuntimeTrace 记录 experience asset id
- [x] 经验单元不能直接决定诊断
- [x] 经验单元不能绕过 SafetyGate / DecisionBoundary

## 6.3 测试

- [x] 编写 ClinicalExperienceProviderTest（YamlClinicalExperienceProviderTest）
- [x] 编写 ExperienceContextRuntimeIntegrationTest

---

# 七、Phase2-P0-F：资产调试 API

目标：提供最小只读 API，方便人工验收资产包和 Runtime 资产使用情况。

## 7.1 API

- [x] 创建 `api/AssetController.java`
- [x] 实现 `GET /api/v1/assets/packages`
- [x] 实现 `GET /api/v1/assets/packages/{package_id}`
- [x] 实现 `GET /api/v1/assets/packages/{package_id}/symptom-groups/{symptom_group}`
- [x] 实现 `GET /api/v1/runtime/{runtime_id}/assets-used`
- [x] 不提供资产编辑 API

## 7.2 测试

- [x] 编写 AssetControllerTest
- [x] 编写 RuntimeAssetsUsedApiTest
- [x] 患者端输出仍不泄露内部资产细节

---

# 八、Phase2-P0-G：集成测试与回归验收

目标：验证 Phase 2 不破坏 Phase 1，并证明资产包可替换。

## 8.1 集成测试

- [ ] RuntimeWithDefaultAssetPackageTest
- [ ] RuntimeWithAlternateAssetPackageTest
- [ ] RuntimeWithBrokenAssetPackageTest
- [ ] RuntimeAssetTraceIntegrationTest
- [ ] PatientOutputAssetIsolationTest
- [ ] Phase1RegressionTest 或直接运行 Phase 1 现有测试

## 8.2 验收项

- [ ] 默认资产包 Runtime 跑通
- [ ] 替代资产包 Runtime 跑通
- [ ] broken package 触发 fail-safe
- [ ] RuntimeTrace 记录 asset_id / version
- [ ] ExperienceContext 返回 mock/verified experience units
- [ ] 患者端不泄露 DDx / must_not_miss / target_diagnosis
- [ ] 医生端仍能看到 DDx / EvidenceGraph
- [ ] Phase 1 所有测试继续通过

---

# 九、问题记录

| 编号 | 问题 | 影响模块 | 状态 | 处理结论 |
|---|---|---|---|---|
| Q1 | 暂无 | - | - | - |

---

# 十、变更记录

| 日期 | 变更 | 说明 |
|---|---|---|
| 2026-06-26 | 创建 Phase 2 任务清单 | 用于约束共享能力资产原型实现 |
| 2026-06-26 | 完成 Phase2-P0-A | 资产元数据、Provider 接口、AssetLoadException 及单元/Mock 测试；Runtime 主流程未改动 |
| 2026-06-26 | 完成 Phase2-P0-B | phase2-default/alt/broken 资产包与 YamlAssetPackageRepository；Runtime 主流程未改动 |
| 2026-06-26 | 完成 Phase2-P0-C | 6 个 YAML Provider 实现 + 5 个单元测试；共享 YamlAssetParsingSupport/YamlProviderSupport；Runtime 主流程未改动 |
| 2026-06-26 | 完成 Phase2-P0-D | Runtime 接入 Provider；sourceAssets 记录 asset_id@version；StartRuntimeRequest 支持 asset_context；StaticRuleProvider 降级为兼容类 |
| 2026-06-26 | 完成 Phase2-P0-E | ExperienceContext 原型：triggerFeatures 匹配、Trace 记录 assetRef、集成测试验证不绕过 SafetyGate/DecisionBoundary |
