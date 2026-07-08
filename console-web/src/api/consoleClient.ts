import {
  fromApiBody,
  networkError,
  toConsoleError,
  type ConsoleError,
} from './errors';
import type {
  ApiResponse,
  AuditCenterSummary,
  AuditBrowserItem,
  AuditBrowserParams,
  AuditConsoleSummary,
  AuditLogListParams,
  CandidateConsoleDetail,
  CandidateConsoleSummary,
  CandidateInboxItem,
  CandidateInboxParams,
  ConsoleOverview,
  CandidateGenerationConsoleSummary,
  CandidateGenerationListParams,
  CandidateListParams,
  CandidateReviewRecord,
  CandidateReviewRequestBody,
  EvaluationConsoleDetail,
  EvaluationConsoleSummary,
  EvaluationListParams,
  GovernanceDomainCard,
  Phase10RuntimeListItem,
  ReviewQueueListParams,
  RuntimeTimeline,
  RuntimeConsoleDetail,
  RuntimeConsoleSummary,
  RuntimeListParams,
} from './types';
import type { DebugContext } from '../auth/debugContextTypes';
import { DEBUG_HEADERS } from '../auth/debugContextTypes';

export type ContextGetter = () => DebugContext;

function joinUrl(base: string, path: string): string {
  if (!base) {
    return path;
  }
  return `${base.replace(/\/$/, '')}${path}`;
}

function buildQuery(params: object): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params as Record<string, unknown>)) {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  }
  const qs = search.toString();
  return qs ? `?${qs}` : '';
}

export function buildDebugHeaders(context: DebugContext, requestId?: string): HeadersInit {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    [DEBUG_HEADERS.actor]: context.actor,
    [DEBUG_HEADERS.roles]: context.roles.join(','),
    [DEBUG_HEADERS.requestId]: requestId ?? crypto.randomUUID(),
  };
  if (context.debugToken) {
    headers[DEBUG_HEADERS.token] = context.debugToken;
  }
  return headers;
}

async function parseApiResponse<T>(response: Response): Promise<T> {
  let body: ApiResponse<T> | null = null;
  try {
    body = (await response.json()) as ApiResponse<T>;
  } catch {
    if (!response.ok) {
      throw toConsoleError('UNKNOWN_ERROR', `HTTP ${response.status}`, response.status);
    }
    throw toConsoleError('JSON_PARSE_ERROR');
  }

  if (!response.ok) {
    const code = body?.error?.code ?? 'UNKNOWN_ERROR';
    const message = body?.error?.message;
    throw fromApiBody(code, message ?? code, response.status);
  }

  if (!body.success) {
    const code = body.error?.code ?? 'UNKNOWN_ERROR';
    throw fromApiBody(code, body.error?.message ?? code, response.status);
  }

  return body.data as T;
}

export function createConsoleClient(getContext: ContextGetter) {
  async function request<T>(path: string, init?: RequestInit): Promise<T> {
    const context = getContext();
    const url = joinUrl(context.apiBaseUrl, path);
    try {
      const response = await fetch(url, {
        ...init,
        headers: {
          ...buildDebugHeaders(context),
          ...(init?.headers ?? {}),
        },
      });
      return parseApiResponse<T>(response);
    } catch (error) {
      if (isConsoleError(error)) {
        throw error;
      }
      throw networkError(error);
    }
  }

  return {
    listRuntimeSessions(params: RuntimeListParams = {}) {
      return request<RuntimeConsoleSummary[]>(
        `/api/v1/debug/console/runtime-sessions${buildQuery(params)}`,
      );
    },

    getConsoleOverview() {
      return request<ConsoleOverview>('/api/v1/console/overview');
    },

    listConsoleRuntimes(params: RuntimeListParams = {}) {
      return request<Phase10RuntimeListItem[]>(`/api/v1/console/runtimes${buildQuery(params)}`);
    },

    getRuntimeTimeline(runtimeId: string) {
      return request<RuntimeTimeline>(
        `/api/v1/console/runtimes/${encodeURIComponent(runtimeId)}/timeline`,
      );
    },

    listGovernanceDomains() {
      return request<GovernanceDomainCard[]>('/api/v1/console/governance/domains');
    },

    listCandidateInbox(params: CandidateInboxParams = {}) {
      return request<CandidateInboxItem[]>(`/api/v1/console/candidates${buildQuery(params)}`);
    },

    listAuditBrowser(params: AuditBrowserParams = {}) {
      return request<AuditBrowserItem[]>(`/api/v1/console/audits${buildQuery(params)}`);
    },

    getRuntimeSession(runtimeId: string) {
      return request<RuntimeConsoleDetail>(
        `/api/v1/debug/console/runtime-sessions/${encodeURIComponent(runtimeId)}`,
      );
    },

    listEvaluationRuns(params: EvaluationListParams = {}) {
      return request<EvaluationConsoleSummary[]>(
        `/api/v1/debug/console/evaluation-runs${buildQuery(params)}`,
      );
    },

    getEvaluationRun(runId: string) {
      return request<EvaluationConsoleDetail>(
        `/api/v1/debug/console/evaluation-runs/${encodeURIComponent(runId)}`,
      );
    },

    listCandidateGenerations(params: CandidateGenerationListParams = {}) {
      return request<CandidateGenerationConsoleSummary[]>(
        `/api/v1/debug/console/candidate-generations${buildQuery(params)}`,
      );
    },

    listCandidates(params: CandidateListParams = {}) {
      return request<CandidateConsoleSummary[]>(
        `/api/v1/debug/console/candidates${buildQuery(params)}`,
      );
    },

    getCandidate(candidateId: string) {
      return request<CandidateConsoleDetail>(
        `/api/v1/debug/console/candidates/${encodeURIComponent(candidateId)}`,
      );
    },

    listReviewQueue(params: ReviewQueueListParams = {}) {
      return request<CandidateConsoleSummary[]>(
        `/api/v1/debug/console/review-queue${buildQuery(params)}`,
      );
    },

    reviewExperienceCandidate(candidateId: string, body: CandidateReviewRequestBody) {
      return request<CandidateReviewRecord>(
        `/api/v1/debug/candidates/experience-candidates/${encodeURIComponent(candidateId)}/review`,
        { method: 'POST', body: JSON.stringify(body) },
      );
    },

    reviewTrainingExampleCandidate(candidateId: string, body: CandidateReviewRequestBody) {
      return request<CandidateReviewRecord>(
        `/api/v1/debug/candidates/training-example-candidates/${encodeURIComponent(candidateId)}/review`,
        { method: 'POST', body: JSON.stringify(body) },
      );
    },

    listAuditLogs(params: AuditLogListParams = {}) {
      return request<AuditConsoleSummary[]>(
        `/api/v1/debug/console/audit-center/audit-logs${buildQuery(params)}`,
      );
    },

    getAuditLog(auditId: string) {
      return request<AuditConsoleSummary>(
        `/api/v1/debug/console/audit-center/audit-logs/${encodeURIComponent(auditId)}`,
      );
    },

    getAuditSummary() {
      return request<AuditCenterSummary>(`/api/v1/debug/console/audit-center/summary`);
    },

    testConnection() {
      return request<RuntimeConsoleSummary[]>(
        '/api/v1/debug/console/runtime-sessions?limit=1',
      );
    },
  };
}

export type ConsoleClient = ReturnType<typeof createConsoleClient>;

function isConsoleError(error: unknown): error is ConsoleError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'code' in error &&
    'message' in error
  );
}
