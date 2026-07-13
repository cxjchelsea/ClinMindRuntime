import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { PatientHomePage } from '../portals/patient/pages/PatientHomePage';

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'test-secret',
      actor: 'patient-a',
      roles: ['PATIENT'],
    },
  }),
}));

describe('PatientPortalApiFallback', () => {
  it('shows demo fallback when Patient View API is unavailable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('backend offline')));

    render(
      <MemoryRouter>
        <PatientHomePage />
      </MemoryRouter>,
    );

    expect(await screen.findByText(/Demo fallback/)).toBeInTheDocument();
    expect(screen.getByText('runtime-demo-001')).toBeInTheDocument();

    vi.unstubAllGlobals();
  });
});
