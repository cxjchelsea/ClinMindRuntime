package com.clinmind.runtime.storage;

public class RuntimeNotFoundException extends RuntimeException {

    private final String runtimeId;

    public RuntimeNotFoundException(String runtimeId) {
        super("Runtime 不存在: " + runtimeId);
        this.runtimeId = runtimeId;
    }

    public String getRuntimeId() {
        return runtimeId;
    }
}
