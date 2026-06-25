from __future__ import annotations

from datetime import datetime, timezone
from typing import Literal
from uuid import uuid4

from pydantic import BaseModel, Field

from app.state.runtime_status import (
    CandidateStatus,
    NextActionType,
    OutputLevel,
    RiskLevel,
    RuntimeMode,
    RuntimeStatus,
    WorkMode,
)


def _utc_now() -> datetime:
    return datetime.now(timezone.utc)


def _generate_runtime_id() -> str:
    return f"rt_{uuid4().hex[:12]}"


class UserInput(BaseModel):
    text: str
    attachments: list[str] = Field(default_factory=list)
    received_at: datetime = Field(default_factory=_utc_now)


class EntryAssessmentResult(BaseModel):
    work_mode: WorkMode
    symptom_group: str | None = None
    reason: str | None = None
    confidence: float | None = None


class PatientProfile(BaseModel):
    age: int | None = None
    sex: str | None = None
    risk_factors: list[str] = Field(default_factory=list)


class SymptomItem(BaseModel):
    name: str | None = None
    duration: str | None = None
    severity: str | None = None
    location: str | None = None
    trigger: str | None = None
    frequency: str | None = None
    relief: str | None = None


class CaseFrame(BaseModel):
    chief_complaint: str | None = None
    patient_profile: PatientProfile = Field(default_factory=PatientProfile)
    symptoms: list[SymptomItem] = Field(default_factory=list)
    past_history: list[str] = Field(default_factory=list)
    medication_history: list[str] = Field(default_factory=list)
    examination_results: list[str] = Field(default_factory=list)
    missing_slots: list[str] = Field(default_factory=list)
    conflicting_slots: list[str] = Field(default_factory=list)


class DiagnosisRef(BaseModel):
    name: str
    risk_level: RiskLevel = RiskLevel.UNKNOWN


class RedFlagRule(BaseModel):
    rule_id: str
    symptom_group: str
    features: list[str] = Field(default_factory=list)
    risk_level: RiskLevel = RiskLevel.HIGH
    action: str | None = None
    patient_constraint: str | None = None


class KnowledgeContext(BaseModel):
    symptom_group: str | None = None
    common_diagnoses: list[DiagnosisRef] = Field(default_factory=list)
    must_not_miss: list[DiagnosisRef] = Field(default_factory=list)
    red_flags: list[RedFlagRule] = Field(default_factory=list)
    required_questions: list[str] = Field(default_factory=list)
    recommended_tests: list[str] = Field(default_factory=list)
    source_assets: list[str] = Field(default_factory=list)


class ExperienceUnit(BaseModel):
    unit_id: str
    title: str
    content: str


class ExperienceContext(BaseModel):
    matched_experience_units: list[ExperienceUnit] = Field(default_factory=list)
    experience_alerts: list[str] = Field(default_factory=list)
    implementation_mode: Literal["empty", "mock"] = "empty"


class SafetyGateResult(BaseModel):
    triggered: bool = False
    risk_level: RiskLevel = RiskLevel.NONE
    matched_rules: list[str] = Field(default_factory=list)
    reason: str | None = None
    required_action: str | None = None
    patient_output_constraint: str | None = None
    fail_safe_required: bool = False


class DDxCandidate(BaseModel):
    name: str
    status: CandidateStatus
    risk_level: RiskLevel = RiskLevel.UNKNOWN
    reason: str | None = None
    patient_visible: bool = False


class DifferentialDiagnosisBoard(BaseModel):
    candidates: list[DDxCandidate] = Field(default_factory=list)
    updated_reason: str | None = None


class EvidenceGraphItem(BaseModel):
    diagnosis: str
    supporting_evidence: list[str] = Field(default_factory=list)
    opposing_evidence: list[str] = Field(default_factory=list)
    missing_evidence: list[str] = Field(default_factory=list)
    conflicting_evidence: list[str] = Field(default_factory=list)
    status: CandidateStatus = CandidateStatus.INSUFFICIENT_EVIDENCE
    next_questions: list[str] = Field(default_factory=list)
    recommended_tests: list[str] = Field(default_factory=list)


class EvidenceGraph(BaseModel):
    items: list[EvidenceGraphItem] = Field(default_factory=list)


class NextAction(BaseModel):
    type: NextActionType
    content: str
    purpose: str | None = None
    target_diagnosis: str | None = None
    priority: Literal["low", "medium", "high"] = "medium"


class QuestionTestPolicyResult(BaseModel):
    next_action: NextAction
    reason: str


class DecisionBoundaryResult(BaseModel):
    allowed_output_level: OutputLevel
    patient_diagnosis_label_allowed: bool = False
    clinician_ddx_allowed: bool = False
    reason: str
    constraints: list[str] = Field(default_factory=list)


class PatientOutput(BaseModel):
    allowed: bool = False
    content: str = ""
    output_level: OutputLevel = OutputLevel.O1_CONTINUE_QUESTIONING
    constraints_applied: list[str] = Field(default_factory=list)


class ClinicianReport(BaseModel):
    allowed: bool = False
    case_summary: str | None = None
    safety_summary: str | None = None
    ddx_summary: list[DDxCandidate] = Field(default_factory=list)
    evidence_summary: EvidenceGraph | None = None
    recommended_questions: list[str] = Field(default_factory=list)
    recommended_tests: list[str] = Field(default_factory=list)


class RuntimeState(BaseModel):
    runtime_id: str = Field(default_factory=_generate_runtime_id)
    session_id: str
    user_id: str | None = None
    version: int = 1
    runtime_status: RuntimeStatus = RuntimeStatus.CREATED
    work_mode: WorkMode | None = None
    mode: RuntimeMode = RuntimeMode.PATIENT_FACING
    input_history: list[UserInput] = Field(default_factory=list)
    entry_assessment: EntryAssessmentResult | None = None
    case_frame: CaseFrame = Field(default_factory=CaseFrame)
    knowledge_context: KnowledgeContext = Field(default_factory=KnowledgeContext)
    experience_context: ExperienceContext = Field(default_factory=ExperienceContext)
    safety_gate: SafetyGateResult | None = None
    differential_board: DifferentialDiagnosisBoard = Field(default_factory=DifferentialDiagnosisBoard)
    evidence_graph: EvidenceGraph = Field(default_factory=EvidenceGraph)
    question_test_policy: QuestionTestPolicyResult | None = None
    decision_boundary: DecisionBoundaryResult | None = None
    patient_output: PatientOutput | None = None
    clinician_report: ClinicianReport | None = None
    runtime_trace_ids: list[str] = Field(default_factory=list)
    created_at: datetime = Field(default_factory=_utc_now)
    updated_at: datetime = Field(default_factory=_utc_now)

    def touch(self) -> None:
        self.updated_at = _utc_now()

    def bump_version(self) -> None:
        self.version += 1
        self.touch()
