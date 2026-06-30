package com.clinmind.runtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clinmind.persistence")
public class ClinmindPersistenceProperties {

    private String mode = "in-memory";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isPostgres() {
        return "postgres".equalsIgnoreCase(mode);
    }

    public boolean isInMemory() {
        return !isPostgres();
    }
}
