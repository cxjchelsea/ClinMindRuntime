import { useMemo, useState } from 'react';
import type { AuditConsoleSummary } from '../api/types';
import { toSafeDisplayRecord } from '../components/SensitiveFieldRenderGuard';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { EmptyState } from '../components/EmptyState';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';
import { useDebugContext } from '../auth/DebugContextProvider';
import { canViewAuditCenter } from '../auth/roleHelpers';

function CountGrid({ title, counts }: { title: string; counts: Record<string, number> }) {
  const entries = Object.entries(counts);
  if (!entries.length) {
    return null;
  }

  return (
    <section className="audit-summary__group">
      <h3 className="audit-summary__group-title">{title}</h3>
      <dl className="audit-summary__counts">
        {entries.map(([key, value]) => (
          <div key={key} className="audit-summary__count">
            <dt>{key}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

function CompactAuditTable({
  title,
  rows,
}: {
  title: string;
  rows: AuditConsoleSummary[];
}) {
  if (!rows.length) {
    return null;
  }

  return (
    <section className="audit-summary__recent">
      <h3 className="audit-summary__group-title">{title}</h3>
      <div className="data-table__wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Audit ID</th>
              <th>Action</th>
              <th>Resource</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.audit_id}>
                <td>{row.audit_id}</td>
                <td>{row.action_type}</td>
                <td>
                  {row.resource_type}/{row.resource_id}
                </td>
                <td>
                  <StatusBadge value={row.result_status} />
                </td>
                <td>{formatInstant(row.created_at)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export function AuditCenterPage() {
  const { client, context } = useDebugContext();
  const auditAccess = canViewAuditCenter(context.roles);

  const [actor, setActor] = useState('');
  const [actionType, setActionType] = useState('');
  const [resourceType, setResourceType] = useState('');
  const [resourceId, setResourceId] = useState('');
  const [resultStatus, setResultStatus] = useState('');
  const [limit, setLimit] = useState('20');
  const [selectedId, setSelectedId] = useState<string | undefined>();

  const summaryQuery = useAsyncQuery(() => client.getAuditSummary(), [client]);

  const listQuery = useAsyncQuery(
    () =>
      client.listAuditLogs({
        actor: actor || undefined,
        action_type: actionType || undefined,
        resource_type: resourceType || undefined,
        resource_id: resourceId || undefined,
        result_status: resultStatus || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, actor, actionType, resourceType, resourceId, resultStatus, limit],
  );

  const detailQuery = useAsyncQuery(
    () => client.getAuditLog(selectedId!),
    [client, selectedId],
    { enabled: Boolean(selectedId) },
  );

  const columns = useMemo<DataTableColumn<AuditConsoleSummary>[]>(
    () => [
      { key: 'audit_id', header: 'Audit ID', render: (row) => row.audit_id },
      { key: 'actor', header: 'Actor', render: (row) => row.actor },
      { key: 'action_type', header: 'Action', render: (row) => row.action_type },
      { key: 'resource_type', header: 'Resource Type', render: (row) => row.resource_type },
      { key: 'resource_id', header: 'Resource ID', render: (row) => row.resource_id },
      {
        key: 'result_status',
        header: 'Status',
        render: (row) => <StatusBadge value={row.result_status} />,
      },
      {
        key: 'created_at',
        header: 'Created',
        render: (row) => formatInstant(row.created_at),
      },
    ],
    [],
  );

  const detailRecord = detailQuery.data
    ? (toSafeDisplayRecord(detailQuery.data as unknown as Record<string, unknown>) as Record<
        string,
        unknown
      >)
    : null;

  const pageError = summaryQuery.error ?? listQuery.error;

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Audit Center</h1>
        <p>AuditLog 查询、过滤与治理复盘（Safe DTO，不含敏感 metadata）。</p>
      </header>

      {!auditAccess && (
        <p className="audit-access-hint" role="note">
          需要 AUDIT_REVIEWER 或 SYSTEM_ADMIN 角色才能查询 Audit Center。
        </p>
      )}

      {pageError && <PageError error={pageError} roles={context.roles} />}

      <section className="audit-summary">
        <h2 className="console-page__section-title">Summary</h2>
        {summaryQuery.loading ? (
          <LoadingState message="加载 summary…" />
        ) : summaryQuery.error ? null : summaryQuery.data ? (
          <>
            <p className="audit-summary__total">Total: {summaryQuery.data.total_count}</p>
            <div className="audit-summary__grid">
              <CountGrid title="By Action Type" counts={summaryQuery.data.count_by_action_type} />
              <CountGrid title="By Resource Type" counts={summaryQuery.data.count_by_resource_type} />
              <CountGrid title="By Result Status" counts={summaryQuery.data.count_by_result_status} />
            </div>
            <CompactAuditTable title="Recent Failures" rows={summaryQuery.data.recent_failures} />
            <CompactAuditTable
              title="Recent Review Actions"
              rows={summaryQuery.data.recent_review_actions}
            />
          </>
        ) : (
          <EmptyState message="暂无 Audit summary。" />
        )}
      </section>

      <form
        className="console-page__filters"
        onSubmit={(event) => {
          event.preventDefault();
          listQuery.reload();
        }}
      >
        <label>
          Actor
          <input value={actor} onChange={(e) => setActor(e.target.value)} placeholder="可选" />
        </label>
        <label>
          Action Type
          <input
            value={actionType}
            onChange={(e) => setActionType(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Resource Type
          <input
            value={resourceType}
            onChange={(e) => setResourceType(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Resource ID
          <input
            value={resourceId}
            onChange={(e) => setResourceId(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Result Status
          <input
            value={resultStatus}
            onChange={(e) => setResultStatus(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Limit
          <input value={limit} onChange={(e) => setLimit(e.target.value)} type="number" min={1} />
        </label>
        <button type="submit">查询</button>
      </form>

      <div className="console-page__layout">
        <section>
          <h2 className="console-page__section-title">Audit Logs</h2>
          {listQuery.loading ? (
            <LoadingState />
          ) : listQuery.error ? null : listQuery.data?.length ? (
            <DataTable
              columns={columns}
              rows={listQuery.data}
              rowKey={(row) => row.audit_id}
              selectedKey={selectedId}
              onSelect={(row) => setSelectedId(row.audit_id)}
            />
          ) : (
            <EmptyState message="暂无 Audit 记录。可调整过滤条件或确认后端已有 AuditLog。" />
          )}
        </section>

        <section>
          <h2 className="console-page__section-title">详情</h2>
          {!selectedId ? (
            <DetailPanel title="Audit Detail" data={null} />
          ) : detailQuery.loading ? (
            <LoadingState message="加载详情…" />
          ) : detailQuery.error ? (
            <PageError error={detailQuery.error} roles={context.roles} />
          ) : (
            <DetailPanel title={`Audit ${selectedId}`} data={detailRecord} />
          )}
        </section>
      </div>
    </div>
  );
}
