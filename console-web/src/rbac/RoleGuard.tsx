import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useDemoRole } from './DemoRoleProvider';
import {
  canAccessPortal,
  hasPermission,
  type Permission,
  type Portal,
} from './rbac';

export function RoleGuard({
  children,
  portal,
  permission,
}: {
  children: ReactNode;
  portal?: Portal;
  permission?: Permission;
}) {
  const { role } = useDemoRole();
  const location = useLocation();
  const portalAllowed = portal ? canAccessPortal(role, portal) : true;
  const permissionAllowed = permission ? hasPermission(role, permission) : true;

  if (!portalAllowed || !permissionAllowed) {
    return <Navigate to="/forbidden" replace state={{ from: location.pathname }} />;
  }

  return <>{children}</>;
}
