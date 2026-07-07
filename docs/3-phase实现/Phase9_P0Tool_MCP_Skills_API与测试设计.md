# Phase 9-P0 Tool / MCP / Skills API 与测试设计

> 上位实现规格：`docs/3-phase实现/Phase9_P0Tool_MCP_Skills受控接入_实现规格.md`  
> 前置冻结：`docs/3-phase实现/Phase8_P2冻结记录.md`  
> 当前 Phase：Phase 9-P0  
> 当前目标：定义 ToolRegistry、McpServerRegistry、SkillRegistry、ToolInvocationRuntime、ToolResultValidation、Tool Governance Debug API、Evaluation Scorer 与测试方案。

---

# 一、API 设计原则

Phase 9-P0 API 是 tool governance / debug API，不是 patient-facing API。

共同原则：

```text
1. PATIENT 角色禁止访问。
2. 所有写操作与 run 操作必须 Audit。
3. 所有 Tool / MCP / Skill 必须先登记再调用。
4. ToolInvocationPolicy 必须在调用前执行。
5. ToolResultValidationService 必须在调用后执行。
6. tool result 不能直接进入 PatientOutput。
7. P0 禁止真实外部写操作。
8. P0 禁止未治理真实 MCP Server 强依赖。
9. API 不返回未脱敏患者原文、真实凭证、完整外部系统响应原文。
```

---

# 二、API 分组

推荐路径前缀：

```text
/api/v1/debug/tool-governance
```

端点分组：

```text
Tool Registry API
MCP Server Registry API
Skill Registry API
Tool Invocation API
Tool Governance Snapshot API
```

---

# 三、Tool Registry API

## 3.1 POST /tools

请求：

```json
{
  "tool_id": "mock_guideline_lookup",
  "tool_version": "0.1.0",
  "tool_name": "Mock Guideline Lookup",
  "tool_type": "LOCAL_DETERMINISTIC",
  "capability_type": "GUIDELINE_LOOKUP",
  "allowed_use_cases": ["clinician_context", "evidence_enrichment"],
  "forbidden_use_cases": ["patient_direct_answer", "final_diagnosis"],
  "input_schema_version": "0.9.0",
  "output_schema_version": "0.9.0",
  "side_effect_level": "READ_ONLY",
  "patient_output_allowed": false,
  "requires_validation": true,
  "requires_decision_boundary": true,
  "risk_level": "LOW"
}
```

响应：

```json
{
  "tool_registry_id": "tool_reg_001",
  "tool_id": "mock_guideline_lookup",
  "tool_version": "0.1.0",
  "status": "DRAFT"
}
```

## 3.2 GET /tools

返回 Safe DTO 列表。

## 3.3 GET /tools/{tool_registry_id}

返回登记详情。

---

# 四、MCP Server Registry API

## 4.1 POST /mcp-servers

请求：

```json
{
  "server_id": "mock_mcp_server",
  "server_version": "0.1.0",
  "server_name": "Mock MCP Server",
  "server_type": "MOCK",
  "transport_type": "IN_PROCESS",
  "allowed_tool_ids": ["mock_guideline_lookup"],
  "forbidden_tool_ids": [],
  "allowed_use_cases": ["evidence_enrichment"],
  "side_effect_level": "READ_ONLY",
  "risk_level": "LOW"
}
```

P0 约束：

```text
server_type 只能是 MOCK / LOCAL。
不接真实远程 MCP server。
```

---

# 五、Skill Registry API

## 5.1 POST /skills

请求：

```json
{
  "skill_id": "mock_case_summary_skill",
  "skill_version": "0.1.0",
  "skill_name": "Mock Case Summary Skill",
  "skill_type": "LOCAL_DETERMINISTIC",
  "capability_type": "CASE_SUMMARY",
  "allowed_use_cases": ["clinician_context", "runtime_trace_summary"],
  "forbidden_use_cases": ["patient_direct_answer", "final_diagnosis"],
  "input_contract_version": "0.9.0",
  "output_contract_version": "0.9.0",
  "requires_validation": true,
  "requires_decision_boundary": true,
  "risk_level": "LOW"
}
```

---

# 六、Tool Invocation API

## 6.1 POST /invocations/run

请求：

```json
{
  "runtime_id": "runtime_demo_001",
  "tool_registry_id": "tool_reg_001",
  "use_case": "evidence_enrichment",
  "input_summary": {
    "symptom_group": "chest_pain",
    "known_facts": ["chest discomfort", "sweating"]
  },
  "input_payload": {
    "query_type": "guideline_metadata",
    "topic": "chest_pain_red_flags"
  },
  "schema_version": "0.9.0"
}
```

响应：

```json
{
  "invocation_id": "tool_inv_001",
  "tool_registry_id": "tool_reg_001",
  "status": "SUCCESS",
  "result_type": "EXTERNAL_CONTEXT",
  "structured_result": {
    "topic": "chest_pain_red_flags",
    "source_type": "mock_guideline_metadata"
  },
  "validation_status": "ACCEPTED",
  "fallback_used": false,
  "warnings": []
}
```

约束：

```text
ToolInvocationResult 不能包含 PatientOutput。
不能包含 final diagnosis。
不能包含 treatment instruction。
不能修改 RuntimeState。
```

## 6.2 GET /invocations/{invocation_id}

返回 Safe DTO。

---

# 七、Access Policy

权限：

```text
create / run：SYSTEM_ADMIN / EVALUATION_REVIEWER
read：SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER
PATIENT 禁止
```

测试要求：

```text
1. PATIENT create / run / read 全部拒绝。
2. READ_ONLY_OBSERVER 不能 create / run。
3. SYSTEM_ADMIN / EVALUATION_REVIEWER 可 create / run。
4. 所有拒绝返回受控错误。
```

---

# 八、Policy 测试

## 8.1 ToolRegistryPolicyTest

覆盖：

```text
1. tool_id / version 缺失拒绝。
2. allowed_use_cases 为空拒绝。
3. patient_output_allowed=true 且 requires_validation=false 拒绝。
4. side_effect_level=EXTERNAL_WRITE / HIGH_RISK_WRITE 在 P0 拒绝。
5. forbidden_use_cases 缺失 patient_direct_answer 时拒绝或补警告。
```

## 8.2 McpServerRegistryPolicyTest

覆盖：

```text
1. P0 远程 server_type 拒绝。
2. side_effect_level=EXTERNAL_WRITE 拒绝。
3. allowed_tool_ids 为空拒绝。
```

## 8.3 SkillRegistryPolicyTest

覆盖：

```text
1. skill_id / version 缺失拒绝。
2. output_contract_version 缺失拒绝。
3. patient_direct_answer use case 拒绝。
```

## 8.4 ToolInvocationPolicyTest

覆盖：

```text
1. 未登记 tool 拒绝。
2. forbidden use_case 拒绝。
3. patient_direct_answer 拒绝。
4. side_effect_level 高风险拒绝。
5. disabled tool SKIPPED。
6. 合法 READ_ONLY tool 允许。
```

---

# 九、Validation 测试

## 9.1 ToolResultValidationServiceTest

覆盖：

```text
1. 合法 structured_result ACCEPTED。
2. result 中出现 final diagnosis 拒绝。
3. result 中出现 treatment instruction 拒绝。
4. result 中出现 PatientOutput 字段拒绝。
5. schema_version 不匹配拒绝。
6. external raw response 过长或含敏感字段拒绝。
```

---

# 十、Runtime / Adapter 测试

## 10.1 ToolInvocationRuntimeTest

覆盖：

```text
1. MockGuidelineLookupTool 成功返回 ExternalContext。
2. MockSkillSummarizer 成功返回 SkillResult。
3. unknown adapter fallback。
4. adapter 抛异常 fallback。
5. fallback 不阻断主链路。
```

---

# 十一、Debug API 测试

建议测试类：

```text
ToolGovernanceDebugControllerTest
```

覆盖：

```text
1. 创建 tool registry。
2. 创建 mcp server registry。
3. 创建 skill registry。
4. run invocation。
5. get invocation。
6. PATIENT denied。
7. READ_ONLY_OBSERVER 只读。
8. Safe DTO 不泄露 raw patient dialogue / secret / executable script。
```

---

# 十二、Evaluation Scorer 测试

新增：

```text
ToolRegistryCompletenessScorerTest
ToolInvocationTraceScorerTest
ToolResultBoundaryScorerTest
ToolSideEffectPolicyScorerTest
ToolFallbackSafetyScorerTest
```

覆盖：

```text
1. tool registry 缺 version 失败。
2. invocation 缺 trace 失败。
3. tool result 包含 patient diagnosis leak 失败。
4. high risk side effect 被允许时失败。
5. fallback 缺记录失败。
```

---

# 十三、Candidate Mapping 测试

新增或扩展：

```text
ToolGovernanceCandidateMappingTest
```

覆盖：

```text
1. tool registry failure -> governance candidate。
2. tool boundary failure -> patient boundary candidate。
3. side effect policy failure -> safety governance candidate。
4. fallback trace failure -> trace quality candidate。
5. 所有 candidate review-required，不自动发布。
```

---

# 十四、人工测试场景

## 场景 1：登记 READ_ONLY local tool

预期：

```text
tool_registry_id 生成。
status=DRAFT。
Audit 记录 CREATE_TOOL_REGISTRY_ENTRY。
```

## 场景 2：登记 high-risk write tool

预期：

```text
ToolRegistryPolicy 拒绝。
不创建可调用工具。
```

## 场景 3：调用 MockGuidelineLookupTool

预期：

```text
返回 ExternalContext。
不返回 PatientOutput。
不返回 final diagnosis。
Trace / Audit 可见。
```

## 场景 4：patient_direct_answer use case

预期：

```text
ToolInvocationPolicy 拒绝。
不执行 adapter。
```

## 场景 5：adapter failure fallback

预期：

```text
返回 fallback result。
不阻断 Runtime。
Audit 记录 failure / fallback。
```

---

# 十五、回归测试要求

Phase 9-P0 完成前必须通过：

```text
mvn test
```

不得破坏：

```text
Phase 8-P2 Model Governance
Phase 8-P1 Provider Governance
Phase 8-P0 Python Provider
Phase 7 Evidence / Graph
Phase 6 Agent Runtime
SafetyGate
DecisionBoundary
EvaluationRunner
Candidate / Review
Persistence / Audit
Console Safe DTO
```

---

# 十六、完成标准

Phase 9-P0 API 与测试完成标准：

```text
1. ToolRegistryEntry 可创建 / 查询。
2. McpServerRegistryEntry 可创建 / 查询。
3. SkillRegistryEntry 可创建 / 查询。
4. ToolInvocation 可运行 mock/local adapter。
5. ToolInvocationPolicy 能拒绝 forbidden / patient_direct_answer / high-risk write。
6. ToolResultValidation 能拒绝越界结果。
7. Debug API 可用。
8. Trace / Audit 可见。
9. Evaluation Scorer 能识别工具治理缺口。
10. Candidate Mapping 可沉淀 review-required candidate。
11. `mvn test` 通过。
12. Phase 1–8 P2 既有测试不回归。
```

---

# 十七、最终结论

Phase 9-P0 API 与测试重点不是“能调用多少外部工具”，而是证明：

```text
任何外部能力都必须先登记、再授权、再调用、再校验、再进入 Trace / Audit / Evaluation / Candidate；
外部能力不能接管 Runtime，不能直接输出患者内容，不能执行高风险写操作。
```
