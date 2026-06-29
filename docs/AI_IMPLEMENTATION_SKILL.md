# AI Implementation Skill：ClinMindRuntime Phase 4-P0

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1-P0 Runtime MVP、Phase 2-P0 共享能力资产原型、Phase 3-P0 训练与评估闭环 MVP 已完成并冻结。  
> 当前进入 Phase 4-P0：经验候选与训练数据候选沉淀机制。后续修改不得破坏 Runtime 主控、安全门、输出边界、Provider 抽象、资产版本追踪、Evaluation 闭环和患者端隔离。

---

# 一、当前项目阶段

```text
当前阶段：Phase 4-P0 经验候选与训练数据候选沉淀机制
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP
Phase 2-P0：共享能力资产原型
Phase 3-P0：训练与评估闭环 MVP，已冻结
Phase 4-P0：P0-A 至 P0-D 已完成，准备进入 P0-E TrainingExampleCandidateGenerator
```

Phase 4-P0 目标：

```text
从 EvaluationRun / EvaluationResult / EvaluationItemResult / MetricResult / SafetyViolation / RegressionFinding / RuntimeCaseExecution 中生成 ExperienceCandidate 与 TrainingExampleCandidate。
```

Phase 4-P0 推荐链路：

```text
EvaluationRun
→ EvaluationResult
→ EvaluationItemResult
→ RegressionFinding / SafetyViolation / RuntimeCaseExecution
→ CandidateGenerationService
→ ExperienceCandidate
→ TrainingExampleCandidate
→ CandidateStore
→ Debug API 查询
→ Review Required
```

重要说明：

```text
Phase 4 不是模型训练阶段。
Phase 4-P0 只做候选沉淀。
Candidate 默认 REVIEW_REQUIRED。
Candidate 不自动上线经验，不自动进入训练集，不自动修改资产包，不自动修改 CapabilityProfile。
真实模型训练、后训练、Python AI Provider、RAG / GraphRAG、MCP、前端平台、数据库持久化等能力仍然后置。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase4_开发任务清单.md
3. docs/Phase4_经验候选与训练数据候选沉淀_实现规格.md
4. docs/Phase4_数据结构设计.md
5. docs/Phase4_候选生成策略设计.md
6. docs/Phase4_Runtime与Evaluation接入设计.md
7. docs/Phase4_API与测试设计.md
8. docs/README.md
9. docs/项目展示导读.md
10. docs/Phase3_P0冻结记录.md
11. docs/Phase3_开发任务清单.md
12. docs/Phase3_人工测试API结果.md
13. docs/架构文档缺口审查清单.md
14. docs/ClinMindRuntime技术实现总方案.md
15. docs/测试与CI总方案.md
16. docs/Phase3_训练与评估闭环_实现规格.md
17. docs/Phase3_评估数据结构设计.md
18. docs/Phase3_病例集与考试流程设计.md
19. docs/Phase3_Runtime评估接入设计.md
20. docs/Phase3_CapabilityProfile更新机制设计.md
21. docs/Phase3_API与测试设计.md
22. docs/全局技术栈与架构选型.md
23. docs/AI前沿技术选型与接入规划.md
24. docs/模型训练与后训练规划.md
25. docs/医学知识库与RAG构建规划.md
26. docs/数据安全与合规边界规划.md
27. docs/数据库持久化设计.md
28. docs/平台前端与Console规划.md
29. docs/部署与运维规划.md
30. docs/Phase2_开发任务清单.md
31. docs/Phase2_共享能力资产原型_实现规格.md
32. docs/Phase2_Provider接口设计.md
33. docs/Phase2_资产数据结构与版本设计.md
34. docs/Phase2_Runtime接入改造设计.md
35. docs/Phase2_API与测试设计.md
36. docs/Phase1_技术栈与工程架构决策.md
37. docs/Phase1_Runtime_MVP_实现规格.md
38. docs/Phase1_数据结构与状态设计.md
39. docs/Phase1_模块接口设计.md
40. docs/Phase1_API与测试设计.md
41. docs/ClinMindRuntime阶段拆分路线图.md
42. docs/ClinMindRuntime完整系统设计.md
43. docs/架构模式与设计模式说明.md
```

解释：

```text
Phase 4 文档优先指导当前新增能力。
docs/Phase3_P0冻结记录.md 是 Phase3-P0 冻结依据。
Phase 1 / Phase 2 / Phase 3 文档是历史基线和回归依据。
Phase 5 专项规划只能指导后续，不是提前实现理由。
docs/架构模式与设计模式说明.md 是学习理解文档，不作为硬性实现规格。
```

---

# 三、当前允许实现的内容

当前只允许按 Phase4-P0-A 到 Phase4-P0-G 顺序推进。

## 3.1 Phase4-P0-A：Candidate 数据结构

```text
CandidateSourceRef
CandidateSourceType
CandidateRiskLevel
CandidateReviewStatus
ExperienceCandidate
ExperienceCandidateType
TrainingExampleCandidate
TrainingTaskType
SanitizationStatus
CandidateGenerationPolicy
CandidateGenerationResult
CandidateSkippedItem
```

## 3.2 Phase4-P0-B：CandidateStore

```text
CandidateStore
InMemoryCandidateStore
```

## 3.3 Phase4-P0-C：CandidateGenerationPolicy 与映射策略

```text
MetricSeverity → CandidateRiskLevel
MetricResult → ExperienceCandidateType
MetricResult → TrainingTaskType
SafetyViolation → ExperienceCandidate
RegressionFinding → ExperienceCandidate
skipped item reason
max_candidates_per_case
```

## 3.4 Phase4-P0-D：ExperienceCandidateGenerator

```text
ExperienceCandidateGenerator
从 failed MetricResult / SafetyViolation / RegressionFinding 生成 ExperienceCandidate
```

## 3.5 Phase4-P0-E：TrainingExampleCandidateGenerator

```text
TrainingExampleCandidateGenerator
从失败病例生成训练样本候选
```

## 3.6 Phase4-P0-F：CandidateGenerationService

```text
CandidateGenerationService
读取 EvaluationRunStore
生成 CandidateGenerationResult
保存到 CandidateStore
```

## 3.7 Phase4-P0-G：Debug API 与测试

```text
CandidateController
/api/v1/debug/candidates/**
Phase4_人工测试API结果.md
```

---

# 四、当前禁止做的事情

```text
1. 不新增真实 RAG / GraphRAG。
2. 不接 Python AI Provider。
3. 不接 MCP / LangGraph / Agent SDK 作为 Runtime 主控。
4. 不训练基础大模型。
5. 不实现 SFT / RLHF / DPO / RFT / 蒸馏训练链路。
6. 不训练 intent / symptom_group / risk_signal 生产模型。
7. 不接 PostgreSQL / Redis / pgvector / Neo4j / Milvus。
8. 不做前端 Training Center / Runtime Console。
9. 不做复杂权限系统 / 审计系统 / 部署运维平台。
10. 不自动上线 CapabilityProfile。
11. 不自动修改 phase2-default 生产资产包。
12. 不做真实医生审核流。
13. 不做经验自动进化。
14. 不把 RuntimeTrace 自动沉淀为正式 Clinical Experience Memory。
15. 不让 LLM-as-Judge 成为唯一评分依据。
16. 不绕过 RuntimeService 直接评估底层模块。
17. 不改变患者端输出边界。
18. 不绕过 SafetyGate 或 DecisionBoundary。
19. 不大规模移动 docs 文件，除非同步更新所有引用。
20. 不直接实现 Phase4-P1 / Phase5 能力。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 4-P0 候选沉淀机制。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、Phase 4-P0 架构约束

```text
1. RuntimeService 仍然是 Runtime 主控入口。
2. Phase 4 只读取 EvaluationRunStore，不调用 RuntimeService 执行新 Runtime。
3. Phase 4 不调用 EvaluationRunner 重新评估。
4. CandidateGenerationService 不能修改 EvaluationRun。
5. CandidateGenerationService 不能修改 RuntimeState。
6. CandidateGenerationService 不能修改 AssetPackage。
7. CandidateGenerationService 不能修改 CapabilityProfile。
8. ExperienceCandidate / TrainingExampleCandidate 默认 REVIEW_REQUIRED。
9. Candidate 不能自动进入正式经验库。
10. Candidate 不能自动进入训练集。
11. Candidate 必须绑定 CandidateSourceRef、risk_level、review_status、asset_package_id、asset_package_version。
12. 代码包结构、依赖方向、API 分层、Provider 边界、Evaluation 架构、存储演进、测试策略必须遵守 docs/ClinMindRuntime技术实现总方案.md。
```

---

# 六、任务清单同步规则

每次实现 Phase 4 代码前，必须同步更新：

```text
docs/Phase4_开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase4_开发任务清单.md。
2. 确认当前任务属于 Phase4-P0-A 到 Phase4-P0-G 的哪一项。
3. 将正在处理的任务从 [ ] 改为 [/]。
4. 如果任务不在清单中，先在对应阶段补充任务项，不要直接实现。
```

实现后：

```text
1. 将已完成任务从 [/] 改为 [x]。
2. 如果任务部分完成，保持 [/]，并补充备注或问题记录。
3. 如果任务被阻塞，将状态改为 [!]，并在问题记录中说明原因。
4. 如果实现过程中新增了必要任务，补充到对应阶段。
```

---

# 七、测试约束

每实现一个 Phase 4 模块，必须补充 JUnit 测试。

至少包含：

```text
CandidateSourceRefTest
ExperienceCandidateTest
TrainingExampleCandidateTest
CandidateGenerationPolicyTest
CandidateGenerationResultTest
InMemoryCandidateStoreTest
ExperienceCandidateGeneratorTest
TrainingExampleCandidateGeneratorTest
CandidateGenerationServiceTest
CandidateControllerTest
CandidateEndToEndIntegrationTest
```

每次 Phase 4 改动后，必须尽量保持：

```text
Phase 1 回归测试通过。
Phase 2 Provider / Asset 回归测试通过。
Phase 3 Evaluation 回归测试通过。
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

# 八、AI 每次执行任务前的检查清单

```text
1. 当前任务是否属于 Phase4-P0-A 到 Phase4-P0-G？
2. 是否读取并更新了 docs/Phase4_开发任务清单.md？
3. 是否会误实现 RAG / Python Provider / DB / Frontend / Model Training？如果会，禁止。
4. 是否保持 Runtime 主控不被绕过？
5. 是否会调用 RuntimeService 执行新 Runtime？如果会，除非任务明确需要，否则禁止。
6. 是否会调用 EvaluationRunner 重新评估？如果会，禁止。
7. 是否影响患者端输出边界？
8. 是否会自动修改资产包或 CapabilityProfile？如果会，禁止。
9. 是否会把候选经验自动上线？如果会，禁止。
10. 是否会把训练候选自动进入训练集？如果会，禁止。
11. 是否违反 docs/ClinMindRuntime技术实现总方案.md 的代码分层和依赖方向？
12. 是否违反 docs/测试与CI总方案.md 的回归测试要求？
13. 是否需要更新 docs/README.md 或 docs/架构文档缺口审查清单.md？
```

---

# 九、当前最优下一步

当前最优实现任务是：

```text
Phase4-P0-E：TrainingExampleCandidateGenerator
```

只应实现：

```text
1. TrainingExampleCandidateGenerator。
2. 从失败病例生成训练样本候选。
3. sanitization_status 默认 NEEDS_REVIEW。
4. TrainingExampleCandidateGeneratorTest。
5. 同步更新 docs/Phase4_开发任务清单.md。
```

不应在 P0-E 中实现：

```text
CandidateGenerationService
CandidateController
API
数据库
前端
模型训练
RAG
LLM 调用
```

---

# 十、最终约束

```text
当前不是在实现完整训练平台。
当前不是在实现经验自动进化。
当前是在实现 Phase 4-P0 候选沉淀机制。
Phase 4 的目标是把值得复盘的信息变成可追踪、可审核、不可自动生效的候选资产。
```
