# ClinMindRuntime 技术实现总方案

> 本文档用于说明 ClinMindRuntime 的代码级落地方式：模块如何分层、包结构如何规划、Runtime 主链路如何实现、Provider 如何接入、Evaluation 如何执行、数据存储如何演进、Python AI Provider / RAG / 模型服务 / MCP 如何后置接入。  
> 它连接完整系统设计、阶段拆分路线图、全局技术栈、AI 前沿技术规划、模型训练规划和各 Phase 详细设计，是后续代码实现的总蓝图。

---

# 一、文档定位

已有文档分别回答：

```text
docs/ClinMindRuntime完整系统设计.md
= 系统是什么、为什么这样设计、长期愿景是什么。

docs/ClinMindRuntime阶段拆分路线图.md
= 每个阶段什么时候做什么。

docs/全局技术栈与架构选型.md
= 用什么技术，以及什么时候接入。

docs/AI前沿技术选型与接入规划.md
= MCP、Agent SDK、LangGraph、GraphRAG、Skills 等怎么接，哪些不能提前接。

docs/模型训练与后训练规划.md
= 模型训练、后训练、模型部署和 Model Provider 怎么演进。
```

本文档回答：

```text
这些设计最终如何落到代码、模块、依赖、API、Provider、存储、测试和部署中。
```

---

# 二、当前实现状态

当前仓库状态：

```text
Phase 1-P0：Runtime MVP 已完成。
Phase 2-P0：共享能力资产原型已完成。
Phase 3-P0：详细设计已完成，代码实现尚未开始。
```

当前最优实现任务：

```text
Phase3-P0-A：Evaluation 数据结构
```

当前禁止提前实现：

```text
Python AI Provider
真实 RAG / GraphRAG
模型训练 / 后训练
MCP / LangGraph / Agent SDK
PostgreSQL / Redis / pgvector / Neo4j
前端 Training Center
完整权限系统
真实医生审核流
```

---

# 三、总体技术实现架构

完整技术实现架构分为六层：

```text
1. API Layer
   对外暴露 Runtime API、debug/internal API、未来 Console API。

2. Application / Orchestration Layer
   负责编排 Runtime 主流程、Evaluation 执行流、Proposal 生成流。

3. Domain Module Layer
   负责 EntryAssessment、CaseFrame、SafetyGate、DDx、EvidenceGraph、DecisionBoundary 等领域逻辑。

4. Provider / Asset Layer
   负责读取可替换资产、知识、能力档案、经验、未来模型 Provider 和外部工具。

5. Storage / Repository Layer
   当前使用 in-memory 与 YAML；后续演进到 PostgreSQL、Redis、pgvector。

6. Integration Layer
   后续连接 Python AI Provider、RAG、GraphRAG、MCP、外部 LLM、模型服务。
```

依赖方向必须单向：

```text
API
→ Application / Runtime Service
→ Domain Module
→ Provider Interface
→ Provider Implementation / Repository / External Service
```

禁止反向依赖：

```text
Provider 不能反向控制 RuntimeService。
External Agent 不能直接修改 RuntimeState。
Model / LLM / MCP 不能绕过 SafetyGate / DecisionBoundary。
```

---

# 四、Java 后端分层设计

推荐代码分层：

```text
com.clinmind.runtime
  api/
  runtime/
  state/
  trace/
  entry/
  caseframe/
  knowledge/
  experience/
  safety/
  reasoning/
  boundary/
  output/
  asset/
  provider/
  evaluation/
  training/        # 后置，不在 Phase 3-P0 实现真实训练
  integration/     # 后置，不在 Phase 3-P0 接外部服务
  common/
```

## 4.1 api

职责：

```text
Controller
Request DTO
Response DTO
Response Mapper
Error response
```

原则：

```text
1. Controller 只做请求接收和响应映射。
2. Controller 不直接调用底层领域模块。
3. Controller 必须通过 RuntimeService / EvaluationService 等应用服务。
4. Patient-facing API 和 debug/internal API 必须路径隔离。
```

## 4.2 runtime / state / trace

职责：

```text
RuntimeService
RuntimeState
RuntimeStatus
RuntimeMode
WorkMode
RuntimeTrace
RuntimeStore
RuntimeTraceAspect
TraceStep
```

原则：

```text
1. RuntimeService 是一次问诊主流程的唯一编排入口。
2. RuntimeState 是诊断状态的唯一主承载。
3. RuntimeTrace 是审计与评估的重要输入。
4. Provider、Scorer、Model 不允许直接修改 RuntimeState。
```

## 4.3 domain modules

领域模块包括：

```text
entry/
caseframe/
knowledge/
experience/
safety/
reasoning/
boundary/
output/
```

职责：

```text
EntryAssessment：判断输入工作态、入口风险、是否支持。
CaseFrame：结构化病例信息。
KnowledgeContext：聚合医学知识资产。
ExperienceContext：聚合已验证经验。
SafetyGate：强安全门。
DifferentialDiagnosisBoard：候选诊断状态。
EvidenceGraph：证据关系。
QuestionTestPolicy：下一步追问 / 检查建议。
DecisionBoundary：输出边界。
PatientOutput / ClinicianReport：分角色输出。
```

## 4.4 asset / provider

职责：

```text
AssetMetadata
AssetPackage
AssetPackageManifest
AssetPackageRepository
KnowledgeProvider
ClinicalExperienceProvider
CapabilityProfileProvider
EvidenceAssetProvider
```

原则：

```text
1. Runtime 不直接读取硬编码资产。
2. Runtime 通过 Provider Interface 读取资产。
3. Provider 返回结构化资产对象。
4. RuntimeTrace 必须记录 asset_package_id、asset_package_version、asset_id、asset_version。
```

## 4.5 evaluation

Phase 3 重点包。

建议结构：

```text
evaluation/
  model/
  repository/
  runner/
  scorer/
  result/
  proposal/
  api/
```

职责：

```text
model：EvaluationCase、ExpectedOutcome、EvaluationRun、EvaluationResult 等数据结构。
repository：YAML case set 读取。
runner：通过 RuntimeService 执行病例。
scorer：对 RuntimeCaseExecution 评分。
result：聚合 EvaluationResult。
proposal：生成 CapabilityProfileUpdateProposal。
api：debug/internal evaluation API。
```

核心约束：

```text
EvaluationRunner 只能调用 RuntimeService。
Scorer 只能评分，不能修改 RuntimeState。
CapabilityProfileProposalService 只能生成 proposal，不能自动上线。
```

## 4.6 training

后置包，Phase 3-P0 不实现真实训练。

未来职责：

```text
TrainingExampleCandidate
TrainingDatasetVersion
ModelProviderMetadata
ModelEvaluationRecord
ModelRegistry
```

边界：

```text
training 包负责训练数据与模型元数据治理，
不负责直接执行医疗问诊，
不负责直接输出患者端答案。
```

## 4.7 integration

后置包，Phase 3-P0 不接入。

未来职责：

```text
PythonAiProviderClient
RagProviderClient
McpAdapter
ExternalLlmClient
EmbeddingClient
GraphRagClient
```

边界：

```text
Integration 只连接外部服务，返回结构化结果。
Integration 不直接修改 RuntimeState。
Integration 不直接调用 PatientOutput。
```

---

# 五、核心包结构规划

当前与近期建议结构：

```text
src/main/java/com/clinmind/runtime/
  api/
    RuntimeController.java
    PatientRuntimeController.java
    AssetController.java
    DebugRuntimeController.java
    EvaluationController.java        # Phase 3-P0-G

  runtime/
    RuntimeService.java
    RuntimeStore.java

  state/
    RuntimeState.java
    RuntimeStatus.java
    RuntimeMode.java
    WorkMode.java
    RuntimeTrace.java

  trace/
    TraceStep.java
    RuntimeTraceAspect.java
    RuntimeTraceCollector.java

  entry/
  caseframe/
  knowledge/
  experience/
  safety/
  reasoning/
  boundary/
  output/

  asset/
    model/
    repository/
    provider/

  evaluation/
    model/                         # Phase3-P0-A
    repository/                    # Phase3-P0-B
    runner/                        # Phase3-P0-C
    scorer/                        # Phase3-P0-D
    result/                        # Phase3-P0-E
    proposal/                      # Phase3-P0-F
    api/                           # Phase3-P0-G

  training/                        # Phase 3-P1 / Phase 4 后置
  integration/                     # Phase 4/5 后置
  common/
    error/
    response/
    time/
```

测试结构：

```text
src/test/java/com/clinmind/runtime/
  phase1 regression tests
  asset tests
  evaluation/
    model/
    repository/
    runner/
    scorer/
    proposal/
    api/
```

资源结构：

```text
src/main/resources/
  assets/packages/phase2-default/
  evaluation/case-sets/phase3-default/
  application.yml
```

---

# 六、Runtime 主流程实现链路

Runtime 主流程必须固定为：

```text
Controller
→ RuntimeService.startRuntime / continueRuntime
→ EntryAssessmentService
→ CaseFrameService
→ KnowledgeContextService
→ ExperienceContextService
→ SafetyGateService
→ DifferentialDiagnosisBoardService
→ EvidenceGraphService
→ QuestionTestPolicyService
→ DecisionBoundaryService
→ PatientOutputService / ClinicianReportService
→ RuntimeTrace
→ RuntimeStore
```

每个模块的输入输出原则：

```text
1. 输入：RuntimeState + 当前模块所需资产 / Provider 结果。
2. 输出：结构化 Result 或 RuntimeState 局部更新。
3. Trace：每个关键模块必须记录执行结果。
4. Error：安全相关失败必须 fail-safe。
```

禁止：

```text
1. Controller 直接调用 SafetyGate / EvidenceGraph 等底层模块。
2. Provider 直接生成患者端最终答案。
3. LLM / RAG / Model 直接覆盖 RuntimeState。
4. DecisionBoundary 之后再让模型自由改写医疗内容。
```

---

# 七、Provider / Asset / Model / MCP 接入边界

## 7.1 Provider 总规则

```text
Runtime Core
→ Provider Interface
→ Provider Implementation
→ Asset / External Service / Model / MCP Server
```

Provider 只能返回：

```text
结构化资产
结构化候选
证据引用
模型草稿
评分草稿
```

Provider 不允许：

```text
直接修改 RuntimeState
直接决定 SafetyGate
直接决定 DecisionBoundary
直接输出 PatientOutput
直接上线 CapabilityProfile
```

## 7.2 YAML Provider

阶段：Phase 2-P0 已实现。

用途：

```text
RedFlagRules
TestRecommendationRules
CapabilityProfile
ClinicalPathway refs
RAG evidence refs
KG-lite refs
Experience units
```

## 7.3 Python AI Provider

阶段：Phase 4/5 后置。

接入方式：

```text
Java Runtime
→ Provider Interface
→ PythonAiProviderClient
→ FastAPI service
```

候选接口：

```text
POST /models/intent-classify
POST /models/extract-case-frame
POST /models/normalize-symptom
POST /models/retrieve-evidence
POST /models/rewrite-patient-safe
POST /models/judge-output-safety
POST /models/mine-experience-candidates
```

## 7.4 RAG / GraphRAG Provider

阶段：Phase 4-P1 / Phase 5。

接入方式：

```text
KnowledgeContextService
→ EvidenceAssetProvider
→ RagProvider / GraphRagProvider
→ EvidenceRef
→ EvidenceGraphService
→ DecisionBoundary
```

## 7.5 MCP Adapter

阶段：Phase 5 或后置实验。

接入方式：

```text
Provider Interface
→ McpAdapter / McpClient
→ MCP Server
→ External Tool / Data Source
```

MCP 不能成为 Runtime 主控。

## 7.6 Model Provider

阶段：Phase 4/5。

接入方式：

```text
ModelProviderMetadata
→ ModelProviderVersion
→ EvaluationResult
→ CapabilityProfileUpdateProposal
→ Review / Governance
→ Runtime 可用 Provider
```

模型上线必须有评估依据和可回滚机制。

---

# 八、Evaluation 技术实现架构

Phase 3 的技术实现链路：

```text
EvaluationController
→ EvaluationRunService
→ EvaluationCaseRepository
→ RuntimeEvaluationRunner
→ RuntimeService.startRuntime / continueRuntime
→ RuntimeCaseExecution
→ EvaluationScorer(s)
→ EvaluationResultAggregator
→ EvaluationResult
→ CapabilityProfileProposalService
```

## 8.1 Evaluation 数据结构

Phase3-P0-A：

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

## 8.2 Case Repository

Phase3-P0-B：

```text
src/main/resources/evaluation/case-sets/phase3-default/
```

## 8.3 Runner

Phase3-P0-C：

```text
RuntimeEvaluationRunner 必须通过 RuntimeService 执行病例。
```

## 8.4 Scorer

Phase3-P0-D：

```text
SafetyGateScorer
PatientBoundaryScorer
DdxCoverageScorer
NextActionScorer
TraceCompletenessScorer
AssetVersionTraceScorer
```

## 8.5 Result / Proposal

Phase3-P0-E / F：

```text
EvaluationResultAggregator
CapabilityEvaluationPolicy
CapabilityProfileUpdateProposal
CapabilityProfileProposalService
```

## 8.6 API

Phase3-P0-G：

```text
POST /api/v1/debug/evaluations/runs
GET /api/v1/debug/evaluations/runs/{run_id}
GET /api/v1/debug/evaluations/runs/{run_id}/result
GET /api/v1/debug/evaluations/runs/{run_id}/items/{case_id}
POST /api/v1/debug/evaluations/runs/{run_id}/capability-profile-proposal
```

---

# 九、数据存储演进方案

## 9.1 Phase 1–3

```text
RuntimeStore：In-memory
AssetPackageRepository：YAML resources
EvaluationCaseRepository：YAML resources
EvaluationRunStore：In-memory
```

原因：

```text
先证明 Runtime、Asset、Evaluation 主线，不让数据库复杂度干扰 P0。
```

## 9.2 Phase 4

可开始引入 PostgreSQL 原型承载：

```text
RuntimeTrace
EvaluationResult
RegressionFinding
ExperienceCandidate
TrainingExampleCandidate
```

## 9.3 Phase 5

PostgreSQL 承载：

```text
Runtime sessions
RuntimeTrace
AssetPackage metadata
EvaluationCaseSet
EvaluationRun / EvaluationResult
CapabilityProfileUpdateProposal
ExperienceCandidate
TrainingDatasetVersion
ModelProviderMetadata
AuditLog
User / Role / Permission
```

Redis 承载：

```text
session cache
short-term context cache
lock
rate limit
job progress
```

pgvector 承载：

```text
evidence embeddings
similar case embeddings
experience embeddings
trace error embeddings
```

---

# 十、API 分层与接口边界

## 10.1 Patient-facing API

原则：

```text
只返回患者安全字段。
不得泄露 DDx Board、EvidenceGraph、must_not_miss、clinician report、asset internals。
```

## 10.2 Clinician-facing API

原则：

```text
可返回候选诊断、证据图、检查建议和医生摘要。
必须标注能力边界和证据状态。
```

## 10.3 Debug / Internal API

原则：

```text
路径必须包含 /api/v1/debug 或 /internal。
可返回 trace、asset used、evaluation result。
不得作为 patient-facing client 使用。
```

## 10.4 Future Console API

Phase 5 后置，用于：

```text
Training Center
Asset Console
Runtime Console
Evaluation Center
Model Registry
Audit Center
```

---

# 十一、Python AI Provider 接入方案

Python Provider 后置，不属于 Phase 3-P0。

未来服务结构：

```text
clinmind-ai-provider/
  app/
    routers/
      intent.py
      caseframe.py
      embedding.py
      rag.py
      safety_expression.py
      judge.py
      experience_mining.py
    services/
      model_registry.py
      llm_client.py
      embedding_service.py
      retrieval_service.py
      inference_service.py
```

Java 侧调用规则：

```text
1. 通过 Provider Interface 调用。
2. 设置 timeout / retry / fallback。
3. Provider 失败不得导致患者端危险输出。
4. Provider 返回结果必须经过 Runtime 校验。
5. 所有调用必须记录 trace 和 provider version。
```

---

# 十二、RAG / GraphRAG / 向量库接入方案

接入顺序：

```text
Phase 2：只保留 evidence ref / kg_lite_ref。
Phase 3：评估必须检查 evidence / asset version trace。
Phase 4-P1：RAG EvidenceProvider 原型。
Phase 5：pgvector-backed retrieval。
后置：Milvus / Qdrant / Neo4j。
```

技术路线：

```text
PostgreSQL + pgvector 优先。
KG-lite 优先 PostgreSQL node / edge tables。
Neo4j 只有复杂图遍历成为核心需求后再接。
```

---

# 十三、模型训练与模型服务接入方案

接入原则：

```text
模型训练提升 Provider 能力。
Runtime 控制最终责任。
```

候选模型能力：

```text
IntentClassifierProvider
SymptomGroupClassifierProvider
RiskSignalClassifierProvider
CaseFrameExtractorProvider
EmbeddingModelProvider
EvidenceRerankerProvider
PatientSafeRewriteProvider
LlmJudgeScorer
ExperienceCandidateMiner
```

上线链路：

```text
TrainingDatasetVersion
→ Model Training / Post-training
→ ModelProviderVersion
→ EvaluationResult
→ CapabilityProfileUpdateProposal
→ Review / Governance
→ Runtime 可用 Provider
```

禁止：

```text
模型直接面向患者输出诊断。
模型直接修改 RuntimeState。
模型绕过 SafetyGate / DecisionBoundary。
模型未经 EvaluationResult 上线。
```

---

# 十四、测试体系总方案

测试分层：

```text
Unit Test：单模块逻辑。
Integration Test：Runtime pipeline。
Regression Test：Phase 1 / Phase 2 不被破坏。
Asset Test：资产版本、替代资产包、坏资产包 fail-safe。
Boundary Test：患者端输出隔离。
Evaluation Test：病例集考试和评分器。
API Test：debug/internal API 路径和响应。
Future Provider Test：Python / RAG / Model Provider 接入后回归。
```

Phase 3 每次改动必须保证：

```text
Phase 1 回归测试通过。
Phase 2 Provider / Asset 回归测试通过。
PatientOutputAssetIsolationTest 通过。
RuntimeAssetVersionMismatchTest 通过。
broken-package fail-safe 测试通过。
新增 Evaluation 模块必须有对应 JUnit 测试。
```

---

# 十五、部署演进方案

## 15.1 当前阶段

```text
Maven package
java -jar
本地运行
JUnit / Postman 验收
```

## 15.2 Phase 5

```text
Docker Compose
Java Runtime service
PostgreSQL
Redis
Python AI Provider service
Frontend service
```

## 15.3 后置

```text
Kubernetes
OpenTelemetry
Prometheus / Grafana
CI/CD release pipeline
model serving infra
```

---

# 十六、各 Phase 的技术实现重点

## Phase 1

```text
Runtime 主链路、RuntimeState、Trace、安全门、输出边界。
```

## Phase 2

```text
Provider 抽象、YAML Asset Package、资产版本、RuntimeTrace asset records。
```

## Phase 3

```text
Evaluation model、case repository、runner、scorer、result、proposal、debug API。
```

## Phase 3-P1

```text
LlmJudgeScorer 辅助、TrainingExampleCandidate、Python offline analysis。
```

## Phase 4

```text
ExperienceCandidate、TrainingExampleCandidate、feedback/outcome、trace analysis。
```

## Phase 4-P1

```text
Python AI Provider prototype、embedding、RAG、GraphRAG、similar case retrieval。
```

## Phase 5

```text
PostgreSQL、pgvector、React Console、Python Provider service、Model Registry、Audit、Security、Docker Compose。
```

---

# 十七、禁止提前实现内容

```text
1. 不在 Phase 3-P0 接 Python AI Provider。
2. 不在 Phase 3-P0 接 RAG / GraphRAG。
3. 不在 Phase 3-P0 接 MCP / LangGraph / Agent SDK。
4. 不在 Phase 3-P0 训练模型。
5. 不在 Phase 3-P0 接 PostgreSQL / Redis / pgvector / Neo4j。
6. 不在 Phase 3-P0 做前端 Training Center。
7. 不让 Provider / Model / LLM / MCP 替代 Runtime 主控。
8. 不让任何模块绕过 SafetyGate / DecisionBoundary。
9. 不自动上线 CapabilityProfile。
10. 不自动把 RuntimeTrace 变成经验或训练数据。
```

---

# 十八、当前最优下一步

当前最优实现任务仍然是：

```text
Phase3-P0-A：Evaluation 数据结构
```

具体动作：

```text
1. 读取 docs/Phase3_开发任务清单.md。
2. 将 Phase3-P0-A 标记为 [/]。
3. 新增 evaluation/model 下的基础数据结构。
4. 编写基础单元测试。
5. 将完成项标记为 [x]。
```

不应在该任务中实现：

```text
EvaluationRunner
Scorer
CapabilityProfile 更新
Python Provider
RAG / GraphRAG
MCP
模型训练
数据库持久化
前端
```

---

# 十九、最终结论

ClinMindRuntime 的技术实现路线是：

```text
先实现稳定 Runtime Core，
再实现可替换 Asset Provider，
再实现 Evaluation 闭环，
再沉淀经验和训练数据，
再接入 AI Provider / RAG / GraphRAG / 模型服务，
最后进入平台化、模型治理和企业级部署。
```

任何新技术、新模型、新 Agent 框架、新数据库、新前端，都必须服务这条主线，而不能取代 Runtime 主控。
