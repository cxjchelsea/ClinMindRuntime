# Phase 1 API 与测试设计

> 本文档是 Phase 1 Runtime MVP 的低层设计文档之一。  
> 它负责定义 Java Spring Boot Runtime Core 的 API 契约、统一响应格式、错误码、静态规则文件格式、测试病例格式和验收用例。  
> 数据结构见 `Phase1_数据结构与状态设计.md`，模块接口见 `Phase1_模块接口设计.md`，技术栈见 `Phase1_技术栈与工程架构决策.md`。

---

# 一、API 设计原则

```text
1. API 只暴露 Runtime 的启动、继续、查询和追踪能力。
2. 患者端输出和医生端报告必须结构化分离。
3. 所有输出必须经过 DecisionBoundary。
4. 高风险或安全模块异常时，API 返回保守结果。
5. Phase 1 先保证 API 可测试，不追求复杂权限体系。
6. API 由 Spring Boot RuntimeController 承载。
```

---

# 二、统一响应格式

Java 侧建议定义：

```java
public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    String traceId
) {}
```

成功响应：

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
| SAFETY_GATE_FAILED | 500 | SafetyGate 执行失败 | 进入 ERROR_SAFE_HALTED |
| DECISION_BOUNDARY_FAILED | 500 | DecisionBoundary 执行失败 | 进入 ERROR_SAFE_HALTED |
| KNOWLEDGE_CONTEXT_FAILED | 500 | Knowledge Context 构建失败 | 保守输出或中止 |
| INTERNAL_ERROR | 500 | 未知系统错误 | 返回系统错误 |

---

# 四、Runtime API

## 4.1 创建 Runtime

```text
POST /api/v1/runtime/start
```

### Java Controller 方法

```java
@PostMapping("/start")
public ApiResponse<RuntimeResponse> startRuntime(@RequestBody StartRuntimeRequest request) {
    // ...
}
```

### 请求

```json
{
  "session_id": "s_001",
  "user_id": "u_001",
  "mode": "PATIENT_FACING",
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
| mode | string | 是 | PATIENT_FACING / CLINICIAN_COPILOT / DEBUG |
| input.text | string | 是 | 用户输入 |
| input.attachments | list | 否 | Phase 1 可为空 |
| basic_info | object | 否 | 年龄、性别等基础信息 |

### 成功响应

```json
{
  "success": true,
  "data": {
    "runtime_id": "rt_001",
    "runtime_status": "WAITING_FOR_USER",
    "work_mode": "CLINICAL_MODE",
    "risk_level": "MEDIUM_HIGH",
    "next_action": {
      "type": "ASK_QUESTION",
      "content": "为了判断风险，需要补充几个关键问题。",
      "purpose": "collect_key_evidence",
      "priority": "high"
    },
    "patient_output": {
      "allowed": true,
      "content": "你描述的是明确症状，需要先补充关键信息来判断风险。",
      "output_level": "O1_CONTINUE_QUESTIONING"
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

### Java Controller 方法

```java
@PostMapping("/continue")
public ApiResponse<RuntimeResponse> continueRuntime(@RequestBody ContinueRuntimeRequest request) {
    // ...
}
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
    "runtime_status": "RECOMMENDING_TESTS",
    "risk_level": "HIGH",
    "next_action": {
      "type": "RECOMMEND_TEST",
      "content": "建议尽快进行必要检查或线下评估。",
      "purpose": "rule_out_high_risk_diagnosis",
      "priority": "high"
    },
    "patient_output": {
      "allowed": true,
      "content": "根据你补充的信息，建议尽快进行线下评估。",
      "output_level": "O5_VISIT_OR_URGENT_CARE_RECOMMENDATION"
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
    "runtime_status": "COLLECTING_EVIDENCE",
    "work_mode": "CLINICAL_MODE",
    "risk_level": "HIGH",
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
    "runtime_status": "COMPLETED"
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
src/main/resources/assets/symptom-groups/chest-pain.yml
src/main/resources/assets/symptom-groups/fever.yml
```

格式：

```yaml
symptom_group: chest_pain
common_diagnoses:
  - name: common_condition_a
    risk_level: LOW
must_not_miss:
  - name: high_risk_condition_a
    risk_level: HIGH
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
src/main/resources/assets/red-flag-rules.yml
```

格式：

```yaml
red_flag_rules:
  - rule_id: rf_001
    symptom_group: chest_pain
    features:
      - activity_related
      - sweating
    risk_level: HIGH
    action: urgent_evaluation
    patient_constraint: no_low_risk_reassurance
```

## 5.3 检查建议规则

路径：

```text
src/main/resources/assets/test-recommendation-rules.yml
```

格式：

```yaml
test_recommendation_rules:
  - rule_id: test_001
    symptom_group: chest_pain
    target_status: NEED_TO_RULE_OUT
    recommended_tests:
      - 基础检查 A
    purpose: collect_missing_evidence
```

## 5.4 静态 Capability Profile

路径：

```text
src/main/resources/assets/capability-profiles.yml
```

格式：

```yaml
capability_profiles:
  - symptom_group: chest_pain
    level: L2
    patient_allowed_outputs:
      - O1_CONTINUE_QUESTIONING
      - O2_RISK_HINT
      - O5_VISIT_OR_URGENT_CARE_RECOMMENDATION
    clinician_allowed_outputs:
      - O3_CLINICIAN_CANDIDATE_DIAGNOSIS
      - O7_CLINICIAN_FULL_REPORT
```

---

# 六、测试病例格式

测试病例建议存放在：

```text
src/test/resources/cases/chest-pain-cases.yml
src/test/resources/cases/fever-cases.yml
```

格式：

```yaml
- case_id: chest_001
  title: 高风险症状输入
  mode: PATIENT_FACING
  input:
    text: "用户描述存在明显不适，并伴随高风险特征"
  basic_info:
    age: 58
    sex: male
  expected:
    work_mode: CLINICAL_MODE
    safety_gate_triggered: true
    risk_level: HIGH
    patient_diagnosis_label_allowed: false
    expected_next_action_types:
      - ASK_QUESTION
      - RECOMMEND_TEST
      - RECOMMEND_VISIT
    forbidden_patient_outputs:
      - low_risk_reassurance
      - definitive_diagnosis
```

---

# 七、最小测试集设计

Phase 1 至少准备 10–20 个测试病例。

## 7.1 病例类型

```text
普通低风险输入
高风险输入
信息缺失输入
误导表达输入
情绪化表达输入
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

# 八、JUnit 测试建议

## 8.1 单元测试

```text
RuntimeStateTest
RuntimeStoreTest
EntryAssessmentServiceTest
CaseFrameServiceTest
StaticRuleProviderTest
KnowledgeContextServiceTest
SafetyGateServiceTest
DifferentialDiagnosisBoardServiceTest
EvidenceGraphServiceTest
QuestionTestPolicyServiceTest
DecisionBoundaryServiceTest
```

## 8.2 集成测试

```text
RuntimeControllerTest
RuntimeFlowIntegrationTest
HighRiskFlowIntegrationTest
PatientVsClinicianOutputTest
RuntimeTraceAspectTest
```

## 8.3 回归测试

每次修改 SafetyGate、DecisionBoundary、EvidenceGraph 后，必须跑最小测试集。

---

# 九、Phase 1 API 验收标准

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

# 十、Phase 1 测试验收标准

```text
1. 至少 10–20 个测试病例全部可以跑通。
2. 高风险病例不会输出低风险安抚性结论。
3. 信息缺失病例会继续追问。
4. 医生端可以看到结构化候选和证据图。
5. 患者端和医生端输出不同。
6. RuntimeTrace 可以解释每一轮为什么这样判断。
7. AOP Trace 可以记录关键模块执行。
8. 静态规则文件可以被替换，不需要改核心链路代码。
```

---

# 十一、不合格表现

```text
1. API 返回的是 LLM 直接回答，而不是 Runtime 结构化结果。
2. SafetyGate 只是免责声明，不影响 DecisionBoundary。
3. 没有 RuntimeTrace 或 Trace 不能解释关键判断。
4. 患者端可以看到医生端完整候选诊断。
5. 静态规则写死在业务逻辑里，无法替换。
6. 测试病例只能人工观察，无法自动断言。
7. LangChain / LangGraph / Spring AI / LangChain4j 取代了 Runtime 主控。
```

---

# 十二、最终说明

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
