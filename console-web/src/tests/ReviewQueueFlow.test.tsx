import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { ReviewQueuePage } from '../pages/ReviewQueuePage';

const mockListQueue = vi.fn();
const mockGet = vi.fn();
const mockReviewExperience = vi.fn();
const mockReviewTraining = vi.fn();

function mockUseDebugContext(roles: string[]) {
  return {
    context: {
      apiBaseUrl: '',
      debugToken: 'test-token',
      actor: 'reviewer-a',
      roles,
    },
    client: {
      listReviewQueue: mockListQueue,
      getCandidate: mockGet,
      reviewExperienceCandidate: mockReviewExperience,
      reviewTrainingExampleCandidate: mockReviewTraining,
    } as unknown as ConsoleClient,
    globalError: null,
    connectionOk: null,
    setContext: vi.fn(),
    setGlobalError: vi.fn(),
    testConnection: vi.fn(),
    clearGlobalError: vi.fn(),
  };
}

let currentRoles = ['CANDIDATE_REVIEWER'];

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => mockUseDebugContext(currentRoles),
}));

describe('ReviewQueueFlow', () => {
  beforeEach(() => {
    currentRoles = ['CANDIDATE_REVIEWER'];
    mockListQueue.mockReset();
    mockGet.mockReset();
    mockReviewExperience.mockReset();
    mockReviewTraining.mockReset();
  });

  it('submits review and shows status without auto-activation wording', async () => {
    mockListQueue.mockResolvedValue([
      {
        candidate_id: 'cand_rq_001',
        candidate_kind: 'EXPERIENCE_CANDIDATE',
        candidate_type: 'SAFETY_LESSON',
        review_status: 'REVIEW_REQUIRED',
        risk_level: 'HIGH',
        sanitization_status: 'SANITIZED',
        title: 'Queue item',
        tags: [],
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
      candidate_id: 'cand_rq_001',
      candidate_kind: 'EXPERIENCE_CANDIDATE',
      candidate_type: 'SAFETY_LESSON',
      task_type: '',
      review_status: 'REVIEW_REQUIRED',
      risk_level: 'HIGH',
      sanitization_status: 'SANITIZED',
      title: 'Queue item',
      summary: 'summary',
      label: 'label',
      tags: [],
      source_ref: {
        source_type: 'EVALUATION',
        evaluation_run_id: 'run_1',
        case_id: 'case_1',
        asset_package_id: 'pkg',
        asset_package_version: '0.2.0',
        metric_id: 'm1',
      },
      created_at: '2026-07-01T10:00:00Z',
      policy_metadata: {},
    });

    mockReviewExperience.mockResolvedValue({
      review_id: 'rev_001',
      candidate_id: 'cand_rq_001',
      candidate_kind: 'EXPERIENCE_CANDIDATE',
      from_status: 'REVIEW_REQUIRED',
      to_status: 'APPROVED',
      decision: 'APPROVE',
      reason: 'Valid',
      reviewer: 'reviewer-a',
      reviewed_at: '2026-07-01T11:00:00Z',
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/review-queue']}>
        <Routes>
          <Route path="/review-queue" element={<ReviewQueuePage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('cand_rq_001')).toBeInTheDocument();
    await user.click(screen.getByText('cand_rq_001'));

    await user.type(screen.getByPlaceholderText('审核理由'), 'Valid synthetic lesson');
    await user.click(screen.getByRole('button', { name: '提交 Review' }));

    await waitFor(() => expect(mockReviewExperience).toHaveBeenCalled());
    expect(await screen.findByRole('status')).toHaveTextContent('APPROVED');
    expect(screen.getByRole('status')).toHaveTextContent('未自动上线或进入训练集');
    expect(screen.queryByText(/已上线/)).not.toBeInTheDocument();
  });

  it('hides review form for read-only observer', async () => {
    currentRoles = ['READ_ONLY_OBSERVER'];
    mockListQueue.mockResolvedValue([
      {
        candidate_id: 'cand_rq_002',
        candidate_kind: 'EXPERIENCE_CANDIDATE',
        candidate_type: 'SAFETY_LESSON',
        review_status: 'REVIEW_REQUIRED',
        risk_level: 'HIGH',
        sanitization_status: 'SANITIZED',
        title: 'Observer item',
        tags: [],
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
      candidate_id: 'cand_rq_002',
      candidate_kind: 'EXPERIENCE_CANDIDATE',
      candidate_type: 'SAFETY_LESSON',
      task_type: '',
      review_status: 'REVIEW_REQUIRED',
      risk_level: 'HIGH',
      sanitization_status: 'SANITIZED',
      title: 'Observer item',
      summary: 'summary',
      label: 'label',
      tags: [],
      source_ref: {
        source_type: 'EVALUATION',
        evaluation_run_id: 'run_1',
        case_id: 'case_1',
        asset_package_id: 'pkg',
        asset_package_version: '0.2.0',
        metric_id: 'm1',
      },
      created_at: '2026-07-01T10:00:00Z',
      policy_metadata: {},
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/review-queue']}>
        <Routes>
          <Route path="/review-queue" element={<ReviewQueuePage />} />
        </Routes>
      </MemoryRouter>,
    );

    await user.click(await screen.findByText('cand_rq_002'));
    expect(screen.getByText(/需要 CANDIDATE_REVIEWER/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '提交 Review' })).not.toBeInTheDocument();
  });
});
