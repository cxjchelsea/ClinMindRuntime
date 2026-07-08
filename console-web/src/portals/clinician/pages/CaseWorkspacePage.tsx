import { clinicianCaseView } from '../../../demo/runtimeDemoData';
import { CaseFramePanel } from '../components/CaseFramePanel';
import { DdxBoard } from '../components/DdxBoard';
import { EvidencePanel } from '../components/EvidencePanel';
import { ReportDraftEditor } from '../components/ReportDraftEditor';
import { RiskPanel } from '../components/RiskPanel';

export function CaseWorkspacePage() {
  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Case Workspace</h1>
        <p>同一 Runtime 的医生端投影：辅助方向、证据摘要、风险提示和草稿。</p>
      </div>
      <section className="portal-panel">
        <h2>Patient Summary</h2>
        <dl className="portal-definition-list">
          <div>
            <dt>Age band</dt>
            <dd>{clinicianCaseView.patient_summary.age_band}</dd>
          </div>
          <div>
            <dt>Sex</dt>
            <dd>{clinicianCaseView.patient_summary.sex}</dd>
          </div>
          <div>
            <dt>Chief concern</dt>
            <dd>{clinicianCaseView.patient_summary.chief_complaint_summary}</dd>
          </div>
        </dl>
      </section>
      <CaseFramePanel frame={clinicianCaseView.case_frame} />
      <section className="portal-panel">
        <h2>Inquiry Timeline</h2>
        <ol className="portal-ordered-list">
          {clinicianCaseView.inquiry_timeline.map((turn) => (
            <li key={`${turn.timestamp}-${turn.speaker}`}>
              <strong>
                {turn.timestamp} · {turn.speaker}
              </strong>
              <span>{turn.summary}</span>
            </li>
          ))}
        </ol>
      </section>
      <DdxBoard candidates={clinicianCaseView.ddx_board} />
      <EvidencePanel items={clinicianCaseView.evidence_panel} />
      <RiskPanel signals={clinicianCaseView.risk_panel} />
      <section className="portal-panel">
        <h2>AI Suggestions</h2>
        <div className="portal-card-grid">
          {clinicianCaseView.ai_suggestions.map((suggestion) => (
            <article className="portal-card" key={suggestion.label}>
              <h3>{suggestion.label}</h3>
              <p>{suggestion.description}</p>
            </article>
          ))}
        </div>
      </section>
      <ReportDraftEditor draft={clinicianCaseView.report_draft} />
    </section>
  );
}
