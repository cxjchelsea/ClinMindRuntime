# Phase 2 Runtime 接入改造设计

> 本文档用于约束 Phase 2 中现有 Runtime Core 如何从 `StaticRuleProvider` 直接依赖，迁移为 Provider / Asset Package 依赖。  
> 它不是新的 Runtime 设计，也不是重写 Phase 1；它只定义 Phase 2 对 Phase 1 现有模块的改造边界、顺序和验收标准。

---

# 一、设计定位

Phase 1 已经完成最小受控诊断 Runtime。Phase 2 不重建 Runtime，也不改动 Runtime 的主控原则。

Phase 2 的 Runtime 接入改造目标是：

```text
现有 Runtime 模块继续围绕 RuntimeState 运行，
但知识、规则、能力档案和经验不再由 StaticRuleProvider 直接提供，
而是通过标准 Provider 接口读取带版本信息的共享资产。
```

改造前：

```text
Runtime Service / Domain Services
  → StaticRuleProvider
  → assets/*.yml
```

改造后：

```text
Runtime Service / Domain Services
  → Provider Interfaces
  → YAML Provider Implementations
  → AssetPackageRepository
  → assets/packages/{package_id}/...
```

---

# 二、改造原则

```text
1. RuntimeState 仍然是唯一事实源。
2. SafetyGate、DecisionBoundary、EvidenceGraph 的主控权仍在 Runtime Core。
3. Provider 只能返回结构化资产对象，不能直接修改 RuntimeState。
4. Provider 不能直接生成患者端输出。
5. Provider 不能替代 SafetyGate 或 DecisionBoundary。
6. 患者端输出边界不得因资产字段增加而放宽。
7. 安全关键资产加载失败必须 fail-closed。
8. Phase 1 全部测试必须继续通过。
```

---

# 三、现有模块改造总览

| 现有模块 | Phase 1 依赖 | Phase 2 改造后依赖 | 改造目的 |
|---|---|---|---|
| KnowledgeContextService | StaticRuleProvider | MedicalKnowledgeProvider + RedFlagRuleProvider + TestRecommendationProvider | 聚合带 metadata 的知识资产 |
| SafetyGateService | KnowledgeContext.redFlags | RedFlagRuleProvider 或带 metadata 的 red flag assets | 保证危险信号资产可版本化、可追踪 |
| EvidenceGraphService | StaticRuleProvider | TestRecommendationProvider + EvidenceAssetProvider | 检查建议和证据引用资产化 |
| DecisionBoundaryService | CapabilityProfileProvider 当前实现 | 标准化 CapabilityProfileProvider | 能力档案资产化、版本化 |
| ExperienceContextService | 空实现 / mock | ClinicalExperienceProvider | 经验单元资产化 |
| RuntimeService | RuntimeState + Services | RuntimeState + Services + AssetQueryContext | 传递资产包上下文 |
| RuntimeTrace | modules / knowledgeUsed | modules + assetUsedRecords / package_id / version | 追踪资产来源 |

---

# 四、AssetQueryContext 接入

Phase 2 应引入 `AssetQueryContext`，用于 Runtime 每轮查询资产时携带上下文。

```text
AssetQueryContext
- packageId
- version
- symptomGroup
- runtimeId
- runtimeMode
- fallbackAllowed
```

来源：

```text
1. StartRuntimeRequest.asset_context，可选。
2. 未传入时使用默认 active package。
3. continueRuntime 默认沿用 RuntimeState 中已经绑定的 package。
```

建议在 RuntimeState 中新增最小字段：

```text
assetPackageId
assetPackageVersion
assetsUsed
```

或者短期写入：

```text
RuntimeTrace.outputSummary.asset_package_id
RuntimeTrace.outputSummary.asset_versions
```

Phase 2 推荐优先在 RuntimeState 中加入资产包上下文，避免 continue 轮次无法知道使用哪个资产包。

---

# 五、KnowledgeContextService 改造

## 5.1 Phase 1 状态

Phase 1 中 KnowledgeContextService 负责聚合静态规则，通常依赖 `StaticRuleProvider`。

## 5.2 Phase 2 改造

改造为依赖：

```text
MedicalKnowledgeProvider
RedFlagRuleProvider
TestRecommendationProvider
```

目标输出仍然是 `KnowledgeContext`，但 `sourceAssets` 不再只是文件名，而应记录：

```text
asset_id@version
package_id
asset_type
```

## 5.3 改造后流程

```text
CaseFrame + EntryAssessment
→ build AssetQueryContext
→ MedicalKnowledgeProvider.loadMedicalKnowledge(...)
→ RedFlagRuleProvider.loadRedFlagRules(...)
→ TestRecommendationProvider.loadTestRecommendations(...)
→ build KnowledgeContext
→ record sourceAssets
```

## 5.4 约束

```text
1. symptom group asset 缺失时，如果属于临床问诊，应 fail-safe。
2. must-not-miss 资产缺失时，应 fail-safe。
3. optional questions 缺失可降级为空。
```

---

# 六、SafetyGateService 改造

## 6.1 Phase 1 状态

SafetyGateService 当前读取 `KnowledgeContext.redFlags`。

## 6.2 Phase 2 改造方式

有两种可选方案：

```text
方案 A：SafetyGateService 继续读取 KnowledgeContext.redFlags，但 redFlags 已经来自 RedFlagRuleProvider，且带 sourceAssets。
方案 B：SafetyGateService 直接依赖 RedFlagRuleProvider。
```

Phase 2 推荐先用方案 A，原因：

```text
1. 改动小。
2. KnowledgeContext 仍然是本轮知识上下文聚合结果。
3. SafetyGate 不需要关心资产包读取细节。
```

## 6.3 约束

```text
Red flag rules 是安全关键资产。
加载失败不能返回空规则。
加载失败必须触发 fail-safe。
```

---

# 七、EvidenceGraphService 改造

## 7.1 Phase 1 状态

EvidenceGraphService 可能直接依赖 StaticRuleProvider 读取检查建议。

## 7.2 Phase 2 改造

改为依赖：

```text
TestRecommendationProvider
EvidenceAssetProvider
```

Phase 2 中 EvidenceAssetProvider 可以是空实现或静态 ref 实现。

## 7.3 输出要求

EvidenceGraphItem 可以继续保持原有字段，但 recommendedTests 和 evidenceRefs 应能追溯资产来源。

```text
recommendedTests
sourceAssetIds
evidenceRefs
```

如果不改 EvidenceGraphItem 结构，至少要在 RuntimeTrace 里记录来源资产。

---

# 八、DecisionBoundaryService 改造

## 8.1 Phase 1 状态

DecisionBoundaryService 读取 CapabilityProfileProvider。

## 8.2 Phase 2 改造

将 CapabilityProfileProvider 标准化为 Phase 2 Provider 接口，并使其返回带 metadata 的 CapabilityProfileAsset。

## 8.3 约束

```text
1. CapabilityProfile 是安全关键资产。
2. 加载失败必须 fail-safe。
3. 患者端默认最小权限。
4. 任何新增资产字段不得绕过 DecisionBoundary 出现在患者端响应中。
```

---

# 九、ExperienceContextService 改造

## 9.1 Phase 1 状态

ExperienceContextService 为空实现或 mock 实现。

## 9.2 Phase 2 改造

改为依赖：

```text
ClinicalExperienceProvider
```

Provider 返回：

```text
ExperienceUnitAsset
```

写入：

```text
ExperienceContext.matchedExperienceUnits
ExperienceContext.experienceAlerts
RuntimeTrace.experienceUsed
```

## 9.3 约束

```text
1. Phase 2 只允许 MOCK_VERIFIED / HUMAN_VERIFIED 的 experience units。
2. 经验只能影响提醒、追问建议、注意事项。
3. 经验不能直接决定诊断。
4. 经验不能绕过 SafetyGate 或 DecisionBoundary。
```

---

# 十、RuntimeService 改造

## 10.1 StartRuntimeRequest 扩展

Phase 2 可以为 start API 增加：

```text
asset_context
  package_id
  version
```

如果不传：

```text
使用默认 active package。
```

## 10.2 ContinueRuntimeRequest 约束

continueRuntime 不应随意切换资产包。默认策略：

```text
continue 轮次沿用 RuntimeState.assetPackageId / assetPackageVersion。
```

如后续支持切换资产包，必须记录到 RuntimeTrace，并验证不会破坏当前 Runtime 状态。

Phase 2 不建议支持中途切换资产包。

## 10.3 Trace 改造

RuntimeService 构建 RuntimeTrace 时，需要记录：

```text
asset_package_id
asset_package_version
asset_ids_used
asset_versions_used
```

至少写入：

```text
trace.knowledgeUsed
trace.outputSummary
```

---

# 十一、ApiResponseMapper 改造

## 11.1 患者端

患者端允许看到：

```text
symptom_group
source_assets_count
next_action.type
next_action.content
next_action.purpose
next_action.priority
```

患者端禁止看到：

```text
common_diagnoses
must_not_miss
target_diagnosis
full evidence graph
asset internal details
asset_id that reveals diagnosis
```

## 11.2 医生端 / Debug

医生端可以看到：

```text
DDx Board
EvidenceGraph
ClinicianReport
asset package/version
source asset ids
```

Debug 模式可看到更完整的 asset metadata。

---

# 十二、迁移顺序

推荐顺序：

```text
1. 新增 AssetMetadata / AssetPackageManifest / AssetQueryContext。
2. 新增 Provider 接口，不改 Runtime。
3. 新增 YAML Provider 实现，并保持 Phase 1 测试通过。
4. 新增 phase2-default 资产包，内容从 Phase 1 assets 迁移。
5. KnowledgeContextService 接入 Provider。
6. EvidenceGraphService 去掉 StaticRuleProvider 直接依赖。
7. CapabilityProfileProvider 标准化。
8. ExperienceContextService 接入 ClinicalExperienceProvider。
9. RuntimeTrace 记录资产包和资产版本。
10. 增加 assets-used API。
11. 跑 Phase 1 全量回归测试和 Phase 2 新增测试。
```

不得一次性重构所有模块。

---

# 十三、兼容策略

Phase 2 初期可以保留 `StaticRuleProvider`，但只能作为兼容 Adapter 或 YAML Provider 内部实现，不应继续被 Runtime 核心模块直接依赖。

过渡期允许：

```text
YamlMedicalKnowledgeProvider → 内部复用 StaticRuleProvider 的解析逻辑
```

但最终依赖方向必须是：

```text
Runtime 模块 → Provider 接口 → YAML Provider / Repository
```

不能是：

```text
Runtime 模块 → StaticRuleProvider → YAML
```

---

# 十四、验收标准

```text
1. KnowledgeContextService 不再直接依赖 StaticRuleProvider。
2. EvidenceGraphService 不再直接依赖 StaticRuleProvider。
3. Runtime 每轮能确定 asset package。
4. RuntimeTrace 能记录资产包和资产版本。
5. 替代资产包可以在不改 Runtime 核心代码的情况下运行。
6. 错误资产包能触发 fail-safe。
7. 患者端不泄露资产内部诊断信息。
8. Phase 1 全部测试继续通过。
```

---

# 十五、非目标

```text
不做完整资产后台。
不做资产在线编辑。
不做真实审核工作流。
不做 RAG 检索。
不做知识图谱引擎。
不做自动经验学习。
不做前端管理台。
不引入复杂微服务。
```
