# AI Implementation Skill：ClinMindRuntime Phase 3

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前阶段进入 Phase 3：训练与评估闭环 MVP。  
> Phase 1-P0 Runtime MVP 和 Phase 2-P0 共享能力资产原型已基本完成，后续修改不得破坏 Runtime、安全门、输出边界、Provider 抽象、资产版本追踪和患者端隔离。

---

# 一、当前项目阶段

```text
Phase 3：训练与评估闭环 MVP
```

当前目标：

```text
建立“病例集考试 → Runtime 执行 → 指标评分 → EvaluationResult → CapabilityProfile 更新建议”的最小闭环。
```

Phase 3 要证明的工程闭环：

```text
EvaluationCaseSet
→ EvaluationRunner
→ RuntimeService.startRuntime / continueRuntime
→ RuntimeState / RuntimeTrace / API Response
→ Scorers
→ EvaluationItemResult
→ EvaluationResult
→ CapabilityProfileUpdateProposal
→ 人工确认后进入后续资产更新流程
```

重要说明：

```text
Phase 3 的“训练”不是训练基础大模型。
Phase 3 的训练含义是：用标准病例集和评估指标校准 Runtime 能力、资产质量和 CapabilityProfile 边界。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase3_开发任务清单.md
3. docs/Phase3_训练与评估闭环_实现规格.md
4. docs/Phase3_评估数据结构设计.md
5. docs/Phase3_病例集与考试流程设计.md
6. docs/Phase3_Runtime评估接入设计.md
7. docs/Phase3_CapabilityProfile更新机制设计.md
8. docs/Phase3_API与测试设计.md
9. docs/全局技术栈与架构选型.md
10. docs/AI前沿技术选型与接入规划.md
11. docs/Phase2_开发任务清单.md
12. docs/Phase2_共享能力资产原型_实现规格.md
13. docs/Phase2_Provider接口设计.md
14. docs/Phase2_资产数据结构与版本设计.md
15. docs/Phase2_Runtime接入改造设计.md
16. docs/Phase2_API与测试设计.md
17. docs/Phase1_技术栈与工程架构决策.md
18. docs/Phase1_Runtime_MVP_实现规格.md
19. docs/Phase1_数据结构与状态设计.md
20. docs/Phase1_模块接口设计.md
21. docs/Phase1_API与测试设计.md
22. docs/ClinMindRuntime阶段拆分路线图.md
23. docs/ClinMindRuntime完整系统设计.md
```

解释：

```text
Phase 3 文档优先指导当前新增能力。
全局技术栈文档约束前端、数据库、向量库、图数据库、Python Provider、部署和权限等长期技术选型。
AI 前沿技术文档约束 MCP、Agent SDK、LangGraph、GraphRAG、LLM-as-a-Judge、Skills、Agent Memory 等技术的接入阶段和边界。
Phase 2 文档仍然约束 Provider、Asset Package、资产版本和 debug API 边界。
Phase 1 文档仍然约束 Runtime Core、安全门、输出边界和患者端安全表达。
总设计文档描述完整愿景，但不能作为提前实现 Phase 4–5 能力的理由。
```

---

# 三、当前技术栈决策

```text
Runtime Core：Java 17+ / Spring Boot 3.x
Evaluation Core：Java / Spring Service
API：Spring Web debug/internal API
Validation：Jakarta Validation
Trace：沿用 RuntimeTrace
Data Model：Java enum / record / class
Case Set：YAML
Evaluation Storage Phase 3：in-memory store
Testing：JUnit 5 + AssertJ / Mockito + SpringBootTest
Python：不作为 Phase 3 主工程
AI 框架：不作为 Phase 3 主控
```

---

# 四、当前允许实现的内容

## 4.1 Evaluation 数据结构

```text
EvaluationCase
EvaluationCaseSet
EvaluationInputTurn
ExpectedOutcome
EvaluationRunConfig
EvaluationRun
EvaluationRunStatus
RuntimeCaseExecution
EvaluationItemResult
EvaluationResult
ScoreBreakdown
MetricResult
SafetyViolation
RegressionFinding
```

## 4.2 病例集 Repository

```text
EvaluationCaseRepository
YamlEvaluationCaseRepository
phase3-default case set YAML
```

## 4.3 EvaluationRunner

```text
EvaluationRunner
RuntimeEvaluationRunner
EvaluationRunStore
```

约束：

```text
EvaluationRunner 必须通过 RuntimeService.startRuntime / continueRuntime 执行病例。
不得绕过 RuntimeService 直接调用 EntryAssessmentService、SafetyGateService、EvidenceGraphService 等内部模块。
```

## 4.4 Scorer 体系

```text
EvaluationScorer
EntryAssessmentScorer
SafetyGateScorer
PatientBoundaryScorer
DdxCoverageScorer
NextActionScorer
TraceCompletenessScorer
AssetVersionTraceScorer
```

## 4.5 CapabilityProfile 更新建议

```text
CapabilityEvaluationPolicy
CapabilityProfileUpdateProposal
CapabilityProfileProposalService
```

只生成 proposal，不自动上线。

## 4.6 Evaluation debug API

```text
POST /api/v1/debug/evaluations/runs
GET /api/v1/debug/evaluations/runs/{run_id}
GET /api/v1/debug/evaluations/runs/{run_id}/result
GET /api/v1/debug/evaluations/runs/{run_id}/items/{case_id}
POST /api/v1/debug/evaluations/runs/{run_id}/capability-profile-proposal
```

---

# 五、当前禁止实现的内容

```text
1. 不训练基础大模型。
2. 不实现 SFT / RLHF / DPO / 蒸馏训练链路。
3. 不做真实临床有效性认证。
4. 不自动上线 CapabilityProfile。
5. 不自动修改 phase2-default 生产资产包。
6. 不做真实医生审核流。
7. 不做完整 Training Center 前端。
8. 不做复杂权限系统。
9. 不做经验自动进化。
10. 不把 RuntimeTrace 自动沉淀为 Clinical Experience Memory。
11. 不引入向量数据库。
12. 不做完整 RAG 评估平台。
13. 不让 LLM-as-judge 成为唯一评分依据。
14. 不绕过 RuntimeService 直接评估底层模块。
15. 不改变患者端输出边界。
16. 不绕过 SafetyGate 或 DecisionBoundary。
17. 不引入 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
18. 不提前接入 PostgreSQL / Redis / Neo4j / Milvus / 前端平台。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 3 训练与评估闭环 MVP。本次只保留接口或记录为 backlog，不实现真实能力。
```

---

# 六、实现顺序

AI 必须按以下顺序推进，不要跳到后续模块。

```text
Phase3-P0-A：Evaluation 数据结构
Phase3-P0-B：病例集 Repository 与 YAML 病例格式
Phase3-P0-C：EvaluationRunner 执行 Runtime
Phase3-P0-D：Scorer 评分器体系
Phase3-P0-E：EvaluationResult 聚合与报告
Phase3-P0-F：CapabilityProfile 更新建议
Phase3-P0-G：Evaluation API 与验收测试
```

每次实现任务只能覆盖一个小阶段，不能一次性生成整个系统。

---

# 七、任务清单同步规则

AI 每次实现、修改或测试 Phase 3 代码后，必须同步更新：

```text
docs/Phase3_开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase3_开发任务清单.md。
2. 确认当前任务属于 Phase3-P0-A 到 Phase3-P0-G 的哪一项。
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

## 8.1 Evaluation 不能替代 Runtime

```text
EvaluationRunner 是 Runtime 的调用者，不是 Runtime 的主控。
EvaluationRunner 不能直接修改 RuntimeState。
EvaluationRunner 不能直接生成患者端输出。
EvaluationRunner 不能绕过 SafetyGate / DecisionBoundary。
```

## 8.2 Scorer 只能评分

```text
Scorer 只能读取 RuntimeCaseExecution 并生成 MetricResult。
Scorer 不能修改 RuntimeState。
Scorer 不能修改资产包。
Scorer 不能直接决定 CapabilityProfile 上线。
```

## 8.3 CapabilityProfile 只能生成 Proposal

```text
CapabilityProfileProposalService 只能生成 CapabilityProfileUpdateProposal。
不得自动写入 src/main/resources/assets/packages/phase2-default。
不得自动改变 Runtime 当前使用的 CapabilityProfile。
```

## 8.4 安全指标优先

```text
高风险漏报、患者端诊断泄漏、低风险安抚误输出属于 CRITICAL。
出现 CRITICAL finding 时，不能因为平均分高而建议升级。
```

## 8.5 评估必须检查资产版本

```text
Evaluation 必须检查 RuntimeTrace 中的 asset_package_id、asset_package_version、asset_id@version。
缺失资产版本记录时，不能认为该症状群能力可升级。
```

## 8.6 前沿 AI 技术只能作为 Provider / Adapter / 辅助评估

```text
MCP / Agent SDK / LangGraph / LangChain / GraphRAG / Skills / LLM-as-a-Judge 不能替代 Runtime 主控。
如果需要引入，必须先查阅 docs/AI前沿技术选型与接入规划.md。
```

---

# 九、测试约束

每实现一个 Evaluation 模块，必须补充 JUnit 测试。

至少包含：

```text
EvaluationCaseTest
EvaluationCaseSetTest
ExpectedOutcomeTest
EvaluationRunConfigTest
EvaluationResultTest
YamlEvaluationCaseRepositoryTest
EvaluationRunnerTest
RuntimeEvaluationRunnerIntegrationTest
EntryAssessmentScorerTest
SafetyGateScorerTest
PatientBoundaryScorerTest
DdxCoverageScorerTest
NextActionScorerTest
TraceCompletenessScorerTest
AssetVersionTraceScorerTest
EvaluationResultAggregatorTest
CapabilityProfileProposalServiceTest
EvaluationControllerTest
EvaluationEndToEndIntegrationTest
```

每次 Phase 3 改动后，必须确保：

```text
Phase 1 回归测试通过。
Phase 2 Provider / Asset 回归测试通过。
PatientOutputAssetIsolationTest 通过。
RuntimeAssetVersionMismatchTest 通过。
broken-package fail-safe 测试通过。
```

---

# 十、AI 每次执行任务前的检查清单

```text
1. 当前任务属于 Phase 3 吗？
2. 当前任务属于 Phase3-P0-A 到 Phase3-P0-G 的哪一步？
3. 是否读取了 Phase3_开发任务清单？
4. 是否会误实现 Phase 4–5 的内容？
5. 是否保持 Evaluation 不绕过 Runtime？
6. 是否需要新增或更新 JUnit 测试？
7. 是否会影响患者端输出边界？
8. 是否会自动修改资产包或 CapabilityProfile？如果会，则禁止。
9. 是否违反 docs/全局技术栈与架构选型.md 的接入阶段？
10. 是否违反 docs/AI前沿技术选型与接入规划.md 的 AI 技术边界？
```

---

# 十一、当前最优下一步

当前最优实现任务是：

```text
Phase3-P0-A：Evaluation 数据结构
```

具体包括：

```text
1. 创建 evaluation 基础数据结构。
2. 创建 EvaluationCase / ExpectedOutcome / EvaluationRun / EvaluationResult 等模型。
3. 创建 SafetyViolation / MetricResult / ScoreBreakdown 等结果结构。
4. 编写基础单元测试。
5. 同步更新 docs/Phase3_开发任务清单.md。
```

不应在这个任务中实现：

```text
EvaluationRunner
Scorer
CapabilityProfile 更新
前端后台
真实审核流
经验进化
MCP
LangGraph
Agent SDK
LLM-as-a-Judge
Python AI Provider
RAG / GraphRAG
数据库持久化
```

---

# 十二、最终约束

```text
当前不是在实现完整训练平台。
当前是在实现 Phase 3 训练与评估闭环 MVP。
Phase 1 Runtime Core 和 Phase 2 Asset Provider 必须保持稳定。
Evaluation 只能评估 Runtime，并生成 EvaluationResult 与 CapabilityProfileUpdateProposal。
Phase 3 的目标是让能力边界有评估依据，而不是继续堆问诊功能。
全局技术栈和 AI 前沿技术可以指导后续方向，但不能成为提前实现 Phase 4/5 的理由。
```
