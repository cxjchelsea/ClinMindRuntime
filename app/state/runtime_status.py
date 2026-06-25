from enum import StrEnum


class RuntimeStatus(StrEnum):
    CREATED = "created"
    ENTRY_ASSESSING = "entry_assessing"
    WELLNESS_MODE = "wellness_mode"
    CLINICAL_MODE = "clinical_mode"
    COLLECTING_CASE_INFO = "collecting_case_info"
    SAFETY_GATE_TRIGGERED = "safety_gate_triggered"
    BUILDING_DIFFERENTIAL = "building_differential"
    COLLECTING_EVIDENCE = "collecting_evidence"
    RECOMMENDING_TESTS = "recommending_tests"
    WAITING_FOR_USER = "waiting_for_user"
    READY_FOR_PATIENT_OUTPUT = "ready_for_patient_output"
    READY_FOR_CLINICIAN_REPORT = "ready_for_clinician_report"
    COMPLETED = "completed"
    ERROR_SAFE_HALTED = "error_safe_halted"


class WorkMode(StrEnum):
    WELLNESS_MODE = "wellness_mode"
    CLINICAL_MODE = "clinical_mode"
    EMERGENCY_HINT = "emergency_hint"
    UNSUPPORTED = "unsupported"


class RuntimeMode(StrEnum):
    PATIENT_FACING = "patient_facing"
    CLINICIAN_COPILOT = "clinician_copilot"
    DEBUG = "debug"


class RiskLevel(StrEnum):
    NONE = "none"
    LOW = "low"
    MEDIUM = "medium"
    MEDIUM_HIGH = "medium_high"
    HIGH = "high"
    UNKNOWN = "unknown"


class CandidateStatus(StrEnum):
    PRIMARY_HYPOTHESIS = "primary_hypothesis"
    MAIN_ALTERNATIVE = "main_alternative"
    MUST_NOT_MISS = "must_not_miss"
    NEED_TO_RULE_OUT = "need_to_rule_out"
    POSSIBLE = "possible"
    POSSIBLE_AFTER_EXCLUSION = "possible_after_exclusion"
    UNLIKELY = "unlikely"
    INSUFFICIENT_EVIDENCE = "insufficient_evidence"


class NextActionType(StrEnum):
    ASK_QUESTION = "ask_question"
    RECOMMEND_TEST = "recommend_test"
    RECOMMEND_VISIT = "recommend_visit"
    WAIT_FOR_USER = "wait_for_user"
    GENERATE_PATIENT_OUTPUT = "generate_patient_output"
    GENERATE_CLINICIAN_REPORT = "generate_clinician_report"
    SAFE_HALT = "safe_halt"


class OutputLevel(StrEnum):
    O1_CONTINUE_QUESTIONING = "O1_continue_questioning"
    O2_RISK_HINT = "O2_risk_hint"
    O3_CLINICIAN_CANDIDATE_DIAGNOSIS = "O3_clinician_candidate_diagnosis"
    O4_LOW_RISK_REFERENCE = "O4_low_risk_reference"
    O5_VISIT_OR_URGENT_CARE_RECOMMENDATION = "O5_visit_or_urgent_care_recommendation"
    O6_TRANSFER_TO_DOCTOR = "O6_transfer_to_doctor"
    O7_CLINICIAN_FULL_REPORT = "O7_clinician_full_report"
