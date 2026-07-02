# ClinMindRuntime 阶段拆分路线图

> 上位总设计：`docs/ClinMindRuntime完整系统设计.md`  
> 文档地图：`docs/00_项目设计地图.md`  
> 当前总设计版本：v2.2  
> 当前项目定位：受控医疗 AI Agent Runtime 与能力治理平台  
> 当前状态：Phase 1–5 已冻结；下一阶段建议进入 Phase 6-P0：受控 Agent 执行层 MVP。

> 本文档是 `ClinMindRuntime完整系统设计.md` 的阶段落地路线图。  
> 总设计定义系统定位、八个能力域、五层架构和统一 Runtime 主链路；本文档负责把完整愿景拆分为可迭代实现阶段，并明确每个阶段的目标、范围、产出物、验证方式和禁止边界。

---

# 一、路线图定位

ClinMindRuntime 的完整愿景不是普通医疗 RAG，也不是自由自治式医疗 Agent，而是：

```text
受控医疗 AI Agent Runtime 与能力治理平台。
```

它的长期目标是让 Agent、RAG / GraphRAG、Model Provider、Tool / MCP / Skills 等能力进入一条统一 Runtime 主链路，并受到 RuntimeState、SafetyGate、Runtime Validation、DecisionBoundary、RuntimeTrace、Evaluation、Audit 和 Governance 的约束。

统一主线：

```text
先验证 Runtime 主控是否成立，
再接入共享能力资产，
再建立 Evaluation 闭环，
再沉淀 Candidate 和治理记录，
再建立 Persistence / Audit / Console 治理底座，
再引入受控 Agent，
再引入 RAG / GraphRAG，
再引入 ModelProvider / 训练治理，
再引入 Tool / MCP / Skills，
最后进入生产级平台治理。
```

阶段拆分不是按照页面功能拆，而是按照系统能力成熟度拆。

---

# 二、命名说明

```text
Phase 1 / Phase 2 / Phase 3 ...
= 项目能力阶段。

PhaseX-P0 / PhaseX-P1 / PhaseX-P2
= 某一阶段内部的优先级或子阶段。
```

命名原则：

```text
P0：证明该阶段核心链路成立。
P1：扩展、加固、治理增强。
P2：可视化、平台化、体验增强或后置补强。
```

---

# 三、总体阶段路线

| 阶段 | 名称 | 核心目标 | 当前状态 |
|---|---|---|---|
| Phase 0 | 设计与骨架 | 建立项目定位、代码骨架和核心文档 | 已完成 |
| Phase 1-P0 | Runtime MVP | 验证 Runtime 主控、状态、安全门、输出边界、Trace | 已完成 |
| Phase 2-P0 | 共享能力资产原型 | 建立 AssetPackage、Provider 接口、CapabilityProfile 原型 | 已完成 |
| Phase 3-P0 | Evaluation 闭环 | 建立病例集、Scorer、EvaluationResult、CapabilityProfile Proposal | 已冻结 |
| Phase 4-P0 | Candidate 生成机制 | 从 Evaluation / Trace 生成 ExperienceCandidate / TrainingExampleCandidate | 已冻结 |
| Phase 4-P1 | Candidate 治理与安全加固 | 脱敏、SourceRef 强校验、Review 记录 | 已冻结 |
| Phase 5-P0 | Persistence / Audit 治理底座 | PostgreSQL、Repository 双实现、AuditLog | 已冻结 |
| Phase 5-P1 | 最小 Console API / Access Governance | RBAC-lite、Safe DTO、Audit Center、Console API | 已冻结 |
| Phase 5-P2 | 最小前端 Console MVP | `console-web/` Runtime / Evaluation / Candidate / Review / Audit 五页 | 已冻结 |
| Phase 6-P0 | 受控 Agent 执行层 MVP | AgentRuntime、AgentPolicy、AgentProposal、InquiryPlanningAgent | 下一阶段 |
| Phase 6-P1 | Agent 能力扩展 | EvidenceOrganizationAgent、PatientRewriteAgent、TraceReviewAgent 等 | 后置 |
| Phase 7-P0 | RAG EvidenceProvider MVP | RAG 返回 EvidenceCandidate / EvidenceRef，并进入 EvidenceGraph | 后置 |
| Phase 7-P1 | KG-lite / GraphRAG 原型 | 轻量图关系和 GraphRAG 证据增强 | 后置 |
| Phase 8-P0 | Python AI Provider / ModelProvider MVP | 模型能力 Provider 化，返回结构化 Draft / Candidate | 后置 |
| Phase 8-P1 | 模型训练与后训练治理 | TrainingDatasetVersion、ModelProviderVersion、ModelRegistry | 后置 |
| Phase 9-P0 | Tool / MCP / Skills 受控接入 | ToolAccessPolicy、McpAdapter、SkillMetadata、ToolExecutionTrace | 后置 |
| Phase 10-P0 | 生产级平台治理 | 正式登录、多租户、RBAC、医生审核平台、发布回滚和运维 | 后置 |

---

# 四、已完成阶段归档

## Phase 1-P0：Runtime MVP

目标：

```text
证明医疗 AI 系统的主控不应是 LLM，而应是 Runtime。
```

核心产出：

```text
RuntimeService
RuntimeState
RuntimeStatus
EntryAssessment
CaseFrame
SafetyGate
DecisionBoundary
PatientOutput / ClinicianReport
RuntimeTrace
```

完成标准：

```text
1. 患者输入能进入 Runtime 实例。
2. 高风险输入能触发 SafetyGate。
3. 患者端不会看到 DDx / EvidenceGraph 等医生端信息。
4. RuntimeTrace 能记录主要执行步骤。
```

状态：已完成。

## Phase 2-P0：共享能力资产原型

目标：

```text
证明知识、规则、能力边界和经验不应散落在代码或 Prompt 中，而应作为版本化能力资产被 Runtime 调用。
```

核心产出：

```text
AssetPackage
AssetMetadata
CapabilityProfile
Provider Interface
YAML Asset Provider
assets-used debug trace
```

状态：已完成。

## Phase 3-P0：Evaluation 闭环

目标：

```text
证明 Runtime 能力必须通过病例集和 Scorer 评估，而不是靠 Demo 观感判断。
```

核心产出：

```text
EvaluationCaseSet
RuntimeEvaluationRunner
EvaluationScorer
EvaluationResult
MetricResult
SafetyViolation
RegressionFinding
CapabilityProfileUpdateProposal
```

状态：已冻结。

## Phase 4-P0 / P1：Candidate 生成与治理

目标：

```text
让评估暴露的问题、运行轨迹和失败样例先沉淀为候选，而不是自动上线为经验或训练数据。
```

核心产出：

```text
ExperienceCandidate
TrainingExampleCandidate
CandidateGenerationService
CandidateSanitizer
CandidateSourceRefFactory
CandidateSourceRefValidator
CandidateReviewService
CandidateReviewRecord
```

状态：已冻结。

## Phase 5-P0 / P1 / P2：Persistence / Audit / Console 治理底座

目标：

```text
让 Runtime、Evaluation、Candidate、Review、Audit 等治理对象可持久化、可审计、可通过安全 Console 查询和复盘。
```

核心产出：

```text
PostgreSQL 持久化
Repository 双实现
AuditLog
Persistence health / Audit API
ActorContext
RBAC-lite
AccessPolicy
Safe DTO
Console API
console-web/ 最小前端 Console MVP
```

状态：已冻结。

---

# 五、Phase 6：受控 Agent 执行层

## Phase 6-P0：受控 Agent 执行层 MVP

目标：

```text
证明 Agent 可以作为 Runtime 授权下的受控执行单元，生成可校验、可拒绝、可追踪的 Proposal，并进入 RuntimeTrace / Evaluation / Audit 闭环。
```

推荐首个 Agent：

```text
InquiryPlanningAgent
```

核心对象：

```text
AgentRegistry
AgentRuntime
AgentPolicy
AgentContext
AgentExecutionRequest
AgentExecutionResult
AgentProposal
AgentProposalValidator
AgentTrace
AgentEvaluationHook
Capability Orchestration
Runtime Validation
InquiryPlanningAgent
```

目标链路：

```text
RuntimeState / CaseFrame
→ Capability Orchestration
→ AgentPolicy
→ AgentRuntime
→ InquiryPlanningAgent
→ InquiryPlanProposal
→ AgentProposalValidator
→ Runtime 采纳 / 部分采纳 / 拒绝 / 降级
→ RuntimeTrace / AuditLog / Evaluation
```

允许做：

```text
1. Agent 抽象接口。
2. 受控 Agent 执行上下文。
3. InquiryPlanningAgent 的 mock / rule-based 实现。
4. AgentProposal 校验与拒绝原因。
5. AgentTrace 进入 RuntimeTrace。
6. Agent 结果进入 Evaluation。
7. Debug API 查看 Agent 执行过程。
```

禁止做：

```text
1. 自由自治式 Agent。
2. 多 Agent 协作。
3. LangGraph / Agent SDK 作为 Runtime 主控。
4. Agent 直接修改 RuntimeState。
5. Agent 直接输出 PatientOutput。
6. Agent 直接决定 SafetyGate / DecisionBoundary。
7. Agent 直接生成最终诊断。
8. RAG / MCP / 模型训练顺手接入。
```

完成标准：

```text
1. Runtime 能在受控节点调用 InquiryPlanningAgent。
2. Agent 只能返回 InquiryPlanProposal。
3. Runtime 能校验并采纳 / 拒绝 / 降级 Agent Proposal。
4. Agent 执行过程进入 RuntimeTrace / AuditLog。
5. 有确定性测试证明 Agent 不能越权。
6. 有 Evaluation 指标衡量追问 Proposal 是否覆盖缺失信息和红旗信息。
```

## Phase 6-P1：Agent 能力扩展

候选 Agent：

```text
EvidenceOrganizationAgent
DdxReasoningDraftAgent
PatientRewriteAgent
TraceReviewAgent
ExperienceCandidateMiningAgent
LlmJudgeAgent
```

原则：

```text
每个 Agent 都只能生成 Proposal / Draft / Finding / ScoreDraft。
每个 Agent 都必须有 Policy、Validator、Trace 和 Evaluation。
```

---

# 六、Phase 7：RAG / KG-lite / GraphRAG

## Phase 7-P0：RAG EvidenceProvider MVP

目标：

```text
让 RAG 成为 EvidenceProvider，而不是患者端回答器。
```

核心对象：

```text
EvidenceProvider
EvidenceRetrievalRequest
EvidenceRetrievalResult
EvidenceCandidate
EvidenceRef
EvidenceProviderPolicy
EvidenceValidationService
EvidenceScorer
```

链路：

```text
RuntimeState / CaseFrame
→ KnowledgeContextService
→ EvidenceProvider
→ RAG retrieval
→ EvidenceCandidate / EvidenceRef
→ Runtime Validation
→ EvidenceGraph
→ DecisionBoundary
```

禁止：

```text
RAG 直接回答患者。
RAG 检索结果直接拼 Prompt 输出。
RAG 绕过 EvidenceGraph。
```

## Phase 7-P1：KG-lite / GraphRAG 原型

目标：

```text
让图关系增强证据组织和鉴别诊断解释，而不是替代 Runtime 主控。
```

优先实现：

```text
PostgreSQL node / edge 表
KG-lite evidence relation
GraphRAGProvider prototype
EvidenceGraph enhancement
```

后置：

```text
Neo4j
Milvus / Qdrant
复杂图谱平台
```

---

# 七、Phase 8：ModelProvider 与模型训练治理

## Phase 8-P0：Python AI Provider / ModelProvider MVP

目标：

```text
让模型能力作为可替换、可评估、可回滚的 Provider 接入 Runtime。
```

候选 Provider：

```text
IntentClassifierProvider
CaseFrameExtractorProvider
RiskSignalClassifierProvider
PatientSafeRewriteProvider
LlmJudgeProvider
EmbeddingProvider
EvidenceRerankerProvider
```

原则：

```text
模型只返回结构化 Draft / Candidate / EvidenceRef / ScoreDraft。
Runtime 决定是否采纳。
```

## Phase 8-P1：模型训练与后训练治理

核心对象：

```text
TrainingExampleCandidate
TrainingDatasetVersion
ModelProviderVersion
ModelProviderMetadata
ModelRegistry
ModelEvaluationResult
ModelRollbackPlan
```

禁止：

```text
模型训练后自动扩大患者端输出权限。
未审核 RuntimeTrace 自动进入训练集。
LLM-as-a-Judge 作为唯一安全评估依据。
```

---

# 八、Phase 9：Tool / MCP / Skills 受控接入

目标：

```text
让外部工具、MCP Server 和 Skills 成为 Runtime 可授权、可审计、可降级的外部能力，而不是绕过 Runtime 的执行入口。
```

核心对象：

```text
ToolAccessPolicy
ToolExecutionRequest
ToolExecutionResult
ToolExecutionTrace
McpAdapter
McpServerMetadata
SkillMetadata
SkillExecutionPolicy
SkillProvider
```

链路：

```text
Runtime / Agent / Provider
→ ToolAccessPolicy
→ Tool / MCP / Skill Adapter
→ Structured ToolResult
→ Runtime Validation
→ Runtime 采纳 / 拒绝 / 降级
→ RuntimeTrace / AuditLog
```

禁止：

```text
MCP Server 直接修改 RuntimeState。
Tool Result 直接进入 PatientOutput。
Skill 自动扩大 CapabilityProfile。
```

---

# 九、Phase 10：生产级平台治理

目标：

```text
从工程原型走向生产级治理平台。
```

候选能力：

```text
正式登录 / OAuth / SSO
多租户
生产级 RBAC / ABAC
正式医生审核平台
资产发布 / 回滚工作流
模型灰度 / 回滚 / 漂移监控
Knowledge Console
Training Center
Docker Compose / 部署脚本 / 运维监控
OpenTelemetry / Prometheus / Grafana
```

原则：

```text
生产级治理必须建立在 Runtime、Evaluation、Audit、AccessPolicy、Safe DTO 已经稳定的基础上。
```

---

# 十、当前不应做什么

在 Phase 6-P0 规格未建立前，不应直接实现：

```text
1. 自由自治式 Agent。
2. 多 Agent 协作。
3. RAG / GraphRAG 真实接入。
4. Python AI Provider。
5. MCP / Tool / Skills。
6. 模型训练 / 后训练。
7. 正式登录 / 多租户 / 生产 RBAC。
8. 正式医生审核平台。
9. TrainingDatasetVersion 发布。
10. ApprovedExperience 自动上线。
```

---

# 十一、当前最优下一步

当前最优顺序：

```text
1. 更新 docs/ClinMindRuntime技术实现总方案.md。
2. 新增 docs/Phase6_P0受控Agent执行层_实现规格.md。
3. 新增 docs/Phase6_P0Agent_API与测试设计.md。
4. 新增 docs/Phase6_P0开发任务清单.md。
5. 更新 docs/AI_IMPLEMENTATION_SKILL.md，将 Phase6_P0 从“设计准备”推进为“可实现但受限”。
```

---

# 十二、最终结论

ClinMindRuntime 的阶段路线已经从早期 Runtime-first 治理主干，升级为：

```text
Runtime 主控
→ Asset
→ Evaluation
→ Candidate
→ Persistence / Audit / Console
→ Controlled Agent
→ RAG / GraphRAG
→ ModelProvider / Training Governance
→ Tool / MCP / Skills
→ Production Governance
```

后续所有实现都必须沿着这条路线推进，不能让 Agent、RAG、模型或工具反过来替代 Runtime 主控。
