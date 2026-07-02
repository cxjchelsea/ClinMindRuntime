import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { AuditCenterPage } from '../pages/AuditCenterPage';

const mockSummary = vi.fn();
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
        actor: 'audit-user',
        roles: ['AUDIT_REVIEWER'],
      },
      client: {
        getAuditSummary: mockSummary,
        listAuditLogs: mockList,
        getAuditLog: mockGet,
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

function renderAuditCenterPage() {
  return render(
    <MemoryRouter initialEntries={['/audit-center']}>
      <Routes>
        <Route path="/audit-center" element={<AuditCenterPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('AuditCenterPage', () => {
  beforeEach(() => {
    mockSummary.mockReset();
    mockList.mockReset();
    mockGet.mockReset();
  });

  it('renders audit summary and safe detail without sensitive metadata', async () => {
    mockSummary.mockResolvedValue({
      total_count: 2,
      count_by_action_type: { CREATE_EVALUATION_RUN: 1, REVIEW_CANDIDATE: 1 },
      count_by_resource_type: { EVALUATION_RUN: 1, CANDIDATE: 1 },
      count_by_result_status: { SUCCESS: 2 },
      recent_failures: [],
      recent_review_actions: [
        {
          audit_id: 'audit_recent_001',
          actor: 'reviewer-a',
          action_type: 'REVIEW_CANDIDATE',
          resource_type: 'CANDIDATE',
          resource_id: 'cand_001',
          result_status: 'SUCCESS',
          created_at: '2026-07-01T12:00:00Z',
          metadata_summary: { decision: 'APPROVE' },
        },
      ],
    });

    mockList.mockResolvedValue([
      {
        audit_id: 'audit_list_001',
        actor: 'system',
        action_type: 'CREATE_EVALUATION_RUN',
        resource_type: 'EVALUATION_RUN',
        resource_id: 'run_001',
        result_status: 'SUCCESS',
        created_at: '2026-07-01T11:00:00Z',
        metadata_summary: { mode: 'in-memory' },
      },
    ]);

    mockGet.mockResolvedValue({
      audit_id: 'audit_list_001',
      actor: 'system',
      action_type: 'CREATE_EVALUATION_RUN',
      resource_type: 'EVALUATION_RUN',
      resource_id: 'run_001',
      result_status: 'SUCCESS',
      created_at: '2026-07-01T11:00:00Z',
      metadata_summary: {
        mode: 'in-memory',
        patient_output: 'must-not-render',
        input: { text: 'hidden' },
      },
    });

    const user = userEvent.setup();
    renderAuditCenterPage();

    expect(await screen.findByText('Total: 2')).toBeInTheDocument();
    expect(screen.getByText('By Action Type')).toBeInTheDocument();
    expect(screen.getByText('Recent Review Actions')).toBeInTheDocument();
    expect(screen.getByText('audit_list_001')).toBeInTheDocument();

    await user.click(screen.getByText('audit_list_001'));

    await waitFor(() => expect(mockGet).toHaveBeenCalledWith('audit_list_001'));

    const detail = await screen.findByRole('heading', { name: /Audit audit_list_001/ });
    const panel = detail.closest('section');
    expect(panel).not.toBeNull();
    expect(within(panel!).getByText('run_001')).toBeInTheDocument();
    expect(within(panel!).queryByText(/patient_output/)).not.toBeInTheDocument();
    expect(within(panel!).queryByText(/must-not-render/)).not.toBeInTheDocument();
  });

  it('shows empty state when audit log list is empty', async () => {
    mockSummary.mockResolvedValue({
      total_count: 0,
      count_by_action_type: {},
      count_by_resource_type: {},
      count_by_result_status: {},
      recent_failures: [],
      recent_review_actions: [],
    });
    mockList.mockResolvedValue([]);

    renderAuditCenterPage();

    expect(await screen.findByText(/暂无 Audit 记录/)).toBeInTheDocument();
  });
});
