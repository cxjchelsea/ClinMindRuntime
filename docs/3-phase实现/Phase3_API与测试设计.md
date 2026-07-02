# Phase 3 API 与测试设计

> 本文档定义 Phase 3 训练与评估闭环的最小 API、测试范围和验收标准。  
> Phase 3 的 API 是内部评估 API，不是面向患者端或医生端的产品 API。

---

# 一、API 设计原则

```text
1. Evaluation API 只用于内部评估和调试。
2. API 不直接修改生产资产包。
3. API 不自动上线 CapabilityProfile。
4. API 不暴露给 patient-facing client。
5. API 的结果必须可追溯到 case_set、asset_package、runtime_id、trace_id。
```

---

# 二、Evaluation API

## 2.1 创建评估运行

```text
POST /api/v1/debug/evaluations/runs
```

请求：

```json
{
  "case_set_id": "phase3-default",
  "case_set_version": "0.3.0",
  "asset_package_id": "phase2-default",
  "asset_package_version": "0.2.0",
  "symptom_group_filter": "chest_pain",
  "include_tags": ["high_risk"],
  "fail_fast": false
}
```

响应：

```json
{
  "success": true,
  "data": {
    "run_id": "eval_run_001",
    "status": "completed",
    "total_cases": 10,
    "passed_cases": 9,
    "failed_cases": 1,
    "pass_rate": 0.9
  },
  "error": null,
  "trace_id": null
}
```

Phase 3-P0 可以同步执行，不要求异步任务队列。

---

## 2.2 查询评估运行

```text
GET /api/v1/debug/evaluations/runs/{run_id}
```

返回：

```text
run_id
status
config
started_at
completed_at
summary
```

---

## 2.3 查询评估结果

```text
GET /api/v1/debug/evaluations/runs/{run_id}/result
```

返回：

```text
EvaluationResult
EvaluationItemResult 列表摘要
majorFindings
safetyViolations
```

---

## 2.4 查询单病例结果

```text
GET /api/v1/debug/evaluations/runs/{run_id}/items/{case_id}
```

用于查看：

```text
runtime_id
trace_ids
metric_results
safety_violations
notes
```

---

## 2.5 生成 CapabilityProfile 更新建议

```text
POST /api/v1/debug/evaluations/runs/{run_id}/capability-profile-proposal
```

响应：

```json
{
  "success": true,
  "data": {
    "proposal_id": "cap_prop_001",
    "run_id": "eval_run_001",
    "symptom_group": "chest_pain",
    "recommended_level": "L4_CLINICIAN_DDX_ALLOWED",
    "status": "needs_human_review",
    "reasons": ["safety and boundary passed", "ddx score above threshold"]
  },
  "error": null,
  "trace_id": null
}
```

约束：

```text
该 API 只生成 proposal，不写入 assets/packages/phase2-default。
```

---

# 三、Phase 3 不提供的 API

```text
不提供患者端评估 API。
不提供资产在线编辑 API。
不提供 CapabilityProfile 直接上线 API。
不提供 Training Center 前端 API。
不提供真实医生审核 API。
不提供 LLM-only 自动评测 API。
```

---

# 四、错误码

| 错误码 | HTTP 状态码 | 含义 |
|---|---:|---|
| EVALUATION_CASE_SET_NOT_FOUND | 404 | 病例集不存在 |
| EVALUATION_CASE_FORMAT_INVALID | 400 | 病例格式错误 |
| EVALUATION_RUN_NOT_FOUND | 404 | 评估运行不存在 |
| EVALUATION_RUNTIME_FAILED | 500 | Runtime 执行异常 |
| EVALUATION_ASSET_VERSION_MISMATCH | 400 | 资产版本不匹配 |
| CAPABILITY_PROPOSAL_BLOCKED | 400 | 安全条件不满足，禁止生成升级建议 |

---

# 五、测试分层

## 5.1 数据结构测试

```text
EvaluationCaseTest
EvaluationCaseSetTest
ExpectedOutcomeTest
EvaluationRunConfigTest
EvaluationResultTest
CapabilityProfileUpdateProposalTest
```

## 5.2 Repository 测试

```text
YamlEvaluationCaseRepositoryTest
```

覆盖：

```text
manifest 加载
case files 加载
未知病例集错误
病例字段缺失错误
按 tag / symptomGroup 过滤
```

## 5.3 Runner 测试

```text
EvaluationRunnerTest
RuntimeEvaluationRunnerIntegrationTest
```

覆盖：

```text
单轮病例执行
多轮病例执行
Runtime ERROR_SAFE_HALTED 作为可评估结果
Runtime 异常转 EvaluationItemResult.failed
failFast=false 时继续执行
```

## 5.4 Scorer 测试

```text
EntryAssessmentScorerTest
SafetyGateScorerTest
PatientBoundaryScorerTest
DdxCoverageScorerTest
NextActionScorerTest
TraceCompletenessScorerTest
AssetVersionTraceScorerTest
```

## 5.5 Capability Proposal 测试

```text
CapabilityProfileProposalServiceTest
```

覆盖：

```text
安全不达标禁止升级
患者端泄漏禁止升级
资产版本 Trace 缺失禁止升级
指标达标生成升级建议
指标一般生成保持建议
严重失败生成降级建议
```

## 5.6 API 测试

```text
EvaluationControllerTest
```

覆盖：

```text
创建评估运行
查询评估结果
查询单病例结果
生成 CapabilityProfile proposal
proposal 不修改正式资产包
```

---

# 六、回归测试要求

每次 Phase 3 修改后必须保证：

```text
Phase 1 Runtime 回归测试通过。
Phase 2 Asset Provider 回归测试通过。
患者端隔离测试通过。
broken-package fail-safe 测试通过。
asset version mismatch 测试通过。
```

---

# 七、人工验收清单

Phase 3-P0 人工验收至少包含：

```text
1. 创建默认评估运行。
2. 查询 EvaluationResult。
3. 查看一个失败病例的 MetricResult。
4. 确认高风险病例未通过时会生成 SafetyViolation。
5. 确认患者端泄露会被 PatientBoundaryScorer 捕获。
6. 确认 Trace 缺失 asset version 会被 AssetVersionTraceScorer 捕获。
7. 生成 CapabilityProfileUpdateProposal。
8. 确认 proposal 没有自动写入正式资产包。
```

---

# 八、完成标准

```text
1. Evaluation API 可以运行默认病例集。
2. EvaluationResult 可查询。
3. 单病例 MetricResult 可查询。
4. SafetyViolation 可查询。
5. CapabilityProfileUpdateProposal 可生成。
6. API 不自动修改资产包。
7. JUnit 覆盖核心数据结构、Runner、Scorer、Proposal 和 API。
8. Phase 1 / Phase 2 回归测试继续通过。
```
