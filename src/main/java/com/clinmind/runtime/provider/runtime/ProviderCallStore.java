package com.clinmind.runtime.provider.runtime;

import java.util.Optional;

public interface ProviderCallStore {

    void save(ProviderCallRecord record);

    Optional<ProviderCallRecord> findById(String providerCallId);
}
