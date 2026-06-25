# AI Implementation Skill：ClinMindRuntime Phase 1

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前阶段只允许实现 Phase 1 Runtime MVP，不允许提前扩展到后续阶段。  
> Phase 1 Runtime Core 采用 Java / Spring Boot，Python 仅作为后续可选 AI Provider。

---

# 一、当前项目阶段

```text
Phase 1：Runtime MVP
```

当前目标：

```text
跑通一个最小受控诊断 Runtime。
```

Phase 1 要证明的工程闭环：

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
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase1_开发任务清单.md
3. docs/Phase1_技术栈与工程架构决策.md
4. docs/Phase1_Runtime_MVP_实现规格.md
5. docs/Phase1_数据结构与状态设计.md
6. docs/Phase1_模块接口设计.md
7. docs/Phase1_API与测试设计.md
8. docs/ClinMindRuntime阶段拆分路线图.md
9. docs/ClinMindRuntime完整系统设计.md
```

解释：

```text
Phase 1 技术栈决策优先于此前的 Python/FastAPI 草案。
Phase 1 低层设计文档优先于总设计文档。
总设计文档描述完整愿景，但不能作为提前实现 Phase 2–5 能力的理由。
任务清单用于跟踪实现进度，但不能覆盖设计文档的架构约束。
```

---

# 三、当前技术栈决策

```text
Runtime Core：Java 17+ / Spring Boot 3.x
API：Spring Web
Validation：Jakarta Validation
Trace：Spring AOP + 自定义 @TraceStep
Data Model：Java enum / record / class
Config Assets：YAML / JSON
Testing：JUnit 5 + AssertJ / Mockito
Storage Phase 1：In-memory RuntimeStore
Python：后续可选 AI Provider，不作为 Runtime 主工程
```

AI 框架边界：

```text
Spring AI / LangChain4j / LangChain / LangGraph 只能作为后续 Provider / Adapter。
它们不能成为 Runtime 主控。
RuntimeState、SafetyGate、EvidenceGraph、DecisionBoundary 必须由 ClinMindRuntime 自己控制。
```

---

# 四、当前允许实现的内容

## 4.1 Runtime 基础设施

```text
Spring Boot 工程骨架
RuntimeController
RuntimeStatus
RuntimeState
RuntimeTrace
RuntimeStore
@TraceStep
RuntimeTraceAspect
Short-term Context 降级实现
```

说明：

```text
Phase 1 暂不单独实现 Redis 级别 ShortTermContextStore。
短期上下文由 RuntimeState.inputHistory 承担。
```

## 4.2 Runtime 执行链路

```text
EntryAssessmentService
CaseFrameService
StaticRuleProvider
KnowledgeContextService
ExperienceContextService 空实现 / mock 实现
SafetyGateService
DifferentialDiagnosisBoardService
EvidenceGraphService
QuestionTestPolicyService
DecisionBoundaryService
PatientOutputService
ClinicianReportService
FailurePolicyService
```

## 4.3 最小静态资产

```text
src/main/resources/assets/symptom-groups/chest-pain.yml
src/main/resources/assets/symptom-groups/fever.yml
src/main/resources/assets/red-flag-rules.yml
src/main/resources/assets/test-recommendation-rules.yml
src/main/resources/assets/capability-profiles.yml
```

---

# 五、当前禁止实现的内容

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
17. 不引入 Spring Cloud、Nacos、消息队列或复杂微服务。
18. 不让 LangChain / LangGraph / Spring AI / LangChain4j 取代 Runtime 主控。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 1 Runtime MVP。本次只保留接口或 mock，不实现真实能力。
```

---

# 六、实现顺序

AI 必须按以下顺序推进，不要跳到后续模块。

```text
MVP-P0-A：Runtime 状态骨架
  1. Spring Boot 工程基础结构
  2. RuntimeStatus / WorkMode / RuntimeMode 等枚举
  3. RuntimeState
  4. RuntimeTrace
  5. RuntimeStore 内存版
  6. @TraceStep 与 RuntimeTraceAspect 基础能力

MVP-P0-B：病例结构化与入口判断
  7. RuntimeController start / continue / status / result / trace
  8. EntryAssessmentService
  9. CaseFrameService

MVP-P0-C：安全门和候选诊断
  10. StaticRuleProvider
  11. KnowledgeContextService
  12. ExperienceContextService 空实现 / mock 实现
  13. SafetyGateService
  14. DifferentialDiagnosisBoardService

MVP-P0-D：证据图与下一步动作
  15. EvidenceGraphService
  16. QuestionTestPolicyService

MVP-P0-E：输出边界与分角色表达
  17. DecisionBoundaryService
  18. PatientOutputService
  19. ClinicianReportService
  20. FailurePolicyService

MVP-P0-F：最小测试集
  21. 测试病例 YAML
  22. Runtime 集成测试
```

每次实现任务只能覆盖一个小阶段，不能一次性生成整个系统。

---

# 七、任务清单同步规则

AI 每次实现、修改或测试 Phase 1 代码后，必须同步更新：

```text
docs/Phase1_开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase1_开发任务清单.md。
2. 确认当前任务属于 MVP-P0-A 到 MVP-P0-F 的哪一项。
3. 将正在处理的任务从 [ ] 改为 [/]。
4. 如果任务不在清单中，先在对应阶段补充任务项，不要直接实现。
```

实现后：

```text
1. 将已完成任务从 [/] 改为 [x]。
2. 如果任务部分完成，保持 [/]，并补充备注或问题记录。
3. 如果任务被阻塞，将状态改为 [!]，并在问题记录中说明原因。
4. 如果实现过程中新增了必要任务，补充到对应阶段。
```

---

# 八、架构约束

## 8.1 RuntimeState 是唯一事实源

```text
所有模块必须围绕 RuntimeState 读写。
不能让自然语言对话历史替代 RuntimeState。
不能让 LLM 输出直接影响下一轮判断，必须先写回结构化状态。
```

## 8.2 模块必须通过结构化对象交互

正确方式：

```text
CaseFrame → KnowledgeContext → SafetyGateResult → DDxBoard → EvidenceGraph → DecisionBoundaryResult
```

错误方式：

```text
把所有内容拼成 prompt，让 LLM 一次性决定风险、诊断、追问和输出。
```

## 8.3 SafetyGate 是硬安全模块

```text
SafetyGate 必须优先于候选诊断输出。
SafetyGate 命中高风险后，DecisionBoundary 必须收紧患者端输出。
SafetyGate 失败时，必须进入 error_safe_halted 或保守输出。
```

## 8.4 DecisionBoundary 必须控制输出

```text
Patient Output 和 Clinician Report 必须经过 DecisionBoundary。
患者端不能直接看到医生端完整候选诊断和证据图。
患者端不能输出确定诊断、处方和治疗方案。
```

## 8.5 EvidenceGraph 是控制层，不只是解释层

```text
EvidenceGraph 必须影响 Question / Test Policy。
不能只把 EvidenceGraph 当作最终解释文本。
```

## 8.6 RuntimeTrace 必须记录关键判断

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

## 8.7 AOP Trace 不能替代业务状态更新

```text
Spring AOP 只负责横切追踪、耗时、异常和审计辅助。
业务状态仍必须由模块显式写回 RuntimeState。
```

---

# 九、推荐工程目录

```text
clinmind-runtime/
├── pom.xml
├── src/main/java/com/clinmind/runtime/
│   ├── ClinMindRuntimeApplication.java
│   ├── api/
│   ├── state/
│   ├── storage/
│   ├── trace/
│   ├── entry/
│   ├── caseframe/
│   ├── knowledge/
│   ├── experience/
│   ├── safety/
│   ├── reasoning/
│   ├── boundary/
│   └── output/
├── src/main/resources/
│   ├── application.yml
│   └── assets/
└── src/test/java/com/clinmind/runtime/
```

---

# 十、API 约束

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

路径变量统一使用：

```text
runtime_id
```

不要使用 `runtimeId`。

---

# 十一、测试约束

每实现一个模块，必须同时补充 JUnit 测试。

至少包含：

```text
RuntimeStateTest
RuntimeStoreTest
EntryAssessmentServiceTest
CaseFrameServiceTest
SafetyGateServiceTest
DifferentialDiagnosisBoardServiceTest
EvidenceGraphServiceTest
DecisionBoundaryServiceTest
RuntimeFlowIntegrationTest
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

# 十二、AI 每次执行任务前的检查清单

```text
1. 当前任务属于 Phase 1 吗？
2. 当前任务属于 MVP-P0-A 到 MVP-P0-F 的哪一步？
3. 是否读取了技术栈决策文档？
4. 是否读取了 Phase 1 对应设计文档？
5. 是否读取并更新了 docs/Phase1_开发任务清单.md？
6. 是否会误实现 Phase 2–5 的内容？
7. 是否需要新增或更新测试？
```

---

# 十三、AI 每次提交代码后的检查清单

```text
1. 是否遵守 Java Spring Boot Runtime Core 技术路线？
2. 是否遵守 Phase 1 范围？
3. 是否新增了不该出现的 RAG / KG / 经验记忆 / 平台后台？
4. 是否所有患者端输出都经过 DecisionBoundary？
5. SafetyGate 失败是否会保守处理？
6. RuntimeTrace 是否记录关键判断？
7. 是否补充了 JUnit 测试？
8. 是否同步更新了 docs/Phase1_开发任务清单.md？
```

---

# 十四、当前最优下一步

当前最优实现任务是：

```text
MVP-P0-A：Runtime 状态骨架
```

具体包括：

```text
1. 创建 Spring Boot 工程基础结构
2. 定义 RuntimeStatus / WorkMode / RuntimeMode 等枚举
3. 定义 RuntimeState Java 模型
4. 定义 RuntimeTrace Java 模型
5. 实现 RuntimeStore 内存版
6. 实现 @TraceStep 和 RuntimeTraceAspect 基础能力
7. 编写 JUnit 基础测试
8. 同步更新 docs/Phase1_开发任务清单.md
```

不要在这个任务中实现 SafetyGate、RAG、KG、经验记忆或平台后台。

---

# 十五、最终约束

```text
当前不是在实现完整医疗 AI 平台。
当前是在实现 Phase 1 Runtime MVP。
当前 Runtime Core 采用 Java / Spring Boot。
Python 和 AI 框架只作为后续 Provider。
Phase 1 的目标是验证受控诊断 Runtime 架构，而不是追求医学知识覆盖全面。
实现完成后必须同步更新 Phase1_开发任务清单。
```
