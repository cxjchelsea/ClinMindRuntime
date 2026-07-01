import { useState } from 'react';
import { formatConsoleError } from '../api/errors';
import { useDebugContext } from './DebugContextProvider';
import {
  AVAILABLE_ROLES,
  type DebugRole,
} from './debugContextTypes';
import './DebugContextPanel.css';

export function DebugContextPanel() {
  const { context, setContext, testConnection, connectionOk, globalError } = useDebugContext();
  const [testing, setTesting] = useState(false);

  const toggleRole = (role: DebugRole) => {
    const roles = context.roles.includes(role)
      ? context.roles.filter((r) => r !== role)
      : [...context.roles, role];
    setContext({ roles: roles.length ? roles : ['READ_ONLY_OBSERVER'] });
  };

  const handleTest = async () => {
    setTesting(true);
    try {
      await testConnection();
    } finally {
      setTesting(false);
    }
  };

  return (
    <section className="debug-panel" aria-label="Debug context">
      <h2 className="debug-panel__title">Debug Context</h2>

      <label className="debug-panel__field" htmlFor="debug-api-base-url">
        <span>API Base URL</span>
        <input
          id="debug-api-base-url"
          type="text"
          value={context.apiBaseUrl}
          placeholder="留空 = Vite 代理 /api"
          onChange={(e) => setContext({ apiBaseUrl: e.target.value })}
        />
      </label>

      <label className="debug-panel__field" htmlFor="debug-token">
        <span>X-Debug-Token</span>
        <input
          id="debug-token"
          type="password"
          value={context.debugToken}
          placeholder="require-debug-token 时必填"
          autoComplete="off"
          onChange={(e) => setContext({ debugToken: e.target.value })}
        />
        <small className="debug-panel__hint">Token 不会写入 localStorage</small>
      </label>

      <label className="debug-panel__field" htmlFor="debug-actor">
        <span>X-Debug-Actor</span>
        <input
          id="debug-actor"
          type="text"
          value={context.actor}
          onChange={(e) => setContext({ actor: e.target.value })}
        />
      </label>

      <fieldset className="debug-panel__roles">
        <legend>X-Debug-Roles</legend>
        {AVAILABLE_ROLES.map((role) => (
          <label key={role} className="debug-panel__role">
            <input
              type="checkbox"
              checked={context.roles.includes(role)}
              onChange={() => toggleRole(role)}
            />
            {role}
          </label>
        ))}
      </fieldset>

      <button
        type="button"
        className="debug-panel__test-btn"
        disabled={testing}
        onClick={handleTest}
      >
        {testing ? '测试中…' : '测试连接'}
      </button>

      {connectionOk === true && (
        <p className="debug-panel__status debug-panel__status--ok">连接成功</p>
      )}
      {globalError && (
        <p className="debug-panel__status debug-panel__status--error" role="alert">
          {formatConsoleError(globalError, context.roles)}
        </p>
      )}
    </section>
  );
}
