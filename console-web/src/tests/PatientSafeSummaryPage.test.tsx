import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { PatientSafeSummaryPage } from '../portals/patient/pages/PatientSafeSummaryPage';

describe('PatientSafeSummaryPage', () => {
  it('renders the safe patient projection without internal governance terms', () => {
    const { container } = render(<PatientSafeSummaryPage />);

    expect(screen.getByRole('heading', { name: 'Patient Safe Summary' })).toBeInTheDocument();
    expect(screen.getByText(/不能替代医生诊疗/)).toBeInTheDocument();

    const rendered = container.textContent?.toLowerCase() ?? '';
    expect(rendered).not.toContain('ddx');
    expect(rendered).not.toContain('audit');
    expect(rendered).not.toContain('trace');
    expect(rendered).not.toContain('candidate');
    expect(rendered).not.toContain('evaluation');
    expect(rendered).not.toContain('confidence_score');
    expect(rendered).not.toContain('raw_evidence');
    expect(rendered).not.toContain('raw_tool_result');
    expect(rendered).not.toContain('model_prompt');
    expect(rendered).not.toContain('internal_reasoning');
  });
});
