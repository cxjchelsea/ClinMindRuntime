import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { ClinicianCaseInboxPage } from '../portals/clinician/pages/ClinicianCaseInboxPage';

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'test-secret',
      actor: 'clinician-a',
      roles: ['CLINICIAN'],
    },
  }),
}));

describe('ClinicianWorkspaceApiFallback', () => {
  it('shows demo fallback when Clinician View API is unavailable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('backend offline')));

    render(
      <MemoryRouter>
        <ClinicianCaseInboxPage />
      </MemoryRouter>,
    );

    expect(await screen.findByText(/Demo fallback/)).toBeInTheDocument();
    expect(screen.getByText('runtime-demo-001')).toBeInTheDocument();

    vi.unstubAllGlobals();
  });
});
