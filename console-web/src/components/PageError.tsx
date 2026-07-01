import { formatConsoleError, type ConsoleError } from '../api/errors';
import './consoleComponents.css';

export function PageError({
  error,
  roles,
}: {
  error: ConsoleError;
  roles?: string[];
}) {
  return (
    <div className="page-error" role="alert">
      {formatConsoleError(error, roles)}
    </div>
  );
}
