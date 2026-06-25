package com.clinmind.runtime.experience;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ExperienceContext;
import com.clinmind.runtime.state.KnowledgeContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExperienceContextServiceTest {

    @Autowired
    private ExperienceContextService experienceContextService;

    @Test
    void returnsEmptyExperienceContext() {
        ExperienceContext context = experienceContextService.buildExperienceContext(
                new CaseFrame(), new KnowledgeContext());

        assertThat(context.matchedExperienceUnits()).isEmpty();
        assertThat(context.experienceAlerts()).isEmpty();
        assertThat(context.implementationMode()).isEqualTo("empty");
    }
}
