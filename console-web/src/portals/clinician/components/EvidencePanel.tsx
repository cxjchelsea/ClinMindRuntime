import type { EvidenceItemView } from '../../../shared/types/clinicianViews';

export function EvidencePanel({ items }: { items: EvidenceItemView[] }) {
  return (
    <section className="portal-panel">
      <h2>Evidence Panel</h2>
      <p className="portal-muted">仅展示摘要、来源和相关性，不展示原始载荷。</p>
      <div className="portal-card-grid">
        {items.map((item) => (
          <article className="portal-card" key={item.title}>
            <h3>{item.title}</h3>
            <span>{item.source}</span>
            <p>{item.summary}</p>
            <small>{item.relevance}</small>
          </article>
        ))}
      </div>
    </section>
  );
}
