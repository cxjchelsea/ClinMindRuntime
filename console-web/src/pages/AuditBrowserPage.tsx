import { useMemo, useState } from 'react';
import type { AuditBrowserItem } from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { useDebugContext } from '../auth/DebugContextProvider';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';

export function AuditBrowserPage() {
  const { client, context } = useDebugContext();
  const [actorId, setActorId] = useState('');
  const [actionType, setActionType] = useState('');
  const [resourceType, setResourceType] = useState('');
  const [status, setStatus] = useState('');
  const [limit, setLimit] = useState('50');
  const [selected, setSelected] = useState<AuditBrowserItem | null>(null);
  const query = useAsyncQuery(
    () =>
      client.listAuditBrowser({
        actor_id: actorId || undefined,
        action_type: actionType || undefined,
        resource_type: resourceType || undefined,
        status: status || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, actorId, actionType, resourceType, status, limit],
  );
  const columns = useMemo<DataTableColumn<AuditBrowserItem>[]>(
    () => [
      { key: 'audit_id', header: 'Audit ID', render: (row) => row.audit_id },
      { key: 'actor', header: 'Actor', render: (row) => row.actor },
      { key: 'action_type', header: 'Action', render: (row) => row.action_type },
      { key: 'result_status', header: 'Result', render: (row) => <StatusBadge value={row.result_status} /> },
      { key: 'created_at', header: 'Created', render: (row) => formatInstant(row.created_at) },
    ],
    [],
  );

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Audit Browser</h1>
        <p>Sanitized audit event stream.</p>
      </header>
      <form className="console-page__filters" onSubmit={(event) => event.preventDefault()}>
        <label>
          Actor
          <input value={actorId} onChange={(event) => setActorId(event.target.value)} />
        </label>
        <label>
          Action
          <input value={actionType} onChange={(event) => setActionType(event.target.value)} />
        </label>
        <label>
          Resource
          <input value={resourceType} onChange={(event) => setResourceType(event.target.value)} />
        </label>
        <label>
          Status
          <input value={status} onChange={(event) => setStatus(event.target.value)} />
        </label>
        <label>
          Limit
          <input type="number" min={1} value={limit} onChange={(event) => setLimit(event.target.value)} />
        </label>
      </form>
      {query.error && <PageError error={query.error} roles={context.roles} />}
      <div className="console-page__layout">
        <section>
          <h2 className="console-page__section-title">Audits</h2>
          {query.loading ? (
            <LoadingState />
          ) : (
            <DataTable
              columns={columns}
              rows={query.data ?? []}
              rowKey={(row) => row.audit_id}
              selectedKey={selected?.audit_id}
              onSelect={setSelected}
            />
          )}
        </section>
        <section>
          <h2 className="console-page__section-title">Detail</h2>
          <DetailPanel title="Audit" data={selected as unknown as Record<string, unknown> | null} />
        </section>
      </div>
    </div>
  );
}
