export interface PatientSessionSummary {
  session_id: string;
  runtime_id: string;
  status: string;
  chief_complaint_summary: string;
  updated_at: string;
  safe_next_step: string;
}

export interface PatientFactSummary {
  label: string;
  value: string;
  confidence_note: string;
}

export interface PatientQuestion {
  id: string;
  prompt: string;
  reason_for_asking: string;
}

export interface SafetyNotice {
  level: 'info' | 'urgent';
  message: string;
}

export interface CareNavigationSuggestion {
  label: string;
  description: string;
}

export interface PatientRuntimeView {
  session_id: string;
  runtime_id: string;
  status: string;
  safe_summary: string;
  collected_facts: PatientFactSummary[];
  next_questions: PatientQuestion[];
  safety_notices: SafetyNotice[];
  care_navigation: CareNavigationSuggestion[];
  allowed_actions: string[];
  disclaimer: string;
}
