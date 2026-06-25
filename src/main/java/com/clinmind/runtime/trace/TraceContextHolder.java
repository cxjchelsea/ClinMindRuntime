package com.clinmind.runtime.trace;

public final class TraceContextHolder {

    private static final ThreadLocal<String> RUNTIME_ID = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    public static void setRuntimeId(String runtimeId) {
        RUNTIME_ID.set(runtimeId);
    }

    public static String getRuntimeId() {
        return RUNTIME_ID.get();
    }

    public static void clear() {
        RUNTIME_ID.remove();
    }
}
