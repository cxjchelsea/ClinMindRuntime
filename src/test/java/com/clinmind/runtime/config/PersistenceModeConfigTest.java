package com.clinmind.runtime.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersistenceModeConfigTest {

    @Test
    void defaultsToInMemoryMode() {
        ClinmindPersistenceProperties properties = new ClinmindPersistenceProperties();

        assertThat(properties.getMode()).isEqualTo("in-memory");
        assertThat(properties.isInMemory()).isTrue();
        assertThat(properties.isPostgres()).isFalse();
    }

    @Test
    void recognizesPostgresMode() {
        ClinmindPersistenceProperties properties = new ClinmindPersistenceProperties();
        properties.setMode("postgres");

        assertThat(properties.isPostgres()).isTrue();
        assertThat(properties.isInMemory()).isFalse();
    }
}
