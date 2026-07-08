import { Outlet } from 'react-router-dom';
import { DebugContextPanel } from '../auth/DebugContextPanel';
import { useDebugContext } from '../auth/DebugContextProvider';
import { rolesLabel } from '../auth/debugContextTypes';
import { ErrorBanner } from '../components/ErrorBanner';
import './AppShell.css';
import { Sidebar } from './Sidebar';

function apiStatusLabel(apiBaseUrl: string): string {
  return apiBaseUrl.trim() ? apiBaseUrl : '/api → localhost:8080 (proxy)';
}

export function AppShell() {
  const { context } = useDebugContext();

  return (
    <div className="app-shell">
      <header className="app-shell__header">
        <div className="app-shell__brand">
          <h1 className="app-shell__brand-title">ClinMind Console</h1>
          <p className="app-shell__brand-subtitle">Governance Console / Runtime Console · Phase10-P0</p>
        </div>
        <div className="app-shell__status">
          <span className="app-shell__status-item">
            <span className="app-shell__status-label">API</span>
            <span>{apiStatusLabel(context.apiBaseUrl)}</span>
          </span>
          <span className="app-shell__status-item">
            <span className="app-shell__status-label">Actor</span>
            <span>{context.actor || '—'}</span>
          </span>
          <span className="app-shell__status-item">
            <span className="app-shell__status-label">Roles</span>
            <span>{rolesLabel(context.roles)}</span>
          </span>
        </div>
      </header>
      <aside className="app-shell__sidebar">
        <Sidebar />
        <DebugContextPanel />
      </aside>
      <main className="app-shell__main">
        <ErrorBanner />
        <Outlet />
      </main>
    </div>
  );
}
