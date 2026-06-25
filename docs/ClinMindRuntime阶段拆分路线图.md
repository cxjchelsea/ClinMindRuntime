# ClinMindRuntime 阶段拆分路线图

> 本文档是 `ClinMindRuntime完整系统设计.md` 的落地路线图。  
> 总设计文档定义完整愿景、能力域、三层架构和核心机制；本文档负责把完整愿景拆分为多个可迭代实现阶段，并明确每个阶段的目标、范围、产出物、验证方式和完成标准。

---

# 一、路线图定位

ClinMindRuntime 的完整愿景是企业级智能诊断训练—运行—经验进化平台。这个愿景不能一次性完成，也不应该一开始就实现完整平台后台、经验进化闭环、真实审核和企业级治理。

核心原则：

```text
先验证 Runtime 是否成立，
再补齐共享能力资产，
再建立训练与评估闭环，
再引入经验进化，
最后走向平台化和企业级治理。
```

阶段拆分不是按照页面功能拆，而是按照系统能力成熟度拆。

---

# 二、命名说明

```text
Phase 0 / Phase 1 / Phase 2 ...
= 项目迭代阶段

MVP-P0 / MVP-P1 / MVP-P2
= 某一阶段内部的任务优先级
```

本文档中的 `Phase 1` 指第一阶段 Runtime MVP，不等同于总设计中可能出现的 P0/P1/P2 任务优先级。

---

# 三、总体阶段划分

| 阶段 | 名称 | 核心目标 | 主要验证点 |
|---|---|---|---|
| Phase 0 | 项目骨架与设计冻结 | 固化总设计、建立工程骨架、准备最小资产 | 项目可启动，架构边界清楚 |
| Phase 1 | Runtime MVP | 跑通一次受控问诊 Runtime | 状态、风险、候选诊断、证据图、输出边界可运行 |
| Phase 2 | 共享能力资产原型 | 把静态规则升级为可管理的知识和能力资产 | Knowledge Context / Capability Profile / Experience Context 有真实资产来源 |
| Phase 3 | 训练与评估闭环 | 建立症状群训练、病例考试和能力授权机制 | Evaluation Results 能驱动 Capability Profile |
| Phase 4 | 经验进化闭环 | 从 RuntimeTrace、反馈和结局中沉淀经验 | Experience Candidates 能被审核并进入 Clinical Experience Memory |
| Phase 5 | 平台化与企业级治理 | 建立管理后台、权限、审计、版本、回滚和企业级运行模式 | 系统具备平台化运行和治理能力 |

---

# 四、阶段拆分总原则

```text
1. 不先做大而全平台：Phase 1 先实现 Runtime 核心机制。
2. 不先追求知识覆盖全面性：Phase 1 只选少量症状群和病例验证机制。
3. 不把 LLM 回答当核心成果：核心是状态、风险、证据、边界和 Trace。
4. 不做未经治理的自动学习：经验进化必须后置。
5. 每个阶段都必须有明确产物和完成标准。
```

---

# 五、Phase 0：项目骨架与设计冻结

## 5.1 阶段目标

Phase 0 的目标是把总设计文档固化为项目起点，并建立最小工程骨架。

## 5.2 本阶段要做什么

```text
1. 固化 ClinMindRuntime 完整系统设计文档
2. 新增阶段拆分路线图
3. 确定 Phase 1 的开发范围
4. 确定 Phase 1 技术栈与工程架构
5. 建立基础工程目录
6. 建立最小 README
7. 准备少量测试病例和静态规则样例
```

## 5.3 阶段产出物

```text
docs/ClinMindRuntime完整系统设计.md
docs/ClinMindRuntime阶段拆分路线图.md
docs/Phase1_技术栈与工程架构决策.md
docs/Phase1_Runtime_MVP_实现规格.md
docs/Phase1_数据结构与状态设计.md
docs/Phase1_模块接口设计.md
docs/Phase1_API与测试设计.md
docs/Phase1_开发任务清单.md
README.md
基础工程目录
少量测试病例
少量静态规则样例
```

## 5.4 完成标准

```text
1. 总设计文档不再继续扩充愿景，只做必要修正
2. 阶段拆分明确
3. Phase 1 范围清晰
4. Phase 1 技术路线明确：Java Spring Boot Runtime Core + 可选 Python AI Provider
5. Phase 1 低层设计已经拆分完成
6. 工程目录可以支撑 Runtime MVP 开发
```

---

# 六、Phase 1：Runtime MVP

## 6.1 阶段目标

Phase 1 是整个项目最关键的阶段。目标是跑通一次“受控诊断 Runtime”的最小闭环。

核心链路：

```text
用户输入
→ Runtime API
→ EntryAssessment
→ CaseFrame
→ Knowledge Context（静态规则）
→ Experience Context（空实现 / mock 实现）
→ SafetyGate
→ Differential Diagnosis Board
→ EvidenceGraph
→ Question / Test Policy
→ DecisionBoundary
→ Patient / Clinician Output
→ RuntimeTrace
```

## 6.2 技术路线

```text
Runtime Core：Java 17+ / Spring Boot 3.x
Trace：Spring AOP + @TraceStep + RuntimeTrace
Storage：In-memory RuntimeStore
Config Assets：YAML / JSON
Testing：JUnit 5
Python：后续可选 AI Provider，不作为 Phase 1 主工程
AI 框架：只能作为后续 Provider / Adapter，不能作为 Runtime 主控
```

## 6.3 本阶段要做什么

```text
Spring Boot 工程骨架
RuntimeController
RuntimeStatus
RuntimeState
RuntimeTrace
RuntimeStore
@TraceStep 与 RuntimeTraceAspect
Short-term Context 降级实现
EntryAssessmentService
CaseFrameService
KnowledgeContextService
ExperienceContextService 空实现 / mock 实现
SafetyGateService
DifferentialDiagnosisBoardService
EvidenceGraphService
QuestionTestPolicyService
DecisionBoundaryService
Patient-facing Output
Clinician-facing Output
FailurePolicyService
10–20 个测试病例
```

Phase 1 暂不单独实现 Redis 级别 ShortTermContextStore，而是由 `RuntimeState.inputHistory` 承担最小短期上下文能力。

## 6.4 本阶段不做什么

```text
不做完整 RAG Evidence Library
不做完整 KG-lite
不做真实 Clinical Experience Memory
不做真实审核流程
不做随访结局接入
不做平台管理后台
不做自动经验学习
不输出患者端确定诊断
不承诺临床有效性
不引入 Spring Cloud、Nacos、消息队列或复杂微服务
不让 LangChain / LangGraph / Spring AI / LangChain4j 取代 Runtime 主控
```

## 6.5 阶段完成标准

```text
1. 用户输入症状后，可以创建 Runtime
2. RuntimeState 能保存并更新病例状态
3. Spring AOP Trace 能记录关键模块执行
4. SafetyGate 能识别配置好的危险信号
5. DDx Board 能生成候选诊断状态
6. EvidenceGraph 能记录支持、反对、缺失证据
7. Question / Test Policy 能决定下一步追问或检查建议
8. DecisionBoundary 能区分患者端和医生端输出
9. RuntimeTrace 能记录本轮判断依据
10. 高风险病例不会输出低风险安抚性结论
11. 至少 10–20 个测试病例可以跑通
```

---

# 七、Phase 2：共享能力资产原型

## 7.1 阶段目标

把 Phase 1 中的静态配置升级为可管理、可版本化、可检索的共享能力资产原型。

## 7.2 本阶段要做什么

```text
Symptom Rotation Library 原型
Clinical Pathway 原型
Red Flag Rules 配置管理
Test Recommendation Rules 配置管理
RAG Evidence Library 原型
KG-lite 原型
Capability Profile 原型
Clinical Experience Memory 原型
资产版本管理字段
Provider 读取机制
```

## 7.3 完成标准

```text
1. Runtime 不再直接读取硬编码规则，而是通过 Provider 读取资产
2. Knowledge Context 能聚合多类知识资产
3. Experience Context 能返回少量已验证经验单元
4. Capability Profile 能被 DecisionBoundary 读取
5. Phase 1 测试病例继续通过
```

---

# 八、Phase 3：训练与评估闭环

## 8.1 阶段目标

建立“症状群训练—病例考试—评估结果—能力授权”的闭环。

## 8.2 本阶段要做什么

```text
症状群训练包管理
标准病例集
病例考试流程
评估指标计算
LLM-only / RAG-only / Runtime 对照实验
Evaluation Results
Capability Profile 更新
DecisionBoundary 按能力等级控制输出
```

## 8.3 完成标准

```text
1. 至少支持 2 个症状群的训练包
2. 每个症状群至少 30–50 个评估病例
3. 能生成 Evaluation Results
4. 能根据评估结果更新 Capability Profile
5. DecisionBoundary 能根据能力等级改变输出范围
```

---

# 九、Phase 4：经验进化闭环

## 9.1 阶段目标

建立“运行记录—反馈—结局—经验候选—审核—经验记忆—再认证”的闭环。

## 9.2 本阶段要做什么

```text
RuntimeTrace 作为复盘输入
Feedback 数据结构
Outcome 数据结构
Shadow Learning 原型
Experience Candidates
Experience Memory Governance
Clinical Experience Memory 更新
回归测试
经验下线和回滚机制
经验触发影响 Question Policy / SafetyGate / DecisionBoundary
```

## 9.3 完成标准

```text
1. RuntimeTrace 能被用于病例复盘
2. 反馈能记录修正原因
3. 结局能回填最终结果
4. Shadow Learning 能生成候选经验
5. 候选经验不能直接进入 Runtime
6. 审核通过后可进入 Clinical Experience Memory
7. 经验上线失败时可回滚
```

---

# 十、Phase 5：平台化与企业级治理

## 10.1 阶段目标

把前面阶段形成的能力整合为企业级平台。

## 10.2 本阶段要做什么

```text
Training Center 后台
Runtime Console 后台
Experience Memory Center 后台
Evaluation & Recertification Center 后台
Audit & Governance Center 后台
Role & Permission 权限体系
版本管理与发布流程
回滚机制
数据脱敏和访问审计
Patient-facing / Clinician Copilot / Silent Evaluation 三种模式完整化
```

## 10.3 完成标准

```text
1. 不同角色权限清楚
2. 平台可管理训练包、经验、评估和审计
3. Runtime Console 可查看一次问诊的完整状态和 Trace
4. 经验上线、下线、回滚可审计
5. Capability Profile 的更新可追踪
6. 三种运行模式可以独立配置输出边界
```

---

# 十一、各阶段依赖关系

```text
Phase 0：设计冻结和工程骨架
  ↓
Phase 1：Runtime MVP
  ↓
Phase 2：共享能力资产原型
  ↓
Phase 3：训练与评估闭环
  ↓
Phase 4：经验进化闭环
  ↓
Phase 5：平台化与企业级治理
```

---

# 十二、第一阶段优先级

```text
MVP-P0-A：Runtime 状态骨架
MVP-P0-B：病例结构化与入口判断
MVP-P0-C：安全门和候选诊断
MVP-P0-D：证据图与下一步动作
MVP-P0-E：输出边界与分角色表达
MVP-P0-F：最小测试集与集成验证
```

---

# 十三、各阶段与总设计文档的对应关系

| 总设计文档内容 | 对应阶段 |
|---|---|
| 五个能力域 | Phase 0 后持续指导所有阶段 |
| 平台管理层 | Phase 5 为主，Phase 3/4 开始局部实现 |
| 共享能力资产层 | Phase 2 开始实现 |
| Runtime 执行层 | Phase 1 核心实现 |
| 训练与能力授权机制 | Phase 3 核心实现 |
| 经验进化机制 | Phase 4 核心实现 |
| 工程落地设计 | Phase 0/1 开始，持续演进 |
| 企业级运行模式 | Phase 5 完整实现，Phase 1 先做患者端/医生端输出区分 |
| 评估体系 | Phase 1 最小测试，Phase 3 正式评估闭环 |
| MVP 实现范围 | Phase 1 |

---

# 十四、当前最应该开始的工作

当前不应继续扩充完整愿景，而应进入 Phase 1 的实现。

当前 Phase 1 文档已经拆分为：

```text
docs/Phase1_技术栈与工程架构决策.md
docs/Phase1_Runtime_MVP_实现规格.md
docs/Phase1_数据结构与状态设计.md
docs/Phase1_模块接口设计.md
docs/Phase1_API与测试设计.md
docs/Phase1_开发任务清单.md
```

下一步应该根据这些文档建立 Java Spring Boot 工程骨架，并优先实现 Runtime 状态骨架。

---

# 十五、最终阶段目标总结

```text
Phase 1：证明受控诊断 Runtime 能跑通。
Phase 2：证明 Runtime 能调用共享能力资产。
Phase 3：证明能力不是口头声明，而能通过评估授权。
Phase 4：证明经验不是自动记忆，而能通过治理进化。
Phase 5：证明系统可以平台化、权限化、审计化、企业级运行。
```

项目落地的核心节奏是：

```text
先让 Runtime 站起来，
再让资产接进来，
再让评估管住它，
再让经验慢慢长出来，
最后再做平台化治理。
```
