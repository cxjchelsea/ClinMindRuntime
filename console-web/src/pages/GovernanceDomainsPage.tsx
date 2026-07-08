import { useMemo } from 'react';
import type { GovernanceDomainCard } from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { useDebugContext } from '../auth/DebugContextProvider';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';

export function GovernanceDomainsPage() {
  const { client, context } = useDebugContext();
  const query = useAsyncQuery(() => client.listGovernanceDomains(), [client]);
  const columns = useMemo<DataTableColumn<GovernanceDomainCard>[]>(
    () => [
      { key: 'domain_id', header: 'ID', render: (row) => row.domain_id },
      { key: 'name', header: 'Name', render: (row) => row.name },
      { key: 'status', header: 'Status', render: (row) => <StatusBadge value={row.status} /> },
      { key: 'record_count', header: 'Records', render: (row) => row.record_count },
      { key: 'alert_count', header: 'Alerts', render: (row) => row.alert_count },
      { key: 'latest_event_at', header: 'Latest', render: (row) => formatInstant(row.latest_event_at ?? '') },
    ],
    [],
  );

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Governance Domains</h1>
        <p>Domain health cards for Phase10-P0.</p>
      </header>
      {query.error && <PageError error={query.error} roles={context.roles} />}
      {query.loading ? <LoadingState /> : <DataTable columns={columns} rows={query.data ?? []} rowKey={(row) => row.domain_id} />}
    </div>
  );
}
