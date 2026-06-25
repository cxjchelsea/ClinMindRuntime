# AI Implementation Skill：ClinMindRuntime Phase 1

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前阶段只允许实现 Phase 1 Runtime MVP，不允许提前扩展到后续阶段。  
> AI 在生成代码、修改代码、补测试或重构时，必须遵守本文档。

---

# 一、当前项目阶段

当前项目阶段：

```text
Phase 1：Runtime MVP
```

当前目标：

```text
跑通一个最小受控诊断 Runtime。
```

Phase 1 要证明的不是完整医学能力，而是以下工程闭环成立：

```text
用户输入
→ Runtime API
→ EntryAssessment
→ CaseFrame
→ Knowledge Context（静态规则）
→ Experience Context（空实现 / mock）
→ SafetyGate
→ Differential Diagnosis Board
→ EvidenceGraph
→ Question / Test Policy
→ DecisionBoundary
→ Patient Output / Clinician Report
→ RuntimeTrace
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/Phase1_Runtime_MVP_实现规格.md
2. docs/Phase1_数据结构与状态设计.md
3. docs/Phase1_模块接口设计.md
4. docs/Phase1_API与测试设计.md
5. docs/ClinMindRuntime阶段拆分路线图.md
6. docs/ClinMindRuntime完整系统设计.md
```

解释：

```text
Phase 1 低层设计文档优先于总设计文档。
总设计文档描述完整愿景，但不能作为提前实现 Phase 2–5 能力的理由。
如果总设计和 Phase 1 文档看起来不一致，以 Phase 1 文档为准。
```

---

# 三、当前允许实现的内容

AI 当前只允许实现以下内容。

## 3.1 Runtime 基础设施

```text
Runtime API
RuntimeStatus
RuntimeState
RuntimeTrace
RuntimeStore
Short-term Context 降级实现
```

说明：

```text
Phase 1 暂不单独实现 Redis 级别 ShortTermContextStore。
短期上下文由 RuntimeState.input_history 承担。
```

## 3.2 Runtime 执行链路

```text
EntryAssessment
CaseFrame
StaticRuleProvider
Knowledge Context
Experience Context 空实现 / mock 实现
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Patient Output
Clinician Report
FailurePolicy
```

## 3.3 最小静态资产

```text
assets/symptom_groups/chest_pain.yml
assets/symptom_groups/fever.yml
assets/red_flag_rules.yml
assets/test_recommendation_rules.yml
assets/capability_profiles.yml
```

## 3.4 最小测试集

```text
tests/cases/chest_pain_cases.yml
tests/cases/fever_cases.yml
tests/test_runtime_flow.py
```

---

# 四、当前禁止实现的内容

AI 不允许在 Phase 1 中实现以下内容。

```text
1. 不做完整 RAG Evidence Library。
2. 不做完整 KG-lite。
3. 不做真实 Clinical Experience Memory。
4. 不做真实审核流程。
5. 不做随访结局接入。
6. 不做 Shadow Learning。
7. 不做 Training Center 后台。
8. 不做 Evaluation Center 后台。
9. 不做 Experience Memory Center 后台。
10. 不做完整权限系统。
11. 不做复杂企业级治理后台。
12. 不做自动经验学习。
13. 不输出患者端确定诊断。
14. 不输出处方建议。
15. 不输出治疗方案自动生成。
16. 不承诺临床有效性。
```

如果用户或任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 1 Runtime MVP。本次只保留接口或 mock，不实现真实能力。
```

---

# 五、实现顺序

AI 必须按以下顺序推进，不要跳到后续模块。

```text
MVP-P0-A：Runtime 状态骨架
  1. RuntimeStatus
  2. RuntimeState
  3. RuntimeTrace
  4. RuntimeStore

MVP-P0-B：病例结构化与入口判断
  5. Runtime API start / continue / status / trace
  6. EntryAssessment
  7. CaseFrame

MVP-P0-C：安全门和候选诊断
  8. StaticRuleProvider
  9. Knowledge Context
  10. Experience Context 空实现 / mock 实现
  11. SafetyGate
  12. Differential Diagnosis Board

MVP-P0-D：证据图与下一步动作
  13. EvidenceGraph
  14. Question / Test Policy

MVP-P0-E：输出边界与分角色表达
  15. DecisionBoundary
  16. Patient Output
  17. Clinician Report
  18. FailurePolicy

MVP-P0-F：最小测试集
  19. 测试病例
  20. Runtime 集成测试
```

每次实现任务只能覆盖一个小阶段，不能一次性生成整个系统。

---

# 六、架构约束

## 6.1 RuntimeState 是唯一事实源

```text
所有模块必须围绕 RuntimeState 读写。
不能让自然语言对话历史替代 RuntimeState。
不能让 LLM 输出直接影响下一轮判断，必须先写回结构化状态。
```

## 6.2 模块必须通过结构化对象交互

禁止用长 Prompt 隐式传递模块状态。

正确方式：

```text
CaseFrame → KnowledgeContext → SafetyGateResult → DDxBoard → EvidenceGraph → DecisionBoundaryResult
```

错误方式：

```text
把所有内容拼成 prompt，让 LLM 一次性决定风险、诊断、追问和输出。
```

## 6.3 SafetyGate 是硬安全模块

```text
SafetyGate 必须优先于候选诊断输出。
SafetyGate 命中高风险后，DecisionBoundary 必须收紧患者端输出。
SafetyGate 失败时，必须进入 error_safe_halted 或保守输出。
```

## 6.4 DecisionBoundary 必须控制输出

```text
Patient Output 和 Clinician Report 必须经过 DecisionBoundary。
患者端不能直接看到医生端完整候选诊断和证据图。
患者端不能输出确定诊断、处方和治疗方案。
```

## 6.5 EvidenceGraph 是控制层，不只是解释层

```text
EvidenceGraph 必须影响 Question / Test Policy。
不能只把 EvidenceGraph 当作最终解释文本。
```

## 6.6 RuntimeTrace 必须记录关键判断

每轮 Runtime 至少记录：

```text
输入
执行模块
使用的知识规则
使用的经验单元，如果有
SafetyGate 结果
DDx 变化
EvidenceGraph 变化
DecisionBoundary 结果
输出摘要
```

---

# 七、代码风格约束

## 7.1 推荐技术栈

Phase 1 推荐：

```text
Python
FastAPI
Pydantic
pytest
YAML / JSON 静态配置
内存存储或 SQLite
```

## 7.2 推荐目录

```text
app/
├── api/
├── state/
├── entry/
├── case/
├── knowledge/
├── experience/
├── safety/
├── reasoning/
├── boundary/
├── output/
└── storage/

assets/
├── symptom_groups/
├── red_flag_rules.yml
├── test_recommendation_rules.yml
└── capability_profiles.yml

tests/
├── cases/
└── test_runtime_flow.py
```

## 7.3 代码要求

```text
1. 使用清晰命名，不使用过度抽象。
2. Pydantic schema 与文档字段保持一致。
3. 每个模块尽量保持纯函数或低副作用。
4. RuntimeStore 统一负责状态保存。
5. 错误处理必须显式，不吞异常。
6. 安全相关失败必须保守处理。
7. 不提前引入复杂微服务、Nacos、消息队列或大型后台。
```

---

# 八、API 约束

当前只实现以下 API：

```text
POST /api/v1/runtime/start
POST /api/v1/runtime/continue
GET  /api/v1/runtime/{runtime_id}/status
GET  /api/v1/runtime/{runtime_id}/result
GET  /api/v1/runtime/{runtime_id}/trace
```

统一响应格式：

```json
{
  "success": true,
  "data": {},
  "error": null,
  "trace_id": "trace_001"
}
```

错误响应：

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

路径变量统一使用：

```text
runtime_id
```

不要使用 `runtimeId`。

---

# 九、测试约束

每实现一个模块，必须同时补充测试。

至少包含：

```text
test_runtime_state.py
test_entry_assessment.py
test_case_frame.py
test_safety_gate.py
test_differential_board.py
test_evidence_graph.py
test_decision_boundary.py
test_runtime_flow.py
```

Phase 1 最小验收测试必须覆盖：

```text
1. 可以创建 Runtime。
2. 可以继续 Runtime。
3. RuntimeState 能更新。
4. SafetyGate 能触发高风险规则。
5. 高风险病例不会输出低风险安抚。
6. 患者端和医生端输出不同。
7. RuntimeTrace 能记录关键模块执行。
8. 静态规则文件可以替换，不需要改核心链路。
```

---

# 十、AI 每次执行任务前的检查清单

AI 在写代码前必须先确认：

```text
1. 当前任务属于 Phase 1 吗？
2. 当前任务属于 MVP-P0-A 到 MVP-P0-F 的哪一步？
3. 是否需要读取 Phase 1 对应设计文档？
4. 是否会误实现 Phase 2–5 的内容？
5. 是否需要新增或更新测试？
```

如果当前任务不属于 Phase 1，AI 必须先说明边界，不应直接实现。

---

# 十一、AI 每次提交代码后的检查清单

AI 写完代码后必须检查：

```text
1. 是否遵守 Phase 1 范围？
2. 是否新增了不该出现的 RAG / KG / 经验记忆 / 平台后台？
3. 是否所有患者端输出都经过 DecisionBoundary？
4. SafetyGate 失败是否会保守处理？
5. RuntimeTrace 是否记录关键判断？
6. 是否补充了测试？
7. 是否破坏了既有 API 或数据结构？
```

---

# 十二、常见错误与禁止行为

## 12.1 禁止把项目写成普通问答系统

错误：

```text
用户输入 → LLM → 回答
```

正确：

```text
用户输入 → RuntimeState → SafetyGate → EvidenceGraph → DecisionBoundary → 输出
```

## 12.2 禁止提前实现真实经验记忆

Phase 1 只能：

```text
Experience Context 空实现 / mock 实现
```

不能实现：

```text
真实 Clinical Experience Memory
真实医生审核
随访结局
Shadow Learning
```

## 12.3 禁止让高风险输出被自然语言淡化

如果 SafetyGate 命中高风险，患者端不能输出：

```text
问题不大
可以先观察
不用担心
可能只是小问题
```

必须输出：

```text
继续补充关键信息
风险提示
线下评估或就医建议
```

## 12.4 禁止医生端内容泄露到患者端

医生端可以包含：

```text
DDx Board
EvidenceGraph
候选诊断状态
推荐检查理由
```

患者端必须受 DecisionBoundary 控制。

---

# 十三、当前最优下一步

当前最优实现任务是：

```text
MVP-P0-A：Runtime 状态骨架
```

具体包括：

```text
1. 创建 Python / FastAPI 工程基础结构
2. 定义 RuntimeStatus 枚举
3. 定义 RuntimeState Pydantic schema
4. 定义 RuntimeTrace Pydantic schema
5. 实现 RuntimeStore 内存版
6. 编写基础单元测试
```

不要在这个任务中实现 SafetyGate、RAG、KG、经验记忆或平台后台。

---

# 十四、最终约束

AI 必须始终记住：

```text
当前不是在实现完整医疗 AI 平台。
当前是在实现 Phase 1 Runtime MVP。
Phase 1 的目标是验证受控诊断 Runtime 架构，而不是追求医学知识覆盖全面。
```
