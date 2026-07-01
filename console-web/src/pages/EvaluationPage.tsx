import { useMemo, useState } from 'react';
import type {
  EvaluationConsoleDetail,
  EvaluationConsoleSummary,
  EvaluationItemConsoleSummary,
} from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { EmptyState } from '../components/EmptyState';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';
import { useDebugContext } from '../auth/DebugContextProvider';

export function EvaluationPage() {
  const { client, context } = useDebugContext();
  const [status, setStatus] = useState('');
  const [caseSetId, setCaseSetId] = useState('');
  const [limit, setLimit] = useState('20');
  const [selectedId, setSelectedId] = useState<string | undefined>();

  const listQuery = useAsyncQuery(
    () =>
      client.listEvaluationRuns({
        status: status || undefined,
        case_set_id: caseSetId || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, status, caseSetId, limit],
  );

  const detailQuery = useAsyncQuery(
    () => client.getEvaluationRun(selectedId!),
    [client, selectedId],
    { enabled: Boolean(selectedId) },
  );

  const listColumns = useMemo<DataTableColumn<EvaluationConsoleSummary>[]>(
    () => [
      { key: 'run_id', header: 'Run ID', render: (row) => row.run_id },
      { key: 'case_set_id', header: 'Case Set', render: (row) => row.case_set_id },
      {
        key: 'case_set_version',
        header: 'Version',
        render: (row) => row.case_set_version,
      },
      {
        key: 'asset_package_id',
        header: 'Asset',
        render: (row) => `${row.asset_package_id}@${row.asset_package_version}`,
      },
      {
        key: 'status',
        header: 'Status',
        render: (row) => <StatusBadge value={row.status} />,
      },
      {
        key: 'started_at',
        header: 'Started',
        render: (row) => formatInstant(row.started_at),
      },
      {
        key: 'completed_at',
        header: 'Completed',
        render: (row) => formatInstant(row.completed_at),
      },
    ],
    [],
  );

  const itemColumns = useMemo<DataTableColumn<EvaluationItemConsoleSummary>[]>(
    () => [
      { key: 'case_id', header: 'Case ID', render: (row) => row.case_id },
      { key: 'runtime_id', header: 'Runtime ID', render: (row) => row.runtime_id },
      {
        key: 'passed',
        header: 'Passed',
        render: (row) => (row.passed ? '是' : '否'),
      },
      {
        key: 'score',
        header: 'Score',
        render: (row) => row.score.toFixed(2),
      },
    ],
    [],
  );

  const detail = detailQuery.data;
  const detailRecord = detail
    ? ({
        run_id: detail.run_id,
        case_set_id: detail.case_set_id,
        case_set_version: detail.case_set_version,
        asset_package_id: detail.asset_package_id,
        asset_package_version: detail.asset_package_version,
        status: detail.status,
        total_cases: detail.total_cases,
        passed_cases: detail.passed_cases,
        failed_cases: detail.failed_cases,
        pass_rate:
          detail.pass_rate != null ? `${(detail.pass_rate * 100).toFixed(1)}%` : null,
      } as Record<string, unknown>)
    : null;

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Evaluation Runs</h1>
        <p>Evaluation 运行列表与聚合结果摘要。</p>
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
          Case Set ID
          <input
            value={caseSetId}
            onChange={(e) => setCaseSetId(e.target.value)}
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
              columns={listColumns}
              rows={listQuery.data}
              rowKey={(row) => row.run_id}
              selectedKey={selectedId}
              onSelect={(row) => setSelectedId(row.run_id)}
            />
          ) : (
            <EmptyState message="暂无 Evaluation 运行记录。" />
          )}
        </section>

        <section>
          <h2 className="console-page__section-title">详情</h2>
          {!selectedId ? (
            <DetailPanel title="Evaluation Detail" data={null} />
          ) : detailQuery.loading ? (
            <LoadingState message="加载详情…" />
          ) : detailQuery.error ? (
            <PageError error={detailQuery.error} roles={context.roles} />
          ) : (
            <>
              <DetailPanel title={`Run ${selectedId}`} data={detailRecord} />
              {detail?.item_summaries?.length ? (
                <div style={{ marginTop: '1rem' }}>
                  <h3 className="console-page__section-title">Item Summary</h3>
                  <DataTable
                    columns={itemColumns}
                    rows={detail.item_summaries}
                    rowKey={(row) => `${row.case_id}-${row.runtime_id}`}
                  />
                </div>
              ) : null}
            </>
          )}
        </section>
      </div>
    </div>
  );
}

export type { EvaluationConsoleDetail };
