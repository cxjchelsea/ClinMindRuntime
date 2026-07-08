import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { DemoRoleProvider } from '../rbac/DemoRoleProvider';
import { RoleGuard } from '../rbac/RoleGuard';

function renderGuard(initialRole: 'PATIENT' | 'GOVERNANCE_REVIEWER') {
  return render(
    <DemoRoleProvider initialRole={initialRole}>
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route
            path="/protected"
            element={
              <RoleGuard portal="governance" permission="governance:read_audit">
                <h1>Allowed governance page</h1>
              </RoleGuard>
            }
          />
          <Route path="/forbidden" element={<h1>Forbidden target</h1>} />
        </Routes>
      </MemoryRouter>
    </DemoRoleProvider>,
  );
}

describe('RoleGuard', () => {
  it('renders children for an allowed role', () => {
    renderGuard('GOVERNANCE_REVIEWER');
    expect(screen.getByRole('heading', { name: 'Allowed governance page' })).toBeInTheDocument();
  });

  it('redirects denied roles to forbidden', () => {
    renderGuard('PATIENT');
    expect(screen.getByRole('heading', { name: 'Forbidden target' })).toBeInTheDocument();
  });
});
