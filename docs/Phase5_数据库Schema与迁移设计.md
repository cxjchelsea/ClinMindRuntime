# Phase 5 数据库 Schema 与迁移设计

> 本文档定义 Phase 5-P0 的 PostgreSQL 表结构、Flyway migration 边界和索引策略。  
> Phase 5-P0 先采用 JSONB + 关键索引字段的混合设计，避免过早把 RuntimeState / EvaluationResult / Candidate 复杂对象拆成大量细表。

---

# 一、设计原则

```text
1. PostgreSQL 是 Phase 5-P0 的唯一主数据库。
2. Flyway 管理 schema migration。
3. 复杂领域对象先用 JSONB 保存，关键查询字段单独列出。
4. schema 必须保留版本字段，方便后续迁移。
5. 入库数据必须遵守 CandidateSanitizer 和数据安全边界。
6. 数据库不能承担 Runtime 决策逻辑。
```

---

# 二、Migration 文件规划

建议新增：

```text
src/main/resources/db/migration/V1__phase5_core_persistence.sql
```

P0 不拆成过多 migration，先保持一个核心 migration，后续 Phase5-P1 再拆分扩展。

---

# 三、核心表

## 3.1 runtime_sessions

```sql
create table runtime_sessions (
  runtime_id varchar(80) primary key,
  session_id varchar(120),
  mode varchar(50),
  work_mode varchar(80),
  runtime_status varchar(80),
  asset_package_id varchar(120),
  asset_package_version varchar(80),
  created_at timestamptz not null,
  updated_at timestamptz not null,
  closed_at timestamptz,
  metadata_jsonb jsonb not null default '{}'::jsonb
);
```

用途：保存 Runtime 会话级元数据。

## 3.2 runtime_states

```sql
create table runtime_states (
  runtime_id varchar(80) primary key references runtime_sessions(runtime_id),
  state_jsonb jsonb not null,
  case_frame_jsonb jsonb,
  safety_state_jsonb jsonb,
  boundary_state_jsonb jsonb,
  version bigint not null default 0,
  updated_at timestamptz not null
);
```

说明：P0 先用 JSONB 保存 RuntimeState 快照，后续再拆分高频字段。

## 3.3 runtime_traces

```sql
create table runtime_traces (
  trace_id varchar(80) primary key,
  runtime_id varchar(80) not null references runtime_sessions(runtime_id),
  step_name varchar(120),
  module_name varchar(120),
  input_summary_jsonb jsonb not null default '{}'::jsonb,
  output_summary_jsonb jsonb not null default '{}'::jsonb,
  asset_refs_jsonb jsonb not null default '[]'::jsonb,
  provider_refs_jsonb jsonb not null default '[]'::jsonb,
  error_code varchar(120),
  created_at timestamptz not null
);
```

说明：Trace 不保存未脱敏完整原文，保存 summary / refs / error。

---

# 四、Evaluation 表

## 4.1 evaluation_runs

```sql
create table evaluation_runs (
  run_id varchar(80) primary key,
  case_set_id varchar(120) not null,
  case_set_version varchar(80) not null,
  asset_package_id varchar(120),
  asset_package_version varchar(80),
  status varchar(50) not null,
  started_at timestamptz not null,
  completed_at timestamptz,
  config_jsonb jsonb not null default '{}'::jsonb,
  result_jsonb jsonb,
  summary_jsonb jsonb not null default '{}'::jsonb
);
```

## 4.2 evaluation_item_results

```sql
create table evaluation_item_results (
  item_result_id varchar(120) primary key,
  run_id varchar(80) not null references evaluation_runs(run_id),
  case_id varchar(120) not null,
  runtime_id varchar(80),
  passed boolean not null,
  score numeric(8,4),
  metric_results_jsonb jsonb not null default '[]'::jsonb,
  safety_violations_jsonb jsonb not null default '[]'::jsonb,
  regression_findings_jsonb jsonb not null default '[]'::jsonb,
  trace_ids_jsonb jsonb not null default '[]'::jsonb,
  notes_jsonb jsonb not null default '[]'::jsonb,
  created_at timestamptz not null
);
```

## 4.3 runtime_case_executions

```sql
create table runtime_case_executions (
  execution_id varchar(120) primary key,
  run_id varchar(80) not null references evaluation_runs(run_id),
  case_id varchar(120) not null,
  runtime_id varchar(80),
  final_state_jsonb jsonb,
  traces_jsonb jsonb not null default '[]'::jsonb,
  operation_responses_jsonb jsonb not null default '[]'::jsonb,
  errors_jsonb jsonb not null default '[]'::jsonb,
  created_at timestamptz not null
);
```

---

# 五、Candidate / Review 表

## 5.1 experience_candidates

```sql
create table experience_candidates (
  candidate_id varchar(120) primary key,
  candidate_type varchar(80) not null,
  title varchar(300) not null,
  summary text not null,
  source_ref_jsonb jsonb not null,
  risk_level varchar(40) not null,
  review_status varchar(50) not null,
  suggested_action text,
  evidence_jsonb jsonb not null default '[]'::jsonb,
  tags_jsonb jsonb not null default '[]'::jsonb,
  created_at timestamptz not null,
  created_by varchar(120),
  metadata_jsonb jsonb not null default '{}'::jsonb
);
```

## 5.2 training_example_candidates

```sql
create table training_example_candidates (
  candidate_id varchar(120) primary key,
  task_type varchar(80) not null,
  source_ref_jsonb jsonb not null,
  input_jsonb jsonb not null,
  expected_output_jsonb jsonb not null default '{}'::jsonb,
  negative_output_jsonb jsonb not null default '{}'::jsonb,
  label varchar(200),
  reason text,
  risk_level varchar(40) not null,
  review_status varchar(50) not null,
  sanitization_status varchar(60) not null,
  tags_jsonb jsonb not null default '[]'::jsonb,
  created_at timestamptz not null,
  metadata_jsonb jsonb not null default '{}'::jsonb
);
```

要求：`input_jsonb` 只能保存 CandidateSanitizer 输出后的 input。

## 5.3 candidate_review_records

```sql
create table candidate_review_records (
  review_id varchar(120) primary key,
  candidate_id varchar(120) not null,
  candidate_kind varchar(80) not null,
  from_status varchar(50) not null,
  to_status varchar(50) not null,
  decision varchar(50) not null,
  reason text not null,
  reviewer varchar(120) not null,
  reviewed_at timestamptz not null,
  source_ref_jsonb jsonb,
  metadata_jsonb jsonb not null default '{}'::jsonb
);
```

---

# 六、Capability Proposal / Audit 表

## 6.1 capability_profile_proposals

```sql
create table capability_profile_proposals (
  proposal_id varchar(120) primary key,
  evaluation_run_id varchar(80) not null,
  symptom_group varchar(120),
  current_level varchar(80),
  proposed_level varchar(80),
  blocked boolean not null,
  blocking_reasons_jsonb jsonb not null default '[]'::jsonb,
  metrics_jsonb jsonb not null default '{}'::jsonb,
  status varchar(50) not null default 'PROPOSED',
  created_at timestamptz not null,
  metadata_jsonb jsonb not null default '{}'::jsonb
);
```

P0 只持久化 proposal，不实现自动上线。

## 6.2 audit_logs

```sql
create table audit_logs (
  audit_id varchar(120) primary key,
  actor_id varchar(120) not null,
  actor_role varchar(80) not null,
  action_type varchar(120) not null,
  resource_type varchar(120) not null,
  resource_id varchar(160) not null,
  request_id varchar(160),
  reason text,
  before_jsonb jsonb,
  after_jsonb jsonb,
  metadata_jsonb jsonb not null default '{}'::jsonb,
  created_at timestamptz not null
);
```

P0 不做完整 RBAC，但审计字段必须先保留。

---

# 七、索引策略

```sql
create index idx_runtime_sessions_status on runtime_sessions(runtime_status);
create index idx_runtime_traces_runtime_created on runtime_traces(runtime_id, created_at);
create index idx_evaluation_runs_status on evaluation_runs(status);
create index idx_evaluation_item_results_run_case on evaluation_item_results(run_id, case_id);
create index idx_runtime_case_executions_run_case on runtime_case_executions(run_id, case_id);
create index idx_experience_candidates_review_status on experience_candidates(review_status);
create index idx_training_candidates_task_review on training_example_candidates(task_type, review_status);
create index idx_candidate_review_records_candidate on candidate_review_records(candidate_id);
create index idx_audit_logs_resource on audit_logs(resource_type, resource_id);
create index idx_audit_logs_actor_created on audit_logs(actor_id, created_at);
```

P0 不创建 pgvector 索引。

---

# 八、Migration 验收标准

```text
1. Flyway 能在空 PostgreSQL 上完成 migration。
2. 所有表有 primary key。
3. 核心外键不破坏现有流程。
4. JSONB 字段默认值合法。
5. 索引创建成功。
6. Testcontainers PostgreSQL 可跑 schema smoke test。
```

---

# 九、最终结论

Phase 5-P0 的 schema 设计采用“关键字段结构化 + 复杂对象 JSONB”的折中方案：

```text
先让系统可持久化和可审计，
再在后续阶段按查询需求拆细表和接 pgvector。
```
