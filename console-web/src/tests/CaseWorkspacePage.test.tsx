import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { CaseWorkspacePage } from '../portals/clinician/pages/CaseWorkspacePage';

describe('CaseWorkspacePage', () => {
  it('renders clinician projection without raw provider fields', () => {
    const { container } = render(<CaseWorkspacePage />);

    expect(screen.getByRole('heading', { name: 'Case Workspace' })).toBeInTheDocument();
    expect(screen.getByText(/候选方向仅用于医生复核/)).toBeInTheDocument();
    expect(screen.getByText(/仅展示摘要、来源和相关性/)).toBeInTheDocument();

    const rendered = container.textContent?.toLowerCase() ?? '';
    expect(rendered).not.toContain('raw_prompt');
    expect(rendered).not.toContain('secret');
    expect(rendered).not.toContain('raw_external_response');
    expect(rendered).not.toContain('full rationale');
    expect(rendered).not.toContain('api_key');
    expect(rendered).not.toContain('private_key');
    expect(rendered).not.toContain('internal_chain_of_thought');
    expect(rendered).not.toContain('full_rationale');
    expect(rendered).not.toContain('unredacted_patient_dialogue');
    expect(rendered).not.toContain('raw patient dialogue');
  });
});
