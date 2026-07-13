import type { DebugContext } from '../../../auth/debugContextTypes';
import { createRoleViewHttpClient } from '../../../shared/api/httpClient';
import type {
  ClinicianCaseSummary,
  ClinicianCaseView,
  ClinicianReportDraftView,
} from '../../../shared/types/clinicianViews';

export function createClinicianClient(getContext: () => DebugContext) {
  const http = createRoleViewHttpClient(getContext);

  return {
    listClinicianCases() {
      return http.request<ClinicianCaseSummary[]>('/api/v1/clinician/cases');
    },

    getClinicianCase(caseId: string) {
      return http.request<ClinicianCaseView>(
        `/api/v1/clinician/cases/${encodeURIComponent(caseId)}`,
      );
    },

    getClinicianReportDraft(caseId: string) {
      return http.request<ClinicianReportDraftView>(
        `/api/v1/clinician/cases/${encodeURIComponent(caseId)}/report-draft`,
      );
    },
  };
}

export type ClinicianClient = ReturnType<typeof createClinicianClient>;
