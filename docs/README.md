# ClinMindRuntime 文档导航

> 本文件是 `docs/` 目录入口，用于说明当前文档体系如何阅读、哪些文档是当前权威入口、哪些是专项设计、哪些是 Phase 实现规格、哪些是实现约束。  
> 当前文档已经完成物理目录重构：入口、总设计、专项设计、Phase 实现和实现约束已分别归入独立目录。  
> 后续新增 Phase 6-P0 文档时，应直接放入 `docs/3-phase实现/`。

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
docs/1-总设计/ClinMindRuntime完整系统设计.md 已升级为 v2.2。
项目定位：受控医疗 AI Agent Runtime 与能力治理平台。
```

当前文档整理状态：

```text
1. docs/ 已完成分层目录重构。
2. docs/0-项目入口/00_项目设计地图.md 是文档体系总入口。
3. docs/1-总设计/ 保存总设计、阶段路线图、技术实现总方案和全局架构支撑文档。
4. docs/2-专项设计/ 保存各能力域专项设计文档。
5. docs/3-phase实现/ 保存 Phase 1–5 的实现规格、任务清单、测试结果和冻结记录；后续 Phase 6 文档也放这里。
6. docs/4-实现约束/ 保存 AI_IMPLEMENTATION_SKILL.md。
```

---

# 二、当前目录结构

```text
docs/
  README.md

  0-项目入口/
    00_项目设计地图.md
    项目展示导读.md
    架构文档缺口审查清单.md

  1-总设计/
    ClinMindRuntime完整系统设计.md
    ClinMindRuntime阶段拆分路线图.md
    ClinMindRuntime技术实现总方案.md
    全局技术栈与架构选型.md
    架构模式与设计模式说明.md

  2-专项设计/
    AI前沿技术选型与接入规划.md
    医学知识库与RAG构建规划.md
    模型训练与后训练规划.md
    数据安全与合规边界规划.md
    数据库持久化设计.md
    平台前端与Console规划.md
    部署与运维规划.md
    测试与CI总方案.md

  3-phase实现/
    Phase1_* / Phase2_* / Phase3_* / Phase4_* / Phase5_*
    Phase3_P0冻结记录.md
    Phase4_P0冻结记录.md
    Phase4_P1冻结记录.md
    Phase5_P0冻结记录.md
    Phase5_P1冻结记录.md
    Phase5_P2冻结记录.md
    Phase5冻结记录.md

  4-实现约束/
    AI_IMPLEMENTATION_SKILL.md
```

---

# 三、推荐阅读顺序

## 3.1 面试官 / 第一次看项目

```text
1. docs/0-项目入口/项目展示导读.md
2. docs/0-项目入口/00_项目设计地图.md
3. docs/1-总设计/ClinMindRuntime完整系统设计.md
4. docs/1-总设计/ClinMindRuntime技术实现总方案.md
5. docs/3-phase实现/Phase5冻结记录.md
6. docs/3-phase实现/Phase5_P2冻结记录.md
7. docs/1-总设计/架构模式与设计模式说明.md
```

阅读目标：快速理解项目为什么不是普通 RAG / Agent Demo，而是一个以 Runtime 主控、Agent / RAG / Model / Tool 受控接入、Evaluation / Audit / Governance 闭环的医疗 AI Runtime 项目。

## 3.2 后续设计 / AI 编码工具

```text
1. docs/0-项目入口/00_项目设计地图.md
2. docs/1-总设计/ClinMindRuntime完整系统设计.md
3. docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md
4. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
5. docs/1-总设计/ClinMindRuntime技术实现总方案.md
6. 当前 Phase 实现规格
7. 当前 Phase 开发任务清单
8. docs/2-专项设计/测试与CI总方案.md
9. docs/2-专项设计/数据安全与合规边界规划.md
```

阅读目标：先确认当前阶段、允许范围和禁止边界，再进入具体实现。

## 3.3 架构学习 / 自我复盘

```text
1. docs/0-项目入口/00_项目设计地图.md
2. docs/1-总设计/ClinMindRuntime完整系统设计.md
3. docs/1-总设计/架构模式与设计模式说明.md
4. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
5. docs/1-总设计/全局技术栈与架构选型.md
6. docs/2-专项设计/数据库持久化设计.md
7. docs/2-专项设计/平台前端与Console规划.md
```

---

# 四、文档分组说明

## 4.1 项目入口与导航组

| 文档 | 状态 | 用途 |
|---|---|---|
| `README.md` | 活跃 | 仓库根入口，说明当前项目状态和阅读入口 |
| `docs/README.md` | 活跃 | docs 目录导航入口 |
| `docs/0-项目入口/00_项目设计地图.md` | 活跃 / 权威入口 | 说明总设计、专项设计、Phase 文档、实现约束和冻结记录之间的关系 |
| `docs/0-项目入口/项目展示导读.md` | 活跃 | 面向阅读者 / 面试官的项目导读 |
| `docs/0-项目入口/架构文档缺口审查清单.md` | 活跃 | 检查文档缺口、过期状态和后续补文档顺序 |

## 4.2 总设计组

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/1-总设计/ClinMindRuntime完整系统设计.md` | 活跃 / 权威 | 定义项目定位、八个能力域、五层架构、统一 Runtime 主链路、AI 前沿技术覆盖矩阵 |
| `docs/1-总设计/ClinMindRuntime技术实现总方案.md` | 活跃 | 代码分层、包结构、统一 Runtime 主链路、Capability Orchestration、AgentExecutionLayer、Runtime Validation |
| `docs/1-总设计/ClinMindRuntime阶段拆分路线图.md` | 活跃 | Phase 1–10 的长期演进顺序 |
| `docs/1-总设计/全局技术栈与架构选型.md` | 活跃 | Java、Spring Boot、React、PostgreSQL、Python、pgvector 等选型 |
| `docs/1-总设计/架构模式与设计模式说明.md` | 活跃 | Provider、Policy、Validator、Store、Strategy、AgentRuntime 等工程模式说明 |

## 4.3 专项设计组

| 文档 | 状态 | 对应总设计位置 |
|---|---|---|
| `docs/2-专项设计/AI前沿技术选型与接入规划.md` | 专项设计 / 技术雷达 | Agent 受控执行域；Tool / MCP / Skills；模型能力域 |
| `docs/2-专项设计/医学知识库与RAG构建规划.md` | 专项设计 | 医学知识与证据域；RAG / KG-lite / GraphRAG |
| `docs/2-专项设计/模型训练与后训练规划.md` | 专项设计 | 模型能力与 Provider 域 |
| `docs/2-专项设计/数据安全与合规边界规划.md` | 专项设计 | 输出边界与安全治理域；Audit；脱敏 |
| `docs/2-专项设计/数据库持久化设计.md` | 专项设计 | Storage / Integration 层 |
| `docs/2-专项设计/平台前端与Console规划.md` | 专项设计 | 平台治理层；Console / Review Queue / Audit Center |
| `docs/2-专项设计/部署与运维规划.md` | 专项设计 / 后置 | 部署、运维、Docker、发布 |
| `docs/2-专项设计/测试与CI总方案.md` | 专项设计 | 测试、CI、回归与质量治理 |

专项文档不是直接编码依据。进入实现前，必须先形成对应 Phase 实现规格和开发任务清单。

## 4.4 Phase 实现规格与冻结归档组

| 阶段 | 状态 | 代表文档 |
|---|---|---|
| Phase 1–2 | 已完成 | `docs/3-phase实现/Phase1_*`、`docs/3-phase实现/Phase2_*` |
| Phase 3 | 已冻结 | `docs/3-phase实现/Phase3_*`、`docs/3-phase实现/Phase3_P0冻结记录.md` |
| Phase 4 | 已冻结 | `docs/3-phase实现/Phase4_*`、`docs/3-phase实现/Phase4_P0冻结记录.md`、`docs/3-phase实现/Phase4_P1冻结记录.md` |
| Phase 5 | 已冻结 | `docs/3-phase实现/Phase5_*`、`docs/3-phase实现/Phase5冻结记录.md` |
| Phase 6-P0 | 待设计 | `docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md`、`docs/3-phase实现/Phase6_P0Agent_API与测试设计.md`、`docs/3-phase实现/Phase6_P0开发任务清单.md` 待新增 |

## 4.5 实现约束组

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md` | 活跃 | 约束 AI / Cursor / Claude Code / Codex 后续实现 |

---

# 五、专项文档如何用于实现

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
docs/1-总设计/ClinMindRuntime完整系统设计.md
↓
docs/2-专项设计/AI前沿技术选型与接入规划.md
↓
docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md
↓
docs/3-phase实现/Phase6_P0开发任务清单.md
↓
代码实现
↓
docs/3-phase实现/Phase6_P0冻结记录.md
```

示例：做 RAG

```text
docs/1-总设计/ClinMindRuntime完整系统设计.md
↓
docs/2-专项设计/医学知识库与RAG构建规划.md
↓
docs/3-phase实现/Phase7_P0RAG_EvidenceProvider_实现规格.md
↓
docs/3-phase实现/Phase7_P0开发任务清单.md
↓
代码实现
↓
docs/3-phase实现/Phase7_P0冻结记录.md
```

---

# 六、当前不应做什么

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
```

---

# 七、当前建议做什么

```text
1. 保持 Phase 1–5 冻结边界。
2. 新增 docs/3-phase实现/Phase6_P0受控Agent执行层_实现规格.md。
3. 新增 docs/3-phase实现/Phase6_P0Agent_API与测试设计.md。
4. 新增 docs/3-phase实现/Phase6_P0开发任务清单.md。
5. 建立 Phase 6-P0 文档后，再更新 docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md。
```

---

# 八、最终结论

当前文档体系已经从平铺式 `docs/` 目录，整理为分层目录结构：

```text
项目入口
→ 总设计
→ 专项设计
→ Phase 实现
→ 实现约束
```

下一步不是继续横向新增规划文档，而是进入：

```text
Phase 6-P0：受控 Agent 执行层 MVP
```
