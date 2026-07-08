import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import App from '../App';
import { DemoRoleProvider } from '../rbac/DemoRoleProvider';

vi.mock('../auth/DebugContextProvider', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../auth/DebugContextProvider')>();
  return {
    ...actual,
    useDebugContext: () => ({
      context: {
        apiBaseUrl: '',
        debugToken: 'route-test-token',
        actor: 'route-test-user',
        roles: ['SYSTEM_ADMIN'],
      },
      client: {
        testConnection: vi.fn(),
        getConsoleOverview: vi.fn().mockResolvedValue({
          runtime_count: 0,
          provider_call_count: 0,
          tool_invocation_count: 0,
          model_governance_record_count: 0,
          candidate_count: 0,
          audit_event_count: 0,
          domain_cards: [],
        }),
        getAuditSummary: vi.fn().mockResolvedValue({
          total_count: 0,
          count_by_action_type: {},
          count_by_resource_type: {},
          count_by_result_status: {},
          recent_failures: [],
          recent_review_actions: [],
        }),
      },
      globalError: null,
      connectionOk: null,
      setContext: vi.fn(),
      setGlobalError: vi.fn(),
      testConnection: vi.fn(),
      clearGlobalError: vi.fn(),
    }),
  };
});

function renderApp(initialRole: 'PATIENT' | 'GOVERNANCE_REVIEWER') {
  return render(
    <DemoRoleProvider initialRole={initialRole}>
      <MemoryRouter initialEntries={['/governance/overview']}>
        <App />
      </MemoryRouter>
    </DemoRoleProvider>,
  );
}

describe('Governance route access', () => {
  it('allows governance reviewer into the governance overview', async () => {
    renderApp('GOVERNANCE_REVIEWER');
    expect(await screen.findByRole('heading', { name: 'Governance Overview' })).toBeInTheDocument();
  });

  it('blocks patient from governance overview', async () => {
    renderApp('PATIENT');
    expect(await screen.findByRole('heading', { name: 'Forbidden' })).toBeInTheDocument();
  });
});
