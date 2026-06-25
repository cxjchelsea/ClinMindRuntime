# ClinMindRuntime 完整系统设计

> 项目名称：ClinMindRuntime  
> 中文定位：企业级智能诊断训练—运行—经验进化平台  
> 核心定义：面向医疗问诊场景的受控诊断 Runtime。它不是普通医疗问答系统，也不是替代医生的自主诊断系统，而是一个能训练、能运行、能积累经验、能复盘再认证的企业级智能诊断支持平台。

---

# 一、项目定位

ClinMindRuntime 是一个企业级智能诊断训练—运行—经验进化平台。

它不是普通医疗问答系统，也不是直接替代医生的全科 AI 医生，而是一个面向多症状群、多角色、多知识源、多评估闭环的智能诊断支持系统。

系统的核心目标是：

> 让 AI 不只是调用医学知识回答问题，而是像受训医生一样，通过标准知识学习、病例训练、真实问诊、医生反馈、随访结局和经验复盘，逐步积累临床经验，并在安全边界内完成更自然、更动态、更接近医生思维的诊断推理。

系统不是让大模型自由生成医学答案，而是让它在一个受控的诊断运行时中工作：

```text
它知道自己在哪些症状群中具备能力。
它知道当前病例有哪些候选诊断。
它知道哪些证据支持或反对某个方向。
它知道下一步应该追问什么或建议什么检查。
它知道当前患者端和医生端分别允许输出什么。
它知道什么时候不能继续在线判断，必须建议就医或转医生。
它知道后续如何从医生反馈、最终诊断和随访结局中复盘。
```

系统角色定位：

```text
患者端：风险提示、信息补全、就医建议、健康教育、检查准备说明
医生端：候选诊断、证据状态、相似经验提醒、检查建议、医生摘要
后台端：病例复盘、错误归因、经验候选发现、再认证评估、审计治理
```

---

# 二、核心设计：五个能力域

## 2.1 从业务主线到能力域

系统的业务主线可以概括为：

```text
1. 训练：通过症状群 Rotation 和病例考试获得能力
2. 经验：通过 Clinical Experience Memory 积累临床经验
3. 运行：通过 Diagnostic Runtime 管理一次问诊推理
4. 表达：通过 Human-like Interaction Layer 让沟通更像医生
5. 复盘：通过医生反馈、随访结局和再认证持续进化
```

对应到系统能力层，可以抽象为五个核心能力域：

```text
医学知识域
临床经验域
诊断状态域
输出边界域
复盘进化域
```

核心公式：

```text
智能诊断 = 医学知识 + 临床经验 + 诊断状态 + 输出边界 + 复盘进化
```

这五项不是系统模块，而是 ClinMindRuntime 的五个核心能力域。它们说明系统为什么不是普通 RAG 问答，而是一个训练—运行—经验进化平台。真正的系统模块划分见第五章。

能力域回答的是“系统具备什么能力”，模块回答的是“这些能力如何工程落地”。两者不是一一对应关系，而是多对多关系。

```text
能力域 = 设计逻辑
模块 = 工程实现
RuntimeState = 五个能力域在一次问诊中的状态承载
RuntimeTrace = 五个能力域如何影响判断的审计记录
Evaluation = 五个能力域是否真正有效的验证方式
```

## 2.2 五个能力域分别指什么

医学知识域指系统掌握的相对稳定的医学事实、指南规则、疾病关系、危险信号和检查依据。它回答“系统知道什么医学知识”。主要支撑组件包括 Clinical Pathway、KG-lite、RAG Evidence Library、Red Flag Rules、Test Recommendation Rules。

临床经验域指系统从真实病例、医生反馈、误诊教训、相似病例和复盘中沉淀出的经验性提醒。它回答“系统参考什么临床经验”。主要支撑组件包括 Clinical Experience Memory、Experience Retriever、Shadow Learning、Experience Memory Governance、Doctor Feedback、Follow-up Outcome。

诊断状态域指一次问诊当前处于什么状态，已经知道什么，还缺什么，哪些候选诊断成立或需要排除。它回答“当前病例处于什么诊断状态”。主要支撑组件包括 RuntimeState、CaseFrame、Differential Diagnosis Board、EvidenceGraph、Question / Test Policy、RuntimeTrace。

输出边界域指系统当前能不能说、能说到什么程度、患者端和医生端分别能看到什么。它回答“当前允许输出什么”。主要支撑组件包括 Capability Profile、SafetyGate、DecisionBoundary、FailurePolicy、Role & Permission。

复盘进化域指系统如何从真实运行、医生反馈、最终诊断、随访结局和评估结果中成长。它回答“系统如何从反馈中进化”。主要支撑组件包括 Doctor Feedback、Follow-up Outcome、Evaluation System、Review & Recertification、Capability Profile Update、Audit & Governance。

## 2.3 能力域与模块的关系

五个能力域不是五个独立服务，而是系统能力的上层抽象。平台管理层、共享能力资产层和 Runtime 执行层中的具体模块，是这些能力域的工程落地方式。

一个能力域通常由多个模块共同支撑，一个模块也可能同时服务多个能力域。

| 能力域 | 主要模块 / 资产 | 被哪些模块调用或影响 |
|---|---|---|
| 医学知识域 | Clinical Pathway、KG-lite、RAG Evidence Library、Red Flag Rules | Knowledge Context、SafetyGate、DDx Board、EvidenceGraph、Question/Test Policy |
| 临床经验域 | Clinical Experience Memory、Shadow Learning、Experience Governance | Experience Context、SafetyGate、EvidenceGraph、Question/Test Policy、DecisionBoundary |
| 诊断状态域 | RuntimeState、CaseFrame、DDx Board、EvidenceGraph、Question/Test Policy、RuntimeTrace | 整个 Runtime 主流程 |
| 输出边界域 | Capability Profile、SafetyGate、DecisionBoundary、FailurePolicy、Role & Permission | Patient-facing、Clinician Copilot、Runtime API |
| 复盘进化域 | Doctor Feedback、Follow-up Outcome、Evaluation、Recertification、Audit | Experience Memory、Capability Profile、Evaluation Center |

典型跨域模块：

```text
SafetyGate：主要属于输出边界域，但依赖医学知识域中的红旗规则，也会参考临床经验域中的高危提醒。
EvidenceGraph：主要属于诊断状态域，但会引用医学知识域的证据，也会接收临床经验域的经验提醒。
Capability Profile：主要属于输出边界域，但由复盘进化域中的评估和再认证结果更新。
Question / Test Policy：主要属于诊断状态域，但会同时参考医学知识、临床经验和输出边界。
RuntimeTrace：主要服务诊断状态域，但会记录五个能力域如何共同影响一次判断。
```

---

# 三、系统要解决的问题

普通医疗大模型问诊不是完全没有医学知识，而是缺少稳定的诊断机制。

主要问题包括：

```text
1. 多轮问诊状态容易丢失
2. 追问容易机械化，缺少诊断目的
3. 候选诊断和证据关系不可追踪
4. 容易在信息不足时过度下结论
5. 高危低概率疾病容易被忽略
6. 不知道自己在哪些场景有能力、哪些场景没有能力
7. 缺少真实病例经验沉淀机制
8. 出错后无法像医生一样复盘和成长
9. 患者沟通容易模板化，要么过度自信，要么过度保守
10. 患者端输出边界不清，容易越权
11. 医生端缺少可审计的证据链和推理路径
```

本系统要解决的是：

> 如何让 AI 在明确能力范围内，像医生一样围绕诊断目标收集证据，像医生一样参考相似病例经验，像医生一样从错误和随访结果中成长，并且知道什么时候可以判断、什么时候必须转医生。

---

# 四、总体设计原则

## 4.1 状态优先

系统不依赖对话历史隐式记忆，而是将一次问诊过程显式维护为 RuntimeState。

```text
对话历史只用于语言连续性。
诊断判断必须基于 RuntimeState。
```

## 4.2 安全优先

SafetyGate 和 DecisionBoundary 优先于自然语言生成。

```text
不是先生成答案再加免责声明，
而是先判断当前是否允许输出。
```

## 4.3 证据优先

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

## 4.4 高危保留

高危候选不能因为置信度低就删除。

```text
高危低概率疾病必须保留为 must_not_miss 或 need_to_rule_out。
```

## 4.5 经验受控

Clinical Experience Memory 只能让系统更警觉，不能让系统更武断。

```text
经验可以影响追问优先级、检查建议和输出边界收紧，
不能直接决定最终诊断。
```

## 4.6 人机分层

患者端和医生端必须有不同输出边界。

```text
患者端：风险提示、就医建议、需要补充的信息、健康教育
医生端：候选诊断、证据图、相似经验提醒、检查建议、诊断摘要
```

## 4.7 可追踪、可复盘、可回滚

每一次问诊都必须记录：

```text
输入是什么
医学知识使用了什么
临床经验触发了什么
CaseFrame 如何变化
SafetyGate 是否触发
DDx 如何变化
EvidenceGraph 如何变化
为什么问这个问题
为什么输出被限制
医生是否采纳
最终结局是什么
```

---

# 五、总体架构

## 5.1 三层结构

ClinMindRuntime 不是简单的“平台层 + Runtime 层”两层结构，而是由三类对象共同组成：

```text
1. 平台管理层：负责训练、治理、评估、权限和审计。
2. 共享能力资产层：由平台管理层产生、维护或治理，被 Runtime 执行层调用。
3. Runtime 执行层：负责一次问诊当下从输入、状态更新、证据推理到安全输出的执行过程。
```

三层关系：

```text
平台管理层
  ↓ 训练 / 审核 / 评估 / 再认证 / 治理
共享能力资产层
  ↓ 被检索 / 被读取 / 被引用 / 被约束
Runtime 执行层
  ↓ 产生 RuntimeTrace / 医生反馈 / 随访结局
平台管理层继续复盘和再认证
```

### 5.1.1 平台管理层

平台管理层负责管理、治理、评估和审计。

```text
1. Training Center：训练中心
2. Runtime Console：运行控制台
3. Experience Memory Center：经验记忆中心
4. Evaluation & Recertification Center：评估与再认证中心
5. Audit & Governance Center：审计与治理中心
6. Role & Permission：角色与权限管理
```

### 5.1.2 共享能力资产层

共享能力资产层不是一次问诊执行模块，而是平台训练、治理、评估或再认证之后形成的能力资产。Runtime 执行层在运行时读取、检索或引用这些资产。

```text
1. Symptom Rotation Library：症状群训练库
2. Clinical Pathway：临床路径
3. KG-lite：轻量医学知识图谱
4. RAG Evidence Library：医学证据库
5. Red Flag Rules：危险信号规则库
6. Test Recommendation Rules：检查建议规则库
7. Capability Profile：能力档案
8. Clinical Experience Memory：临床经验记忆
9. Experience Candidates：经验候选
10. Evaluation Results：评估结果
11. Recertification Records：再认证记录
12. Audit Logs：审计记录
```

### 5.1.3 Runtime 执行层

Runtime 执行层负责一次问诊当下如何运行。

```text
1. Runtime API
2. EntryAssessment
3. RuntimeState
4. RuntimeStatus
5. RuntimeTrace
6. Short-term Context
7. CaseFrame
8. Knowledge Context
9. Experience Context
10. SafetyGate
11. Differential Diagnosis Board
12. EvidenceGraph
13. Question / Test Policy
14. DecisionBoundary
15. Human-like Interaction Layer
16. FailurePolicy
```

注意：`Symptom Rotation Library`、`Capability Profile`、`Clinical Experience Memory` 等不是消失了，而是被放到共享能力资产层。Runtime 执行层会通过 `Knowledge Context`、`Experience Context`、`DecisionBoundary` 等模块读取和使用它们。

## 5.2 三层结构与能力域的关系

三层结构和五个能力域不是同一套划分。

```text
五个能力域：解释系统具备哪些核心能力。
三层结构：解释这些能力如何被管理、沉淀和执行。
```

对应关系：

| 能力域 | 平台管理层如何支撑 | 共享能力资产层如何承载 | Runtime 执行层如何使用 |
|---|---|---|---|
| 医学知识域 | Training Center 管理症状群、路径、病例和规则 | Clinical Pathway、KG-lite、RAG Evidence、Red Flag Rules | Knowledge Context、SafetyGate、DDx Board、EvidenceGraph、Question/Test Policy 使用 |
| 临床经验域 | Experience Memory Center 管理审核、上线、回滚 | Clinical Experience Memory、Experience Candidates | Experience Context 检索，SafetyGate / EvidenceGraph / Question Policy 引用 |
| 诊断状态域 | Runtime Console 展示状态和追踪 | 无固定资产，主要是运行时状态 | RuntimeState、CaseFrame、DDx Board、EvidenceGraph、Question/Test Policy 执行 |
| 输出边界域 | Evaluation & Recertification Center 和 Role & Permission 管理授权 | Capability Profile、Recertification Records、Role Permissions | SafetyGate、DecisionBoundary、FailurePolicy 控制输出 |
| 复盘进化域 | Evaluation Center、Audit Center、Review 流程管理复盘 | Evaluation Results、Audit Logs、Follow-up Outcomes | RuntimeTrace、Doctor Feedback、Silent Evaluation 反向提供数据 |

## 5.3 Runtime 层如何调用共享能力资产

Runtime 执行层并不是孤立运行的。一次问诊执行时，它会读取共享能力资产层中的内容：

```text
Knowledge Context 读取：
- Symptom Rotation Library
- Clinical Pathway
- KG-lite
- RAG Evidence Library
- Red Flag Rules
- Test Recommendation Rules

Experience Context 读取：
- Clinical Experience Memory
- 已验证经验单元
- 相似病例经验
- 误诊教训

DecisionBoundary 读取：
- Capability Profile
- Recertification Records
- Role Permissions
- FailurePolicy 配置

RuntimeTrace 写回：
- Knowledge 使用记录
- Experience 触发记录
- SafetyGate 判断记录
- DecisionBoundary 输出限制记录
- 医生反馈和随访结局入口
```

因此，原来广义 Runtime 层里的部分模块，在新结构中被更准确地分为：

```text
运行时执行模块：直接参与本轮问诊执行，例如 CaseFrame、SafetyGate、EvidenceGraph。
共享能力资产：由平台产生或治理，被运行时调用，例如 Capability Profile、Clinical Experience Memory。
平台治理流程：不直接进入线上问诊执行，例如 Shadow Learning、Review & Recertification、Evaluation System。
```

## 5.4 总体运行链路

```text
用户 / 医生输入
  ↓
Runtime API 创建或继续一次 Runtime
  ↓
EntryAssessment 判断工作态
  ↓
Update CaseFrame
  ↓
识别症状群
  ↓
Knowledge Context 查询医学知识：Clinical Pathway / KG-lite / RAG Evidence / Red Flag Rules
  ↓
DecisionBoundary 读取 Capability Profile
  ↓
Experience Context 检索 Clinical Experience Memory 中的相似经验
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
Human-like Interaction Layer 生成患者端或医生端表达
  ↓
RuntimeTrace 记录五个能力域如何影响本轮判断
  ↓
医生反馈 / 最终诊断 / 随访结局
  ↓
Shadow Learning 发现经验候选
  ↓
Experience Memory Governance 审核治理
  ↓
Clinical Experience Memory 更新
  ↓
Review & Recertification 再认证
  ↓
Capability Profile 更新
```

---

# 六、平台化管理后台设计

本章说明平台管理层如何支撑训练、治理、评估和管理。平台层不直接参与每一轮问诊推理，但它决定 Runtime 能力从哪里来、经验如何治理、输出权限如何授权、系统表现如何被评估。

## 6.1 Training Center：训练中心

训练中心负责管理症状群 Rotation、标准病例、模拟患者、病例考试和能力授权。

核心功能：

```text
症状群管理
临床路径管理
标准病例管理
模拟患者生成
病例考试配置
考试结果分析
能力等级授权
训练版本管理
```

训练中心不是为了训练一个黑箱模型，而是为了决定系统在不同症状群上的运行权限。

数据流：

```text
Clinical Pathway / 标准病例
  ↓
Rotation 训练
  ↓
病例考试
  ↓
能力指标计算
  ↓
Capability Profile 更新
  ↓
Runtime 输出边界生效
```

## 6.2 Runtime Console：运行控制台

运行控制台用于查看一次问诊 Runtime 的当前状态。

核心功能：

```text
Runtime 列表
当前 RuntimeStatus
CaseFrame 查看
Knowledge Context 查看
Experience Context 查看
SafetyGate 触发记录
Differential Board 查看
EvidenceGraph 查看
Question / Test Policy 动作查看
DecisionBoundary 输出限制原因
Patient-facing 输出预览
Clinician Report 输出预览
RuntimeTrace 时间线
```

Runtime Console 用于医生端协作、调试、复盘和面试展示。

## 6.3 Experience Memory Center：经验记忆中心

经验记忆中心负责管理 Clinical Experience Memory。

核心功能：

```text
经验候选列表
医生审核
适用范围编辑
不适用范围编辑
证据来源标注
经验状态流转
版本管理
冲突检测
回归测试结果查看
上线 / 下线 / 回滚
```

经验状态：

```text
Candidate：经验候选，不能影响线上输出
Reviewed：医生已审核，但不能直接影响 Runtime
Validated：通过离线评估，可进入 Runtime
Deprecated：废弃或降权，不再影响 Runtime
```

## 6.4 Evaluation & Recertification Center：评估与再认证中心

评估与再认证中心负责病例考试、对照实验、版本回归测试和能力等级更新。

核心功能：

```text
评估集管理
病例 Rubric 配置
LLM-only / RAG-only / Runtime 对照实验
高危病例漏放率监控
输出越权率监控
医生摘要可用性评估
经验上线前回归测试
版本发布前再认证
Capability Profile 自动降级或升级建议
```

## 6.5 Audit & Governance Center：审计与治理中心

审计与治理中心负责安全、合规和可追踪。

核心功能：

```text
用户输入审计
模型调用审计
知识检索审计
经验记忆触发审计
DecisionBoundary 输出审计
医生审核记录
数据脱敏记录
权限访问记录
异常与 fail-safe 记录
版本发布记录
```

## 6.6 Role & Permission：角色与权限

角色：

```text
Patient：只能看到患者端安全输出
Doctor：可查看医生端报告和证据图
Reviewer：可审核经验候选和病例复盘
Admin：可管理平台配置和权限
Evaluator：可管理评估集和再认证流程
Developer：可查看调试信息，但不能查看真实身份信息
```

权限原则：

```text
患者端不能看到医生端 DDx 全量细节。
医生端可以看到证据与候选，但不能直接修改系统经验记忆。
Reviewer 可以审核经验，但不能直接跳过评估上线。
Admin 可以管理权限，但不能绕过医疗安全规则。
Developer 只能访问脱敏数据。
```

---

# 七、Runtime 层总体设计

第六章已经说明平台管理层如何训练、治理和评估系统。本章开始进入 Runtime 执行层。

Runtime 执行层负责一次问诊从输入到输出的完整运行过程。它不是一个固定 Workflow，而是一个状态驱动的诊断运行时：每一轮用户输入都会更新 RuntimeState，并由当前病例状态、医学知识、临床经验、危险信号、证据充分性和输出边界共同决定下一步动作。

Runtime 执行层的设计按照三层展开：

```text
1. Runtime 基础设施：API、状态、状态对象、追踪、短期上下文
2. Runtime 执行链路：入口判断、病例结构化、知识/经验检索、安全门、候选诊断、证据图、追问策略、输出边界、表达层
3. Runtime 外部闭环：医生反馈、随访结局、经验候选、再认证和能力更新
```

一次 Runtime 的核心循环：

```text
输入
→ 更新状态
→ 调用知识和经验
→ 判断风险
→ 更新候选诊断和证据
→ 决定下一步动作
→ 控制输出边界
→ 生成分角色表达
→ 写入审计与追踪
```

---

# 八、Runtime 基础设施

Runtime 基础设施负责让一次问诊可创建、可继续、可查看、可追踪、可恢复。

## 8.1 Runtime API

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/continue
GET  /api/v1/runtime/{runtimeId}/status
GET  /api/v1/runtime/{runtimeId}/result
GET  /api/v1/runtime/{runtimeId}/trace
```

`start` 用于创建一次新的诊断 Runtime。

请求示例：

```json
{
  "user_id": "u_001",
  "session_id": "s_001",
  "mode": "patient_facing",
  "input": {
    "text": "我最近胸口闷，活动后更明显",
    "attachments": []
  },
  "basic_info": {
    "age": 58,
    "sex": "male"
  }
}
```

响应示例：

```json
{
  "runtime_id": "rt_001",
  "status": "collecting_case_info",
  "work_mode": "clinical_mode",
  "risk_level": "medium_high",
  "red_flags": ["活动后加重"],
  "allowed_output": "O1_continue_questioning",
  "next_action": {
    "type": "ask_question",
    "content": "胸闷时是否伴随出汗、气短，或者疼痛向左肩、后背放射？",
    "purpose": "rule_out_high_risk_diagnosis"
  },
  "case_frame_summary": {},
  "timestamp": 1760000000000
}
```

`continue` 用于提交用户补充信息，推进 Runtime 一轮。

```json
{
  "runtime_id": "rt_001",
  "input": {
    "text": "有点出汗，走路快的时候更明显，休息会缓解"
  }
}
```

系统执行：

```text
Update CaseFrame
→ Query Knowledge Context
→ Retrieve Experience Context
→ SafetyGate
→ Update DDx
→ Update EvidenceGraph
→ Question / Test Policy
→ DecisionBoundary
→ Generate Output
→ Write Trace
```

## 8.2 RuntimeStatus

RuntimeStatus 表示一次问诊当前处于什么状态。

```text
created
entry_assessing
wellness_mode
clinical_mode
collecting_case_info
safety_gate_triggered
building_differential
collecting_evidence
recommending_tests
waiting_for_user
waiting_for_doctor
ready_for_patient_output
ready_for_clinician_report
completed
follow_up_pending
under_review
archived
error_safe_halted
```

| 状态 | 含义 | 患者端输出权限 |
|---|---|---|
| created | Runtime 已创建 | 不输出 |
| entry_assessing | 正在判断入口工作态 | 不输出 |
| wellness_mode | 健康管理态 | 健康教育 / 日常建议 |
| clinical_mode | 临床问诊态 | 按边界控制 |
| collecting_case_info | 收集基础病例信息 | 继续追问 |
| safety_gate_triggered | 命中高危信号 | 只允许风险提示 / 就医建议 |
| building_differential | 构建候选诊断池 | 不输出诊断方向 |
| collecting_evidence | 收集关键证据 | 继续追问或建议检查 |
| recommending_tests | 建议检查 | 只输出检查准备或就医建议 |
| waiting_for_user | 等待用户补充 | 不输出诊断方向 |
| waiting_for_doctor | 需要医生介入 | 提示转医生 |
| ready_for_patient_output | 可生成患者端安全输出 | 受 DecisionBoundary 限制 |
| ready_for_clinician_report | 可生成医生端报告 | 医生端完整报告 |
| completed | 本轮闭环完成 | 按边界输出 |
| follow_up_pending | 等待随访结局 | 不输出新判断 |
| under_review | 医生复盘中 | 不输出 |
| archived | 已归档 | 不输出 |
| error_safe_halted | 安全模块异常，中止 | 只输出保守安全提示 |

## 8.3 RuntimeState

RuntimeState 是一次问诊的中心状态对象，是诊断状态的唯一事实源。

```json
{
  "runtime_id": "rt_001",
  "user_id": "u_001",
  "session_id": "s_001",
  "version": 1,
  "runtime_status": "collecting_evidence",
  "work_mode": "clinical_mode",
  "entry_assessment": {},
  "case_frame": {},
  "knowledge_context": {
    "clinical_pathway": {},
    "kg_relations": [],
    "rag_evidence": []
  },
  "experience_context": {
    "matched_experience_units": [],
    "experience_alerts": []
  },
  "diagnostic_state": {
    "differential_board": {},
    "evidence_graph": {},
    "question_test_policy_state": {}
  },
  "boundary_state": {
    "capability_profile_snapshot": {},
    "safety_gate": {},
    "decision_boundary": {},
    "failure_policy": {}
  },
  "evolution_trace": {
    "doctor_feedback": null,
    "follow_up_outcome": null,
    "review_status": null,
    "recertification_status": null
  },
  "patient_output": {},
  "clinician_report": {},
  "uncertainty": {},
  "audit_log": {},
  "runtime_trace": [],
  "created_at": "2026-06-25T10:00:00+09:00",
  "updated_at": "2026-06-25T10:03:00+09:00"
}
```

旧项目字段映射：

```text
patient_state → case_frame
DDx → diagnostic_state.differential_board
workupPlan → diagnostic_state.question_test_policy_state.recommended_tests
triage → boundary_state.safety_gate + boundary_state.decision_boundary
uncertainty → uncertainty
executionTrace → runtime_trace
audit → audit_log
managementPlan → clinician_management_reference，仅医生端可见
```

原则：

```text
1. RuntimeState 是诊断状态的唯一事实源
2. 对话历史不能替代 RuntimeState
3. LLM 输出必须写回 RuntimeState 后才允许影响下一轮
4. 每次状态变化必须生成 RuntimeTrace
5. 重要状态变更必须带版本号，支持回滚和复盘
```

## 8.4 RuntimeTrace

RuntimeTrace 记录每一轮问诊中五个能力域如何影响判断。

```json
{
  "trace_id": "trace_001",
  "runtime_id": "rt_001",
  "input": "胸口闷，活动后明显，还有点出汗",
  "knowledge_used": [
    {
      "type": "clinical_pathway",
      "name": "chest_pain_pathway"
    },
    {
      "type": "red_flag_rule",
      "name": "chest_pain_with_sweating"
    }
  ],
  "experience_used": [
    {
      "experience_id": "chest_pain_exp_023",
      "effect": "increase_question_priority"
    }
  ],
  "diagnostic_state_change": {
    "case_frame_updated": true,
    "ddx_added": ["急性冠脉综合征", "肺栓塞"],
    "evidence_graph_updated": true
  },
  "boundary_decision": {
    "allowed_output": "O5",
    "patient_diagnostic_label_allowed": false,
    "reason": "高危胸痛尚未排除"
  },
  "next_action": {
    "type": "recommend_test",
    "content": "建议尽快完善心电图和心肌损伤标志物检查"
  }
}
```

## 8.5 短期对话上下文

ClinMindRuntime 需要区分短期对话上下文和长期诊断状态。

```text
Redis：保存短期对话上下文
数据库：保存 RuntimeState 长期状态
内存存储：开发环境或 Redis 不可用时的临时降级
```

对话历史最多保留最近 20 条，但诊断判断不能依赖对话历史。

```text
短期上下文用于语言连续性。
RuntimeState 用于诊断决策。
```

---

# 九、Runtime 执行链路

本章按照一次问诊 Runtime 的执行顺序展开。每个模块不是孤立存在，而是在一次状态更新循环中依次参与判断。

## 9.1 EntryAssessment：入口工作态判定

EntryAssessment 负责判断当前输入是否应该进入临床问诊 Runtime。

工作态：

```text
wellness_mode：健康管理、日常咨询、轻症科普、非诊断需求
clinical_mode：存在明确症状，需要问诊支持
emergency_hint：疑似急症，直接触发 SafetyGate
unsupported：超出系统能力范围，建议咨询医生或改写输入
```

## 9.2 CaseFrame：病例状态构建

CaseFrame 将用户输入转换为结构化病例状态。

第一版最小字段集：

```json
{
  "chief_complaint": null,
  "patient_profile": {
    "age": null,
    "sex": null,
    "risk_factors": []
  },
  "symptoms": [
    {
      "name": null,
      "duration": null,
      "severity": null,
      "location": null,
      "trigger": null,
      "frequency": null,
      "relief": null
    }
  ],
  "associated_symptoms": [],
  "vital_signs": {},
  "past_history": [],
  "family_history": [],
  "medication_history": [],
  "allergy_history": [],
  "examination_results": [],
  "user_answers": {},
  "conflicting_slots": [],
  "missing_slots": []
}
```

字段完整度只是辅助指标，不能直接决定是否可以诊断。核心判断必须基于高危信号、关键证据、高危候选是否排除、能力等级和 DecisionBoundary。

## 9.3 Knowledge Context：医学知识上下文

Knowledge Context 汇集本轮问诊需要使用的医学知识。

来源包括：

```text
Symptom Rotation Library
Clinical Pathway
KG-lite
RAG Evidence Library
Red Flag Rules
Test Recommendation Rules
```

它服务于 SafetyGate、DDx Board、EvidenceGraph 和 Question / Test Policy，但不直接决定最终输出。

## 9.4 Experience Context：临床经验上下文

Experience Context 汇集本轮问诊匹配到的临床经验。

来源包括：

```text
Clinical Experience Memory
相似病例经验
误诊教训
医生审核经验
经验触发日志
```

经验可以提高系统警觉，影响追问顺序、检查建议和输出边界收紧，但不能直接给出最终诊断，也不能覆盖指南和安全规则。

## 9.5 SafetyGate：危险信号识别

SafetyGate 是 Runtime 的硬安全模块，优先于候选诊断和自然语言生成。

输入：

```text
CaseFrame
症状群
患者基础信息
既往史
当前候选诊断
红旗规则库
Capability Profile
Clinical Experience Memory 提醒
```

规则：

```text
1. SafetyGate 命中高危后，患者端不能输出低风险判断
2. SafetyGate 失败时必须 fail-safe
3. SafetyGate 规则必须有来源：临床路径、指南、医生配置或已验证经验
4. LLM 可以辅助识别表达，但不能覆盖高危规则
```

## 9.6 Differential Diagnosis Board：候选诊断状态板

系统不直接输出唯一诊断，而是维护候选诊断池。

候选状态：

```text
primary_hypothesis
main_alternative
must_not_miss
need_to_rule_out
possible
possible_after_exclusion
unlikely
insufficient_evidence
```

原则：

```text
1. 高危候选不能因低分删除
2. 常见病可以排序，高危病必须保留
3. 候选诊断必须绑定证据状态
4. 患者端是否展示候选诊断由 DecisionBoundary 决定
```

## 9.7 EvidenceGraph：证据状态图

EvidenceGraph 是诊断控制层，不只是解释层。

```json
{
  "diagnosis": "急性冠脉综合征",
  "supporting_evidence": ["胸痛", "活动后加重", "出汗", "气短"],
  "opposing_evidence": [],
  "missing_evidence": ["心电图", "肌钙蛋白", "疼痛放射部位"],
  "conflicting_evidence": [],
  "experience_alerts": ["胸痛伴出汗和气短时，不能因症状短暂缓解而判断低风险"],
  "recommended_questions": ["疼痛是否向左肩、后背或下颌放射？"],
  "recommended_tests": ["心电图", "肌钙蛋白"],
  "status": "need_to_rule_out"
}
```

EvidenceGraph 决定：

```text
下一步问什么
是否建议检查
是否允许输出诊断方向
是否需要转医生
是否禁止患者端诊断标签
哪些经验提醒需要触发
```

## 9.8 Question / Test Policy：动态追问与检查建议

医生诊断不是机械补字段，而是根据当前候选诊断和缺失证据决定下一步。

优先级：

```text
危险信号
> 高危疾病排除
> 经验记忆提醒
> 高信息增益问题
> 关键检查建议
> 常见病鉴别
> 普通病史补全
```

旧项目 InformationGapIdentifier / AdaptiveQuestioningStrategy / CompletenessCalculator 的升级关系：

```text
InformationGapIdentifier → EvidenceGapIdentifier
AdaptiveQuestioningStrategy → QuestionTestPolicy
CompletenessCalculator → EvidenceSufficiencyEvaluator
```

旧项目是“缺字段 → 问字段”，新项目必须升级为“候选诊断 / 高危排除 / 缺失证据 → 决定下一步追问或检查建议”。

## 9.9 DecisionBoundary：输出边界控制

DecisionBoundary 根据能力档案、风险等级、证据状态和经验提醒决定当前能输出什么。

输入：

```text
Capability Profile
SafetyGate
EvidenceGraph
Differential Diagnosis Board
Clinical Experience Memory
当前模式 patient / clinician / silent
FailurePolicy
```

输出等级：

```text
O1：继续追问
O2：风险提示
O3：医生端候选诊断方向
O4：低风险初步判断参考
O5：门诊 / 急诊建议
O6：转人工医生
O7：医生端完整鉴别诊断报告
```

原则：

```text
先判断是否允许输出，再生成回答。
```

## 9.10 Human-like Interaction Layer：分角色表达

Human-like Interaction Layer 只负责表达，不负责医学决策。

四个能力：

```text
1. 解释为什么问
2. 表达不确定性
3. 根据用户情绪调整语气
4. 用审核后的临床经验提醒风险
```

边界：

```text
只能改变表达方式
不能降低风险等级
不能改变 SafetyGate
不能改变 DecisionBoundary
不能新增诊断结论
不能编造临床经验
不能为了安抚用户而弱化就医建议
不能将医生端内容泄露到患者端
```

## 9.11 FailurePolicy：医疗 Fail-safe

旧项目有服务降级经验，但医疗场景不能所有失败都继续流程。

规则：

```text
LLM 失败：可以降级为规则输出
RAG 失败：可以提示证据不足，不能引用指南
Human-like Layer 失败：可以输出结构化安全结果
Explanation 失败：可以返回医生端结构化报告
KG 失败：可以用规则和 RAG 候选补充，但标记证据不足
SafetyGate 失败：必须 fail-safe，不输出诊断方向
DecisionBoundary 失败：必须 fail-safe，不输出诊断方向
EvidenceGraph 失败：不得输出候选诊断，只能继续追问或转医生
RiskAssessment 失败：高危场景默认收紧输出
```

安全相关模块失败时，RuntimeStatus 进入：

```text
error_safe_halted
```

---

# 十、训练与能力授权机制

本章解释系统能力从哪里来，以及为什么 Runtime 不能在所有症状群上自由输出。

## 10.1 Symptom Rotation Library

Symptom Rotation Library 属于共享能力资产层，由 Training Center 管理，被 Runtime 通过 Knowledge Context 使用。

系统不一开始承诺覆盖所有诊断问题，而是按症状群逐步训练和验证。

第一阶段覆盖五个高频症状群：

```text
发热
咳嗽 / 呼吸困难
腹痛 / 呕吐 / 腹泻
胸痛 / 胸闷
头痛 / 眩晕
```

每个 rotation 包含：

```text
标准医学知识
临床路径
常见候选诊断
必须排除的高危疾病
必问病史
推荐检查
典型病例
非典型病例
易误诊病例
医生点评
评估指标
```

## 10.2 病例考试

完成一个 rotation 后，系统必须通过病例考试，才能获得对应症状群能力等级。

考试不只看最终诊断是否命中，而是看完整诊断行为：

```text
危险信号是否识别
高危疾病是否排除
必问问题是否覆盖
候选诊断是否合理
证据归因是否正确
推荐检查是否合理
是否过度自信
是否给出不安全建议
是否知道什么时候转人工
医生摘要是否可用
沟通是否清楚自然
```

## 10.3 Capability Profile

Capability Profile 属于共享能力资产层，由病例考试、评估结果和再认证记录共同更新。Runtime 在执行时通过 DecisionBoundary 和 SafetyGate 读取它。

能力等级：

```text
L1：只能做病史结构化
L2：可以做危险信号识别和就医分级
L3：可以给医生端候选诊断方向
L4：低风险、信息充分场景下给初步判断参考
L5：严格验证后的受控自主诊断，仅限特定闭环场景
```

运行时约束：

```text
如果 chest_pain 只有 L2，患者端不能输出候选诊断。
如果 fever 达到 L3，也只能在医生端展示候选方向。
如果存在高危信号，即使能力等级较高，也必须受 SafetyGate 限制。
```

---

# 十一、经验进化机制

本章解释系统如何从病例、医生反馈和随访结局中沉淀经验，但又不让未经验证的经验直接影响线上判断。

## 11.1 Clinical Experience Memory

Clinical Experience Memory 属于共享能力资产层，由 Experience Memory Center 管理，被 Runtime 通过 Experience Context 检索。

Clinical Experience Memory 不是普通聊天记忆，也不是原始病例库，而是经过医生审核、质控和再认证的经验单元。

经验单元示例：

```json
{
  "experience_id": "chest_pain_exp_023",
  "symptom_group": "chest_pain",
  "case_pattern": {
    "initial_impression": "年轻患者胸痛，情绪紧张，容易被判断为焦虑",
    "hidden_risk": "肺栓塞",
    "trigger_features": ["胸痛", "气短", "近期久坐", "焦虑样表现"]
  },
  "clinical_lesson": "年轻患者胸痛不能直接归因于焦虑。如果合并气短、近期久坐、下肢肿痛或激素/避孕药使用，需要优先排除肺栓塞。",
  "must_ask": ["是否呼吸困难", "是否咯血", "是否近期长途旅行或久坐", "是否下肢肿痛"],
  "source": "doctor_reviewed_case",
  "validation_status": "approved_for_runtime_reference",
  "applicable_scope": "young_or_middle_aged_chest_pain_with_dyspnea"
}
```

允许影响：

```text
候选诊断优先级
追问顺序
检查建议
风险提醒
输出边界收紧
```

不允许影响：

```text
最终诊断结论
处方建议
高危降级
跳过必要检查
覆盖指南原则
```

## 11.2 Shadow Learning

Shadow Learning 属于平台治理流程，不是线上 Runtime 执行模块。它负责在后台发现病例模式和经验候选。

可分析：

```text
相似病例聚类
症状组合模式发现
高危病例早期表现挖掘
常见漏问问题统计
诊断修正路径分析
医生驳回原因聚类
随访结局与初始判断差异分析
```

## 11.3 Experience Memory Governance

经验状态：

```text
Candidate：经验候选，不能影响线上输出
Reviewed：医生已审核，但不能直接影响 Runtime
Validated：通过离线评估，可进入 Runtime
Deprecated：废弃或降权，不再影响 Runtime
```

经验与指南冲突时：

```text
不能自动使用经验。
必须标记 conflict。
进入医生审核。
患者侧不展示。
医生端只作为经验性提醒展示。
```

## 11.4 Review & Recertification

Review & Recertification 属于平台治理流程，负责决定经验和能力是否可以继续被 Runtime 使用。

流程：

```text
真实病例
  ↓
医生反馈
  ↓
最终诊断 / 随访结果
  ↓
错误归因
  ↓
Shadow Learning 发现经验候选
  ↓
医生审核
  ↓
Clinical Experience Memory 更新
  ↓
离线评估
  ↓
再认证
  ↓
Capability Profile 更新
```

如果新经验或新版本导致高危漏放率上升，系统应降低对应症状群权限，而不是自动升级能力。

## 11.5 Clinical Data Maturity Pipeline

```text
D0：医学知识数据
D1：标准教学病例
D2：模拟患者病例
D3：医生审核病例
D4：真实业务病例与随访结局
```

用途隔离：

```text
Knowledge Data：用于 RAG、KG-lite、Clinical Pathway
Training Data：用于症状群 Rotation、模拟患者、问诊策略优化
Evaluation Data：用于固定病例考试和对照实验，不能被训练阶段看到
Experience Data：用于 Clinical Experience Memory，必须来自医生审核或经过验证的病例复盘
```

原则：

```text
训练集不能污染评估集。
模拟病例不能伪装成真实病例。
单个病例经验不能直接变成普遍规则。
未经审核的真实病例不能进入 Runtime。
```

---

# 十二、工程落地设计

本章说明如何把前面的能力域、三层结构和 Runtime 执行链路落到代码、数据表和交互协议中。

## 12.1 推荐工程目录

实现时不建议把五个能力域强行做成五个大服务，而是按工程责任组织目录。

```text
clinmind-runtime/
├── api/                  # Runtime API
├── knowledge/            # 医学知识域 / 共享能力资产读取
│   ├── clinical_pathway_provider.py
│   ├── kg_provider.py
│   └── rag_evidence_provider.py
├── experience/           # 临床经验域 / 共享能力资产读取
│   ├── experience_memory.py
│   ├── experience_retriever.py
│   └── experience_governance.py
├── state/                # 诊断状态域的一部分
│   ├── runtime_state.py
│   ├── runtime_status.py
│   └── runtime_trace.py
├── reasoning/            # 诊断状态域的一部分
│   ├── case_frame.py
│   ├── differential_board.py
│   ├── evidence_graph.py
│   └── question_test_policy.py
├── boundary/             # 输出边界域
│   ├── capability_profile.py
│   ├── safety_gate.py
│   ├── decision_boundary.py
│   └── failure_policy.py
├── evolution/            # 复盘进化域
│   ├── doctor_feedback.py
│   ├── follow_up_outcome.py
│   ├── evaluation.py
│   └── recertification.py
├── assets/               # 共享能力资产层
│   ├── symptom_rotation_library/
│   ├── clinical_pathways/
│   ├── rag_evidence_library/
│   ├── clinical_experience_memory/
│   └── capability_profiles/
├── interaction/          # 表达层
│   ├── patient_output.py
│   └── clinician_report.py
└── platform/             # 平台管理层
    ├── training_center/
    ├── experience_center/
    ├── evaluation_center/
    └── audit_center/
```

## 12.2 数据库表设计方向

医学知识域：

```text
clinical_pathways
kg_nodes
kg_edges
evidence_documents
red_flag_rules
test_recommendation_rules
```

临床经验域：

```text
experience_units
experience_cases
experience_review_logs
experience_versions
experience_trigger_logs
```

诊断状态域：

```text
runtime_states
case_frames
ddx_candidates
evidence_items
question_actions
test_actions
runtime_traces
```

输出边界域：

```text
capability_profiles
safety_gate_results
decision_boundary_results
failure_policy_logs
role_permissions
```

复盘进化域：

```text
doctor_feedback
follow_up_outcomes
evaluation_cases
evaluation_runs
recertification_records
capability_profile_versions
```

共享能力资产层：

```text
symptom_rotation_libraries
clinical_pathway_versions
rag_evidence_versions
experience_memory_versions
capability_profile_versions
evaluation_result_versions
recertification_record_versions
```

## 12.3 实时交互协议

```text
HTTP：用于 start / continue / status / result / trace
SSE：优先用于患者端流式返回安全表达
WebSocket：可作为后续全双工医生端调试和实时状态观察
```

---

# 十三、企业级运行模式

## 13.1 Patient-facing Mode

面向患者，只允许输出：

```text
风险提示
就医建议
需要补充的信息
健康教育
需要携带的检查资料
```

禁止输出：

```text
确定诊断
处方
复杂检查结论
高危场景下的居家观察建议
```

## 13.2 Clinician Copilot Mode

面向医生，允许输出：

```text
候选诊断
证据状态图
相似病例经验提醒
推荐追问
推荐检查
医生摘要
鉴别诊断报告
```

AI 提供支持，医生负责最终判断。

## 13.3 Silent Evaluation Mode

AI 生成判断但不影响医生决策。

记录：

```text
AI 候选诊断
AI 风险分级
AI 推荐检查
AI 经验提醒
医生判断
最终诊断
随访结果
```

用于真实表现评估和后续再认证。

---

# 十四、评估体系

系统评估不是只看诊断命中率，而是看整个诊断行为是否安全、合理、像医生。

指标：

```text
危险信号识别率
高危病例漏放率
必问问题覆盖率
候选诊断 Top-3 召回率
推荐检查合理率
经验提醒命中率
不安全建议率
输出边界正确率
证据引用准确率
医生摘要可用性
沟通自然度
不确定性表达质量
医生反馈采纳率
```

第一阶段最小评估集：

```text
症状群：胸痛 / 发热
病例数量：50-100 个
病例类型：普通病例、高危病例、信息缺失病例、误导表达病例
对比对象：LLM-only、RAG-only、ClinMindRuntime
```

---

# 十五、MVP 实现范围

P0 必做：

```text
Runtime API
RuntimeState
RuntimeStatus
RuntimeTrace
Short-term Context
EntryAssessment
CaseFrame
Knowledge Context
Experience Context
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Patient-facing / Clinician-facing 输出区分
FailurePolicy
小型病例评估集
```

P1 第二阶段：

```text
RAG Evidence Library
KG-lite
Clinical Pathway
Capability Profile
医生端报告
Clinical Experience Memory 原型
Silent Evaluation 原型
Training Center 原型
Evaluation Center 原型
```

P2 后续扩展：

```text
医生审核流程
随访结局接入
Shadow Learning
Review & Recertification
Experience Memory Center
Audit & Governance Center
OCR / 检查报告解析
多模态输入
真实业务病例评估
```

第一阶段不要做：

```text
治疗方案患者端输出
处方建议
复杂 Nacos 微服务治理
过度完整的 Docker Compose
大规模真实病例经验记忆
自动在线学习
```

---

# 十六、面试讲法

项目一句话：

> ClinMindRuntime 是一个医疗问诊场景下的受控诊断 Runtime。它不是让大模型直接诊断，而是把问诊过程拆成病例状态、危险信号、候选诊断、证据图、动态追问和输出边界，让模型在可控流程中完成诊断支持。

系统核心讲法：

> 我的系统不是简单按照功能模块堆起来的，而是先抽象出五个核心能力域：医学知识、临床经验、诊断状态、输出边界和复盘进化。这五个能力域共同构成智能诊断的核心能力。具体实现时，再通过平台管理层、共享能力资产层和 Runtime 执行层落地。能力域是设计逻辑，三层结构是系统组织方式，具体模块是工程实现。

与普通 RAG 医疗问答的区别：

```text
普通 RAG：用户问题 → 检索知识 → 拼 Prompt → LLM 回答
ClinMindRuntime：用户输入 → CaseFrame → Knowledge/Experience Context → SafetyGate → DDx Board → EvidenceGraph → Question/Test Policy → DecisionBoundary → 分角色输出
```

与旧 AIdoctor 的关系：

> 旧项目 AIdoctor 已经实现过 CDP 状态管理、主动问诊、多引擎诊断、证据链分析和执行追踪。但我复盘后发现，旧项目更像固定 Workflow，主要解决“有哪些服务”和“流程怎么跑通”。ClinMindRuntime 是在这个基础上的架构升级，重点从功能服务集合转向状态驱动的诊断 Runtime，强调 SafetyGate、EvidenceGraph、DecisionBoundary 和复盘再认证。

当前边界：

> 当前版本主要验证 Runtime 机制，包括状态管理、危险信号识别、证据图、动态追问和输出边界。Clinical Experience Memory、Silent Evaluation、真实医生审核和随访结局是后续真实业务接入后的扩展闭环，我不会把当前版本描述成已经临床有效的产品。

---

# 十七、最终定义

ClinMindRuntime 是一个企业级智能诊断训练—运行—经验进化平台。

它的系统核心可以拆分为五个能力域：

```text
医学知识域
临床经验域
诊断状态域
输出边界域
复盘进化域
```

它的系统结构可以拆分为三层：

```text
平台管理层
共享能力资产层
Runtime 执行层
```

它通过：

```text
症状群 Rotation
病例考试
Capability Profile
Clinical Data Maturity Pipeline
RuntimeState
CaseFrame
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Human-like Interaction Layer
Clinical Experience Memory
Experience Memory Governance
Shadow Learning
Review & Recertification
Evaluation System
Platform Governance
```

让 AI 在安全边界内逐步积累临床经验，并在问诊过程中表现得更像一个受训医生：

```text
知道当前病例是什么状态
知道用了哪些医学知识
知道触发了哪些临床经验
知道有哪些候选诊断
知道哪些证据支持或反对
知道还缺什么关键信息
知道下一步该问什么或建议什么检查
知道什么时候不能继续在线判断
知道患者端和医生端能输出什么
知道出错后如何复盘和再认证
知道经验记忆如何被审核、验证、回滚和治理
```

系统的核心价值不是替代医生，而是让医疗 AI 从普通问答升级为：

```text
可训练
可积累经验
可受控学习
可授权
可追踪证据
可动态决策
可自然沟通
可复盘再认证
可平台化治理
```

的企业级智能诊断 Runtime。
