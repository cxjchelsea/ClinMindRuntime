import { describe, expect, it } from 'vitest';
import { canAccessPortal, getDefaultRouteForRole, hasPermission } from '../rbac/rbac';

describe('rbac', () => {
  it('keeps patient and clinician permissions scoped to their portals', () => {
    expect(canAccessPortal('PATIENT', 'patient')).toBe(true);
    expect(canAccessPortal('PATIENT', 'clinician')).toBe(false);
    expect(canAccessPortal('PATIENT', 'governance')).toBe(false);
    expect(hasPermission('PATIENT', 'patient:read_self')).toBe(true);
    expect(hasPermission('PATIENT', 'governance:read_audit')).toBe(false);

    expect(canAccessPortal('CLINICIAN', 'clinician')).toBe(true);
    expect(hasPermission('CLINICIAN', 'clinician:read_case')).toBe(true);
    expect(hasPermission('CLINICIAN', 'governance:read_overview')).toBe(false);
  });

  it('allows governance read roles and returns role default routes', () => {
    expect(canAccessPortal('GOVERNANCE_REVIEWER', 'governance')).toBe(true);
    expect(canAccessPortal('READ_ONLY_OBSERVER', 'governance')).toBe(true);
    expect(hasPermission('SYSTEM_ADMIN', 'admin:read_settings')).toBe(true);
    expect(getDefaultRouteForRole('PATIENT')).toBe('/patient');
    expect(getDefaultRouteForRole('CLINICIAN')).toBe('/clinician/cases');
    expect(getDefaultRouteForRole('GOVERNANCE_REVIEWER')).toBe('/governance/overview');
  });
});
