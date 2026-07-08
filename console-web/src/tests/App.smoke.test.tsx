import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import App from '../App';
import { DebugContextProvider } from '../auth/DebugContextProvider';
import { DemoRoleProvider } from '../rbac/DemoRoleProvider';

function renderApp(route = '/governance/runtimes') {
  return render(
    <DebugContextProvider>
      <DemoRoleProvider>
        <MemoryRouter initialEntries={[route]}>
          <App />
        </MemoryRouter>
      </DemoRoleProvider>
    </DebugContextProvider>,
  );
}

describe('App smoke', () => {
  it('renders portal shell with governance navigation and debug context', async () => {
    renderApp('/runtime');

    expect(screen.getByText('ClinMind Runtime')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Portal navigation' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Runtimes' })).toBeInTheDocument();
    expect(screen.getByRole('region', { name: 'Debug context' })).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Runtime Timeline' })).toBeInTheDocument();
  });
});

describe('DebugContextPanel', () => {
  it('updates actor and persists roles selection', async () => {
    const user = userEvent.setup();
    localStorage.clear();

    renderApp('/runtime');

    const actorInput = screen.getByLabelText('X-Debug-Actor');
    await user.clear(actorInput);
    await user.type(actorInput, 'reviewer-a');

    expect(actorInput).toHaveValue('reviewer-a');

    const adminRole = screen.getByRole('checkbox', { name: 'SYSTEM_ADMIN' });
    await user.click(adminRole);

    expect(localStorage.getItem('clinmind.console.actor')).toBe('reviewer-a');
    expect(localStorage.getItem('clinmind.console.roles')).toContain('SYSTEM_ADMIN');
  });

  it('shows network error when connection test fails', async () => {
    const user = userEvent.setup();
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new TypeError('Failed to fetch')),
    );

    renderApp('/runtime');

    await user.click(screen.getByRole('button', { name: '测试连接' }));

    const panel = screen.getByRole('region', { name: 'Debug context' });
    expect(
      await within(panel).findByRole('alert'),
    ).toHaveTextContent('后端未启动或 API 地址错误');

    vi.unstubAllGlobals();
  });
});
