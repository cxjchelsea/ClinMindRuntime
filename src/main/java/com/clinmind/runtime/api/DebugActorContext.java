package com.clinmind.runtime.api;

public final class DebugActorContext {

    private static final ThreadLocal<String> ACTOR = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private DebugActorContext() {
    }

    public static void setActor(String actor) {
        ACTOR.set(actor);
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String getOrDefault() {
        String actor = ACTOR.get();
        return actor == null || actor.isBlank() ? "system-debug" : actor;
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void clear() {
        ACTOR.remove();
        REQUEST_ID.remove();
    }
}
