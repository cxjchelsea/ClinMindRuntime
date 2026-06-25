# ClinMindRuntime 旧项目补充设计

> 来源旧项目：`cxjchelsea/AIdoctor`  
> 目标新项目：`cxjchelsea/ClinMindRuntime`  
> 文档目的：不是整理旧项目资产，也不是把旧项目整体搬迁过来，而是从 AIdoctor 中抽取 **ClinMindRuntime 当前设计中尚未充分展开、但旧项目已有可参考实现或工程细节的内容**，直接补充到新项目设计中。

---

## 0. 使用原则

新项目 ClinMindRuntime 已经有完整的核心设计方向：

```text
症状群 Rotation
能力档案 Capability Profile
Clinical Experience Memory
Diagnostic Runtime
SafetyGate
Differential Diagnosis Board
EvidenceGraph
DecisionBoundary
Human-like Interaction Layer
Shadow Learning
Review & Recertification
```

因此，旧项目中如果只是和这些概念重复，就不再重复保留。

本文件只保留三类内容：

```text
1. 新项目已有方向，但旧项目提供了更具体工程落点的内容
2. 新项目设计中没有展开，但旧项目已经实现过的工程机制
3. 旧项目虽然实现不完整，但能作为新项目 MVP 第一版落地骨架的内容
```

不保留三类内容：

```text
1. 旧项目中只是概念包装、但没有实质实现的内容
2. 新项目已经设计得更完整的内容
3. 医疗风险较高、容易让患者端越权的内容
```

---

# 一、可以直接补充进新项目的内容

## 1. Runtime API：启动、继续、查状态、查结果

### 1.1 为什么要补充

ClinMindRuntime 的设计已经有 Runtime 思想，但还需要明确一次问诊对外暴露哪些基础接口。

AIdoctor 旧项目中已经形成了比较清楚的诊断流程 API：

```text
POST /api/v1/diagnosis/start
POST /api/v1/diagnosis/continue
GET  /api/v1/diagnosis/{cdpId}/status
GET  /api/v1/diagnosis/{cdpId}/result
```

这部分可以直接迁移为 ClinMindRuntime 的 Runtime API。

### 1.2 在新项目中的设计

ClinMindRuntime 第一阶段建议提供以下接口：

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/continue
GET  /api/v1/runtime/{runtimeId}/status
GET  /api/v1/runtime/{runtimeId}/result
GET  /api/v1/runtime/{runtimeId}/trace
```

其中：

```text
start：创建一次新的诊断 Runtime
continue：提交用户补充信息，推进 Runtime 一轮
status：查看当前 Runtime 状态
result：查看最终输出结果
trace：查看诊断过程追踪，供医生端、调试和复盘使用
```

### 1.3 建议请求结构

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

### 1.4 建议响应结构

```json
{
  "runtime_id": "rt_001",
  "status": "collecting_evidence",
  "work_mode": "clinical_mode",
  "risk_level": "high",
  "red_flags": ["胸闷", "活动后加重"],
  "allowed_output": "O2_risk_warning",
  "next_action": {
    "type": "ask_question",
    "content": "胸闷时是否伴随出汗、气短，或者疼痛向左肩、后背放射？",
    "purpose": "rule_out_high_risk_diagnosis"
  },
  "case_frame_summary": {},
  "timestamp": 1760000000000
}
```

### 1.5 与新项目已有设计的关系

这不是新增一个独立模块，而是把 Diagnostic Runtime 对外产品化。

新项目已有 Diagnostic Runtime，但如果没有这组 API，项目会停留在架构设计层。加入这组接口后，面试时可以说明：

> Runtime 不是一个抽象概念，而是一次问诊从 start 到 continue 再到 result 的完整状态机。

---

## 2. RuntimeStatus：补充具体运行状态枚举

### 2.1 为什么要补充

新项目已经有 CaseFrame、EvidenceGraph、DecisionBoundary 等对象，但还缺少一次问诊的状态枚举。

AIdoctor 旧项目中的 CDP 状态设计可以直接补充到新项目中，但要做医疗 Runtime 化改造。

旧项目中有：

```text
initial
wellness_mode
clinical_mode_collecting
clinical_mode_diagnosing
clinical_mode_managing
completed
follow_up
```

这组状态值得保留，但要升级。

### 2.2 新项目建议状态枚举

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

### 2.3 状态解释

| 状态 | 含义 | 是否允许患者端输出诊断 |
|---|---|---|
| `created` | Runtime 已创建，尚未完成入口判断 | 否 |
| `entry_assessing` | 正在判断健康管理态还是临床诊疗态 | 否 |
| `wellness_mode` | 健康管理态，仅提供健康教育或生活方式建议 | 否 |
| `collecting_case_info` | 收集主诉、现病史、既往史等基础病例信息 | 否 |
| `safety_gate_triggered` | 命中高危信号，输出被收紧 | 否，只能风险提示 |
| `building_differential` | 构建候选诊断池 | 否 |
| `collecting_evidence` | 围绕候选诊断收集关键证据 | 否 |
| `recommending_tests` | 建议检查以排除或确认关键方向 | 患者端只展示就医/检查准备建议 |
| `waiting_for_user` | 等待用户补充信息 | 否 |
| `waiting_for_doctor` | 需要医生介入或审核 | 否 |
| `ready_for_patient_output` | 可生成患者端安全输出 | 仅限风险提示/就医建议/健康教育 |
| `ready_for_clinician_report` | 可生成医生端报告 | 医生端允许完整 DDx 和证据图 |
| `completed` | 当前问诊闭环结束 | 按 DecisionBoundary 输出 |
| `follow_up_pending` | 等待随访结局 | 否 |
| `under_review` | 进入医生复盘或质控 | 否 |
| `archived` | 已归档 | 否 |
| `error_safe_halted` | 安全相关模块异常，流程保守中止 | 否 |

### 2.4 设计收益

这部分补充后，新项目可以更像一个真正的 Runtime，而不是只有对象没有生命周期。

---

## 3. Entry Assessment：入口工作态判定

### 3.1 为什么要补充

ClinMindRuntime 当前主线主要围绕诊断 Runtime 展开，但真实医疗产品里并不是所有用户输入都应该进入诊断流程。

AIdoctor 旧项目有一个很实用的设计：先做健康状态判定，然后决定进入：

```text
wellness_mode：健康管理态
clinical_mode：临床诊疗态
```

这部分可以补充到新项目作为 Runtime 的入口层。

### 3.2 新项目中的定位

新增模块：

```text
EntryAssessment
```

它在 CaseFrame 完整构建之前运行，用于判断当前输入属于哪类任务。

```text
用户输入
  ↓
EntryAssessment
  ↓
wellness_mode / clinical_mode / emergency_hint / unsupported
```

### 3.3 工作态分类

```text
wellness_mode：健康管理、日常咨询、轻症科普、非诊断需求
clinical_mode：存在明确症状、需要问诊支持
emergency_hint：疑似急症，直接触发 SafetyGate
unsupported：超出系统能力范围，建议咨询医生或改写输入
```

### 3.4 输出结构

```json
{
  "work_mode": "clinical_mode",
  "reason": "用户描述胸闷且活动后加重，需要进入临床问诊流程",
  "risk_level": "medium_high",
  "red_flags": ["活动后加重"],
  "next_runtime_status": "collecting_case_info"
}
```

### 3.5 与 SafetyGate 的区别

EntryAssessment 负责判断是否进入诊断流程。

SafetyGate 负责判断当前是否存在高危风险。

二者关系是：

```text
EntryAssessment：是否进入临床问诊
SafetyGate：进入后是否需要收紧输出或建议急诊
```

---

## 4. RuntimeState 数据结构：直接吸收 CDP 的字段经验

### 4.1 为什么要补充

新项目中已经设计了 CaseFrame、EvidenceGraph、DecisionBoundary 等对象，但还需要一个统一的持久化状态对象。

AIdoctor 的 CDP 字段设计比较完整，可以直接作为 ClinMindRuntime 第一版 RuntimeState 的基础。

### 4.2 新项目建议结构

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

### 4.3 直接继承的字段思想

从旧项目可直接继承：

```text
user_id / patient_id
session_id
version
status
patient_state
DDx
evidenceGraph
workupPlan
triage
uncertainty
audit
executionTrace
createdAt
updatedAt
```

但在新项目中需要改名和升级：

```text
patient_state → case_frame
DDx → differential_board
workupPlan → question_test_policy_state.recommended_tests
triage → safety_gate + decision_boundary
executionTrace → runtime_trace
```

### 4.4 不建议继承的字段

旧项目中的 `managementPlan` 不建议直接进入患者端 RuntimeState。

原因：治疗和处置建议风险较高，容易越权。

建议改为：

```text
clinician_management_reference
```

并且只允许医生端模式使用。

---

## 5. CaseFrame 的最小字段集：直接吸收旧项目的信息缺口字段

### 5.1 为什么要补充

新项目中有 CaseFrame 概念，但第一版落地需要一个最小字段集。

AIdoctor 旧项目的 dialog-service 已经定义了一套基础信息缺口字段，可以作为 CaseFrame 第一版字段。

### 5.2 第一版 CaseFrame 字段

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

### 5.3 字段分级

直接吸收旧项目的 required / important / optional 思路。

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

### 5.4 升级规则

旧项目中这些字段用于计算完整度。

新项目中不能只用完整度推进流程，而要升级为：

```text
普通完整度：辅助指标
关键证据完整度：核心指标
高危排除证据：硬约束
```

例如：

```text
胸痛场景下，即使普通字段完整度达到 80%，如果没有排除急性冠脉综合征、主动脉夹层、肺栓塞，患者端也不能输出低风险判断。
```

---

## 6. 对话上下文管理：直接保留 Redis + 内存降级思路

### 6.1 为什么要补充

新项目有多轮问诊，但还需要明确短期对话上下文如何保存。

AIdoctor 旧项目使用 Redis 保存对话上下文，并在 Redis 不可用时使用内存存储作为降级。

这部分可以直接用于 ClinMindRuntime 第一阶段。

### 6.2 新项目设计

```text
Redis：保存短期对话上下文
数据库：保存 RuntimeState 长期状态
内存存储：开发环境或 Redis 不可用时的临时降级
```

### 6.3 上下文内容

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

### 6.4 对话历史长度

旧项目限制最近 20 条对话。

新项目可以保留这个策略，但需要说明：

```text
短期对话上下文只用于语言连续性。
真正的诊断状态必须写入 RuntimeState，不能只依赖 conversation_history。
```

这点很重要。

---

## 7. WebSocket 实时对话：作为患者端交互补充

### 7.1 为什么要补充

旧项目 dialog-service 已经有 WebSocket 实时对话接口思想。

ClinMindRuntime 第一阶段可以保留 HTTP API，但如果要展示产品体验，WebSocket 或 SSE 可以作为实时问诊交互补充。

### 7.2 新项目建议

```text
HTTP：用于 start / continue / status / result
WebSocket 或 SSE：用于实时返回追问、风险提示、医生端推理过程片段
```

### 7.3 第一阶段建议

第一阶段不必复杂实现全双工，优先用 SSE 即可：

```text
患者端：流式返回自然表达
医生端：流式返回结构化诊断状态更新
```

旧项目的 WebSocket 经验可以作为后续升级参考。

---

# 二、需要在新设计中升级后使用的内容

## 8. 五步流程：不照搬，作为 Runtime Loop 的 MVP 骨架

### 8.1 旧项目内容

旧项目五步流程是：

```text
1. 识别问题
2. 构建鉴别诊断候选集并分层
3. 组织候选集并建立分流路径
4. 采集关键证据并形成验证计划
5. 回填证据并输出终点结论包
```

### 8.2 为什么不能原样保留

新项目已经有 Diagnostic Runtime，不需要再重复一个固定 Workflow。

而且旧项目五步流程的问题是：

```text
1. 步骤推进偏固定
2. 信息完整度阈值影响较大
3. SafetyGate 和 DecisionBoundary 没有成为硬控制层
4. 高危疾病排除状态没有贯穿每一步
```

### 8.3 在新项目中的升级方式

将五步流程变成 Runtime Loop 中的内部阶段：

```text
Runtime Round
  1. Parse & Update CaseFrame
  2. Run SafetyGate
  3. Update Differential Board
  4. Update EvidenceGraph
  5. Decide Question / Test Action
  6. Apply DecisionBoundary
  7. Generate Patient / Clinician Output
  8. Write RuntimeTrace
```

### 8.4 关键变化

旧项目：

```text
流程决定下一步
```

新项目：

```text
状态决定下一步
```

旧项目：

```text
信息完整度够了 → 进入诊断
```

新项目：

```text
风险等级 + 证据充分性 + 高危排除状态 + 能力权限 → 决定是否允许输出
```

---

## 9. 信息缺口识别：升级为 Evidence Gap

### 9.1 旧项目内容

旧项目可以识别：

```text
主诉
症状诱因
症状持续时间
症状严重程度
症状部位
症状频率
伴随症状
家族史
既往史
用药史
过敏史
```

并分为：

```text
required
important
optional
```

### 9.2 可直接用的部分

这些字段可以直接作为 CaseFrame 第一版字段。

`required / important / optional` 也可以作为基础信息优先级。

### 9.3 必须升级的部分

旧项目的信息缺口是 slot filling。

新项目必须升级为 Evidence Gap：

```text
不是问“缺哪个字段”
而是问“当前候选诊断还缺什么关键证据”
```

示例：

```json
{
  "gap_type": "missing_evidence",
  "target_diagnosis": "急性冠脉综合征",
  "missing_evidence": ["疼痛放射部位", "活动后加重", "出汗", "心电图", "肌钙蛋白"],
  "priority": "high",
  "reason": "胸痛场景下该方向属于 must_not_miss，高危排除优先"
}
```

### 9.4 模块命名

```text
旧：InformationGapIdentifier
新：EvidenceGapIdentifier

旧：CompletenessCalculator
新：EvidenceSufficiencyEvaluator

旧：AdaptiveQuestioningStrategy
新：QuestionTestPolicy
```

---

## 10. 三层诊断分层：升级为 Differential Diagnosis Board

### 10.1 旧项目内容

旧项目中已有：

```text
primary_hypothesis
main_alternatives
must_exclude
```

这和新项目 Differential Diagnosis Board 高度相关。

### 10.2 不重复保留的原因

新项目已经设计了 Differential Diagnosis Board，所以不需要把旧项目的三层分类作为独立模块重复保留。

### 10.3 作为新项目的补充点

旧项目能补充的是：第一版 Board 可以采用三层结构作为最小实现。

```json
{
  "primary_hypothesis": [],
  "main_alternatives": [],
  "must_exclude": [],
  "all_candidates": []
}
```

### 10.4 必须升级的地方

旧项目 `must_exclude` 更接近单个高危候选。

新项目要改成列表，并且引入候选状态：

```text
must_not_miss
need_to_rule_out
possible
possible_after_exclusion
unlikely
insufficient_evidence
```

新项目原则：

```text
高危候选不能因为分数低而删除。
```

---

## 11. 多引擎融合：升级为候选与证据提供器

### 11.1 旧项目内容

旧项目有五类引擎：

```text
规则引擎
知识图谱引擎
统计模型引擎
LLM 引擎
鉴别诊断引擎
```

并通过加权融合产生候选诊断可能性。

### 11.2 新项目不应照搬的部分

新项目不应该继续把“加权融合分数”作为最终决策。

原因：

```text
1. 医疗诊断不能只按分数排序
2. 高危低概率疾病仍然要保留
3. 患者端输出权限不是由分数决定，而是由 DecisionBoundary 决定
4. LLM 分数和统计分数不一定可比较
```

### 11.3 新项目升级方式

将五引擎改成 Candidate & Evidence Providers：

```text
Rule Provider：提供红旗信号、禁忌输出、高危疾病规则
KG Provider：提供疾病-症状-检查关系
RAG Provider：提供指南依据和证据引用
LLM Provider：提供语义理解、候选补全、证据归因草稿
Statistical Provider：提供风险评分，作为辅助信号
Differential Provider：组织候选诊断池
```

这些 Provider 不直接决定最终回答。

最终回答必须经过：

```text
SafetyGate
EvidenceGraph
DecisionBoundary
```

---

## 12. 证据链分析：升级为控制型 EvidenceGraph

### 12.1 旧项目内容

旧项目的 EvidenceAnalyzer 已经支持：

```text
supporting_evidence
opposing_evidence
evidence_strength
evidence_summary
rule_engine 来源
knowledge_graph 来源
llm_engine 来源
```

### 12.2 新项目已有内容

新项目已经设计了 EvidenceGraph，所以不需要重复保留旧的 EvidenceAnalyzer 概念。

### 12.3 旧项目能补充的部分

旧项目能补充的是 EvidenceGraph 第一版实现时的证据来源结构。

建议 EvidenceGraph 中保留来源字段：

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

### 12.4 必须升级的部分

旧项目证据链偏解释：

```text
为什么这个疾病可能？
```

新项目 EvidenceGraph 必须控制：

```text
下一步问什么
是否建议检查
是否允许输出候选诊断
是否触发转医生
是否禁止患者端诊断标签
```

---

## 13. 服务容错：从普通降级升级为医疗 Fail-safe

### 13.1 旧项目内容

旧项目里多个服务调用失败后会继续流程，例如：

```text
健康状态判定服务失败 → 默认进入 clinical_mode
诊断引擎失败 → 使用空 DDx 继续
风险评估失败 → 不影响流程继续
解释服务失败 → 使用简化结果
```

这体现了工程容错意识。

### 13.2 新项目不能照搬的原因

医疗系统不能所有模块失败都继续输出。

尤其是 SafetyGate、RiskAssessment、DecisionBoundary 失败时，不能正常给患者输出。

### 13.3 新项目补充规则

新增：

```text
FailurePolicy
```

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

### 13.4 状态处理

当安全相关模块失败时，RuntimeStatus 应进入：

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

# 三、旧项目中不应补进新项目主线的内容

## 14. 不保留“五脑思想”作为主表达

旧项目 README 中有“五脑思想”表述。

这个说法不建议继续使用。

原因：

```text
1. 面试中显得概念包装重
2. 不如 Runtime / State / Evidence / Boundary 专业
3. 新项目已经有更强的架构表达
```

新项目统一使用：

```text
Diagnostic Runtime
State-driven Diagnosis
Evidence-controlled Reasoning
DecisionBoundary
Clinical Experience Memory
```

---

## 15. 不把治疗推理作为患者端核心能力

旧项目有 treatment-engine-service 和 managementPlan。

新项目不应将其作为患者端核心能力。

原因：

```text
1. 治疗建议和用药建议风险高
2. 患者端容易越权
3. 合规责任重
4. 与 ClinMindRuntime 第一阶段“诊断支持 Runtime”主线不一致
```

如果保留，只能作为医生端扩展：

```text
Clinician Management Reference
```

并且必须满足：

```text
仅医生端可见
必须标注证据来源
不能生成处方
不能替代医生决策
```

---

## 16. 不把 OCR 作为第一阶段核心

旧项目支持 OCR 和检查单识别。

这部分有价值，但不适合作为 ClinMindRuntime 第一阶段主线。

第一阶段应聚焦：

```text
文本问诊
CaseFrame
SafetyGate
DDx Board
EvidenceGraph
QuestionTestPolicy
DecisionBoundary
RuntimeTrace
```

OCR 可以放到第二阶段：

```text
检查报告解析
化验单结构化
影像报告文本解析
心电图报告文本解析
```

---

# 四、建议写入新项目总体设计的补充段落

以下内容可以直接合并进 `系统总体设计.md`。

## 17. 旧项目经验如何进入 ClinMindRuntime

ClinMindRuntime 并不是从零开始设计。旧版 AIdoctor 项目已经实现过一个多服务医疗问诊系统，包括 CDP 状态管理、主动问诊、候选诊断生成、证据链分析、风险评估和执行追踪。

但旧系统的核心问题是：它更像一个固定诊断 Workflow，主要按照步骤推进；而医疗问诊中的关键问题不是“流程执行到哪一步”，而是“当前诊断状态是否允许继续输出”。

因此，ClinMindRuntime 只吸收旧项目中能够补充新 Runtime 的工程机制：

```text
1. Runtime API：start / continue / status / result / trace
2. RuntimeStatus：一次问诊的完整状态生命周期
3. EntryAssessment：入口工作态判定
4. RuntimeState：从旧 CDP 升级而来的中心状态对象
5. CaseFrame 最小字段集：由旧项目信息缺口字段升级而来
6. Redis 对话上下文：用于短期语言连续性
7. RuntimeTrace：由旧项目执行追踪升级而来
8. FailurePolicy：由旧项目服务降级机制升级为医疗 fail-safe
```

旧项目中与新方案重复的内容不再重复保留，例如候选诊断、证据图、风险评估这些新项目已经有更完整设计；旧项目只提供第一版落地字段、接口和工程经验。

最终，AIdoctor 不是 ClinMindRuntime 的架构来源，而是 ClinMindRuntime 的工程补充来源。

---

# 五、MVP 实现建议

## 18. 第一阶段真正应该从旧项目拿过来的东西

第一阶段只拿这些：

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

这些是能直接补强新项目的。

## 19. 第一阶段不要拿的东西

第一阶段不要拿：

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

原因：这些会让新项目变重，且和当前 Runtime 设计主线不完全一致。

---

# 六、最终结论

AIdoctor 中真正能补充 ClinMindRuntime 的，不是宏观架构，而是一些具体工程落点：

```text
Runtime API
RuntimeStatus
RuntimeState
CaseFrame 最小字段集
EntryAssessment
短期对话上下文
RuntimeTrace
FailurePolicy
```

这些内容可以直接让 ClinMindRuntime 从“设计很完整”变成“第一阶段可实现、可演示、可面试讲清楚”的工程项目。

旧项目中和新项目已有设计重复的部分，不再重复搬迁；旧项目中实现不完整的部分，只作为新项目升级提示，不作为已完成能力。