import { formatConsoleError } from '../api/errors';
import { useDebugContext } from '../auth/DebugContextProvider';
import './ErrorBanner.css';

export function ErrorBanner() {
  const { globalError, clearGlobalError, context } = useDebugContext();

  if (!globalError) {
    return null;
  }

  return (
    <div className="error-banner" role="alert">
      <div className="error-banner__content">
        <strong>请求失败</strong>
        <span>{formatConsoleError(globalError, context.roles)}</span>
      </div>
      <button type="button" className="error-banner__dismiss" onClick={clearGlobalError}>
        关闭
      </button>
    </div>
  );
}
