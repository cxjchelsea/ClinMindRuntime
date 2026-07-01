import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { createConsoleClient, type ConsoleClient } from '../api/consoleClient';
import type { ConsoleError } from '../api/errors';
import {
  DEFAULT_DEBUG_CONTEXT,
  loadPersistedDebugContext,
  persistDebugContext,
  type DebugContext,
} from './debugContextTypes';

interface DebugContextValue {
  context: DebugContext;
  client: ConsoleClient;
  globalError: ConsoleError | null;
  connectionOk: boolean | null;
  setContext: (patch: Partial<DebugContext>) => void;
  setGlobalError: (error: ConsoleError | null) => void;
  testConnection: () => Promise<void>;
  clearGlobalError: () => void;
}

const DebugContextReact = createContext<DebugContextValue | null>(null);

function createInitialContext(): DebugContext {
  return { ...DEFAULT_DEBUG_CONTEXT, ...loadPersistedDebugContext() };
}

export function DebugContextProvider({ children }: { children: ReactNode }) {
  const [context, setContextState] = useState<DebugContext>(createInitialContext);
  const [globalError, setGlobalError] = useState<ConsoleError | null>(null);
  const [connectionOk, setConnectionOk] = useState<boolean | null>(null);

  const setContext = useCallback((patch: Partial<DebugContext>) => {
    setContextState((prev) => {
      const next = { ...prev, ...patch };
      persistDebugContext(next);
      return next;
    });
  }, []);

  const client = useMemo(() => createConsoleClient(() => context), [context]);

  const testConnection = useCallback(async () => {
    setGlobalError(null);
    try {
      await client.testConnection();
      setConnectionOk(true);
    } catch (error) {
      setConnectionOk(false);
      setGlobalError(error as ConsoleError);
    }
  }, [client]);

  const value = useMemo(
    () => ({
      context,
      client,
      globalError,
      connectionOk,
      setContext,
      setGlobalError,
      testConnection,
      clearGlobalError: () => setGlobalError(null),
    }),
    [context, client, globalError, connectionOk, setContext, testConnection],
  );

  return (
    <DebugContextReact.Provider value={value}>{children}</DebugContextReact.Provider>
  );
}

export function useDebugContext(): DebugContextValue {
  const value = useContext(DebugContextReact);
  if (!value) {
    throw new Error('useDebugContext must be used within DebugContextProvider');
  }
  return value;
}
