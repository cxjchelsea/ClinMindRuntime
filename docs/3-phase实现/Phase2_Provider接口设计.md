# Phase 2 Provider 接口设计

> 本文档定义 Phase 2 的 Provider 抽象层。  
> 核心原则：Runtime Core 只能依赖 Provider 接口，不能直接依赖 YAML 文件、LangChain、Spring AI、Python 服务或任何具体框架实现。

---

# 一、Provider 设计目标

Phase 1 中多个模块仍直接或间接依赖 `StaticRuleProvider`。Phase 2 要把它拆成稳定接口：

```text
Runtime 模块
  ↓
Provider 接口
  ↓
Yaml Provider / Mock Provider / Future DB Provider / Future RAG Provider
```

这样才能保证：

```text
1. Runtime 主控权仍在 ClinMindRuntime。
2. 框架只能作为 Provider / Adapter。
3. 资产来源可以替换，但 Runtime 链路不需要改。
4. 资产使用可以被 Trace 记录。
5. 错误 Provider 必须 fail-closed。
```

---

# 二、接口总览

```text
MedicalKnowledgeProvider
RedFlagRuleProvider
TestRecommendationProvider
CapabilityProfileProvider
ClinicalExperienceProvider
EvidenceAssetProvider
AssetPackageRepository
```

---

# 三、MedicalKnowledgeProvider

## 3.1 职责

根据症状群返回基础医学知识资产，包括 common diagnoses、must-not-miss、required questions、recommended tests 等。

## 3.2 接口建议

```java
public interface MedicalKnowledgeProvider {
    MedicalKnowledgeAsset loadMedicalKnowledge(String symptomGroup, AssetQueryContext context);
}
```

## 3.3 返回对象

```text
MedicalKnowledgeAsset
- metadata: AssetMetadata
- symptomGroup: String
- commonDiagnoses: List<DiagnosisRef>
- mustNotMiss: List<DiagnosisRef>
- requiredQuestions: List<String>
- recommendedTests: List<String>
```

## 3.4 Phase 2 实现

```text
YamlMedicalKnowledgeProvider
```

---

# 四、RedFlagRuleProvider

## 4.1 职责

为 SafetyGate 提供危险信号规则。

## 4.2 接口建议

```java
public interface RedFlagRuleProvider {
    List<RedFlagRuleAsset> loadRedFlagRules(String symptomGroup, AssetQueryContext context);
}
```

## 4.3 约束

```text
1. 规则资产缺失或解析失败必须抛出 AssetLoadException。
2. SafetyGate 捕获 AssetLoadException 后必须 fail-safe。
3. 不允许返回空列表来掩盖规则加载失败。
```

---

# 五、TestRecommendationProvider

## 5.1 职责

为 EvidenceGraph / QuestionTestPolicy 提供检查建议规则。

## 5.2 接口建议

```java
public interface TestRecommendationProvider {
    List<TestRecommendationAsset> loadTestRecommendations(String symptomGroup, AssetQueryContext context);
}
```

## 5.3 约束

```text
检查建议不能直接输出给患者端。
患者端是否能看到检查建议必须经过 DecisionBoundary 和 PatientOutputService。
```

---

# 六、CapabilityProfileProvider

## 6.1 职责

为 DecisionBoundary 提供能力档案。

## 6.2 接口建议

```java
public interface CapabilityProfileProvider {
    CapabilityProfileAsset loadCapabilityProfile(String symptomGroup, AssetQueryContext context);
}
```

## 6.3 约束

```text
1. CapabilityProfile 加载失败时，DecisionBoundary 必须 fail-safe。
2. 患者端默认不允许诊断标签。
3. 医生端是否可见 DDx / EvidenceGraph 由 CapabilityProfile 和 RuntimeMode 决定。
```

---

# 七、ClinicalExperienceProvider

## 7.1 职责

为 ExperienceContextService 提供已验证或 mock 的经验单元。

## 7.2 接口建议

```java
public interface ClinicalExperienceProvider {
    List<ExperienceUnitAsset> retrieveExperienceUnits(
        CaseFrame caseFrame,
        KnowledgeContext knowledgeContext,
        AssetQueryContext context
    );
}
```

## 7.3 Phase 2 实现边界

```text
Phase 2 可以返回 verified/mock experience units。
不得实现自动经验学习。
不得把 RuntimeTrace 自动转为经验。
不得让经验绕过 SafetyGate / DecisionBoundary。
```

---

# 八、EvidenceAssetProvider

## 8.1 职责

预留 RAG Evidence / KG-lite 资产入口。

## 8.2 接口建议

```java
public interface EvidenceAssetProvider {
    List<EvidenceAssetRef> retrieveEvidenceRefs(
        String symptomGroup,
        List<DDxCandidate> candidates,
        AssetQueryContext context
    );
}
```

## 8.3 Phase 2 限制

```text
Phase 2 只预留接口或返回静态 evidence refs。
不实现完整 RAG 检索。
不引入向量数据库。
不引入 KG 引擎。
```

---

# 九、AssetPackageRepository

## 9.1 职责

读取资产包 manifest，并解析资产包内各类 YAML / JSON 文件。

## 9.2 接口建议

```java
public interface AssetPackageRepository {
    AssetPackageManifest loadManifest(String packageId);

    List<AssetPackageManifest> listPackages();

    AssetResource loadResource(String packageId, String relativePath);
}
```

## 9.3 Phase 2 实现

```text
YamlAssetPackageRepository
```

---

# 十、AssetQueryContext

Provider 查询上下文，用于指定当前使用哪个资产包、版本和运行模式。

```text
AssetQueryContext
- packageId: String
- version: String
- symptomGroup: String
- runtimeId: String
- runtimeMode: RuntimeMode
- fallbackAllowed: boolean
```

Phase 2 默认可以使用 `phase2-default` 资产包。

---

# 十一、错误处理

Provider 错误分为：

```text
ASSET_NOT_FOUND
ASSET_FORMAT_INVALID
ASSET_VERSION_MISMATCH
ASSET_STATUS_DISABLED
ASSET_LOAD_FAILED
```

所有安全关键资产加载失败都必须 fail-closed。

安全关键资产包括：

```text
red flag rules
capability profile
symptom group must-not-miss rules
```

非安全关键资产可以降级：

```text
experience units
evidence refs
optional required questions
```

---

# 十二、框架 Adapter 边界

允许后续增加：

```text
SpringAiEvidenceProviderAdapter
LangChain4jEvidenceProviderAdapter
PythonRagProviderAdapter
DatabaseAssetProvider
```

但所有 Adapter 必须满足：

```text
1. 返回结构化资产对象。
2. 不直接修改 RuntimeState。
3. 不直接生成患者端输出。
4. 不决定 SafetyGate 最终结果。
5. 不绕过 DecisionBoundary。
```

---

# 十三、Phase 2 接入点

| Runtime 模块 | Phase 1 依赖 | Phase 2 依赖 |
|---|---|---|
| KnowledgeContextService | StaticRuleProvider | MedicalKnowledgeProvider + RedFlagRuleProvider + TestRecommendationProvider |
| SafetyGateService | StaticRuleProvider / KnowledgeContext.redFlags | RedFlagRuleProvider 或 KnowledgeContext 中带 metadata 的规则资产 |
| EvidenceGraphService | StaticRuleProvider | TestRecommendationProvider + EvidenceAssetProvider |
| DecisionBoundaryService | CapabilityProfileProvider 当前实现 | CapabilityProfileProvider 接口标准化 |
| ExperienceContextService | empty/mock | ClinicalExperienceProvider |

---

# 十四、完成标准

```text
1. 所有 Provider 接口创建完成。
2. YAML 实现全部位于 provider/yaml 包下。
3. Runtime 核心模块不再直接 new 或注入 StaticRuleProvider 具体类。
4. Provider 返回对象包含 AssetMetadata。
5. Provider 异常能触发 fail-safe 或安全降级。
6. 替换 Provider 后 Runtime 流程测试仍通过。
```
