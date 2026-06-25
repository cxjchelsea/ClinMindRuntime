package com.clinmind.runtime.state;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record UserInput(
        String text,
        List<String> attachments,
        Instant receivedAt
) {
    public UserInput {
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public UserInput(String text) {
        this(text, List.of(), Instant.now());
    }

    public UserInput(String text, List<String> attachments) {
        this(text, attachments, Instant.now());
    }
}
