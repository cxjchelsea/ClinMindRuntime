import { toSafeDisplayRecord } from './SensitiveFieldRenderGuard';
import './consoleComponents.css';

function formatValue(value: unknown): string {
  if (value == null || value === '') {
    return '—';
  }
  if (typeof value === 'boolean') {
    return value ? '是' : '否';
  }
  if (Array.isArray(value)) {
    return value.map(String).join(', ');
  }
  if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  return String(value);
}

const FIELD_LABELS: Record<string, string> = {
  runtime_id: 'Runtime ID',
  session_id: 'Session ID',
  runtime_status: 'Status',
  work_mode: 'Work Mode',
  mode: 'Mode',
  asset_package_id: 'Asset Package',
  asset_package_version: 'Asset Version',
  version: 'Version',
  trace_count: 'Trace Count',
  safety_gate_triggered: 'Safety Triggered',
  created_at: 'Created At',
  updated_at: 'Updated At',
  run_id: 'Run ID',
  case_set_id: 'Case Set',
  case_set_version: 'Case Set Version',
  status: 'Status',
  item_count: 'Item Count',
  total_cases: 'Total Cases',
  passed_cases: 'Passed Cases',
  failed_cases: 'Failed Cases',
  pass_rate: 'Pass Rate',
  started_at: 'Started At',
  completed_at: 'Completed At',
  policy_metadata: 'Policy Metadata',
  title: 'Title',
  summary: 'Summary',
  label: 'Label',
  tags: 'Tags',
  candidate_id: 'Candidate ID',
  candidate_kind: 'Kind',
  candidate_type: 'Type',
  task_type: 'Task Type',
  review_status: 'Review Status',
  risk_level: 'Risk Level',
  sanitization_status: 'Sanitization Status',
  source_ref: 'Source Ref',
};

export function DetailPanel({
  title,
  data,
}: {
  title: string;
  data: Record<string, unknown> | null;
}) {
  if (!data) {
    return (
      <section className="detail-panel">
        <h2 className="detail-panel__title">{title}</h2>
        <p className="detail-panel__placeholder">选择列表中的一行查看详情</p>
      </section>
    );
  }

  const safe = toSafeDisplayRecord(data);
  const entries = Object.entries(safe).filter(([key]) => key !== 'item_summaries');

  return (
    <section className="detail-panel">
      <h2 className="detail-panel__title">{title}</h2>
      <dl className="detail-panel__list">
        {entries.map(([key, value]) => (
          <div key={key} className="detail-panel__item">
            <dt>{FIELD_LABELS[key] ?? key}</dt>
            <dd>{formatValue(value)}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}
