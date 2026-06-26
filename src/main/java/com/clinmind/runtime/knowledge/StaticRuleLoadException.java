package com.clinmind.runtime.knowledge;

public class StaticRuleLoadException extends RuntimeException {

    public StaticRuleLoadException(String message) {
        super(message);
    }

    public StaticRuleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
