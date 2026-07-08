import type { ReportDraftView } from '../../../shared/types/clinicianViews';

export function ReportDraftEditor({ draft }: { draft: ReportDraftView }) {
  return (
    <section className="portal-panel">
      <h2>Report Draft</h2>
      <p className="portal-muted">P0 草稿仅本地展示，医生保留最终判断权。</p>
      <label className="portal-form-field">
        Impression
        <textarea defaultValue={draft.impression} />
      </label>
      <h3>Suggested follow-up questions</h3>
      <ul className="portal-list">
        {draft.suggested_questions.map((question) => (
          <li key={question}>{question}</li>
        ))}
      </ul>
      <p>{draft.clinician_note}</p>
    </section>
  );
}
