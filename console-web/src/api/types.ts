export interface ApiErrorBody {
  code: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiErrorBody;
  trace_id?: string;
}

export interface RuntimeConsoleSummary {
  runtime_id: string;
  session_id: string;
  runtime_status: string;
  mode: string;
  asset_package_id: string;
  asset_package_version: string;
  version: number;
  trace_count: number;
  created_at: string;
  updated_at: string;
}

export interface RuntimeConsoleDetail {
  runtime_id: string;
  session_id: string;
  runtime_status: string;
  work_mode: string;
  mode: string;
  asset_package_id: string;
  asset_package_version: string;
  version: number;
  trace_count: number;
  safety_gate_triggered: boolean;
  created_at: string;
  updated_at: string;
}

export interface GovernanceDomainCard {
  domain_id: string;
  name: string;
  status: string;
  record_count: number;
  alert_count: number;
  latest_event_at: string | null;
}

export interface ConsoleOverview {
  phase: string;
  runtime_count: number;
  provider_call_count: number;
  tool_invocation_count: number;
  model_governance_record_count: number;
  candidate_count: number;
  audit_event_count: number;
  domain_cards: GovernanceDomainCard[];
  generated_at: string;
}

export interface Phase10RuntimeListItem {
  runtime_id: string;
  session_id: string;
  runtime_status: string;
  mode: string;
  version: number;
  trace_count: number;
  safety_gate_present: boolean;
  decision_boundary_present: boolean;
  created_at: string;
  updated_at: string;
}

export interface RuntimeTimelineNode {
  node_id: string;
  type: string;
  label: string;
  status: string;
  created_at: string | null;
  summary: Record<string, unknown>;
}

export interface RuntimeTimeline {
  runtime_id: string;
  runtime_status: string;
  trace_count: number;
  nodes: RuntimeTimelineNode[];
}

export interface CandidateInboxItem {
  candidate_id: string;
  candidate_kind: string;
  candidate_type: string;
  title: string;
  summary: string;
  risk_level: string;
  review_status: string;
  sanitization_status: string | null;
  tags: string[];
  created_at: string;
  metadata: Record<string, unknown>;
}

export interface AuditBrowserItem {
  audit_id: string;
  request_id: string | null;
  actor: string;
  action_type: string;
  resource_type: string;
  resource_id: string;
  result_status: string;
  created_at: string;
  metadata: Record<string, unknown>;
}

export interface EvaluationConsoleSummary {
  run_id: string;
  case_set_id: string;
  case_set_version: string;
  asset_package_id: string;
  asset_package_version: string;
  status: string;
  item_count: number;
  started_at: string;
  completed_at: string;
}

export interface EvaluationItemConsoleSummary {
  case_id: string;
  runtime_id: string;
  passed: boolean;
  score: number;
}

export interface EvaluationConsoleDetail {
  run_id: string;
  case_set_id: string;
  case_set_version: string;
  asset_package_id: string;
  asset_package_version: string;
  status: string;
  total_cases: number | null;
  passed_cases: number | null;
  failed_cases: number | null;
  pass_rate: number | null;
  item_summaries: EvaluationItemConsoleSummary[];
}

export interface SourceRefSummary {
  source_type: string;
  evaluation_run_id: string;
  case_id: string;
  asset_package_id: string;
  asset_package_version: string;
  metric_id: string;
}

export interface CandidateGenerationConsoleSummary {
  generation_id: string;
  source_evaluation_run_id: string;
  experience_candidate_count: number;
  training_candidate_count: number;
  skipped_item_count: number;
  started_at: string;
  completed_at: string;
}

export interface CandidateConsoleSummary {
  candidate_id: string;
  candidate_kind: string;
  candidate_type: string;
  review_status: string;
  risk_level: string;
  sanitization_status: string;
  title: string;
  tags: string[];
  source_ref: SourceRefSummary;
  created_at: string;
}

export interface CandidateConsoleDetail {
  candidate_id: string;
  candidate_kind: string;
  candidate_type: string;
  task_type: string;
  review_status: string;
  risk_level: string;
  sanitization_status: string;
  title: string;
  summary: string;
  label: string;
  tags: string[];
  source_ref: SourceRefSummary;
  created_at: string;
  policy_metadata: Record<string, unknown>;
}

export interface ReviewConsoleSummary {
  review_id: string;
  candidate_id: string;
  candidate_kind: string;
  from_status: string;
  to_status: string;
  decision: string;
  reviewer: string;
  reviewed_at: string;
  source_ref: SourceRefSummary;
}

export interface AuditConsoleSummary {
  audit_id: string;
  actor: string;
  action_type: string;
  resource_type: string;
  resource_id: string;
  result_status: string;
  created_at: string;
  metadata_summary: Record<string, unknown>;
}

export interface AuditCenterSummary {
  total_count: number;
  count_by_action_type: Record<string, number>;
  count_by_resource_type: Record<string, number>;
  count_by_result_status: Record<string, number>;
  recent_failures: AuditConsoleSummary[];
  recent_review_actions: AuditConsoleSummary[];
}

export type ReviewDecision = 'APPROVE' | 'REJECT' | 'DEPRECATE' | 'REQUEST_CHANGES';

export interface CandidateReviewRequestBody {
  decision: ReviewDecision;
  reason: string;
  reviewer: string;
}

export interface CandidateReviewRecord {
  review_id: string;
  candidate_id: string;
  candidate_kind: string;
  from_status: string;
  to_status: string;
  decision: ReviewDecision;
  reason: string;
  reviewer: string;
  reviewed_at: string;
}

export interface RuntimeListParams {
  status?: string;
  session_id?: string;
  limit?: number;
}

export interface CandidateInboxParams {
  review_status?: string;
  risk_level?: string;
  candidate_type?: string;
  limit?: number;
}

export interface AuditBrowserParams {
  actor_id?: string;
  action_type?: string;
  resource_type?: string;
  status?: string;
  limit?: number;
}

export interface EvaluationListParams {
  status?: string;
  case_set_id?: string;
  limit?: number;
}

export interface CandidateGenerationListParams {
  source_evaluation_run_id?: string;
  limit?: number;
}

export interface CandidateListParams {
  kind?: string;
  review_status?: string;
  risk_level?: string;
  limit?: number;
}

export interface ReviewQueueListParams {
  kind?: string;
  risk_level?: string;
  task_type?: string;
  limit?: number;
}

export interface AuditLogListParams {
  actor?: string;
  action_type?: string;
  resource_type?: string;
  resource_id?: string;
  result_status?: string;
  from?: string;
  to?: string;
  limit?: number;
}
