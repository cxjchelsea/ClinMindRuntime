import { useMemo, useState } from 'react';
import type { RuntimeConsoleDetail, RuntimeConsoleSummary } from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { EmptyState } from '../components/EmptyState';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';
import { useDebugContext } from '../auth/DebugContextProvider';

export function RuntimePage() {
  const { client, context } = useDebugContext();
  const [status, setStatus] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [limit, setLimit] = useState('20');
  const [selectedId, setSelectedId] = useState<string | undefined>();

  const listQuery = useAsyncQuery(
    () =>
      client.listRuntimeSessions({
        status: status || undefined,
        session_id: sessionId || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, status, sessionId, limit],
  );

  const detailQuery = useAsyncQuery(
    () => client.getRuntimeSession(selectedId!),
    [client, selectedId],
    { enabled: Boolean(selectedId) },
  );

  const columns = useMemo<DataTableColumn<RuntimeConsoleSummary>[]>(
    () => [
      {
        key: 'runtime_id',
        header: 'Runtime ID',
        render: (row) => row.runtime_id,
      },
      {
        key: 'session_id',
        header: 'Session',
        render: (row) => row.session_id,
      },
      {
        key: 'runtime_status',
        header: 'Status',
        render: (row) => <StatusBadge value={row.runtime_status} />,
      },
      {
        key: 'mode',
        header: 'Mode',
        render: (row) => row.mode,
      },
      {
        key: 'asset_package_id',
        header: 'Asset',
        render: (row) => `${row.asset_package_id}@${row.asset_package_version}`,
      },
      {
        key: 'trace_count',
        header: 'Traces',
        render: (row) => row.trace_count,
      },
      {
        key: 'updated_at',
        header: 'Updated',
        render: (row) => formatInstant(row.updated_at),
      },
    ],
    [],
  );

  const detailRecord = detailQuery.data
    ? (detailQuery.data as unknown as Record<string, unknown>)
    : null;

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Runtime Sessions</h1>
        <p>Safe DTO 摘要与详情，不含患者原文。</p>
      </header>

      <form
        className="console-page__filters"
        onSubmit={(event) => {
          event.preventDefault();
          listQuery.reload();
        }}
      >
        <label>
          Status
          <input value={status} onChange={(e) => setStatus(e.target.value)} placeholder="可选" />
        </label>
        <label>
          Session ID
          <input
            value={sessionId}
            onChange={(e) => setSessionId(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Limit
          <input value={limit} onChange={(e) => setLimit(e.target.value)} type="number" min={1} />
        </label>
        <button type="submit">查询</button>
      </form>

      {listQuery.error && <PageError error={listQuery.error} roles={context.roles} />}

      <div className="console-page__layout">
        <section>
          <h2 className="console-page__section-title">列表</h2>
          {listQuery.loading ? (
            <LoadingState />
          ) : listQuery.data?.length ? (
            <DataTable
              columns={columns}
              rows={listQuery.data}
              rowKey={(row) => row.runtime_id}
              selectedKey={selectedId}
              onSelect={(row) => setSelectedId(row.runtime_id)}
            />
          ) : (
            <EmptyState message="暂无 Runtime 会话。可先启动 Runtime 或调整过滤条件。" />
          )}
        </section>

        <section>
          <h2 className="console-page__section-title">详情</h2>
          {!selectedId ? (
            <DetailPanel title="Runtime Detail" data={null} />
          ) : detailQuery.loading ? (
            <LoadingState message="加载详情…" />
          ) : detailQuery.error ? (
            <PageError error={detailQuery.error} roles={context.roles} />
          ) : (
            <DetailPanel
              title={`Runtime ${selectedId}`}
              data={detailRecord}
            />
          )}
        </section>
      </div>
    </div>
  );
}

export type { RuntimeConsoleDetail };
