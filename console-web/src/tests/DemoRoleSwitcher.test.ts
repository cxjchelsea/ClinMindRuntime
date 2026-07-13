import { describe, expect, it } from 'vitest';
import { debugRolesForAppRole } from '../rbac/DemoRoleSwitcher';

describe('DemoRoleSwitcher debug role mapping', () => {
  it.each([
    ['PATIENT', ['PATIENT']],
    ['CLINICIAN', ['CLINICIAN']],
    ['GOVERNANCE_REVIEWER', ['READ_ONLY_OBSERVER']],
    ['READ_ONLY_OBSERVER', ['READ_ONLY_OBSERVER']],
    ['SYSTEM_ADMIN', ['SYSTEM_ADMIN']],
  ] as const)('maps %s to backend debug roles', (role, expected) => {
    expect(debugRolesForAppRole(role)).toEqual(expected);
  });
});