# ClinMindRuntime

受控医疗 AI Agent Runtime 与能力治理平台：以 Runtime 为主控，将 Agent / RAG / Model / Tool 作为受控能力单元接入统一主链路，并通过 Evaluation / Candidate / Audit / Governance 形成可追踪、可评估、可回滚的医疗 AI 能力闭环。

当前版本：**Phase 11-P1 收口完成度待验证，尚未冻结**（Runtime-backed Role View 已补齐；Java/Python/TypeScript/build 已通过，Vitest 与浏览器人工验证仍阻塞）。

当前总设计：[`docs/1-总设计/ClinMindRuntime完整系统设计.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E5%AE%8C%E6%95%B4%E7%B3%BB%E7%BB%9F%E8%AE%BE%E8%AE%A1.md) 已升级为 **v2.2**，项目定位为：

```text
受控医疗 AI Agent Runtime 与能力治理平台
```

文档体系入口：[`docs/0-项目入口/00_项目设计地图.md`](docs/0-%E9%A1%B9%E7%9B%AE%E5%85%A5%E5%8F%A3/00_%E9%A1%B9%E7%9B%AE%E8%AE%BE%E8%AE%A1%E5%9C%B0%E5%9B%BE.md)

---

## 项目定位

ClinMindRuntime 不是普通 RAG 医疗问答，也不是自由自治式医疗 Agent。

它的核心思路是：

- **Runtime 主控**：问诊流程、安全门、患者/医生双端输出边界由代码控制。
- **统一 Runtime 主链路**：所有能力都插入同一条 Runtime 主链路，而不是各自独立运行。
- **Capability Orchestration**：Runtime 根据当前病例状态决定是否调用 Agent / RAG / Model / Tool。
- **Agent 受控执行**：Agent 只能生成 Proposal / Draft / Candidate / Finding，不能直接修改 RuntimeState 或输出最终诊断。
- **RAG 证据化**：RAG / GraphRAG 只能返回 EvidenceCandidate，进入 EvidenceGraph 后再由 Runtime 判断作用。
- **Model Provider 化**：模型能力只能作为可评估、可替换、可回滚的 Provider 接入。
- **Tool / MCP / Skills 最小权限**：外部工具调用必须经过 ToolInvocationPolicy、ToolResultValidationService 和 Runtime Validation。
- **Evaluation 闭环**：标准病例集 → Runtime 执行 → Scorer 评分 → 聚合报告 → CapabilityProfile 更新建议。
- **Candidate 治理**：从 Evaluation 暴露的问题中生成可追踪、可审核、不可自动生效的经验候选与训练数据候选。
- **Persistence / Audit / Console 治理底座**：通过 PostgreSQL、Repository 双实现、AuditLog、Safe DTO 和最小 Console，让治理对象可持久化、可审计、可复盘。

与“检索 + 大模型直接回答”的区别：

```text
Runtime 不绕过结构化模块做最终临床判断。
Agent 不绕过 Runtime 修改状态或输出诊断。
RAG 不绕过 EvidenceGraph 和 DecisionBoundary 直接回答患者。
Model 不绕过 Evaluation / CapabilityProfile 直接上线。
Tool / MCP 不绕过 Runtime Validation 写入系统状态。
Candidate 不自动上线经验或进入训练集。
Console 不暴露敏感原文和未脱敏候选输入。
```

---

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
| Phase 5-P2 | 最小前端 Console MVP（`console-web/`） | 已冻结 |
| Phase 6-P0 | 受控 Agent 执行层 MVP | 已冻结 |
| Phase 7-P0 | RAG EvidenceProvider MVP | 已冻结 |
| Phase 7-P1 | KG-lite / Graph Evidence 原型 | 已冻结 |
| Phase 8-P0 | Python AI Provider / EmbeddingProvider MVP | 已冻结 |
| Phase 8-P1 | ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP | 已冻结 |
| Phase 8-P2 | ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP | 已冻结 |
| Phase 9-P0 | Tool / MCP / Skills 受控接入 MVP | 已冻结 |

Phase 9-P0 冻结记录见 [`docs/3-phase实现/Phase9_P0冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase9_P0%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md)。Phase 8-P2 冻结记录见 [`docs/3-phase实现/Phase8_P2冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase8_P2%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md)。

当前尚未完整实现：

```text
真实远程 MCP Adapter
Skills 文件系统与 Skill Store
Tool Console / Skill Console
Multi-Agent / Handoffs
生产级登录 / 多租户 / RBAC
正式医生审核平台
```

---

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

开发服务器默认 `http://localhost:5173`，侧边栏 **Debug Context** 可配置 Token / Actor / Roles 并测试连接；`/api` 代理至后端 `8080`。

前端测试：

```powershell
cd console-web
npm run test
npm run build
```

---

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

---

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

---

## 文档入口

| 文档 | 说明 |
|------|------|
| [`docs/0-项目入口/00_项目设计地图.md`](docs/0-%E9%A1%B9%E7%9B%AE%E5%85%A5%E5%8F%A3/00_%E9%A1%B9%E7%9B%AE%E8%AE%BE%E8%AE%A1%E5%9C%B0%E5%9B%BE.md) | 文档体系总入口，说明总设计、专项设计、Phase 文档和实现约束之间的关系 |
| [`docs/README.md`](docs/README.md) | docs 目录导航 |
| [`docs/1-总设计/ClinMindRuntime完整系统设计.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E5%AE%8C%E6%95%B4%E7%B3%BB%E7%BB%9F%E8%AE%BE%E8%AE%A1.md) | v2.2 权威总设计：八个能力域、五层架构、统一 Runtime 主链路 |
| [`docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`](docs/4-%E5%AE%9E%E7%8E%B0%E7%BA%A6%E6%9D%9F/AI_IMPLEMENTATION_SKILL.md) | AI 实现约束（给 Cursor / Agent / Claude Code / Codex） |
| [`docs/0-项目入口/项目展示导读.md`](docs/0-%E9%A1%B9%E7%9B%AE%E5%85%A5%E5%8F%A3/%E9%A1%B9%E7%9B%AE%E5%B1%95%E7%A4%BA%E5%AF%BC%E8%AF%BB.md) | 面试 / 展示用精简导读 |
| [`docs/3-phase实现/Phase5冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase5%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md) | Phase 5 全阶段冻结依据 |
| [`docs/3-phase实现/Phase8_P1冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase8_P1%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md) | Phase 8-P1 冻结依据 |
| [`docs/3-phase实现/Phase8_P1人工测试结果.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase8_P1%E4%BA%BA%E5%B7%A5%E6%B5%8B%E8%AF%95%E7%BB%93%E6%9E%9C.md) | Phase 8-P1 验收记录 |
| [`docs/3-phase实现/Phase8_P2冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase8_P2%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md) | Phase 8-P2 冻结依据 |
| [`docs/3-phase实现/Phase8_P2人工测试结果.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase8_P2%E4%BA%BA%E5%B7%A5%E6%B5%8B%E8%AF%95%E7%BB%93%E6%9E%9C.md) | Phase 8-P2 验收记录 |
| [`docs/3-phase实现/Phase9_P0冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase9_P0%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md) | Phase 9-P0 冻结依据 |
| [`docs/3-phase实现/Phase9_P0人工测试结果.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase9_P0%E4%BA%BA%E5%B7%A5%E6%B5%8B%E8%AF%95%E7%BB%93%E6%9E%9C.md) | Phase 9-P0 验收记录 |
| [`docs/1-总设计/ClinMindRuntime阶段拆分路线图.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E9%98%B6%E6%AE%B5%E6%8B%86%E5%88%86%E8%B7%AF%E7%BA%BF%E5%9B%BE.md) | 阶段路线图：Phase 6/7/8/9/10 长期演进顺序 |
| [`docs/1-总设计/ClinMindRuntime技术实现总方案.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E6%8A%80%E6%9C%AF%E5%AE%9E%E7%8E%B0%E6%80%BB%E6%96%B9%E6%A1%88.md) | 技术实现总方案：统一 Runtime 主链路、Capability Orchestration、AgentExecutionLayer、Runtime Validation |

---

## 当前不做什么

- 不向 Phase 1–9 P0 已冻结阶段继续堆新能力。
- 不让模型能力接管 Runtime、SafetyGate 或 PatientOutput。
- 不训练基础大模型 / 不做 RLHF。
- 不自动修改生产资产包或 CapabilityProfile。
- 不自动上线 ExperienceCandidate。
- 不自动把 TrainingExampleCandidate 进入训练集。
- 不把 Candidate review 当作正式临床审核。
- 不做正式登录、OAuth、多租户。
- 不提前接真实远程 MCP、Browser Agent / Computer Use / RPA 或高风险写工具。
- 不让 Agent / RAG / Model / Tool 绕过 Runtime Validation 和 DecisionBoundary。

---

## 下一阶段

**Phase 9-P0 已冻结。** 当前建议下一阶段进入：

```text
Phase 9-P1：真实 MCP adapter 设计
或 Phase 9-P2：Skills 文件系统与 Skill Store
或 Phase 10：Tool Console / Skill Console / Provider Console
```

进入下一阶段前，应先完成：

```text
1. 新增对应 Phase 的实现规格。
2. 新增对应 API 与测试设计。
3. 新增对应开发任务清单。
4. 更新 docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md，明确新阶段允许范围和禁止边界。
```

不应在未立项的新 Phase 中破坏 Phase 1–9 P0 已冻结的 Runtime 主控、ProviderCapabilityPolicy、ProviderValidation、Model Governance、Tool Governance、Safe DTO 与治理边界。

---

## License

Internal prototype — 见仓库根目录许可说明（如有）。

## Phase 11-P1 当前状态（2026-07-14）

Patient / Clinician 已采用 RuntimeStore 主路径与显式 seed fallback；PARTIAL、FALLBACK、policy、sanitizer 和 audit 边界已实现。当前冻结状态为 **BLOCKED / NOT FROZEN**，详见：

- docs/3-phase实现/Phase11_P1人工测试结果.md
- docs/3-phase实现/Phase11_P1冻结记录.md

在 Vitest 全量测试和浏览器人工验证完成前，不得宣称 Phase 11-P1 已冻结。