package com.clinmind.runtime.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

@Component
public class JsonSnapshotMapper {

    private final ObjectMapper objectMapper;

    public JsonSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new PersistenceSerializationException("Failed to serialize snapshot", ex);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new PersistenceSerializationException("Failed to deserialize snapshot", ex);
        }
    }

    public <T> T fromJson(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new PersistenceSerializationException("Failed to deserialize snapshot", ex);
        }
    }

    public PGobject toJsonb(Object value) {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            jsonObject.setValue(toJson(value));
        } catch (Exception ex) {
            throw new PersistenceSerializationException("Failed to create jsonb value", ex);
        }
        return jsonObject;
    }

    public String readJsonb(Object dbValue) {
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof PGobject pgObject) {
            return pgObject.getValue();
        }
        return String.valueOf(dbValue);
    }
}
