import { useEffect, useMemo, useState } from 'react';
import { useDebugContext } from '../../../auth/DebugContextProvider';
import { patientRuntimeView, patientSessionSummaries } from '../../../demo/runtimeDemoData';
import type {
  PatientRuntimeView,
  PatientSessionSummary,
} from '../../../shared/types/patientViews';
import { createPatientClient } from '../api/patientClient';

interface PatientViewState<T> {
  data: T;
  loading: boolean;
  source: 'api' | 'demo-fallback';
  error: string | null;
}

export function usePatientSessions(): PatientViewState<PatientSessionSummary[]> {
  const { context } = useDebugContext();
  const client = useMemo(() => createPatientClient(() => context), [context]);
  const [state, setState] = useState<PatientViewState<PatientSessionSummary[]>>({
    data: patientSessionSummaries,
    loading: true,
    source: 'demo-fallback',
    error: null,
  });

  useEffect(() => {
    let active = true;
    setState((prev) => ({ ...prev, loading: true }));
    client.listPatientSessions()
      .then((data) => {
        if (active) {
          setState({ data, loading: false, source: 'api', error: null });
        }
      })
      .catch((error: Error) => {
        if (active) {
          setState({
            data: patientSessionSummaries,
            loading: false,
            source: 'demo-fallback',
            error: error.message,
          });
        }
      });
    return () => {
      active = false;
    };
  }, [client]);

  return state;
}

export function usePatientRuntimeView(sessionId: string): PatientViewState<PatientRuntimeView> {
  const { context } = useDebugContext();
  const client = useMemo(() => createPatientClient(() => context), [context]);
  const [state, setState] = useState<PatientViewState<PatientRuntimeView>>({
    data: patientRuntimeView,
    loading: true,
    source: 'demo-fallback',
    error: null,
  });

  useEffect(() => {
    let active = true;
    setState((prev) => ({ ...prev, loading: true }));
    client.getPatientRuntimeView(sessionId)
      .then((data) => {
        if (active) {
          setState({ data, loading: false, source: 'api', error: null });
        }
      })
      .catch((error: Error) => {
        if (active) {
          setState({
            data: patientRuntimeView,
            loading: false,
            source: 'demo-fallback',
            error: error.message,
          });
        }
      });
    return () => {
      active = false;
    };
  }, [client, sessionId]);

  return state;
}
