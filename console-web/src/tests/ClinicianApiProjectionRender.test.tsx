import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { CaseWorkspacePage } from '../portals/clinician/pages/CaseWorkspacePage';

vi.mock('../auth/DebugContextProvider', () => ({
  useDebugContext: () => ({
    context: {
      apiBaseUrl: '',
      debugToken: 'test-secret',
      actor: 'clinician-a',
      roles: ['CLINICIAN'],
    },
  }),
}));

describe('ClinicianApiProjectionRender', () => {
  it('renders ClinicianCaseView returned by API first', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({
        success: true,
        data: {
          case_id: 'runtime-demo-001',
          runtime_id: 'runtime-demo-001',
          status: 'api_complete',
          patient_summary: {
            age_band: 'adult',
            sex: 'not specified',
            chief_complaint_summary: 'API projected chief concern.',
            context_notes: [],
          },
          case_frame: {
            current_problem: 'API projected case frame.',
            known_context: [],
            missing_information: [],
          },
          inquiry_timeline: [],
          ddx_board: [],
          evidence_panel: [],
          risk_panel: [],
          ai_suggestions: [],
          report_draft: {
            impression: 'API projected read-only draft.',
            suggested_questions: [],
            clinician_note: 'submit disabled',
            editable: true,
            submit_enabled: false,
            projection_status: 'COMPLETE',
          },
          projection_status: 'COMPLETE',
          missing_sections: [],
        },
      }),
    }));

    render(
      <MemoryRouter initialEntries={['/clinician/cases/runtime-demo-001']}>
        <Routes>
          <Route path="/clinician/cases/:caseId" element={<CaseWorkspacePage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('API projected chief concern.')).toBeInTheDocument();
    expect(screen.queryByText(/Demo fallback/)).not.toBeInTheDocument();

    vi.unstubAllGlobals();
  });
});
