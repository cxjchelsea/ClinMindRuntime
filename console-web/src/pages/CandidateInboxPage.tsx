import { useMemo, useState } from 'react';
import type { CandidateInboxItem } from '../api/types';
import { DataTable, type DataTableColumn } from '../components/DataTable';
import { DetailPanel } from '../components/DetailPanel';
import { LoadingState } from '../components/LoadingState';
import { PageError } from '../components/PageError';
import { StatusBadge } from '../components/StatusBadge';
import '../components/consoleComponents.css';
import { useDebugContext } from '../auth/DebugContextProvider';
import { formatInstant, useAsyncQuery } from '../hooks/useAsyncQuery';

export function CandidateInboxPage() {
  const { client, context } = useDebugContext();
  const [reviewStatus, setReviewStatus] = useState('');
  const [riskLevel, setRiskLevel] = useState('');
  const [candidateType, setCandidateType] = useState('');
  const [limit, setLimit] = useState('20');
  const [selected, setSelected] = useState<CandidateInboxItem | null>(null);
  const query = useAsyncQuery(
    () =>
      client.listCandidateInbox({
        review_status: reviewStatus || undefined,
        risk_level: riskLevel || undefined,
        candidate_type: candidateType || undefined,
        limit: limit ? Number(limit) : undefined,
      }),
    [client, reviewStatus, riskLevel, candidateType, limit],
  );
  const columns = useMemo<DataTableColumn<CandidateInboxItem>[]>(
    () => [
      { key: 'candidate_id', header: 'Candidate ID', render: (row) => row.candidate_id },
      { key: 'candidate_kind', header: 'Kind', render: (row) => row.candidate_kind },
      { key: 'risk_level', header: 'Risk', render: (row) => <StatusBadge value={row.risk_level} /> },
      { key: 'review_status', header: 'Review', render: (row) => <StatusBadge value={row.review_status} /> },
      { key: 'created_at', header: 'Created', render: (row) => formatInstant(row.created_at) },
    ],
    [],
  );

  return (
    <div className="console-page">
      <header className="console-page__header">
        <h1>Candidate Inbox</h1>
        <p>Read-only candidate queue.</p>
      </header>
      <form className="console-page__filters" onSubmit={(event) => event.preventDefault()}>
        <label>
          Review
          <input value={reviewStatus} onChange={(event) => setReviewStatus(event.target.value)} />
        </label>
        <label>
          Risk
          <input value={riskLevel} onChange={(event) => setRiskLevel(event.target.value)} />
        </label>
        <label>
          Type
          <input value={candidateType} onChange={(event) => setCandidateType(event.target.value)} />
        </label>
        <label>
          Limit
          <input type="number" min={1} value={limit} onChange={(event) => setLimit(event.target.value)} />
        </label>
      </form>
      {query.error && <PageError error={query.error} roles={context.roles} />}
      <div className="console-page__layout">
        <section>
          <h2 className="console-page__section-title">Inbox</h2>
          {query.loading ? (
            <LoadingState />
          ) : (
            <DataTable
              columns={columns}
              rows={query.data ?? []}
              rowKey={(row) => row.candidate_id}
              selectedKey={selected?.candidate_id}
              onSelect={setSelected}
            />
          )}
        </section>
        <section>
          <h2 className="console-page__section-title">Detail</h2>
          <DetailPanel title="Candidate" data={selected as unknown as Record<string, unknown> | null} />
        </section>
      </div>
    </div>
  );
}
