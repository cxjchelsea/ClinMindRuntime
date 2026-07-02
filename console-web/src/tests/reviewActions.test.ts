import { describe, expect, it, vi } from 'vitest';
import type { ConsoleClient } from '../api/consoleClient';
import { resolveReviewInvoker, submitCandidateReview } from '../api/reviewActions';

describe('review endpoint selection', () => {
  it('uses experience endpoint for experience candidates', async () => {
    const reviewExperienceCandidate = vi.fn().mockResolvedValue({ to_status: 'APPROVED' });
    const reviewTrainingExampleCandidate = vi.fn();
    const client = {
      reviewExperienceCandidate,
      reviewTrainingExampleCandidate,
    } as unknown as ConsoleClient;

    const invoke = resolveReviewInvoker(client, 'EXPERIENCE_CANDIDATE');
    await invoke('cand_001', {
      decision: 'APPROVE',
      reason: 'ok',
      reviewer: 'reviewer-a',
    });

    expect(reviewExperienceCandidate).toHaveBeenCalledWith('cand_001', {
      decision: 'APPROVE',
      reason: 'ok',
      reviewer: 'reviewer-a',
    });
    expect(reviewTrainingExampleCandidate).not.toHaveBeenCalled();
  });

  it('uses training endpoint for training example candidates', async () => {
    const reviewExperienceCandidate = vi.fn();
    const reviewTrainingExampleCandidate = vi.fn().mockResolvedValue({ to_status: 'APPROVED' });
    const client = {
      reviewExperienceCandidate,
      reviewTrainingExampleCandidate,
    } as unknown as ConsoleClient;

    await submitCandidateReview(client, 'TRAINING_EXAMPLE_CANDIDATE', 'cand_t001', {
      decision: 'REJECT',
      reason: 'no',
      reviewer: 'reviewer-b',
    });

    expect(reviewTrainingExampleCandidate).toHaveBeenCalled();
    expect(reviewExperienceCandidate).not.toHaveBeenCalled();
  });
});
