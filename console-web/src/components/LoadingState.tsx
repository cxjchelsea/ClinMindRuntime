import './consoleComponents.css';

export function LoadingState({ message = '加载中…' }: { message?: string }) {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      {message}
    </div>
  );
}
