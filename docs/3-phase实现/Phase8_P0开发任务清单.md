# Phase 8-P0 开发任务清单：Python AI Provider / EmbeddingProvider MVP

> 上位实现规格：`docs/3-phase实现/Phase8_P0Python_AIProvider_实现规格.md`  
> API 与测试设计：`docs/3-phase实现/Phase8_P0Provider_API与测试设计.md`  
> 前置专项规划：`docs/2-专项设计/Python_AIProvider接入规划.md`  
> 当前目标：按最小闭环实现 Java Runtime 调用 Python AI Provider，不让 Python 接管 Runtime。

---

# 一、Phase 8-P0 总目标

Phase 8-P0 要完成的不是完整模型平台，而是一个最小闭环：

```text
Java Runtime
→ PythonProviderClient
→ Python FastAPI Provider
→ EmbeddingProvider / RerankerProvider
→ ProviderResult
→ ProviderValidation
→ Evidence Retrieval enhancement
→ Trace / Audit / Evaluation
```

最终要证明：

```text
Python 可以提供模型能力；
Java Runtime 仍然主控；
Python 返回结构化结果；
Java 可以验证、采纳、拒绝或降级；
Provider 调用可追踪、可审计、可评估。
```

---

# 二、任务总览

| 编号 | 任务 | 状态 |
|---|---|---|
| P8-A | 建立 Python provider 工程骨架 | 待做 |
| P8-B | 实现 Python /health 与 /v1/providers | 待做 |
| P8-C | 实现 Python EmbeddingProvider MVP | 待做 |
| P8-D | 实现 Python RerankerProvider MVP | 待做 |
| P8-E | 建立 Java provider domain 对象 | 待做 |
| P8-F | 实现 Java PythonProviderClient | 待做 |
| P8-G | 实现 ProviderValidationService | 待做 |
| P8-H | Evidence Retrieval 接入 rerank / embedding | 待做 |
| P8-I | Provider Debug API | 待做 |
| P8-J | Trace / Audit 接入 | 待做 |
| P8-K | Provider Evaluation Scorer | 待做 |
| P8-L | 测试、人工验证与冻结记录 | 待做 |

---

# 三、P8-A：建立 Python provider 工程骨架

## 目标

新增独立 Python FastAPI Provider 服务。

## 任务

```text
[ ] 新增 python-provider/ 目录。
[ ] 新增 pyproject.toml 或 requirements.txt。
[ ] 新增 app/main.py。
[ ] 新增 app/config.py。
[ ] 新增 app/providers/。
[ ] 新增 app/schemas/。
[ ] 新增 tests/。
[ ] 配置 pytest。
```

## 验收标准

```text
[ ] python-provider 可以独立启动。
[ ] pytest 可以运行。
[ ] 不依赖 Java 进程也能启动 /health。
```

---

# 四、P8-B：实现 /health 与 /v1/providers

## 目标

让 Java 能发现 Python Provider 状态和能力。

## 任务

```text
[ ] 实现 GET /health。
[ ] 实现 GET /v1/providers。
[ ] 返回 provider_id / provider_version。
[ ] 返回 embedding / rerank capabilities。
[ ] 返回 model_id / model_version / dimension。
```

## 验收标准

```text
[ ] /health 返回 UP。
[ ] /v1/providers 返回 EMBEDDING / RERANK。
[ ] provider_version 存在。
[ ] model_version 存在。
```

---

# 五、P8-C：实现 Python EmbeddingProvider MVP

## 目标

提供 deterministic / mock embedding 能力，验证跨语言结构化调用。

## 任务

```text
[ ] 新增 embedding schema。
[ ] 新增 EmbeddingProvider。
[ ] 实现 POST /v1/embeddings。
[ ] 为输入 item 生成固定维度 vector。
[ ] 返回 text_hash。
[ ] 不在响应中返回 raw text。
[ ] 支持空输入 / 非法输入错误响应。
```

## P0 实现方式

```text
可以使用 hash-based deterministic embedding。
不要求真实 embedding 模型。
不要求 OpenAI / sentence-transformers。
```

## 验收标准

```text
[ ] 同一 text 生成稳定 vector。
[ ] vector dimension 一致。
[ ] 不返回 raw text。
[ ] item_id 原样返回。
[ ] pytest 通过。
```

---

# 六、P8-D：实现 Python RerankerProvider MVP

## 目标

提供 deterministic / mock rerank 能力，验证 evidence candidate ordering。

## 任务

```text
[ ] 新增 rerank schema。
[ ] 新增 RerankerProvider。
[ ] 实现 POST /v1/rerank。
[ ] 根据 query / item text 做简单 keyword overlap 或 hash score。
[ ] 返回 ranked_items。
[ ] score 控制在 0–1。
[ ] 不新增 item_id。
[ ] 不输出诊断结论。
```

## 验收标准

```text
[ ] ranked_items 只包含输入 item_id。
[ ] rank 从 1 开始。
[ ] score 范围合法。
[ ] 与胸痛 query 更相关的 chest_pain item 排名前。
[ ] pytest 通过。
```

---

# 七、P8-E：建立 Java provider domain 对象

## 目标

新增 Java 侧 Provider 抽象。

## 建议包路径

```text
src/main/java/com/clinmind/runtime/provider/
src/main/java/com/clinmind/runtime/provider/python/
src/main/java/com/clinmind/runtime/provider/embedding/
src/main/java/com/clinmind/runtime/provider/rerank/
src/main/java/com/clinmind/runtime/provider/validation/
```

## 任务

```text
[ ] 新增 ProviderStatus。
[ ] 新增 ProviderCapabilityType。
[ ] 新增 ProviderInvocationRequest。
[ ] 新增 ProviderInvocationResult。
[ ] 新增 ProviderTrace。
[ ] 新增 ProviderValidationResult。
[ ] 新增 EmbeddingRequest / EmbeddingResult。
[ ] 新增 RerankRequest / RerankResult。
[ ] 新增 ProviderCapabilityDescriptor。
```

## 验收标准

```text
[ ] 所有对象不包含 PatientOutput。
[ ] ProviderInvocationResult 有 status / warnings / error_code。
[ ] ProviderTrace 有 provider_id / model_version / latency。
```

---

# 八、P8-F：实现 Java PythonProviderClient

## 目标

Java 可通过 HTTP 调用 Python Provider。

## 任务

```text
[ ] 新增 PythonProviderClient。
[ ] 支持 health check。
[ ] 支持 get providers。
[ ] 支持 embeddings。
[ ] 支持 rerank。
[ ] 支持 timeout 配置。
[ ] 支持 provider.enabled=false 时跳过。
[ ] 捕获连接失败 / 超时 / 非 2xx 响应。
```

## 验收标准

```text
[ ] Python 可用时调用成功。
[ ] Python 不可用时返回 MODEL_UNAVAILABLE / FAILED。
[ ] 超时时返回 TIMEOUT。
[ ] 不抛未处理异常到 RuntimeService。
```

---

# 九、P8-G：实现 ProviderValidationService

## 目标

确保 Python 返回结果不能越权或污染 Runtime。

## 任务

```text
[ ] 新增 ProviderValidationService。
[ ] 校验 provider_id。
[ ] 校验 provider_version。
[ ] 校验 model_id / model_version。
[ ] 校验 schema_version。
[ ] 校验 request_id 对齐。
[ ] 校验 embedding dimension。
[ ] 校验 vector 非空、非 NaN。
[ ] 校验 rerank item_id 属于输入候选。
[ ] 校验 rerank score 范围。
[ ] 校验禁止字段。
```

## 验收标准

```text
[ ] 合法 embedding result 通过。
[ ] 合法 rerank result 通过。
[ ] 非法 schema 被拒绝。
[ ] 非法 item_id 被拒绝。
[ ] 所有拒绝有 reasons。
```

---

# 十、P8-H：Evidence Retrieval 接入 rerank / embedding

## 目标

用 Python Provider 增强 Phase 7-P0 evidence retrieval，但不破坏 fallback。

## 任务

```text
[ ] 在 RagEvidenceProvider 或 Evidence orchestration 后接入 RerankerProvider。
[ ] 构造 RerankRequest。
[ ] 调用 PythonProviderClient。
[ ] ProviderValidation 通过后更新 candidate ordering。
[ ] ProviderValidation 失败时 fallback 原排序。
[ ] Python 不可用时 fallback 原排序。
[ ] 可选接入 EmbeddingProvider 作为辅助 score。
```

## 验收标准

```text
[ ] rerank 成功时 ordering 可变化。
[ ] rerank 失败时原 ordering 保持。
[ ] EvidenceCandidate 仍经过 EvidenceValidation。
[ ] PatientOutput 不泄露 rerank score。
```

---

# 十一、P8-I：Provider Debug API

## 目标

提供最小 debug API 观察 Provider 调用。

## 任务

```text
[ ] 新增 ProviderDebugController。
[ ] 实现 GET /api/v1/debug/providers/health。
[ ] 实现 GET /api/v1/debug/providers/capabilities。
[ ] 实现 POST /api/v1/debug/providers/rerank/run。
[ ] 实现 POST /api/v1/debug/providers/embeddings/run。
[ ] 实现 GET /api/v1/debug/providers/calls/{provider_call_id}。
[ ] 所有响应使用 Safe DTO。
[ ] 接入 DebugTokenFilter / ActorContext / AccessPolicy。
```

## 验收标准

```text
[ ] 未授权访问被拒绝。
[ ] PATIENT 角色不能调用。
[ ] SYSTEM_ADMIN / EVALUATION_REVIEWER 可 run。
[ ] READ_ONLY_OBSERVER 可读。
[ ] API 不返回 raw patient text。
```

---

# 十二、P8-J：Trace / Audit 接入

## 目标

让 Provider 调用可复盘。

## 任务

```text
[ ] 新增 ProviderCallStore。
[ ] ProviderTrace 记录 provider_call_id。
[ ] RuntimeTrace 记录 Provider summary。
[ ] Debug API 调用写入 AuditLog。
[ ] fallback 写入 AuditLog。
[ ] validation rejected 写入 AuditLog。
```

## 验收标准

```text
[ ] 每次 provider call 有 provider_call_id。
[ ] AuditLog 可看到 RUN_PYTHON_PROVIDER。
[ ] fallback_used 可查询。
[ ] trace 不保存 raw patient dialogue。
```

---

# 十三、P8-K：Provider Evaluation Scorer

## 目标

让 Python Provider 能力进入 Evaluation。

## 任务

```text
[ ] 新增 ProviderTraceCompletenessScorer。
[ ] 新增 ProviderSchemaValidationScorer。
[ ] 新增 ProviderFallbackSafetyScorer。
[ ] 可选新增 RerankOrderingQualityScorer。
[ ] EvaluationResult 中加入 provider 相关 metric。
[ ] 支持 provider_eval tag。
```

## 验收标准

```text
[ ] 缺 provider_id / model_version 时得分失败。
[ ] fallback 无 trace 时得分失败。
[ ] invalid schema 被 scorer 识别。
[ ] provider result 泄露到 PatientOutput 时 boundary scorer 失败。
```

---

# 十四、P8-L：测试、人工验证与冻结记录

## 目标

完成 Phase 8-P0 收口。

## 任务

```text
[ ] 完成 Python pytest。
[ ] 完成 PythonProviderClientTest。
[ ] 完成 ProviderValidationServiceTest。
[ ] 完成 ProviderFallbackTest。
[ ] 完成 ProviderDebugControllerTest。
[ ] 完成 ProviderEnhancedEvidenceRetrievalTest。
[ ] 完成 ProviderTraceAuditTest。
[ ] 完成 Provider Evaluation Scorer tests。
[ ] 运行 mvn test。
[ ] 运行 python-provider pytest。
[ ] 运行 console-web npm run test / npm run build。
[ ] 编写 Phase8_P0人工测试结果.md。
[ ] 编写 Phase8_P0冻结记录.md。
```

## 验收标准

```text
[ ] Java 测试通过。
[ ] Python 测试通过。
[ ] Phase 1–7 P1 回归不破坏。
[ ] 前端测试 / build 不回归；如存在 flake，必须明确与 P8 无直接关联。
[ ] 人工测试覆盖 Python 正常、Python 未启动、非法 schema、PatientOutput 边界。
[ ] 冻结记录明确已做 / 未做 / 后置任务。
```

---

# 十五、推荐实现顺序

建议严格按以下顺序实现：

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

原因：

```text
先保证 Python Provider 可独立运行，
再保证 Java 可安全调用，
再接入 evidence retrieval，
最后接入 Evaluation 和冻结。
```

---

# 十六、开发期间禁止事项

```text
1. 不让 Python 成为 Runtime 主控。
2. 不创建 Python Agent 自主循环。
3. 不让 Python 直接输出 PatientOutput。
4. 不让 Python 直接判断最终诊断。
5. 不让 Python 修改 RuntimeState。
6. 不把 LLM 生成内容直接返回患者。
7. 不跳过 ProviderValidation。
8. 不跳过 Java Runtime fallback。
9. 不破坏 Phase 7 Evidence / Graph 能力。
10. 不改写 Phase 1–7 P1 冻结记录。
```

---

# 十七、Phase 8-P0 完成后的后置任务

```text
1. Phase 8-P1：JudgeProvider / RiskSignalClassifierProvider。
2. Phase 8-P1：ModelProvider / ProviderCapabilityProfile。
3. Phase 8-P2：ModelRegistry / PromptRegistry。
4. Phase 8-P2：TrainingDatasetVersion。
5. Phase 10：Model Console / Provider Console。
6. 后置专项：LoRA / DPO / RFT / distillation。
```

---

# 十八、最终 Definition of Done

Phase 8-P0 完成的最终标准：

```text
[ ] Python Provider 可以独立启动。
[ ] Java Runtime 可以调用 Python Provider。
[ ] EmbeddingProvider 返回结构化 embedding result。
[ ] RerankerProvider 返回结构化 rerank result。
[ ] ProviderValidation 可以拒绝非法结果。
[ ] Provider 失败时 Java fallback。
[ ] Evidence retrieval 可以受控使用 rerank result。
[ ] PatientOutput 不泄露 provider 内部结果。
[ ] Provider call 进入 Trace / Audit。
[ ] Provider 能力进入 Evaluation。
[ ] Java / Python 测试通过。
[ ] Phase8_P0冻结记录完成。
```
