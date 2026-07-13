import { Link } from 'react-router-dom';
import { useClinicianCases } from '../hooks/useClinicianCaseView';

export function ClinicianDashboardPage() {
  const { data: clinicianCaseSummaries, loading, source, error } = useClinicianCases();
  const caseSummary = clinicianCaseSummaries[0];
  const watchCount = clinicianCaseSummaries.filter((item) =>
    item.risk_level.toLowerCase().includes('watch'),
  ).length;

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Clinician Workspace</h1>
        <p>医生端查看病例辅助信息，所有 AI 输出均为复核建议。</p>
      </div>
      {source === 'demo-fallback' ? (
        <p className="audit-access-hint">
          Demo fallback：Clinician View API 暂不可用，当前使用本地演示投影。{error ? `原因：${error}` : ''}
        </p>
      ) : null}
      {loading ? <p className="portal-muted">Loading Clinician View API...</p> : null}
      <div className="console-metrics">
        <div className="console-metric">
          <span>Open cases</span>
          <strong>{clinicianCaseSummaries.length}</strong>
        </div>
        <div className="console-metric">
          <span>Risk watch</span>
          <strong>{watchCount}</strong>
        </div>
        <div className="console-metric">
          <span>Runtime</span>
          <strong>{caseSummary?.runtime_id ?? '—'}</strong>
        </div>
      </div>
      <article className="portal-panel">
        <h2>Next case</h2>
        {caseSummary ? (
          <>
            <p>{caseSummary.chief_complaint_summary}</p>
            <p className="portal-muted">Projection: {caseSummary.projection_status ?? source}</p>
            <Link className="portal-link-button" to={`/clinician/cases/${caseSummary.case_id}`}>
              Open workspace
            </Link>
          </>
        ) : (
          <p className="portal-muted">No clinician cases available.</p>
        )}
      </article>
    </section>
  );
}
