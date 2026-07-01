import { Outlet } from 'react-router-dom';
import './AppShell.css';
import { Sidebar } from './Sidebar';

export function AppShell() {
  return (
    <div className="app-shell">
      <header className="app-shell__header">
        <div className="app-shell__brand">
          <h1 className="app-shell__brand-title">ClinMind Console</h1>
          <p className="app-shell__brand-subtitle">Governance MVP · Phase 5-P2</p>
        </div>
        <div className="app-shell__status">
          <span className="app-shell__status-item">
            <span className="app-shell__status-label">API</span>
            <span>/api → localhost:8080</span>
          </span>
          <span className="app-shell__status-item">
            <span className="app-shell__status-label">Actor</span>
            <span>—</span>
          </span>
          <span className="app-shell__status-item">
            <span className="app-shell__status-label">Roles</span>
            <span>—</span>
          </span>
        </div>
      </header>
      <aside className="app-shell__sidebar">
        <Sidebar />
      </aside>
      <main className="app-shell__main">
        <Outlet />
      </main>
    </div>
  );
}
