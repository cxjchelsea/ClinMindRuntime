package com.clinmind.runtime.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.clinmind.runtime.state.RuntimeMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record StartRuntimeRequest(
        @JsonProperty("session_id") @NotBlank String sessionId,
        @JsonProperty("user_id") String userId,
        @NotNull RuntimeMode mode,
        @NotNull @Valid UserInputRequest input,
        @JsonProperty("basic_info") Map<String, Object> basicInfo
) {
}
