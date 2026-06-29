# AI Implementation Skill：ClinMindRuntime Phase 4-P1

> 本文件用于约束 AI / Cursor / Claude Code / Codex 在本仓库中的实现行为。  
> 当前 Phase 1-P0 Runtime MVP、Phase 2-P0 共享能力资产原型、Phase 3-P0 训练与评估闭环 MVP、Phase 4-P0 候选沉淀机制均已完成并冻结。  
> 当前进入 Phase 4-P1：候选治理与安全加固。后续修改不得破坏 Runtime 主控、安全门、输出边界、Provider 抽象、资产版本追踪、Evaluation 闭环、候选 REVIEW_REQUIRED 边界和患者端隔离。

---

# 一、当前项目阶段

```text
当前阶段：Phase 4-P1 候选治理与安全加固 — 已完成
下一步：Phase4-P1 冻结记录 / Phase4-P2 规划
```

当前已经完成的主线：

```text
Phase 1-P0：Runtime MVP
Phase 2-P0：共享能力资产原型
Phase 3-P0：训练与评估闭环 MVP，已冻结
Phase 4-P0：候选沉淀机制 + debug API，已冻结
Phase 4-P1：候选治理与安全加固，已完成（P1-A 至 P1-F）
```

Phase 4-P1 目标：

```text
把 Phase4-P0 生成的 Candidate，从“可生成、可查询”提升为“可脱敏、可强校验、可记录 review 决策、仍不自动生效”。
```

Phase 4-P1 推荐链路：

```text
EvaluationRun / RuntimeCaseExecution
→ CandidateGenerationService
→ CandidateSanitizer
→ CandidateSourceRefFactory / Validator
→ ExperienceCandidate / TrainingExampleCandidate
→ CandidateStore
→ CandidateReviewService
→ CandidateReviewRecord
→ Review-aware Debug API
```

重要说明：

```text
Phase 4-P1 不是模型训练阶段。
Phase 4-P1 不是正式医生审核平台。
Phase 4-P1 不接数据库、不做前端、不做 RAG。
Candidate review 只记录人工决策，不自动上线经验、不自动进入训练集、不自动修改资产包、不自动修改 CapabilityProfile。
```

---

# 二、权威文档优先级

AI 实现时必须优先参考以下文档，优先级从高到低：

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase4_P1开发任务清单.md
3. docs/Phase4_P1候选治理与安全加固_实现规格.md
4. docs/Phase4_P1候选脱敏与来源校验设计.md
5. docs/Phase4_P1候选Review记录设计.md
6. docs/Phase4_P0冻结记录.md
7. docs/Phase4_开发任务清单.md
8. docs/Phase4_经验候选与训练数据候选沉淀_实现规格.md
9. docs/Phase4_数据结构设计.md
10. docs/Phase4_候选生成策略设计.md
11. docs/Phase4_Runtime与Evaluation接入设计.md
12. docs/Phase4_API与测试设计.md
13. docs/README.md
14. docs/项目展示导读.md
15. docs/Phase3_P0冻结记录.md
16. docs/Phase3_开发任务清单.md
17. docs/Phase3_人工测试API结果.md
18. docs/架构文档缺口审查清单.md
19. docs/ClinMindRuntime技术实现总方案.md
20. docs/测试与CI总方案.md
21. docs/数据安全与合规边界规划.md
22. docs/模型训练与后训练规划.md
23. docs/医学知识库与RAG构建规划.md
24. docs/数据库持久化设计.md
25. docs/平台前端与Console规划.md
26. docs/部署与运维规划.md
27. docs/ClinMindRuntime阶段拆分路线图.md
28. docs/ClinMindRuntime完整系统设计.md
29. docs/架构模式与设计模式说明.md
```

解释：

```text
Phase 4-P1 文档优先指导当前新增能力。
docs/Phase4_P0冻结记录.md 是 P0 冻结边界与 hardening backlog 依据。
docs/数据安全与合规边界规划.md 是 CandidateSanitizer 的安全依据。
Phase 5 专项规划只能指导后续，不是提前实现理由。
```

---

# 三、当前允许实现的内容

Phase 4-P1-A 至 P1-F 已全部完成。后续改动不得破坏 P1 脱敏、SourceRef 校验、Review 记录与 REVIEW_REQUIRED 边界。

## 3.1 Phase4-P1-A：CandidateSanitizer 与脱敏策略

```text
CandidateSanitizationPolicy
CandidateSanitizationResult
CandidateSanitizer
TrainingExampleCandidateGenerator 接入 CandidateSanitizer
sanitization_status 由 sanitizer 决定
metadata 写入 sanitizer_policy_id / policy_version
```

## 3.2 Phase4-P1-B：CandidateSourceRef Factory 与组合校验

```text
CandidateSourceRefFactory
CandidateSourceRefValidator
CandidateSourceRefValidationException
Generator 从 new CandidateSourceRef 改为 factory 创建
按 source_type 校验必填字段
```

## 3.3 Phase4-P1-C：CandidateNotFoundException resourceType

```text
CandidateResourceType enum
CandidateNotFoundException 增加 resourceType
InMemoryCandidateStore 抛异常时指定 resourceType
ApiExceptionHandler 不再依赖 message 字符串
```

## 3.4 Phase4-P1-D：CandidateReview 数据结构

```text
CandidateKind
CandidateReviewDecision
CandidateReviewRecord
CandidateReviewRequest
CandidateReviewTransitionPolicy
```

## 3.5 Phase4-P1-E：CandidateReviewStore 与 Service

```text
CandidateReviewStore
InMemoryCandidateReviewStore
CandidateReviewService
reviewExperienceCandidate
reviewTrainingExampleCandidate
```

## 3.6 Phase4-P1-F：Review Debug API 与端到端测试

```text
POST /api/v1/debug/candidates/experience-candidates/{candidate_id}/review
POST /api/v1/debug/candidates/training-example-candidates/{candidate_id}/review
GET /api/v1/debug/candidates/reviews/{review_id}
GET /api/v1/debug/candidates/{candidate_id}/reviews
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
12. 不做正式医生审核平台。
13. 不做经验自动进化。
14. 不把 RuntimeTrace 自动沉淀为正式 Clinical Experience Memory。
15. 不让 LLM-as-Judge 成为唯一评分依据。
16. 不绕过 RuntimeService 直接评估底层模块。
17. 不改变患者端输出边界。
18. 不绕过 SafetyGate 或 DecisionBoundary。
19. 不大规模移动 docs 文件，除非同步更新所有引用。
20. 不直接实现 Phase5 能力。
```

如果任务中出现上述需求，AI 必须回复：

```text
该能力属于后续 Phase，不属于当前 Phase 4-P1 候选治理与安全加固。本次只保留为 backlog 或文档规划，不实现真实能力。
```

---

# 五、Phase 4-P1 架构约束

```text
1. RuntimeService 仍然是 Runtime 主控入口。
2. CandidateGenerationService 仍然只读取 EvaluationRunStore，不调用 RuntimeService 执行新 Runtime。
3. CandidateSanitizer 必须在 TrainingExampleCandidateGenerator 写入 input 前生效。
4. CandidateSourceRef 必须通过 Factory / Validator 创建。
5. CandidateNotFoundException 必须通过 resourceType 映射错误码。
6. CandidateReviewService 只能记录 review，不得修改 AssetPackage / CapabilityProfile / TrainingDataset。
7. Candidate review_status 即使变成 APPROVED，也不代表 Runtime 可用。
8. Candidate review 不得触发模型训练。
9. Debug API 不得返回未脱敏真实患者原文。
10. 所有改动必须保持 Phase1/2/3/4-P0 回归通过。
```

---

# 六、任务清单同步规则

每次实现 Phase 4-P1 代码前，必须同步更新：

```text
docs/Phase4_P1开发任务清单.md
```

实现前：

```text
1. 读取 docs/Phase4_P1开发任务清单.md。
2. 确认当前任务属于 Phase4-P1-A 到 Phase4-P1-F 的哪一项。
3. 将正在处理的任务从 [ ] 改为 [/]。
4. 如果任务不在清单中，先补清单，不要直接实现。
```

实现后：

```text
1. 将已完成任务从 [/] 改为 [x]。
2. 如果任务部分完成，保持 [/] 并说明原因。
3. 如果任务被阻塞，将状态改为 [!] 并说明原因。
```

---

# 七、测试约束

每实现一个 Phase 4-P1 模块，必须补充 JUnit 测试。

至少包含：

```text
CandidateSanitizerTest
CandidateSourceRefFactoryTest
CandidateSourceRefValidatorTest
CandidateNotFoundExceptionTest
CandidateReviewRecordTest
CandidateReviewTransitionPolicyTest
CandidateReviewServiceTest
CandidateReviewControllerTest
CandidateReviewEndToEndIntegrationTest
```

每次 Phase 4-P1 改动后，必须尽量保持：

```text
Phase 1 回归测试通过。
Phase 2 Provider / Asset 回归测试通过。
Phase 3 Evaluation 回归测试通过。
Phase 4-P0 Candidate generation 回归测试通过。
CandidateControllerTest 通过。
CandidateEndToEndIntegrationTest 通过。
```

如果无法实际运行测试，必须在回复或提交说明中明确：

```text
未在本地运行测试，仅完成代码 / 文档修改。
```

---

# 八、AI 每次执行任务前的检查清单

```text
1. 当前任务是否属于 Phase4-P1-A 到 Phase4-P1-F？
2. 是否读取并更新了 docs/Phase4_P1开发任务清单.md？
3. 是否会误实现 RAG / Python Provider / DB / Frontend / Model Training？如果会，禁止。
4. 是否保持 Runtime 主控不被绕过？
5. 是否影响患者端输出边界？
6. 是否会自动修改资产包或 CapabilityProfile？如果会，禁止。
7. 是否会把候选经验自动上线？如果会，禁止。
8. 是否会把训练候选自动进入训练集？如果会，禁止。
9. 是否会让 review 触发 Runtime 行为变化？如果会，禁止。
10. 是否违反数据安全与合规边界规划？如果会，禁止。
11. 是否需要更新 docs/README.md 或 docs/架构文档缺口审查清单.md？
```

---

# 九、当前最优下一步

当前最优实现任务是：

```text
Phase4-P1 已完成。下一步：编写 Phase4_P1冻结记录.md，或按 Phase4-P2 / Phase5 规划推进。
```

P1 已完成内容：

```text
1. CandidateSanitizationPolicy / Result / Sanitizer + Generator 接入。
2. CandidateSourceRefFactory / Validator + Generator 接入。
3. CandidateNotFoundException resourceType + 错误码映射。
4. CandidateReviewRecord / Service / Store。
5. Review Debug API + 端到端测试。
6. docs/Phase4_P1开发任务清单.md 与 Phase4_P1人工测试API结果.md 已更新。
7. 全量 mvn test 292 项全绿。
```

不应在 P1 冻结后继续扩展 P1 范围：

```text
CandidateReviewService
Review API
数据库
前端
模型训练
RAG
```

---

# 十、最终约束

```text
当前不是在实现完整训练平台。
当前不是在实现正式医生审核平台。
当前是在 Phase 4-P1 候选治理与安全加固已完成阶段。
Phase 4-P1 的目标是让候选更安全、更可追踪、更可治理，但仍然不自动生效。
```
