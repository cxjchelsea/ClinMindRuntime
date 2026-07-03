# Phase 8-P0 人工测试结果：Python AI Provider / EmbeddingProvider MVP

> 测试日期：2026-07-02  
> 代码基线：commit `39f2435`  
> 规格：`Phase8_P0Python_AIProvider_实现规格.md`  
> 任务清单：`Phase8_P0开发任务清单.md`

---

## 一、Python Provider 独立测试

| 场景 | 操作 | 预期 | 结果 |
|---|---|---|---|
| 健康检查 | `GET /health` | status=UP，provider_version=0.8.0-p0 | ✅ pytest |
| 能力发现 | `GET /v1/providers` | EMBEDDING dimension=16，RERANK enabled | ✅ pytest |
| Embedding | `POST /v1/embeddings` | 稳定 vector，不返回 raw text | ✅ pytest |
| Rerank | `POST /v1/rerank` 胸痛 query | chest_pain item 排名第一 | ✅ pytest |

启动方式：

```powershell
cd python-provider
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 18080
```

---

## 二、Java 自动化测试（JDK 21）

| 场景 | 测试类 | 结果 |
|---|---|---|
| ProviderValidation | `ProviderValidationServiceTest` | ✅ 4/4 |
| Rerank 增强 | `EvidenceRerankEnhancementServiceTest` | ✅ 2/2 |
| Debug API | `ProviderDebugControllerTest` | ✅ 3/3 |
| 集成 fallback | `ProviderFallbackIntegrationTest` | ✅ 1/1 |
| Evaluation Scorer | `ProviderScorerTest` | ✅ 2/2 |
| **全量回归** | `mvn test` | ✅ **457 通过，0 失败** |

环境：`JAVA_HOME=D:\cxj\software\jdk21`

---

## 三、Java ↔ Python 联调（Debug API + Evidence）

| 场景 | 操作 | 预期 | 结果 |
|---|---|---|---|
| Provider 健康 | `GET /debug/providers/health` | SUCCESS，provider 0.8.0-p0 | ✅ |
| 能力发现 | `GET /debug/providers/capabilities` | EMBEDDING + RERANK | ✅ |
| Rerank 调用 | `POST /debug/providers/rerank/run` | chest_pain 排第一，无 fallback | ✅ |
| Embedding 调用 | `POST /debug/providers/embeddings/run` | dimension=16，ACCEPTED | ✅ |
| 调用追溯 | `GET /debug/providers/calls/{id}` | trace / latency 可查 | ✅ |
| Evidence 增强 | `POST /debug/evidence/retrieve` | `PROVIDER_RERANK_APPLIED` | ✅ |
| Python 停止后 | `POST /debug/providers/rerank/run` | fallback_used=true | ✅ |
| 患者边界 | Runtime start 后检查 PatientOutput | 无 rerank/provider 泄露 | ✅ |

Java 启动：

```powershell
$env:JAVA_HOME='D:\cxj\software\jdk21'
java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar --clinmind.python-provider.enabled=true
```

---

## 四、边界与安全

| 检查项 | 结果 |
|---|---|
| Python 不直接写 RuntimeState | ✅ |
| ProviderValidation 拒绝非法 item_id | ✅ |
| PATIENT 角色不可调用 Debug API | ✅ |
| AuditLog 记录 RUN_PYTHON_PROVIDER | ✅ |

---

## 五、结论

**Phase 8-P0 人工与自动化验收通过**，可依据 `Phase8_P0冻结记录.md` 冻结。
