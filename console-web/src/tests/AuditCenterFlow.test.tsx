import { render, screen, waitFor } from '@testing-library/react';
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

describe('AuditCenterFlow', () => {
  beforeEach(() => {
    mockSummary.mockReset();
    mockList.mockReset();
    mockGet.mockReset();

    mockSummary.mockResolvedValue({
      total_count: 1,
      count_by_action_type: { GENERATE_CANDIDATES: 1 },
      count_by_resource_type: { CANDIDATE_GENERATION: 1 },
      count_by_result_status: { SUCCESS: 1 },
      recent_failures: [],
      recent_review_actions: [],
    });
  });

  it('applies filters and reloads audit log list', async () => {
    mockList.mockResolvedValue([
      {
        audit_id: 'audit_filtered_001',
        actor: 'gen-user',
        action_type: 'GENERATE_CANDIDATES',
        resource_type: 'CANDIDATE_GENERATION',
        resource_id: 'gen_001',
        result_status: 'SUCCESS',
        created_at: '2026-07-01T09:00:00Z',
        metadata_summary: {},
      },
    ]);

    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/audit-center']}>
        <Routes>
          <Route path="/audit-center" element={<AuditCenterPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await screen.findByText('Total: 1');

    const actorInput = screen.getAllByPlaceholderText('可选')[0];
    await user.type(actorInput, 'gen-user');
    await user.click(screen.getByRole('button', { name: '查询' }));

    await waitFor(() =>
      expect(mockList).toHaveBeenLastCalledWith(
        expect.objectContaining({
          actor: 'gen-user',
          limit: 20,
        }),
      ),
    );

    expect(await screen.findByText('audit_filtered_001')).toBeInTheDocument();
  });
});
