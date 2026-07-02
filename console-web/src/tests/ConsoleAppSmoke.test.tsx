import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import App from '../App';

const emptySummary = {
  total_count: 0,
  count_by_action_type: {},
  count_by_resource_type: {},
  count_by_result_status: {},
  recent_failures: [],
  recent_review_actions: [],
};

function createMockClient(): ConsoleClient {
  return {
    listRuntimeSessions: vi.fn().mockResolvedValue([]),
    getRuntimeSession: vi.fn().mockResolvedValue({}),
    listEvaluationRuns: vi.fn().mockResolvedValue([]),
    getEvaluationRun: vi.fn().mockResolvedValue({ item_summaries: [] }),
    listCandidates: vi.fn().mockResolvedValue([]),
    getCandidate: vi.fn().mockResolvedValue({}),
    listReviewQueue: vi.fn().mockResolvedValue([]),
    reviewExperienceCandidate: vi.fn(),
    reviewTrainingExampleCandidate: vi.fn(),
    getAuditSummary: vi.fn().mockResolvedValue(emptySummary),
    listAuditLogs: vi.fn().mockResolvedValue([]),
    getAuditLog: vi.fn().mockResolvedValue({}),
    testConnection: vi.fn().mockResolvedValue([]),
  } as unknown as ConsoleClient;
}

vi.mock('../auth/DebugContextProvider', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../auth/DebugContextProvider')>();
  return {
    ...actual,
    DebugContextProvider: ({ children }: { children: React.ReactNode }) => children,
    useDebugContext: () => ({
      context: {
        apiBaseUrl: '',
        debugToken: 'smoke-token',
        actor: 'smoke-user',
        roles: ['SYSTEM_ADMIN'],
      },
      client: createMockClient(),
      globalError: null,
      connectionOk: null,
      setContext: vi.fn(),
      setGlobalError: vi.fn(),
      testConnection: vi.fn(),
      clearGlobalError: vi.fn(),
    }),
  };
});

function renderApp(route = '/runtime') {
  return render(
    <MemoryRouter initialEntries={[route]}>
      <App />
    </MemoryRouter>,
  );
}

describe('ConsoleAppSmoke', () => {
  it('renders all governance pages from sidebar navigation', async () => {
    const user = userEvent.setup();
    renderApp('/runtime');

    expect(screen.getByRole('heading', { name: 'Runtime Sessions' })).toBeInTheDocument();
    expect(await screen.findByText(/暂无 Runtime 会话/)).toBeInTheDocument();

    await user.click(screen.getByRole('link', { name: 'Evaluation Runs' }));
    expect(await screen.findByRole('heading', { name: 'Evaluation Runs' })).toBeInTheDocument();

    await user.click(screen.getByRole('link', { name: 'Candidates' }));
    expect(await screen.findByRole('heading', { name: 'Candidates' })).toBeInTheDocument();

    await user.click(screen.getByRole('link', { name: 'Review Queue' }));
    expect(await screen.findByRole('heading', { name: 'Review Queue' })).toBeInTheDocument();
    expect(screen.getByText(/自动上线经验或进入训练集/)).toBeInTheDocument();

    await user.click(screen.getByRole('link', { name: 'Audit Center' }));
    expect(await screen.findByRole('heading', { name: 'Audit Center' })).toBeInTheDocument();
    expect(await screen.findByText('Total: 0')).toBeInTheDocument();
  });

  it('redirects index route to runtime', async () => {
    renderApp('/');

    expect(await screen.findByRole('heading', { name: 'Runtime Sessions' })).toBeInTheDocument();
  });
});
