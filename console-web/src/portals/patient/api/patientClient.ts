import type { DebugContext } from '../../../auth/debugContextTypes';
import { createRoleViewHttpClient } from '../../../shared/api/httpClient';
import type {
  PatientRuntimeView,
  PatientSafeSummary,
  PatientSessionSummary,
} from '../../../shared/types/patientViews';

export function createPatientClient(getContext: () => DebugContext) {
  const http = createRoleViewHttpClient(getContext);

  return {
    listPatientSessions() {
      return http.request<PatientSessionSummary[]>('/api/v1/patient/sessions');
    },

    getPatientRuntimeView(sessionId: string) {
      return http.request<PatientRuntimeView>(
        `/api/v1/patient/sessions/${encodeURIComponent(sessionId)}`,
      );
    },

    getPatientSafeSummary(sessionId: string) {
      return http.request<PatientSafeSummary>(
        `/api/v1/patient/sessions/${encodeURIComponent(sessionId)}/summary`,
      );
    },
  };
}

export type PatientClient = ReturnType<typeof createPatientClient>;
