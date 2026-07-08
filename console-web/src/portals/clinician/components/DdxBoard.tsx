import type { DdxCandidateView } from '../../../shared/types/clinicianViews';

export function DdxBoard({ candidates }: { candidates: DdxCandidateView[] }) {
  return (
    <section className="portal-panel">
      <h2>DDx Board</h2>
      <p className="portal-muted">候选方向仅用于医生复核，不是最终诊断。</p>
      <div className="portal-card-grid">
        {candidates.map((candidate) => (
          <article className="portal-card" key={candidate.name}>
            <h3>{candidate.name}</h3>
            <span className="portal-pill">{candidate.likelihood}</span>
            <p>{candidate.supporting_summary}</p>
            <small>{candidate.uncertainty_note}</small>
          </article>
        ))}
      </div>
    </section>
  );
}
