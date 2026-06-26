# AI Implementation Skill：ClinMindRuntime Phase 2

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前阶段进入 Phase 2：共享能力资产原型。  
> Phase 1 Runtime MVP 已基本完成，后续修改不得破坏 Phase 1 的 Runtime、安全门、输出边界和 Trace 验收结果。

---

# 一、当前项目阶段

```text
Phase 2：共享能力资产原型
```

当前目标：

```text
把 Phase 1 的静态 YAML 规则升级为可管理、可替换、可版本化、可追踪的共享能力资产原型。
```

Phase 2 要证明的工程闭环：

```text
Asset Package Manifest
→ AssetMetadata / AssetVersion
→ Provider Interfaces
→ YAML Provider Implementations
→ Knowledge / RedFlag / Test / Capability / Experience Assets
→ Runtime 通过 Provider 读取资产
→ RuntimeTrace 记录 asset_id / version / package_id
→ 患者端输出边界保持不变
→ Phase 1 回归测试继续通过
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase2_开发任务清单.md
3. docs/Phase2_共享能力资产原型_实现规格.md
4. docs/Phase2_Provider接口设计.md
5. docs/Phase2_资产数据结构与版本设计.md
6. docs/Phase2_API与测试设计.md
7. docs/Phase1_技术栈与工程架构决策.md
8. docs/Phase1_Runtime_MVP_实现规格.md
9. docs/Phase1_数据结构与状态设计.md
10. docs/Phase1_模块接口设计.md
11. docs/Phase1_API与测试设计.md
12. docs/ClinMindRuntime阶段拆分路线图.md
13. docs/ClinMindRuntime完整系统设计.md
```

解释：

```text
Phase 2 文档优先于 Phase 1 文档，用于指导当前新增能力。
Phase 1 文档仍然约束 Runtime Core、安全门、输出边界和患者端安全表达。
总设计文档描述完整愿景，但不能作为提前实现 Phase 3–5 能力的理由。
```

---

# 三、当前技术栈决策

```text
Runtime Core：Java 17+ / Spring Boot 3.x
API：Spring Web
Validation：Jakarta Validation
Trace：Spring AOP + RuntimeTrace
Data Model：Java enum / record / class
Config Assets：YAML / JSON Asset Package
Testing：JUnit 5 + AssertJ / Mockito
Storage Phase 2：仍以 classpath YAML / memory 为主
Python：后续可选 AI Provider，不作为 Runtime 主工程
```

AI 框架边界：

```text
Spring AI / LangChain4j / LangChain / LangGraph 只能作为后续 Provider / Adapter。
它们不能成为 Runtime 主控。
RuntimeState、SafetyGate、EvidenceGraph、DecisionBoundary 必须由 ClinMindRuntime 自己控制。
```

---

# 四、当前允许实现的内容

## 4.1 Asset 基础结构

```text
AssetMetadata
AssetType
AssetStatus
ReviewStatus
AssetVersion
AssetPackageManifest
AssetQueryContext
AssetUsedRecord
AssetLoadException
```

## 4.2 Provider 接口

```text
MedicalKnowledgeProvider
RedFlagRuleProvider
TestRecommendationProvider
CapabilityProfileProvider
ClinicalExperienceProvider
EvidenceAssetProvider
AssetPackageRepository
```

## 4.3 YAML Provider 实现

```text
YamlAssetPackageRepository
YamlMedicalKnowledgeProvider
YamlRedFlagRuleProvider
YamlTestRecommendationProvider
YamlCapabilityProfileProvider
YamlClinicalExperienceProvider
StaticEvidenceAssetProvider 或空实现
```

## 4.4 Runtime 接入

```text
KnowledgeContextService 通过 Provider 聚合资产
SafetyGateService 通过 Provider 获得危险信号
EvidenceGraphService 通过 Provider 获得检查建议
DecisionBoundaryService 通过 CapabilityProfileProvider 获得能力档案
ExperienceContextService 通过 ClinicalExperienceProvider 获得经验单元
RuntimeTrace 记录 asset_id / version / package_id
```

## 4.5 只读调试 API

```text
GET /api/v1/assets/packages
GET /api/v1/assets/packages/{package_id}
GET /api/v1/assets/packages/{package_id}/symptom-groups/{symptom_group}
GET /api/v1/runtime/{runtime_id}/assets-used
```

---

# 五、当前禁止实现的内容

```text
1. 不做完整知识管理后台。
2. 不做前端 Asset Console。
3. 不做真实医生审核流程。
4. 不做自动经验学习。
5. 不做真实随访结局回填。
6. 不做完整 RAG Evidence Library。
7. 不做完整 KG-lite。
8. 不引入向量数据库。
9. 不引入复杂数据库治理。
10. 不引入 Spring Cloud、Nacos、消息队列或复杂微服务。
11. 不让 LangChain / LangGraph / Spring AI / LangChain4j 取代 Runtime 主控。
12. 不改变患者端安全输出边界。
13. 不输出患者端确定诊断。
14. 不绕过 SafetyGate。
15. 不绕过 DecisionBoundary。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 2 共享能力资产原型。本次只保留接口或 mock，不实现真实能力。
```

---

# 六、实现顺序

AI 必须按以下顺序推进，不要跳到后续模块。

```text
Phase2-P0-A：资产元数据与 Provider 接口
Phase2-P0-B：YAML Asset Package Repository
Phase2-P0-C：YAML Provider 实现
Phase2-P0-D：Runtime 接入 Provider
Phase2-P0-E：ExperienceContext 原型
Phase2-P0-F：资产调试 API
Phase2-P0-G：集成测试与回归验收
```

每次实现任务只能覆盖一个小阶段，不能一次性生成整个系统。

---

# 七、任务清单同步规则

AI 每次实现、修改或测试 Phase 2 代码后，必须同步更新：

```text
docs/Phase2_开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase2_开发任务清单.md。
2. 确认当前任务属于 Phase2-P0-A 到 Phase2-P0-G 的哪一项。
3. 将正在处理的任务从 [ ] 改为 [/]。
4. 如果任务不在清单中，先在对应阶段补充任务项，不要直接实现。
```

实现后：

```text
1. 将已完成任务从 [/] 改为 [x]。
2. 如果任务部分完成，保持 [/]，并补充备注或问题记录。
3. 如果任务被阻塞，将状态改为 [!]，并在问题记录中说明原因。
4. 如果实现过程中新增了必要任务，补充到对应阶段。
```

---

# 八、架构约束

## 8.1 Runtime 主控权不变

```text
Provider 只能提供结构化资产。
Provider 不能直接修改 RuntimeState。
Provider 不能决定最终输出。
Provider 不能绕过 SafetyGate 或 DecisionBoundary。
```

## 8.2 Provider 必须返回结构化对象

正确方式：

```text
Provider → Asset Object → KnowledgeContext / SafetyGate / EvidenceGraph / DecisionBoundary
```

错误方式：

```text
Provider 返回一段自然语言，让 LLM 或 Runtime 直接拼接为最终回答。
```

## 8.3 安全关键资产必须 fail-closed

安全关键资产包括：

```text
red flag rules
capability profile
must-not-miss symptom group assets
```

加载失败时必须进入 fail-safe 或 `ERROR_SAFE_HALTED`。

## 8.4 患者端隔离必须继续保持

```text
Patient-facing API 不能泄露 DDx、must_not_miss、target_diagnosis、full evidence graph。
任何新增资产字段默认不得出现在患者端响应中。
```

## 8.5 RuntimeTrace 必须记录资产使用

Phase 2 至少记录：

```text
package_id
asset_id
asset_type
version
module_name
```

---

# 九、测试约束

每实现一个 Provider 或 Asset 模块，必须补充 JUnit 测试。

至少包含：

```text
AssetMetadataTest
AssetPackageManifestTest
YamlAssetPackageRepositoryTest
YamlMedicalKnowledgeProviderTest
YamlRedFlagRuleProviderTest
YamlTestRecommendationProviderTest
YamlCapabilityProfileProviderTest
YamlClinicalExperienceProviderTest
RuntimeWithDefaultAssetPackageTest
RuntimeWithAlternateAssetPackageTest
RuntimeWithBrokenAssetPackageTest
RuntimeAssetTraceIntegrationTest
PatientOutputAssetIsolationTest
```

每次 Phase 2 改动后，必须确保 Phase 1 测试继续通过。

---

# 十、AI 每次执行任务前的检查清单

```text
1. 当前任务属于 Phase 2 吗？
2. 当前任务属于 Phase2-P0-A 到 Phase2-P0-G 的哪一步？
3. 是否读取了 Phase2_开发任务清单？
4. 是否会误实现 Phase 3–5 的内容？
5. 是否保持 Runtime Core 不被 Provider 取代？
6. 是否需要新增或更新 JUnit 测试？
7. 是否会影响患者端输出边界？
```

---

# 十一、当前最优下一步

当前最优实现任务是：

```text
Phase2-P0-A：资产元数据与 Provider 接口
```

具体包括：

```text
1. 创建 asset 基础数据结构。
2. 创建 provider 接口。
3. 创建 AssetLoadException。
4. 编写基础单元测试。
5. 同步更新 docs/Phase2_开发任务清单.md。
```

不要在这个任务中实现 RAG、KG、训练中心、经验进化、前端后台或真实审核流。

---

# 十二、最终约束

```text
当前不是在实现完整医疗 AI 平台。
当前是在实现 Phase 2 共享能力资产原型。
Phase 1 Runtime Core 必须保持稳定。
Provider 只能作为资产读取与能力接入层。
Phase 2 的目标是让 Runtime 依赖可替换、可版本化、可追踪的共享资产，而不是继续堆医疗问答功能。
```
