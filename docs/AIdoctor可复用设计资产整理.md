# AIdoctor 旧项目可复用设计资产整理

> 来源项目：`cxjchelsea/AIdoctor`  
> 目标项目：`cxjchelsea/ClinMindRuntime`  
> 目的：从旧 AI 医生项目中抽取真正有工程价值、可迁移到 ClinMindRuntime 的设计资产，避免重做已有能力，并把旧项目从“多服务医疗诊断流程系统”升级为“受控诊断 Runtime”。

---

## 1. 总体判断

旧项目 `AIdoctor` 不应该被整体废弃。

它已经具备以下工程基础：

- CDP 临床决策包状态管理
- Java 编排服务 + Python AI 服务的混合架构
- 多服务拆分和 Docker Compose 部署
- 主动问诊与信息缺口识别
- 鉴别诊断候选集生成
- 多引擎融合诊断
- 三层候选诊断分层
- 证据链分析
- 检查建议、风险评估、解释生成
- 执行追踪与审计字段

但是旧项目的问题也很明显：

- 更偏固定 Workflow，而不是真正的 Runtime
- 信息收集更多是 slot filling，而不是围绕诊断证据动态决策
- 风险控制没有形成独立的 SafetyGate / DecisionBoundary
- 证据链更像解释层，还没有成为诊断控制层
- 多引擎融合以加权分数为主，缺少安全边界控制
- 治疗推理在患者端风险较高，不适合作为核心卖点
- 临床经验记忆、医生反馈、再认证机制尚未形成闭环

因此，新项目 `ClinMindRuntime` 的合理定位不是推翻旧项目，而是：

> 在 AIdoctor 的工程基础上，将原来的“多服务医疗问答 / 诊断流程系统”升级为“状态驱动、证据可追踪、输出受控、可复盘再认证的医疗诊断 Runtime”。

---

## 2. 旧项目中最值得保留的设计

### 2.1 CDP：保留并升级为 Runtime State

旧项目中的 CDP（Clinical Decision Package）是最重要的可复用资产。

它已经承担了一次问诊过程中的中心状态对象，包含：

- patientState：患者状态
- ddx：鉴别诊断列表
- evidenceGraph：证据图
- workupPlan：检查计划
- managementPlan：处置计划
- triage：风险评估
- uncertainty：不确定性信息
- audit：审计信息
- executionTrace：执行追踪
- cdpStatus：当前状态
- version：版本号

这些字段与 ClinMindRuntime 的核心对象高度对应。

建议映射如下：

| AIdoctor 旧字段 | ClinMindRuntime 新对象 | 迁移方式 |
|---|---|---|
| `patientState` | `CaseFrame` | 保留概念，字段更规范化 |
| `ddx` | `Differential Diagnosis Board` | 从普通候选列表升级为候选诊断状态板 |
| `evidenceGraph` | `EvidenceGraph` | 从解释型证据链升级为控制型证据状态图 |
| `triage` | `SafetyGate / DecisionBoundary` | 拆分为风险识别和输出边界控制 |
| `uncertainty` | `MissingEvidence / ConflictEvidence` | 显式记录缺失证据和冲突证据 |
| `audit` | `AuditLog` | 保留，增加安全边界和医生反馈记录 |
| `executionTrace` | `RuntimeTrace` | 保留，作为复盘和再认证依据 |
| `cdpStatus` | `RuntimeStatus` | 从流程状态升级为诊断运行状态 |

建议在新项目中将 CDP 改名为：

```text
DiagnosticRuntimeState
```

或者保留 CDP 名称，但明确说明：

> CDP 是 ClinMindRuntime 的一次诊断运行状态快照，不只是数据包，而是 Runtime 的状态内核。

---

### 2.2 Java 编排 + Python AI 服务：保留

旧项目采用：

- Spring Boot / Java：流程编排、状态管理、事务、接口聚合
- FastAPI / Python：LLM 调用、知识图谱推理、诊断引擎、主动问诊、OCR
- MySQL：结构化业务数据
- Redis：对话上下文缓存
- Neo4j：医学知识图谱
- Docker Compose：多服务部署

这个架构可以继续保留。

原因是医疗诊断系统本身就需要把“稳定业务 Runtime”和“AI 能力模块”分开：

```text
Java Orchestrator：负责稳定、可审计、可回滚、可编排
Python AI Services：负责 LLM、RAG、KG、模型推理、Prompt 策略
```

新项目中建议继续使用这种分层，但命名上做调整：

| 旧服务 | 新定位 |
|---|---|
| diagnosis-service | Runtime Orchestrator |
| diagnosis-engine-service | Diagnostic Reasoning Service |
| dialog-service | Question / Test Policy Service |
| risk-assessment-service | SafetyGate Service |
| workup-planner-service | Test Policy / Workup Service |
| explanation-service | Human-like Interaction / Report Service |
| treatment-engine-service | Clinician-only Management Reference Service |

---

### 2.3 五步诊断流程：保留骨架，但升级为 Runtime Loop

旧项目中的五步流程是：

```text
Step 1：识别问题
Step 2：构建鉴别诊断候选集并分层
Step 3：组织候选集并建立分流路径
Step 4：采集关键证据并形成验证计划
Step 5：回填证据并输出终点结论包
```

这个流程有价值，因为它说明旧项目已经不是简单问答，而是有诊断流程意识。

但它的问题是：

- 更像固定 Workflow
- 主要依赖信息完整度阈值推进流程
- 没有把高危排除、证据充分性、输出权限作为硬约束

在 ClinMindRuntime 中，应升级为 Runtime Loop：

```text
User Input
  ↓
Update CaseFrame
  ↓
SafetyGate
  ↓
Update Differential Diagnosis Board
  ↓
Update EvidenceGraph
  ↓
Question / Test Policy
  ↓
DecisionBoundary
  ↓
Human-like Interaction Layer
  ↓
RuntimeTrace / AuditLog
```

也就是说：

> 旧项目的五步流程可以作为流程骨架，但新项目的核心应该是每一轮都由 Runtime 状态决定下一步动作，而不是简单按步骤推进。

---

### 2.4 主动问诊与信息缺口识别：保留并升级

旧项目的 dialog-service 已经实现了：

- 信息缺口识别
- 信息缺口分级：required / important / optional
- 完整度计算
- 智能追问生成
- NLU 理解用户回答
- NLG 生成自然语言问题
- 对话上下文缓存
- WebSocket 实时对话

这部分应该保留。

但旧逻辑更偏：

```text
缺字段 → 问字段
```

例如：

- 缺症状诱因 → 问诱因
- 缺症状持续时间 → 问持续时间
- 缺症状严重程度 → 问严重程度

在 ClinMindRuntime 中，应升级为：

```text
候选诊断 / 高危排除 / 缺失证据 → 决定下一步追问或检查建议
```

新的 Question / Test Policy 应该考虑：

- 当前症状群
- 当前候选诊断
- 必须排除的高危疾病
- 支持证据
- 反对证据
- 缺失证据
- 经验记忆提醒
- 检查是否能改变判断
- 患者端是否允许展示

建议迁移后的模块命名：

```text
旧：InformationGapIdentifier
新：EvidenceGapIdentifier

旧：AdaptiveQuestioningStrategy
新：QuestionTestPolicy

旧：CompletenessCalculator
新：EvidenceSufficiencyEvaluator
```

---

### 2.5 多引擎融合诊断：保留，但降低决策权

旧项目中的 diagnosis-engine-service 有五引擎融合设计：

- Rule Engine
- Knowledge Graph Engine
- Statistical Model Engine
- LLM Engine
- Differential Engine

这个设计应该保留，因为它说明系统不是完全依赖大模型。

但旧项目的融合方式主要是：

```text
各引擎输出 disease possibilities
按固定权重加权融合
排序取 Top 5
```

这可以作为原型，但不应该作为最终临床判断机制。

在 ClinMindRuntime 中，建议改为：

```text
Rule Engine：负责红旗规则和硬安全边界
Knowledge Graph：负责疾病-症状-检查关系召回
RAG Evidence：负责指南和证据引用
LLM Engine：负责语义理解、证据归因、自然表达
Statistical Model：负责风险评分，可选
Differential Engine：负责候选诊断组织
DecisionBoundary：负责最终输出权限控制
```

核心原则：

> 多引擎可以提供候选和证据，但不能直接决定患者端输出。最终输出必须经过 SafetyGate 和 DecisionBoundary。

---

### 2.6 三层候选诊断分层：保留并扩展

旧项目中已经有三层候选诊断结构：

```text
primary_hypothesis：首要假设
main_alternatives：主要备选
must_exclude：必须排除
```

这个设计非常适合迁移到 ClinMindRuntime 的 Differential Diagnosis Board。

但需要升级：

旧版问题：

- 高危诊断依赖关键词识别
- must_exclude 只支持单个候选
- 候选状态不够丰富
- 缺少高危候选保留机制

新版建议：

```json
{
  "candidates": [
    {
      "name": "急性冠脉综合征",
      "risk_level": "high",
      "status": "must_not_miss",
      "supporting_evidence": [],
      "opposing_evidence": [],
      "missing_evidence": []
    },
    {
      "name": "胃食管反流",
      "risk_level": "medium",
      "status": "possible_after_exclusion"
    }
  ]
}
```

候选状态建议包括：

```text
primary_hypothesis
main_alternative
must_not_miss
need_to_rule_out
possible_after_exclusion
unlikely
insufficient_evidence
```

核心原则：

> 高危候选不能因为分数低就删除，只能标记为必须排除或需要排除。

---

### 2.7 证据链分析：保留并升级为 EvidenceGraph

旧项目中的 EvidenceAnalyzer 已经能为候选疾病构建：

- supporting_evidence
- opposing_evidence
- evidence_strength
- evidence_summary
- rule_engine 来源证据
- knowledge_graph 来源证据
- llm_engine 来源证据

这部分是 EvidenceGraph 的前身。

但旧版更像解释层：

```text
为什么这个疾病可能成立？
```

ClinMindRuntime 中的 EvidenceGraph 应该升级为控制层：

```text
当前证据是否足够？
还缺什么关键证据？
下一步应该问什么？
是否允许输出候选诊断？
是否需要转急诊或人工医生？
```

新版 EvidenceGraph 建议字段：

```json
{
  "diagnosis": "急性冠脉综合征",
  "supporting_evidence": [],
  "opposing_evidence": [],
  "missing_evidence": [],
  "conflicting_evidence": [],
  "experience_alerts": [],
  "recommended_questions": [],
  "recommended_tests": [],
  "status": "need_to_rule_out"
}
```

---

### 2.8 执行追踪与审计：保留并加强

旧项目中已经有：

- TraceExecution 注解
- executionTrace 字段
- audit 字段
- cdpId 追踪上下文

这部分非常重要，应完整保留。

在 ClinMindRuntime 中，应升级为 RuntimeTrace：

```text
每轮输入是什么
CaseFrame 更新了什么
SafetyGate 是否触发
DDx 如何变化
EvidenceGraph 如何变化
Question / Test Policy 为什么选择这个问题
DecisionBoundary 为什么限制输出
Human-like Layer 输出了什么
医生是否采纳
最终诊断 / 随访结局是什么
```

RuntimeTrace 是后续实现复盘、医生反馈、Shadow Learning、再认证的基础。

---

## 3. 可以保留但需要降级的设计

### 3.1 治疗推理服务

旧项目中有 treatment-engine-service 和 managementPlan。

这部分不建议作为患者端核心能力。

原因：

- 治疗建议、用药建议和处方属于高风险医疗行为
- 患者端输出容易产生误导
- 合规和责任边界更复杂

建议定位：

```text
患者端：不输出治疗方案，只输出就医建议、风险提示、检查准备、健康教育。
医生端：可以输出治疗方向参考，但必须标注为医生辅助信息。
```

迁移方式：

```text
旧 treatment-engine-service
→ Clinician-only Management Reference Service
```

---

### 3.2 OCR 和多模态输入

旧项目支持 OCR 识别医疗报告和检查单。

这部分可以保留为后续扩展能力，但不是 ClinMindRuntime 第一阶段核心。

第一阶段建议聚焦：

```text
文本主诉
多轮问诊
结构化病例状态
候选诊断
证据图
风险分诊
输出边界
```

OCR 可作为第二阶段：

```text
检查报告解析
化验单结构化
影像报告文本解析
心电图报告文本解析
```

---

## 4. 不建议继续使用的表达或机制

### 4.1 不建议继续使用“五脑思想”作为项目主表达

旧项目 README 中使用“五脑思想”描述架构。

这个说法容易显得包装化，面试中不如下面这些表达专业：

```text
状态驱动的诊断 Runtime
受控医疗 Agent 架构
证据状态驱动的诊断支持系统
多模块诊断编排系统
```

建议在新项目中不再使用“五脑思想”作为主概念。

---

### 4.2 不建议用固定完整度阈值决定诊断推进

旧项目中有基于完整度阈值推进诊断流程的设计。

这在工程原型中可以使用，但医疗场景里不能作为核心判断。

应改为：

```text
普通信息完整度：辅助指标
关键证据完整度：核心指标
高危疾病排除状态：硬约束
DecisionBoundary：最终输出权限
```

例如：

```text
即使普通字段完整度达到 80%，如果胸痛高危证据没有排除，患者端也不能输出低风险判断。
```

---

### 4.3 服务失败后不能一律继续流程

旧项目中有多处“服务调用失败后继续流程”的容错设计。

这在普通业务系统中合理，但医疗场景要区分安全等级。

建议改为：

```text
非关键服务失败：允许降级
表达层失败：允许返回结构化结果
LLM 失败：允许规则兜底
RAG 失败：允许提示证据不足
SafetyGate 失败：必须 fail-safe
RiskAssessment 失败：必须 fail-safe
DecisionBoundary 失败：必须 fail-safe
EvidenceGraph 构建失败：不得输出诊断方向
```

核心原则：

> 医疗安全模块失败时，系统应该更保守，而不是继续正常输出。

---

## 5. 新项目建议目录设计

建议 ClinMindRuntime 初始目录如下：

```text
ClinMindRuntime/
├── README.md
├── docs/
│   ├── AIdoctor可复用设计资产整理.md
│   ├── 系统总体设计.md
│   ├── Runtime核心对象设计.md
│   ├── SafetyGate与DecisionBoundary设计.md
│   ├── EvidenceGraph设计.md
│   ├── QuestionTestPolicy设计.md
│   ├── ClinicalExperienceMemory设计.md
│   └── MVP实现边界.md
├── runtime-orchestrator/
├── diagnosis-reasoning-service/
├── question-policy-service/
├── safety-gate-service/
├── evidence-service/
├── interaction-service/
├── evaluation-service/
└── docker-compose.yml
```

第一阶段不建议直接完整复刻旧项目所有服务。

更合理的 MVP 是：

```text
1. Runtime Orchestrator
2. CaseFrame
3. SafetyGate
4. Differential Diagnosis Board
5. EvidenceGraph
6. Question / Test Policy
7. DecisionBoundary
8. Patient-facing / Clinician-facing 输出区分
9. RuntimeTrace
10. 小型病例评估集
```

---

## 6. 迁移优先级

### P0：必须优先迁移

```text
CDP 状态中心 → DiagnosticRuntimeState
五步流程骨架 → Runtime Loop
信息缺口识别 → EvidenceGapIdentifier
主动追问 → QuestionTestPolicy
三层候选诊断 → DifferentialDiagnosisBoard
证据链 → EvidenceGraph
风险评估 → SafetyGate / DecisionBoundary
执行追踪 → RuntimeTrace
```

### P1：可以第二阶段迁移

```text
多引擎融合诊断
知识图谱推理
RAG Evidence Library
医生端报告
Docker Compose 多服务部署
评估服务
```

### P2：后续扩展

```text
OCR
多模态检查报告输入
治疗方案参考
医生反馈闭环
Silent Evaluation
Clinical Experience Memory
Shadow Learning
Review & Recertification
```

---

## 7. 面试表述建议

可以这样讲旧项目和新项目的关系：

> 我原来的 AIdoctor 项目已经实现了 CDP 状态管理、多服务编排、主动问诊、多引擎诊断、证据分析和执行追踪。后来我复盘发现，旧项目虽然能跑通问诊流程，但整体还是偏固定 Workflow，信息收集也更像 slot filling，缺少明确的 SafetyGate、DecisionBoundary 和经验治理机制。  
> 所以 ClinMindRuntime 不是推翻旧项目，而是在原有工程基础上，把它升级为状态驱动、证据可追踪、输出受控、可复盘再认证的诊断 Runtime。

如果面试官问“你这个项目最大的演进是什么”，可以回答：

> 最大的演进是从功能服务集合，升级为诊断 Runtime。旧项目关注“有哪些工具服务”，新项目关注“一次问诊中系统处于什么诊断状态、有哪些候选诊断、证据是否充分、下一步该问什么、当前是否允许输出”。

如果面试官问“为什么不直接用大模型回答”，可以回答：

> 医疗场景不能把最终控制权交给大模型。大模型可以做语言理解、候选生成和解释表达，但高危识别、输出权限、患者端边界和医生端报告必须由 Runtime 控制。

---

## 8. 最终结论

AIdoctor 中最值得保留的不是某个具体服务，而是以下工程资产：

```text
中心状态对象 CDP
多服务编排经验
主动问诊机制
候选诊断分层
多引擎诊断思想
证据链分析
执行追踪和审计意识
Java + Python 混合架构
```

ClinMindRuntime 的核心任务是把这些资产升级为：

```text
DiagnosticRuntimeState
SafetyGate
DifferentialDiagnosisBoard
EvidenceGraph
QuestionTestPolicy
DecisionBoundary
RuntimeTrace
ClinicalExperienceMemory
ReviewAndRecertification
```

一句话总结：

> AIdoctor 是旧的工程底座，ClinMindRuntime 是新的诊断 Runtime 架构升级。旧项目负责证明你做过完整工程，新项目负责证明你已经从“做功能”进化到“设计医疗 AI 运行时”。
