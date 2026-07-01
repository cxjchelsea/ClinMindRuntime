import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import App from '../App';
import { DebugContextProvider } from '../auth/DebugContextProvider';

describe('App smoke', () => {
  it('renders console shell with navigation and debug context', () => {
    render(
      <DebugContextProvider>
        <MemoryRouter initialEntries={['/runtime']}>
          <App />
        </MemoryRouter>
      </DebugContextProvider>,
    );

    expect(screen.getByText('ClinMind Console')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Console navigation' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Runtime Sessions' })).toBeInTheDocument();
    expect(screen.getByRole('region', { name: 'Debug context' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Runtime Sessions' })).toBeInTheDocument();
  });
});

describe('DebugContextPanel', () => {
  it('updates actor and persists roles selection', async () => {
    const user = userEvent.setup();
    localStorage.clear();

    render(
      <DebugContextProvider>
        <MemoryRouter initialEntries={['/runtime']}>
          <App />
        </MemoryRouter>
      </DebugContextProvider>,
    );

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

    render(
      <DebugContextProvider>
        <MemoryRouter initialEntries={['/runtime']}>
          <App />
        </MemoryRouter>
      </DebugContextProvider>,
    );

    await user.click(screen.getByRole('button', { name: '测试连接' }));

    const panel = screen.getByRole('region', { name: 'Debug context' });
    expect(
      await within(panel).findByRole('alert'),
    ).toHaveTextContent('后端未启动或 API 地址错误');

    vi.unstubAllGlobals();
  });
});
