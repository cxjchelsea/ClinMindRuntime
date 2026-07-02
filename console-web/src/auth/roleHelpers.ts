import type { DebugRole } from '../auth/debugContextTypes';

export function canReviewCandidate(roles: DebugRole[]): boolean {
  return roles.includes('CANDIDATE_REVIEWER') || roles.includes('SYSTEM_ADMIN');
}

export function canViewAuditCenter(roles: DebugRole[]): boolean {
  return roles.includes('AUDIT_REVIEWER') || roles.includes('SYSTEM_ADMIN');
}
