# ClinMindRuntime 完整系统设计

> 项目名称：ClinMindRuntime  
> 中文定位：企业级智能诊断训练—运行—经验进化平台  
> 核心定义：面向医疗问诊场景的受控诊断 Runtime，而不是普通医疗问答系统，也不是替代医生的自主诊断系统。

---

## 0. 当前实现边界说明

ClinMindRuntime 的完整目标是构建一个可训练、可授权、可追踪、可复盘、可经验进化的企业级智能诊断 Runtime。

但在项目第一阶段，系统不应被描述为已经具备真实临床诊断能力的医疗产品。第一阶段重点验证的是：

```text
1. 如何把一次问诊过程显式状态化
2. 如何让大模型在受控 Runtime 中工作
3. 如何优先识别高危信号
4. 如何维护候选诊断和证据关系
5. 如何根据风险、证据和能力边界控制输出
6. 如何把每一步诊断行为记录下来，供后续医生复盘和再认证
```

当前阶段可实现的核心能力：

```text
已实现 / 第一阶段应实现：
- Runtime API
- RuntimeState
- CaseFrame
- EntryAssessment
- SafetyGate
- Differential Diagnosis Board
- EvidenceGraph
- Question / Test Policy
- DecisionBoundary
- Patient-facing / Clinician-facing 输出区分
- RuntimeTrace
- 小型病例评估集

原型实现 / 第二阶段扩展：
- Clinical Experience Memory
- 医生反馈记录
- Silent Evaluation
- 病例复盘
- Capability Profile

后续真实业务扩展：
- 真实医生审核经验
- 随访结局接入
- Shadow Learning
- Review & Recertification
- 临床合规部署
- 医疗器械注册或医疗软件合规评估
```

面试中推荐表述：

> 当前项目不是证明 AI 能替代医生，而是验证医疗问诊场景下如何构建一个受控诊断 Runtime：让大模型不能直接自由诊断，而是在病例状态、危险信号、候选诊断、证据图和输出边界的约束下完成诊断支持。

---

# 一、项目定位

## 1.1 项目一句话定义

ClinMindRuntime 是一个面向多症状群、多知识源、多角色、多评估闭环的智能诊断 Runtime 平台。

它的目标不是让大模型直接回答医学问题，而是让 AI 像受训医生一样，在明确能力范围和安全边界内完成：

```text
病例理解
主动问诊
危险信号识别
候选诊断组织
证据收集与归因
检查建议
患者端安全沟通
医生端诊断辅助
医生反馈复盘
经验记忆更新
能力再认证
```

系统核心不是：

```text
医学知识 + RAG + Prompt
```

而是：

```text
医学知识 + 临床经验 + 诊断状态 + 输出边界 + 复盘进化
```

## 1.2 项目不做什么

ClinMindRuntime 第一阶段明确不做：

```text
1. 不替代医生给出最终诊断
2. 不在患者端输出确定诊断
3. 不在患者端输出处方或治疗方案
4. 不让 Shadow Learning 直接影响线上判断
5. 不把模拟病例伪装成真实临床验证
6. 不把大模型输出当作唯一诊断依据
7. 不把相似病例直接当作当前病例结论
```

系统的定位是：

```text
患者端：风险提示、信息补全、就医建议、健康教育
医生端：候选诊断、证据状态、相似经验提醒、检查建议、医生摘要
后台端：病例复盘、错误归因、经验候选发现、再认证评估
```

---

# 二、系统要解决的问题

普通医疗大模型问诊的主要问题不是完全没有医学知识，而是缺少稳定、安全、可追踪的诊断机制。

核心问题包括：

```text
1. 多轮问诊状态容易丢失
2. 追问容易机械化，缺少诊断目的
3. 候选诊断和证据关系不可追踪
4. 信息不足时容易过度下结论
5. 高危低概率疾病容易被忽略
6. 不知道自己在哪些症状群有能力，哪些没有能力
7. 缺少真实病例经验沉淀机制
8. 出错后无法像医生一样复盘和成长
9. 患者端输出边界不清，容易越权
10. 医生端缺少可审计的证据链和推理路径
```

ClinMindRuntime 要解决的问题是：

> 如何让 AI 在明确能力范围内，围绕诊断目标收集证据，参考审核后的临床经验，动态决定下一步追问或检查建议，并在患者端和医生端分别输出安全、可解释、可追踪的内容。

---

# 三、总体设计原则

## 3.1 状态优先

系统不依赖对话历史隐式记忆，而是将一次问诊过程显式维护为 RuntimeState。

```text
对话历史只用于语言连续性。
诊断判断必须基于 RuntimeState。
```

## 3.2 安全优先

SafetyGate 和 DecisionBoundary 优先于自然语言生成。

```text
不是先生成答案再加免责声明，
而是先判断当前是否允许输出。
```

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

高危候选不能因为置信度低就删除。

```text
高危低概率疾病必须保留为 must_not_miss 或 need_to_rule_out。
```

## 3.5 经验受控

Clinical Experience Memory 只能让系统更警觉，不能让系统更武断。

```text
经验可以影响追问优先级、检查建议和输出边界收紧，
不能直接决定最终诊断。
```

## 3.6 人机分层

患者端和医生端必须有不同输出边界。

```text
患者端：风险提示、就医建议、需要补充的信息、健康教育
医生端：候选诊断、证据图、相似经验提醒、检查建议、诊断摘要
```

## 3.7 可追踪、可复盘、可回滚

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

# 四、系统总体架构

## 4.1 核心模块

ClinMindRuntime 由十二个核心模块组成：

```text
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
```

## 4.2 总体运行链路

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
Review & Recertification 审核和再认证
  ↓
Clinical Experience Memory 更新
  ↓
Capability Profile 更新
```

## 4.3 与旧 AIdoctor 项目的关系

旧版 AIdoctor 项目提供了若干工程补充，而不是新项目的架构主线。

直接吸收的工程落点包括：

```text
Runtime API：start / continue / status / result / trace
RuntimeStatus：一次问诊的状态生命周期
RuntimeState：由旧 CDP 字段经验升级而来
CaseFrame 最小字段集：来自旧项目信息缺口字段
EntryAssessment：wellness_mode / clinical_mode 分流思想
Redis 对话上下文：用于短期语言连续性
RuntimeTrace：由旧执行追踪升级而来
FailurePolicy：由旧服务降级机制升级为医疗 fail-safe
```

不直接吸收的内容包括：

```text
五脑思想表达
固定五步 Workflow
治疗推理患者端能力
OCR 作为第一阶段核心
加权融合分数作为最终判断
完整微服务拆分和 Nacos
```

---

# 五、Runtime API 设计

Runtime API 将抽象的诊断 Runtime 产品化。

## 5.1 API 列表

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/continue
GET  /api/v1/runtime/{runtimeId}/status
GET  /api/v1/runtime/{runtimeId}/result
GET  /api/v1/runtime/{runtimeId}/trace
```

## 5.2 start

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

## 5.3 continue

用于提交用户补充信息，推进 Runtime 一轮。

请求示例：

```json
{
  "runtime_id": "rt_001",
  "input": {
    "text": "有点出汗，走路快的时候更明显，休息会缓解"
  }
}
```

系统收到后执行一轮 Runtime：

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

## 5.4 status

返回当前 Runtime 状态。

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

## 5.5 result

返回当前允许展示的结果。

患者端和医生端结果必须分离。

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

## 5.6 trace

返回 Runtime 过程追踪，供医生端、调试、复盘和评估使用。

---

# 六、RuntimeStatus 状态生命周期

RuntimeStatus 表示一次问诊 Runtime 当前处于什么状态。

```text
created
entry_assessing
wellness_mode
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

# 七、RuntimeState 设计

RuntimeState 是一次问诊的中心状态对象。

它吸收旧项目 CDP 的工程字段经验，但在 ClinMindRuntime 中被重新定义为诊断 Runtime 状态内核。

## 7.1 RuntimeState 示例

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

## 7.2 核心原则

```text
1. RuntimeState 是诊断状态的唯一事实源
2. 对话历史不能替代 RuntimeState
3. LLM 输出必须写回 RuntimeState 后才允许影响下一轮
4. 每次状态变化必须生成 RuntimeTrace
5. 重要状态变更必须带版本号，支持回滚和复盘
```

---

# 八、EntryAssessment 入口工作态判定

EntryAssessment 负责判断当前用户输入是否应该进入临床诊疗 Runtime。

## 8.1 工作态分类

```text
wellness_mode：健康管理、日常咨询、轻症科普、非诊断需求
clinical_mode：存在明确症状，需要问诊支持
ergency_hint：疑似急症，直接触发 SafetyGate
unsupported：超出系统能力范围，建议咨询医生或改写输入
```

## 8.2 输出结构

```json
{
  "work_mode": "clinical_mode",
  "reason": "用户描述胸闷且活动后加重，需要进入临床问诊流程",
  "risk_level": "medium_high",
  "red_flags": ["活动后加重"],
  "next_runtime_status": "collecting_case_info"
}
```

## 8.3 与 SafetyGate 的区别

```text
EntryAssessment：是否进入临床问诊
SafetyGate：进入后是否命中高危风险，需要收紧输出
```

---

# 九、Symptom Rotation Library 症状群训练库

系统不一开始承诺覆盖所有疾病，而是按症状群逐步训练和验证。

第一阶段建议覆盖：

```text
发热
咳嗽 / 呼吸困难
腹痛 / 呕吐 / 腹泻
胸痛 / 胸闷
头痛 / 眩晕
```

每个症状群包含：

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

示例：胸痛 rotation

```text
常见方向：胃食管反流、肌肉骨骼痛、焦虑相关胸闷
高危方向：急性冠脉综合征、主动脉夹层、肺栓塞、张力性气胸
必问问题：放射痛、大汗、气短、活动后加重、下肢肿痛、近期制动
关键检查：心电图、肌钙蛋白、D-二聚体、胸部影像
```

---

# 十、Capability Profile 能力档案

Capability Profile 决定系统在某个症状群下能输出到什么程度。

## 10.1 能力等级

```text
L1：只能做病史结构化
L2：可以做危险信号识别和就医分级
L3：可以给医生端候选诊断方向
L4：低风险、信息充分场景下给初步判断参考
L5：严格验证后的受控自主诊断，仅限特定闭环场景
```

## 10.2 示例

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

## 10.3 运行时约束

```text
如果 chest_pain 只有 L2，患者端不能输出候选诊断。
如果 fever 达到 L3，也只能在医生端展示候选方向。
如果存在高危信号，即使能力等级较高，也必须受 SafetyGate 限制。
```

---

# 十一、CaseFrame 病例状态

CaseFrame 将用户输入转换为结构化病例状态。

## 11.1 第一版最小字段集

```json
{
  "chief_complaint": null,
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

## 11.2 字段分级

第一版可以吸收旧项目的信息缺口分级：

```text
Required：
- chief_complaint
- symptom_trigger
- symptom_duration

Important：
- symptom_severity
- symptom_location
- symptom_frequency
- associated_symptoms

Optional：
- family_history
- past_history
- medication_history
- allergy_history
```

## 11.3 注意

字段完整度只是辅助指标，不能直接决定是否可以诊断。

核心判断必须基于：

```text
高危信号是否存在
关键证据是否缺失
高危候选是否排除
系统能力等级是否允许
DecisionBoundary 是否允许输出
```

---

# 十二、SafetyGate 危险信号识别

SafetyGate 是 Runtime 的硬安全模块，优先于候选诊断和自然语言生成。

## 12.1 输入

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

## 12.2 输出

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

## 12.3 规则

```text
1. SafetyGate 命中高危后，患者端不能输出低风险判断
2. SafetyGate 失败时必须 fail-safe
3. SafetyGate 规则必须有来源：临床路径、指南、医生配置或已验证经验
4. LLM 可以辅助识别表达，但不能覆盖高危规则
```

---

# 十三、Differential Diagnosis Board 候选诊断状态板

系统不直接输出唯一诊断，而是维护候选诊断池。

## 13.1 结构

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

## 13.2 候选状态

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

## 13.3 原则

```text
1. 高危候选不能因低分删除
2. 常见病可以排序，高危病必须保留
3. 候选诊断必须绑定证据状态
4. 患者端是否展示候选诊断由 DecisionBoundary 决定
```

---

# 十四、EvidenceGraph 证据状态图

EvidenceGraph 是诊断控制层，不只是解释层。

## 14.1 结构

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

## 14.2 证据来源结构

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

## 14.3 作用

EvidenceGraph 决定：

```text
下一步问什么
是否建议检查
是否允许输出诊断方向
是否需要转医生
是否禁止患者端诊断标签
```

---

# 十五、Question / Test Policy 动态追问与检查建议

医生诊断不是机械补字段，而是围绕候选诊断和缺失证据动态决策。

## 15.1 优先级

```text
危险信号
> 高危疾病排除
> 经验记忆提醒
> 高信息增益问题
> 关键检查建议
> 常见病鉴别
> 普通病史补全
```

## 15.2 动作结构

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

---

# 十六、DecisionBoundary 输出边界

DecisionBoundary 决定当前能输出什么。

## 16.1 输入

```text
Capability Profile
SafetyGate
EvidenceGraph
Differential Diagnosis Board
Clinical Experience Memory
当前模式 patient / clinician / silent
FailurePolicy
```

## 16.2 输出等级

```text
O1：继续追问
O2：风险提示
O3：医生端候选诊断方向
O4：低风险初步判断参考
O5：门诊 / 急诊建议
O6：转人工医生
O7：医生端完整鉴别诊断报告
```

## 16.3 示例

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

核心原则：

```text
先判断是否允许输出，再生成回答。
```

---

# 十七、Human-like Interaction Layer 类医生表达层

Human-like Interaction Layer 只负责表达，不负责医学决策。

## 17.1 能力

```text
1. 解释为什么问
2. 表达不确定性
3. 根据用户情绪调整语气
4. 用审核后的临床经验提醒风险
```

## 17.2 边界

```text
不能改变 SafetyGate
不能改变 DecisionBoundary
不能新增诊断结论
不能为了安抚用户弱化风险
不能将医生端内容泄露到患者端
不能编造临床经验
```

示例：

```text
目前还不能先按胃部问题处理，因为胸痛、出汗和气短这些信息提示需要先排除心血管风险。我们先把最危险的情况排除掉，再看是否更像胃食管反流或肌肉疼痛。
```

---

# 十八、Clinical Experience Memory 临床经验记忆

Clinical Experience Memory 不是普通聊天记忆，也不是原始病例库，而是经过医生审核、质控和再认证的经验单元。

## 18.1 经验单元示例

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
  "source": "doctor_reviewed_case",
  "validation_status": "approved_for_runtime_reference",
  "applicable_scope": "young_or_middle_aged_chest_pain_with_dyspnea"
}
```

## 18.2 经验状态

```text
Candidate：经验候选，不能影响线上输出
Reviewed：医生已审核，但不能直接影响 Runtime
Validated：通过离线评估，可进入 Runtime
Deprecated：废弃或降权，不再影响 Runtime
```

## 18.3 使用边界

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

# 十九、Shadow Learning 受控经验发现

Shadow Learning 允许系统从病例中发现模式，但不允许直接改变线上行为。

## 19.1 可做分析

```text
相似病例聚类
症状组合模式发现
高危病例早期表现挖掘
常见漏问问题统计
诊断修正路径分析
医生驳回原因聚类
随访结局与初始判断差异分析
```

## 19.2 流程

```text
真实病例 / Silent Evaluation
  ↓
Shadow Learning 发现经验候选
  ↓
Experience Candidate
  ↓
医生审核
  ↓
离线评估
  ↓
Validated Experience
  ↓
Clinical Experience Memory
```

---

# 二十、Clinical Data Maturity Pipeline 数据成熟度管线

医疗数据不能一开始就假设有大量真实临床病例。

系统将数据分为五级：

```text
D0：医学知识数据
D1：标准教学病例
D2：模拟患者病例
D3：医生审核病例
D4：真实业务病例与随访结局
```

## 20.1 用途隔离

```text
Knowledge Data：用于 RAG、KG、Clinical Pathway
Training Data：用于训练问诊策略和症状群 Rotation
Evaluation Data：用于固定评估，不能被训练看到
Experience Data：用于 Clinical Experience Memory，必须医生审核
```

## 20.2 冷启动路径

```text
D0 指南和教材知识
→ D1 标准病例
→ D2 模拟患者多轮问诊
→ 小型固定评估集
→ 医生审核形成 D3 经验
→ Silent Evaluation 积累 D4
```

---

# 二十一、Review & Recertification 复盘与再认证

真实问诊后，医生反馈和随访结局进入复盘流程。

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

# 二十二、FailurePolicy 医疗 Fail-safe 策略

旧项目有服务降级经验，但医疗场景不能所有失败都继续流程。

## 22.1 失败处理规则

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

## 22.2 安全失败状态

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

# 二十三、短期对话上下文

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

---

# 二十四、企业级运行模式

## 24.1 Patient-facing Mode

允许输出：

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

允许输出：

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

# 二十五、评估体系

系统评估不能只看最终诊断命中率，而要看完整诊断行为。

## 25.1 指标

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

## 25.2 第一阶段最小评估集

建议第一阶段构造小型评估集：

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

# 二十六、MVP 实现范围

第一阶段不追求完整医疗平台，而是跑通诊断 Runtime 最小闭环。

## 26.1 P0 必做

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

## 26.2 P1 第二阶段

```text
RAG Evidence Library
KG-lite
Clinical Pathway
Capability Profile
医生端报告
Clinical Experience Memory 原型
Silent Evaluation 原型
```

## 26.3 P2 后续扩展

```text
医生审核流程
随访结局接入
Shadow Learning
Review & Recertification
OCR / 检查报告解析
多模态输入
真实业务病例评估
```

## 26.4 第一阶段不要做

```text
治疗方案患者端输出
处方建议
复杂 Nacos 微服务治理
过度完整的 Docker Compose
大规模真实病例经验记忆
自动在线学习
```

---

# 二十七、推荐工程架构

第一阶段建议轻量化，不要直接复刻旧项目的完整微服务。

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
├── evaluation/
│   ├── cases/
│   ├── rubrics/
│   └── reports/
└── docker-compose.yml
```

第一阶段可以采用：

```text
FastAPI：快速实现 Runtime API 和 AI 服务
SQLite / PostgreSQL：保存 RuntimeState
Redis：短期对话上下文
本地 JSON / YAML：症状群规则、红旗规则、评估病例
```

后续再扩展：

```text
Spring Boot Orchestrator
Neo4j KG-lite
向量数据库
独立评估服务
医生审核后台
```

---

# 二十八、面试讲法

## 28.1 项目一句话

> ClinMindRuntime 是一个医疗问诊场景下的受控诊断 Runtime。它不是让大模型直接诊断，而是把问诊过程拆成病例状态、危险信号、候选诊断、证据图、动态追问和输出边界，让模型在可控流程中完成诊断支持。

## 28.2 与普通 RAG 医疗问答的区别

普通 RAG：

```text
用户问题 → 检索知识 → 拼 Prompt → LLM 回答
```

ClinMindRuntime：

```text
用户输入 → CaseFrame → SafetyGate → DDx Board → EvidenceGraph → Question/Test Policy → DecisionBoundary → 分角色输出
```

区别在于：

```text
1. 不直接回答，而是维护诊断状态
2. 不只检索知识，而是组织证据关系
3. 不只生成内容，而是先判断输出权限
4. 不只单轮回答，而是可复盘、可评估、可再认证
```

## 28.3 与旧 AIdoctor 的关系

> 旧项目 AIdoctor 已经实现过 CDP 状态管理、主动问诊、多引擎诊断、证据链分析和执行追踪。但我复盘后发现，旧项目更像固定 Workflow，主要解决“有哪些服务”和“流程怎么跑通”。ClinMindRuntime 是在这个基础上的架构升级，重点从功能服务集合转向状态驱动的诊断 Runtime，强调 SafetyGate、EvidenceGraph、DecisionBoundary 和复盘再认证。

## 28.4 当前项目边界

> 当前版本主要验证 Runtime 机制，包括状态管理、危险信号识别、证据图、动态追问和输出边界。Clinical Experience Memory、Silent Evaluation、真实医生审核和随访结局是后续真实业务接入后的扩展闭环，我不会把当前版本描述成已经临床有效的产品。

---

# 二十九、最终定义

ClinMindRuntime 是一个企业级智能诊断训练—运行—经验进化平台。

它通过：

```text
症状群 Rotation
Capability Profile
RuntimeState
CaseFrame
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Human-like Interaction Layer
Clinical Experience Memory
Shadow Learning
Review & Recertification
Evaluation System
```

让 AI 在安全边界内逐步积累经验，并在问诊过程中表现得更像一个受训医生：

```text
知道当前病例是什么状态
知道有哪些候选诊断
知道哪些证据支持或反对
知道还缺什么关键信息
知道下一步该问什么或建议什么检查
知道什么时候不能继续在线判断
知道患者端和医生端能输出什么
知道出错后如何复盘和再认证
```

系统的核心价值不是替代医生，而是让医疗 AI 从普通问答升级为：

```text
可训练
可授权
可追踪
可动态决策
可安全输出
可自然沟通
可复盘
可经验进化
```

的诊断 Runtime。
