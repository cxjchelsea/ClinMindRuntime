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
真实模型训练、后训练、Python AI Provider、RAG / GraphRAG、MCP、前端平台、数据库持久化等能力均后置。
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
9. docs/架构文档缺口审查清单.md
10. docs/ClinMindRuntime技术实现总方案.md
11. docs/全局技术栈与架构选型.md
12. docs/AI前沿技术选型与接入规划.md
13. docs/模型训练与后训练规划.md
14. docs/医学知识库与RAG构建规划.md
15. docs/数据安全与合规边界规划.md
16. docs/测试与CI总方案.md
17. docs/数据库持久化设计.md
18. docs/平台前端与Console规划.md
19. docs/部署与运维规划.md
20. docs/Phase2_开发任务清单.md
21. docs/Phase2_共享能力资产原型_实现规格.md
22. docs/Phase2_Provider接口设计.md
23. docs/Phase2_资产数据结构与版本设计.md
24. docs/Phase2_Runtime接入改造设计.md
25. docs/Phase2_API与测试设计.md
26. docs/Phase1_技术栈与工程架构决策.md
27. docs/Phase1_Runtime_MVP_实现规格.md
28. docs/Phase1_数据结构与状态设计.md
29. docs/Phase1_模块接口设计.md
30. docs/Phase1_API与测试设计.md
31. docs/ClinMindRuntime阶段拆分路线图.md
32. docs/ClinMindRuntime完整系统设计.md
```

解释：

```text
Phase 3 文档优先指导当前新增能力。
架构文档缺口审查清单用于判断哪些专项文档已经充分、哪些仍需补齐，避免继续凭感觉判断文档体系是否完整。
技术实现总方案约束包结构、依赖方向、Runtime 主链路、Provider 边界、Evaluation 实现、存储演进、测试和部署演进。
全局技术栈文档约束前端、数据库、向量库、图数据库、Python Provider、部署和权限等长期技术选型。
AI 前沿技术文档约束 MCP、Agent SDK、LangGraph、GraphRAG、LLM-as-a-Judge、Skills、Agent Memory 等技术的接入阶段和边界。
模型训练与后训练规划约束意图识别、病例抽取、证据检索、安全表达、后训练、模型部署和 Model Provider 的长期路线。
医学知识库与 RAG 构建规划约束 KnowledgeContext、EvidenceAssetProvider、RAG、KG-lite、GraphRAG 与 EvidenceGraph 的边界。
数据安全与合规边界规划约束敏感数据、脱敏、debug/internal API、训练数据和审计边界。
测试与 CI 总方案约束测试分层、回归基线、CI 演进和验收方式。
数据库持久化设计约束 PostgreSQL、Redis、pgvector、Trace、Evaluation、Model Registry 等持久化方向。
平台前端与 Console 规划约束 Runtime Console、Asset Console、Evaluation Center、Model Registry、Audit Center 的后续范围。
部署与运维规划约束 Docker Compose、多服务部署、环境变量、健康检查、日志和监控的后续路线。
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
Model Training：不属于 Phase 3-P0
RAG / GraphRAG：不属于 Phase 3-P0
Database Persistence：不属于 Phase 3-P0
Frontend Console：不属于 Phase 3-P0
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
2. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
3. 不训练 intent / symptom_group / risk_signal 生产模型。
4. 不接 Python AI Provider。
5. 不接真实 RAG / GraphRAG。
6. 不接 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
7. 不做真实临床有效性认证。
8. 不自动上线 CapabilityProfile。
9. 不自动修改 phase2-default 生产资产包。
10. 不做真实医生审核流。
11. 不做完整 Training Center 前端。
12. 不做复杂权限系统。
13. 不做经验自动进化。
14. 不把 RuntimeTrace 自动沉淀为 Clinical Experience Memory。
15. 不引入向量数据库。
16. 不做完整 RAG 评估平台。
17. 不让 LLM-as-judge 成为唯一评分依据。
18. 不绕过 RuntimeService 直接评估底层模块。
19. 不改变患者端输出边界。
20. 不绕过 SafetyGate 或 DecisionBoundary。
21. 不提前接入 PostgreSQL / Redis / Neo4j / Milvus / 前端平台。
22. 不提前实现 Docker Compose / 复杂部署 / 运维体系。
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

```text
1. EvaluationRunner 是 Runtime 的调用者，不是 Runtime 的主控。
2. Scorer 只能评分，不能修改 RuntimeState、资产包或 CapabilityProfile。
3. CapabilityProfileProposalService 只能生成 proposal，不能自动上线。
4. 高风险漏报、患者端诊断泄漏、低风险安抚误输出属于 CRITICAL。
5. Evaluation 必须检查 RuntimeTrace 中的 asset_package_id、asset_package_version、asset_id@version。
6. MCP / Agent SDK / LangGraph / LangChain / GraphRAG / Skills / LLM-as-a-Judge 不能替代 Runtime 主控。
7. 模型训练 / 后训练只能提升可替换 Provider 的能力。
8. 代码包结构、依赖方向、API 分层、Provider 边界、Evaluation 架构、存储演进、测试策略必须遵守 docs/ClinMindRuntime技术实现总方案.md。
9. 不得笼统声称文档体系已经完整，文档完整性判断必须参考 docs/架构文档缺口审查清单.md。
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
9. 是否违反 docs/ClinMindRuntime技术实现总方案.md 的代码分层和依赖方向？
10. 是否违反 docs/全局技术栈与架构选型.md 的接入阶段？
11. 是否违反 docs/AI前沿技术选型与接入规划.md 的 AI 技术边界？
12. 是否违反 docs/模型训练与后训练规划.md 的训练接入边界？
13. 是否违反 docs/医学知识库与RAG构建规划.md 的 RAG / EvidenceGraph 边界？
14. 是否违反 docs/数据安全与合规边界规划.md 的数据安全边界？
15. 是否违反 docs/测试与CI总方案.md 的回归测试要求？
16. 是否需要更新 docs/架构文档缺口审查清单.md？
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
模型训练 / 后训练
数据库持久化
部署运维
```

---

# 十二、最终约束

```text
当前不是在实现完整训练平台。
当前是在实现 Phase 3 训练与评估闭环 MVP。
Phase 1 Runtime Core 和 Phase 2 Asset Provider 必须保持稳定。
Evaluation 只能评估 Runtime，并生成 EvaluationResult 与 CapabilityProfileUpdateProposal。
Phase 3 的目标是让能力边界有评估依据，而不是继续堆问诊功能。
新增六份专项规划用于指导后续 Phase 4/5，不是提前实现 Phase 4/5 的理由。
```
