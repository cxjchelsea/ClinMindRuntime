# Phase 3 开发任务清单

> 本文档用于跟踪 ClinMindRuntime Phase 3 训练与评估闭环的实现进度。  
> Phase 3 的目标是病例集考试、评估指标、EvaluationResult 和 CapabilityProfile 更新建议。  
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
2. 每次实现前，先确认任务属于 Phase3-P0-A 到 Phase3-P0-G 的哪一项。
3. 每次实现后，必须更新任务状态。
4. 不允许把 Phase 4–5 的经验进化、审核流、平台后台提前塞进 Phase 3。
5. 不允许训练基础大模型。
6. 不允许自动上线 CapabilityProfile。
7. 标记完成前，必须有对应代码和 JUnit 测试，或在备注中说明原因。
8. Phase 3 修改不能破坏 Phase 1 / Phase 2 测试。
```

---

# 二、Phase3-P0-A：Evaluation 数据结构

目标：建立评估闭环所需基础数据结构，不改 Runtime 主流程。

## 2.1 病例结构

- [x] 创建 `evaluation/EvaluationCase.java`
- [x] 创建 `evaluation/EvaluationCaseSet.java`
- [x] 创建 `evaluation/EvaluationInputTurn.java`
- [x] 创建 `evaluation/ExpectedOutcome.java`
- [x] 创建 `evaluation/CaseSeverity.java`

## 2.2 评估运行结构

- [x] 创建 `evaluation/EvaluationRunConfig.java`
- [x] 创建 `evaluation/EvaluationRun.java`
- [x] 创建 `evaluation/EvaluationRunStatus.java`
- [x] 创建 `evaluation/RuntimeCaseExecution.java`

## 2.3 结果结构

- [x] 创建 `evaluation/EvaluationItemResult.java`
- [x] 创建 `evaluation/EvaluationResult.java`
- [x] 创建 `evaluation/ScoreBreakdown.java`
- [x] 创建 `evaluation/MetricResult.java`
- [x] 创建 `evaluation/MetricSeverity.java`
- [x] 创建 `evaluation/SafetyViolation.java`
- [x] 创建 `evaluation/SafetyViolationType.java`
- [x] 创建 `evaluation/RegressionFinding.java`

## 2.4 测试

- [x] 编写 Evaluation 数据结构单元测试
- [x] 编写 JSON 序列化 / 反序列化测试

---

# 三、Phase3-P0-B：病例集 Repository 与 YAML 病例格式

目标：从 YAML 加载标准病例集。

## 3.1 资源文件

- [x] 创建 `src/main/resources/evaluation/case-sets/phase3-default/manifest.yml`
- [x] 创建 `chest-pain-cases.yml`
- [x] 创建 `fever-cases.yml`
- [x] 创建 `wellness-regression-cases.yml`
- [x] 创建 `unsupported-regression-cases.yml`
- [x] 创建 `patient-boundary-cases.yml`
- [x] 创建 `trace-asset-cases.yml`

## 3.2 Repository

- [x] 创建 `evaluation/EvaluationCaseRepository.java`
- [x] 创建 `evaluation/yaml/YamlEvaluationCaseRepository.java`
- [x] 实现 `loadCaseSet(caseSetId)`
- [x] 实现 `loadCases(caseSetId)`
- [x] 支持按 symptomGroup 过滤
- [x] 支持按 tag 过滤
- [x] 病例格式错误时抛出明确异常

## 3.3 测试

- [x] 编写 YamlEvaluationCaseRepositoryTest
- [x] 测试未知 case_set
- [x] 测试格式错误病例
- [x] 测试病例过滤

---

# 四、Phase3-P0-C：EvaluationRunner 执行 Runtime

目标：让评估器通过 RuntimeService 运行病例，不绕过 Runtime。

## 4.1 Runner

- [x] 创建 `evaluation/EvaluationRunner.java`
- [x] 创建 `evaluation/RuntimeEvaluationRunner.java`
- [x] 创建 `evaluation/EvaluationRunStore.java`
- [x] 实现单轮病例 startRuntime
- [x] 实现多轮病例 continueRuntime
- [x] 收集 RuntimeState
- [x] 收集 RuntimeTrace
- [x] 捕获 Runtime 异常并转为 EvaluationItemResult
- [x] 支持 failFast=false 继续执行

## 4.2 测试

- [x] 编写 EvaluationRunnerTest
- [x] 编写 RuntimeEvaluationRunnerIntegrationTest
- [x] 测试 ERROR_SAFE_HALTED 可作为有效评估结果
- [x] 测试多轮病例不会重复 EntryAssessment

---

# 五、Phase3-P0-D：Scorer 评分器体系

目标：对 RuntimeCaseExecution 进行结构化评分。

## 5.1 Scorer 接口

- [x] 创建 `evaluation/scorer/EvaluationScorer.java`
- [x] 创建 `evaluation/scorer/ScorerContext.java`

## 5.2 评分器实现

- [x] EntryAssessmentScorer
- [x] SafetyGateScorer
- [x] PatientBoundaryScorer
- [x] DdxCoverageScorer
- [x] NextActionScorer
- [x] TraceCompletenessScorer
- [x] AssetVersionTraceScorer

## 5.3 测试

- [x] EntryAssessmentScorerTest
- [x] SafetyGateScorerTest
- [x] PatientBoundaryScorerTest
- [x] DdxCoverageScorerTest
- [x] NextActionScorerTest
- [x] TraceCompletenessScorerTest
- [x] AssetVersionTraceScorerTest

---

# 六、Phase3-P0-E：EvaluationResult 聚合与报告

目标：把多个病例结果聚合为一次评估报告。

## 6.1 聚合器

- [x] 创建 `evaluation/EvaluationResultAggregator.java`
- [x] 计算 totalCases / passedCases / failedCases
- [x] 计算 passRate / averageScore
- [x] 计算 safetyPassRate
- [x] 计算 boundaryPassRate
- [x] 计算 tracePassRate
- [x] 计算 assetTracePassRate
- [x] 生成 RegressionFinding

## 6.2 测试

- [x] EvaluationResultAggregatorTest
- [x] 测试 critical failure 会影响 passed
- [x] 测试 majorFindings 聚合

---

# 七、Phase3-P0-F：CapabilityProfile 更新建议

目标：根据 EvaluationResult 生成 CapabilityProfileUpdateProposal，不自动上线。

## 7.1 数据结构

- [x] 创建 `evaluation/capability/CapabilityEvaluationPolicy.java`
- [x] 创建 `evaluation/capability/CapabilityProfileUpdateProposal.java`
- [x] 创建 `evaluation/capability/ProposalStatus.java`

## 7.2 服务

- [x] 创建 `evaluation/capability/CapabilityProfileProposalService.java`
- [x] 安全不达标禁止升级
- [x] 患者端泄漏禁止升级
- [x] asset trace 不达标禁止升级
- [x] 指标达标生成升级建议
- [x] 指标一般生成保持建议
- [x] 严重失败生成降级建议
- [x] 不写入正式 assets/packages/phase2-default

## 7.3 测试

- [x] CapabilityProfileProposalServiceTest
- [x] 测试升级 / 保持 / 降级 / 阻塞四类情况

---

# 八、Phase3-P0-G：Evaluation API 与验收测试

目标：提供内部 debug API 和完整回归验收。

## 8.1 API

- [x] 创建 `api/EvaluationController.java`
- [x] 实现 `POST /api/v1/debug/evaluations/runs`
- [x] 实现 `GET /api/v1/debug/evaluations/runs/{run_id}`
- [x] 实现 `GET /api/v1/debug/evaluations/runs/{run_id}/result`
- [x] 实现 `GET /api/v1/debug/evaluations/runs/{run_id}/items/{case_id}`
- [x] 实现 `POST /api/v1/debug/evaluations/runs/{run_id}/capability-profile-proposal`

## 8.2 测试

- [x] EvaluationControllerTest
- [x] EvaluationEndToEndIntegrationTest
- [x] Phase1RegressionTest 继续通过
- [x] Phase2 Asset Provider 回归继续通过
- [x] PatientOutputAssetIsolationTest 继续通过
- [x] RuntimeAssetVersionMismatchTest 继续通过

## 8.3 人工验收

- [x] 创建 `docs/Phase3_人工测试API结果.md`
- [x] 记录默认病例集评估运行结果
- [x] 记录失败病例 MetricResult
- [x] 记录 CapabilityProfileUpdateProposal

---

# 九、问题记录

| 编号 | 问题 | 影响模块 | 状态 | 处理结论 |
|---|---|---|---|---|
| Q1 | 暂无 | - | - | - |

---

# 十、变更记录

| 日期 | 变更 | 说明 |
|---|---|---|
| 2026-06-26 | 创建 Phase 3 任务清单 | 用于约束训练与评估闭环 MVP 实现 |
| 2026-06-26 | 完成 Phase3-P0-A | Evaluation 基础数据结构（17 类）+ 单元测试与 JSON 序列化测试；150 项测试全绿 |
| 2026-06-26 | 完成 Phase3-P0-B | phase3-default 病例集 YAML + YamlEvaluationCaseRepository；13 个标准病例；157 项测试全绿 |
| 2026-06-26 | 完成 Phase3-P0-C | RuntimeEvaluationRunner 经 RuntimeService 执行病例；EvaluationRunStore；164 项测试全绿 |
| 2026-06-26 | 完成 Phase3-P0-D | 7 个 EvaluationScorer + EvaluationItemScoringService；176 项测试全绿 |
| 2026-06-26 | 完成 Phase3-P0-E | EvaluationResultAggregator 聚合 itemResults → EvaluationResult；180 项测试全绿 |
| 2026-06-26 | 完成 Phase3-P0-F | CapabilityProfileProposalService 生成升级/保持/降级/阻塞建议；188 项测试全绿 |
| 2026-06-26 | 完成 Phase3-P0-G | Evaluation debug API + Controller/E2E 测试；193 项测试全绿 |
| 2026-06-26 | Phase3 人工 API 验收 | `docs/Phase3_人工测试API结果.md`；8 条 debug Evaluation API 抽测通过 |
| 2026-06-26 | Phase3-P0 冻结清理 | 文档同步、README、case_set_version 校验、MetricResult.applicable、API 错误码修正 |
