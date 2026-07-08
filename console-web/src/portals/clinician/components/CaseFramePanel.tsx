import type { CaseFrameView } from '../../../shared/types/clinicianViews';

export function CaseFramePanel({ frame }: { frame: CaseFrameView }) {
  return (
    <section className="portal-panel">
      <h2>Case Frame</h2>
      <p>{frame.current_problem}</p>
      <div className="portal-grid portal-grid--two">
        <div>
          <h3>Known context</h3>
          <ul className="portal-list">
            {frame.known_context.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </div>
        <div>
          <h3>Missing information</h3>
          <ul className="portal-list">
            {frame.missing_information.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
