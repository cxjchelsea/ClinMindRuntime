# Phase 1 Runtime MVP 实现规格

> 本文档是 `ClinMindRuntime阶段拆分路线图.md` 中 Phase 1 的详细设计文档。  
> 总设计文档回答“完整系统是什么”，阶段路线图回答“分几步实现”，本文档回答“第一阶段具体怎么实现”。

---

# 一、Phase 1 定位

Phase 1 的目标不是做完整医疗 AI 平台，也不是做完整 RAG、知识图谱、经验记忆或医生审核后台。

Phase 1 的核心目标是：

```text
跑通一个最小受控诊断 Runtime。
```

也就是说，系统需要证明：

```text
用户输入症状后，系统不是直接让 LLM 回答，
而是创建一次 Runtime，维护病例状态，识别风险，构建候选诊断，组织证据图，决定下一步追问或检查建议，并根据输出边界生成患者端或医生端内容。
```

Phase 1 的核心闭环：

```text
用户输入
  ↓
Runtime API
  ↓
EntryAssessment
  ↓
RuntimeState / RuntimeStatus
  ↓
CaseFrame
  ↓
Knowledge Context（静态规则）
  ↓
Experience Context（空实现 / mock）
  ↓
SafetyGate
  ↓
Differential Diagnosis Board
  ↓
EvidenceGraph
  ↓
Question / Test Policy
  ↓
DecisionBoundary
  ↓
Patient Output / Clinician Report
  ↓
RuntimeTrace
```

---

# 二、Phase 1 做什么与不做什么

## 2.1 Phase 1 必做

```text
1. Runtime API
2. RuntimeStatus
3. RuntimeState
4. RuntimeTrace
5. EntryAssessment
6. CaseFrame
7. Knowledge Context 静态规则读取
8. Experience Context 空实现 / mock 实现
9. SafetyGate
10. Differential Diagnosis Board
11. EvidenceGraph
12. Question / Test Policy
13. DecisionBoundary
14. Patient-facing Output
15. Clinician-facing Report
16. FailurePolicy
17. 最小测试病例集
```

## 2.2 Phase 1 不做

```text
不做完整 RAG Evidence Library
不做完整 KG-lite
不做真实 Clinical Experience Memory
不做真实医生审核
不做随访结局接入
不做 Shadow Learning
不做 Training Center 后台
不做 Evaluation Center 后台
不做 Experience Memory Center 后台
不做完整权限系统
不做患者端确定诊断
不做处方建议
不做治疗方案自动生成
```

## 2.3 Phase 1 的合理降级

为了让 Phase 1 可以快速落地，允许以下降级：

```text
Knowledge Context：先读取本地 JSON / YAML 规则。
Experience Context：先返回空列表，或返回少量 mock 经验。
Capability Profile：先用静态配置，不做训练生成。
存储：先用内存 / JSON / SQLite，不强制上复杂数据库。
前端：可以先用 Swagger / Postman / 简单调试页面验证。
LLM：可选，不作为 Phase 1 的核心依赖。
```

---

# 三、推荐工程目录

Phase 1 建议先使用单体后端结构，避免一开始过度微服务化。

```text
clinmind-runtime/
├── app/
│   ├── main.py
│   ├── api/
│   │   └── runtime_api.py
│   ├── state/
│   │   ├── runtime_state.py
│   │   ├── runtime_status.py
│   │   └── runtime_trace.py
│   ├── entry/
│   │   └── entry_assessment.py
│   ├── case/
│   │   └── case_frame.py
│   ├── knowledge/
│   │   ├── knowledge_context.py
│   │   └── static_rule_provider.py
│   ├── experience/
│   │   └── experience_context.py
│   ├── safety/
│   │   └── safety_gate.py
│   ├── reasoning/
│   │   ├── differential_board.py
│   │   ├── evidence_graph.py
│   │   └── question_test_policy.py
│   ├── boundary/
│   │   ├── decision_boundary.py
│   │   ├── capability_profile_provider.py
│   │   └── failure_policy.py
│   ├── output/
│   │   ├── patient_output.py
│   │   └── clinician_report.py
│   └── storage/
│       ├── runtime_store.py
│       └── rule_store.py
├── assets/
│   ├── symptom_groups/
│   │   ├── chest_pain.yml
│   │   └── fever.yml
│   ├── red_flag_rules.yml
│   ├── test_recommendation_rules.yml
│   └── capability_profiles.yml
├── tests/
│   ├── cases/
│   │   ├── chest_pain_cases.yml
│   │   └── fever_cases.yml
│   └── test_runtime_flow.py
└── docs/
```

---

# 四、Runtime API 设计

## 4.1 创建 Runtime

```text
POST /api/v1/runtime/start
```

请求：

```json
{
  "session_id": "s_001",
  "user_id": "u_001",
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

响应：

```json
{
  "runtime_id": "rt_001",
  "runtime_status": "collecting_case_info",
  "work_mode": "clinical_mode",
  "risk_level": "medium_high",
  "next_action": {
    "type": "ask_question",
    "content": "胸闷时是否伴随出汗、气短，或者疼痛向左肩、后背放射？",
    "purpose": "rule_out_high_risk_diagnosis"
  },
  "patient_output": {
    "allowed": true,
    "content": "你提到胸闷在活动后更明显，这类情况需要先补充几个关键问题来判断风险。"
  }
}
```

## 4.2 继续 Runtime

```text
POST /api/v1/runtime/continue
```

请求：

```json
{
  "runtime_id": "rt_001",
  "input": {
    "text": "有点出汗，走路快的时候更明显，休息会缓解"
  }
}
```

响应字段与 start 类似，但必须包含最新 RuntimeStatus、DecisionBoundary 和 RuntimeTrace ID。

## 4.3 查询 Runtime 状态

```text
GET /api/v1/runtime/{runtime_id}/status
```

返回：

```json
{
  "runtime_id": "rt_001",
  "runtime_status": "collecting_evidence",
  "work_mode": "clinical_mode",
  "risk_level": "high",
  "current_step": "question_test_policy",
  "updated_at": "2026-06-25T10:03:00+09:00"
}
```

## 4.4 查询 Runtime 完整结果

```text
GET /api/v1/runtime/{runtime_id}/result
```

用于返回患者端安全输出和医生端结构化报告。

## 4.5 查询 RuntimeTrace

```text
GET /api/v1/runtime/{runtime_id}/trace
```

用于调试和复盘。

---

# 五、RuntimeStatus 状态设计

Phase 1 只实现必要状态。

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
ready_for_patient_output
ready_for_clinician_report
completed
error_safe_halted
```

## 5.1 状态流转

```text
created
  ↓
entry_assessing
  ↓
wellness_mode / clinical_mode / safety_gate_triggered / error_safe_halted
  ↓
collecting_case_info
  ↓
building_differential
  ↓
collecting_evidence
  ↓
recommending_tests / waiting_for_user / ready_for_patient_output / ready_for_clinician_report
  ↓
completed
```

## 5.2 状态约束

```text
SafetyGate 命中高危时，可以进入 safety_gate_triggered。
SafetyGate 或 DecisionBoundary 失败时，必须进入 error_safe_halted。
患者端输出前必须经过 DecisionBoundary。
医生端报告可以包含 DDx 和 EvidenceGraph，但患者端不一定能看到。
```

---

# 六、RuntimeState Schema

RuntimeState 是 Phase 1 的核心状态对象。

```json
{
  "runtime_id": "rt_001",
  "session_id": "s_001",
  "user_id": "u_001",
  "version": 1,
  "runtime_status": "collecting_evidence",
  "work_mode": "clinical_mode",
  "mode": "patient_facing",
  "input_history": [],
  "entry_assessment": {},
  "case_frame": {},
  "knowledge_context": {},
  "experience_context": {},
  "safety_gate": {},
  "differential_board": {},
  "evidence_graph": {},
  "question_test_policy": {},
  "decision_boundary": {},
  "patient_output": {},
  "clinician_report": {},
  "runtime_trace_ids": [],
  "created_at": "2026-06-25T10:00:00+09:00",
  "updated_at": "2026-06-25T10:03:00+09:00"
}
```

原则：

```text
1. 每轮输入必须读取 RuntimeState。
2. 每轮模块执行后必须更新 RuntimeState。
3. LLM 生成内容不能直接成为下一轮事实，必须先写回结构化状态。
4. 关键判断必须写入 RuntimeTrace。
5. RuntimeState 是诊断状态唯一事实源。
```

---

# 七、模块详细设计

## 7.1 EntryAssessment

职责：判断输入进入哪种工作态。

输入：

```text
user_input
basic_info
历史 RuntimeState，可选
```

输出：

```json
{
  "work_mode": "clinical_mode",
  "reason": "用户描述明确症状：胸闷、活动后加重",
  "entry_risk_hint": "medium_high",
  "suggested_symptom_group": "chest_pain"
}
```

工作态：

```text
wellness_mode
clinical_mode
emergency_hint
unsupported
```

Phase 1 可先用关键词和简单规则实现。

---

## 7.2 CaseFrame

职责：把用户输入结构化为病例状态。

Schema：

```json
{
  "chief_complaint": "胸口闷",
  "patient_profile": {
    "age": 58,
    "sex": "male",
    "risk_factors": []
  },
  "symptoms": [
    {
      "name": "胸闷",
      "duration": null,
      "severity": null,
      "location": "胸部",
      "trigger": "活动后加重",
      "relief": "休息缓解",
      "associated_symptoms": ["出汗"]
    }
  ],
  "past_history": [],
  "medication_history": [],
  "examination_results": [],
  "missing_slots": ["疼痛放射", "气短", "心电图", "肌钙蛋白"],
  "conflicting_slots": []
}
```

Phase 1 可以先用规则抽取 + 可选 LLM 辅助抽取。

---

## 7.3 Knowledge Context

职责：读取 Phase 1 静态知识规则。

输入：

```text
symptom_group
case_frame
```

输出：

```json
{
  "symptom_group": "chest_pain",
  "common_diagnoses": ["心绞痛", "胃食管反流", "焦虑相关胸闷"],
  "must_not_miss": ["急性冠脉综合征", "肺栓塞", "主动脉夹层"],
  "red_flags": [
    {
      "rule_id": "cp_rf_001",
      "features": ["胸痛或胸闷", "活动后加重", "出汗"],
      "risk_level": "high",
      "action": "recommend_urgent_evaluation"
    }
  ],
  "required_questions": ["是否气短", "是否放射痛", "是否恶心呕吐"],
  "recommended_tests": ["心电图", "肌钙蛋白"]
}
```

Phase 1 先从 `assets/symptom_groups/*.yml` 读取。

---

## 7.4 Experience Context

职责：保留临床经验接口，但 Phase 1 不接真实经验库。

输入：

```text
case_frame
symptom_group
```

输出：

```json
{
  "matched_experience_units": [],
  "experience_alerts": [],
  "implementation_mode": "empty_or_mock"
}
```

Phase 1 允许两种实现：

```text
空实现：始终返回空经验。
mock 实现：返回少量手写经验提醒，用于验证 Trace 和 DecisionBoundary 是否能接收经验输入。
```

---

## 7.5 SafetyGate

职责：识别危险信号，控制高危场景。

输入：

```text
case_frame
knowledge_context.red_flags
experience_context
capability_profile
```

输出：

```json
{
  "triggered": true,
  "risk_level": "high",
  "matched_rules": ["cp_rf_001"],
  "reason": "胸闷活动后加重并伴出汗，需要优先排除心血管高危情况",
  "required_action": "urgent_evaluation",
  "patient_output_constraint": "no_low_risk_reassurance"
}
```

规则：

```text
SafetyGate 命中高危后，患者端不能输出低风险结论。
SafetyGate 失败时，FailurePolicy 必须进入 fail-safe。
SafetyGate 的结果必须写入 RuntimeTrace。
```

---

## 7.6 Differential Diagnosis Board

职责：维护候选诊断状态。

候选状态：

```text
primary_hypothesis
main_alternative
must_not_miss
need_to_rule_out
possible
unlikely
insufficient_evidence
```

输出示例：

```json
{
  "candidates": [
    {
      "name": "急性冠脉综合征",
      "status": "need_to_rule_out",
      "risk_level": "high",
      "reason": "胸闷活动后加重并伴出汗",
      "patient_visible": false
    },
    {
      "name": "胃食管反流",
      "status": "possible",
      "risk_level": "low",
      "reason": "胸部不适可见于消化系统原因，但当前证据不足",
      "patient_visible": false
    }
  ]
}
```

原则：

```text
高危候选不能因信息不足被删除。
患者端是否展示候选由 DecisionBoundary 决定。
医生端可以展示候选和证据状态。
```

---

## 7.7 EvidenceGraph

职责：维护候选诊断和证据之间的关系。

输出示例：

```json
{
  "items": [
    {
      "diagnosis": "急性冠脉综合征",
      "supporting_evidence": ["胸闷", "活动后加重", "出汗", "休息缓解"],
      "opposing_evidence": [],
      "missing_evidence": ["心电图", "肌钙蛋白", "疼痛放射", "气短"],
      "status": "need_to_rule_out",
      "next_questions": ["是否向左肩、后背或下颌放射？", "是否伴随气短？"],
      "recommended_tests": ["心电图", "肌钙蛋白"]
    }
  ]
}
```

EvidenceGraph 必须服务两个动作：

```text
1. 决定下一步问什么或建议什么检查。
2. 为医生端报告提供证据解释。
```

---

## 7.8 Question / Test Policy

职责：根据 EvidenceGraph 决定下一步动作。

输出动作类型：

```text
ask_question
recommend_test
recommend_visit
wait_for_user
```

输出示例：

```json
{
  "next_action": {
    "type": "ask_question",
    "content": "胸闷时是否伴随气短，或者疼痛向左肩、后背、下颌放射？",
    "purpose": "rule_out_high_risk_diagnosis",
    "target_diagnosis": "急性冠脉综合征",
    "priority": "high"
  }
}
```

优先级：

```text
危险信号
> 高危疾病排除
> 关键缺失证据
> 推荐检查
> 普通病史补全
```

---

## 7.9 DecisionBoundary

职责：判断当前允许输出什么。

输入：

```text
mode
capability_profile
safety_gate
differential_board
evidence_graph
question_test_policy
failure_policy
```

输出：

```json
{
  "allowed_output_level": "O2_risk_hint_and_question",
  "patient_diagnosis_label_allowed": false,
  "clinician_ddx_allowed": true,
  "reason": "高危胸痛尚未排除，患者端不能输出诊断方向",
  "constraints": ["no_low_risk_reassurance", "no_definitive_diagnosis"]
}
```

输出等级：

```text
O1_continue_questioning
O2_risk_hint
O3_clinician_candidate_diagnosis
O4_low_risk_reference
O5_visit_or_urgent_care_recommendation
O6_transfer_to_doctor
O7_clinician_full_report
```

Phase 1 必须至少区分：

```text
患者端：追问 / 风险提示 / 就医建议
医生端：候选诊断 / 证据图 / 推荐追问 / 推荐检查
```

---

## 7.10 Patient Output

职责：生成患者端安全表达。

原则：

```text
不能输出确定诊断。
不能输出处方。
不能在高危未排除时输出低风险安抚。
必须解释为什么继续追问或建议就医。
```

示例：

```text
你提到胸闷在活动后更明显，并且伴随出汗。为了安全起见，需要先补充几个关键问题，判断是否存在需要尽快就医评估的风险。请问胸闷时是否伴随气短，或者疼痛向左肩、后背、下颌放射？
```

---

## 7.11 Clinician Report

职责：生成医生端结构化报告。

内容：

```text
CaseFrame Summary
SafetyGate Result
Differential Diagnosis Board
EvidenceGraph
Recommended Questions
Recommended Tests
DecisionBoundary Reason
RuntimeTrace Summary
```

医生端可以展示候选诊断，但必须标记证据状态和不确定性。

---

## 7.12 RuntimeTrace

职责：记录每轮 Runtime 执行过程。

Schema：

```json
{
  "trace_id": "trace_001",
  "runtime_id": "rt_001",
  "step": 1,
  "input": "胸口闷，活动后明显，还有点出汗",
  "modules_executed": [
    "EntryAssessment",
    "CaseFrame",
    "KnowledgeContext",
    "SafetyGate",
    "DDxBoard",
    "EvidenceGraph",
    "QuestionTestPolicy",
    "DecisionBoundary"
  ],
  "knowledge_used": ["chest_pain.yml", "cp_rf_001"],
  "experience_used": [],
  "safety_gate_result": {},
  "ddx_change": {},
  "evidence_graph_change": {},
  "decision_boundary_result": {},
  "output_summary": {},
  "created_at": "2026-06-25T10:03:00+09:00"
}
```

---

# 八、静态规则文件设计

## 8.1 症状群规则

示例：`assets/symptom_groups/chest_pain.yml`

```yaml
symptom_group: chest_pain
common_diagnoses:
  - name: 心绞痛
  - name: 胃食管反流
  - name: 焦虑相关胸闷
must_not_miss:
  - name: 急性冠脉综合征
  - name: 肺栓塞
  - name: 主动脉夹层
required_questions:
  - 是否伴随出汗？
  - 是否伴随气短？
  - 是否向左肩、后背或下颌放射？
recommended_tests:
  - 心电图
  - 肌钙蛋白
```

## 8.2 危险信号规则

```yaml
red_flag_rules:
  - rule_id: cp_rf_001
    symptom_group: chest_pain
    features:
      - 活动后加重
      - 出汗
    risk_level: high
    action: urgent_evaluation
    patient_constraint: no_low_risk_reassurance
```

## 8.3 能力档案静态配置

```yaml
capability_profiles:
  - symptom_group: chest_pain
    level: L2
    patient_allowed_outputs:
      - O1_continue_questioning
      - O2_risk_hint
      - O5_visit_or_urgent_care_recommendation
    clinician_allowed_outputs:
      - O3_clinician_candidate_diagnosis
      - O7_clinician_full_report
```

---

# 九、最小测试病例设计

Phase 1 至少准备 10–20 个测试病例。

## 9.1 病例类型

```text
普通低风险病例
高危红旗病例
信息缺失病例
误导表达病例
患者焦虑表达病例
症状不典型病例
```

## 9.2 胸痛 / 胸闷病例示例

### Case 1：高危胸闷

输入：

```text
58 岁男性，胸口闷，活动后明显，伴出汗，休息缓解。
```

预期：

```text
SafetyGate 命中高危。
DDx 保留急性冠脉综合征为 need_to_rule_out。
患者端不能输出低风险判断。
系统应追问放射痛、气短，或建议尽快完善心电图和肌钙蛋白。
```

### Case 2：信息缺失胸痛

输入：

```text
我今天有点胸口不舒服。
```

预期：

```text
进入 clinical_mode。
CaseFrame 标记大量 missing_slots。
Question Policy 优先追问持续时间、诱因、伴随症状和危险信号。
不能输出诊断方向。
```

### Case 3：可能低风险但需排除

输入：

```text
饭后胸口烧灼感，躺下更明显，没有出汗和气短。
```

预期：

```text
可考虑胃食管反流 possible。
仍需确认是否有活动后加重、放射痛等高危特征。
患者端可输出低风险倾向但不能确定诊断。
```

## 9.3 发热病例示例

### Case 4：普通发热

输入：

```text
低烧 37.8 度，流鼻涕，轻微咳嗽，没有呼吸困难。
```

预期：

```text
进入 clinical_mode 或 wellness_mode 取决于规则。
SafetyGate 不触发高危。
Question Policy 追问持续时间、基础疾病、是否高热等。
```

### Case 5：高危发热

输入：

```text
发烧 39.5 度，意识有点模糊，呼吸急促。
```

预期：

```text
SafetyGate 命中高危。
患者端输出就医建议。
医生端提示需考虑严重感染等高危方向。
```

---

# 十、Phase 1 完成标准

Phase 1 完成必须满足：

```text
1. 可以通过 API 创建和继续 Runtime。
2. RuntimeState 能被稳定创建、读取、更新。
3. RuntimeStatus 能反映当前问诊状态。
4. CaseFrame 能结构化保存主诉、症状和缺失信息。
5. Knowledge Context 能读取静态症状群规则。
6. SafetyGate 能识别至少胸痛和发热两个症状群的危险信号。
7. DDx Board 能生成候选诊断状态。
8. EvidenceGraph 能记录支持证据、反对证据、缺失证据。
9. Question / Test Policy 能生成下一步追问或检查建议。
10. DecisionBoundary 能区分患者端和医生端输出。
11. 高危病例不会输出低风险安抚性结论。
12. RuntimeTrace 能记录每轮模块执行和关键判断。
13. 10–20 个测试病例可以跑通。
14. 模块失败时能进入 error_safe_halted 或保守输出。
```

---

# 十一、Phase 1 不合格表现

如果出现以下情况，说明 Phase 1 还没有真正完成：

```text
1. 用户输入后直接生成自然语言回答，没有 RuntimeState。
2. SafetyGate 只是免责声明，不参与输出边界控制。
3. 候选诊断没有状态，只有字符串列表。
4. EvidenceGraph 只是解释结果，不影响下一步追问。
5. 患者端和医生端输出没有差异。
6. RuntimeTrace 无法解释为什么问这个问题、为什么限制输出。
7. 高危病例仍可能输出“可能问题不大”。
8. Knowledge Context 只是拼 Prompt，没有结构化规则。
```

---

# 十二、Phase 1 与后续阶段关系

Phase 1 的模块要为后续阶段保留接口。

| Phase 1 模块 | 后续扩展方向 |
|---|---|
| Knowledge Context | Phase 2 接入 Clinical Pathway、KG-lite、RAG Evidence Library |
| Experience Context | Phase 2/4 接入 Clinical Experience Memory |
| Capability Profile Provider | Phase 3 接入 Evaluation Results 和能力授权 |
| RuntimeTrace | Phase 4 作为经验进化和复盘输入 |
| DecisionBoundary | Phase 3/5 接入更完整的权限和再认证状态 |
| Static Rules | Phase 2 升级为可版本化共享能力资产 |

因此，Phase 1 开发时不能把规则和逻辑写死在业务函数里，应该通过 Provider 或配置读取，为后续替换资产层留接口。

---

# 十三、开发顺序建议

推荐顺序：

```text
1. 定义 RuntimeStatus、RuntimeState、RuntimeTrace 数据结构
2. 实现 RuntimeStore
3. 实现 Runtime API start / continue / status / trace
4. 实现 EntryAssessment
5. 实现 CaseFrame
6. 实现 StaticRuleProvider 和 Knowledge Context
7. 实现 SafetyGate
8. 实现 DDx Board
9. 实现 EvidenceGraph
10. 实现 Question / Test Policy
11. 实现 DecisionBoundary
12. 实现 Patient Output 和 Clinician Report
13. 编写 10–20 个测试病例
14. 跑通完整 Runtime 流程
```

---

# 十四、最终目标总结

Phase 1 的最终目标不是证明系统医学能力很强，而是证明 ClinMindRuntime 的核心架构成立：

```text
它能维护诊断状态，
能识别危险信号，
能保留候选诊断，
能组织证据关系，
能根据证据缺口决定下一步动作，
能根据风险和能力边界限制输出，
能记录每一步判断依据。
```

如果 Phase 1 完成，ClinMindRuntime 就已经具备了从普通医疗问答升级为“受控诊断 Runtime”的最小可运行形态。
