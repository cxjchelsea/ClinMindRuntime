export const DEBUG_HEADERS = {
  token: 'X-Debug-Token',
  actor: 'X-Debug-Actor',
  roles: 'X-Debug-Roles',
  requestId: 'X-Request-Id',
} as const;

export const AVAILABLE_ROLES = [
  'SYSTEM_ADMIN',
  'EVALUATION_REVIEWER',
  'CANDIDATE_REVIEWER',
  'AUDIT_REVIEWER',
  'READ_ONLY_OBSERVER',
] as const;

export type DebugRole = (typeof AVAILABLE_ROLES)[number];

export interface DebugContext {
  apiBaseUrl: string;
  debugToken: string;
  actor: string;
  roles: DebugRole[];
}

export const DEFAULT_DEBUG_CONTEXT: DebugContext = {
  apiBaseUrl: '',
  debugToken: '',
  actor: 'console-user',
  roles: ['READ_ONLY_OBSERVER'],
};

const STORAGE_KEYS = {
  apiBaseUrl: 'clinmind.console.apiBaseUrl',
  actor: 'clinmind.console.actor',
  roles: 'clinmind.console.roles',
} as const;

export function loadPersistedDebugContext(): Partial<DebugContext> {
  try {
    const apiBaseUrl = localStorage.getItem(STORAGE_KEYS.apiBaseUrl);
    const actor = localStorage.getItem(STORAGE_KEYS.actor);
    const rolesRaw = localStorage.getItem(STORAGE_KEYS.roles);
    const roles = rolesRaw
      ? rolesRaw
          .split(',')
          .map((r) => r.trim())
          .filter((r): r is DebugRole => AVAILABLE_ROLES.includes(r as DebugRole))
      : undefined;
    return {
      ...(apiBaseUrl != null ? { apiBaseUrl } : {}),
      ...(actor != null ? { actor } : {}),
      ...(roles?.length ? { roles } : {}),
    };
  } catch {
    return {};
  }
}

export function persistDebugContext(context: DebugContext): void {
  try {
    localStorage.setItem(STORAGE_KEYS.apiBaseUrl, context.apiBaseUrl);
    localStorage.setItem(STORAGE_KEYS.actor, context.actor);
    localStorage.setItem(STORAGE_KEYS.roles, context.roles.join(','));
  } catch {
    // ignore storage failures in MVP
  }
}

export function rolesLabel(roles: DebugRole[]): string {
  return roles.length ? roles.join(', ') : '—';
}
