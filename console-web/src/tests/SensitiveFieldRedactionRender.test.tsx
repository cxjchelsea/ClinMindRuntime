import { render, screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { DetailPanel } from '../components/DetailPanel';
import {
  containsSensitiveMarker,
  toSafeDisplayRecord,
} from '../components/SensitiveFieldRenderGuard';

const SENSITIVE_SAMPLES = [
  {
    label: 'runtime detail',
    payload: {
      runtime_id: 'rt_001',
      session_id: 's1',
      runtime_status: 'COMPLETED',
      patient_output: '患者原文不应显示',
      clinician_report: '医生报告不应显示',
      input_history: [{ text: 'hidden turn' }],
      input_texts: ['turn-a'],
    },
  },
  {
    label: 'candidate detail',
    payload: {
      candidate_id: 'c1',
      candidate_kind: 'TRAINING_EXAMPLE_CANDIDATE',
      review_status: 'REVIEW_REQUIRED',
      policy_metadata: {
        sanitizer: 'v1',
        input: { text: 'raw training input secret' },
      },
    },
  },
  {
    label: 'evaluation detail',
    payload: {
      run_id: 'run_1',
      status: 'COMPLETED',
      ddx_board: { items: ['hidden'] },
      full_ddx_board: { hidden: true },
    },
  },
  {
    label: 'audit detail',
    payload: {
      audit_id: 'audit_1',
      action_type: 'QUERY_CONSOLE_RUNTIME',
      metadata_summary: {
        mode: 'in-memory',
        patient_output: 'must not render',
        raw_input: 'must not render',
      },
    },
  },
] as const;

describe('SensitiveFieldRedactionRender', () => {
  it.each(SENSITIVE_SAMPLES)(
    'DetailPanel does not render sensitive fields for $label',
    ({ payload }) => {
      render(<DetailPanel title="Safe Detail" data={payload as Record<string, unknown>} />);

      const panel = screen.getByRole('heading', { name: 'Safe Detail' }).closest('section');
      expect(panel).not.toBeNull();

      const allText = panel!.textContent ?? '';

      expect(allText).not.toMatch(/patient_output|clinician_report|input_history|input_texts/);
      expect(allText).not.toMatch(/raw training input|must not render|患者原文不应显示/);
      expect(allText).not.toMatch(/ddx_board|full_ddx_board/);
      expect(containsSensitiveMarker(allText)).toBe(false);
      expect(within(panel!).getByText('Safe Detail')).toBeInTheDocument();
    },
  );

  it('toSafeDisplayRecord keeps non-sensitive audit and runtime fields', () => {
    const safe = toSafeDisplayRecord({
      runtime_id: 'rt_001',
      audit_id: 'audit_1',
      metadata_summary: { mode: 'in-memory', patient_output: 'x' },
    });

    expect(safe).toMatchObject({
      runtime_id: 'rt_001',
      audit_id: 'audit_1',
    });
    expect(JSON.stringify(safe)).toContain('in-memory');
    expect(JSON.stringify(safe)).not.toContain('patient_output');
  });
});
