# ClinMindRuntime 文档导航

> 本文件是 `docs/` 目录入口，用于说明当前文档体系如何阅读、哪些文档是当前权威入口、哪些是专项设计、哪些是 Phase 冻结记录、哪些是后续规划。  
> 当前文档体系采用“总设计 → 项目设计地图 → 专项设计 → Phase 实现规格 → 开发任务清单 → 代码 → 冻结记录”的层级关系。

---

# 一、当前项目阶段

当前仓库状态：

```text
Phase 1-P0：Runtime MVP 已完成。
Phase 2-P0：共享能力资产原型已完成。
Phase 3-P0：训练与评估闭环 MVP 已完成并冻结。
Phase 4-P0：经验候选与训练数据候选沉淀机制已完成并冻结。
Phase 4-P1：候选治理与安全加固已完成并冻结。
Phase 5-P0：持久化与治理底座已完成并冻结。
Phase 5-P1：最小 Console 与访问治理已完成并冻结。
Phase 5-P2：最小前端 Console MVP 已完成并冻结。
```

当前总设计状态：

```text
docs/ClinMindRuntime完整系统设计.md 已升级为 v2.2。
项目定位已从 Runtime-first 治理主干升级为：受控医疗 AI Agent Runtime 与能力治理平台。
```

当前最优先任务：

```text
1. 以 docs/00_项目设计地图.md 作为文档体系总入口。
2. 保持 Phase 1–5 已冻结边界，不向已冻结阶段继续堆功能。
3. 进入 Phase 6-P0 前，先完成路线图、技术实现总方案、AI_IMPLEMENTATION_SKILL.md 同步。
4. 下一阶段建议进入 Phase 6-P0：受控 Agent 执行层 MVP。
5. 在 Phase6_P0 实现规格完成前，不直接写 Agent 代码。
```

---

# 二、推荐阅读顺序

## 2.1 面试官 / 第一次看项目

```text
1. docs/项目展示导读.md
2. docs/00_项目设计地图.md
3. docs/ClinMindRuntime完整系统设计.md
4. docs/ClinMindRuntime技术实现总方案.md
5. docs/Phase5冻结记录.md
6. docs/Phase5_P2冻结记录.md
7. docs/架构模式与设计模式说明.md
```

阅读目标：快速理解项目为什么不是普通 RAG / Agent Demo，而是一个以 Runtime 主控、Agent / RAG / Model / Tool 受控接入、Evaluation / Audit / Governance 闭环的医疗 AI Runtime 项目。

## 2.2 后续设计 / AI 编码工具

```text
1. docs/00_项目设计地图.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/AI_IMPLEMENTATION_SKILL.md
4. docs/ClinMindRuntime阶段拆分路线图.md
5. docs/ClinMindRuntime技术实现总方案.md
6. 当前 Phase 实现规格
7. 当前 Phase 开发任务清单
8. docs/测试与CI总方案.md
9. docs/数据安全与合规边界规划.md
```

阅读目标：先确认当前阶段、允许范围和禁止边界，再进入具体实现。

## 2.3 架构学习 / 自我复盘

```text
1. docs/00_项目设计地图.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/架构模式与设计模式说明.md
4. docs/ClinMindRuntime阶段拆分路线图.md
5. docs/全局技术栈与架构选型.md
6. docs/数据库持久化设计.md
7. docs/平台前端与Console规划.md
```

---

# 三、文档分层

## 3.1 第 1 层：总设计

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/ClinMindRuntime完整系统设计.md` | 活跃 / 权威 | 定义项目定位、八个能力域、五层架构、统一 Runtime 主链路、AI 前沿技术覆盖矩阵 |

## 3.2 第 2 层：项目设计地图 / 导航

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/00_项目设计地图.md` | 活跃 / 权威入口 | 说明总设计、专项设计、Phase 文档、实现约束和冻结记录之间的关系 |
| `docs/README.md` | 活跃 | docs 目录导航入口 |
| `README.md` | 活跃 | 仓库根入口，说明当前项目状态和阅读入口 |

## 3.3 第 3 层：专项设计文档

| 文档 | 状态 | 对应总设计位置 |
|---|---|---|
| `docs/AI前沿技术选型与接入规划.md` | 专项设计 / 技术雷达 | Agent 受控执行域；Tool / MCP / Skills；模型能力域 |
| `docs/医学知识库与RAG构建规划.md` | 专项设计 | 医学知识与证据域；RAG / KG-lite / GraphRAG |
| `docs/模型训练与后训练规划.md` | 专项设计 | 模型能力与 Provider 域 |
| `docs/数据安全与合规边界规划.md` | 专项设计 | 输出边界与安全治理域；Audit；脱敏 |
| `docs/数据库持久化设计.md` | 专项设计 | Storage / Integration 层 |
| `docs/平台前端与Console规划.md` | 专项设计 | 平台治理层；Console / Review Queue / Audit Center |
| `docs/部署与运维规划.md` | 专项设计 / 后置 | 部署、运维、Docker、发布 |
| `docs/测试与CI总方案.md` | 专项设计 | 测试、CI、回归与质量治理 |
| `docs/架构模式与设计模式说明.md` | 专项设计 | 全局架构模式、Provider / Policy / Validator / Store 等 |
| `docs/全局技术栈与架构选型.md` | 专项设计 | 全局技术栈 |
| `docs/架构文档缺口审查清单.md` | 文档治理 | 检查文档缺口与过期状态 |
| `docs/项目展示导读.md` | 展示入口 | 面向阅读者 / 面试官的项目导读 |

专项文档不是直接编码依据。进入实现前，必须先形成对应 Phase 实现规格和开发任务清单。

## 3.4 第 4 层：Phase 实现规格与任务清单

| 阶段 | 状态 | 代表文档 |
|---|---|---|
| Phase 5-P2 | 已冻结 | `docs/Phase5_P2冻结记录.md`、`docs/Phase5_P2开发任务清单.md`、`docs/Phase5_P2最小前端Console_MVP_实现规格.md` |
| Phase 5-P1 | 已冻结 | `docs/Phase5_P1冻结记录.md`、`docs/Phase5_P1最小Console与访问治理_实现规格.md` |
| Phase 5-P0 | 已冻结 | `docs/Phase5_P0冻结记录.md`、`docs/Phase5_P0持久化与治理底座_实现规格.md` |
| Phase 4 及更早 | 已冻结 | `docs/Phase4_P1冻结记录.md`、`docs/Phase4_P0冻结记录.md`、`docs/Phase3_P0冻结记录.md` |
| Phase 6-P0 | 待设计 | `docs/Phase6_P0受控Agent执行层_实现规格.md`、`docs/Phase6_P0开发任务清单.md` 待新增 |

## 3.5 第 5 层：实现约束 / AI 编码入口 / 冻结记录

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/AI_IMPLEMENTATION_SKILL.md` | 活跃 | 约束 AI / Cursor / Claude Code / Codex 后续实现 |
| `docs/Phase5冻结记录.md` | 已冻结 | Phase 5 全阶段归档依据 |
| 各 Phase 冻结记录 | 已冻结 | 阶段完成事实、禁止回改边界、后续检查项 |

---

# 四、专项文档如何用于实现

正确链路：

```text
总设计
↓
项目设计地图
↓
专项设计
↓
Phase 实现规格
↓
Phase 开发任务清单
↓
代码实现
↓
测试与人工验证
↓
冻结记录
```

示例：做 Agent

```text
docs/ClinMindRuntime完整系统设计.md
↓
docs/AI前沿技术选型与接入规划.md
↓
docs/Phase6_P0受控Agent执行层_实现规格.md
↓
docs/Phase6_P0开发任务清单.md
↓
代码实现
↓
docs/Phase6_P0冻结记录.md
```

示例：做 RAG

```text
docs/ClinMindRuntime完整系统设计.md
↓
docs/医学知识库与RAG构建规划.md
↓
docs/Phase7_P0RAG_EvidenceProvider_实现规格.md
↓
docs/Phase7_P0开发任务清单.md
↓
代码实现
↓
docs/Phase7_P0冻结记录.md
```

---

# 五、当前不应做什么

```text
1. 不应向 Phase 1–5 已冻结阶段继续堆新能力。
2. 不应在 Phase 6-P0 规格未建立前直接实现 Agent。
3. 不应提前实现 RAG / GraphRAG / Python AI Provider。
4. 不应提前实现 MCP / Tool / Skills 正式接入。
5. 不应提前实现模型训练 / 后训练。
6. 不应实现正式登录 / JWT / OAuth / 多租户。
7. 不应实现正式医生审核平台。
8. 不应实现 ApprovedExperience 自动生效或 TrainingDatasetVersion 发布。
9. 不应把专项规划文档当作当前实现完成状态。
10. 不应大规模移动 docs 文件，除非同步更新全部引用。
```

---

# 六、当前建议做什么

```text
1. 阅读 docs/00_项目设计地图.md，确认文档体系关系。
2. 阅读 docs/ClinMindRuntime完整系统设计.md，确认 v2.2 总设计。
3. 补充 Phase5-P2 冻结状态与 README 状态同步。
4. 更新 docs/ClinMindRuntime阶段拆分路线图.md，使其与总设计 v2.2 对齐。
5. 更新 docs/ClinMindRuntime技术实现总方案.md，加入 Capability Orchestration 与 AgentExecutionLayer。
6. 更新 docs/AI_IMPLEMENTATION_SKILL.md，明确下一阶段为 Phase6-P0 设计准备。
7. 新增 Phase6_P0 受控 Agent 执行层实现规格与开发任务清单。
```

---

# 七、文档维护规则

```text
1. 新增重要文档后，必须更新本文件。
2. 新增或修改实现约束后，必须更新 AI_IMPLEMENTATION_SKILL.md。
3. 发现新的架构缺口后，必须更新 架构文档缺口审查清单.md。
4. 进入新 Phase 前，必须先把规划文档升级为实现规格。
5. 每个专项文档应在开头加入“文档定位块”。
6. 不再凭感觉判断文档是否完整，以 docs/00_项目设计地图.md 和 docs/架构文档缺口审查清单.md 为准。
```

---

# 八、最终结论

当前文档体系已支撑 Phase 1–5 全链路，并已通过 `docs/00_项目设计地图.md` 将专项设计文档挂接到总设计之下。

下一步不是直接写代码，而是同步路线图、技术实现总方案和 AI 实现约束，然后进入：

```text
Phase 6-P0：受控 Agent 执行层 MVP
```
