# Phase 9-P0 Tool / MCP / Skills 受控接入实现规格

> 上位总设计：`docs/1-总设计/ClinMindRuntime完整系统设计.md`  
> 阶段路线图：`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`  
> 技术实现总方案：`docs/1-总设计/ClinMindRuntime技术实现总方案.md`  
> 前置阶段：Phase 8-P2 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 已冻结  
> 当前 Phase：Phase 9-P0  
> 当前目标：建立 Tool / MCP / Skills 的受控接入最小闭环，使外部能力可以被登记、授权、调用、校验、降级、追踪、审计、评估和候选治理，但不能接管 Runtime、不能直接输出 PatientOutput、不能直接修改 RuntimeState。

---

# 一、Phase 定位

Phase 9-P0 不是完整工具生态，也不是把 MCP / Skills 直接开放给 Agent 自主调用。

Phase 9-P0 的目标是：

```text
在 Java Runtime 主控下，
新增外部能力治理域，
把 Tool / MCP / Skills 纳入统一的 Policy -> Invocation -> Validation -> Trace -> Audit -> Evaluation -> Candidate Governance 闭环。
```

核心命题：

```text
Tool / MCP / Skills 只能提供 ToolResult / ExternalContext / SkillResult，
不能决定 RuntimeState，不能直接输出 PatientOutput，不能绕过 SafetyGate 和 DecisionBoundary。
```

---

# 二、前置状态

当前已经具备：

```text
Phase 6-P0：受控 Agent 执行层
Phase 7-P0：RAG EvidenceProvider
Phase 7-P1：KG-lite / Graph Evidence
Phase 8-P0：Python AI Provider / Embedding / Rerank
Phase 8-P1：Judge / Risk / ProviderCapabilityProfile
Phase 8-P2：ModelRegistry / PromptRegistry / TrainingDatasetVersion / Model Governance
```

Phase 9-P0 应复用：

```text
Runtime 主控链路
ProviderCapabilityPolicy / ProviderValidation 思路
ModelGovernance 的 registry / policy / audit / evaluation / candidate 思路
Evaluation Scorer
CandidateMappingPolicy
AuditLogService
ActorContext / Debug Access Policy
```

---

# 三、当前不做什么

Phase 9-P0 明确不做：

```text
1. 不接真实第三方医疗系统。
2. 不接真实 EHR / HIS / LIS / PACS。
3. 不接真实线上 MCP Server 作为强依赖。
4. 不允许 Agent 自主发现和调用任意工具。
5. 不允许 Tool / MCP / Skill 直接修改 RuntimeState。
6. 不允许 Tool / MCP / Skill 直接写入 PatientOutput。
7. 不允许 Tool / MCP / Skill 决定诊断、治疗、转诊或用药。
8. 不做生产级 Secret 管理。
9. 不做生产级工具市场 / Skill Store。
10. 不做浏览器自动操作、Computer Use、RPA 或真实外部写操作。
11. 不做真实支付、预约、处方、消息发送等高风险工具。
```

P0 可以做：

```text
ToolRegistry MVP
McpServerRegistry MVP
SkillRegistry MVP
ToolInvocationPolicy
ToolInvocationRuntime
ToolResultValidationService
ToolGovernanceSnapshot
Debug API
Trace / Audit
Evaluation Scorer
Candidate Mapping
mock / deterministic local tools
mock MCP / mock Skill adapter
```

---

# 四、Phase 9-P0 核心链路

目标链路：

```text
RuntimeState / CapabilityOrchestrationRequest
↓
ToolCapabilityPolicy 判断是否允许外部能力调用
↓
ToolInvocationRequest
↓
ToolInvocationRuntime
  ├── LocalToolAdapter
  ├── MockMcpAdapter
  └── MockSkillAdapter
↓
ToolInvocationResult / ExternalContext / SkillResult
↓
ToolResultValidationService
↓
Runtime 采纳 / 部分采纳 / 拒绝 / 降级
↓
ToolGovernanceSnapshot
↓
RuntimeTrace / AuditLog / Evaluation
↓
Candidate governance
```

核心边界：

```text
Registry 只登记，不自动授权。
Policy 决定是否可调用。
Adapter 只执行受控请求。
Validation 决定结果是否可进入 Runtime。
Runtime 决定是否采纳。
```

---

# 五、核心对象设计

## 5.1 ToolRegistryEntry

用于登记本地工具或受控工具能力。

建议字段：

```text
tool_registry_id
tool_id
tool_version
tool_name
tool_type
capability_type
allowed_use_cases
forbidden_use_cases
input_schema_version
output_schema_version
side_effect_level
patient_output_allowed
requires_validation
requires_decision_boundary
status
risk_level
created_at
created_by
```

tool_type 候选：

```text
LOCAL_DETERMINISTIC
MOCK_EXTERNAL
MCP_PROXY
SKILL_ADAPTER
```

side_effect_level 候选：

```text
READ_ONLY
LOCAL_COMPUTE
EXTERNAL_READ
EXTERNAL_WRITE
HIGH_RISK_WRITE
```

P0 只允许：

```text
READ_ONLY
LOCAL_COMPUTE
MOCK_EXTERNAL
```

## 5.2 McpServerRegistryEntry

用于登记 MCP Server 或 MCP-like server。

建议字段：

```text
mcp_server_registry_id
server_id
server_version
server_name
server_type
transport_type
allowed_tool_ids
forbidden_tool_ids
allowed_use_cases
side_effect_level
status
risk_level
created_at
created_by
```

P0 只支持 mock MCP adapter，不接真实线上 MCP Server。

## 5.3 SkillRegistryEntry

用于登记 Skill 能力。

建议字段：

```text
skill_registry_id
skill_id
skill_version
skill_name
skill_type
capability_type
allowed_use_cases
forbidden_use_cases
input_contract_version
output_contract_version
requires_validation
requires_decision_boundary
status
risk_level
created_at
created_by
```

P0 只支持 deterministic / mock skill。

## 5.4 ToolInvocationRequest

建议字段：

```text
invocation_id
runtime_id
session_id
tool_registry_id
capability_type
use_case
input_summary
input_payload
actor_context
schema_version
```

约束：

```text
input_payload 必须是结构化对象。
不得包含未脱敏患者原文。
不得包含完整医生端推理链。
不得包含可执行脚本。
```

## 5.5 ToolInvocationResult

建议字段：

```text
invocation_id
tool_registry_id
tool_id
tool_version
status
result_type
structured_result
external_context
warnings
error_code
latency_ms
trace
```

result_type 候选：

```text
TOOL_RESULT
EXTERNAL_CONTEXT
SKILL_RESULT
NO_OP
FALLBACK
```

## 5.6 ToolGovernanceSnapshot

用于进入 RuntimeState / Evaluation / Candidate。

建议字段：

```text
invocation_id
tool_registry_id
tool_id
tool_version
capability_type
use_case
policy_status
validation_status
fallback_used
side_effect_level
result_type
warnings
trace
```

---

# 六、Policy 与 Validation

必须新增：

```text
ToolInvocationPolicy
ToolRegistryPolicy
McpServerRegistryPolicy
SkillRegistryPolicy
ToolResultValidationService
```

最低规则：

```text
1. 未登记 tool / skill / mcp server 不允许调用。
2. forbidden use case 拒绝。
3. patient_direct_answer 拒绝。
4. side_effect_level=EXTERNAL_WRITE / HIGH_RISK_WRITE 在 P0 拒绝。
5. patient_output_allowed 默认 false。
6. requires_validation 默认 true。
7. 输出中出现 final diagnosis / treatment instruction / patient-facing directive 时拒绝。
8. 工具失败时 fallback，不阻断 Runtime 主链路。
```

---

# 七、P0 推荐 mock 工具

P0 可以先实现三个本地 mock 能力：

```text
1. LocalClinicalCalculatorTool
   - 只做本地 deterministic 计算。
   - 不输出诊断。

2. MockGuidelineLookupTool
   - 返回结构化 guideline metadata。
   - 不直接生成患者建议。

3. MockSkillSummarizer
   - 对输入 summary 进行结构化归纳。
   - 不输出 PatientOutput。
```

注意：这些 mock 能力只是证明工具治理链路，不代表真实临床工具。

---

# 八、与 Runtime 的关系

Phase 9-P0 可以接入 CapabilityOrchestration，但必须受控。

正确关系：

```text
RuntimeState
↓
CapabilityOrchestrationService 决定是否需要工具能力
↓
ToolInvocationPolicy
↓
ToolInvocationRuntime
↓
ToolResultValidationService
↓
Runtime 决定采纳 / 部分采纳 / 拒绝 / 降级
```

禁止：

```text
Agent -> 自主发现工具 -> 自主调用 -> 自主修改 RuntimeState
Tool -> 直接写入 PatientOutput
Tool -> 直接触发 SafetyGate
MCP -> 直接返回最终诊断
Skill -> 直接发布 TrainingDatasetVersion
```

---

# 九、与 Model Governance 的关系

Phase 9-P0 不属于模型训练治理，但可以复用治理思想。

可后续形成：

```text
ToolRegistryCandidate
McpServerRegistryCandidate
SkillRegistryCandidate
ToolInvocationPolicyCandidate
ToolResultValidationRuleCandidate
```

禁止自动发布：

```text
ToolRegistryEntry
McpServerRegistryEntry
SkillRegistryEntry
ToolPolicy
```

---

# 十、Trace / Audit

新增 Audit action：

```text
CREATE_TOOL_REGISTRY_ENTRY
CREATE_MCP_SERVER_REGISTRY_ENTRY
CREATE_SKILL_REGISTRY_ENTRY
RUN_TOOL_INVOCATION
RUN_MCP_TOOL_INVOCATION
RUN_SKILL_INVOCATION
TOOL_INVOCATION_POLICY_REJECTED
TOOL_RESULT_VALIDATION_REJECTED
```

新增 Audit resource：

```text
TOOL_GOVERNANCE
TOOL_INVOCATION
```

Trace 至少记录：

```text
invocation_id
tool_registry_id
tool_id
tool_version
use_case
side_effect_level
policy_status
validation_status
fallback_used
latency_ms
```

不得记录：

```text
未脱敏患者原文
完整患者对话
真实外部凭证
可执行脚本
真实外部系统响应原文
```

---

# 十一、Debug / Governance API

推荐最小 API：

```text
POST /api/v1/debug/tool-governance/tools
GET  /api/v1/debug/tool-governance/tools
GET  /api/v1/debug/tool-governance/tools/{tool_registry_id}

POST /api/v1/debug/tool-governance/mcp-servers
GET  /api/v1/debug/tool-governance/mcp-servers

POST /api/v1/debug/tool-governance/skills
GET  /api/v1/debug/tool-governance/skills

POST /api/v1/debug/tool-governance/invocations/run
GET  /api/v1/debug/tool-governance/invocations/{invocation_id}
```

权限：

```text
write / run：SYSTEM_ADMIN / EVALUATION_REVIEWER
read：SYSTEM_ADMIN / EVALUATION_REVIEWER / READ_ONLY_OBSERVER
PATIENT 禁止
```

---

# 十二、Evaluation Scorer

新增：

```text
ToolRegistryCompletenessScorer
ToolInvocationTraceScorer
ToolResultBoundaryScorer
ToolSideEffectPolicyScorer
ToolFallbackSafetyScorer
McpRegistrySafetyScorer
SkillRegistryContractScorer
```

P0 最小：

```text
ToolRegistryCompletenessScorer
ToolInvocationTraceScorer
ToolResultBoundaryScorer
ToolSideEffectPolicyScorer
ToolFallbackSafetyScorer
```

支持 tag：

```text
tool_governance_eval
tool_invocation_eval
```

---

# 十三、完成标准

Phase 9-P0 完成时必须满足：

```text
1. 可以登记 ToolRegistryEntry。
2. 可以登记 McpServerRegistryEntry。
3. 可以登记 SkillRegistryEntry。
4. 可以运行 mock/local ToolInvocation。
5. ToolInvocationPolicy 可拒绝 forbidden use case / patient_direct_answer / high-risk write。
6. ToolResultValidationService 可拒绝越界输出。
7. ToolGovernanceSnapshot 可进入 RuntimeState 或 Evaluation。
8. Debug API 可用。
9. Trace / Audit 可见。
10. Evaluation Scorer 能识别工具治理缺口。
11. Candidate Mapping 可沉淀 review-required candidate。
12. 工具失败时 fallback，不阻断 Runtime 主链路。
13. 不直接输出 PatientOutput。
14. 不修改 RuntimeState 决策。
15. `mvn test` 通过。
16. Phase 1–8 P2 既有测试不回归。
```

---

# 十四、后置任务

Phase 9-P0 不完成但可后置：

```text
1. 真实 MCP Server 接入。
2. Spring AI MCP adapter。
3. 真实 Skills 文件系统与 Skill Store。
4. 真实外部医疗系统 API。
5. Secret 管理。
6. Tool Console / Skill Console。
7. Browser Agent / Computer Use / RPA。
8. 高风险写工具的审批流。
```

---

# 十五、最终结论

Phase 9-P0 的本质是：

```text
从“模型能力可以被治理”
扩展到“外部工具、MCP、Skills 能力也可以被治理”。
```

它完成后，ClinMindRuntime 的外部能力域将具备最小闭环：

```text
Registry
→ Policy
→ Invocation
→ Validation
→ Trace / Audit
→ Evaluation
→ Candidate Governance
```

但仍然不做真实外部系统强依赖、不做高风险写操作、不让外部能力接管 Runtime。
