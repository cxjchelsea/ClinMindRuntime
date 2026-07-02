# Phase 2 共享能力资产原型实现规格

> 本文档定义 ClinMindRuntime Phase 2 的目标、范围、能力边界、模块清单、开发顺序和验收标准。  
> Phase 2 的核心不是继续堆 Runtime 流程，而是把 Phase 1 的静态 YAML 规则升级为可管理、可版本化、可替换的共享能力资产原型。

---

# 一、阶段定位

Phase 1 已经完成最小受控诊断 Runtime：

```text
RuntimeState
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
RuntimeTrace
```

Phase 2 要解决的新问题是：

```text
Runtime 依赖的知识、规则、能力档案和经验上下文，如何从临时静态配置升级为可管理、可替换、可版本化的共享能力资产？
```

Phase 2 的产物应服务 Runtime，而不是替代 Runtime。

---

# 二、Phase 2 核心目标

```text
建立共享能力资产原型。
```

具体目标：

```text
1. 把 StaticRuleProvider 抽象为 Provider / Repository / Asset Package 体系。
2. 让症状群规则、危险信号、检查建议、能力档案、经验单元都有统一资产元数据。
3. 支持多套资产包，例如 assets/v1、assets/v2、assets-test。
4. Runtime 不直接依赖具体 YAML 文件，而是依赖 Provider 接口。
5. RuntimeTrace 能记录本轮使用了哪些资产及其版本。
6. 错误资产包必须 fail-closed，不能让 SafetyGate 失效。
7. 保持 Phase 1 所有测试病例继续通过。
```

---

# 三、Phase 2 做什么

## 3.1 Provider 抽象层

```text
MedicalKnowledgeProvider
RedFlagRuleProvider
TestRecommendationProvider
CapabilityProfileProvider
ClinicalExperienceProvider
EvidenceAssetProvider（接口预留）
```

Phase 2 的实现可以仍然基于 YAML，但 Runtime 模块必须依赖接口，而不是依赖 `StaticRuleProvider` 具体类。

## 3.2 资产数据结构

```text
AssetMetadata
AssetVersion
SymptomGroupAsset
RedFlagRuleAsset
TestRecommendationAsset
CapabilityProfileAsset
ExperienceUnitAsset
EvidenceAssetRef
AssetPackageManifest
```

## 3.3 YAML Asset Repository

```text
YamlAssetPackageRepository
YamlMedicalKnowledgeProvider
YamlRedFlagRuleProvider
YamlTestRecommendationProvider
YamlCapabilityProfileProvider
YamlClinicalExperienceProvider
```

## 3.4 Runtime 接入改造

```text
KnowledgeContextService 通过 Provider 聚合资产
SafetyGateService 通过 RedFlagRuleProvider 获取危险信号
EvidenceGraphService 通过 TestRecommendationProvider 获取检查建议
DecisionBoundaryService 通过 CapabilityProfileProvider 获取能力档案
ExperienceContextService 通过 ClinicalExperienceProvider 获取已验证经验单元
RuntimeTrace 记录 asset_id / version / package_id
```

## 3.5 最小调试 API

Phase 2 可以增加只读调试 API，但不做前端后台。

```text
GET /api/v1/assets/packages
GET /api/v1/assets/packages/{package_id}
GET /api/v1/assets/packages/{package_id}/symptom-groups/{symptom_group}
GET /api/v1/runtime/{runtime_id}/assets-used
```

---

# 四、Phase 2 不做什么

```text
不做完整知识管理后台
不做前端 Asset Console
不做医生审核流
不做自动经验学习
不做真实随访结局回填
不做完整 RAG Evidence Library
不做完整 KG-lite
不做复杂数据库治理
不引入 Spring Cloud / Nacos / MQ
不让 Spring AI / LangChain4j / LangChain / LangGraph 成为 Runtime 主控
不改变患者端输出边界
不输出患者端确定诊断
```

说明：

```text
Phase 2 可以预留 EvidenceAssetProvider / KgLiteProvider 接口，
但不实现完整 RAG 或知识图谱。
```

---

# 五、推荐工程目录

```text
src/main/java/com/clinmind/runtime/asset/
  AssetMetadata.java
  AssetVersion.java
  AssetStatus.java
  AssetPackageManifest.java
  AssetPackageRepository.java
  AssetUsedRecord.java

src/main/java/com/clinmind/runtime/provider/
  MedicalKnowledgeProvider.java
  RedFlagRuleProvider.java
  TestRecommendationProvider.java
  CapabilityProfileProvider.java
  ClinicalExperienceProvider.java
  EvidenceAssetProvider.java

src/main/java/com/clinmind/runtime/provider/yaml/
  YamlAssetPackageRepository.java
  YamlMedicalKnowledgeProvider.java
  YamlRedFlagRuleProvider.java
  YamlTestRecommendationProvider.java
  YamlCapabilityProfileProvider.java
  YamlClinicalExperienceProvider.java

src/main/resources/assets/packages/
  phase2-default/
    manifest.yml
    symptom-groups/
    red-flag-rules.yml
    test-recommendation-rules.yml
    capability-profiles.yml
    experience-units.yml
  phase2-alt/
    manifest.yml
    ...
```

---

# 六、Phase 2 开发顺序

```text
Phase2-P0-A：资产元数据与 Provider 接口
Phase2-P0-B：YAML Asset Package Repository
Phase2-P0-C：知识 / 风险 / 检查 / 能力 Provider 实现
Phase2-P0-D：ExperienceContext 从空实现升级为 verified/mock experience units
Phase2-P0-E：Runtime 服务改造为依赖 Provider 接口
Phase2-P0-F：资产调试 API 与 assets-used 查询
Phase2-P0-G：集成测试和回归测试
```

每次实现只能处理一个小阶段，不允许一次性生成完整平台。

---

# 七、完成标准

Phase 2 完成时必须满足：

```text
1. Runtime 模块不再直接依赖 StaticRuleProvider 具体类。
2. 至少支持一个默认资产包和一个替代资产包。
3. 每个资产包有 manifest，包含 package_id、version、status、created_at、source。
4. KnowledgeContext.sourceAssets 能记录 asset_id 和 version。
5. RuntimeTrace.outputSummary 或 knowledgeUsed 能体现本轮使用的资产包版本。
6. 替换资产包后 Runtime 核心链路不需要修改。
7. 错误资产包会触发 fail-safe，不会让 SafetyGate 静默失效。
8. ExperienceContext 能返回少量 verified/mock experience units。
9. Phase 1 所有测试继续通过。
10. 新增 Phase 2 Provider / Asset / Runtime 集成测试全部通过。
```

---

# 八、Phase 2 验收用例

至少覆盖：

```text
1. 默认资产包加载成功。
2. 替代资产包加载成功。
3. 同一病例在不同资产包下产生不同 KnowledgeContext。
4. 同一病例在不同资产包下 Runtime 核心流程不需要修改。
5. 错误资产包缺失 red-flag-rules.yml 时进入 ERROR_SAFE_HALTED。
6. Patient-facing 输出不泄露 DDx、must_not_miss、target_diagnosis。
7. Clinician-copilot 输出能看到 DDx 和 EvidenceGraph。
8. RuntimeTrace 能记录使用过的 asset_id / version。
9. ExperienceContext 能返回 verified/mock experience unit。
10. Phase 1 的 12 个 YAML 病例继续通过。
```

---

# 九、与后续阶段关系

| Phase 2 产物 | 后续阶段扩展 |
|---|---|
| Provider 接口 | Phase 3 接 Evaluation Results，Phase 4 接 Experience Memory |
| AssetMetadata / Version | Phase 5 接资产后台、权限、审计、发布和回滚 |
| YamlAssetPackageRepository | 后续替换为 DB / 对象存储 / 远程资产服务 |
| ClinicalExperienceProvider | Phase 4 升级为 Clinical Experience Memory |
| EvidenceAssetProvider 接口 | Phase 2 只预留，Phase 3/4 再接 RAG Evidence Library |
| RuntimeTrace asset 记录 | Phase 4 作为复盘和经验进化输入 |

---

# 十、最终约束

```text
Phase 2 是共享能力资产原型，不是完整平台。
Phase 2 的目标是让 Runtime 依赖 Provider 和资产包，而不是依赖硬编码 YAML。
Phase 2 仍然必须保持 Java Spring Boot Runtime Core。
AI 框架和 Python 服务仍然只能作为后续 Provider / Adapter。
```
