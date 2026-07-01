import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import App from '../App';

describe('App smoke', () => {
  it('renders console shell with navigation', () => {
    render(
      <MemoryRouter initialEntries={['/runtime']}>
        <App />
      </MemoryRouter>,
    );

    expect(screen.getByText('ClinMind Console')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Console navigation' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Runtime Sessions' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Evaluation Runs' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Candidates' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Review Queue' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Audit Center' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Runtime Sessions' })).toBeInTheDocument();
  });
});
