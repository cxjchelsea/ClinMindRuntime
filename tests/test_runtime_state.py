import json

from app.state.runtime_state import (
    CaseFrame,
    ClinicianReport,
    DecisionBoundaryResult,
    DifferentialDiagnosisBoard,
    EntryAssessmentResult,
    EvidenceGraph,
    EvidenceGraphItem,
    ExperienceContext,
    KnowledgeContext,
    NextAction,
    PatientOutput,
    QuestionTestPolicyResult,
    RuntimeState,
    SafetyGateResult,
    UserInput,
)
from app.state.runtime_status import (
    CandidateStatus,
    NextActionType,
    OutputLevel,
    RiskLevel,
    RuntimeMode,
    RuntimeStatus,
    WorkMode,
)


def test_runtime_state_default_creation() -> None:
    state = RuntimeState(session_id="s_001")
    assert state.runtime_id.startswith("rt_")
    assert state.runtime_status == RuntimeStatus.CREATED
    assert state.mode == RuntimeMode.PATIENT_FACING
    assert state.case_frame == CaseFrame()
    assert state.knowledge_context == KnowledgeContext()
    assert state.experience_context.implementation_mode == "empty"
    assert state.input_history == []
    assert state.version == 1


def test_runtime_state_json_round_trip() -> None:
    state = RuntimeState(
        session_id="s_001",
        user_id="u_001",
        runtime_status=RuntimeStatus.CLINICAL_MODE,
        work_mode=WorkMode.CLINICAL_MODE,
        entry_assessment=EntryAssessmentResult(
            work_mode=WorkMode.CLINICAL_MODE,
            symptom_group="chest_pain",
            reason="symptom detected",
        ),
        input_history=[UserInput(text="胸口闷")],
        safety_gate=SafetyGateResult(
            triggered=True,
            risk_level=RiskLevel.HIGH,
            matched_rules=["rf_001"],
        ),
        differential_board=DifferentialDiagnosisBoard(
            candidates=[
                {
                    "name": "high_risk_a",
                    "status": CandidateStatus.MUST_NOT_MISS,
                    "risk_level": RiskLevel.HIGH,
                }
            ]
        ),
        evidence_graph=EvidenceGraph(
            items=[
                EvidenceGraphItem(
                    diagnosis="high_risk_a",
                    missing_evidence=["activity_related"],
                    status=CandidateStatus.NEED_TO_RULE_OUT,
                )
            ]
        ),
        question_test_policy=QuestionTestPolicyResult(
            next_action=NextAction(
                type=NextActionType.ASK_QUESTION,
                content="是否活动后加重？",
                priority="high",
            ),
            reason="missing key evidence",
        ),
        decision_boundary=DecisionBoundaryResult(
            allowed_output_level=OutputLevel.O1_CONTINUE_QUESTIONING,
            patient_diagnosis_label_allowed=False,
            clinician_ddx_allowed=True,
            reason="high risk not ruled out",
        ),
        patient_output=PatientOutput(
            allowed=True,
            content="需要补充关键信息",
            output_level=OutputLevel.O1_CONTINUE_QUESTIONING,
        ),
        clinician_report=ClinicianReport(
            allowed=True,
            case_summary="58岁男性，胸闷",
        ),
    )

    payload = state.model_dump(mode="json")
    json_str = json.dumps(payload, ensure_ascii=False)
    restored = RuntimeState.model_validate_json(json_str)

    assert restored.runtime_id == state.runtime_id
    assert restored.session_id == "s_001"
    assert restored.entry_assessment is not None
    assert restored.entry_assessment.symptom_group == "chest_pain"
    assert restored.safety_gate is not None
    assert restored.safety_gate.triggered is True
    assert len(restored.differential_board.candidates) == 1
    assert restored.patient_output is not None
    assert restored.patient_output.content == "需要补充关键信息"


def test_nested_structures_defaults() -> None:
    knowledge = KnowledgeContext(symptom_group="fever")
    experience = ExperienceContext()
    assert knowledge.source_assets == []
    assert experience.matched_experience_units == []
    assert experience.implementation_mode == "empty"
