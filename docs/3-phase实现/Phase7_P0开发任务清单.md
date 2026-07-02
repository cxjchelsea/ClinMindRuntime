# Phase 7-P0 开发任务清单：RAG EvidenceProvider MVP

> 上位实现规格：`docs/3-phase实现/Phase7_P0RAG_EvidenceProvider_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase7_P0RAG_API与测试设计.md`  
> 当前目标：按最小闭环实现 RAG EvidenceProvider MVP，让 RAG 只返回 EvidenceCandidate / EvidenceRef，不直接回答患者。

---

# 一、Phase 7-P0 总目标

Phase 7-P0 要完成的不是完整 RAG 平台，而是一个最小闭环：

```text
RuntimeState / CaseFrame
→ EvidenceProviderPolicy
→ RagEvidenceProvider
→ EvidenceCandidate / EvidenceRef
→ EvidenceValidationService
→ EvidenceGraph
→ DecisionBoundary
→ RuntimeTrace / Audit / Evaluation
```

最终要证明：

```text
RAG 可以作为 Runtime 授权下的 EvidenceProvider，
检索结果只能生成可追踪证据候选，
证据候选必须可校验、可拒绝、可部分采纳，
证据进入 EvidenceGraph，
不能直接进入 PatientOutput。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P7-A | 建立 Evidence domain 基础对象 | 待做 |
| P7-B | 建立 Evidence corpus 与加载器 | 待做 |
| P7-C | 建立 EvidenceProviderPolicy | 待做 |
| P7-D | 实现 RagEvidenceProvider MVP | 待做 |
| P7-E | 实现 EvidenceValidationService / RuntimeValidation 接入 | 待做 |
| P7-F | EvidenceGraph 集成 | 待做 |
| P7-G | Capability Orchestration 接入 evidence retrieval | 待做 |
| P7-H | Evidence Debug API | 待做 |
| P7-I | Trace / Audit 接入 | 待做 |
| P7-J | Evidence Evaluation Scorer | 待做 |
| P7-K | 测试、人工验证与冻结记录 | 待做 |

---

# 三、P7-A：建立 Evidence domain 基础对象

## 目标

新增 RAG EvidenceProvider 的核心数据结构。

## 建议包路径

```text
src/main/java/.../evidence/
```

建议保持：

```text
evidence
evidence.rag
evidence.api
evidence.validation
evidence.corpus
evidence.policy
```

## 任务

```text
[ ] 新增 EvidenceRef。
[ ] 新增 EvidenceCandidate。
[ ] 新增 EvidenceUseCase。
[ ] 新增 EvidenceRiskLevel。
[ ] 新增 EvidenceRetrievalRequest。
[ ] 新增 EvidenceRetrievalResult。
[ ] 新增 EvidenceRetrievalStatus。
[ ] 新增 EvidenceRetrievalTrace。
[ ] 新增 EvidenceValidationResult。
```

## 验收标准

```text
[ ] EvidenceRef 必须包含 source_id / chunk_id / symptom_group / retrieval_score。
[ ] EvidenceCandidate 必须包含 use_case / confidence / reason_summary。
[ ] EvidenceRetrievalResult 必须包含 provider_id / provider_version / corpus_version。
[ ] 所有对象不包含 PatientOutput。
[ ] 所有对象不包含 raw patient dialogue。
```

---

# 四、P7-B：建立 Evidence corpus 与加载器

## 目标

建立最小 evidence corpus，支持 deterministic retrieval。

## 任务

```text
[ ] 新增 evidence_chunks.yml 或 evidence corpus JSON/YAML。
[ ] 覆盖 chest_pain / fever / abdominal_pain。
[ ] 每个 symptom_group 至少 3 条 chunk。
[ ] 新增 EvidenceChunk。
[ ] 新增 EvidenceCorpus。
[ ] 新增 EvidenceCorpusRepository。
[ ] 新增 YamlEvidenceCorpusRepository 或 ResourceEvidenceCorpusRepository。
[ ] 加载时校验 source_id / chunk_id / symptom_group / version。
```

## 验收标准

```text
[ ] corpus 可以正常加载。
[ ] chunk_count > 0。
[ ] 支持 symptom_group 查询。
[ ] 缺少关键字段的 chunk 被拒绝或记录 warning。
[ ] 不使用真实敏感病例。
```

---

# 五、P7-C：建立 EvidenceProviderPolicy

## 目标

控制何时允许 evidence retrieval。

## 任务

```text
[ ] 新增 EvidenceProviderPolicy。
[ ] 支持 symptom_group 白名单。
[ ] corpus 不可用时 fail-closed。
[ ] retrieval_limit 超限时降级到最大值。
[ ] unsupported symptom_group 返回 POLICY_REJECTED 或 NO_EVIDENCE_FOUND。
[ ] 高风险场景下证据不可用时必须产生 warning。
```

## 验收标准

```text
[ ] chest_pain / fever / abdominal_pain 允许检索。
[ ] unknown symptom_group 不伪造证据。
[ ] corpus unavailable 不静默成功。
[ ] Policy 拒绝有 reasons。
```

---

# 六、P7-D：实现 RagEvidenceProvider MVP

## 目标

实现最小 deterministic / keyword-based RAG EvidenceProvider。

## 任务

```text
[ ] 新增 EvidenceProvider 接口。
[ ] 新增 RagEvidenceProvider。
[ ] 根据 symptom_group 检索 evidence chunks。
[ ] 根据 known_facts / red_flag_summary 做简单匹配。
[ ] 计算 retrieval_score。
[ ] 构造 EvidenceRef。
[ ] 构造 EvidenceCandidate。
[ ] 按 retrieval_score 排序。
[ ] 限制 retrieval_limit。
[ ] 返回 EvidenceRetrievalResult。
```

## P0 检索策略

```text
1. symptom_group 精确匹配优先。
2. content_summary / diagnosis_tags / use_cases 关键词匹配加分。
3. red_flag_summary 命中 HIGH risk chunk 加分。
4. 分数低于阈值可不返回。
```

## 验收标准

```text
[ ] chest_pain 能召回 chest_pain chunks。
[ ] fever 能召回 fever chunks。
[ ] retrieval_score 稳定。
[ ] 返回 EvidenceCandidate，不返回自然语言患者答案。
[ ] NO_EVIDENCE_FOUND 时不伪造证据。
```

---

# 七、P7-E：实现 EvidenceValidationService / RuntimeValidation 接入

## 目标

确保 evidence candidate 不能越权。

## 任务

```text
[ ] 新增 EvidenceValidationService。
[ ] 校验 EvidenceRef source_id 非空。
[ ] 校验 chunk_id 非空。
[ ] 校验 symptom_group 非空。
[ ] 校验 retrieval_score 存在且范围合法。
[ ] 校验 evidence_corpus_version 或 asset_package_version 存在。
[ ] 校验 use_case 在允许集合内。
[ ] 禁止 patient_direct_answer use_case。
[ ] 支持 ACCEPTED / PARTIALLY_ACCEPTED / REJECTED / DEGRADED。
[ ] 接入 RuntimeValidationService 或独立 evidence validation boundary。
```

## 验收标准

```text
[ ] 合法 EvidenceCandidate 通过。
[ ] 缺 source_id 拒绝。
[ ] 缺 chunk_id 拒绝。
[ ] 缺 retrieval_score 拒绝。
[ ] 禁止 use_case 拒绝。
[ ] 超出 candidate 数量时部分采纳。
```

---

# 八、P7-F：EvidenceGraph 集成

## 目标

让 accepted EvidenceCandidate 进入 EvidenceGraph。

## 任务

```text
[ ] 扩展 EvidenceGraph item 结构，支持 evidence_ref。
[ ] 新增 EvidenceCandidateToGraphMapper。
[ ] EvidenceGraphService 读取 accepted EvidenceCandidate。
[ ] EvidenceGraph 中记录 source_id / chunk_id / use_case / confidence。
[ ] rejected EvidenceCandidate 不进入 EvidenceGraph。
[ ] ClinicianReport 可展示证据摘要。
[ ] PatientOutput 不展示 retrieval_score / 内部证据推理链。
```

## 验收标准

```text
[ ] EvidenceGraph 有 evidence_ref。
[ ] ClinicianReport 可见证据来源摘要。
[ ] PatientOutput 不泄露 retrieval_score。
[ ] DecisionBoundary 仍控制患者端输出。
```

---

# 九、P7-G：Capability Orchestration 接入 evidence retrieval

## 目标

在 Runtime 主链路中接入 EvidenceProvider，但不破坏 Phase 6 Agent。

## 任务

```text
[ ] 新增 EvidenceCapabilityOrchestrator 或扩展 CapabilityOrchestrationService。
[ ] 判断是否需要 evidence retrieval。
[ ] 构造 EvidenceRetrievalRequest。
[ ] 调用 EvidenceProviderPolicy。
[ ] 调用 RagEvidenceProvider。
[ ] 保存 EvidenceRetrievalSnapshot 到 RuntimeState 或 capability snapshot。
[ ] Agent orchestration 与 evidence retrieval 互不覆盖。
[ ] 失败时 fallback，不阻断 Runtime 主链路。
```

## 验收标准

```text
[ ] SafetyGate fail-safe 时不执行 evidence retrieval。
[ ] EvidenceProvider 失败时 Runtime 降级继续。
[ ] AgentOrchestrationSnapshot 不被覆盖。
[ ] EvidenceRetrievalSnapshot 可被 Trace / Evaluation 读取。
```

---

# 十、P7-H：Evidence Debug API

## 目标

提供最小 debug API 观察 evidence retrieval。

## 任务

```text
[ ] 新增 EvidenceDebugController。
[ ] 实现 POST /api/v1/debug/evidence/retrieve。
[ ] 实现 GET /api/v1/debug/evidence/retrievals/{retrieval_id}。
[ ] 实现 GET /api/v1/debug/evidence/corpus。
[ ] 所有响应使用 Safe DTO。
[ ] 接入 DebugTokenFilter / ActorContext / AccessPolicy。
```

## 验收标准

```text
[ ] 未授权访问被拒绝。
[ ] PATIENT 角色不能调用。
[ ] SYSTEM_ADMIN / EVALUATION_REVIEWER 可 run。
[ ] READ_ONLY_OBSERVER 可读 corpus / retrieval summary。
[ ] API 不返回 raw runtime state。
[ ] API 不返回未脱敏患者原文。
```

---

# 十一、P7-I：Trace / Audit 接入

## 目标

让 evidence retrieval 可复盘。

## 任务

```text
[ ] EvidenceRetrievalResult 记录 retrieval_id。
[ ] EvidenceRetrievalTrace 记录 provider_id / corpus_version / query_terms。
[ ] RuntimeTrace 记录 evidence retrieval summary。
[ ] Debug API 调用写入 AuditLog。
[ ] Policy 拒绝也记录 trace / audit。
[ ] Validation 拒绝也记录 trace / audit。
```

## 验收标准

```text
[ ] 每次 retrieval 有 retrieval_id。
[ ] AuditLog 可看到 RUN_EVIDENCE_RETRIEVAL / QUERY_EVIDENCE_RETRIEVAL。
[ ] Trace 不保存 raw patient dialogue。
[ ] Trace 保留 accepted / rejected candidate ids。
```

---

# 十二、P7-J：Evidence Evaluation Scorer

## 目标

让 evidence retrieval 能被 Evaluation 衡量。

## 任务

```text
[ ] 新增 EvidenceTraceCompletenessScorer。
[ ] 新增 EvidenceSourceVersionScorer。
[ ] 新增 EvidencePatientBoundaryScorer。
[ ] 新增 EvidenceUseCaseSafetyScorer。
[ ] 可选新增 EvidenceRecallScorer。
[ ] 支持 case tags 含 evidence_eval 时启用 evidence scorer。
[ ] EvaluationResult 中加入 evidence 相关 metric。
```

## 验收标准

```text
[ ] 缺 source_id / chunk_id / version 时得分失败。
[ ] PatientOutput 泄露 retrieval_score 时得分失败。
[ ] accepted evidence candidate 有 trace 时得分通过。
[ ] unsupported use_case 得分失败。
```

---

# 十三、P7-K：测试、人工验证与冻结记录

## 目标

完成 Phase 7-P0 收口。

## 任务

```text
[ ] 完成 EvidenceCorpusRepositoryTest。
[ ] 完成 EvidenceProviderPolicyTest。
[ ] 完成 RagEvidenceProviderTest。
[ ] 完成 EvidenceValidationServiceTest。
[ ] 完成 EvidenceGraphIntegrationTest。
[ ] 完成 EvidenceDebugControllerTest。
[ ] 完成 EvidenceTraceAuditTest。
[ ] 完成 Evidence Evaluation Scorer tests。
[ ] 运行 mvn test。
[ ] 运行 console-web npm run test / npm run build。
[ ] 编写 Phase7_P0人工测试结果.md。
[ ] 编写 Phase7_P0冻结记录.md。
```

## 验收标准

```text
[ ] 所有后端测试通过。
[ ] Phase 1–6 P0 回归不破坏。
[ ] 前端测试 / build 不回归；如存在 flake，必须明确说明与 P7 无直接关联。
[ ] 人工测试覆盖高风险胸痛、普通发热、unknown symptom_group。
[ ] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十四、推荐实现顺序

建议严格按以下顺序实现：

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

原因：

```text
先保证 evidence candidate 结构正确，
再让检索返回证据，
再接入 Runtime，
最后接入 Evaluation 和冻结。
```

---

# 十五、开发期间禁止事项

```text
1. 不让 RAG 直接回答患者。
2. 不把检索结果拼 Prompt 生成患者端回答。
3. 不让 EvidenceProvider 修改 RuntimeState。
4. 不跳过 EvidenceValidationService。
5. 不跳过 DecisionBoundary。
6. 不引入真实 GraphRAG。
7. 不引入 Neo4j / Milvus / Qdrant。
8. 不引入 LLM / reranker 训练。
9. 不破坏 Phase 6-P0 Agent runtime。
10. 不改写 Phase 1–6 P0 冻结记录。
```

---

# 十六、Phase 7-P0 完成后的后置任务

```text
1. Phase 7-P1：KG-lite node / edge。
2. Phase 7-P1：GraphRAG Provider prototype。
3. Phase 8-P0：EmbeddingProvider / Python AI Provider。
4. Phase 8-P1：Reranker / ModelProvider。
5. Phase 10：Knowledge Console。
6. Phase 10：正式知识审核、发布、回滚工作流。
```

---

# 十七、最终 Definition of Done

Phase 7-P0 完成的最终标准：

```text
[ ] Runtime 可以受控调用 RagEvidenceProvider。
[ ] RAG 只能输出 EvidenceCandidate / EvidenceRef。
[ ] EvidenceCandidate 必须经过 EvidenceValidation / RuntimeValidation。
[ ] EvidenceGraph 可以采纳 accepted evidence。
[ ] PatientOutput 不泄露证据评分或医生端推理链。
[ ] ClinicianReport 可以展示证据摘要和来源。
[ ] Evidence retrieval 进入 Trace / Audit。
[ ] Evidence 能力进入 Evaluation。
[ ] Debug API 可以安全观察 retrieval。
[ ] 所有新增测试和既有回归测试通过。
[ ] Phase7_P0冻结记录完成。
```
