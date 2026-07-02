# AI Implementation Skill：ClinMindRuntime（Phase 8-P0 可实现但受限）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已冻结；Phase 7-P0 RAG EvidenceProvider MVP 已冻结；Phase 7-P1 KG-lite / Graph Evidence 原型已冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 8-P0 三份实现级文档已经建立，可以按任务清单进入 **Python AI Provider / EmbeddingProvider MVP** 的受限代码实现。

---

# 一、当前项目阶段

```text
当前阶段：Phase 8-P0 可实现但受限
前置状态：Phase 1–7 P1 已冻结
当前实现目标：Python AI Provider / EmbeddingProvider MVP
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP，已完成
Phase 2-P0：共享能力资产原型，已完成
Phase 3-P0：训练与评估闭环 MVP，已冻结
Phase 4-P0：候选沉淀机制 + debug API，已冻结
Phase 4-P1：候选治理与安全加固，已冻结
Phase 5-P0：持久化与治理底座，已冻结
Phase 5-P1：最小 Console 与访问治理，已冻结
Phase 5-P2：最小前端 Console MVP，已冻结
Phase 6-P0：受控 Agent 执行层 MVP，已冻结
Phase 7-P0：RAG EvidenceProvider MVP，已冻结
Phase 7-P1：KG-lite / Graph Evidence 原型，已冻结
Phase 8-P0：Python AI Provider / EmbeddingProvider MVP，可按文档受限实现
```

当前项目权威定位：

```text
ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台。

不是普通 RAG 医疗问答。
不是自由自治式医疗 Agent。
不是模型直接回答患者。
不是多 Agent Demo。
不是完整 GraphRAG 产品。
不是 Python 主控系统。
```

当前统一主链路：

```text
用户 / 医生输入
→ Runtime API
→ RuntimeService
→ EntryAssessment
→ RuntimeState / CaseFrame
→ KnowledgeContext / ExperienceContext
→ SafetyGate
→ Capability Orchestration
→ Agent / RAG / Graph / Model / Tool 受控调用
→ Runtime Validation / Provider Validation
→ Runtime 采纳 / 部分采纳 / 拒绝 / 降级
→ DDx Board / EvidenceGraph / QuestionPolicy
→ DecisionBoundary
→ PatientOutput / ClinicianReport
→ RuntimeTrace / AuditLog
→ Evaluation
→ Candidate / TrainingExample / CapabilityProfile Proposal
→ Review / Governance
→ 通过评估后进入下一轮 Runtime 可用能力
```

---

# 二、权威文档优先级

AI 实现或修改本仓库时，必须优先参考以下文档，优先级从高到低：

```text
1. docs/0-项目入口/00_项目设计地图.md
2. docs/1-总设计/ClinMindRuntime完整系统设计.md
3. docs/README.md
4. docs/2-专项设计/Python_AIProvider接入规划.md
5. docs/3-phase实现/Phase8_P0Python_AIProvider_实现规格.md
6. docs/3-phase实现/Phase8_P0Provider_API与测试设计.md
7. docs/3-phase实现/Phase8_P0开发任务清单.md
8. docs/3-phase实现/Phase7_P1冻结记录.md
9. docs/3-phase实现/Phase7_P0冻结记录.md
10. docs/3-phase实现/Phase6_P0冻结记录.md
11. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
12. docs/1-总设计/ClinMindRuntime技术实现总方案.md
```

解释：

```text
docs/0-项目入口/00_项目设计地图.md 是文档体系总入口。
docs/1-总设计/ClinMindRuntime完整系统设计.md 是系统级权威总设计。
docs/2-专项设计/Python_AIProvider接入规划.md 是 Phase 8 的跨语言协同前置规划。
Phase 8-P0 三份文档是当前代码实现的直接依据。
docs/3-phase实现/Phase 1–7 P1 冻结记录是已完成能力的边界依据。
```

---

# 三、当前允许做的事情

当前允许：

```text
1. 按 Phase8_P0开发任务清单实现 Python AI Provider / EmbeddingProvider MVP。
2. 新增 python-provider FastAPI 工程。
3. 新增 Python /health、/v1/providers、/v1/embeddings、/v1/rerank。
4. 新增 Java provider domain 对象。
5. 新增 Java PythonProviderClient。
6. 新增 ProviderValidationService。
7. 新增 Provider Debug API。
8. 将 rerank / embedding 结果受控接入 Evidence Retrieval。
9. 新增 ProviderTrace / Audit 接入。
10. 新增 Provider Evaluation Scorer。
11. 已冻结阶段的 bug fix、测试补强、文档修正。
```

---

# 四、当前禁止做的事情

```text
1. 不向 Phase 1–7 P1 已冻结阶段继续堆新能力。
2. 不让 Python 成为 Runtime 主控。
3. 不创建 Python Agent 自主循环。
4. 不让 Python 直接输出 PatientOutput。
5. 不让 Python 直接判断最终诊断。
6. 不让 Python 直接修改 RuntimeState。
7. 不让 Python 直接决定 SafetyGate / DecisionBoundary。
8. 不把 LLM 生成内容直接返回患者。
9. 不跳过 ProviderValidation。
10. 不跳过 Java Runtime fallback。
11. 不做 LoRA / DPO / RLHF / RFT 正式训练流水线。
12. 不做完整 ModelRegistry / PromptRegistry / MLOps。
13. 不引入复杂向量数据库作为 P0 主线。
14. 不提前接入 MCP / Tool / Skills。
15. 不新增正式登录 / OAuth / 多租户 / 生产级 RBAC。
16. 不实现正式医生审核平台。
17. 不自动上线 ExperienceCandidate。
18. 不自动发布 TrainingDatasetVersion。
19. 不自动扩大 CapabilityProfile 输出权限。
20. 不改写 Phase 1–7 P1 冻结记录中的历史事实。
```

---

# 五、已冻结能力边界（勿再扩展旧 Phase）

```text
Phase 6-P0：InquiryPlanningAgent / AgentRuntime / AgentProposalValidator 已冻结。
Phase 7-P0：RagEvidenceProvider / EvidenceRef / EvidenceCandidate / EvidenceValidation 已冻结。
Phase 7-P1：KG-lite / GraphEvidenceProvider / GraphEvidenceValidation 已冻结。
```

这些能力只能被 Phase 8-P0 复用，不能继续向旧 Phase 堆新能力。

---

# 六、Phase 8-P0 实现边界

Phase 8-P0 当前直接依据：

```text
docs/3-phase实现/Phase8_P0Python_AIProvider_实现规格.md
docs/3-phase实现/Phase8_P0Provider_API与测试设计.md
docs/3-phase实现/Phase8_P0开发任务清单.md
```

Phase 8-P0 核心目标：

```text
证明 Python 可以作为 Java Runtime 授权下的 AI Provider，
提供 embedding / rerank 等结构化模型能力，
并进入 ProviderValidation、Trace、Audit、Evaluation 闭环，
不能接管 Runtime，也不能直接回答患者。
```

Phase 8-P0 可以涉及的对象：

```text
python-provider
PythonProviderClient
ProviderInvocationRequest
ProviderInvocationResult
ProviderStatus
ProviderTrace
ProviderValidationResult
ProviderValidationService
EmbeddingRequest
EmbeddingResult
RerankRequest
RerankResult
ProviderDebugController
ProviderTraceCompletenessScorer
ProviderSchemaValidationScorer
ProviderFallbackSafetyScorer
```

Python AI Provider 只能输出：

```text
EmbeddingResult
RerankResult
ProviderResult
ProviderTrace
ProviderWarning
```

Python AI Provider 不能输出：

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

```text
1. P8-A：Python provider 工程骨架。
2. P8-B：/health 与 /v1/providers。
3. P8-C：EmbeddingProvider MVP。
4. P8-D：RerankerProvider MVP。
5. P8-E：Java provider domain 对象。
6. P8-F：PythonProviderClient。
7. P8-G：ProviderValidationService。
8. P8-I：Provider Debug API。
9. P8-H：Evidence Retrieval 接入 rerank / embedding。
10. P8-J：Trace / Audit。
11. P8-K：Evaluation Scorer。
12. P8-L：测试、人工验证、冻结记录。
```

不得跳过文档任务清单直接做 Python Agent、LLM 医疗问答、训练流水线或 ModelRegistry。

---

# 八、测试要求

Phase 8-P0 实现完成前必须至少覆盖：

```text
Python pytest：health / providers / embeddings / rerank / error response
Java：PythonProviderClientTest
Java：ProviderValidationServiceTest
Java：ProviderFallbackTest
Java：ProviderDebugControllerTest
Java：ProviderEnhancedEvidenceRetrievalTest
Java：ProviderTraceAuditTest
Java：Provider Evaluation Scorer tests
```

并保持：

```text
mvn test 通过。
python-provider pytest 通过。
Phase 1–7 P1 既有 Runtime / Agent / Evidence / Graph / Evaluation / Candidate / Persistence / Console 测试不回归。
console-web npm run test / npm run build 不回归；如仍存在既有 flake，必须明确与 Phase 8-P0 无直接关联。
```

---

# 九、冻结要求

Phase 8-P0 完成后必须新增：

```text
docs/3-phase实现/Phase8_P0人工测试结果.md
docs/3-phase实现/Phase8_P0冻结记录.md
```

冻结记录必须说明：

```text
1. 已实现哪些 Python Provider 能力。
2. Java ProviderClient / ProviderValidation 的能力边界。
3. ProviderResult 如何进入 Evidence Retrieval。
4. Python 失败时如何 fallback。
5. PatientOutput 如何保持边界。
6. API 与测试结果。
7. 人工测试结果。
8. 后置到 Phase 8-P1 / Phase 10 的任务。
```

---

# 十、最终结论

当前 AI 实现约束是：

```text
可以进入 Phase 8-P0 的 Python AI Provider / EmbeddingProvider MVP 实现；
只能按三份 Phase 8-P0 文档受限实现；
不可以让 Python 接管 Runtime；
不可以让 Python Agent 自主循环；
不可以让 Python 直接回答患者；
不可以让 ProviderResult 绕过 ProviderValidation、Runtime Validation、Trace、Audit 和 Evaluation。
```
