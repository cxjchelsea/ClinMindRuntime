# Phase 6-P0 开发任务清单：受控 Agent 执行层 MVP

> 上位实现规格：`docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase6_P0Agent_API与测试设计.md`  
> 当前目标：按最小闭环实现受控 Agent 执行层，不引入自由自治式 Agent。

---

# 一、Phase 6-P0 总目标

Phase 6-P0 要完成的不是完整 Agent 平台，而是一个最小闭环：

```text
RuntimeState
→ AgentPolicy
→ AgentRuntime
→ InquiryPlanningAgent
→ InquiryPlanProposal
→ AgentProposalValidator
→ RuntimeValidation
→ RuntimeTrace / Evaluation
```

最终要证明：

```text
Agent 能被 Runtime 授权调用，
Agent 只能生成 Proposal，
Proposal 能被校验、拒绝、部分采纳或降级，
Agent 不能绕过 SafetyGate / DecisionBoundary / RuntimeTrace / Evaluation。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P6-A | 建立 Agent domain 基础对象 | 待做 |
| P6-B | 建立 AgentRegistry / AgentPolicy | 待做 |
| P6-C | 建立 AgentRuntime 执行框架 | 待做 |
| P6-D | 实现 InquiryPlanningAgent MVP | 待做 |
| P6-E | 实现 AgentProposalValidator / RuntimeValidation 接入 | 待做 |
| P6-F | Runtime 主链路接入 Capability Orchestration | 待做 |
| P6-G | Agent Debug API | 待做 |
| P6-H | Agent Trace / Audit 接入 | 待做 |
| P6-I | Agent Evaluation Scorer | 待做 |
| P6-J | 测试、人工验证与冻结记录 | 待做 |

---

# 三、P6-A：建立 Agent domain 基础对象

## 目标

新增 Agent 执行层的基础数据结构。

## 建议包路径

```text
src/main/java/.../agent/
```

具体包名以当前项目实际包结构为准，但建议保持：

```text
agent
agent.inquiry
capability
```

## 任务

```text
[ ] 新增 AgentId / AgentType / AgentStatus 等枚举或值对象。
[ ] 新增 AgentMetadata。
[ ] 新增 AgentContext。
[ ] 新增 AgentExecutionRequest。
[ ] 新增 AgentExecutionResult。
[ ] 新增 AgentProposal 抽象或接口。
[ ] 新增 AgentExecutionStatus。
[ ] 新增 AgentTrace。
```

## 验收标准

```text
[ ] 所有对象可单元测试。
[ ] 字段不包含 raw patient dialogue。
[ ] AgentProposal 不能包含 PatientOutput。
[ ] AgentExecutionResult 有明确 status / warnings / rejection reasons。
```

---

# 四、P6-B：建立 AgentRegistry / AgentPolicy

## 目标

让 Runtime 能知道有哪些 Agent，以及当前是否允许调用。

## 任务

```text
[ ] 新增 AgentRegistry 接口。
[ ] 新增 InMemoryAgentRegistry 实现。
[ ] 注册 InquiryPlanningAgent metadata。
[ ] 新增 AgentPolicy。
[ ] 实现 symptom_group 支持校验。
[ ] 实现 enabled / disabled 校验。
[ ] 实现 missing_facts 为空时拒绝调用。
[ ] 实现 fail-closed 默认策略。
```

## 验收标准

```text
[ ] 未注册 Agent 无法调用。
[ ] disabled Agent 无法调用。
[ ] unsupported symptom_group 无法调用。
[ ] missing_facts 为空时拒绝或降级。
[ ] Policy 拒绝时返回明确 reasons。
```

---

# 五、P6-C：建立 AgentRuntime 执行框架

## 目标

统一执行 Agent，并返回标准 AgentExecutionResult。

## 任务

```text
[ ] 新增 AgentRuntime。
[ ] AgentRuntime 调用 AgentRegistry。
[ ] AgentRuntime 调用 AgentPolicy。
[ ] AgentRuntime 构造 execution_id。
[ ] AgentRuntime 捕获 Agent 执行异常。
[ ] AgentRuntime 输出 AgentExecutionResult。
[ ] AgentRuntime 生成 AgentTrace。
```

## 验收标准

```text
[ ] 成功执行返回 SUCCESS。
[ ] Policy 拒绝返回 POLICY_REJECTED。
[ ] Agent 异常返回 FAILED 或 DEGRADED。
[ ] 任何失败都有 error_code / reasons。
[ ] 不抛出未处理异常到 Controller。
```

---

# 六、P6-D：实现 InquiryPlanningAgent MVP

## 目标

实现第一个受控 Agent：问诊规划 Agent。

## 任务

```text
[ ] 新增 InquiryPlanningAgent。
[ ] 新增 InquiryPlanningInput。
[ ] 新增 InquiryPlanProposal。
[ ] 新增 InquiryQuestionCandidate。
[ ] 基于 missing_facts 生成追问候选。
[ ] 基于 red_flag_candidates 提高问题优先级。
[ ] 控制 max_question_count。
[ ] 生成 patient_safe_wording。
[ ] 输出 reasoning_summary / safety_notes。
```

## P0 实现方式

Phase 6-P0 可以先用 rule-based / deterministic mock，不需要接 LLM。

建议规则：

```text
duration → 追问持续时间
severity → 追问严重程度
radiation_pain → 追问是否放射痛
dyspnea → 追问是否呼吸困难
history → 追问既往病史
red_flag → 高优先级
```

## 验收标准

```text
[ ] 输入 chest_pain + missing duration 时生成持续时间问题。
[ ] red_flag 相关 missing facts 优先级为 HIGH。
[ ] 问题数量不超过 max_question_count。
[ ] 问题文本不包含诊断暗示。
[ ] 每个问题都有 target_missing_fact。
```

---

# 七、P6-E：实现 AgentProposalValidator / RuntimeValidation 接入

## 目标

确保 Agent 输出不能越权。

## 任务

```text
[ ] 新增 AgentProposalValidator。
[ ] 新增 InquiryPlanProposalValidator。
[ ] 校验 proposed_questions 非空。
[ ] 校验 proposed_questions 数量。
[ ] 校验 target_missing_fact 合法。
[ ] 校验 question_text 不含诊断暗示。
[ ] 校验 clinical_purpose 非空。
[ ] 校验 risk_related 问题优先级。
[ ] 新增 RuntimeValidationService 或扩展现有校验服务。
[ ] 将 AgentProposal 接入 RuntimeValidation。
```

## 禁止词 / 风险提示示例

P0 可先配置简单规则：

```text
你是不是心梗
你可能是癌症
你应该吃
你可以不用去医院
确诊
最终诊断
```

## 验收标准

```text
[ ] 合法 Proposal 通过。
[ ] 诊断暗示被拒绝。
[ ] 超过问题数量时部分采纳或拒绝。
[ ] 缺少 target_missing_fact 被拒绝。
[ ] 所有拒绝都有 reasons。
```

---

# 八、P6-F：Runtime 主链路接入 Capability Orchestration

## 目标

在不重写 Runtime 主链路的前提下，加入受控能力编排节点。

## 任务

```text
[ ] 新增 CapabilityOrchestrationService。
[ ] 新增 CapabilityInvocationPlan。
[ ] 在 RuntimeService 合适节点调用 CapabilityOrchestrationService。
[ ] 当前仅支持 INQUIRY_PLANNING capability。
[ ] 将 InquiryPlanProposal 转换为 next question candidates 或 QuestionPolicy 输入。
[ ] 不直接修改 RuntimeState 核心字段。
[ ] 不绕过 SafetyGate / DecisionBoundary。
```

## 验收标准

```text
[ ] Runtime 主链路仍可正常执行。
[ ] 高风险输入仍先经过 SafetyGate。
[ ] Agent Proposal 不直接变成 PatientOutput。
[ ] Runtime 可以在 Agent 失败时降级到原有问诊策略。
```

---

# 九、P6-G：Agent Debug API

## 目标

提供最小 debug API 观察 Agent 执行。

## 任务

```text
[ ] 新增 AgentDebugController。
[ ] 实现 POST /api/v1/debug/agents/inquiry-planning/run。
[ ] 实现 GET /api/v1/debug/agents/executions/{execution_id}。
[ ] 实现 GET /api/v1/debug/agents/registry。
[ ] 所有响应使用 Safe DTO。
[ ] 接入 DebugTokenFilter / ActorContext / AccessPolicy。
```

## 验收标准

```text
[ ] 未授权访问被拒绝。
[ ] PATIENT 角色不能调用。
[ ] DEVELOPER / EVALUATOR 可调用。
[ ] API 不返回 raw runtime state。
[ ] API 不返回未脱敏患者原文。
```

---

# 十、P6-H：Agent Trace / Audit 接入

## 目标

让 Agent 执行可复盘。

## 任务

```text
[ ] AgentRuntime 生成 AgentTrace。
[ ] AgentTrace 进入 RuntimeTrace 或可被 RuntimeTrace 引用。
[ ] Agent API 调用写入 AuditLog 或 Debug action log。
[ ] Trace 中记录 agent_id、agent_version、policy_decision、validation_decision。
[ ] Trace 中记录 accepted / rejected question ids。
[ ] Trace 不保存 raw patient dialogue。
```

## 验收标准

```text
[ ] 每次 Agent 执行有 trace_id。
[ ] Policy 拒绝也有 trace。
[ ] Validation 拒绝也有 trace。
[ ] AuditLog 可看到 Agent debug API 调用。
```

---

# 十一、P6-I：Agent Evaluation Scorer

## 目标

让 Agent 能力进入 Evaluation，而不是只靠人工体验。

## 任务

```text
[ ] 新增 InquiryPlanCoverageScorer。
[ ] 新增 RedFlagQuestionPriorityScorer。
[ ] 新增 PatientSafeQuestionScorer。
[ ] 新增 AgentTraceCompletenessScorer。
[ ] 在 EvaluationResult 中加入 Agent 相关 metric。
[ ] 为 EvaluationCase 增加 expected_missing_facts / expected_red_flag_questions 支持，或用 metadata 承载。
```

## 验收标准

```text
[ ] Scorer 可独立单元测试。
[ ] Agent question 覆盖 missing_facts 时得分提高。
[ ] 忽略 red_flag 时得分降低。
[ ] 问题含诊断暗示时安全得分失败。
[ ] 缺少 AgentTrace 时 trace completeness 失败。
```

---

# 十二、P6-J：测试、人工验证与冻结记录

## 目标

完成 Phase 6-P0 收口。

## 任务

```text
[ ] 完成 AgentRegistryTest。
[ ] 完成 AgentPolicyTest。
[ ] 完成 AgentRuntimeTest。
[ ] 完成 InquiryPlanningAgentTest。
[ ] 完成 AgentProposalValidatorTest。
[ ] 完成 RuntimeValidationServiceTest。
[ ] 完成 AgentDebugControllerTest。
[ ] 完成 AgentTraceIntegrationTest。
[ ] 完成 Agent Evaluation Scorer tests。
[ ] 运行 mvn test。
[ ] 运行 console-web npm run test / npm run build，确认未破坏前端。
[ ] 编写 Phase6_P0人工测试结果.md。
[ ] 编写 Phase6_P0冻结记录.md。
```

## 验收标准

```text
[ ] 所有后端测试通过。
[ ] 前端测试 / build 不回归。
[ ] 人工测试覆盖高风险胸痛、普通发热、missing_facts 为空三个场景。
[ ] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十三、推荐实现顺序

建议严格按以下顺序实现：

```text
1. P6-A：Agent domain 基础对象。
2. P6-B：AgentRegistry / AgentPolicy。
3. P6-D：InquiryPlanningAgent MVP。
4. P6-E：AgentProposalValidator。
5. P6-C：AgentRuntime。
6. P6-H：AgentTrace。
7. P6-G：Debug API。
8. P6-F：Runtime 主链路接入 Capability Orchestration。
9. P6-I：Evaluation Scorer。
10. P6-J：测试、人工验证、冻结记录。
```

原因：

```text
先保证 Agent 本身受控，
再让 Runtime 调用它，
最后才接入 Evaluation 和冻结。
```

---

# 十四、开发期间禁止事项

```text
1. 不创建自由 Agent loop。
2. 不让 Agent 自己规划工具调用。
3. 不让 Agent 调 RAG / MCP / Python Provider。
4. 不让 Agent 输出 PatientOutput。
5. 不让 Agent 修改 RuntimeState。
6. 不把 Agent 的 reasoning_summary 暴露给患者端。
7. 不跳过 AgentProposalValidator。
8. 不跳过 RuntimeValidation。
9. 不因为 Agent 执行失败影响原 Runtime 主链路兜底。
10. 不改写 Phase 1–5 冻结记录。
```

---

# 十五、Phase 6-P0 完成后的后置任务

```text
1. Phase 6-P1：EvidenceOrganizationAgent。
2. Phase 6-P1：PatientRewriteAgent。
3. Phase 6-P1：TraceReviewAgent。
4. Phase 6-P1：Agent Console 页面。
5. Phase 7-P0：RAG EvidenceProvider。
6. Phase 8-P0：Python AI Provider / ModelProvider。
7. Phase 9-P0：Tool / MCP / Skills。
```

---

# 十六、最终 Definition of Done

Phase 6-P0 完成的最终标准：

```text
[ ] Runtime 可以受控调用 InquiryPlanningAgent。
[ ] Agent 只能输出 InquiryPlanProposal。
[ ] Proposal 必须经过 Validator 和 RuntimeValidation。
[ ] Runtime 可以拒绝危险 Proposal。
[ ] Agent 执行进入 Trace / Audit。
[ ] Agent 能力进入 Evaluation。
[ ] Debug API 可以安全观察 Agent 执行。
[ ] 所有新增测试和既有回归测试通过。
[ ] Phase6_P0冻结记录完成。
```
