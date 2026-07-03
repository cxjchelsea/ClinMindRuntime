# AI Implementation Skill：ClinMindRuntime（Phase 8-P0 已冻结）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结；**Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结**。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 8-P0 已冻结 |
| 前置状态 | Phase 1–7 P1 已冻结 |
| 代码基线 | commit `39f2435` |
| 下一方向 | Phase 8-P1 规划 / 实现（待专项文档与任务清单） |

已完成主线（节选）：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- **Phase 8-P0**：Python AI Provider / EmbeddingProvider MVP，已冻结

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider → Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase8_P0冻结记录.md`
5. `docs/3-phase实现/Phase8_P0Python_AIProvider_实现规格.md`
6. `docs/3-phase实现/Phase8_P0Provider_API与测试设计.md`
7. `docs/3-phase实现/Phase8_P0开发任务清单.md`
8. `docs/2-专项设计/Python_AIProvider接入规划.md`
9. Phase 1–7 各阶段冻结记录

---

# 三、当前允许做的事情

1. 已冻结阶段的 bug fix、测试补强、文档修正。
2. 按 Phase 8-P1 专项文档（待建立）推进 JudgeProvider / ModelProvider 等后置能力。
3. Console / 运维 / 部署类非核心 Runtime 增强（需有对应 phase 文档）。

---

# 四、当前禁止做的事情

1. 向 Phase 1–8 P0 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不让 Python 成为 Runtime 主控或 Agent 自主循环。
3. 不让 Python 直接输出 PatientOutput / 最终诊断 / 修改 RuntimeState。
4. 不跳过 ProviderValidation 与 Java Runtime fallback。
5. 不做 LoRA / DPO / 完整 ModelRegistry / 外部向量库主线（属 P1+）。
6. 不改写历史冻结记录中的事实。

---

# 五、已冻结能力边界（勿再扩展）

| Phase | 边界 |
|---|---|
| Phase 6-P0 | InquiryPlanningAgent / AgentRuntime / AgentProposalValidator |
| Phase 7-P0 | RagEvidenceProvider / EvidenceValidation |
| Phase 7-P1 | KG-lite / GraphEvidenceProvider |
| **Phase 8-P0** | `python-provider`、PythonProviderClient、ProviderValidation、Evidence rerank 增强、Provider Debug API |

Phase 8-P0 只能被后续 Phase **复用**，不能继续向 P0 范围堆新能力。

---

# 六、Phase 8-P0 冻结摘要

**Provider ID：** `python_ai_provider` v`0.8.0-p0`

**已交付：**

- Python FastAPI：`/health`、`/v1/providers`、`/v1/embeddings`、`/v1/rerank`
- Java：`HttpPythonProviderClient`、`ProviderValidationService`、`EvidenceRerankEnhancementService`
- Debug API：`/api/v1/debug/providers/**`
- Audit：`RUN_PYTHON_PROVIDER` / `QUERY_PYTHON_PROVIDER`
- Evaluation：`provider_eval` 门控 Scorer

**默认配置：** `clinmind.python-provider.enabled=false`（需显式开启才调用 Python）

**详细边界：** 见 `docs/3-phase实现/Phase8_P0冻结记录.md`

---

# 七、Phase 8-P0 验收清单（已完成）

- [x] P8-A ~ P8-L 全部任务完成
- [x] Python pytest 5/5 通过
- [x] Java `mvn test` 457 通过，0 失败
- [x] Java ↔ Python 联调（health / capabilities / rerank / embedding）
- [x] Python 不可用 fallback 验证
- [x] Evidence retrieval `PROVIDER_RERANK_APPLIED` 验证
- [x] PatientOutput 不泄露 provider 内部字段
- [x] `Phase8_P0人工测试结果.md` / `Phase8_P0冻结记录.md` 已编写

---

# 八、测试回归要求

修改已冻结代码后必须至少保证：

- [x] `python-provider` pytest 通过
- [x] `mvn test` 通过（JDK 17+，推荐 JDK 21）
- [ ] `console-web npm run test && npm run build`（涉及前端改动时）

---

# 九、最终结论

- Phase 8-P0 **已冻结**；Python 仅作为 Java 授权下的 AI Provider。
- 新实现必须继续遵守：ProviderValidation → Runtime 采纳/拒绝/降级 → Trace/Audit/Evaluation。
- 下一阶段进入 **Phase 8-P1** 前，需先建立实现规格与任务清单，不可跳过文档直接编码。
