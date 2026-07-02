import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { CandidatePage } from '../pages/CandidatePage';

const mockList = vi.fn();
const mockGet = vi.fn();

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'test-token',
      actor: 'test-user',
      roles: ['READ_ONLY_OBSERVER'],
    },
    client: {
      listCandidates: mockList,
      getCandidate: mockGet,
    } as unknown as ConsoleClient,
    globalError: null,
    connectionOk: null,
    setContext: vi.fn(),
    setGlobalError: vi.fn(),
    testConnection: vi.fn(),
    clearGlobalError: vi.fn(),
  }),
}));

describe('CandidatePage', () => {
  beforeEach(() => {
    mockList.mockReset();
    mockGet.mockReset();
  });

  it('renders candidate detail without raw training input fields', async () => {
    mockList.mockResolvedValue([
      {
        candidate_id: 'cand_test_001',
        candidate_kind: 'EXPERIENCE_CANDIDATE',
        candidate_type: 'SAFETY_LESSON',
        review_status: 'REVIEW_REQUIRED',
        risk_level: 'HIGH',
        sanitization_status: 'SANITIZED',
        title: 'Safety lesson',
        tags: ['high_risk'],
        source_ref: {
          source_type: 'EVALUATION',
          evaluation_run_id: 'run_1',
          case_id: 'case_1',
          asset_package_id: 'pkg',
          asset_package_version: '0.2.0',
          metric_id: 'm1',
        },
        created_at: '2026-07-01T10:00:00Z',
      },
    ]);

    mockGet.mockResolvedValue({
      candidate_id: 'cand_test_001',
      candidate_kind: 'EXPERIENCE_CANDIDATE',
      candidate_type: 'SAFETY_LESSON',
      task_type: '',
      review_status: 'REVIEW_REQUIRED',
      risk_level: 'HIGH',
      sanitization_status: 'SANITIZED',
      title: 'Safety lesson',
      summary: 'sanitized summary',
      label: 'label-a',
      tags: ['high_risk'],
      source_ref: {
        source_type: 'EVALUATION',
        evaluation_run_id: 'run_1',
        case_id: 'case_1',
        asset_package_id: 'pkg',
        asset_package_version: '0.2.0',
        metric_id: 'm1',
      },
      created_at: '2026-07-01T10:00:00Z',
      input: 'raw training input must not show',
      policy_metadata: { input: { text: 'hidden' }, mode: 'sanitized' },
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/candidates']}>
        <Routes>
          <Route path="/candidates" element={<CandidatePage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('cand_test_001')).toBeInTheDocument();
    await user.click(screen.getByText('cand_test_001'));

    await waitFor(() => expect(mockGet).toHaveBeenCalledWith('cand_test_001'));

    const heading = await screen.findByRole('heading', { name: /Candidate cand_test_001/ });
    const panel = heading.closest('section');
    expect(panel).not.toBeNull();
    expect(within(panel!).getByText('sanitized summary')).toBeInTheDocument();
    expect(within(panel!).queryByText(/raw training input/)).not.toBeInTheDocument();
    expect(within(panel!).queryByText(/"input"/)).not.toBeInTheDocument();
  });
});
