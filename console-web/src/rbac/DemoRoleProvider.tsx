import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type { AppRole } from './rbac';

interface DemoRoleContextValue {
  role: AppRole;
  setRole: (role: AppRole) => void;
}

const DemoRoleContext = createContext<DemoRoleContextValue | null>(null);

export function DemoRoleProvider({
  children,
  initialRole = 'GOVERNANCE_REVIEWER',
}: {
  children: ReactNode;
  initialRole?: AppRole;
}) {
  const [role, setRole] = useState<AppRole>(initialRole);
  const value = useMemo(() => ({ role, setRole }), [role]);

  return <DemoRoleContext.Provider value={value}>{children}</DemoRoleContext.Provider>;
}

export function useDemoRole(): DemoRoleContextValue {
  const value = useContext(DemoRoleContext);
  if (!value) {
    throw new Error('useDemoRole must be used within DemoRoleProvider');
  }
  return value;
}
