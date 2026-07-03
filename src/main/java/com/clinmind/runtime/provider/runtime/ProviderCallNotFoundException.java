package com.clinmind.runtime.provider.runtime;

import com.clinmind.runtime.api.ApiException;
import org.springframework.http.HttpStatus;

public class ProviderCallNotFoundException extends ApiException {

    public ProviderCallNotFoundException(String providerCallId) {
        super(HttpStatus.NOT_FOUND, "PROVIDER_CALL_NOT_FOUND", "provider call not found: " + providerCallId);
    }
}
