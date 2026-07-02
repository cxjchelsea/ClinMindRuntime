import { useState, type FormEvent } from 'react';
import type { ConsoleError } from '../api/errors';
import type { CandidateReviewRecord, ReviewDecision } from '../api/types';
import { PageError } from './PageError';
import './consoleComponents.css';

export interface ReviewFormValues {
  decision: ReviewDecision;
  reason: string;
  reviewer: string;
}

interface ReviewFormProps {
  candidateId: string;
  defaultReviewer: string;
  disabled?: boolean;
  disabledReason?: string;
  onSubmit: (values: ReviewFormValues) => Promise<CandidateReviewRecord>;
}

export function ReviewForm({
  candidateId,
  defaultReviewer,
  disabled = false,
  disabledReason,
  onSubmit,
}: ReviewFormProps) {
  const [decision, setDecision] = useState<ReviewDecision>('APPROVE');
  const [reason, setReason] = useState('');
  const [reviewer, setReviewer] = useState(defaultReviewer);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ConsoleError | null>(null);
  const [successStatus, setSuccessStatus] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (disabled) {
      return;
    }
    setSubmitting(true);
    setError(null);
    setSuccessStatus(null);
    try {
      const record = await onSubmit({ decision, reason, reviewer });
      setSuccessStatus(record.to_status);
      setReason('');
    } catch (err) {
      setError(err as ConsoleError);
    } finally {
      setSubmitting(false);
    }
  };

  if (disabled) {
    return (
      <div className="review-form review-form--disabled">
        <p>{disabledReason ?? '当前角色不可执行 review 操作。'}</p>
      </div>
    );
  }

  return (
    <form className="review-form" onSubmit={handleSubmit} aria-label={`Review ${candidateId}`}>
      <h3 className="review-form__title">Review Candidate</h3>

      <label className="review-form__field">
        Decision
        <select value={decision} onChange={(e) => setDecision(e.target.value as ReviewDecision)}>
          <option value="APPROVE">Approve</option>
          <option value="REJECT">Reject</option>
          <option value="DEPRECATE">Deprecate</option>
        </select>
      </label>

      <label className="review-form__field">
        Reason
        <textarea
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          required
          rows={3}
          placeholder="审核理由"
        />
      </label>

      <label className="review-form__field">
        Reviewer
        <input value={reviewer} onChange={(e) => setReviewer(e.target.value)} required />
      </label>

      {error && <PageError error={error} />}
      {successStatus && (
        <p className="review-form__success" role="status">
          Review 已提交，当前 review_status：<strong>{successStatus}</strong>（未自动上线或进入训练集）
        </p>
      )}

      <button type="submit" disabled={submitting || !reason.trim() || !reviewer.trim()}>
        {submitting ? '提交中…' : '提交 Review'}
      </button>
    </form>
  );
}
