# ClinMindRuntime 阶段拆分路线图

> 本文档是 `ClinMindRuntime完整系统设计.md` 的落地路线图。  
> 总设计文档定义完整愿景、能力域、四层架构、Runtime 主控、训练评估、模型后训练和平台治理；本文档负责把完整愿景拆分为多个可迭代实现阶段，并明确每个阶段的目标、范围、产出物、验证方式和完成标准。

---

# 一、路线图定位

ClinMindRuntime 的完整愿景是企业级智能诊断训练—运行—评估—后训练—经验进化平台。

这个愿景不能一次性完成，也不应该一开始就实现完整平台后台、真实 RAG、模型后训练、Python Provider、MCP、GraphRAG、经验进化、真实审核和企业级治理。

核心原则：

```text
先验证 Runtime 是否成立，
再补齐共享能力资产，
再建立训练与评估闭环，
再沉淀训练数据和经验候选，
再接入 Python AI Provider / RAG / GraphRAG / 模型能力，
最后走向平台化、后训练和企业级治理。
```

阶段拆分不是按照页面功能拆，而是按照系统能力成熟度拆。

---

# 二、命名说明

```text
Phase 0 / Phase 1 / Phase 2 ...
= 项目迭代阶段

PhaseX-P0 / PhaseX-P1 / PhaseX-P2
= 某一阶段内部的任务优先级
```

本文档中的 `Phase 1` 指第一阶段 Runtime MVP，不等同于总设计中所有 P0/P1/P2 任务优先级。

---

# 三、总体阶段划分

| 阶段 | 名称 | 核心目标 | 主要验证点 |
|---|---|---|---|
| Phase 0 | 项目骨架与设计冻结 | 固化总设计、建立工程骨架、准备最小资产 | 项目可启动，架构边界清楚 |
| Phase 1 | Runtime MVP | 跑通一次受控问诊 Runtime | 状态、风险、候选诊断、证据图、输出边界可运行 |
| Phase 2 | 共享能力资产原型 | 把静态规则升级为可管理、可替换、可版本化、可追踪的资产包和 Provider | Runtime 能通过 Provider 读取知识、规则、能力档案和经验原型 |
| Phase 3 | 训练与评估闭环 | 建立病例集考试、评估指标、EvaluationResult 和能力授权机制 | EvaluationResult 能驱动 CapabilityProfileUpdateProposal |
| Phase 3-P1 | 评估增强与训练数据候选 | 在评估闭环基础上沉淀训练样本候选和辅助评分能力 | LLM-as-a-Judge / Python 离线分析可作为辅助，不替代确定性评分 |
| Phase 4 | 经验进化与训练数据沉淀 | 从 RuntimeTrace、反馈和评估结果中沉淀经验候选与训练数据候选 | ExperienceCandidate / TrainingExampleCandidate 可被治理 |
| Phase 4-P1 | AI Provider / RAG / GraphRAG 原型 | 引入 Python Provider、embedding、RAG、GraphRAG、相似病例实验 | AI 能力以 Provider 形式接入，不替代 Runtime 主控 |
| Phase 5 | 平台化、模型服务化与企业级治理 | 建立管理后台、数据库、权限、审计、模型注册、资产发布和回滚 | 系统具备平台化运行、模型服务化和治理能力 |
| Phase 5-P1+ | 后置高级能力 | MCP Server、Skills 管理、Multi-Agent 审核、模型后训练管理、复杂部署 | 高级能力只在平台治理边界内试验 |

---

# 四、阶段拆分总原则

```text
1. 不先做大而全平台：Phase 1 先实现 Runtime 核心机制。
2. 不先追求知识覆盖全面性：Phase 1 只选少量症状群和病例验证机制。
3. 不把 LLM 回答当核心成果：核心是状态、风险、证据、边界和 Trace。
4. 不做未经治理的自动学习：经验进化和后训练必须后置。
5. 不为了显得“AI”而提前接入 MCP / LangGraph / RAG / Python Provider。
6. 每个阶段都必须有明确产物和完成标准。
7. 所有模型能力必须以 Provider 形式接入，并接受 Runtime 校验。
```

---

# 五、Phase 0：项目骨架与设计冻结

## 5.1 阶段目标

Phase 0 的目标是把总设计文档固化为项目起点，并建立最小工程骨架。

## 5.2 本阶段要做什么

```text
1. 固化 ClinMindRuntime 完整系统设计文档。
2. 新增阶段拆分路线图。
3. 确定 Phase 1 的开发范围。
4. 确定 Phase 1 技术栈与工程架构。
5. 建立基础工程目录。
6. 建立最小 README。
7. 准备少量测试病例和静态规则样例。
```

## 5.3 阶段产出物

```text
docs/ClinMindRuntime完整系统设计.md
docs/ClinMindRuntime阶段拆分路线图.md
docs/Phase1_技术栈与工程架构决策.md
docs/Phase1_Runtime_MVP_实现规格.md
docs/Phase1_数据结构与状态设计.md
docs/Phase1_模块接口设计.md
docs/Phase1_API与测试设计.md
docs/Phase1_开发任务清单.md
README.md
基础工程目录
少量测试病例
少量静态规则样例
```

---

# 六、Phase 1：Runtime MVP

## 6.1 阶段目标

Phase 1 的目标是跑通一次“受控诊断 Runtime”的最小闭环。

核心链路：

```text
用户输入
→ Runtime API
→ EntryAssessment
→ CaseFrame
→ Knowledge Context（静态规则）
→ Experience Context（空实现 / mock 实现）
→ SafetyGate
→ Differential Diagnosis Board
→ EvidenceGraph
→ Question / Test Policy
→ DecisionBoundary
→ Patient / Clinician Output
→ RuntimeTrace
```

## 6.2 技术路线

```text
Runtime Core：Java 17+ / Spring Boot 3.x
Trace：Spring AOP + @TraceStep + RuntimeTrace
Storage：In-memory RuntimeStore
Config Assets：YAML / JSON
Testing：JUnit 5
Python：后续可选 AI Provider，不作为 Phase 1 主工程
AI 框架：只能作为后续 Provider / Adapter，不能作为 Runtime 主控
```

## 6.3 本阶段要做什么

```text
Spring Boot 工程骨架
RuntimeController
RuntimeStatus
RuntimeState
RuntimeTrace
RuntimeStore
@TraceStep 与 RuntimeTraceAspect
EntryAssessmentService
CaseFrameService
KnowledgeContextService
ExperienceContextService 空实现 / mock 实现
SafetyGateService
DifferentialDiagnosisBoardService
EvidenceGraphService
QuestionTestPolicyService
DecisionBoundaryService
Patient-facing Output
Clinician-facing Output
FailurePolicyService
10–20 个测试病例
```

## 6.4 本阶段不做什么

```text
不做完整 RAG Evidence Library
不做完整 KG-lite
不做真实 Clinical Experience Memory
不做真实审核流程
不做随访结局接入
不做平台管理后台
不做自动经验学习
不训练模型
不接 Python Provider
不接 MCP / LangGraph / Agent SDK
不输出患者端确定诊断
不承诺临床有效性
不引入 Spring Cloud、Nacos、消息队列或复杂微服务
```

## 6.5 完成标准

```text
1. 用户输入症状后，可以创建 Runtime。
2. RuntimeState 能保存并更新病例状态。
3. Spring AOP Trace 能记录关键模块执行。
4. SafetyGate 能识别配置好的危险信号。
5. DDx Board 能生成候选诊断状态。
6. EvidenceGraph 能记录支持、反对、缺失证据。
7. Question / Test Policy 能决定下一步追问或检查建议。
8. DecisionBoundary 能区分患者端和医生端输出。
9. RuntimeTrace 能记录本轮判断依据。
10. 高风险病例不会输出低风险安抚性结论。
11. 至少 10–20 个测试病例可以跑通。
```

---

# 七、Phase 2：共享能力资产原型

## 7.1 阶段目标

把 Phase 1 中的静态配置升级为可管理、可版本化、可检索、可替换、可追踪的共享能力资产原型。

Phase 2 的重点是：

```text
Provider 接口
Asset Package Manifest
AssetMetadata / AssetVersion
YAML Provider 实现
Runtime 接入 Provider
RuntimeTrace 记录资产使用
```

## 7.2 本阶段要做什么

```text
Provider 读取机制
资产版本管理字段
Symptom Rotation Library 的资产包原型
Clinical Pathway 静态引用 / Provider 接口原型
Red Flag Rules 资产化
Test Recommendation Rules 资产化
RAG Evidence 静态引用 / Provider 接口原型
KG-lite 静态引用 / Provider 接口原型
Capability Profile 资产化
Clinical Experience Memory 的 mock / verified experience unit 原型
```

## 7.3 本阶段不做什么

```text
不做完整 RAG Evidence Library
不引入向量数据库
不做完整 KG-lite 引擎
不做真实 Clinical Experience Memory 后台
不做自动经验学习
不做医生审核流
不做前端资产管理后台
不做复杂数据库治理
不训练模型
不接 Python AI Provider
不接 MCP / LangGraph / Agent SDK
不引入 Spring Cloud、Nacos、消息队列或复杂微服务
```

## 7.4 完成标准

```text
1. Runtime 不再直接读取硬编码规则，而是通过 Provider 读取资产。
2. Knowledge Context 能聚合带 AssetMetadata 的多类知识资产。
3. Experience Context 能返回少量 mock / verified experience units。
4. Capability Profile 能被 DecisionBoundary 读取，并带 asset version。
5. RuntimeTrace 能记录 package_id、asset_id、asset_type、version、module_name。
6. 替代资产包可以在不修改 Runtime 核心代码的情况下运行。
7. 错误资产包能触发 fail-safe。
8. Phase 1 测试病例继续通过。
```

---

# 八、Phase 3：训练与评估闭环

## 8.1 阶段目标

建立“病例集考试—Runtime 执行—指标评分—EvaluationResult—CapabilityProfile 更新建议”的闭环。

Phase 3-P0 的训练含义不是训练模型，而是用标准病例集和评估指标训练 / 校准系统能力边界。

## 8.2 本阶段要做什么

```text
EvaluationCaseSet
EvaluationCaseRepository
EvaluationRunner
RuntimeCaseExecution
EvaluationScorer
SafetyGateScorer
PatientBoundaryScorer
DdxCoverageScorer
NextActionScorer
TraceCompletenessScorer
AssetVersionTraceScorer
EvaluationResult
RegressionFinding
CapabilityProfileUpdateProposal
Evaluation debug API
```

## 8.3 本阶段不做什么

```text
不训练模型
不接 LLM-as-a-Judge 真实调用
不接 Python evaluation platform
不接真实 RAG / GraphRAG
不接 MCP / LangGraph / Agent SDK
不自动上线 CapabilityProfile
不做前端 Training Center
不做数据库持久化
不做真实医生审核流程
```

## 8.4 完成标准

```text
1. 至少支持 chest_pain、fever、wellness、unsupported 四类病例。
2. EvaluationRunner 可以批量执行病例集。
3. 每个病例能产生 EvaluationItemResult。
4. 每次运行能产生 EvaluationResult。
5. EvaluationResult 至少包含 safety、boundary、ddx、next_action、trace、asset_version 指标。
6. PatientBoundaryScorer 能识别患者端诊断泄漏。
7. SafetyGateScorer 能识别高风险病例没有触发安全门的错误。
8. AssetVersionTraceScorer 能识别 trace 中缺失资产版本记录的错误。
9. CapabilityProfileProposalService 能根据阈值生成更新建议。
10. 不自动修改生产 CapabilityProfile。
11. Phase 1 / Phase 2 回归测试继续通过。
```

---

# 九、Phase 3-P1：评估增强与训练数据候选

## 9.1 阶段目标

在 Phase 3-P0 的确定性评估闭环基础上，引入辅助评估和训练数据沉淀能力。

## 9.2 可做什么

```text
LlmJudgeScorer 辅助评分器
Python offline evaluation notebook
Evaluation report visualization
Trace error clustering
TrainingExampleCandidate 数据结构
PatientBoundaryViolation 样本沉淀
Intent / symptom_group / risk_signal 训练数据候选沉淀
```

## 9.3 不做什么

```text
不把 LLM-as-a-Judge 作为唯一评分依据
不训练生产模型
不自动上线模型 Provider
不自动修改 CapabilityProfile
不接复杂训练平台
```

---

# 十、Phase 4：经验进化与训练数据沉淀

## 10.1 阶段目标

建立“运行记录—评估结果—反馈—经验候选—训练数据候选—审核—经验记忆”的闭环。

## 10.2 本阶段要做什么

```text
RuntimeTrace 作为复盘输入
Feedback 数据结构
Outcome 数据结构
SafetyViolation / RegressionFinding 复盘入口
ExperienceCandidate
TrainingExampleCandidate
Shadow Learning 原型
Experience Memory Governance
Clinical Experience Memory 更新候选
训练数据版本 TrainingDatasetVersion 原型
经验下线和回滚机制
经验触发影响 Question Policy / SafetyGate / DecisionBoundary
```

## 10.3 可引入的模型 / Python 能力

```text
Python offline analysis
FailureClusterProvider
TraceSummarizationProvider
SimilarCaseRetriever
ExperienceCandidateMiner
CaseDifficultyClassifier
小型 intent / symptom_group / risk_signal 分类器实验
```

## 10.4 完成标准

```text
1. RuntimeTrace 能被用于病例复盘。
2. 反馈能记录修正原因。
3. 结局能回填最终结果。
4. EvaluationResult 能生成经验候选和训练数据候选。
5. 候选经验不能直接进入 Runtime。
6. 候选训练样本不能未经审核直接进入训练集。
7. 审核通过后可进入 Clinical Experience Memory 或 TrainingDatasetVersion。
8. 经验上线失败时可回滚。
```

---

# 十一、Phase 4-P1：AI Provider / RAG / GraphRAG 原型

## 11.1 阶段目标

在 Runtime、资产和评估闭环稳定之后，开始接入真实 AI 能力 Provider。

## 11.2 可做什么

```text
Python AI Provider prototype
EmbeddingModelProvider
EvidenceRetrieverProvider
EvidenceRerankerProvider
QueryRewriteProvider
RAG EvidenceProvider 原型
GraphRAG / KG-lite evidence experiment
MCP read-only adapter prototype
similar case retrieval experiment
```

## 11.3 不做什么

```text
不让 RAG 直接生成患者端最终回答
不让 GraphRAG 直接控制诊断输出
不让 MCP 绕过 Provider Interface
不让 Python Provider 修改 RuntimeState
不引入完整 Milvus / Neo4j，除非 pgvector / PostgreSQL KG-lite 不够
```

---

# 十二、Phase 5：平台化、模型服务化与企业级治理

## 12.1 阶段目标

把前面阶段形成的能力整合为企业级平台。

## 12.2 本阶段要做什么

```text
Training Center 后台
Runtime Console 后台
Asset Console 后台
Experience Memory Center 后台
Evaluation & Recertification Center 后台
Model Registry / Model Provider Center
Audit & Governance Center 后台
Role & Permission 权限体系
PostgreSQL 持久化
pgvector 检索能力
Redis 缓存 / session / 锁
Python AI Provider service
Docker Compose 多服务部署
版本管理与发布流程
回滚机制
数据脱敏和访问审计
Patient-facing / Clinician Copilot / Silent Evaluation 三种模式完整化
```

## 12.3 完成标准

```text
1. 不同角色权限清楚。
2. 平台可管理训练包、经验、评估、模型版本和审计。
3. Runtime Console 可查看一次问诊的完整状态和 Trace。
4. 模型 Provider 版本、训练数据版本、评估结果可追踪。
5. 经验上线、下线、回滚可审计。
6. Capability Profile 的更新可追踪。
7. 三种运行模式可以独立配置输出边界。
```

---

# 十三、Phase 5-P1+：后置高级能力

后置高级能力包括：

```text
MCP Client / Server
Spring AI MCP
Provider Skills / Skill Metadata
Multi-Agent review workflow
Computer Use for internal QA
Voice / Realtime interaction layer
Neo4j / Milvus / Qdrant
SFT / DPO / Distillation / RFT
模型灰度发布 / A/B 评估
OpenTelemetry / Prometheus / Grafana
Kubernetes / CI-CD 完整发布系统
```

这些能力只有在平台化治理边界完成后才允许进入。

---

# 十四、各阶段依赖关系

```text
Phase 0：设计冻结和工程骨架
  ↓
Phase 1：Runtime MVP
  ↓
Phase 2：共享能力资产原型
  ↓
Phase 3：训练与评估闭环
  ↓
Phase 3-P1：评估增强与训练数据候选
  ↓
Phase 4：经验进化与训练数据沉淀
  ↓
Phase 4-P1：AI Provider / RAG / GraphRAG 原型
  ↓
Phase 5：平台化、模型服务化与企业级治理
  ↓
Phase 5-P1+：后置高级能力
```

---

# 十五、阶段内部优先级

## 15.1 Phase 1 优先级

```text
MVP-P0-A：Runtime 状态骨架
MVP-P0-B：病例结构化与入口判断
MVP-P0-C：安全门和候选诊断
MVP-P0-D：证据图与下一步动作
MVP-P0-E：输出边界与分角色表达
MVP-P0-F：最小测试集与集成验证
```

## 15.2 Phase 2 优先级

```text
Phase2-P0-A：资产元数据与 Provider 接口
Phase2-P0-B：YAML Asset Package Repository
Phase2-P0-C：YAML Provider 实现
Phase2-P0-D：Runtime 接入 Provider
Phase2-P0-E：ExperienceContext 原型
Phase2-P0-F：资产调试 API
Phase2-P0-G：集成测试与回归验收
```

## 15.3 Phase 3 优先级

```text
Phase3-P0-A：Evaluation 数据结构
Phase3-P0-B：病例集 Repository 与 YAML 病例格式
Phase3-P0-C：EvaluationRunner 执行 Runtime
Phase3-P0-D：Scorer 评分器体系
Phase3-P0-E：EvaluationResult 聚合与报告
Phase3-P0-F：CapabilityProfile 更新建议
Phase3-P0-G：Evaluation API 与验收测试
```

## 15.4 Phase 4 优先级

```text
Phase4-P0-A：Feedback / Outcome / RegressionFinding 复盘结构
Phase4-P0-B：ExperienceCandidate 与 TrainingExampleCandidate
Phase4-P0-C：候选经验与训练样本治理流程
Phase4-P0-D：Python offline analysis 原型
Phase4-P0-E：相似病例 / 错误聚类实验
```

## 15.5 Phase 5 优先级

```text
Phase5-P0-A：PostgreSQL 持久化
Phase5-P0-B：React Console 原型
Phase5-P0-C：Model Registry / Asset Registry
Phase5-P0-D：Python AI Provider service
Phase5-P0-E：pgvector evidence / similar case retrieval
Phase5-P0-F：权限、审计、发布、回滚
```

---

# 十六、各阶段与总设计文档的对应关系

| 总设计文档内容 | 对应阶段 |
|---|---|
| Runtime 执行层 | Phase 1 核心实现 |
| 共享能力资产层 | Phase 2 开始实现 |
| Evaluation 与能力授权 | Phase 3 核心实现 |
| LLM-as-a-Judge / 训练数据候选 | Phase 3-P1 开始 |
| 经验进化机制 | Phase 4 核心实现 |
| 模型训练与后训练规划 | Phase 3-P1 起沉淀数据，Phase 4/5 接入模型能力 |
| RAG / GraphRAG / Python Provider | Phase 4-P1 起原型，Phase 5 服务化 |
| 平台管理层 | Phase 5 为主，Phase 3/4 局部实现 |
| MCP / Skills / Multi-Agent | Phase 5-P1+ 后置试验 |
| 企业级运行模式 | Phase 5 完整实现 |

---

# 十七、当前最应该开始的工作

当前仓库已完成：

```text
Phase 1-P0：Runtime MVP
Phase 2-P0：共享能力资产原型
Phase 3-P0：训练与评估闭环 MVP（已冻结）
Phase 4-P0 / P4-P1：候选沉淀与治理（已冻结）
Phase 5-P0：持久化与治理底座（已冻结）
Phase 5-P1：最小 Console API 与访问治理（已冻结）
Phase 5-P2：最小前端 Console MVP（已完成）
```

当前无强制实现任务。若继续演进，应从后置任务立项：

```text
正式登录 / OAuth
Docker Compose
完整产品化前端 / Training Center
Python AI Provider / RAG / GraphRAG
模型训练 / 后训练
```

暂不应该在未立项时开始：

```text
破坏 Safe DTO 或 Console RBAC-lite 边界
ApprovedExperience 自动生效
正式医生审核平台语义
```

---

# 十八、最终阶段目标总结

```text
Phase 1：证明受控诊断 Runtime 能跑通。
Phase 2：证明 Runtime 能调用可替换、可版本化、可追踪的共享能力资产。
Phase 3：证明能力不是口头声明，而能通过评估授权。
Phase 3-P1：证明评估结果可以开始沉淀训练样本候选和辅助评分能力。
Phase 4：证明经验和训练样本不是自动记忆，而能通过治理沉淀。
Phase 4-P1：证明 AI Provider、RAG、GraphRAG 能在 Provider 边界内增强能力。
Phase 5：证明系统可以平台化、模型服务化、权限化、审计化、企业级运行。
```

项目落地的核心节奏是：

```text
先让 Runtime 站起来，
再让资产接进来，
再让评估管住它，
再让训练数据和经验慢慢长出来，
再让模型和 RAG 作为 Provider 接入，
最后再做平台化治理。
```
