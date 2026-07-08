import { NavLink } from 'react-router-dom';
import './Sidebar.css';

const NAV_ITEMS = [
  { to: '/overview', label: 'Overview' },
  { to: '/runtime-timeline', label: 'Runtime Timeline' },
  { to: '/governance-domains', label: 'Governance Domains' },
  { to: '/candidate-inbox', label: 'Candidate Inbox' },
  { to: '/audit-browser', label: 'Audit Browser' },
  { to: '/runtime', label: 'Runtime Sessions' },
  { to: '/evaluation', label: 'Evaluation Runs' },
  { to: '/candidates', label: 'Candidates' },
  { to: '/review-queue', label: 'Review Queue' },
  { to: '/audit-center', label: 'Audit Center' },
] as const;

export function Sidebar() {
  return (
    <nav className="sidebar" aria-label="Console navigation">
      <p className="sidebar__section-label">Governance</p>
      {NAV_ITEMS.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) =>
            `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
          }
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  );
}
