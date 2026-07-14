import { useEffect, useMemo, useRef, useState } from 'react';
import { useDebugContext } from '../../../auth/DebugContextProvider';
import { clinicianCaseSummaries, clinicianCaseView } from '../../../demo/runtimeDemoData';
import type {
  ClinicianCaseSummary,
  ClinicianCaseView,
} from '../../../shared/types/clinicianViews';
import { createClinicianClient } from '../api/clinicianClient';

interface ClinicianViewState<T> {
  data: T;
  loading: boolean;
  source: 'api' | 'demo-fallback';
  error: string | null;
}

export function useClinicianCases(): ClinicianViewState<ClinicianCaseSummary[]> {
  const { context } = useDebugContext();
  const contextRef = useRef(context);
  contextRef.current = context;
  const requestContextKey = [
    context.apiBaseUrl,
    context.debugToken,
    context.actor,
    context.roles.join(','),
  ].join('\u0000');
  const client = useMemo(
    () => createClinicianClient(() => contextRef.current),
    [],
  );
  const [state, setState] = useState<ClinicianViewState<ClinicianCaseSummary[]>>({
    data: clinicianCaseSummaries,
    loading: true,
    source: 'demo-fallback',
    error: null,
  });

  useEffect(() => {
    let active = true;
    setState((prev) => ({ ...prev, loading: true }));
    client.listClinicianCases()
      .then((data) => {
        if (active) {
          setState({ data, loading: false, source: 'api', error: null });
        }
      })
      .catch((error: Error) => {
        if (active) {
          setState({
            data: clinicianCaseSummaries,
            loading: false,
            source: 'demo-fallback',
            error: error.message,
          });
        }
      });
    return () => {
      active = false;
    };
  }, [client, requestContextKey]);

  return state;
}

export function useClinicianCaseView(caseId: string): ClinicianViewState<ClinicianCaseView> {
  const { context } = useDebugContext();
  const contextRef = useRef(context);
  contextRef.current = context;
  const requestContextKey = [
    context.apiBaseUrl,
    context.debugToken,
    context.actor,
    context.roles.join(','),
  ].join('\u0000');
  const client = useMemo(
    () => createClinicianClient(() => contextRef.current),
    [],
  );
  const [state, setState] = useState<ClinicianViewState<ClinicianCaseView>>({
    data: clinicianCaseView,
    loading: true,
    source: 'demo-fallback',
    error: null,
  });

  useEffect(() => {
    let active = true;
    setState((prev) => ({ ...prev, loading: true }));
    client.getClinicianCase(caseId)
      .then((data) => {
        if (active) {
          setState({ data, loading: false, source: 'api', error: null });
        }
      })
      .catch((error: Error) => {
        if (active) {
          setState({
            data: clinicianCaseView,
            loading: false,
            source: 'demo-fallback',
            error: error.message,
          });
        }
      });
    return () => {
      active = false;
    };
  }, [caseId, client, requestContextKey]);

  return state;
}
