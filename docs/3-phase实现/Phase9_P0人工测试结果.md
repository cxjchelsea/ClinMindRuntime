# Phase 9-P0 人工测试结果：Tool / MCP / Skills 受控接入 MVP

> 规格：`Phase9_P0Tool_MCP_Skills受控接入_实现规格.md`
> API 与测试设计：`Phase9_P0Tool_MCP_Skills_API与测试设计.md`
> 任务清单：`Phase9_P0开发任务清单.md`
> 测试日期：2026-07-07

---

# 一、测试范围

本次验证覆盖 Phase 9-P0 的外部能力治理最小闭环：

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

本次验证不覆盖真实第三方医疗系统、真实 EHR / HIS / LIS / PACS、真实远程 MCP Server、Browser Agent / Computer Use / RPA、高风险写工具、生产级 Secret 管理或 Tool Console / Skill Console。

---

# 二、自动化测试结果

## Java targeted tests

命令：

```powershell
mvn "-Dtest=ToolRegistryPolicyTest,McpServerRegistryPolicyTest,SkillRegistryPolicyTest,ToolInvocationPolicyTest,ToolGovernancePolicyTest,ToolResultValidationServiceTest,ToolInvocationRuntimeTest,ToolGovernanceDebugControllerTest,ToolGovernanceScorerTest,ToolGovernanceCandidateMappingTest" test
```

结果：

```text
27 run, 0 failures, 0 errors, 0 skipped
```

覆盖：

- ToolRegistryPolicy
- McpServerRegistryPolicy
- SkillRegistryPolicy
- ToolInvocationPolicy
- ToolResultValidationService
- ToolInvocationRuntime
- ToolGovernanceDebugController
- Tool Governance Evaluation Scorer
- Tool Governance Candidate Mapping

## Java full regression

命令：

```powershell
mvn test
```

结果：

```text
525 run, 0 failures, 0 errors, 23 skipped
```

## Python provider / frontend

本阶段未修改 `python-provider` 或 `console-web`，未运行 pytest、npm test 或 npm build。

---

# 三、人工场景验证

## 场景 1：登记 READ_ONLY local tool

验证点：

- `POST /api/v1/debug/tool-governance/tools` 可创建 `ToolRegistryEntry`。
- 返回 `tool_registry_id`。
- 默认 `status=DRAFT`。
- `SYSTEM_ADMIN` / `EVALUATION_REVIEWER` 可写。
- `READ_ONLY_OBSERVER` 不能写。

结果：通过。

## 场景 2：登记 high-risk write tool

输入：

```text
side_effect_level=HIGH_RISK_WRITE
```

预期：

- ToolRegistryPolicy 拒绝。
- API 返回 `TOOL_GOVERNANCE_POLICY_REJECTED`。
- 不创建可调用工具。

结果：通过。

## 场景 3：登记 MCP Server

验证点：

- `MOCK` / `LOCAL` MCP-like server 可登记。
- `REMOTE` MCP Server 在 P0 被拒绝。
- 需要声明 allowed tool ids 和 allowed use cases。

结果：通过。

## 场景 4：登记 Skill

验证点：

- Skill 必须有 input / output contract version。
- `patient_direct_answer` 必须被 forbidden。
- `requires_validation=false` 被拒绝。

结果：通过。

## 场景 5：调用 MockGuidelineLookupTool

验证点：

- `POST /api/v1/debug/tool-governance/invocations/run` 可调用已登记 tool。
- 返回 `EXTERNAL_CONTEXT`。
- 不返回 `PatientOutput`。
- 不返回 final diagnosis / treatment instruction。
- invocation 可通过 GET 查询。

结果：通过。

## 场景 6：patient_direct_answer use case

输入：

```text
use_case=patient_direct_answer
```

预期：

- ToolInvocationPolicy 拒绝。
- adapter 不执行。
- 返回 `POLICY_REJECTED` / `NO_OP`。

结果：通过。

## 场景 7：adapter failure fallback

验证点：

- adapter 抛异常时返回 `FALLBACK`。
- `fallback_used=true`。
- fallback result 标记 `safe_to_continue=true`。
- 不阻断 Runtime 主链路。

结果：通过。

## 场景 8：ToolResultValidationService 越界拒绝

验证点：

- `PatientOutput` 字段被拒绝。
- final diagnosis 表达被拒绝。
- treatment instruction / prescribe 表达被拒绝。
- secret / script / raw external response 被拒绝。

结果：通过。

## 场景 9：Evaluation / Candidate 治理接入

验证点：

- `tool_governance_eval` / `tool_invocation_eval` tag 可触发对应 scorer。
- 缺 tool version、缺 trace、validation rejected、高风险 side effect、fallback trace 缺失可被 scorer 识别。
- scorer failure 可映射为 review-required candidate。
- 不自动发布 ToolRegistryEntry、ToolPolicy 或 Skill。

结果：通过。

---

# 四、结论

Phase 9-P0 自动化与人工验收通过。

本阶段实现的是 Tool / MCP / Skills 受控接入 MVP，不是完整工具生态、真实 MCP 平台、真实外部医疗系统接入或工具自治调用平台。所有外部能力都必须经过 Registry、Policy、Invocation、Validation、Trace、Audit、Evaluation 和 Candidate Governance；工具结果不能直接进入 PatientOutput，也不能修改 RuntimeState 决策。
