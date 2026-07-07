package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.toolgov.policy.SkillRegistryPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillRegistryPolicyTest {

    @Test
    void rejectsMissingOutputContract() {
        SkillRegistryEntry entry = new SkillRegistryEntry(
                "skill_reg_001",
                "mock_case_summary_skill",
                "0.1.0",
                "Mock Skill",
                SkillType.LOCAL_DETERMINISTIC,
                "CASE_SUMMARY",
                List.of("evidence_enrichment"),
                List.of("patient_direct_answer"),
                "0.9.0",
                "",
                true,
                true,
                ToolRegistryStatus.DRAFT,
                "LOW",
                null,
                "tester");

        assertThat(new SkillRegistryPolicy().validateCreate(entry).reasons())
                .contains("output_contract_version missing");
    }
}
