import { describe, expect, it } from 'vitest';
import {
  containsSensitiveMarker,
  filterSensitiveFields,
  SENSITIVE_FIELD_KEYS,
  toSafeDisplayRecord,
} from '../components/SensitiveFieldRenderGuard';

describe('SensitiveFieldRenderGuard', () => {
  it('removes blocked field keys recursively', () => {
    const input = {
      runtime_id: 'rt_001',
      patient_output: 'secret',
      input_history: [{ text: 'hidden' }],
      nested: {
        clinician_report: 'hidden',
        trace_count: 3,
      },
    };

    const filtered = filterSensitiveFields(input) as Record<string, unknown>;
    expect(filtered.runtime_id).toBe('rt_001');
    expect(filtered.trace_count).toBeUndefined();
    expect(filtered.patient_output).toBeUndefined();
    expect(filtered.input_history).toBeUndefined();
    expect((filtered.nested as Record<string, unknown>).trace_count).toBe(3);
  });

  it('builds safe display records without sensitive keys', () => {
    const safe = toSafeDisplayRecord({
      session_id: 's1',
      patient_output: 'must not render',
      safety_gate_triggered: true,
    });

    expect(Object.keys(safe)).toEqual(['session_id', 'safety_gate_triggered']);
    expect(SENSITIVE_FIELD_KEYS.has('patient_output')).toBe(true);
  });

  it('detects sensitive markers in rendered text', () => {
    expect(containsSensitiveMarker('{"patient_output":"x"}')).toBe(true);
    expect(containsSensitiveMarker('runtime_status=COMPLETED')).toBe(false);
  });

  it('filters candidate policy metadata input fields', () => {
    const safe = toSafeDisplayRecord({
      candidate_id: 'c1',
      policy_metadata: { input: { text: 'secret' }, sanitizer: 'v1' },
    });
    expect(JSON.stringify(safe)).not.toContain('"input"');
    expect(JSON.stringify(safe)).toContain('sanitizer');
  });
});
