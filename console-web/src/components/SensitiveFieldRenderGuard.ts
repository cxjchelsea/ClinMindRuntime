export const SENSITIVE_FIELD_KEYS = new Set([
  'patient_output',
  'clinician_report',
  'input_history',
  'input_texts',
  'raw_input',
  'patient_input',
  'input',
  'ddx_board',
  'differential_diagnosis',
  'full_ddx_board',
]);

const SENSITIVE_CONTENT_PATTERNS = [
  'patient_output',
  'clinician_report',
  'input_history',
  'input_texts',
  'raw_input',
  'patient_input',
  'raw training input',
  'full ddx board',
];

export function filterSensitiveFields(value: unknown): unknown {
  if (Array.isArray(value)) {
    return value.map((item) => filterSensitiveFields(item));
  }
  if (value && typeof value === 'object') {
    const result: Record<string, unknown> = {};
    for (const [key, nested] of Object.entries(value as Record<string, unknown>)) {
      if (SENSITIVE_FIELD_KEYS.has(key)) {
        continue;
      }
      result[key] = filterSensitiveFields(nested);
    }
    return result;
  }
  return value;
}

export function containsSensitiveMarker(text: string): boolean {
  const lower = text.toLowerCase();
  return SENSITIVE_CONTENT_PATTERNS.some((pattern) => lower.includes(pattern));
}

export function toSafeDisplayRecord(data: Record<string, unknown> | null): Record<string, unknown> {
  if (!data) {
    return {};
  }
  return filterSensitiveFields(data) as Record<string, unknown>;
}
