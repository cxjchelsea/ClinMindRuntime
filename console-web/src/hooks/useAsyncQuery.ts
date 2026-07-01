import { useCallback, useEffect, useState, type DependencyList } from 'react';
import type { ConsoleError } from '../api/errors';

export interface AsyncQueryState<T> {
  loading: boolean;
  data: T | null;
  error: ConsoleError | null;
}

export function useAsyncQuery<T>(
  queryFn: () => Promise<T>,
  deps: DependencyList,
  options?: { enabled?: boolean },
): AsyncQueryState<T> & { reload: () => void } {
  const enabled = options?.enabled ?? true;
  const [loading, setLoading] = useState(enabled);
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ConsoleError | null>(null);
  const [tick, setTick] = useState(0);

  const reload = useCallback(() => setTick((value) => value + 1), []);

  useEffect(() => {
    if (!enabled) {
      setLoading(false);
      setData(null);
      setError(null);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    queryFn()
      .then((result) => {
        if (!cancelled) {
          setData(result);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setData(null);
          setError(err as ConsoleError);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick, enabled]);

  return { loading, data, error, reload };
}

function formatInstant(value: string): string {
  if (!value) {
    return '—';
  }
  try {
    return new Date(value).toLocaleString();
  } catch {
    return value;
  }
}

export { formatInstant };
