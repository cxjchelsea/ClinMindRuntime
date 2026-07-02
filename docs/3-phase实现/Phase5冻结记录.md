# Phase 5 冻结记录

> 本文档用于记录 ClinMindRuntime **Phase 5 全阶段**（P0 持久化与治理底座、P1 最小 Console API、P2 最小前端 Console MVP）的归档状态、冻结依据与统一边界。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 5 规划内的三条主线均已交付，后续不再向 Phase 5 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 5（含 P0 / P1 / P2）
冻结状态：已全部冻结
冻结日期：2026-06-25
代码基线：commit fe16d2b
```

Phase 5 三条主线均已完成：

| 子阶段 | 主题 | 状态 | 冻结记录 |
|--------|------|------|----------|
| **P0** | 持久化与治理底座 | 已冻结 | [`Phase5_P0冻结记录.md`](Phase5_P0冻结记录.md) |
| **P1** | 最小 Console API 与访问治理 | 已冻结 | [`Phase5_P1冻结记录.md`](Phase5_P1冻结记录.md) |
| **P2** | 最小前端 Console MVP（`console-web/`） | 已冻结 | [`Phase5_P2冻结记录.md`](Phase5_P2冻结记录.md) |

Phase 5 整体目标已经达到：

```text
Runtime / Evaluation / Candidate / Review / Audit 等治理对象可持久化、可经 Safe Console API 查询与权限控制、可经 console-web/ 可视化复盘，且不泄露敏感字段、不改变 AI 决策边界。
```

---

# 二、冻结依据

## 2.1 任务清单（全部 [x]）

```text
docs/Phase5_P0开发任务清单.md   — P0-A～P0-H
docs/Phase5_P1开发任务清单.md   — P1-A～P1-F
docs/Phase5_P2开发任务清单.md   — P2-A～P2-F
```

## 2.2 自动化测试

```text
后端：mvn test — exit 0（in-memory 默认套件；P1 冻结时约 405 项，归档复验通过）
postgres 专项：RUN_POSTGRES_TESTS=true + Docker（含 Phase5P1ConsolePostgresEndToEndIntegrationTest 等）
前端：console-web npm run test — 35 项；npm run build — 通过
```

## 2.3 人工 / E2E 验收

```text
docs/Phase5_P0人工测试API结果.md
docs/Phase5_P1人工测试API结果.md
docs/Phase5_P2人工测试结果.md
```

## 2.4 规格与设计文档

```text
docs/Phase5_P0持久化与治理底座_实现规格.md
docs/Phase5_P1最小Console与访问治理_实现规格.md
docs/Phase5_P2最小前端Console_MVP_实现规格.md
docs/Phase5_P1_RBAC与AuditCenter设计.md
docs/Phase5_P1Console_API与测试设计.md
docs/Phase5_P2前端信息架构与页面设计.md
docs/Phase5_P2_API对接与前端状态管理设计.md
docs/Phase5_P2前端安全边界与测试设计.md
```

---

# 三、Phase 5 交付能力总览

## 3.1 P0：持久化与治理底座

```text
in-memory / postgres 双模式
Flyway schema、Jdbc*Store、JsonSnapshotMapper
AuditLog（Service / Store / debug API）
Persistence health API
```

## 3.2 P1：最小 Console API

```text
ActorContext / RBAC-lite / AccessPolicy
SafeConsoleDtoMapper
/api/v1/debug/console/**（Runtime、Evaluation、Candidate、Review Queue、Audit Center）
Candidate review 接入 AccessPolicy
Console 查询与 review 写 AuditLog
```

## 3.3 P2：最小前端 Console MVP

```text
console-web/ 五页治理界面
DebugContextPanel、consoleClient、Review 表单
SensitiveFieldRenderGuard、35 项 vitest
```

---

# 四、冻结范围与变更规则

Phase 5 冻结后，P0/P1/P2 范围内只允许：

```text
bug fix
测试补强
文档同步
错误码 / 小型一致性修复
postgres CI / Docker 兼容性修复
依赖安全升级（不破坏 API 与 Safe DTO 契约）
```

不允许在 Phase 5 内继续追加：

```text
完整产品化前端 / Training Center
正式登录 / OAuth / 多租户
Docker Compose 一键编排
Python AI Provider / RAG / GraphRAG
模型训练 / 后训练
ApprovedExperience 自动生效
TrainingDatasetVersion 正式发布
正式医生审核平台
扩大 Safe DTO 暴露面或绕过 AccessPolicy
```

---

# 五、已知限制（Phase 5 整体）

```text
1. RBAC-lite 基于 debug header，不是生产级身份认证。
2. Console API 与 console-web 均为 debug / 治理 MVP，不面向患者端。
3. 无 Redis、pgvector、消息队列。
4. 无 Docker Compose；本地需分别启动 jar 与 npm run dev。
5. Candidate review 不自动修改 AssetPackage / CapabilityProfile / TrainingDataset。
6. postgres 专项测试需 Docker + RUN_POSTGRES_TESTS=true。
```

---

# 六、后置任务（不在 Phase 5 范围）

若继续演进，须单独立项，参见 `docs/Phase5_P2开发任务清单.md` §十：

```text
正式登录 / JWT / OAuth
Docker Compose 本地编排
完整产品化前端 / Training Center
Python AI Provider / RAG / GraphRAG
模型训练 / 后训练
MCP / LangGraph / Agent SDK
正式医生审核平台
ApprovedExperience 生效机制
TrainingDatasetVersion 发布
```

---

# 七、文档索引

| 类型 | 文档 |
|------|------|
| 总冻结 | 本文档 |
| 子阶段冻结 | P0 / P1 / P2 冻结记录 |
| 任务清单 | Phase5_P0/P1/P2 开发任务清单 |
| 人工验收 | Phase5_P0/P1 人工测试API结果、Phase5_P2 人工测试结果 |
| 实现约束 | AI_IMPLEMENTATION_SKILL.md、ClinMindRuntime技术实现总方案.md |
| 文档审查 | 架构文档缺口审查清单.md |

---

# 八、最终结论

Phase 5 已完成从**持久化治理底座**到**Console API**再到**最小前端 Console**的完整闭环。  
当前项目下一步不是继续向 Phase 5 加功能，而是：

```text
1. 保持 Phase 1–5 后端与 console-web 前端回归。
2. 从后置任务中单独立项新 Phase。
3. 不破坏已冻结的 Runtime 主控、Safe DTO 与 Console 访问治理边界。
```
