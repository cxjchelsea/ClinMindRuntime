# AI Implementation Skill：ClinMindRuntime Phase 3 Freeze

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1-P0 Runtime MVP、Phase 2-P0 共享能力资产原型、Phase 3-P0 训练与评估闭环 MVP 已完成。  
> 后续修改不得破坏 Runtime 主控、安全门、输出边界、Provider 抽象、资产版本追踪、Evaluation 闭环和患者端隔离。

---

# 一、当前项目阶段

```text
当前阶段：Phase 3-P0 freeze / cleanup / release polish
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP
Phase 2-P0：共享能力资产原型
Phase 3-P0：训练与评估闭环 MVP
```

Phase 3-P0 已完成的工程闭环：

```text
EvaluationCaseSet
→ RuntimeEvaluationRunner
→ RuntimeService.startRuntime / continueRuntime
→ RuntimeState / RuntimeTrace / API Response
→ EvaluationScorer
→ EvaluationItemResult
→ EvaluationResult
→ CapabilityProfileUpdateProposal
```

当前最优任务不是继续堆新功能，而是：

```text
1. 修正文档状态不一致。
2. 完成文档导航和项目展示导读。
3. 做 Phase3-P0 freeze 前小型质量清理。
4. 保持 Phase 1 / Phase 2 / Phase 3 回归测试通过。
5. 暂不进入 Phase 4。
```

重要说明：

```text
Phase 3 的“训练”不是训练基础大模型。
Phase 3 的训练含义是：用标准病例集和评估指标校准 Runtime 能力、资产质量和 CapabilityProfile 边界。
真实模型训练、后训练、Python AI Provider、RAG / GraphRAG、MCP、前端平台、数据库持久化等能力仍然后置。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/README.md
3. docs/项目展示导读.md
4. docs/Phase3_开发任务清单.md
5. docs/Phase3_人工测试API结果.md
6. docs/架构文档缺口审查清单.md
7. docs/ClinMindRuntime技术实现总方案.md
8. docs/测试与CI总方案.md
9. docs/Phase3_训练与评估闭环_实现规格.md
10. docs/Phase3_评估数据结构设计.md
11. docs/Phase3_病例集与考试流程设计.md
12. docs/Phase3_Runtime评估接入设计.md
13. docs/Phase3_CapabilityProfile更新机制设计.md
14. docs/Phase3_API与测试设计.md
15. docs/全局技术栈与架构选型.md
16. docs/AI前沿技术选型与接入规划.md
17. docs/模型训练与后训练规划.md
18. docs/医学知识库与RAG构建规划.md
19. docs/数据安全与合规边界规划.md
20. docs/数据库持久化设计.md
21. docs/平台前端与Console规划.md
22. docs/部署与运维规划.md
23. docs/Phase2_开发任务清单.md
24. docs/Phase2_共享能力资产原型_实现规格.md
25. docs/Phase2_Provider接口设计.md
26. docs/Phase2_资产数据结构与版本设计.md
27. docs/Phase2_Runtime接入改造设计.md
28. docs/Phase2_API与测试设计.md
29. docs/Phase1_技术栈与工程架构决策.md
30. docs/Phase1_Runtime_MVP_实现规格.md
31. docs/Phase1_数据结构与状态设计.md
32. docs/Phase1_模块接口设计.md
33. docs/Phase1_API与测试设计.md
34. docs/ClinMindRuntime阶段拆分路线图.md
35. docs/ClinMindRuntime完整系统设计.md
36. docs/架构模式与设计模式说明.md
```

解释：

```text
docs/README.md 是文档导航入口。
docs/项目展示导读.md 用于项目展示、面试说明和当前能力概览。
docs/Phase3_开发任务清单.md 是 Phase3-P0 已完成状态的依据。
docs/Phase3_人工测试API结果.md 是 Phase3-P0 人工验收依据。
docs/架构文档缺口审查清单.md 用于判断文档体系覆盖状态。
docs/ClinMindRuntime技术实现总方案.md 约束代码包结构、依赖方向、Runtime 主链路、Provider 边界、Evaluation 架构和存储演进。
docs/测试与CI总方案.md 约束测试分层、回归基线和验收方式。
Phase 1 / Phase 2 文档是历史基线和回归依据。
Phase 4 / Phase 5 专项规划只能指导后续，不是提前实现理由。
docs/架构模式与设计模式说明.md 是学习理解文档，不作为硬性实现规格。
```

---

# 三、当前允许做的事情

当前允许的任务属于 freeze / cleanup / release polish：

```text
1. 更新文档状态和文档导航。
2. 补充项目展示导读、README、启动说明和验收说明。
3. 修复不改变架构边界的小型一致性问题。
4. 增强 Phase3-P0 回归测试。
5. 修正 API 错误码语义。
6. 校验 Evaluation case_set_version 与 manifest.version 一致。
7. 优化 Evaluation notApplicable 指标语义。
8. 更新 pom.xml description。
9. 打 Phase3-P0 freeze 标记前的收尾清理。
```

允许的小型代码清理示例：

```text
Evaluation item not found 错误码从 EVALUATION_RUN_NOT_FOUND 改为 EVALUATION_ITEM_NOT_FOUND。
EvaluationRunConfig.caseSetVersion 与 YAML manifest.version 不一致时抛出明确异常。
MetricResult / ScoreBreakdown 区分 applicable 与 not_applicable，避免未配置指标被当作满分误导。
```

---

# 四、当前禁止做的事情

```text
1. 不进入 Phase 4。
2. 不新增真实 RAG / GraphRAG。
3. 不接 Python AI Provider。
4. 不接 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
5. 不训练基础大模型。
6. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
7. 不训练 intent / symptom_group / risk_signal 生产模型。
8. 不接 PostgreSQL / Redis / pgvector / Neo4j / Milvus。
9. 不做前端 Training Center / Runtime Console。
10. 不做复杂权限系统 / 审计系统 / 部署运维平台。
11. 不自动上线 CapabilityProfile。
12. 不自动修改 phase2-default 生产资产包。
13. 不做真实医生审核流。
14. 不做经验自动进化。
15. 不把 RuntimeTrace 自动沉淀为 Clinical Experience Memory。
16. 不让 LLM-as-Judge 成为唯一评分依据。
17. 不绕过 RuntimeService 直接评估底层模块。
18. 不改变患者端输出边界。
19. 不绕过 SafetyGate 或 DecisionBoundary。
20. 不大规模移动 docs 文件，除非同步更新所有引用。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 3-P0 freeze cleanup。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、当前架构约束

```text
1. RuntimeService 仍然是 Runtime 主控入口。
2. EvaluationRunner 只能通过 RuntimeService.startRuntime / continueRuntime 执行病例。
3. Scorer 只能评分，不能修改 RuntimeState、资产包或 CapabilityProfile。
4. CapabilityProfileProposalService 只能生成 proposal，不能自动上线。
5. 高风险漏报、患者端诊断泄漏、低风险安抚误输出属于 CRITICAL。
6. Evaluation 必须检查 RuntimeTrace 中的 asset_package_id、asset_package_version、asset_id@version。
7. MCP / Agent SDK / LangGraph / LangChain / GraphRAG / Skills / LLM-as-a-Judge 不能替代 Runtime 主控。
8. 模型训练 / 后训练只能提升可替换 Provider 的能力。
9. 代码包结构、依赖方向、API 分层、Provider 边界、Evaluation 架构、存储演进、测试策略必须遵守 docs/ClinMindRuntime技术实现总方案.md。
10. 文档完整性判断必须参考 docs/架构文档缺口审查清单.md。
```

---

# 六、测试约束

每次 Phase 3 freeze cleanup 改动后，必须尽量保持：

```text
Phase 1 回归测试通过。
Phase 2 Provider / Asset 回归测试通过。
PatientOutputAssetIsolationTest 通过。
RuntimeAssetVersionMismatchTest 通过。
broken-package fail-safe 测试通过。
EvaluationControllerTest 通过。
EvaluationEndToEndIntegrationTest 通过。
```

如果无法实际运行测试，必须在回复或提交说明中明确：

```text
未在本地运行测试，仅完成代码 / 文档修改。
```

---

# 七、AI 每次执行任务前的检查清单

```text
1. 当前任务是否属于 Phase3-P0 freeze / cleanup / release polish？
2. 是否会误进入 Phase 4？如果会，禁止。
3. 是否保持 Evaluation 不绕过 Runtime？
4. 是否影响患者端输出边界？
5. 是否会自动修改资产包或 CapabilityProfile？如果会，禁止。
6. 是否违反 docs/ClinMindRuntime技术实现总方案.md 的代码分层和依赖方向？
7. 是否违反 docs/测试与CI总方案.md 的回归测试要求？
8. 是否违反 docs/医学知识库与RAG构建规划.md 的 RAG / EvidenceGraph 边界？
9. 是否违反 docs/数据安全与合规边界规划.md 的数据安全边界？
10. 是否需要更新 docs/README.md 或 docs/架构文档缺口审查清单.md？
```

---

# 八、当前最优下一步

当前最优任务是：

```text
Phase3-P0 freeze cleanup
```

建议顺序：

```text
1. 文档状态同步：docs/README.md、docs/项目展示导读.md、AI_IMPLEMENTATION_SKILL、架构文档缺口审查清单。
2. 小型一致性修复：case_set_version 校验、notApplicable 语义、Evaluation item 错误码。
3. 更新 pom.xml description。
4. 运行测试或记录未运行原因。
5. 冻结 Phase3-P0。
```

---

# 九、最终约束

```text
当前不是在实现完整训练平台。
当前不是在进入 Phase 4。
当前是在完成 Phase 3-P0 freeze / cleanup / release polish。
新增规划文档只能指导后续 Phase 4/5，不能成为提前实现 Phase 4/5 的理由。
```
