import { useMemo, useState } from 'react';
import type { Phase10RuntimeListItem, RuntimeTimelineNode } from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { EmptyState } from '../components/EmptyState';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { useDebugContext } from '../auth/DebugContextProvider';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';

export function RuntimeTimelinePage() {
  const { client, context } = useDebugContext();
  const [status, setStatus] = useState('');
  const [limit, setLimit] = useState('20');
  const [selectedId, setSelectedId] = useState<string>();

  const runtimeQuery = useAsyncQuery(
    () => client.listConsoleRuntimes({ status: status || undefined, limit: limit ? Number(limit) : undefined }),
    [client, status, limit],
  );
  const timelineQuery = useAsyncQuery(
    () => client.getRuntimeTimeline(selectedId!),
    [client, selectedId],
    { enabled: Boolean(selectedId) },
  );

  const runtimeColumns = useMemo<DataTableColumn<Phase10RuntimeListItem>[]>(
    () => [
      { key: 'runtime_id', header: 'Runtime ID', render: (row) => row.runtime_id },
      { key: 'runtime_status', header: 'Status', render: (row) => <StatusBadge value={row.runtime_status} /> },
      { key: 'trace_count', header: 'Traces', render: (row) => row.trace_count },
      { key: 'safety_gate_present', header: 'Safety', render: (row) => (row.safety_gate_present ? 'present' : 'missing') },
      { key: 'updated_at', header: 'Updated', render: (row) => formatInstant(row.updated_at) },
    ],
    [],
  );

  const nodeColumns = useMemo<DataTableColumn<RuntimeTimelineNode>[]>(
    () => [
      { key: 'label', header: 'Node', render: (row) => row.label },
      { key: 'type', header: 'Type', render: (row) => row.type },
      { key: 'status', header: 'Status', render: (row) => <StatusBadge value={row.status} /> },
      { key: 'created_at', header: 'Time', render: (row) => formatInstant(row.created_at ?? '') },
    ],
    [],
  );

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Runtime Timeline</h1>
        <p>Read-only timeline with governance checkpoints.</p>
      </header>

      <form className="console-page__filters" onSubmit={(event) => event.preventDefault()}>
        <label>
          Status
          <input value={status} onChange={(event) => setStatus(event.target.value)} />
        </label>
        <label>
          Limit
          <input type="number" min={1} value={limit} onChange={(event) => setLimit(event.target.value)} />
        </label>
      </form>

      {runtimeQuery.error && <PageError error={runtimeQuery.error} roles={context.roles} />}

      <div className="console-page__layout">
        <section>
          <h2 className="console-page__section-title">Runtimes</h2>
          {runtimeQuery.loading ? (
            <LoadingState />
          ) : runtimeQuery.data?.length ? (
            <DataTable
              columns={runtimeColumns}
              rows={runtimeQuery.data}
              rowKey={(row) => row.runtime_id}
              selectedKey={selectedId}
              onSelect={(row) => setSelectedId(row.runtime_id)}
            />
          ) : (
            <EmptyState message="No runtime sessions." />
          )}
        </section>

        <section>
          <h2 className="console-page__section-title">Timeline</h2>
          {timelineQuery.loading ? (
            <LoadingState />
          ) : timelineQuery.error ? (
            <PageError error={timelineQuery.error} roles={context.roles} />
          ) : timelineQuery.data ? (
            <>
              <DataTable columns={nodeColumns} rows={timelineQuery.data.nodes} rowKey={(row) => row.node_id} />
              <DetailPanel title="Timeline Summary" data={timelineQuery.data as unknown as Record<string, unknown>} />
            </>
          ) : (
            <DetailPanel title="Timeline" data={null} />
          )}
        </section>
      </div>
    </div>
  );
}
