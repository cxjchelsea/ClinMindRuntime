export interface ConsoleError {
  code: string;
  message: string;
  httpStatus?: number;
}

const ERROR_MESSAGES: Record<string, string> = {
  DEBUG_TOKEN_REQUIRED: '请填写 X-Debug-Token',
  INVALID_DEBUG_TOKEN: 'Debug token 错误',
  ACCESS_DENIED: '当前角色无权访问该资源',
  CONSOLE_RESOURCE_NOT_FOUND: '资源不存在',
  CONSOLE_QUERY_INVALID: '查询参数错误',
  AUDIT_QUERY_INVALID: 'Audit 查询参数错误',
  NETWORK_ERROR: '后端未启动或 API 地址错误',
  JSON_PARSE_ERROR: '响应解析失败',
  UNKNOWN_ERROR: '未知错误',
};

export function mapErrorCode(code: string, fallbackMessage?: string): string {
  return ERROR_MESSAGES[code] ?? fallbackMessage ?? code;
}

export function mapHttpStatus(status: number): string {
  switch (status) {
    case 401:
      return '未授权（401）：请检查 Debug Token';
    case 403:
      return '禁止访问（403）：当前角色权限不足';
    case 404:
      return '资源不存在（404）';
    case 400:
      return '请求参数错误（400）';
    default:
      return `HTTP 错误（${status}）`;
  }
}

export function toConsoleError(
  code: string,
  message?: string,
  httpStatus?: number,
): ConsoleError {
  return {
    code,
    message: mapErrorCode(code, message),
    httpStatus,
  };
}

export function fromApiBody(
  code: string,
  message: string,
  httpStatus?: number,
): ConsoleError {
  return toConsoleError(code, message, httpStatus);
}

export function networkError(cause?: unknown): ConsoleError {
  const detail = cause instanceof Error ? cause.message : undefined;
  return {
    code: 'NETWORK_ERROR',
    message: detail ? `${ERROR_MESSAGES.NETWORK_ERROR}（${detail}）` : ERROR_MESSAGES.NETWORK_ERROR,
  };
}

export function formatConsoleError(error: ConsoleError, roles?: string[]): string {
  const parts = [error.message];
  if ((error.httpStatus === 403 || error.code === 'ACCESS_DENIED') && roles?.length) {
    parts.push(`当前 roles: ${roles.join(', ')}`);
  }
  if (error.code && error.code !== error.message) {
    parts.push(`[${error.code}]`);
  }
  return parts.join(' · ');
}
