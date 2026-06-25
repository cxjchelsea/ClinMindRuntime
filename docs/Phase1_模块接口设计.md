# Phase 1 模块接口设计

> 本文档是 Phase 1 Runtime MVP 的低层设计文档之一。  
> 它负责定义 Phase 1 核心模块的职责、上下游关系、输入输出、读写字段、核心方法和失败策略。  
> 数据结构见 `Phase1_数据结构与状态设计.md`，API 与测试见 `Phase1_API与测试设计.md`。

---

# 一、模块设计原则

```text
1. 每个模块只处理一个清晰职责。
2. 模块之间通过结构化对象传递信息，而不是通过长 Prompt 隐式传递。
3. 所有模块都围绕 RuntimeState 读写。
4. 关键模块输出必须写入 RuntimeTrace。
5. 医疗安全相关模块失败时，必须进入保守策略。
6. Phase 1 允许静态规则和 mock 实现，但接口要为后续扩展保留。
```

---

# 二、Phase 1 模块执行顺序

```text
Runtime API
  ↓
RuntimeStore
  ↓
EntryAssessment
  ↓
CaseFrameBuilder
  ↓
StaticRuleProvider
  ↓
KnowledgeContextBuilder
  ↓
ExperienceContextBuilder
  ↓
SafetyGate
  ↓
DifferentialDiagnosisBoardBuilder
  ↓
EvidenceGraphBuilder
  ↓
QuestionTestPolicy
  ↓
DecisionBoundary
  ↓
PatientOutputBuilder / ClinicianReportBuilder
  ↓
RuntimeTraceRecorder
```

---

# 三、模块总览表

| 模块 | 主职责 | 主要输入 | 主要输出 |
|---|---|---|---|
| Runtime API | 接收请求、返回 Runtime 结果 | HTTP 请求 | RuntimeResponse |
| RuntimeStore | 保存和读取 RuntimeState | runtime_id、RuntimeState | RuntimeState |
| EntryAssessment | 判断入口工作态 | UserInput、basic_info | EntryAssessmentResult |
| CaseFrameBuilder | 构建病例状态 | UserInput、旧 CaseFrame | CaseFrame |
| StaticRuleProvider | 读取静态规则 | symptom_group | 静态规则对象 |
| KnowledgeContextBuilder | 生成知识上下文 | CaseFrame、规则对象 | KnowledgeContext |
| ExperienceContextBuilder | 生成经验上下文 | CaseFrame、KnowledgeContext | ExperienceContext |
| SafetyGate | 识别危险信号 | CaseFrame、KnowledgeContext、ExperienceContext | SafetyGateResult |
| DDxBoardBuilder | 构建候选诊断状态板 | CaseFrame、KnowledgeContext、SafetyGateResult | DifferentialDiagnosisBoard |
| EvidenceGraphBuilder | 构建证据图 | CaseFrame、DDxBoard、KnowledgeContext | EvidenceGraph |
| QuestionTestPolicy | 决定下一步动作 | EvidenceGraph、SafetyGateResult | QuestionTestPolicyResult |
| DecisionBoundary | 控制输出边界 | RuntimeState 中关键状态 | DecisionBoundaryResult |
| PatientOutputBuilder | 生成患者端安全表达 | DecisionBoundary、NextAction | PatientOutput |
| ClinicianReportBuilder | 生成医生端报告 | CaseFrame、DDx、EvidenceGraph | ClinicianReport |
| RuntimeTraceRecorder | 记录执行过程 | 模块结果 | RuntimeTrace |
| FailurePolicy | 处理失败策略 | 异常、模块名、RuntimeState | 保守结果 / error_safe_halted |

---

# 四、Runtime API

## 4.1 职责

```text
1. 接收 start / continue / status / result / trace 请求。
2. 创建或读取 RuntimeState。
3. 调用 Runtime 执行链路。
4. 返回患者端或医生端结果。
```

## 4.2 核心方法

```python
def start_runtime(request: StartRuntimeRequest) -> RuntimeResponse:
    pass

def continue_runtime(request: ContinueRuntimeRequest) -> RuntimeResponse:
    pass

def get_runtime_status(runtime_id: str) -> RuntimeStatusResponse:
    pass

def get_runtime_result(runtime_id: str) -> RuntimeResultResponse:
    pass

def get_runtime_trace(runtime_id: str) -> RuntimeTraceResponse:
    pass
```

## 4.3 读写字段

```text
读取：HTTP request
写入：RuntimeState、RuntimeTrace
```

---

# 五、RuntimeStore

## 5.1 职责

保存、读取、更新 RuntimeState。

Phase 1 可用内存字典、JSON 文件或 SQLite 实现。

## 5.2 核心方法

```python
def create(state: RuntimeState) -> RuntimeState:
    pass

def get(runtime_id: str) -> RuntimeState:
    pass

def update(state: RuntimeState) -> RuntimeState:
    pass

def exists(runtime_id: str) -> bool:
    pass
```

## 5.3 失败策略

```text
runtime_id 不存在：返回 404。
状态读取失败：进入 error_safe_halted 或返回系统错误。
状态更新失败：不能继续生成输出。
```

---

# 六、EntryAssessment

## 6.1 职责

判断用户输入是否进入临床问诊 Runtime，以及建议的症状群。

## 6.2 输入

```text
UserInput
basic_info
旧 RuntimeState，可选
```

## 6.3 输出

```text
EntryAssessmentResult
```

## 6.4 核心方法

```python
def assess_entry(user_input: UserInput, basic_info: dict | None = None) -> EntryAssessmentResult:
    pass
```

## 6.5 Phase 1 实现策略

```text
先用关键词和规则判断。
可选使用 LLM 辅助分类，但不能让 LLM 结果直接绕过规则。
```

## 6.6 写入 RuntimeState

```text
entry_assessment
work_mode
runtime_status
```

---

# 七、CaseFrameBuilder

## 7.1 职责

把用户输入转换为结构化病例状态，并在多轮对话中更新已有 CaseFrame。

## 7.2 输入

```text
UserInput
existing CaseFrame
basic_info
```

## 7.3 输出

```text
CaseFrame
```

## 7.4 核心方法

```python
def build_or_update_case_frame(
    user_input: UserInput,
    existing_case_frame: CaseFrame | None,
    basic_info: dict | None = None
) -> CaseFrame:
    pass
```

## 7.5 写入 RuntimeState

```text
case_frame
```

## 7.6 失败策略

```text
抽取失败时保留原 CaseFrame，并将关键信息加入 missing_slots。
不能因为抽取失败而输出诊断方向。
```

---

# 八、StaticRuleProvider

## 8.1 职责

从本地 YAML / JSON 中读取症状群规则、危险信号规则、检查建议规则和静态能力档案。

## 8.2 核心方法

```python
def load_symptom_group_rules(symptom_group: str) -> dict:
    pass

def load_red_flag_rules(symptom_group: str) -> list[dict]:
    pass

def load_test_recommendation_rules(symptom_group: str) -> list[dict]:
    pass

def load_capability_profile(symptom_group: str) -> dict:
    pass
```

## 8.3 失败策略

```text
规则缺失：KnowledgeContext 标记 source_assets 为空。
危险信号规则读取失败：SafetyGate 应进入 fail-safe。
能力档案读取失败：DecisionBoundary 默认收紧输出。
```

---

# 九、KnowledgeContextBuilder

## 9.1 职责

根据 CaseFrame 和症状群，聚合 Phase 1 静态医学规则。

## 9.2 输入

```text
CaseFrame
EntryAssessmentResult
StaticRuleProvider
```

## 9.3 输出

```text
KnowledgeContext
```

## 9.4 核心方法

```python
def build_knowledge_context(
    case_frame: CaseFrame,
    entry_assessment: EntryAssessmentResult
) -> KnowledgeContext:
    pass
```

## 9.5 写入 RuntimeState

```text
knowledge_context
```

---

# 十、ExperienceContextBuilder

## 10.1 职责

为 Phase 1 保留经验上下文接口。

## 10.2 输入

```text
CaseFrame
KnowledgeContext
```

## 10.3 输出

```text
ExperienceContext
```

## 10.4 核心方法

```python
def build_experience_context(
    case_frame: CaseFrame,
    knowledge_context: KnowledgeContext
) -> ExperienceContext:
    pass
```

## 10.5 Phase 1 实现策略

```text
默认 empty 实现：返回空经验。
可选 mock 实现：返回少量手写经验提醒，用于验证 Trace 和边界链路。
```

---

# 十一、SafetyGate

## 11.1 职责

识别危险信号，并给出风险等级和输出限制。

## 11.2 输入

```text
CaseFrame
KnowledgeContext.red_flags
ExperienceContext
CapabilityProfile
```

## 11.3 输出

```text
SafetyGateResult
```

## 11.4 核心方法

```python
def evaluate_safety(
    case_frame: CaseFrame,
    knowledge_context: KnowledgeContext,
    experience_context: ExperienceContext,
    capability_profile: dict | None = None
) -> SafetyGateResult:
    pass
```

## 11.5 写入 RuntimeState

```text
safety_gate
runtime_status，可选更新为 safety_gate_triggered 或 error_safe_halted
```

## 11.6 Trace 记录点

```text
matched_rules
risk_level
required_action
patient_output_constraint
```

## 11.7 失败策略

```text
SafetyGate 失败时必须 fail-safe。
不得在 SafetyGate 失败后继续生成诊断方向。
```

---

# 十二、DifferentialDiagnosisBoardBuilder

## 12.1 职责

构建候选诊断状态板，保留高风险候选，不提前给出唯一诊断。

## 12.2 输入

```text
CaseFrame
KnowledgeContext.common_diagnoses
KnowledgeContext.must_not_miss
SafetyGateResult
```

## 12.3 输出

```text
DifferentialDiagnosisBoard
```

## 12.4 核心方法

```python
def build_differential_board(
    case_frame: CaseFrame,
    knowledge_context: KnowledgeContext,
    safety_gate_result: SafetyGateResult
) -> DifferentialDiagnosisBoard:
    pass
```

## 12.5 写入 RuntimeState

```text
differential_board
```

## 12.6 约束

```text
高风险候选必须保留为 must_not_miss 或 need_to_rule_out。
患者端是否可见由 DecisionBoundary 决定。
```

---

# 十三、EvidenceGraphBuilder

## 13.1 职责

为每个候选诊断组织支持证据、反对证据和缺失证据。

## 13.2 输入

```text
CaseFrame
DifferentialDiagnosisBoard
KnowledgeContext
ExperienceContext
```

## 13.3 输出

```text
EvidenceGraph
```

## 13.4 核心方法

```python
def build_evidence_graph(
    case_frame: CaseFrame,
    differential_board: DifferentialDiagnosisBoard,
    knowledge_context: KnowledgeContext,
    experience_context: ExperienceContext
) -> EvidenceGraph:
    pass
```

## 13.5 写入 RuntimeState

```text
evidence_graph
```

## 13.6 约束

```text
EvidenceGraph 不是解释层，而是控制层。
Question / Test Policy 必须读取 EvidenceGraph 的 missing_evidence。
```

---

# 十四、QuestionTestPolicy

## 14.1 职责

根据 EvidenceGraph 和 SafetyGate 决定下一步追问、检查建议或就医评估建议。

## 14.2 输入

```text
EvidenceGraph
SafetyGateResult
KnowledgeContext.required_questions
KnowledgeContext.recommended_tests
```

## 14.3 输出

```text
QuestionTestPolicyResult
```

## 14.4 核心方法

```python
def decide_next_action(
    evidence_graph: EvidenceGraph,
    safety_gate_result: SafetyGateResult,
    knowledge_context: KnowledgeContext
) -> QuestionTestPolicyResult:
    pass
```

## 14.5 优先级

```text
危险信号
> 高风险候选排除
> 关键缺失证据
> 检查建议
> 普通病史补全
```

---

# 十五、DecisionBoundary

## 15.1 职责

根据风险、证据状态、能力档案和运行模式决定当前允许输出什么。

## 15.2 输入

```text
RuntimeMode
CapabilityProfile
SafetyGateResult
DifferentialDiagnosisBoard
EvidenceGraph
QuestionTestPolicyResult
FailurePolicy 状态
```

## 15.3 输出

```text
DecisionBoundaryResult
```

## 15.4 核心方法

```python
def decide_output_boundary(
    runtime_state: RuntimeState,
    capability_profile: dict | None = None
) -> DecisionBoundaryResult:
    pass
```

## 15.5 写入 RuntimeState

```text
decision_boundary
runtime_status，可选更新为 ready_for_patient_output / ready_for_clinician_report / error_safe_halted
```

## 15.6 失败策略

```text
DecisionBoundary 失败时必须 fail-safe。
患者端默认不允许诊断标签。
```

---

# 十六、PatientOutputBuilder

## 16.1 职责

在 DecisionBoundary 允许的范围内生成患者端安全表达。

## 16.2 输入

```text
DecisionBoundaryResult
QuestionTestPolicyResult
SafetyGateResult
```

## 16.3 输出

```text
PatientOutput
```

## 16.4 核心方法

```python
def build_patient_output(
    decision_boundary: DecisionBoundaryResult,
    question_test_policy: QuestionTestPolicyResult,
    safety_gate_result: SafetyGateResult
) -> PatientOutput:
    pass
```

## 16.5 约束

```text
不能输出确定诊断。
不能输出处方。
不能在高风险未排除时输出低风险安抚。
必须解释为什么继续追问或建议就医评估。
```

---

# 十七、ClinicianReportBuilder

## 17.1 职责

生成医生端结构化报告。

## 17.2 输入

```text
CaseFrame
SafetyGateResult
DifferentialDiagnosisBoard
EvidenceGraph
QuestionTestPolicyResult
DecisionBoundaryResult
```

## 17.3 输出

```text
ClinicianReport
```

## 17.4 核心方法

```python
def build_clinician_report(runtime_state: RuntimeState) -> ClinicianReport:
    pass
```

## 17.5 约束

```text
医生端可以展示候选诊断和证据状态。
必须标记不确定性和缺失证据。
不能把医生端内容泄露到患者端。
```

---

# 十八、RuntimeTraceRecorder

## 18.1 职责

记录每轮 Runtime 的输入、模块执行、知识使用、经验使用、安全判断、证据变化和输出边界。

## 18.2 核心方法

```python
def record_trace(
    runtime_state: RuntimeState,
    input_text: str,
    modules_executed: list[str],
    intermediate_results: dict
) -> RuntimeTrace:
    pass
```

## 18.3 写入 RuntimeState

```text
runtime_trace_ids
```

---

# 十九、FailurePolicy

## 19.1 职责

统一处理关键模块失败时的保守策略。

## 19.2 核心方法

```python
def handle_failure(
    module_name: str,
    error: Exception,
    runtime_state: RuntimeState
) -> RuntimeState:
    pass
```

## 19.3 规则

```text
SafetyGate 失败：进入 error_safe_halted。
DecisionBoundary 失败：进入 error_safe_halted。
EvidenceGraph 失败：不得输出候选诊断。
KnowledgeContext 失败：不得引用知识来源。
PatientOutput 失败：返回结构化保守提示。
```

---

# 二十、模块接口完成标准

```text
1. 每个模块都有明确输入输出。
2. 每个模块读写 RuntimeState 字段清楚。
3. 安全相关模块有 fail-safe 策略。
4. RuntimeTrace 记录点清楚。
5. Phase 1 可先使用规则和 mock，但接口可以无缝升级到 Phase 2–4。
```
