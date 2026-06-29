# ClinMindRuntime

受控医疗 AI Runtime：结构化临床推理、资产治理、评估闭环与候选治理，**不是**普通 RAG 聊天应用。

当前版本：**Phase 4-P1 Design Ready**（Phase 1 Runtime + Phase 2 Asset Provider + Phase 3 Evaluation + Phase 4-P0 Candidate Sedimentation 已落地并冻结；Phase 4-P1 候选治理与安全加固设计已完成）

## 项目定位

ClinMindRuntime 是一个面向临床 AI 系统的 **Java/Spring Boot 运行时**，核心思路：

- **Runtime 主控**：问诊流程、安全门、患者/医生双端输出边界由代码控制
- **Asset Provider**：症状群知识、红旗规则、CapabilityProfile 等以 YAML 资产包注入
- **Evaluation 闭环**：标准病例集 → Runtime 执行 → Scorer 评分 → 聚合报告 → CapabilityProfile 更新建议
- **Candidate 沉淀**：从 Evaluation 暴露的问题中生成可追踪、可审核、不可自动生效的经验候选与训练数据候选
- **Candidate 治理**：通过脱敏、来源校验和 review 记录，让候选更安全、更可治理，但仍不自动生效

与「检索 + 大模型直接回答」的区别：Runtime **不绕过**结构化模块做最终临床判断；Evaluation **不绕过** Runtime 直接评输出文本；Candidate **不自动** 上线经验或进入训练集。

## 当前已实现 / 已设计

| 阶段 | 能力 | 状态 |
|------|------|------|
| Phase 1 | 患者/医生 Runtime、SafetyGate、Trace、DecisionBoundary | 已完成 |
| Phase 2 | 资产包 `phase2-default`、Provider 接口、debug `assets-used` | 已完成 |
| Phase 3 | YAML 病例集、`RuntimeEvaluationRunner`、7 个 Scorer、EvaluationResult 聚合、CapabilityProfile Proposal、debug Evaluation API | 已冻结 |
| Phase 4-P0 | ExperienceCandidate / TrainingExampleCandidate 候选沉淀机制、Candidate debug API | 已冻结 |
| Phase 4-P1 | CandidateSanitizer、SourceRef 强校验、Candidate review 记录 | 设计完成，准备实现 P1-A |

Phase 3-P0 冻结记录见 [`docs/Phase3_P0冻结记录.md`](docs/Phase3_P0冻结记录.md)。Phase 4-P0 冻结记录见 [`docs/Phase4_P0冻结记录.md`](docs/Phase4_P0冻结记录.md)。Phase 4-P1 设计见 [`docs/Phase4_P1候选治理与安全加固_实现规格.md`](docs/Phase4_P1候选治理与安全加固_实现规格.md)。

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
- 不自动上线 ExperienceCandidate
- 不自动把 TrainingExampleCandidate 进入训练集
- 不把 Candidate review 当作正式临床审核
- 无数据库持久化、无前端后台、无权限系统
- 无 MCP / LangGraph / 完整 RAG 平台

## 文档入口

| 文档 | 说明 |
|------|------|
| [`docs/README.md`](docs/README.md) | 文档导航 |
| [`docs/项目展示导读.md`](docs/项目展示导读.md) | 面试/展示用精简导读 |
| [`docs/Phase3_P0冻结记录.md`](docs/Phase3_P0冻结记录.md) | Phase 3-P0 冻结依据 |
| [`docs/Phase4_P0冻结记录.md`](docs/Phase4_P0冻结记录.md) | Phase 4-P0 冻结依据 |
| [`docs/Phase4_P1候选治理与安全加固_实现规格.md`](docs/Phase4_P1候选治理与安全加固_实现规格.md) | Phase 4-P1 总体规格 |
| [`docs/Phase4_P1开发任务清单.md`](docs/Phase4_P1开发任务清单.md) | Phase 4-P1 实现顺序 |
| [`docs/ClinMindRuntime完整系统设计.md`](docs/ClinMindRuntime完整系统设计.md) | 系统总设计 |
| [`docs/ClinMindRuntime阶段拆分路线图.md`](docs/ClinMindRuntime阶段拆分路线图.md) | Phase 1–5 路线 |
| [`docs/AI_IMPLEMENTATION_SKILL.md`](docs/AI_IMPLEMENTATION_SKILL.md) | AI 实现约束（给 Cursor/Agent） |

## 下一阶段

**Phase 4-P1-A**：CandidateSanitizer 与脱敏策略。

只应实现 `CandidateSanitizationPolicy`、`CandidateSanitizationResult`、`CandidateSanitizer` 以及 `TrainingExampleCandidateGenerator` 接入脱敏层，不应跳到 ReviewService、Review API、数据库、前端或模型训练。

## License

Internal prototype — 见仓库根目录许可说明（如有）。