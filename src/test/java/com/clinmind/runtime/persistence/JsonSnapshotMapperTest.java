package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonSnapshotMapperTest {

    private JsonSnapshotMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new JsonSnapshotMapper(objectMapper);
    }

    @Test
    void roundTripsPlainObject() {
        Map<String, Object> payload = Map.of("key", "value", "count", 2);

        String json = mapper.toJson(payload);
        Map<String, Object> restored = mapper.fromJson(json, new TypeReference<>() {});

        assertThat(restored).containsEntry("key", "value");
        assertThat(restored).containsEntry("count", 2);
    }

    @Test
    void returnsNullForBlankJson() {
        assertThat(mapper.fromJson("", Map.class)).isNull();
        assertThat(mapper.toJson(null)).isNull();
    }

    @Test
    void wrapsDeserializationFailure() {
        assertThatThrownBy(() -> mapper.fromJson("{invalid", Map.class))
                .isInstanceOf(PersistenceSerializationException.class);
    }

    private static final class Unserializable {
        @SuppressWarnings("unused")
        private final Object self = this;
    }
}
