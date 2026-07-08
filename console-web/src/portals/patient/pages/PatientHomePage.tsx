import { Link } from 'react-router-dom';
import { DEMO_RUNTIME_ID, patientSessionSummaries } from '../../../demo/runtimeDemoData';

export function PatientHomePage() {
  const session = patientSessionSummaries[0];

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Patient Portal</h1>
        <p>面向患者的安全问询入口，只展示整理后的自我信息和安全提醒。</p>
      </div>
      <div className="portal-grid portal-grid--two">
        <article className="portal-panel">
          <h2>Recent session</h2>
          <dl className="portal-definition-list">
            <div>
              <dt>Runtime</dt>
              <dd>{session.runtime_id}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{session.status}</dd>
            </div>
            <div>
              <dt>Summary</dt>
              <dd>{session.chief_complaint_summary}</dd>
            </div>
            <div>
              <dt>Next step</dt>
              <dd>{session.safe_next_step}</dd>
            </div>
          </dl>
          <div className="portal-action-row">
            <Link className="portal-link-button" to={`/patient/sessions/${DEMO_RUNTIME_ID}/summary`}>
              View safe summary
            </Link>
            <Link className="portal-link-button portal-link-button--secondary" to="/patient/inquiry">
              Continue inquiry
            </Link>
          </div>
        </article>
        <article className="portal-panel">
          <h2>Care boundary</h2>
          <p>
            AI 可以帮助整理信息和提醒风险，但不能替代医生诊疗；如果症状加重，请优先联系急救或线下医疗服务。
          </p>
        </article>
      </div>
    </section>
  );
}
