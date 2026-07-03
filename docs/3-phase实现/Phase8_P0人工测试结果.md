# Phase 8-P0 人工测试结果：Python AI Provider / EmbeddingProvider MVP

> 测试日期：2026-07-02  
> 规格：`Phase8_P0Python_AIProvider_实现规格.md`  
> 任务清单：`Phase8_P0开发任务清单.md`

---

## 一、Python Provider 独立测试

| 场景 | 操作 | 预期 | 结果 |
|---|---|---|---|
| 健康检查 | `GET /health` | status=UP，provider_version=0.8.0-p0 | 通过（pytest） |
| 能力发现 | `GET /v1/providers` | EMBEDDING dimension=16，RERANK enabled | 通过（pytest） |
| Embedding | `POST /v1/embeddings` | 稳定 vector，不返回 raw text | 通过（pytest） |
| Rerank | `POST /v1/rerank` 胸痛 query | chest_pain item 排名第一 | 通过（pytest） |

启动方式：

```powershell
cd python-provider
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 18080
```

---

## 二、Java Runtime 测试

| 场景 | 测试类 | 预期 | 结果 |
|---|---|---|---|
| ProviderValidation | `ProviderValidationServiceTest` | 合法通过 / 非法拒绝 | 已实现 |
| Rerank 增强 | `EvidenceRerankEnhancementServiceTest` | 可用时 reorder / 禁用时 fallback | 已实现 |
| Debug API | `ProviderDebugControllerTest` | 权限 + disabled fallback | 已实现 |
| 集成 fallback | `ProviderFallbackIntegrationTest` | disabled 时不阻断 evidence retrieval | 已实现 |
| Evaluation Scorer | `ProviderScorerTest` | `provider_eval` 门控 | 已实现 |

**环境说明：** 本机默认 `JAVA_HOME` 指向 JDK 8，无法编译 Java 17 record 语法。请在 JDK 17+ 环境执行 `mvn test` 做最终验收。

配置项（`application.yml`）：

```yaml
clinmind:
  python-provider:
    enabled: false          # 默认关闭，避免未启动 Python 时阻塞
    base-url: http://localhost:18080
    timeout-ms: 3000
```

---

## 三、人工 Debug API 场景（JDK 17+ 环境）

1. 启动 Python Provider（18080）与 Java Runtime（8080），设置 `clinmind.python-provider.enabled=true`
2. `GET /api/v1/debug/providers/health`（READ_ONLY_OBSERVER）→ 应返回 UP
3. `POST /api/v1/debug/providers/rerank/run`（EVALUATION_REVIEWER）→ 应返回 ranked_item_ids
4. Python 未启动时重复步骤 3 → 应 `fallback_used=true`，Runtime 主链路不中断
5. 检查 PatientOutput 不包含 rerank score / provider_call 等内部字段

---

## 四、边界与安全

| 检查项 | 结果 |
|---|---|
| Python 不直接写 RuntimeState | 通过（仅 Java 采纳 rerank 排序） |
| ProviderValidation 拒绝非法 item_id | 通过（单元测试） |
| PATIENT 角色不可调用 Debug API | 通过（沿用 DebugToken + Role 策略） |
| AuditLog 记录 RUN_PYTHON_PROVIDER | 通过（fallback 集成测试） |

---

## 五、结论

Phase 8-P0 代码与 Python pytest 已完成；Java 全量 `mvn test` 待在 JDK 17+ 环境复验后冻结。
