# ClinMindRuntime 完整系统设计

> 项目名称：ClinMindRuntime  
> 中文定位：企业级智能诊断训练—运行—评估—后训练—经验进化平台  
> 核心定义：面向医疗问诊场景的受控诊断 Runtime。它不是普通医疗问答系统，也不是替代医生的自主诊断系统，而是一个能运行、能评估、能训练能力、能沉淀经验、能治理上线的企业级智能诊断支持平台。

---

# 一、项目定位

ClinMindRuntime 是一个面向医疗问诊场景的智能诊断 Runtime 与能力治理系统。

它不是：

```text
普通 RAG 医疗问答
普通 Prompt Chain
普通多 Agent Demo
直接替代医生的 AI 医生
直接训练一个模型让它回答患者
```

它是：

```text
受控 Runtime
+ 共享能力资产
+ 训练与评估闭环
+ 模型训练 / 后训练规划
+ 经验候选治理
+ 平台化审计与发布体系
```

系统的核心目标是：

```text
让 AI 不只是调用医学知识回答问题，
而是在一个可控、可追踪、可评估、可治理的诊断 Runtime 中工作。
```

系统需要知道：

```text
1. 当前用户输入是否属于临床问诊。
2. 当前病例属于哪个症状群。
3. 当前病例已有信息和缺失信息是什么。
4. 哪些候选诊断需要考虑。
5. 哪些高危低概率疾病不能遗漏。
6. 哪些证据支持、反对或仍然缺失。
7. 下一步应该追问什么或建议什么检查。
8. 患者端和医生端分别允许看到什么。
9. 当前能力是否已经通过评估授权。
10. 运行结果是否能生成经验候选或训练数据。
11. 哪些模型能力可以作为 Provider 接入，哪些不能越过 Runtime。
```

系统角色定位：

```text
患者端：风险提示、信息补全、就医建议、健康教育、检查准备说明。
医生端：候选诊断、证据状态、相似经验提醒、检查建议、医生摘要。
后台端：病例复盘、错误归因、训练数据沉淀、经验候选发现、再认证评估、审计治理。
```

---

# 二、核心设计：六个能力域

## 2.1 从五个能力域扩展为六个能力域

早期设计中，ClinMindRuntime 的核心能力域是：

```text
医学知识域
临床经验域
诊断状态域
输出边界域
复盘进化域
```

随着 Phase 3 训练与评估闭环、AI 前沿技术规划、模型训练与后训练规划的加入，系统需要新增一个明确能力域：

```text
模型能力域
```

因此完整系统的能力域调整为：

```text
医学知识域
临床经验域
诊断状态域
输出边界域
评估进化域
模型能力域
```

核心公式：

```text
智能诊断能力
= 医学知识
+ 临床经验
+ 诊断状态
+ 输出边界
+ 评估进化
+ 模型能力
```

## 2.2 六个能力域分别指什么

### 医学知识域

医学知识域指系统掌握的相对稳定的医学事实、指南规则、疾病关系、危险信号和检查依据。

主要资产 / 模块：

```text
Clinical Pathway
KG-lite
RAG Evidence Library
Red Flag Rules
Test Recommendation Rules
MedicalKnowledgeProvider
EvidenceAssetProvider
```

### 临床经验域

临床经验域指系统从真实病例、医生反馈、误诊教训、相似病例和复盘中沉淀出的经验性提醒。

主要资产 / 模块：

```text
Clinical Experience Memory
ExperienceCandidate
ExperienceUnitAsset
ClinicalExperienceProvider
ExperienceContext
Experience Memory Governance
```

### 诊断状态域

诊断状态域指一次问诊当前处于什么状态，已经知道什么，还缺什么，哪些候选诊断成立或需要排除。

主要模块：

```text
RuntimeState
RuntimeStatus
EntryAssessment
CaseFrame
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
RuntimeTrace
```

### 输出边界域

输出边界域指系统当前能不能说、能说到什么程度、患者端和医生端分别能看到什么。

主要模块：

```text
Capability Profile
SafetyGate
DecisionBoundary
FailurePolicy
PatientOutputService
ClinicianReportService
Role & Permission
```

### 评估进化域

评估进化域指系统如何证明自己具备能力、如何发现错误、如何决定是否授权或降级。

主要模块：

```text
EvaluationCaseSet
EvaluationRunner
EvaluationScorer
EvaluationResult
SafetyViolation
RegressionFinding
CapabilityProfileUpdateProposal
Review & Recertification
Audit & Governance
```

### 模型能力域

模型能力域指系统中可训练、可替换、可评估、可部署的 AI 模型能力。

主要能力：

```text
IntentClassifierProvider
WorkModeClassifierProvider
SymptomGroupClassifierProvider
RiskSignalClassifierProvider
CaseFrameExtractorProvider
EmbeddingModelProvider
EvidenceRetrieverProvider
EvidenceRerankerProvider
PatientSafeRewriteProvider
LlmJudgeScorer
ExperienceCandidateMiner
```

模型能力域不直接控制最终医疗输出。它只能作为 Provider 返回结构化候选结果。

---

# 三、总体设计原则

## 3.1 Runtime 主控

```text
RuntimeState、SafetyGate、DecisionBoundary、RuntimeTrace 是系统主控核心。
```

模型、RAG、MCP、Agent SDK、LangGraph、GraphRAG、Skills 等都不能替代 Runtime 主控。

## 3.2 安全优先

```text
不是先生成答案再加免责声明，
而是先判断当前是否允许输出。
```

SafetyGate 和 DecisionBoundary 优先于自然语言生成。

## 3.3 证据优先

系统不直接输出疾病结论，而是维护候选诊断与证据之间的关系。

```text
候选诊断
支持证据
反对证据
缺失证据
冲突证据
必须追问
推荐检查
输出权限
```

## 3.4 高危保留

```text
高危低概率疾病必须保留为 must_not_miss 或 need_to_rule_out。
```

## 3.5 经验受控

Clinical Experience Memory 只能让系统更警觉，不能让系统更武断。

```text
经验可以影响追问优先级、检查建议和输出边界收紧，
不能直接决定最终诊断。
```

## 3.6 模型受控

模型能力只能生成结构化候选。

```text
模型可以帮助识别意图、抽取病例、检索证据、改写表达、辅助评分、挖掘经验候选；
模型不能直接决定 RuntimeState、SafetyGate、DecisionBoundary 或患者端最终输出。
```

## 3.7 评估授权

能力不是口头声明，而要通过评估授权。

```text
EvaluationResult
→ CapabilityProfileUpdateProposal
→ Review / Governance
→ CapabilityProfileAsset
→ DecisionBoundary
```

## 3.8 可追踪、可复盘、可回滚

每一次问诊都必须记录：

```text
输入是什么
医学知识使用了什么
模型 Provider 输出了什么候选
临床经验触发了什么
CaseFrame 如何变化
SafetyGate 是否触发
DDx 如何变化
EvidenceGraph 如何变化
为什么输出被限制
医生是否采纳
最终结局是什么
```

---

# 四、总体架构

## 4.1 四层结构

完整系统由四层组成：

```text
1. 平台管理层：负责训练、治理、评估、权限、审计、发布和回滚。
2. 共享能力资产层：承载医学知识、规则、经验、能力边界、评估结果和模型版本元数据。
3. Runtime 执行层：负责一次问诊当下从输入、状态更新、证据推理到安全输出的执行过程。
4. Provider / Model / Tool 层：负责 LLM、RAG、embedding、模型推理、MCP 外部工具和 AI 前沿技术适配。
```

四层关系：

```text
平台管理层
  ↓ 训练 / 评估 / 审核 / 发布 / 回滚 / 治理
共享能力资产层
  ↓ 被 Runtime 读取 / 检索 / 引用 / 约束
Runtime 执行层
  ↓ 调用 Provider，产生 RuntimeTrace / Evaluation 数据 / 反馈入口
Provider / Model / Tool 层
  ↓ 返回结构化 Draft / Candidate / EvidenceRef
Runtime 执行层继续校验、约束和输出
```

## 4.2 平台管理层

平台管理层负责管理、治理、评估和审计。

```text
Training Center
Runtime Console
Asset Console
Experience Memory Center
Evaluation & Recertification Center
Model Registry / Training Center
Audit & Governance Center
Role & Permission
```

## 4.3 共享能力资产层

共享能力资产层由平台训练、治理、评估或再认证之后形成，被 Runtime 执行层读取。

```text
Symptom Rotation Library
Clinical Pathway
KG-lite
RAG Evidence Library
Red Flag Rules
Test Recommendation Rules
Capability Profile
Clinical Experience Memory
Experience Candidates
Evaluation Results
Training Dataset Version
Model Provider Metadata
Recertification Records
Audit Logs
```

## 4.4 Runtime 执行层

Runtime 执行层负责一次问诊当下如何运行。

```text
Runtime API
EntryAssessment
RuntimeState
RuntimeStatus
RuntimeTrace
Short-term Context
CaseFrame
Knowledge Context
Experience Context
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
PatientOutputService
ClinicianReportService
FailurePolicy
```

## 4.5 Provider / Model / Tool 层

Provider / Model / Tool 层负责提供可替换能力。

```text
Java Provider
Python AI Provider
LLM Provider
RAG Provider
Embedding Provider
Model Provider
MCP Adapter
Tool Calling Adapter
GraphRAG Provider
LlmJudgeScorer Provider
```

它们的输出必须是结构化结果：

```text
CaseFrameDraft
SymptomNormalizationCandidate
RiskSignalCandidate
EvidenceRef
ExperienceCandidate
PatientSafeExpressionDraft
MetricResultDraft
```

不能直接作为患者端最终答案。

---

# 五、核心运行链路

一次问诊的完整运行链路：

```text
用户 / 医生输入
  ↓
Runtime API 创建或继续 Runtime
  ↓
EntryAssessment 判断工作态
  ↓
可选 Intent / WorkMode / SymptomGroup Model Provider 生成结构化候选
  ↓
Runtime 校验候选并更新 RuntimeState
  ↓
CaseFrame 更新
  ↓
Knowledge Context 查询医学知识：Clinical Pathway / KG-lite / RAG Evidence / Red Flag Rules
  ↓
Experience Context 检索已验证经验
  ↓
SafetyGate 危险信号识别
  ↓
Differential Diagnosis Board 构建候选诊断
  ↓
EvidenceGraph 组织证据关系
  ↓
Question / Test Policy 决定下一步追问或检查建议
  ↓
DecisionBoundary 判断当前允许输出什么
  ↓
PatientOutput / ClinicianReport 生成分角色输出
  ↓
RuntimeTrace 记录知识、经验、模型候选、资产版本和输出边界
  ↓
Evaluation / Feedback / Outcome 进入复盘与训练数据候选
```

---

# 六、模型训练与后训练闭环

## 6.1 为什么需要模型训练规划

ClinMindRuntime 的 AI 能力不能永远停留在规则、YAML 和 mock Provider。

长期需要训练或后训练的能力包括：

```text
意图识别
工作态分类
症状群识别
病例结构化抽取
风险信号识别
证据检索与 rerank
患者安全表达改写
医生端报告草稿
LLM-as-a-Judge 辅助评分
经验候选挖掘
```

## 6.2 训练数据来源

训练数据来自系统运行和评估闭环，而不是凭空构造。

```text
RuntimeTrace
EvaluationCaseSet
EvaluationItemResult
EvaluationResult
SafetyViolation
PatientBoundaryViolation
Doctor Feedback
Follow-up Outcome
ExperienceCandidate
Human-reviewed correction
RAG evidence feedback
```

## 6.3 后训练闭环

```text
Runtime Execution
→ RuntimeTrace
→ EvaluationResult
→ RegressionFinding / SafetyViolation
→ TrainingExampleCandidate
→ Human Review / Rule-based Filter
→ TrainingDatasetVersion
→ Model Training / Post-training
→ ModelProviderVersion
→ Evaluation again
→ CapabilityProfileUpdateProposal
→ Review / Governance
→ Runtime 再验证
```

## 6.4 模型部署边界

模型不直接部署在 Runtime Core 内部。

推荐结构：

```text
Java Runtime Core
→ Provider Interface
→ Python AI Provider / Java AI Provider
→ Model Runtime / LLM API / Embedding Service
```

## 6.5 模型上线边界

模型能力不能自动扩大输出权限。

正确关系：

```text
ModelProviderVersion
→ EvaluationResult
→ CapabilityProfileUpdateProposal
→ Review / Governance
→ CapabilityProfileAsset
→ DecisionBoundary
```

错误关系：

```text
模型表现看起来不错
→ 直接允许患者端输出更多诊断信息
```

---

# 七、AI 前沿技术接入边界

AI 前沿技术是能力扩展，不是 Runtime 主控替代。

| 技术 | 在系统中的位置 | 接入阶段 |
|---|---|---|
| Tool Calling | Provider 内部工具调用 | Phase 4/5 |
| MCP | 外部工具 / 数据源标准连接协议 | Phase 5 或 Provider 实验 |
| Agent SDK | 编排思想参考，不替代 Runtime | Phase 4/5 实验 |
| LangGraph | 状态图思想参考，可用于 Python Provider 实验 | Phase 4/5 |
| LangChain / LlamaIndex | Python RAG / Agent Provider 实验框架 | Phase 4/5 |
| GraphRAG | EvidenceProvider / KG-lite 增强方向 | Phase 4/5 后置 |
| LLM-as-a-Judge | EvaluationScorer 辅助评分器 | Phase 3-P1 起 |
| Guardrails | Provider 输出检查思想，不能替代 DecisionBoundary | Phase 3/4 |
| Skills | 可复用 Capability / Provider 能力包 | Phase 5 |
| Agent Memory | 与 ExperienceCandidate / ExperienceContext 对照 | Phase 4 |
| Multi-Agent | 后台审核、资产治理协作模式 | Phase 5 后置 |

核心关系：

```text
前沿 AI 技术负责扩展能力，
ClinMindRuntime 负责约束能力。
```

---

# 八、技术栈总路线

工程技术栈遵循 `docs/全局技术栈与架构选型.md`。

当前长期选型：

```text
Runtime Core：Java 17+ / Spring Boot 3.x
Evaluation Core：Java Service
Python AI Provider：FastAPI，后置接入
Main DB：PostgreSQL，Phase 4/5 接入
Vector：pgvector 优先，Milvus / Qdrant 后置
KG-lite：PostgreSQL node / edge tables 优先，Neo4j 后置
Frontend：React + TypeScript + Vite，Phase 5 接入
Deployment：Docker Compose 后置
Security：Spring Security / JWT / RBAC / AuditLog 后置
```

---

# 九、阶段路线总览

```text
Phase 1-P0：Runtime MVP
  证明受控诊断 Runtime 能跑通。

Phase 2-P0：共享能力资产原型
  证明 Runtime 能通过 Provider 读取可替换、可版本化、可追踪资产。

Phase 3-P0：训练与评估闭环
  证明能力不是口头声明，而能通过 EvaluationCaseSet、Scorer 和 EvaluationResult 授权。

Phase 3-P1：评估增强与训练数据候选
  可引入 LLM-as-a-Judge、Python 离线分析、TrainingExampleCandidate。

Phase 4-P0：经验进化与训练数据沉淀
  从 RuntimeTrace、EvaluationResult、Feedback 中生成 ExperienceCandidate 和训练数据候选。

Phase 4-P1：RAG / GraphRAG / Python Provider 原型
  引入 embedding、similar case retrieval、RAG EvidenceProvider、KG-lite / GraphRAG 实验。

Phase 5-P0：平台化、模型服务化与治理
  引入 PostgreSQL、pgvector、React Console、Python AI Provider、Model Registry、Training Center、Audit。
```

---

# 十、系统最终闭环

完整闭环：

```text
标准知识 / 症状群资产 / 评估病例
  ↓
Runtime 执行
  ↓
RuntimeTrace / PatientOutput / ClinicianReport
  ↓
EvaluationRunner / Scorer
  ↓
EvaluationResult / SafetyViolation / RegressionFinding
  ↓
CapabilityProfileUpdateProposal
  ↓
ExperienceCandidate / TrainingExampleCandidate
  ↓
Review / Governance
  ↓
ExperienceUnitAsset / TrainingDatasetVersion / ModelProviderVersion
  ↓
Provider / Asset 更新候选
  ↓
再次 Evaluation
  ↓
通过后进入 Runtime 可用能力
```

---

# 十一、最终结论

ClinMindRuntime 的最终形态不是一个“会回答医学问题的模型”，而是一个受控医疗 AI Runtime 与能力治理平台。

它的核心路线是：

```text
先让 Runtime 站起来，
再让资产接进来，
再让评估管住它，
再让训练和经验慢慢长出来，
最后再做平台化治理。
```

模型训练、MCP、GraphRAG、Skills、Multi-Agent、前端平台、数据库持久化都必须服务这条主线，而不能反过来取代 Runtime 主控。
