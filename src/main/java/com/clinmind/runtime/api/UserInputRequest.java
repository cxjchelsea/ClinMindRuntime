package com.clinmind.runtime.api;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UserInputRequest(
        @NotBlank String text,
        List<String> attachments
) {
    public UserInputRequest {
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}
