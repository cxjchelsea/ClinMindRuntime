package com.clinmind.runtime.toolgov.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryToolGovernanceStore<T> {

    private final ConcurrentMap<String, T> records = new ConcurrentHashMap<>();

    public T save(String id, T record) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        records.put(id, record);
        return record;
    }

    public Optional<T> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(records.get(id));
    }

    public List<T> findAll() {
        return List.copyOf(new ArrayList<>(records.values()));
    }
}
