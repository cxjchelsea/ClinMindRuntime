# Phase 6-P0 受控 Agent 执行层 MVP 实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> AI 实现约束：`docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`  
> 当前 Phase：Phase 6-P0  
> 当前目标：建立受控 Agent 执行层 MVP，不实现自由自治式医疗 Agent。

---

# 一、Phase 定位

Phase 6-P0 的目标不是把 ClinMindRuntime 改造成一个自由 Agent 系统，而是在现有 Runtime 主控链路中增加一个受控 Agent 执行层。

核心命题：

```text
Agent 可以参与问诊规划，
但 Agent 不能成为医疗主控。
```

Phase 6-P0 要证明：

```text
Runtime 可以在明确授权下调用 Agent；
Agent 只能生成结构化 Proposal；
Proposal 必须经过 Validator 和 Runtime Validation；
Runtime 可以采纳、部分采纳、拒绝或降级；
Agent 执行过程必须进入 RuntimeTrace / Audit / Evaluation。
```

---

# 二、当前不做什么

Phase 6-P0 明确不做：

```text
1. 不做自由自治式 Agent。
2. 不做多 Agent 协作。
3. 不引入 LangGraph / Agent SDK 作为 Runtime 主控。
4. 不接真实 RAG / GraphRAG。
5. 不接 Python AI Provider。
6. 不接 MCP / Tool / Skills。
7. 不做模型训练 / 后训练。
8. 不让 Agent 直接生成最终诊断。
9. 不让 Agent 直接生成 PatientOutput。
10. 不让 Agent 直接修改 RuntimeState。
11. 不让 Agent 决定 SafetyGate。
12. 不让 Agent 决定 DecisionBoundary。
13. 不让 Agent 直接扩大 CapabilityProfile。
```

---

# 三、Phase 6-P0 核心链路

目标链路：

```text
RuntimeState / CaseFrame
↓
CapabilityOrchestrationService 判断是否需要 Agent
↓
AgentPolicy 判断是否允许调用
↓
AgentExecutionRequest 构造受控输入
↓
AgentRuntime 调用受控 Agent
↓
InquiryPlanningAgent 生成 InquiryPlanProposal
↓
AgentProposalValidator 校验 Proposal
↓
RuntimeValidationService 统一校验外部能力结果
↓
Runtime 采纳 / 部分采纳 / 拒绝 / 降级
↓
RuntimeTrace / AuditLog 记录
↓
EvaluationScorer 评估 Agent Proposal 质量
```

Phase 6-P0 只在 Capability Orchestration 节点引入第一个 Agent 能力。

---

# 四、首个 Agent：InquiryPlanningAgent

## 4.1 为什么选择它

选择 `InquiryPlanningAgent` 的原因：

```text
1. 它体现 Agent 的规划价值。
2. 风险可控，因为它只生成下一步追问 Proposal。
3. 它不需要真实 RAG / MCP / 多 Agent 即可验证 AgentExecutionLayer。
4. 可以用缺失信息覆盖率、红旗信息优先级、患者表达安全性等指标评估。
5. 它不会直接产生诊断或治疗建议。
```

## 4.2 输入

`InquiryPlanningAgent` 输入来自 Runtime 受控上下文，而不是完整 raw snapshot。

建议输入结构：

```text
InquiryPlanningInput
- runtime_id
- session_id
- symptom_group
- case_frame_summary
- known_facts
- missing_facts
- red_flag_candidates
- current_questions_asked
- current_ddx_board_summary
- evidence_graph_summary
- patient_boundary_rules
- allowed_question_types
- max_question_count
- capability_profile_snapshot
```

输入限制：

```text
1. 不传完整患者原始对话。
2. 不传未脱敏敏感信息。
3. 不传可让 Agent 直接输出诊断的完整医生端推理链。
4. 不传数据库写权限。
5. 不传 Tool / MCP / RAG 调用权限。
```

## 4.3 输出

Agent 只能输出 `InquiryPlanProposal`。

建议结构：

```text
InquiryPlanProposal
- proposal_id
- runtime_id
- agent_id
- agent_version
- proposed_questions
- reasoning_summary
- uncertainty_level
- rejected_options
- safety_notes
- created_at
```

其中：

```text
InquiryQuestionCandidate
- question_id
- question_text
- clinical_purpose
- target_missing_fact
- priority
- risk_related
- patient_safe_wording
- expected_answer_type
- should_ask_now
- reject_if_boundary_violation
```

输出限制：

```text
1. 不能输出最终诊断。
2. 不能输出治疗方案。
3. 不能输出药物建议。
4. 不能输出医生端 DDx 全量内容给患者。
5. 不能输出“你可能是某某病”的患者端暗示。
6. 不能要求患者自行做高风险判断。
```

---

# 五、核心对象设计

## 5.1 AgentRegistry

职责：

```text
登记当前 Runtime 允许调用的 Agent。
根据 agent_id / capability / symptom_group 查询 Agent。
返回 Agent metadata。
```

建议字段：

```text
agent_id
agent_name
agent_version
agent_type
supported_capability
supported_symptom_groups
risk_level
enabled
```

P0 可以先使用 in-memory registry。

## 5.2 AgentRuntime

职责：

```text
执行 Agent。
统一生成 execution_id。
调用 AgentPolicy。
构造 AgentContext。
返回 AgentExecutionResult。
记录 AgentTrace。
```

AgentRuntime 不负责医疗决策。

## 5.3 AgentPolicy

职责：

```text
判断当前 RuntimeState 下是否允许调用某个 Agent。
判断当前 symptom_group 是否支持。
判断当前风险等级是否允许。
判断当前角色和模式是否允许。
判断当前 Agent 是否启用。
```

P0 必须 fail-closed：

```text
不确定是否允许调用时，默认拒绝。
```

## 5.4 AgentExecutionRequest

建议字段：

```text
execution_id
runtime_id
agent_id
agent_version
agent_task_type
input_payload
policy_context
trace_context
created_at
```

## 5.5 AgentExecutionResult

建议字段：

```text
execution_id
runtime_id
agent_id
status
proposal
validation_result
warnings
error_code
started_at
finished_at
```

状态：

```text
SUCCESS
POLICY_REJECTED
VALIDATION_REJECTED
PARTIALLY_ACCEPTED
DEGRADED
FAILED
```

## 5.6 AgentProposalValidator

职责：

```text
校验 Agent 输出是否结构完整。
校验 proposed_questions 是否越界。
校验 question_text 是否患者端安全。
校验 target_missing_fact 是否真实缺失。
校验 high-risk missing fact 是否被低优先级处理。
校验 proposed_questions 数量是否超过限制。
```

## 5.7 AgentTrace

必须记录：

```text
execution_id
runtime_id
agent_id
agent_version
input_summary
output_summary
policy_decision
validation_decision
accepted_question_ids
rejected_question_ids
rejection_reasons
created_at
```

不得记录：

```text
完整未脱敏患者原文。
敏感身份信息。
可直接泄露医生端内部推理的全量内容。
```

---

# 六、Runtime Validation 规则

Phase 6-P0 必须引入面向 Agent Proposal 的 Runtime Validation。

验证项：

```text
1. Agent Proposal 是否来自已注册 Agent。
2. Agent 是否在当前 RuntimeState 下被授权。
3. Proposal 是否只能影响 QuestionPolicy / next_question_candidates。
4. Proposal 是否试图修改 RuntimeState 其他字段。
5. Proposal 是否包含患者端禁止信息。
6. Proposal 是否包含诊断结论暗示。
7. Proposal 是否缺少 target_missing_fact。
8. Proposal 是否忽略 red_flag_candidates。
9. Proposal 是否超过最大问题数量。
10. Proposal 是否可进入 RuntimeTrace。
```

Validation 结果：

```text
ACCEPTED
PARTIALLY_ACCEPTED
REJECTED
DEGRADED
```

---

# 七、与现有 Runtime 的集成边界

Phase 6-P0 不重写现有 Runtime 主链路。

推荐集成点：

```text
EntryAssessment
→ CaseFrame
→ KnowledgeContext
→ ExperienceContext
→ SafetyGate
→ CapabilityOrchestrationService
→ InquiryPlanningAgent
→ AgentProposalValidator
→ RuntimeValidationService
→ QuestionPolicy / NextQuestionCandidate 更新
→ DecisionBoundary
→ PatientOutput / ClinicianReport
→ RuntimeTrace
```

注意：

```text
Agent 不能跳过 SafetyGate。
Agent 输出不能跳过 DecisionBoundary。
Agent Proposal 不能直接变成 PatientOutput。
```

---

# 八、API 边界

Phase 6-P0 只新增 debug / internal API，不新增 patient-facing Agent API。

候选 API：

```text
POST /api/v1/debug/agents/inquiry-planning/run
GET  /api/v1/debug/agents/executions/{execution_id}
GET  /api/v1/debug/agents/executions/{execution_id}/trace
POST /api/v1/debug/agents/proposals/{proposal_id}/validate
```

API 详细设计见：

```text
docs/3-phase实现/Phase6_P0Agent_API与测试设计.md
```

---

# 九、Evaluation 接入

Phase 6-P0 应新增 Agent 相关评分器。

候选 Scorer：

```text
InquiryPlanCoverageScorer
RedFlagQuestionPriorityScorer
PatientSafeQuestionScorer
AgentTraceCompletenessScorer
AgentBoundaryViolationScorer
```

评估目标：

```text
1. 是否覆盖关键 missing_facts。
2. 是否优先追问 red_flag_candidates。
3. 问题是否患者端安全。
4. 是否没有泄露诊断暗示。
5. AgentTrace 是否完整。
6. 被拒绝 Proposal 是否有清晰原因。
```

---

# 十、存储策略

P0 建议：

```text
AgentExecution / AgentTrace 可以先 in-memory + RuntimeTrace 集成。
不强制新增 PostgreSQL 表。
```

如果需要持久化，后置新增：

```text
agent_executions
agent_traces
agent_proposals
agent_validation_results
```

Phase 6-P0 不应因为表结构设计拖慢核心链路验证。

---

# 十一、完成标准

Phase 6-P0 完成时必须满足：

```text
1. 已有 AgentRegistry / AgentRuntime / AgentPolicy 基础抽象。
2. 已有 InquiryPlanningAgent 的 rule-based 或 mock 实现。
3. Agent 只能输出 InquiryPlanProposal。
4. AgentProposalValidator 可以拒绝危险或越界 Proposal。
5. RuntimeValidationService 可以处理 Agent Proposal。
6. Runtime 能采纳 / 部分采纳 / 拒绝 / 降级 Agent 输出。
7. Agent 执行过程进入 RuntimeTrace。
8. 有 debug API 可以查看 Agent 执行结果。
9. 有单元测试证明 Agent 不能越权。
10. 有 Evaluation 测试衡量 InquiryPlanProposal 质量。
11. `mvn test` 通过。
12. 不破坏 Phase 1–5 既有 API 和 Console 边界。
```

---

# 十二、Phase 6-P0 后置任务

Phase 6-P0 不完成但可作为后续方向：

```text
1. EvidenceOrganizationAgent。
2. PatientRewriteAgent。
3. TraceReviewAgent。
4. ExperienceCandidateMiningAgent。
5. LLM Judge Agent。
6. LangGraph / Agent SDK 受控实验。
7. 多 Agent / Handoffs。
8. Agent Execution PostgreSQL 持久化。
9. Agent Console 页面。
```

这些应进入 Phase 6-P1 或更后阶段。

---

# 十三、最终结论

Phase 6-P0 的本质是：

```text
在不破坏 Runtime 主控的前提下，
引入第一个受控 Agent 执行单元，
让 Agent 产生可校验 Proposal，
并把 Proposal 纳入 Runtime Validation、Trace、Audit、Evaluation 和 Governance。
```

它不是 Agent 化重写项目，而是给 ClinMindRuntime 增加受控智能执行能力。
