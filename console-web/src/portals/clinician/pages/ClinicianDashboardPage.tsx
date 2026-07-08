import { Link } from 'react-router-dom';
import { clinicianCaseSummaries } from '../../../demo/runtimeDemoData';

export function ClinicianDashboardPage() {
  const caseSummary = clinicianCaseSummaries[0];

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Clinician Workspace</h1>
        <p>医生端查看病例辅助信息，所有 AI 输出均为复核建议。</p>
      </div>
      <div className="console-metrics">
        <div className="console-metric">
          <span>Open cases</span>
          <strong>1</strong>
        </div>
        <div className="console-metric">
          <span>Risk watch</span>
          <strong>1</strong>
        </div>
        <div className="console-metric">
          <span>Runtime</span>
          <strong>{caseSummary.runtime_id}</strong>
        </div>
      </div>
      <article className="portal-panel">
        <h2>Next case</h2>
        <p>{caseSummary.chief_complaint_summary}</p>
        <Link className="portal-link-button" to={`/clinician/cases/${caseSummary.case_id}`}>
          Open workspace
        </Link>
      </article>
    </section>
  );
}
