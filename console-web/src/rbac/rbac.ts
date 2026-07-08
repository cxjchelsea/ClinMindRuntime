export type AppRole =
  | 'PATIENT'
  | 'CLINICIAN'
  | 'GOVERNANCE_REVIEWER'
  | 'SYSTEM_ADMIN'
  | 'READ_ONLY_OBSERVER';

export type Portal = 'patient' | 'clinician' | 'governance' | 'admin';

export type Permission =
  | 'patient:read_self'
  | 'patient:create_intake'
  | 'clinician:read_case'
  | 'clinician:edit_report_draft'
  | 'governance:read_overview'
  | 'governance:read_runtime_timeline'
  | 'governance:read_candidate_inbox'
  | 'governance:read_audit'
  | 'admin:read_settings';

export const ROLE_PERMISSIONS: Record<AppRole, Permission[]> = {
  PATIENT: ['patient:read_self', 'patient:create_intake'],
  CLINICIAN: ['clinician:read_case', 'clinician:edit_report_draft'],
  GOVERNANCE_REVIEWER: [
    'governance:read_overview',
    'governance:read_runtime_timeline',
    'governance:read_candidate_inbox',
    'governance:read_audit',
  ],
  SYSTEM_ADMIN: [
    'governance:read_overview',
    'governance:read_runtime_timeline',
    'governance:read_candidate_inbox',
    'governance:read_audit',
    'admin:read_settings',
  ],
  READ_ONLY_OBSERVER: [
    'governance:read_overview',
    'governance:read_runtime_timeline',
    'governance:read_candidate_inbox',
    'governance:read_audit',
  ],
};

const PORTAL_ROLES: Record<Portal, AppRole[]> = {
  patient: ['PATIENT'],
  clinician: ['CLINICIAN'],
  governance: ['GOVERNANCE_REVIEWER', 'SYSTEM_ADMIN', 'READ_ONLY_OBSERVER'],
  admin: ['SYSTEM_ADMIN'],
};

const DEFAULT_ROUTES: Record<AppRole, string> = {
  PATIENT: '/patient',
  CLINICIAN: '/clinician/cases',
  GOVERNANCE_REVIEWER: '/governance/overview',
  SYSTEM_ADMIN: '/governance/overview',
  READ_ONLY_OBSERVER: '/governance/overview',
};

export function hasPermission(role: AppRole, permission: Permission): boolean {
  return ROLE_PERMISSIONS[role].includes(permission);
}

export function canAccessPortal(role: AppRole, portal: Portal): boolean {
  return PORTAL_ROLES[portal].includes(role);
}

export function getDefaultRouteForRole(role: AppRole): string {
  return DEFAULT_ROUTES[role];
}
