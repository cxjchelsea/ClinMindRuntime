export interface ClinicianCaseSummary {
  case_id: string;
  runtime_id: string;
  status: string;
  risk_level: string;
  chief_complaint_summary: string;
  updated_at: string;
  assigned_clinician?: string;
}

export interface PatientSummary {
  age_band: string;
  sex: string;
  chief_complaint_summary: string;
  context_notes: string[];
}

export interface CaseFrameView {
  current_problem: string;
  known_context: string[];
  missing_information: string[];
}

export interface InquiryTurnView {
  speaker: 'patient' | 'assistant';
  summary: string;
  timestamp: string;
}

export interface DdxCandidateView {
  name: string;
  likelihood: 'low' | 'medium' | 'high';
  supporting_summary: string;
  uncertainty_note: string;
}

export interface EvidenceItemView {
  title: string;
  source: string;
  summary: string;
  relevance: string;
}

export interface RiskSignalView {
  label: string;
  level: 'watch' | 'urgent';
  note: string;
}

export interface ClinicianSuggestionView {
  label: string;
  description: string;
}

export interface ReportDraftView {
  impression: string;
  suggested_questions: string[];
  clinician_note: string;
}

export interface ClinicianCaseView {
  case_id: string;
  runtime_id: string;
  patient_summary: PatientSummary;
  case_frame: CaseFrameView;
  inquiry_timeline: InquiryTurnView[];
  ddx_board: DdxCandidateView[];
  evidence_panel: EvidenceItemView[];
  risk_panel: RiskSignalView[];
  ai_suggestions: ClinicianSuggestionView[];
  report_draft: ReportDraftView;
}
