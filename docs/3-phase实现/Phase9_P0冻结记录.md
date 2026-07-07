# Phase 9-P0 冻结记录：Tool / MCP / Skills 受控接入 MVP

> 上位规格：`Phase9_P0Tool_MCP_Skills受控接入_实现规格.md`
> API 与测试设计：`Phase9_P0Tool_MCP_Skills_API与测试设计.md`
> 任务清单：`Phase9_P0开发任务清单.md`
> 人工测试：`Phase9_P0人工测试结果.md`
> 冻结日期：2026-07-07

---

# 一、冻结结论

Phase 9-P0 **Tool / MCP / Skills 受控接入 MVP** 已实现并通过自动化测试、全量回归和人工验收，现冻结。

本阶段完成的是外部能力治理域的最小闭环：

```text
ToolRegistryEntry / McpServerRegistryEntry / SkillRegistryEntry
-> ToolInvocationPolicy
-> ToolInvocationRuntime
-> ToolResultValidationService
-> ToolGovernanceSnapshot
-> Trace / Audit
-> Evaluation Scorer
-> Candidate Mapping
-> Review-required governance
```

---

# 二、已实现能力

## Tool governance domain

新增：

- `ToolRegistryEntry`
- `McpServerRegistryEntry`
- `SkillRegistryEntry`
- `ToolInvocationRequest`
- `ToolInvocationResult`
- `ToolGovernanceSnapshot`
- `ToolValidationResult`
- `ToolRegistryStatus`
- `ToolType`
- `ToolSideEffectLevel`
- `McpServerType`
- `SkillType`
- `ToolInvocationStatus`
- `ToolResultType`

## Store / service / policy

新增：

- in-memory tool governance stores
- `ToolGovernanceService`
- `ToolInvocationRuntime`
- `ToolResultValidationService`
- `ToolRegistryPolicy`
- `McpServerRegistryPolicy`
- `SkillRegistryPolicy`
- `ToolInvocationPolicy`

## Mock / local adapters

新增：

- `MockGuidelineLookupToolAdapter`
- `LocalClinicalCalculatorToolAdapter`
- `MockSkillSummarizerAdapter`

这些 adapter 只返回结构化 `ToolResult` / `ExternalContext` / `SkillResult`，不返回 `PatientOutput`、最终诊断、治疗指令或真实外部系统原文。

## Debug / Governance API

新增：

- `POST /api/v1/debug/tool-governance/tools`
- `GET /api/v1/debug/tool-governance/tools`
- `GET /api/v1/debug/tool-governance/tools/{tool_registry_id}`
- `POST /api/v1/debug/tool-governance/mcp-servers`
- `GET /api/v1/debug/tool-governance/mcp-servers`
- `POST /api/v1/debug/tool-governance/skills`
- `GET /api/v1/debug/tool-governance/skills`
- `POST /api/v1/debug/tool-governance/invocations/run`
- `GET /api/v1/debug/tool-governance/invocations/{invocation_id}`

## Trace / Audit

新增 audit action：

- `CREATE_TOOL_REGISTRY_ENTRY`
- `CREATE_MCP_SERVER_REGISTRY_ENTRY`
- `CREATE_SKILL_REGISTRY_ENTRY`
- `RUN_TOOL_INVOCATION`
- `RUN_MCP_TOOL_INVOCATION`
- `RUN_SKILL_INVOCATION`
- `TOOL_INVOCATION_POLICY_REJECTED`
- `TOOL_RESULT_VALIDATION_REJECTED`

新增 audit resource：

- `TOOL_GOVERNANCE`
- `TOOL_INVOCATION`

## Evaluation scorer

新增：

- `ToolRegistryCompletenessScorer`
- `ToolInvocationTraceScorer`
- `ToolResultBoundaryScorer`
- `ToolSideEffectPolicyScorer`
- `ToolFallbackSafetyScorer`

支持 tag：

- `tool_governance_eval`
- `tool_invocation_eval`

## Candidate governance

已扩展 `CandidateMappingPolicy`：

- tool registry failure -> trace/governance review candidate
- tool boundary failure -> patient boundary review candidate
- side effect policy failure -> safety governance review candidate
- fallback trace failure -> trace quality review candidate

所有 candidate 仍需人工 review，不自动发布 ToolRegistryEntry、ToolPolicy、McpServerRegistryEntry 或 SkillRegistryEntry。

---

# 三、治理边界

## ToolRegistryPolicy

冻结规则：

- `tool_id` / `tool_version` 必填。
- `input_schema_version` / `output_schema_version` 必填。
- `allowed_use_cases` 不得为空。
- `patient_direct_answer` 必须被 forbidden。
- `side_effect_level=EXTERNAL_WRITE` / `HIGH_RISK_WRITE` 在 P0 拒绝。
- `patient_output_allowed=true` 且 `requires_validation=false` 拒绝。

## McpServerRegistryPolicy

冻结规则：

- P0 只允许 `MOCK` / `LOCAL` MCP-like server。
- `REMOTE` MCP server 拒绝。
- `allowed_tool_ids` / `allowed_use_cases` 不得为空。
- 外部写和高风险写 MCP server 拒绝。

## SkillRegistryPolicy

冻结规则：

- `skill_id` / `skill_version` 必填。
- `input_contract_version` / `output_contract_version` 必填。
- `patient_direct_answer` 必须被 forbidden。
- Skill 必须 `requires_validation=true`。

## ToolInvocationPolicy

冻结规则：

- 未登记 tool 拒绝。
- `DISABLED` / `BLOCKED` tool 返回 `SKIPPED`。
- forbidden use case 拒绝。
- `patient_direct_answer` 一律拒绝。
- 外部写和高风险写调用一律拒绝。
- schema version mismatch 拒绝。
- policy rejected 时不执行 adapter。

## ToolResultValidationService

冻结规则：

- `result_type` 必须存在。
- `PatientOutput` 字段拒绝。
- final diagnosis 表达拒绝。
- treatment instruction / prescription 表达拒绝。
- secret-like 内容拒绝。
- executable script 内容拒绝。
- raw external response 拒绝。

## ToolInvocationRuntime

冻结规则：

- 执行前必须经过 `ToolInvocationPolicy`。
- adapter 执行后必须经过 `ToolResultValidationService`。
- adapter exception 返回 fallback，不阻断 Runtime 主链路。
- invocation result 必须保存。
- create / run / reject / fallback 必须可审计。

---

# 四、测试结果

Phase 9-P0 targeted Java tests：

```text
27 run, 0 failures, 0 errors, 0 skipped
```

完整 Java 回归：

```text
525 run, 0 failures, 0 errors, 23 skipped
```

Python provider / frontend：

```text
未修改 python-provider 或 console-web，pytest / npm test / npm build 不适用
```

---

# 五、未做事项

以下事项明确后置，不属于 Phase 9-P0 冻结范围：

1. 真实远程 MCP Server 接入。
2. Spring AI MCP adapter。
3. Skills 文件系统与 Skill Store。
4. 真实第三方医疗系统接入。
5. EHR / HIS / LIS / PACS 接入。
6. 高风险写工具审批流。
7. Secret 管理。
8. Tool Console / Skill Console。
9. Browser Agent / Computer Use / RPA。
10. 自动工具发现、自动工具发布或 Agent 自治调用任意工具。

---

# 六、最终冻结状态

Phase 9-P0 已冻结。

冻结后的约束：

- 不继续向 Phase 9-P0 堆叠真实 MCP、真实外部系统、高风险写工具或 Browser / Computer Use 能力。
- 后续能力进入 Phase 9-P1 / Phase 9-P2 / Phase 10。
- 已冻结代码可接受 bug fix、测试补强和文档修正。
- 不得突破 Runtime 主控、ToolInvocationPolicy、ToolResultValidationService、SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 和 Candidate Governance 边界。
