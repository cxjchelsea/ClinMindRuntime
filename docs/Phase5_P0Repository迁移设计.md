# Phase 5-P0 Repository 迁移设计

> 本文档定义 Phase 5-P0 如何从 InMemory Store 迁移到可配置的 PostgreSQL Repository / Store 双实现。  
> 迁移目标不是一次性删除 in-memory，而是建立可测试、可回退、可逐步替换的持久化适配层。

---

# 一、迁移原则

```text
1. Interface 优先，Implementation 后置。
2. InMemory 实现保留，PostgreSQL 实现新增。
3. 使用配置切换，不通过代码注释切换。
4. 不改变 Runtime / Evaluation / Candidate / Review 的业务语义。
5. 单模块迁移，单模块回归，不做大爆炸式重构。
```

---

# 二、配置策略

建议新增配置项：

```yaml
clinmind:
  persistence:
    mode: in-memory # in-memory | postgres
```

P0 默认：

```text
本地开发默认 in-memory。
PostgreSQL 验收环境使用 postgres。
CI 可先保留 in-memory，全量测试稳定后再加入 Testcontainers postgres profile。
```

---

# 三、需要迁移的 Store / Repository

## 3.1 RuntimeStore

现状：保存 RuntimeState、RuntimeTrace、operation record。

目标：

```text
RuntimeStore interface
InMemoryRuntimeStore
JdbcRuntimeStore
```

持久化内容：

```text
runtime_sessions
runtime_traces
operation_records（P0 可内嵌 state_snapshot 或后置独立表）
```

## 3.2 EvaluationRunStore

目标：

```text
EvaluationRunStore interface
InMemoryEvaluationRunStore
JdbcEvaluationRunStore
```

持久化内容：

```text
evaluation_runs
evaluation_items
runtime_case_executions
```

## 3.3 CandidateStore

目标：

```text
CandidateStore interface
InMemoryCandidateStore
JdbcCandidateStore
```

持久化内容：

```text
candidate_generations
experience_candidates
training_example_candidates
```

## 3.4 CandidateReviewStore

目标：

```text
CandidateReviewStore interface
InMemoryCandidateReviewStore
JdbcCandidateReviewStore
```

持久化内容：

```text
candidate_review_records
```

## 3.5 AuditLogStore

新增：

```text
AuditLogStore interface
InMemoryAuditLogStore
JdbcAuditLogStore
```

---

# 四、包结构建议

新增包：

```text
com.clinmind.runtime.persistence
com.clinmind.runtime.persistence.jdbc
com.clinmind.runtime.audit
com.clinmind.runtime.audit.store
```

可选拆分：

```text
com.clinmind.runtime.storage.jdbc
com.clinmind.runtime.evaluation.store.jdbc
com.clinmind.runtime.candidate.store.jdbc
com.clinmind.runtime.candidate.review.jdbc
```

P0 推荐优先按领域放置 JDBC 实现，避免所有 persistence 代码堆在一个目录。

---

# 五、序列化策略

复杂对象使用 JSONB 快照。

建议新增：

```text
JsonSnapshotMapper
RuntimeSnapshotMapper
EvaluationSnapshotMapper
CandidateSnapshotMapper
ReviewSnapshotMapper
```

实现方式：

```text
ObjectMapper.writeValueAsString(object)
ObjectMapper.readValue(json, targetType)
```

必须测试：

```text
RuntimeState snapshot round-trip
EvaluationRun round-trip
CandidateGenerationResult round-trip
ExperienceCandidate round-trip
TrainingExampleCandidate round-trip
CandidateReviewRecord round-trip
```

---

# 六、Spring Bean 切换

推荐使用：

```text
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "in-memory", matchIfMissing = true)
```

注意：

```text
1. 不要在业务 Service 中判断当前 mode。
2. Service 只依赖 interface。
3. mode 切换只发生在 Spring Bean 装配层。
```

---

# 七、迁移顺序

推荐顺序：

```text
1. 新增 Flyway / datasource 配置。
2. 新增 AuditLogStore，因为它依赖最少。
3. 迁移 CandidateReviewStore。
4. 迁移 CandidateStore。
5. 迁移 EvaluationRunStore。
6. 迁移 RuntimeStore。
```

为什么不先迁 RuntimeStore：

```text
RuntimeStore 关联 RuntimeState / Trace / OperationRecord，影响最大。
先迁 Candidate / Review 可以验证 JSONB、Repository 双实现、测试容器和事务边界。
```

---

# 八、事务边界

P0 建议：

```text
CandidateReviewService.reviewXXX：更新 candidate review_status + 保存 review record 应在同一事务。
CandidateGenerationService.generate：保存 generation result + candidates 应在同一事务。
EvaluationRunner.run：EvaluationRun / items / executions 保存应在同一事务边界内或具备失败恢复状态。
```

P0 不做：

```text
分布式事务
跨服务事件总线
Outbox pattern
复杂重试队列
```

---

# 九、测试策略

## 9.1 单元测试

```text
JsonSnapshotMapperTest
JdbcCandidateStoreTest
JdbcCandidateReviewStoreTest
JdbcAuditLogStoreTest
```

## 9.2 集成测试

```text
PostgresCandidatePersistenceIntegrationTest
PostgresReviewPersistenceIntegrationTest
PostgresEvaluationPersistenceIntegrationTest
PostgresRuntimePersistenceIntegrationTest
```

## 9.3 回归测试

```text
InMemory 模式：既有 292 项回归继续通过。
Postgres 模式：新增持久化专项测试通过。
```

---

# 十、验收标准

```text
1. in-memory 模式无需数据库即可启动。
2. postgres 模式能通过 Flyway 初始化 schema。
3. postgres 模式下 Candidate generation 后重启仍可查询 generation / candidate。
4. postgres 模式下 Candidate review 后重启仍可查询 review record。
5. postgres 模式下 AuditLog 可查询。
6. 业务 Service 不感知具体存储实现。
```

---

# 十一、最终结论

Phase 5-P0 Repository 迁移的关键不是“换数据库”，而是建立可切换、可回退、可测试的持久化边界。

正确顺序是：

```text
Interface 不变
→ InMemory 保留
→ PostgreSQL 适配
→ 配置切换
→ 持久化回归
```
