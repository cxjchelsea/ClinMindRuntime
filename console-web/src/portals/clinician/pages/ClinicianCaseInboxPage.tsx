import { Link } from 'react-router-dom';
import { useClinicianCases } from '../hooks/useClinicianCaseView';

export function ClinicianCaseInboxPage() {
  const { data: clinicianCaseSummaries, loading, source, error } = useClinicianCases();

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Clinician Case Inbox</h1>
        <p>病例列表仅展示医生复核所需的摘要投影。</p>
      </div>
      {source === 'demo-fallback' ? (
        <p className="audit-access-hint">
          Demo fallback：Clinician View API 暂不可用，当前使用本地演示投影。{error ? `原因：${error}` : ''}
        </p>
      ) : null}
      {loading ? <p className="portal-muted">Loading Clinician View API...</p> : null}
      <div className="data-table__wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Case</th>
              <th>Status</th>
              <th>Risk</th>
              <th>Summary</th>
              <th>Projection</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            {clinicianCaseSummaries.map((item) => (
              <tr key={item.case_id}>
                <td>
                  <Link to={`/clinician/cases/${item.case_id}`}>{item.case_id}</Link>
                </td>
                <td>{item.status}</td>
                <td>{item.risk_level}</td>
                <td>{item.chief_complaint_summary}</td>
                <td>{item.projection_status ?? source}</td>
                <td>{item.updated_at}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
