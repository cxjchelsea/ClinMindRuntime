import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { EvaluationPage } from '../pages/EvaluationPage';

const mockList = vi.fn();
const mockGet = vi.fn();

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'test-token',
      actor: 'test-user',
      roles: ['EVALUATION_REVIEWER'],
    },
    client: {
      listEvaluationRuns: mockList,
      getEvaluationRun: mockGet,
    } as unknown as ConsoleClient,
    globalError: null,
    connectionOk: null,
    setContext: vi.fn(),
    setGlobalError: vi.fn(),
    testConnection: vi.fn(),
    clearGlobalError: vi.fn(),
  }),
}));

function renderEvaluationPage() {
  return render(
    <MemoryRouter initialEntries={['/evaluation']}>
      <Routes>
        <Route path="/evaluation" element={<EvaluationPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('EvaluationPage', () => {
  beforeEach(() => {
    mockList.mockReset();
    mockGet.mockReset();
  });

  it('loads evaluation runs and item summary on selection', async () => {
    mockList.mockResolvedValue([
      {
        run_id: 'eval_run_001',
        case_set_id: 'phase3-default',
        case_set_version: '0.3.0',
        asset_package_id: 'phase2-default',
        asset_package_version: '0.2.0',
        status: 'COMPLETED',
        item_count: 1,
        started_at: '2026-07-01T09:00:00Z',
        completed_at: '2026-07-01T09:10:00Z',
      },
    ]);

    mockGet.mockResolvedValue({
      run_id: 'eval_run_001',
      case_set_id: 'phase3-default',
      case_set_version: '0.3.0',
      asset_package_id: 'phase2-default',
      asset_package_version: '0.2.0',
      status: 'COMPLETED',
      total_cases: 1,
      passed_cases: 1,
      failed_cases: 0,
      pass_rate: 1,
      item_summaries: [
        {
          case_id: 'case_001',
          runtime_id: 'rt_eval_001',
          passed: true,
          score: 0.92,
        },
      ],
    });

    const user = userEvent.setup();
    renderEvaluationPage();

    expect(await screen.findByText('eval_run_001')).toBeInTheDocument();
    await user.click(screen.getByText('eval_run_001'));

    await waitFor(() => expect(mockGet).toHaveBeenCalledWith('eval_run_001'));
    expect(await screen.findByText('case_001')).toBeInTheDocument();
    expect(screen.getByText('0.92')).toBeInTheDocument();
  });
});
