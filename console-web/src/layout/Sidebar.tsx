import { NavLink } from 'react-router-dom';
import { useDemoRole } from '../rbac/DemoRoleProvider';
import { canAccessPortal } from '../rbac/rbac';
import './Sidebar.css';

const PATIENT_NAV = [
  { to: '/patient', label: 'Patient Home' },
  { to: '/patient/intake', label: 'Symptom Intake' },
  { to: '/patient/inquiry', label: 'Guided Inquiry' },
  { to: '/patient/sessions/runtime-demo-001/summary', label: 'Safe Summary' },
] as const;

const CLINICIAN_NAV = [
  { to: '/clinician', label: 'Dashboard' },
  { to: '/clinician/cases', label: 'Case Inbox' },
  { to: '/clinician/cases/runtime-demo-001', label: 'Case Workspace' },
  { to: '/clinician/cases/runtime-demo-001/report', label: 'Report Draft' },
] as const;

const GOVERNANCE_NAV = [
  { to: '/governance/overview', label: 'Overview' },
  { to: '/governance/runtime-timeline', label: 'Runtime Timeline' },
  { to: '/governance/runtimes/runtime-demo-001', label: 'Demo Runtime' },
  { to: '/governance/domains', label: 'Governance Domains' },
  { to: '/governance/candidate-inbox', label: 'Candidate Inbox' },
  { to: '/governance/audits', label: 'Audit Browser' },
  { to: '/governance/runtime', label: 'Runtime Sessions' },
  { to: '/governance/evaluations', label: 'Evaluation Runs' },
  { to: '/governance/candidates', label: 'Candidates' },
  { to: '/governance/review-queue', label: 'Review Queue' },
  { to: '/governance/audit-center', label: 'Audit Center' },
] as const;

function renderLinks(items: readonly { to: string; label: string }[]) {
  return items.map((item) => (
    <NavLink
      key={item.to}
      to={item.to}
      className={({ isActive }) =>
        `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
      }
    >
      {item.label}
    </NavLink>
  ));
}

export function Sidebar() {
  const { role } = useDemoRole();

  return (
    <nav className="sidebar" aria-label="Portal navigation">
      <p className="sidebar__section-label">Demo walkthrough</p>
      <NavLink className="sidebar__link" to="/patient/sessions/runtime-demo-001/summary">
        Patient projection
      </NavLink>
      <NavLink className="sidebar__link" to="/clinician/cases/runtime-demo-001">
        Clinician projection
      </NavLink>
      <NavLink className="sidebar__link" to="/governance/runtimes/runtime-demo-001">
        Governance projection
      </NavLink>

      {canAccessPortal(role, 'patient') ? (
        <>
          <p className="sidebar__section-label">Patient</p>
          {renderLinks(PATIENT_NAV)}
        </>
      ) : null}

      {canAccessPortal(role, 'clinician') ? (
        <>
          <p className="sidebar__section-label">Clinician</p>
          {renderLinks(CLINICIAN_NAV)}
        </>
      ) : null}

      {canAccessPortal(role, 'governance') ? (
        <>
          <p className="sidebar__section-label">Governance</p>
          {renderLinks(GOVERNANCE_NAV)}
        </>
      ) : null}
    </nav>
  );
}
