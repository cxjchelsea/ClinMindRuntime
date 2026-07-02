import type { ConsoleClient } from './consoleClient';
import type { CandidateReviewRecord, CandidateReviewRequestBody } from './types';

export function resolveReviewInvoker(
  client: ConsoleClient,
  candidateKind: string,
): (candidateId: string, body: CandidateReviewRequestBody) => Promise<CandidateReviewRecord> {
  if (candidateKind === 'TRAINING_EXAMPLE_CANDIDATE') {
    return client.reviewTrainingExampleCandidate.bind(client);
  }
  return client.reviewExperienceCandidate.bind(client);
}

export async function submitCandidateReview(
  client: ConsoleClient,
  candidateKind: string,
  candidateId: string,
  body: CandidateReviewRequestBody,
): Promise<CandidateReviewRecord> {
  const invoke = resolveReviewInvoker(client, candidateKind);
  return invoke(candidateId, body);
}
