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
当前阶段：Phase 5-P0 freeze complete / Phase 5-P1 planning pending。
```

当前最优先任务：

```text
1. 读取 docs/Phase5_P0冻结记录.md 确认边界。
2. 在有 Docker 的环境执行 RUN_POSTGRES_TESTS=true postgres 专项测试。
3. 规划 Phase 5-P1（最小 Console / RBAC 强化）。
4. 继续禁止提前接入 RAG、Python Provider、前端、模型训练和正式 RBAC。
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
8. docs/Phase5_P0持久化与治理底座_实现规格.md
8. docs/架构模式与设计模式说明.md
```

阅读目标：快速理解项目定位、为什么不是普通 RAG / Agent Demo、Phase 1–4 已经完成并冻结了什么，以及 Phase 5 为什么优先做持久化与治理底座。

## 2.2 后续代码实现 / AI 编码工具

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase5_P0开发任务清单.md
3. docs/Phase5_P0持久化与治理底座_实现规格.md
4. docs/Phase5_P0数据库Schema设计.md
5. docs/Phase5_P0Repository迁移设计.md
6. docs/Phase5_P0审计与权限边界设计.md
7. docs/Phase5_P0_API与测试设计.md
8. docs/Phase4_P1冻结记录.md
9. docs/测试与CI总方案.md
10. docs/数据安全与合规边界规划.md
```

阅读目标：明确 Phase 5-P0 当前只允许按任务清单做持久化与治理底座，不得跳到 RAG、模型训练、前端 Console、正式 RBAC 或正式审核平台。

## 2.3 架构学习 / 自我复盘

```text
1. docs/架构模式与设计模式说明.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/ClinMindRuntime阶段拆分路线图.md
4. docs/全局技术栈与架构选型.md
5. docs/数据库持久化设计.md
6. docs/部署与运维规划.md
```

---

# 三、文档分类

## 3.1 项目入口文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/README.md` | 活跃 | 文档导航入口 |
| `docs/项目展示导读.md` | 活跃 | 面向面试官 / 自我复盘的项目导读 |

## 3.2 已冻结 Phase 文档：Phase 5-P0

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

## 3.3 已冻结 Phase 文档：Phase 4 及更早

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase4_P1冻结记录.md` | 已冻结 | Phase4-P1 冻结依据、边界与 hardening backlog |
| `docs/Phase4_P1人工测试API结果.md` | 验收归档 | Phase4-P1 人工 / E2E 验收记录 |
| `docs/Phase4_P0冻结记录.md` | 已冻结 | Phase4-P0 冻结依据、边界与 hardening backlog |
| `docs/Phase4_人工测试API结果.md` | 验收归档 | Phase4-P0 人工 API 验收记录 |
| `docs/Phase3_P0冻结记录.md` | 已冻结 | Phase3-P0 冻结依据 |
| `docs/Phase3_人工测试API结果.md` | 验收归档 | Phase3 人工 API 验收记录 |

## 3.4 总体架构与专项规划文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/ClinMindRuntime完整系统设计.md` | 稳定基线 | 系统长期定位 |
| `docs/ClinMindRuntime阶段拆分路线图.md` | 稳定基线 | Phase 1–5 演进顺序 |
| `docs/ClinMindRuntime技术实现总方案.md` | 活跃 | 代码分层与依赖方向 |
| `docs/数据库持久化设计.md` | 规划基线 | 长期数据库设计，Phase5_P0 文档是当前实现规格 |
| `docs/数据安全与合规边界规划.md` | 规划基线 | 隐私、脱敏、debug API 与训练数据边界 |
| `docs/平台前端与Console规划.md` | 后续规划 | Phase5-P0 不实现前端 Console |
| `docs/部署与运维规划.md` | 后续规划 | Phase5-P0 可参考但不做完整部署平台 |

---

# 四、当前不应做什么

```text
1. 不应向 Phase 4-P0 / Phase 4-P1 / Phase 5-P0 继续堆新能力（均已冻结）。
2. 不应提前实现 RAG / GraphRAG / Python AI Provider。
3. 不应提前做前端 Console。
4. 不应提前实现模型训练 / 后训练。
5. 不应实现正式 RBAC / 登录 / 多租户。
6. 不应实现 ApprovedExperience 自动生效或 TrainingDatasetVersion 发布。
7. 不应大规模移动 docs 文件，除非同步更新全部引用。
```

---

# 五、当前建议做什么

```text
1. 以 docs/Phase5_P0冻结记录.md 作为 Phase 5-P0 边界依据。
2. 规划 Phase 5-P1（最小 Console API / RBAC 强化）。
3. 在有 Docker 的环境跑通 RUN_POSTGRES_TESTS=true postgres 专项。
4. 保持 Phase1/2/3/4/5 in-memory 回归测试通过。
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

当前文档体系已支撑 Phase5-P0 冻结与 Phase5-P1 规划。Phase3-P0、Phase4-P0、Phase4-P1、Phase5-P0 均已冻结，下一步应进入 Phase 5-P1 规划。
