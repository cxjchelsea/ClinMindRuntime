import type { RiskSignalView } from '../../../shared/types/clinicianViews';

export function RiskPanel({ signals }: { signals: RiskSignalView[] }) {
  return (
    <section className="portal-panel">
      <h2>Risk Panel</h2>
      <div className="portal-list">
        {signals.map((signal) => (
          <p key={signal.label} className={`risk-signal risk-signal--${signal.level}`}>
            <strong>{signal.label}</strong>
            <span>{signal.note}</span>
          </p>
        ))}
      </div>
    </section>
  );
}
