import { describe, expect, it, vi, afterEach } from 'vitest';
import { buildDebugHeaders, createConsoleClient } from '../api/consoleClient';
import type { DebugContext } from '../auth/debugContextTypes';

const TEST_CONTEXT: DebugContext = {
  apiBaseUrl: '',
  debugToken: 'secret-token',
  actor: 'test-actor',
  roles: ['EVALUATION_REVIEWER', 'READ_ONLY_OBSERVER'],
};

describe('consoleClient headers', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('injects debug headers on every request', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ success: true, data: [] }),
    });
    vi.stubGlobal('fetch', fetchMock);
    vi.stubGlobal('crypto', { randomUUID: () => 'req-test-001' });

    const client = createConsoleClient(() => TEST_CONTEXT);
    await client.listRuntimeSessions({ limit: 1 });

    expect(fetchMock).toHaveBeenCalledOnce();
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    const headers = init.headers as Record<string, string>;
    expect(headers['X-Debug-Token']).toBe('secret-token');
    expect(headers['X-Debug-Actor']).toBe('test-actor');
    expect(headers['X-Debug-Roles']).toBe('EVALUATION_REVIEWER,READ_ONLY_OBSERVER');
    expect(headers['X-Request-Id']).toBe('req-test-001');
  });

  it('maps 403 api errors', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 403,
        json: async () => ({
          success: false,
          error: { code: 'ACCESS_DENIED', message: 'denied' },
        }),
      }),
    );

    const client = createConsoleClient(() => TEST_CONTEXT);
    await expect(client.getAuditSummary()).rejects.toMatchObject({
      code: 'ACCESS_DENIED',
      httpStatus: 403,
    });
  });
});

describe('buildDebugHeaders', () => {
  it('omits token header when token empty', () => {
    const headers = buildDebugHeaders(
      { ...TEST_CONTEXT, debugToken: '' },
      'req-1',
    ) as Record<string, string>;
    expect(headers['X-Debug-Token']).toBeUndefined();
    expect(headers['X-Request-Id']).toBe('req-1');
  });
});
