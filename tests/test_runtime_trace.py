from app.state.runtime_state import DecisionBoundaryResult, SafetyGateResult
from app.state.runtime_status import OutputLevel, RiskLevel
from app.state.runtime_trace import RuntimeTrace


def test_runtime_trace_creation() -> None:
    trace = RuntimeTrace(runtime_id="rt_001", step=1, input="胸口闷")
    assert trace.trace_id.startswith("trace_")
    assert trace.runtime_id == "rt_001"
    assert trace.input == "胸口闷"
    assert trace.modules_executed == []


def test_runtime_trace_records_key_fields() -> None:
    safety = SafetyGateResult(
        triggered=True,
        risk_level=RiskLevel.HIGH,
        matched_rules=["rf_001"],
        reason="activity related chest pain",
    )
    boundary = DecisionBoundaryResult(
        allowed_output_level=OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION,
        patient_diagnosis_label_allowed=False,
        clinician_ddx_allowed=True,
        reason="high risk",
        constraints=["no_low_risk_reassurance"],
    )

    trace = RuntimeTrace(
        runtime_id="rt_001",
        step=2,
        input="活动后加重，出汗",
        modules_executed=["EntryAssessment", "SafetyGate", "DecisionBoundary"],
        knowledge_used=["assets/red_flag_rules.yml"],
        experience_used=[],
        safety_gate_result=safety,
        ddx_change={"added": ["high_risk_a"]},
        evidence_graph_change={"missing_evidence": ["sweating"]},
        decision_boundary_result=boundary,
        output_summary={"patient_output_level": "O5_visit_or_urgent_care_recommendation"},
    )

    assert "SafetyGate" in trace.modules_executed
    assert trace.safety_gate_result is not None
    assert trace.safety_gate_result.triggered is True
    assert trace.ddx_change == {"added": ["high_risk_a"]}
    assert trace.evidence_graph_change == {"missing_evidence": ["sweating"]}
    assert trace.decision_boundary_result is not None
    assert trace.output_summary is not None


def test_runtime_trace_helper_methods() -> None:
    trace = RuntimeTrace(runtime_id="rt_001", input="test")
    trace.record_module("EntryAssessment")
    trace.record_module("EntryAssessment")
    trace.record_knowledge("assets/symptom_groups/chest_pain.yml")
    trace.record_experience("exp_001")

    assert trace.modules_executed == ["EntryAssessment"]
    assert trace.knowledge_used == ["assets/symptom_groups/chest_pain.yml"]
    assert trace.experience_used == ["exp_001"]

    payload = trace.model_dump(mode="json")
    restored = RuntimeTrace.model_validate(payload)
    assert restored.trace_id == trace.trace_id
