# Phase 11 后架构缺口与路线收敛决策

> 文档性质：架构决策记录  
> 对应路线图：`ClinMindRuntime阶段拆分路线图.md` v3.0  
> 对应总设计：`ClinMindRuntime完整系统设计.md` v3.0  
> 对应技术蓝图：`ClinMindRuntime技术实现总方案.md` v3.0  
> 当前系统基线：Phase 1–11 P0 已冻结；Phase 11-P1 收口中  
> 决策目标：确定 Phase 11 之后的主线，在不删除完整设计范围的前提下阻止项目无边界横向扩张。

---

# 一、背景

ClinMindRuntime 已经覆盖 Runtime、Agent、RAG、KG-lite、Python Provider、模型治理、Tool / MCP / Skills 治理、Evaluation、Candidate、Persistence、Audit、Governance Console 和三角色前端。

这证明了“Runtime 主控、能力受控、结果可验证、输出可治理”的架构原则可以成立。

但代码和文档同时暴露出明显失衡：

```text
控制平面：
Runtime / Policy / Validation / Audit / Evaluation / Governance
相对完整

真实能力平面：
LLM Agent / Evidence / Embedding / Rerank / FHIR Tool / Clinical Workflow
仍以规则、Mock、YAML 或局部投影为主
```

如果继续优先增加 Multi-Agent、MCP、Skill Store、更多 Console 页面和生产级平台对象，项目会进一步扩大“治理外壳”，但无法增强真实临床价值和求职展示可信度。

---

# 二、核心判断

Phase 11 之后不应继续沿用“每个新技术单独增加一个 Phase”的无序横向扩张方式。

后续实现需要按依赖收敛：

```text
第一优先：真实临床证据与单场景纵切
第二优先：Runtime 执行语义最小统一
第三优先：患者事实与纵向状态
随后：事务动作、完整 Agent、知识、模型、工具、经验和生产治理
```

收敛只调整实现顺序，不删除完整系统设计中已经规划的能力。

完整设计仍包括：

```text
Agent / Workflow / Multi-Agent
Deep Evidence / KG-lite / GraphRAG
ModelProvider / Training / Post-training
Tool / MCP / Skills / External Integration
Clinical Experience Memory
Production Governance Platform
Voice / Realtime / Isolated Experiments
```

---

# 三、保留的架构原则

以下原则继续作为不可突破的系统边界：

1. Runtime 是唯一主控；
2. Agent 只能生成 Proposal / Draft / Finding；
3. RAG 只能生成 EvidenceCandidate / EvidenceRef；
4. Model 只能作为可替换、可评估、可降级的 Provider；
5. Tool / MCP / Skill 结果必须经过 Policy 和 Validation；
6. SafetyGate 和 DecisionBoundary 不由模型接管；
7. Patient View 与 Clinician View 必须在后端完成安全投影；
8. Candidate 不自动上线；
9. Trace、Audit、Evaluation 必须覆盖关键能力调用；
10. 外部数据只能作为数据，不能自动成为 Runtime 控制指令；
11. Patient Facts、Medical Knowledge、Runtime Evidence、System Experience 和 Agent Inference 必须分离；
12. 真实写操作必须经过 Action Proposal、Staged Execution 和 Commit / Rollback。

---

# 四、主要架构缺口

## 4.1 Evidence 仍然偏检索原型

当前 Evidence 能力已经具备 Candidate、Validation、EvidenceGraph、Trace 和 Evaluation，但真实临床价值仍受以下限制：

- 医学语料规模和来源有限；
- 缺少真实 embedding 和稳定 hybrid retrieval；
- 缺少证据资产版本；
- 缺少临床主张与证据片段的明确绑定；
- 缺少 citation entailment；
- 缺少 freshness、patient applicability 和 conflict 的独立判断；
- 医生端 Evidence Panel 尚未形成完整真实数据展示。

因此 EvidenceProvider 应升级为核心临床能力，而不是继续作为简单检索插件。

## 4.2 Runtime Policy 仍然分散

当前 Agent、Evidence、Provider、Tool、SafetyGate、DecisionBoundary 都有各自的 Policy 或 Validation。

这些模块本身合理，但随着真实能力增加，需要最小统一语义：

```text
ALLOW
DEGRADE
REVIEW_REQUIRED
BLOCK
```

同时需要明确：

- 外部数据的来源与可信度；
- 能力执行前授权；
- 能力执行后安全重评估；
- 失败后的恢复动作；
- 为什么允许、降级或阻断。

Phase 12 只建立最小统一对象；Phase 14 再建设正式 Policy IR、RuntimeRiskState 和 Capability Lease。

## 4.3 缺少患者事实与纵向状态域

当前 RuntimeState 适合保存一次会话的执行状态，EvidenceGraph 适合保存本轮决策证据，Experience / Candidate 适合保存系统改进经验。

这些对象都不适合承担完整患者纵向事实。

未来需要独立区分：

```text
患者事实
医学知识
本轮证据
系统经验
Agent 推断
```

完整 Clinical Fact Ledger、双时间、来源冲突和当前状态重建进入 Phase 13。

## 4.4 缺少真实能力失败后的恢复

当前系统重视 reject、degrade、fail-safe 和 halt，但真实 Provider、LLM、RAG、FHIR Tool 接入后，还必须支持：

```text
Rule fallback
Restricted retry
Ask clarification
Switch to read-only
Human review
Rollback to checkpoint
Safe halt
```

系统不能以“全部阻断”为安全目标，还需要衡量 false block rate 和恢复成功率。

## 4.5 缺少副作用动作的正式事务边界

只读 Tool 可以通过 Policy、Validation 和 Trace 受控接入。

一旦系统出现病历写入、报告提交、转诊、预约或其他外部副作用，就必须增加：

```text
RuntimeActionProposal
Authorization
Preconditions
Shadow / Staged Execution
Result Validation
Postconditions
Commit / Rollback / Compensation
Human Approval
Kill Switch
```

该能力进入 Phase 15，未完成前不得启用真实写 Tool。

---

# 五、路线选择

## 5.1 选择 A：继续横向扩展平台

包括：

- 更多 Agent；
- Multi-Agent / Handoff；
- 远程 MCP；
- Skill Store；
- 更多 Console；
- 生产级多租户；
- 自动训练与发布。

结论：不作为立即主线，但全部保留在 Phase 16–22 的完整路线中。

原因：这些能力不能解决当前真实 Agent、Evidence、FHIR 数据和量化评测不足的问题。

## 5.2 选择 B：优先建设完整 Clinical Fact Ledger

包括：

- Append-only Event Ledger；
- Bi-temporal Fact Ledger；
- Reconciliation；
- Current State Projection；
- 多资源适配器。

结论：保留为 Phase 13，不作为立即主线。

原因：架构价值高，但当前缺乏真实单次临床闭环，提前建设会继续扩大抽象层。

## 5.3 选择 C：真实临床证据纵切 + Runtime 最小收敛

包括：

- 真实公开医学资料；
- 真实 hybrid retrieval；
- Evidence Asset / Claim / Citation；
- LLM-backed InquiryPlanningAgent；
- 只读 FHIR Tool；
- CapabilityDecision；
- Provenance / Trust；
- Post-Capability SafetyGate；
- Recovery；
- 胸痛场景端到端 Evaluation。

结论：采用，作为 Phase 12 主线。

---

# 六、Phase 12 目标架构

```text
Patient Input / FHIR Data / External Evidence
↓
Data Provenance & Trust Classification
↓
RuntimeState / CaseFrame / RuntimeRisk Context
↓
Pre-Capability SafetyGate
↓
CapabilityDecision
↓
LLM Agent / Evidence Engine / Read-only FHIR Tool
↓
Structured Proposal / Evidence / Tool Result
↓
Runtime Validation
↓
Post-Capability SafetyGate
↓
Recovery / Controlled Adoption
↓
DecisionBoundary
↓
Patient / Clinician / Governance Projection
↓
Trace / Audit / Evaluation
```

Phase 13 再扩展：

```text
RawSourceEvent
↓
ClinicalDatum / ClinicalFact
↓
Temporal Reconciliation
↓
CurrentClinicalStateProjection
↓
Runtime Context
```

Phase 14 再扩展：

```text
Policy IR
RuntimeRiskState
CapabilityLease
Causal Trace
```

Phase 15 仅在出现真实写操作后扩展：

```text
RuntimeActionProposal
→ Shadow State
→ Preconditions
→ Staged Execution
→ Postconditions
→ Commit / Rollback
```

---

# 七、Phase 12 首个临床纵切

固定首个领域：

```text
胸痛 / 胸闷风险分层与追问
```

选择原因：

- 当前项目已有胸痛测试和 SafetyGate 基础；
- 能展示主动追问、危险信号、FHIR 病史、医学证据和角色化输出；
- 容易设计正常、高风险、缺失、冲突和故障注入场景；
- 范围足够小，能避免扩展为全病种系统。

必须打通：

```text
患者输入
→ LLM 追问 Proposal
→ Runtime 校验
→ FHIR 只读查询
→ Evidence Engine
→ Evidence Validation
→ Post-Safety
→ PatientOutput / ClinicianReport
→ Governance Trace
→ Evaluation
```

---

# 八、后续完整阶段

路线收敛后，完整系统仍按以下阶段实现：

```text
Phase 13
Clinical Data & Fact Plane

Phase 14
Unified Runtime Governance Kernel

Phase 15
Transactional Action Governance

Phase 16
Controlled Agent & Workflow Expansion

Phase 17
Advanced Knowledge & Evidence Platform

Phase 18
Model & Training Lifecycle

Phase 19
Tool / MCP / Skills & External Integration

Phase 20
Experience Memory & Continuous Improvement

Phase 21
Production Governance Platform

Phase 22
Advanced Interaction & Experimental Capabilities
```

这些阶段不是被删除，而是必须按依赖进入。

---

# 九、成功标准

路线收敛成功不能只通过文档数量判断，必须满足：

1. 主演示链路使用真实 LLM、真实检索和真实只读 FHIR 数据；
2. Mock 仅作为明确 fallback；
3. 医生端关键主张可追踪到来源、版本和 source span；
4. 外部数据不能改变 Runtime 控制流；
5. 能力执行后重新评估风险；
6. Provider 或 Tool 失败后可以安全恢复；
7. 患者端不泄露 DDx、Trace、原始证据和内部推理；
8. 有 task success、safety violation、citation entailment、recovery success、false block rate 等量化结果；
9. 三角色视图来自同一个真实 Runtime；
10. Phase 11-P1 和 Phase 12 各自具有明确冻结记录；
11. 完整系统设计、技术实现总方案和路线图中的对象可以互相追踪；
12. 后续完整能力有正式阶段归属，不形成设计孤岛。

---

# 十、v3.0 反向写回结果

本决策已经正式写回：

```text
ClinMindRuntime完整系统设计.md v3.0
ClinMindRuntime技术实现总方案.md v3.0
ClinMindRuntime阶段拆分路线图.md v3.0
```

写回内容包括：

```text
Clinical Evidence Engine
Clinical Data & Fact Plane
Unified Runtime Governance Kernel
Transactional Action Governance
Controlled Agent / Workflow
Advanced Knowledge / GraphRAG
Model & Training Lifecycle
Tool / MCP / Skills Integration
Experience Memory
Production Governance
Advanced Interaction / Experiments
```

三份上位文档现在分别承担：

```text
完整系统设计：定义系统是什么和完整能力边界。
阶段路线图：定义每项能力在哪个 Phase 实现。
技术实现总方案：定义模块、接口、数据、API、测试和部署如何实现。
```

---

# 十一、最终决策

ClinMindRuntime 已完成架构广度验证，下一阶段停止无边界横向扩张，但不删除完整设计范围。

采用以下执行路线：

```text
Phase 11-P1
收口真实 Runtime 角色投影

Phase 12
真实 Evidence + 真实 LLM Agent + 只读 FHIR Tool + Runtime 最小统一治理

Phase 13–15
患者事实、正式 Runtime Governance 和事务动作内核

Phase 16–20
Agent、Workflow、Knowledge、Model、Tool 和 Experience 完整生命周期

Phase 21
生产治理、审核、发布、运维与合规

Phase 22
隔离边界内的高级交互与技术实验
```

核心原则：

```text
完整设计必须全部进入规划，
但任何能力都不能抢在依赖条件之前实现；
路线调整的是顺序，不是系统愿景。
```
