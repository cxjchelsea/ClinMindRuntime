# 测试与 CI 总方案

> 本文档定义 ClinMindRuntime 的测试分层、回归基线、验收方式和 CI 演进路线。  
> 本项目的测试目标不是只证明接口能跑通，而是证明 Runtime 主控、安全边界、资产版本、Evaluation 评分和未来 Provider 接入都不会破坏核心能力。

---

# 一、测试目标

ClinMindRuntime 的测试体系要验证：

```text
1. Runtime 主流程能稳定执行。
2. SafetyGate 和 DecisionBoundary 不被绕过。
3. 患者端输出不会泄露医生端信息。
4. 资产包版本和 Provider 使用可追踪。
5. 错误资产包能够 fail-safe。
6. Evaluation 能用病例集评估 Runtime。
7. 后续 Python / RAG / Model Provider 接入后仍能回归。
```

---

# 二、测试分层

## 2.1 Unit Test

验证单个类或单个模块：

```text
EntryAssessmentService
CaseFrameService
SafetyGateService
DecisionBoundaryService
AssetPackageRepository
Provider implementation
Evaluation model
EvaluationScorer
```

## 2.2 Integration Test

验证模块串联：

```text
RuntimeService start / continue
Runtime pipeline
Provider asset loading
Trace collection
EvaluationRunner 调用 RuntimeService
```

## 2.3 Regression Test

验证旧能力不坏：

```text
Phase 1 Runtime regression
Phase 2 Asset / Provider regression
Patient output boundary regression
Asset version mismatch regression
Broken package fail-safe regression
```

## 2.4 API Test

验证接口边界：

```text
patient-facing API
clinician-facing API
debug/internal API
asset debug API
evaluation debug API
```

## 2.5 Evaluation Test

验证 Phase 3 病例集考试：

```text
EvaluationCaseSet loading
RuntimeEvaluationRunner
SafetyGateScorer
PatientBoundaryScorer
TraceCompletenessScorer
AssetVersionTraceScorer
EvaluationResultAggregator
CapabilityProfileProposalService
```

---

# 三、核心回归基线

每次重要改动后必须保护以下基线：

```text
1. 高风险病例不会输出低风险安抚。
2. 患者端不会看到 DDx Board、EvidenceGraph、must_not_miss、asset internals。
3. RuntimeTrace 能记录关键模块执行。
4. RuntimeTrace 能记录资产包和资产版本。
5. 资产版本不匹配会进入 fail-safe。
6. 坏资产包加载失败不能 fail-open。
7. EvaluationRunner 不绕过 RuntimeService。
8. Scorer 不能修改 RuntimeState。
```

---

# 四、Phase 测试策略

## Phase 1

重点：

```text
RuntimeState
RuntimeTrace
SafetyGate
DecisionBoundary
PatientOutput isolation
```

## Phase 2

重点：

```text
AssetMetadata
AssetPackageRepository
Provider interface
YAML provider
Runtime asset trace
asset version mismatch
broken package fail-safe
```

## Phase 3

重点：

```text
EvaluationCase model
YAML case repository
RuntimeEvaluationRunner
Scorer
EvaluationResult
CapabilityProfileUpdateProposal
Evaluation debug API
```

## Phase 4/5 后续

重点：

```text
PostgreSQL repository migration
Python Provider fallback
RAG evidence correctness
Model Provider version trace
AuditLog
RBAC
```

---

# 五、测试命名规范

推荐命名：

```text
<ModuleName>Test.java
<ModuleName>IntegrationTest.java
<ScenarioName>RegressionTest.java
```

示例：

```text
PatientOutputAssetIsolationTest
RuntimeAssetVersionMismatchTest
BrokenAssetPackageFailSafeTest
RuntimeEvaluationRunnerIntegrationTest
SafetyGateScorerTest
```

---

# 六、CI 策略

## 6.1 当前阶段

当前可使用最小 CI：

```text
mvn test
```

CI 至少应检查：

```text
编译通过
全部 JUnit 测试通过
无明显格式错误
```

## 6.2 Phase 3 后

增加：

```text
Evaluation tests
Phase1/2 regression tests
asset package loading tests
case set YAML validation tests
```

## 6.3 Phase 5 后

增加：

```text
Docker Compose smoke test
PostgreSQL integration test
Redis integration test
Frontend build test
Python Provider contract test
```

---

# 七、手工验收策略

除了 JUnit，应保留人工 API 验收：

```text
1. 创建 Runtime。
2. 输入高风险症状。
3. 查看患者端输出是否安全。
4. 查看医生端输出是否包含证据。
5. 查看 RuntimeTrace。
6. 查看 assets-used debug API。
7. 执行 Evaluation run。
8. 查看 EvaluationResult。
```

Phase 3 后建议维护：

```text
docs/Phase3_人工测试API结果.md
```

---

# 八、Provider Contract Test

未来 Provider 接入后必须测试：

```text
Provider timeout
Provider invalid response
Provider version missing
Provider returns unsafe content
Provider fallback
Provider trace recorded
```

适用于：

```text
Python AI Provider
RAG Provider
GraphRAG Provider
Model Provider
MCP Adapter
```

---

# 九、测试数据管理

测试数据来源：

```text
synthetic cases
YAML evaluation cases
mock asset packages
broken asset packages
high-risk regression cases
patient-boundary regression cases
```

原则：

```text
不使用真实可识别患者数据。
高风险病例必须长期保留为回归样例。
每次新增 bug fix 应沉淀一个 regression case。
```

---

# 十、最终结论

ClinMindRuntime 的测试核心不是覆盖率数字，而是保护架构边界。

必须长期保护：

```text
Runtime 不被绕过。
SafetyGate 不被绕过。
DecisionBoundary 不被绕过。
患者端隔离不被破坏。
资产版本可追踪。
Evaluation 结果可信。
```
