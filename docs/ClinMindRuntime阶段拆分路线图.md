# ClinMindRuntime 阶段拆分路线图

> 本文档是 `ClinMindRuntime完整系统设计.md` 的落地路线图。  
> 总设计文档定义完整愿景、能力域、三层架构和核心机制；本文档负责把完整愿景拆分为多个可迭代实现阶段，并明确每个阶段的目标、范围、产出物、验证方式和完成标准。

---

# 一、路线图定位

ClinMindRuntime 的完整愿景是企业级智能诊断训练—运行—经验进化平台。这个愿景不能一次性完成，也不应该一开始就实现完整平台后台、经验进化闭环、真实医生审核和企业级治理。

本路线图的核心原则是：

```text
先验证 Runtime 是否成立，
再补齐共享能力资产，
再建立训练与评估闭环，
再引入经验进化，
最后走向平台化和企业级治理。
```

因此，阶段拆分不是按照“页面功能”拆，而是按照系统能力成熟度拆。

---

# 二、总体阶段划分

| 阶段 | 名称 | 核心目标 | 主要验证点 |
|---|---|---|---|
| Phase 0 | 项目骨架与设计冻结 | 固化总设计、建立工程骨架、准备最小知识资产 | 项目可启动，架构边界清楚 |
| Phase 1 | Runtime MVP | 跑通一次受控问诊 Runtime | 状态、风险、候选诊断、证据图、输出边界可运行 |
| Phase 2 | 共享能力资产原型 | 把静态规则升级为可管理的知识和能力资产 | Knowledge Context / Capability Profile / Experience Context 有真实资产来源 |
| Phase 3 | 训练与评估闭环 | 建立症状群训练、病例考试和能力授权机制 | Evaluation Results 能驱动 Capability Profile |
| Phase 4 | 经验进化闭环 | 从 RuntimeTrace、医生反馈、随访结局中沉淀经验 | Experience Candidates 能被审核并进入 Clinical Experience Memory |
| Phase 5 | 平台化与企业级治理 | 建立管理后台、权限、审计、版本、回滚和企业级运行模式 | 系统具备平台化运行和治理能力 |

---

# 三、阶段拆分总原则

## 3.1 不先做大而全平台

第一阶段不做完整 Training Center、Experience Memory Center、Evaluation Center，也不做完整企业级后台。先实现 Runtime 的核心机制。

## 3.2 不先追求医疗知识全面性

第一阶段只选少量症状群和少量病例验证机制，不追求覆盖所有疾病。

## 3.3 不把 LLM 回答当核心成果

系统的核心不是“LLM 回答得像医生”，而是：

```text
RuntimeState 是否稳定
SafetyGate 是否可靠
DDx Board 是否可追踪
EvidenceGraph 是否能控制下一步动作
DecisionBoundary 是否能限制输出
RuntimeTrace 是否能复盘
```

## 3.4 不做未经治理的自动学习

经验进化必须后置。真实病例经验、医生反馈和随访结局不能直接改线上行为，必须经过审核、评估和再认证。

## 3.5 每个阶段都必须有完成标准

每个阶段都必须能用明确产物和测试结果判断是否完成，而不是只写概念。

---

# 四、Phase 0：项目骨架与设计冻结

## 4.1 阶段目标

Phase 0 的目标是把总设计文档固化为项目起点，并建立最小工程骨架。

它不追求问诊能力，而是确认：

```text
项目定位明确
核心设计文档稳定
阶段路线清楚
工程目录初步确定
后续实现不会继续无边界扩展愿景
```

## 4.2 本阶段要做什么

```text
1. 固化 ClinMindRuntime 完整系统设计文档
2. 新增阶段拆分路线图
3. 确定 Phase 1 的开发范围
4. 建立基础工程目录
5. 建立最小 README
6. 准备少量测试病例和静态规则样例
```

## 4.3 本阶段不做什么

```text
不实现完整 Runtime
不接入真实医疗数据
不做完整 RAG
不做完整 KG
不做平台后台
不做经验记忆
不做医生审核流程
```

## 4.4 阶段产出物

```text
docs/ClinMindRuntime完整系统设计.md
docs/ClinMindRuntime阶段拆分路线图.md
docs/Phase1_Runtime_MVP_实现规格.md（下一步生成）
README.md
基础工程目录
少量测试病例
少量静态规则样例
```

## 4.5 完成标准

```text
1. 总设计文档不再继续扩充愿景，只做必要修正
2. 阶段拆分明确
3. Phase 1 范围清晰
4. 工程目录可以支撑 Runtime MVP 开发
```

---

# 五、Phase 1：Runtime MVP

## 5.1 阶段目标

Phase 1 是整个项目最关键的阶段。目标是跑通一次“受控诊断 Runtime”的最小闭环。

这一阶段要证明 ClinMindRuntime 不是普通医疗问答，而是一个状态驱动、证据驱动、安全边界驱动的问诊运行时。

核心验证目标：

```text
用户输入
→ Runtime API
→ EntryAssessment
→ CaseFrame
→ Knowledge Context
→ Experience Context（可为空实现）
→ SafetyGate
→ Differential Diagnosis Board
→ EvidenceGraph
→ Question / Test Policy
→ DecisionBoundary
→ Patient / Clinician Output
→ RuntimeTrace
```

## 5.2 本阶段要做什么

### 5.2.1 Runtime 基础设施

```text
Runtime API
RuntimeStatus
RuntimeState
RuntimeTrace
Short-term Context
```

### 5.2.2 Runtime 执行链路

```text
EntryAssessment
CaseFrame
Knowledge Context
Experience Context 空实现 / mock 实现
SafetyGate
Differential Diagnosis Board
EvidenceGraph
Question / Test Policy
DecisionBoundary
Patient-facing Output
Clinician-facing Output
FailurePolicy
```

### 5.2.3 最小知识资产

```text
胸痛 / 胸闷症状群静态规则
发热症状群静态规则
危险信号规则
必问问题配置
候选诊断配置
检查建议配置
```

### 5.2.4 最小评估病例

```text
10–20 个手写测试病例
至少包含普通病例、高危病例、信息缺失病例、误导表达病例
```

## 5.3 本阶段不做什么

```text
不做完整 RAG Evidence Library
不做完整 KG-lite
不做真实 Clinical Experience Memory
不做真实医生审核
不做随访结局接入
不做平台管理后台
不做自动经验学习
不输出处方建议
不输出患者端确定诊断
```

## 5.4 技术实现建议

Phase 1 可以先采用轻量实现：

```text
后端：FastAPI
存储：内存 + SQLite / JSON 文件
短期上下文：内存或 Redis 可选
知识资产：JSON / YAML 静态配置
模型调用：可选，先以规则和结构化输出为主
前端：可先不做完整前端，用 Swagger / Postman / 简单页面验证
```

## 5.5 关键数据结构

Phase 1 必须明确以下 schema：

```text
RuntimeState
RuntimeStatus
RuntimeTrace
CaseFrame
KnowledgeContext
ExperienceContext
SafetyGateResult
DDxCandidate
DifferentialDiagnosisBoard
EvidenceGraph
QuestionAction
TestRecommendationAction
DecisionBoundaryResult
PatientOutput
ClinicianReport
```

## 5.6 阶段完成标准

Phase 1 完成时，系统应能做到：

```text
1. 用户输入症状后，可以创建 Runtime
2. RuntimeState 能保存并更新病例状态
3. SafetyGate 能识别配置好的危险信号
4. DDx Board 能生成候选诊断状态
5. EvidenceGraph 能记录支持、反对、缺失证据
6. Question / Test Policy 能决定下一步追问或检查建议
7. DecisionBoundary 能区分患者端和医生端输出
8. RuntimeTrace 能记录本轮判断依据
9. 高危病例不会输出低风险安抚性结论
10. 至少 10–20 个测试病例可以跑通
```

## 5.7 Phase 1 的核心价值

Phase 1 不追求医学覆盖面，而是验证系统范式：

```text
从“问答系统”升级为“受控诊断 Runtime”是否成立。
```

只要 Phase 1 能跑通，项目就已经具备和普通 RAG 医疗问答拉开差距的核心雏形。

---

# 六、Phase 2：共享能力资产原型

## 6.1 阶段目标

Phase 2 的目标是把 Phase 1 中的静态配置升级为可管理、可版本化、可检索的共享能力资产原型。

Phase 1 中的 Knowledge Context 和 Experience Context 可以先是静态或 mock；Phase 2 要开始让它们连接真实资产层。

## 6.2 本阶段要做什么

```text
1. 建立 Symptom Rotation Library 原型
2. 建立 Clinical Pathway 原型
3. 建立 Red Flag Rules 配置管理
4. 建立 Test Recommendation Rules 配置管理
5. 建立 RAG Evidence Library 原型
6. 建立 KG-lite 原型
7. 建立 Capability Profile 原型
8. 建立 Clinical Experience Memory 原型
9. 建立资产版本管理字段
10. 让 Runtime 通过 Provider 读取这些资产
```

## 6.3 本阶段不做什么

```text
不做完整 Training Center
不做完整后台 UI
不做真实医生审核闭环
不做真实自动经验进化
不做大规模知识库
不做复杂图数据库优化
```

## 6.4 技术实现建议

```text
Clinical Pathway：JSON / YAML + 数据库存储
Red Flag Rules：规则表 + 版本字段
RAG Evidence Library：少量 markdown / PDF 切片 + 向量检索
KG-lite：先用关系表或轻量图结构，不必直接上复杂 Neo4j
Capability Profile：数据库表 + 静态评估结果生成
Clinical Experience Memory：少量手写 verified experience units
```

## 6.5 阶段完成标准

```text
1. Runtime 不再直接读取硬编码规则，而是通过 Provider 读取资产
2. Knowledge Context 能聚合 Clinical Pathway、RAG Evidence、KG-lite 和规则
3. Experience Context 能返回少量已验证经验单元
4. Capability Profile 能被 DecisionBoundary 读取
5. 资产具备版本字段和来源字段
6. Phase 1 测试病例继续通过
```

---

# 七、Phase 3：训练与评估闭环

## 7.1 阶段目标

Phase 3 的目标是建立“症状群训练—病例考试—评估结果—能力授权”的闭环。

这一阶段要证明：

```text
系统能力不是口头声明，而是由 Evaluation Results 和 Capability Profile 支撑。
```

## 7.2 本阶段要做什么

```text
1. 建立症状群训练包管理机制
2. 建立标准病例集
3. 建立病例考试流程
4. 建立评估指标计算
5. 建立 LLM-only / RAG-only / Runtime 对照实验
6. 生成 Evaluation Results
7. 根据 Evaluation Results 更新 Capability Profile
8. 让 DecisionBoundary 根据 Capability Profile 控制输出
```

## 7.3 本阶段不做什么

```text
不做真实医院级评估
不承诺临床有效性
不做自动上线能力升级
不做复杂统计平台
```

## 7.4 最小评估指标

```text
危险信号识别率
高危病例漏放率
必问问题覆盖率
候选诊断 Top-3 召回率
推荐检查合理率
输出越权率
证据归因正确率
医生端摘要可用性
患者端安全表达合格率
```

## 7.5 阶段完成标准

```text
1. 至少支持 2 个症状群的训练包
2. 每个症状群至少 30–50 个评估病例
3. 能生成 Evaluation Results
4. 能根据评估结果更新 Capability Profile
5. DecisionBoundary 能根据能力等级改变输出范围
6. 能完成 LLM-only / RAG-only / Runtime 的初步对照
```

---

# 八、Phase 4：经验进化闭环

## 8.1 阶段目标

Phase 4 的目标是建立“运行记录—医生反馈—随访结局—经验候选—审核—经验记忆—再认证”的闭环。

这一阶段要证明：

```text
系统不是在线自动学习，而是受控地沉淀临床经验。
```

## 8.2 本阶段要做什么

```text
1. 完善 RuntimeTrace 作为复盘输入
2. 建立 Doctor Feedback 数据结构
3. 建立 Follow-up Outcome 数据结构
4. 建立 Shadow Learning 原型
5. 生成 Experience Candidates
6. 建立 Experience Memory Governance 流程
7. 将审核通过的经验写入 Clinical Experience Memory
8. 对经验上线前进行回归测试
9. 建立经验下线和回滚机制
10. 让经验触发影响 Question Policy、SafetyGate 和 DecisionBoundary
```

## 8.3 本阶段不做什么

```text
不让模型自动修改线上经验
不让未经审核经验影响 Runtime
不让单个病例直接变成规则
不做无边界个性化记忆
```

## 8.4 经验状态流转

```text
Candidate
  ↓ 医生审核
Reviewed
  ↓ 离线评估 / 回归测试
Validated
  ↓ 上线到 Runtime 可检索范围
Active
  ↓ 过期 / 冲突 / 失败 / 回滚
Deprecated
```

## 8.5 阶段完成标准

```text
1. RuntimeTrace 能被用于病例复盘
2. Doctor Feedback 能记录医生修正原因
3. Follow-up Outcome 能回填最终结局
4. Shadow Learning 能生成候选经验
5. 候选经验不能直接进入 Runtime
6. 经验审核通过后可进入 Clinical Experience Memory
7. 经验触发可被 RuntimeTrace 记录
8. 经验上线失败时可回滚
```

---

# 九、Phase 5：平台化与企业级治理

## 9.1 阶段目标

Phase 5 的目标是把前面阶段形成的能力整合为企业级平台。

这一阶段才开始重点建设完整后台、权限、审计、版本管理、治理流程和多角色协作。

## 9.2 本阶段要做什么

```text
1. Training Center 后台
2. Runtime Console 后台
3. Experience Memory Center 后台
4. Evaluation & Recertification Center 后台
5. Audit & Governance Center 后台
6. Role & Permission 权限体系
7. 版本管理与发布流程
8. 回滚机制
9. 数据脱敏和访问审计
10. Patient-facing / Clinician Copilot / Silent Evaluation 三种模式完整化
```

## 9.3 本阶段不做什么

```text
不在没有真实业务约束前过度工程化
不为了平台化牺牲 Runtime 可验证性
不把后台页面数量当作系统成熟度
```

## 9.4 阶段完成标准

```text
1. 不同角色权限清楚
2. 平台可管理训练包、经验、评估和审计
3. Runtime Console 可查看一次问诊的完整状态和 Trace
4. 经验上线、下线、回滚可审计
5. Capability Profile 的更新可追踪
6. 三种运行模式可以独立配置输出边界
```

---

# 十、各阶段依赖关系

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

其中最关键的依赖是：

```text
没有 Phase 1 的 RuntimeState / SafetyGate / EvidenceGraph / DecisionBoundary，后续资产层和平台层没有真实调用对象。
没有 Phase 2 的共享能力资产，Phase 3 的训练评估无法沉淀为可用资产。
没有 Phase 3 的 Evaluation Results，Capability Profile 就没有可信来源。
没有 Phase 4 的复盘闭环，Clinical Experience Memory 就只是手写经验库。
没有 Phase 5 的治理平台，系统无法进入企业级运行。
```

---

# 十一、第一阶段优先级

Phase 1 中的开发优先级如下：

## 11.1 P0-A：Runtime 状态骨架

```text
Runtime API
RuntimeStatus
RuntimeState
RuntimeTrace
Short-term Context
```

完成后应能创建 Runtime、保存状态、查看状态和记录 Trace。

## 11.2 P0-B：病例结构化与入口判断

```text
EntryAssessment
CaseFrame
```

完成后应能判断输入属于健康咨询、临床问诊、急症提示还是不支持场景，并将主诉和基础症状写入 CaseFrame。

## 11.3 P0-C：安全门和候选诊断

```text
Knowledge Context 静态规则
SafetyGate
Differential Diagnosis Board
```

完成后应能根据静态规则识别危险信号，并维护候选诊断状态。

## 11.4 P0-D：证据图与下一步动作

```text
EvidenceGraph
Question / Test Policy
```

完成后应能根据缺失证据决定继续追问或建议检查。

## 11.5 P0-E：输出边界与分角色表达

```text
DecisionBoundary
Patient-facing Output
Clinician-facing Output
FailurePolicy
```

完成后应能区分患者端和医生端输出，并在高危或模块失败时收紧输出。

## 11.6 P0-F：最小测试集

```text
10–20 个测试病例
胸痛 / 发热两个症状群
普通病例 / 高危病例 / 信息缺失病例 / 误导表达病例
```

完成后应能用测试病例证明 Runtime MVP 能跑通。

---

# 十二、各阶段与总设计文档的对应关系

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

# 十三、当前最应该开始的工作

当前不应继续扩充完整愿景，而应进入 Phase 1 详细设计。

下一份文档建议是：

```text
docs/Phase1_Runtime_MVP_实现规格.md
```

该文档应具体定义：

```text
1. Runtime API 详细接口
2. RuntimeState schema
3. RuntimeStatus 状态流转
4. CaseFrame schema
5. Knowledge Context 静态规则格式
6. Experience Context 空实现 / mock 格式
7. SafetyGate 输入输出
8. DDx Board 输入输出
9. EvidenceGraph 输入输出
10. Question / Test Policy 输入输出
11. DecisionBoundary 输入输出
12. RuntimeTrace 格式
13. 数据库表 / 文件存储设计
14. 工程目录与模块职责
15. 最小测试病例
16. Phase 1 完成标准
```

---

# 十四、最终阶段目标总结

ClinMindRuntime 的实现路线不是一次性建成完整医疗 AI 平台，而是逐步验证以下能力：

```text
Phase 1：证明受控诊断 Runtime 能跑通。
Phase 2：证明 Runtime 能调用共享能力资产。
Phase 3：证明能力不是口头声明，而能通过评估授权。
Phase 4：证明经验不是自动记忆，而能通过治理进化。
Phase 5：证明系统可以平台化、权限化、审计化、企业级运行。
```

因此，项目落地的核心节奏是：

```text
先让 Runtime 站起来，
再让资产接进来，
再让评估管住它，
再让经验慢慢长出来，
最后再做平台化治理。
```
