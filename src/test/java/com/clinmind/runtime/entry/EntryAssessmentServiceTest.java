package com.clinmind.runtime.entry;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EntryAssessmentServiceTest {

    private final EntryAssessmentService service = new EntryAssessmentService();

    @ParameterizedTest
    @CsvSource({
            "怎么养生比较好, WELLNESS_MODE",
            "我最近胸口闷，活动后更明显, CLINICAL_MODE",
            "胸口闷，活动后加重，出汗, EMERGENCY_HINT",
            "呼吸困难，快不行了, EMERGENCY_HINT",
            "帮我写Python代码, UNSUPPORTED"
    })
    void assessEntryModes(String text, WorkMode expectedMode) {
        assertThat(service.assessEntry(new UserInput(text), null).workMode()).isEqualTo(expectedMode);
    }

    @Test
    void emergencyHintOnlyMarksWorkMode() {
        var result = service.assessEntry(new UserInput("胸口闷，活动后加重，出汗"), null);
        assertThat(result.workMode()).isEqualTo(WorkMode.EMERGENCY_HINT);
        assertThat(result.workMode().getValue()).isNotEqualTo("safety_gate_triggered");
    }

    @Test
    void detectsFeverSymptomGroup() {
        var result = service.assessEntry(new UserInput("我发烧两天了"), null);
        assertThat(result.workMode()).isEqualTo(WorkMode.CLINICAL_MODE);
        assertThat(result.symptomGroup()).isEqualTo("fever");
    }
}
