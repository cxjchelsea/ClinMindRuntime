# Phase 1 Runtime MVP 实现规格

> 本文档是 Phase 1 的总览入口。  
> 它不承载全部低层设计，而是定义 Phase 1 的目标、范围、核心闭环、模块清单、开发顺序和验收标准。  
> 详细的数据结构、模块接口、API 契约和测试用例拆分到独立文档中。

---

# 一、文档定位

ClinMindRuntime 的文档体系分为四层：

```text
ClinMindRuntime完整系统设计.md
= 完整愿景、核心能力域、三层架构和长期系统形态

ClinMindRuntime阶段拆分路线图.md
= Phase 0–5 的阶段拆分、阶段目标和能力演进路径

Phase1_Runtime_MVP_实现规格.md
= Phase 1 的总览入口，说明第一阶段要做什么、不做什么、如何验收

Phase1_数据结构与状态设计.md
Phase1_模块接口设计.md
Phase1_API与测试设计.md
= Phase 1 的低层详细设计，直接指导开发实现
```

本文档的重点不是写尽所有字段和方法，而是确保 Phase 1 的实现边界清楚、开发顺序清楚、验收标准清楚。

---

# 二、命名与范围说明

为了避免和总设计文档中的 MVP 优先级混淆，本文档统一使用以下命名：

```text
Phase 0 / Phase 1 / Phase 2 ...
= 项目迭代阶段

MVP-P0 / MVP-P1 / MVP-P2
= 某一阶段内部的任务优先级
```

因此，`Phase 1` 指项目第一阶段 Runtime MVP，不等同于总设计中可能出现的 `P1 第二阶段` 或任务优先级。

---

# 三、Phase 1 核心目标

Phase 1 的目标不是实现完整医疗 AI 平台，也不是实现完整 RAG、知识图谱、训练后台、经验记忆或医生审核流程。

Phase 1 的核心目标是：

```text
跑通一个最小受控诊断 Runtime。
```

系统需要证明：

```text
用户输入症状后，系统不是直接让 LLM 生成医学回答，
而是创建一次 Runtime，维护病例状态，识别风险，构建候选诊断，组织证据图，决定下一步追问或检查建议，并根据输出边界生成患者端或医生端内容。
```

Phase 1 成功的标志是：

```text
受控诊断 Runtime 的核心链路可以被 API 调用、被状态记录、被 Trace 复盘、被测试病例验证。
```

---

# 四、Phase 1 核心闭环

Phase 1 必须跑通以下链路：

```text
用户输入
  ↓
Runtime API
  ↓
EntryAssessment
  ↓
RuntimeState / RuntimeStatus
  ↓
CaseFrame
  ↓
Knowledge Context（静态规则）
  ↓
Experience Context（空实现 / mock）
  ↓
SafetyGate
  ↓
Differential Diagnosis Board
  ↓
EvidenceGraph
  ↓
Question / Test Policy
  ↓
DecisionBoundary
  ↓
Patient Output / Clinician Report
  ↓
RuntimeTrace
```

Phase 1 不是为了证明诊断覆盖面，而是证明这条 Runtime 链路成立。

---

# 五、Phase 1 做什么

## 5.1 Runtime 基础设施

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
Phase 1 暂不单独实现 Redis 级别 ShortTermContextStore，
而是由 RuntimeState.input_history 承担最小短期上下文能力。
后续如接入 Redis，再将其外拆为独立上下文存储。
```

## 5.2 Runtime 执行链路

```text
EntryAssessment
CaseFrame
Knowledge Context
Experience Context
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Patient Output
Clinician Report
FailurePolicy
```

## 5.3 最小静态资产

```text
胸痛 / 胸闷症状群静态规则
发热症状群静态规则
危险信号规则
必问问题配置
候选诊断配置
检查建议配置
静态 Capability Profile
```

## 5.4 最小测试集

```text
10–20 个测试病例
至少覆盖胸痛 / 胸闷、发热两个症状群
至少包含普通病例、高危病例、信息缺失病例、误导表达病例
```

---

# 六、Phase 1 不做什么

```text
不做完整 RAG Evidence Library
不做完整 KG-lite
不做真实 Clinical Experience Memory
不做真实医生审核
不做随访结局接入
不做 Shadow Learning
不做 Training Center 后台
不做 Evaluation Center 后台
不做 Experience Memory Center 后台
不做完整权限系统
不做患者端确定诊断
不做处方建议
不做治疗方案自动生成
不承诺临床有效性
```

Phase 1 的医疗表达必须保持为原型系统和安全边界验证，不应包装成真实诊疗产品。

---

# 七、Phase 1 合理降级策略

| 模块 | Phase 1 实现方式 | 后续阶段升级 |
|---|---|---|
| Knowledge Context | 静态 JSON / YAML 规则 | Phase 2 接入 Clinical Pathway、KG-lite、RAG Evidence Library |
| Experience Context | 空实现或 mock 经验 | Phase 2/4 接入 Clinical Experience Memory |
| Capability Profile | 静态配置 | Phase 3 由 Evaluation Results 生成 |
| Short-term Context | RuntimeState.input_history | 后续接入 Redis / 独立 ShortTermContextStore |
| RuntimeStore | 内存 / JSON / SQLite | 后续升级为数据库持久化 |
| 前端 | Swagger / Postman / 简单调试页面 | Phase 5 再做完整 Runtime Console |
| LLM | 可选，仅辅助抽取或表达 | 后续再加强 Prompt、工具调用和评估 |

---

# 八、详细设计文档入口

Phase 1 的低层设计拆分为三份文档。

## 8.1 数据结构与状态设计

文档：

```text
docs/Phase1_数据结构与状态设计.md
```

负责定义：

```text
RuntimeStatus 枚举
RuntimeStatus 状态迁移表
RuntimeState 严格字段设计
CaseFrame 严格字段设计
KnowledgeContext 字段设计
ExperienceContext 字段设计
SafetyGateResult 字段设计
DDxCandidate / DDxBoard 字段设计
EvidenceGraph 字段设计
QuestionAction / TestAction 字段设计
DecisionBoundaryResult 字段设计
PatientOutput / ClinicianReport 字段设计
RuntimeTrace 字段设计
```

## 8.2 模块接口设计

文档：

```text
docs/Phase1_模块接口设计.md
```

负责定义：

```text
每个模块职责
每个模块输入输出
每个模块读取 RuntimeState 哪些字段
每个模块写入 RuntimeState 哪些字段
每个模块核心方法签名
模块执行顺序
模块失败策略
RuntimeTrace 记录点
```

## 8.3 API 与测试设计

文档：

```text
docs/Phase1_API与测试设计.md
```

负责定义：

```text
Runtime API 请求响应契约
统一响应格式
错误码
静态规则文件格式
测试病例格式
测试用例表
验收标准
不合格表现
```

---

# 九、推荐工程目录

Phase 1 建议先使用单体后端结构，避免一开始过度微服务化。

```text
clinmind-runtime/
├── app/
│   ├── main.py
│   ├── api/
│   │   └── runtime_api.py
│   ├── state/
│   │   ├── runtime_state.py
│   │   ├── runtime_status.py
│   │   └── runtime_trace.py
│   ├── entry/
│   │   └── entry_assessment.py
│   ├── case/
│   │   └── case_frame.py
│   ├── knowledge/
│   │   ├── knowledge_context.py
│   │   └── static_rule_provider.py
│   ├── experience/
│   │   └── experience_context.py
│   ├── safety/
│   │   └── safety_gate.py
│   ├── reasoning/
│   │   ├── differential_board.py
│   │   ├── evidence_graph.py
│   │   └── question_test_policy.py
│   ├── boundary/
│   │   ├── decision_boundary.py
│   │   ├── capability_profile_provider.py
│   │   └── failure_policy.py
│   ├── output/
│   │   ├── patient_output.py
│   │   └── clinician_report.py
│   └── storage/
│       ├── runtime_store.py
│       └── rule_store.py
├── assets/
│   ├── symptom_groups/
│   │   ├── chest_pain.yml
│   │   └── fever.yml
│   ├── red_flag_rules.yml
│   ├── test_recommendation_rules.yml
│   └── capability_profiles.yml
├── tests/
│   ├── cases/
│   │   ├── chest_pain_cases.yml
│   │   └── fever_cases.yml
│   └── test_runtime_flow.py
└── docs/
```

---

# 十、开发顺序建议

```text
1. 定义 RuntimeStatus、RuntimeState、RuntimeTrace 数据结构
2. 实现 RuntimeStore
3. 实现 Runtime API start / continue / status / trace
4. 实现 EntryAssessment
5. 实现 CaseFrame
6. 实现 StaticRuleProvider 和 Knowledge Context
7. 实现 Experience Context 空实现 / mock 实现
8. 实现 SafetyGate
9. 实现 Differential Diagnosis Board
10. 实现 EvidenceGraph
11. 实现 Question / Test Policy
12. 实现 DecisionBoundary
13. 实现 Patient Output 和 Clinician Report
14. 编写 10–20 个测试病例
15. 跑通完整 Runtime 流程
```

---

# 十一、Phase 1 完成标准

```text
1. 可以通过 API 创建和继续 Runtime。
2. RuntimeState 能被稳定创建、读取、更新。
3. RuntimeStatus 能反映当前问诊状态。
4. CaseFrame 能结构化保存主诉、症状和缺失信息。
5. Knowledge Context 能读取静态症状群规则。
6. Experience Context 能以空实现或 mock 形式参与链路。
7. SafetyGate 能识别至少胸痛和发热两个症状群的危险信号。
8. DDx Board 能生成候选诊断状态。
9. EvidenceGraph 能记录支持证据、反对证据、缺失证据。
10. Question / Test Policy 能生成下一步追问或检查建议。
11. DecisionBoundary 能区分患者端和医生端输出。
12. 高危病例不会输出低风险安抚性结论。
13. RuntimeTrace 能记录每轮模块执行和关键判断。
14. 10–20 个测试病例可以跑通。
15. 模块失败时能进入 error_safe_halted 或保守输出。
```

---

# 十二、Phase 1 不合格表现

```text
1. 用户输入后直接生成自然语言回答，没有 RuntimeState。
2. SafetyGate 只是免责声明，不参与输出边界控制。
3. 候选诊断没有状态，只有字符串列表。
4. EvidenceGraph 只是解释结果，不影响下一步追问。
5. 患者端和医生端输出没有差异。
6. RuntimeTrace 无法解释为什么问这个问题、为什么限制输出。
7. 高危病例仍可能输出“可能问题不大”。
8. Knowledge Context 只是拼 Prompt，没有结构化规则。
```

---

# 十三、Phase 1 与后续阶段关系

| Phase 1 模块 | 后续扩展方向 |
|---|---|
| Knowledge Context | Phase 2 接入 Clinical Pathway、KG-lite、RAG Evidence Library |
| Experience Context | Phase 2/4 接入 Clinical Experience Memory |
| Capability Profile Provider | Phase 3 接入 Evaluation Results 和能力授权 |
| RuntimeTrace | Phase 4 作为经验进化和复盘输入 |
| DecisionBoundary | Phase 3/5 接入更完整的权限和再认证状态 |
| Static Rules | Phase 2 升级为可版本化共享能力资产 |

Phase 1 开发时不能把规则和逻辑写死在业务函数里，应通过 Provider 或配置读取，为后续替换资产层留接口。

---

# 十四、Phase 1 最终目标总结

Phase 1 的最终目标不是证明系统医学能力很强，而是证明 ClinMindRuntime 的核心架构成立：

```text
它能维护诊断状态，
能识别危险信号，
能保留候选诊断，
能组织证据关系，
能根据证据缺口决定下一步动作，
能根据风险和能力边界限制输出，
能记录每一步判断依据。
```

如果 Phase 1 完成，ClinMindRuntime 就具备了从普通医疗问答升级为“受控诊断 Runtime”的最小可运行形态。
