# Phase 1 API 与测试设计

> 本文档是 Phase 1 Runtime MVP 的低层设计文档之一。  
> 它负责定义 Runtime API 契约、统一响应格式、错误码、静态规则文件格式、测试病例格式和验收用例。  
> 数据结构见 `Phase1_数据结构与状态设计.md`，模块接口见 `Phase1_模块接口设计.md`。

---

# 一、API 设计原则

```text
1. API 只暴露 Runtime 的启动、继续、查询和追踪能力。
2. 患者端输出和医生端报告必须结构化分离。
3. 所有输出必须经过 DecisionBoundary。
4. 高风险或安全模块异常时，API 返回保守结果。
5. Phase 1 先保证 API 可测试，不追求复杂权限体系。
```

---

# 二、统一响应格式

所有 API 响应采用统一结构：

```json
{
  "success": true,
  "data": {},
  "error": null,
  "trace_id": "trace_001"
}
```

失败响应：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RUNTIME_NOT_FOUND",
    "message": "Runtime 不存在"
  },
  "trace_id": null
}
```

---

# 三、错误码

| 错误码 | HTTP 状态码 | 含义 | 处理方式 |
|---|---:|---|---|
| INVALID_REQUEST | 400 | 请求字段缺失或格式错误 | 返回参数错误 |
| INVALID_MODE | 400 | mode 不合法 | 返回参数错误 |
| RUNTIME_NOT_FOUND | 404 | runtime_id 不存在 | 返回错误 |
| RUNTIME_STATE_CONFLICT | 409 | Runtime 状态冲突 | 返回状态冲突 |
| SAFETY_GATE_FAILED | 500 | SafetyGate 执行失败 | 进入 error_safe_halted |
| DECISION_BOUNDARY_FAILED | 500 | DecisionBoundary 执行失败 | 进入 error_safe_halted |
| KNOWLEDGE_CONTEXT_FAILED | 500 | Knowledge Context 构建失败 | 保守输出或中止 |
| INTERNAL_ERROR | 500 | 未知系统错误 | 返回系统错误 |

---

# 四、Runtime API

## 4.1 创建 Runtime

```text
POST /api/v1/runtime/start
```

### 请求

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

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| session_id | string | 是 | 会话 ID |
| user_id | string | 否 | 用户 ID，可为空 |
| mode | string | 是 | patient_facing / clinician_copilot / debug |
| input.text | string | 是 | 用户输入 |
| input.attachments | list | 否 | Phase 1 可为空 |
| basic_info | object | 否 | 年龄、性别等基础信息 |

### 成功响应

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "runtime_status": "waiting_for_user",
    "work_mode": "clinical_mode",
    "risk_level": "medium_high",
    "next_action": {
      "type": "ask_question",
      "content": "为了判断风险，需要补充几个关键问题。",
      "purpose": "collect_key_evidence",
      "priority": "high"
    },
    "patient_output": {
      "allowed": true,
      "content": "你描述的是明确症状，需要先补充关键信息来判断风险。",
      "output_level": "O1_continue_questioning"
    },
    "clinician_report": null
  },
  "error": null,
  "trace_id": "trace_001"
}
```

---

## 4.2 继续 Runtime

```text
POST /api/v1/runtime/continue
```

### 请求

```json
{
  "runtime_id": "rt_001",
  "input": {
    "text": "有点出汗，走路快的时候更明显，休息会缓解"
  }
}
```

### 成功响应

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "runtime_status": "recommending_tests",
    "risk_level": "high",
    "next_action": {
      "type": "recommend_test",
      "content": "建议尽快进行必要检查或就医评估。",
      "purpose": "rule_out_high_risk_diagnosis",
      "priority": "high"
    },
    "patient_output": {
      "allowed": true,
      "content": "根据你补充的信息，建议尽快进行线下评估。",
      "output_level": "O5_visit_or_urgent_care_recommendation"
    }
  },
  "error": null,
  "trace_id": "trace_002"
}
```

---

## 4.3 查询 Runtime 状态

```text
GET /api/v1/runtime/{runtime_id}/status
```

响应：

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "runtime_status": "collecting_evidence",
    "work_mode": "clinical_mode",
    "risk_level": "high",
    "updated_at": "2026-06-25T10:03:00+09:00"
  },
  "error": null,
  "trace_id": null
}
```

---

## 4.4 查询 Runtime 结果

```text
GET /api/v1/runtime/{runtime_id}/result
```

响应：

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "patient_output": {},
    "clinician_report": {},
    "decision_boundary": {},
    "runtime_status": "completed"
  },
  "error": null,
  "trace_id": null
}
```

---

## 4.5 查询 RuntimeTrace

```text
GET /api/v1/runtime/{runtime_id}/trace
```

响应：

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "traces": []
  },
  "error": null,
  "trace_id": null
}
```

---

# 五、静态规则文件格式

Phase 1 使用静态 YAML / JSON 规则，不做完整资产管理后台。

## 5.1 症状群规则

路径示例：

```text
assets/symptom_groups/chest_pain.yml
assets/symptom_groups/fever.yml
```

格式：

```yaml
symptom_group: chest_pain
common_diagnoses:
  - name: common_condition_a
    risk_level: low
must_not_miss:
  - name: high_risk_condition_a
    risk_level: high
required_questions:
  - 是否伴随明显不适？
  - 是否有持续加重？
recommended_tests:
  - 基础检查 A
  - 基础检查 B
```

说明：

```text
Phase 1 的规则文件只用于验证 Runtime 机制。
具体医学内容应保持保守，并在后续阶段由正式知识资产替换。
```

## 5.2 危险信号规则

路径：

```text
assets/red_flag_rules.yml
```

格式：

```yaml
red_flag_rules:
  - rule_id: rf_001
    symptom_group: chest_pain
    features:
      - activity_related
      - sweating
    risk_level: high
    action: urgent_evaluation
    patient_constraint: no_low_risk_reassurance
```

## 5.3 检查建议规则

路径：

```text
assets/test_recommendation_rules.yml
```

格式：

```yaml
test_recommendation_rules:
  - rule_id: test_001
    symptom_group: chest_pain
    target_status: need_to_rule_out
    recommended_tests:
      - 基础检查 A
    purpose: collect_missing_evidence
```

## 5.4 静态 Capability Profile

路径：

```text
assets/capability_profiles.yml
```

格式：

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

# 六、测试病例格式

测试病例建议存放在：

```text
tests/cases/chest_pain_cases.yml
tests/cases/fever_cases.yml
```

格式：

```yaml
- case_id: chest_001
  title: 高风险症状输入
  mode: patient_facing
  input:
    text: "用户描述存在明显不适，并伴随高风险特征"
  basic_info:
    age: 58
    sex: male
  expected:
    work_mode: clinical_mode
    safety_gate_triggered: true
    risk_level: high
    patient_diagnosis_label_allowed: false
    expected_next_action_types:
      - ask_question
      - recommend_test
      - recommend_visit
    forbidden_patient_outputs:
      - low_risk_reassurance
      - definitive_diagnosis
```

字段说明：

| 字段 | 含义 |
|---|---|
| case_id | 测试病例 ID |
| title | 测试病例名称 |
| mode | 运行模式 |
| input.text | 用户输入 |
| basic_info | 基础信息 |
| expected.work_mode | 预期工作态 |
| expected.safety_gate_triggered | 是否应触发安全门 |
| expected.risk_level | 预期风险等级 |
| expected.patient_diagnosis_label_allowed | 患者端是否允许诊断标签 |
| expected_next_action_types | 允许的下一步动作类型 |
| forbidden_patient_outputs | 禁止出现的患者端输出类型 |

---

# 七、最小测试集设计

Phase 1 至少准备 10–20 个测试病例。

## 7.1 病例类型

```text
普通低风险输入
高风险输入
信息缺失输入
误导表达输入
焦虑或情绪化表达输入
非典型表达输入
```

## 7.2 症状群覆盖

```text
胸痛 / 胸闷：至少 5–10 例
发热：至少 5–10 例
```

## 7.3 每个测试病例至少验证

```text
EntryAssessment 是否正确
CaseFrame 是否抽取关键信息
SafetyGate 是否正确触发
DDx Board 是否保留必要候选
EvidenceGraph 是否记录缺失证据
Question / Test Policy 是否给出合理下一步动作
DecisionBoundary 是否限制患者端输出
RuntimeTrace 是否记录关键判断
```

---

# 八、验收用例示例

## 8.1 Case A：信息缺失输入

输入：

```text
我今天有点不舒服。
```

预期：

```text
进入 clinical_mode 或继续澄清。
CaseFrame 标记 missing_slots。
Question / Test Policy 生成追问。
患者端不能输出诊断方向。
```

## 8.2 Case B：高风险输入

输入：

```text
用户描述症状在活动后加重，并伴随明显不适。
```

预期：

```text
SafetyGate 触发。
risk_level 为 high 或 medium_high。
DecisionBoundary 禁止低风险安抚。
PatientOutput 应提示尽快线下评估或继续补充关键问题。
RuntimeTrace 记录命中规则。
```

## 8.3 Case C：医生端模式

输入：

```text
医生端输入一个已经结构化的症状描述。
```

预期：

```text
ClinicianReport 允许展示 DDx Board 和 EvidenceGraph。
PatientOutput 不应包含医生端完整内容。
```

---

# 九、自动化测试建议

## 9.1 单元测试

```text
test_entry_assessment.py
test_case_frame.py
test_knowledge_context.py
test_safety_gate.py
test_differential_board.py
test_evidence_graph.py
test_question_test_policy.py
test_decision_boundary.py
```

## 9.2 集成测试

```text
test_runtime_start_flow.py
test_runtime_continue_flow.py
test_high_risk_flow.py
test_patient_vs_clinician_output.py
test_runtime_trace.py
```

## 9.3 回归测试

每次修改 SafetyGate、DecisionBoundary、EvidenceGraph 后，必须跑最小测试集。

---

# 十、Phase 1 API 验收标准

```text
1. start API 可以创建 Runtime 并返回 runtime_id。
2. continue API 可以推进同一个 Runtime。
3. status API 可以查询当前状态。
4. result API 可以返回患者端和医生端分离结果。
5. trace API 可以返回每轮执行记录。
6. 非法请求能返回统一错误格式。
7. runtime_id 不存在时返回 RUNTIME_NOT_FOUND。
8. SafetyGate 或 DecisionBoundary 失败时不会继续输出高风险内容。
```

---

# 十一、Phase 1 测试验收标准

```text
1. 至少 10–20 个测试病例全部可以跑通。
2. 高风险病例不会输出低风险安抚性结论。
3. 信息缺失病例会继续追问。
4. 医生端可以看到结构化候选和证据图。
5. 患者端和医生端输出不同。
6. RuntimeTrace 可以解释每一轮为什么这样判断。
7. 静态规则文件可以被替换，不需要改核心链路代码。
```

---

# 十二、不合格表现

```text
1. API 返回的是 LLM 直接回答，而不是 Runtime 结构化结果。
2. SafetyGate 只是免责声明，不影响 DecisionBoundary。
3. 没有 RuntimeTrace 或 Trace 不能解释关键判断。
4. 患者端可以看到医生端完整候选诊断。
5. 静态规则写死在业务逻辑里，无法替换。
6. 测试病例只能人工观察，无法自动断言。
```

---

# 十三、最终说明

Phase 1 的 API 与测试设计不是为了证明系统具备真实临床能力，而是为了证明受控诊断 Runtime 的工程闭环成立：

```text
可创建
可继续
可查询
可追踪
可区分输出边界
可用测试病例验证
```

完成本阶段后，后续 Phase 2 才能把静态规则替换为正式共享能力资产，Phase 3 才能引入评估和能力授权，Phase 4 才能引入经验进化。
