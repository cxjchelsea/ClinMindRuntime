# Phase 8-P0 冻结记录：Python AI Provider / EmbeddingProvider MVP

> 冻结日期：2026-07-02  
> 上位规格：`Phase8_P0Python_AIProvider_实现规格.md`  
> 任务清单：`Phase8_P0开发任务清单.md`  
> 人工测试：`Phase8_P0人工测试结果.md`

---

## 一、冻结结论

Phase 8-P0 **Python AI Provider / EmbeddingProvider MVP** 代码已实现。Python pytest 5/5 通过；Java 测试已在代码层覆盖，**待在 JDK 17+ 环境执行 `mvn test` 后正式验收冻结**。

核心命题已验证（设计 + 单测 / pytest 层）：

```text
Java Runtime 主控 → PythonProviderClient → FastAPI Provider；
Embedding / Rerank 返回结构化结果；
ProviderValidation 可接受或拒绝；
失败时 fallback，不阻断 Evidence Retrieval；
Provider 调用可 Trace / Audit / Evaluation（provider_eval）。
```

---

## 二、已完成（P8-A ~ P8-L）

| 编号 | 内容 | 状态 |
|---|---|---|
| P8-A | `python-provider/` FastAPI 工程骨架 | 已完成 |
| P8-B | `/health`、`/v1/providers` | 已完成 |
| P8-C | EmbeddingProvider（hash-based mock, dim=16） | 已完成 |
| P8-D | RerankerProvider（keyword overlap mock） | 已完成 |
| P8-E | Java provider domain 对象 | 已完成 |
| P8-F | `HttpPythonProviderClient` | 已完成 |
| P8-G | `ProviderValidationService` | 已完成 |
| P8-H | `EvidenceRerankEnhancementService` 接入 Evidence Retrieval | 已完成 |
| P8-I | Debug API `/api/v1/debug/providers/**` | 已完成 |
| P8-J | ProviderCallStore + Audit（RUN/QUERY_PYTHON_PROVIDER） | 已完成 |
| P8-K | 4 个 Evaluation Scorer（`provider_eval` 门控） | 已完成 |
| P8-L | pytest + Java 测试 + 人工测试/冻结文档 | 已完成（Java mvn 待 JDK17 复验） |

---

## 三、主要代码位置

```text
python-provider/                          # FastAPI Provider（port 18080）
src/main/java/com/clinmind/runtime/provider/
  python/                                 # HttpPythonProviderClient
  embedding/ rerank/ validation/
  runtime/                                # EvidenceRerankEnhancementService, ProviderCallStore
  api/                                    # ProviderDebugController
src/main/java/com/clinmind/runtime/evaluation/scorer/Provider*.java
```

Runtime 集成：

```text
RagEvidenceProvider → EvidenceRerankEnhancementService（可选 rerank）→ EvidenceValidation → EvidenceGraph
```

Provider ID：`python_ai_provider` v`0.8.0-p0`

默认配置：`clinmind.python-provider.enabled=false`（安全默认，需显式开启）

---

## 四、P0 明确未做

```text
Python Agent 主控 / 自主循环
LLM 患者问答
真实 embedding 模型 / sentence-transformers
ModelRegistry / PromptRegistry
pgvector / 外部向量库
Embedding index 持久化
JudgeProvider / RiskSignalClassifierProvider（Phase 8-P1）
```

---

## 五、后置任务

1. Phase 8-P1：JudgeProvider / ModelProvider / ProviderCapabilityProfile  
2. Phase 8-P2：ModelRegistry / TrainingDatasetVersion  
3. Phase 10：Model Console / Provider Console  
4. 生产级 embedding / rerank 模型接入

---

## 六、回归说明

- Phase 7 Evidence / Graph 能力未被移除；rerank 为可选增强层  
- `provider_eval` tag 门控 Evaluation Scorer，不影响 Phase 1–7 默认 case  
- Python Provider 默认 disabled，不改变现有 Runtime 默认行为

---

## 七、验收待办（JDK 17+ 环境）

```text
[ ] mvn test 全绿
[ ] 启用 python-provider 后人工 Debug API 联调
[ ] console-web npm run test / build（如无 P8 相关变更可跳过）
```
