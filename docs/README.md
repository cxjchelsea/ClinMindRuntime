# ClinMindRuntime 文档导航

> 本文件是 `docs/` 目录入口，用于说明当前文档体系如何阅读、哪些文档是当前实现约束、哪些是历史基线、哪些是后续规划。  
> 当前阶段不进行大规模物理搬迁，先通过本文档完成“逻辑归档”。后续如果需要移动目录，必须同步更新所有文档引用和 `AI_IMPLEMENTATION_SKILL.md`。

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
Phase 5-P2：最小前端 Console MVP — 已完成。
```

当前最优先任务：

```text
1. 阅读 docs/Phase5_P2人工测试结果.md 了解 P2 验收结论。
2. 若继续演进，从 Phase5_P2开发任务清单 §十 后置任务中立项。
3. 保持 Phase 1–5 后端 mvn test 回归与 console-web 前端测试通过。
4. console-web 只调用 Safe Console API，不展示敏感字段。
5. 继续禁止 RAG、Python Provider、模型训练、正式审核平台和 ApprovedExperience 自动生效。
```

---

# 二、推荐阅读顺序

## 2.1 面试官 / 第一次看项目

```text
1. docs/项目展示导读.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/ClinMindRuntime技术实现总方案.md
4. docs/Phase3_P0冻结记录.md
5. docs/Phase4_P0冻结记录.md
6. docs/Phase4_P1冻结记录.md
7. docs/Phase5_P0冻结记录.md
8. docs/Phase5_P1最小Console与访问治理_实现规格.md
9. docs/架构模式与设计模式说明.md
```

阅读目标：快速理解项目定位、为什么不是普通 RAG / Agent Demo、Phase 1–5-P0 已经完成并冻结了什么，以及 Phase 5-P1 为什么先做最小 Console API 与访问治理。

## 2.2 后续代码实现 / AI 编码工具

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase5_P1开发任务清单.md
3. docs/Phase5_P1最小Console与访问治理_实现规格.md
4. docs/Phase5_P1_RBAC与AuditCenter设计.md
5. docs/Phase5_P1Console_API与测试设计.md
6. docs/Phase5_P0冻结记录.md
7. docs/测试与CI总方案.md
8. docs/数据安全与合规边界规划.md
```

阅读目标：了解 Phase 1–5-P2 已交付能力；P1/P2 文档作为冻结与归档基线；后置任务须单独立项，不得跳到 RAG、模型训练、完整产品化前端或正式 OAuth。

## 2.3 架构学习 / 自我复盘

```text
1. docs/架构模式与设计模式说明.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/ClinMindRuntime阶段拆分路线图.md
4. docs/全局技术栈与架构选型.md
5. docs/数据库持久化设计.md
6. docs/平台前端与Console规划.md
```

---

# 三、文档分类

## 3.1 项目入口文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/README.md` | 活跃 | 文档导航入口 |
| `docs/AI_IMPLEMENTATION_SKILL.md` | 活跃 | 约束 AI / Cursor / Claude Code / Codex 后续实现 |
| `docs/项目展示导读.md` | 活跃 | 面向面试官 / 自我复盘的项目导读 |

## 3.2 已归档实现约束文档：Phase 5-P2

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase5_P2开发任务清单.md` | 已归档 | Phase5-P2-A 到 P2-F 实现顺序（已完成） |
| `docs/Phase5_P2人工测试结果.md` | 验收归档 | Phase5-P2 前端 build / test / 联调验收 |
| `docs/Phase5_P2最小前端Console_MVP_实现规格.md` | 归档基线 | Phase5-P2 总体实现规格 |
| `docs/Phase5_P2前端信息架构与页面设计.md` | 归档基线 | 页面结构、导航与展示边界 |
| `docs/Phase5_P2_API对接与前端状态管理设计.md` | 归档基线 | Console API Client、Debug Context |
| `docs/Phase5_P2前端安全边界与测试设计.md` | 归档基线 | 前端安全边界与测试分层 |

## 3.3 已冻结 Phase 文档：Phase 5-P1

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase5_P1冻结记录.md` | 已冻结 | Phase5-P1 冻结依据、边界与 P2 检查项 |
| `docs/Phase5_P1人工测试API结果.md` | 验收归档 | Phase5-P1 人工 / E2E 验收记录 |
| `docs/Phase5_P1开发任务清单.md` | 归档 | Phase5-P1-A 到 P1-F 实现顺序（已完成） |
| `docs/Phase5_P1最小Console与访问治理_实现规格.md` | 冻结基线 | Phase5-P1 总体实现规格 |
| `docs/Phase5_P1_RBAC与AuditCenter设计.md` | 冻结基线 | ActorContext、RBAC-lite、Audit Center 设计 |
| `docs/Phase5_P1Console_API与测试设计.md` | 冻结基线 | Console API、安全 DTO、测试和人工验收依据 |

## 3.4 已冻结 Phase 文档：Phase 5-P0

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase5_P0冻结记录.md` | 已冻结 | Phase5-P0 冻结依据、边界与 P1 检查项 |
| `docs/Phase5_P0人工测试API结果.md` | 验收归档 | Phase5-P0 人工 / E2E 验收记录 |
| `docs/Phase5_P0开发任务清单.md` | 归档 | Phase5-P0-A 到 P0-H 实现顺序（已完成） |
| `docs/Phase5_P0持久化与治理底座_实现规格.md` | 冻结基线 | Phase5-P0 总体实现规格 |
| `docs/Phase5_P0数据库Schema设计.md` | 冻结基线 | PostgreSQL schema / JSONB / migration 依据 |
| `docs/Phase5_P0Repository迁移设计.md` | 冻结基线 | in-memory / postgres 双实现迁移依据 |
| `docs/Phase5_P0审计与权限边界设计.md` | 冻结基线 | AuditLog 与 debug API 最小访问边界 |
| `docs/Phase5_P0_API与测试设计.md` | 冻结基线 | Persistence health、Audit API、测试与人工验收依据 |

## 3.5 已冻结 Phase 文档：Phase 4 及更早

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase4_P1冻结记录.md` | 已冻结 | Phase4-P1 冻结依据、边界与 hardening backlog |
| `docs/Phase4_P1人工测试API结果.md` | 验收归档 | Phase4-P1 人工 / E2E 验收记录 |
| `docs/Phase4_P0冻结记录.md` | 已冻结 | Phase4-P0 冻结依据、边界与 hardening backlog |
| `docs/Phase4_人工测试API结果.md` | 验收归档 | Phase4-P0 人工 API 验收记录 |
| `docs/Phase3_P0冻结记录.md` | 已冻结 | Phase3-P0 冻结依据 |
| `docs/Phase3_人工测试API结果.md` | 验收归档 | Phase3 人工 API 验收记录 |

## 3.6 总体架构与专项规划文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/ClinMindRuntime完整系统设计.md` | 稳定基线 | 系统长期定位 |
| `docs/ClinMindRuntime阶段拆分路线图.md` | 稳定基线 | Phase 1–5 的长期演进顺序 |
| `docs/ClinMindRuntime技术实现总方案.md` | 活跃 | 代码分层、包结构、当前实现状态与后置边界 |
| `docs/架构文档缺口审查清单.md` | 活跃 | 文档覆盖审查与阶段判断 |
| `docs/数据库持久化设计.md` | 规划基线 | 长期数据库设计，Phase5-P0 已完成第一版落地 |
| `docs/数据安全与合规边界规划.md` | 规划基线 | 隐私、脱敏、debug API 与训练数据边界 |
| `docs/平台前端与Console规划.md` | 规划基线 | 完整产品化前端仍后置；P5-P2 已实现 console-web/ 最小 MVP |
| `docs/部署与运维规划.md` | 后续规划 | P1 不做完整部署平台 |

---

# 四、当前不应做什么

```text
1. 不应向 Phase 4-P0 / Phase 4-P1 / Phase 5-P0 / Phase 5-P1 继续堆新能力（均已冻结）。
2. 不应在 P2 归档范围内无立项地扩展 Console API 或 console-web/ 大能力。
3. 不应提前实现 RAG / GraphRAG / Python AI Provider。
4. 不应提前做完整产品化前端 / Training Center（最小 console-web/ 已交付）。
5. 不应提前实现模型训练 / 后训练。
6. 不应实现正式登录 / JWT / OAuth / 多租户。
7. 不应实现正式医生审核平台。
8. 不应实现 ApprovedExperience 自动生效或 TrainingDatasetVersion 发布。
9. 不应大规模移动 docs 文件，除非同步更新全部引用。
```

---

# 五、当前建议做什么

```text
1. 阅读 docs/Phase5_P2人工测试结果.md 了解 P2 验收结论。
2. 若继续演进，从 Phase5_P2开发任务清单 §十 后置任务中立项，而非破坏 P1/P2 边界。
3. 保持 Phase1/2/3/4/5 后端回归与 console-web 前端测试通过。
```

---

# 六、文档维护规则

```text
1. 新增重要文档后，必须更新本文件。
2. 新增或修改实现约束后，必须更新 AI_IMPLEMENTATION_SKILL.md。
3. 发现新的架构缺口后，必须更新 架构文档缺口审查清单.md。
4. 进入新 Phase 前，必须先把规划文档升级为实现规格。
5. 不再凭感觉判断文档是否完整，以 docs/架构文档缺口审查清单.md 为准。
```

---

# 七、最终结论

当前文档体系已支撑 Phase 1–5-P2 全链路。Phase5-P1 已冻结，Phase5-P2 最小前端 Console MVP 已完成。