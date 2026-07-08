import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RuntimeTimelinePage } from '../pages/RuntimeTimelinePage';

const getRuntimeTimeline = vi.fn();

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'timeline-test-token',
      actor: 'timeline-test-user',
      roles: ['SYSTEM_ADMIN'],
    },
    client: {
      listConsoleRuntimes: vi.fn().mockResolvedValue([
        {
          runtime_id: 'runtime-demo-001',
          runtime_status: 'completed',
          trace_count: 3,
          safety_gate_present: true,
          updated_at: '2026-07-08T10:20:00Z',
        },
      ]),
      getRuntimeTimeline,
    },
  }),
}));

describe('RuntimeTimelinePage route params', () => {
  beforeEach(() => {
    getRuntimeTimeline.mockReset();
    getRuntimeTimeline.mockResolvedValue({
      runtime_id: 'runtime-demo-001',
      nodes: [
        {
          node_id: 'n1',
          label: 'Runtime accepted',
          type: 'runtime',
          status: 'completed',
          created_at: '2026-07-08T10:20:00Z',
        },
      ],
    });
  });

  it('loads the timeline for runtimeId from /governance/runtimes/:runtimeId', async () => {
    render(
      <MemoryRouter initialEntries={['/governance/runtimes/runtime-demo-001']}>
        <Routes>
          <Route path="/governance/runtimes/:runtimeId" element={<RuntimeTimelinePage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('Runtime accepted')).toBeInTheDocument();
    expect(getRuntimeTimeline).toHaveBeenCalledWith('runtime-demo-001');
  });
});
