# Phase 1 技术栈与工程架构决策

> 本文档用于固定 ClinMindRuntime Phase 1 的技术栈和工程边界。  
> 当前决策：Phase 1 Runtime Core 采用 Java / Spring Boot 实现，Python 只作为后续可选 AI Provider，不作为 Runtime 主控。

---

# 一、核心决策

Phase 1 采用以下工程架构：

```text
Java / Spring Boot
= Runtime Core、API、状态管理、状态流转、规则执行、输出边界、RuntimeTrace、AOP Trace、测试主工程

Python AI Provider（后续可选）
= LLM 调用、RAG 实验、embedding、结构化抽取、模型评估等可替换能力

AI 框架
= 只能作为 Provider / Adapter，不允许取代 ClinMindRuntime 自己的 RuntimeState、SafetyGate、EvidenceGraph、DecisionBoundary
```

---

# 二、为什么 Phase 1 Runtime Core 使用 Java

ClinMindRuntime 的 Phase 1 核心不是“调用大模型回答问题”，而是构建一个受控诊断 Runtime。

Java / Spring Boot 更适合承担：

```text
1. Runtime API 和会话生命周期管理
2. RuntimeState 的强类型状态管理
3. RuntimeStatus 状态流转
4. SafetyGate 强规则执行
5. DecisionBoundary 输出边界控制
6. RuntimeTrace / Audit 风格记录
7. AOP Trace / 日志 / 异常捕获
8. 高并发接口服务
9. 后续权限、审计、版本、治理扩展
```

因此，Phase 1 不再把 Python / FastAPI 作为 Runtime Core。

---

# 三、Phase 1 推荐技术栈

## 3.1 Runtime Core

```text
Language: Java 17+
Framework: Spring Boot 3.x
Build: Maven 或 Gradle，优先 Maven
API: Spring Web
Validation: Jakarta Validation
Data Model: Java record / class / enum
Trace: Spring AOP + 自定义 @TraceStep 注解
Config Assets: YAML / JSON
Testing: JUnit 5 + AssertJ / Mockito
Storage Phase 1: In-memory RuntimeStore
Storage Later: SQLite / PostgreSQL / Redis 按阶段接入
```

## 3.2 可选 AI Provider

```text
Python Provider：后续可通过 HTTP 接入
Spring AI：可作为 Java 侧 LLM / RAG Provider
LangChain4j：可作为 Java 侧 LLM / RAG Provider
LangChain / LangGraph：可作为 Python 侧实验 Provider
```

---

# 四、框架使用边界

## 4.1 允许

```text
1. Spring Boot 作为 Runtime Core 工程框架。
2. Spring AOP 作为 RuntimeTrace / 模块追踪机制。
3. Spring AI / LangChain4j 作为后续 Java AI Provider。
4. LangChain / LangGraph 作为后续 Python AI Provider。
5. YAML / JSON 静态规则作为 Phase 1 的 Knowledge Context 来源。
```

## 4.2 禁止

```text
1. 禁止让 LangChain / LangGraph / Spring AI / LangChain4j 成为 Runtime 主控。
2. 禁止把 ClinMindRuntime 改成普通 Agent Chain。
3. 禁止让 LLM 决定 SafetyGate 和 DecisionBoundary 的最终结果。
4. 禁止在 Phase 1 引入完整 RAG、KG、Clinical Experience Memory、平台后台。
5. 禁止为了“企业级”一开始引入 Spring Cloud、Nacos、消息队列或复杂微服务。
```

---

# 五、Phase 1 工程结构建议

```text
clinmind-runtime/
├── pom.xml
├── src/main/java/com/clinmind/runtime/
│   ├── ClinMindRuntimeApplication.java
│   ├── api/
│   │   └── RuntimeController.java
│   ├── state/
│   │   ├── RuntimeState.java
│   │   ├── RuntimeStatus.java
│   │   ├── RuntimeMode.java
│   │   ├── WorkMode.java
│   │   └── RuntimeTrace.java
│   ├── storage/
│   │   └── RuntimeStore.java
│   ├── trace/
│   │   ├── TraceStep.java
│   │   └── RuntimeTraceAspect.java
│   ├── entry/
│   │   └── EntryAssessmentService.java
│   ├── caseframe/
│   │   └── CaseFrameService.java
│   ├── knowledge/
│   │   ├── StaticRuleProvider.java
│   │   └── KnowledgeContextService.java
│   ├── experience/
│   │   └── ExperienceContextService.java
│   ├── safety/
│   │   └── SafetyGateService.java
│   ├── reasoning/
│   │   ├── DifferentialDiagnosisBoardService.java
│   │   ├── EvidenceGraphService.java
│   │   └── QuestionTestPolicyService.java
│   ├── boundary/
│   │   ├── DecisionBoundaryService.java
│   │   ├── CapabilityProfileProvider.java
│   │   └── FailurePolicyService.java
│   └── output/
│       ├── PatientOutputService.java
│       └── ClinicianReportService.java
├── src/main/resources/
│   ├── application.yml
│   └── assets/
│       ├── symptom-groups/
│       │   ├── chest-pain.yml
│       │   └── fever.yml
│       ├── red-flag-rules.yml
│       ├── test-recommendation-rules.yml
│       └── capability-profiles.yml
└── src/test/java/com/clinmind/runtime/
```

---

# 六、AOP Trace 设计原则

旧项目中的 AOP 切面思想应被保留，但在新项目中升级为 RuntimeTrace 机制。

Phase 1 建议设计：

```java
@TraceStep("SafetyGate")
public SafetyGateResult evaluateSafety(RuntimeState state) {
    // ...
}
```

AOP Trace 至少记录：

```text
runtime_id
module_name
input_summary
output_summary
start_time
end_time
duration_ms
error_message
trace_step_status
```

AOP Trace 不能替代业务写入 RuntimeState，它只负责横切追踪和审计辅助。

---

# 七、Python Provider 的后续接入方式

Phase 1 不强依赖 Python。

后续如果需要 Python AI Provider，应通过 HTTP / RPC 接口接入，例如：

```text
POST /ai/extract-case-frame
POST /ai/normalize-symptom
POST /ai/generate-safe-expression
POST /rag/retrieve-evidence
```

Java Runtime Core 只调用 Provider 返回的结构化结果，不能把 Provider 输出直接作为最终诊断。

---

# 八、当前结论

```text
Phase 1 主工程：Java + Spring Boot
状态和模块建模：Java enum / record / class
追踪机制：Spring AOP + RuntimeTrace
测试：JUnit 5
配置资产：YAML / JSON
LLM / RAG 框架：暂不作为主控，只作为后续 Provider
Python：后续可选 Provider，不作为 Phase 1 主工程
```

这一路线能体现 ClinMindRuntime 的企业级 Runtime 设计能力，同时避免项目过早膨胀成复杂多服务系统。
