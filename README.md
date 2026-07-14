# ClinMindRuntime

受控医疗 AI Agent Runtime 与能力治理平台。

ClinMindRuntime 以 Java Runtime 为唯一控制平面，将 Agent、Clinical Evidence、Model Provider、Tool / MCP / Skill 作为受控能力接入统一主链路，并通过 Safety、Validation、DecisionBoundary、Trace、Audit、Evaluation、Review 与 Release Governance 形成可追踪、可评估、可降级、可恢复和可回滚的医疗 AI 能力闭环。

## 当前状态

```text
Phase 1–11 P1：已完成并冻结
Phase 12-P0：Clinical Evidence Engine 设计评审期
Phase 12-P1：真实 LLM Agent + 只读 FHIR + 最小统一治理，尚未开始
Phase 12-P2：胸痛 / 胸闷临床纵切，尚未开始
```

当前总设计版本：**v3.0**。

Phase 12-P0 设计完成评审前，不进入正式代码实现。

---

## 项目定位

ClinMindRuntime 不是：

```text
普通医疗聊天机器人
普通文档 RAG
自由自治式 AI 医生
由 LLM 直接维护患者状态的 Agent
由模型直接执行处方、病历写入或高风险动作的系统
只依赖最终文本审核的 Guardrail Demo
```

它是：

```text
Runtime 主控
+ Clinical Data / Fact Plane
+ Clinical Evidence Engine
+ Controlled Agent / Workflow
+ Model Provider Lifecycle
+ Tool / MCP / Skills Governance
+ Unified Runtime Governance Kernel
+ Role-safe Patient / Clinician Projection
+ Evaluation / Audit / Continuous Improvement
+ Production Governance Platform
```

核心权力边界：

```text
Runtime
负责状态、控制流、能力授权、安全判断、结果提交、输出边界、恢复和审计。

Agent / Evidence / Model / Tool / MCP / Skill
只能生成 Proposal、Draft、Candidate、EvidenceRef 或 Structured Result。

Evaluation / Review / Governance
负责能力评估、发布授权、回滚、再认证和持续改进。
```

---

## 当前已冻结能力

| 阶段 | 能力 | 状态 |
|---|---|---|
| Phase 1 | RuntimeState、SafetyGate、DecisionBoundary、Trace 与角色输出 | FROZEN |
| Phase 2 | AssetPackage、Provider、版本与 CapabilityProfile | FROZEN |
| Phase 3 | Evaluation CaseSet、Runner、Scorer 与回归闭环 | FROZEN |
| Phase 4 | Candidate、Sanitization、SourceRef 与 Review | FROZEN |
| Phase 5 | PostgreSQL、Repository、Audit、Safe DTO 与 Console 基础 | FROZEN |
| Phase 6 | 受控 Agent 执行层 | FROZEN |
| Phase 7 | RAG EvidenceProvider 与 KG-lite / Graph Evidence 原型 | FROZEN |
| Phase 8 | Python Provider、Model / Prompt / Dataset Governance 原型 | FROZEN |
| Phase 9 | Tool / MCP / Skills 治理原型 | FROZEN |
| Phase 10 | Governance / Runtime Console MVP | FROZEN |
| Phase 11-P0 | Patient / Clinician / Governance 三角色前端 | FROZEN |
| Phase 11-P1 | Runtime-backed Role-specific View API / Frontend BFF | FROZEN |

Phase 11-P1 冻结证据：

- [`Phase11_P1人工测试结果.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase11_P1%E4%BA%BA%E5%B7%A5%E6%B5%8B%E8%AF%95%E7%BB%93%E6%9E%9C.md)
- [`Phase11_P1冻结记录.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase11_P1%E5%86%BB%E7%BB%93%E8%AE%B0%E5%BD%95.md)

---

## Phase 12 设计入口

Phase 12 用一个可评测临床纵切证明真实 Evidence、LLM Agent、FHIR 数据和 Runtime 治理可以协同，但仍不允许模型绕过 Runtime 做最终医疗决定。

推荐阅读：

1. [`Phase12真实临床能力纵切_总体设计.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase12%E7%9C%9F%E5%AE%9E%E4%B8%B4%E5%BA%8A%E8%83%BD%E5%8A%9B%E7%BA%B5%E5%88%87_%E6%80%BB%E4%BD%93%E8%AE%BE%E8%AE%A1.md)
2. [`Phase12_P0ClinicalEvidenceEngine_实现规格.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase12_P0ClinicalEvidenceEngine_%E5%AE%9E%E7%8E%B0%E8%A7%84%E6%A0%BC.md)
3. [`Phase12_P0EvidenceEngine_API与测试设计.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase12_P0EvidenceEngine_API%E4%B8%8E%E6%B5%8B%E8%AF%95%E8%AE%BE%E8%AE%A1.md)
4. [`Phase12_P0开发任务清单.md`](docs/3-phase%E5%AE%9E%E7%8E%B0/Phase12_P0%E5%BC%80%E5%8F%91%E4%BB%BB%E5%8A%A1%E6%B8%85%E5%8D%95.md)
5. [`AI_IMPLEMENTATION_SKILL.md`](docs/4-%E5%AE%9E%E7%8E%B0%E7%BA%A6%E6%9D%9F/AI_IMPLEMENTATION_SKILL.md)

Phase 12-P0 核心链路：

```text
Versioned Medical Source
→ Chunk / Span / Curated Claim
→ PostgreSQL Lexical Recall + Dense Recall
→ RRF + Cross-encoder Rerank
→ Authority / Freshness / Applicability
→ Citation Entailment / Conflict
→ EvidenceValidation
→ RuntimeEvidenceGraph Patch
→ Runtime Commit / DecisionBoundary
```

---

## 关键文档

| 文档 | 作用 |
|---|---|
| [`00_项目设计地图.md`](docs/0-%E9%A1%B9%E7%9B%AE%E5%85%A5%E5%8F%A3/00_%E9%A1%B9%E7%9B%AE%E8%AE%BE%E8%AE%A1%E5%9C%B0%E5%9B%BE.md) | 文档体系总入口 |
| [`ClinMindRuntime完整系统设计.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E5%AE%8C%E6%95%B4%E7%B3%BB%E7%BB%9F%E8%AE%BE%E8%AE%A1.md) | v3.0 权威系统设计 |
| [`ClinMindRuntime阶段拆分路线图.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E9%98%B6%E6%AE%B5%E6%8B%86%E5%88%86%E8%B7%AF%E7%BA%BF%E5%9B%BE.md) | Phase 0–22 实现路线 |
| [`ClinMindRuntime技术实现总方案.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/ClinMindRuntime%E6%8A%80%E6%9C%AF%E5%AE%9E%E7%8E%B0%E6%80%BB%E6%96%B9%E6%A1%88.md) | 模块、接口、API、存储、测试与部署蓝图 |
| [`Phase11后架构缺口与路线收敛决策.md`](docs/1-%E6%80%BB%E8%AE%BE%E8%AE%A1/Phase11%E5%90%8E%E6%9E%B6%E6%9E%84%E7%BC%BA%E5%8F%A3%E4%B8%8E%E8%B7%AF%E7%BA%BF%E6%94%B6%E6%95%9B%E5%86%B3%E7%AD%96.md) | Phase 11 后的优先级和边界决策 |
| [`docs/README.md`](docs/README.md) | docs 目录导航 |

---

## 快速启动

后端要求 Java 17+，推荐使用 JDK 21 与 Maven 3.9+。

```powershell
set JAVA_HOME=D:\cxj\software\jdk21
mvn test
mvn -DskipTests package
java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar
```

后端默认：`http://localhost:8080`

前端：

```powershell
cd console-web
npm install
npm run typecheck
npm test
npm run build
npm run dev
```

前端开发服务器默认：`http://localhost:5173`

---

## 运行 Runtime 示例

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

患者端输出必须遵守 DecisionBoundary，不得泄露 DDx、Raw Evidence、Trace、Prompt 或完整内部推理。

---

## 当前禁止事项

```text
不在设计评审完成前实现 Phase 12-P0 产品代码。
不并行启动 Phase 12-P1、P2 或 Phase 13–22。
不让 Evidence Engine、Agent、Model、Tool 绕过 Runtime Validation。
不导入真实患者或 PHI 数据。
不发布许可证未知的证据资产。
不让未验证 Citation 进入 accepted EvidenceGraph。
不提前接入外部写操作、生产认证或自由自治 Multi-Agent。
```

---

## License

Internal prototype — 见仓库根目录许可说明（如有）。