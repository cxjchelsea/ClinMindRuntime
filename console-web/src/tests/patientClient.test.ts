import { describe, expect, it, vi } from 'vitest';
import { createPatientClient } from '../portals/patient/api/patientClient';

const context = {
  apiBaseUrl: '',
  debugToken: 'test-secret',
  actor: 'patient-a',
  roles: ['PATIENT' as const],
};

describe('patientClient', () => {
  it('reads patient sessions through the role-specific API', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({
        success: true,
        data: [{ session_id: 'runtime-demo-001', runtime_id: 'runtime-demo-001' }],
      }),
    });
    vi.stubGlobal('fetch', fetchMock);

    const client = createPatientClient(() => context);
    const result = await client.listPatientSessions();

    expect(result[0].runtime_id).toBe('runtime-demo-001');
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/patient/sessions',
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Debug-Roles': 'PATIENT',
          'X-Debug-Token': 'test-secret',
        }),
      }),
    );

    vi.unstubAllGlobals();
  });
});
