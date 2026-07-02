# Phase 5-P0 数据库 Schema 设计

> 本文档定义 Phase 5-P0 的 PostgreSQL schema、表边界、JSONB 使用原则和迁移顺序。  
> P0 目标是保存治理对象快照与审计链，不追求完整产品化查询模型。

---

# 一、设计原则

```text
1. 关键对象必须有稳定 ID。
2. 高频过滤字段列化，复杂结构 JSONB 化。
3. 所有表保留 created_at / updated_at。
4. 医疗与候选相关数据保留 source_ref / asset_package_version / sanitization_status。
5. 不在 P0 建复杂多租户模型。
6. 不把 YAML asset package 正文迁移进数据库。
```

---

# 二、Migration 组织

建议新增：

```text
src/main/resources/db/migration/V5_0_0__phase5_p0_governance_schema.sql
```

后续拆分可以为：

```text
V5_0_1__runtime_trace_tables.sql
V5_0_2__evaluation_tables.sql
V5_0_3__candidate_tables.sql
V5_0_4__audit_log_tables.sql
```

P0 可先单文件，避免迁移管理过早复杂化。

---

# 三、Runtime 表

## 3.1 runtime_sessions

用途：保存 Runtime 会话级摘要。

```sql
create table runtime_sessions (
  runtime_id varchar(128) primary key,
  session_id varchar(128),
  user_id varchar(128),
  mode varchar(64),
  runtime_status varchar(64),
  asset_package_id varchar(128),
  asset_package_version varchar(64),
  created_at timestamptz not null,
  updated_at timestamptz not null,
  state_snapshot jsonb not null
);
```

索引：

```sql
create index idx_runtime_sessions_session_id on runtime_sessions(session_id);
create index idx_runtime_sessions_status on runtime_sessions(runtime_status);
create index idx_runtime_sessions_asset on runtime_sessions(asset_package_id, asset_package_version);
```

## 3.2 runtime_traces

用途：保存 RuntimeTrace 快照。

```sql
create table runtime_traces (
  trace_id varchar(128) primary key,
  runtime_id varchar(128) not null,
  step_name varchar(128),
  module_name varchar(128),
  asset_package_id varchar(128),
  asset_package_version varchar(64),
  created_at timestamptz not null,
  trace_payload jsonb not null,
  foreign key (runtime_id) references runtime_sessions(runtime_id)
);
```

---

# 四、Evaluation 表

## 4.1 evaluation_runs

```sql
create table evaluation_runs (
  run_id varchar(128) primary key,
  case_set_id varchar(128) not null,
  case_set_version varchar(64),
  asset_package_id varchar(128),
  asset_package_version varchar(64),
  status varchar(64) not null,
  started_at timestamptz,
  completed_at timestamptz,
  config_snapshot jsonb not null,
  result_snapshot jsonb
);
```

## 4.2 evaluation_items

```sql
create table evaluation_items (
  item_id varchar(160) primary key,
  run_id varchar(128) not null,
  case_id varchar(128) not null,
  runtime_id varchar(128),
  passed boolean,
  total_score numeric(8,4),
  severity varchar(64),
  created_at timestamptz not null,
  item_snapshot jsonb not null,
  foreign key (run_id) references evaluation_runs(run_id)
);
```

## 4.3 runtime_case_executions

```sql
create table runtime_case_executions (
  execution_id varchar(160) primary key,
  run_id varchar(128) not null,
  case_id varchar(128) not null,
  runtime_id varchar(128),
  created_at timestamptz not null,
  execution_snapshot jsonb not null,
  foreign key (run_id) references evaluation_runs(run_id)
);
```

---

# 五、Candidate 表

## 5.1 candidate_generations

```sql
create table candidate_generations (
  generation_id varchar(128) primary key,
  source_evaluation_run_id varchar(128),
  started_at timestamptz,
  completed_at timestamptz,
  experience_candidate_count integer not null default 0,
  training_candidate_count integer not null default 0,
  skipped_item_count integer not null default 0,
  generation_snapshot jsonb not null
);
```

## 5.2 experience_candidates

```sql
create table experience_candidates (
  candidate_id varchar(160) primary key,
  generation_id varchar(128),
  candidate_type varchar(80) not null,
  risk_level varchar(40) not null,
  review_status varchar(40) not null,
  asset_package_id varchar(128),
  asset_package_version varchar(64),
  created_at timestamptz not null,
  candidate_snapshot jsonb not null,
  source_ref jsonb not null
);
```

## 5.3 training_example_candidates

```sql
create table training_example_candidates (
  candidate_id varchar(160) primary key,
  generation_id varchar(128),
  task_type varchar(80) not null,
  risk_level varchar(40) not null,
  review_status varchar(40) not null,
  sanitization_status varchar(40) not null,
  sanitizer_policy_id varchar(128),
  sanitizer_policy_version varchar(64),
  asset_package_id varchar(128),
  asset_package_version varchar(64),
  created_at timestamptz not null,
  candidate_snapshot jsonb not null,
  source_ref jsonb not null
);
```

---

# 六、Review 表

## 6.1 candidate_review_records

```sql
create table candidate_review_records (
  review_id varchar(128) primary key,
  candidate_id varchar(160) not null,
  candidate_kind varchar(80) not null,
  from_status varchar(40) not null,
  to_status varchar(40) not null,
  decision varchar(40) not null,
  reviewer varchar(128) not null,
  reviewed_at timestamptz not null,
  reason text not null,
  source_ref jsonb,
  metadata jsonb
);
```

索引：

```sql
create index idx_candidate_reviews_candidate_id on candidate_review_records(candidate_id);
create index idx_candidate_reviews_reviewer on candidate_review_records(reviewer);
```

---

# 七、AuditLog 表

## 7.1 audit_logs

```sql
create table audit_logs (
  audit_id varchar(128) primary key,
  request_id varchar(128),
  actor varchar(128),
  action_type varchar(128) not null,
  resource_type varchar(128) not null,
  resource_id varchar(160),
  result_status varchar(40) not null,
  created_at timestamptz not null,
  metadata jsonb
);
```

索引：

```sql
create index idx_audit_logs_resource on audit_logs(resource_type, resource_id);
create index idx_audit_logs_actor on audit_logs(actor);
create index idx_audit_logs_created_at on audit_logs(created_at);
```

---

# 八、JSONB 使用规范

允许 JSONB 存储：

```text
RuntimeState snapshot
RuntimeTrace payload
EvaluationResult snapshot
EvaluationItemResult snapshot
Candidate snapshot
Candidate source_ref
Review metadata
Audit metadata
```

必须列化的字段：

```text
id
status
type
risk_level
review_status
sanitization_status
asset_package_id
asset_package_version
created_at
updated_at
```

禁止 P0 直接依赖 JSONB 做复杂业务判断。

---

# 九、迁移验收标准

```text
1. Flyway 启动时能自动创建 schema。
2. PostgreSQL 模式下能创建 Runtime 并查询 trace。
3. PostgreSQL 模式下能创建 EvaluationRun 并查询 result / item。
4. PostgreSQL 模式下能生成 Candidate 并查询 candidates。
5. PostgreSQL 模式下能记录 CandidateReviewRecord。
6. AuditLog 能记录关键 debug 操作。
7. In-memory 模式仍可运行所有既有测试。
```

---

# 十、最终结论

Phase 5-P0 schema 不是最终产品化数据模型，而是治理对象可持久化的第一版底座。

后续 Console、RBAC、正式审核、TrainingDatasetVersion 和 ApprovedExperience 都应基于这层 schema 继续扩展。
