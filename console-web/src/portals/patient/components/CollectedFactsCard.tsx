import type { PatientFactSummary } from '../../../shared/types/patientViews';

export function CollectedFactsCard({ facts }: { facts: PatientFactSummary[] }) {
  return (
    <section className="portal-panel">
      <h2>Collected facts</h2>
      <div className="portal-card-grid">
        {facts.map((fact) => (
          <article className="portal-card" key={fact.label}>
            <h3>{fact.label}</h3>
            <p>{fact.value}</p>
            <span>{fact.confidence_note}</span>
          </article>
        ))}
      </div>
    </section>
  );
}
