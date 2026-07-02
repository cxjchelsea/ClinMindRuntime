import { useMemo, useState } from 'react';
import { submitCandidateReview } from '../api/reviewActions';
import type { CandidateConsoleSummary } from '../api/types';
import { toSafeDisplayRecord } from '../components/SensitiveFieldRenderGuard';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { EmptyState } from '../components/EmptyState';
import { GovernanceNotice } from '../components/GovernanceNotice';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { ReviewForm } from '../components/ReviewForm';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';
import { useDebugContext } from '../auth/DebugContextProvider';
import { canReviewCandidate } from '../auth/roleHelpers';

export function ReviewQueuePage() {
  const { client, context } = useDebugContext();
  const [kind, setKind] = useState('');
  const [riskLevel, setRiskLevel] = useState('');
  const [taskType, setTaskType] = useState('');
  const [limit, setLimit] = useState('20');
  const [selectedId, setSelectedId] = useState<string | undefined>();
  const [selectedKind, setSelectedKind] = useState<string | undefined>();

  const reviewerEnabled = canReviewCandidate(context.roles);

  const listQuery = useAsyncQuery(
    () =>
      client.listReviewQueue({
        kind: kind || undefined,
        risk_level: riskLevel || undefined,
        task_type: taskType || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, kind, riskLevel, taskType, limit],
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
        key: 'review_status',
        header: 'Status',
        render: (row) => <StatusBadge value={row.review_status} />,
      },
      { key: 'risk_level', header: 'Risk', render: (row) => row.risk_level },
      {
        key: 'title',
        header: 'Title',
        render: (row) => row.title || '—',
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

  const handleSelect = (row: CandidateConsoleSummary) => {
    setSelectedId(row.candidate_id);
    setSelectedKind(row.candidate_kind);
  };

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Review Queue</h1>
        <p>待审核候选（默认 review_status = REVIEW_REQUIRED）。</p>
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
          Risk Level
          <input
            value={riskLevel}
            onChange={(e) => setRiskLevel(e.target.value)}
            placeholder="可选"
          />
        </label>
        <label>
          Task Type
          <input value={taskType} onChange={(e) => setTaskType(e.target.value)} placeholder="可选" />
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
          <h2 className="console-page__section-title">队列</h2>
          {listQuery.loading ? (
            <LoadingState />
          ) : listQuery.data?.length ? (
            <DataTable
              columns={columns}
              rows={listQuery.data}
              rowKey={(row) => row.candidate_id}
              selectedKey={selectedId}
              onSelect={handleSelect}
            />
          ) : (
            <EmptyState message="Review Queue 为空。" />
          )}
        </section>

        <section>
          <h2 className="console-page__section-title">详情与 Review</h2>
          {!selectedId ? (
            <DetailPanel title="Candidate Detail" data={null} />
          ) : detailQuery.loading ? (
            <LoadingState message="加载详情…" />
          ) : detailQuery.error ? (
            <PageError error={detailQuery.error} roles={context.roles} />
          ) : (
            <>
              <DetailPanel title={`Candidate ${selectedId}`} data={detailRecord} />
              <ReviewForm
                candidateId={selectedId}
                defaultReviewer={context.actor}
                disabled={!reviewerEnabled}
                disabledReason="需要 CANDIDATE_REVIEWER 或 SYSTEM_ADMIN 角色才能提交 review。"
                onSubmit={async (values) => {
                  const record = await submitCandidateReview(
                    client,
                    selectedKind ?? detailQuery.data!.candidate_kind,
                    selectedId,
                    values,
                  );
                  listQuery.reload();
                  detailQuery.reload();
                  return record;
                }}
              />
            </>
          )}
        </section>
      </div>
    </div>
  );
}
