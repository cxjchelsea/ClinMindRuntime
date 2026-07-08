import type { SafetyNotice } from '../../../shared/types/patientViews';

export function PatientSafetyNotice({ notices }: { notices: SafetyNotice[] }) {
  return (
    <section className="portal-panel" aria-label="Safety notices">
      <h2>Safety notices</h2>
      <div className="portal-list">
        {notices.map((notice) => (
          <p key={notice.message} className={`patient-notice patient-notice--${notice.level}`}>
            {notice.message}
          </p>
        ))}
      </div>
    </section>
  );
}
