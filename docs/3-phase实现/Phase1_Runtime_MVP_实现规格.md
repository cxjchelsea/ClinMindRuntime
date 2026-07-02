# Phase 1 Runtime MVP 实现规格

> 本文档是 Phase 1 的总览入口。  
> 它不承载全部低层设计，而是定义 Phase 1 的目标、范围、核心闭环、模块清单、开发顺序和验收标准。  
> 当前 Phase 1 Runtime Core 采用 Java / Spring Boot；Python 和 AI 框架只作为后续可选 Provider。

---

# 一、文档定位

ClinMindRuntime 的文档体系分为：

```text
ClinMindRuntime完整系统设计.md
= 完整愿景、核心能力域、三层架构和长期系统形态

ClinMindRuntime阶段拆分路线图.md
= Phase 0–5 的阶段拆分、阶段目标和能力演进路径

Phase1_技术栈与工程架构决策.md
= Phase 1 的技术栈、工程形态和框架边界

Phase1_Runtime_MVP_实现规格.md
= Phase 1 的总览入口，说明第一阶段要做什么、不做什么、如何验收

Phase1_数据结构与状态设计.md
Phase1_模块接口设计.md
Phase1_API与测试设计.md
Phase1_开发任务清单.md
= Phase 1 的低层详细设计和实现跟踪
```

---

# 二、技术路线总原则

Phase 1 采用：

```text
Java / Spring Boot Runtime Core
+ Spring AOP RuntimeTrace
+ YAML / JSON 静态规则
+ In-memory RuntimeStore
+ JUnit 5 测试
```

后续可选：

```text
Python AI Provider
Spring AI Provider
LangChain4j Provider
LangChain / LangGraph Provider
```

但任何 AI 框架都不能成为 Runtime 主控。

```text
RuntimeState
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
RuntimeTrace
```

必须由 ClinMindRuntime 自己控制。

---

# 三、Phase 1 核心目标

Phase 1 的目标不是实现完整医疗 AI 平台，也不是实现完整 RAG、知识图谱、训练后台、经验记忆或审核流程。

Phase 1 的核心目标是：

```text
跑通一个最小受控诊断 Runtime。
```

系统需要证明：

```text
用户输入症状后，系统不是直接让 LLM 生成医学回答，
而是创建一次 Runtime，维护病例状态，识别风险，构建候选诊断，组织证据图，决定下一步追问或检查建议，并根据输出边界生成患者端或医生端内容。
```

---

# 四、Phase 1 核心闭环

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

---

# 五、Phase 1 做什么

## 5.1 Runtime 基础设施

```text
Spring Boot 工程骨架
RuntimeController
RuntimeStatus / WorkMode / RuntimeMode 等枚举
RuntimeState
RuntimeTrace
RuntimeStore 内存版
@TraceStep
RuntimeTraceAspect
Short-term Context 降级实现
```

Phase 1 暂不单独实现 Redis 级别 ShortTermContextStore，而是由 `RuntimeState.inputHistory` 承担最小短期上下文能力。

## 5.2 Runtime 执行链路

```text
EntryAssessmentService
CaseFrameService
StaticRuleProvider
KnowledgeContextService
ExperienceContextService
SafetyGateService
DifferentialDiagnosisBoardService
EvidenceGraphService
QuestionTestPolicyService
DecisionBoundaryService
PatientOutputService
ClinicianReportService
FailurePolicyService
```

## 5.3 最小静态资产

```text
src/main/resources/assets/symptom-groups/chest-pain.yml
src/main/resources/assets/symptom-groups/fever.yml
src/main/resources/assets/red-flag-rules.yml
src/main/resources/assets/test-recommendation-rules.yml
src/main/resources/assets/capability-profiles.yml
```

## 5.4 最小测试集

```text
10–20 个测试病例
至少覆盖胸痛 / 胸闷、发热两个症状群
至少包含普通病例、高风险病例、信息缺失病例、误导表达病例
JUnit 5 单元测试和集成测试
```

---

# 六、Phase 1 不做什么

```text
不做完整 RAG Evidence Library
不做完整 KG-lite
不做真实 Clinical Experience Memory
不做真实审核流程
不做随访结局接入
不做 Shadow Learning
不做 Training Center 后台
不做 Evaluation Center 后台
不做 Experience Memory Center 后台
不做完整权限系统
不做复杂企业级治理后台
不做患者端确定诊断
不做处方建议
不做治疗方案自动生成
不承诺临床有效性
不引入 Spring Cloud、Nacos、消息队列或复杂微服务
不让 LangChain / LangGraph / Spring AI / LangChain4j 取代 Runtime 主控
```

---

# 七、Phase 1 合理降级策略

| 模块 | Phase 1 实现方式 | 后续阶段升级 |
|---|---|---|
| Knowledge Context | Java 读取静态 YAML / JSON 规则 | Phase 2 接入 Clinical Pathway、KG-lite、RAG Evidence Library |
| Experience Context | Java 空实现或 mock 经验 | Phase 2/4 接入 Clinical Experience Memory |
| Capability Profile | Java 读取静态配置 | Phase 3 由 Evaluation Results 生成 |
| Short-term Context | RuntimeState.inputHistory | 后续接入 Redis / 独立 ShortTermContextStore |
| RuntimeStore | Java 内存存储 | 后续升级为 SQLite / PostgreSQL / Redis |
| AI Provider | Phase 1 不强依赖 | 后续接 Python Provider / Spring AI / LangChain4j |
| 前端 | Swagger / Postman / 简单调试页面 | Phase 5 再做完整 Runtime Console |

---

# 八、推荐工程目录

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

# 九、开发顺序建议

```text
1. 创建 Spring Boot 工程基础结构
2. 定义 RuntimeStatus、RuntimeState、RuntimeTrace
3. 实现 RuntimeStore 内存版
4. 实现 @TraceStep 和 RuntimeTraceAspect 基础能力
5. 实现 Runtime API start / continue / status / result / trace
6. 实现 EntryAssessmentService
7. 实现 CaseFrameService
8. 实现 StaticRuleProvider 和 KnowledgeContextService
9. 实现 ExperienceContextService 空实现 / mock 实现
10. 实现 SafetyGateService
11. 实现 DifferentialDiagnosisBoardService
12. 实现 EvidenceGraphService
13. 实现 QuestionTestPolicyService
14. 实现 DecisionBoundaryService
15. 实现 PatientOutputService 和 ClinicianReportService
16. 实现 FailurePolicyService
17. 编写 10–20 个测试病例
18. 跑通完整 Runtime 流程
```

---

# 十、Phase 1 完成标准

```text
1. 可以通过 API 创建和继续 Runtime。
2. RuntimeState 能被稳定创建、读取、更新。
3. RuntimeStatus 能反映当前问诊状态。
4. RuntimeTrace 能记录关键模块执行。
5. Spring AOP Trace 基础能力可用。
6. CaseFrame 能结构化保存主诉、症状和缺失信息。
7. Knowledge Context 能读取静态症状群规则。
8. Experience Context 能以空实现或 mock 形式参与链路。
9. SafetyGate 能识别至少胸痛和发热两个症状群的危险信号。
10. DDx Board 能生成候选诊断状态。
11. EvidenceGraph 能记录支持证据、反对证据、缺失证据。
12. Question / Test Policy 能生成下一步追问或检查建议。
13. DecisionBoundary 能区分患者端和医生端输出。
14. 高风险病例不会输出低风险安抚性结论。
15. 10–20 个测试病例可以跑通。
16. 模块失败时能进入 error_safe_halted 或保守输出。
```

---

# 十一、Phase 1 与后续阶段关系

| Phase 1 模块 | 后续扩展方向 |
|---|---|
| Knowledge Context | Phase 2 接入 Clinical Pathway、KG-lite、RAG Evidence Library |
| Experience Context | Phase 2/4 接入 Clinical Experience Memory |
| Capability Profile Provider | Phase 3 接入 Evaluation Results 和能力授权 |
| RuntimeTrace / AOP Trace | Phase 4 作为经验进化和复盘输入 |
| DecisionBoundary | Phase 3/5 接入更完整的权限和再认证状态 |
| Static Rules | Phase 2 升级为可版本化共享能力资产 |
| Python AI Provider | 后续接入 LLM / RAG / embedding / 评估能力 |

---

# 十二、最终目标总结

Phase 1 的最终目标不是证明系统医学能力很强，而是证明 ClinMindRuntime 的核心架构成立：

```text
它能维护诊断状态，
能识别危险信号，
能保留候选诊断，
能组织证据关系，
能根据证据缺口决定下一步动作，
能根据风险和能力边界限制输出，
能通过 AOP Trace / RuntimeTrace 记录每一步判断依据。
```

如果 Phase 1 完成，ClinMindRuntime 就具备了从普通医疗问答升级为“受控诊断 Runtime”的最小可运行形态。
