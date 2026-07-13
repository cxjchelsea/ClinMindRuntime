import {
  fromApiBody,
  networkError,
  toConsoleError,
  type ConsoleError,
} from '../../api/errors';
import type { ApiResponse } from '../../api/types';
import type { DebugContext } from '../../auth/debugContextTypes';
import { DEBUG_HEADERS } from '../../auth/debugContextTypes';

function joinUrl(base: string, path: string): string {
  if (!base) {
    return path;
  }
  return `${base.replace(/\/$/, '')}${path}`;
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
    throw fromApiBody(code, body?.error?.message ?? code, response.status);
  }

  if (!body.success) {
    const code = body.error?.code ?? 'UNKNOWN_ERROR';
    throw fromApiBody(code, body.error?.message ?? code, response.status);
  }

  return body.data as T;
}

export function createRoleViewHttpClient(getContext: () => DebugContext) {
  async function request<T>(path: string): Promise<T> {
    const context = getContext();
    try {
      const response = await fetch(joinUrl(context.apiBaseUrl, path), {
        headers: buildDebugHeaders(context),
      });
      return parseApiResponse<T>(response);
    } catch (error) {
      if (isConsoleError(error)) {
        throw error;
      }
      throw networkError(error);
    }
  }

  return { request };
}

function isConsoleError(error: unknown): error is ConsoleError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'code' in error &&
    'message' in error
  );
}
