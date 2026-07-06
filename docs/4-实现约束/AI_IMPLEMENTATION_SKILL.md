# AI Implementation Skill：ClinMindRuntime（Phase 8-P2 可实现但受限）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结；Phase 8-P0 Python AI Provider / EmbeddingProvider MVP 已冻结；Phase 8-P1 ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP 已冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 8-P2 三份实现级文档已经建立，可以按任务清单进入 **ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP** 的受限代码实现。

---

# 一、当前项目阶段

| 项 | 内容 |
|---|---|
| 当前阶段 | Phase 8-P2 可实现但受限 |
| 前置状态 | Phase 1–8 P1 已冻结 |
| 前置冻结基线 | Phase 8-P1 commit `01409b2` |
| 当前实现目标 | ModelRegistry / PromptRegistry / TrainingDatasetVersion / ModelExperimentRecord MVP |

已完成主线：

- Phase 1-P0 ~ Phase 7-P1：已冻结
- Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，已冻结
- Phase 8-P1：ModelProvider / JudgeProvider / ProviderCapabilityProfile MVP，已冻结
- Phase 8-P2：设计文档已建立，可受限实现

当前项目权威定位：

- ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台
- 不是普通 RAG 医疗问答、不是自由自治式 Agent、不是 Python 主控系统、不是模型直接诊断系统、不是自动训练/自动发布平台

当前统一主链路：

`用户输入 → Runtime API → SafetyGate → Agent / RAG / Graph / Python Provider → Policy / Validation → DDx / EvidenceGraph → PatientOutput / ClinicianReport → Trace / Audit → Evaluation → Candidate / Governance`

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档（从高到低）：

1. `docs/0-项目入口/00_项目设计地图.md`
2. `docs/1-总设计/ClinMindRuntime完整系统设计.md`
3. `docs/README.md`
4. `docs/3-phase实现/Phase8_P2ModelRegistry与PromptRegistry_实现规格.md`
5. `docs/3-phase实现/Phase8_P2ModelGovernance_API与测试设计.md`
6. `docs/3-phase实现/Phase8_P2开发任务清单.md`
7. `docs/3-phase实现/Phase8_P1冻结记录.md`
8. `docs/3-phase实现/Phase8_P1人工测试结果.md`
9. `docs/3-phase实现/Phase8_P0冻结记录.md`
10. `docs/2-专项设计/Python_AIProvider接入规划.md`
11. Phase 1–7 各阶段冻结记录

---

# 三、当前允许做的事情

1. 按 Phase8_P2 开发任务清单实现 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP。
2. 新增 model governance domain 对象：ModelRegistryEntry、PromptRegistryEntry、TrainingDatasetVersion、ModelExperimentRecord、ModelEvaluationReport、ModelReleaseCandidate、ModelRollbackPlan。
3. 新增 model governance policy：ModelRegistryPolicy、PromptRegistryPolicy、TrainingDatasetVersionPolicy、ModelExperimentPolicy、ModelReleasePolicy。
4. 新增 ModelGovernanceService 与 in-memory store / repository。
5. 新增 Model Governance Debug / Governance API。
6. 新增 Model Governance Evaluation Scorer。
7. 扩展 CandidateMappingPolicy，将模型治理缺口沉淀为 review-required candidate。
8. 新增 Trace / Audit 记录。
9. 已冻结阶段的 bug fix、测试补强、文档修正。

---

# 四、当前禁止做的事情

1. 向 Phase 1–8 P1 已冻结阶段继续堆新能力（除非走 bug fix）。
2. 不做真实模型训练流水线。
3. 不做 LoRA / DPO / RLHF / RFT / distillation。
4. 不自动发布 TrainingDatasetVersion。
5. 不自动上线 ModelVersion。
6. 不自动发布 PromptVersion。
7. 不自动切换 ProviderCapabilityProfile。
8. 不接真实外部云模型作为强依赖主线。
9. 不做完整 MLOps / 自动灰度 / 自动回滚。
10. 不保存未脱敏患者原文、完整患者对话或未经权限控制的完整 prompt 原文。
11. 不让 ModelRegistry / PromptRegistry / TrainingDatasetVersion 接管 Runtime 主控。
12. 不绕过 ProviderCapabilityPolicy、ProviderValidation、SafetyGate、DecisionBoundary、Trace、Audit、Evaluation 或 Candidate Governance。
13. 不改写历史冻结记录中的事实。

---

# 五、已冻结能力边界（勿再扩展）

| Phase | 边界 |
|---|---|
| Phase 6-P0 | InquiryPlanningAgent / AgentRuntime / AgentProposalValidator |
| Phase 7-P0 | RagEvidenceProvider / EvidenceValidation |
| Phase 7-P1 | KG-lite / GraphEvidenceProvider |
| Phase 8-P0 | `python-provider`、PythonProviderClient、ProviderValidation、Evidence rerank 增强、Provider Debug API |
| Phase 8-P1 | ProviderCapabilityProfile、ProviderCapabilityPolicy、JudgeProvider、RiskSignalClassifierProvider、Judge / Risk / Profile Validation、Debug API、Evaluation Scorer、ProviderGovernanceSnapshot、Candidate 映射 |

Phase 8-P0 / P1 只能被 Phase 8-P2 复用，不能继续向已冻结范围堆新能力。

---

# 六、Phase 8-P2 实现边界

Phase 8-P2 当前直接依据：

```text
docs/3-phase实现/Phase8_P2ModelRegistry与PromptRegistry_实现规格.md
docs/3-phase实现/Phase8_P2ModelGovernance_API与测试设计.md
docs/3-phase实现/Phase8_P2开发任务清单.md
```

Phase 8-P2 核心目标：

```text
证明模型、prompt、数据集、实验、评估报告和发布候选都可以被最小治理化；
任何训练、发布、上线或能力替换都必须进入 review-required；
Runtime 主控、ProviderValidation、安全边界和 Candidate governance 不被破坏。
```

Phase 8-P2 可以涉及：

```text
ModelRegistryEntry
PromptRegistryEntry
TrainingDatasetVersion
ModelExperimentRecord
ModelEvaluationReport
ModelReleaseCandidate
ModelRollbackPlan
ModelRegistryPolicy
PromptRegistryPolicy
TrainingDatasetVersionPolicy
ModelExperimentPolicy
ModelReleasePolicy
ModelGovernanceService
ModelGovernanceDebugController
Model Governance Evaluation Scorer
Model Governance Candidate Mapping
```

Phase 8-P2 只能输出或创建：

```text
ModelRegistryEntry
PromptRegistryEntry
TrainingDatasetVersion
ModelExperimentRecord
ModelEvaluationReport
ModelReleaseCandidate(review-required)
ModelRollbackPlan
ModelGovernanceTrace
ModelGovernanceAuditRecord
ReviewRequiredCandidate
```

Phase 8-P2 不能输出或触发：

```text
Auto Published ModelVersion
Auto Published PromptVersion
Auto Published TrainingDatasetVersion
RuntimeState Decision
SafetyGate Decision
DecisionBoundary Decision
PatientOutput
Final Diagnosis
Final Treatment Advice
Automatic Model Deployment
Automatic Rollback
```

---

# 七、实现顺序约束

必须优先按任务清单顺序推进：

1. P8P2-A：model governance domain。
2. P8P2-B：in-memory store。
3. P8P2-C：Model / Prompt policy。
4. P8P2-D：Dataset policy。
5. P8P2-E：Experiment / Report / Release policy。
6. P8P2-F：ModelGovernanceService。
7. P8P2-G：Debug / Governance API。
8. P8P2-H：Trace / Audit。
9. P8P2-I：Evaluation Scorer。
10. P8P2-J：Candidate Mapping。
11. P8P2-K：测试、人工验证、冻结记录。

不得跳过文档任务清单直接做真实训练、真实模型发布、Model Console 或生产级 MLOps。

---

# 八、测试要求

Phase 8-P2 实现完成前必须至少覆盖：

- Java：ModelRegistryPolicyTest
- Java：PromptRegistryPolicyTest
- Java：TrainingDatasetVersionPolicyTest
- Java：ModelReleasePolicyTest
- Java：ModelGovernanceDebugControllerTest
- Java：ModelGovernanceScorerTest
- Java：ModelGovernanceCandidateMappingTest

并保持：

- `mvn test` 通过
- 如修改 `python-provider`，`python-provider` pytest 通过
- Phase 1–8 P1 既有 Runtime / Agent / Evidence / Graph / Provider / Evaluation / Candidate / Persistence / Console 测试不回归
- 涉及前端改动时，`console-web npm run test && npm run build` 通过

---

# 九、冻结要求

Phase 8-P2 完成后必须新增：

```text
docs/3-phase实现/Phase8_P2人工测试结果.md
docs/3-phase实现/Phase8_P2冻结记录.md
```

冻结记录必须说明：

1. 已实现哪些 ModelRegistry / PromptRegistry / DatasetVersion / Experiment / ReleaseCandidate 能力。
2. 各类 Policy 的拒绝边界。
3. TrainingDatasetVersion 如何保证不自动发布、不保存 raw patient dialogue。
4. ModelReleaseCandidate 如何保持 review-required 与 auto_publish=false。
5. Evaluation Scorer 如何识别模型治理缺口。
6. Candidate Mapping 如何只沉淀待审核候选。
7. API 与测试结果。
8. 后置到 Phase 8-P3 / Phase 9 / Phase 10 的任务。

---

# 十、最终结论

当前 AI 实现约束是：

```text
可以进入 Phase 8-P2 的 ModelRegistry / PromptRegistry / TrainingDatasetVersion MVP 实现；
只能按三份 Phase 8-P2 文档受限实现；
不可以做真实训练、自动发布、自动上线或自动回滚；
不可以保存未脱敏患者原文或未经权限控制的完整 prompt；
不可以让模型治理注册层接管 Runtime；
所有结果必须继续遵守 Policy → Validation → Trace / Audit → Evaluation → Candidate Governance。
```
