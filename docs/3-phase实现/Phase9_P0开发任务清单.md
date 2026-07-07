# Phase 9-P0 开发任务清单：Tool / MCP / Skills 受控接入 MVP

> 上位实现规格：`docs/3-phase实现/Phase9_P0Tool_MCP_Skills受控接入_实现规格.md`
> API 与测试设计：`docs/3-phase实现/Phase9_P0Tool_MCP_Skills_API与测试设计.md`
> 前置冻结：`docs/3-phase实现/Phase8_P2冻结记录.md`
> 当前目标：建立外部能力治理域的最小闭环，但不接真实第三方医疗系统、不做真实高风险写操作、不让 Tool / MCP / Skills 接管 Runtime。

---

# 一、Phase 9-P0 总目标

Phase 9-P0 要完成的不是完整工具生态，而是外部能力受控接入最小闭环：

```text
ToolRegistryEntry / McpServerRegistryEntry / SkillRegistryEntry
→ ToolInvocationPolicy
→ ToolInvocationRuntime
→ ToolResultValidationService
→ ToolGovernanceSnapshot
→ Trace / Audit
→ Evaluation Scorer
→ Candidate Mapping
→ Review-required governance
```

最终要证明：

```text
Tool / MCP / Skills 可以被登记、授权、调用、校验、降级、追踪、审计、评估和候选治理；
外部能力不能直接输出 PatientOutput；
外部能力不能修改 RuntimeState 决策；
外部能力不能执行高风险写操作；
外部能力失败时可以 fallback，不阻断 Runtime 主链路。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P9P0-A | 建立 tool governance domain 对象 | 已完成 |
| P9P0-B | 建立 in-memory store / registry | 已完成 |
| P9P0-C | 实现 Tool / MCP / Skill Registry Policy | 已完成 |
| P9P0-D | 实现 ToolInvocationPolicy | 已完成 |
| P9P0-E | 实现 mock/local adapters | 已完成 |
| P9P0-F | 实现 ToolInvocationRuntime | 已完成 |
| P9P0-G | 实现 ToolResultValidationService | 已完成 |
| P9P0-H | 实现 Debug / Governance API | 已完成 |
| P9P0-I | Trace / Audit 接入 | 已完成 |
| P9P0-J | Evaluation Scorer 接入 | 已完成 |
| P9P0-K | Candidate Mapping 接入 | 已完成 |
| P9P0-L | 测试、人工验证与冻结记录 | 已完成 |

---

# 三、P9P0-A：建立 tool governance domain 对象

## 目标

新增 Tool / MCP / Skills 治理对象。

## 建议包路径

```text
src/main/java/com/clinmind/runtime/toolgov/
src/main/java/com/clinmind/runtime/toolgov/registry/
src/main/java/com/clinmind/runtime/toolgov/invocation/
src/main/java/com/clinmind/runtime/toolgov/policy/
src/main/java/com/clinmind/runtime/toolgov/adapter/
src/main/java/com/clinmind/runtime/toolgov/api/
```

## 任务

```text
[x] 新增 ToolRegistryEntry。
[x] 新增 ToolRegistryStatus。
[x] 新增 ToolType。
[x] 新增 ToolSideEffectLevel。
[x] 新增 McpServerRegistryEntry。
[x] 新增 McpServerType。
[x] 新增 SkillRegistryEntry。
[x] 新增 SkillType。
[x] 新增 ToolInvocationRequest。
[x] 新增 ToolInvocationResult。
[x] 新增 ToolInvocationStatus。
[x] 新增 ToolResultType。
[x] 新增 ToolGovernanceSnapshot。
[x] 新增 ToolValidationResult。
```

## 验收标准

```text
[x] 所有 registry 对象包含 id / version / status。
[x] Invocation 对象包含 invocation_id / runtime_id / use_case / schema_version。
[x] 不包含 raw patient dialogue。
[x] 不包含 secret / executable script。
```

---

# 四、P9P0-B：建立 in-memory store / registry

## 目标

先用 in-memory store 完成 P0 MVP。

## 任务

```text
[x] 新增 ToolRegistryStore。
[x] 新增 McpServerRegistryStore。
[x] 新增 SkillRegistryStore。
[x] 新增 ToolInvocationStore。
[x] 使用 ConcurrentHashMap。
[x] 支持 create / findById / findAll。
```

## 验收标准

```text
[x] 不暴露可变内部集合。
[x] 单元测试覆盖 save / find。
```

---

# 五、P9P0-C：实现 Tool / MCP / Skill Registry Policy

## 目标

控制外部能力能否登记。

## 任务

```text
[x] 新增 ToolRegistryPolicy。
[x] 校验 tool_id / tool_version / allowed_use_cases。
[x] side_effect_level=EXTERNAL_WRITE / HIGH_RISK_WRITE 在 P0 拒绝。
[x] patient_output_allowed=true 且 requires_validation=false 拒绝。
[x] 新增 McpServerRegistryPolicy。
[x] P0 只允许 MOCK / LOCAL server。
[x] 新增 SkillRegistryPolicy。
[x] 校验 input / output contract version。
[x] patient_direct_answer forbidden。
```

## 验收标准

```text
[x] 高风险写能力不能登记为可调用。
[x] 远程 MCP server P0 拒绝。
[x] 所有拒绝带 reasons。
```

---

# 六、P9P0-D：实现 ToolInvocationPolicy

## 目标

控制外部能力能否被调用。

## 任务

```text
[x] 新增 ToolInvocationPolicy。
[x] 未登记 tool 拒绝。
[x] disabled tool SKIPPED。
[x] forbidden use_case 拒绝。
[x] patient_direct_answer 拒绝。
[x] side_effect_level=EXTERNAL_WRITE / HIGH_RISK_WRITE 拒绝。
[x] schema_version mismatch 拒绝或降级。
```

## 验收标准

```text
[x] 合法 READ_ONLY tool 允许。
[x] patient_direct_answer 一律拒绝。
[x] high-risk write 一律拒绝。
[x] policy rejected 不执行 adapter。
```

---

# 七、P9P0-E：实现 mock/local adapters

## 目标

用 deterministic adapter 证明调用链路，不接真实外部系统。

## 任务

```text
[x] 新增 ToolAdapter 接口。
[x] 新增 MockGuidelineLookupToolAdapter。
[x] 新增 LocalClinicalCalculatorToolAdapter。
[x] 新增 MockSkillSummarizerAdapter。
[x] adapter 只返回 structured_result。
[x] adapter 不返回 PatientOutput。
[x] adapter 不返回 final diagnosis / treatment instruction。
```

## 验收标准

```text
[x] MockGuidelineLookupTool 返回 ExternalContext。
[x] LocalClinicalCalculatorTool 返回 ToolResult。
[x] MockSkillSummarizer 返回 SkillResult。
[x] adapter failure 可被 runtime fallback。
```

---

# 八、P9P0-F：实现 ToolInvocationRuntime

## 目标

统一执行 policy、adapter、fallback、trace。

## 任务

```text
[x] 新增 ToolInvocationRuntime。
[x] 根据 tool_type / capability_type 选择 adapter。
[x] 执行前调用 ToolInvocationPolicy。
[x] 执行失败时返回 fallback result。
[x] 保存 ToolInvocationResult。
[x] 生成 ToolGovernanceSnapshot。
```

## 验收标准

```text
[x] policy rejected 时不执行 adapter。
[x] adapter exception 时 fallback。
[x] fallback 不阻断 Runtime 主链路。
[x] invocation store 可查询。
```

---

# 九、P9P0-G：实现 ToolResultValidationService

## 目标

防止工具结果越界。

## 任务

```text
[x] 新增 ToolResultValidationService。
[x] 校验 result_type。
[x] 校验 schema_version。
[x] 拒绝 PatientOutput 字段。
[x] 拒绝 final diagnosis 表达。
[x] 拒绝 treatment instruction 表达。
[x] 拒绝 raw external response / secret / script。
```

## 验收标准

```text
[x] 合法 structured_result ACCEPTED。
[x] 越界结果 REJECTED。
[x] validation rejected 不进入 Runtime 采纳。
```

---

# 十、P9P0-H：实现 Debug / Governance API

## 目标

提供最小工具治理 API。

## 任务

```text
[x] 新增 ToolGovernanceDebugController。
[x] POST /api/v1/debug/tool-governance/tools。
[x] GET /api/v1/debug/tool-governance/tools。
[x] GET /api/v1/debug/tool-governance/tools/{tool_registry_id}。
[x] POST /api/v1/debug/tool-governance/mcp-servers。
[x] GET /api/v1/debug/tool-governance/mcp-servers。
[x] POST /api/v1/debug/tool-governance/skills。
[x] GET /api/v1/debug/tool-governance/skills。
[x] POST /api/v1/debug/tool-governance/invocations/run。
[x] GET /api/v1/debug/tool-governance/invocations/{invocation_id}。
[x] 所有响应 Safe DTO。
[x] 接入 AccessPolicy。
```

## 验收标准

```text
[x] SYSTEM_ADMIN / EVALUATION_REVIEWER 可写和 run。
[x] READ_ONLY_OBSERVER 只读。
[x] PATIENT 禁止。
[x] Safe DTO 不泄露敏感内容。
```

---

# 十一、P9P0-I：Trace / Audit 接入

## 目标

让工具治理操作可复盘。

## 任务

```text
[x] AuditActionType 新增 CREATE_TOOL_REGISTRY_ENTRY。
[x] AuditActionType 新增 CREATE_MCP_SERVER_REGISTRY_ENTRY。
[x] AuditActionType 新增 CREATE_SKILL_REGISTRY_ENTRY。
[x] AuditActionType 新增 RUN_TOOL_INVOCATION。
[x] AuditActionType 新增 TOOL_INVOCATION_POLICY_REJECTED。
[x] AuditActionType 新增 TOOL_RESULT_VALIDATION_REJECTED。
[x] AuditResourceType 新增 TOOL_GOVERNANCE / TOOL_INVOCATION。
[x] Service / Runtime 写操作记录 Audit。
```

## 验收标准

```text
[x] 每次 create / run 有 audit record。
[x] policy rejected 有 audit record。
[x] validation rejected 有 audit record。
[x] audit 不包含 raw text / secret。
```

---

# 十二、P9P0-J：Evaluation Scorer 接入

## 目标

让 tool governance 进入 Evaluation。

## 任务

```text
[x] 新增 ToolRegistryCompletenessScorer。
[x] 新增 ToolInvocationTraceScorer。
[x] 新增 ToolResultBoundaryScorer。
[x] 新增 ToolSideEffectPolicyScorer。
[x] 新增 ToolFallbackSafetyScorer。
[x] 支持 tool_governance_eval / tool_invocation_eval tag。
```

## 验收标准

```text
[x] 缺 tool version 得分失败。
[x] invocation 缺 trace 得分失败。
[x] result 边界泄露得分失败。
[x] high-risk side effect 被允许时得分失败。
[x] fallback 缺记录得分失败。
```

---

# 十三、P9P0-K：Candidate Mapping 接入

## 目标

工具治理缺口可以沉淀为待审核 candidate。

## 任务

```text
[x] 扩展 CandidateMappingPolicy。
[x] tool registry failure → governance candidate。
[x] tool boundary failure → patient boundary candidate。
[x] side effect policy failure → safety governance candidate。
[x] fallback trace failure → trace quality candidate。
[x] 确保所有 candidate review-required。
```

## 验收标准

```text
[x] 不自动发布 ToolRegistryEntry / ToolPolicy。
[x] candidate risk level 合理。
[x] ToolGovernanceCandidateMappingTest 覆盖。
```

---

# 十四、P9P0-L：测试、人工验证与冻结记录

## 目标

完成 Phase 9-P0 收口。

## 任务

```text
[x] 完成 ToolRegistryPolicyTest。
[x] 完成 McpServerRegistryPolicyTest。
[x] 完成 SkillRegistryPolicyTest。
[x] 完成 ToolInvocationPolicyTest。
[x] 完成 ToolResultValidationServiceTest。
[x] 完成 ToolInvocationRuntimeTest。
[x] 完成 ToolGovernanceDebugControllerTest。
[x] 完成 ToolGovernanceScorerTest。
[x] 完成 ToolGovernanceCandidateMappingTest。
[x] 运行 mvn test。
[x] 编写 Phase9_P0人工测试结果.md。
[x] 编写 Phase9_P0冻结记录.md。
[x] 更新 AI_IMPLEMENTATION_SKILL.md 为 Phase 9-P0 已冻结。
```

## 验收标准

```text
[x] Java 测试通过。
[x] Phase 1–8 P2 回归不破坏。
[x] 人工测试覆盖工具登记、高风险工具拒绝、mock tool 调用、patient_direct_answer 拒绝、fallback。
[x] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十五、推荐实现顺序

建议严格按以下顺序实现：

```text
1. P9P0-A：tool governance domain。
2. P9P0-B：in-memory store / registry。
3. P9P0-C：registry policy。
4. P9P0-D：invocation policy。
5. P9P0-E：mock/local adapters。
6. P9P0-F：ToolInvocationRuntime。
7. P9P0-G：ToolResultValidationService。
8. P9P0-H：Debug / Governance API。
9. P9P0-I：Trace / Audit。
10. P9P0-J：Evaluation Scorer。
11. P9P0-K：Candidate Mapping。
12. P9P0-L：测试、人工验证、冻结记录。
```

---

# 十六、开发期间禁止事项

```text
1. 不接真实第三方医疗系统。
2. 不接真实远程 MCP server。
3. 不做真实高风险写操作。
4. 不让 Agent 自主发现和调用任意工具。
5. 不让 Tool / MCP / Skill 直接修改 RuntimeState。
6. 不让 Tool / MCP / Skill 直接写入 PatientOutput。
7. 不让工具决定诊断、治疗、转诊或用药。
8. 不保存 raw patient dialogue / secret / executable script。
9. 不绕过 ToolInvocationPolicy。
10. 不绕过 ToolResultValidationService。
11. 不改写 Phase 1–8 P2 冻结记录。
```

---

# 十七、Phase 9-P0 完成后的后置任务

```text
1. Phase 9-P1：真实 MCP adapter 设计。
2. Phase 9-P1：Spring AI MCP adapter 可选接入。
3. Phase 9-P2：Skills 文件系统与 Skill Store。
4. Phase 10：Tool Console / Skill Console。
5. 后置生产能力：Secret 管理、高风险工具审批流、灰度工具发布。
```

---

# 十八、最终 Definition of Done

Phase 9-P0 完成的最终标准：

```text
[x] ToolRegistryEntry 可创建 / 查询。
[x] McpServerRegistryEntry 可创建 / 查询。
[x] SkillRegistryEntry 可创建 / 查询。
[x] ToolInvocation 可运行 mock/local adapter。
[x] ToolInvocationPolicy 可拒绝 forbidden / patient_direct_answer / high-risk write。
[x] ToolResultValidationService 可拒绝越界结果。
[x] ToolGovernanceSnapshot 可追踪。
[x] Debug API 可用。
[x] Audit / Trace 可见。
[x] Evaluation Scorer 可识别工具治理缺口。
[x] Candidate Mapping 可沉淀待审核治理候选。
[x] Java 测试通过。
[x] Phase9_P0冻结记录完成。
```
