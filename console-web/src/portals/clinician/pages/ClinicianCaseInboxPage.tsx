import { Link } from 'react-router-dom';
import { clinicianCaseSummaries } from '../../../demo/runtimeDemoData';

export function ClinicianCaseInboxPage() {
  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Clinician Case Inbox</h1>
        <p>病例列表仅展示医生复核所需的摘要投影。</p>
      </div>
      <div className="data-table__wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Case</th>
              <th>Status</th>
              <th>Risk</th>
              <th>Summary</th>
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
                <td>{item.updated_at}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
