# ClinMindRuntime

受控医疗 AI Runtime：结构化临床推理、资产治理、评估闭环、候选治理、持久化治理底座与最小 Console 治理，**不是**普通 RAG 聊天应用。

当前版本：**Phase 5-P2 进行中（P2-A 已完成）**（Phase 1–5-P1 已落地/冻结；最小前端 Console MVP 实现中）

## 项目定位

ClinMindRuntime 是一个面向临床 AI 系统的 **Java/Spring Boot 运行时**，核心思路：

- **Runtime 主控**：问诊流程、安全门、患者/医生双端输出边界由代码控制
- **Asset Provider**：症状群知识、红旗规则、CapabilityProfile 等以 YAML 资产包注入
- **Evaluation 闭环**：标准病例集 → Runtime 执行 → Scorer 评分 → 聚合报告 → CapabilityProfile 更新建议
- **Candidate 沉淀**：从 Evaluation 暴露的问题中生成可追踪、可审核、不可自动生效的经验候选与训练数据候选
- **Candidate 治理**：通过脱敏、来源校验和 review 记录，让候选更安全、更可治理，但仍不自动生效
- **Persistence 治理底座**：通过 PostgreSQL、Repository 双实现和 AuditLog，让核心治理对象可持久化、可审计、可恢复
- **Console 治理边界**：通过 ActorContext、RBAC-lite、Safe DTO 和 Audit Center，让治理对象可安全查询和复盘

与「检索 + 大模型直接回答」的区别：Runtime **不绕过**结构化模块做最终临床判断；Evaluation **不绕过** Runtime 直接评输出文本；Candidate **不自动** 上线经验或进入训练集；Persistence **不改变** AI 决策边界；Console **不暴露**敏感原文和未脱敏候选输入。

## 当前已实现 / 已设计

| 阶段 | 能力 | 状态 |
|------|------|------|
| Phase 1 | 患者/医生 Runtime、SafetyGate、Trace、DecisionBoundary | 已完成 |
| Phase 2 | 资产包 `phase2-default`、Provider 接口、debug `assets-used` | 已完成 |
| Phase 3 | YAML 病例集、`RuntimeEvaluationRunner`、7 个 Scorer、EvaluationResult 聚合、CapabilityProfile Proposal、debug Evaluation API | 已冻结 |
| Phase 4-P0 | ExperienceCandidate / TrainingExampleCandidate 候选沉淀机制、Candidate debug API | 已冻结 |
| Phase 4-P1 | CandidateSanitizer、SourceRef 强校验、Candidate review 记录 | 已冻结 |
| Phase 5-P0 | PostgreSQL 持久化、Repository 双实现、AuditLog、Persistence health / Audit API | 已冻结 |
| Phase 5-P1 | 最小 Console API、RBAC-lite、Audit Center、Safe DTO | 已冻结 |
| Phase 5-P2 | 最小前端 Console MVP（`console-web/`） | P2-A 已完成，P2-B 进行中 |

Phase 5-P1 冻结记录见 [`docs/Phase5_P1冻结记录.md`](docs/Phase5_P1冻结记录.md)。Phase 5-P2 规格见 [`docs/Phase5_P2最小前端Console_MVP_实现规格.md`](docs/Phase5_P2最小前端Console_MVP_实现规格.md)。

## 快速启动

**环境**：JDK 21（推荐）、Maven 3.9+

```powershell
set JAVA_HOME=D:\cxj\software\jdk21
mvn -DskipTests package
java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar
```

服务默认：`http://localhost:8080`

### 前端 Console（Phase 5-P2）

```powershell
cd console-web
npm install
npm run dev
```

开发服务器默认 `http://localhost:5173`，`/api` 代理至后端 `8080`。需先启动 Java 后端（P2-B 起对接 Console API）。

## 运行一次 Runtime（患者端）

```http
POST /api/v1/runtime/start
Content-Type: application/json

{
  "session_id": "demo_001",
  "mode": "patient_facing",
  "input": { "text": "胸口闷，活动后更明显，出汗" },
  "basic_info": { "age": 58, "sex": "male" }
}
```

高风险输入应返回 `runtime_status: safety_gate_triggered`，患者端输出含风险提示且 **不泄露** DDx 板。

## 运行一次 Evaluation

```http
POST /api/v1/debug/evaluations/runs
Content-Type: application/json

{
  "case_set_id": "phase3-default",
  "case_set_version": "0.3.0",
  "asset_package_id": "phase2-default",
  "asset_package_version": "0.2.0",
  "include_tags": ["high_risk"],
  "fail_fast": false
}
```

后续可查询：

- `GET /api/v1/debug/evaluations/runs/{run_id}/result`
- `GET /api/v1/debug/evaluations/runs/{run_id}/items/{case_id}`
- `POST /api/v1/debug/evaluations/runs/{run_id}/capability-profile-proposal?symptom_group=chest_pain`

## 当前不做什么

- 不训练基础大模型 / 不做 RLHF
- 不自动修改生产资产包或 CapabilityProfile
- 不自动上线 ExperienceCandidate
- 不自动把 TrainingExampleCandidate 进入训练集
- 不把 Candidate review 当作正式临床审核
- 不做完整前端 Console、正式登录、OAuth、多租户
- 不做 MCP / LangGraph / 完整 RAG 平台

## 文档入口

| 文档 | 说明 |
|------|------|
| [`docs/README.md`](docs/README.md) | 文档导航 |
| [`docs/项目展示导读.md`](docs/项目展示导读.md) | 面试/展示用精简导读 |
| [`docs/Phase5_P1最小Console与访问治理_实现规格.md`](docs/Phase5_P1最小Console与访问治理_实现规格.md) | Phase 5-P1 总体规格 |
| [`docs/Phase5_P1_RBAC与AuditCenter设计.md`](docs/Phase5_P1_RBAC与AuditCenter设计.md) | RBAC-lite 与 Audit Center 设计 |
| [`docs/Phase5_P1Console_API与测试设计.md`](docs/Phase5_P1Console_API与测试设计.md) | Console API 与测试设计 |
| [`docs/Phase5_P1开发任务清单.md`](docs/Phase5_P1开发任务清单.md) | Phase 5-P1 实现顺序 |
| [`docs/Phase5_P1冻结记录.md`](docs/Phase5_P1冻结记录.md) | Phase 5-P1 冻结依据 |
| [`docs/Phase5_P1人工测试API结果.md`](docs/Phase5_P1人工测试API结果.md) | Phase 5-P1 人工 / E2E 验收记录 |
| [`docs/Phase5_P0冻结记录.md`](docs/Phase5_P0冻结记录.md) | Phase 5-P0 冻结依据 |
| [`docs/AI_IMPLEMENTATION_SKILL.md`](docs/AI_IMPLEMENTATION_SKILL.md) | AI 实现约束（给 Cursor/Agent） |

## 下一阶段

**Phase 5-P2-B**：Console API Client 与 Debug Context（`console-web/`）。

P2-A 已完成 Vite + React 基础布局；不应跳过 P2-B 直接实现全部页面，也不应实现正式登录、Docker Compose、RAG 或模型训练。

## License

Internal prototype — 见仓库根目录许可说明（如有）。