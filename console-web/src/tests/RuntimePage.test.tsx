import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { RuntimePage } from '../pages/RuntimePage';

const mockList = vi.fn();
const mockGet = vi.fn();

vi.mock('../auth/DebugContextProvider', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../auth/DebugContextProvider')>();
  return {
    ...actual,
    useDebugContext: () => ({
      context: {
        apiBaseUrl: '',
        debugToken: 'test-token',
        actor: 'test-user',
        roles: ['READ_ONLY_OBSERVER'],
      },
      client: {
        listRuntimeSessions: mockList,
        getRuntimeSession: mockGet,
      } as unknown as ConsoleClient,
      globalError: null,
      connectionOk: null,
      setContext: vi.fn(),
      setGlobalError: vi.fn(),
      testConnection: vi.fn(),
      clearGlobalError: vi.fn(),
    }),
  };
});

function renderRuntimePage() {
  return render(
    <MemoryRouter initialEntries={['/runtime']}>
      <Routes>
        <Route path="/runtime" element={<RuntimePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('RuntimePage', () => {
  beforeEach(() => {
    mockList.mockReset();
    mockGet.mockReset();
  });

  it('loads runtime list and shows safe detail without sensitive fields', async () => {
    mockList.mockResolvedValue([
      {
        runtime_id: 'rt_test_001',
        session_id: 'session-a',
        runtime_status: 'COMPLETED',
        mode: 'patient_facing',
        asset_package_id: 'phase2-default',
        asset_package_version: '0.2.0',
        version: 1,
        trace_count: 4,
        created_at: '2026-07-01T08:00:00Z',
        updated_at: '2026-07-01T08:05:00Z',
      },
    ]);

    mockGet.mockResolvedValue({
      runtime_id: 'rt_test_001',
      session_id: 'session-a',
      runtime_status: 'COMPLETED',
      work_mode: 'standard',
      mode: 'patient_facing',
      asset_package_id: 'phase2-default',
      asset_package_version: '0.2.0',
      version: 1,
      trace_count: 4,
      safety_gate_triggered: false,
      patient_output: 'should-not-render',
      input_history: ['hidden'],
      created_at: '2026-07-01T08:00:00Z',
      updated_at: '2026-07-01T08:05:00Z',
    });

    const user = userEvent.setup();
    renderRuntimePage();

    expect(await screen.findByText('rt_test_001')).toBeInTheDocument();
    await user.click(screen.getByText('rt_test_001'));

    await waitFor(() => expect(mockGet).toHaveBeenCalledWith('rt_test_001'));

    const detail = await screen.findByRole('heading', { name: /Runtime rt_test_001/ });
    const panel = detail.closest('section');
    expect(panel).not.toBeNull();
    expect(within(panel!).getByText('session-a')).toBeInTheDocument();
    expect(within(panel!).queryByText(/patient_output/)).not.toBeInTheDocument();
    expect(within(panel!).queryByText(/should-not-render/)).not.toBeInTheDocument();
    expect(within(panel!).queryByText(/input_history/)).not.toBeInTheDocument();
  });

  it('shows empty state when list is empty', async () => {
    mockList.mockResolvedValue([]);
    renderRuntimePage();

    expect(await screen.findByText(/暂无 Runtime 会话/)).toBeInTheDocument();
  });
});
