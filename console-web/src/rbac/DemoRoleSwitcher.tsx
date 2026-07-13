import { useNavigate } from 'react-router-dom';
import { useDebugContext } from '../auth/DebugContextProvider';
import type { DebugRole } from '../auth/debugContextTypes';
import { useDemoRole } from './DemoRoleProvider';
import { getDefaultRouteForRole, type AppRole } from './rbac';

const ROLE_OPTIONS: { value: AppRole; label: string }[] = [
  { value: 'PATIENT', label: 'Patient' },
  { value: 'CLINICIAN', label: 'Clinician' },
  { value: 'GOVERNANCE_REVIEWER', label: 'Governance' },
  { value: 'READ_ONLY_OBSERVER', label: 'Read only' },
  { value: 'SYSTEM_ADMIN', label: 'Admin' },
];

export function debugRolesForAppRole(role: AppRole): DebugRole[] {
  return role === 'GOVERNANCE_REVIEWER' ? ['READ_ONLY_OBSERVER'] : [role];
}

export function DemoRoleSwitcher() {
  const { role, setRole } = useDemoRole();
  const { setContext } = useDebugContext();
  const navigate = useNavigate();

  return (
    <label className="demo-role-switcher">
      <span>Demo role</span>
      <select
        value={role}
        onChange={(event) => {
          const nextRole = event.target.value as AppRole;
          setRole(nextRole);
          setContext({ roles: debugRolesForAppRole(nextRole) });
          navigate(getDefaultRouteForRole(nextRole));
        }}
      >
        {ROLE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}
