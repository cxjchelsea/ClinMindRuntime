import { Link, useLocation } from 'react-router-dom';
import { useDemoRole } from '../rbac/DemoRoleProvider';
import { getDefaultRouteForRole } from '../rbac/rbac';

export function ForbiddenPage() {
  const { role } = useDemoRole();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from;

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Forbidden</h1>
        <p>
          当前演示角色不能访问该 Portal。RoleGuard 只用于 P0 前端演示，不代表生产级认证。
        </p>
      </div>
      <div className="portal-panel">
        <p className="portal-muted">Role: {role}</p>
        {from ? <p className="portal-muted">Blocked route: {from}</p> : null}
        <Link className="portal-link-button" to={getDefaultRouteForRole(role)}>
          Back to allowed portal
        </Link>
      </div>
    </section>
  );
}
