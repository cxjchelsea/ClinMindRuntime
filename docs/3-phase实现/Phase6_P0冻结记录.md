# Phase 6-P0 冻结记录

> 本文档记录 ClinMindRuntime Phase 6-P0「受控 Agent 执行层 MVP」的冻结状态、依据、边界与后置任务。  
> 冻结表示 P0 最小闭环已完成，后续不再向 Phase 6-P0 继续堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 6-P0
冻结状态：已冻结
前置状态：Phase 1–5 已全部冻结
冻结日期：2026-07-02
代码基线：commit fadc02e
```

Phase 6-P0 已完成的主线：

```text
AgentRegistry / AgentPolicy / AgentRuntime
→ InquiryPlanningAgent（rule-based MVP）
→ InquiryPlanProposal + AgentProposalValidator + RuntimeValidationService
→ CapabilityOrchestrationService 接入 RuntimeService
→ QuestionTestPolicy 采纳 Agent 追问 Proposal
→ Agent Debug API + AuditLog + AgentTrace
→ Agent Evaluation Scorer（agent_eval 标签门控）
```

Phase 6-P0 已证明：

```text
Runtime 可在授权下调用 Agent；
Agent 只能生成可校验 Proposal；
Proposal 可被接受 / 部分接受 / 拒绝；
Agent 不能绕过 SafetyGate / DecisionBoundary；
Agent 执行进入 Trace / Audit / Evaluation 闭环。
```

---

# 二、冻结依据

## 2.1 任务清单

```text
docs/3-phase实现/Phase6_P0开发任务清单.md — P6-A ~ P6-J 均已完成
```

## 2.2 自动化测试

```text
mvn test — exit 0
新增测试：AgentRegistryTest / AgentPolicyTest / InquiryPlanningAgentTest
         InquiryPlanProposalValidatorTest / AgentRuntimeTest
         AgentDebugControllerTest / Phase6P0AcceptanceIntegrationTest
Phase 1–5 回归：未被破坏
```

## 2.3 人工验收

```text
docs/3-phase实现/Phase6_P0人工测试结果.md
覆盖：高风险胸痛、普通发热、missing_facts 为空
```

## 2.4 规格文档

```text
docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md
docs/3-phase实现/Phase6_P0Agent_API与测试设计.md
docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md（已同步为 P0 冻结态）
```

---

# 三、代码落点（摘要）

| 模块 | 包路径 |
|------|--------|
| Agent Domain | `com.clinmind.runtime.agent.*` |
| InquiryPlanningAgent | `agent/inquiry/` |
| Capability Orchestration | `agent/capability/CapabilityOrchestrationService` |
| Debug API | `agent/api/AgentDebugController` |
| Evaluation Scorer | `evaluation/scorer/InquiryPlanCoverageScorer` 等 4 项 |
| Runtime 集成 | `RuntimeService` / `QuestionTestPolicyService` / `RuntimeState.agentOrchestration` |

Debug API：

```text
POST /api/v1/debug/agents/inquiry-planning/run
GET  /api/v1/debug/agents/executions/{execution_id}
GET  /api/v1/debug/agents/registry
```

---

# 四、冻结边界（仍禁止）

```text
1. 不向 Phase 6-P0 继续堆 LLM / 多 Agent / LangGraph 主控。
2. 不接真实 RAG / MCP / Python Provider。
3. 不让 Agent 输出 PatientOutput 或直接修改 RuntimeState 核心字段。
4. 不绕过 AgentPolicy / AgentProposalValidator / RuntimeValidation。
5. 不向 Phase 1–5 已冻结能力继续堆功能（bug fix 除外）。
```

---

# 五、已知限制

```text
1. InquiryPlanningAgent 为 deterministic rule-based，非 LLM。
2. Agent 执行与 Trace 为 in-memory，重启后丢失。
3. Agent Scorer 需 evaluation case 带 agent_eval 标签才生效。
4. 无 Agent Console 前端页面。
5. console-web 全量 vitest 有 3 项 5s 超时 flake（与 P6 无直接关联）。
```

---

# 六、后置任务（Phase 6-P1 及以后）

```text
1. EvidenceOrganizationAgent
2. PatientRewriteAgent
3. TraceReviewAgent
4. Agent Console 页面
5. Agent Execution PostgreSQL 持久化
6. LLM-backed InquiryPlanning（仍受控 Proposal 模式）
7. Phase 7-P0：RAG EvidenceProvider
```

---

# 七、最终结论

Phase 6-P0 的本质已落地：

```text
在不破坏 Runtime 主控的前提下，
引入第一个受控 Agent 执行单元 InquiryPlanningAgent，
让 Agent 产生可校验 Proposal，
并把 Proposal 纳入 Runtime Validation、Trace、Audit 与 Evaluation。
```

下一阶段建议：**Phase 6-P1 Agent 能力扩展** 或 **Phase 7-P0 RAG EvidenceProvider**（按路线图优先级决策）。
