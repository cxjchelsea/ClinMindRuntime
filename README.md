# ClinMindRuntime

受控医疗 AI Runtime：结构化临床推理、资产治理与评估闭环，**不是**普通 RAG 聊天应用。

当前版本：**Phase 3 Evaluation MVP**（Phase 1 Runtime + Phase 2 Asset Provider + Phase 3 Evaluation 已落地）

## 项目定位

ClinMindRuntime 是一个面向临床 AI 系统的 **Java/Spring Boot 运行时**，核心思路：

- **Runtime 主控**：问诊流程、安全门、患者/医生双端输出边界由代码控制
- **Asset Provider**：症状群知识、红旗规则、CapabilityProfile 等以 YAML 资产包注入
- **Evaluation 闭环**：标准病例集 → Runtime 执行 → Scorer 评分 → 聚合报告 → CapabilityProfile 更新建议

与「检索 + 大模型直接回答」的区别：Runtime **不绕过**结构化模块做最终临床判断；Evaluation **不绕过** Runtime 直接评输出文本。

## 当前已实现（Phase 3-P0）

| 阶段 | 能力 |
|------|------|
| Phase 1 | 患者/医生 Runtime、SafetyGate、Trace、DecisionBoundary |
| Phase 2 | 资产包 `phase2-default`、Provider 接口、debug `assets-used` |
| Phase 3 | YAML 病例集、`RuntimeEvaluationRunner`、7 个 Scorer、EvaluationResult 聚合、CapabilityProfile Proposal、debug Evaluation API |

**193+ JUnit 测试全绿**；人工 API 验收见 [`docs/Phase3_人工测试API结果.md`](docs/Phase3_人工测试API结果.md)。

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
| [`docs/项目展示导读.md`](docs/项目展示导读.md) | 面试/展示用精简导读 |
| [`docs/ClinMindRuntime完整系统设计.md`](docs/ClinMindRuntime完整系统设计.md) | 系统总设计 |
| [`docs/ClinMindRuntime阶段拆分路线图.md`](docs/ClinMindRuntime阶段拆分路线图.md) | Phase 1–5 路线 |
| [`docs/Phase3_开发任务清单.md`](docs/Phase3_开发任务清单.md) | Phase 3 实现进度 |
| [`docs/AI_IMPLEMENTATION_SKILL.md`](docs/AI_IMPLEMENTATION_SKILL.md) | AI 实现约束（给 Cursor/Agent） |

## 下一阶段

**Phase 4**：Feedback / Outcome / RegressionFinding 复盘与经验候选（未开始）。Phase 3-P0 已冻结，优先质量清理而非继续堆 Phase 4/5。

## License

Internal prototype — 见仓库根目录许可说明（如有）。
