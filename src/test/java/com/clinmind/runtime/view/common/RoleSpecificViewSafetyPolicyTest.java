package com.clinmind.runtime.view.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleSpecificViewSafetyPolicyTest {

    private final RoleSpecificViewSafetyPolicy policy = new RoleSpecificViewSafetyPolicy();

    @Test
    void patientForbiddenFieldsIncludeInternalGovernancePayloads() {
        assertThat(policy.isPatientForbidden("ddx_candidates")).isTrue();
        assertThat(policy.isPatientForbidden("trace_nodes")).isTrue();
        assertThat(policy.isPatientForbidden("raw_evidence")).isTrue();
        assertThat(policy.isPatientForbidden("model_prompt")).isTrue();
        assertThat(policy.isPatientForbidden("safe_summary")).isFalse();
    }

    @Test
    void clinicianForbiddenFieldsIncludeRawProviderPayloads() {
        assertThat(policy.isClinicianForbidden("raw_prompt")).isTrue();
        assertThat(policy.isClinicianForbidden("api_key")).isTrue();
        assertThat(policy.isClinicianForbidden("raw_external_response")).isTrue();
        assertThat(policy.isClinicianForbidden("full_rationale")).isTrue();
        assertThat(policy.isClinicianForbidden("evidence_panel")).isFalse();
    }
}
