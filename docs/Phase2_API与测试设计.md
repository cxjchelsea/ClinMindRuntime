# Phase 2 API 与测试设计

> 本文档定义 Phase 2 共享能力资产原型的只读调试 API、测试范围和验收用例。  
> Phase 2 不做正式前端、不做完整资产管理后台，只提供必要的后端资产查询与 Runtime 资产使用追踪能力。

---

# 一、API 设计原则

```text
1. API 只做资产调试和验收，不做完整后台。
2. API 默认只读，不提供线上编辑能力。
3. 资产变更仍通过 YAML / Git 管理。
4. 患者端 API 不能暴露内部诊断资产细节。
5. Runtime 资产使用记录必须可追踪。
```

---

# 二、资产包 API

## 2.1 查询资产包列表

```text
GET /api/v1/assets/packages
```

响应示例：

```json
{
  "success": true,
  "data": {
    "packages": [
      {
        "package_id": "phase2-default",
        "version": "0.2.0",
        "status": "active",
        "display_name": "Phase 2 Default Asset Package",
        "supported_symptom_groups": ["chest_pain", "fever"],
        "default_package": true
      }
    ]
  },
  "error": null,
  "trace_id": null
}
```

## 2.2 查询资产包详情

```text
GET /api/v1/assets/packages/{package_id}
```

响应应包含 manifest 基础字段，不需要返回所有内部规则详情。

## 2.3 查询症状群资产摘要

```text
GET /api/v1/assets/packages/{package_id}/symptom-groups/{symptom_group}
```

返回：

```text
asset metadata
symptom_group
common diagnosis count
must-not-miss count
required question count
recommended test count
source asset ids
```

患者端不能调用该调试 API。Phase 2 可以暂不做正式权限，但命名上应明确这是 debug / internal API。

---

# 三、Runtime 资产使用 API

## 3.1 查询 Runtime 使用过的资产

```text
GET /api/v1/runtime/{runtime_id}/assets-used
```

响应示例：

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "package_id": "phase2-default",
    "assets": [
      {
        "asset_id": "asset_symptom_chest_pain_v1",
        "asset_type": "symptom_group",
        "version": "0.2.0",
        "module_name": "KnowledgeContext"
      }
    ]
  },
  "error": null,
  "trace_id": null
}
```

---

# 四、请求上下文

Phase 2 可以在 start API 中支持可选 `asset_context`：

```json
{
  "session_id": "s_001",
  "mode": "patient_facing",
  "asset_context": {
    "package_id": "phase2-default",
    "version": "0.2.0"
  },
  "input": {
    "text": "胸口闷，活动后更明显"
  }
}
```

如果不传：

```text
使用默认 active 资产包。
```

约束：

```text
1. asset_context 只能影响 Provider 查询。
2. 不能绕过 SafetyGate。
3. 不能绕过 DecisionBoundary。
4. 不存在或 disabled 的 package 必须返回错误或 fail-safe。
```

---

# 五、错误码

新增错误码：

| 错误码 | HTTP 状态码 | 含义 | 处理 |
|---|---:|---|---|
| ASSET_PACKAGE_NOT_FOUND | 404 | 资产包不存在 | 返回错误 |
| ASSET_PACKAGE_DISABLED | 400 | 资产包不可用 | 返回错误 |
| ASSET_NOT_FOUND | 404 | 资产不存在 | 返回错误或 fail-safe |
| ASSET_FORMAT_INVALID | 500 | 资产格式错误 | 安全关键资产 fail-safe |
| ASSET_VERSION_MISMATCH | 409 | 资产版本不匹配 | 返回冲突 |
| ASSET_LOAD_FAILED | 500 | 资产加载失败 | fail-safe 或降级 |

---

# 六、测试分层

## 6.1 单元测试

```text
AssetMetadataTest
AssetPackageManifestTest
YamlAssetPackageRepositoryTest
YamlMedicalKnowledgeProviderTest
YamlRedFlagRuleProviderTest
YamlTestRecommendationProviderTest
YamlCapabilityProfileProviderTest
YamlClinicalExperienceProviderTest
```

## 6.2 Provider 测试

```text
默认资产包能加载
替代资产包能加载
disabled 资产不能被 Runtime 使用
riskCritical 资产缺失时抛出 AssetLoadException
非安全关键经验资产缺失时允许降级为空
```

## 6.3 Runtime 集成测试

```text
RuntimeWithDefaultAssetPackageTest
RuntimeWithAlternateAssetPackageTest
RuntimeAssetTraceIntegrationTest
RuntimeFailClosedAssetIntegrationTest
PatientOutputAssetIsolationTest
```

## 6.4 回归测试

Phase 2 每次修改 Provider / Asset 体系后，必须跑 Phase 1 的全部测试。

---

# 七、关键验收用例

## 7.1 默认资产包

```text
输入：胸闷病例
资产包：phase2-default
预期：Runtime 能正常完成 KnowledgeContext、SafetyGate、DDx、EvidenceGraph、DecisionBoundary
```

## 7.2 替代资产包

```text
输入：同一胸闷病例
资产包：phase2-alt
预期：KnowledgeContext 使用 phase2-alt 资产，Runtime 核心链路不需要改代码
```

## 7.3 错误资产包

```text
输入：胸闷病例
资产包：broken-package，缺少 red-flag-rules.yml
预期：安全关键资产加载失败，Runtime 进入 ERROR_SAFE_HALTED 或返回安全错误
```

## 7.4 患者端隔离

```text
输入：patient_facing 胸闷病例
预期：响应中不出现 common_diagnoses / must_not_miss / target_diagnosis / full evidence graph
```

## 7.5 医生端可见

```text
输入：clinician_copilot 胸闷病例
预期：医生端能看到 DDx Board、EvidenceGraph、asset package/version
```

## 7.6 Trace 资产记录

```text
输入：任意临床病例
预期：/trace 或 /assets-used 能看到 asset_id、asset_type、version、module_name
```

## 7.7 ExperienceContext

```text
输入：命中 mock experience 的病例
预期：ExperienceContext 返回 verified/mock experience unit，并记录 source asset
```

---

# 八、不合格表现

```text
1. Runtime 模块仍直接依赖 StaticRuleProvider 具体类。
2. 替代资产包需要修改 Runtime 核心代码才能运行。
3. 安全关键规则加载失败后 SafetyGate 静默通过。
4. 患者端看到 must_not_miss 或 target_diagnosis。
5. RuntimeTrace 无法说明使用了哪个资产包。
6. Provider 返回自然语言长文本而不是结构化资产对象。
7. LangChain / Spring AI / Python 服务直接成为 Runtime 主控。
```

---

# 九、完成标准

```text
1. 新增资产包 API 可以查询默认资产包和替代资产包。
2. Runtime 可以通过 asset_context 指定资产包。
3. RuntimeTrace 或 assets-used API 可以查询资产使用记录。
4. 错误资产包测试能触发 fail-safe。
5. 患者端隔离测试继续通过。
6. Phase 1 全部测试继续通过。
7. Phase 2 新增测试全部通过。
```
