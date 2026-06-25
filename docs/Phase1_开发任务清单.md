# Phase 1 开发任务清单

> 本文档用于跟踪 ClinMindRuntime Phase 1 Runtime MVP 的实现进度。  
> AI / Cursor / Claude Code / Codex 每完成一个实现任务后，必须同步更新本文档。  
> 本文档不是新的设计文档，而是实现进度控制表。

---

# 一、使用规则

```text
1. 每次只实现一个小任务或一个小模块，不允许一次性实现整个 Phase 1。
2. 每次实现前，先查看当前任务所属阶段。
3. 每次实现后，必须更新任务状态。
4. 如果实现过程中发现设计缺口，可以在“问题记录”中补充，但不要擅自扩大实现范围。
5. 标记完成前，必须有对应代码和测试，或在备注中说明为什么暂时没有测试。
```

任务状态约定：

```text
[ ] 未开始
[/] 进行中
[x] 已完成
[!] 阻塞 / 需要人工确认
```

---

# 二、MVP-P0-A：Runtime 状态骨架

目标：先让 Runtime 的状态对象、状态枚举、Trace 和最小存储能力站起来。

## 2.1 工程基础

- [ ] 创建 Python / FastAPI 项目基础结构
- [ ] 创建 `app/` 主目录
- [ ] 创建 `app/main.py`
- [ ] 创建 `app/state/` 目录
- [ ] 创建 `app/storage/` 目录
- [ ] 创建 `tests/` 目录
- [ ] 创建基础依赖文件，例如 `requirements.txt` 或 `pyproject.toml`

## 2.2 RuntimeStatus

- [ ] 创建 `app/state/runtime_status.py`
- [ ] 定义 `RuntimeStatus` 枚举
- [ ] 定义 `WorkMode` 枚举
- [ ] 定义 `RuntimeMode` 枚举
- [ ] 定义 `RiskLevel` 枚举
- [ ] 定义 `CandidateStatus` 枚举，包含 `possible_after_exclusion`
- [ ] 定义 `NextActionType` 枚举
- [ ] 定义 `OutputLevel` 枚举
- [ ] 编写枚举单元测试

## 2.3 RuntimeState

- [ ] 创建 `app/state/runtime_state.py`
- [ ] 使用 Pydantic 定义 `UserInput`
- [ ] 使用 Pydantic 定义 `EntryAssessmentResult`
- [ ] 使用 Pydantic 定义 `CaseFrame` 最小结构
- [ ] 使用 Pydantic 定义 `KnowledgeContext` 最小结构
- [ ] 使用 Pydantic 定义 `ExperienceContext` 空实现结构
- [ ] 使用 Pydantic 定义 `SafetyGateResult`
- [ ] 使用 Pydantic 定义 `DifferentialDiagnosisBoard`
- [ ] 使用 Pydantic 定义 `EvidenceGraph`
- [ ] 使用 Pydantic 定义 `QuestionTestPolicyResult`
- [ ] 使用 Pydantic 定义 `DecisionBoundaryResult`
- [ ] 使用 Pydantic 定义 `PatientOutput`
- [ ] 使用 Pydantic 定义 `ClinicianReport`
- [ ] 使用 Pydantic 定义 `RuntimeState`
- [ ] 编写 RuntimeState 序列化 / 反序列化测试

## 2.4 RuntimeTrace

- [ ] 创建 `app/state/runtime_trace.py`
- [ ] 定义 `RuntimeTrace` schema
- [ ] 支持记录输入、模块执行、知识使用、安全门结果、候选诊断变化、证据图变化、输出边界结果
- [ ] 编写 RuntimeTrace 单元测试

## 2.5 RuntimeStore

- [ ] 创建 `app/storage/runtime_store.py`
- [ ] 实现内存版 RuntimeStore
- [ ] 实现 `create(state)`
- [ ] 实现 `get(runtime_id)`
- [ ] 实现 `update(state)`
- [ ] 实现 `exists(runtime_id)`
- [ ] 定义 runtime 不存在时的异常或错误返回
- [ ] 编写 RuntimeStore 单元测试

## 2.6 MVP-P0-A 验收

- [ ] 所有 schema 能正常 import
- [ ] RuntimeState 能创建默认对象
- [ ] RuntimeState 能 JSON 序列化和反序列化
- [ ] RuntimeTrace 能创建并保存关键字段
- [ ] RuntimeStore 能完成 create / get / update / exists
- [ ] 运行 `pytest` 通过

---

# 三、MVP-P0-B：病例结构化与入口判断

目标：让系统可以通过 API 创建 Runtime，并完成入口判断和 CaseFrame 初始化。

## 3.1 Runtime API 基础

- [ ] 创建 `app/api/runtime_api.py`
- [ ] 实现 `POST /api/v1/runtime/start`
- [ ] 实现 `POST /api/v1/runtime/continue`
- [ ] 实现 `GET /api/v1/runtime/{runtime_id}/status`
- [ ] 实现 `GET /api/v1/runtime/{runtime_id}/result`
- [ ] 实现 `GET /api/v1/runtime/{runtime_id}/trace`
- [ ] 统一使用 `runtime_id`，不要使用 `runtimeId`
- [ ] 实现统一响应格式
- [ ] 实现基础错误码

## 3.2 EntryAssessment

- [ ] 创建 `app/entry/entry_assessment.py`
- [ ] 实现 `assess_entry(user_input, basic_info)`
- [ ] 支持 `wellness_mode`
- [ ] 支持 `clinical_mode`
- [ ] 支持 `emergency_hint`
- [ ] 支持 `unsupported`
- [ ] emergency_hint 不直接设置 `safety_gate_triggered`，只标记 work_mode
- [ ] 编写 EntryAssessment 单元测试

## 3.3 CaseFrame

- [ ] 创建 `app/case/case_frame.py`
- [ ] 实现 `build_or_update_case_frame(...)`
- [ ] 支持主诉提取
- [ ] 支持基础症状提取
- [ ] 支持 basic_info 写入 patient_profile
- [ ] 支持 missing_slots 生成
- [ ] 编写 CaseFrame 单元测试

## 3.4 MVP-P0-B 验收

- [ ] start API 能创建 Runtime
- [ ] continue API 能读取并更新 Runtime
- [ ] EntryAssessment 结果能写入 RuntimeState
- [ ] CaseFrame 能写入 RuntimeState
- [ ] runtime_id 不存在时返回统一错误
- [ ] API 基础测试通过

---

# 四、MVP-P0-C：安全门和候选诊断

目标：让系统能读取静态规则，识别风险，并构建候选诊断状态板。

## 4.1 StaticRuleProvider

- [ ] 创建 `app/knowledge/static_rule_provider.py`
- [ ] 创建 `assets/symptom_groups/chest_pain.yml`
- [ ] 创建 `assets/symptom_groups/fever.yml`
- [ ] 创建 `assets/red_flag_rules.yml`
- [ ] 创建 `assets/test_recommendation_rules.yml`
- [ ] 创建 `assets/capability_profiles.yml`
- [ ] 实现症状群规则读取
- [ ] 实现危险信号规则读取
- [ ] 实现检查建议规则读取
- [ ] 实现静态 Capability Profile 读取
- [ ] 编写规则读取测试

## 4.2 KnowledgeContext

- [ ] 创建 `app/knowledge/knowledge_context.py`
- [ ] 实现 `build_knowledge_context(...)`
- [ ] 将静态规则聚合为 KnowledgeContext
- [ ] 记录 source_assets
- [ ] 编写 KnowledgeContext 单元测试

## 4.3 ExperienceContext

- [ ] 创建 `app/experience/experience_context.py`
- [ ] 实现空 ExperienceContext
- [ ] 可选实现 mock ExperienceContext
- [ ] 明确不接入真实 Clinical Experience Memory
- [ ] 编写 ExperienceContext 单元测试

## 4.4 SafetyGate

- [ ] 创建 `app/safety/safety_gate.py`
- [ ] 实现 `evaluate_safety(...)`
- [ ] 支持危险信号规则匹配
- [ ] 命中高风险时设置输出限制
- [ ] SafetyGate 失败时进入保守策略
- [ ] 编写 SafetyGate 单元测试

## 4.5 Differential Diagnosis Board

- [ ] 创建 `app/reasoning/differential_board.py`
- [ ] 实现 `build_differential_board(...)`
- [ ] 支持 common_diagnoses
- [ ] 支持 must_not_miss
- [ ] 支持 `need_to_rule_out`
- [ ] 支持 `possible_after_exclusion`
- [ ] 高风险候选不能被删除
- [ ] 编写 DDx Board 单元测试

## 4.6 MVP-P0-C 验收

- [ ] 静态规则能被读取
- [ ] KnowledgeContext 能生成
- [ ] ExperienceContext 能以空实现参与链路
- [ ] SafetyGate 能识别配置好的高风险规则
- [ ] DDx Board 能生成候选诊断状态
- [ ] 高风险候选保留为 must_not_miss 或 need_to_rule_out

---

# 五、MVP-P0-D：证据图与下一步动作

目标：让系统能根据候选诊断和缺失证据决定下一步追问或检查建议。

## 5.1 EvidenceGraph

- [ ] 创建 `app/reasoning/evidence_graph.py`
- [ ] 实现 `build_evidence_graph(...)`
- [ ] 支持 supporting_evidence
- [ ] 支持 opposing_evidence
- [ ] 支持 missing_evidence
- [ ] 支持 next_questions
- [ ] 支持 recommended_tests
- [ ] 编写 EvidenceGraph 单元测试

## 5.2 Question / Test Policy

- [ ] 创建 `app/reasoning/question_test_policy.py`
- [ ] 实现 `decide_next_action(...)`
- [ ] 高风险优先
- [ ] 缺失证据优先
- [ ] 支持 ask_question
- [ ] 支持 recommend_test
- [ ] 支持 recommend_visit
- [ ] 编写 Question / Test Policy 单元测试

## 5.3 MVP-P0-D 验收

- [ ] EvidenceGraph 不只是解释文本，而能影响下一步动作
- [ ] 缺失证据能触发追问
- [ ] 高风险候选能触发检查或就医评估建议
- [ ] Question / Test Policy 输出结构化 NextAction

---

# 六、MVP-P0-E：输出边界与分角色表达

目标：让系统能够根据风险和能力边界区分患者端与医生端输出。

## 6.1 DecisionBoundary

- [ ] 创建 `app/boundary/decision_boundary.py`
- [ ] 创建 `app/boundary/capability_profile_provider.py`
- [ ] 实现 `decide_output_boundary(...)`
- [ ] 读取静态 Capability Profile
- [ ] 高风险未排除时禁止低风险安抚
- [ ] 患者端默认不展示完整候选诊断
- [ ] 医生端允许展示 DDx 和 EvidenceGraph
- [ ] 编写 DecisionBoundary 单元测试

## 6.2 PatientOutput

- [ ] 创建 `app/output/patient_output.py`
- [ ] 实现 `build_patient_output(...)`
- [ ] 输出继续追问
- [ ] 输出风险提示
- [ ] 输出线下评估建议
- [ ] 禁止确定诊断
- [ ] 禁止处方建议
- [ ] 编写 PatientOutput 单元测试

## 6.3 ClinicianReport

- [ ] 创建 `app/output/clinician_report.py`
- [ ] 实现 `build_clinician_report(...)`
- [ ] 展示 CaseFrame Summary
- [ ] 展示 SafetyGate Result
- [ ] 展示 DDx Board
- [ ] 展示 EvidenceGraph
- [ ] 展示 Recommended Questions / Tests
- [ ] 编写 ClinicianReport 单元测试

## 6.4 FailurePolicy

- [ ] 创建 `app/boundary/failure_policy.py`
- [ ] 实现安全模块失败时的保守输出
- [ ] SafetyGate 失败时进入 `error_safe_halted`
- [ ] DecisionBoundary 失败时进入 `error_safe_halted`
- [ ] 编写 FailurePolicy 单元测试

## 6.5 MVP-P0-E 验收

- [ ] PatientOutput 和 ClinicianReport 明确分离
- [ ] 所有患者端输出经过 DecisionBoundary
- [ ] 高风险场景不会输出低风险安抚
- [ ] 医生端可以看到候选和证据图
- [ ] 患者端看不到医生端完整内容

---

# 七、MVP-P0-F：最小测试集与集成验证

目标：用测试病例验证完整 Runtime MVP 闭环。

## 7.1 测试病例

- [ ] 创建 `tests/cases/chest_pain_cases.yml`
- [ ] 创建 `tests/cases/fever_cases.yml`
- [ ] 至少 5 个胸痛 / 胸闷病例
- [ ] 至少 5 个发热病例
- [ ] 至少包含普通病例
- [ ] 至少包含高风险病例
- [ ] 至少包含信息缺失病例
- [ ] 至少包含误导表达病例

## 7.2 集成测试

- [ ] 创建 `tests/test_runtime_flow.py`
- [ ] 测试 start API 完整链路
- [ ] 测试 continue API 完整链路
- [ ] 测试高风险链路
- [ ] 测试患者端 / 医生端输出分离
- [ ] 测试 RuntimeTrace 记录
- [ ] 测试静态规则替换不影响核心链路

## 7.3 Phase 1 总体验收

- [ ] 10–20 个测试病例可以跑通
- [ ] 高风险病例不会输出低风险安抚
- [ ] 信息缺失病例会继续追问
- [ ] 医生端可以看到结构化候选和证据图
- [ ] 患者端和医生端输出不同
- [ ] RuntimeTrace 可以解释每一轮判断
- [ ] 所有测试通过

---

# 八、问题记录

用于记录实现过程中发现的设计缺口或需要人工确认的问题。

| 编号 | 问题 | 影响模块 | 状态 | 处理结论 |
|---|---|---|---|---|
| Q1 | 暂无 | - | - | - |

---

# 九、变更记录

| 日期 | 变更 | 说明 |
|---|---|---|
| 2026-06-25 | 创建任务清单 | 用于约束 Phase 1 实现进度 |
