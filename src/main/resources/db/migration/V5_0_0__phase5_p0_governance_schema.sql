-- Phase 5-P0 governance schema

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

create index idx_runtime_sessions_session_id on runtime_sessions(session_id);
create index idx_runtime_sessions_status on runtime_sessions(runtime_status);
create index idx_runtime_sessions_asset on runtime_sessions(asset_package_id, asset_package_version);

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

create table runtime_case_executions (
  execution_id varchar(160) primary key,
  run_id varchar(128) not null,
  case_id varchar(128) not null,
  runtime_id varchar(128),
  created_at timestamptz not null,
  execution_snapshot jsonb not null,
  foreign key (run_id) references evaluation_runs(run_id)
);

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

create index idx_candidate_reviews_candidate_id on candidate_review_records(candidate_id);
create index idx_candidate_reviews_reviewer on candidate_review_records(reviewer);

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

create index idx_audit_logs_resource on audit_logs(resource_type, resource_id);
create index idx_audit_logs_actor on audit_logs(actor);
create index idx_audit_logs_created_at on audit_logs(created_at);
