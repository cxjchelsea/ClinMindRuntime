import { useMemo, useState } from 'react';
import type { CandidateConsoleSummary } from '../api/types';
import { toSafeDisplayRecord } from '../components/SensitiveFieldRenderGuard';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { EmptyState } from '../components/EmptyState';
import { GovernanceNotice } from '../components/GovernanceNotice';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';
import { useDebugContext } from '../auth/DebugContextProvider';

export function CandidatePage() {
  const { client, context } = useDebugContext();
  const [kind, setKind] = useState('');
  const [reviewStatus, setReviewStatus] = useState('');
  const [riskLevel, setRiskLevel] = useState('');
  const [limit, setLimit] = useState('20');
  const [selectedId, setSelectedId] = useState<string | undefined>();

  const listQuery = useAsyncQuery(
    () =>
      client.listCandidates({
        kind: kind || undefined,
        review_status: reviewStatus || undefined,
        risk_level: riskLevel || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, kind, reviewStatus, riskLevel, limit],
  );

  const detailQuery = useAsyncQuery(
    () => client.getCandidate(selectedId!),
    [client, selectedId],
    { enabled: Boolean(selectedId) },
  );

  const columns = useMemo<DataTableColumn<CandidateConsoleSummary>[]>(
    () => [
      { key: 'candidate_id', header: 'Candidate ID', render: (row) => row.candidate_id },
      { key: 'candidate_kind', header: 'Kind', render: (row) => row.candidate_kind },
      {
        key: 'candidate_type',
        header: 'Type',
        render: (row) => row.candidate_type,
      },
      {
        key: 'review_status',
        header: 'Review',
        render: (row) => <StatusBadge value={row.review_status} />,
      },
      {
        key: 'risk_level',
        header: 'Risk',
        render: (row) => row.risk_level,
      },
      {
        key: 'source_ref',
        header: 'Case',
        render: (row) => row.source_ref?.case_id ?? '—',
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

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Candidates</h1>
        <p>Experience / Training 候选摘要与 Safe DTO 详情。</p>
      </header>

      <GovernanceNotice />

      <form
        className="console-page__filters"
        onSubmit={(event) => {
          event.preventDefault();
          listQuery.reload();
        }}
      >
        <label>
          Kind
          <input value={kind} onChange={(e) => setKind(e.target.value)} placeholder="可选" />
        </label>
        <label>
          Review Status
          <input
            value={reviewStatus}
            onChange={(e) => setReviewStatus(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Risk Level
          <input
            value={riskLevel}
            onChange={(e) => setRiskLevel(e.target.value)}
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
              rowKey={(row) => row.candidate_id}
              selectedKey={selectedId}
              onSelect={(row) => setSelectedId(row.candidate_id)}
            />
          ) : (
            <EmptyState message="暂无 Candidate 记录。" />
          )}
        </section>

        <section>
          <h2 className="console-page__section-title">详情</h2>
          {!selectedId ? (
            <DetailPanel title="Candidate Detail" data={null} />
          ) : detailQuery.loading ? (
            <LoadingState message="加载详情…" />
          ) : detailQuery.error ? (
            <PageError error={detailQuery.error} roles={context.roles} />
          ) : (
            <DetailPanel title={`Candidate ${selectedId}`} data={detailRecord} />
          )}
        </section>
      </div>
    </div>
  );
}
