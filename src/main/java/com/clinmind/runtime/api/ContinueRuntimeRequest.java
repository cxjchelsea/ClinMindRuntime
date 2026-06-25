package com.clinmind.runtime.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContinueRuntimeRequest(
        @JsonProperty("runtime_id") @NotBlank String runtimeId,
        @NotNull @Valid UserInputRequest input
) {
}
