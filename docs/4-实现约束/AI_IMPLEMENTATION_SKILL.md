# AI Implementation Skill：ClinMindRuntime（Phase 7-P0 可实现但受限）

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> Phase 1–5 均已落地并冻结；Phase 6-P0 受控 Agent 执行层 MVP 已于 2026-07-02 冻结。  
> 总设计 v2.2：受控医疗 AI Agent Runtime 与能力治理平台。  
> 本文件位于 `docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md`。  
> Phase 7-P0 三份实现级文档已经建立，可以按任务清单进入 **RAG EvidenceProvider MVP** 的受限代码实现。

---

# 一、当前项目阶段

```text
当前阶段：Phase 7-P0 可实现但受限
前置状态：Phase 1–6 P0 已冻结
当前实现目标：RAG EvidenceProvider MVP
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
Phase 7-P0：RAG EvidenceProvider MVP，可按文档受限实现
```

当前项目权威定位：

```text
ClinMindRuntime = 受控医疗 AI Agent Runtime 与能力治理平台。

不是普通 RAG 医疗问答。
不是自由自治式医疗 Agent。
不是模型直接回答患者。
不是多 Agent Demo。
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
→ Agent / RAG / Model / Tool 受控调用
→ Runtime Validation
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
4. docs/3-phase实现/Phase7_P0RAG_EvidenceProvider_实现规格.md
5. docs/3-phase实现/Phase7_P0RAG_API与测试设计.md
6. docs/3-phase实现/Phase7_P0开发任务清单.md
7. docs/3-phase实现/Phase6_P0冻结记录.md
8. docs/3-phase实现/Phase6_P0人工测试结果.md
9. docs/3-phase实现/Phase5冻结记录.md
10. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
11. docs/1-总设计/ClinMindRuntime技术实现总方案.md
12. docs/2-专项设计/医学知识库与RAG构建规划.md
```

解释：

```text
docs/0-项目入口/00_项目设计地图.md 是文档体系总入口。
docs/1-总设计/ClinMindRuntime完整系统设计.md 是系统级权威总设计。
Phase 7-P0 三份文档是当前代码实现的直接依据。
docs/3-phase实现/Phase 1–6 P0 冻结记录是已完成能力的边界依据。
docs/2-专项设计/ 下的专项规划文档不是直接编码依据，必须经过 Phase 实现规格转化。
```

---

# 三、当前允许做的事情

当前允许：

```text
1. 按 Phase7_P0开发任务清单实现 RAG EvidenceProvider MVP。
2. 新增 Evidence domain 基础对象。
3. 新增 Evidence corpus 与加载器。
4. 新增 EvidenceProviderPolicy。
5. 新增 EvidenceProvider 接口与 RagEvidenceProvider deterministic / keyword MVP。
6. 新增 EvidenceValidationService。
7. 新增 EvidenceGraph 集成。
8. 新增 Evidence Debug API。
9. 新增 Evidence Retrieval Trace / Audit 接入。
10. 新增 Evidence Evaluation Scorer。
11. 已冻结阶段的 bug fix、测试补强、文档修正。
```

---

# 四、当前禁止做的事情

```text
1. 不向 Phase 1–6 P0 已冻结阶段继续堆新能力。
2. 不实现自由自治式 Agent。
3. 不实现多 Agent 协作。
4. 不使用 LangGraph / Agent SDK 作为 Runtime 主控。
5. 不在 Phase 7-P0 做 GraphRAG。
6. 不在 Phase 7-P0 做 KG-lite node / edge 正式图谱。
7. 不提前接入 Python AI Provider。
8. 不提前接入 MCP / Tool / Skills。
9. 不提前做模型训练 / 后训练。
10. 不新增正式登录 / OAuth / 多租户 / 生产级 RBAC。
11. 不实现正式医生审核平台。
12. 不让 Agent / RAG / Model / Tool 直接输出 PatientOutput。
13. 不让任何外部能力直接修改 RuntimeState。
14. 不绕过 Runtime Validation。
15. 不绕过 SafetyGate。
16. 不绕过 DecisionBoundary。
17. 不自动上线 ExperienceCandidate。
18. 不自动发布 TrainingDatasetVersion。
19. 不自动扩大 CapabilityProfile 输出权限。
20. 不改写 Phase 1–6 P0 冻结记录中的历史事实。
21. 不把 RAG 检索结果直接拼 Prompt 生成患者端回答。
22. 不引入 Neo4j / Milvus / Qdrant 作为 P0 依赖。
```

---

# 五、Phase 6-P0 冻结边界（已完成，勿再扩展）

Phase 6-P0 冻结记录：

```text
docs/3-phase实现/Phase6_P0冻结记录.md
docs/3-phase实现/Phase6_P0人工测试结果.md
```

Phase 6-P0 已完成 Agent：

```text
InquiryPlanningAgent
```

Phase 6-P0 已完成能力只能被 Phase 7-P0 复用，不能继续向 P6-P0 堆新能力。

---

# 六、Phase 7-P0 实现边界

Phase 7-P0 当前直接依据：

```text
docs/3-phase实现/Phase7_P0RAG_EvidenceProvider_实现规格.md
docs/3-phase实现/Phase7_P0RAG_API与测试设计.md
docs/3-phase实现/Phase7_P0开发任务清单.md
```

Phase 7-P0 核心目标：

```text
证明 RAG 可以作为 Runtime 授权下的 EvidenceProvider，
只返回可追踪、可校验、可拒绝的 EvidenceCandidate / EvidenceRef，
并进入 EvidenceGraph、Trace、Audit、Evaluation，
不能直接回答患者。
```

Phase 7-P0 可以涉及的对象：

```text
EvidenceRef
EvidenceCandidate
EvidenceRetrievalRequest
EvidenceRetrievalResult
EvidenceProvider
RagEvidenceProvider
EvidenceProviderPolicy
EvidenceValidationService
EvidenceRetrievalTrace
EvidenceRetrievalSnapshot
EvidenceDebugController
EvidenceTraceCompletenessScorer
EvidenceSourceVersionScorer
EvidencePatientBoundaryScorer
EvidenceUseCaseSafetyScorer
```

RAG / EvidenceProvider 只能输出：

```text
EvidenceCandidate
EvidenceRef
EvidenceRetrievalResult
EvidenceRetrievalTrace
```

RAG / EvidenceProvider 不能输出：

```text
PatientOutput
Final Diagnosis
Final Treatment Advice
RuntimeState Decision
DecisionBoundary Decision
PublishedKnowledgeAsset
```

---

# 七、实现顺序约束

必须优先按任务清单顺序推进：

```text
1. P7-A：Evidence domain 基础对象。
2. P7-B：Evidence corpus 与加载器。
3. P7-C：EvidenceProviderPolicy。
4. P7-D：RagEvidenceProvider MVP。
5. P7-E：EvidenceValidationService。
6. P7-H：Evidence Debug API。
7. P7-F：EvidenceGraph 集成。
8. P7-G：Capability Orchestration 接入。
9. P7-I：Trace / Audit。
10. P7-J：Evaluation Scorer。
11. P7-K：测试、人工验证、冻结记录。
```

不得跳过文档任务清单直接做 GraphRAG、向量库或 LLM 生成。

---

# 八、测试要求

Phase 7-P0 实现完成前必须至少覆盖：

```text
EvidenceCorpusRepositoryTest
EvidenceProviderPolicyTest
RagEvidenceProviderTest
EvidenceValidationServiceTest
EvidenceGraphIntegrationTest
EvidenceDebugControllerTest
EvidenceTraceAuditTest
Evidence Evaluation Scorer tests
```

并保持：

```text
mvn test 通过。
Phase 1–6 P0 既有 Runtime / Agent / Evaluation / Candidate / Persistence / Console 测试不回归。
console-web npm run test / npm run build 不回归；如仍存在既有 flake，必须明确与 Phase 7 无直接关联。
```

---

# 九、冻结要求

Phase 7-P0 完成后必须新增：

```text
docs/3-phase实现/Phase7_P0人工测试结果.md
docs/3-phase实现/Phase7_P0冻结记录.md
```

冻结记录必须说明：

```text
1. 已实现哪些 EvidenceProvider 对象。
2. RagEvidenceProvider 的能力边界。
3. EvidenceCandidate 如何进入 EvidenceGraph。
4. PatientOutput 如何保持边界。
5. API 与测试结果。
6. 人工测试结果。
7. 后置到 Phase 7-P1 / Phase 8 / Phase 10 的任务。
```

---

# 十、最终结论

当前 AI 实现约束是：

```text
可以进入 Phase 7-P0 的 RAG EvidenceProvider MVP 实现；
只能按三份 Phase 7-P0 文档受限实现；
不可以让 RAG 直接回答患者；
不可以把检索结果拼 Prompt 生成 PatientOutput；
不可以引入 GraphRAG / 向量库 / LLM 作为 P0 主线；
不可以绕过 Runtime Validation、EvidenceGraph、SafetyGate、DecisionBoundary、Trace、Audit 和 Evaluation。
```
