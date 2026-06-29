# ClinMindRuntime

受控医疗 AI Runtime：结构化临床推理、资产治理与评估闭环，**不是**普通 RAG 聊天应用。

当前版本：**Phase 3-P0 Frozen / Phase 4 Preparation Pending**（Phase 1 Runtime + Phase 2 Asset Provider + Phase 3 Evaluation 已落地并冻结）

## 项目定位

ClinMindRuntime 是一个面向临床 AI 系统的 **Java/Spring Boot 运行时**，核心思路：

- **Runtime 主控**：问诊流程、安全门、患者/医生双端输出边界由代码控制
- **Asset Provider**：症状群知识、红旗规则、CapabilityProfile 等以 YAML 资产包注入
- **Evaluation 闭环**：标准病例集 → Runtime 执行 → Scorer 评分 → 聚合报告 → CapabilityProfile 更新建议

与「检索 + 大模型直接回答」的区别：Runtime **不绕过**结构化模块做最终临床判断；Evaluation **不绕过** Runtime 直接评输出文本。

## 当前已实现

| 阶段 | 能力 | 状态 |
|------|------|------|
| Phase 1 | 患者/医生 Runtime、SafetyGate、Trace、DecisionBoundary | 已完成 |
| Phase 2 | 资产包 `phase2-default`、Provider 接口、debug `assets-used` | 已完成 |
| Phase 3 | YAML 病例集、`RuntimeEvaluationRunner`、7 个 Scorer、EvaluationResult 聚合、CapabilityProfile Proposal、debug Evaluation API | 已冻结 |

Phase 3-P0 冻结记录见 [`docs/Phase3_P0冻结记录.md`](docs/Phase3_P0冻结记录.md)。人工 API 验收见 [`docs/Phase3_人工测试API结果.md`](docs/Phase3_人工测试API结果.md)。

## 快速启动

**环境**：JDK 21（推荐）、Maven 3.9+

```powershell
set JAVA_HOME=D:\cxj\software\jdk21
mvn -DskipTests package
java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar
```

服务默认：`http://localhost:8080`

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
- 无数据库持久化、无前端后台、无权限系统
- 无 MCP / LangGraph / 完整 RAG 平台

## 文档入口

| 文档 | 说明 |
|------|------|
| [`docs/README.md`](docs/README.md) | 文档导航 |
| [`docs/项目展示导读.md`](docs/项目展示导读.md) | 面试/展示用精简导读 |
| [`docs/Phase3_P0冻结记录.md`](docs/Phase3_P0冻结记录.md) | Phase 3-P0 冻结依据 |
| [`docs/ClinMindRuntime完整系统设计.md`](docs/ClinMindRuntime完整系统设计.md) | 系统总设计 |
| [`docs/ClinMindRuntime阶段拆分路线图.md`](docs/ClinMindRuntime阶段拆分路线图.md) | Phase 1–5 路线 |
| [`docs/Phase3_开发任务清单.md`](docs/Phase3_开发任务清单.md) | Phase 3 实现进度 |
| [`docs/AI_IMPLEMENTATION_SKILL.md`](docs/AI_IMPLEMENTATION_SKILL.md) | AI 实现约束（给 Cursor/Agent） |

## 下一阶段

**Phase 4 准备阶段**：先补 Phase4 详细设计和开发任务清单，推荐主题是 `ExperienceCandidate / TrainingExampleCandidate` 候选沉淀机制。

当前不应跳过设计直接实现 Phase4 功能代码。

## License

Internal prototype — 见仓库根目录许可说明（如有）。