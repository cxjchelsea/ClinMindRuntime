package com.clinmind.runtime.caseframe;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.UserInput;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CaseFrameServiceTest {

    private final CaseFrameService service = new CaseFrameService();

    @Test
    void buildCaseFrameFromBasicInfo() {
        var frame = service.buildOrUpdateCaseFrame(
                new UserInput("胸口闷，活动后更明显"),
                null,
                Map.of("age", 58, "sex", "male"));

        assertThat(frame.patientProfile().age()).isEqualTo(58);
        assertThat(frame.patientProfile().sex()).isEqualTo("male");
        assertThat(frame.chiefComplaint()).isEqualTo("胸口闷");
        assertThat(frame.symptoms()).anyMatch(item -> "chest_discomfort".equals(item.name()));
        assertThat(frame.missingSlots()).contains("symptom_duration");
    }

    @Test
    void updateExistingCaseFrame() {
        var existing = service.buildOrUpdateCaseFrame(
                new UserInput("胸口闷"),
                null,
                Map.of("age", 58, "sex", "male"));
        var updated = service.buildOrUpdateCaseFrame(
                new UserInput("有点出汗，走路快的时候更明显，休息会缓解"),
                existing,
                null);

        assertThat(updated.chiefComplaint()).isEqualTo("胸口闷");
        assertThat(updated.symptoms()).anyMatch(item -> "sweating".equals(item.name()));
        assertThat(updated.patientProfile().age()).isEqualTo(58);
    }

    @Test
    void missingSlotsGenerated() {
        var frame = service.buildOrUpdateCaseFrame(new UserInput("今天有点不舒服"), null, null);
        assertThat(frame.missingSlots()).contains("age", "sex");
        assertThat(frame.chiefComplaint()).isEqualTo("今天有点不舒服");
    }
}
