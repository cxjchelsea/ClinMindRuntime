package com.clinmind.runtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clinmind.debug-api")
public class ClinmindDebugApiProperties {

    private boolean enabled = true;
    private boolean requireDebugToken = false;
    private String debugToken = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRequireDebugToken() {
        return requireDebugToken;
    }

    public void setRequireDebugToken(boolean requireDebugToken) {
        this.requireDebugToken = requireDebugToken;
    }

    public String getDebugToken() {
        return debugToken;
    }

    public void setDebugToken(String debugToken) {
        this.debugToken = debugToken;
    }
}
