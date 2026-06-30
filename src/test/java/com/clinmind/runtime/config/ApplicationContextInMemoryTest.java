package com.clinmind.runtime.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.candidate.store.InMemoryCandidateStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationContextInMemoryTest {

    @Autowired
    private ClinmindPersistenceProperties persistenceProperties;

    @Autowired
    private CandidateStore candidateStore;

    @Test
    void loadsInMemoryModeByDefault() {
        assertThat(persistenceProperties.isInMemory()).isTrue();
        assertThat(candidateStore).isInstanceOf(InMemoryCandidateStore.class);
    }
}
