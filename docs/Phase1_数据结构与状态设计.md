# Phase 1 数据结构与状态设计

> 本文档是 Phase 1 Runtime MVP 的低层设计文档之一，负责定义 Runtime MVP 的核心数据结构、枚举、状态流转和字段读写关系。  
> 本项目是医疗问诊支持系统原型，所有输出均用于安全边界和架构验证，不作为真实诊疗建议。

---

# 一、设计原则

```text
1. RuntimeState 是诊断状态唯一事实源。
2. 对话历史只服务语言连续性，不能替代结构化病例状态。
3. 每个模块只读写明确字段。
4. 关键判断必须写入 RuntimeTrace。
5. 患者端输出和医生端输出必须结构化分离。
6. Phase 1 先保证可运行、可测试、可扩展。
```

---

# 二、核心枚举

## 2.1 RuntimeStatus

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

## 2.2 WorkMode

```text
wellness_mode
clinical_mode
emergency_hint
unsupported
```

## 2.3 RuntimeMode

```text
patient_facing
clinician_copilot
debug
```

## 2.4 RiskLevel

```text
none
low
medium
medium_high
high
unknown
```

## 2.5 CandidateStatus

```text
primary_hypothesis
main_alternative
must_not_miss
need_to_rule_out
possible
unlikely
insufficient_evidence
```

## 2.6 NextActionType

```text
ask_question
recommend_test
recommend_visit
wait_for_user
generate_patient_output
generate_clinician_report
safe_halt
```

## 2.7 OutputLevel

```text
O1_continue_questioning
O2_risk_hint
O3_clinician_candidate_diagnosis
O4_low_risk_reference
O5_visit_or_urgent_care_recommendation
O6_transfer_to_doctor
O7_clinician_full_report
```

---

# 三、RuntimeStatus 状态流转

## 3.1 主状态流转

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

## 3.2 状态迁移规则

| 当前状态 | 触发条件 | 下一状态 |
|---|---|---|
| created | 调用 start API | entry_assessing |
| entry_assessing | 判断为健康咨询 | wellness_mode |
| entry_assessing | 判断为临床问诊 | clinical_mode |
| entry_assessing | 疑似高风险 | safety_gate_triggered |
| clinical_mode | 病例信息不足 | collecting_case_info |
| collecting_case_info | 已识别症状群 | building_differential |
| building_differential | 候选诊断构建完成 | collecting_evidence |
| collecting_evidence | 需要继续补充信息 | waiting_for_user |
| collecting_evidence | 需要建议检查或就医评估 | recommending_tests |
| collecting_evidence | 可生成患者端安全输出 | ready_for_patient_output |
| collecting_evidence | 可生成医生端报告 | ready_for_clinician_report |
| 任意状态 | 安全模块或边界模块失败 | error_safe_halted |

---

# 四、RuntimeState

RuntimeState 是 Phase 1 的中心状态对象。

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| runtime_id | string | 是 | 自动生成 | Runtime 唯一标识 |
| session_id | string | 是 | 无 | 会话 ID |
| user_id | string | 否 | null | 用户 ID |
| version | int | 是 | 1 | 状态版本号 |
| runtime_status | RuntimeStatus | 是 | created | 当前状态 |
| work_mode | WorkMode | 否 | null | 入口工作态 |
| mode | RuntimeMode | 是 | patient_facing | 运行模式 |
| input_history | list[UserInput] | 是 | [] | 输入历史 |
| entry_assessment | EntryAssessmentResult | 否 | null | 入口判断结果 |
| case_frame | CaseFrame | 是 | 空对象 | 病例结构化状态 |
| knowledge_context | KnowledgeContext | 是 | 空对象 | 医学知识上下文 |
| experience_context | ExperienceContext | 是 | 空对象 | 经验上下文 |
| safety_gate | SafetyGateResult | 否 | null | 危险信号判断 |
| differential_board | DifferentialDiagnosisBoard | 是 | 空对象 | 候选诊断状态板 |
| evidence_graph | EvidenceGraph | 是 | 空对象 | 证据图 |
| question_test_policy | QuestionTestPolicyResult | 否 | null | 下一步动作 |
| decision_boundary | DecisionBoundaryResult | 否 | null | 输出边界 |
| patient_output | PatientOutput | 否 | null | 患者端输出 |
| clinician_report | ClinicianReport | 否 | null | 医生端报告 |
| runtime_trace_ids | list[string] | 是 | [] | Trace ID 列表 |
| created_at | datetime | 是 | 当前时间 | 创建时间 |
| updated_at | datetime | 是 | 当前时间 | 更新时间 |

---

# 五、核心子结构

## 5.1 CaseFrame

```text
chief_complaint: string | null
patient_profile: PatientProfile
symptoms: list[SymptomItem]
past_history: list[string]
medication_history: list[string]
examination_results: list[string]
missing_slots: list[string]
conflicting_slots: list[string]
```

## 5.2 KnowledgeContext

```text
symptom_group: string | null
common_diagnoses: list[DiagnosisRef]
must_not_miss: list[DiagnosisRef]
red_flags: list[RedFlagRule]
required_questions: list[string]
recommended_tests: list[string]
source_assets: list[string]
```

## 5.3 ExperienceContext

```text
matched_experience_units: list[ExperienceUnit]
experience_alerts: list[string]
implementation_mode: empty | mock
```

## 5.4 SafetyGateResult

```text
triggered: bool
risk_level: RiskLevel
matched_rules: list[string]
reason: string | null
required_action: string | null
patient_output_constraint: string | null
fail_safe_required: bool
```

## 5.5 DifferentialDiagnosisBoard

```text
candidates: list[DDxCandidate]
updated_reason: string | null
```

DDxCandidate：

```text
name: string
status: CandidateStatus
risk_level: RiskLevel
reason: string | null
patient_visible: bool
```

## 5.6 EvidenceGraph

```text
items: list[EvidenceGraphItem]
```

EvidenceGraphItem：

```text
diagnosis: string
supporting_evidence: list[string]
opposing_evidence: list[string]
missing_evidence: list[string]
conflicting_evidence: list[string]
status: CandidateStatus
next_questions: list[string]
recommended_tests: list[string]
```

## 5.7 QuestionTestPolicyResult

```text
next_action: NextAction
reason: string
```

NextAction：

```text
type: NextActionType
content: string
purpose: string | null
target_diagnosis: string | null
priority: low | medium | high
```

## 5.8 DecisionBoundaryResult

```text
allowed_output_level: OutputLevel
patient_diagnosis_label_allowed: bool
clinician_ddx_allowed: bool
reason: string
constraints: list[string]
```

## 5.9 PatientOutput

```text
allowed: bool
content: string
output_level: OutputLevel
constraints_applied: list[string]
```

## 5.10 ClinicianReport

```text
allowed: bool
case_summary: string | null
safety_summary: string | null
ddx_summary: list[DDxCandidate]
evidence_summary: EvidenceGraph | null
recommended_questions: list[string]
recommended_tests: list[string]
```

## 5.11 RuntimeTrace

```text
trace_id: string
runtime_id: string
step: int
input: string
modules_executed: list[string]
knowledge_used: list[string]
experience_used: list[string]
safety_gate_result: SafetyGateResult | null
ddx_change: object | null
evidence_graph_change: object | null
decision_boundary_result: DecisionBoundaryResult | null
output_summary: object | null
created_at: datetime
```

---

# 六、字段读写关系

| 模块 | 主要读取 | 主要写入 |
|---|---|---|
| Runtime API | 请求参数 | RuntimeState 基础字段、input_history |
| EntryAssessment | input_history、basic_info | entry_assessment、work_mode、runtime_status |
| CaseFrame | input_history、旧 case_frame | case_frame |
| Knowledge Context | case_frame、entry_assessment | knowledge_context |
| Experience Context | case_frame、knowledge_context | experience_context |
| SafetyGate | case_frame、knowledge_context、experience_context | safety_gate、runtime_status |
| DDx Board | case_frame、knowledge_context、safety_gate | differential_board |
| EvidenceGraph | case_frame、differential_board、knowledge_context | evidence_graph |
| Question/Test Policy | evidence_graph、safety_gate | question_test_policy、runtime_status |
| DecisionBoundary | mode、safety_gate、differential_board、evidence_graph、question_test_policy | decision_boundary |
| Output | decision_boundary、question_test_policy、evidence_graph | patient_output、clinician_report |
| RuntimeTrace | 关键中间结果 | runtime_trace_ids |

---

# 七、完成标准

```text
1. 所有核心结构可以用 Pydantic / dataclass 表达。
2. RuntimeState 可以完整序列化和反序列化。
3. RuntimeStatus 有明确枚举和迁移规则。
4. 每个模块的读写字段清楚。
5. PatientOutput 和 ClinicianReport 结构分离。
6. RuntimeTrace 能复盘一次 Runtime 的核心判断。
7. 后续 Phase 2–4 可以在不推翻结构的前提下扩展字段。
```
