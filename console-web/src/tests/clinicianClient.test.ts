import { describe, expect, it, vi } from 'vitest';
import { createClinicianClient } from '../portals/clinician/api/clinicianClient';

const context = {
  apiBaseUrl: '',
  debugToken: 'test-secret',
  actor: 'clinician-a',
  roles: ['CLINICIAN' as const],
};

describe('clinicianClient', () => {
  it('reads clinician cases through the role-specific API', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({
        success: true,
        data: [{ case_id: 'runtime-demo-001', runtime_id: 'runtime-demo-001' }],
      }),
    });
    vi.stubGlobal('fetch', fetchMock);

    const client = createClinicianClient(() => context);
    const result = await client.listClinicianCases();

    expect(result[0].case_id).toBe('runtime-demo-001');
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/clinician/cases',
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Debug-Roles': 'CLINICIAN',
          'X-Debug-Token': 'test-secret',
        }),
      }),
    );

    vi.unstubAllGlobals();
  });
});
