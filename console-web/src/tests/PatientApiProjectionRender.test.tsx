import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { PatientSafeSummaryPage } from '../portals/patient/pages/PatientSafeSummaryPage';

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'test-secret',
      actor: 'patient-a',
      roles: ['PATIENT'],
    },
  }),
}));

describe('PatientApiProjectionRender', () => {
  it('renders PatientRuntimeView returned by API first', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({
        success: true,
        data: {
          session_id: 'runtime-demo-001',
          runtime_id: 'runtime-demo-001',
          status: 'api_complete',
          safe_summary: 'API safe summary from backend projection.',
          collected_facts: [],
          next_questions: [],
          safety_notices: [],
          care_navigation: [],
          allowed_actions: ['view_safe_summary'],
          disclaimer: 'API disclaimer.',
          projection_status: 'COMPLETE',
          missing_sections: [],
        },
      }),
    }));

    render(
      <MemoryRouter initialEntries={['/patient/sessions/runtime-demo-001/summary']}>
        <Routes>
          <Route path="/patient/sessions/:sessionId/summary" element={<PatientSafeSummaryPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('API safe summary from backend projection.')).toBeInTheDocument();
    expect(screen.queryByText(/Demo fallback/)).not.toBeInTheDocument();

    vi.unstubAllGlobals();
  });
});
