import './consoleComponents.css';

export function StatusBadge({ value }: { value: string }) {
  const normalized = value?.toLowerCase() ?? 'unknown';
  return <span className={`status-badge status-badge--${normalized}`}>{value}</span>;
}
