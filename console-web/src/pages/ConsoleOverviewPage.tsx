import { useMemo } from 'react';
import type { GovernanceDomainCard } from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { useDebugContext } from '../auth/DebugContextProvider';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';

export function ConsoleOverviewPage() {
  const { client, context } = useDebugContext();
  const overviewQuery = useAsyncQuery(() => client.getConsoleOverview(), [client]);

  const columns = useMemo<DataTableColumn<GovernanceDomainCard>[]>(
    () => [
      { key: 'domain_id', header: 'Domain', render: (row) => row.name },
      { key: 'status', header: 'Status', render: (row) => <StatusBadge value={row.status} /> },
      { key: 'record_count', header: 'Records', render: (row) => row.record_count },
      { key: 'alert_count', header: 'Alerts', render: (row) => row.alert_count },
      { key: 'latest_event_at', header: 'Latest', render: (row) => formatInstant(row.latest_event_at ?? '') },
    ],
    [],
  );

  const data = overviewQuery.data;

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Console Overview</h1>
        <p>Phase10-P0 read-only governance snapshot.</p>
      </header>

      {overviewQuery.error && <PageError error={overviewQuery.error} roles={context.roles} />}

      {overviewQuery.loading ? (
        <LoadingState />
      ) : data ? (
        <>
          <section className="console-metrics">
            <div className="console-metric">
              <span>Runtimes</span>
              <strong>{data.runtime_count}</strong>
            </div>
            <div className="console-metric">
              <span>Providers</span>
              <strong>{data.provider_call_count}</strong>
            </div>
            <div className="console-metric">
              <span>Tools</span>
              <strong>{data.tool_invocation_count}</strong>
            </div>
            <div className="console-metric">
              <span>Models</span>
              <strong>{data.model_governance_record_count}</strong>
            </div>
            <div className="console-metric">
              <span>Candidates</span>
              <strong>{data.candidate_count}</strong>
            </div>
            <div className="console-metric">
              <span>Audits</span>
              <strong>{data.audit_event_count}</strong>
            </div>
          </section>

          <section>
            <h2 className="console-page__section-title">Domains</h2>
            <DataTable
              columns={columns}
              rows={data.domain_cards}
              rowKey={(row) => row.domain_id}
            />
          </section>
        </>
      ) : null}
    </div>
  );
}
