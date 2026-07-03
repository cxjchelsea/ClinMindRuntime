# AI Implementation Skill：ClinMindRuntime（Phase 8-P1 已冻结）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结；Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结；Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结。
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 8-P1 已完成实现、验收与冻结归档；后续只能进行 bug fix、测试补强、文档勘误，新增模型平台能力应进入 Phase 8-P2 或 Phase 10。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 8-P1 已冻结 |
| 前置状态 | Phase 1–8 P1 已冻结 |
| 冻结代码基线 | Phase 8-P1 commit `01409b2` |
| 后续方向 | Phase 8-P2 ModelRegistry / PromptRegistry / TrainingDatasetVersion，或 Phase 10 Provider Console |

已完成主线：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，已冻结
- Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP，已冻结

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统、不是模型直接诊断系统

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider → Policy / Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase8_P1ModelProvider与JudgeProvider_实现规格.md`
5. `docs/3-phase实现/Phase8_P1ProviderCapability_API与测试设计.md`
6. `docs/3-phase实现/Phase8_P1开发任务清单.md`
7. `docs/3-phase实现/Phase8_P1冻结记录.md`
8. `docs/3-phase实现/Phase8_P1人工测试结果.md`
9. `docs/3-phase实现/Phase8_P0冻结记录.md`
10. `docs/2-专项设计/Python_AIProvider接入规划.md`
11. Phase 1–7 各阶段冻结记录

---

# 三、当前允许做的事情

1. 已冻结阶段的 bug fix、测试补强、文档修正。
2. 在不改变 Phase 8-P1 边界的前提下补充测试 fixture、验收记录或说明。
3. 为 Phase 8-P2 / Phase 10 新建设计文档和任务清单。

---

# 四、当前禁止做的事情

1. 向 Phase 1–8 P1 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不让 Python 成为 Runtime 主控或 Agent 自主循环。
3. 不让 Python 直接输出 PatientOutput / 最终诊断 / 修改 RuntimeState。
4. 不让 JudgeProvider 直接决定最终诊断。
5. 不让 RiskSignalClassifierProvider 直接触发 SafetyGate。
6. 不跳过 ProviderCapabilityPolicy。
7. 不跳过 ProviderValidation 与 Java Runtime fallback。
8. 不把 judge rationale / risk internal score 直接展示给患者。
9. 不做真实 LLM / 外部云模型强依赖主线。
10. 不做 LoRA / DPO / RFT / 完整 ModelRegistry / PromptRegistry / MLOps。
11. 不自动发布 TrainingDatasetVersion。
12. 不改写历史冻结记录中的事实。

---

# 五、已冻结能力边界（勿再扩展）

| Phase | 边界 |
|---|---|
| Phase 6-P0 | InquiryPlanningAgent / AgentRuntime / AgentProposalValidator |
| Phase 7-P0 | RagEvidenceProvider / EvidenceValidation |
| Phase 7-P1 | KG-lite / GraphEvidenceProvider |
| Phase 8-P0 | `python-provider`、PythonProviderClient、ProviderValidation、Evidence rerank 增强、Provider Debug API |
| Phase 8-P1 | ProviderCapabilityProfile、ProviderCapabilityPolicy、JudgeProvider、RiskSignalClassifierProvider、Judge / Risk / Profile Validation、Debug API、Evaluation Scorer、ProviderGovernanceSnapshot、Candidate 映射 |

Phase 8-P0 / P1 只能被后续阶段复用，不能继续向已冻结范围堆新能力。

---

# 六、Phase 8-P1 冻结边界

Phase 8-P1 冻结归档依据：

```text
docs/3-phase实现/Phase8_P1ModelProvider与JudgeProvider_实现规格.md
docs/3-phase实现/Phase8_P1ProviderCapability_API与测试设计.md
docs/3-phase实现/Phase8_P1开发任务清单.md
docs/3-phase实现/Phase8_P1人工测试结果.md
docs/3-phase实现/Phase8_P1冻结记录.md
```

Phase 8-P1 核心目标：

```text
证明模型能力可以被画像、授权、调用、验证、追踪、审计和评估；
Judge / Classifier 只能提供辅助信号；
Runtime 主控、安全边界和治理闭环不能被破坏。
```

Phase 8-P1 可以涉及：

```text
ProviderCapabilityProfile
ProviderCapabilityPolicy
JudgeProvider
JudgeRequest
JudgeScoreResult
RiskSignalClassifierProvider
RiskSignalClassificationRequest
RiskSignalDraft
Judge / Risk / Profile Validation
Judge / Risk / Profile Debug API
Judge / Risk / Profile Evaluation Scorer
```

Judge / Risk / Profile 只能输出：

```text
JudgeScoreResult
RiskSignalDraft
ProviderCapabilityProfile
ProviderTrace
ProviderWarning
```

Judge / Risk / Profile 不能输出：

```text
PatientOutput
Final Diagnosis
Final Treatment Advice
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
ApprovedExperience
PublishedTrainingDataset
```

---

# 七、实现顺序约束

必须优先按任务清单顺序推进：

1. P8P1-A：Python 配置与 capability profile schema。
2. P8P1-B：`/v1/capability-profiles`。
3. P8P1-C：JudgeProvider MVP。
4. P8P1-D：RiskSignalClassifierProvider MVP。
5. P8P1-E：Java capability / judge / risk domain。
6. P8P1-F：ProviderCapabilityPolicy。
7. P8P1-G：PythonProviderClient 扩展。
8. P8P1-H：ProviderValidationService 扩展。
9. P8P1-I：Debug API 扩展。
10. P8P1-J：Evaluation Scorer。
11. P8P1-K：Trace / Audit / Candidate。
12. P8P1-L：测试、人工验证、冻结记录。

不得跳过文档任务清单直接做真实 LLM、模型训练流水线或 ModelRegistry。

---

# 八、测试要求

Phase 8-P1 冻结时已覆盖：

- Python pytest：capability profiles / judge / risk classifier / error response
- Java：ProviderCapabilityPolicyTest
- Java：ProviderCapabilityProfileValidationTest
- Java：JudgeProviderIntegrationTest
- Java：RiskClassifierIntegrationTest
- Java：ProviderDebugController P8-P1 tests
- Java：Judge / Risk / Profile Evaluation Scorer tests

并保持：

- `mvn test` 通过
- `python-provider` pytest 通过
- Phase 1–8 P0 既有 Runtime / Agent / Evidence / Graph / Provider / Evaluation / Candidate / Persistence / Console 测试不回归
- 涉及前端改动时，`console-web npm run test && npm run build` 通过

---

# 九、冻结要求

Phase 8-P1 冻结归档文件：

```text
docs/3-phase实现/Phase8_P1人工测试结果.md
docs/3-phase实现/Phase8_P1冻结记录.md
```

冻结记录必须说明：

1. 已实现哪些 Judge / Risk / Profile 能力。
2. ProviderCapabilityPolicy 的授权边界。
3. Judge 结果如何进入 Evaluation。
4. RiskSignalDraft 如何保持不直接触发 SafetyGate。
5. PatientOutput 如何保持边界。
6. API 与测试结果。
7. 后置到 Phase 8-P2 / Phase 10 的任务。

---

# 十、最终结论

当前 AI 实现约束是：

```text
Phase 8-P1 已冻结；
只能对 Phase 8-P1 做 bug fix、测试补强和文档勘误；
新增模型平台能力必须进入 Phase 8-P2 / Phase 10；
不可以让模型能力接管 Runtime；
不可以让 Judge 直接决定诊断；
不可以让 Risk classifier 直接触发 SafetyGate；
不可以让 ProviderResult 绕过 ProviderCapabilityPolicy、ProviderValidation、Trace、Audit 和 Evaluation。
```
