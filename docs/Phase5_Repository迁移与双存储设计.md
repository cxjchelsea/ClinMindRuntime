# Phase 5 Repository 迁移与双存储设计

> 本文档定义 Phase 5-P0 如何从 in-memory store 迁移到 PostgreSQL store，同时保留 memory 模式作为快速测试和回归基线。  
> 目标不是一次性重写 Runtime，而是在不破坏 Phase1–4 行为的前提下替换存储实现。

---

# 一、设计目标

```text
1. 保留现有 in-memory store。
2. 新增 PostgreSQL store。
3. 用配置切换 store 实现。
4. 不改变 RuntimeService / EvaluationRunner / CandidateGenerationService 的业务语义。
5. 不让数据库层承担 Runtime 决策。
6. 保持现有 API 与测试可回归。
```

---

# 二、配置策略

建议新增配置：

```properties
clinmind.persistence.mode=memory
```

可选值：

```text
memory
postgres
```

Spring profile 建议：

```text
application.yml              # 默认 memory
application-postgres.yml     # postgres mode
application-test.yml         # test memory
application-it.yml           # testcontainers postgres
```

---

# 三、需要抽象的 Store / Repository

## 3.1 RuntimeStore

现状：RuntimeStore 是 Runtime 主状态存储。

Phase 5-P0 目标：

```text
RuntimeStore interface
InMemoryRuntimeStore
PostgresRuntimeStore
```

需要支持：

```text
save(RuntimeState state)
findById(String runtimeId)
appendTrace(String runtimeId, RuntimeTrace trace)
getTraces(String runtimeId)
update(RuntimeState state)
```

## 3.2 EvaluationRunStore

目标：

```text
EvaluationRunStore interface
InMemoryEvaluationRunStore
PostgresEvaluationRunStore
```

需要支持：

```text
saveRun(EvaluationRun run)
getRun(String runId)
saveExecution(RuntimeCaseExecution execution)
getExecution(String runId, String caseId)
saveItemResult(EvaluationItemResult item)
getItemResult(String runId, String caseId)
saveResult(EvaluationResult result)
getResult(String runId)
```

## 3.3 CandidateStore

目标：

```text
CandidateStore interface
InMemoryCandidateStore
PostgresCandidateStore
```

需要支持现有 P0/P1 能力：

```text
saveGenerationResult
getGenerationResult
listExperienceCandidates
listTrainingExampleCandidates
getExperienceCandidate
getTrainingExampleCandidate
updateExperienceCandidate
updateTrainingExampleCandidate
```

## 3.4 CandidateReviewStore

目标：

```text
CandidateReviewStore interface
InMemoryCandidateReviewStore
PostgresCandidateReviewStore
```

需要支持：

```text
saveReviewRecord
getReviewRecord
listReviewsByCandidate
```

## 3.5 AuditLogStore

新增：

```text
AuditLogStore interface
PostgresAuditLogStore
InMemoryAuditLogStore（测试可选）
```

---

# 四、Bean 切换方式

推荐：

```java
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
```

Memory 实现：

```java
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "memory", matchIfMissing = true)
```

这样默认不影响现有测试。

---

# 五、序列化策略

P0 建议使用 Jackson ObjectMapper：

```text
Domain Object → DTO / Map → JSONB
JSONB → DTO / Domain Object
```

原则：

```text
1. 不把 Entity 当成领域模型。
2. 不让数据库 Entity 泄漏到 RuntimeService。
3. JSONB 字段序列化失败必须 fail-fast，不静默吞错。
4. 时间字段统一使用 Instant。
```

---

# 六、迁移顺序

推荐实现顺序：

```text
1. 引入 Flyway / PostgreSQL / Testcontainers 依赖。
2. 新增 migration schema。
3. 新增 persistence config。
4. 新增 AuditLogStore / AuditService。
5. CandidateStore → PostgresCandidateStore。
6. CandidateReviewStore → PostgresCandidateReviewStore。
7. EvaluationRunStore → PostgresEvaluationRunStore。
8. RuntimeStore → PostgresRuntimeStore。
9. 补 E2E：postgres mode 下生成 Evaluation → Candidate → Review → AuditLog。
```

为什么先 Candidate / Review：

```text
1. Candidate / Review 结构相对稳定。
2. RuntimeState 最复杂，最后迁移风险更低。
3. Candidate / Review 最能体现治理闭环持久化价值。
```

---

# 七、兼容性要求

```text
1. memory mode 下现有 292 项测试应保持通过。
2. postgres mode 下新增 integration test。
3. API response 不因 store 切换而改变。
4. candidate_id / run_id / runtime_id 不因重启而丢失。
5. Review 后状态在 postgres mode 下可重新查询。
```

---

# 八、错误处理

新增错误码建议：

```text
PERSISTENCE_WRITE_FAILED
PERSISTENCE_READ_FAILED
PERSISTENCE_SERIALIZATION_FAILED
PERSISTENCE_MODE_NOT_SUPPORTED
DATABASE_MIGRATION_NOT_READY
```

原则：

```text
1. 持久化失败不能伪装成业务空结果。
2. 数据库不可用时 postgres mode 启动应失败。
3. memory mode 不依赖数据库。
```

---

# 九、测试设计

新增测试：

```text
PersistenceModeConfigTest
PostgresSchemaMigrationTest
PostgresCandidateStoreTest
PostgresCandidateReviewStoreTest
PostgresEvaluationRunStoreTest
PostgresRuntimeStoreTest
PostgresCandidateReviewAuditIntegrationTest
```

P0 至少完成：

```text
PostgresSchemaMigrationTest
PostgresCandidateStoreTest
PostgresCandidateReviewStoreTest
PostgresCandidateReviewAuditIntegrationTest
```

---

# 十、最终结论

Phase 5-P0 的 Repository 迁移要走“小步替换”：

```text
保留 memory
新增 postgres
配置切换
先治理对象
后 RuntimeState
测试护航
```

这样既能体现工程成熟度，又不会破坏前四个阶段已经稳定的 Runtime / Evaluation / Candidate 主线。
