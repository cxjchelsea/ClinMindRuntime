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
它知道什么时候不能继续在线判断，必须建议就医或转医生。
```

系统角色定位：

```text
患者端：风险提示、信息补全、就医建议、健康教育、检查准备说明
医生端：候选诊断、证据状态、相似经验提醒、检查建议、医生摘要
后台端：病例复盘、错误归因、经验候选发现、再认证评估、审计治理
```

---

# 二、设计核心：训练、经验、运行、表达、复盘

系统由五条主线组成：

```text
1. 训练：通过症状群 Rotation 和病例考试获得能力
2. 经验：通过 Clinical Experience Memory 积累临床经验
3. 运行：通过 Diagnostic Runtime 管理一次问诊推理
4. 表达：通过 Human-like Interaction Layer 让沟通更像医生
5. 复盘：通过医生反馈、随访结局和再认证持续进化
```

整体闭环：

```text
症状群 Rotation
  ↓
标准病例训练
  ↓
病例考试
  ↓
能力档案 Capability Profile
  ↓
真实问诊 / 医生协作 / Silent Evaluation
  ↓
CaseFrame + EvidenceGraph + DecisionBoundary 运行
  ↓
医生反馈 / 最终诊断 / 随访结局
  ↓
经验候选发现 Shadow Learning
  ↓
医生审核与质控
  ↓
Clinical Experience Memory
  ↓
再评估 / 再认证
  ↓
能力档案更新
```

普通医疗 Agent 主要依赖：

```text
医学知识 + RAG + Prompt
```

本系统依赖：

```text
医学知识 + 病例经验 + 诊断状态 + 医生反馈 + 再认证
```

核心公式：

```text
智能诊断 = 医学知识 + 临床经验 + 诊断状态 + 输出边界 + 复盘进化
```

这五项不是系统模块，而是 ClinMindRuntime 的五个核心能力域。它们说明系统为什么不是普通 RAG 问答，而是一个训练—运行—经验进化平台。真正的系统模块划分见 5.1 平台模块。

五个能力域与支撑组件：

```text
医学知识域：
Clinical Pathway + KG-lite + RAG Evidence Library
临床经验域：
Clinical Experience Memory + Shadow Learning + Experience Memory Governance
诊断状态域：
RuntimeState + CaseFrame + Differential Diagnosis Board + EvidenceGraph + Question / Test Policy
输出边界域：
Capability Profile + SafetyGate + DecisionBoundary + FailurePolicy
复盘进化域：
Doctor Feedback + Follow-up Outcome + Review & Recertification + Evaluation System
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

## 5.1 平台模块

ClinMindRuntime 由平台层和 Runtime 层共同组成，平台层负责训练、治理、评估和管理；Runtime 层负责一次问诊过程中的状态维护、证据推理和安全输出。

```text
平台层：
1. Training Center：训练中心
2. Runtime Console：运行控制台
3. Experience Memory Center：经验记忆中心
4. Evaluation & Recertification Center：评估与再认证中心
5. Audit & Governance Center：审计与治理中心
6. Role & Permission：角色与权限管理

Runtime 层：
1. Runtime API
2. EntryAssessment
3. RuntimeState
4. Symptom Rotation Library
5. Capability Profile
6. CaseFrame
7. SafetyGate
8. Differential Diagnosis Board
9. EvidenceGraph
10. Question / Test Policy
11. DecisionBoundary
12. Human-like Interaction Layer
13. Clinical Experience Memory
14. Shadow Learning
15. Review & Recertification
16. Evaluation System
17. FailurePolicy
```

## 5.2 总体运行链路

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
查询 Capability Profile
  ↓
检索 Clinical Experience Memory 中的相似经验
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
RuntimeTrace 记录过程
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

# 六、平台化管理后台设计

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

# 七、Runtime API 设计

## 7.1 API 列表

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/continue
GET  /api/v1/runtime/{runtimeId}/status
GET  /api/v1/runtime/{runtimeId}/result
GET  /api/v1/runtime/{runtimeId}/trace
```

## 7.2 start

用于创建一次新的诊断 Runtime。

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

## 7.3 continue

用于提交用户补充信息，推进 Runtime 一轮。

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
→ SafetyGate
→ Update DDx
→ Update EvidenceGraph
→ Question / Test Policy
→ DecisionBoundary
→ Generate Output
→ Write Trace
```

## 7.4 status

```json
{
  "runtime_id": "rt_001",
  "runtime_status": "safety_gate_triggered",
  "work_mode": "clinical_mode",
  "current_step": "collecting_evidence",
  "risk_level": "high",
  "allowed_output": "O5_emergency_recommendation"
}
```

## 7.5 result

患者端示例：

```json
{
  "mode": "patient_facing",
  "message": "你描述的胸闷在活动后明显，并伴随出汗，需要优先排除心血管急症。建议尽快就医完善心电图和心肌损伤标志物检查。",
  "diagnostic_label_allowed": false,
  "next_action": "seek_medical_care"
}
```

医生端示例：

```json
{
  "mode": "clinician_copilot",
  "differential_board": {},
  "evidence_graph": {},
  "recommended_questions": [],
  "recommended_tests": [],
  "experience_alerts": [],
  "runtime_trace_summary": {}
}
```

## 7.6 trace

返回 Runtime 过程追踪，供医生端、调试、复盘和评估使用。

---

# 八、RuntimeStatus 状态生命周期

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

---

# 九、RuntimeState 设计

RuntimeState 是一次问诊的中心状态对象。它吸收旧项目 CDP 的工程字段经验，但在 ClinMindRuntime 中被重新定义为诊断 Runtime 状态内核。

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
  "capability_profile_snapshot": {},
  "differential_board": {},
  "evidence_graph": {},
  "question_test_policy_state": {},
  "safety_gate": {},
  "decision_boundary": {},
  "patient_output": {},
  "clinician_report": {},
  "uncertainty": {},
  "audit_log": {},
  "runtime_trace": {},
  "created_at": "2026-06-25T10:00:00+09:00",
  "updated_at": "2026-06-25T10:03:00+09:00"
}
```

旧项目字段映射：

```text
patient_state → case_frame
DDx → differential_board
workupPlan → question_test_policy_state.recommended_tests
triage → safety_gate + decision_boundary
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

---

# 十、EntryAssessment 入口工作态判定

EntryAssessment 负责判断当前输入是否应该进入临床问诊 Runtime。

工作态：

```text
wellness_mode：健康管理、日常咨询、轻症科普、非诊断需求
clinical_mode：存在明确症状，需要问诊支持
emergency_hint：疑似急症，直接触发 SafetyGate
unsupported：超出系统能力范围，建议咨询医生或改写输入
```

输出示例：

```json
{
  "work_mode": "clinical_mode",
  "reason": "用户描述胸闷且活动后加重，需要进入临床问诊流程",
  "risk_level": "medium_high",
  "red_flags": ["活动后加重"],
  "next_runtime_status": "collecting_case_info"
}
```

区别：

```text
EntryAssessment：是否进入临床问诊。
SafetyGate：进入后是否命中高危风险，需要收紧输出。
```

---

# 十一、医生培养机制：Rotation、病例考试与能力档案

医生培养机制不是文档中的一个章节，而是系统运行权限和经验进化的基础。

## 11.1 症状群 Rotation

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

胸痛 rotation 示例：

```text
常见方向：胃食管反流、肌肉骨骼痛、焦虑相关胸闷
高危方向：急性冠脉综合征、主动脉夹层、肺栓塞、张力性气胸
必问问题：放射痛、大汗、气短、活动后加重、下肢肿痛、近期制动
关键检查：心电图、肌钙蛋白、D-二聚体、胸部影像
```

## 11.2 病例考试

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

## 11.3 Capability Profile 能力档案

能力等级：

```text
L1：只能做病史结构化
L2：可以做危险信号识别和就医分级
L3：可以给医生端候选诊断方向
L4：低风险、信息充分场景下给初步判断参考
L5：严格验证后的受控自主诊断，仅限特定闭环场景
```

能力档案示例：

```json
{
  "agent_id": "clinmind_runtime_v1",
  "capability_profile": {
    "fever": {
      "level": "L3_candidate_diagnosis",
      "case_exposure": 520,
      "red_flag_recall": 0.95,
      "unsafe_output_rate": 0.02,
      "common_pitfalls": [
        "早期肺炎漏问呼吸情况",
        "持续高热低估脱水风险"
      ]
    },
    "chest_pain": {
      "level": "L2_triage_only",
      "case_exposure": 210,
      "red_flag_recall": 0.91,
      "unsafe_output_rate": 0.05,
      "common_pitfalls": [
        "年轻患者胸痛误判为焦虑",
        "未充分排除肺栓塞"
      ]
    }
  }
}
```

运行时约束：

```text
如果 chest_pain 只有 L2，患者端不能输出候选诊断。
如果 fever 达到 L3，也只能在医生端展示候选方向。
如果存在高危信号，即使能力等级较高，也必须受 SafetyGate 限制。
```

---

# 十二、CaseFrame 病例状态

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

字段分级吸收旧项目 required / important / optional 思路：

```text
Required：chief_complaint、symptom_trigger、symptom_duration
Important：symptom_severity、symptom_location、symptom_frequency、associated_symptoms
Optional：family_history、past_history、medication_history、allergy_history
```

注意：字段完整度只是辅助指标，不能直接决定是否可以诊断。

核心判断必须基于：

```text
高危信号是否存在
关键证据是否缺失
高危候选是否排除
系统能力等级是否允许
DecisionBoundary 是否允许输出
```

---

# 十三、SafetyGate 危险信号识别

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

输出示例：

```json
{
  "risk_level": "high",
  "red_flags": [
    "胸痛",
    "出汗",
    "气短",
    "活动后加重"
  ],
  "allow_patient_facing_diagnosis": false,
  "next_action": "recommend_emergency_care",
  "reason": "胸痛伴出汗、气短和活动后加重，需要优先排除急性冠脉综合征等高危情况"
}
```

规则：

```text
1. SafetyGate 命中高危后，患者端不能输出低风险判断
2. SafetyGate 失败时必须 fail-safe
3. SafetyGate 规则必须有来源：临床路径、指南、医生配置或已验证经验
4. LLM 可以辅助识别表达，但不能覆盖高危规则
```

---

# 十四、Differential Diagnosis Board 候选诊断状态板

系统不直接输出唯一诊断，而是维护候选诊断池。

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

旧项目三层分类 `primary_hypothesis / main_alternatives / must_exclude` 可作为第一版 Board 的最小结构，但 `must_exclude` 必须升级为列表，并引入更细候选状态。

---

# 十五、EvidenceGraph 证据状态图

EvidenceGraph 是诊断控制层，不只是解释层。

示例：

```json
{
  "diagnosis": "急性冠脉综合征",
  "supporting_evidence": [
    "胸痛",
    "活动后加重",
    "出汗",
    "气短",
    "高血压史"
  ],
  "opposing_evidence": [],
  "missing_evidence": [
    "心电图",
    "肌钙蛋白",
    "疼痛放射部位"
  ],
  "conflicting_evidence": [],
  "experience_alerts": [
    "胸痛伴出汗和气短时，不能因症状短暂缓解而判断低风险"
  ],
  "recommended_questions": [
    "疼痛是否向左肩、后背或下颌放射？"
  ],
  "recommended_tests": [
    "心电图",
    "肌钙蛋白"
  ],
  "status": "need_to_rule_out"
}
```

证据来源结构：

```json
{
  "source": "rule_engine | knowledge_graph | rag | llm | doctor_feedback | clinical_experience",
  "type": "supporting | opposing | missing | conflicting",
  "content": "胸痛活动后加重",
  "target_diagnosis": "急性冠脉综合征",
  "strength": "strong | medium | weak",
  "trace_id": "trace_001"
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

旧项目 EvidenceAnalyzer 可作为 EvidenceGraph 第一版实现参考，但必须从解释型证据链升级为控制型证据图。

---

# 十六、Question / Test Policy 动态追问与检查建议

医生诊断不是机械补字段，而是根据当前候选诊断和缺失证据决定下一步。

系统决定：

```text
下一步应该追问什么
是否需要建议检查
这个问题或检查服务于哪个候选诊断
它是在确认常见病，还是排除高危病
它是否能显著改变当前判断
```

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

追问示例：

```json
{
  "action_type": "ask_question",
  "content": "疼痛是否向左肩、后背或下颌放射？",
  "purpose": "rule_out_high_risk_diagnosis",
  "target_diagnoses": [
    "急性冠脉综合征",
    "主动脉夹层"
  ],
  "reason": "当前胸痛伴出汗、气短和活动后加重，需要优先排除高危心血管疾病。",
  "priority": "high"
}
```

检查建议示例：

```json
{
  "action_type": "recommend_test",
  "content": "建议尽快完善心电图和心肌损伤标志物检查。",
  "purpose": "rule_out_high_risk_diagnosis",
  "target_diagnoses": [
    "急性冠脉综合征"
  ],
  "priority": "urgent",
  "patient_facing_allowed": true,
  "clinician_mode_detail": "用于评估心肌缺血或损伤风险。"
}
```

旧项目 InformationGapIdentifier / AdaptiveQuestioningStrategy / CompletenessCalculator 的升级关系：

```text
InformationGapIdentifier → EvidenceGapIdentifier
AdaptiveQuestioningStrategy → QuestionTestPolicy
CompletenessCalculator → EvidenceSufficiencyEvaluator
```

旧项目是“缺字段 → 问字段”，新项目必须升级为“候选诊断 / 高危排除 / 缺失证据 → 决定下一步追问或检查建议”。

---

# 十七、DecisionBoundary 输出边界

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

示例：

```json
{
  "allowed_output": "O5",
  "patient_facing_allowed": true,
  "clinician_copilot_allowed": true,
  "diagnostic_label_allowed_for_patient": false,
  "reason": [
    "当前为胸痛症状群",
    "系统能力等级为 L2，仅允许分诊",
    "存在高危心血管信号",
    "急性冠脉综合征尚未排除",
    "匹配胸痛高危经验单元"
  ]
}
```

原则：

```text
先判断是否允许输出，再生成回答。
```

---

# 十八、Human-like Interaction Layer 类医生表达层

Human-like Interaction Layer 只负责表达，不负责医学决策。

四个能力：

```text
1. 解释为什么问
2. 表达不确定性
3. 根据用户情绪调整语气
4. 用审核后的临床经验提醒风险
```

机械问法：

```text
有没有呼吸困难？
```

类医生表达：

```text
我需要先确认有没有呼吸困难，因为胸痛合并气短时，需要优先排除心肺方面的急症。你可以先告诉我，现在有没有喘不上气、说话费力或者嘴唇发紫？
```

机械判断：

```text
你这是胃病。
```

类医生表达：

```text
目前还不能先按胃部问题处理，因为胸痛、出汗和气短这些信息提示需要先排除心血管风险。我们先把最危险的情况排除掉，再看是否更像胃食管反流或肌肉疼痛。
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

---

# 十九、知识与证据系统

系统使用四类知识源：

```text
1. Clinical Pathway：症状群临床路径
2. KG-lite：结构化医学关系
3. RAG Evidence Library：指南、文献、说明书、科普资料
4. Clinical Experience Memory：审核后的临床经验
```

分工：

```text
Clinical Pathway：控制流程
KG-lite：提供疾病、症状、危险信号和检查关系
RAG：提供可追溯医学依据
Clinical Experience Memory：提供相似病例、误诊教训和经验提醒
```

原则：

```text
RAG 不直接决定诊断
知识图谱不单独完成诊断
经验记忆不自动覆盖指南
Shadow Learning 不直接影响线上输出
真正控制诊断的是 Runtime 状态
```

旧项目多引擎融合升级为 Candidate & Evidence Providers：

```text
Rule Provider：提供红旗信号、禁忌输出、高危疾病规则
KG Provider：提供疾病-症状-检查关系
RAG Provider：提供指南依据和证据引用
LLM Provider：提供语义理解、候选补全、证据归因草稿
Statistical Provider：提供风险评分，作为辅助信号
Differential Provider：组织候选诊断池
```

这些 Provider 不直接决定最终回答，最终回答必须经过 SafetyGate、EvidenceGraph 和 DecisionBoundary。

---

# 二十、Clinical Experience Memory 临床经验记忆

医生的经验不是简单背更多知识，而是在大量病例中形成敏感性：

```text
哪些表现容易误导
哪些高危病早期很像普通病
哪些问题必须追问
哪些检查真正改变判断
哪些患者表达方式容易造成误判
哪些看似矛盾的信息提示病情正在变化
```

Clinical Experience Memory 不是普通聊天记忆，也不是原始病例库，而是经过医生审核、质控和再认证的经验单元。

经验单元示例：

```json
{
  "experience_id": "chest_pain_exp_023",
  "symptom_group": "chest_pain",
  "case_pattern": {
    "initial_impression": "年轻患者胸痛，情绪紧张，容易被判断为焦虑",
    "hidden_risk": "肺栓塞",
    "trigger_features": [
      "胸痛",
      "气短",
      "近期久坐",
      "焦虑样表现"
    ]
  },
  "clinical_lesson": "年轻患者胸痛不能直接归因于焦虑。如果合并气短、近期久坐、下肢肿痛或激素/避孕药使用，需要优先排除肺栓塞。",
  "must_ask": [
    "是否呼吸困难",
    "是否咯血",
    "是否近期长途旅行或久坐",
    "是否下肢肿痛",
    "是否使用激素或避孕药"
  ],
  "recommended_tests": [
    "D-二聚体",
    "肺动脉CTA"
  ],
  "source": "doctor_reviewed_case",
  "validation_status": "approved_for_runtime_reference",
  "applicable_scope": "young_or_middle_aged_chest_pain_with_dyspnea",
  "not_applicable_when": [
    "明确外伤后胸痛",
    "已完成相关检查并排除肺栓塞"
  ]
}
```

运行时作用：

```text
1. 相似病例提醒
2. 调整候选诊断优先级
3. 影响追问策略
4. 修正输出边界
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

原则：

```text
经验只能让系统更警觉，不能让系统更武断。
```

---

# 二十一、Shadow Learning 受控经验发现机制

系统不应该完全禁止无监督学习。如果完全禁止，AI 很难像医生一样从大量病例中发现新模式。但医疗场景也不能允许模型从真实病例中自动学习后直接改变临床判断。

因此采用三层机制。

## 21.1 Shadow Learning：影子学习层

可在后台分析：

```text
相似病例聚类
症状组合模式发现
高危病例早期表现挖掘
常见漏问问题统计
诊断修正路径分析
医生驳回原因聚类
随访结局与初始判断差异分析
```

这些结果只进入影子层，不直接影响线上输出。

## 21.2 Experience Candidate：经验候选层

```json
{
  "candidate_experience": "部分年轻胸痛患者在初始对话中表现为焦虑，但若合并气短和近期久坐，最终诊断中肺栓塞比例更高。",
  "supporting_cases": 18,
  "risk_level": "high",
  "suggested_runtime_action": "在年轻胸痛伴气短场景中优先追问久坐、下肢肿痛和咯血。",
  "status": "pending_review"
}
```

经验候选不会直接进入 Runtime，只会进入医生审核或质控流程。

## 21.3 Validated Experience：验证经验层

只有满足以下条件，经验才能进入 Clinical Experience Memory 并影响运行时：

```text
经过医生审核
有足够病例支持
明确适用范围
明确不适用范围
通过离线评估
不增加高危漏放率
版本记录完整
```

---

# 二十二、Clinical Data Maturity Pipeline 临床数据成熟度管线

临床数据不足是现实问题。系统不能一开始就依赖真实医院病历，也不能完全依赖模拟病例自我验证。

## 22.1 数据分层

```text
D0：医学知识数据
来源：指南、教材、临床路径、科普资料、药品说明书。
用途：构建 RAG Evidence Library、Clinical Pathway、KG-lite。
限制：只能提供医学依据，不能当作临床经验。

D1：标准教学病例
来源：公开病例、教材病例、医生手工编写病例。
用途：训练症状群 Rotation、构建病例考试。
限制：偏理想化，不能代表真实用户表达。

D2：模拟患者病例
来源：基于标准病例生成多轮问诊场景，包括表达模糊、焦虑、信息遗漏、前后矛盾等用户类型。
用途：训练问诊策略和 Human-like Interaction。
限制：只能训练流程能力，不能作为真实临床有效性证明。

D3：医生审核病例
来源：医生提供或审核后的脱敏病例。
用途：构建 Clinical Experience Memory，形成经验单元。
限制：需要标注来源、适用范围、审核状态和版本。

D4：真实业务病例与随访结局
来源：真实预问诊、医生反馈、最终诊断、检查结果和随访结果。
用途：Silent Evaluation、真实表现评估、经验进化。
限制：必须脱敏、合规、审计、医生审核，不能直接在线学习。
```

## 22.2 数据用途隔离

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

## 22.3 冷启动方案

```text
第一步：用指南、教材、临床路径构建 D0 知识层。
第二步：用公开病例和医生编写病例构建 D1 标准病例集。
第三步：基于 D1 生成 D2 模拟患者多轮问诊数据。
第四步：用固定评估集对比 LLM-only、RAG-only 和本系统 Runtime。
第五步：在医生审核后，将部分高质量病例复盘转为 D3 经验单元。
第六步：后续进入 Silent Evaluation，逐步积累 D4 真实业务数据。
```

## 22.4 Data Gap Queue 数据缺口队列

系统应主动发现自己在哪些场景数据不足：

```text
某症状群高危病例太少
某类年龄段病例不足
某类主诉表达覆盖不足
某个候选诊断经常召回失败
某个危险信号经常漏问
某类检查建议经常被医生驳回
```

示例：

```json
{
  "gap_id": "chest_pain_gap_012",
  "symptom_group": "chest_pain",
  "gap_type": "high_risk_case_insufficient",
  "description": "肺栓塞相关病例不足，系统在年轻胸痛伴气短场景下追问不稳定。",
  "suggested_action": [
    "补充医生审核病例",
    "生成模拟患者场景",
    "增加病例考试样本",
    "更新 Question/Test Policy"
  ]
}
```

---

# 二十三、Experience Memory Governance 经验记忆治理机制

Clinical Experience Memory 是系统最有价值的模块，也是最大风险来源。如果缺少治理，系统可能错误固化经验、放大个别医生习惯、把少数病例当普遍规律、继续使用过期经验，或与指南冲突。

## 23.1 经验状态

```text
Candidate：经验候选
由 Shadow Learning、医生反馈或错误复盘生成，不能影响线上输出。

Reviewed：医生已审核
医生确认这条经验有临床意义，但还不能直接影响 Runtime。

Validated：已验证经验
通过离线评估，证明不会增加高危漏放率或不安全建议率，可以进入 Runtime。

Deprecated：废弃或降权经验
过期、冲突、表现不稳定或被医生否定后，不再影响 Runtime。
```

状态示例：

```json
{
  "experience_id": "chest_pain_exp_023",
  "status": "validated",
  "source": "doctor_reviewed_case_cluster",
  "reviewer_role": "cardiology_physician",
  "evidence_level": "multi_case_pattern",
  "last_reviewed_at": "2026-06-01",
  "expires_at": "2027-06-01"
}
```

## 23.2 适用边界

每条经验必须写明：

```text
适用于什么场景
不适用于什么场景
证据来源是什么
是否和指南一致
是否只适合医生端展示
是否允许影响患者侧输出
```

示例：

```json
{
  "clinical_lesson": "年轻患者胸痛不能直接归因于焦虑，若合并气短和近期久坐，需要排除肺栓塞。",
  "applicable_scope": [
    "young_or_middle_aged_patient",
    "chest_pain",
    "dyspnea_or_recent_immobility"
  ],
  "not_applicable_when": [
    "clear_trauma_related_chest_pain",
    "pulmonary_embolism_already_excluded",
    "symptoms_fully_explained_by_confirmed_non_urgent_condition"
  ],
  "allowed_runtime_effect": [
    "increase_question_priority",
    "add_experience_alert",
    "raise_triage_attention"
  ],
  "forbidden_runtime_effect": [
    "direct_final_diagnosis",
    "override_guideline",
    "lower_safety_level"
  ]
}
```

## 23.3 知识优先级

```text
安全规则
> 用户明确事实
> 当前权威指南 / 临床路径
> 医生审核经验
> 相似病例
> LLM 假设
```

经验与指南冲突时：

```text
不能自动使用经验。
必须标记 conflict。
进入医生审核。
患者侧不展示。
医生端只作为经验性提醒展示。
```

## 23.4 回归测试和回滚

每次新增经验、修改经验或调整经验权重，都必须跑病例回归测试。

观察：

```text
高危病例漏放率是否上升
不安全建议率是否上升
过度诊断率是否上升
输出边界正确率是否下降
医生摘要是否变差
追问轮数是否异常增加
```

经验必须像代码一样版本管理，支持：

```text
单条经验下线
某个症状群经验回滚
某个版本整体回滚
自动降低相关能力等级
```

## 23.5 经验污染监控

污染信号：

```text
某条经验被异常频繁触发
某类经验导致系统过度诊断
某个医生来源的经验被大量驳回
某个症状群经验与指南冲突增多
经验触发后医生采纳率持续下降
经验触发后不安全建议率上升
```

处理：

```text
自动降权
暂停触发
进入人工复审
回滚版本
降低症状群能力等级
```

---

# 二十四、企业级运行模式

## 24.1 Patient-facing Mode

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

## 24.2 Clinician Copilot Mode

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

## 24.3 Silent Evaluation Mode

AI 生成判断但不影响医生决策。

系统记录：

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

# 二十五、Review & Recertification 复盘与再认证

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

错误类型：

```text
主诉理解错误
危险信号漏问
候选诊断漏召回
证据归因错误
检查建议不合理
经验提醒误触发
输出越权
过度诊断
低估风险
沟通表达不清
```

如果新经验或新版本导致高危漏放率上升，系统应降低对应症状群权限，而不是自动升级能力。

---

# 二十六、FailurePolicy 医疗 Fail-safe 策略

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

患者端输出：

```text
当前信息不足或系统无法完成安全评估，建议咨询医生或及时就医。
```

医生端输出：

```text
安全模块执行失败，当前 AI 输出不可作为诊断参考。
```

---

# 二十七、短期对话上下文与实时交互

ClinMindRuntime 需要区分短期对话上下文和长期诊断状态。

```text
Redis：保存短期对话上下文
数据库：保存 RuntimeState 长期状态
内存存储：开发环境或 Redis 不可用时的临时降级
```

示例：

```json
{
  "runtime_id": "rt_001",
  "conversation_history": [
    {
      "role": "user",
      "content": "我最近胸口闷"
    },
    {
      "role": "assistant",
      "content": "胸闷是在活动后更明显，还是休息时也会出现？"
    }
  ],
  "last_question_field": "symptom_trigger",
  "last_updated_at": "2026-06-25T10:00:00+09:00"
}
```

对话历史最多保留最近 20 条，但诊断判断不能依赖对话历史。

```text
短期上下文用于语言连续性。
RuntimeState 用于诊断决策。
```

实时交互建议：

```text
HTTP：用于 start / continue / status / result / trace
SSE：优先用于患者端流式返回安全表达
WebSocket：可作为后续全双工医生端调试和实时状态观察
```

---

# 二十八、评估体系

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

医生 Rubric：

```text
病史采集是否充分
是否抓住关键危险信号
候选诊断是否合理
追问是否有临床目的
检查建议是否合理
经验提醒是否有价值
表达是否自然但不过度承诺
是否适合给患者看
是否适合给医生参考
```

第一阶段最小评估集：

```text
症状群：胸痛 / 发热
病例数量：50-100 个
病例类型：普通病例、高危病例、信息缺失病例、误导表达病例
对比对象：LLM-only、RAG-only、ClinMindRuntime
```

核心对比指标：

```text
危险信号识别率
高危病例漏放率
必问问题覆盖率
输出越权率
医生摘要可用性
平均追问轮数
```

---

# 二十九、参考对标与吸收点

## 29.1 AMIE

启发：诊断对话不是闲聊，而是目标导向的信息采集。

吸收为：

```text
CaseFrame
Question / Test Policy
Human-like Interaction Layer
```

## 29.2 多模态诊断对话

启发：真实医生不仅听主诉，还看检查、图像、病历和病程变化。

吸收为：

```text
多源输入
检查报告解析
影像 / 图片输入扩展
随访结果进入经验记忆
```

第一阶段先做文本和检查报告，后续扩展图片、心电、影像报告和病历文档。

## 29.3 MAI-DxO

启发：诊断是顺序决策，不是一次性回答。

吸收为：

```text
Question / Test Policy
Differential Diagnosis Board
EvidenceGraph
DecisionBoundary
```

## 29.4 HealthBench

启发：医疗 AI 评估不能只看准确率。

吸收为：

```text
医生 Rubric
行为级评估
沟通自然度评估
不确定性表达评估
输出边界正确率
```

## 29.5 OpenEvidence

启发：医生需要可信证据，而不是黑箱答案。

吸收为：

```text
RAG Evidence Library
证据引用
医生端 EvidenceGraph
知识源版本管理
```

## 29.6 Hippocratic AI

启发：患者侧医疗 Agent 必须有安全边界。

吸收为：

```text
Patient-facing Mode 限权
SafetyGate
DecisionBoundary
人工升级机制
高危场景禁止普通建议
```

## 29.7 福棠·百川 / 小儿方

启发：国内医疗 AI 落地更现实的路径是“真实医生 + AI 医生”的双医生制，而不是一开始让 AI 替代医生。

吸收为：

```text
Clinician Copilot Mode
Silent Evaluation Mode
医生反馈闭环
能力档案与分级授权
```

## 29.8 AI for Medicine

启发：医学 AI 应拆成诊断、预后预测和治疗建议等不同任务。

吸收为：

```text
第一阶段聚焦诊断支持
第二阶段扩展风险预测
第三阶段在医生端支持治疗方案参考
```

患者端不做治疗和处方，医生端只做辅助参考。

## 29.9 Stanford CS337

启发：医疗 AI 要关注真实世界影响。

吸收为：

```text
Patient-facing Mode
Clinician Copilot Mode
Silent Evaluation Mode
审计日志
医生反馈
真实业务验证
```

## 29.10 Berkeley LLM Agents

启发：Agent 重点是状态、规划、工具、评估和人机协作。

吸收为：

```text
State Orchestrator
CaseFrame
EvidenceGraph
Question / Test Policy
Evaluation Agent
Human-like Interaction Layer
```

---

# 三十、MVP 实现范围

## 30.1 P0 必做

```text
Runtime API
RuntimeState
RuntimeStatus
EntryAssessment
CaseFrame
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Patient-facing / Clinician-facing 输出区分
RuntimeTrace
FailurePolicy
小型病例评估集
```

## 30.2 P1 第二阶段

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

## 30.3 P2 后续扩展

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

## 30.4 第一阶段不要做

```text
治疗方案患者端输出
处方建议
复杂 Nacos 微服务治理
过度完整的 Docker Compose
大规模真实病例经验记忆
自动在线学习
```

## 30.5 从旧项目第一阶段真正拿过来的内容

```text
1. start / continue / status / result / trace 这组 API
2. RuntimeStatus 状态枚举
3. RuntimeState 持久化结构
4. CaseFrame 最小字段集
5. required / important / optional 字段分级
6. Redis 短期对话上下文
7. 最近 20 条对话历史限制
8. RuntimeTrace 追踪结构
9. EntryAssessment 的 wellness_mode / clinical_mode 分流思想
10. FailurePolicy 的服务降级分类
```

## 30.6 第一阶段不要从旧项目拿的内容

```text
1. treatment-engine-service
2. OCR
3. 完整微服务拆分
4. Nacos
5. 复杂 Docker Compose
6. 固定五步 Workflow
7. 加权融合分数作为最终判断
8. “五脑思想”表达
```

---

# 三十一、推荐工程架构

第一阶段建议轻量化，不要直接复刻旧项目完整微服务。

```text
clinmind-runtime/
├── docs/
├── runtime-orchestrator/
│   ├── api/
│   ├── state/
│   ├── safety/
│   ├── boundary/
│   ├── trace/
│   └── policy/
├── reasoning-service/
│   ├── case_frame/
│   ├── ddx/
│   ├── evidence_graph/
│   └── prompts/
├── platform/
│   ├── training_center/
│   ├── experience_center/
│   ├── evaluation_center/
│   └── audit_center/
├── evaluation/
│   ├── cases/
│   ├── rubrics/
│   └── reports/
└── docker-compose.yml
```

第一阶段技术建议：

```text
FastAPI：快速实现 Runtime API 和 AI 服务
SQLite / PostgreSQL：保存 RuntimeState
Redis：短期对话上下文
本地 JSON / YAML：症状群规则、红旗规则、评估病例
```

后续扩展：

```text
Spring Boot Orchestrator
Neo4j KG-lite
向量数据库
独立评估服务
医生审核后台
```

---

# 三十二、核心亮点

## 32.1 Clinical Experience Memory

系统不仅有知识库，还有审核后的病例经验、误诊教训和医生点评，使 AI 能像医生一样从病例中积累经验。

## 32.2 受控无监督经验发现

系统允许在 Shadow Learning 中进行无监督 / 自监督模式发现，但只有通过医生审核和再认证的经验才能影响 Runtime。

## 32.3 医生培养机制进入 Runtime

训练、病例考试、能力档案、运行权限和再认证直接决定系统能输出什么。

## 32.4 EvidenceGraph 证据状态图

每个候选诊断都有支持证据、反对证据、缺失证据、经验提醒、推荐追问和推荐检查。

## 32.5 Human-like Interaction Layer

系统不是机械问诊，而是解释为什么问、表达不确定性、根据用户情绪调整语气，同时不改变医学安全判断。

## 32.6 DecisionBoundary 输出边界

系统先判断是否允许输出，再生成回答，避免信息不足或高危场景中越权回答。

## 32.7 三种企业级运行模式

患者端、医生 Copilot、Silent Evaluation 分别服务患者沟通、医生辅助和真实验证。

## 32.8 平台化治理能力

训练中心、经验记忆中心、评估再认证中心和审计治理中心，使系统不是一个 Demo，而是一个可持续演进的平台。

---

# 三十三、面试讲法

## 33.1 项目一句话

> ClinMindRuntime 是一个医疗问诊场景下的受控诊断 Runtime。它不是让大模型直接诊断，而是把问诊过程拆成病例状态、危险信号、候选诊断、证据图、动态追问和输出边界，让模型在可控流程中完成诊断支持。

## 33.2 与普通 RAG 医疗问答的区别

普通 RAG：

```text
用户问题 → 检索知识 → 拼 Prompt → LLM 回答
```

ClinMindRuntime：

```text
用户输入 → CaseFrame → SafetyGate → DDx Board → EvidenceGraph → Question/Test Policy → DecisionBoundary → 分角色输出
```

区别：

```text
1. 不直接回答，而是维护诊断状态
2. 不只检索知识，而是组织证据关系
3. 不只生成内容，而是先判断输出权限
4. 不只单轮回答，而是可复盘、可评估、可再认证
```

## 33.3 与旧 AIdoctor 的关系

> 旧项目 AIdoctor 已经实现过 CDP 状态管理、主动问诊、多引擎诊断、证据链分析和执行追踪。但我复盘后发现，旧项目更像固定 Workflow，主要解决“有哪些服务”和“流程怎么跑通”。ClinMindRuntime 是在这个基础上的架构升级，重点从功能服务集合转向状态驱动的诊断 Runtime，强调 SafetyGate、EvidenceGraph、DecisionBoundary 和复盘再认证。

## 33.4 当前边界

> 当前版本主要验证 Runtime 机制，包括状态管理、危险信号识别、证据图、动态追问和输出边界。Clinical Experience Memory、Silent Evaluation、真实医生审核和随访结局是后续真实业务接入后的扩展闭环，我不会把当前版本描述成已经临床有效的产品。

---

# 三十四、最终定义

ClinMindRuntime 是一个企业级智能诊断训练—运行—经验进化平台。

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
