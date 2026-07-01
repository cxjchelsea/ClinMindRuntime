import { describe, expect, it } from 'vitest';
import {
  formatConsoleError,
  mapErrorCode,
  mapHttpStatus,
  networkError,
  toConsoleError,
} from '../api/errors';

describe('error mapping', () => {
  it('maps known error codes to user messages', () => {
    expect(mapErrorCode('ACCESS_DENIED')).toBe('当前角色无权访问该资源');
    expect(mapErrorCode('DEBUG_TOKEN_REQUIRED')).toBe('请填写 X-Debug-Token');
  });

  it('maps http status to messages', () => {
    expect(mapHttpStatus(401)).toContain('401');
    expect(mapHttpStatus(403)).toContain('403');
    expect(mapHttpStatus(404)).toContain('404');
  });

  it('builds console errors with mapped messages', () => {
    const error = toConsoleError('INVALID_DEBUG_TOKEN', 'raw', 401);
    expect(error.code).toBe('INVALID_DEBUG_TOKEN');
    expect(error.message).toBe('Debug token 错误');
    expect(error.httpStatus).toBe(401);
  });

  it('formats 403 with current roles', () => {
    const error = toConsoleError('ACCESS_DENIED');
    const text = formatConsoleError(error, ['READ_ONLY_OBSERVER']);
    expect(text).toContain('当前 roles: READ_ONLY_OBSERVER');
  });

  it('creates network errors', () => {
    expect(networkError().code).toBe('NETWORK_ERROR');
  });
});
