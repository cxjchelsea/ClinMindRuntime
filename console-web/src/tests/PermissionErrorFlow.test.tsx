import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { fromApiBody } from '../api/errors';
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
        actor: 'observer-a',
        roles: ['READ_ONLY_OBSERVER'],
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

describe('PermissionErrorFlow', () => {
  beforeEach(() => {
    mockSummary.mockReset();
    mockList.mockReset();
    mockGet.mockReset();

    const denied = Promise.reject(fromApiBody('ACCESS_DENIED', 'Access denied', 403));
    denied.catch(() => undefined);
    mockSummary.mockReturnValue(denied);
    mockList.mockReturnValue(denied);
  });

  it('shows permission error for audit center when API returns 403', async () => {
    render(
      <MemoryRouter initialEntries={['/audit-center']}>
        <Routes>
          <Route path="/audit-center" element={<AuditCenterPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByRole('alert')).toHaveTextContent('当前角色无权访问该资源');
    expect(screen.getByRole('alert')).toHaveTextContent('READ_ONLY_OBSERVER');
    expect(screen.getByText(/需要 AUDIT_REVIEWER/)).toBeInTheDocument();
  });
});
