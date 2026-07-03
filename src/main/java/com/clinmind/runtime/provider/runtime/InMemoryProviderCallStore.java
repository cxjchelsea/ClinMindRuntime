package com.clinmind.runtime.provider.runtime;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryProviderCallStore implements ProviderCallStore {

    private final Map<String, ProviderCallRecord> records = new ConcurrentHashMap<>();

    @Override
    public void save(ProviderCallRecord record) {
        records.put(record.providerCallId(), record);
    }

    @Override
    public Optional<ProviderCallRecord> findById(String providerCallId) {
        return Optional.ofNullable(records.get(providerCallId));
    }
}
