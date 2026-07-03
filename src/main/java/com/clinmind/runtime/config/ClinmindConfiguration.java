package com.clinmind.runtime.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ClinmindPersistenceProperties.class,
    ClinmindDebugApiProperties.class,
    ClinmindPythonProviderProperties.class
})
public class ClinmindConfiguration {
}
