import { Link } from 'react-router-dom';
import { DEMO_RUNTIME_ID } from '../../../demo/runtimeDemoData';
import { usePatientSessions } from '../hooks/usePatientRuntimeView';

export function PatientHomePage() {
  const { data: sessions, loading, source, error } = usePatientSessions();
  const session = sessions[0];

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Patient Portal</h1>
        <p>面向患者的安全问询入口，只展示整理后的自我信息和安全提醒。</p>
      </div>
      {source === 'demo-fallback' ? (
        <p className="audit-access-hint">
          Demo fallback：Patient View API 暂不可用，当前使用本地演示投影。{error ? `原因：${error}` : ''}
        </p>
      ) : null}
      {loading ? <p className="portal-muted">Loading Patient View API...</p> : null}
      <div className="portal-grid portal-grid--two">
        <article className="portal-panel">
          <h2>Recent session</h2>
          {session ? (
            <>
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
                  <dt>Projection</dt>
                  <dd>{session.projection_status ?? source}</dd>
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
                <Link className="portal-link-button" to={`/patient/sessions/${session.session_id ?? DEMO_RUNTIME_ID}/summary`}>
                  View safe summary
                </Link>
                <Link className="portal-link-button portal-link-button--secondary" to="/patient/inquiry">
                  Continue inquiry
                </Link>
              </div>
            </>
          ) : (
            <p className="portal-muted">No patient sessions available.</p>
          )}
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
